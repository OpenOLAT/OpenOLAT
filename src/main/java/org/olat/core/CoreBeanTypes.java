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

package org.olat.core;

import org.olat.core.commons.persistence.AdditionalDBMappings;
import org.olat.core.extensions.Extension;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.notifications.NotificationsHandler;


	/**
	 * Define all classes which can be loaded via spring by type 
	 * @author Christian Guretzki
	 */
public enum CoreBeanTypes implements BeanType {
	extension(Extension.class),
	siteDefinition(SiteDefinition.class),
	basicManager(BasicManager.class),
	portlet(Portlet.class),
	notificationsHandler(NotificationsHandler.class),
	additionalDBMappings(AdditionalDBMappings.class);

	
	private Class extensionType;
	
	private CoreBeanTypes(Class extensionType) {
		this.extensionType = extensionType;
	}
	
	public Class getExtensionTypeClass() {
		return extensionType;
	}
}


