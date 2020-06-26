package com.github.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

/**
 * @author hangs.zhang
 * @date 2020/06/25 15:28
 * *****************
 * function:
 */
/*
Root容器
1. SpringServletContainerInitializer继承自ServletContainerInitializer, 在tomcat容器启动时加载
    1. onStartup(), 调用WebApplicationInitializer的onStartup方法
    2. 是通过servlet包提供的@HandlesTypes注解, 找到的所有WebApplicationInitializer.class

2. AbstractContextLoaderInitializer, 继承自WebApplicationInitializer
    1. onStartup
    2. registerContextLoaderListener
        1. 通过子类的getRootConfigClasses方法提供的Configuration类, 创建root容器
        2. 创建ContextLoaderListener, 为servlet的监听器
        3. 注册监听器

3. ContextLoaderListener, servlet的监听器, web容器启动时调用contextInitialized

4. 然后会去调用ContextLoader的initWebApplicationContext方法中去refresh root容器

Web容器加载过程
1. SpringServletContainerInitializer继承自ServletContainerInitializer, 在tomcat容器启动时加载
     onStartup()

2. AbstractDispatcherServletInitializer, 继承自AbstractContextLoaderInitializer
     onStartup()
     registerDispatcherServlet()
        1. 创建web容器
            1. AbstractAnnotationConfigDispatcherServletInitializer
                1. createServletApplicationContext()方法创建, 从子类的getServletConfigClasses方法获取Configuration类, 进行注册, 但是此时的容器并没有加载
        2. 创建DispatcherServlet,注册到tomcat
        3. 从子类获取过滤器等参数, 注册

3. HttpServletBean, 继承自HttpServlet
     init(), 为servlet的方法, 在servlet启动时调用

4. FrameworkServlet ,继承HttpServletBean, 实现了ApplicationContextAware接口
     initServletBean()
     initWebApplicationContext()
     configureAndRefreshWebApplicationContext()

容器加载顺序: root容器->web容器, root容器为web容器的父容器
*/
public class StartWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * springContext相关的Bean
     *
     * @return
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{SpringRootConfig.class};
    }


    /**
     * DispatcherServlet中上下文相关的Bean
     *
     * @return
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{MVCConfig.class};
    }

    /**
     * servlet请求路径
     *
     * @return
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /**
     * 拦截并处理请求编码
     *
     * @return
     */
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return new Filter[]{characterEncodingFilter};
    }
}
