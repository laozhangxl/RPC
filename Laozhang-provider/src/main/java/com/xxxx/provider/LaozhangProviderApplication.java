package com.xxxx.provider;

import com.xxxx.rpc.annotation.EnableProviderRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProviderRpc
public class LaozhangProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaozhangProviderApplication.class, args);
    }

}
