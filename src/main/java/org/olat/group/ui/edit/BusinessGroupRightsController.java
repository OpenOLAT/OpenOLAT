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

import java.util.Iterator;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupRightsController extends BasicController {
	
	private RightsToGroupDataModel rightDataModel;
	private Choice rightsChoice;
	private List<String> selectedRights;
	private BGRights bgRights;
	
	private final VelocityContainer mainVC;
	
	private BusinessGroup businessGroup;
	private final BGRightManager rightManager;
	private final BusinessGroupService businessGroupService;
	
	public BusinessGroupRightsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		this.businessGroup = businessGroup;
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		// Initialize available rights
		bgRights = new CourseRights(ureq.getLocale());
		
		mainVC = createVelocityContainer("tab_bgRights");
		selectedRights = rightManager.findBGRights(businessGroup);
		rightDataModel = new RightsToGroupDataModel(bgRights, selectedRights);

		rightsChoice = new Choice("rightsChoice", getTranslator());
		rightsChoice.setSubmitKey("submit");
		rightsChoice.setCancelKey("cancel");
		rightsChoice.setTableDataModel(this.rightDataModel);
		rightsChoice.addListener(this);
		mainVC.put("rightsChoice", rightsChoice);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == rightsChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				updateGroupRightsRelations();
				// do loggin
				for (Iterator<String> iter = selectedRights.iterator(); iter.hasNext();) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_RIGHT_UPDATED, getClass(),
							LoggingResourceable.wrapBGRight(iter.next()));
				}
				if (selectedRights.size()==0) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_RIGHT_UPDATED_EMPTY, getClass());
				}
				// notify current active users of this business group
				BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT, businessGroup, null);
			}
		}
	}
	
	/**
	 * Update the rights associated to this group: remove and add rights
	 */
	private void updateGroupRightsRelations() {
		// refresh group to prevent stale object exception and context proxy issues
		businessGroup = businessGroupService.loadBusinessGroup(businessGroup);
		// 1) add rights to group
		List<Integer> addedRights = rightsChoice.getAddedRows();
		for (Integer position: addedRights) {
			String right = rightDataModel.getObject(position.intValue());
			rightManager.addBGRight(right, businessGroup);
			selectedRights.add(right);
		}
		// 2) remove rights from group
		List<Integer> removedRights = rightsChoice.getRemovedRows();
		for (Integer position:removedRights) {
			String right = rightDataModel.getObject(position.intValue());
			rightManager.removeBGRight(right, businessGroup);
			selectedRights.remove(right);
		}
	}
}
