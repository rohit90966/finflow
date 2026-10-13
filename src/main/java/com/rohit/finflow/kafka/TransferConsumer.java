package com.rohit.finflow.kafka;

import com.rohit.finflow.entity.Idempotency;
import com.rohit.finflow.entity.Transaction;
import com.rohit.finflow.enums.TransactionStatus;
import com.rohit.finflow.kafka.event.EmailEvent;
import com.rohit.finflow.kafka.event.TransferEvent;
import com.rohit.finflow.service.IdempotencyService;
import com.rohit.finflow.service.TransactionProcessingService;
import com.rohit.finflow.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransferConsumer {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private TransactionProcessingService transactionProcessingService;

    @Autowired
    private EmailProducer emailProducer;


    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000)
    )
    @KafkaListener(
            topics = "transfer-event",
            concurrency = "4",
            groupId = "finflow-group"
    )
    public void consumeTransfer(TransferEvent transferEvent) throws Exception {

        Transaction transaction =
                transactionService
                        .findById(transferEvent.getTransactionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Transaction not found")
                        );

        Idempotency idempotency =
                idempotencyService.findByIdempotencyKey(
                        transferEvent.getIdempotencyKey()
                );

        if (idempotency == null) {
            throw new IllegalStateException(
                    "Idempotency record missing"
            );
        }

        // Already successfully processed
        if (idempotency.getTransactionStatus()
                == TransactionStatus.SUCCESS) {
            return;
        }


        /*
         * DB ROW-LEVEL LOCKING happens inside
         * transactionProcessingService.processTransfer()
         */
        EmailEvent finalEmailEvent =
                transactionProcessingService.processTransfer(
                        transferEvent,
                        transaction,
                        idempotency
                );


        /*
         * Email is outside the DB transaction.
         */
//        if (finalEmailEvent != null) {
//            emailProducer.publishEmailEvent(finalEmailEvent);
//        }
    }


    @DltHandler
    public void DltHandler() {
        log.error("Transfer_Event moved to DLT");
    }
}