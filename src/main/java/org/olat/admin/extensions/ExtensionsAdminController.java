/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.extensions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.admin.SystemAdminMainController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.extensions.Extension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 *
 * @author Christian Guretzki
 */
public class ExtensionsAdminController extends BasicController {
	private static final Logger log = Tracing.createLoggerFor(ExtensionsAdminController.class);
		

	private VelocityContainer content;
	private SimpleStackedPanel mainPanel;
	
	
	public ExtensionsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainPanel = new SimpleStackedPanel("extensionsPanel");
		// use combined translator from system admin main
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
		content = createVelocityContainer("extensionsAdmin");

		Map<ExtensionKey,Map<String, GenericBeanDefinition>> extensionList = new HashMap<>();
		extensionList.put(new ExtensionKey("Extension", "ext"), getBeanDefListFor(Extension.class));
		extensionList.put(new ExtensionKey("Sites definitions", "sitesdefs"), getBeanDefListFor(SiteDefinition.class));
		extensionList.put(new ExtensionKey("Portlets", "portles"), getBeanDefListFor(Portlet.class));
		extensionList.put(new ExtensionKey("Notification's handlers", "notifications"), getBeanDefListFor(NotificationsHandler.class));
		extensionList.put(new ExtensionKey("Course node configuration", "nodeconfig"), getBeanDefListFor(CourseNodeConfiguration.class));
		content.contextPut("extensionList", extensionList);

		mainPanel.setContent(content);
		putInitialPanel(mainPanel);
	}


	private Map<String, GenericBeanDefinition> getBeanDefListFor(Class<?> clazz) {
		boolean debug = log.isDebugEnabled();
		
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
		XmlWebApplicationContext context = (XmlWebApplicationContext)applicationContext;
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

		Map<String, GenericBeanDefinition> beanDefinitionList  = new HashMap<>();
		
		String[] beanNames = beanFactory.getBeanNamesForType(clazz);
		for (int i = 0; i < beanNames.length; i++) {
			try {
				if(debug) log.debug(">>> beanNames=" + beanNames[i]);
				GenericBeanDefinition beanDef = (GenericBeanDefinition)beanFactory.getBeanDefinition(beanNames[i]);
				ConstructorArgumentValues args = beanDef.getConstructorArgumentValues();
				List<ValueHolder> values = args.getGenericArgumentValues();
				for (Iterator<ValueHolder> iterator = values.iterator(); iterator.hasNext();) {
					ValueHolder valueHolder = iterator.next();
					if(debug) {
						log.debug("valueHolder=" + valueHolder);
						log.debug("valueHolder.getType()=" + valueHolder.getType());
						log.debug("valueHolder.getName()=" + valueHolder.getName());
						log.debug("valueHolder.getValue()=" + valueHolder.getValue());
					}
				}
				beanDefinitionList.put(beanNames[i], beanDef);
			} catch (NoSuchBeanDefinitionException e) {
				log.warn("Error while trying to analyze bean with name: "+ beanNames[i] +" :"+e);
			} catch (Exception e) {
				log.warn("Error while trying to analyze bean with name: "+ beanNames[i] +" :"+e);
			}
		}
		return beanDefinitionList;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static class ExtensionKey {
		
		private final String name;
		private final String key;
		
		public ExtensionKey(String name, String key) {
			this.name = name;
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public String getKey() {
			return key;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof ExtensionKey) {
				ExtensionKey extensionKey = (ExtensionKey)obj;
				return name != null && name.equals(extensionKey.getName());
			}
			return false;
		}
	}
}