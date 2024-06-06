package com.itgr.zhaojCodeSandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ygking
 */
@RestController("/")
public class TestController {

    @GetMapping("/health")
    public String healthCheck(){
        return "ok";
    }
}
