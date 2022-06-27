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

package org.olat.core.commons.services.webdav;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

import javax.servlet.ServletException;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.olat.core.helpers.SettingsTest;
import org.olat.core.logging.Tracing;
import org.olat.core.servlets.OpenOLATServlet;
import org.olat.test.OlatTestCase;

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

/**
 * 
 * Description:<br>
 * Abstract class which start and stop a grizzly server for every test run
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public abstract class WebDAVTestCase extends OlatTestCase {
	private static final Logger log = Tracing.createLoggerFor(WebDAVTestCase.class);

	public final static int PORT = 9997;
	public final static String HOST = "localhost";
	public final static String PROTOCOL = "http";

	private static Undertow webServer;
	
	@BeforeClass
	public static void setUp() throws Exception {
		try {
			if(webServer == null) {
				SettingsTest.createHttpDefaultPortSettings();
				
				DeploymentInfo servletBuilder = deployment()
	                    .setClassLoader(WebDAVTestCase.class.getClassLoader())
	                    .setContextPath("/")
	                    .setDeploymentName("test.war")
	                    .addServlets(
	                            servlet("MessageServlet", OpenOLATServlet.class)
	                                    .addInitParam("message", "Hello World")
	                                    .addMapping("/*"));

	            DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
	            manager.deploy();

	            webServer = Undertow.builder()
	                    .addHttpListener(PORT, HOST)
	                    .setHandler(manager.start())
	                    .build();
	            webServer.start();
			}
		} catch (ServletException ex) {
			log.error("Cannot start the Grizzly Web Container for WebDAV");
		}
	}
}