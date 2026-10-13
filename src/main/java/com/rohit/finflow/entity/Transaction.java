package com.rohit.finflow.entity;

import com.rohit.finflow.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account senderAccount;

    @ManyToOne
    private Account receiverAccount;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;

}
