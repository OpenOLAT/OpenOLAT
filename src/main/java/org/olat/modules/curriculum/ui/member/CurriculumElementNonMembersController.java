/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
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
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
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
 * Initial date: 4 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementNonMembersController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FlexiFiltersTab allTab;
	
	private FlexiTableElement tableEl;
	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private final VelocityContainer detailsVC;
	private MemberManagementTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private int counter = 0;
	private final boolean chatEnabled;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;

	private final String avatarMapperBaseURL;
	private final UserAvatarMapper avatarMapper;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private List<CurriculumElement> descendants;
	
	private ToolsController	toolsCtrl;
	private ContactFormController contactCtrl;
	private CloseableCalloutWindowController calloutCtrl;

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
	
	public CurriculumElementNonMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, UserAvatarMapper avatarMapper, String avatarMapperBaseURL) {
		super(ureq, wControl, "curriculum_element_non_members", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.toolbarPanel = toolbarPanel;
		this.curriculumElement = curriculumElement;
		curriculum = curriculumElement.getCurriculum();
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		this.avatarMapperBaseURL = avatarMapperBaseURL;
		this.avatarMapper = avatarMapper;
		detailsVC = createVelocityContainer("member_details");
		
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
		

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);

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
		// Reservations
		List<OLATResource> resources = getCurriculumElementsResources();
		List<ResourceReservation> reservations = acService.getReservations(resources);
		Set<Long> reservationsSet = reservations.stream()
				.map(r -> r.getIdentity().getKey())
				.collect(Collectors.toSet());

		// Memberships
		List<CurriculumElement> elements = getSearchCurriculumElements();
		List<CurriculumMember> members = curriculumService
				.getCurriculumElementsMembers(new SearchMemberParameters(elements));
		Set<Long> membersSet = members.stream()
				.map(c -> c.getIdentity().getKey())
				.collect(Collectors.toSet());
		
		// History
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setElements(elements);
		List<CurriculumElementMembershipHistory> membershipsHistory = curriculumService
				.getCurriculumElementMembershipsHistory(searchParams);

		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<Long> loadStatus = new ArrayList<>();
		for(CurriculumElementMembershipHistory history:membershipsHistory) {
			Identity identity = history.getIdentity();
			if(reservationsSet.contains(identity.getKey()) || membersSet.contains(identity.getKey())) {
				continue;
			}
			
			MemberRow row = keyToMemberMap.computeIfAbsent(identity.getKey(),
					key -> new MemberRow(identity, userPropertyHandlers, getLocale()));
			

			forgeLinks(row);
			forgeOnlineStatus(row, loadStatus);
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
	
	private List<CurriculumElement> getSearchCurriculumElements() {
		List<CurriculumElement> elements;
		if(thisLevelButton.getComponent().isPrimary()) {
			elements = List.of(curriculumElement);
		} else {
			elements = new ArrayList<>(descendants);
			elements.add(curriculumElement);
		}
		return elements;
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
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			loadModel(true);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allLevelsButton == source) {
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
	
	private UserInfoProfileConfig createProfilConfig() {
		UserInfoProfileConfig profileConfig = userInfoService.createProfileConfig();
		profileConfig.setChatEnabled(true);
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		return profileConfig;
	}
	
	private class ToolsController extends BasicController {
		
		private final Link contactLink;
		private final VelocityContainer mainVC;
		
		private MemberRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow member) {
			super(ureq, wControl, Util
					.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
			this.member = member;

			mainVC = createVelocityContainer("tools");

			contactLink = addLink("contact", "contact", "o_icon o_icon-fw o_icon_mail");
			
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
			}
		}
	}
}
