package com.rohit.finflow.repository;

import com.rohit.finflow.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findBySenderAccount_Id(Long accountId);

    Page<Transaction> findBySenderAccount_IdOrReceiverAccount_Id(Long senderAccountId, Long receiverAccountId, Pageable pageable);

}

