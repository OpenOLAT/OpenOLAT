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
*/
package org.olat.test;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * Description:<br>
 * MockServletContextWebContextLoader
 * 
 * <P>
 * Initial Date:  13.01.2010 <br>
 * @author guido
 */
public class MockServletContextWebContextLoader extends AbstractContextLoader {

		
		protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {
			return new XmlBeanDefinitionReader(context);
		}

		public final XmlWebApplicationContext loadContext(final String... locations) throws Exception {

			System.out.println("Loading ApplicationContext for locations ["
					+ StringUtils.arrayToCommaDelimitedString(locations) + "].");


			XmlWebApplicationContext appContext = new XmlWebApplicationContext();
			// ResourceBasePath has to be set since the Spring 3 implementation of MockServletContext is only
			// compatible wit Servlet 2.5, but not with Servlet 3.0. Should be fixed for Spring 4.
			MockServletContext servletContext = new MockServletContext("/META-INF/resources");
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext); 
			MockPropertySource propertySource = new MockPropertySource();
			propertySource.setProperty("jms.provider", "activemq");
			appContext.getEnvironment().getPropertySources().addFirst(propertySource);
			appContext.setServletContext(servletContext);  
			appContext.setConfigLocations(locations);
			appContext.refresh();  
			appContext.registerShutdownHook();  

			return appContext;

		}

		@Override
		public ApplicationContext loadContext(MergedContextConfiguration config) throws Exception {
			return loadContext(config.getLocations());
		}

		@Override
		protected String getResourceSuffix() {
			return "";
		}

}