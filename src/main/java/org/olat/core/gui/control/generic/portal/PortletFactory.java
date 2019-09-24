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

package org.olat.core.gui.control.generic.portal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Factory to create instances of portlets defined in the
 * WEB-INF/olat_portals.xml
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public class PortletFactory {
	private static final Logger log = Tracing.createLoggerFor(PortletFactory.class);
	
	private static Map<String, Portlet> portlets;
	private static Object lockObject = new Object();
	
	/**
	 * Singleton
	 */
	private PortletFactory() {
		// singleton
	}
	
	/**
	 * [used by Spring]
	 */
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Portal",
				new SiteContextEntryControllerCreator(PortalSite.class));
	}
	
	public static Map<String, Portlet> getPortlets() {
		if (portlets == null) {
			synchronized(lockObject) {
				if (portlets == null) { // check again in synchronized-block, only one may create list
					initPortlets();
				}
			}
		}
		return portlets;
	}
	
	
	private static void initPortlets() {
		portlets = new HashMap<>();
		Map<String, Portlet> portletMap = CoreSpringFactory.getBeansOfType(Portlet.class);
		Collection<Portlet> portletValues = portletMap.values();
		for (Portlet portlet : portletValues) {
			log.debug("initPortlets portlet=" + portlet);
			if (portlet.isEnabled()) {
				portlets.put(portlet.getName(), portlet);
				log.debug("portlet is enabled => add to list portlet=" + portlet);
			}
		}
		
	}

	/**
	 * Factory method to create a portled wrapped in a portlet container.
	 * @param defaultConfiguration The default configuration map
	 * @param wControl
	 * @param ureq
	 * @return The portlet container that contains the portlet
	 */
	public static PortletContainer getPortletContainerFor(Portlet portlet, WindowControl wControl, UserRequest ureq) {		
		return new PortletContainer(wControl, ureq, portlet.createInstance(wControl, ureq, portlet.getConfiguration()));
	}
	
	/**
	 * @param beanName : The bean name to check for
	 * @return true if such a bean does exist in the config, false
	 *         otherwhise
	 */
	public static boolean containsPortlet(String beanName) {
		return getPortlets().containsKey(beanName);
	}
}
