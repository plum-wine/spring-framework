package com.github.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Iterator;
import java.util.Objects;

/**
 * @author hangs.zhang
 * @date 2019/11/03 19:28
 * *****************
 * function:
 */
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Iterator<String> beanNamesIterator = beanFactory.getBeanNamesIterator();
		beanNamesIterator.forEachRemaining(beanName -> {
			if (Objects.equals("loginService", beanName)) {
				GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanFactory.getBeanDefinition(beanName);
				System.out.println(beanDefinition);
			}
		});
	}

}
