package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@MapperScan("com.example.mapper")
@SpringBootApplication
public class CommunityApplication {

    @PostConstruct
    public void init(){
//        解决redis和elasticsearch的冲突问题
//        netty4Utils
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
