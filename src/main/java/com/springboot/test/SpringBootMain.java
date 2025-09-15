package com.springboot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.locks.LockSupport;

@SpringBootApplication
public class SpringBootMain {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBootMain.class, args);
    }
}