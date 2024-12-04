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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.modules.curriculum.ui.event.EditMemberEvent;
import org.olat.modules.curriculum.ui.member.MemberManagementTableModel.MemberCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementPendingUsersController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	protected static final String FILTER_CONFIRMATION_BY = "confirmationBy";
	protected static final String FILTER_CONFIRMATION_DATE = "confirmationDate";
	
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FlexiFiltersTab allTab;
	
	private FlexiTableElement tableEl;
	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private FormLink acceptAllButton;
	private FormLink addReservationButton;
	private final VelocityContainer detailsVC;
	private MemberManagementTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	
	private ToolsController toolsCtrl;
	private ContactFormController contactCtrl;
	private EditMemberController editSingleMemberCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	private int counter = 0;
	private boolean memberChanged;
	private final boolean chatEnabled;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final boolean membersManaged;
	private final String avatarMapperBaseURL;
	private final UserAvatarMapper avatarMapper;
	private final CurriculumSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private List<CurriculumElement> descendants;
	
	@Autowired
	private ACService acService;
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
	@Autowired
	private UserInfoService userInfoService;
	
	public CurriculumElementPendingUsersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback,
			UserAvatarMapper avatarMapper, String avatarMapperBaseURL) {
		super(ureq, wControl, "curriculum_element_pending", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.curriculumElement = curriculumElement;
		curriculum = curriculumElement.getCurriculum();
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		this.avatarMapper = avatarMapper;
		this.avatarMapperBaseURL = avatarMapperBaseURL;
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
		allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
		allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_structure");
		allLevelsButton.setPrimary(true);
		thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
		thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
		
		// Add/remove buttons
		if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			acceptAllButton = uifactory.addFormLink("accept.all", formLayout, Link.BUTTON);
			acceptAllButton.setIconLeftCSS("o_icon o_icon-fw o_icon_accept_all");
			
			addReservationButton = uifactory.addFormLink("add.member", formLayout, Link.BUTTON);
			addReservationButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.registration,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.role,
				new RolesFlexiCellRenderer(getTranslator())));
		NumOfCellRenderer numOfRenderer = new NumOfCellRenderer(descendants.size() + 1);	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.asParticipant,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.pending));

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
		rolesValues.add(SelectionValues.entry(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("confirmation.membership.by.admin")));
		rolesValues.add(SelectionValues.entry(ConfirmationByEnum.PARTICIPANT.name(), translate("confirmation.membership.by.participant")));
		FlexiTableSingleSelectionFilter confirmationByFilter = new FlexiTableSingleSelectionFilter(translate("filter.confirmation.by"),
				FILTER_CONFIRMATION_BY, rolesValues, true);
		filters.add(confirmationByFilter);

		SelectionValues datesValues = new SelectionValues();
		datesValues.add(SelectionValues.entry("true", translate("filter.confirmation.date")));
		FlexiTableOneClickSelectionFilter confirmationDateFilter = new FlexiTableOneClickSelectionFilter(translate("filter.confirmation.date"),
				FILTER_CONFIRMATION_DATE, datesValues, true);
		filters.add(confirmationDateFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		FlexiFiltersTab confirmByAdminTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("search.confirm.by.admin"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_CONFIRMATION_BY,
						ConfirmationByEnum.ADMINISTRATIVE_ROLE.name())));
		tabs.add(confirmByAdminTab);
		
		FlexiFiltersTab confirmByParticipantsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.coach.name(), translate("search.confirm.by.participant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_CONFIRMATION_BY,
						ConfirmationByEnum.PARTICIPANT.name())));
		tabs.add(confirmByParticipantsTab);
		
		FlexiFiltersTab withConfirmationDateTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FILTER_CONFIRMATION_DATE, translate("search.confirm.date"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_CONFIRMATION_DATE, Boolean.TRUE)));
		tabs.add(withConfirmationDateTab);

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
	
	private void reloadMember(UserRequest ureq, Identity member) {
		boolean openDetails = false;
		MemberRow row = tableModel.getObject(member);
		if(row != null && row.getDetailsController() != null) {
			doCloseMemberDetails(row);
			openDetails = true;
		}
		loadModel(false);
		if(openDetails) {
			MemberRow reloadedRow = tableModel.getObject(member);
			doOpenMemberDetails(ureq, reloadedRow);
		}
	}
	
	private void loadModel(boolean reset) {
		// Reservations
		List<OLATResource> resources = getCurriculumElementsResources();
		List<ResourceReservation> reservations = acService.getReservations(resources);

		// Memberships
		SearchMemberParameters params = getSearchCurriculumElementParameters();
		List<CurriculumMember> members = curriculumService.getCurriculumElementsMembers(params);
		
		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<Long> loadStatus = new ArrayList<>();
		for(ResourceReservation reservation:reservations) {
			final Identity member = reservation.getIdentity();
			CurriculumRoles role = ResourceToRoleKey.reservationToRole(reservation.getType());
			if(role != null) {
				MemberRow row = keyToMemberMap.computeIfAbsent(reservation.getIdentity().getKey(),
						key -> new MemberRow(member, userPropertyHandlers, getLocale()));
				row.addReservation(role, reservation);
				
				forgeLinks(row);
				forgeOnlineStatus(row, loadStatus);
				keyToMemberMap.put(row.getIdentityKey(), row);
			}
		}
		
		for(CurriculumMember member:members) {
			MemberRow row = keyToMemberMap.get(member.getIdentity().getKey());
			if(row != null && CurriculumRoles.isValueOf(member.getRole())) {
				CurriculumRoles role = CurriculumRoles.valueOf(member.getRole());
				row.addRole(role);
			}
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
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(reset, reset, true);
	}
	
	private List<OLATResource> getCurriculumElementsResources() {
		List<OLATResource> resources = new ArrayList<>();
		resources.add(curriculumElement.getResource());
		if(allLevelsButton.getComponent().isPrimary()) {
			for(CurriculumElement element:descendants) {
				resources.add(element.getResource());
			}
		}
		return resources;
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
	
	private SearchMemberParameters getSearchCurriculumElementParameters() {
		List<CurriculumElement> elements;
		if(thisLevelButton.getComponent().isPrimary()) {
			elements = List.of(curriculumElement);
		} else {
			elements = new ArrayList<>(descendants);
			elements.add(curriculumElement);
		}
		return new SearchMemberParameters(elements);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			loadModel(true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent singleEvent) {
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddMemberReservation(toAdd, (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			} else if (event instanceof MultiIdentityChosenEvent multiEvent) {
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddMemberReservation(multiEvent.getChosenIdentities(), (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(editSingleMemberCtrl == source) {
			if(event == Event.BACK_EVENT) {
				toolbarPanel.popController(editSingleMemberCtrl);
				if(memberChanged) {
					reloadMember(ureq, editSingleMemberCtrl.getMember());
					memberChanged = false;
				}
				cleanUp();
			} else if(event == Event.CHANGED_EVENT) {
				memberChanged = true;
			}
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(event instanceof EditMemberEvent ede) {
			doEditMember(ureq, ede.getMember());
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editSingleMemberCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		editSingleMemberCtrl = null;
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
		if(addReservationButton == source) {
			doAddReservation(ureq);
		} else if(acceptAllButton == source) {
			doAcceptAll(ureq);
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
	private void doIm(UserRequest ureq, MemberRow member) {
		Buddy buddy = imService.getBuddyById(member.getIdentityKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	private void doAcceptAll(UserRequest ureq) {
		// TODO curriculum
		getWindowControl().setWarning("Not implemented");
	}
	
	private void doAccept(UserRequest ureq, MemberRow member) {
		// TODO curriculum
		getWindowControl().setWarning("Not implemented");
	}
	
	private void doDecline(UserRequest ureq, MemberRow member) {
		// TODO curriculum
		getWindowControl().setWarning("Not implemented");
	}
	
	private void doAddReservation(UserRequest ureq) {
		doAddReservation(ureq, CurriculumRoles.participant);
	}
	
	private void doAddReservation(UserRequest ureq, CurriculumRoles role) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(role);
		listenTo(userSearchCtrl);
		
		String title = translate("add.member.role",  translate("role.".concat(role.name())));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMemberReservation(List<Identity> identitiesToAdd, CurriculumRoles role) {
		for(Identity identityToAdd:identitiesToAdd) {
			curriculumService.addMemberReservation(curriculumElement, identityToAdd, role, null, Boolean.TRUE, getIdentity(), null);
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
		
		UserInfoProfileConfig profileConfig = createProfilConfig();
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(),
				curriculum, elements, row, profileConfig, mainForm);
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
	
	private void doEditMember(UserRequest ureq, MemberRow member) {
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		doEditMember(ureq, identity);
	}
	
	private void doEditMember(UserRequest ureq, Identity member) {
		String fullname = userManager.getUserDisplayName(member);
		UserInfoProfileConfig profileConfig = createProfilConfig();
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		editSingleMemberCtrl = new EditMemberController(ureq, getWindowControl(),
				curriculum, elements, member, profileConfig);
		listenTo(editSingleMemberCtrl);
		toolbarPanel.pushController(fullname, editSingleMemberCtrl);
	}
	
	private UserInfoProfileConfig createProfilConfig() {
		UserInfoProfileConfig profileConfig = userInfoService.createProfileConfig();
		profileConfig.setChatEnabled(true);
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		return profileConfig;
	}
	
	private class ToolsController extends BasicController {
		
		private final Link contactLink;
		private final Link acceptLink;
		private final Link declineLink;
		private final Link editMemberLink;
		private final VelocityContainer mainVC;
		
		private MemberRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow member) {
			super(ureq, wControl, Util
					.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
			this.member = member;

			mainVC = createVelocityContainer("tools");

			contactLink = addLink("contact", "contact", "o_icon o_icon-fw o_icon_mail");
			acceptLink = addLink("accept", "accept", "o_icon o_icon-fw o_icon_check");
			declineLink = addLink("decline", "decline", "o_icon o_icon-fw o_icon_decline");
			editMemberLink = addLink("edit.member", "edit.member", "o_icon o_icon-fw o_icon_edit");
			
			putInitialPanel(mainVC);
		}
		private Link addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			return link;
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(contactLink == source) {
				doOpenContact(ureq, member);
			} else if(editMemberLink == source) {
				doEditMember(ureq, member);
			} else if(acceptLink == source) {
				doAccept(ureq, member);
			} else if(declineLink == source) {
				doDecline(ureq, member);
			}
		}
	}
}
