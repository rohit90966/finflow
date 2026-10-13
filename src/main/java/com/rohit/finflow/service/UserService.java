package com.rohit.finflow.service;

import com.rohit.finflow.entity.Account;
import com.rohit.finflow.entity.Transaction;
import com.rohit.finflow.entity.User;
import com.rohit.finflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;
    BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
    public void createUser(User user){
        if(user.getPassword()==null){
            throw new NullPointerException("Password is Empty");
        }
        String password= user.getPassword();
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean update(User user) {

        if(user == null || user.getId() == null){
            return false;
        }

        User userById = userRepository.findById(user.getId())
                .orElse(null);

        if(userById == null){
            return false;
        }

        userById.setName(user.getName());
        userById.setEmail(user.getEmail());
        userById.setPassword(user.getPassword());

        userRepository.save(userById);

        return true;
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        User user=userRepository.findByEmail(email);
        return user;
    }


}
