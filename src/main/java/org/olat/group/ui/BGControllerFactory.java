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

package org.olat.group.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.run.BusinessGroupMainRunController;

/**
 * Description: <BR>
 * Factory to create all controllers needed to work with business groups. The
 * methods will configure generic run, edit and management controllers to make
 * the desired featureset available
 * <P>
 * Initial Date: Aug 19, 2004
 * 
 * @author patrick
 */

public class BGControllerFactory {

	private static BGControllerFactory INSTANCE = null;
	
	static {
		INSTANCE = new BGControllerFactory();
	}

	/**
	 * Use getInstance instead
	 */
	private BGControllerFactory() {
	//
	}

	/**
	 * @return business group controller factory
	 */
	public static BGControllerFactory getInstance() {
		return INSTANCE;
	}

	//
	// 2) Group run controllers
	//

	/**
	 * Factory method to create a configured group run controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param businessGroup
	 * @param isGMAdmin true if user is group management administrator
	 * @param initialViewIdentifier
	 * @return a run controller for this business group
	 */
	public BusinessGroupMainRunController createRunControllerFor(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		// build up the context path
		WindowControl bwControl;
		OLATResourceable businessOres = businessGroup;
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
		//OLAT-5944: check if the current context entry is not already the group entry to avoid duplicate in the business path
		if(ce.equals(wControl.getBusinessControl().getCurrentContextEntry())) {
			bwControl = wControl;
		} else {
			bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
		}
		return new BusinessGroupMainRunController(ureq, bwControl, businessGroup);
	}
}