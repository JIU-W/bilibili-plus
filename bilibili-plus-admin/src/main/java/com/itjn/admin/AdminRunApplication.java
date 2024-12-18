package com.itjn.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.itjn"})
@MapperScan(basePackages = {"com.itjn.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class AdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminRunApplication.class, args);
    }

}
