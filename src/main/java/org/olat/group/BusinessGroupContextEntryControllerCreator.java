/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.group.ui.BGControllerFactory;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;

/**
 * <h3>Description:</h3>
 * <p>
 * This class can create run controllers for business groups for a given context
 * entry
 * <p>
 * Initial Date: 19.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class BusinessGroupContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = ce.getOLATResourceable();

		Long gKey = ores.getResourceableId();
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		Controller ctrl = null;
		BusinessGroup bgroup = bgs.loadBusinessGroup(gKey);
		if(bgroup != null) {
			// check if allowed to start (must be member or admin)
			//fxdiff VCRP-1,2: access control of resources
			if (ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isGroupManager()
					|| bgs.isIdentityInBusinessGroup(ureq.getIdentity(), bgroup) || isAccessControlled(bgroup)) {
				// only olatadmins or admins of this group can administer this group
				ctrl = BGControllerFactory.getInstance().createRunControllerFor(ureq, wControl, bgroup, null);
			}
		}
		return ctrl;
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getTabName(org.olat.core.id.context.ContextEntry)
	 */
	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		OLATResourceable ores = ce.getOLATResourceable();
		Long gKey = ores.getResourceableId();
		BusinessGroup bgroup = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(gKey);
		return bgroup == null ? "" : bgroup.getName();
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = ce.getOLATResourceable();
		Long gKey = ores.getResourceableId();
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroup bgroup = bgs.loadBusinessGroup(gKey);
		if (bgroup == null) {
			return false;
		}	
		return ureq.getUserSession().getRoles().isOLATAdmin() ||
				ureq.getUserSession().getRoles().isGroupManager() ||
				bgs.isIdentityInBusinessGroup(ureq.getIdentity(), bgroup) || isAccessControlled(bgroup);
	}
	
	private boolean isAccessControlled(BusinessGroup bgroup) {
		AccessControlModule acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		if(acModule.isEnabled()) {
			ACFrontendManager acFrontendManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
			if(acFrontendManager.isResourceAccessControled(bgroup.getResource(), new Date())) {
				return true;
			}
		}
		return false;
	}
}
