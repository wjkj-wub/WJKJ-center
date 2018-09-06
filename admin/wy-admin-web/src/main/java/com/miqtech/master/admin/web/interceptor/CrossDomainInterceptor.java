package com.miqtech.master.admin.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.miqtech.master.admin.web.annotation.CrossDomain;

/**
 * 拦截请求添加跨域功能
 * @author 叶岸平
 */
public class CrossDomainInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
		if (!handler.getClass().isAssignableFrom(HandlerMethod.class)) {//是否为一个控制器
			return true;
		}
		CrossDomain value = ((HandlerMethod) handler).getMethodAnnotation(CrossDomain.class);
		if (value == null || value.value() == false) {
			return true;
		}
		res.addHeader("Access-Control-Allow-Origin", "*");
		res.addHeader("Access-Control-Allow-Credentials", "true");
		res.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
		res.addHeader("P3P", "CP=CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR");
		return true;
	}
}
