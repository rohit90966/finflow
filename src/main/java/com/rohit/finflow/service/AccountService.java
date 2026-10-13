package com.rohit.finflow.service;

import com.rohit.finflow.entity.Account;
import com.rohit.finflow.entity.User;
import com.rohit.finflow.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public List<Account> getAccount(Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return null;
        }

        return user.getBankAccounts();
    }

    public Account getById(Long accountId) {
        return accountRepository.getById(accountId);
    }

    public Account findByIdAndUserId(Long accountId, Long id) {
        return accountRepository.findByIdAndUser_Id(accountId,id);
    }

    public Account findByAccountNumber(String senderAccountNumber) {
        return accountRepository.findByAccountNumber(senderAccountNumber);
    }

    public Account findByAccountNumberAndUser_Id(String accountNumber, Long id) {
        return accountRepository.findByAccountNumberAndUser_Id(accountNumber, id);
    }

    public List<Account> getAllAccounts(String email){
        User user=userService.findByEmail(email);
        List<Account> a=accountRepository.findByUser_id(user.getId());
        return a;
    }
}
