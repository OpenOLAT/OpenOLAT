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
package org.olat.group.ui.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.helpTooltip.HelpTooltip;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BusinessGroupFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupEditDetailsController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final BusinessGroupFormController editController;
	
	private BusinessGroup businessGroup;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public BusinessGroupEditDetailsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		this.businessGroup = businessGroup;
		
		mainVC = createVelocityContainer("tab_bgDetail");

		//hoover help text for ID
		HelpTooltip idHelpText = new HelpTooltip("idHelpText", translate("group.id.help"));
		mainVC.put("idHelpText", idHelpText);

		editController = new BusinessGroupFormController(ureq, getWindowControl(), businessGroup);
		listenTo(editController);
		
		mainVC.put("businessGroupForm", editController.getInitialComponent());
		mainVC.contextPut("groupid", businessGroup.getKey());
		putInitialPanel(mainVC);
	}
	
	public BusinessGroup getGroup() {
		return businessGroup;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editController) {
			if(event == Event.DONE_EVENT) {
				businessGroup = updateBusinessGroup();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}
	
	public void setAllowWaitingList(boolean allowWaitingList) {
		editController.setAllowWaitingList(allowWaitingList);	
	}
	
	/**
	 * persist the updates
	 */
	private BusinessGroup updateBusinessGroup() {
		String bgName = editController.getGroupName();
		String bgDesc = editController.getGroupDescription();
		Integer bgMax = editController.getGroupMax();
		Integer bgMin = editController.getGroupMin();
		boolean waitingListEnabled = editController.isWaitingListEnabled();
		boolean autoCloseRanksEnabled = editController.isAutoCloseRanksEnabled();
		return businessGroupService.updateBusinessGroup(getIdentity(), businessGroup, bgName, bgDesc,
				bgMin, bgMax, waitingListEnabled, autoCloseRanksEnabled);
	}
}
