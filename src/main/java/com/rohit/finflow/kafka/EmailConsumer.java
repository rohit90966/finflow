package com.rohit.finflow.kafka;

import com.rohit.finflow.kafka.event.EmailEvent;
import com.rohit.finflow.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailConsumer {

    @Autowired
    private EmailService emailService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(3000)
    )
    @KafkaListener(topics = "email-event", groupId = "email-group",concurrency = "3")
    public void consumeEmail(EmailEvent event) {

        String senderSubject = "FinFlow - Debit Transaction Successful";

        String senderBody =
                "Dear Customer,\n\n" +
                        "Your transfer has been completed successfully.\n\n" +
                        "Transaction Details:\n" +
                        "----------------------------------------\n" +
                        "Status             : " + event.getStatus() + "\n" +
                        "Transferred Amount : ₹" + event.getAmount() + "\n" +
                        "From Account       : " + event.getSenderAccountNumber() + "\n" +
                        "To Account         : " + event.getReceiverAccountNumber() + "\n" +
                        "Remaining Balance  : ₹" + event.getSenderRemainingBalance() + "\n" +
                        "Transaction Time   : " + event.getTime() + "\n" +
                        "----------------------------------------\n\n" +
                        "Thank you for banking with FinFlow.\n\n" +
                        "Regards,\n" +
                        "FinFlow Team";

        emailService.sendMail(
                event.getSenderEmail(),
                senderBody,
                senderSubject
        );

        // ================= Receiver Email =================

        String receiverSubject = "FinFlow - Money Credited Successfully";

        String receiverBody =
                "Dear Customer,\n\n" +
                        "An amount has been credited to your account.\n\n" +
                        "Transaction Details:\n" +
                        "----------------------------------------\n" +
                        "Status             : " + event.getStatus() + "\n" +
                        "Received Amount    : ₹" + event.getAmount() + "\n" +
                        "From Account       : " + event.getSenderAccountNumber() + "\n" +
                        "To Account         : " + event.getReceiverAccountNumber() + "\n" +
                        "Current Balance    : ₹" + event.getReceiverCurrentBalance() + "\n" +
                        "Transaction Time   : " + event.getTime() + "\n" +
                        "----------------------------------------\n\n" +
                        "Thank you for banking with FinFlow.\n\n" +
                        "Regards,\n" +
                        "FinFlow Team";

        emailService.sendMail(
                event.getReceiverEmail(),
                receiverBody,
                receiverSubject
        );
    }
    @DltHandler
    public void dltHandler(){
        log.error("Email event moved to DLT");
    }
}