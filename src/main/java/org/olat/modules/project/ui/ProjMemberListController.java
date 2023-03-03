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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.InvitationContext;
import org.olat.course.member.wizard.InvitationFinishCallback;
import org.olat.course.member.wizard.Invitation_1_MailStep;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.project.ProjMemberInfo;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjMemberListTableModel.MemberCols;
import org.olat.modules.project.ui.event.OpenProjectEvent;
import org.olat.modules.project.ui.wizard.AddMemberUserStep;
import org.olat.modules.project.ui.wizard.ProjectRolesContext;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class ProjMemberListController extends FormBasicController implements Activateable2 {
	
	private final BreadcrumbedStackedPanel stackPanel;
	private FormLink addMemberLink;
	private DropdownItem addMemberDropdown;
	private FormLink invitationLink;
	private FormLink removeButton;
	private FormLink contactButton;
	private FlexiTableElement tableEl;
	private ProjMemberListTableModel tableModel;
	
	private int counter = 0;
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private boolean reloadProjectAfterWizard;

	private StepsMainRunController addMembersWizardCtrl;
	private StepsMainRunController invitationWizardCtrl;
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private ProjMemberEditController memberEditCtrl;
	private UserInfoMainController visitingCardCtrl;
	private ProjMemberRemoveConfirmationController memberRemoveConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public ProjMemberListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			ProjProject project, ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl, "member_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.stackPanel = stackPanel;
		this.project = project;
		this.secCallback = secCallback;

		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ProjMemberListTableModel.USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (secCallback.canEditMembers()) {
			addMemberLink = uifactory.addFormLink("member.add", formLayout, Link.BUTTON);
			addMemberLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add_member");
			
			addMemberDropdown = uifactory.addDropdownMenu("member.dropdown", null, formLayout, getTranslator());
			addMemberDropdown.setOrientation(DropdownOrientation.right);
			
			invitationLink = uifactory.addFormLink("member.invitation.add", formLayout, Link.LINK);
			invitationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			addMemberDropdown.addElement(invitationLink);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		initColumnsModel(columnsModel);
		
		tableModel = new ProjMemberListTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("member.list.empty.message", null, "o_icon_user");
		tableEl.setAndLoadPersistedPreferences(ureq, "project-member-list");
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setExportEnabled(true);
		
		contactButton = uifactory.addFormLink("member.bulk.contact", formLayout, Link.BUTTON);
		tableEl.addBatchButton(contactButton);
		
		if (secCallback.canEditMembers()) {
			removeButton = uifactory.addFormLink("member.bulk.remove", formLayout, Link.BUTTON);
			tableEl.addBatchButton(removeButton);
		}
	}
	
	private void initColumnsModel(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.id));
		
		int colPos = ProjMemberListTableModel.USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(ProjMemberListTableModel.USER_PROPS_ID , userPropertyHandler);

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
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.lastVisitDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.roles));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(MemberCols.tools);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
	}
	
	protected void loadModel() {
		loadModel(tableEl.getQuickSearchString());
	}
	
	private void loadModel(String searchString) {
		ProjMemberInfoSearchParameters params = new ProjMemberInfoSearchParameters();
		params.setProject(project);
		params.setRoles(ProjectRole.ALL);
		params.setSearchString(searchString);
		loadModel(params);
	}
	
	private void loadModel(ProjMemberInfoSearchParameters params) {
		params.setUserProperties(userPropertyHandlers);
		
		List<ProjMemberInfo> members = projectService.getMembersInfos(params);
		List<ProjMemberRow> rows = new ArrayList<>(members.size());
		for (ProjMemberInfo member : members) {
			ProjMemberRow row = new ProjMemberRow(member);
			
			Set<ProjectRole> roles = member.getRoles();
			String translatedRoles = roles.stream()
					.filter(role -> ProjectRole.invitee != role)
					.map(role -> ProjectUIFactory.translateRole(getTranslator(), role))
					.sorted()
					.collect(Collectors.joining(", "));
			if (roles.contains(ProjectRole.invitee) ) {
				translatedRoles += " " + ProjectUIFactory.translateRole(getTranslator(), ProjectRole.invitee);
			}
			row.setTranslatedRoles(translatedRoles);
			
			forgeToolsLink(row);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeToolsLink(ProjMemberRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == addMembersWizardCtrl || source == invitationWizardCtrl) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reload(ureq, reloadProjectAfterWizard);
				}
			}
		} else if (source == contactCtrl) {
			if (cmc != null) {
				cmc.deactivate();
			} else {
				stackPanel.popController(contactCtrl);
			}
			cleanUp();
		} else if (memberEditCtrl == source) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				doApplyMemberships(ureq, memberEditCtrl.getMember(), memberEditCtrl.getRoles());
			}
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if (source == memberRemoveConfirmationCtrl) {
			if (Event.DONE_EVENT == event) {
				List<Identity> members = memberRemoveConfirmationCtrl.getIdentities();
				doRemoveMembers(ureq, members);
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(memberRemoveConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(memberEditCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		memberRemoveConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		memberEditCtrl = null;
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
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjMemberRow row = tableModel.getObject(se.getIndex());
				if ("select".equals(cmd)) {
					doEditMemberships(ureq, row.getIdentity());
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				String cmd = event.getCommand();
				if(FlexiTableReduceEvent.SEARCH.equals(event.getCommand()) || FlexiTableReduceEvent.QUICK_SEARCH.equals(event.getCommand())) {
					FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
					loadModel(se.getSearch());
				} else if(FormEvent.RESET.getCommand().equals(cmd)) {
					loadModel();
				}
			}
		} else if (source == addMemberLink) {
			doImportMembers(ureq);
		} else if (source == invitationLink) {
			doInvitation(ureq);
		} else if (contactButton == source) {
			doSendMail(ureq);
		} else if( removeButton == source) {
			doConfirmRemoveSelectedMembers(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjMemberRow) {
				doOpenTools(ureq, (ProjMemberRow)link.getUserObject(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doImportMembers(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizardCtrl);
		
		reloadProjectAfterWizard = false;
		Step start = new AddMemberUserStep(ureq);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			Set<Identity> identites = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
			Set<ProjectRole> roles = ((ProjectRolesContext)runContext.get("roles")).getProjectRoles();
			
			if (identites.contains(getIdentity())) {
				Set<ProjectRole> currentRoles = projectService.getRoles(project, getIdentity());
				HashSet<ProjectRole> rolesCopy = new HashSet<>(roles);
				rolesCopy.removeAll(currentRoles);
				if (!rolesCopy.isEmpty()) {
					// New roles
					reloadProjectAfterWizard = true;
				}
			}
			
			Map<Identity, Set<ProjectRole>> identityToRoles = new HashMap<>(identites.size());
			identites.forEach(identity -> identityToRoles.put(identity, roles));
			projectService.updateMembers(getIdentity(), project, identityToRoles);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		addMembersWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("member.add"), null);
		listenTo(addMembersWizardCtrl);
		getWindowControl().pushAsModalDialog(addMembersWizardCtrl.getInitialComponent());
	}
	
	protected void doInvitation(UserRequest ureq) {
		removeAsListenerAndDispose(invitationWizardCtrl);

		InvitationContext invitationContext = InvitationContext.valueOf(getIdentity(), project);
		Step start = new Invitation_1_MailStep(ureq, invitationContext);
		StepRunnerCallback finish = new InvitationFinishCallback(invitationContext);
		invitationWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("member.invitation.add.title"), null);
		listenTo(invitationWizardCtrl);
		getWindowControl().pushAsModalDialog(invitationWizardCtrl.getInitialComponent());
	}
	
	private void doOpenTools(UserRequest ureq, ProjMemberRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenVisitingCard(UserRequest ureq, ProjMemberRow member) {
		removeAsListenerAndDispose(visitingCardCtrl);
		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentity().getKey());
		visitingCardCtrl = new UserInfoMainController(ureq, getWindowControl(), choosenIdentity, false, false);
		listenTo(visitingCardCtrl);
		
		String fullname = userManager.getUserDisplayName(choosenIdentity);
		stackPanel.pushController(fullname, visitingCardCtrl);
	}
	
	private void doOpenContact(UserRequest ureq, ProjMemberRow member) {
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
		
		stackPanel.pushController(fullname, contactCtrl);
	}
	
	private final void doConfirmRemoveMember(UserRequest ureq, ProjMemberRow member) {
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
			List<Identity> identities = securityManager.loadIdentityByKeys(memberKeys);
			memberRemoveConfirmationCtrl = new ProjMemberRemoveConfirmationController(ureq, getWindowControl(), identities);
			listenTo(memberRemoveConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), memberRemoveConfirmationCtrl.getInitialComponent(),
					true, translate("member.remove"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doRemoveMembers(UserRequest ureq, List<Identity> members) {
		// A project must have at least in owner
		List<Identity> owners = projectService.getMembers(project, List.of(ProjectRole.owner));
		List<Identity> ownersCopy = new ArrayList<>(owners);
		ownersCopy.removeAll(members);
		if (ownersCopy.isEmpty()) {
			if (members.size() == 1) {
				showInfo("error.owner.not.removed");
			} else {
				showInfo("error.owners.not.removed");
			}
			members.removeAll(owners);
		}
		
		projectService.removeMembers(getIdentity(), project, members);
		reload(ureq, members.contains(getIdentity()));
	}
	
	private void doEditMemberships(UserRequest ureq, Identity identity) {
		Set<ProjectRole> roles = projectService.getRoles(project, identity);
		memberEditCtrl = new ProjMemberEditController(ureq, getWindowControl(), project, identity, roles);
		listenTo(memberEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), memberEditCtrl.getInitialComponent(),
				true, translate("member.edit"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doApplyMemberships(UserRequest ureq, Identity member, Set<ProjectRole> roles) {
		// A project must have at least in owner
		List<Identity> owners = projectService.getMembers(project, List.of(ProjectRole.owner));
		if (!roles.contains(ProjectRole.owner) && owners.size() == 1 && member.equals(owners.get(0))) {
			roles.add(ProjectRole.owner);
			showInfo("error.last.owner.not.removed");
		}
		
		// Reload the project if the user has changed his own roles.
		boolean updatedMyself = false;
		if (member.equals(getIdentity())) {
			Set<ProjectRole> currentRoles = projectService.getRoles(project, getIdentity());
			if (!currentRoles.equals(roles)) {
				updatedMyself = true;
			}
		}
		
		projectService.updateMembers(getIdentity(), project, Map.of(member, roles));
		reload(ureq, updatedMyself);
	}

	private void reload(UserRequest ureq, boolean updatedMyself) {
		if (updatedMyself) {
			showInfo("error.project.reloaded.changed.roles");
			fireEvent(ureq, new OpenProjectEvent(project));
		} else {
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private List<Long> getSelectedIdentityKeys() {
		return tableEl.getMultiSelectedIndex().stream()
			.map(index -> tableModel.getObject(index.intValue()))
			.map(ProjMemberRow::getIdentity)
			.map(Identity::getKey)
			.collect(Collectors.toList());
	}
	
	private void doSendMail(UserRequest ureq) {
		List<Long> identityKeys = getSelectedIdentityKeys();
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		if(identities.isEmpty()) {
			showWarning("error.select.one.user");
			return;
		}
		
		ContactMessage contactMessage = new ContactMessage(getIdentity());
		String name;
		if (identities.size() == 1) {
			name = userManager.getUserDisplayName(identities.get(0));
		} else {
			name = project == null ? null : project.getTitle();
		}
		ContactList contactList = new ContactList(name);
		contactList.addAllIdentites(identities);
		contactMessage.addEmailTo(contactList);
		
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		listenTo(contactCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent(),
				true, translate("member.bulk.contact"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ProjMemberRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ProjMemberRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("member_tools");
			
			addLink("member.home", "home", "o_icon o_icon_home");
			addLink("contact", "contact", "o_icon o_icon_mail");
			if (secCallback.canEditMembers()) {
				addLink("member.edit", "edit", "o_icon o_icon_edit");
				addLink("member.remove", "remove", "o_icon o_icon_remove");
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
				} else if("edit".equals(cmd)) {
					doEditMemberships(ureq, row.getIdentity());
				}
			}
		}
	}
}
