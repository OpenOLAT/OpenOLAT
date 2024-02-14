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
package org.olat.core.commons.services.csp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSPBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(CSPBuilder.class);
	
	private final Directive defaultSrc;
	private final ScriptSrcDirective scriptSrc;
	private final Directive styleSrc;
	private final Directive imgSrc;
	private final Directive fontSrc;
	private final Directive connectSrc;
	private final Directive frameSrc;
	private final Directive mediaSrc;
	private final Directive objectSrc;
	private final Directive workerSrc;
	
	private final List<Directive> directives;
	
	public CSPBuilder(CSPModule securityModule) {
		List<CSPDirectiveProvider> directiveProviders = securityModule.getDirectiveProviders();
		defaultSrc = new Directive("default-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_DEFAULT_SRC,
				null,
				null);
		scriptSrc = new ScriptSrcDirective("script-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_SCRIPT_SRC,
				securityModule.getContentSecurityPolicyScriptSrc(),
				getProvidedUrls(directiveProviders, CSPDirectiveProvider::getScriptSrcUrls),
				securityModule.isAllowUnsafeEval());
		styleSrc = new Directive("style-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_STYLE_SRC,
				securityModule.getContentSecurityPolicyStyleSrc(),
				null);
		imgSrc = new Directive("img-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_IMG_SRC,
				securityModule.getContentSecurityPolicyImgSrc(),
				getProvidedUrls(directiveProviders, CSPDirectiveProvider::getImgSrcUrls));
		fontSrc = new Directive("font-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_FONT_SRC,
				securityModule.getContentSecurityPolicyFontSrc(),
				getProvidedUrls(directiveProviders, CSPDirectiveProvider::getFontSrcUrls));
		connectSrc = new Directive("connect-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_CONNECT_SRC,
				securityModule.getContentSecurityPolicyConnectSrc(),
				getProvidedUrls(directiveProviders, CSPDirectiveProvider::getConnectSrcUrls));
		frameSrc = new Directive("frame-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_FRAME_SRC,
				securityModule.getContentSecurityPolicyFrameSrc(),
				getProvidedUrls(directiveProviders, CSPDirectiveProvider::getFrameSrcUrls));
		mediaSrc = new Directive("media-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_MEDIA_SRC,
				securityModule.getContentSecurityPolicyMediaSrc(),
				getProvidedUrls(directiveProviders, CSPDirectiveProvider::getMediaSrcUrls));
		objectSrc = new Directive("object-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_OBJECT_SRC,
				securityModule.getContentSecurityPolicyObjectSrc(),
				null);
		workerSrc = new Directive("worker-src", CSPModule.DEFAULT_CONTENT_SECURITY_POLICY_WORKER_SRC,
				securityModule.getContentSecurityPolicyWorkerSrc(),
				null);
		
		directives = List.of(defaultSrc, scriptSrc, styleSrc, imgSrc, imgSrc, fontSrc, connectSrc, frameSrc,
				mediaSrc, objectSrc, workerSrc);
	}
	
	private static String getProvidedUrls(List<CSPDirectiveProvider> directiveProviders, Function<CSPDirectiveProvider, Collection<String>> urlMethod) {
		return directiveProviders.stream()
				.map(urlMethod)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(CSPBuilder::normalizeUrl)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.joining(" "));
	}
	
	private static String normalizeUrl(String urlString) {
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
	
	public Directive defaultSrc() {
		return defaultSrc;
	}
	
	public ScriptSrcDirective scriptSrc() {
		return scriptSrc;
	}
	
	public Directive styleSrc() {
		return scriptSrc;
	}
	
	public Directive imgSrc() {
		return imgSrc;
	}
	
	public Directive fontSrc() {
		return fontSrc;
	}
	
	public Directive connectSrc() {
		return connectSrc;
	}
	
	public Directive frameSrc() {
		return frameSrc;
	}
	
	public Directive mediaSrc() {
		return mediaSrc;
	}
	
	public Directive objectSrc() {
		return objectSrc;
	}
	
	public Directive workerSrc() {
		return workerSrc;
	}
	
	public CSPBuilder defaultDirectives() {
		for(Directive directive:directives) {
			directive.useDefault();
		}
		return this;
	}
	
	public CSPBuilder configurationDirectives() {
		for(Directive directive:directives) {
			directive.useConfiguration();
		}
		return this;
	}

	public String build() {
		StringBuilder header = new StringBuilder(1024);
		header.append("report-uri ").append(Settings.getServerContextPath()).append("/csp/;");
		defaultSrc.append(header);
		scriptSrc.append(header);
		styleSrc.append(header);
		imgSrc.append(header);
		fontSrc.append(header);
		connectSrc.append(header);
		frameSrc.append(header);
		mediaSrc.append(header);
		objectSrc.append(header);
		workerSrc.append(header);
		return header.toString();
	}
	
	public class ScriptSrcDirective extends Directive {
		
		private boolean allowUnsafeEval;

		public ScriptSrcDirective(String name, String baseDirective, String configuredDirective, String providersDirectives,
				boolean allowUnsafeEval) {
			super(name, baseDirective, configuredDirective, providersDirectives);
			this.allowUnsafeEval = allowUnsafeEval;
		}
		
		public boolean isAllowUnsafeEval() {
			return allowUnsafeEval;
		}
		
		public ScriptSrcDirective allowUnsafeEval() {
			allowUnsafeEval = true;
			return this;
		}

		@Override
		public boolean hasDirective() {
			return isAllowUnsafeEval() || super.hasDirective();
		}
		
		@Override
		protected void appendOptions(StringBuilder header) {
			if(isAllowUnsafeEval()) {
				header.append(" 'unsafe-eval'");
			}
			super.appendOptions(header);
		}
	}
	
	public class PluginTypesDirective extends Directive {
		
		public PluginTypesDirective(String name, String baseDirective, String configuredDirective, String providersDirectives) {
			super(name, baseDirective, configuredDirective, providersDirectives);
		}

		@Override
		protected void appendAllForbiden(StringBuilder header) {
			// Write nothing, none is not supported for this one
		}
	}

	public class Directive {
		private final String name;
		private final String defaultDirective;
		private final String configuredDirectives;
		private final String providersDirectives;
		private String additionalDirectives;
		
		private boolean hidden = false;
		private boolean useDefault = false;
		private boolean useConfiguration = false;
		private boolean allForbidden = false;

		public Directive(String name, String defaultDirective, String configuredDirective, String providersDirectives) {
			this.name = name;
			this.defaultDirective = defaultDirective;
			this.configuredDirectives = configuredDirective;
			this.providersDirectives = providersDirectives;
		}
		
		public final String getName() {
			return name;
		}
	
		public Directive hide() {
			hidden = true;
			return this;
		}
		
		public Directive useDefault() {
			useDefault = true;
			return this;
		}
		
		public Directive useConfiguration() {
			useConfiguration = true;
			return this;
		}
		
		public Directive allForbidden() {
			allForbidden = true;
			return this;
		}
		
		public String getAdditionalDirectives() {
			return additionalDirectives;
		}

		public void setAdditionalDirectives(String additionalDirectives) {
			this.additionalDirectives = additionalDirectives;
		}

		public CSPBuilder builder() {
			return CSPBuilder.this;
		}
		
		public boolean hasDirective() {
			return (useDefault && (StringHelper.containsNonWhitespace(defaultDirective) || StringHelper.containsNonWhitespace(providersDirectives)))
					|| (useConfiguration && StringHelper.containsNonWhitespace(configuredDirectives))
					|| StringHelper.containsNonWhitespace(getAdditionalDirectives());
		}
		
		protected void append(StringBuilder header) {
			if(hidden) {
				// Do nothing, the directive doesn't appear in the header
			} else if(allForbidden) {
				appendAllForbiden(header);
			} else {
				header.append(getName());
				appendOptions(header);
				header.append(";");
			}
		}
		
		protected void appendAllForbiden(StringBuilder header) {
			header.append(getName()).append(" 'none'").append(";");
		}

		protected void appendOptions(StringBuilder header) {
			if(useDefault) {
				if(StringHelper.containsNonWhitespace(defaultDirective)) {
					header.append(" ").append(defaultDirective);
				}
				if(StringHelper.containsNonWhitespace(providersDirectives)) {
					header.append(" ").append(providersDirectives);
				}
			}
			if(useConfiguration) {
				header.append(" ").append(configuredDirectives);
			}
			if(StringHelper.containsNonWhitespace(getAdditionalDirectives())) {
				header.append(" ").append(getAdditionalDirectives());
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			append(sb);
			return sb.toString();
		}
	}
}
