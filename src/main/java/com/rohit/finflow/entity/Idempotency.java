package com.rohit.finflow.entity;

import com.rohit.finflow.enums.TransactionStatus;
import com.rohit.finflow.service.TransactionService;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency")
@Getter
@Setter
public class Idempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
