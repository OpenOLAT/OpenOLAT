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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.scope.FormScopeSelection;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.RoleListController;
import org.olat.modules.curriculum.ui.event.RoleEvent;
import org.olat.modules.curriculum.ui.member.MemberManagementTableModel.MemberCols;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementUserManagementController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String ACTIVE_SCOPE = "Active";
	private static final String PENDING_SCOPE = "Pending";
	private static final String NON_MEMBERS_SCOPE = "NonMembers";
	private static final String HISTORY_SCOPE = "History";
	
	private static final String FILTER_ROLE = "Role";
	
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FlexiFiltersTab allTab;
	
	private FlexiTableElement tableEl;
	private FormLink addMemberButton;
	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private FormLink removeMembershipButton;
	private FormScopeSelection searchScopes;
	private final VelocityContainer detailsVC;
	private MemberManagementTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmRemoveCtrl;
	
	private ToolsController toolsCtrl;
	private RoleListController roleListCtrl;
	private ContactFormController contactCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	private int counter = 0;
	private final boolean chatEnabled;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final boolean membersManaged;
	private final String avatarMapperBaseURL;
	private final CurriculumSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
	private final Map<CurriculumRoles,FlexiFiltersTab> rolesToTab = new EnumMap<>(CurriculumRoles.class);
	
	private List<CurriculumElement> descendants;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserSessionManager sessionManager;
	
	public CurriculumElementUserManagementController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_user_mgmt", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.curriculumElement = curriculumElement;
		curriculum = curriculumElement.getCurriculum();
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		detailsVC = createVelocityContainer("member_details");
		
		membersManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.members);
		descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initTableForm(formLayout);
	}

	private void initButtonsForm(FormItemContainer formLayout) {
		// Scope active, pending...
		List<Scope> scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(ACTIVE_SCOPE, translate("search.active"), null));
		scopes.add(ScopeFactory.createScope(PENDING_SCOPE, translate("search.pending"), null));
		scopes.add(ScopeFactory.createScope(NON_MEMBERS_SCOPE, translate("search.non.members"), null));
		scopes.add(ScopeFactory.createScope(HISTORY_SCOPE, translate("search.members.history"), null));
		searchScopes = uifactory.addScopeSelection("search.scopes", null, formLayout, scopes);
		
		allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
		allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_circle_check");
		allLevelsButton.setPrimary(true);
		thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
		thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
		
		// Add/remove buttons
		if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			addMemberButton = uifactory.addFormLink("add.member", formLayout, Link.BUTTON);
			addMemberButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
			addMemberButton.setIconRightCSS("o_icon o_icon_caret");
		
			removeMembershipButton = uifactory.addFormLink("remove.memberships", formLayout, Link.BUTTON);
		}
	}
	
	private void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		if(chatEnabled) {
			DefaultFlexiColumnModel chatCol = new DefaultFlexiColumnModel(MemberCols.online);
			chatCol.setExportable(false);
			columnsModel.addFlexiColumnModel(chatCol);
		}

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			String name = userPropertyHandler.getName();
			String action = UserConstants.NICKNAME.equals(name) || UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name)
					? TOGGLE_DETAILS_CMD : null;
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, action, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.role,
				new RolesFlexiCellRenderer(getTranslator())));
		NumOfCellRenderer numOfRenderer = new NumOfCellRenderer(descendants.size() + 1);	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.asParticipant,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asCoach,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asOwner,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asMasterCoach,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asElementOwner,
				numOfRenderer));

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(MemberCols.tools);
		toolsColumn.setIconHeader("o_icon o_icon-lg o_icon_actions");
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);

		tableModel = new MemberManagementTableModel(columnsModel, getTranslator(), getLocale(),
				imModule.isOnlineStatusEnabled()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		initFilters();
		initFiltersPresets();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues rolesValues = new SelectionValues();
		rolesValues.add(SelectionValues.entry(CurriculumRoles.participant.name(), translate("search.role.participant")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.coach.name(), translate("search.role.coach")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.mastercoach.name(), translate("search.role.mastercoach")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.owner.name(), translate("search.role.course.owner")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.curriculumelementowner.name(), translate("search.role.owner")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.roles"),
				FILTER_ROLE, rolesValues, true);
		filters.add(statusFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of())));
		tabs.add(allTab);
		
		FlexiFiltersTab participantsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.participant.name(), translate("search.role.participant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.participant.name()))));
		tabs.add(participantsTab);
		rolesToTab.put(CurriculumRoles.participant, participantsTab);
		
		FlexiFiltersTab coachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.coach.name(), translate("search.role.coach"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.coach.name()))));
		tabs.add(coachesTab);
		rolesToTab.put(CurriculumRoles.coach, coachesTab);
		
		FlexiFiltersTab masterCoachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.mastercoach.name(), translate("search.role.mastercoach"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.mastercoach.name()))));
		tabs.add(masterCoachesTab);
		rolesToTab.put(CurriculumRoles.mastercoach, masterCoachesTab);
		
		FlexiFiltersTab ownersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.owner.name(), translate("search.role.course.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.owner.name()))));
		tabs.add(ownersTab);
		rolesToTab.put(CurriculumRoles.owner, ownersTab);
		
		FlexiFiltersTab curriculumElementOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.curriculumelementowner.name(), translate("search.role.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.curriculumelementowner.name()))));
		tabs.add(curriculumElementOwnersTab);
		rolesToTab.put(CurriculumRoles.curriculumelementowner, curriculumElementOwnersTab);

		FlexiFiltersTab searchTab = FlexiFiltersTabFactory.tab("search", translate("search"), TabSelectionBehavior.clear);
		searchTab.setLargeSearch(true);
		searchTab.setFiltersExpanded(true);
		tabs.add(searchTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof MemberRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	private void loadModel(boolean reset) {
		SearchMemberParameters params = getSearchParameters();
		List<CurriculumMember> members = curriculumService.getCurriculumElementsMembers(params);
		
		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<Long> loadStatus = new ArrayList<>();
		for(CurriculumMember member:members) {
			MemberRow row = keyToMemberMap.computeIfAbsent(member.getIdentity().getKey(),
					key -> new MemberRow(member, userPropertyHandlers, getLocale()));
			
			if(CurriculumRoles.isValueOf(member.getRole())) {
				CurriculumRoles role = CurriculumRoles.valueOf(member.getRole());
				row.addRole(role);
			}
		
			forgeLinks(row);
			forgeOnlineStatus(row, loadStatus);
			keyToMemberMap.put(row.getIdentityKey(), row);
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
		
		List<MemberRow> rows = new ArrayList<>(keyToMemberMap.values());
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}
	
	private void forgeOnlineStatus(MemberRow row, List<Long> loadStatus) {
		if(chatEnabled) {
			Long identityKey = row.getIdentityKey();
			if(identityKey.equals(getIdentity().getKey())) {
				row.setOnlineStatus("me");
			} else if(sessionManager.isOnline(identityKey)) {
				loadStatus.add(identityKey);
			} else {
				row.setOnlineStatus(Presence.unavailable.name());
			}
		}
	}
	
	private void forgeLinks(MemberRow row) {
		String id = Integer.toString(++counter);
		
		FormLink toolsLink = uifactory.addFormLink("tools_".concat(id), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setTitle(translate("action.more"));
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		
		FormLink chatLink = uifactory.addFormLink("chat_".concat(id), "im", "", null, null, Link.NONTRANSLATED);
		chatLink.setIconLeftCSS("o_icon o_icon_status_unavailable");
		chatLink.setTitle(translate("user.info.presence.unavailable"));
		chatLink.setUserObject(row);
		row.setChatLink(chatLink);
	}
	
	private SearchMemberParameters getSearchParameters() {
		List<CurriculumElement> elements;
		if(thisLevelButton.getComponent().isPrimary()) {
			elements = List.of(curriculumElement);
		} else {
			elements = new ArrayList<>(descendants);
			elements.add(curriculumElement);
		}
		SearchMemberParameters params = new SearchMemberParameters(elements);
		params.setSearchString(tableEl.getQuickSearchString());
		params.setUserProperties(userPropertyHandlers);
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, FILTER_ROLE);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<CurriculumRoles> roles = filterValues.stream()
						.map(CurriculumRoles::valueOf)
						.toList();
				params.setRoles(roles);
			}
		}
		
		return params;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ACTIVE_SCOPE.equals(type)) {
			searchScopes.setSelectedKey(ACTIVE_SCOPE);
			loadModel(true);
		}  else if(CurriculumRoles.isValueOf(type)) {
			FlexiFiltersTab tab = rolesToTab.get(CurriculumRoles.valueOf(type));
			if(tab == null) {
				tab = allTab;
			}
			tableEl.setSelectedFilterTab(ureq, tab);
			loadModel(true);
		} else if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			loadModel(true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<MemberRow> rows = (List<MemberRow>)confirmRemoveCtrl.getUserObject();
				doRemove(rows);
			}
		} else if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent singleEvent) {
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddMember(toAdd, (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			} else if (event instanceof MultiIdentityChosenEvent multiEvent) {
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddMember(multiEvent.getChosenIdentities(), (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(roleListCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
			if(event instanceof RoleEvent re) {
				doSearchMember(ureq, re.getRole());
			}
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = null;
		userSearchCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMemberButton == source) {
			doRollCallout(ureq);
		} else if(removeMembershipButton == source) {
			doConfirmRemoveAllMemberships(ureq);
		} else if(allLevelsButton == source) {
			doToggleLevels(false);
		} else if(thisLevelButton == source) {
			doToggleLevels(true);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					MemberRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel(true);
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				 MemberRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenMemberDetails(ureq, row);
				} else {
					doCloseMemberDetails(row);
				}
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof MemberRow row) {
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, row, link);
			} else if("im".equals(cmd)) {
				doIm(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doToggleLevels(boolean thisLevel) {
		allLevelsButton.setPrimary(!thisLevel);
		thisLevelButton.setPrimary(thisLevel);
		loadModel(true);
	}
	
	/**
	 * Open private chat
	 * @param ureq The user request
	 * @param member The member
	 */
	protected void doIm(UserRequest ureq, MemberRow member) {
		Buddy buddy = imService.getBuddyById(member.getIdentityKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	private void doConfirmRemoveAllMemberships(UserRequest ureq) {
		Set<Integer> selectedRows = tableEl.getMultiSelectedIndex();
		List<MemberRow> rows = new ArrayList<>(selectedRows.size());
		for(Integer selectedRow:selectedRows) {
			MemberRow row = tableModel.getObject(selectedRow.intValue());
			if(row.getInheritanceMode() == GroupMembershipInheritance.root || row.getInheritanceMode() == GroupMembershipInheritance.none) {
				rows.add(row);
			}
		}
		
		if(rows.isEmpty()) {
			showWarning("warning.atleastone.member");
		} else {
			String title = translate("confirm.remove.member.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.member.text", ""), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}
	
	private void doRemove(List<MemberRow> membersToRemove) {
		for(MemberRow memberToRemove:membersToRemove) {
			if(CurriculumRoles.isValueOf(memberToRemove.getRole())) {
				CurriculumRoles role = CurriculumRoles.valueOf(memberToRemove.getRole());
				Identity member = securityManager.loadIdentityByKey(memberToRemove.getIdentityKey());
				curriculumService.removeMember(curriculumElement, member, role, getIdentity());
			}
		}
		loadModel(true);
	}
	
	private void doRollCallout(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(roleListCtrl);
		
		String title = translate("add.member");
		CurriculumRoles[] roles = new CurriculumRoles[] {
				CurriculumRoles.curriculumelementowner, CurriculumRoles.mastercoach,
				CurriculumRoles.owner, CurriculumRoles.coach, CurriculumRoles.participant
		};
		roleListCtrl = new RoleListController(ureq, getWindowControl(), roles);
		listenTo(roleListCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), roleListCtrl.getInitialComponent(), addMemberButton, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	private void doSearchMember(UserRequest ureq, CurriculumRoles role) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(role);
		listenTo(userSearchCtrl);
		
		String title = translate("add.member.role",  translate("role.".concat(role.name())));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMember(List<Identity> identitiesToAdd, CurriculumRoles role) {
		for(Identity identityToAdd:identitiesToAdd) {
			curriculumService.addMember(curriculumElement, identityToAdd, role, getIdentity());
		}
		loadModel(true);
	}
	
	private void doOpenMemberDetails(UserRequest ureq, MemberRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(),
				curriculum, curriculumElement, elements, row, avatarMapper, avatarMapperBaseURL, mainForm);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doCloseMemberDetails(MemberRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private void doOpenTools(UserRequest ureq, MemberRow member, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), member);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenContact(UserRequest ureq, MemberRow member) {
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
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private MemberRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow member) {
			super(ureq, wControl);
			this.member = member;

			mainVC = createVelocityContainer("tools");

			addLink("contact", "contact", "o_icon o_icon-fw o_icon_mail");
			
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
			if(source instanceof Link link) {
				String cmd = link.getCommand();
				if("contact".equals(cmd)) {
					doOpenContact(ureq, member);
				}
			}
		}
	}
}
