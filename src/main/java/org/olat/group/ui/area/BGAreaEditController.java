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

package org.olat.group.ui.area;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * This controller can be used to edit the business grou area metadata and
 * associate business groups to the business group area.
 * <P>
 * Initial Date: Aug 30, 2004
 * 
 * @author gnaegi
 */
public class BGAreaEditController extends BasicController {

	// GUI components
	private TabbedPane tabbedPane;
	private Link backLink;
	private final VelocityContainer mainVC;
	private VelocityContainer detailsTabVC, groupsTabVC;
	private BGAreaFormController areaController;
	private GroupsToAreaDataModel groupsDataModel;
	private Choice groupsChoice;
	// area, context and group references
	private BGArea area;
	private OLATResource resource;
	private RepositoryEntry repoEntry;
	private List<BusinessGroup> allGroups;
	private List<BusinessGroup> inAreaGroups;

	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	/**
	 * Constructor for the business group area edit controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param area The business group area
	 */
	public BGAreaEditController(UserRequest ureq, WindowControl wControl, BGArea area, boolean back) {
		super(ureq, wControl);

		this.area = area;
		resource = area.getResource();
		repoEntry = repositoryManager.lookupRepositoryEntry(resource, false);

		// tabbed pane
		tabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
		tabbedPane.addListener(this);
		// details tab
		initAndAddDetailsTab(ureq, wControl);
		// groups tab
		initAndAddGroupsTab();
		// initialize main view
		mainVC = createVelocityContainer("edit");
		if(back) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
			mainVC.put("backLink", backLink);
		}
		mainVC.put("tabbedpane", tabbedPane);
		mainVC.contextPut("title", translate("area.edit.title", new String[] { StringHelper.escapeHtml(area.getName()) }));
		putInitialPanel(mainVC);
	}

	/**
	 * initialize the area details tab
	 */
	private void initAndAddDetailsTab(UserRequest ureq, WindowControl wControl) {
		detailsTabVC = createVelocityContainer("detailstab");

		removeAsListenerAndDispose(areaController);
		areaController = new BGAreaFormController(ureq, wControl, area, false);
		listenTo(areaController);
		detailsTabVC.put("areaForm", areaController.getInitialComponent());
		tabbedPane.addTab(translate("tab.details"), detailsTabVC);
	}

	/**
	 * initalize the group to area association tab
	 */
	private void initAndAddGroupsTab() {
		groupsTabVC = createVelocityContainer("groupstab");
		tabbedPane.addTab(translate("tab.groups"), groupsTabVC);

		allGroups = businessGroupService.findBusinessGroups(null, repoEntry, 0, -1);
		List<BusinessGroup> repoGroups = new ArrayList<>(allGroups);
		inAreaGroups = areaManager.findBusinessGroupsOfArea(area);
		for(BusinessGroup inAreaGroup:inAreaGroups) {
			if(!allGroups.contains(inAreaGroup)) {
				allGroups.add(inAreaGroup);
			}
		}
		groupsDataModel = new GroupsToAreaDataModel(allGroups, repoGroups, inAreaGroups, getTranslator());

		groupsChoice = new Choice("groupsChoice", getTranslator());
		groupsChoice.setSubmitKey("submit");
		groupsChoice.setCancelKey("cancel");
		groupsChoice.setModel(groupsDataModel);
		groupsChoice.setEscapeHtml(false);
		groupsChoice.addListener(this);
		groupsTabVC.put("groupsChoice", groupsChoice);
		groupsTabVC.contextPut("noGroupsFound", Boolean.valueOf(allGroups.isEmpty()));
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == groupsChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				doUpdateGroupAreaRelations();
				// do logging
				if (inAreaGroups.isEmpty()) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.BGAREA_UPDATED_NOW_EMPTY, getClass());
				} else {
					for (BusinessGroup aGroup : inAreaGroups) {
						ThreadLocalUserActivityLogger.log(GroupLoggingAction.BGAREA_UPDATED_MEMBER_GROUP, getClass(), 
								LoggingResourceable.wrap(aGroup));
					}
				}
			}
		} else if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == areaController) {
			if (event == Event.DONE_EVENT) {
				BGArea updatedArea = doAreaUpdate();
				if (updatedArea == null) {
					areaController.resetAreaName();
					getWindowControl().setWarning(translate("error.area.name.exists"));
				} else {
					area = updatedArea;
					mainVC.contextPut("title", translate("area.edit.title", new String[] { StringHelper.escapeHtml(area.getName()) }));
				}
			} else if (event == Event.CANCELLED_EVENT) {
				// area might have been changed, reload from db
				area = areaManager.reloadArea(area);
				removeAsListenerAndDispose(areaController);
				areaController = new BGAreaFormController(ureq, getWindowControl(), area, false);
				listenTo(areaController);
				detailsTabVC.put("areaForm", areaController.getInitialComponent());
				fireEvent(ureq, event);
			}
		}
	}

	/**
	 * Update a group area
	 * 
	 * @return the updated area
	 */
	public BGArea doAreaUpdate() {
		area.setName(areaController.getAreaName());
		area.setDescription(areaController.getAreaDescription());
		return areaManager.updateBGArea(area);
	}

	/**
	 * Update the groups associated to this area: remove and add groups
	 */
	public void doUpdateGroupAreaRelations() {
		// 1) add groups to area
		List<Integer> addedGroups = groupsChoice.getAddedRows();
		for (Integer position:addedGroups) {
			BusinessGroup group = groupsDataModel.getObject(position.intValue());
			// refresh group to prevent stale object exception and context proxy
			// issues
			group = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(group);
			// refresh group also in table model
			allGroups.set(position.intValue(), group);
			// add group now to area and update in area group list
			areaManager.addBGToBGArea(group, area);
			inAreaGroups.add(group);
		}
		// 2) remove groups from area
		List<Integer> removedGroups = groupsChoice.getRemovedRows();
		for (Integer position:removedGroups) {
			BusinessGroup group = groupsDataModel.getObject(position.intValue());
			areaManager.removeBGFromArea(group, area);
			inAreaGroups.remove(group);
		}
	}

}
