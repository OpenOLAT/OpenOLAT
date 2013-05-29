/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.core.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;

/**
 * 
 * Allow cross origin for our javascript client
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CrossOriginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//
	}
	
	@Override
	public void destroy() {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		addHeaders(request, response);
		chain.doFilter(request, response);
		addHeaders(request, response);
	}
	
	private void addHeaders(ServletRequest request, ServletResponse response) {
		if(response instanceof HttpServletResponse) {
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			HttpServletResponse httpResponse = (HttpServletResponse)response;
			String origin = httpRequest.getHeader("origin");
			if(isDomainAllowed(origin)) {
				if(origin != null && !origin.isEmpty()) {
					httpResponse.setHeader("Access-Control-Allow-Origin", origin);
				}
				String headers = httpRequest.getHeader("access-control-request-headers");
				if(headers != null && !headers.isEmpty()) {
					httpResponse.setHeader("Access-Control-Allow-Headers", headers);
				}
				httpResponse.setHeader("Access-Control-Allow-Methods", "POST, PUT, DELETE, GET, OPTIONS");
				httpResponse.setHeader("Access-Control-Max-Age", "1728000");

				String method = httpRequest.getHeader("access-control-request-method");
				if("OPTIONS".equals(method)) {
					httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
				}
			}
		}
	}
	
	private boolean isDomainAllowed(String origin) {
		String allowedDomains = Settings.getCrossOriginFilter();
		if(StringHelper.containsNonWhitespace(allowedDomains) && StringHelper.containsNonWhitespace(origin)) {
			if("*".equals(allowedDomains)) {
				return true;
			}
			String[] domains = allowedDomains.split(",");
			for(String domain:domains) {
				if(domain.equals(origin)) {
					return true;
				}
			}
		}
		return false;
	}
}