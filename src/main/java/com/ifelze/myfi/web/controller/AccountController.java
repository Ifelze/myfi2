package com.ifelze.myfi.web.controller;

import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.ifelze.myfi.config.JHipsterProperties;
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
    private JHipsterProperties jHipsterProperties;
    
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
    @RequestMapping("register_success")
    public String registerSuccess(){
        return "register_success";
    }
    @RequestMapping("forgot_password")
    public String forgotPassword(Model model){
    	model.addAttribute("forgotPasswordCmd", new ManagedUserVM());
        return "forgot_password";
    }
    @RequestMapping("reset_password")
    public String getResetPassword(Model model, HttpServletRequest request){
    	String key = request.getParameter("key");
    	if(key == null){
    		key = (String)request.getSession().getAttribute("resetPasswordKey");
    	}
    	if(!"".equals(key)){
    		request.getSession().setAttribute("resetPasswordKey", key);
    		Optional<User> user = userRepository.findOneByResetKey(key);
    		if(user == null || !user.isPresent()){
    			log.error("Either key is expired or Invalid try.");
    			return "reset_password_error";
    		}
    	}
    	model.addAttribute("resetCommand", new ManagedUserVM());
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
	       return "redirect:register_success";
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

	@PostMapping("/forgot_password")
	@Timed
	public String forgotPassword(@ModelAttribute("forgotPasswordCmd") @Validated ManagedUserVM managedUserVM,
			BindingResult bindingResult, HttpServletRequest request) 
	{
		
		Optional<User> users = userRepository.findOneByEmail(managedUserVM.getEmail());
	
		if (users != null && users.isPresent()) 
		{
			User user = users.get();
			String resetKey = user.getResetKey();
			if(resetKey == null)
			{
				Optional<User> updatedUser = userService.requestPasswordReset(managedUserVM.getEmail());
				if(updatedUser != null && updatedUser.isPresent()){
					user = updatedUser.get();
				}
			}
			mailService.sendForgotPasswordEmail(user, jHipsterProperties.getMail().getBaseUrl());
			return "forgot_password_success";
		}
		bindingResult.rejectValue("email", "", "please enter valid registered email id");
		return "forgot_password";
	}
	@RequestMapping("resend_activation_success")
    public String getResendActivationSuccessfulLink(Model model){
        return "resend_activation_success";
    }
	@RequestMapping("resend_activation_link")
	public String resendApplicaionLink(Model model) {
		model.addAttribute("resendActivationCommond", new ManagedUserVM());
		return "resend_activation_link";
	}
	@PostMapping("/resend_activation_link")
    @Timed
    public String resendActivationKey(@ModelAttribute("resendActivationCommond") @Validated ManagedUserVM managedUserVM, 
    		BindingResult bindingResult, HttpServletRequest request )  
	{	
    	String emailid=request.getParameter("email");
    	log.debug("userRepository:" + userRepository);
    	log.debug(managedUserVM.toString());
		Optional<User> users = userRepository.findOneByEmail(managedUserVM.getEmail());
		if (users != null && users.isPresent())
    	{
   		    User user=users.get();   		
   		    String existingEmailid=user.getEmail();
			if (emailid.equals(existingEmailid))
			{
				String activationKey = user.getActivationKey();
				if (activationKey == null)
				{
			    	bindingResult.rejectValue("email", "", "This user is already activated. Please enter valid email");
					return "resend_activation_link";
				} else {
					if (users != null) {
						mailService.resendActivationEmail(users.get(), jHipsterProperties.getMail().getBaseUrl());
						return "resend_activation_success";
					}
				}
			}
    	}
    	bindingResult.rejectValue("email", "", "please enter registerd e-mailid");
    	return "resend_activation_link";
	}
	/**
	 * This method will reset user password based on resetPasswordKey stored in the session.
	 * The resetPasswordKey might be sent from user email.
	 * The resetPasswordKey will be cleared after the password reset success.
	 * @param bindingResult This is used to add errors.
	 * @param request This is request from password_reset page.
	 * @return This method returns either resetPassword_success if success or reset_password if error.
	 */
	@PostMapping("/reset_password")
	@Timed
	public String resetPassword(@ModelAttribute("resetCommand") @Validated ManagedUserVM managedUserVM,
			BindingResult bindingResult, HttpServletRequest request) {
			        
		    String resetKey = (String)request.getSession().getAttribute("resetPasswordKey");
		    if(!"".equals(resetKey)){
				Optional<User> user = userRepository.findOneByResetKey(resetKey);
				if(user != null) {
					String password = request.getParameter("password");
					String confirmPassword = request.getParameter("confirmPassword");
					if(password == null || "".equals(password.trim())){
						bindingResult.reject("password", "Please enter password");
					}
					if(confirmPassword == null || "".equals(confirmPassword.trim())){
						bindingResult.reject("confirmPassword", "Please enter Confirm password");
					}
					if(!bindingResult.hasErrors() && !confirmPassword.equals(password)){
						bindingResult.reject("password", "Password and confirm password does not match");
					}
					if(!bindingResult.hasErrors()){
						userService.completePasswordReset(password, resetKey);
						request.getSession().removeAttribute("resetPasswordKey");
						return "resetPassword_success";
					}
				}
		    }
			return "reset_password";				
	}
}

