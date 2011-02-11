package org.olat.core.test;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockServletContext;
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
			MockServletContext servletContext = new MockServletContext(); 

			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);  

			appContext.setServletContext(servletContext);  
			appContext.setConfigLocations(locations);
			appContext.refresh();  
			appContext.registerShutdownHook();  

			return appContext;

		}

		@Override
		protected String getResourceSuffix() {
			// TODO Auto-generated method stub
			return "";
		}

}