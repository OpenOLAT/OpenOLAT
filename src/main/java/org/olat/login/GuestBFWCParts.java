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
package org.olat.login;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.gui.control.OlatFooterController;

/**
 * Initial Date:  30.01.2008 <br>
 * @author patrickb
 */
public class GuestBFWCParts implements BaseFullWebappControllerParts {
	
	private boolean showTopNav = true;

	@Override
	public LockableController createFooterController(UserRequest ureq, WindowControl wControl) {
		Controller footerCtr = null;
		// ----------- footer, optional (e.g. for copyright, powered by) ------------------
		if (CoreSpringFactory.containsBean("fullWebApp.GuestFooterControllerCreator")) {
			ControllerCreator footerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.GuestFooterControllerCreator");
			footerCtr = footerCreator.createController(ureq, wControl);
		} else {
			footerCtr = new OlatFooterController(ureq,wControl);
		}
		return (LockableController)footerCtr;
	}

	@Override
	public Controller createHeaderController(UserRequest ureq, WindowControl wControl) {
		Controller headerCtr = null;
		// ----------- header, optional (e.g. for logo, advertising ) ------------------		
		if (CoreSpringFactory.containsBean("fullWebApp.GuestHeaderControllerCreator")) {
			ControllerCreator headerControllerCreator = (ControllerCreator)  CoreSpringFactory.getBean("fullWebApp.GuestHeaderControllerCreator");
			headerCtr = headerControllerCreator.createController(ureq, wControl);
		}
		return headerCtr;
	}

	@Override
	public LockableController createTopNavController(UserRequest ureq, WindowControl wControl) {
		if (showTopNav) {
			LockableController topNavCtr = null;
			if (CoreSpringFactory.containsBean("fullWebApp.GuestTopNavControllerCreator")) {
				ControllerCreator headerControllerCreator = (ControllerCreator)  CoreSpringFactory.getBean("fullWebApp.GuestTopNavControllerCreator");
				topNavCtr = (LockableController)headerControllerCreator.createController(ureq, wControl);
			}
			return topNavCtr;
		} else {
			return null;
		}
	}

	@Override
	public Controller getContentController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public List<SiteInstance> getSiteInstances(UserRequest ureq, WindowControl wControl) {
		SiteDefinitions sitedefs = CoreSpringFactory.getImpl(SiteDefinitions.class);
		List<SiteInstance> sites = new ArrayList<>();
		for (SiteDefinition sitedef : sitedefs.getSiteDefList()) {
			SiteInstance site = sitedef.createSite(ureq, wControl);
			if (site != null) {
				// site == null means that site is not visible to the current user
				sites.add(site);
			}
		}
		return sites;
	}
}
