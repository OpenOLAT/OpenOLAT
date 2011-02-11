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

package org.olat.admin.site;

import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.admin.UserAdminMainController;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
/**
 * Initial Date:  Jan 16, 2006
 * @author Florian Gnaegi
 * </pre>
 */
public class UserAdminSite implements SiteInstance {
	private static final OLATResourceable ORES_OLATUSERADMINS = OresHelper.lookupType(UserAdminMainController.class);
	
	// refer to the definitions in org.olat
	private static final String PACKAGE = Util.getPackageName(BaseChiefController.class);
	
	private NavElement origNavElem;
	private NavElement curNavElem;
	/**
	 * 
	 */
	public UserAdminSite(Locale loc) {
		Translator trans = new PackageTranslator(PACKAGE, loc);
		origNavElem = new DefaultNavElement(trans.translate("topnav.useradmin"), trans.translate("topnav.useradmin.alt"), "o_site_useradmin");
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
		MainLayoutController c = ControllerFactory.createLaunchController(ORES_OLATUSERADMINS, null, ureq, wControl, true);
		return c;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#isKeepState()
	 */
	public boolean isKeepState() {
		return true;
	}
	
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

}

