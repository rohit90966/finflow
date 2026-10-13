package com.rohit.finflow.kafka;

import com.rohit.finflow.kafka.event.TransferEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransferProducer {

    @Autowired
    private KafkaTemplate<String,TransferEvent> kafkaTemplate;

    public void publishTransferEvent(TransferEvent event) {

        kafkaTemplate.send(
                "transfer-event",
                event.getSenderAccountNumber(),
                event
        );
    }

}
