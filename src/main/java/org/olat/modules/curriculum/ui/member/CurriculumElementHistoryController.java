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
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.member.MemberHistoryDetailsTableModel.MemberHistoryCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementHistoryController extends FormBasicController implements Activateable2 {
	
	private static final String ALL_TAB_ID = "all";
	private static final String LAST_7_DAYS_TAB_ID = "7days";
	private static final String LAST_4_WEEKS_TAB_ID = "4weeks";
	private static final String LAST_12_MONTHS_TAB_ID = "12months";
	
	protected static final String FILTER_DATE = "date";
	
	private FlexiFiltersTab allTab;
	
	private FlexiTableElement tableEl;
	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private MemberHistoryDetailsTableModel tableModel;
	
	private final CurriculumElement curriculumElement;

	private List<CurriculumElement> descendants;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementHistoryController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl, "curriculum_element_history", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));

		this.curriculumElement = curriculumElement;
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
		
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberHistoryCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.member));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.role,
				new CurriculumMembershipCellRenderer(getTranslator(), ", ")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.activity));
		GroupMembershipStatusRenderer memberStatusRenderer = new GroupMembershipStatusRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.originalValue,
				memberStatusRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.newValue,
				memberStatusRenderer));
		DefaultFlexiColumnModel noteCol = new DefaultFlexiColumnModel(MemberHistoryCols.note);
		noteCol.setIconHeader("o_icon o_icon_notes");
		columnsModel.addFlexiColumnModel(noteCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.actor));
		
		tableModel = new MemberHistoryDetailsTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MemberHistoryCols.creationDate.name(), false));
		sortOptions.setFromColumnModel(true);
		tableEl.setSortSettings(sortOptions);
		
		initFilters();
		initFiltersPresets();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		FlexiTableDateRangeFilter dateFilter = new FlexiTableDateRangeFilter(translate("filter.date"), FILTER_DATE,
				true, true, translate("filter.date.from"), translate("filter.date.to"), getLocale());
		filters.add(dateFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		tabs.add(allTab);

		Date now = DateUtils.getStartOfDay(new Date());
		DateRange last7Days = new DateRange();
		last7Days.setEnd(DateUtils.addDays(now, -7));
		FlexiFiltersTab last7DaysTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_7_DAYS_TAB_ID, translate("filter.last.7.days"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last7Days)));
		tabs.add(last7DaysTab);
		
		DateRange last4Weeks = new DateRange();
		last4Weeks.setEnd(DateUtils.addWeeks(now, -4));
		FlexiFiltersTab last4WeeksTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_4_WEEKS_TAB_ID, translate("filter.last.4.weeks"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last4Weeks)));
		tabs.add(last4WeeksTab);

		DateRange last12Months = new DateRange();
		last12Months.setEnd(DateUtils.addMonth(now, -12));
		FlexiFiltersTab last12MonthsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_12_MONTHS_TAB_ID, translate("filter.last.12.months"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last12Months)));
		tabs.add(last12MonthsTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	private void loadModel(boolean reset) {
		List<CurriculumElement> elements = getSearchCurriculumElements();
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setElements(elements);
		List<CurriculumElementMembershipHistory> membershipsHistory = curriculumService.getCurriculumElementMembershipsHistory(searchParams);
		List<MemberHistoryDetailsRow> rows = new ArrayList<>();
		for(CurriculumElementMembershipHistory elementHistory:membershipsHistory) {
			List<GroupMembershipHistory> points = elementHistory.getHistory();
			for(GroupMembershipHistory point:points) {
				rows.add(forgeRow(point));
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}
	
	private MemberHistoryDetailsRow forgeRow(GroupMembershipHistory point) {
		Identity user = point.getIdentity();
		String userDisplayName = userManager.getUserDisplayName(user);
		MemberHistoryDetailsRow row = new MemberHistoryDetailsRow(user, userDisplayName, point);

		Identity actor = point.getCreator();
		if(actor != null) {
			String actorDisplayName = userManager.getUserDisplayName(actor);
			row.setActorDisplayName(actorDisplayName);
		}
		
		return row;
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

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allLevelsButton == source) {
			doToggleLevels(false);
		} else if(thisLevelButton == source) {
			doToggleLevels(true);
		} else if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
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
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
	}
	
	private void doToggleLevels(boolean thisLevel) {
		allLevelsButton.setPrimary(!thisLevel);
		thisLevelButton.setPrimary(thisLevel);
		loadModel(true);
	}
}
