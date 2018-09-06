package com.miqtech.master.admin.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.miqtech.master.entity.common.SystemUser;

/**
 * Servlet Filter implementation class SessionAdminFilter
 */
@WebFilter("/SessionAdminFilter")
public class SessionAdminFilter implements Filter {

	private static final String FORWARD_URL = "/login";
	private static final String CAPTCHA_URL = "/captcha";
	private static final List<String> FILTER_URLS = new ArrayList<String>();

	public SessionAdminFilter() {
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		// 排除资源文件
		String uri = request.getRequestURI();
		String filterWith = ".css,.js,.jpg,.jpeg,.png,.gif,.ico";
		for (String str : filterWith.split(",")) {
			if (uri.endsWith(str)) {
				chain.doFilter(request, response);
				return;
			}
		}

		String servletPath = request.getServletPath();
		if (servletPath.equals(FORWARD_URL) || servletPath.equals(CAPTCHA_URL)
				|| StringUtils.startsWith(servletPath, "/api/") || FILTER_URLS.contains(servletPath)
				|| servletPath.contains("netbar/resource/toConfirmUp")
				|| servletPath.contains("netbar/resource/toConfirmC")
				|| servletPath.contains("netbar/resource/toConfirmB")
				|| servletPath.contains("netbar/resource/confirmUp") || servletPath.contains("netbar/resource/confirmC")
				|| servletPath.contains("netbar/resource/confirmB")) {
			chain.doFilter(req, res);
			return;
		}
		SystemUser user = (SystemUser) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(FORWARD_URL);
			return;
		} else {
			chain.doFilter(req, res);
			return;
		}
	}

}
