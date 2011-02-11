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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Description:<br>
 * The core spring factory is used to load resources and spring beans. The
 * application context is generated from XML files. Normally you should not use this class and instead
 * inject dependencies at xml level or with autowire!
 * 
 * <P>
 * Initial Date: 12.06.2006 <br>
 * 
 * @author patrickb
 */
public class CoreSpringFactory implements ServletContextAware, BeanFactoryAware {
	// Access servletContext only for spring beans admin-functions
	public static ServletContext servletContext;
	private static List<String> beanNamesCalledFromSource = new ArrayList<String>();
	private static OLog log = Tracing.createLoggerFor(CoreSpringFactory.class);
	private static DefaultListableBeanFactory beanFactory;
	
	/**
	 * [used by spring only]
	 */
	private CoreSpringFactory() {
		//
	}


	/**
	 * wrapper to the applicationContext (we are facading spring's
	 * applicationContext)
	 * 
	 * @param path
	 *            a path in spring notation (e.g. "classpath*:/*.hbm.xml", see
	 *            springframework.org)
	 * @return the resources found
	 */
	public static Resource[] getResources(String path) {
		Resource[] res;
		try {
			ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
			res = context.getResources(path);
		} catch (IOException e) {
			throw new AssertException(
					"i/o error while asking for resources, path:" + path);
		}
		return res;
	}

	/**
	 * @param beanName
	 *            The bean name to check for. Be sure the bean does exist,
	 *            otherwise an NoSuchBeanDefinitionException will be thrown
	 * @return The bean
	 * 
	 */
	public static Object getBean(String beanName) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
		Object o = context.getBean(beanName);
		beanNamesCalledFromSource.add(beanName);
		return o;
	}

	/**
	 * @param beanName
	 *            The bean name to check for. Be sure the bean does exist,
	 *            otherwise an NoSuchBeanDefinitionException will be thrown
	 * @return The bean
	 */
	public static Object getBean(Class<?> interfaceName) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
		Map<String, ?> m = context.getBeansOfType(interfaceName);
		if (m.size() > 1)  {
			//more than one bean found -> excecption 
			throw new OLATRuntimeException("found more than one bean for: "+interfaceName +". Calling this method should only find one bean!", null);
		} else if (m.size() == 1 ) {
			return new ArrayList(m.values()).get(0);
		}
		//fallback for beans named like the fully qualified path (legacy)
		Object o = context.getBean(interfaceName.getName());
		beanNamesCalledFromSource.add(interfaceName.getName());
		return o;
	}
	
	/**
	 * 
	 * @param beanName
	 * @return
	 */
	public static boolean containsSingleton(String beanName) {
		return beanFactory.containsSingleton(beanName);
	}
	
	/**
	 * @param beanName
	 *            The bean name to check for
	 * @return true if such a bean does exist, false
	 *         otherwhise. But if such a bean definition exists it will get created! 
	 *         Use the containsSingleton to check for lazy init beans whether they
	 *         are instantiated or not.
	 */
	public static boolean containsBean(String beanName) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
		return context.containsBean(beanName);
	}
	
	/**
	 * 
	 * @param classz
	 * @return
	 */
	public static boolean containsBean(Class classz) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
		Map beans = context.getBeansOfType(classz);
		return beans.size() > 0;
	}
	
	
	/**
	 * normally you should not use this!
	 * At the moment it is used for calling the shutdown hook in Spring
	 * @return the OLAT Spring application Context
	 */
	public static XmlWebApplicationContext getContext() {
		return (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
	}


	/**
	 * [used by spring]
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		CoreSpringFactory.servletContext = servletContext;
	}
	
	public static Map<String, Object> getBeansOfType(BeanType extensionType) {
		XmlWebApplicationContext context = (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
		Map beans = context.getBeansOfType(extensionType.getExtensionTypeClass());
		Map<String, Object> clone = new HashMap<String, Object>(beans);
		return clone;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
		
	}

}
