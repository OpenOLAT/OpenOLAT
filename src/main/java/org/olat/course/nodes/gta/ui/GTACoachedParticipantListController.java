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
package org.olat.course.nodes.gta.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.ui.CoachParticipantsTableModel.CGCols;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedParticipantListController extends GTACoachedListController {
	
	private FlexiTableElement tableEl;
	private CoachParticipantsTableModel tableModel;


	private List<UserPropertiesRow> assessableIdentities;
	
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public GTACoachedParticipantListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, userCourseEnv.getCourseEnvironment(), gtaNode);
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		UserCourseEnvironmentImpl coachCourseEnv = (UserCourseEnvironmentImpl)userCourseEnv;

		boolean admin = userCourseEnv.isAdmin();

		Set<Identity> duplicateKiller = new HashSet<>();
		assessableIdentities = new ArrayList<>();
		List<BusinessGroup> coachedGroups = admin ? cgm.getAllBusinessGroups() : coachCourseEnv.getCoachedGroups();
		List<Identity> participants = businessGroupService.getMembers(coachedGroups, GroupRoles.participant.name());
		for(Identity participant:participants) {
			if(!duplicateKiller.contains(participant)) {
				assessableIdentities.add(new UserPropertiesRow(participant, userPropertyHandlers, getLocale()));
				duplicateKiller.add(participant);
			}
		}
		
		RepositoryEntry re = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		boolean repoTutor = admin || (coachedGroups.isEmpty() && repositoryService.hasRole(getIdentity(), re, GroupRoles.coach.name()));
		if(repoTutor) {
			List<Identity> courseParticipants = repositoryService.getMembers(re, GroupRoles.participant.name());
			for(Identity participant:courseParticipants) {
				if(!duplicateKiller.contains(participant)) {
					assessableIdentities.add(new UserPropertiesRow(participant, userPropertyHandlers, getLocale()));
					duplicateKiller.add(participant);
				}
			}
		}
		
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.username.i18nKey(), CGCols.username.ordinal(),
					true, CGCols.username.name()));
		}
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(GTACoachedGroupGradingController.USER_PROPS_ID , userPropertyHandler);
			
			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)) {
				col = new StaticFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, userPropertyHandler.getName(), true, propName,
						new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
		}

		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskName.i18nKey(), CGCols.taskName.ordinal(),
					true, CGCols.taskName.name()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskStatus.i18nKey(), CGCols.taskStatus.ordinal(),
				true, CGCols.taskStatus.name(), new TaskStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("select", translate("select"), "select"));
		tableModel = new CoachParticipantsTableModel(userPropertyHandlers, getLocale(), columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, 10, false, getTranslator(), formLayout);
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "gta-coached-participants");
	}
	
	protected void updateModel() {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<TaskLight> tasks = gtaManager.getTasksLight(entry, gtaNode);
		Map<Long,TaskLight> identityToTasks = new HashMap<>();
		for(TaskLight task:tasks) {
			if(task.getIdentityKey() != null) {
				identityToTasks.put(task.getIdentityKey(), task);
			}
		}
		
		List<CoachedIdentityRow> rows = new ArrayList<>(assessableIdentities.size());
		for(UserPropertiesRow assessableIdentity:assessableIdentities) {
			TaskLight task = identityToTasks.get(assessableIdentity.getIdentityKey());
			rows.add(new CoachedIdentityRow(assessableIdentity, task));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CoachedIdentityRow row = tableModel.getObject(se.getIndex());
				if(StringHelper.containsNonWhitespace(cmd)) {
					fireEvent(ureq, new SelectIdentityEvent(row.getIdentity().getIdentityKey()));	
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
