package com.github.app;

import com.github.config.AppConfiguration;
import com.github.service.Login;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author hangs.zhang
 * @date 2020/06/21 14:18
 * *****************
 * function:
 */
public class AnnotationApp {

    public static void main(String[] args) {
        // AnnotationConfigApplicationContext
        // 从注解中解析BeanDefinition并注册

        // AbstractApplicationContext
        // -> refresh
        // -> obtainFreshBeanFactory (生成BeanDefinition)
        // -> invokeBeanFactoryPostProcessors (调用 BeanFactoryPostProcessor 各个实现类的 postProcessBeanFactory(factory) 方法)
        // -> finishBeanFactoryInitialization(beanFactory) (实例化所有的bean)
        // -> preInstantiateSingletons

        // DefaultListableBeanFactory
        // -> preInstantiateSingletons
        // -> getBean

        // AbstractBeanFactory
        // -> doGetBean
        // ----> DefaultSingletonBeanRegistry
        // ----> getSingleton()方法
        // ----> 回调createBean()方法
        // -> 第一次调用getSingleton (主要是解决循环依赖与懒加载)
        // -> 第二次调用getSingleton (实例化)

        // AbstractAutowiredCapableBeanFactory
        // -> createBean
        // -> doCreateBean
        // -> populateBean 装配

        // AutowiredAnnotationBeanPostProcessor
        // -> postProcessProperties

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfiguration.class);
        Login login = applicationContext.getBean("login", Login.class);
        login.loginCheck("boy", "password");
    }

}
