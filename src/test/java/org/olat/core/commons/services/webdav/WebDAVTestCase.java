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

import java.io.IOException;

import javax.servlet.Servlet;

import org.junit.BeforeClass;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.servlets.OpenOLATServlet;
import org.olat.test.OlatTestCase;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;

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
	private static final OLog log = Tracing.createLoggerFor(WebDAVTestCase.class);

	public final static int PORT = 9997;
	public final static String HOST = "localhost";
	public final static String PROTOCOL = "http";
	public final static String CONTEXT_PATH = "webdav";

	private static GrizzlyWebServer webServer;
	
	@BeforeClass
	public static void setUp() throws Exception {
		try {
			if(webServer == null) {
				webServer = new GrizzlyWebServer(PORT);
				webServer.useAsynchronousWrite(false);
				ServletAdapter sa = new ServletAdapter();
				Servlet servletInstance = null;
				try {
					servletInstance = new OpenOLATServlet();
				} catch (Exception ex) {
					log.error("Cannot instantiate the Grizzly Servlet Container", ex);
				}
				sa.setServletInstance(servletInstance);
				sa.addInitParameter("debug", "0");
				sa.addInitParameter("input", "32768");
				sa.addInitParameter("output", "32768");
				sa.addInitParameter("listings", "true");
				sa.addInitParameter("readonly", "false");
				sa.setContextPath("/" + CONTEXT_PATH);
				webServer.addGrizzlyAdapter(sa, new String[]{""});

				log.info("Starting the Grizzly Web Container for WebDAV...");
				webServer.start();
			}
		} catch (IOException ex) {
			log.error("Cannot start the Grizzly Web Container for WebDAV");
		}
	}
}