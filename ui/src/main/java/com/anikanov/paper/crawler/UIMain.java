package com.anikanov.paper.crawler;

import com.anikanov.paper.crawler.config.GlobalConstants;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class UIMain {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            GlobalConstants.setJarPath(args[0]);
        }
        System.out.println(args);
        GlobalConstants.refreshOutputDir();
        Application.launch(JavaFXApplication.class, args);
    }
}