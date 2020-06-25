package com.github.controller;

import com.github.service.FoobarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hangs.zhang
 * @date 2020/06/25 13:22
 * *****************
 * function:
 */
@RestController
@RequestMapping("/foobar")
public class FoobarController {

    @Autowired
    private FoobarService foobarService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/error")
    public String error() {
        throw new RuntimeException("my error");
    }

    @GetMapping("/param")
    public String param(@RequestParam String username, @RequestParam Integer id) {
        System.out.println("username:" + username);
        System.out.println("id:" + id);
        return "success";
    }

}
