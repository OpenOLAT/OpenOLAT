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

package org.olat.group.ui.portlet;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.generic.portal.PortletToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.group.ui.BGControllerFactory;

/**
 * Description:<br>
 * Displays the list of most used groups
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupsPortlet extends AbstractPortlet {
	private GroupsPortletRunController runCtr;


	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		Translator translator = Util.createPackageTranslator(BGControllerFactory.class, ureq.getLocale());
		GroupsPortlet p = new GroupsPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setDefaultMaxEntries(getDefaultMaxEntries());
		p.setTranslator(translator);
		return p;
	}

	@Override
	public String getTitle() {
		return getTranslator().translate("groupsPortlet.title");
	}

	@Override
	public String getDescription() {
		return getTranslator().translate("groupsPortlet.description");
	}

	@Override
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(runCtr != null) {
			runCtr.dispose();
		}
		runCtr = new GroupsPortletRunController(wControl, ureq, getTranslator(), getName(), getDefaultMaxEntries());
		return runCtr.getInitialComponent();
	}

	@Override
	public void dispose() {
		disposeRunComponent();
	}

	@Override
	public String getCssClass() {
		return "o_portlet_groups";
	}

	@Override
	public void disposeRunComponent() {
		if (runCtr != null) {
			runCtr.dispose();
			runCtr = null;
		}
	}

	@Override
	public PortletToolController<BusinessGroupEntry> getTools(UserRequest ureq, WindowControl wControl) {
		//portlet was not yet visible
		if ( runCtr == null ) {
			runCtr = new GroupsPortletRunController(wControl, ureq, getTranslator(), getName(), getDefaultMaxEntries());
		}
	  return runCtr.createSortingTool(ureq, wControl);
	}
}
