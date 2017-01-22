/**
 * 
 */
package com.ifelze.myfi.web.controller;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.ifelze.myfi.service.UserService;
import com.ifelze.myfi.web.rest.vm.ManagedUserVM;

/**
 * @author nithya
 *
 */
@RestController
public class ProfileAJAXController {
	private final Logger log = LoggerFactory.getLogger(ProfileAJAXController.class);
	
	@Inject
    private UserService userService;
	
    @PostMapping(path="edit_profile", 
    		produces={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
    		consumes={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Timed
    public ResponseEntity<?> editProfile(@RequestBody ManagedUserVM user){
    	log.info("edit_profile:" + user.toString());
    	userService.updateUser(user.getFirstName(), user.getLastName(), null);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping(path="edit_profile_cp", 
    		produces={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
    		consumes={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Timed
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> input){
    	log.info("edit_profile_cp:" + input);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}