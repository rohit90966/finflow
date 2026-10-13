package com.rohit.finflow.service;

import com.rohit.finflow.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User byUserName=userService.findByEmail(email);

        if(byUserName!=null){
            UserDetails user=org.springframework.security.core.userdetails.User.builder()
                    .username(byUserName.getEmail())
                    .password(byUserName.getPassword())
                    .roles(byUserName.getRole()).build();
            return user;
        }
        throw new UsernameNotFoundException("User not found"+ email);
    }
}
