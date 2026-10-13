package com.rohit.finflow.kafka.event;

import com.rohit.finflow.enums.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransferEvent {

    private String userEmail;
    private String receiverEmail;

    private Long transactionId;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private TransactionStatus status;

    private String idempotencyKey;

    private LocalDateTime createdAt;
}