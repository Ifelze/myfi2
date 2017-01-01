/**
 * 
 */
package com.ifelze.myfi.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author nithya
 *
 */
@Controller
public class ProfileController {
	@RequestMapping("profile_home")
    public String getHome(Model model){
        return "profile_home";
    }
    @RequestMapping("profile_settings")
    public String getSettings(Model model){
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
}