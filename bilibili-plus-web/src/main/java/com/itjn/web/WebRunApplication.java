package com.itjn.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.itjn"})
@MapperScan("com.itjn.mappers")
@EnableTransactionManagement
@EnableScheduling
public class WebRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebRunApplication.class, args);
    }

}
