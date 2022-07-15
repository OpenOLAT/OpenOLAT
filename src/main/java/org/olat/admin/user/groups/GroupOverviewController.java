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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.admin.user.groups.BusinessGroupTableModelWithType.Cols;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
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
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.model.InvitationEntry;
import org.olat.modules.invitation.ui.InvitationURLController;
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
public class GroupOverviewController extends FormBasicController {
	private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	private static final String TABLE_ACTION_UNSUBSCRIBE = "unsubscribe";

	private FormLink addGroups;
	private FormLink leaveLink;
	private FlexiTableElement tableEl;
	private BusinessGroupTableModelWithType tableDataModel;
	
	private CloseableModalController cmc;
	private GroupSearchController groupsCtrl;
	private DialogBoxController confirmSendMailBox;
	private InvitationURLController invitationUrlCtrl;
	private GroupLeaveDialogBoxController removeFromGrpDlg;
	private CloseableCalloutWindowController urlCalloutCtrl;
	
	private int counter = 0;
	private final Identity identity;
	private final boolean canEdit;
	private final boolean canOpenGroup;
	private final boolean isInvitee;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;

	public GroupOverviewController(UserRequest ureq, WindowControl control, Identity editedIdentity,
			boolean canEdit, boolean canOpenGroup) {
		super(ureq, control, "groupoverview", Util.createPackageTranslator(BusinessGroupTableModelWithType.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(BGRoleCellRenderer.class, getLocale(), getTranslator()));	
		this.identity = editedIdentity;
		this.canEdit = canEdit;
		this.canOpenGroup = canOpenGroup;
		isInvitee = securityManager.getRoles(editedIdentity).isInvitee();

		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key));
		String action = canOpenGroup ? TABLE_ACTION_LAUNCH : null;
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name, action, new BusinessGroupNameCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.firstTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lastTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.role, new BGRoleCellRenderer(getLocale())));
		
		DefaultFlexiColumnModel invitationLinkCol = new DefaultFlexiColumnModel(isInvitee, Cols.invitationLink);
		invitationLinkCol.setExportable(false);
		invitationLinkCol.setAlwaysVisible(isInvitee);
		columnsModel.addFlexiColumnModel(invitationLinkCol);
		
		if(canEdit) {
			DefaultFlexiColumnModel leaveCol = new DefaultFlexiColumnModel(Cols.allowLeave, TABLE_ACTION_UNSUBSCRIBE,
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("table.header.leave"), TABLE_ACTION_UNSUBSCRIBE), null));
			leaveCol.setAlwaysVisible(true);
			leaveCol.setExportable(false);
			columnsModel.addFlexiColumnModel(leaveCol);	
		}
		
		tableDataModel = new BusinessGroupTableModelWithType(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table.groups", tableDataModel, 25, false, getTranslator(), formLayout);
		if(canEdit) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			tableEl.setEmptyTableSettings("table.empty.hint", null, "o_icon_group", "add.groups", "o_icon_add", false);					
			
			leaveLink = uifactory.addFormLink("table.leave", formLayout, Link.BUTTON);
			tableEl.addBatchButton(leaveLink);
			
			addGroups = uifactory.addFormLink("add.groups", formLayout, Link.BUTTON);
			addGroups.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		} else {
			tableEl.setEmptyTableSettings("table.empty", null, "o_icon_group", null, null, false);	
		}
	}

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
		
		List<InvitationEntry> invitations = invitationService.findInvitations(identity);
		Map<Group,Invitation> groupToInvitations = invitations.stream()
				.collect(Collectors.toMap(InvitationEntry::getInvitationGroup, InvitationEntry::getInvitation, (u, v) -> u));

		//retrieve all user's membership if there are more than 50 groups
		List<BusinessGroupMembership> groupsAsOwner = businessGroupService.getBusinessGroupMembership(groupKeysWithMembers, identity);
		Map<Long, BusinessGroupMembership> memberships = new HashMap<>();
		for(BusinessGroupMembership membership: groupsAsOwner) {
			memberships.put(membership.getGroupKey(), membership);
		}

		List<GroupOverviewRow> items = new ArrayList<>();
		for(BusinessGroup group:groups) {
			BusinessGroupMembership membership =  memberships.get(group.getKey());
			Invitation invitation = groupToInvitations.get(group.getBaseGroup());
			items.add(forgeRow(group, membership, invitation));
		}
		tableDataModel.setObjects(items);
		tableEl.reset(true, true, true);
	}
	
	private GroupOverviewRow forgeRow(BusinessGroup group, BusinessGroupMembership membership, Invitation invitation) {
		GroupOverviewRow row = new GroupOverviewRow(group, membership, invitation, Boolean.TRUE);
		
		if(invitation != null) {
			FormLink invitationLink = uifactory.addFormLink("invitation_" + (++counter), "invitation", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			invitationLink.setIconLeftCSS("o_icon o_icon_link o_icon-fw");
			invitationLink.setTitle(translate("invitation.link.long"));
			row.setInvitationLink(invitationLink);
			invitationLink.setUserObject(row);	
		}
		
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == groupsCtrl && event instanceof AddToGroupsEvent){
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
		} else if(invitationUrlCtrl == source) {
			if(urlCalloutCtrl != null) {
				urlCalloutCtrl.deactivate();
			}
			cleanUpPopups();
		} else if (source == cmc || source == urlCalloutCtrl) {
			cleanUpPopups();
		}
	}
	
	private void cleanUpPopups() {
		removeAsListenerAndDispose(invitationUrlCtrl);
		removeAsListenerAndDispose(removeFromGrpDlg);
		removeAsListenerAndDispose(urlCalloutCtrl);
		removeAsListenerAndDispose(groupsCtrl);
		removeAsListenerAndDispose(cmc);
		invitationUrlCtrl = null;
		removeFromGrpDlg = null;
		urlCalloutCtrl = null;
		groupsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addGroups){
			doGroupAddDialog(ureq);
		} else if(source == leaveLink) {
			doConfirmLeave(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if (TABLE_ACTION_LAUNCH.equals(se.getCommand())) {
					GroupOverviewRow item = tableDataModel.getObject(se.getIndex());
					doLaunch(ureq, item);
				} else if (TABLE_ACTION_UNSUBSCRIBE.equals(se.getCommand())) {
					GroupOverviewRow item = tableDataModel.getObject(se.getIndex());
					doConfirmLeave(ureq, item);
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doGroupAddDialog(ureq);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("invitation".equals(link.getCmd()) && link.getUserObject() instanceof GroupOverviewRow) {
				GroupOverviewRow row = (GroupOverviewRow)link.getUserObject();
				doOpenInvitationLink(ureq, link.getFormDispatchId(), row);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doLaunch(UserRequest ureq, GroupOverviewRow row) {
		BusinessGroup currBusinessGroup = businessGroupService.loadBusinessGroup(row.getKey());
		if (currBusinessGroup==null) {
			//group seems to be removed meanwhile, reload table and show error
			showError("group.removed");
			updateModel();	
		} else {
			String businessPath = "[BusinessGroup:" + currBusinessGroup.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
	
	private void doGroupAddDialog(UserRequest ureq) {
		groupsCtrl = new GroupSearchController(ureq, getWindowControl());
		listenTo(groupsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("add.groups"), groupsCtrl.getInitialComponent(), true, translate("add.groups"), true);
		listenTo(cmc);
		cmc.activate();
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
	
	private void doOpenInvitationLink(UserRequest ureq, String elementId, GroupOverviewRow row) {
		String url = invitationService.toUrl(row.getInvitation());
		invitationUrlCtrl = new InvitationURLController(ureq, getWindowControl(), url);
		listenTo(invitationUrlCtrl);

		String title = translate("invitation.link.long");
		urlCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				invitationUrlCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(urlCalloutCtrl);
		urlCalloutCtrl.activate();
	}
	
	private void doConfirmLeave(UserRequest ureq, GroupOverviewRow row) {
		BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(row.getKey());
		if (businessGroup == null) {
			showError("group.removed");
			updateModel();	
		} else {
			doConfirmLeave(ureq, List.of(businessGroup));
		}
	}
	
	private void doConfirmLeave(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<Long> groupKeys = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			GroupOverviewRow row = tableDataModel.getObject(selectedIndex.intValue());
			if(row != null) {
				groupKeys.add(row.getKey());
			}
		}

		List<BusinessGroup> businessGroups = businessGroupService.loadBusinessGroups(groupKeys);
		doConfirmLeave(ureq, businessGroups);
	}
	
	private void doConfirmLeave(UserRequest ureq, List<BusinessGroup> groupsToLeave) {
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
}
