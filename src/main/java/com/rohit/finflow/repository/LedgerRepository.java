package com.rohit.finflow.repository;

import com.rohit.finflow.entity.Ledger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface LedgerRepository extends JpaRepository<Ledger,Long> {
    Page<Ledger> findByAccount_AccountNumber(String accountNumber,
                                             Pageable pageable);
}
