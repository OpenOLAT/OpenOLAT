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
* <p>
*/
package org.olat.core.helpers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.WebappHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SettingsTest
 * 
 * <P>
 * Initial Date:  12.07.2010 <br>
 * @author patrickb
 */
public class SettingsTest {
	private static String serverFqnd = "www.myolat.org";
	private static String contextPath = "/olat";
	private static String httpDefaultPort ="80";
	private static String httpOtherPort ="8080";
	private static String httpsDefaultPort ="443";
	private static String httpsOtherPort ="8443";

	
	/**
	 * Test method for {@link org.olat.core.helpers.Settings#createServerURI()}.
	 */
	@Test
	public void testCreateServerURI() {
		Settings settings = createHttpDefaultPortSettings();
		String serverUri = settings.createServerURI();
		String expectedValue = "http://"+serverFqnd;
		assertEquals("no :port appended if default http port 80 is used.",expectedValue, serverUri);		
		
		settings = createHttpOtherPortSettings();
		serverUri = settings.createServerURI();
		expectedValue = "http://"+serverFqnd+":"+httpOtherPort;
		assertEquals("other :port appended.", expectedValue, serverUri);
		
		
		settings = createHttpsDefaultPortSettings();
		serverUri = settings.createServerURI();
		expectedValue = "https://"+serverFqnd;
		assertEquals("no :port appended if default https port 443 is used.",expectedValue, serverUri);
		
		
		settings = createHttpsOtherPortSettings();
		serverUri = settings.createServerURI();
		expectedValue = "https://"+serverFqnd+":"+httpsOtherPort;
		assertEquals("other :port appended.",expectedValue, serverUri);
	}
	
	/**
	 * Test method for {@link org.olat.core.helpers.Settings#getServerContextPathURI()}.
	 */
	@Test
	public void testGetServerContextPathURI(){

		Settings settings = createHttpDefaultPortSettings();
		String serverUriWithContext = settings.getServerContextPathURI();
		String expectedValue = "http://"+serverFqnd+contextPath;
		assertEquals("no :port appended if default http port 80 is used.",expectedValue, serverUriWithContext);		
		
		settings = createHttpOtherPortSettings();
		serverUriWithContext = settings.getServerContextPathURI();
		expectedValue = "http://"+serverFqnd+":"+httpOtherPort+contextPath;
		assertEquals("other :port appended.", expectedValue, serverUriWithContext);
		
		
		settings = createHttpsDefaultPortSettings();
		serverUriWithContext = settings.getServerContextPathURI();
		expectedValue = "https://"+serverFqnd+contextPath;
		assertEquals("no :port appended if default https port 443 is used.",expectedValue, serverUriWithContext);
		
		
		settings = createHttpsOtherPortSettings();
		serverUriWithContext = settings.getServerContextPathURI();
		expectedValue = "https://"+serverFqnd+":"+httpsOtherPort+contextPath;
		assertEquals("other :port appended.",expectedValue, serverUriWithContext);
	}
	
	/**
	 * Test method for {@link org.olat.core.helpers.Settings#getURIScheme()}.
	 */
	@Test
	public void testGetURIScheme(){
		Settings settings = createHttpDefaultPortSettings();
		String serverUriScheme = settings.getURIScheme();
		String expectedValue = "http:";
		assertEquals("no :port appended if default http port 80 is used.",expectedValue, serverUriScheme);		
		
		settings = createHttpOtherPortSettings();
		serverUriScheme = settings.getURIScheme();
		expectedValue = "http:";
		assertEquals("other :port appended.", expectedValue, serverUriScheme);
		
		
		settings = createHttpsDefaultPortSettings();
		serverUriScheme = settings.getURIScheme();
		expectedValue = "https:";
		assertEquals("no :port appended if default https port 443 is used.",expectedValue, serverUriScheme);
		
		
		settings = createHttpsOtherPortSettings();
		serverUriScheme = settings.getURIScheme();
		expectedValue = "https:";
		assertEquals("other :port appended.",expectedValue, serverUriScheme);
	}
	
	private Settings createHttpDefaultPortSettings(){
		Map<String,String> addToServerconfig = new HashMap<String,String>();
		addToServerconfig.put("server_securePort", "0");//${server.port.ssl}
		addToServerconfig.put("server_insecurePort", SettingsTest.httpDefaultPort);//${server.port}
		Settings settings = createCommonSettingsForPortTests(addToServerconfig);
		return settings;
	}
	
	private Settings createHttpOtherPortSettings(){
		Map<String,String> addToServerconfig = new HashMap<String,String>();
		addToServerconfig.put("server_securePort", "0");//${server.port.ssl}
		addToServerconfig.put("server_insecurePort", SettingsTest.httpOtherPort);//${server.port}
		Settings settings = createCommonSettingsForPortTests(addToServerconfig);
		return settings;
	}
	
	private Settings createHttpsDefaultPortSettings(){
		Map<String,String> addToServerconfig = new HashMap<String,String>();
		addToServerconfig.put("server_securePort", SettingsTest.httpsDefaultPort);//${server.port.ssl}
		addToServerconfig.put("server_insecurePort", "0");//${server.port}
		Settings settings = createCommonSettingsForPortTests(addToServerconfig);
		return settings;
	}
	
	private Settings createHttpsOtherPortSettings(){
		Map<String,String> addToServerconfig = new HashMap<String,String>();
		addToServerconfig.put("server_securePort", SettingsTest.httpsOtherPort);//${server.port.ssl}
		addToServerconfig.put("server_insecurePort", "0");//${server.port}
		Settings settings = createCommonSettingsForPortTests(addToServerconfig);
		return settings;
	}
	
	private Settings createCommonSettingsForPortTests(Map<String,String> addToServerconfig){
		Settings settings = new Settings();
		PersistedProperties persistedPropertiesHttp = Mockito.mock(PersistedProperties.class);
		Properties defaultPropertiesHttp = new Properties();
		defaultPropertiesHttp.setProperty("dummykey", "dummyvalue");
		persistedPropertiesHttp.setDefaultProperties(defaultPropertiesHttp);
		settings.setPersistedProperties(persistedPropertiesHttp);
				
		Map<String,String> serverconfig = new HashMap<String,String>(addToServerconfig);
		serverconfig.put("server_fqdn", SettingsTest.serverFqnd);//${server.domainname}
		serverconfig.put("serverContextPath", SettingsTest.contextPath);//${server.contextpath}
		WebappHelper.setServletContextPath(SettingsTest.contextPath);
		settings.setServerconfig(serverconfig);
		
		return settings;
	}
	
	

}
