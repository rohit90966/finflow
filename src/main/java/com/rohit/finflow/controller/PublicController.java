package com.rohit.finflow.controller;

import com.rohit.finflow.entity.User;
import com.rohit.finflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("public")
public class PublicController {

    @Autowired
    private UserService userService;
    @PostMapping("create-user")
    public ResponseEntity<HttpStatus> createUser(@RequestBody User user){
       try{
           userService.createUser(user);
           return new ResponseEntity<>(HttpStatus.NO_CONTENT);

       }
       catch (NullPointerException e){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(e.getMessage()));
       }
    }
}
