package com.anikanov.paper.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class PaperCrawlerMain {
    public static void main(String[] args) {
        SpringApplication.run(PaperCrawlerMain.class, args);
    }
}