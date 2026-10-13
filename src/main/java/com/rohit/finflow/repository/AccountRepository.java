package com.rohit.finflow.repository;

import com.rohit.finflow.entity.Account;
import com.rohit.finflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    Account getById(Long id);

    Account findByIdAndUser_Id(Long accountId, Long userId);

    Account findByAccountNumber(String senderAccountNumber);
    Account findByAccountNumberAndUser_Id(String accountNumber, Long userId);
    List<Account> findByUser_id(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Account findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
