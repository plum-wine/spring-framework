package com.github.service.impl;

import com.github.service.FoobarService;
import com.github.service.Login;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("login")
public class LoginImpl implements Login, InitializingBean {

	@Autowired
	private FoobarService foobarService;

	public void setFoobarService(FoobarService foobarService) {
		this.foobarService = foobarService;
	}

	@Override
	public String loginCheck(String userName, String password) {
		System.out.println("process\t" + foobarService.doMessage(userName));
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