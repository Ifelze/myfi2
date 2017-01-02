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
    
    @GetMapping("registration")
    public String getRegister(Model model){
		model.addAttribute("registerCommand", new ManagedUserVM());
        return "registration";
    }
    @RequestMapping("login")
    public String getLogin(Model model){
        return "login";
    }
    @RequestMapping("forgot_password")
    public String forgotPassword(Model model){
        return "forgot_password";
    }
    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     */
    @PostMapping("/registration")
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
    		return "registration";
    	}
        User user = userService.createUser(managedUserVM.getLogin(), managedUserVM.getPassword(),
        managedUserVM.getFirstName(), managedUserVM.getLastName(), managedUserVM.getEmail().toLowerCase(),
        managedUserVM.getLangKey());
        if(user != null){
	           mailService.sendActivationEmail(user);
	       return "redirect:start";
        }
        
        return "registration";
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
}