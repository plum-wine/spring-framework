package com.github;

import com.github.service.Login;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 * spring 只支持单例模式的循环依赖,不支持原型模式的循环依赖
 */
public class App {

	public static void main(String[] args) {
		String XMLPath = "classpath:spring-config.xml";

		// scan->parse->put2map->new->autowire->lifecycle callback->proxy

		// ClassPathXmlApplication
		// ->constructor
		// ->refresh

		// AbstractApplicationContext
		// -> refresh
		// -> obtainFreshBeanFactory (生成BeanDefinition)
		// -> invokeBeanFactoryPostProcessors (调用 BeanFactoryPostProcessor 各个实现类的 postProcessBeanFactory(factory) 方法)
		// -> finishBeanFactoryInitialization(beanFactory) (实例化所有的bean)
		// -> preInstantiateSingletons

		// DefaultListableBeanFactory
		// -> preInstantiateSingletons
		// -> getBean
		// -> doGetBean
		// -> 第一次调用getSingleton (主要是解决循环依赖与懒加载)
		// -> 第二次调用getSingleton (实例化)
		// -> createBean
		// -> doCreateBean

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(XMLPath);
		Login login = applicationContext.getBean("loginService", Login.class);
		login.loginCheck("boy", "123");
	}

}
