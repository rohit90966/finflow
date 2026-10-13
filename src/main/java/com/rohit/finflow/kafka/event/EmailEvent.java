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
public class EmailEvent {

    private String senderEmail;
    private String receiverEmail;

    private String senderAccountNumber;
    private String receiverAccountNumber;

    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime time;

    private BigDecimal senderRemainingBalance;
    private BigDecimal receiverCurrentBalance;
}