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
package org.olat.course.member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.member.MemberSearchTableModel.MembersCols;
import org.olat.course.member.component.MemberOriginCellRenderer;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.MemberView;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.CourseRoleCellRenderer;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.main.SearchMembersParams.Origin;
import org.olat.group.ui.main.SearchMembersParams.UserType;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberSearchController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = MemberSearchController.class.getCanonicalName();
	
	private static final String ALL_TAB_ID = "All";
	private static final String FILTER_ROLE = "filter.role";
	private static final String FILTER_ORIGIN = "filter.origin";
	private static final String FILTER_TYPE = "filter.type";
	
	private static final String CMD_SELECT = "select";
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private MemberSearchTableModel tableModel;
	
	private final boolean coach;
	private final boolean owner;
	private final MemberSearchConfig config;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private Object userObject;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MemberViewQueries memberQueries;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public MemberSearchController(UserRequest ureq, WindowControl wControl, Form rootForm, MemberSearchConfig config) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_search", rootForm);
		setTranslator(userManager.getPropertyHandlerTranslator(Util
				.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator())));
		rootForm.setHideDirtyMarkingMessage(true);
		this.config = config;

		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, config.repositoryEntry());
		coach = reSecurity.isCoach();
		owner = reSecurity.isEntryAdmin();
		
		initForm(ureq);
		loadModel();
		preselectRows();
	}
	
	public MemberSearchController(UserRequest ureq, WindowControl wControl, MemberSearchConfig config) {
		super(ureq, wControl, "member_search");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.config = config;

		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, config.repositoryEntry());
		coach = reSecurity.isCoach();
		owner = reSecurity.isEntryAdmin();
		
		initForm(ureq);
		mainForm.setHideDirtyMarkingMessage(true);
		loadModel();
		preselectRows();
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public List<Identity> getSelectedIdentities() {
		Set<Integer> selectedIndexSet = tableEl.getMultiSelectedIndex();
		List<Long> selectedIdentitiesKey = new ArrayList<>();
		for(Integer selectedIndex:selectedIndexSet) {
			MemberRow row = tableModel.getObject(selectedIndex.intValue());
			selectedIdentitiesKey.add(row.getIdentityKey());
		}
		return selectedIdentitiesKey.isEmpty()
				? List.of()
				: securityManager.loadIdentityByKeys(selectedIdentitiesKey);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTableForm(formLayout, ureq);
		if(config.showSelectButton()) {
			initButtonsForm(formLayout, ureq);
		}
	}
	
	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MembersCols.id));
		}
		
		DefaultFlexiColumnModel userPortraitColumn = new DefaultFlexiColumnModel(MembersCols.userPortrait);
		userPortraitColumn.setExportable(false);
		userPortraitColumn.setIconHeader("o_icon o_ac_guest_icon");
		userPortraitColumn.setHeaderTooltip(translate("table.header.user.portrait"));
		columnsModel.addFlexiColumnModel(userPortraitColumn);
		
		int colPos = USER_PROPS_OFFSET;
		String action = config.singleSelection() ? CMD_SELECT : null;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			
			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, action, true, propName,
						new StaticFlexiCellRenderer(action, new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MembersCols.role,
				new CourseRoleCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MembersCols.origin,
				new MemberOriginCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MembersCols.creationDate,
				new DateFlexiCellRenderer(getLocale())));
		
		tableModel = new MemberSearchTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(true);
		tableEl.setMultiSelect(config.multiSelection());
		tableEl.setSelectAllEnable(config.multiSelection());
		tableEl.setAndLoadPersistedPreferences(ureq, config.prefsId());
		
		initFilters();
		initFiltersPresets(ureq);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		if(config.withOwners() && owner) {
			FlexiFiltersTab ownersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(GroupRoles.owner.name(), translate("role.repo.owner"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(GroupRoles.owner.name()))));
			ownersTab.setFiltersExpanded(true);
			tabs.add(ownersTab);
		}
		if(config.withCoaches() && (owner || coach)) {
			FlexiFiltersTab coachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(GroupRoles.coach.name(), translate("role.repo.tutor"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(GroupRoles.coach.name()))));
			coachesTab.setFiltersExpanded(true);
			tabs.add(coachesTab);
		}
		if(config.withParticipants()) {
			FlexiFiltersTab participantsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(GroupRoles.participant.name(), translate("role.repo.participant"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(GroupRoles.participant.name()))));
			participantsTab.setFiltersExpanded(true);
			tabs.add(participantsTab);
		}

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues rolesValues = new SelectionValues();
		if(config.withOwners() && owner) {
			rolesValues.add(SelectionValues.entry(GroupRoles.owner.name(), translate("role.repo.owner")));
		}
		if(config.withCoaches() && (owner || coach)) {
			rolesValues.add(SelectionValues.entry(GroupRoles.coach.name(), translate("role.repo.tutor")));
		}
		if(config.withParticipants()) {
			rolesValues.add(SelectionValues.entry(GroupRoles.participant.name(), translate("role.repo.participant")));
		}
		if(config.withWaiting() && (owner || coach)) {
			rolesValues.add(SelectionValues.entry(GroupRoles.waiting.name(), translate("role.group.waiting")));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.role"),
				FILTER_ROLE, rolesValues, true));
		
		// User origins
		SelectionValues originValues = new SelectionValues();
		originValues.add(SelectionValues.entry(Origin.repositoryEntry.name(), translate("filter.origin.repo")));
		originValues.add(SelectionValues.entry(Origin.businessGroup.name(), translate("filter.origin.group")));
		originValues.add(SelectionValues.entry(Origin.curriculum.name(), translate("filter.origin.curriculum")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.origin"),
				FILTER_ORIGIN, originValues, true));

		// User types
		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(UserType.user.name(), translate("filter.user.type.registered")));
		typeValues.add(SelectionValues.entry(UserType.invitee.name(), translate("filter.user.type.invitee")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.user.type"),
				FILTER_TYPE, typeValues, true));
		
		// groups
		SelectionValues groupValues = new SelectionValues();
		if(owner || coach) {
			SearchBusinessGroupParams params;
			if(owner) {
				params = new SearchBusinessGroupParams();
			} else {
				params = new SearchBusinessGroupParams(getIdentity(), true, false);
			}
			List<BusinessGroup> coachedGroups = businessGroupService.findBusinessGroups(params, config.repositoryEntry(), 0, -1);
			if(coachedGroups != null && !coachedGroups.isEmpty()) {
				for(BusinessGroup coachedGroup:coachedGroups) {
					String groupName = StringHelper.escapeHtml(coachedGroup.getName());
					groupValues.add(new SelectionValue("businessgroup-" + coachedGroup.getKey(), groupName, null,
							"o_icon o_icon_group", null, true));
				}
			}
			
			List<CurriculumElement> coachedCurriculumElements;
			if(owner) {
				coachedCurriculumElements = curriculumService.getCurriculumElements(config.repositoryEntry());
			} else {
				coachedCurriculumElements = curriculumService.getCurriculumElements(config.repositoryEntry(), getIdentity(), List.of(CurriculumRoles.coach));
			}
			
			if(!coachedCurriculumElements.isEmpty()) {
				for(CurriculumElement coachedCurriculumElement:coachedCurriculumElements) {
					String name = StringHelper.escapeHtml(CurriculumHelper.getLabel(coachedCurriculumElement, getTranslator()));
					groupValues.add(new SelectionValue("curriculumelement-" + coachedCurriculumElement.getKey(), name, null,
							"o_icon o_icon_curriculum_element", null, true));
				}
			}
		}
		
		if(!groupValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.groups"),
					AssessedIdentityListState.FILTER_GROUPS, groupValues, true));
		}

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initButtonsForm(FormItemContainer formLayout, UserRequest ureq) {
		uifactory.addFormSubmitButton("select", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void preselectRows() {
		Collection<Long> preselectedKeys = config.preselectedIdentitiesKeys();
		if(preselectedKeys != null && !preselectedKeys.isEmpty()) {
			List<Integer> selectedIndexList = tableModel.getIndexes(preselectedKeys);
			tableEl.setMultiSelectedIndex(new HashSet<>(selectedIndexList));
		}
	}
	
	private void loadModel() {
		RepositoryEntry entry = config.repositoryEntry();
		SearchMembersParams params = getSearchParameters();
		List<MemberView> members = memberQueries.getRepositoryEntryMembers(entry, params);
		if(config.identitiesList() != null) {
			Set<Long> identitiesSet = config.identitiesList()
					.stream().map(Identity::getKey).collect(Collectors.toSet());
			members = members.stream()
					.filter(member -> identitiesSet.contains(member.getKey()))
					.toList();
		}
		
		List<MemberRow> rows = new ArrayList<>();
		for(MemberView member:members) {
			MemberRow row = forgeRow(member, member.getIdentity());
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		tableEl.clearError();
	}
	
	private MemberRow forgeRow(MemberView member, Identity identity) {
		MemberRow row = new MemberRow(member.getIdentity(), userPropertyHandlers,
				member.getMemberShip(), member.getCreationDate(), getLocale());
		
		String userPortraitId = "portrait_" + row.getIdentityKey();
		Component comp = tableEl.getComponent().getComponent(userPortraitId);
		if(comp instanceof UserPortraitComponent userPortraitComp) {
			row.setPortraitUser(userPortraitComp.getPortraitUser());
			row.setPortraitComponent(userPortraitComp);
		} else {
			PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), identity);
			row.setPortraitUser(portraitUser);
			UserPortraitComponent userPortraitComp = UserPortraitFactory.createUserPortrait(userPortraitId, tableEl, getLocale());
			userPortraitComp.setPortraitUser(portraitUser);
			userPortraitComp.setDisplayPresence(true);
			userPortraitComp.setSize(PortraitSize.small);
			row.setPortraitComponent(userPortraitComp);
		}
		
		return row;
	}
	
	private SearchMembersParams getSearchParameters() {
		SearchMembersParams params = new SearchMembersParams();
		params.setPending(false);
		params.setSearchAsRole(getIdentity(), config.searchAsRole());
		if(config.runningTestSession()) {
			params.setOnlyRunningTestSessions(true);
			params.setRunningTestSessionsSubIdent(config.runningTestSessionSubIdent());
		}

		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, FILTER_ROLE);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> rolesValues = extendedFilter.getValues();
			if(rolesValues != null && !rolesValues.isEmpty()) {
				List<GroupRoles> roles = rolesValues.stream()
						.map(GroupRoles::valueOf)
						.toList();
				params.setRoles(roles.toArray(new GroupRoles[roles.size()]));	
			}
		}
		if(params.getRoles() == null) {
			params.setRoles(new GroupRoles[] { GroupRoles.owner, GroupRoles.coach, GroupRoles.participant } );	
		}
		
		FlexiTableFilter originFilter = FlexiTableFilter.getFilter(filters, FILTER_ORIGIN);
		if (originFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> originValues = extendedFilter.getValues();
			if(originValues != null && !originValues.isEmpty()) {
				List<Origin> roles = originValues.stream()
						.map(Origin::valueOf)
						.toList();
				params.setOrigins(EnumSet.copyOf(roles));
			}
		}
		
		FlexiTableFilter userTypeFilter = FlexiTableFilter.getFilter(filters, FILTER_TYPE);
		if (userTypeFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> userTypesValues = extendedFilter.getValues();
			if(userTypesValues != null && !userTypesValues.isEmpty()) {
				List<UserType> roles = userTypesValues.stream()
						.map(UserType::valueOf)
						.toList();
				params.setUserTypes(EnumSet.copyOf(roles));
			}
		}
		
		List<Long> businessGroupKeys = null;
		List<Long> curriculumElementKeys = null;
		FlexiTableFilter groupsFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_GROUPS);
		if(groupsFilter != null && groupsFilter.isSelected()) {
			businessGroupKeys = new ArrayList<>();
			curriculumElementKeys = new ArrayList<>();
			List<String> filterValues = ((FlexiTableExtendedFilter)groupsFilter).getValues();
			if(filterValues != null) {
				for(String filterValue:filterValues) {
					int index = filterValue.indexOf('-');
					if(index > 0) {
						Long key = Long.valueOf(filterValue.substring(index + 1));
						if(filterValue.startsWith("businessgroup-")) {
							businessGroupKeys.add(key);
						} else if(filterValue.startsWith("curriculumelement-")) {
							curriculumElementKeys.add(key);
						}
					}
				}
			}
		}
		
		params.setBusinessGroupKeys(businessGroupKeys);
		params.setCurriculumElementKeys(curriculumElementKeys);
		
		String searchString = tableEl.getQuickSearchString();
		if(StringHelper.containsNonWhitespace(searchString)) {
			Map<String,String> searchMap = new HashMap<>();
			for(UserPropertyHandler handler: this.userPropertyHandlers) {
				searchMap.put(handler.getName(), searchString);
			}
			params.setUserPropertiesSearch(searchMap);
		}

		return params;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if(tableEl.getMultiSelectedIndex().isEmpty()) {
			tableEl.setErrorKey("error.atleastone");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se && CMD_SELECT.equals(se.getCommand())) {
				MemberRow row = tableModel.getObject(se.getIndex());
				Identity selectedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				fireEvent(ureq, new SingleIdentityChosenEvent(selectedIdentity));
			} else if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Identity> selectedIdentities = getSelectedIdentities();
		if(selectedIdentities == null || selectedIdentities.isEmpty()) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(selectedIdentities.size() == 1) {
			fireEvent(ureq, new SingleIdentityChosenEvent(selectedIdentities.get(0)));
		} else {
			fireEvent(ureq, new MultiIdentityChosenEvent(selectedIdentities));
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
