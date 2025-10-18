    package com.zerooneblog.api.infrastructure.controller;

    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    public class HomeController {

        @GetMapping("/home")
        public String sayHello() {
            return "Hello, World!\nWelcom to 01 blog";
        }
    }