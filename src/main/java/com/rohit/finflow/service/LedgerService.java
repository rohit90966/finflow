package com.rohit.finflow.service;

import com.rohit.finflow.entity.Ledger;
import com.rohit.finflow.repository.LedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LedgerService {

    @Autowired
    private LedgerRepository ledgerRepository;


    public void save(Ledger ledger) {
        ledgerRepository.save(ledger);
    }

    public Page<Ledger> getByAccount_Number(String accountNumber, Pageable pageable) {

        return ledgerRepository.findByAccount_AccountNumber(accountNumber,pageable);
    }

}
