package com.rohit.finflow.repository;

import com.rohit.finflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface UserRepository extends JpaRepository<User,Long> {

    User findByEmail(String email);
    @Query("SELECT a.user FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<User> findByAccountNumber(@Param("accountNumber") String accountNumber);
}
