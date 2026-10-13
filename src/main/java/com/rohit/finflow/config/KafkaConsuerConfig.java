package com.rohit.finflow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaConsuerConfig {
    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Bean
    public DefaultErrorHandler errorHandler(){
        DeadLetterPublishingRecoverer recoverer=new DeadLetterPublishingRecoverer(kafkaTemplate);
        FixedBackOff fixedBackOff=new FixedBackOff(2000L,3L);
        DefaultErrorHandler defaultErrorHandler=new DefaultErrorHandler(recoverer,fixedBackOff);

        return defaultErrorHandler;
    }
}
