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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMember;
import org.olat.modules.curriculum.ui.CurriculumElementCalendarController;
import org.olat.modules.curriculum.ui.CurriculumElementListController;
import org.olat.modules.curriculum.ui.CurriculumStructureCalloutController;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementRowEvent;
import org.olat.modules.curriculum.ui.member.CurriculumMemberImplementationsTableModel.ImplementationCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMemberImplementationsController extends FormBasicController {

	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";

	protected static final String FILTER_TYPE = "type";
	protected static final String FILTER_ROLES = "roles";
	protected static final String FILTER_STATUS = "status";
	protected static final String FILTER_PERIOD = "period";
	protected static final String FILTER_CURRICULUMS = "curriculums";
	
	protected static final String SELECT_CMD = "select";
	private static final String SELECT_CALENDAR_CMD = "calendar";
	private static final String SELECT_STRUCTURE_CMD = "structure";
	protected static final String SELECT_CURRICULUM_CMD = "selectCURRICULUM";

	private FlexiFiltersTab relevantTab;
    private final SelectionValues curriculumsKV = new SelectionValues();
    
	private FlexiTableElement tableEl;
	private final TooledStackedPanel toolbarPanel;
	private CurriculumMemberImplementationsTableModel tableModel; 
	
	private final Identity member;
	private final CurriculumSecurityCallback secCallback;

	private CurriculumElementCalendarController calendarsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CurriculumStructureCalloutController curriculumStructureCalloutCtrl;

	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumMemberImplementationsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Identity member, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "member_implementations", Util
				.createPackageTranslator(CurriculumElementListController.class, ureq.getLocale()));
		this.member = member;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImplementationCols.key));
		DefaultFlexiColumnModel displayNameCol = new DefaultFlexiColumnModel(ImplementationCols.displayName, SELECT_CMD);
		displayNameCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(displayNameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.structure));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImplementationCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.curriculum, SELECT_CURRICULUM_CMD));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.beginDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.endDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.roles,
				new RolesFlexiCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationCols.allRoles,
				new RolesFlexiCellRenderer(getTranslator())));
		DefaultFlexiColumnModel calendarsCol = new DefaultFlexiColumnModel(ImplementationCols.calendars);
		calendarsCol.setIconHeader("o_icon o_icon-lg o_icon_calendar");
		columnsModel.addFlexiColumnModel(calendarsCol);
		DefaultFlexiColumnModel openCol = new DefaultFlexiColumnModel(ImplementationCols.start);
		openCol.setAlwaysVisible(true);
		openCol.setIconHeader("o_icon o_icon_arrow_up_right_from_square");
		openCol.setHeaderTooltip(translate("open.new.tab"));
		columnsModel.addFlexiColumnModel(openCol);
		
		tableModel = new CurriculumMemberImplementationsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_cu_implementations");
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.curriculum.empty")
				.withIconCss("o_icon_curriculum_element")
				.build());
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-people-implementations-v1");

		initFilters();
		initFiltersPresets();
		initSorters();
		
		tableEl.setSelectedFilterTab(ureq, relevantTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
				FILTER_CURRICULUMS, curriculumsKV, true);
		filters.add(curriculumFilter);
		
		SelectionValues rolePK = new SelectionValues();
		rolePK.add(SelectionValues.entry(CurriculumRoles.participant.name(), translate("role.participant")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.coach.name(), translate("role.coach")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.mastercoach.name(), translate("role.mastercoach")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.owner.name(), translate("role.owner")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.curriculumelementowner.name(), translate("role.curriculumelementowner")));
		rolePK.add(SelectionValues.entry(CurriculumRoles.curriculumowner.name(), translate("role.curriculumowner")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.roles"),
				FILTER_ROLES, rolePK, true));
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		SelectionValues typesValues = new SelectionValues();
		for(CurriculumElementType type:types) {
			typesValues.add(SelectionValues.entry(type.getKey().toString(), StringHelper.escapeHtml(type.getDisplayName())));
		}
		typesValues.sort(SelectionValues.VALUE_ASC);
		FlexiTableMultiSelectionFilter typeFilter = new FlexiTableMultiSelectionFilter(translate("filter.types"),
				FILTER_TYPE, typesValues, true);
		filters.add(typeFilter);
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, false);
		filters.add(statusFilter);
		
		filters.add(new FlexiTableDateRangeFilter(translate("filter.date.range"), FILTER_PERIOD, true, false,
				getLocale()));

		tableEl.setFilters(true, filters, true, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.preparation.name(), CurriculumElementStatus.provisional.name(),
								CurriculumElementStatus.confirmed.name(), CurriculumElementStatus.active.name() ))));
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		
		FlexiFiltersTab finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("finished", translate("filter.finished"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.finished.name()))));
		finishedTab.setFiltersExpanded(true);
		tabs.add(finishedTab);
		
		tableEl.setFilterTabs(true, tabs);
	}

	private void initSorters() {
		List<FlexiTableSort> sorters = new ArrayList<>(14);
		sorters.add(new FlexiTableSort(translate("orderby.relevance"), ImplementationCols.relevance.name()));
		sorters.add(FlexiTableSort.SPACER);
		sorters.add(new FlexiTableSort(translate(ImplementationCols.key.i18nHeaderKey()), ImplementationCols.key.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.displayName.i18nHeaderKey()), ImplementationCols.displayName.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.identifier.i18nHeaderKey()), ImplementationCols.identifier.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.curriculum.i18nHeaderKey()), ImplementationCols.curriculum.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.beginDate.i18nHeaderKey()), ImplementationCols.beginDate.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.endDate.i18nHeaderKey()), ImplementationCols.endDate.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.type.i18nHeaderKey()), ImplementationCols.type.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.status.i18nHeaderKey()), ImplementationCols.status.name()));
		sorters.add(new FlexiTableSort(translate(ImplementationCols.roles.i18nHeaderKey()), ImplementationCols.roles.name()));
		
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(ImplementationCols.relevance.name(), true));
		tableEl.setSortSettings(options);
	}
	
	private void loadModel() {
		List<CurriculumElementMember> elements = curriculumService.getImplementationsOf(getIdentity(), List.of(), member);
		
		Set<Curriculum> curriculums = new HashSet<>();
		Map<Long,CurriculumMemberImplementationRow> rowsMap = new HashMap<>();
		for(CurriculumElementMember elementMember:elements) {
			CurriculumElement element = elementMember.element();
			CurriculumElement implementation = element.getImplementation() != null
					? element.getImplementation()
					: element;

			CurriculumMemberImplementationRow row = rowsMap
					.computeIfAbsent(implementation.getKey(), _ -> new CurriculumMemberImplementationRow(implementation));
			if(element.getImplementation() == null) {
				row.addToImplementationRoles(elementMember.role());
			}
			row.addToAllRoles(elementMember.role());
			curriculums.add(element.getCurriculum());
		}

		List<CurriculumMemberImplementationRow> rows = new ArrayList<>(rowsMap.values());
		for(CurriculumMemberImplementationRow row:rows) {
			forgeRow(row);
		}
		loadCurriculumsFilter(curriculums);
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void loadCurriculumsFilter(Set<Curriculum> curriculums) {
		for(Curriculum cur:curriculums) {
			String key = cur.getKey().toString();
			String value = StringHelper.escapeHtml(cur.getDisplayName());
			if(StringHelper.containsNonWhitespace(cur.getIdentifier())) {
				value += " <small class=\"mute\"> \u00B7 " + StringHelper.escapeHtml(cur.getIdentifier()) + "</small>";
			}
			curriculumsKV.add(SelectionValues.entry(key, value));
		}
	}
	
	private void forgeRow(CurriculumMemberImplementationRow row) {
		String id = row.getCurriculumElement().getKey().toString();
		
		CurriculumElementType type = row.getCurriculumElementType();
		if(type == null || !type.isSingleElement()) {
			FormLink structureLink = uifactory.addFormLink("structure_".concat(id), SELECT_STRUCTURE_CMD, "", null, null, Link.NONTRANSLATED);
			structureLink.setAriaDialogOpener();
			structureLink.setIconLeftCSS("o_icon o_icon-lg o_icon_structure");
			structureLink.setTitle(translate("action.structure"));
			structureLink.setUserObject(row);
			row.setStructureLink(structureLink);
		}
		
		if(row.isCalendarsEnabled()) {
			FormLink calendarsLink = uifactory.addFormLink("cals_".concat(id), SELECT_CALENDAR_CMD, "", null, null, Link.LINK | Link.NONTRANSLATED);
			calendarsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_calendar");
			calendarsLink.setTitle(translate("calendars"));
			row.setCalendarsLink(calendarsLink);
			calendarsLink.setUserObject(row);
		}
		
		String path = "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + row.getCurriculum().getKey() + "][Implementations:0][CurriculumElement:" + row.getCurriculumElement().getKey() + "][Overview:0]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
		ExternalLinkItem openLink = uifactory.addExternalLink(id, id, url, "_blank", flc);
		openLink.setIconLeftCSS("o_icon o_icon_arrow_up_right_from_square");
		openLink.setTooltip(translate("open.new.tab"));
		row.setOpenLink(openLink);
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (curriculumStructureCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectCurriculumElementRowEvent scee) {
				doOpenCurriculumElementDetails(ureq, scee.getCurriculumElement());
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(curriculumStructureCalloutCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		curriculumStructureCalloutCtrl = null;
		toolsCalloutCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(SELECT_CMD.equals(cmd)) {
					CurriculumMemberImplementationRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementDetails(ureq, row.getCurriculumElement());
				} else if(SELECT_CURRICULUM_CMD.equals(cmd)) {
					CurriculumMemberImplementationRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculun(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if(SELECT_STRUCTURE_CMD.equals(cmd)
					&& link.getUserObject() instanceof  CurriculumMemberImplementationRow row) {
				doOpenStructure(ureq, row, link);
			} else if(SELECT_CALENDAR_CMD.equals(cmd)
					&& link.getUserObject() instanceof  CurriculumMemberImplementationRow row) {
				doOpenCalendars(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenCalendars(UserRequest ureq, CurriculumMemberImplementationRow row) {
		removeAsListenerAndDispose(calendarsCtrl);
		toolbarPanel.popController(calendarsCtrl);
		
		CurriculumElement element = row.getCurriculumElement();
		OLATResourceable ores = OresHelper.createOLATResourceableType("Calendars");
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntriesWithDescendants(element);
		calendarsCtrl = new CurriculumElementCalendarController(ureq, bwControl, element, entries, secCallback);
		listenTo(calendarsCtrl);
		toolbarPanel.pushController(translate("calendars"), calendarsCtrl);
	}
	
	private void doOpenStructure(UserRequest ureq, CurriculumMemberImplementationRow row, FormLink link) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		curriculumStructureCalloutCtrl = new CurriculumStructureCalloutController(ureq, getWindowControl(),
				curriculumElement, null, false, secCallback);
		listenTo(curriculumStructureCalloutCtrl);
		
		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true,  null);
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				curriculumStructureCalloutCtrl.getInitialComponent(), link, "", true, "", settings);
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenCurriculumElementDetails(UserRequest ureq, CurriculumElement element) {
		String path = "[Implementations:0][CurriculumElement:" + element.getKey() + "][Overview:0]";
		doOpen(ureq, path);
	}

	private void doOpenCurriculun(UserRequest ureq, CurriculumMemberImplementationRow row) {
		String path = "[Curriculums:0][Curriculum:" + row.getCurriculum().getKey() + "]";
		doOpen(ureq, path);
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(businessPath);
		fireEvent(ureq, new ActivateEvent(entries));
	}
}
