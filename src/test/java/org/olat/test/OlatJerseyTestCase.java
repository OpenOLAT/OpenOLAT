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

package org.olat.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.restapi.RestModule;
import org.olat.restapi.security.RestApiLoginFilter;
import org.olat.restapi.support.OlatRestApplication;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.LinkVO;
import org.springframework.beans.factory.annotation.Autowired;

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
public abstract class OlatJerseyTestCase extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(OlatJerseyTestCase.class);
	
	protected static final JsonFactory jsonFactory = new JsonFactory();
	
	public final static String PACKAGE_NAME = "org.olat.restapi";
	public final static String CONTEXT_PATH = "olat";
	
	public final static int PORT = 9998;
	public final static String HOST = "localhost";
	public final static String PROTOCOL = "http";

	private static boolean webServerStarted = false;
	private static GrizzlyWebServer webServer;
	
	@Autowired
	private RestModule restModule;
  
	/**
	 * @param arg0
	 */
	public OlatJerseyTestCase() {
		super();
		instantiateGrizzlyWebServer();
	}
	
	/**
	 * Instantiates the Grizzly Web Server
	 */
	private void instantiateGrizzlyWebServer() {
		if(webServer == null) {
			webServer = new GrizzlyWebServer(PORT);
			ServletAdapter sa = new ServletAdapter();
			Servlet servletInstance = null;
			try {
				servletInstance = (HttpServlet)Class.forName("com.sun.jersey.spi.container.servlet.ServletContainer").newInstance();
			} catch (Exception ex) {
				log.error("Cannot instantiate the Grizzly Servlet Container", ex);
			}
			sa.setServletInstance(servletInstance);
			sa.addFilter(new RestApiLoginFilter(), "jerseyfilter", null);
			sa.addInitParameter("javax.ws.rs.Application", OlatRestApplication.class.getName());
			sa.setContextPath("/" + CONTEXT_PATH);
			webServer.addGrizzlyAdapter(sa, null);
		}
	}
	
	protected URI getBaseURI() {
    return UriBuilder.fromUri(PROTOCOL + "://" + HOST + "/").port(PORT).build();
	}
	
	protected URI getContextURI() {
		return UriBuilder.fromUri(getBaseURI()).path(CONTEXT_PATH).build();
	}
	
  @Before
  public void setUp() throws Exception {
  	//always enabled the REST API for testing
		restModule.setEnabled(true);
  	
		try {
			if(!webServerStarted) {
				log.info("Starting the Grizzly Web Container...");
				webServer.start();
			}
		} catch (IOException ex) {
			log.error("Cannot start the Grizzly Web Container");
		}
  }
	
	protected List<ErrorVO> parseErrorArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<ErrorVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<LinkVO> parseLinkArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<LinkVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<FileVO> parseFileArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<FileVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}