/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.core.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.analytics.AnalyticsModule;
import org.olat.core.commons.services.analytics.spi.GoogleAnalyticsSPI;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HeadersFilter implements Filter {
	
	private static final OLog log = Tracing.createLoggerFor(HeadersFilter.class);
	
	private AnalyticsModule analyticsModule;
	private BaseSecurityModule securityModule;

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
		if(securityModule == null) {
			securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		}
		if(analyticsModule == null) {
			analyticsModule = CoreSpringFactory.getImpl(AnalyticsModule.class);
		}
		addSecurityHeaders(response);
		chain.doFilter(request, response);
	}
	
	private void addSecurityHeaders(ServletResponse response) {
		if(response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;	
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
				httpResponse.setHeader("Content-Security-Policy", buildContentSecurityPolicy());
			}
		}
	}
	
	private String buildContentSecurityPolicy() {
		StringBuilder sb = new StringBuilder(128);
		buildContentSecurityPolicy(sb, "default-src", null, BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_DEFAULT_SRC);
		buildContentSecurityPolicy(sb, "connect-src", securityModule.getContentSecurityPolicyConnectSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_CONNECT_SRC);
		String scriptDirective = getMandatoryScriptDirective();
		buildContentSecurityPolicy(sb, "script-src", securityModule.getContentSecurityPolicyScriptSrc(), scriptDirective);
		buildContentSecurityPolicy(sb, "style-src", securityModule.getContentSecurityPolicyStyleSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_STYLE_SRC);
		buildContentSecurityPolicy(sb, "img-src", securityModule.getContentSecurityPolicyImgSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_IMG_SRC);
		buildContentSecurityPolicy(sb, "font-src", securityModule.getContentSecurityPolicyFontSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_FONT_SRC);
		buildContentSecurityPolicy(sb, "worker-src", securityModule.getContentSecurityPolicyWorkerSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_WORKER_SRC);
		buildContentSecurityPolicy(sb, "frame-src", securityModule.getContentSecurityPolicyFrameSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_FRAME_SRC);
		buildContentSecurityPolicy(sb, "media-src", securityModule.getContentSecurityPolicyMediaSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_MEDIA_SRC);
		buildContentSecurityPolicy(sb, "object-src", securityModule.getContentSecurityPolicyObjectSrc(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_OBJECT_SRC);
		buildContentSecurityPolicy(sb, "plugin-types", securityModule.getContentSecurityPolicyPluginType(), BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_PLUGIN_TYPE_SRC);
		return sb.toString();
	}
	
	private String getMandatoryScriptDirective() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_SCRIPT_SRC);
		if(StringHelper.containsNonWhitespace(WebappHelper.getMathJaxCdn())) {
			try {
				String mathJaxCdn = WebappHelper.getMathJaxCdn();
				if(mathJaxCdn.startsWith("//")) {
					mathJaxCdn = "https:" + mathJaxCdn;
				}
				URL url = URI.create(mathJaxCdn).toURL();
				sb.append(" ").append(url.getProtocol()).append("://").append(url.getHost());
			} catch (MalformedURLException e) {
				log.error("", e);
			}
		}
		
		if(analyticsModule != null && analyticsModule.getAnalyticsProvider() instanceof GoogleAnalyticsSPI) {
			sb.append(" ").append("https://www.google-analytics.com");
		}
		return sb.toString();
	}
	

	private void buildContentSecurityPolicy(StringBuilder sb, String directiveName, String options, String mandatoryOptions) {
		if(StringHelper.containsNonWhitespace(options) || StringHelper.containsNonWhitespace(mandatoryOptions)) {
			sb.append(directiveName)
			  .append(" ");
			if(StringHelper.containsNonWhitespace(mandatoryOptions)) {
				sb.append(" ").append(mandatoryOptions);
			}
			if(StringHelper.containsNonWhitespace(options)) {
				sb.append(" ").append(options);
			}
			sb.append(";");
		}
	}
}