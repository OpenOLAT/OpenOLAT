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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
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
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.member.MemberManagementTableModel.MemberCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public abstract class AbstractMembersController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	protected static final String TOGGLE_DETAILS_CMD = "toggle-details";

	protected FlexiFiltersTab allTab;
	
	protected FlexiTableElement tableEl;
	protected FormLink allLevelsButton;
	protected FormLink thisLevelButton;
	protected final VelocityContainer detailsVC;
	protected MemberManagementTableModel tableModel;
	protected final TooledStackedPanel toolbarPanel;

	protected int counter = 0;
	protected final boolean chatEnabled;
	protected final CurriculumSecurityCallback secCallback;
	protected final List<UserPropertyHandler> userPropertyHandlers;

	protected final Curriculum curriculum;
	protected final List<CurriculumElement> descendants;
	protected final CurriculumElement curriculumElement;
	
	protected ContactFormController contactCtrl;
	protected EditMemberController editSingleMemberCtrl;

	@Autowired
	protected ACService acService;
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	protected InstantMessagingModule imModule;
	@Autowired
	protected InstantMessagingService imService;
	@Autowired
	protected UserSessionManager sessionManager;
	@Autowired
	protected BaseSecurityModule securityModule;
	@Autowired
	protected CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public AbstractMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel, String page,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, page, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		this.curriculum = curriculumElement.getCurriculum();
		
		this.toolbarPanel = toolbarPanel;
		toolbarPanel.addListener(this);
		
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		detailsVC = createVelocityContainer("member_details");
		
		descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);
	}
	
	@Override
	protected void doDispose() {
		toolbarPanel.removeListener(this);
		super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initTableForm(formLayout);
	}
	
	protected void initButtonsForm(FormItemContainer formLayout) {
		allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
		allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_structure");
		allLevelsButton.setPrimary(true);
		thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
		thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
	}

	protected void initTableForm(FormItemContainer formLayout) {
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
		
		initColumns(columnsModel);

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
	
	protected abstract void initColumns(FlexiTableColumnModel columnsModel);
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		initFilters(filters);
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected abstract void initFilters(List<FlexiTableExtendedFilter> filters);
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		initFiltersPresets(tabs);

		FlexiFiltersTab searchTab = FlexiFiltersTabFactory.tab("search", translate("search"), TabSelectionBehavior.clear);
		searchTab.setLargeSearch(true);
		searchTab.setFiltersExpanded(true);
		tabs.add(searchTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected abstract void initFiltersPresets(List<FlexiFiltersTab> tabs);
	
	protected void updateUI() {
		boolean canSubTypes = !descendants.isEmpty() || curriculumElement.getType() == null
				|| !curriculumElement.getType().isSingleElement();
		allLevelsButton.setVisible(canSubTypes);
		thisLevelButton.setVisible(canSubTypes);
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
	
	protected void reloadMember(UserRequest ureq, Identity member) {
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
	
	protected abstract void loadModel(boolean reset);
	
	protected void loadImStatus(List<Long> loadStatus, Map<Long,MemberRow> keyToMemberMap) {
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
	}
	
	protected List<OLATResource> getCurriculumElementsResources() {
		List<OLATResource> resources = new ArrayList<>();
		resources.add(curriculumElement.getResource());
		if(allLevelsButton.getComponent().isPrimary()) {
			for(CurriculumElement element:descendants) {
				if(element.getResource() != null) {
					resources.add(element.getResource());
				}
			}
		}
		return resources;
	}
	
	protected SearchMemberParameters getSearchCurriculumElementParameters() {
		List<CurriculumElement> elements = getSearchCurriculumElements();
		return new SearchMemberParameters(elements);
	}
	
	protected List<CurriculumElement> getSearchCurriculumElements() {
		List<CurriculumElement> elements;
		if(thisLevelButton.getComponent().isPrimary()) {
			elements = List.of(curriculumElement);
		} else {
			elements = new ArrayList<>(descendants);
			elements.add(curriculumElement);
		}
		return elements;
	}
	
	protected void forgeLinks(MemberRow row) {
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
	
	protected List<CurriculumElement> getAllCurriculumElements() {
		List<CurriculumElement> elements = new ArrayList<>();
		elements.add(curriculumElement);
		if(descendants != null) {
			elements.addAll(descendants);
		}
		return elements;
	}
	
	protected final void forgeOnlineStatus(MemberRow row, List<Long> loadStatus) {
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
	
	

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof MemberDetailsController detailsCtrl
				&& detailsCtrl.getUserObject() instanceof MemberRow row) {
			if(event == Event.CHANGED_EVENT) {
				reloadMember(ureq, row.getIdentity());
			}
		}
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(editSingleMemberCtrl);
		removeAsListenerAndDispose(contactCtrl);
		editSingleMemberCtrl = null;
		contactCtrl = null;
	}
	

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(toolbarPanel == source && event instanceof PopEvent pe && pe.getController() == contactCtrl) {
			tableEl.deselectAll();
		}
		super.event(ureq, source, event);
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

	protected abstract void doOpenTools(UserRequest ureq, MemberRow member, FormLink link);

	protected abstract void doOpenMemberDetails(UserRequest ureq, MemberRow row);
	
	protected final void doOpenMemberDetails(UserRequest ureq, MemberRow row, boolean withEdit, boolean withAcceptDecline) {
		if(row == null) return;
		
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		Identity member = securityManager.loadIdentityByKey(row.getIdentityKey());
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, null, withEdit, withAcceptDecline, true, false, true,
				true, true, true);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, curriculumElement, elements, member, config);
		detailsCtrl.setUserObject(row);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	protected final void doCloseMemberDetails(MemberRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	protected void doEditMember(UserRequest ureq, MemberRow member) {
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		doEditMember(ureq, identity);
	}
	
	protected void doEditMember(UserRequest ureq, Identity member) {
		String fullname = userManager.getUserDisplayName(member);
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		editSingleMemberCtrl = new EditMemberController(ureq, getWindowControl(),
				curriculum, elements, member, profileConfig);
		listenTo(editSingleMemberCtrl);
		toolbarPanel.pushController(fullname, editSingleMemberCtrl);
	}

	protected void doOpenContact(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities();
		doOpenContact(ureq, identities);
	}
	
	protected void doOpenContact(UserRequest ureq, MemberRow member) {
		doOpenContact(ureq, List.of(member.getIdentity()));
	}
	
	protected void doOpenContact(UserRequest ureq, List<Identity> members) {
		removeAsListenerAndDispose(contactCtrl);
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList emailList = new ContactList(curriculumElement.getDisplayName());
		emailList.addAllIdentites(members);
		cmsg.addEmailTo(emailList);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("Contact");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactCtrl = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactCtrl);
		
		toolbarPanel.pushController(translate("contact.title"), contactCtrl);
	}
	
	protected List<Identity> getSelectedIdentities() {
		List<Identity> identities = new ArrayList<>();
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		for(Integer index:selectedIndexes) {
			MemberRow row = tableModel.getObject(index.intValue());
			if(row != null) {
				identities.add(row.getIdentity());
			}
		}
		return identities;
	}
	
	protected List<Offer> getAvailableOffers() {
		return (curriculumElement.getParent() == null)
				? acService.findOfferByResource(curriculumElement.getResource(), true, null, null)
				: List.of();
	}
	
}
