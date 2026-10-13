package com.rohit.finflow.controller;

import com.rohit.finflow.dto.LedgerResponse;
import com.rohit.finflow.entity.Ledger;
import com.rohit.finflow.service.AccountService;
import com.rohit.finflow.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("Admin")
public class AdminController {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountService accountService;

    @GetMapping("{accountNumber}")
    public List<LedgerResponse> getById(@PathVariable String accountNumber,
                                @RequestParam(defaultValue = "0",required = false) int pageNo,
                                @RequestParam(defaultValue = "5",required = false) int pageSize,
                                @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
                                @RequestParam(defaultValue = "DESC",required = false) String order
    ){
        Sort sort;

        if (order.equalsIgnoreCase("DESC")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        Pageable pageable= PageRequest.of(pageNo,pageSize,sort);
        LedgerResponse ledgerResponse=new LedgerResponse();
        List<LedgerResponse>ledgerResponseList=new ArrayList<>();
        List<Ledger> a=ledgerService.getByAccount_Number(accountNumber,pageable).getContent();
        for(Ledger ledger :a){
            ledgerResponse.setAccountNumber(ledger.getAccount().getAccountNumber());
            ledgerResponse.setTransactionId(ledger.getTransaction().getId());
            ledgerResponse.setEntryType(ledger.getEntryType());
            ledgerResponse.setAmount(ledger.getAmount());
            ledgerResponse.setBalanceAfter(ledger.getBalanceAfter());
            ledgerResponse.setCreatedAt(ledger.getCreatedAt());
            ledgerResponse.setRemark(ledger.getRemark());
            ledgerResponseList.add(ledgerResponse);
        }
        return ledgerResponseList;
    }
}
