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
* <p>
*/ 

package org.olat.group.ui.area;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.util.logging.activity.LoggingResourceable;

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
	private static final String PACKAGE = Util.getPackageName(BGAreaEditController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);

	// helpers

	private Translator trans;
	// GUI components
	private TabbedPane tabbedPane;
	private VelocityContainer editVC, detailsTabVC, groupsTabVC;
	private BGAreaFormController areaController;
	private GroupsToAreaDataModel groupsDataModel;
	private Choice groupsChoice;
	// area, context and group references
	private BGArea area;
	private BGContext bgContext;
	private List allGroups, inAreaGroups;
	// managers
	private BGAreaManager areaManager;
	private BGContextManager contextManager;

	/**
	 * Constructor for the business group area edit controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param area The business group area
	 */
	public BGAreaEditController(UserRequest ureq, WindowControl wControl, BGArea area) {
		super(ureq, wControl);

		this.trans = new PackageTranslator(PACKAGE, ureq.getLocale());
		this.area = area;
		this.areaManager = BGAreaManagerImpl.getInstance();
		this.bgContext = area.getGroupContext();
		this.contextManager = BGContextManagerImpl.getInstance();

		// tabbed pane
		tabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
		tabbedPane.addListener(this);
		// details tab
		initAndAddDetailsTab(ureq, wControl);
		// groups tab
		initAndAddGroupsTab();
		// initialize main view
		initEditVC();
		putInitialPanel(this.editVC);
	}

	/**
	 * initialize the main velocity wrapper container
	 */
	private void initEditVC() {
		editVC = new VelocityContainer("edit", VELOCITY_ROOT + "/edit.html", trans, this);
		editVC.put("tabbedpane", tabbedPane);
		editVC.contextPut("title", trans.translate("area.edit.title", new String[] { StringEscapeUtils.escapeHtml(this.area.getName()).toString() }));
	}

	/**
	 * initialize the area details tab
	 */
	private void initAndAddDetailsTab(UserRequest ureq, WindowControl wControl) {
		this.detailsTabVC = new VelocityContainer("detailstab", VELOCITY_ROOT + "/detailstab.html", this.trans, this);
		//TODO:pb: refactor BGControllerFactory.create..AreaController to be
		//usefull here
		if (this.areaController != null) {
			removeAsListenerAndDispose(this.areaController);
		}
		this.areaController = new BGAreaFormController(ureq, wControl, this.area, false);
		listenTo(this.areaController);
		this.detailsTabVC.put("areaForm", this.areaController.getInitialComponent());
		this.tabbedPane.addTab(this.trans.translate("tab.details"), this.detailsTabVC);
	}

	/**
	 * initalize the group to area association tab
	 */
	private void initAndAddGroupsTab() {
		groupsTabVC = new VelocityContainer("groupstab", VELOCITY_ROOT + "/groupstab.html", trans, this);
		tabbedPane.addTab(trans.translate("tab.groups"), groupsTabVC);

		this.allGroups = contextManager.getGroupsOfBGContext(this.bgContext);
		this.inAreaGroups = areaManager.findBusinessGroupsOfArea(this.area);
		this.groupsDataModel = new GroupsToAreaDataModel(this.allGroups, this.inAreaGroups);

		groupsChoice = new Choice("groupsChoice", trans);
		groupsChoice.setSubmitKey("submit");
		groupsChoice.setCancelKey("cancel");
		groupsChoice.setTableDataModel(groupsDataModel);
		groupsChoice.addListener(this);
		groupsTabVC.put(groupsChoice);
		groupsTabVC.contextPut("noGroupsFound", (allGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == this.groupsChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				doUpdateGroupAreaRelations();
				// do logging
				if (this.inAreaGroups.size()==0) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.BGAREA_UPDATED_NOW_EMPTY, getClass());
				} else {
					for (Iterator it = inAreaGroups.iterator(); it.hasNext();) {
						BusinessGroup aGroup = (BusinessGroup) it.next();
						ThreadLocalUserActivityLogger.log(GroupLoggingAction.BGAREA_UPDATED_MEMBER_GROUP, getClass(), 
								LoggingResourceable.wrap(aGroup));
					}
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == this.areaController) {
			if (event == Event.DONE_EVENT) {
				BGArea updatedArea = doAreaUpdate();
				if (updatedArea == null) {
					this.areaController.resetAreaName();
					getWindowControl().setWarning(this.trans.translate("error.area.name.exists"));
				} else {
					this.area = updatedArea;
					this.editVC.contextPut("title", this.trans.translate("area.edit.title", new String[] { StringEscapeUtils.escapeHtml(this.area.getName()).toString() }));
				}
			} else if (event == Event.CANCELLED_EVENT) {
				// area might have been changed, reload from db
				this.area = this.areaManager.reloadArea(this.area);
				//TODO:pb: refactor BGControllerFactory.create..AreaController to be
				//usefull here
				
				if (this.areaController != null) {
					removeAsListenerAndDispose(this.areaController);
				}
				this.areaController = new BGAreaFormController(ureq, getWindowControl(), this.area, false);
				listenTo(this.areaController);
				this.detailsTabVC.put("areaForm", this.areaController.getInitialComponent());
			}
		}
	}

	/**
	 * Update a group area
	 * 
	 * @return the updated area
	 */
	public BGArea doAreaUpdate() {
		this.area.setName(this.areaController.getAreaName());
		this.area.setDescription(this.areaController.getAreaDescription());
		return this.areaManager.updateBGArea(this.area);
	}

	/**
	 * Update the groups associated to this area: remove and add groups
	 */
	public void doUpdateGroupAreaRelations() {
		// 1) add groups to area
		List addedGroups = groupsChoice.getAddedRows();
		Iterator iterator = addedGroups.iterator();
		while (iterator.hasNext()) {
			Integer position = (Integer) iterator.next();
			BusinessGroup group = groupsDataModel.getGroup(position.intValue());
			// refresh group to prevent stale object exception and context proxy
			// issues
			group = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(group);
			// refresh group also in table model
			this.allGroups.set(position.intValue(), group);
			// add group now to area and update in area group list
			areaManager.addBGToBGArea(group, area);
			this.inAreaGroups.add(group);
		}
		// 2) remove groups from area
		List removedGroups = groupsChoice.getRemovedRows();
		iterator = removedGroups.iterator();
		while (iterator.hasNext()) {
			Integer position = (Integer) iterator.next();
			BusinessGroup group = groupsDataModel.getGroup(position.intValue());
			areaManager.removeBGFromArea(group, area);
			this.inAreaGroups.remove(group);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// don't dispose anything
	}
}
