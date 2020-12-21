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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableReduceEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.member.MemberListController;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.MemberListTableModel.Cols;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.IdentityStatusCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractMemberListController extends FormBasicController implements Activateable2 {

	protected static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	public static final String TABLE_ACTION_EDIT = "tbl_edit";
	public static final String TABLE_ACTION_MAIL = "tbl_mail";
	public static final String TABLE_ACTION_REMOVE = "tbl_remove";
	public static final String TABLE_ACTION_GRADUATE = "tbl_graduate";
	public static final String TABLE_ACTION_IM = "tbl_im";
	public static final String TABLE_ACTION_HOME = "tbl_home";
	public static final String TABLE_ACTION_CONTACT = "tbl_contact";
	public static final String TABLE_ACTION_ASSESSMENT = "tbl_assessment";

	protected FlexiTableElement membersTable;
	protected MemberListTableModel memberListModel;
	protected final TooledStackedPanel toolbarPanel;
	private FormLink editButton;
	private FormLink mailButton;
	private FormLink removeButton;
	
	private ToolsController toolsCtrl;
	protected CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private DialogBoxController confirmSendMailChangesBox;
	private DialogBoxController confirmSendMailGraduatesBox;
	private UserInfoMainController visitingCardCtrl;
	private EditMembershipController editMembersCtrl;
	private MemberLeaveConfirmationController leaveDialogBox;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditSingleMembershipController editSingleMemberCtrl;
	private StepsMainRunController editMemberShipStepsController;

	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AtomicInteger counter = new AtomicInteger();
	protected final RepositoryEntry repoEntry;
	private final BusinessGroup businessGroup;
	private final boolean isLastVisitVisible;
	private final boolean chatEnabled;
	
	private boolean overrideManaged = false;
	private final boolean globallyManaged;
	private final MemberListSecurityCallback secCallback;
	
	@Autowired
	private MemberViewQueries memberQueries;
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private UserSessionManager sessionManager;

	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry,
			String page, MemberListSecurityCallback secCallback,  TooledStackedPanel stackPanel) {
		this(ureq, wControl, repoEntry, null, page, secCallback, stackPanel, Util.createPackageTranslator(AbstractMemberListController.class, ureq.getLocale()));
	}
	
	public AbstractMemberListController(UserRequest ureq, WindowControl wControl, BusinessGroup group,
			String page, MemberListSecurityCallback secCallback, TooledStackedPanel stackPanel) {
		this(ureq, wControl, null, group, page, secCallback, stackPanel, Util.createPackageTranslator(AbstractMemberListController.class, ureq.getLocale()));
	}
	
	protected AbstractMemberListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, BusinessGroup group,
			String page, MemberListSecurityCallback secCallback, TooledStackedPanel stackPanel, Translator translator) {
		super(ureq, wControl, page, Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), translator));
		
		this.businessGroup = group;
		this.repoEntry = repoEntry;
		this.toolbarPanel = stackPanel;
		this.secCallback = secCallback;

		globallyManaged = calcGloballyManaged();
		
		Roles roles = ureq.getUserSession().getRoles();
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled() && !secCallback.isReadonly();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		isLastVisitVisible = securityModule.isUserLastVisitVisible(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public void overrideManaged(UserRequest ureq, boolean override) {
		if(isAllowedToOverrideManaged(ureq)) {
			overrideManaged = override;
			editButton.setVisible((!globallyManaged || overrideManaged) && !secCallback.isReadonly());
			removeButton.setVisible((!globallyManaged || overrideManaged) && !secCallback.isReadonly());
			flc.setDirty(true);
		}
	}
	
	protected boolean isAllowedToOverrideManaged(UserRequest ureq) {
		if(repoEntry != null) {
			Roles roles = ureq.getUserSession().getRoles();
			return roles.isAdministrator() && repositoryService.hasRoleExpanded(getIdentity(), repoEntry,
					OrganisationRoles.administrator.name());
		}
		return false;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		SortKey defaultSortKey = initColumns(columnsModel);
		
		memberListModel = new MemberListTableModel(columnsModel, imModule.isOnlineStatusEnabled());
		membersTable = uifactory.addTableElement(getWindowControl(), "memberList", memberListModel, 20, false, getTranslator(), formLayout);
		membersTable.setMultiSelect(true);
		membersTable.setEmtpyTableMessageKey("nomembers");
		membersTable.setAndLoadPersistedPreferences(ureq, this.getClass().getSimpleName() + "-v3");
		membersTable.setSearchEnabled(true);
		
		membersTable.setExportEnabled(true);
		membersTable.setSelectAllEnable(true);
		membersTable.setElementCssClass("o_sel_member_list");
		
		if(defaultSortKey != null) {
			FlexiTableSortOptions options = new FlexiTableSortOptions();
			options.setDefaultOrderBy(defaultSortKey);
			membersTable.setSortSettings(options);
		}

		editButton = uifactory.addFormLink("edit.members", formLayout, Link.BUTTON);
		editButton.setVisible((!globallyManaged || overrideManaged) && !secCallback.isReadonly());
		mailButton = uifactory.addFormLink("table.header.mail", formLayout, Link.BUTTON);
		removeButton = uifactory.addFormLink("table.header.remove", formLayout, Link.BUTTON);
		removeButton.setVisible((!globallyManaged || overrideManaged) && !secCallback.isReadonly());
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
			
			List<CurriculumElement> elements = curriculumService.getCurriculumElements(repoEntry);
			for(CurriculumElement element:elements) {
				managed &= CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.members);
			}
		}
		return managed;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private SortKey initColumns(FlexiTableColumnModel columnsModel) {
		SortKey defaultSortKey = null;
		String editAction = secCallback.isReadonly() ? null : TABLE_ACTION_EDIT;
		
		if(chatEnabled) {
			DefaultFlexiColumnModel chatCol = new DefaultFlexiColumnModel(Cols.online.i18n(), Cols.online.ordinal());
			chatCol.setExportable(false);
			columnsModel.addFlexiColumnModel(chatCol);
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.identityStatus, new IdentityStatusCellRenderer(getLocale())));
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, editAction, true, propName,
						new StaticFlexiCellRenderer(editAction, new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
			
			if(defaultSortKey == null) {
				defaultSortKey = new SortKey(propName, true);
			}
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.firstTime));
		if(isLastVisitVisible) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lastTime));
		}
		
		CourseRoleCellRenderer roleRenderer = new CourseRoleCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.role, roleRenderer));
		if(repoEntry != null) {
			GroupCellRenderer groupRenderer = new GroupCellRenderer();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.groups, groupRenderer));
		}
		
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(Cols.tools);
		toolsCol.setExportable(false);
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);
		return defaultSortKey;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == membersTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				MemberRow row = memberListModel.getObject(se.getIndex());
				if(TABLE_ACTION_IM.equals(cmd)) {
					doIm(ureq, row);
				} else if(TABLE_ACTION_EDIT.equals(cmd)) {
					openEdit(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				String cmd = event.getCommand();
				if(FlexiTableReduceEvent.SEARCH.equals(event.getCommand()) || FlexiTableReduceEvent.QUICK_SEARCH.equals(event.getCommand())) {
					FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
					String search = se.getSearch();
					doSearch(search);
				} else if(FormEvent.RESET.getCommand().equals(cmd)) {
					doResetSearch();
				}
			}
		} else if(editButton == source) {
			List<MemberRow> selectedItems = getMultiSelectedRows();
			openEdit(ureq, selectedItems);
		} else if(mailButton == source) {
			List<MemberRow> selectedItems = getMultiSelectedRows();
			doSendMail(ureq, selectedItems);
		} else if(removeButton == source) {
			List<MemberRow> selectedItems = getMultiSelectedRows();
			doConfirmRemoveMembers(ureq, selectedItems);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				MemberRow row = (MemberRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} else if("im".equals(cmd)) {
				MemberRow row = (MemberRow)link.getUserObject();
				doIm(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<MemberRow> getMultiSelectedRows() {
		Set<Integer> selections = membersTable.getMultiSelectedIndex();
		List<MemberRow> rows = new ArrayList<>(selections.size());
		if(selections.isEmpty()) {
			//do nothing
		} else {
			for(Integer i:selections) {
				int index = i.intValue();
				if(index >= 0 && index < memberListModel.getRowCount()) {
					MemberRow row = memberListModel.getObject(index);
					if(row != null) {
						rows.add(row);
					}
				}
			}
		}
		return rows;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == leaveDialogBox) {
			if (Event.DONE_EVENT == event) {
				List<Identity> members = leaveDialogBox.getIdentities();
				doRemoveMembers(ureq, members, leaveDialogBox.isSendMail());
				reloadModel();
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == editMemberShipStepsController) {
            if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
                // Close the dialog
                getWindowControl().pop();
                cleanUpPopups();
                
                // Reload form
                reloadModel();
            }
		} else if(source == editMembersCtrl) {
			/*cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doConfirmChangePermission(ureq, e, editMembersCtrl.getMembers());
			}*/
		} else if(source == editSingleMemberCtrl) {
			cmc.deactivate();
			cleanUpPopups();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doConfirmChangePermission(ureq, e, null);
			}
		} else if(confirmSendMailChangesBox == source) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			MailConfirmation confirmation = (MailConfirmation)confirmSendMailChangesBox.getUserObject();
			MemberPermissionChangeEvent e =confirmation.getE();
			if(e.getMember() != null) {
				doChangePermission(ureq, e, sendMail);
			} else {
				doChangePermission(ureq, e, confirmation.getMembers(), sendMail);
			}
		} else if(confirmSendMailGraduatesBox == source) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			GraduationConfirmation confirmation = (GraduationConfirmation)confirmSendMailGraduatesBox.getUserObject();
			doGraduate(confirmation.getRows(), sendMail);
		} else if (source == contactCtrl) {
			if(cmc != null) {
				cmc.deactivate();
			} else {
				toolbarPanel.popController(contactCtrl);
			}
			cleanUpPopups();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUpPopups();
			}
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
        removeAsListenerAndDispose(editMemberShipStepsController);
        
		cmc = null;
		contactCtrl = null;
		leaveDialogBox = null;
		editMembersCtrl = null;
		editSingleMemberCtrl = null;
		editMemberShipStepsController = null;
	}
	
	protected final void doConfirmRemoveMembers(UserRequest ureq, List<MemberRow> members) {
		if(members.isEmpty()) {
			showWarning("error.select.one.user");
		} else {
			int numOfOwners =
					repoEntry == null ? businessGroupService.countMembers(businessGroup, GroupRoles.coach.name())
					: repositoryService.countMembers(repoEntry, GroupRoles.owner.name());
			
			int numOfRemovedOwner = 0;
			List<Long> identityKeys = new ArrayList<>();
			for(MemberRow member:members) {
				identityKeys.add(member.getIdentityKey());
				if(member.getMembership().isOwner()) {
					numOfRemovedOwner++;
				}
			}
			if(numOfRemovedOwner == 0 || numOfOwners - numOfRemovedOwner > 0) {
				for (MemberRow member : members) {
					if (member.getCurriculumElements() != null) {
						showWarning("error.remove.user.from.curriculum");
						return;
					}
				}
				
				List<Identity> ids = securityManager.loadIdentityByKeys(identityKeys);
				leaveDialogBox = new MemberLeaveConfirmationController(ureq, getWindowControl(), ids, repoEntry != null);
				listenTo(leaveDialogBox);
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"), leaveDialogBox.getInitialComponent(),
						true, translate("edit.member"));
				cmc.activate();
				listenTo(cmc);
			} else {
				showWarning("error.atleastone");
			}
		}
	}
	
	protected void openEdit(UserRequest ureq, MemberRow member) {
		if(guardModalController(editSingleMemberCtrl)) return;
		
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		editSingleMemberCtrl = new EditSingleMembershipController(ureq, getWindowControl(), identity, repoEntry, businessGroup, false, overrideManaged);
		listenTo(editSingleMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editSingleMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void openEdit(UserRequest ureq, List<MemberRow> members) {
		if(members.isEmpty()) {
			showWarning("error.select.one.user");
		} else {
			List<Long> identityKeys = getMemberKeys(members);
			List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
			if(identities.size() == 1) {
				editSingleMemberCtrl = new EditSingleMembershipController(ureq, getWindowControl(), identities.get(0), repoEntry, businessGroup, false, overrideManaged);
				listenTo(editSingleMemberCtrl);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), editSingleMemberCtrl.getInitialComponent(),
						true, translate("edit.member"));
				cmc.activate();
				listenTo(cmc);
			} else {
				// Collect data in membersContext
				boolean sendMailMandatory = groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
				MembersContext membersContext = MembersContext.valueOf(repoEntry, businessGroup, overrideManaged, sendMailMandatory);
				
				
				// Create first step and finish callback
		        Step editMembershipStep = new EditMembershipStep1(ureq, identities, membersContext);
		        FinishedCallback finish = new FinishedCallback();
		        CancelCallback cancel = new CancelCallback();
		        
		        
		        // Create step controller
		        editMemberShipStepsController = new StepsMainRunController(ureq, getWindowControl(), editMembershipStep, finish, cancel, translate("edit.member"), null);
		        listenTo(editMemberShipStepsController);
		        getWindowControl().pushAsModalDialog(editMemberShipStepsController.getInitialComponent());
		        
				/*
				editMembersCtrl = new EditMembershipController(ureq, getWindowControl(), identities, repoEntry, businessGroup, overrideManaged);
				listenTo(editMembersCtrl);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), editMembersCtrl.getInitialComponent(),
						true, translate("edit.member"));
						*/
			}
			
		}
	}
	
	protected void doSearch(String search) {
		Map<String,String> propertiesSearch = new HashMap<>();
		for(UserPropertyHandler handler:userPropertyHandlers) {
			propertiesSearch.put(handler.getName(), search);
		}
		getSearchParams().setUserPropertiesSearch(propertiesSearch);
		getSearchParams().setLogin(search);
		reloadModel();
	}
	
	protected void doResetSearch() {
		getSearchParams().setLogin(null);
		getSearchParams().setUserPropertiesSearch(null);
		reloadModel();
	}
	
	private void doOpenTools(UserRequest ureq, MemberRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	/**
	 * Open private chat
	 * @param ureq
	 * @param member
	 */
	protected void doIm(UserRequest ureq, MemberRow member) {
		Buddy buddy = imService.getBuddyById(member.getIdentityKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(ureq, buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	protected void doConfirmChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, List<Identity> members) {
		if(e.size() == 0) {
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
			confirmSendMailChangesBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailChangesBox);
			confirmSendMailChangesBox.setUserObject(new MailConfirmation(e, members));
		}
	}
	
	protected void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			Roles roles = ureq.getUserSession().getRoles();
			List<RepositoryEntryPermissionChangeEvent> changes = Collections.singletonList((RepositoryEntryPermissionChangeEvent)e);
			repositoryManager.updateRepositoryEntryMemberships(getIdentity(), roles, repoEntry, changes, mailing);
			
			curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, e.getCurriculumChanges(), mailing);
		}

		businessGroupService.updateMemberships(getIdentity(), e.getGroupChanges(), mailing);
		//make sure all is committed before loading the model again (I see issues without)
		DBFactory.getInstance().commitAndCloseSession();
		reloadModel();
	}
	
	protected void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent changes, List<Identity> members, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			Roles roles = ureq.getUserSession().getRoles();
			List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.getRepoChanges();
			repositoryManager.updateRepositoryEntryMemberships(getIdentity(), roles, repoEntry, repoChanges, mailing);

			List<CurriculumElementMembershipChange> curriuclumChanges = changes.getCurriculumChanges();
			curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriuclumChanges, mailing);
		}

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.getGroupChanges();
		businessGroupService.updateMemberships(getIdentity(), allModifications, mailing);

		reloadModel();
	}
	
	protected void doRemoveMembers(UserRequest ureq, List<Identity> members, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(repoEntry != null) {
			Roles roles = ureq.getUserSession().getRoles();
			List<CurriculumElement> elements = curriculumService.getCurriculumElements(repoEntry);
			elements = curriculumService.filterElementsWithoutManagerRole(elements, roles);
			for(CurriculumElement element:elements) {
				curriculumService.removeMembers(element, members, overrideManaged);
			}
			businessGroupService.removeMembers(getIdentity(), members, repoEntry.getOlatResource(), mailing, overrideManaged);
			repositoryManager.removeMembers(getIdentity(), members, repoEntry, mailing);
		} else {
			businessGroupService.removeMembers(getIdentity(), members, businessGroup.getResource(), mailing, overrideManaged);
		}
		reloadModel();
	}
	
	protected void doSendMail(UserRequest ureq, List<MemberRow> members) {
		List<Long> identityKeys = getMemberKeys(members);
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		if(identities.isEmpty()) {
			showWarning("error.msg.send.no.rcps");
			return;
		}
		
		ContactMessage contactMessage = new ContactMessage(getIdentity());
		String name;
		if(identities.size() == 1) {
			name = userManager.getUserDisplayName(identities.get(0));
		} else {
			name = repoEntry != null ? repoEntry.getDisplayname() : businessGroup.getName();
		}
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
	
	protected void doConfirmGraduate(UserRequest ureq, List<MemberRow> members) {
		boolean mailMandatory = groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mailMandatory) {
			doGraduate(members, true);
		} else {
			confirmSendMailGraduatesBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailGraduatesBox);
			confirmSendMailGraduatesBox.setUserObject(new GraduationConfirmation(members));
		}
	}
	
	protected void doGraduate(List<MemberRow> members, boolean sendEmail) {
		MailPackage sendmailPackage = new MailPackage(sendEmail);
		if(businessGroup != null) {
			List<Long> identityKeys = getMemberKeys(members);
			List<Identity> identitiesToGraduate = securityManager.loadIdentityByKeys(identityKeys);
			businessGroupService.moveIdentityFromWaitingListToParticipant(getIdentity(), identitiesToGraduate,
					businessGroup, sendmailPackage);
		} else {
			Map<Long, BusinessGroup> groupsMap = new HashMap<>();
			Map<BusinessGroup, List<Identity>> graduatesMap = new HashMap<>();
			for(MemberRow member:members) {
				List<BusinessGroupShort> groups = member.getGroups();
				if(groups != null && !groups.isEmpty()) {
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
						fullGroup, sendmailPackage);
			}
		}
		reloadModel();
	}
	
	protected void doOpenVisitingCard(UserRequest ureq, MemberRow member) {
		removeAsListenerAndDispose(visitingCardCtrl);
		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentityKey());
		visitingCardCtrl = new UserInfoMainController(ureq, getWindowControl(), choosenIdentity, false, false);
		listenTo(visitingCardCtrl);
		
		String fullname = userManager.getUserDisplayName(choosenIdentity);
		toolbarPanel.pushController(fullname, visitingCardCtrl);
	}
	
	protected void doOpenContact(UserRequest ureq, MemberRow member) {
		removeAsListenerAndDispose(contactCtrl);
		
		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentityKey());
		String fullname = userManager.getUserDisplayName(choosenIdentity);
		
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList emailList = new ContactList(fullname);
		emailList.add(choosenIdentity);
		cmsg.addEmailTo(emailList);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("Contact");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactCtrl = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactCtrl);
		
		toolbarPanel.pushController(fullname, contactCtrl);
	}
	
	protected abstract void doOpenAssessmentTool(UserRequest ureq, MemberRow member);
	
	protected List<Long> getMemberKeys(List<MemberRow> members) {
		List<Long> keys = new ArrayList<>(members.size());
		if(!members.isEmpty()) {
			for(MemberRow member:members) {
				keys.add(member.getIdentityKey());
			}
		}
		return keys;
	}

	protected abstract SearchMembersParams getSearchParams();
	
	public void reloadModel() {
		updateTableModel(getSearchParams());
	}

	protected List<MemberRow> updateTableModel(SearchMembersParams params) {
		List<MemberView> memberViews;
		if(repoEntry != null) {
			memberViews = memberQueries.getRepositoryEntryMembers(repoEntry, params, userPropertyHandlers, getLocale());
		} else if(businessGroup != null) {
			memberViews = memberQueries.getBusinessGroupMembers(businessGroup, params, userPropertyHandlers, getLocale());
		} else {
			memberViews = Collections.emptyList();
		}

		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<MemberRow> memberList = new ArrayList<>();
		
		Long me = getIdentity().getKey();
		Set<Long> loadStatus = new HashSet<>();
		for(MemberView memberView:memberViews) {
			Long identityKey = memberView.getIdentityKey();
			MemberRow member = new MemberRow(memberView);
			if(chatEnabled) {
				if(identityKey.equals(me)) {
					member.setOnlineStatus("me");
				} else if(sessionManager.isOnline(identityKey)) {
					loadStatus.add(identityKey);
				} else {
					member.setOnlineStatus(Presence.unavailable.name());
				}
			}
			memberList.add(member);
			forgeLinks(member);
			keyToMemberMap.put(identityKey, member);
		}
		
		if(!loadStatus.isEmpty()) {
			List<Long> statusToLoadList = new ArrayList<>(loadStatus);
			Map<Long,String> statusMap = imService.getBuddyStatus(statusToLoadList);
			for(Long toLoad:statusToLoadList) {
				String status = statusMap.get(toLoad);
				MemberRow member = keyToMemberMap.get(toLoad);
				if(status == null) {
					member.setOnlineStatus(Presence.available.name());	
				} else {
					member.setOnlineStatus(status);	
				}
			}
		}
		
		if(repoEntry != null && isLastVisitVisible) {
			Map<Long,Date> lastLaunchDates = userInfosMgr.getRecentLaunchDates(repoEntry.getOlatResource());
			for(MemberRow memberView:keyToMemberMap.values()) {
				Long identityKey = memberView.getView().getIdentityKey();
				Date date = lastLaunchDates.get(identityKey);
				memberView.setLastTime(date);
			}
		}

		memberListModel.setObjects(memberList);
		membersTable.reset(true, true, true);
		return memberList;
	}
	
	protected void forgeLinks(MemberRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		
		FormLink chatLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "im", "", null, null, Link.NONTRANSLATED);
		chatLink.setIconLeftCSS("o_icon o_icon_status_unavailable");
		chatLink.setUserObject(row);
		row.setChatLink(chatLink);
	}
	
	private class FinishedCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
        	// Extract data from wizard
            MemberPermissionChangeEvent changeEvent = (MemberPermissionChangeEvent) runContext.get("membershipChanges");
            @SuppressWarnings("unchecked")
			List<Identity> members = (List<Identity>) runContext.get("members");
            boolean sendMail = (Boolean) runContext.get("sendMail");
            
            // Apply changes
            if(changeEvent.size() != 0) {
            	if(members == null) {
    				doChangePermission(ureq, changeEvent, sendMail);
    			} else {
    				doChangePermission(ureq, changeEvent, members, sendMail);
    			}
    		}

            // Fire event
            return StepsMainRunController.DONE_MODIFIED;
        }
    }

    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            return Step.NOSTEP;
        }
    }	
	
	private class GraduationConfirmation {
		private final List<MemberRow> rows;
		
		public GraduationConfirmation(List<MemberRow> rows) {
			this.rows = new ArrayList<>(rows);
		}

		public List<MemberRow> getRows() {
			return rows;
		}
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
	
	private class ToolsController extends BasicController {
		
		private final MemberRow row;
		
		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			
			//links
			addLink("home", TABLE_ACTION_HOME, "o_icon o_icon_home", links);
			addLink("contact", TABLE_ACTION_CONTACT, "o_icon o_icon_mail", links);
			if(repoEntry != null && "CourseModule".equals(repoEntry.getOlatResource().getResourceableTypeName())) {
				addLink("assessment", TABLE_ACTION_ASSESSMENT, "o_icon o_icon_certificate", links);
			}
			
			links.add("-");
			
			if(row.getMembership().isBusinessGroupWaiting() && !secCallback.isReadonly()) {
				addLink("table.header.graduate", TABLE_ACTION_GRADUATE, "o_icon o_icon_graduate", links);
			}

			if(!secCallback.isReadonly()) {
				addLink("edit.member", TABLE_ACTION_EDIT, "o_icon o_icon_edit", links);
			}
			
			if((!globallyManaged || overrideManaged) && secCallback.canRemoveMembers()) {
				addLink("table.header.remove", TABLE_ACTION_REMOVE, "o_icon o_icon_remove", links);
			}
			cleanSeparator(links);
			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void cleanSeparator(List<String> links) {
			if(!links.isEmpty() && links.get(links.size() - 1).equals("-")) {
				links.remove(links.size() - 1);
			}
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void doDispose() {
			//
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if(TABLE_ACTION_GRADUATE.equals(cmd)) {
					doConfirmGraduate(ureq, Collections.singletonList(row));
				} else if(TABLE_ACTION_EDIT.equals(cmd)) {
					openEdit(ureq, row);
				} else if(TABLE_ACTION_REMOVE.equals(cmd)) {
					doConfirmRemoveMembers(ureq, Collections.singletonList(row));
				} else if(TABLE_ACTION_HOME.equals(cmd)) {
					doOpenVisitingCard(ureq, row);
				} else if(TABLE_ACTION_CONTACT.equals(cmd)) {
					doOpenContact(ureq, row);
				} else if(TABLE_ACTION_ASSESSMENT.equals(cmd)) {
					doOpenAssessmentTool(ureq, row);
				}
			}
		}
	}
}
