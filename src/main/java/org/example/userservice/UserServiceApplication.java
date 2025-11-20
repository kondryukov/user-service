package org.example.userservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "org.example.userservice")
@EntityScan(basePackages = "org.example.userservice.domain")
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner printKafkaProps(KafkaProperties props) {
        return args -> {
            System.out.println("=== Effective producer props ===");
            props.buildProducerProperties()
                    .forEach((k, v) -> System.out.println(k + " = " + v));
        };
    }
}
