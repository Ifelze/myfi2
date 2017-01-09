/**
 * 
 */
package com.ifelze.myfi.web.controller;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codahale.metrics.annotation.Timed;
import com.ifelze.myfi.domain.User;
import com.ifelze.myfi.service.UserService;
import com.ifelze.myfi.web.rest.vm.ManagedUserVM;

/**
 * @author nithya
 *
 */
@Controller
public class ProfileController {
	private final Logger log = LoggerFactory.getLogger(ProfileController.class);
	
	@Inject
    private UserService userService;
	
	@RequestMapping("profile_home")
    public String getHome(Model model){
        return "profile_home";
    }
    @RequestMapping("profile_settings")
    public String getSettings(Model model){
    	User user = userService.getUserWithAuthorities();
    	model.addAttribute("currUser", user);
        return "profile_settings";
    }
    @RequestMapping("profile_users")
    public String getUsers(Model model){
        return "profile_users";
    }
    @RequestMapping("profile_comments")
    public String getComments(Model model){
        return "profile_comments";
    }
    @RequestMapping("profile_me")
    public String getProfileMe(Model model){
        return "profile_me";
    }
    @RequestMapping("profile_history")
    public String getHistory(Model model){
        return "profile_history";
    }
    @RequestMapping("profile_projects")
    public String getProjects(Model model){
        return "profile_projects";
    }
}