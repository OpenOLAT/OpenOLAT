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

package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.EnrollmentTableModelWithMaxSize.Stats;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGAreaManager;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

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

	private static final Logger log = Tracing.createLoggerFor(ENRunController.class);

	private static final String CMD_VISIT_CARD = "cmd.visit.card";
	private static final String CMD_ENROLL_IN_GROUP = "cmd.enroll.in.group";
	private static final String CMD_ENROLLED_CANCEL = "cmd.enrolled.cancel";


	private ModuleConfiguration moduleConfig;
	private List<Long> enrollableGroupKeys;
	private List<Long> enrollableAreaKeys;
	private VelocityContainer enrollVC;
	private ENCourseNode enNode;

	private EnrollmentTableModelWithMaxSize groupListModel;
	private TableController tableCtr;

	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private EnrollmentManager enrollmentManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	private final UserCourseEnvironment userCourseEnv;
	private CourseGroupManager courseGroupManager;
	private CoursePropertyManager coursePropertyManager;

	private boolean cancelEnrollEnabled;
	private int maxEnrollCount;

	//registered in event bus
	private List<Long> registeredGroupKeys;

	public ENRunController(ModuleConfiguration moduleConfiguration, UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, ENCourseNode enNode) {
		super(ureq, wControl);

		this.moduleConfig = moduleConfiguration;
		this.enNode = enNode;
		this.userCourseEnv = userCourseEnv;
		addLoggingResourceable(LoggingResourceable.wrap(enNode));

		// init managers
		courseGroupManager = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();

		// Get groupnames from configuration
		enrollableGroupKeys = moduleConfig.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		if(enrollableGroupKeys == null || enrollableGroupKeys.isEmpty()) {
			String groupNamesConfig = (String)moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME);
			enrollableGroupKeys = businessGroupService.toGroupKeys(groupNamesConfig, courseGroupManager.getCourseEntry());
		}

		enrollableAreaKeys = moduleConfig.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
		if(enrollableAreaKeys == null || enrollableAreaKeys.isEmpty()) {
			String areaInitVal = (String) moduleConfig.get(ENCourseNode.CONFIG_AREANAME);
			enrollableAreaKeys = areaManager.toAreaKeys(areaInitVal, courseGroupManager.getCourseResource());
		}

		maxEnrollCount = moduleConfiguration.getIntegerSafe(ENCourseNode.CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT, 1);
		cancelEnrollEnabled = moduleConfig.getBooleanSafe(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED);

		registerGroupChangedEvents(enrollableGroupKeys, enrollableAreaKeys, getIdentity());
		// Set correct view
		enrollVC = createVelocityContainer("enrollmultiple");

		List<EnrollmentRow> enrollmentRows = enrollmentManager.getEnrollments(getIdentity(), enrollableGroupKeys, enrollableAreaKeys, 256);

		// Sort groups
		if (moduleConfig.getBooleanSafe(ENCourseNode.CONFIG_GROUP_SORTED, false)) {
			for (int i = 0; i < enrollableGroupKeys.size(); i++) {
				long groupKey = enrollableGroupKeys.get(i);
				for (int j = i; j < enrollmentRows.size(); j++) {
					if (enrollmentRows.get(j).getKey().equals(groupKey)) {
						if (i == j) {
							break;
						} else {
							EnrollmentRow temp = enrollmentRows.get(i);
							enrollmentRows.set(i, enrollmentRows.get(j));
							enrollmentRows.set(j, temp);
						}
					}
				}
			}
		}

		for (int i = 0; i <enrollmentRows.size(); i++) {
			enrollmentRows.get(i).setSortKey(i);
		}

		groupListModel = new EnrollmentTableModelWithMaxSize(enrollmentRows, getTranslator(), getIdentity(), cancelEnrollEnabled, maxEnrollCount);
		Stats stats = groupListModel.getStats();
		tableCtr = createTableController(ureq, stats.isSomeGroupWaitingListEnabled());
		tableCtr.setTableDataModel(groupListModel);

		doEnrollView(stats);

		// push title and learning objectives, only visible on intro page
		enrollVC.contextPut("menuTitle", enNode.getShortTitle());
		enrollVC.contextPut("displayTitle", enNode.getLongTitle());

		putInitialPanel (enrollVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		String cmd = event.getCommand();
		if (source == tableCtr) {
			if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				EnrollmentRow row = groupListModel.getObject(rowid);
				Long choosenGroupKey = row.getKey();

				if (actionid.equals(CMD_ENROLL_IN_GROUP)) {
					BusinessGroup choosenGroup = businessGroupService.loadBusinessGroup(choosenGroupKey);
					addLoggingResourceable(LoggingResourceable.wrap(choosenGroup));

					if(log.isDebugEnabled()) {
						log.debug("CMD_ENROLL_IN_GROUP ureq.getComponentID()=" + ureq.getComponentID() + "  ureq.getComponentTimestamp()=" + ureq.getComponentTimestamp());
					}

					EnrollStatus enrollStatus = enrollmentManager.doEnroll(userCourseEnv,
							ureq.getUserSession().getRoles(), choosenGroup, enNode, coursePropertyManager,
							getWindowControl(), getTranslator(), enrollableGroupKeys, enrollableAreaKeys,
							courseGroupManager);
					if (enrollStatus.isEnrolled() || enrollStatus.isInWaitingList() ) {
						//OK
					} else {
						getWindowControl().setError(enrollStatus.getErrorMessage());
					}
					// events are already fired BusinessGroupManager level :: BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, choosenGroup,  ureq.getIdentity());
					// but async
					// fire event to indicate runmaincontroller that the menuview is to update
					doEnrollView(updateModel());
					if (enrollStatus.isEnrolled() ) {
						fireEvent(ureq, new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, choosenGroup, getIdentity(), null));
					} else {
						fireEvent(ureq, Event.DONE_EVENT);
					}

				} else if (actionid.equals(CMD_ENROLLED_CANCEL)) {
					BusinessGroup choosenGroup = businessGroupService.loadBusinessGroup(choosenGroupKey);
					addLoggingResourceable(LoggingResourceable.wrap(choosenGroup));

					List<String> roles = businessGroupService.getIdentityRolesInBusinessGroup(getIdentity(), choosenGroup);
					RepositoryEntry courseEntry = courseGroupManager.getCourseEntry();
					if (roles.contains(GroupRoles.waiting.name())) {
						enrollmentManager.doCancelEnrollmentInWaitingList(userCourseEnv, choosenGroup, courseEntry,
								enNode, coursePropertyManager, getWindowControl(), getTranslator());
					} else if (roles.contains(GroupRoles.participant.name())) {
						enrollmentManager.doCancelEnrollment(userCourseEnv, choosenGroup, courseEntry, enNode,
								coursePropertyManager, getWindowControl(), getTranslator());
					}

					// fire event to indicate runmaincontroller that the menuview is to update
					fireEvent(ureq, new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, choosenGroup, getIdentity(), null));
					// events are already fired BusinessGroupManager level :: BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group,  ureq.getIdentity());
					// but async
					doEnrollView(updateModel());
				} else if(CMD_VISIT_CARD.equals(actionid)) {
					List<String> roles = businessGroupService.getIdentityRolesInBusinessGroup(getIdentity(), row);

					String businessPath;
					if(roles.contains(GroupRoles.coach.name()) || roles.contains(GroupRoles.participant.name())) {
						businessPath = "[BusinessGroup:" + choosenGroupKey + "]";
					} else {
						businessPath = "[GroupCard:" + choosenGroupKey + "]";
					}
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				}
			}
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			dispose();
		}
	}

	private void doEnrollView(Stats stats) {
		//num. of groups where the user is participant or in the waiting list
		int numOfParticipatingGroups = stats.getParticipantingGroupNames().size();
		int numOfWaitingGroups = stats.getWaitingGroupNames().size();

		enrollVC.contextPut("multiEnroll", (maxEnrollCount > 1 && numOfParticipatingGroups + numOfWaitingGroups < maxEnrollCount));
		if(numOfParticipatingGroups > 0 || numOfWaitingGroups > 0) {
			int numOfConfiguredAuthorizedEnrollments = maxEnrollCount - numOfParticipatingGroups - numOfWaitingGroups;
			int numOfAvailableAuthorizedEnrollments = groupListModel.getRowCount() - numOfParticipatingGroups - numOfWaitingGroups;
			int numOfAuthorizedEnrollments = Math.min(numOfConfiguredAuthorizedEnrollments, numOfAvailableAuthorizedEnrollments);
			String[] hintNumbers = new String[]{
					String.valueOf(numOfParticipatingGroups + numOfWaitingGroups),
					String.valueOf(numOfAuthorizedEnrollments)
			};
			enrollVC.contextPut("multipleHint", translate("multiple.select.hint.outstanding", hintNumbers));
		} else {
			int numOfAuthorizedEnrollments = Math.min(groupListModel.getRowCount(), maxEnrollCount);
			enrollVC.contextPut("multipleHint", translate("multiple.select.hint", String.valueOf(numOfAuthorizedEnrollments)));
		}

		if (numOfParticipatingGroups > 0) {
			enrollVC.contextPut("isEnrolledView", Boolean.TRUE);
			List<String> groupnames = new ArrayList<>(numOfParticipatingGroups);
			for(String groupName: stats.getParticipantingGroupNames()){
				groupnames.add(StringHelper.escapeHtml(groupName));
			}
			enrollVC.contextPut("groupNames", groupnames);
		} else {
			enrollVC.contextPut("isEnrolledView", Boolean.FALSE);
		}

		if (numOfWaitingGroups > 0){
			enrollVC.contextPut("isInWaitingList", Boolean.TRUE);
			List<String> waitingListNames = new ArrayList<>(numOfWaitingGroups);
			for(String groupName:stats.getWaitingGroupNames()){
				waitingListNames.add(StringHelper.escapeHtml(groupName));
			}
			enrollVC.contextPut("waitingListNames", waitingListNames);
		} else {
			enrollVC.contextPut("isInWaitingList", Boolean.FALSE);
		}
		// 3. Add group list to view
		enrollVC.put("grouplisttable", tableCtr.getInitialComponent());
	}

	private Stats updateModel() {
		List<EnrollmentRow> enrollmentRows = enrollmentManager.getEnrollments(getIdentity(), enrollableGroupKeys, enrollableAreaKeys, 256);
		groupListModel = new EnrollmentTableModelWithMaxSize(enrollmentRows, getTranslator(), getIdentity(), cancelEnrollEnabled, maxEnrollCount);
		Stats stats = groupListModel.getStats();
		tableCtr.setTableDataModel(groupListModel);
		return stats;
	}

	private TableController createTableController(UserRequest ureq, boolean hasAnyWaitingList) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("grouplist.no.groups"), null, "o_icon_group");

		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);


		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("grouplist.table.sort", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.name", 1, CMD_VISIT_CARD, getLocale()));
		DefaultColumnDescriptor descCd = new DefaultColumnDescriptor("grouplist.table.desc", 2, null, getLocale());
		descCd.setEscapeHtml(EscapeMode.antisamy);
		tableCtr.addColumnDescriptor(descCd);
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.partipiciant", 3, null, getLocale()) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
				if (a == null || b == null) {
					boolean bb = (b == null);
					return (a == null) ? (bb ? 0: -1) : (bb ? 1: 0);
				}
				try {
					Integer la, lb;
					if (a instanceof String) {
						String sa = (String) a;
						la = Integer.parseInt(sa.substring(0, sa.indexOf("/")));
					} else {
						la = (Integer) a;
					}
					if (b instanceof String) {
						String sb = (String) b;
						lb = Integer.parseInt(sb.substring(0, sb.indexOf("/")));
					} else {
						lb = (Integer) b;
					}
					return la.compareTo(lb);
				} catch (NumberFormatException e) {
					return super.compareTo(rowa, rowb);
				}
			}
		});
		tableCtr.addColumnDescriptor(hasAnyWaitingList, new DefaultColumnDescriptor("grouplist.table.waitingList", 4, null, getLocale()));
		DefaultColumnDescriptor stateColdEsc = new DefaultColumnDescriptor("grouplist.table.state", 5, null, getLocale());
		stateColdEsc.setEscapeHtml(EscapeMode.none);
		tableCtr.addColumnDescriptor(stateColdEsc);
		String enrollCmd = userCourseEnv.isCourseReadOnly() ? null : CMD_ENROLL_IN_GROUP;
		BooleanColumnDescriptor columnDesc = new BooleanColumnDescriptor("grouplist.table.enroll", 6, enrollCmd,
		  	translate(CMD_ENROLL_IN_GROUP), translate("grouplist.table.no_action")) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Integer a = (Integer) table.getTableDataModel().getValueAt(rowa, 0);
				Integer b = (Integer) table.getTableDataModel().getValueAt(rowb, 0);
				
				return Integer.compare(a, b);
			}
		};
		tableCtr.addColumnDescriptor(columnDesc);
		String cancelCmd = userCourseEnv.isCourseReadOnly() ? null : CMD_ENROLLED_CANCEL;
 		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("grouplist.table.cancel_enroll", 7, cancelCmd,
	  		  	translate(CMD_ENROLLED_CANCEL), translate("grouplist.table.no_action")));
 		tableCtr.setSortColumn(5, true);
 		return tableCtr;
	}

	@Override
	protected void doDispose() {
		deregisterGroupChangedEvents();
	}

	/*
	 * Add as listener to BusinessGroups so we are being notified about changes.
	 */
	private void registerGroupChangedEvents(List<Long> groupKeys, List<Long> areaKeys, Identity identity) {
		registeredGroupKeys = enrollmentManager.getBusinessGroupKeys(groupKeys, areaKeys);
		for (Long groupKey: registeredGroupKeys) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(BusinessGroup.class, groupKey);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, ores);
		}
	}

	private void deregisterGroupChangedEvents() {
		if(registeredGroupKeys != null) {
			for (Long groupKey:registeredGroupKeys) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(BusinessGroup.class, groupKey);
				CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, ores);
			}
		}
	}
}