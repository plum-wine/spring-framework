package com.github.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Iterator;

/**
 * @author hangs.zhang
 * @date 2019/11/03 19:28
 * *****************
 * function:
 */
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 *
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException in case of errors
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Iterator<String> beanNamesIterator = beanFactory.getBeanNamesIterator();
		beanNamesIterator.forEachRemaining(beanName -> {
			if (beanName.equals("loginService")) {
				GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanFactory.getBeanDefinition(beanName);
				System.out.println(beanDefinition);
			}
		});
	}

}
