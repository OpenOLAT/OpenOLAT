/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPBuilder;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * the filter add security HTTP headers to every requests.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HeadersFilter implements Filter {
	
	private static final Logger log = Tracing.createLoggerFor(HeadersFilter.class);

	@Autowired
	private CSPModule securityModule;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private UserSessionManager userSessionManager;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(securityModule == null) {
			CoreSpringFactory.autowireObject(this);
		}
		addSecurityHeaders(request, response);
		chain.doFilter(request, response);
	}
	
	private void addSecurityHeaders(ServletRequest request, ServletResponse response) {
		if(response instanceof HttpServletResponse httpResponse) {
			if(securityModule.isStrictTransportSecurityEnabled()) {
				httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			}
			if(securityModule.isXContentTypeOptionsEnabled()) {
				httpResponse.setHeader("X-Content-Type-Options", "nosniff");
			}
			
			boolean content = isContent(request);
			if (securityModule.isXFrameOptionsSameoriginEnabled() && !content) {
				httpResponse.setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
			}
			
			if(securityModule.isContentSecurityPolicyEnabled()) {
				String header = securityModule.isContentSecurityPolicyReportOnlyEnabled()
						? "Content-Security-Policy-Report-Only"
						: "Content-Security-Policy";
				CSPBuilder builder = new CSPBuilder(securityModule);
				builder
					.defaultDirectives()
					.configurationDirectives();
				if(content) {
					builder.frameAncestors().addAdditionalDirectives(Settings.createServerURI());
				}
				String policy = builder.build();
				httpResponse.setHeader(header, policy);
			}
		}
	}
	
	private final boolean isContent(ServletRequest request) {
		if(request instanceof HttpServletRequest hreq) {
			try {
				UserSession usess = userSessionManager.getUserSessionIfAlreadySet(hreq);
				if(usess != null && usess.isContentDelivery()) {
					return true;
				}
				
				String pathInfo = DispatcherModule.subtractContextPath(hreq);
				if(pathInfo.contains(DispatcherModule.PATH_MAPPED)) {
					String subInfo = pathInfo.substring(DispatcherModule.PATH_MAPPED.length());
					
					int slashPos = subInfo.indexOf('/');

					String smappath;
					if (slashPos == -1) {
						smappath = subInfo;
					} else {
						smappath = subInfo.substring(0, slashPos);
					}
					return mapperService.isSandbox(smappath);
				}
			} catch (UnsupportedEncodingException e) {
				log.error("", e);
			}
		}
		return false;
	}
}