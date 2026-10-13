package com.rohit.finflow.controller;

import com.rohit.finflow.entity.Account;
import com.rohit.finflow.entity.User;
import com.rohit.finflow.service.AccountService;
import com.rohit.finflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?>getAll(){
       Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
       String email=authentication.getName();

       List<Account>a=accountService.getAllAccounts(email);
       if(a!=null){
           return new ResponseEntity<>(a,HttpStatus.OK);
       }
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping()
    public ResponseEntity<?> createAccount(@RequestBody Account account) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        Long id=userService.findByEmail(email).getId();

        User user = userService.findById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        account.setUser(user);
        user.getBankAccounts().add(account);
        accountService.save(account);
        return ResponseEntity.ok(account);
    }

    @GetMapping("{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findByEmail(email);

        Account account=accountService.findByAccountNumberAndUser_Id(accountNumber,user.getId());
        if(account!=null){
            return new ResponseEntity<>(account,HttpStatus.OK);
        }
         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PutMapping("{accountId}")
    public ResponseEntity<?> update(@PathVariable Long accountId,@RequestBody Account account){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findByEmail(email);
        List<Account>a=user.getBankAccounts();
        Account accountById=accountService.getById(accountId);

        for(Account account1:a){
            if(account1.getId()==accountId){
                accountById.setAccountNumber(account.getAccountNumber());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/{accountId}/check-balance")
    public ResponseEntity<?> checkBalance(@PathVariable Long accountId)
            throws AccessDeniedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());

        Account account = accountService.findByIdAndUserId(accountId, user.getId());
        if (account != null) {
            return new ResponseEntity<>(account.getBalance(),HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}

