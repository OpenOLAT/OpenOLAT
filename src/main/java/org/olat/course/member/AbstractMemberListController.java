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
package org.olat.course.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.course.member.MemberListTableModel.Cols;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.user.UserManager;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractMemberListController extends BasicController {
	
	public static final String TABLE_ACTION_EDIT = "tbl_edit";
	public static final String TABLE_ACTION_MAIL = "tbl_mail";
	public static final String TABLE_ACTION_REMOVE = "tbl_remove";
	
	protected final MemberListTableModel memberListModel;
	protected final TableController memberListCtr;
	protected final VelocityContainer mainVC;
	
	private DialogBoxController leaveDialogBox;
	protected CloseableModalController cmc;
	private EditMembershipController editMemberCtrl;

	private final UserManager userManager;
	private final RepositoryEntry repoEntry;
	private final BaseSecurity securityManager;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	
	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, String page) {
		super(ureq, wControl);
		
		this.repoEntry = repoEntry;
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		mainVC = createVelocityContainer(page);

		//table
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, this.getClass().getSimpleName());
		tableConfig.setTableEmptyMessage(translate("nomembers"));			
		memberListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator(), false);
		listenTo(memberListCtr);

		int numOfColumns = initColumns();
		memberListModel = new MemberListTableModel(numOfColumns);
		memberListCtr.setTableDataModel(memberListModel);
		memberListCtr.setMultiSelect(true);
		memberListCtr.addMultiSelectAction("table.header.edit", TABLE_ACTION_EDIT);
		memberListCtr.addMultiSelectAction("table.header.mail", TABLE_ACTION_MAIL);
		memberListCtr.addMultiSelectAction("table.header.remove", TABLE_ACTION_REMOVE);

		mainVC.put("memberList", memberListCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	protected int initColumns() {
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastName.i18n(), Cols.lastName.ordinal(), null, getLocale()));
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.firstName.i18n(), Cols.firstName.ordinal(), null, getLocale()));
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), null, getLocale()));
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), null, getLocale()));
		CustomCellRenderer roleRenderer = new CourseRoleCellRenderer(getLocale());
		memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer));
		CustomCellRenderer groupRenderer = new GroupCellRenderer();
		memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.groups.i18n(), Cols.groups.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, groupRenderer));
		memberListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_EDIT, "action", translate("table.header.edit")));
		memberListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_REMOVE, "action", translate("table.header.remove")));
		return 8;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == memberListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();

				MemberView member = memberListModel.getObject(te.getRowId());
				if(actionid.equals(TABLE_ACTION_EDIT)) {
					openEdit(ureq, member);
				} else if(actionid.equals(TABLE_ACTION_REMOVE)) {
					confirmDelete(ureq, Collections.singletonList(member));
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent te = (TableMultiSelectEvent)event;
				List<MemberView> selectedItems = memberListModel.getObjects(te.getSelection());
				if(TABLE_ACTION_REMOVE.equals(te.getAction())) {
					confirmDelete(ureq, selectedItems);
				} else if(TABLE_ACTION_EDIT.equals(te.getAction())) {
					//TODO
				}if(TABLE_ACTION_MAIL.equals(te.getAction())) {
					//TODO
				}
			}
		} else if (source == leaveDialogBox) {
			if (event != Event.CANCELLED_EVENT && DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<Identity> members = (List<Identity>)leaveDialogBox.getUserObject();
				doLeave(ureq, members);
				reloadModel();
			}
		} else if(source == editMemberCtrl) {
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doChangePermission(e);
			}
			
			//do something
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == cmc) {
			cleanUpPopups();
		}
	}
	
	/**
	 * Aggressive clean up all popup controllers
	 */
	protected void cleanUpPopups() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editMemberCtrl);
		removeAsListenerAndDispose(leaveDialogBox);
		cmc = null;
		leaveDialogBox = null;
		editMemberCtrl = null;
	}
	
	protected void confirmDelete(UserRequest ureq, List<MemberView> members) {
		List<Long> identityKeys = new ArrayList<Long>();
		for(MemberView member:members) {
			identityKeys.add(member.getIdentityKey());
		}
		List<Identity> ids = securityManager.loadIdentityByKeys(identityKeys);
		StringBuilder sb = new StringBuilder();
		for(Identity id:ids) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(userManager.getUserDisplayName(id.getUser()));
		}
		leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", sb.toString()), leaveDialogBox);
		leaveDialogBox.setUserObject(ids);
	}
	
	protected void openEdit(UserRequest ureq, MemberView member) {
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		editMemberCtrl = new EditMembershipController(ureq, getWindowControl(), identity, repoEntry);
		listenTo(editMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void doChangePermission(MemberPermissionChangeEvent e) {
		doChangeRepoPermission(e);
		doChangeGroupPermissions(e.getGroupChanges());
		
		//make sure all is committed before loading the model again (I see issues without)
		DBFactory.getInstance().commitAndCloseSession();
		reloadModel();
	}
	
	private void doChangeRepoPermission(RepositoryEntryPermissionChangeEvent e) {
		//first change repository permission
		List<RepositoryEntryPermissionChangeEvent> changes = Collections.singletonList(e);
		repositoryManager.updateRepositoryEntryMembership(getIdentity(), repoEntry, changes);
	}
	
	private void doChangeGroupPermissions(List<BusinessGroupMembershipChange> changes) {
		businessGroupService.updateMemberships(getIdentity(), changes);
	}
	
	protected void doLeave(UserRequest ureq, List<Identity> members) {
		repositoryManager.removeMembers(members, repoEntry);
		businessGroupService.removeMembers(getIdentity(), members, repoEntry.getOlatResource());
		reloadModel();
	}

	protected abstract SearchMembersParams getSearchParams();
	
	protected void reloadModel() {
		updateTableModel(getSearchParams());
	}

	protected List<MemberView> updateTableModel(SearchMembersParams params) {
		//course membership
		List<RepositoryEntryMembership> repoMemberships =
				repositoryManager.getRepositoryEntryMembership(repoEntry);

		//groups membership
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, repoEntry.getOlatResource(), 0, -1);
		List<Long> groupKeys = new ArrayList<Long>();
		Map<Long,BusinessGroupShort> keyToGroupMap = new HashMap<Long,BusinessGroupShort>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
			keyToGroupMap.put(group.getKey(), group);
		}
		List<BusinessGroupMembership> memberships = groups.isEmpty() ? Collections.<BusinessGroupMembership>emptyList() :
				businessGroupService.getBusinessGroupMembership(groupKeys);
		
		//get identities
		Set<Long> identityKeys = new HashSet<Long>();
		for(RepositoryEntryMembership membership: repoMemberships) {
			identityKeys.add(membership.getIdentityKey());
		}
		for(BusinessGroupMembership membership:memberships) {
			identityKeys.add(membership.getIdentityKey());
		}
		List<IdentityShort> identities = securityManager.findShortIdentitiesByKey(identityKeys);
		Map<Long,MemberView> keyToMemberMap = new HashMap<Long,MemberView>();
		List<MemberView> memberList = new ArrayList<MemberView>();
		for(IdentityShort identity:identities) {
			MemberView member = new MemberView(identity.getKey());
			member.setFirstName(identity.getFirstName());
			member.setLastName(identity.getLastName());
			memberList.add(member);
			keyToMemberMap.put(identity.getKey(), member);
		}
		
		for(BusinessGroupMembership membership:memberships) {
			Long identityKey = membership.getIdentityKey();
			MemberView memberView = keyToMemberMap.get(identityKey);
			if(memberView != null) {
				memberView.setFirstTime(membership.getCreationDate());
				memberView.setLastTime(membership.getLastModified());
				if(membership.isOwner()) {
					memberView.getMembership().setGroupTutor(true);
				}
				if(membership.isParticipant()) {
					memberView.getMembership().setGroupParticipant(true);
				}
				if(membership.isWaiting()) {
					memberView.getMembership().setGroupWaiting(true);
				}
				
				Long groupKey = membership.getGroupKey();
				BusinessGroupShort group = keyToGroupMap.get(groupKey);
				memberView.addGroup(group);
			}
		}
		
		for(RepositoryEntryMembership membership:repoMemberships) {
			Long identityKey = membership.getIdentityKey();
			MemberView memberView = keyToMemberMap.get(identityKey);
			if(memberView != null) {
				memberView.setFirstTime(membership.getCreationDate());
				memberView.setLastTime(membership.getLastModified());
				if(membership.getOwnerRepoKey() != null) {
					memberView.getMembership().setRepoOwner(true);
				}
				if(membership.getTutorRepoKey() != null) {
					memberView.getMembership().setRepoTutor(true);
				}
				if(membership.getParticipantRepoKey() != null) {
					memberView.getMembership().setRepoParticipant(true);
				}
			}
		}
		
		filterByRoles(memberList, params);
		memberListModel.setObjects(memberList);
		memberListCtr.modelChanged();
		return memberList;
	}
	
	/**
	 * This filter method preserve the multiple roles of a member. If we want only the waiting list but
	 * a member is in the waiting list and owner of the course, we want it to know.
	 * @param memberList
	 * @param params
	 * @return
	 */
	private void filterByRoles(List<MemberView> memberList, SearchMembersParams params) {
		List<MemberView> members = new ArrayList<MemberView>(memberList);
		if(params.isRepoOwners()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isRepoOwner()) {
					it.remove();
				}
			}
		}
		
		if(params.isRepoTutors()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isRepoTutor()) {
					it.remove();
				}
			}
		}
		
		if(params.isRepoParticipants()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isRepoParticipant()) {
					it.remove();
				}
			}
		}
		
		if(params.isGroupTutors()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isGroupTutor()) {
					it.remove();
				}
			}
		}
		
		if(params.isGroupParticipants()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isGroupParticipant()) {
					it.remove();
				}
			}
		}
		
		if(params.isGroupWaitingList()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isGroupWaiting()) {
					it.remove();
				}
			}
		}
		
		memberList.removeAll(members);
	}
}
