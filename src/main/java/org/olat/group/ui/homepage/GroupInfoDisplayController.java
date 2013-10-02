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
package org.olat.group.ui.homepage;


import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;

/**
 * 
 * Initial Date:  Aug 19, 2009 <br>
 * @author twuersch, www.frentix.com
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupInfoDisplayController extends BasicController {
	
	
	private final VelocityContainer content;
	
	public GroupInfoDisplayController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		content = createVelocityContainer("groupinfodisplay");
		content.contextPut("description", businessGroup.getDescription());
		content.contextPut("name", StringHelper.escapeHtml(businessGroup.getName()));
		
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		int numParticipants = 0;
		if (businessGroup.getPartipiciantGroup() != null) {
			numParticipants =  securityManager.countIdentitiesOfSecurityGroup(businessGroup.getPartipiciantGroup());
		}
		int numOwners = 0;
		if (businessGroup.getOwnerGroup() != null) {
			numOwners = securityManager.countIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup());
		}
		content.contextPut("numMembers", numParticipants + numOwners);
		
		putInitialPanel(content);
	}

	@Override
	protected void doDispose() {
		// Nothing to do here.
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Do nothing.
	}
}
