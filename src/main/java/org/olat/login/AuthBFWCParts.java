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

/**
 * Description:<br>
 * OLAT Sites, Topnavigation (search etc.), Footer and no Header for the 
 * authenticated user.
 * 
 * <P>
 * Initial Date:  29.01.2008 <br>
 * @author patrickb
 */
public class AuthBFWCParts implements BaseFullWebappControllerParts {

	/**
	 * @see org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts#createFooterController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public LockableController createFooterController(UserRequest ureq, WindowControl wControl) {
		Controller footerCtr = null;
		// ----------- footer, optional (e.g. for copyright, powerd by) ------------------
		if (CoreSpringFactory.containsBean("fullWebApp.FooterControllerCreator")) {
			ControllerCreator footerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.FooterControllerCreator");
			footerCtr = footerCreator.createController(ureq, wControl);
		}
		return (LockableController)footerCtr;
	}

	/**
	 * @see org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts#createHeaderController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createHeaderController(UserRequest ureq, WindowControl wControl) {
		Controller headerCtr = null;
		// ----------- header, optional (e.g. for logo, advertising ) ------------------		
		if (CoreSpringFactory.containsBean("fullWebApp.HeaderControllerCreator")) {
			ControllerCreator headerControllerCreator = (ControllerCreator)  CoreSpringFactory.getBean("fullWebApp.HeaderControllerCreator");
			headerCtr = headerControllerCreator.createController(ureq, wControl);
		}
		return headerCtr;
	}

	/**
	 * @see org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts#createTopNavController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public LockableController createTopNavController(UserRequest ureq, WindowControl wControl) {
		Controller topnavCtr = null;
		// ----------- topnav, optional (e.g. for imprint, logout) ------------------		
		if (CoreSpringFactory.containsBean("fullWebApp.TopNavControllerCreator")) {
			ControllerCreator topnavControllerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.TopNavControllerCreator");
			topnavCtr = topnavControllerCreator.createController(ureq, wControl);
		}
		return (LockableController)topnavCtr;
	}

	/**
	 * @see org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts#getSiteInstances(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
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

	@Override
	public Controller getContentController(UserRequest ureq, WindowControl control) {
		//could be used for first time information, or to start a workflow to set 
		//language etc.
		// not working as overlay, but as a content-replacement
		return null;
	}

}
