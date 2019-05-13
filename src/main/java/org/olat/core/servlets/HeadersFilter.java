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
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.analytics.AnalyticsModule;
import org.olat.core.commons.services.analytics.AnalyticsSPI;
import org.olat.core.commons.services.analytics.spi.GoogleAnalyticsSPI;
import org.olat.core.commons.services.analytics.spi.MatomoSPI;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.commons.services.doceditor.collabora.CollaboraModule;
import org.olat.core.commons.services.doceditor.office365.Office365Module;
import org.olat.core.commons.services.doceditor.office365.Office365Service;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.modules.card2brain.Card2BrainModule;
import org.olat.modules.edubase.EdubaseModule;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.vitero.ViteroModule;
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
	private ViteroModule viteroModule;
	@Autowired
	private EdubaseModule edubaseModule;
	@Autowired
	private AnalyticsModule analyticsModule;
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private Office365Module office365Module;
	@Autowired
	private Office365Service office365Service;
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private Card2BrainModule card2BrainModule;
	@Autowired
	private OpenMeetingsModule openMeetingsModule;

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
		if(securityModule == null || edubaseModule != null
				|| analyticsModule != null || card2BrainModule != null
				|| openMeetingsModule != null|| edusharingModule != null
				|| viteroModule != null) {
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
		
		appendEdusharingUrl(sb);
		sb.append(";");
	}
	
	private void appendConnectSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("connect-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_CONNECT_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyConnectSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyConnectSrc());
		}
		
		appendAnalyticsUrl(sb);
		appendEdusharingUrl(sb);
		sb.append(";");
	}
	
	private void appendScriptSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("script-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_SCRIPT_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyScriptSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyScriptSrc());
		}
		
		appendMathJaxUrl(sb);
		appendAnalyticsUrl(sb);
		appendEdusharingUrl(sb);
		appendOnlyOfficeUrl(sb);
		sb.append(";");
	}
	
	private void appendImgSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("img-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_IMG_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyImgSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyImgSrc());
		}
		appendAnalyticsUrl(sb);
		appendEdubaseUrl(sb);
		appendEdusharingUrl(sb);
		sb.append(";");
	}
	
	private void appendMediaSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("media-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_MEDIA_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyMediaSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyMediaSrc());
		}
		appendOpenMeetingsUrl(sb);
		appendEdusharingUrl(sb);
		sb.append(";");
	}
	
	private void appendFrameSrcDirective(StringBuilder sb, boolean standard) {
		sb.append("frame-src ")
		  .append(CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_FRAME_SRC);
		if(!standard && StringHelper.containsNonWhitespace(securityModule.getContentSecurityPolicyFrameSrc())) {
			sb.append(" ").append(securityModule.getContentSecurityPolicyFrameSrc());
		}
		
		appendCard2BrainUrl(sb);
		appendEdubaseUrl(sb);
		appendEdusharingUrl(sb);
		appendOnlyOfficeUrl(sb);
		appendCollaboraUrl(sb);
		appendOffice365Urls(sb);
		appendViteroUrl(sb);
		sb.append(";");
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
	
	private void appendMathJaxUrl(StringBuilder sb) {
		String mathJaxCdn = WebappHelper.getMathJaxCdn();
		if(StringHelper.containsNonWhitespace(mathJaxCdn)
				&& (mathJaxCdn.startsWith("//") || mathJaxCdn.startsWith("https://") || mathJaxCdn.startsWith("http://"))) {
			try {
				if(mathJaxCdn.startsWith("//")) {
					mathJaxCdn = "https:" + mathJaxCdn;
				}
				URL url = URI.create(mathJaxCdn).toURL();
				sb.append(" ").append(url.getProtocol()).append("://").append(url.getHost());
			} catch (MalformedURLException e) {
				log.error("", e);
			}
		}
	}
	
	private void appendOpenMeetingsUrl(StringBuilder sb) {
		if(openMeetingsModule != null && openMeetingsModule.isEnabled()) {
			appendUrl(sb, openMeetingsModule.getOpenMeetingsURI().toString());
		}
	}
	
	private void appendAnalyticsUrl(StringBuilder sb) {
		if(analyticsModule != null) {
			AnalyticsSPI spi = analyticsModule.getAnalyticsProvider();
			if(spi instanceof GoogleAnalyticsSPI) {
				sb.append(" ").append("https://www.google-analytics.com");
			} else if(spi instanceof MatomoSPI) {
				String trackerUrl = ((MatomoSPI)spi).getTrackerUrl();
				if(StringHelper.containsNonWhitespace(trackerUrl)) {
					sb.append(" ").append(trackerUrl);
				}
			}
		}
	}
	
	private void appendEdubaseUrl(StringBuilder sb) {
		if(edubaseModule != null && edubaseModule.isEnabled()) {
			appendUrl(sb, edubaseModule.getLtiBaseUrl());
		}
	}
	
	private void appendEdusharingUrl(StringBuilder sb) {
		if(edusharingModule != null && edusharingModule.isEnabled()) {
			appendUrl(sb, edusharingModule.getBaseUrl());
		}
	}
	
	private void appendCollaboraUrl(StringBuilder sb) {
		if(collaboraModule != null && collaboraModule.isEnabled()) {
			appendUrl(sb, collaboraModule.getBaseUrl());
		}
	}
	
	private void appendOffice365Urls(StringBuilder sb) {
		if(office365Module != null && office365Module.isEnabled()) {
			Collection<String> contentSecurityPolicyUrls = office365Service.getContentSecurityPolicyUrls();
			for (String url : contentSecurityPolicyUrls) {
				appendUrl(sb, url);
			}
		}
	}
	
	private void appendOnlyOfficeUrl(StringBuilder sb) {
		if(onlyOfficeModule != null && onlyOfficeModule.isEnabled()) {
			appendUrl(sb, onlyOfficeModule.getBaseUrl());
		}
	}
	
	private void appendCard2BrainUrl(StringBuilder sb) {
		if(card2BrainModule != null && card2BrainModule.isEnabled()) {
			appendUrl(sb, card2BrainModule.getVerifyLtiUrl());
		}
	}
	
	private void appendViteroUrl(StringBuilder sb) {
		if(viteroModule != null && viteroModule.isEnabled()) {
			appendUrl(sb, viteroModule.getVmsURI().toString());
		}
	}
	
	private void appendUrl(StringBuilder sb, String urlString) {
		try {
			URL url = new URL(urlString);
			String protocol = url.getProtocol();
			String host = url.getHost();
			sb.append(" ").append(protocol).append("://").append(host);
		} catch (MalformedURLException e) {
			log.error("", e);
		}
	}
}