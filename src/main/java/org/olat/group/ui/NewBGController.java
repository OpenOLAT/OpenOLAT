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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.resource.OLATResource;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Shows the group form until a a group could be created or the form is
 * cancelled. A group can not be created if its name already exists in the given
 * context. This will show an error on the form.<br>
 * Sends {@link Event#DONE_EVENT} in the case of successfully group creation and
 * {@link Event#CANCELLED_EVENT} if the user no longer wishes to create a group.
 * <P>
 * Initial Date: 28.06.2007 <br>
 * 
 * @author patrickb
 */
public class NewBGController extends BasicController {

	private OLATResource resource;
	private BusinessGroupService businessGroupService;
	private VelocityContainer contentVC;
	private BusinessGroupFormController groupCreateController;
	private boolean bulkMode = false;
	private Set<BusinessGroup> newGroups;

	/**
	 * @param ureq
	 * @param wControl
	 * @param minMaxEnabled
	 * @param bgContext
	 * @param bulkMode
	 */
	NewBGController(UserRequest ureq, WindowControl wControl, boolean minMaxEnabled, OLATResource resource){
		this(ureq,wControl,minMaxEnabled,resource,true,null);
	}
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param minMaxEnabled
	 * @param bgContext
	 * @param bulkMode
	 * @param csvGroupNames
	 */
	NewBGController(UserRequest ureq, WindowControl wControl, boolean minMaxEnabled, OLATResource resource, boolean bulkMode, String csvGroupNames) {
		super(ureq, wControl);
		this.resource = resource;
		this.bulkMode = bulkMode;
		//
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		this.contentVC = this.createVelocityContainer("bgform");
		this.contentVC.contextPut("bulkMode", bulkMode ? Boolean.TRUE : Boolean.FALSE);
		
		this.groupCreateController = new BusinessGroupFormController(ureq, wControl, null, minMaxEnabled, bulkMode);
		listenTo(this.groupCreateController);
		this.contentVC.put("groupForm", this.groupCreateController.getInitialComponent());
		if (csvGroupNames != null) {
			this.groupCreateController.setGroupName(csvGroupNames);
		}
		this.putInitialPanel(contentVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose.
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == this.groupCreateController) {
			if (event == Event.DONE_EVENT) {
				String bgDesc = this.groupCreateController.getGroupDescription();
				Integer bgMax = this.groupCreateController.getGroupMax();
				Integer bgMin = this.groupCreateController.getGroupMin();
				Boolean enableWaitingList = this.groupCreateController.isWaitingListEnabled();
				Boolean enableAutoCloseRanks = this.groupCreateController.isAutoCloseRanksEnabled();
				
				Set<String> allNames = new HashSet<String>();
				if (this.bulkMode) {
					allNames = this.groupCreateController.getGroupNames();
				} else {
					allNames.add(this.groupCreateController.getGroupName());
				}

				this.newGroups = businessGroupService.createUniqueBusinessGroupsFor(allNames, bgDesc, bgMin, bgMax,	enableWaitingList, enableAutoCloseRanks, resource);
				if(this.newGroups != null){
						for (Iterator<BusinessGroup> iter = this.newGroups.iterator(); iter.hasNext();) {
							BusinessGroup bg = iter.next();
							LoggingResourceable resourceInfo = LoggingResourceable.wrap(bg);
							ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CREATED, getClass(), resourceInfo);	
						}						
					// workflow successfully finished
					// so far no events on the systembus to inform about new groups in BGContext 
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					// Could not create any group, because one or groups-name already exist. Set error of non existing name
					this.groupCreateController.setGroupNameExistsError(null);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				// workflow cancelled
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here. 
	}

	/**
	 * if Event.DONE_EVENT received the return value is always NOT NULL. If
	 * Event_FORM_CANCELLED ist received this will be null.
	 * 
	 * @return
	 */
	public BusinessGroup getCreatedGroup() {
		return newGroups.iterator().next();
	}
	
	/**
	 * Returns the new business groups.
	 * 
	 * @return the new groups.
	 */
	public Set<BusinessGroup> getCreatedGroups(){
		return newGroups;
	}
	
	/**
	 * Returns the names of the new business groups.
	 * 
	 * @return the new group names.
	 */
	public Set<String> getCreatedGroupNames(){
		Set<String> groupNames = new HashSet<String>();
		for (Iterator<BusinessGroup> iterator = this.newGroups.iterator(); iterator.hasNext();) {
			 groupNames.add( iterator.next().getName());
		}
		return groupNames;
	}
	
	/**
	 * if bulkmode is on or not
	 * @return
	 */
	public boolean isBulkMode(){
		return bulkMode;
	}

}
