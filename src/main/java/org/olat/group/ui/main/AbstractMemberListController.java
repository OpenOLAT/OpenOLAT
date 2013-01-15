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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.course.member.MemberListController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.MemberListTableModel.Cols;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	
	protected final TableController memberListCtr;
	protected final VelocityContainer mainVC;
	
	protected CloseableModalController cmc;
	private EditMembershipController editMemberCtrl;
	private ContactFormController contactCtrl;
	private MemberLeaveConfirmationController leaveDialogBox;
	private DialogBoxController confirmSendMailBox;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private final RepositoryEntry repoEntry;
	private final BusinessGroup businessGroup;
	private final boolean isAdministrativeUser;
	
	private final UserManager userManager;
	
	private final BaseSecurity securityManager;
	private final BaseSecurityModule securityModule;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	private final BusinessGroupModule groupModule;
	private final ACService acService;
	
	private static final CourseMembershipComparator MEMBERSHIP_COMPARATOR = new CourseMembershipComparator();
	
	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, String page) {
		this(ureq, wControl, repoEntry, null, page);
	}
	
	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, BusinessGroup group, String page) {
		this(ureq, wControl, null, group, page);
	}
	
	private AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, BusinessGroup group, String page) {
		super(ureq, wControl, Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale()));
		
		this.businessGroup = group;
		this.repoEntry = repoEntry;
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		acService = CoreSpringFactory.getImpl(ACService.class);

		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
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
	
	protected void initColumns() {
		if(isAdministrativeUser) {
			memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.username.i18n(), Cols.username.ordinal(), null, getLocale()));
		}
		
		int offset = Cols.values().length;
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			memberListCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i++ + offset, null, getLocale()));
		}
		
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), null, getLocale()));
		memberListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), null, getLocale()));
		CustomCellRenderer roleRenderer = new CourseRoleCellRenderer(getLocale());
		memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer) {
			@Override
			public int compareTo(final int rowa, final int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
				if(a instanceof CourseMembership && b instanceof CourseMembership) {
					return MEMBERSHIP_COMPARATOR.compare((CourseMembership)a, (CourseMembership)b);
				} else {
					return super.compareTo(rowa, rowb);
				}
			}
		});
		if(repoEntry != null) {
			CustomCellRenderer groupRenderer = new GroupCellRenderer();
			memberListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.groups.i18n(), Cols.groups.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, groupRenderer));
		}
		
		memberListCtr.addColumnDescriptor(new GraduateColumnDescriptor("table.header.graduate", TABLE_ACTION_GRADUATE, getTranslator()));
		memberListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_EDIT, "table.header.edit", translate("table.header.edit")));
		memberListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_REMOVE, "table.header.remove", translate("table.header.remove")));
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
					doGraduate(ureq, Collections.singletonList(member));
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
					doGraduate(ureq, selectedItems);
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
		} else if(source == editMemberCtrl) {
			cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				if(e.getMember() != null) {
					doConfirmChangePermission(ureq, e, null);
				} else {
					doConfirmChangePermission(ureq, e, editMemberCtrl.getMembers());
				}
			}
		} else if(confirmSendMailBox == source) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			MailConfirmation confirmation = (MailConfirmation)confirmSendMailBox.getUserObject();
			MemberPermissionChangeEvent e =confirmation.getE();
			if(e.getMember() != null) {
				doChangePermission(ureq, e, null, sendMail);
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
		removeAsListenerAndDispose(editMemberCtrl);
		removeAsListenerAndDispose(leaveDialogBox);
		removeAsListenerAndDispose(contactCtrl);
		cmc = null;
		contactCtrl = null;
		leaveDialogBox = null;
		editMemberCtrl = null;
	}
	
	protected void confirmDelete(UserRequest ureq, List<MemberView> members) {
		int numOfOwners =
				repoEntry == null ? securityManager.countIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup())
				: securityManager.countIdentitiesOfSecurityGroup(repoEntry.getOwnerGroup());
		
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
		editMemberCtrl = new EditMembershipController(ureq, getWindowControl(), identity, repoEntry, businessGroup);
		listenTo(editMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void openEdit(UserRequest ureq, List<MemberView> members) {
		List<Long> identityKeys = getMemberKeys(members);
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		editMemberCtrl = new EditMembershipController(ureq, getWindowControl(), identities, repoEntry, businessGroup);
		listenTo(editMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
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
		if(repoEntry != null) {
			List<RepositoryEntryPermissionChangeEvent> changes = Collections.singletonList((RepositoryEntryPermissionChangeEvent)e);
			repositoryManager.updateRepositoryEntryMembership(getIdentity(), ureq.getUserSession().getRoles(), repoEntry, changes, null);
		}

		businessGroupService.updateMemberships(getIdentity(), e.getGroupChanges(), null);
		//make sure all is committed before loading the model again (I see issues without)
		DBFactory.getInstance().commitAndCloseSession();
		
		if(e.getGroupChanges() != null && !e.getGroupChanges().isEmpty()) {
			for (BusinessGroupMembershipChange mod : e.getGroupChanges()) {
				sendMailAfterChangePermission(mod);
			}
		}

		reloadModel();
	}
	
	protected void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent changes, List<Identity> members, boolean sendMail) {
		if(repoEntry != null) {
			List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.generateRepositoryChanges(members);
			repositoryManager.updateRepositoryEntryMembership(getIdentity(), ureq.getUserSession().getRoles(), repoEntry, repoChanges, null);
		}

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.generateBusinessGroupMembershipChange(members);
		businessGroupService.updateMemberships(getIdentity(), allModifications, null);
		DBFactory.getInstance().commitAndCloseSession();
		
		if(allModifications != null && !allModifications.isEmpty()) {
			for (BusinessGroupMembershipChange mod : allModifications) {
				sendMailAfterChangePermission(mod);
			}
		}

		//make sure all is committed before loading the model again (I see issues without)
		DBFactory.getInstance().commitAndCloseSession();
		reloadModel();
	}
	
	protected void sendMailAfterChangePermission(BusinessGroupMembershipChange mod) {
		MailTemplate template = null;
		if(mod.getParticipant() != null) {
			if(mod.getParticipant().booleanValue()) {
				 template = BGMailHelper.createAddParticipantMailTemplate(mod.getGroup(), mod.getMember());
			} else {
				 template = BGMailHelper.createRemoveParticipantMailTemplate(mod.getGroup(), mod.getMember());
			}
		}
		
		if(mod.getWaitingList() != null) {
			if(mod.getWaitingList().booleanValue()) {
				template = BGMailHelper.createAddWaitinglistMailTemplate(mod.getGroup(), mod.getMember());
			} else {
				template = BGMailHelper.createRemoveWaitinglistMailTemplate(mod.getGroup(), mod.getMember());
			}
		}
		
		if(template != null) {	
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			MailContext ctx = new MailContextImpl(null, null, getWindowControl().getBusinessControl().getAsString());
			MailerResult mailerResult = mailer.sendMailAsSeparateMails(ctx, Collections.singletonList(mod.getMember()), null, template, getIdentity());
			MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), getLocale());
		}
	}
	
	protected void doLeave(List<Identity> members, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			repositoryManager.removeMembers(members, repoEntry);
			businessGroupService.removeMembers(getIdentity(), members, repoEntry.getOlatResource(), mailing);
		} else {
			businessGroupService.removeMembers(getIdentity(), members, businessGroup.getResource(), mailing);
		}
		reloadModel();
	}
	
	protected void doSendMail(UserRequest ureq, List<MemberView> members) {
		List<Long> identityKeys = getMemberKeys(members);
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		
		ContactMessage contactMessage = new ContactMessage(getIdentity());
		String name = repoEntry != null ? repoEntry.getDisplayname() : businessGroup.getName();
		ContactList contactList = new ContactList(name);
		contactList.addAllIdentites(identities);
		contactMessage.addEmailTo(contactList);
		
		contactCtrl = new ContactFormController(ureq, getWindowControl(), false, true, false, false, contactMessage);
		listenTo(contactCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent(),
				true, translate("mail.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void doGraduate(UserRequest ureq, List<MemberView> members) {
		if(businessGroup != null) {
			List<Long> identityKeys = getMemberKeys(members);
			List<Identity> identitiesToGraduate = securityManager.loadIdentityByKeys(identityKeys);
			businessGroupService.moveIdentityFromWaitingListToParticipant(getIdentity(), identitiesToGraduate,
					businessGroup, null);
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
		List<RepositoryEntryMembership> repoMemberships =
				repoEntry == null ? Collections.<RepositoryEntryMembership>emptyList()
				: repositoryManager.getRepositoryEntryMembership(repoEntry);

		//groups membership
		List<BusinessGroup> groups = 
				repoEntry == null ? Collections.singletonList(businessGroup)
				: businessGroupService.findBusinessGroups(null, repoEntry.getOlatResource(), 0, -1);
				
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
				MemberView member = new MemberView(identity);
				member.getMembership().setPending(true);
				memberList.add(member);
				keyToMemberMap.put(identity.getKey(), member);
			}
		}
		
		for(Identity identity:identities) {
			MemberView member = new MemberView(identity);
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
