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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.util.UserSession;
import org.olat.group.ui.BGControllerFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;

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
	
	private BusinessGroup group;
	private Boolean authorized;
	
	@Override
	public ContextEntryControllerCreator clone() {
		return new BusinessGroupContextEntryControllerCreator();
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		Controller ctrl = null;
		BusinessGroup bgroup = getBusinessGroup(ces.get(0));
		if(bgroup != null && isAuthorized(ureq, bgroup)) {
			ctrl = BGControllerFactory.getInstance().createRunControllerFor(ureq, wControl, bgroup);
		}
		return ctrl;
	}

	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		BusinessGroup bgroup = getBusinessGroup(ce);
		return bgroup == null ? "" : bgroup.getName();
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		BusinessGroup bgroup = getBusinessGroup(ce);	
		return bgroup != null && isAuthorized(ureq, bgroup);
	}
	
	private boolean isAccessControlled(BusinessGroup bgroup) {
		AccessControlModule acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		if(acModule.isEnabled()) {
			ACService acService = CoreSpringFactory.getImpl(ACService.class);
			if(acService.isResourceAccessControled(bgroup.getResource(), new Date())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAuthorized(UserRequest ureq, BusinessGroup bgroup) {
		if(authorized == null) {
			UserSession usess = ureq.getUserSession();
			Object wildcard = usess.getEntry("wild_card_" + bgroup.getKey());
			authorized = (wildcard != null && Boolean.TRUE.equals(wildcard))
				|| usess.getRoles().isAdministrator()
				|| usess.getRoles().isGroupManager() 
				|| CoreSpringFactory.getImpl(BusinessGroupService.class).isIdentityInBusinessGroup(ureq.getIdentity(), bgroup)  
				|| isAccessControlled(bgroup);
		}
		return authorized.booleanValue();
	}
	
	private BusinessGroup getBusinessGroup(ContextEntry ce) {
		if(group == null) {
			OLATResourceable ores = ce.getOLATResourceable();
			Long gKey = ores.getResourceableId();
			BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			group = bgs.loadBusinessGroup(gKey);
		}
		return group;
	}
}
