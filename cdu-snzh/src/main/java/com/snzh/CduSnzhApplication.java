package com.snzh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.snzh.mapper")
public class CduSnzhApplication {

    public static void main(String[] args) {
        SpringApplication.run(CduSnzhApplication.class, args);
    }

}
