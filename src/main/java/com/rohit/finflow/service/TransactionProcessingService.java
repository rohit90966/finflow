package com.rohit.finflow.service;

import com.rohit.finflow.entity.Account;
import com.rohit.finflow.entity.Idempotency;
import com.rohit.finflow.entity.Ledger;
import com.rohit.finflow.entity.Transaction;
import com.rohit.finflow.enums.EntryType;
import com.rohit.finflow.enums.TransactionStatus;
import com.rohit.finflow.kafka.event.EmailEvent;
import com.rohit.finflow.kafka.event.TransferEvent;
import com.rohit.finflow.repository.AccountRepository;
import com.rohit.finflow.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionProcessingService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private LedgerService ledgerService;


    @Transactional
    public EmailEvent processTransfer(
            TransferEvent transferEvent,
            Transaction transaction,
            Idempotency idempotency
    ) {

        String senderAccNo =
                transferEvent.getSenderAccountNumber();

        String receiverAccNo =
                transferEvent.getReceiverAccountNumber();


        Account senderAccount;
        Account receiverAccount;


        if (senderAccNo.compareTo(receiverAccNo) < 0) {

            senderAccount =
                    accountRepository
                            .findByAccountNumberForUpdate(
                                    senderAccNo
                            );

            receiverAccount =
                    accountRepository
                            .findByAccountNumberForUpdate(
                                    receiverAccNo
                            );

        } else {

            receiverAccount =
                    accountRepository
                            .findByAccountNumberForUpdate(
                                    receiverAccNo
                            );

            senderAccount =
                    accountRepository
                            .findByAccountNumberForUpdate(
                                    senderAccNo
                            );
        }


        if (senderAccount == null) {
            throw new IllegalArgumentException(
                    "Sender account not found"
            );
        }

        if (receiverAccount == null) {
            throw new IllegalArgumentException(
                    "Receiver account not found"
            );
        }


        BigDecimal amount =
                transferEvent.getAmount();


        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount should be greater than 0"
            );
        }


        if (amount.compareTo(
                senderAccount.getBalance()
        ) > 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }


        senderAccount.setBalance(
                senderAccount
                        .getBalance()
                        .subtract(amount)
        );

        receiverAccount.setBalance(
                receiverAccount
                        .getBalance()
                        .add(amount)
        );


        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        idempotency.setTransactionStatus(
                TransactionStatus.SUCCESS
        );

        idempotencyService.save(idempotency);


        transaction.setStatus(
                TransactionStatus.SUCCESS
        );

        transactionRepository.save(transaction);

        Ledger ledgerSender = new Ledger();

        ledgerSender.setAccount(senderAccount);

        ledgerSender.setTransaction(transaction);

        ledgerSender.setEntryType(
                EntryType.DEBIT
        );

        ledgerSender.setAmount(amount);

        ledgerSender.setBalanceAfter(
                senderAccount.getBalance()
        );

        ledgerSender.setCreatedAt(
                transaction.getCreatedAt()
        );

        ledgerSender.setRemark(
                "Transfer to "
                        + receiverAccount.getAccountNumber()
        );

        Ledger ledgerReceiver = new Ledger();

        ledgerReceiver.setAccount(receiverAccount);

        ledgerReceiver.setTransaction(transaction);

        ledgerReceiver.setEntryType(
                EntryType.CREDIT
        );

        ledgerReceiver.setAmount(amount);

        ledgerReceiver.setBalanceAfter(
                receiverAccount.getBalance()
        );

        ledgerReceiver.setCreatedAt(
                transaction.getCreatedAt()
        );

        ledgerReceiver.setRemark(
                "Received from "
                        + senderAccount.getAccountNumber()
        );


        ledgerService.save(ledgerReceiver);
        ledgerService.save(ledgerSender);

        EmailEvent emailEvent = new EmailEvent();

        emailEvent.setSenderEmail(
                transferEvent.getUserEmail()
        );

        emailEvent.setReceiverEmail(
                transferEvent.getReceiverEmail()
        );

        emailEvent.setReceiverAccountNumber(
                transferEvent.getReceiverAccountNumber()
        );

        emailEvent.setSenderAccountNumber(
                transferEvent.getSenderAccountNumber()
        );

        emailEvent.setStatus(
                TransactionStatus.SUCCESS
        );

        emailEvent.setAmount(
                transferEvent.getAmount()
        );

        emailEvent.setTime(
                transaction.getCreatedAt()
        );

        emailEvent.setReceiverCurrentBalance(
                receiverAccount.getBalance()
        );

        emailEvent.setSenderRemainingBalance(
                senderAccount.getBalance()
        );


        return emailEvent;
    }
}