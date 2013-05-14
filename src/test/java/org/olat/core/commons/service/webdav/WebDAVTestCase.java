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

package org.olat.core.commons.service.webdav;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.servlet.Servlet;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.olat.core.commons.services.webdav.SecureWebdavServlet;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;

/**
 * 
 * Description:<br>
 * Abstract class which start and stop a grizzly server for every test
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class WebDAVTestCase extends OlatTestCase {
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
					servletInstance = new SecureWebdavServlet();
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
	
	@Test
	public void testPropFind()
	throws IOException, URISyntaxException {
		//create a user
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("webdav-1-" + UUID.randomUUID().toString());
		
		//list root content of its webdav folder
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user.getName(), "A6B7C8");
		URI uri = conn.getContextURI().build();
		HttpResponse response = conn.propfind(uri);
		assertEquals(207, response.getStatusLine().getStatusCode());
		
		String xml = EntityUtils.toString(response.getEntity());
		Assert.assertTrue(xml.indexOf("/webdav/coursefolders/") > 0);
	}
}