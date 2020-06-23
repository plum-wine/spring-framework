package com.github.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author hangs.zhang
 * @date 2020/06/21 21:28
 * *****************
 * function:
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.github")
public class AppConfiguration {
}
