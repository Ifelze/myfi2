/**
 * 
 */
package com.ifelze.myfi.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author nithya
 *
 */
@Controller
public class Tools {
	private final Logger log = LoggerFactory.getLogger(Tools.class);
	
    @RequestMapping("emi_calculator")
    public String getEmiCalculator(Model model){
        return "emi_calculator";
    }
    @RequestMapping("fd_calculator")
    public String getFDCalculator(Model model){
        return "fd_calculator";
    }
}