package com.dpb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 22:33
 */
@SpringBootApplication
@MapperScan("com.dpb.mapper")
public class OAuthSourceApp {

    public static void main(String[] args) {
        SpringApplication.run(OAuthSourceApp.class,args);
    }
}
