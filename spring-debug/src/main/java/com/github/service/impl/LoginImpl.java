package com.github.service.impl;

import com.github.service.Login;
import org.springframework.beans.factory.InitializingBean;

public class LoginImpl implements Login, InitializingBean {
 
	@Override
	public String loginCheck(String userName, String password) {
		System.out.println("boy登录...");
		return "success";
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("LoginImpl.afterPropertiesSet");
	}

	public void init() {
		System.out.println("LoginImpl.init");
	}

}