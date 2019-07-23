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
*/
package org.olat.core.helpers;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.gui.control.Event;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.GenericEventListener;

/**
 * Initial Date:  12.07.2010 <br>
 * @author patrickb
 */
public class SettingsTest {
	private static String serverFqnd = "www.myolat.org";
	private static String contextPath = "/olat";
	private static int httpDefaultPort = 80;
	private static int httpOtherPort = 8080;
	private static int httpsDefaultPort = 443;
	private static int httpsOtherPort = 8443;

	
	/**
	 * Test method for {@link org.olat.core.helpers.Settings#createServerURI()}.
	 */
	@Test
	public void testCreateServerURI() {
		Settings settings = createHttpDefaultPortSettings();
		Assert.assertNotNull(settings);
		
		String serverUri = Settings.createServerURI();
		String expectedValue = "http://"+serverFqnd;
		assertEquals("no :port appended if default http port 80 is used.",expectedValue, serverUri);		
		
		settings = createHttpOtherPortSettings();
		serverUri = Settings.createServerURI();
		expectedValue = "http://"+serverFqnd+":"+httpOtherPort;
		assertEquals("other :port appended.", expectedValue, serverUri);
		
		
		settings = createHttpsDefaultPortSettings();
		serverUri = Settings.createServerURI();
		expectedValue = "https://"+serverFqnd;
		assertEquals("no :port appended if default https port 443 is used.",expectedValue, serverUri);
		
		
		settings = createHttpsOtherPortSettings();
		serverUri = Settings.createServerURI();
		expectedValue = "https://"+serverFqnd+":"+httpsOtherPort;
		assertEquals("other :port appended.",expectedValue, serverUri);
	}
	
	/**
	 * Test method for {@link org.olat.core.helpers.Settings#getServerContextPathURI()}.
	 */
	@Test
	public void testGetServerContextPathURI(){
		Settings settings = createHttpDefaultPortSettings();
		Assert.assertNotNull(settings);
		
		String serverUriWithContext = Settings.getServerContextPathURI();
		String expectedValue = "http://"+serverFqnd+contextPath;
		assertEquals("no :port appended if default http port 80 is used.",expectedValue, serverUriWithContext);		
		
		settings = createHttpOtherPortSettings();
		serverUriWithContext = Settings.getServerContextPathURI();
		expectedValue = "http://"+serverFqnd+":"+httpOtherPort+contextPath;
		assertEquals("other :port appended.", expectedValue, serverUriWithContext);
		
		
		settings = createHttpsDefaultPortSettings();
		serverUriWithContext = Settings.getServerContextPathURI();
		expectedValue = "https://"+serverFqnd+contextPath;
		assertEquals("no :port appended if default https port 443 is used.",expectedValue, serverUriWithContext);
		
		settings = createHttpsOtherPortSettings();
		serverUriWithContext = Settings.getServerContextPathURI();
		expectedValue = "https://"+serverFqnd+":"+httpsOtherPort+contextPath;
		assertEquals("other :port appended.",expectedValue, serverUriWithContext);
	}
	
	/**
	 * Test method for {@link org.olat.core.helpers.Settings#getURIScheme()}.
	 */
	@Test
	public void testGetURIScheme(){
		Settings settings = createHttpDefaultPortSettings();
		Assert.assertNotNull(settings);
		
		String serverUriScheme = Settings.getURIScheme();
		String expectedValue = "http:";
		assertEquals("no :port appended if default http port 80 is used.",expectedValue, serverUriScheme);		
		
		settings = createHttpOtherPortSettings();
		serverUriScheme = Settings.getURIScheme();
		expectedValue = "http:";
		assertEquals("other :port appended.", expectedValue, serverUriScheme);
		
		settings = createHttpsDefaultPortSettings();
		serverUriScheme = Settings.getURIScheme();
		expectedValue = "https:";
		assertEquals("no :port appended if default https port 443 is used.",expectedValue, serverUriScheme);
		
		settings = createHttpsOtherPortSettings();
		serverUriScheme = Settings.getURIScheme();
		expectedValue = "https:";
		assertEquals("other :port appended.",expectedValue, serverUriScheme);
	}
	
	public static Settings createHttpDefaultPortSettings(){
		Settings settings = createCommonSettingsForPortTests(0, SettingsTest.httpDefaultPort);
		return settings;
	}
	
	public static Settings createHttpOtherPortSettings(){
		Settings settings = createCommonSettingsForPortTests(0, SettingsTest.httpOtherPort);
		return settings;
	}
	
	public static Settings createHttpsDefaultPortSettings(){
		Settings settings = createCommonSettingsForPortTests(SettingsTest.httpsDefaultPort, 0);
		return settings;
	}
	
	public static Settings createHttpsOtherPortSettings(){
		Settings settings = createCommonSettingsForPortTests(SettingsTest.httpsOtherPort, 0);
		return settings;
	}
	
	public static Settings createCommonSettingsForPortTests(int securePort, int insecurePort){
		Settings settings = new Settings();
		PersistedProperties persistedPropertiesHttp = new PersistedProperties(new DummyListener());
		Properties defaultPropertiesHttp = new Properties();
		defaultPropertiesHttp.setProperty("dummykey", "dummyvalue");
		persistedPropertiesHttp.setDefaultProperties(defaultPropertiesHttp);
		settings.setServerSecurePort(securePort);
		settings.setServerInsecurePort(insecurePort);
		settings.setServerDomainName(SettingsTest.serverFqnd);//${server.domainname}
		if (settings.getVersion() == null) {
			// used by ConfluenceLinkSPITest
			settings.setVersion("10.4");			
		}
		// used by ConfluenceLinkSPITest
		settings.setApplicationName("OpenOLAT-jUnit-runner");
		WebappHelper.setServletContextPath(SettingsTest.contextPath);
		return settings;
	}
	
	private static class DummyListener implements GenericEventListener {
		@Override
		public void event(Event event) {
			//
		}
	}
}