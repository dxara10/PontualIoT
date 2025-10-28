package com.pontualiot.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "API funcionando!";
    }

    @GetMapping("/")
    public String root() {
        return "PontualIoT API - Funcionando!";
    }
}