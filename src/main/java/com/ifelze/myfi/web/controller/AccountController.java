package com.ifelze.myfi.web.controller;

import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.codahale.metrics.annotation.Timed;
import com.ifelze.myfi.domain.User;
import com.ifelze.myfi.repository.UserRepository;
import com.ifelze.myfi.service.MailService;
import com.ifelze.myfi.service.UserService;
import com.ifelze.myfi.web.rest.vm.ManagedUserVM;

/**
 * controller for managing the current user's account.
 */
@Controller
public class AccountController {
	
    private final Logger log = LoggerFactory.getLogger(AccountController.class);
    
    @Inject
    private MailService mailService;
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserRepository userRepository;
    
    @GetMapping("register")
    public String getRegister(Model model){
		model.addAttribute("registerCommand", new ManagedUserVM());
        return "register";
    }
    @RequestMapping("login")
    public String getLogin(Model model){
        return "login";
    }
    @RequestMapping("forgot_password1")
    public String forgotPassword(Model model){
    	model.addAttribute("forgotCommand", new ManagedUserVM());
        return "forgot_password1";
    }
    @RequestMapping("reset_password")
    public String getResetPassword(Model model, HttpServletRequest request){
    	model.addAttribute("resetCommand", new ManagedUserVM());
    	String key = request.getParameter("key");
    	if(!"".equals(key)){
    		Optional<User> user = userRepository.findOneByResetKey(key);
    		if(user == null || !user.isPresent()){
    			//bindingResult.reject("Either key is expired or Invalid try.");
    			log.error("Either key is expired or Invalid try.");
    			return "reset_password_error";
    		}
    	}
    	return "reset_password";
    }
    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     */
    @PostMapping("/register")
    @Timed
    public String registerUser(@ModelAttribute("registerCommand") @Validated ManagedUserVM managedUserVM, 
    		BindingResult bindingResult) {
    	log.debug("userRepository:" + userRepository);
    	log.debug(managedUserVM.toString());
    	Optional<User> users = userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase());
    	if(users != null && users.isPresent()){
    		bindingResult.rejectValue("login", "", "login already in use");
    	}else{
    		users = userRepository.findOneByEmail(managedUserVM.getEmail());
    		if(users != null && users.isPresent()){
    			bindingResult.rejectValue("email", "", "e-mail address already in use");
    		}
    	}
    	if(bindingResult.hasErrors()){
    		return "register";
    	}
        User user = userService.createUser(managedUserVM.getLogin(), managedUserVM.getPassword(),
        managedUserVM.getFirstName(), managedUserVM.getLastName(), managedUserVM.getEmail().toLowerCase(),
        managedUserVM.getLangKey());
        if(user != null){
	           mailService.sendActivationEmail(user);
	       return "redirect:start";
        }
        
        return "register";
    }
    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public String activateAccount(@RequestParam(value = "key") String key, Model model) {
    	Optional<User> users = userService.activateRegistration(key);
    	if(users != null && users.isPresent()){
    		model.addAttribute("activated", true);
    	}else{
    		model.addAttribute("activated", false);
    	}
    	return "activate";
    }

	@PostMapping("/forgot_password1")
	@Timed
	public String forgotPassword(@ModelAttribute("forgotCommand") @Validated ManagedUserVM managedUserVM,
			BindingResult bindingResult, HttpServletRequest request) 
	{
		
		String emailid = request.getParameter("email");
		String existingEmailid;
	  
		Optional<User> users = userRepository.findOneByEmail(managedUserVM.getEmail());
	
		if (users != null && users.isPresent()) 
		{
			User user = users.get();
			existingEmailid = user.getEmail();
			if (emailid.equals(existingEmailid))
			{
				String resetKey = user.getResetKey();
				if(resetKey == null)
				{
					Optional<User> user2 = userService.requestPasswordReset(managedUserVM.getEmail());
					if (user2 != null) 
					{
						String baseUrl = request.getScheme() + // "http"
								"://" + // "://"
								request.getServerName() + // "myhost"
								":" + // ":"
								request.getServerPort() + // "80"
								request.getContextPath(); // "/myContextPath" or ""
						// if
						// deployed in root
						// context
						mailService.sendForgotPasswordEmail(user2.get(), baseUrl);
						return "mailsent_sucess";
					}
				}
			}
		}
		bindingResult.rejectValue("email", "", "please enter valid registered email id");
		return "forgot_password1";
	}
	
	/**
	 * This method will reset user password based on resetPasswordKey stored in the session.
	 * The resetPasswordKey might be sent from user email.
	 * The resetPasswordKey will be cleared after the password reset success.
	 * @param managedUserVM This is User DTO. This has password and confirmPassword fields to capture from front end.
	 * @param bindingResult This is used to add errors.
	 * @param request This is request from password_reset page.
	 * @return This method returns either resetPassword_success if success or reset_password if error.
	 */
	@PostMapping("/reset_password")
	@Timed
	public String resetPassword(@ModelAttribute("resetCommand") @Validated ManagedUserVM managedUserVM,
			BindingResult bindingResult, HttpServletRequest request) {
			        
		    String resetKey = request.getParameter("resetPasswordKey");
		    if(!"".equals(resetKey)){
				Optional<User> user = userRepository.findOneByResetKey(resetKey);
				if(user!=null) {
					userService.completePasswordReset(managedUserVM.getPassword(), resetKey);
					return "resetPassword_success";				
				}
		    }
			return "reset_password";				
	}
}

