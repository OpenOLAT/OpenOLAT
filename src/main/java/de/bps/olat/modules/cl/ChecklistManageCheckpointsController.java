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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.ExcelMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Display table with checkpoints of the checklist and the choice
 * for all identities. Table data can be filtered by learning groups.
 * 
 * <P>
 * Initial Date:  22.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistManageCheckpointsController extends BasicController {
	
	protected static final String EDIT_ACTION = "cl.edit.identity";
	protected static final String DETAILS_ACTION = "cl.user.details";
	protected static final String USER_PROPS_ID = ChecklistManageCheckpointsController.class.getCanonicalName();
	
	private Identity selectedIdentity;
	
	// GUI
	private TableController manageChecklistTable, editChecklistTable;
	private CloseableModalController cmc;
	private GroupChoiceForm groupForm;
	private Panel panel;
	private Link closeManageButton, visitingCardButton;
	
	// data
	private Checklist checklist;
	private ICourse course;
	private ChecklistManageTableDataModel manageTableData;
	private ChecklistRunTableDataModel editTableData;
	private List<BusinessGroup> lstGroups;
	private List<Identity> allIdentities, notInGroupIdentities;
	private CourseGroupManager cgm;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private CloseableModalController cmcUserInfo;
	private UserInfoMainController uimc;
	
	private final boolean readOnly;
	
	protected ChecklistManageCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, ICourse course, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale()));
		this.checklist = checklist;
		this.course = course;
		this.allIdentities = new ArrayList<>();
		this.notInGroupIdentities = new ArrayList<>();
		this.lstGroups = new ArrayList<>();
		this.readOnly = readOnly;
		
		loadData();
	
		cgm = course.getCourseEnvironment().getCourseGroupManager();
		Identity identity = ureq.getIdentity();
		boolean isAdmin = cgm.isIdentityCourseAdministrator(identity);
		if(isAdmin) {
			// collect all identities with results
			Set<Identity> identitiesWithResult = new HashSet<>();
			for( Checkpoint checkpoint : this.checklist.getCheckpoints() ) {
				for( CheckpointResult result : checkpoint.getResults() ) {
					identitiesWithResult.add(securityManager.loadIdentityByKey(result.getIdentityId()));
				}
			}
			
			// collect all identities in learning groups
			Set<Identity> identitiesInGroups = new HashSet<>();
			identitiesInGroups.addAll(cgm.getParticipantsFromBusinessGroups());
			identitiesInGroups.addAll(cgm.getParticipantsFromCurriculumElements());
			identitiesInGroups.addAll(cgm.getParticipants());
			
			// all identities with result and/or in learning groups
			Set<Identity> identitiesAll = new HashSet<>();
			identitiesAll.addAll(identitiesInGroups);
			identitiesAll.addAll(identitiesWithResult);
			allIdentities.addAll(identitiesAll);
			
			// collect all identities not in any learning group
			Set<Identity> identitiesNotInGroups = new HashSet<>();
			identitiesNotInGroups.addAll(identitiesAll);
			identitiesNotInGroups.removeAll(identitiesInGroups);
			notInGroupIdentities.addAll(identitiesNotInGroups);
			
			// collect all learning groups
			lstGroups.addAll(cgm.getAllBusinessGroups());
		} else if(cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT, GroupRoles.coach)) {
			// collect all identities in learning groups
			Set<Identity> identitiesInGroups = new HashSet<>();
			identitiesInGroups.addAll(cgm.getParticipantsFromBusinessGroups());
			identitiesInGroups.addAll(cgm.getParticipantsFromCurriculumElements());
			identitiesInGroups.addAll(cgm.getParticipants());
			allIdentities.addAll(identitiesInGroups);
			
			// collect all learning groups
			lstGroups.addAll(cgm.getAllBusinessGroups());
		} else if(cgm.isIdentityCourseCoach(identity)) {
			Set<Identity> identitiesInGroups = new HashSet<>();
			List<CurriculumElement> coachedElements = cgm.getCoachedCurriculumElements(identity);
			List<Long> curriculumElementKeys = new ArrayList<>();
			for(CurriculumElement coachedElement:coachedElements) {
				curriculumElementKeys.add(coachedElement.getKey());
			}
			List<Identity> participants = cgm.getParticipantsFromCurriculumElements(curriculumElementKeys);
			identitiesInGroups.addAll(participants);
			for( BusinessGroup group : cgm.getAllBusinessGroups() ) {
				if(businessGroupService.hasRoles(identity, group, GroupRoles.coach.name())) {
					lstGroups.add(group);
					identitiesInGroups.addAll(businessGroupService.getMembers(group, GroupRoles.participant.name()));
				}
			}
			allIdentities.addAll(identitiesInGroups);
		}
		
		displayChecklist(ureq, isAdmin);
	}
	
	private void displayChecklist(UserRequest ureq, boolean isAdmin) {
		// add title
		VelocityContainer displayChecklistVC = createVelocityContainer("manage");
		String listTitle = checklist.getTitle() == null ? "" : checklist.getTitle();
		displayChecklistVC.contextPut("checklistTitle", listTitle);
		
		// group choice
		removeAsListenerAndDispose(groupForm);
		groupForm = new GroupChoiceForm(ureq, getWindowControl(), lstGroups, isAdmin);
		listenTo(groupForm);
		
		displayChecklistVC.put("groupForm", groupForm.getInitialComponent());
		
		// the table
		panel = new Panel("manageTable");
		initManageTable(ureq);
		displayChecklistVC.put("manageTable", panel);
		
		// save and close
		closeManageButton = LinkFactory.createButton("cl.close", displayChecklistVC, this);

		putInitialPanel(displayChecklistVC);
	}
	
	private void initManageTable(UserRequest ureq) {
		// reload data
		loadData();
		
		// load participants
		List<Identity> lstIdents = new ArrayList<>();
		if(groupForm.getSelection().equals(GroupChoiceForm.CHOICE_ALL)) {
			lstIdents.addAll(allIdentities);
		} else if(groupForm.getSelection().equals(GroupChoiceForm.CHOICE_OTHERS)) {
			lstIdents.addAll(notInGroupIdentities);
		} else if(StringHelper.isLong(groupForm.getSelection())) {
			Long groupKey = Long.valueOf(groupForm.getSelection());
			BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
			lstIdents.addAll(businessGroupService.getMembers(group, GroupRoles.participant.name()));
		}
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		// prepare table for run view
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("cl.table.empty"), null, "o_cl_icon");
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "ExtendedManageTable");
		
		removeAsListenerAndDispose(manageChecklistTable);
		manageChecklistTable = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(manageChecklistTable);
		
		int cols = 0;
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			manageChecklistTable.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i++, DETAILS_ACTION, getLocale()));
			cols++;
		}

		int j = 500;

		List<Checkpoint> checkpointList = checklist.getCheckpointsSorted(ChecklistUIFactory.comparatorTitleAsc);
		for( Checkpoint checkpoint : checkpointList ) {
			String pointTitle = checkpoint.getTitle() == null ? "" : checkpoint.getTitle();
			manageChecklistTable.addColumnDescriptor(new ChecklistMultiSelectColumnDescriptor(pointTitle, j++));
			cols++;
		}
		
		if(!readOnly) {
			manageChecklistTable.addColumnDescriptor(new StaticColumnDescriptor(EDIT_ACTION, "cl.edit.title", translate(EDIT_ACTION)));
			cols++;
		}
		
		manageChecklistTable.setMultiSelect(false);
		manageTableData = new ChecklistManageTableDataModel(checkpointList, lstIdents, userPropertyHandlers, cols);
		manageChecklistTable.setTableDataModel(manageTableData);
		
		panel.setContent(manageChecklistTable.getInitialComponent());
	}
	
	private void initEditTable(UserRequest ureq, Identity identity) {
		List<Checkpoint> checkpoints = checklist.getCheckpoints();
		editTableData = new ChecklistRunTableDataModel(checkpoints, getTranslator());
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("cl.table.empty"), null, "o_cl_icon");
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "ExtendedEditTable");
		
		removeAsListenerAndDispose(editChecklistTable);
		editChecklistTable = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(editChecklistTable);
		
		editChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.title", 0, null, ureq.getLocale()));
		editChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.description", 1, null, ureq.getLocale()));
		editChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.mode", 2, null, ureq.getLocale()));
		editChecklistTable.setMultiSelect(true);
		editChecklistTable.addMultiSelectAction("cl.close", "close");
		editChecklistTable.addMultiSelectAction("cl.save.close", "save");
		editChecklistTable.setTableDataModel(editTableData);
		
		for(int i = 0; i<checkpoints.size(); i++) {
			Checkpoint checkpoint = editTableData.getObject(i);
			boolean selected = checkpoint.getSelectionFor(identity).booleanValue();
			editChecklistTable.setMultiSelectSelectedAt(i, selected);
		}
	}
	
	private void loadData() {
		checklist = ChecklistManager.getInstance().loadChecklist(checklist);
	}
	
	private void updateCheckpointsFor(Identity identity, BitSet selection) {
		ChecklistManager manager = ChecklistManager.getInstance();
		int size = checklist.getCheckpoints().size();
		for(int i = 0; i < size; i++) {
			Checkpoint checkpoint = checklist.getCheckpoints().get(i);
			Boolean selected = checkpoint.getSelectionFor(identity);
			if(selected.booleanValue() != selection.get(i)) {
				checkpoint.setSelectionFor(identity, selection.get(i));
				manager.updateCheckpoint(checkpoint);
			}
		}
	}
	
	private void downloadResults(UserRequest ureq) {
		int cdcnt = manageTableData.getColumnCount();
		int rcnt = manageTableData.getRowCount();
		StringBuilder sb = new StringBuilder();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		// additional informations
		sb.append(translate("cl.course.title")).append('\t').append(course.getCourseTitle());
		sb.append('\n');
		String listTitle = checklist.getTitle() == null ? "" : checklist.getTitle();
		sb.append(translate("cl.title")).append('\t').append(listTitle);
		sb.append('\n').append('\n');
		// header
		for (int c = 0; c < (cdcnt-1); c++) { // skip last column (action)
			ColumnDescriptor cd = manageChecklistTable.getColumnDescriptor(c);
			String headerKey = cd.getHeaderKey();
			String headerVal = cd.translateHeaderKey() ? translate(headerKey) : headerKey;
			sb.append('\t').append(headerVal);
		}
		sb.append('\n');
		// checkpoint description
		if(isAdministrativeUser) {
			sb.append('\t');
		}
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			sb.append('\t');
		}

		for (Checkpoint checkpoint : checklist.getCheckpoints()) {
			sb.append('\t').append(checkpoint.getDescription());
		}
		sb.append('\n');
		// data
		for (int r = 0; r < rcnt; r++) {
			for (int c = 0; c < (cdcnt-1); c++) { // skip last column (action)
				ColumnDescriptor cd = manageChecklistTable.getColumnDescriptor(c);
				StringOutput so = new StringOutput();
				cd.renderValue(so, r, null);
				String cellValue = so.toString();
				cellValue = StringHelper.stripLineBreaks(cellValue);
				sb.append('\t').append(cellValue);
			}
			sb.append('\n');
		}
		String res = sb.toString();

		String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
		ExcelMediaResource emr = new ExcelMediaResource(res, charset);
		ureq.getDispatchResult().setResultingMediaResource(emr);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == closeManageButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(source == visitingCardButton) {
			openVisitingCard(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == manageChecklistTable) {
			if(event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent tableEvent = (TableEvent)event;
				Long identityKey = manageTableData.getParticipantKeyAt(tableEvent.getRowId());
				selectedIdentity = securityManager.loadIdentityByKey(identityKey, false);
				if(tableEvent.getActionId().equals(EDIT_ACTION)) {
					initEditTable(ureq, selectedIdentity);
					VelocityContainer vcManageUser = createVelocityContainer("manageUser");
					vcManageUser.put("table", editChecklistTable.getInitialComponent());
					String name = selectedIdentity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()) + " " + selectedIdentity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
					visitingCardButton = LinkFactory.createLink("cl.manage.user.visitingcard", vcManageUser, this);
					visitingCardButton.setCustomDisplayText(name);
					
					removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), translate("cl.close"), vcManageUser, true, translate("cl.edit.title"));
					listenTo(cmc);
					
					cmc.activate();
				} else if(tableEvent.getActionId().equals(DETAILS_ACTION)) {
					openVisitingCard(ureq);
				}
			}
		} else if(source == editChecklistTable) {
			if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent)event;
				if(tmse.getAction().equals("save")) {
					BitSet selection = tmse.getSelection();
					updateCheckpointsFor(selectedIdentity, selection);
					initManageTable(ureq);
				}
				cmc.deactivate();
			}
		} else if(source == groupForm) {
			if(event == Event.CHANGED_EVENT) {
				initManageTable(ureq);
			} else if(event.getCommand().equals(GroupChoiceForm.EXPORT_TABLE)) {
				downloadResults(ureq);
			}
		}
	}
	
	private void openVisitingCard(UserRequest ureq) {
		
		removeAsListenerAndDispose(uimc);
		uimc = new UserInfoMainController(ureq, getWindowControl(), selectedIdentity, false, false);
		listenTo(uimc);
		
		removeAsListenerAndDispose(cmc);
		cmcUserInfo = new CloseableModalController(getWindowControl(), translate("cl.close"), uimc.getInitialComponent());
		listenTo(cmcUserInfo);
		cmcUserInfo.activate();
	}

}

class GroupChoiceForm extends FormBasicController {

	protected static final String CHOICE_ALL = "cl.choice.all";
	protected static final String CHOICE_OTHERS = "cl.choice.others";
	protected static final String EXPORT_TABLE = "cl.export";

	private List<BusinessGroup> lstGroups;
	private SingleSelection groupChoice;
	private FormLink exportButton;
	private boolean isAdmin;

	public GroupChoiceForm(UserRequest ureq, WindowControl wControl, List<BusinessGroup> lstGroups, boolean isAdmin) {
		super(ureq, wControl);
		this.lstGroups = lstGroups;
		this.isAdmin = isAdmin;
		initForm(this.flc, this, ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer mainLayout = FormLayoutContainer.createHorizontalFormLayout("mainLayout", getTranslator());
		formLayout.add(mainLayout);
		
		int size = lstGroups.size();
		String[] keys = new String[size+2];
		String[] values = new String[size+2];
		// all option
		keys[0] = CHOICE_ALL;
		values[0] = translate(CHOICE_ALL);
		// others option
		int count = 1;
		if(isAdmin) {
			keys[1] = CHOICE_OTHERS;
			values[1] = translate(CHOICE_OTHERS);
			count++;
		}
		// the groups
		for(int i = 0; i < size; i++) {
			keys[i+count] = lstGroups.get(i).getKey().toString();
			values[i+count] = lstGroups.get(i).getName();
		}
		
		groupChoice = uifactory.addDropdownSingleselect("cl.choice.groups", "cl.choice.groups", mainLayout, keys, values, null);
		groupChoice.addActionListener(FormEvent.ONCHANGE);
		groupChoice.select(CHOICE_ALL, true);
		
		exportButton = uifactory.addFormLink(EXPORT_TABLE, EXPORT_TABLE, null, mainLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == groupChoice) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(source == exportButton) {
			fireEvent(ureq, new Event(EXPORT_TABLE));
		}
	}
	
	String getSelection() {
		return groupChoice.getSelectedKey();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}
}
