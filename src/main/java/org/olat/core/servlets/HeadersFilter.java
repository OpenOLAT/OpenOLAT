/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.core.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPDirectiveProvider;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
	private List<CSPDirectiveProvider> directiveProviders;

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
		if(securityModule == null || directiveProviders == null) {
			CoreSpringFactory.autowireObject(this);
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
				String header = securityModule.isContentSecurityPolicyReportOnlyEnabled()
						? "Content-Security-Policy-Report-Only" : "Content-Security-Policy";
				httpResponse.setHeader(header, buildContentSecurityPolicy());
			}
		}
	}
	
	private String buildContentSecurityPolicy() {
		StringBuilder sb = new StringBuilder(512);
		String reportUri = Settings.getServerContextPath() + "/csp/";
		appendDirective(sb, "report-uri", null, reportUri);
		
		//policy
		appendDirective(sb, "default-src", null, CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_DEFAULT_SRC);
		appendConnectSrcDirective(sb, false);
		appendScriptSrcDirective(sb, false);
		appendDirective(sb, "style-src", securityModule.getContentSecurityPolicyStyleSrc(),
				CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_STYLE_SRC);
		appendImgSrcDirective(sb, false);
		appendFontSrcDirective(sb, false);
		appendDirective(sb, "worker-src", securityModule.getContentSecurityPolicyWorkerSrc(),
				CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_WORKER_SRC);
		appendFrameSrcDirective(sb, false);
		appendMediaSrcDirective(sb, false);
		appendDirective(sb, "object-src", securityModule.getContentSecurityPolicyObjectSrc(),
				CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_OBJECT_SRC);
		appendDirective(sb, "plugin-types", securityModule.getContentSecurityPolicyPluginType(),
				CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_PLUGIN_TYPE_SRC);
		return sb.toString();
	}
	
	public String getDefaultDirective(String src) {
		StringBuilder sb = new StringBuilder();
		switch(src) {
			case "default-src": 
				appendDirective(sb, "default-src", null, CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_DEFAULT_SRC);
				break;
			case "script-src": appendScriptSrcDirective(sb, true); break;
			case "style-src":
				appendDirective(sb, "style-src", null, CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_STYLE_SRC);
				break;
			case "img-src": appendImgSrcDirective(sb, true); break;
			case "font-src": appendFontSrcDirective(sb, true); break;
			case "connect-src": appendConnectSrcDirective(sb, true); break;
			case "frame-src": appendFrameSrcDirective(sb, true); break;
			case "media-src": appendMediaSrcDirective(sb, true); break;
			case "object-src":
				appendDirective(sb, "object-src", null, CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_OBJECT_SRC);
				break;
			default:
		}
		
		return sb.toString();
		
	}
	
	private void appendFontSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("font-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_FONT_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyFontSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyFontSrc());
		}
		
		sb.append(" ").append(getProvidedUrls(CSPDirectiveProvider::getFontSrcUrls)).append(";");
	}
	
	private void appendConnectSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("connect-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_CONNECT_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyConnectSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyConnectSrc());
		}
		
		sb.append(" ").append(getProvidedUrls(CSPDirectiveProvider::getConnectSrcUrls)).append(";");
	}
	
	private void appendScriptSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("script-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_SCRIPT_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyScriptSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyScriptSrc());
		}
		
		sb.append(" ").append(getProvidedUrls(CSPDirectiveProvider::getScriptSrcUrls)).append(";");
	}
	
	private void appendImgSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("img-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_IMG_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyImgSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyImgSrc());
		}
		
		sb.append(" ").append(getProvidedUrls(CSPDirectiveProvider::getImgSrcUrls)).append(";");
	}
	
	private void appendMediaSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("media-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_MEDIA_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyMediaSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyMediaSrc());
		}

		sb.append(" ").append(getProvidedUrls(CSPDirectiveProvider::getMediaSrcUrls)).append(";");
	}
	
	private void appendFrameSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("frame-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_FRAME_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyFrameSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyFrameSrc());
		}
		
		sb.append(" ").append(getProvidedUrls(CSPDirectiveProvider::getFrameSrcUrls)).append(";");
	}
	
	private void appendDirective(StringBuilder sb, String directiveName, String options, String mandatoryOptions) {
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
	
	private String getProvidedUrls(Function<CSPDirectiveProvider, Collection<String>> urlMethod) {
		return directiveProviders.stream()
				.map(urlMethod)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(this::normalizeUrl)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}
	
	private String normalizeUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			String protocol = url.getProtocol();
			String host = url.getHost();
			return new StringBuilder().append(protocol).append("://").append(host).toString();
		} catch (MalformedURLException e) {
			log.error("", e);
		}
		return null;
	}
	
}