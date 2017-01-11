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
public class HomeController {
	@RequestMapping("start")
    public String getIndex(Model model){
        return "start";
    }
    @RequestMapping("terms_conditions")
    public String getTermsConditions(Model model){
        return "terms_conditions";
    }
    @RequestMapping("401")
    public String getTermsConditions(){
        return "401";
    }
}
