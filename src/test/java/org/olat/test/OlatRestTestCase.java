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

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.olat.core.helpers.SettingsTest;
import org.olat.core.logging.Tracing;
import org.olat.restapi.RestModule;
import org.olat.restapi.security.RestApiLoginFilter;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.LinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.core.UriBuilder;

/**
 * 
 * Description:<br>
 * Abstract class which start and stop an undertow server
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public abstract class OlatRestTestCase extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(OlatRestTestCase.class);
	
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
  
	public OlatRestTestCase() {
		super();
	}
	
	/**
	 * Instantiates the server
	 */
	@Before
	public void instantiateServer() {
		if(webServer == null) {
			SettingsTest.createHttpDefaultPortSettings();
			try {
				ServletInfo cxfInfo = servlet("CXFServlet", org.apache.cxf.transport.servlet.CXFServlet.class)
		        		.setMultipartConfig(new MultipartConfigElement((String)null))
		        		.addMapping("/*");
				
				DeploymentInfo servletBuilder = deployment()
				        .setClassLoader(OlatRestTestCase.class.getClassLoader())
				        .setContextPath("/" + CONTEXT_PATH) 
				        .setDeploymentName("rest.war")
				        .addServlets(cxfInfo)
				        .addFilters(filter("REST security filter", RestApiLoginFilter.class))
				        .addFilterUrlMapping("REST security filter", "/*", DispatcherType.REQUEST);

				DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
				manager.deploy();
				manager.getDeployment()
					.getServletContext()
					.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
				
				HttpHandler servletHandler = manager.start();
	            PathHandler path = Handlers.path(Handlers.redirect("/" + CONTEXT_PATH))
	                    .addPrefixPath("/" + CONTEXT_PATH, servletHandler);

				webServer = Undertow.builder()
				        .addHttpListener(PORT, HOST)
				        .setHandler(path)
				        .build();
			} catch (ServletException e) {
				log.error("", e);
			}
		}
		
		restModule.setEnabled(true);
		if(!webServerStarted) {
			webServer.start();
			webServerStarted = true;
			log.info("Starting the Undertow...");
		}
	}
	
	protected URI getBaseURI() {
    return UriBuilder.fromUri(PROTOCOL + "://" + HOST + "/").port(PORT).build();
	}
	
	protected URI getContextURI() {
		return UriBuilder.fromUri(getBaseURI()).path(CONTEXT_PATH).build();
	}
	
	protected List<ErrorVO> parseErrorArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<ErrorVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<LinkVO> parseLinkArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<LinkVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<FileVO> parseFileArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<FileVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}