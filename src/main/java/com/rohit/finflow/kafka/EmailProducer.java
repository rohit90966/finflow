package com.rohit.finflow.kafka;

import com.rohit.finflow.kafka.event.EmailEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer {

    @Autowired
    private KafkaTemplate<String, EmailEvent> kafkaTemplate;

    public void publishEmailEvent(EmailEvent emailEvent){
        kafkaTemplate.send(
                "email-event",
                emailEvent
        ).addCallback(

                result -> {
                    System.out.println("=========== SUCCESS ===========");
                    System.out.println(result.getRecordMetadata());
                },

                ex -> {
                    System.out.println("=========== FAILED ===========");
                    ex.printStackTrace();
                }

        );
    }
}
