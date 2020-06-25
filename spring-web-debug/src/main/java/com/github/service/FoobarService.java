package com.github.service;

import org.springframework.stereotype.Service;

/**
 * @author hangs.zhang
 * @date 2020/06/25 15:36
 * *****************
 * function:
 */
@Service
public class FoobarService {

    public String doMessage(String message) {
        return "foobar : " + message;
    }

}
