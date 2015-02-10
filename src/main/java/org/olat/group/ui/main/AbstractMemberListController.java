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
package org.olat.group.ui.main;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SearchIdentityParams;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.member.MemberListController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.MemberListTableModel.Cols;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.ResourceReservation;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractMemberListController extends BasicController implements Activateable2 {

	protected static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();
	
	public static final String TABLE_ACTION_EDIT = "tbl_edit";
	public static final String TABLE_ACTION_MAIL = "tbl_mail";
	public static final String TABLE_ACTION_REMOVE = "tbl_remove";
	public static final String TABLE_ACTION_GRADUATE = "tbl_graduate";
	public static final String TABLE_ACTION_IM = "tbl_im";
	
	protected final TableController memberListCtr;
	protected final VelocityContainer mainVC;
	
	protected CloseableModalController cmc;
	private EditMembershipController editMembersCtrl;
	private EditSingleMembershipController editSingleMemberCtrl;
	private ContactFormController contactCtrl;
	private MemberLeaveConfirmationController leaveDialogBox;
	private DialogBoxController confirmSendMailBox;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private final RepositoryEntry repoEntry;
	private final BusinessGroup businessGroup;
	private final boolean isLastVisitVisible;
	private final boolean isAdministrativeUser;
	private final boolean chatEnabled;
	private final boolean globallyManaged;
	
	private final UserManager userManager;
	
	private final BaseSecurity securityManager;
	private final BaseSecurityModule securityModule;
	private final RepositoryService repositoryService;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	private final BusinessGroupModule groupModule;
	private final ACService acService;
	private final InstantMessagingModule imModule;
	private final InstantMessagingService imService;
	private final UserSessionManager sessionManager;
	
	private final GroupMemberViewComparator memberViewComparator;
	private static final CourseMembershipComparator MEMBERSHIP_COMPARATOR = new CourseMembershipComparator();
	
	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry,
			String page) {
		this(ureq, wControl, repoEntry, null, page, Util.createPackageTranslator(AbstractMemberListController.class, ureq.getLocale()));
	}
	
	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, BusinessGroup group,
			String page) {
		this(ureq, wControl, null, group, page, Util.createPackageTranslator(AbstractMemberListController.class, ureq.getLocale()));
	}
	
	protected AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, BusinessGroup group,
			String page, Translator translator) {
		super(ureq, wControl, Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), translator));
		
		this.businessGroup = group;
		this.repoEntry = repoEntry;
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		acService = CoreSpringFactory.getImpl(ACService.class);
		imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		memberViewComparator = new GroupMemberViewComparator(Collator.getInstance(getLocale()));

		globallyManaged = calcGloballyManaged();
		
		Roles roles = ureq.getUserSession().getRoles();
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		isLastVisitVisible = securityModule.isUserLastVisitVisible(roles);
		mainVC = createVelocityContainer(page);

		//table
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, this.getClass().getSimpleName());
		tableConfig.setTableEmptyMessage(translate("nomembers"));
		
		memberListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator(), true);
		listenTo(memberListCtr);

		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		initColumns();
		MemberListTableModel memberListModel = new MemberListTableModel(userPropertyHandlers);
		memberListCtr.setTableDataModel(memberListModel);
		memberListCtr.setMultiSelect(true);
		if(!globallyManaged) {
			memberListCtr.addMultiSelectAction("edit.members", TABLE_ACTION_EDIT);
		}
		memberListCtr.addMultiSelectAction("table.header.mail", TABLE_ACTION_MAIL);
		if(!globallyManaged) {
			memberListCtr.addMultiSelectAction("table.header.remove", TABLE_ACTION_REMOVE);
		}

		mainVC.put("memberList", memberListCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private boolean calcGloballyManaged() {
		boolean managed = true;
		if(businessGroup != null) {
			managed &= BusinessGroupManagedFlag.isManaged(businessGroup, BusinessGroupManagedFlag.membersmanagement);
		}
		if(repoEntry != null) {
			boolean managedEntry = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement);
			managed &= managedEntry;
			
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, repoEntry, 0, -1);
			for(BusinessGroup group:groups) {
				managed &= BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.membersmanagement);
			}
		}
		return managed;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	protected void initColumns() {
		int offset = Cols.values().length;
		if(chatEnabled) {
			memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.online.i18n(), Cols.online.ordinal(), TABLE_ACTION_IM, getLocale(),
					ColumnDescriptor.ALIGNMENT_LEFT, new OnlineIconRenderer()));
		}
		if(isAdministrativeUser) {
			memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.username.i18n(), Cols.username.ordinal(), null, getLocale()));
		}
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			memberListCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i++ + offset, null, getLocale()));
		}
		
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), null, getLocale()));
		if(isLastVisitVisible) {
			memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), null, getLocale()));
		}
		
		CustomCellRenderer roleRenderer = new CourseRoleCellRenderer(getLocale());
		memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer) {
			@Override
			public int compareTo(final int rowa, final int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
				if(a instanceof CourseMembership && b instanceof CourseMembership) {
					return MEMBERSHIP_COMPARATOR.compare((CourseMembership)a, (CourseMembership)b);
				}
				return super.compareTo(rowa, rowb);
			}
		});
		if(repoEntry != null) {
			CustomCellRenderer groupRenderer = new GroupCellRenderer();
			memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.groups.i18n(), Cols.groups.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, groupRenderer) {
				@Override
				public int compareTo(final int rowa, final int rowb) {
					Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
					Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
					if(a instanceof MemberView && b instanceof MemberView) {
						return memberViewComparator.compare((MemberView)a, (MemberView)b);
					}
					return super.compareTo(rowa, rowb);
				}
			});
		}
		
		memberListCtr.addColumnDescriptor(new GraduateColumnDescriptor("table.header.graduate", TABLE_ACTION_GRADUATE, getTranslator()));
		memberListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_EDIT, "table.header.edit", translate("table.header.edit")));
		if(!globallyManaged) {
			memberListCtr.addColumnDescriptor(new LeaveColumnDescriptor("table.header.remove", TABLE_ACTION_REMOVE, getTranslator()));
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
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

				MemberView member = (MemberView)memberListCtr.getTableDataModel().getObject(te.getRowId());
				if(TABLE_ACTION_EDIT.equals(actionid)) {
					openEdit(ureq, member);
				} else if(TABLE_ACTION_REMOVE.equals(actionid)) {
					confirmDelete(ureq, Collections.singletonList(member));
				} else if(TABLE_ACTION_GRADUATE.equals(actionid)) {
					doGraduate(Collections.singletonList(member));
				} else if(TABLE_ACTION_IM.equals(actionid)) {
					doIm(ureq, member);
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent te = (TableMultiSelectEvent)event;
				@SuppressWarnings("unchecked")
				List<MemberView> selectedItems = memberListCtr.getObjects(te.getSelection());
				if(TABLE_ACTION_REMOVE.equals(te.getAction())) {
					confirmDelete(ureq, selectedItems);
				} else if(TABLE_ACTION_EDIT.equals(te.getAction())) {
					openEdit(ureq, selectedItems);
				} else if(TABLE_ACTION_MAIL.equals(te.getAction())) {
					doSendMail(ureq, selectedItems);
				} else if(TABLE_ACTION_GRADUATE.equals(te.getAction())) {
					doGraduate(selectedItems);
				}
			}
		} else if (source == leaveDialogBox) {
			if (Event.DONE_EVENT == event) {
				List<Identity> members = leaveDialogBox.getIdentities();
				doLeave(members, leaveDialogBox.isSendMail());
				reloadModel();
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if(source == editMembersCtrl) {
			cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doConfirmChangePermission(ureq, e, editMembersCtrl.getMembers());
			}
		} else if(source == editSingleMemberCtrl) {
			cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doConfirmChangePermission(ureq, e, null);
			}
		} else if(confirmSendMailBox == source) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			MailConfirmation confirmation = (MailConfirmation)confirmSendMailBox.getUserObject();
			MemberPermissionChangeEvent e =confirmation.getE();
			if(e.getMember() != null) {
				doChangePermission(ureq, e, sendMail);
			} else {
				doChangePermission(ureq, e, confirmation.getMembers(), sendMail);
			}
		} else if (source == contactCtrl) {
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
		removeAsListenerAndDispose(editMembersCtrl);
		removeAsListenerAndDispose(editSingleMemberCtrl);
		removeAsListenerAndDispose(leaveDialogBox);
		removeAsListenerAndDispose(contactCtrl);
		cmc = null;
		contactCtrl = null;
		leaveDialogBox = null;
		editMembersCtrl = null;
		editSingleMemberCtrl = null;
	}
	
	protected void confirmDelete(UserRequest ureq, List<MemberView> members) {
		int numOfOwners =
				repoEntry == null ? businessGroupService.countMembers(businessGroup, GroupRoles.coach.name())
				: repositoryService.countMembers(repoEntry, GroupRoles.owner.name());
		
		int numOfRemovedOwner = 0;
		List<Long> identityKeys = new ArrayList<Long>();
		for(MemberView member:members) {
			identityKeys.add(member.getIdentityKey());
			if(member.getMembership().isOwner()) {
				numOfRemovedOwner++;
			}
		}
		if(numOfRemovedOwner == 0 || numOfOwners - numOfRemovedOwner > 0) {
			List<Identity> ids = securityManager.loadIdentityByKeys(identityKeys);
			leaveDialogBox = new MemberLeaveConfirmationController(ureq, getWindowControl(), ids);
			listenTo(leaveDialogBox);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), leaveDialogBox.getInitialComponent(),
					true, translate("edit.member"));
			cmc.activate();
			listenTo(cmc);
		} else {
			showWarning("error.atleastone");
		}
	}
	
	protected void openEdit(UserRequest ureq, MemberView member) {
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		editSingleMemberCtrl = new EditSingleMembershipController(ureq, getWindowControl(), identity, repoEntry, businessGroup);
		listenTo(editSingleMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editSingleMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void openEdit(UserRequest ureq, List<MemberView> members) {
		List<Long> identityKeys = getMemberKeys(members);
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		editMembersCtrl = new EditMembershipController(ureq, getWindowControl(), identities, repoEntry, businessGroup);
		listenTo(editMembersCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMembersCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	/**
	 * Open private chat
	 * @param ureq
	 * @param member
	 */
	protected void doIm(UserRequest ureq, MemberView member) {
		Buddy buddy = imService.getBuddyById(member.getIdentityKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(ureq, buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	protected void doConfirmChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, List<Identity> members) {
		boolean groupChangesEmpty = e.getGroupChanges() == null || e.getGroupChanges().isEmpty();
		boolean repoChangesEmpty = e.getRepoOwner() == null && e.getRepoParticipant() == null && e.getRepoTutor() == null;
		if(groupChangesEmpty && repoChangesEmpty) {
			//nothing to do
			return;
		}

		boolean mailMandatory = groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mailMandatory) {
			if(members == null) {
				doChangePermission(ureq, e, true);
			} else {
				doChangePermission(ureq, e, members, true);
			}
		} else {
			confirmSendMailBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailBox);
			confirmSendMailBox.setUserObject(new MailConfirmation(e, members));
		}
	}
	
	protected void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			List<RepositoryEntryPermissionChangeEvent> changes = Collections.singletonList((RepositoryEntryPermissionChangeEvent)e);
			repositoryManager.updateRepositoryEntryMemberships(getIdentity(), ureq.getUserSession().getRoles(), repoEntry, changes, mailing);
		}

		businessGroupService.updateMemberships(getIdentity(), e.getGroupChanges(), mailing);
		//make sure all is committed before loading the model again (I see issues without)
		DBFactory.getInstance().commitAndCloseSession();
		reloadModel();
	}
	
	protected void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent changes, List<Identity> members, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.generateRepositoryChanges(members);
			repositoryManager.updateRepositoryEntryMemberships(getIdentity(), ureq.getUserSession().getRoles(), repoEntry, repoChanges, mailing);
		}

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.generateBusinessGroupMembershipChange(members);
		businessGroupService.updateMemberships(getIdentity(), allModifications, mailing);

		reloadModel();
	}
	
	protected void doLeave(List<Identity> members, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			repositoryManager.removeMembers(getIdentity(), members, repoEntry, mailing);
			businessGroupService.removeMembers(getIdentity(), members, repoEntry.getOlatResource(), mailing);
		} else {
			businessGroupService.removeMembers(getIdentity(), members, businessGroup.getResource(), mailing);
		}
		reloadModel();
	}
	
	protected void doSendMail(UserRequest ureq, List<MemberView> members) {
		List<Long> identityKeys = getMemberKeys(members);
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		if(identities.isEmpty()) {
			showWarning("error.msg.send.no.rcps");
			return;
		}
		
		ContactMessage contactMessage = new ContactMessage(getIdentity());
		String name = repoEntry != null ? repoEntry.getDisplayname() : businessGroup.getName();
		ContactList contactList = new ContactList(name);
		contactList.addAllIdentites(identities);
		contactMessage.addEmailTo(contactList);
		
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		listenTo(contactCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent(),
				true, translate("mail.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void doGraduate(List<MemberView> members) {
		if(businessGroup != null) {
			List<Long> identityKeys = getMemberKeys(members);
			List<Identity> identitiesToGraduate = securityManager.loadIdentityByKeys(identityKeys);
			businessGroupService.moveIdentityFromWaitingListToParticipant(getIdentity(), identitiesToGraduate,
					businessGroup, null);
		} else {
			Map<Long, BusinessGroup> groupsMap = new HashMap<>();
			Map<BusinessGroup, List<Identity>> graduatesMap = new HashMap<>();
			for(MemberView member:members) {
				List<BusinessGroupShort> groups = member.getGroups();
				if(groups != null && groups.size() > 0) {
					Identity memberIdentity = securityManager.loadIdentityByKey(member.getIdentityKey());
					for(BusinessGroupShort group:groups) {
						if(businessGroupService.hasRoles(memberIdentity, group, GroupRoles.waiting.name())) {
							BusinessGroup fullGroup = groupsMap.get(group.getKey());
							if(fullGroup == null) {
								fullGroup = businessGroupService.loadBusinessGroup(group.getKey());
								groupsMap.put(group.getKey(), fullGroup);
							}
							
							List<Identity> identitiesToGraduate = graduatesMap.get(fullGroup);
							if(identitiesToGraduate == null) {
								 identitiesToGraduate = new ArrayList<>();
								 graduatesMap.put(fullGroup, identitiesToGraduate);
							}
							identitiesToGraduate.add(memberIdentity);
						}
					}
				}
			}
			
			for(Map.Entry<BusinessGroup, List<Identity>> entry:graduatesMap.entrySet()) {
				BusinessGroup fullGroup = entry.getKey();
				List<Identity> identitiesToGraduate = entry.getValue();
				businessGroupService.moveIdentityFromWaitingListToParticipant(getIdentity(), identitiesToGraduate,
						fullGroup, null);
			}
		}
		reloadModel();
	}
	
	protected List<Long> getMemberKeys(List<MemberView> members) {
		List<Long> keys = new ArrayList<Long>(members.size());
		if(members != null && !members.isEmpty()) {
			for(MemberView member:members) {
				keys.add(member.getIdentityKey());
			}
		}
		return keys;
	}

	protected abstract SearchMembersParams getSearchParams();
	
	public void reloadModel() {
		updateTableModel(getSearchParams());
	}

	protected List<MemberView> updateTableModel(SearchMembersParams params) {
		//course membership
		boolean managedMembersRepo = 
				RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement);
		
		List<RepositoryEntryMembership> repoMemberships =
				repoEntry == null ? Collections.<RepositoryEntryMembership>emptyList()
				: repositoryManager.getRepositoryEntryMembership(repoEntry);

		//groups membership
		List<BusinessGroup> groups = 
				repoEntry == null ? Collections.singletonList(businessGroup)
				: businessGroupService.findBusinessGroups(null, repoEntry, 0, -1);
				
		List<Long> groupKeys = new ArrayList<Long>();
		Map<Long,BusinessGroupShort> keyToGroupMap = new HashMap<Long,BusinessGroupShort>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
			keyToGroupMap.put(group.getKey(), group);
		}

		List<BusinessGroupMembership> memberships = groups.isEmpty() ? Collections.<BusinessGroupMembership>emptyList() :
			businessGroupService.getBusinessGroupsMembership(groups);

		//get identities
		Set<Long> identityKeys = new HashSet<Long>();
		for(RepositoryEntryMembership membership: repoMemberships) {
			identityKeys.add(membership.getIdentityKey());
		}
		for(BusinessGroupMembership membership:memberships) {
			identityKeys.add(membership.getIdentityKey());
		}
		
		List<Identity> identities;
		if(identityKeys.isEmpty()) {
			identities = new ArrayList<Identity>(0);
		} else {
			SearchIdentityParams idParams = new SearchIdentityParams();
			idParams.setIdentityKeys(identityKeys);
			if(params.getUserPropertiesSearch() != null && !params.getUserPropertiesSearch().isEmpty()) {
				idParams.setUserProperties(params.getUserPropertiesSearch());
			}
			if(StringHelper.containsNonWhitespace(params.getLogin())) {
				idParams.setLogin(params.getLogin());
			}
			identities = securityManager.getIdentitiesByPowerSearch(idParams, 0, -1);
		}

		Map<Long,MemberView> keyToMemberMap = new HashMap<Long,MemberView>();
		List<MemberView> memberList = new ArrayList<MemberView>();
		Locale locale = getLocale();

		//reservations
		if(params.isPending()) {
			List<OLATResource> resourcesForReservations = new ArrayList<OLATResource>();
			if(repoEntry != null) {
				resourcesForReservations.add(repoEntry.getOlatResource());
			}
			for(BusinessGroup group:groups) {
				resourcesForReservations.add(group.getResource());
			}
			List<ResourceReservation> reservations = acService.getReservations(resourcesForReservations);
			for(ResourceReservation reservation:reservations) {
				Identity identity = reservation.getIdentity();
				MemberView member = new MemberView(identity, userPropertyHandlers, locale);
				member.getMembership().setPending(true);
				memberList.add(member);
				keyToMemberMap.put(identity.getKey(), member);
			}
		}
		
		
		Long me = getIdentity().getKey();
		Set<Long> loadStatus = new HashSet<Long>();
		for(Identity identity:identities) {
			MemberView member = new MemberView(identity, userPropertyHandlers, locale);
			if(chatEnabled) {
				if(identity.getKey().equals(me)) {
					member.setOnlineStatus("me");
				} else if(sessionManager.isOnline(identity.getKey())) {
					loadStatus.add(identity.getKey());
				} else {
					member.setOnlineStatus(Presence.unavailable.name());
				}
			}
			memberList.add(member);
			keyToMemberMap.put(identity.getKey(), member);
		}
		
		if(loadStatus.size() > 0) {
			List<Long> statusToLoadList = new ArrayList<Long>(loadStatus);
			Map<Long,String> statusMap = imService.getBuddyStatus(statusToLoadList);
			for(Long toLoad:statusToLoadList) {
				String status = statusMap.get(toLoad);
				MemberView member = keyToMemberMap.get(toLoad);
				if(status == null) {
					member.setOnlineStatus(Presence.available.name());	
				} else {
					member.setOnlineStatus(status);	
				}
			}
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
				memberView.getMembership().setManagedMembersRepo(managedMembersRepo);
				if(membership.isOwner()) {
					memberView.getMembership().setRepoOwner(true);
				}
				if(membership.isCoach()) {
					memberView.getMembership().setRepoTutor(true);
				}
				if(membership.isParticipant()) {
					memberView.getMembership().setRepoParticipant(true);
				}
			}
		}
		
		//the order of the filter is important
		filterByRoles(memberList, params);
		filterByOrigin(memberList, params);
		((MemberListTableModel)memberListCtr.getTableDataModel()).setObjects(memberList);
		memberListCtr.modelChanged();
		return memberList;
	}
	
	private void filterByOrigin(List<MemberView> memberList, SearchMembersParams params) {
		if(params.isGroupOrigin() && params.isRepoOrigin()) {
			//do ntohing not very useful :-)
		} else if(params.isGroupOrigin()) {
			for(Iterator<MemberView> it=memberList.iterator(); it.hasNext(); ) {
				CourseMembership m = it.next().getMembership();
				if(!m.isGroupTutor() && !m.isGroupParticipant() && !m.isGroupWaiting()) {
					it.remove();
				}
			}
		} else if(params.isRepoOrigin()) {
			for(Iterator<MemberView> it=memberList.iterator(); it.hasNext(); ) {
				CourseMembership m = it.next().getMembership();
				if(!m.isRepoOwner() && !m.isRepoTutor() && !m.isRepoParticipant()) {
					it.remove();
				}
			}
		}
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
		
		if(params.isPending()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMembership().isPending()) {
					it.remove();
				}
			}
		}
		
		memberList.removeAll(members);
	}
	
	private class MailConfirmation {
		private final List<Identity> members;
		private final MemberPermissionChangeEvent e;
		
		public MailConfirmation(MemberPermissionChangeEvent e, List<Identity> members) {
			this.e = e;
			this.members = members;
		}

		public List<Identity> getMembers() {
			return members;
		}

		public MemberPermissionChangeEvent getE() {
			return e;
		}
	}
}
