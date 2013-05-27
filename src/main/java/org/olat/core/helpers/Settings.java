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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.Initializable;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.configuration.PersistedPropertiesChangedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.GenericEventListener;
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
public class Settings implements Initializable, Destroyable, GenericEventListener {

	private static boolean debug = false;
	private static boolean allowLoadtestMode = false;
	private static boolean readOnlyDebug = false;
	private static boolean ajaxGloballyOnBoolean = false;
	private static String guiThemeIdentifyer = "default";
	private static Map<String, String> serverconfig = null;
	private static List<Pattern> ajaxBlacklistPatterns = new ArrayList<Pattern>();
	private static boolean jUnitTest;
	private static final String KEY_SERVER_MODJK_ENABLED = "server_modjk_enabled";
	// the persited properties contain user configurable config data (overrides
	// default values from spring config)
	private static PersistedProperties persistedProperties;
	private static String applicationName;
	private static String version;
	private static String buildIdentifier;
	private static OLog log = Tracing.createLoggerFor(Settings.class);


	private static final String KEY_GUI_THEME_IDENTIFYER = "layout.theme";
	private static int nodeId;
	private static String clusterMode;
	private static Date buildDate;
	private static String repoRevision;
	private static String patchRepoRevision;
	private static String crossOriginFilter;
	
	/**
	 * [used by spring]
	 */
	Settings() {
		//
	}
	
	// fxdiff: only set build id from build date if none is provided in olat.local.properties!
	private static void setBuildIdFromBuildDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		buildIdentifier = formatter.format(buildDate);
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
	
	//fxdiff: get the mercurial changeset Information from the time this release had been built
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

	public void setPersistedProperties(PersistedProperties persistedProperties) {
		Settings.persistedProperties = persistedProperties;
	}

	/**
	 * @return
	 */
	public static boolean isDebuging() {
		return debug;
	}

	/**
	 * @return if ajax mode is system-wide enabled or not
	 */
	public static boolean isAjaxGloballyOn() {
		return ajaxGloballyOnBoolean;
	}


	public static boolean isAllowLoadtestMode() {
		return allowLoadtestMode;
	}
	
	/**
	 * [spring]
	 * @param allowLoadtestMode
	 */
	public void setAllowLoadtestMode(boolean allowLoadtestMode) {
		Settings.allowLoadtestMode = allowLoadtestMode;
	}
	
	public static boolean isReadOnlyDebug() {
		return readOnlyDebug;
	}

	/**
	 * [spring]
	 * @param readOnlyDebug
	 */
	public void setReadOnlyDebug(boolean readOnlyDebug) {
		Settings.readOnlyDebug = readOnlyDebug;
	}
	
	/**
	 * affects only new usersessions
	 * [spring]
	 * @param 
	 */
	public void setAjaxGloballyOn(boolean ajaxGloballyOn) {
		Settings.ajaxGloballyOnBoolean = ajaxGloballyOn;
	}
	
	/**
	 * for direct static access from code
	 * @param ajaxGloballyOn
	 */
	public static void setAjaxGloballyEnabled(boolean ajaxGloballyOn) {
		Settings.ajaxGloballyOnBoolean = ajaxGloballyOn;
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
	
	
	
	
	/**
	 * 	key='server_name'
			key='server_fqdn'
			key='server_securePort'
			key='server_insecurePort'
			key='server_modjk_enabled'
			key='server_core_jar_name'
			key="serverContextPath"
	 * @return
	 */
	
	//TODO getServerconfig only by enum to be less input tolerant
	public static String getServerconfig(String key){
		if("serverContextPath".equals(key)) {
			return WebappHelper.getServletContextPath();
		}
		return Settings.serverconfig.get(key);
	}
	
	/**
	 * [spring]
	 * @param serverconfig
	 */
	public void setServerconfig(Map<String,String> serverconfig){
		Settings.serverconfig = serverconfig;
	}

	/**
	 * [spring]
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		Settings.debug = debug;
	}
	
	/**
	 * @see org.olat.core.configuration.ServiceLifeCycle#init()
	 */
	public void init() {
		// Initialize the user configuration and the spring default configuration
		//
		// Set the default theme configured in the spring configuration
		persistedProperties.setStringPropertyDefault(KEY_GUI_THEME_IDENTIFYER, guiThemeIdentifyer);		
		// Override gui theme with value from properties configuration
		guiThemeIdentifyer = persistedProperties.getStringPropertyValue(KEY_GUI_THEME_IDENTIFYER, false);
	}

	/**
	 * @see org.olat.core.configuration.ServiceLifeCycle#destroy()
	 */
	public void destroy() {
		if (persistedProperties != null) {
			persistedProperties.destroy();
			persistedProperties = null;
		}
	}


	/**
	 * @return the CSS theme used for this webapp
	 */
	public static String getGuiThemeIdentifyer() {
		return guiThemeIdentifyer;			
	}

	/**
	 * Set the CSS theme used for this webapp. Only used by spring. Use static
	 * method to change the theme at runtime!
	 * 
	 * @param guiTheme
	 */
	public void setGuiThemeIdentifyer(String guiThemeIdentifyer) {
		Settings.guiThemeIdentifyer = guiThemeIdentifyer;
	}

	/**
	 * Set the CSS theme used for this webapp. The configuration is stored in
	 * the olatdata/system/configuration properties file and overrides the
	 * spring default configuration.
	 * 
	 * @param newGuiThemeIdentifyer
	 */
	public static void setGuiThemeIdentifyerGlobally(String newGuiThemeIdentifyer) {
		if (!guiThemeIdentifyer.equals(newGuiThemeIdentifyer)) {
			// store new configuration and notify other nodes
			persistedProperties.setStringProperty(KEY_GUI_THEME_IDENTIFYER, newGuiThemeIdentifyer, true);
		}
	}
	
	public void event(Event event) {
		if (event instanceof PersistedPropertiesChangedEvent) {
			// Override gui theme with value from properties configuration
			guiThemeIdentifyer = persistedProperties.getStringPropertyValue(KEY_GUI_THEME_IDENTIFYER, false);
		}
	}

	
	/**
	 * check if mod jk is enabled
	 * @return
	 */
	public static boolean isModjkEnabled() {
		return Settings.serverconfig.containsKey(KEY_SERVER_MODJK_ENABLED) 
				 && Settings.serverconfig.get(KEY_SERVER_MODJK_ENABLED).equalsIgnoreCase("true");
	}
	
	
	public static String getURIScheme() {
		return (isSecurePortAvailable() ? "https:" : "http:");
	}

	private static boolean isSecurePortAvailable() {
		return ! Settings.getServerconfig("server_securePort").equals("0");
	}


	public static String createServerURI() {
		String uri;
		String port;
		
		if (isSecurePortAvailable()) {
			port = Settings.getServerconfig("server_securePort");
			uri = "https://" + Settings.getServerconfig("server_fqdn") + createURIPortPartWithDefaultPortCheck(port, 443);
		} else {
			port = Settings.getServerconfig("server_insecurePort");
			uri = "http://" + Settings.getServerconfig("server_fqdn") + createURIPortPartWithDefaultPortCheck(port, 80);
		}
		return uri;
	}
	
	/**
	 * @param configuredPort comes from the spring configuration file, null is converted to ""
	 * @param defaultPort use 80 for http, 443 for https
	 * @return "" if no port is defined, or a default port. i.e. ":8080" if a port is configured and non-standard
	 */
	private static String createURIPortPartWithDefaultPortCheck(String configuredPort, int defaultPort){

		if( ! StringHelper.containsNonWhitespace(configuredPort)){
			return "";
		}

		int portFromConfig = Integer.valueOf(configuredPort);
		if(portFromConfig == defaultPort){
			return "";
		}else{
			return ":" + portFromConfig;
		}
	}
	
	/**
	 * 
	 * @return the full server path like https://www.olat.uzh.ch/olat
	 * depending on settings like secure/insecure, context path, ...
	 */
	public static String getServerContextPathURI() {
		return createServerURI() + WebappHelper.getServletContextPath();
	}
	
	public static String getCrossOriginFilter() {
		return crossOriginFilter;
	}
	
	public void setCrossOriginFilter(String crossOriginFilter) {
		Settings.crossOriginFilter = crossOriginFilter;
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
