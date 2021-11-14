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
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.admin.user.groups.BusinessGroupTableModelWithType.Cols;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.AddToGroupsEvent;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.BGRoleCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * GroupOverviewController creates a model and displays a table with all groups a user is in.
 * The following rows are shown: type of group, groupname, 
 * role of user in group (participant, owner, on waiting list), date of joining the group
 * 
 * <P>
 * Initial Date:  22.09.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupOverviewController extends BasicController {
	private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	private static final String TABLE_ACTION_UNSUBSCRIBE = "unsubscribe";
	
	private final VelocityContainer vc;
	private final TableController groupListCtr;
	private final BusinessGroupTableModelWithType tableDataModel;
	
	private Link addGroups;
	private DialogBoxController confirmSendMailBox;
	private CloseableModalController cmc;
	private GroupSearchController groupsCtrl;
	private GroupLeaveDialogBoxController removeFromGrpDlg;

	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	private final Identity identity;

	public GroupOverviewController(UserRequest ureq, WindowControl control, Identity identity, boolean canEdit, boolean canOpenGroup) {
		super(ureq, control, Util.createPackageTranslator(BusinessGroupTableModelWithType.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(BGRoleCellRenderer.class, getLocale(), getTranslator()));
		
		this.identity = identity;

		vc = createVelocityContainer("groupoverview");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("table.emtpy"), (canEdit ? translate("table.empty.hint") : null), "o_icon_group");
		tableConfig.setTableEmptyNextPrimaryAction(translate("add.groups"), "o_icon_add");
		groupListCtr = new TableController(tableConfig, ureq, control, getTranslator());
		listenTo(groupListCtr);
		groupListCtr.addColumnDescriptor(new BusinessGroupNameColumnDescriptor(canOpenGroup ? TABLE_ACTION_LAUNCH : null, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.key.i18n(), Cols.key.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), null, getLocale()));
		CustomCellRenderer roleRenderer = new BGRoleCellRenderer(getLocale());
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer));
		if(canEdit) {
			groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(),
					TABLE_ACTION_UNSUBSCRIBE, translate("table.header.leave"), null));
			
			groupListCtr.setMultiSelect(true);
			groupListCtr.addMultiSelectAction("table.leave", TABLE_ACTION_UNSUBSCRIBE);
			addGroups = LinkFactory.createButton("add.groups", vc, this);
			addGroups.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		}
		
		tableDataModel = new BusinessGroupTableModelWithType(getTranslator(), 4);
		groupListCtr.setTableDataModel(tableDataModel);		
		vc.put("table.groups", groupListCtr.getInitialComponent());	
		updateModel();
		putInitialPanel(vc);
	}

	/**
	 * @param ureq
	 * @param control
	 * @param identity
	 * @return
	 */
	private void updateModel() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(identity);
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		List<Long> groupKeysWithMembers;
		if(groups.size() > 50) {
			groupKeysWithMembers = null;
		} else {
			groupKeysWithMembers = new ArrayList<>(groups.size());
			for(BusinessGroup view:groups) {
				groupKeysWithMembers.add(view.getKey());
			}
		}

		//retrieve all user's membership if there are more than 50 groups
		List<BusinessGroupMembership> groupsAsOwner = businessGroupService.getBusinessGroupMembership(groupKeysWithMembers, identity);
		Map<Long, BusinessGroupMembership> memberships = new HashMap<>();
		for(BusinessGroupMembership membership: groupsAsOwner) {
			memberships.put(membership.getGroupKey(), membership);
		}

		List<GroupOverviewRow> items = new ArrayList<>();
		for(BusinessGroup group:groups) {
			BusinessGroupMembership membership =  memberships.get(group.getKey());
			GroupOverviewRow tableItem = new GroupOverviewRow(group, membership, Boolean.TRUE);
			items.add(tableItem);
		}
		tableDataModel.setEntries(items);
		groupListCtr.modelChanged();
	}

	@Override
	protected void event(	UserRequest ureq, Component source, Event event) {
		if (source == addGroups){
			doGroupAddDialog(ureq);
		}
	}

	private void doGroupAddDialog(UserRequest ureq) {
		groupsCtrl = new GroupSearchController(ureq, getWindowControl());
		listenTo(groupsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("add.groups"), groupsCtrl.getInitialComponent(), true, translate("add.groups"), true);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == groupListCtr){
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				GroupOverviewRow item = tableDataModel.getObject(te.getRowId());
				BusinessGroup currBusinessGroup = businessGroupService.loadBusinessGroup(item.getKey());
				if (currBusinessGroup==null) {
					//group seems to be removed meanwhile, reload table and show error
					showError("group.removed");
					updateModel();	
				} else if (TABLE_ACTION_LAUNCH.equals(te.getActionId())) {
					NewControllerFactory.getInstance().launch("[BusinessGroup:" + currBusinessGroup.getKey() + "]", ureq, getWindowControl());
				} else if (TABLE_ACTION_UNSUBSCRIBE.equals(te.getActionId())){
					doLeave(ureq, Collections.singletonList(currBusinessGroup));
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent mse = (TableMultiSelectEvent)event;
				List<GroupOverviewRow> items = tableDataModel.getObjects(mse.getSelection());
				if (TABLE_ACTION_UNSUBSCRIBE.equals(mse.getAction())){
					List<BusinessGroup> groups = toBusinessGroups(items);
					doLeave(ureq, groups);
				}
			} else if (event.equals(TableController.EVENT_EMPTY_TABLE_NEXT_PRIMARY_ACTION)) {
				doGroupAddDialog(ureq);
			}
		}	else if (source == groupsCtrl && event instanceof AddToGroupsEvent){
			AddToGroupsEvent groupsEv = (AddToGroupsEvent) event;
			if (groupsEv.isEmpty()) {
				// no groups selected
				showWarning("group.add.result.none");
			} else {
				if (cmc != null) {
					cmc.deactivate();				
				}
				
				boolean mailMandatory = groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
				if(mailMandatory) {
					doAddToGroups(groupsEv, true);
					updateModel();
				} else {
					confirmSendMailBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailBox);
					confirmSendMailBox.setUserObject(groupsEv);
				}
			}
			cleanUpPopups();
		} else if(source == confirmSendMailBox) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			AddToGroupsEvent groupsEv = (AddToGroupsEvent)confirmSendMailBox.getUserObject();
			doAddToGroups(groupsEv, sendMail);
			updateModel();
		} else if (source == removeFromGrpDlg){
			if(event == Event.DONE_EVENT) {
				boolean sendMail = removeFromGrpDlg.isSendMail();
				List<BusinessGroup> groupsToDelete = removeFromGrpDlg.getGroupsToDelete();
				List<BusinessGroup> groupsToLeave = removeFromGrpDlg.getGroupsToLeave();
				removeUserFromGroup(ureq, groupsToLeave, groupsToDelete, sendMail);
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == cmc) {
			cleanUpPopups();
		}
	}
	
	private void cleanUpPopups() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(removeFromGrpDlg);
		removeAsListenerAndDispose(groupsCtrl);
		cmc = null;
		groupsCtrl = null;
		removeFromGrpDlg = null;
	}
	
	private void doAddToGroups(AddToGroupsEvent e, boolean sendMail) {
		List<BusinessGroupMembershipChange> changes = new ArrayList<>();
		if(e.getOwnerGroupKeys() != null && !e.getOwnerGroupKeys().isEmpty()) {
			for(Long tutorGroupKey:e.getOwnerGroupKeys()) {
				BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(identity, tutorGroupKey);
				change.setTutor(Boolean.TRUE);
				changes.add(change);
			}
		}
		if(e.getParticipantGroupKeys() != null && !e.getParticipantGroupKeys().isEmpty()) {
			for(Long partGroupKey:e.getParticipantGroupKeys()) {
				BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(identity, partGroupKey);
				change.setParticipant(Boolean.TRUE);
				changes.add(change);
			}
		}
		
		MailPackage mailing = new MailPackage(sendMail);
		businessGroupService.updateMemberships(getIdentity(), changes, mailing);
	}
	
	private void doLeave(UserRequest ureq, List<BusinessGroup> groupsToLeave) {
		List<BusinessGroup> groupsToDelete = new ArrayList<>(1);
		for(BusinessGroup group:groupsToLeave) {
			int numOfOwners = businessGroupService.countMembers(group, GroupRoles.coach.name());
			int numOfParticipants = businessGroupService.countMembers(group, GroupRoles.participant.name());
			if ((numOfOwners == 1 && numOfParticipants == 0) || (numOfOwners == 0 && numOfParticipants == 1)) {
				groupsToDelete.add(group);
			}
		}
		removeFromGrpDlg = new GroupLeaveDialogBoxController(ureq, getWindowControl(), identity, groupsToLeave, groupsToDelete);
		listenTo(removeFromGrpDlg);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeFromGrpDlg.getInitialComponent(),
				true, translate("unsubscribe.title"));
		cmc.activate();
		listenTo(cmc);
	}

	/**
	 * 
	 * @param ureq
	 * @param doSendMail
	 */
	private void removeUserFromGroup(UserRequest ureq, List<BusinessGroup> groupsToLeave, List<BusinessGroup> groupsToDelete, boolean doSendMail) {
		Roles roles = ureq.getUserSession().getRoles();
		
		for(BusinessGroup group:groupsToLeave) {
			if (groupsToDelete.contains(group)) {
				// really delete the group as it has no more owners/participants
				businessGroupLifecycleManager.deleteBusinessGroup(group, getIdentity(), doSendMail);
			} else {
				// 1) remove as owner
				if (businessGroupService.hasRoles(identity, group, GroupRoles.coach.name())) {
					businessGroupService.removeOwners(ureq.getIdentity(), Collections.singletonList(identity), group);
				}
				MailPackage mailing = new MailPackage(doSendMail);
				// 2) remove as participant
				businessGroupService.removeParticipants(getIdentity(), Collections.singletonList(identity), group, mailing);
				MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(),
						roles.isAdministrator() || roles.isSystemAdmin(), getLocale());
			}
		}

		updateModel();

		StringBuilder groupNames = new StringBuilder();
		for(BusinessGroup group:groupsToLeave) {
			if(groupNames.length() > 0) groupNames.append(", ");
			groupNames.append(group.getName());
		}
		showInfo("unsubscribe.successful", groupNames.toString());	
	}
	
	private List<BusinessGroup> toBusinessGroups(List<GroupOverviewRow> items) {
		List<Long> groupKeys = new ArrayList<>();
		for(GroupOverviewRow item:items) {
			groupKeys.add(item.getKey());
		}
		return businessGroupService.loadBusinessGroups(groupKeys);
	}
}
