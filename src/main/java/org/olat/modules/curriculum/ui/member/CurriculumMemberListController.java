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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
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
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.MemberListController;
import org.olat.group.ui.main.EditMembershipController;
import org.olat.group.ui.main.EditSingleMembershipController;
import org.olat.group.ui.main.MemberLeaveConfirmationController;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.olat.modules.coach.ui.UserOverviewController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumMemberStats;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.member.CurriculumMemberListTableModel.MemberCols;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMemberListController extends FormBasicController implements Activateable2 {
	
	protected static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();

	public static final int USER_PROPS_OFFSET = 500;
	
	private FormLink editButton;
	private FormLink removeButton;
	private FormLink contactButton;
	private FlexiTableElement tableEl;
	private CurriculumMemberListTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private int counter = 0;
	private boolean membersManaged;
	private boolean overrideManaged = false;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<CurriculumRoles> restrictToRoles;
	private final CurriculumSecurityCallback secCallback;
	private final RoleSecurityCallback userOverviewSecurityCallback;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private CurriculumMemberSearchForm searchForm;
	private UserOverviewController userOverviewCtrl;
	private UserInfoMainController visitingCardCtrl;
	private EditMembershipController editMembersCtrl;
	private MemberLeaveConfirmationController leaveDialogBox;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditSingleMembershipController editSingleMemberCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumMemberListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumElement curriculumElement, List<CurriculumRoles> restrictToRoles,
			CurriculumSecurityCallback secCallback, boolean extendedSearch) {
		super(ureq, wControl, "members", Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.toolbarPanel = toolbarPanel;
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
		this.secCallback = secCallback;
		this.restrictToRoles = restrictToRoles;
		userOverviewSecurityCallback = RoleSecurityCallbackFactory
				.createFromStringsList(curriculumModule.getUserOverviewRightList());
		membersManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.members);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		
		if(extendedSearch) {
			searchForm = new CurriculumMemberSearchForm(ureq, getWindowControl(), mainForm);
			searchForm.setEnabled(true);
			listenTo(searchForm);
			tableEl.setSearchEnabled(true);
			tableEl.setExtendedSearch(searchForm);
			tableEl.expandExtendedSearch(ureq);
		} else {
			reloadModel();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		initColumnsModel(columnsModel);
		
		tableModel = new CurriculumMemberListTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setEmtpyTableMessageKey("nomembers");
		tableEl.setAndLoadPersistedPreferences(ureq, this.getClass().getSimpleName());
		tableEl.setSearchEnabled(true);
		
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setElementCssClass("o_sel_curriculum_member_list");
		
		contactButton = uifactory.addFormLink("contact.all", formLayout, Link.BUTTON);
		tableEl.addBatchButton(contactButton);
		
		if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			removeButton = uifactory.addFormLink("remove.memberships", formLayout, Link.BUTTON);
			tableEl.addBatchButton(removeButton);
		
			editButton = uifactory.addFormLink("edit", formLayout, Link.BUTTON);
			tableEl.addBatchButton(editButton);
		}
	}
	
	private void initColumnsModel(FlexiTableColumnModel columnsModel) {

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.id));
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, "select", true, propName,
						new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.progression, new LearningProgressCompletionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.firstTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.role, new CurriculumMembershipCellRenderer(getTranslator())));
		
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(MemberCols.tools);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	protected void reloadModel() {
		loadModel(tableEl.getQuickSearchString());
	}
	
	private void loadModel(String searchString) {
		SearchMemberParameters params = new SearchMemberParameters();
		params.setSearchString(searchString);
		loadModel(params);
	}
	
	private void loadModel(SearchMemberParameters params) {
		params.setUserProperties(userPropertyHandlers);
		if(restrictToRoles != null && !restrictToRoles.isEmpty()) {
			params.setRoles(restrictToRoles);
		}
		
		List<CurriculumMemberStats> members = curriculumService.getMembersWithStats(curriculumElement, params);
		List<CurriculumMemberRow> rows = new ArrayList<>();
		for(CurriculumMemberStats member:members) {
			rows.add(forgeRow(member));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumMemberRow forgeRow(CurriculumMemberStats member) {
		CurriculumMemberRow row = new CurriculumMemberRow(member.getIdentity(), member.getMembership(),
				member.getFirstTime(), member.getAverageCompletion());
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		
		return row;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchForm == source) {
			if(event instanceof SearchMembersEvent) {
				loadModel(((SearchMembersEvent)event).getSearchParameters());
			}
		} else if (source == contactCtrl) {
			if(cmc != null) {
				cmc.deactivate();
			} else {
				toolbarPanel.popController(contactCtrl);
			}
			cleanUp();
		} else if(userOverviewCtrl == source) {
			if("next.student".equals(event.getCommand())) {
				doNextMember(ureq, userOverviewCtrl.getEntry().getIdentityKey());
			} else if("previous.student".equals(event.getCommand())) {
				doPreviousMember(ureq, userOverviewCtrl.getEntry().getIdentityKey());
			}
		} else if(editMembersCtrl == source) {
			cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doApplyMemberships(ureq, editMembersCtrl.getMembers(), e);
			}
			cleanUp();
		} else if(editSingleMemberCtrl == source) {
			cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				doApplyMemberships(ureq, List.of(editSingleMemberCtrl.getMember()), e);
			}
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if (source == leaveDialogBox) {
			if (Event.DONE_EVENT == event) {
				List<Identity> members = leaveDialogBox.getIdentities();
				doRemoveMembers(ureq, members, leaveDialogBox.isSendMail());
				reloadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editSingleMemberCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(editMembersCtrl);
		removeAsListenerAndDispose(leaveDialogBox);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		editSingleMemberCtrl = null;
		toolsCalloutCtrl = null;
		editMembersCtrl = null;
		leaveDialogBox = null;
		contactCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					doSelect(ureq, tableModel.getObject(se.getIndex()), se.getIndex());
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				String cmd = event.getCommand();
				if(FlexiTableReduceEvent.SEARCH.equals(event.getCommand()) || FlexiTableReduceEvent.QUICK_SEARCH.equals(event.getCommand())) {
					FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
					loadModel(se.getSearch());
				} else if(FormEvent.RESET.getCommand().equals(cmd)) {
					reloadModel();
				}
			}
		} else if(contactButton == source) {
			doSendMail(ureq);
		} else if(removeButton == source) {
			doConfirmRemoveSelectedMembers(ureq);
		} else if(editButton == source) {
			doEditMemberships(ureq, getSelectedIdentityKeys());
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof CurriculumMemberRow) {
				doOpenTools(ureq, (CurriculumMemberRow)link.getUserObject(), link);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doNextMember(UserRequest ureq, Long identityKey) {
		int index = tableModel.indexOf(identityKey) + 1;
		if(index >= 0 && index < tableModel.getRowCount()) {
			doSelect(ureq, tableModel.getObject(index), index);
		} else if(tableModel.getRowCount() > 0) {
			doSelect(ureq, tableModel.getObject(0), 0);
		}
	}
	
	private void doPreviousMember(UserRequest ureq, Long identityKey) {
		int index = tableModel.indexOf(identityKey) - 1;
		if(index >= 0 && index < tableModel.getRowCount()) {
			doSelect(ureq, tableModel.getObject(index), index);
		} else if(tableModel.getRowCount() > 0) {
			index = tableModel.getRowCount() - 1;
			doSelect(ureq, tableModel.getObject(index), index);
		}
	}
	
	private void doSelect(UserRequest ureq, CurriculumMemberRow row, int index) {
		toolbarPanel.popController(userOverviewCtrl);
		
		Identity identity = row.getIdentity();
		StudentStatEntry identityStats = new StudentStatEntry(identity, userPropertyHandlers, getLocale());
        OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
        WindowControl bwControl = addToHistory(ureq, ores, null);
		
		userOverviewCtrl = new UserOverviewController(ureq, bwControl, toolbarPanel,
				identityStats, identity, index, tableModel.getRowCount(), null, userOverviewSecurityCallback);
		listenTo(userOverviewCtrl);
		
		String displayName = userManager.getUserDisplayName(identity);
		toolbarPanel.pushController(displayName, userOverviewCtrl);
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumMemberRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenVisitingCard(UserRequest ureq, CurriculumMemberRow member) {
		removeAsListenerAndDispose(visitingCardCtrl);
		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentity().getKey());
		visitingCardCtrl = new UserInfoMainController(ureq, getWindowControl(), choosenIdentity, false, false);
		listenTo(visitingCardCtrl);
		
		String fullname = userManager.getUserDisplayName(choosenIdentity);
		toolbarPanel.pushController(fullname, visitingCardCtrl);
	}
	
	private void doOpenContact(UserRequest ureq, CurriculumMemberRow member) {
		removeAsListenerAndDispose(contactCtrl);
		
		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentity().getKey());
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
	
	private final void doConfirmRemoveMember(UserRequest ureq, CurriculumMemberRow member) {
		doConfirmRemoveMembers(ureq, Collections.singletonList(member.getIdentity().getKey()));
	}
	
	private final void doConfirmRemoveSelectedMembers(UserRequest ureq) {
		List<Long> identityKeys = getSelectedIdentityKeys();
		doConfirmRemoveMembers(ureq, identityKeys);
	}
	
	private final void doConfirmRemoveMembers(UserRequest ureq, List<Long> memberKeys) {
		if(memberKeys.isEmpty()) {
			showWarning("error.select.one.user");
		} else {
			List<Identity> ids = securityManager.loadIdentityByKeys(memberKeys);
			leaveDialogBox = new MemberLeaveConfirmationController(ureq, getWindowControl(), ids, false);
			listenTo(leaveDialogBox);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), leaveDialogBox.getInitialComponent(),
					true, translate("edit.member"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doRemoveMembers(UserRequest ureq, List<Identity> members, boolean sendMail) {
		Roles roles = ureq.getUserSession().getRoles();
		List<CurriculumElement> elements = Collections.singletonList(curriculumElement);
		elements = curriculumService.filterElementsWithoutManagerRole(elements, roles);
		for(CurriculumElement element:elements) {
			curriculumService.removeMembers(element, members, overrideManaged);
		}
		reloadModel();
		dbInstance.commitAndCloseSession();
		
		if(sendMail) {
			MailTemplate template = CurriculumMailing.getRemoveMailTemplate(curriculum, curriculumElement, getIdentity());
			MailPackage mailing = new MailPackage(template, null);
			for(Identity member:members) {
				CurriculumMailing.sendEmail(getIdentity(), member, curriculum, curriculumElement, mailing);
			}
		}
	}
	
	private void doEditMemberships(UserRequest ureq, Identity identity) {
		editSingleMemberCtrl = new EditSingleMembershipController(ureq, getWindowControl(),
				identity, curriculum, curriculumElement, false, overrideManaged);
		listenTo(editSingleMemberCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editSingleMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditMemberships(UserRequest ureq, List<Long> memberKeys) {
		if(memberKeys.isEmpty()) {
			showWarning("error.select.one.user");
		} else {
			List<Identity> identities = securityManager.loadIdentityByKeys(memberKeys);

			editMembersCtrl = new EditMembershipController(ureq, getWindowControl(), identities,
					curriculum, curriculumElement, overrideManaged);
			listenTo(editMembersCtrl);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editMembersCtrl.getInitialComponent(),
					true, translate("edit.member"));

			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doApplyMemberships(UserRequest ureq, List<Identity> members, MemberPermissionChangeEvent e) {
		MailPackage mailing = new MailPackage(false);
		Roles roles = ureq.getUserSession().getRoles();
		//commit all changes to the curriculum memberships
		List<CurriculumElementMembershipChange> curriculumChanges = e.generateCurriculumElementMembershipChange(members);
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriculumChanges, mailing);
		
		reloadModel();
	}
	
	private List<Long> getSelectedIdentityKeys() {
		return tableEl.getMultiSelectedIndex().stream()
			.map(index -> tableModel.getObject(index.intValue()))
			.map(CurriculumMemberRow::getIdentity)
			.map(Identity::getKey)
			.collect(Collectors.toList());
	}
	
	private void doSendMail(UserRequest ureq) {
		List<Long> identityKeys = getSelectedIdentityKeys();
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
			name = curriculumElement == null ? null : curriculumElement.getDisplayName();
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
	
	private class ToolsController extends BasicController {
		
		private final CurriculumMemberRow row;
		
		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumMemberRow row) {
			super(ureq, wControl, Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
			this.row = row;
			
			mainVC = createVelocityContainer("tools");

			//links
			addLink("home", "home", "o_icon o_icon_home");
			addLink("contact", "contact", "o_icon o_icon_mail");
			if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
				addLink("edit.member", "edit.member", "o_icon o_icon_edit");
				addLink("remove.memberships", "remove", "o_icon o_icon_remove");
			}

			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
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
				if("home".equals(cmd)) {
					doOpenVisitingCard(ureq, row);
				} else if("contact".equals(cmd)) {
					doOpenContact(ureq, row);
				} else if("remove".equals(cmd)) {
					doConfirmRemoveMember(ureq, row);
				} else if("edit.member".equals(cmd)) {
					doEditMemberships(ureq, row.getIdentity());
				}
			}
		}
	}
}
