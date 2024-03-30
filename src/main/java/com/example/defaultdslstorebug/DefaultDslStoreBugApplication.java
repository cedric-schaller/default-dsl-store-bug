package com.example.defaultdslstorebug;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

import java.util.function.Consumer;

@SpringBootApplication
public class DefaultDslStoreBugApplication {

    public static final String SOME_TOPIC_NAME = "some-topic";

    public static void main(String[] args) {
        SpringApplication.run(DefaultDslStoreBugApplication.class, args);
    }

    @Bean
    public Consumer<GlobalKTable<String, String>> createStateStore() {
        return globalKTable -> {
            // no op
        };
    }

    @Bean
    public NewTopic pendingTransfer() {
        return TopicBuilder.name(SOME_TOPIC_NAME)
                .partitions(12)
                .build();
    }

}
