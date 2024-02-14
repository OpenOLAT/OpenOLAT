/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.core.servlets;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPBuilder;
import org.olat.core.commons.services.csp.CSPModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * the filter add security HTTP headers to every requests.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HeadersFilter implements Filter {

	@Autowired
	private CSPModule securityModule;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(securityModule == null) {
			CoreSpringFactory.autowireObject(this);
		}
		addSecurityHeaders(response);
		chain.doFilter(request, response);
	}
	
	private void addSecurityHeaders(ServletResponse response) {
		if(response instanceof HttpServletResponse httpResponse) {
			if(securityModule.isStrictTransportSecurityEnabled()) {
				httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			}
			if(securityModule.isXContentTypeOptionsEnabled()) {
				httpResponse.setHeader("X-Content-Type-Options", "nosniff");
			}
			if (securityModule.isXFrameOptionsSameoriginEnabled()) {		
				httpResponse.setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
			}
			if(securityModule.isContentSecurityPolicyEnabled()) {
				String header = securityModule.isContentSecurityPolicyReportOnlyEnabled()
						? "Content-Security-Policy-Report-Only" : "Content-Security-Policy";
				CSPBuilder builder = new CSPBuilder(securityModule);
				String policy = builder
						.defaultDirectives()
						.configurationDirectives()
						.build();
				httpResponse.setHeader(header, policy);
			}
		}
	}
}