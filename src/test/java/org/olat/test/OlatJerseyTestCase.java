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

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.filter;
import static io.undertow.servlet.Servlets.servlet;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
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

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

/**
 * 
 * Description:<br>
 * Abstract class which start and stop an undertow server
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
	private static Undertow webServer;
	
	@Autowired
	private RestModule restModule;
  
	/**
	 * @param arg0
	 */
	public OlatJerseyTestCase() {
		super();
		instantiateServer();
	}
	
	/**
	 * Instantiates the server
	 */
	private void instantiateServer() {
		if(webServer == null) {
			try {
				DeploymentInfo servletBuilder = deployment()
				        .setClassLoader(OlatJerseyTestCase.class.getClassLoader())
				        .setContextPath("/" + CONTEXT_PATH)
				        .setDeploymentName("rest.war")
				        .addServlets(
				                servlet("REST Servlet",  com.sun.jersey.spi.container.servlet.ServletContainer.class)
		        		        		.addInitParam("javax.ws.rs.Application", OlatRestApplication.class.getName())
		        		        		.setMultipartConfig(new MultipartConfigElement((String)null))
				                        .addMapping("/*"))
				        .addFilters(filter("REST security filter", RestApiLoginFilter.class))
				        .addFilterUrlMapping("REST security filter", "/*", DispatcherType.REQUEST);

				DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
				manager.deploy();

				webServer = Undertow.builder()
				        .addHttpListener(PORT, HOST)
				        .setHandler(manager.start())
				        .build();
				webServer.start();
				webServerStarted = true;
			} catch (ServletException e) {
				log.error("", e);
			}
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

	if(!webServerStarted) {
		log.info("Starting the Grizzly Web Container...");
		webServer.start();
		webServerStarted=true;
	}
  }
	
	protected List<ErrorVO> parseErrorArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<ErrorVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<LinkVO> parseLinkArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<LinkVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<FileVO> parseFileArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<FileVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}