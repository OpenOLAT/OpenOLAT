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
package org.olat.repository.ui;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;

/**
 * Description:<br>
 * TODO: patrickb Class Description for RepositoyUIFactory
 * 
 * <P>
 * Initial Date:  03.12.2007 <br>
 * @author patrickb
 */
public class RepositoyUIFactory {
	
	private static final String HIERARCHY_DELIMITER = " / ";
	
	public static String getIconCssClass(String type) {
		String iconCSSClass = "o_" + type.replace(".", "-");
		iconCSSClass = iconCSSClass.concat("_icon");
		return iconCSSClass;
	}
	
	public static String getIconCssClass(RepositoryEntryShort re) {
		if(re == null) return "";
		
		String iconCSSClass = "o_" + re.getResourceType().replace(".", "-");
		if (re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			iconCSSClass = iconCSSClass.concat("_icon_closed");
		} else {
			iconCSSClass = iconCSSClass.concat("_icon");
		}
		return iconCSSClass;
	}
	
	public static String getIconCssClass(RepositoryEntry re) {
		if(re == null) return "";
		
		String iconCSSClass = "o_" + re.getOlatResource().getResourceableTypeName().replace(".", "-");
		if (re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			iconCSSClass = iconCSSClass.concat("_icon_closed");
		} else {
			iconCSSClass = iconCSSClass.concat("_icon");
		}
		return iconCSSClass;
	}
	
	/**
	 * Create main controller that does nothing but displaying a message that
	 * this resource is disabled due to security constraints
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static GenericMainController createRepoEntryDisabledDueToSecurityMessageController(UserRequest ureq, WindowControl wControl) {
		//wrap simple message into mainLayout
		GenericMainController glc = new GenericMainController(ureq, wControl) {
			@Override
			public void init(UserRequest uureq) {
				Panel empty = new Panel("empty");			
				setTranslator(Util.createPackageTranslator(RepositoryModule.class, uureq.getLocale())); 
				MessageController contentCtr = MessageUIFactory.createInfoMessage(uureq, getWindowControl(), translate("security.disabled.title"), translate("security.disabled.info"));
				listenTo(contentCtr); // auto dispose later
				Component resComp = contentCtr.getInitialComponent();
				LayoutMain3ColsController columnLayoutCtr = new LayoutMain3ColsController(uureq, getWindowControl(), empty, resComp, /*do not save no prefs*/null);
				listenTo(columnLayoutCtr); // auto dispose later
				putInitialPanel(columnLayoutCtr.getInitialComponent());
			}
		
			@Override
			protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest uureq) {
				//no menutree means no menu events.
				return null;
			}
		
		};
		glc.init(ureq);
		return glc;
	}

	
	public static Controller createLifecylceAdminController(UserRequest ureq, WindowControl wControl) {
		Controller ctrl = new LifecycleAdminController(ureq, wControl);
		return ctrl;
	}
}