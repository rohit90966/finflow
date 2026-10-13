package com.rohit.finflow.controller;

import com.rohit.finflow.entity.User;
import com.rohit.finflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public ResponseEntity<?> getById(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        Long id=userService.findByEmail(email).getId();
        User user=userService.findById(id);
        if(user!=null){
            return new ResponseEntity<>(user,HttpStatus.OK);
        }
        return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody User user){
        if(userService.update(user)){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @DeleteMapping("")
    public void delete(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        Long id=userService.findByEmail(email).getId();
        userService.delete(id);
    }
}
