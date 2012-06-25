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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.notifications.NotificationHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.BusinessGroupTableModel;

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
	private static final String TABLE_ACTION_UNSUBSCRIBE = "unsubscribe";
	private VelocityContainer vc;
	private TableController tblCtr;
	private GroupOverviewModel tableDataModel;
	private WindowControl wControl;
	private Identity identity;
	private Link addGroups;
	private CloseableModalWindowWrapperController calloutCtrl;
	private GroupSearchController groupsCtrl;
	private DialogBoxController removeFromGrpDlg;
	private DialogBoxController sendMailDlg;
	private static String TABLE_ACTION_LAUNCH ;

	public GroupOverviewController(UserRequest ureq, WindowControl control, Identity identity, Boolean canStartGroups) {
		super(ureq, control, Util.createPackageTranslator(BusinessGroupTableModel.class, ureq.getLocale()));
		this.wControl = control;
		this.identity = identity;
		if (canStartGroups){
			TABLE_ACTION_LAUNCH = "bgTblLaunch";
		} else {
			TABLE_ACTION_LAUNCH = null;
		}		
		
		vc = createVelocityContainer("groupoverview");
		buildTableController(ureq, control); 
		addGroups = LinkFactory.createButton("add.groups", vc, this);		
		vc.put("table.groups", tblCtr.getInitialComponent());	
		putInitialPanel(vc);
	}

	/**
	 * @param ureq
	 * @param control
	 * @param identity
	 * @return
	 */
	private void buildTableController(UserRequest ureq, WindowControl control) {
		
		removeAsListenerAndDispose(tblCtr);
		tblCtr = new TableController(null, ureq, control, getTranslator());
		listenTo(tblCtr);
		
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.type", 0, null, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.name", 1, TABLE_ACTION_LAUNCH, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.role", 2, null, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.joindate", 3, null, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_UNSUBSCRIBE, "table.user.unsubscribe", translate("table.user.unsubscribe")));

		//build data model
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BaseSecurity sm = BaseSecurityManager.getInstance();
		List<Object[]> userGroups = new ArrayList<Object[]>();
			//loop over all kind of groups with all possible memberships
			List<String> bgTypes = new ArrayList<String>();
			bgTypes.add(BusinessGroup.TYPE_BUDDYGROUP);
			bgTypes.add(BusinessGroup.TYPE_LEARNINGROUP);
			bgTypes.add(BusinessGroup.TYPE_RIGHTGROUP);
			for (String bgType : bgTypes) {				
				List<BusinessGroup> ownedGroups = bgs.findBusinessGroupsOwnedBy(bgType, identity, null);
				List<BusinessGroup> attendedGroups = bgs.findBusinessGroupsAttendedBy(bgType, identity, null);
				List<BusinessGroup> waitingGroups = bgs.findBusinessGroupsWithWaitingListAttendedBy(bgType, identity, null);
				//using HashSet to remove duplicate entries
				HashSet<BusinessGroup> allGroups = new HashSet<BusinessGroup>();
				allGroups.addAll(ownedGroups);
				allGroups.addAll(attendedGroups);
				allGroups.addAll(waitingGroups);
				
				Iterator<BusinessGroup> iter = allGroups.iterator();
				while (iter.hasNext()) {
					Object[] groupEntry = new Object[4];
					BusinessGroup group = iter.next();
					groupEntry[0] = translate(group.getType());
					groupEntry[1] = group;
					Date joinDate = null;
					if(attendedGroups.contains(group)&&ownedGroups.contains(group)) {
						groupEntry[2] = translate("attende.and.owner");
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getPartipiciantGroup(), identity);
					}
					else if(attendedGroups.contains(group)) {
						groupEntry[2] = translate("attende");
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getPartipiciantGroup(), identity);
					}
					else if(ownedGroups.contains(group)) {
						groupEntry[2] = translate("owner");
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getOwnerGroup(), identity);
					}
					else if(waitingGroups.contains(group)) {
						int waitingListPosition = bgm.getPositionInWaitingListFor(identity, group);
						groupEntry[2] = translate("waiting", String.valueOf(waitingListPosition));
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getWaitingGroup(), identity);
					}
					groupEntry[3] = joinDate;
					
					userGroups.add(groupEntry);
					}			
			}
		tableDataModel = new GroupOverviewModel(userGroups, 4);
		tblCtr.setTableDataModel(tableDataModel);
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
	protected void event(	UserRequest ureq, Component source, Event event) {
		if (source == addGroups){
			groupsCtrl = new GroupSearchController(ureq, getWindowControl());
			listenTo(groupsCtrl);
			
			calloutCtrl = new CloseableModalWindowWrapperController(ureq, getWindowControl(), translate("add.groups"), groupsCtrl.getInitialComponent(), "ccgroupadd");
			calloutCtrl.setInitialWindowSize(500, 400);
//			calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, groupsCtrl.getInitialComponent(), addGroups, translate("add.groups"), false, null);
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tblCtr){
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				BusinessGroup currBusinessGroup = tableDataModel.getBusinessGroupAtRow(rowid);
				if (actionid.equals(TABLE_ACTION_LAUNCH)) {
					currBusinessGroup = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(currBusinessGroup);
					if (currBusinessGroup==null) {
						//group seems to be removed meanwhile, reload table and show error
						showError("group.removed");
						updateGroupsTable(ureq);	
					}
					else {
						BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), true, null);
					}
				} else if (actionid.equals(TABLE_ACTION_UNSUBSCRIBE)){
					// fxdiff: FXOLAT-101 see similar doBuddyGroupLeave() in BGMainController
					String groupName = currBusinessGroup.getName();
					BaseSecurity securityManager = BaseSecurityManager.getInstance();
					List<Identity> ownerList = securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getOwnerGroup());
					List<Identity> partList = securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getPartipiciantGroup());
					
					String rmText = translate("unsubscribe.text", new String[]{NotificationHelper.getFormatedName(identity), groupName});
					if ((ownerList.size() == 1 && partList.size() == 0) || (ownerList.size() == 0 && partList.size() == 1)) {
						rmText += " <br/>" + translate("unsubscribe.group.del");
					}
					removeFromGrpDlg = activateYesNoDialog(ureq, translate("unsubscribe.title"), rmText, removeFromGrpDlg);
					removeFromGrpDlg.setUserObject(currBusinessGroup);
				}
			}
		}	else if (source == groupsCtrl && event instanceof AddToGroupsEvent){
			AddToGroupsEvent groupsEv = (AddToGroupsEvent) event;
			if (groupsEv.getOwnerGroupKeys().isEmpty() && groupsEv.getParticipantGroupKeys().isEmpty()){
				// no groups selected
				showWarning("group.add.result.none");
			} else {
				if (calloutCtrl != null) calloutCtrl.deactivate();				
				String[] resultTextArgs = GroupAddManager.getInstance().addIdentityToGroups(groupsEv, identity, getIdentity());
				if (resultTextArgs != null){
					String message = translate("group.add.result", resultTextArgs);
					getWindowControl().setInfo(message);
				} else {		
					showWarning("group.add.result.none");
				}
				updateGroupsTable(ureq);
			}			
		} else if (source == removeFromGrpDlg && DialogBoxUIFactory.isYesEvent(event)){
			//fxdiff: FXOLAT-138 let user decide to send notif-mail or not
			sendMailDlg = activateYesNoDialog(ureq, translate("unsubscribe.title"), translate("send.email.notif"), sendMailDlg);
		} else if (source == sendMailDlg){
			if (DialogBoxUIFactory.isYesEvent(event))
				removeUserFromGroup(ureq, true);
			else
				removeUserFromGroup(ureq, false);
		}
			
	}

	/**
	 * 
	 * @param ureq
	 * @param doSendMail
	 */
	private void removeUserFromGroup(UserRequest ureq, boolean doSendMail) {
		// fxdiff: FXOLAT-101 see similar doBuddyGroupLeave() in BGMainController
		final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup currBusinessGroup = (BusinessGroup) removeFromGrpDlg.getUserObject();
		String groupName = currBusinessGroup.getName();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
		SecurityGroup owners = currBusinessGroup.getOwnerGroup();
		List<Identity> ownerList = securityManager.getIdentitiesOfSecurityGroup(owners);
		List<Identity> partList = securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getPartipiciantGroup());

		if ((ownerList.size() == 1 && partList.size() == 0) || (ownerList.size() == 0 && partList.size() == 1)) {
			// really delete the group as it has no more owners/participants
			if(doSendMail)
				bgm.deleteBusinessGroupWithMail(currBusinessGroup, wControl, ureq, getTranslator(), null);
			else
				bgm.deleteBusinessGroup(currBusinessGroup);
		} else {
			// 1) remove as owner
			if (securityManager.isIdentityInSecurityGroup(identity, owners)) {
				bgm.removeOwnerAndFireEvent(ureq.getIdentity(), identity, currBusinessGroup, flags, false);
			}

			// 2) remove as participant
			final BusinessGroup toRemFromGroup = currBusinessGroup;
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
				public void execute() {
					bgm.removeParticipantAndFireEvent(getIdentity(), identity, toRemFromGroup, flags, false);
				}
			});

			// 3) notify user about this action:
			if(doSendMail){
				MailTemplate mailTemplate = BGMailHelper.createRemoveParticipantMailTemplate(currBusinessGroup, getIdentity());
				MailerWithTemplate mailer = MailerWithTemplate.getInstance();
				MailerResult mailerResult = mailer.sendMail(null, identity, null, null, mailTemplate, null);
				MailHelper.printErrorsAndWarnings(mailerResult, wControl, getLocale());
			}
		}

		updateGroupsTable(ureq);
		showInfo("unsubscribe.successful", groupName);	
	}
	
	/**
	 * @param ureq
	 * update Table
	 */
	private void updateGroupsTable(UserRequest ureq) {
		buildTableController(ureq, wControl); 
		vc.put("table.groups", tblCtr.getInitialComponent());
	}


	
}
