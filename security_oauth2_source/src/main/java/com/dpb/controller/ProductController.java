package com.dpb.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 22:34
 */
@RestController
public class ProductController {

    @RequestMapping("/findAll")
    public String findAll(){
        return "产品列表信息...";
    }
}
