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
* <p>
*/ 

package org.olat.test.site;

import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.GUIDemoMainController;
/**
 * 
 * Description:<br>
 * TODO: Lavinia Dumitrescu Class Description for GUIDemoSite
 * 
 * <P>
 * Initial Date:  11.09.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class GUIDemoSite implements SiteInstance {
	private static final OLATResourceable ORES_TESTING = OresHelper.lookupType(GUIDemoMainController.class);
	
	private NavElement origNavElem;
	private NavElement curNavElem;
	/**
	 * 
	 */
	public GUIDemoSite(Locale loc) {
		origNavElem = new DefaultNavElement("gui_demo", "gui demo", "o_site_guidemo");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	public NavElement getNavElement() {
		return curNavElem;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#createController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public MainLayoutController createController(UserRequest ureq, WindowControl wControl) {
		MainLayoutController c = ControllerFactory.createLaunchController(ORES_TESTING, null, ureq, wControl, true);
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

