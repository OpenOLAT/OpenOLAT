/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.helpers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * Description:<br>
 * generic settings related to the gui framework. see also coreconfig.xml for comments
 * <P>
 * Initial Date:  04.01.2007 <br>
 *
 * @author Felix Jost
 */
public class Settings {

	private static boolean debug = false;
	private static String htmlEditorContentCssClassPrefixes;
	private static List<Pattern> ajaxBlacklistPatterns = new ArrayList<>();
	private static boolean jUnitTest;
	private static String applicationName;
	private static String version;
	private static String buildIdentifier;
	private static final Logger log = Tracing.createLoggerFor(Settings.class);

	private static int nodeId;
	private static String clusterMode;
	private static Date buildDate;
	private static String repoRevision;
	private static File guiCustomThemePath;
	
	private static int securePort;
	private static int insecurePort;
	private static String domainName;
	private static String legacyContextPath;
	
	private static String loginPath;
	
	/**
	 * [used by spring]
	 */
	Settings() {
		//
	}
	
	// fxdiff: only set build id from build date if none is provided in olat.local.properties!
	private static void setBuildIdFromBuildDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		buildIdentifier = formatter.format(getBuildDate());
	}
	
	// fxdiff: only set build date 
	private static void setBuildDate(){
		//extract the latest build number as date where this class was compiled
		Resource res = new ClassPathResource("org/olat/core/helpers/Settings.class");
		try {
			buildDate = new Date(res.lastModified());
		} catch (IOException e) {
			buildDate = new Date();
		}
	}
	
	/**
	 * 
	 * @return a identifier for this build e.g. the build date of a class like 20100329
	 */
	public static String getBuildIdentifier() {
		if (buildIdentifier == null) {
			setBuildIdFromBuildDate();
		} 
		return buildIdentifier;
	}
	
	/**
	 * [spring]
	 * @param buildId
	 */
	public void setBuildIdentifier(String buildId){
		buildIdentifier = buildId;
	}
	
	public void setRepoRevision(String repoRev){
		repoRevision = repoRev;
	}
	
	public static String getRepoRevision(){
		return repoRevision;
	}
	
	/**
	 * 
	 * @return the exacte date and time this class was comiled
	 */
	public static Date getBuildDate() {
		if (buildDate == null){
			setBuildDate();
		}
		return buildDate;
	}

	/**
	 * @return
	 */
	public static boolean isDebuging() {
		return debug;
	}

	/**
	 * Set the list of regular expressions that represent user agents that are not
	 * allowed to use the ajax mode. 
	 * <p>
	 * Note that this method is not thread save. The intention is to set this list only
	 * once at system startup by spring. After that values can only be red. 
	 * [spring]
	 * 
	 * @param userAgents
	 */
	public void setAjaxBlacklistedUserAgents(List<String> userAgents) {
		// Use CopyOnWriteArrayList instead of the ArrayList if you make a GUI
		// that allows changing of the list values
		for (String regexp : userAgents) {
			try {
				Pattern pattern = Pattern.compile(regexp);
				ajaxBlacklistPatterns.add(pattern);				
			} catch (PatternSyntaxException e) {
				log.error("Ignoring invalid ajax blacklist user agent::" + regexp + " Please fix your brasatoconfig.xml", e);
			}
		}
	}

	/**
	 * Checks against a list of browser defined in brasatoconfig.xml whether the
	 * browser is on the AJAX blacklist.
	 * <p>
	 * Note that this configuration changed in OLAT 7.1. In previous releases OLAT
	 * used a whitelist mechanism which is now converted into a blacklist.
	 * 
	 * @param ureq
	 * @return true: user agent is blacklistet for AJAX mode; false: user agent
	 *         can use AJAX mode
	 */
	public static boolean isBrowserAjaxBlacklisted(UserRequest ureq) {
		String uag = ureq.getHttpReq().getHeader("user-agent");
		if (uag == null) return false;
		for (Pattern agentPattern : ajaxBlacklistPatterns) {
			if (agentPattern.matcher(uag).matches()) {
				// This browser is on the web 1.0 mode list, not AJAX certified
				return true;
			}
		}
		// Passed all patterns, is certified
		return false;
	}
	
	/**
	 * @return the versionId, which is a short string consisting of 0-9 which is derived from the version without the dots.
	 */
	public static String getVersionId() {
		return Settings.version.replaceAll("\\.", "");
	}
	
	
	/**
	 * @return the version which e.g. 7.0.0
	 */
	public static String getVersion() {
		return Settings.version;
	}
	
	public static String getFullVersionInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(applicationName).append(" ").append(getVersion()).append(" (Build ").append(getBuildIdentifier()).append(")");
		return sb.toString();
	}
	
	/**
	 * [spring]
	 * @param versionId the versionId to set
	 */
	public void setVersion(String version) {
		Settings.version = version;
	}
	
	public static int getServerSecurePort() {
		return Settings.securePort;
	}
	
	public void setServerSecurePort(int securePort) {
		Settings.securePort = securePort;
	}

	public static int getServerInsecurePort() {
		return Settings.insecurePort;
	}
	
	public void setServerInsecurePort(int insecurePort) {
		Settings.insecurePort = insecurePort;
	}
	
	public static String getServerDomainName() {
		return Settings.domainName;
	}
	
	public void setServerDomainName(String domainName) {
		Settings.domainName = domainName;
	}
	
	public static String getLoginPath() {
		return loginPath;
	}
	
	public void setLoginPath(String path) {
		Settings.loginPath = path;
	}
	
	public static String getServerContextPath() {
		return WebappHelper.getServletContextPath();
	}
	

	public String getLegacyContext() {
		return legacyContextPath;
	}

	public void setLegacyContext(String legacyContextPath) {
		Settings.legacyContextPath = legacyContextPath;
	}

	/**
	 * [spring]
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		Settings.debug = debug;
	}


	/**
	 * @return A regexp that matches for css class name prefixes that should be
	 *         used in the HTML editor to limit css names that are available in
	 *         the menus.
	 */
	public static String getHtmlEditorContentCssClassPrefixes() {
		return htmlEditorContentCssClassPrefixes;
	}

	/**
	 * Set the regexp that matches for css class name prefixes that should be
	 * used in the HTML editor to limit css names that are available in the
	 * menus or let empty for no rule.
	 * 
	 * @param contentCssClassPrefixes
	 */
	public void setHtmlEditorContentCssClassPrefixes(String contentCssClassPrefixes) {
		if (StringHelper.containsNonWhitespace(contentCssClassPrefixes)) {
			htmlEditorContentCssClassPrefixes = contentCssClassPrefixes.trim();
		}
	}

	
	/**
	 * @return the File object pointing to the custom themes folder or null if
	 *         no custom themes folder configured
	 */
	public static File getGuiCustomThemePath() {
		return guiCustomThemePath;			
	}

	/**
	 * Set the custom CSS themes folder (optional). Only used by spring.
	 * 
	 * @param guiCustomThemePath
	 *            Absolute path pointing to the custom themes directory
	 */
	public void setGuiCustomThemePath(String guiCustomThemePath) {
		File newPath = new File(guiCustomThemePath);
		if (newPath.exists()) {
			Settings.guiCustomThemePath = newPath;
		} else {
			log.info("No custom theme directory configured, path::"
					+ guiCustomThemePath
					+ " invalid. Configure property layout.custom.themes.dir if you want to use a custom themes directory.");
		}
	}
	
	public static String getURIScheme() {
		return (isSecurePortAvailable() ? "https:" : "http:");
	}

	public static boolean isSecurePortAvailable() {
		return getServerSecurePort() > 0;
	}
	
	public static boolean isInsecurePortAvailable() {
		return getServerInsecurePort() > 0;
	}

	public static String createServerURI() {
		String uri;
		if (isSecurePortAvailable()) {
			int port = getServerSecurePort();
			uri = "https://" + getServerDomainName() + createURIPortPartWithDefaultPortCheck(port, 443);
		} else {
			int port = getServerInsecurePort();
			uri = "http://" + getServerDomainName() + createURIPortPartWithDefaultPortCheck(port, 80);
		}
		return uri;
	}
	
	/**
	 * @param configuredPort comes from the spring configuration file, null is converted to ""
	 * @param defaultPort use 80 for http, 443 for https
	 * @return "" if no port is defined, or a default port. i.e. ":8080" if a port is configured and non-standard
	 */
	private static String createURIPortPartWithDefaultPortCheck(int configuredPort, int defaultPort){
		if(configuredPort <= 0){
			return "";
		}
		return (configuredPort == defaultPort) ? "" :  ":" + configuredPort;
	}
	
	/**
	 * 
	 * @return the full server path like https://www.olat.uzh.ch/olat
	 * depending on settings like secure/insecure, context path, ...
	 */
	public static String getServerContextPathURI() {
		return createServerURI() + WebappHelper.getServletContextPath();
	}
	
	public static String getSecureServerContextPathURI() {
		String uri = null;
		if (isSecurePortAvailable()) {
			int port = getServerSecurePort();
			uri = "https://" + getServerDomainName() + createURIPortPartWithDefaultPortCheck(port, 443);
		}
		return uri;
	}
	
	public static String getInsecureServerContextPathURI() {
		String uri = null;
		if (isInsecurePortAvailable()) {
			int port = getServerInsecurePort();
			uri = "http://" + getServerDomainName() + createURIPortPartWithDefaultPortCheck(port, 80);
		}
		return uri;
	}
	
	/**
	 * @return True if this is a JUnit test.
	 * 
	 * core usage: persistence.DB
	 */
	public static boolean isJUnitTest() {
		return jUnitTest;
	}

	/**
	 * @param b
	 */
	public static void setJUnitTest(boolean b) {
		jUnitTest = b;
	}

	/**
	 * 
	 * @return the default is OLAT
	 */
	public static String getApplicationName() {
		return applicationName;
	}
	
	public void setApplicationName(String applicationName) {
		Settings.applicationName = applicationName;
	}
	
	public static int getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(int nodeId) {
		Settings.nodeId = nodeId;
	}
	
	public void setClusterMode(String clusterMode) {
		Settings.clusterMode = clusterMode;
	}
	
	/**
	 * @return Returns the clusterMode.
	 */
	public static String getClusterMode() {
		return clusterMode;
	}

	/**
	 * @return a string like N1 oder N2 depending on which cluster node you are.
	 * Will return N1 mostly if running in single-vm-mode.
	 * IMPORTANT: as long as this is used to track errors also, the format must be the same as in error-logs!
	 * therefore also return something in single-vm-mode!
	 */
	// as long as this is used to track errors also, the format must be the same as in error-logs!
	// therefore also return something in single-vm-mode!
	public static String getNodeInfo() {
		return "N"+nodeId;
//		if (clusterMode.equalsIgnoreCase("Cluster")) {
//		return "N-"+nodeId;
//		}
//		return "";
	}

}
