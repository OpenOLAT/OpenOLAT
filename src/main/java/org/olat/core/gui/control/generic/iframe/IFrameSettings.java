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
package org.olat.core.gui.control.generic.iframe;

/**
 * 
 * Initial date: 13 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IFrameSettings {

	private String contentSecurityPolicy;
	private boolean strictSanitize = false;
	private boolean useContentDomain = false;
	private boolean persistMapper = false;
	private boolean randomizeMapper = false;
	private boolean iframeResizer = true;
	
	
	public IFrameSettings() {
		//
	}
	
	public static IFrameSettings sanitize() {
		IFrameSettings securityOptions = new IFrameSettings();
		securityOptions.setStrictSanitize(true);
		securityOptions.setIframeResizer(true);
		return securityOptions;
	}
	
	/**
	 * 
	 * @return The options
	 */
	public static IFrameSettings secure() {
		IFrameSettings securityOptions = new IFrameSettings();
		securityOptions.setStrictSanitize(false);
		securityOptions.setUseContentDomain(true);
		securityOptions.setIframeResizer(true);
		return securityOptions;
	}
	
	public IFrameSettings copy() {
		IFrameSettings securityOptions = new IFrameSettings();
		securityOptions.setContentSecurityPolicy(contentSecurityPolicy);
		securityOptions.setUseContentDomain(useContentDomain);
		securityOptions.setStrictSanitize(strictSanitize);
		securityOptions.setIframeResizer(iframeResizer);
		securityOptions.setPersistMapper(persistMapper);
		securityOptions.setRandomizeMapper(randomizeMapper);
		return securityOptions;
	}
	
	public String getContentSecurityPolicy() {
		return contentSecurityPolicy;
	}
	
	public void setContentSecurityPolicy(String contentSecurityPolicy) {
		this.contentSecurityPolicy = contentSecurityPolicy;
	}

	public boolean isStrictSanitize() {
		return strictSanitize;
	}

	public void setStrictSanitize(boolean strictSanitize) {
		this.strictSanitize = strictSanitize;
	}

	public boolean isUseContentDomain() {
		return useContentDomain;
	}

	public void setUseContentDomain(boolean useContentDomain) {
		this.useContentDomain = useContentDomain;
	}

	public boolean isPersistMapper() {
		return persistMapper;
	}

	public void setPersistMapper(boolean persistMapper) {
		this.persistMapper = persistMapper;
	}

	public boolean isRandomizeMapper() {
		return randomizeMapper;
	}

	public void setRandomizeMapper(boolean randomizeMapper) {
		this.randomizeMapper = randomizeMapper;
	}

	public boolean isIframeResizer() {
		return iframeResizer;
	}

	public void setIframeResizer(boolean iframeResizer) {
		this.iframeResizer = iframeResizer;
	}
}
