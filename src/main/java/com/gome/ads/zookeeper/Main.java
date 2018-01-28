package com.gome.ads.zookeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Administrator
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.gome.ads.zookeeper"})
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}

