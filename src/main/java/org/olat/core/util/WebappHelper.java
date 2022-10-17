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
package org.olat.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import jakarta.servlet.ServletContext;

import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.Initializable;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  04.01.2007 <br>
 *
 * @author Felix Jost
 */
public class WebappHelper implements Initializable, Destroyable, ServletContextAware {
	
	private static final Logger log = Tracing.createLoggerFor(WebappHelper.class);
	private static int nodeId;
	private static final String bootId = UUID.randomUUID().toString();
	private static String fullPathToSrc;
	private static String fullPathToWebappSrc;
	private static ServletContext servletContext;
	private static String contextRoot;
	private static String instanceId;
	private static String userDataRoot;
	private static String defaultCharset;
	private static boolean enforceUtf8Filesystem;
	private static Map<String, String> mailConfig = new HashMap<>(6);
	private static long timeOfServerStartup = System.currentTimeMillis();
	
	private static String mathJaxCdn;
	private static String mathJaxConfig;
	private static boolean mathJaxMarkers;
	private static String mathLiveCdn;
	
	private static String mobileContext;
	
	/** need to set this at least once before the actual request, since we cannot extract it from the servletContext, 
	 * but many methods use it (renderers) which do not have access to userRequest and thus to to getPathInfo...**/
	private static String servletContextPath;
	private String applicationName;
	private String version;
	private static String buildJdk;
	private static String changeSet;
	private static String changeSetDate;
	private static String revisionNumber;
	private static String implementationVersion;
	

	/**
	 * 
	 * @see Initializable
	 */
	public void init() {
		//servletContext.getRealPath("/");  does not work with an unpacked war file we only use it for fallback for unit testing
		Resource res = new ClassPathResource(CoreSpringFactory.class.getCanonicalName().replaceAll("\\.", "\\/")+".class");
		try {
			String fullPath = res.getURL().toString();

			if (fullPath.contains(File.separator + "WEB-INF")) {
				fullPath = fullPath.substring(fullPath.indexOf("file:")+5, fullPath.indexOf(File.separator + "WEB-INF"));
			} else {
				fullPath = servletContext.getRealPath("/");
			}
			log.info("Sucessfully extracted context root path as: "+fullPath);
			contextRoot = fullPath;
		} catch (Exception e) {
			throw new StartupException("Error getting canonical context root.", e);
		}
		servletContextPath = servletContext.getContextPath();
		
		try(InputStream meta = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF")) {
			if(meta != null) {
				Properties props = new Properties();
				props.load(meta);
				changeSet = props.getProperty("Build-Change-Set");
				changeSetDate = props.getProperty("Build-Change-Set-Date");
				revisionNumber = props.getProperty("Build-Revision-Number");
				implementationVersion = props.getProperty("Implementation-Version");
				buildJdk = props.getProperty("Build-Jdk");
			}
		} catch (Exception e) {
			log.warn("MANIFEST.MF not found", e);
		}

		File fil = new File(fullPathToSrc);
		if(fil.exists()){
			log.info("Path to source set to: " + fullPathToSrc);
		} else {
			log.info("Path to source doesn't exist (only needed for debugging purpose): " + fullPathToSrc);
		}
		
		testUtf8FileSystem();
		logStartup();
	}
	
	/**
	 * [used by spring]
	 * @param nodeId
	 */
	public void setNodeId(int nodeId) {
		WebappHelper.nodeId = nodeId;
		System.setProperty("nodeId", Integer.toString(nodeId));
	}

	/**
	 * 
	 * @return
	 */
	public static int getNodeId() {
		return nodeId;
	}
	
	/**
	 * @return Return a unique ID per node and per reboot
	 */
	public static String getBootId() {
		return bootId;
	}

	/**
	 * implements service interface
	 */
	public void destroy() {
		log.info("");
		log.info("*********************************************");
		log.info("*                SHUTDOWN                    ");
		log.info("*********************************************");
		log.info("* Application:   " + applicationName);
		log.info("* StopTimeStamp: " + new Date());
		log.info("*********************************************");
		log.info("");
	}
	
	/**
	 * [spring]
	 * @param applicationName
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * @return the context path like "olat" in localhost:8080/olat
	 */
	public static String getServletContextPath() {
		return servletContextPath;
	}
	
	/**
	 * [used by JUnitTest]
	 * @param contextPath
	 */
	public static void setServletContextPath(String contextPath) {
		servletContextPath = contextPath;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static String getMimeType(String fileName) {
		return servletContext.getMimeType(fileName.toLowerCase());
	}

	/**
	 * This path is depends how you deploy OpenOLAT. When deploying in eclipse you will get an different path 
	 * than deploying within tomcat and a different on JBoass AS / Wildfly. Use instead
	 * @see getContextRealPath(java.lang.String path) to retrieve a path
	 * @return the root folder of the webapplication, e.g. /opt/olat3/olat/target/classes  (no trailing slash)
	 */
	public static String getContextRoot() {
		return contextRoot;
	}
	
	public static String getContextRealPath(String path) {
		if(Settings.isJUnitTest()) {
			//The mocked servlet context of spring doesn't have a real real path
			return contextRoot + path;
		}
		return servletContext.getRealPath(path);
	}
	
	/**
	 * needed only for development; with debug mode enabled.
 	 * The returned path does never end with a slash
	 * @return the absolute path to the application webapp source directory, e.g. /opt/olat3/webapp/WEB-INF/src"  (no trailing slash) 
	 */
	public static String getSourcePath() {
		//String srcPath = getContextRoot() + "/" + relPathToSrc;
		File fil = new File(fullPathToSrc);
		if(fil.exists()){
			log.debug("Path to source set to: " + fullPathToSrc);
		} else if (Settings.isDebuging()) {
			log.error("Path to source wrong, debugging may not work as expected: " + fullPathToSrc, new Exception("getSourcePath"));
		} else {
			log.info("Path to source not valid: " + fullPathToSrc);
		}
		
		return fullPathToSrc;
	}
	
	public static String getWebappSourcePath() {
		File webapp = new File(fullPathToWebappSrc);
		if(webapp.exists()){
			return fullPathToWebappSrc;
		}else{
			return null;
		}
	}

	public static String getBuildOutputFolderRoot() {
		try {
			String resource = "/serviceconfig/olat.properties";
			Resource res = new ClassPathResource(resource);
			String protocol = res.getURL().getProtocol();
			if("file".equals(protocol)) {
				String path = res.getFile().getParentFile().getParentFile().getAbsolutePath();
				return path;
			} else {
				return null;
			}
		} catch (IOException e) {
			log.error("Path to build output is not accessible", e);
			return null;
		}
	}

	/**
	 * @return the time when the server was started up
	 * used e.g. for stats or a Last-modified of resources which are in jars and do not have a last-modified available
	 */
	public static Long getTimeOfServerStartup() {
		return timeOfServerStartup;
	}

	/**
	 * @return The context of the moible application starting with /
	 */
	public static String getMobileContext() {
		return mobileContext;
	}

	public void setMobileContext(String mobileContext) {
		if(!mobileContext.startsWith("/")) {
			mobileContext = "/" + mobileContext;
		}
		
		WebappHelper.mobileContext = mobileContext;
	}
	
	public static String getMathJaxCdn() {
		return mathJaxCdn;
	}

	public void setMathJaxCdn(String mathJaxCdn) {
		WebappHelper.mathJaxCdn = mathJaxCdn;
	}

	public static String getMathJaxConfig() {
		return mathJaxConfig;
	}

	public void setMathJaxConfig(String mathJaxConfig) {
		WebappHelper.mathJaxConfig = mathJaxConfig;
	}
	
	public static boolean isMathJaxMarkers() {
		return mathJaxMarkers;
	}
	
	public void setMathJaxMarkers(boolean mathJaxMarkers) {
		WebappHelper.mathJaxMarkers = mathJaxMarkers;
	}

	public static String getMathLiveCdn() {
		return mathLiveCdn;
	}

	public void setMathLiveCdn(String mathLiveCdn) {
		WebappHelper.mathLiveCdn = mathLiveCdn;
	}

	public void setFullPathToSrc(String fullPathToSrc) {
		File path = new File(fullPathToSrc);
		if (path.exists()) {
			WebappHelper.fullPathToSrc = fullPathToSrc;			
		} else {
			log.debug("Invalid fullPathToSrc configuration, reset to empty path");
			WebappHelper.fullPathToSrc = "";
		}
 	}

	public void setFullPathToWebappSrc(String fullPathToWebappSrc) {
		File path = new File(fullPathToWebappSrc);
		if (path.exists()) {
			WebappHelper.fullPathToWebappSrc = fullPathToWebappSrc;
		} else {
			log.debug("Invalid fullPathToWebappSrc configuration, reset to empty path");
			WebappHelper.fullPathToWebappSrc = "";
		}
 	}
	
	/**
	 * 
	 * @return
	 */
	public static String getInstanceId() {
		return instanceId;
	}

	/**
	 * [spring]
	 * @param instanceId
	 */
	public void setInstanceId(String instanceId) {
		if (instanceId == null) throw new StartupException("No instance id set for this installation (see olat.properties). Please fix!");
		if (instanceId.length() > 10) throw new StartupException("InstanceID is limited to 10 characters (see olat.properties). Please fix!", null);
		WebappHelper.instanceId = instanceId;
	}

	public static String getUserDataRoot() {
		return userDataRoot;
	}
	
	public static String getTmpDir() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * [spring]
	 * @param userDataRoot
	 */
	public void setUserDataRoot(String userDataRoot) {
		if (!StringHelper.containsNonWhitespace(userDataRoot)) {
			userDataRoot = System.getProperty("java.io.tmpdir") + "/olatdata";
			log.info("using java.io.tmpdir as userdata. this is the default if userdata.dir is not set");
		}
		File fUserData = new File(userDataRoot);
		if (!fUserData.exists()) {
			if (!fUserData.mkdirs()) throw new StartupException("Unable to create userdata dir '" + userDataRoot + "'. Please fix!");
		}
		//fxdiff: reset tmp-dir from within application to circumvent startup-params. 
		//do not write to system default (/var/tmp) as this leads to permission problems with multiple instances on one host!
		log.info("Setting userdata root to: "+userDataRoot);
		WebappHelper.userDataRoot = userDataRoot;
	}

	/**
	 * [spring]
	 * @param mailConfig
	 */
	public void setMailConfig(Map<String, String> mailConfig) {
		WebappHelper.mailConfig = mailConfig;
		String mailHost = WebappHelper.mailConfig.get("mailhost");
		if (mailHost == null) log.warn("Attention! mailhost fqdn is not set. The system has restricted functionality, e.g. Self Registration will not work!");
	}

	public static String getDefaultCharset() {
		if (defaultCharset == null) return "ISO-8859-1";
		return defaultCharset;
	}

	/**
	 * [spring]
	 * @param defaultCharset
	 */
	public void setDefaultCharset(String defaultCharset) {
		WebappHelper.defaultCharset = defaultCharset;
	}

	/**
	 * [spring]
	 * @param enforceUtf8Filesystem
	 */
	public void setEnforceUtf8Filesystem(boolean enforceUtf8Filesystem) {
		WebappHelper.enforceUtf8Filesystem = enforceUtf8Filesystem;
	}

	/**
	 *	key="mailhost"
	 *	key="mailTimeout"
	 *	key="smtpUser"
	 *	key="smtpPwd"
	 *	key="mailSupport"
	 *  key="mailReplyTo" - default from email address (reply-to)
	 *  key="mailFromDomain" - own domain of our smtp server where it is allowed to use foreign addresses
	 *  key="mailFrom" - real from email address
	 *  key="mailFromName" - plain text name for from address
	 * @param string
	 * @return
	 */
	public static String getMailConfig(String key) {
		return WebappHelper.mailConfig.get(key);
	}

	public static String setMailConfig(String key, String value) {
		return WebappHelper.mailConfig.put(key, value);
	}

	public static boolean isMailHostAuthenticationEnabled() {
		return StringHelper.containsNonWhitespace(getMailConfig("smtpUser"));
	}
	

	/**
	 * Test if filesystem is capable to store UTF-8 characters
	 * Try to read/write a file with UTF-8 chars in the filename in a temporary directory.
	 *
	 */
	private void testUtf8FileSystem() {
		File tmpDir = new File(new File(WebappHelper.getUserDataRoot()), "tmp");
		if (!tmpDir.exists()) tmpDir.mkdir();
		File writeFile = new File(tmpDir, "UTF-8 test läsÖiç-首页f新");
		if (writeFile.exists()) {
			// remove exising files first
			try {
				Files.deleteIfExists(writeFile.toPath());
			} catch (IOException e) {
				log.warn("Cannot delete test file for UTF-8 file system.", e);
			}
		}
		try {
			if(!writeFile.createNewFile()) {
				log.warn("No UTF-8 capable filesystem found! Error while writing testfile to filesystem");
			}
		} catch (IOException e) {
			log.warn("No UTF-8 capable filesystem found! Error while writing testfile to filesystem", e);
		}
		// try to lookup file: get files from filesystem and search for file we created above
		File[] tmpFiles = tmpDir.listFiles();
		boolean foundUtf8File = false;
		if(tmpFiles != null){
			for (int i = 0; i < tmpFiles.length; i++) {
				File tmpFile = tmpFiles[i];
				if (tmpFile.getName().equals("UTF-8 test läsÖiç-首页f新")) {
					foundUtf8File = true;
					break;
				}
			}
		}
		if (foundUtf8File) {
			// test ok
			log.info("UTF-8 capable filesystem detected");				
		} else {
			// test failed
			log.warn("No UTF-8 capable filesystem found! Could not read / write UTF-8 characters from / to filesystem! "
					+ "You probably misconfigured your system, try setting your LANG variable to a correct value.");
			log.warn("Your current file encoding configuration: java.nio.charset.Charset.defaultCharset().name()::"
					+ java.nio.charset.Charset.defaultCharset().name() + " (the one used) and your system property file.encoding::"
					+ System.getProperty("file.encoding") + " (the one configured)");
		}
		// try to delete file anyway
		try {
			Files.deleteIfExists(writeFile.toPath());
		} catch (IOException e) {
			log.warn("Cannot delete test file for UTF-8 file system.", e);
		}
		
		if (!foundUtf8File && WebappHelper.enforceUtf8Filesystem) {
			throw new BeanInitializationException(
					"System startup aborted to to file system missconfiguration. See previous warnings in logfile and fix your " + 
					"Java environment. This check can be disabled by setting enforce.utf8.filesystem=false, but be aware that the " +
					"decision to use a certain encoding on the filesystem is a one-time decision. You can not cange to UTF-8 later!");			
		}
	}
	
	/**
	 * Either the version of the core or the webapps provided version gets printed
	 */
	private void logStartup() {
		log.info("");
		log.info("*********************************************");
		log.info("*                STARTUP                     ");
		log.info("*********************************************");
		log.info("* Application:    " + applicationName);
		log.info("* Version:        " + version);
		log.info("* StartTimeStamp: " + new Date());
		log.info("*********************************************");
		log.info("");
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public static String getBuildJdk() {
		return buildJdk;
	}

	public static String getChangeSet() {
		return changeSet;
	}

	public static String getChangeSetDate() {
		return changeSetDate;
	}
	
	public static String getRevisionNumber() {
		return revisionNumber;
	}

	public static String getImplementationVersion() {
		return implementationVersion;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		WebappHelper.servletContext = servletContext;
	}
	
}
