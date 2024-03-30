package com.example.defaultdslstorebug;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka
class DefaultDslStoreBugApplicationIT {

    private static final String TOPIC_NAME = "foo";
    private static Path stateStoreDir;

    @BeforeAll
    public static void setup() throws IOException {
        stateStoreDir = Files.createTempDirectory(DefaultDslStoreBugApplicationIT.class.getSimpleName());
    }

    @Test
    void testStateStoreIsInMemory() {
        assertThat(stateStoreDir).isEmptyDirectory();
    }

    @TestConfiguration
    @EnableKafkaStreams
    static class KafkaStreamsConfig {

        @Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
        private String brokerAddresses;

        @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
        public KafkaStreamsConfiguration kStreamsConfigWithInMemoryStateStores() {
            Map<String, Object> props = new HashMap<>();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "should-be-stored-in-memory");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddresses);
            props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
            props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
            props.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreDir.toString());

            // Property introduced with KIP-591 (Kafka 3.2) -- does not work
            props.put(StreamsConfig.DEFAULT_DSL_STORE_CONFIG, "in_memory");
            // Property introduced with KIP-954 (Kafka 3.7) -- does work
//             props.put(StreamsConfig.DSL_STORE_SUPPLIERS_CLASS_CONFIG, BuiltInDslStoreSuppliers.InMemoryDslStoreSuppliers.class);
            return new KafkaStreamsConfiguration(props);
        }

        @Bean
        public GlobalKTable<?, ?> globalKTable(StreamsBuilder builder) {
            return builder.globalTable(TOPIC_NAME);
        }

        @Bean
        public NewTopic newTopic() {
            return TopicBuilder.name(TOPIC_NAME)
                    .partitions(1)
                    .build();
        }
    }
}
