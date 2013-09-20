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

package org.olat.gui.demo.site;

import org.olat.ControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.gui.demo.GUIDemoMainController;
/**
 * 
 * Description:<br>
 * TODO: Lavinia Dumitrescu Class Description for GUIDemoSite
 * 
 * <P>
 * Initial Date:  11.09.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class GUIDemoSite extends AbstractSiteInstance {
	private static final OLATResourceable ORES_TESTING = OresHelper.lookupType(GUIDemoMainController.class);
	
	private NavElement origNavElem;
	private NavElement curNavElem;
	/**
	 * 
	 */
	public GUIDemoSite(SiteDefinition siteDef) {
		super(siteDef);
		origNavElem = new DefaultNavElement("gui_demo", "gui demo", "o_site_guidemo");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		MainLayoutController c = ControllerFactory.createLaunchController(ORES_TESTING, ureq, wControl, true);
		return c;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#isKeepState()
	 */
	public boolean isKeepState() {
		return true;
	}
	
	/**
	 * @see org.olat.navigation.SiteInstance#reset()
	 */
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

}

