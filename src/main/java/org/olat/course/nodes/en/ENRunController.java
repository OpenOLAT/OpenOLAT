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

package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.BusinessGroupTableModelWithMaxSize;
import org.olat.modules.ModuleConfiguration;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<BR>
 * Run controller for the entrollment course node
 * <p>
 * Fires BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, and BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT via the
 * event agency (not controller events)
 * <P>
 * Initial Date:  Sep 8, 2004
 *
 * @author Felix Jost, gnaegi
 */
public class ENRunController extends BasicController implements GenericEventListener {
	
	private OLog log = Tracing.createLoggerFor(this.getClass());
	
	private static final String CMD_ENROLL_IN_GROUP = "cmd.enroll.in.group";
	private static final String CMD_ENROLLED_CANCEL = "cmd.enrolled.cancel";
	
	
	private ModuleConfiguration moduleConfig;
	private List enrollableGroupNames, enrollableAreaNames;
	private VelocityContainer enrollVC;
	private ENCourseNode enNode;

	private BusinessGroupTableModelWithMaxSize groupListModel;
	private TableController tableCtr;

	// Managers
	private EnrollmentManager enrollmentManager;
	private CourseGroupManager courseGroupManager;
	private CoursePropertyManager coursePropertyManager;

	// workflow variables
	private BusinessGroup enrolledGroup;
	private BusinessGroup waitingListGroup;
	
	private boolean cancelEnrollEnabled;
	
	/**
	 * @param moduleConfiguration
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param enNode
	 */
	public ENRunController(ModuleConfiguration moduleConfiguration, UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, ENCourseNode enNode) {
		super(ureq, wControl);
		
		this.moduleConfig = moduleConfiguration;
		this.enNode = enNode;
		addLoggingResourceable(LoggingResourceable.wrap(enNode));

		//this.trans = new PackageTranslator(PACKAGE, ureq.getLocale());
		// init managers
		this.enrollmentManager = EnrollmentManager.getInstance();
		this.courseGroupManager = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		this.coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();

		// Get groupnames from configuration
		String groupNamesConfig = (String) moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME);
		String areaNamesConfig = (String) moduleConfig.get(ENCourseNode.CONFIG_AREANAME);
		this.enrollableGroupNames = splitNames(groupNamesConfig);
		this.enrollableAreaNames = splitNames(areaNamesConfig);
		cancelEnrollEnabled = ((Boolean) moduleConfig.get(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED)).booleanValue();

		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		enrolledGroup = enrollmentManager.getBusinessGroupWhereEnrolled(identity, this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
		waitingListGroup = enrollmentManager.getBusinessGroupWhereInWaitingList(identity, this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
		registerGroupChangedEvents(enrollableGroupNames,enrollableAreaNames, courseGroupManager, ureq.getIdentity());
		// Set correct view
		enrollVC = createVelocityContainer("enrollmultiple");
		List groups = enrollmentManager.loadGroupsFromNames(this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
		
		tableCtr = createTableController(ureq, enrollmentManager.hasAnyWaitingList(groups));
		
		doEnrollView(ureq);

		// push title and learning objectives, only visible on intro page
		enrollVC.contextPut("menuTitle", enNode.getShortTitle());
		enrollVC.contextPut("displayTitle", enNode.getLongTitle());

		// Adding learning objectives
		String learningObj = enNode.getLearningObjectives();
		if (learningObj != null) {
			Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
			enrollVC.put("learningObjectives", learningObjectives);
			enrollVC.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator					
		}
		
		putInitialPanel (enrollVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {	
		String cmd = event.getCommand();
		 if (source == tableCtr) {
			if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				BusinessGroup choosenGroup = groupListModel.getBusinessGroupAt(rowid);
				addLoggingResourceable(LoggingResourceable.wrap(choosenGroup));
				
				if (actionid.equals(CMD_ENROLL_IN_GROUP)) {
					log.debug("CMD_ENROLL_IN_GROUP ureq.getComponentID()=" + ureq.getComponentID() + "  ureq.getComponentTimestamp()=" + ureq.getComponentTimestamp());
					EnrollStatus enrollStatus = enrollmentManager.doEnroll(ureq.getIdentity(), choosenGroup, enNode, coursePropertyManager, getWindowControl(), getTranslator(),
							                                                   enrollableGroupNames, enrollableAreaNames, courseGroupManager);
					if (enrollStatus.isEnrolled() ) {
						enrolledGroup = choosenGroup;
					} else if (enrollStatus.isInWaitingList() ) {
						waitingListGroup = choosenGroup;
					} else {
						getWindowControl().setError(enrollStatus.getErrorMessage());
					}
					// events are already fired BusinessGroupManager level :: BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, choosenGroup,  ureq.getIdentity());
					doEnrollView(ureq);
					// fire event to indicate runmaincontroller that the menuview is to update
					fireEvent(ureq, Event.DONE_EVENT);
				} else if (actionid.equals(CMD_ENROLLED_CANCEL)) {
					if (waitingListGroup != null) {
						enrollmentManager.doCancelEnrollmentInWaitingList(ureq.getIdentity(), choosenGroup, enNode, coursePropertyManager, getWindowControl(), getTranslator());
						waitingListGroup = null;
					} else {
						enrollmentManager.doCancelEnrollment(ureq.getIdentity(), choosenGroup, enNode, coursePropertyManager, getWindowControl(), getTranslator());
						enrolledGroup = null;
					}
					doEnrollView(ureq);
					if (enrolledGroup == null) {
						// fire event to indicate runmaincontroller that the menuview is to update
						fireEvent(ureq, Event.DONE_EVENT);
					}
					// events are already fired BusinessGroupManager level :: BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group,  ureq.getIdentity());
					
				}
			}
		}
	}

	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent delEvent = (OLATResourceableJustBeforeDeletedEvent) event;
			dispose();
		}	
	}	

	private void doEnrollView(UserRequest ureq) {
		//TODO read from config: 1) user can choose or 2) round robin
		// for now only case 1
    if (enrolledGroup != null) {
    	enrollVC.contextPut("isEnrolledView", Boolean.TRUE);
    	enrollVC.contextPut("isWaitingList", Boolean.FALSE);
  		String desc = this.enrolledGroup.getDescription();
  		enrollVC.contextPut("groupName", this.enrolledGroup.getName());
  		enrollVC.contextPut("groupDesc", (desc == null) ? "" : this.enrolledGroup.getDescription());    	
    } else if (waitingListGroup != null){
    	enrollVC.contextPut("isEnrolledView", Boolean.TRUE);
    	enrollVC.contextPut("isWaitingList", Boolean.TRUE);
  		String desc = this.waitingListGroup.getDescription();
  		enrollVC.contextPut("groupName", this.waitingListGroup.getName());
  		enrollVC.contextPut("groupDesc", (desc == null) ? "" : this.waitingListGroup.getDescription());    	
    } else {
    	enrollVC.contextPut("isEnrolledView", Boolean.FALSE);
    }
		doEnrollMultipleView(ureq);
	}

	private void doEnrollMultipleView(UserRequest ureq) {
		// 1. Fetch groups from database
		List groups = enrollmentManager.loadGroupsFromNames(this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
		List members = this.courseGroupManager.getNumberOfMembersFromGroups(groups);
		// 2. Build group list
		groupListModel = new BusinessGroupTableModelWithMaxSize(groups, members, getTranslator(), ureq.getIdentity(), cancelEnrollEnabled);
	  tableCtr.setTableDataModel(groupListModel);
	  tableCtr.modelChanged();
		// 3. Add group list to view
		enrollVC.put("grouplisttable", tableCtr.getInitialComponent());
	}

	private TableController createTableController(UserRequest ureq, boolean hasAnyWaitingList) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("grouplist.no.groups"));
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.name", 0, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.desc", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.partipiciant", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(hasAnyWaitingList,new DefaultColumnDescriptor("grouplist.table.waitingList", 3, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.state", 4, null, ureq.getLocale()));
		BooleanColumnDescriptor columnDesc = new BooleanColumnDescriptor("grouplist.table.enroll", 5, CMD_ENROLL_IN_GROUP,
		  	translate(CMD_ENROLL_IN_GROUP), translate("grouplist.table.no_action"));
		columnDesc.setSortingAllowed(false);
	  tableCtr.addColumnDescriptor(columnDesc);
 		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("grouplist.table.cancel_enroll", 6, CMD_ENROLLED_CANCEL,
	  		  	translate(CMD_ENROLLED_CANCEL), translate("grouplist.table.no_action")));
 		return tableCtr;
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		deregisterGroupChangedEvents(enrollableGroupNames,enrollableAreaNames, courseGroupManager);
	}

	/*
	 * Add as listener to BusinessGroups so we are being notified about changes.
	 */
	private void registerGroupChangedEvents(List enrollableGroupNames, List enrollableAreaNames, CourseGroupManager courseGroupManager, Identity identity) {
		List groups = enrollmentManager.loadGroupsFromNames(enrollableGroupNames, enrollableAreaNames, courseGroupManager);
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, (BusinessGroup) iter.next());
		}
	}
	
	private void deregisterGroupChangedEvents(List enrollableGroupNames, List enrollableAreaNames, CourseGroupManager courseGroupManager) {
		List groups = enrollmentManager.loadGroupsFromNames(enrollableGroupNames, enrollableAreaNames, courseGroupManager);
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, (BusinessGroup) iter.next());
		}
	}
	

	//////////////////
	// Helper Methods
	//////////////////
	private List splitNames(String namesList) {
		List names = new ArrayList();
		if (namesList != null) {
			String[] name = namesList.split(",");
			for (int i = 0; i < name.length; i++) {
				names.add(name[i].trim());
			}
		}
		return names;
	}

}