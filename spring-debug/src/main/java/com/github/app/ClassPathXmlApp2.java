package com.github.app;

import com.github.entity.User;
import com.github.entity.UserFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hangs.zhang
 * @date 2020/06/21 14:18
 * *****************
 * function:
 */
public class ClassPathXmlApp2 {

    public static void main(String[] args) {
        String XMLPath = "classpath:spring-config.xml";
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(XMLPath);

        User user1a = applicationContext.getBean("user1", User.class);
        User user1b = applicationContext.getBean("user2", User.class);
        User user2a = applicationContext.getBean("userFactoryBean", User.class);
        User user2b = applicationContext.getBean("userFactoryBean", User.class);

        System.out.println(user1a);
        System.out.println(user1b);
        System.out.println(user2a);
        System.out.println(user2b);

        // 获取FactoryBean本身的实例需要在BeanName前面加上&
        UserFactoryBean userFactoryBean = applicationContext.getBean("&userFactoryBean", UserFactoryBean.class);
        System.out.println(userFactoryBean);
    }

}
