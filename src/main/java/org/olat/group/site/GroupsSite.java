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

package org.olat.group.site;

import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class GroupsSite implements SiteInstance {
	private static final OLATResourceable ORES_GROUPS = OresHelper.createOLATResourceableInstance("BGMainController", 0l);

	// refer to the definitions in org.olat
	private NavElement origNavElem;
	private NavElement curNavElem;

	/**
	 * 
	 */
	public GroupsSite(Locale loc) {
		Translator trans = Util.createPackageTranslator(BaseChiefController.class, loc);
		origNavElem = new DefaultNavElement(trans.translate("topnav.buddygroups"), trans.translate("topnav.buddygroups.alt"), "o_site_groups");
		origNavElem.setAccessKey("g".charAt(0));
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	public NavElement getNavElement() {
		return curNavElem;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public MainLayoutController createController(UserRequest ureq, WindowControl wControl) {
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(GroupsSite.class, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, new StateSite(this), wControl, true);
		MainLayoutController c = ControllerFactory.createLaunchController(ORES_GROUPS, ureq, bwControl, true);
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
