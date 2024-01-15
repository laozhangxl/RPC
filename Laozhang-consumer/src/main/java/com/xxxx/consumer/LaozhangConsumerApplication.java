package com.xxxx.consumer;

import com.xxxx.rpc.annotation.EnableConsumerRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConsumerRpc
public class LaozhangConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaozhangConsumerApplication.class, args);
        System.out.println("启动成功...");
    }

}
