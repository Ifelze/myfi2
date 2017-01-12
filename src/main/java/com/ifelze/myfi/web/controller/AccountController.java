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
    @RequestMapping("resend_activation_success")
    public String getResendActivationSuccessfulLink(Model model){
        return "resend_activation_success";
    }
    @RequestMapping("forgot_password")
    public String forgotPassword(Model model){
        return "forgot_password";
    }
    @RequestMapping("resend_activation_link")
	public String resendApplicaionLink(Model model) {
		model.addAttribute("resendActivationCommond", new ManagedUserVM());
		return "resend_activation_link";
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
						String baseUrl = request.getScheme() + // "http"
								"://" + // "://"
								request.getServerName() + // "myhost"
								":" + // ":"
								request.getServerPort() + // "80"
								request.getContextPath(); // "/myContextPath" or
															// "" if
															// deployed in root
															// context
						mailService.resendActivationEmail(users.get(), baseUrl);
						return "resend_activation_success";
					}
				}
			}
    	}
    	bindingResult.rejectValue("email", "", "please enter registerd e-mailid");
    	return "resend_activation_link";
	}
}