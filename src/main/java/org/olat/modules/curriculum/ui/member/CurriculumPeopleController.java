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
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.ui.component.OrganisationWithParentsNameComparator;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.MemberImplementationsInfos;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;
import org.olat.modules.curriculum.ui.component.NextEvent;
import org.olat.modules.curriculum.ui.component.PreviousEvent;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.member.CurriculumPeopleTableModel.PeopleCols;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.ResourceReservationsIdentityInfos;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumPeopleController extends FormBasicController implements Activateable2 {

	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = CurriculumElementMemberUsersController.class.getCanonicalName();

	private static final String SELECT_CMD = "select-details";
	protected static final String FILTER_ROLES = "roles";
	protected static final String FILTER_PENDING = "pending";
	protected static final String FILTER_ORGANISATIONS = "organisations";

	private FlexiFiltersTab allTab;
	private final Map<CurriculumRoles,FlexiFiltersTab> rolesToTab = new EnumMap<>(CurriculumRoles.class);

	private FormLink contactBatchButton;
	private FlexiTableElement tableEl;
	private CurriculumPeopleTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;

	private final boolean organisationsEnabled;
	private final CurriculumSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
    private final SelectionValues organisationsKV = new SelectionValues();
	
	private ToolsController toolsCtrl;
	private ContactFormController contactCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private CurriculumMemberOverviewController memberOverviewCtrl;

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;
	
	public CurriculumPeopleController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "people", Util
				.createPackageTranslator(CurriculumManagerRootController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		organisationsEnabled = organisationModule.isEnabled();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = USER_PROPS_OFFSET;
		String defaultSortKey = null;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			String id = "userProp-" + colIndex;
			String name = userPropertyHandler.getName();
			String action = UserConstants.NICKNAME.equals(name) || UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name)
					? SELECT_CMD : null;
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, action, true, id));
			colIndex++;
			if(UserConstants.LASTNAME.equals(name)) {
				defaultSortKey = id;
			}
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PeopleCols.numOfImplementations));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PeopleCols.role,
				new RolesFlexiCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PeopleCols.numOfPendingReservations));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(PeopleCols.tools));
		
		tableModel = new CurriculumPeopleTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
		tableEl.setSelectAllEnable(true);

		contactBatchButton = uifactory.addFormLink("contact", formLayout, Link.BUTTON);
		tableEl.addBatchButton(contactBatchButton);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(defaultSortKey, false));
		tableEl.setSortSettings(sortOptions);
		
		initFilters();
		initFiltersPresets();
		
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-people-v1");
	}
	
    private  void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
    	if(organisationsEnabled) {
    		FlexiTableMultiSelectionFilter participantFilter = new FlexiTableMultiSelectionFilter(translate("filter.organisations"),
    				FILTER_ORGANISATIONS, organisationsKV, true);
    		filters.add(participantFilter);
    	}
		
		SelectionValues rolePK = new SelectionValues();
		rolePK.add(SelectionValues.entry(CurriculumRoles.participant.name(), translate("role.participant")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.coach.name(), translate("role.coach")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.mastercoach.name(), translate("role.mastercoach")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.owner.name(), translate("role.owner")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.curriculumelementowner.name(), translate("role.curriculumelementowner")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.curriculumowner.name(), translate("role.curriculumowner")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.roles"),
				FILTER_ROLES, rolePK, true));
		
		SelectionValues recertificationValues = new SelectionValues();
		recertificationValues.add(SelectionValues.entry(FILTER_PENDING, translate("filter.pending.memberships")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.pending.memberships"),
				FILTER_PENDING, recertificationValues, true));

		tableEl.setFilters(true, filters, true, false);
    }
    
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		FlexiFiltersTab participantsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.participant.name(), translate("search.role.participant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLES,
						List.of(CurriculumRoles.participant.name()))));
		tabs.add(participantsTab);
		rolesToTab.put(CurriculumRoles.participant, participantsTab);
		
		FlexiFiltersTab pendingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FILTER_PENDING, translate("search.pending"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_PENDING, FILTER_PENDING)));
		tabs.add(pendingTab);
		
		FlexiFiltersTab coachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.coach.name(), translate("search.role.coach"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLES,
						List.of(CurriculumRoles.coach.name()))));
		tabs.add(coachesTab);
		rolesToTab.put(CurriculumRoles.coach, coachesTab);
		
		FlexiFiltersTab masterCoachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.mastercoach.name(), translate("search.role.mastercoach"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLES,
						List.of(CurriculumRoles.mastercoach.name()))));
		tabs.add(masterCoachesTab);
		rolesToTab.put(CurriculumRoles.mastercoach, masterCoachesTab);
		
		FlexiFiltersTab ownersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.owner.name(), translate("search.role.course.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLES,
						List.of(CurriculumRoles.owner.name()))));
		tabs.add(ownersTab);
		rolesToTab.put(CurriculumRoles.owner, ownersTab);
		
		FlexiFiltersTab curriculumElementOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.curriculumelementowner.name(), translate("search.role.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLES,
						List.of(CurriculumRoles.curriculumelementowner.name()))));
		tabs.add(curriculumElementOwnersTab);
		rolesToTab.put(CurriculumRoles.curriculumelementowner, curriculumElementOwnersTab);
		
		FlexiFiltersTab curriculumOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.curriculumowner.name(), translate("search.role.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLES,
						List.of(CurriculumRoles.curriculumowner.name()))));
		tabs.add(curriculumOwnersTab);
		rolesToTab.put(CurriculumRoles.curriculumowner, curriculumOwnersTab);

		tableEl.setFilterTabs(true, tabs);
	}
    
    private void filterModel() {
    	tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
    	tableEl.reset(true, true, true);
    }
	
	private void loadModel() {
		SearchMemberParameters params = new SearchMemberParameters(List.of());
		List<String> managerRoles = List.of(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				CurriculumRoles.curriculummanager.name(), CurriculumRoles.curriculumowner.name());
		List<String> membersRoles = List.of(CurriculumRoles.values()).stream()
				.map(CurriculumRoles::name).toList();
		List<MemberImplementationsInfos> members = curriculumService.getMembers(params, getIdentity(), managerRoles, membersRoles);
		List<CurriculumPeopleRow> rows = new ArrayList<>();
		for(MemberImplementationsInfos member:members) {
			CurriculumPeopleRow row = new CurriculumPeopleRow(member.getIdentity(),
					member.getRoles(), member.getImplementationsKeys().size(), userPropertyHandlers, getLocale());
			forgeLinks(row);
			rows.add(row);
		}
		
		// Add future members which only have a reservation
		Map<Long,CurriculumPeopleRow> rowsMap = rows.stream()
				.collect(Collectors.toMap(CurriculumPeopleRow::getIdentityKey, r -> r, (u, _) -> u));
		List<ResourceReservationsIdentityInfos> reservations = acService.getCurriculumReservationsManagedBy(getIdentity());
		List<Long> pendingMembersKeys = reservations.stream()
				.filter(r -> !rowsMap.containsKey(r.identityKey()))
				.map(ResourceReservationsIdentityInfos::identityKey)
				.toList();
		List<Identity> pendingMembers = securityManager.loadIdentityByKeys(pendingMembersKeys);
		for(Identity pendingMember:pendingMembers) {
			CurriculumPeopleRow row = rowsMap.get(pendingMember.getKey());
			if(row == null) {
				row = new CurriculumPeopleRow(pendingMember, List.of(), 0, userPropertyHandlers, getLocale());
				rows.add(row);
				rowsMap.put(row.getIdentityKey(), row);
			}
		}
		
		for(ResourceReservationsIdentityInfos reservation:reservations) {
			CurriculumPeopleRow row = rowsMap.get(reservation.identityKey());
			if(row != null) {
				row.addNumOfReservations(reservation.numOfReservations());
			}
		}
		
		if(organisationsEnabled) {
			identityPowerSearchQueries.appendOrganisations(rows);
			loadOrganisationsFilterFromModel(rows);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeLinks(CurriculumPeopleRow row) {
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
    private void loadOrganisationsFilterFromModel(List<CurriculumPeopleRow> entries) {
    	Set<OrganisationWithParents> organisationsSet = new HashSet<>();
		for(CurriculumPeopleRow entry:entries) {
			if(entry.getOrganisations() != null) {
				organisationsSet.addAll(entry.getOrganisations());
			}
		}
		
		List<OrganisationWithParents> organisationsList = new ArrayList<>(organisationsSet);
		if(organisationsList.size() > 1) {
			Collections.sort(organisationsList, new OrganisationWithParentsNameComparator(getLocale()));
		}
    	for(OrganisationWithParents organisation:organisationsList) {
			organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(),
					StringHelper.escapeHtml(organisation.getDisplayName())));
		}
    }
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Member".equalsIgnoreCase(type)) {
			Long memberKey = entries.get(0).getOLATResourceable().getResourceableId();
			activateMember(ureq, memberKey);
		} else if(CurriculumRoles.isValueOf(type.toLowerCase())) {
			FlexiFiltersTab tab = rolesToTab.get(CurriculumRoles.valueOf(type.toLowerCase()));
			if(tab == null) {
				tab = allTab;
			}
			tableEl.setSelectedFilterTab(ureq, tab);
			filterModel();
		} else if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			filterModel();
		}
	}
	
	private void activateMember(UserRequest ureq, Long memberKey) {
		CurriculumPeopleRow row = tableModel.getObjectByKey(memberKey);
		if(row != null) {
			doSelectMember(ureq, row);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(memberOverviewCtrl == source) {
			if(event instanceof NextEvent ne) {
				doNextMember(ureq, ne.getIdentity());
			} else if(event instanceof PreviousEvent pe) {
				doPreviousMember(ureq, pe.getIdentity());
			} else if(event instanceof ActivateEvent) {
				fireEvent(ureq, event);
			}
		} else if(contactCtrl == source) {
			toolbarPanel.popController(contactCtrl);
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(calloutCtrl == source) {
			cleanUp();
		} else if(event instanceof ActivateEvent) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		calloutCtrl = null;
		contactCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(SELECT_CMD.equals(se.getCommand())) {
					CurriculumPeopleRow row = tableModel.getObject(se.getIndex());
					doSelectMember(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(contactBatchButton == source) {
			doOpenContact(ureq);
		} else if(source instanceof FormLink link && link.getUserObject() instanceof CurriculumPeopleRow row) {
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void doOpenTools(UserRequest ureq, CurriculumPeopleRow member, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), member);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	protected void doOpenContact(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities();
		if(!identities.isEmpty()) {
			doOpenContact(ureq, identities);
		}
	}
	
	private List<Identity> getSelectedIdentities() {
		List<Long> identitiesKeys = new ArrayList<>();
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		for(Integer index:selectedIndexes) {
			CurriculumPeopleRow row = tableModel.getObject(index.intValue());
			if(row != null) {
				identitiesKeys.add(row.getIdentityKey());
			}
		}
		return identitiesKeys.isEmpty()
				? List.of()
				: securityManager.loadIdentityByKeys(identitiesKeys);
	}
	
	private void doOpenContact(UserRequest ureq, CurriculumPeopleRow member) {
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		if(identity != null) {
			doOpenContact(ureq, List.of(identity));
		}
	}
	
	private void doOpenContact(UserRequest ureq, List<Identity> members) {
		removeAsListenerAndDispose(contactCtrl);
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList emailList = new ContactList("");
		emailList.addAllIdentites(members);
		cmsg.addEmailTo(emailList);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("Contact");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactCtrl = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactCtrl);

		toolbarPanel.pushController(translate("contact.title"), contactCtrl);
	}
	
	private void doNextMember(UserRequest ureq, Identity member) {
		CurriculumPeopleRow currentMemberRow = tableModel.getObjectByKey(member.getKey());
		if(currentMemberRow != null) {
			CurriculumPeopleRow nextMemberRow = tableModel.getNext(currentMemberRow);
			doSelectMember(ureq, nextMemberRow);
		}
	}
	
	private void doPreviousMember(UserRequest ureq, Identity member) {
		CurriculumPeopleRow currentMemberRow = tableModel.getObjectByKey(member.getKey());
		if(currentMemberRow != null) {
			CurriculumPeopleRow previousMemberRow = tableModel.getPrevious(currentMemberRow);
			doSelectMember(ureq, previousMemberRow);
		}
	}
	
	private void doSelectMember(UserRequest ureq, CurriculumPeopleRow memberRow) {
		toolbarPanel.popController(memberOverviewCtrl);
		removeAsListenerAndDispose(memberOverviewCtrl);
		memberOverviewCtrl = null;
		
		if(memberRow != null) {
			Identity member = securityManager.loadIdentityByKey(memberRow.getIdentityKey());
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Member", member.getKey());
			WindowControl bwControl = addToHistory(ureq, ores, null);
			memberOverviewCtrl = new CurriculumMemberOverviewController(ureq, bwControl, toolbarPanel, member, secCallback);
			listenTo(memberOverviewCtrl);
	
			String fullname = userManager.getUserDisplayName(member);
			toolbarPanel.pushController(fullname, memberOverviewCtrl);
		}
	}
	
	private class ToolsController extends BasicController {
	
		private final Link contactLink;
		private final VelocityContainer mainVC;
		
		private CurriculumPeopleRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumPeopleRow member) {
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
