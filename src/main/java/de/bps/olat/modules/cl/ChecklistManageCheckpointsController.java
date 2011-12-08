/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
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
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
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
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;

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
	
	protected final static String EDIT_ACTION = "cl.edit.identity";
	protected final static String DETAILS_ACTION = "cl.user.details";
	
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
	
	private CloseableModalController cmcUserInfo;
	private UserInfoMainController uimc;
	
	protected ChecklistManageCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, ICourse course) {
		super(ureq, wControl);
		this.checklist = checklist;
		this.course = course;
		this.allIdentities = new ArrayList<Identity>();
		this.notInGroupIdentities = new ArrayList<Identity>();
		this.lstGroups = new ArrayList<BusinessGroup>();
		
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		loadData();
		
		
		cgm = course.getCourseEnvironment().getCourseGroupManager();
		Identity identity = ureq.getIdentity();
		boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin() || cgm.isIdentityCourseAdministrator(identity);
		if(cgm.isIdentityCourseAdministrator(identity)) {
			// collect all identities with results
			HashSet<Identity> identitiesWithResult = new HashSet<Identity>();
			for( Checkpoint checkpoint : this.checklist.getCheckpoints() ) {
				for( CheckpointResult result : checkpoint.getResults() ) {
					identitiesWithResult.add(secMgr.loadIdentityByKey(result.getIdentityId()));
				}
			}
			
			// collect all identities in learning groups
			HashSet<Identity> identitiesInGroups = new HashSet<Identity>();
			identitiesInGroups.addAll(cgm.getParticipantsFromLearningGroup(null));
			//fxdiff VCRP-1,2: access control of resources
			identitiesInGroups.addAll(cgm.getParticipants());
			
			// all identities with result and/or in learning groups
			HashSet<Identity> identitiesAll = new HashSet<Identity>();
			identitiesAll.addAll(identitiesInGroups);
			identitiesAll.addAll(identitiesWithResult);
			allIdentities.addAll(identitiesAll);
			
			// collect all identities not in any learning group
			HashSet<Identity> identitiesNotInGroups = new HashSet<Identity>();
			identitiesNotInGroups.addAll(identitiesAll);
			identitiesNotInGroups.removeAll(identitiesInGroups);
			notInGroupIdentities.addAll(identitiesNotInGroups);
			
			// collect all learning groups
			lstGroups.addAll(cgm.getAllLearningGroupsFromAllContexts());
		} else if(cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT)) {
			// collect all identities in learning groups
			HashSet<Identity> identitiesInGroups = new HashSet<Identity>();
			identitiesInGroups.addAll(cgm.getParticipantsFromLearningGroup(null));
			//fxdiff VCRP-1,2: access control of resources
			identitiesInGroups.addAll(cgm.getParticipants());
			allIdentities.addAll(identitiesInGroups);
			
			// collect all learning groups
			lstGroups.addAll(cgm.getAllLearningGroupsFromAllContexts());
		} else if(cgm.isIdentityCourseCoach(identity)) {
			HashSet<Identity> identitiesInGroups = new HashSet<Identity>();
			for( Object obj : cgm.getAllLearningGroupsFromAllContexts() ) {
				BusinessGroup group = (BusinessGroup) obj;
				if(cgm.getCoachesFromLearningGroup(group.getName()).contains(identity)) {
					lstGroups.add(group);
					identitiesInGroups.addAll(cgm.getParticipantsFromLearningGroup(group.getName()));
				}
			}
			allIdentities.addAll(identitiesInGroups);
		}
		
		displayChecklist(ureq, isAdmin);
	}
	
	private void displayChecklist(UserRequest ureq, boolean isAdmin) {
		// add title
		VelocityContainer displayChecklistVC = this.createVelocityContainer("manage");
		displayChecklistVC.contextPut("checklistTitle", this.checklist.getTitle());
		
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
		List<Identity> lstIdents = new ArrayList<Identity>();
		if(groupForm.getSelection().equals(GroupChoiceForm.CHOICE_ALL)) {
			lstIdents.addAll(allIdentities);
		} else if(groupForm.getSelection().equals(GroupChoiceForm.CHOICE_OTHERS)) {
			lstIdents.addAll(notInGroupIdentities);
		} else {
			lstIdents.addAll(cgm.getParticipantsFromLearningGroup(groupForm.getSelection()));
		}
		
		// prepare table for run view
		manageTableData = new ChecklistManageTableDataModel(this.checklist, lstIdents);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("cl.table.empty"));
		tableConfig.setColumnMovingOffered(true);
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "ExtendedManageTable");
		
		removeAsListenerAndDispose(manageChecklistTable);
		manageChecklistTable = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(manageChecklistTable);
		
		manageChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.identity", 0, DETAILS_ACTION, ureq.getLocale()));
		int i = 1;
		for( Checkpoint checkpoint : checklist.getCheckpoints() ) {
			manageChecklistTable.addColumnDescriptor(new ChecklistMultiSelectColumnDescriptor(checkpoint.getTitle(), i));
			i++;
		}
		manageChecklistTable.addColumnDescriptor(new BooleanColumnDescriptor("cl.edit.title", i, EDIT_ACTION, translate(EDIT_ACTION), ""));
		manageChecklistTable.setMultiSelect(false);
		manageChecklistTable.setTableDataModel(manageTableData);
		
		panel.setContent(manageChecklistTable.getInitialComponent());
	}
	
	private void initEditTable(UserRequest ureq, Identity identity) {
		editTableData = new ChecklistRunTableDataModel(this.checklist.getCheckpoints(), getTranslator());
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("cl.table.empty"));
		tableConfig.setColumnMovingOffered(true);
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
		
		for(int i = 0; i < this.checklist.getCheckpoints().size(); i++) {
			Checkpoint checkpoint = (Checkpoint) editTableData.getObject(i);
			boolean selected = checkpoint.getSelectionFor(identity).booleanValue();
			editChecklistTable.setMultiSelectSelectedAt(i, selected);
		}
	}
	
	private void loadData() {
		this.checklist = ChecklistManager.getInstance().loadChecklist(checklist);
	}
	
	private void updateCheckpointsFor(Identity identity, BitSet selection) {
		ChecklistManager manager = ChecklistManager.getInstance();
		int size = this.checklist.getCheckpoints().size();
		for(int i = 0; i < size; i++) {
			Checkpoint checkpoint = this.checklist.getCheckpoints().get(i);
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
		// additional informations
		sb.append(translate("cl.course.title")).append('\t').append(this.course.getCourseTitle());
		sb.append('\n');
		sb.append(translate("cl.title")).append('\t').append(this.checklist.getTitle());
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
		sb.append('\t');
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

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
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
				selectedIdentity = manageTableData.getParticipantAt(tableEvent.getRowId());
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
		uimc = new UserInfoMainController(ureq, getWindowControl(), selectedIdentity);
		listenTo(uimc);
		
		removeAsListenerAndDispose(cmc);
		cmcUserInfo = new CloseableModalController(getWindowControl(), translate("cl.close"), uimc.getInitialComponent());
		listenTo(cmcUserInfo);
		cmcUserInfo.activate();
	}

}

class GroupChoiceForm extends FormBasicController {

	protected final static String CHOICE_ALL = "cl.choice.all";
	protected final static String CHOICE_OTHERS = "cl.choice.others";
	protected final static String EXPORT_TABLE = "cl.export";

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
			keys[i+count] = lstGroups.get(i).getName();
			values[i+count] = lstGroups.get(i).getName();
		}
		
		groupChoice = uifactory.addDropdownSingleselect("cl.choice.groups", "cl.choice.groups", mainLayout, keys, values, null);
		groupChoice.addActionListener(this, FormEvent.ONCHANGE);
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
	protected void doDispose() {
		// nothing to do
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}
}
