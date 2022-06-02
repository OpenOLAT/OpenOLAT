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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.manager.SearchPracticeItemParametersHelper;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.PracticeComposeTableModel.ComposeCols;
import org.olat.course.nodes.practice.ui.events.StartPracticeEvent;
import org.olat.course.nodes.practice.ui.renders.LevelCircleCellRenderer;
import org.olat.course.nodes.practice.ui.renders.PercentCellRenderer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeComposeSerieController extends FormBasicController {

	public static final String ALL_TAB_ID = "All";
	public static final String ALL_NOT_PRACTICED_ID = "NotPracticed";
	public static final String NOT_ASSIGNED = "notassigned";
	
	public static final String FILTER_LEVEL = "level";
	public static final String FILTER_CORRECT = "correct";
	public static final String FILTER_TAXONOMY = "taxonomy";
	
	private FormLink newSerieButton;
	private FlexiTableElement tableEl;
	private PracticeComposeTableModel tableModel;

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab notPracticedTab;
	
	private final int numOfLevels;
	private final RepositoryEntry courseEntry;
	private final PracticeCourseNode courseNode;
	private final List<PracticeResource> resources;
	private final List<TaxonomyLevel> descendantLevels;
	
	@Autowired
	private PracticeService practiceService;
	
	public PracticeComposeSerieController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode) {
		super(ureq, wControl, "compose");
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		numOfLevels = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_LEVELS, 3);
		
		resources = practiceService.getResources(courseEntry, courseNode.getIdent());
		
		List<Long> selectedLevels = courseNode.getModuleConfiguration()
				.getList(PracticeEditController.CONFIG_KEY_FILTER_TAXONOMY_LEVELS, Long.class);
		if(selectedLevels != null && !selectedLevels.isEmpty()) {
			descendantLevels = practiceService.getTaxonomyWithDescendants(selectedLevels);
		} else {
			descendantLevels = null;
		}
	
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableModel = new PracticeComposeTableModel(columnsModel, numOfLevels, getLocale());

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ComposeCols.question));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ComposeCols.level,
				new LevelCircleCellRenderer(numOfLevels)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ComposeCols.correct,
				new PercentCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ComposeCols.lastAttempts));
		
		DefaultFlexiColumnModel playCol = new DefaultFlexiColumnModel("play", -1, "play",
				new StaticFlexiCellRenderer("", "play", "o_practice_play", "o_icon o_icon_start", null));
		columnsModel.addFlexiColumnModel(playCol);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setCustomizeColumns(false);
		tableEl.setSearchEnabled(true);
		
		newSerieButton = uifactory.addFormLink("new.custom.serie", formLayout, Link.BUTTON);
		tableEl.addBatchButton(newSerieButton);
		
		initFilters();
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "practice-serie-composer");
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// Taxonomy levels
		if(descendantLevels != null && !descendantLevels.isEmpty()) {
			SelectionValues taxonomyLevelsValues = new SelectionValues();
			for(TaxonomyLevel level:descendantLevels) {
				taxonomyLevelsValues.add(SelectionValues.entry(level.getKey().toString(), level.getDisplayName()));
			}
			taxonomyLevelsValues.add(SelectionValues.entry(NOT_ASSIGNED, translate("wo.taxonomy.level.label")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.taxonomy.level.label"),
					FILTER_TAXONOMY, taxonomyLevelsValues, true));
		}
		
		// Level
		SelectionValues levelValues = new SelectionValues();
		for(int i=0; i<=numOfLevels; i++) {
			String level = Integer.toString(i);
			levelValues.add(SelectionValues.entry(level, translate("filter.level", level)));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.level.label"),
				FILTER_LEVEL, levelValues, true));
		
		// Correct
		SelectionValues correctValues = new SelectionValues();
		correctValues.add(SelectionValues.entry("0-25", translate("filter.correct", "0", "25")));
		correctValues.add(SelectionValues.entry("25-50", translate("filter.correct", "25", "50")));
		correctValues.add(SelectionValues.entry("50-75", translate("filter.correct", "50", "75")));
		correctValues.add(SelectionValues.entry("75-100", translate("filter.correct", "75", "100")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.correct.label"),
				FILTER_CORRECT, correctValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private final void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.reloadData, List.of());
		allTab.setElementCssClass("o_sel_practice_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		notPracticedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_NOT_PRACTICED_ID, translate("filter.not.practiced"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(PlayMode.newQuestions, "notPracticed")));
		notPracticedTab.setFiltersExpanded(true);
		tabs.add(notPracticedTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void loadModel() {
		SearchPracticeItemParameters searchParams = getSearchParams();
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, -1, getLocale());
		List<PracticeAssessmentItemGlobalRef> globalRefs = practiceService.getPracticeAssessmentItemGlobalRefs(items, getIdentity());
		Map<String,PracticeAssessmentItemGlobalRef> globalRefsMap = globalRefs.stream()
				.collect(Collectors.toMap(PracticeAssessmentItemGlobalRef::getIdentifier, ref -> ref, (u, v) -> u));
		
		List<PracticeComposeItemRow> rows = new ArrayList<>(items.size());
		for(PracticeItem item:items) {
			PracticeAssessmentItemGlobalRef globalRef = null;
			if(item.getIdentifier() != null) {
				globalRef = globalRefsMap.get(item.getIdentifier());
			}
			rows.add(new PracticeComposeItemRow(item, globalRef));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
	}
	
	private void filterModel(List<FlexiTableFilter> filters) {
		String searchString = tableEl.getQuickSearchString();
		boolean notAnswered = tableEl.getSelectedFilterTab() == notPracticedTab;
		
		List<Long> levels = new ArrayList<>();
		FlexiTableFilter levelFilter = FlexiTableFilter.getFilter(filters, FILTER_LEVEL);
		if(levelFilter != null) {
			levels = ((FlexiTableMultiSelectionFilter)levelFilter).getLongValues();
		}
		
		Double correctFrom = null;
		Double correctTo = null;
		FlexiTableFilter correctFilter = FlexiTableFilter.getFilter(filters, FILTER_CORRECT);
		if(correctFilter != null) {
			String filterVal = correctFilter.getValue();
			if(StringHelper.containsNonWhitespace(filterVal)) {
				int index = filterVal.indexOf('-');
				correctFrom = Double.parseDouble(filterVal.substring(0, index)) / 100.0d;
				correctTo = Double.parseDouble(filterVal.substring(index + 1)) / 100.0d;
			}
		}
		
		List<String> taxonomyLevelsKeyPath = new ArrayList<>();
		boolean includeWithoutTaxonomy = false;
		FlexiTableFilter taxonomyFilter = FlexiTableFilter.getFilter(filters, FILTER_TAXONOMY);
		if(taxonomyFilter != null) {
			List<String> selectedValues = ((FlexiTableMultiSelectionFilter)taxonomyFilter).getValues();
			taxonomyLevelsKeyPath = toTaxonomyKeyPaths(Set.copyOf(selectedValues));
			includeWithoutTaxonomy = selectedValues.contains(NOT_ASSIGNED);
		}
		
		tableModel.filter(searchString, notAnswered, taxonomyLevelsKeyPath, includeWithoutTaxonomy, levels, correctFrom, correctTo);
		tableEl.reset(true, true, true);
	}
	
	private SearchPracticeItemParameters getSearchParams() {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(getIdentity(), courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.all);
		return searchParams;
	}
	
	private List<String> toTaxonomyKeyPaths(Set<String> keyLevels) {
		if(keyLevels == null || keyLevels.isEmpty()) return List.of();
		
		List<String> taxonomyKeyPaths = new ArrayList<>(keyLevels.size());
		for(TaxonomyLevel taxonomyLevel:descendantLevels) {
			if(keyLevels.contains(taxonomyLevel.getKey().toString())) {
				List<String> keyPaths = SearchPracticeItemParametersHelper.buildKeyOfTaxonomicPath(taxonomyLevel);
				taxonomyKeyPaths.addAll(keyPaths);
			}
		}
		return taxonomyKeyPaths;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newSerieButton == source) {
			doStartSelectedItems(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("play".equals(se.getCommand())) {
					PracticeComposeItemRow row = tableModel.getObject(se.getIndex());
					doStartSingleItem(ureq, row.getItem());
				}
			} else if(event instanceof FlexiTableFilterTabEvent) {
				filterModel(null);
			} else if(event instanceof FlexiTableSearchEvent) {
				filterModel(((FlexiTableSearchEvent)event).getFilters());
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doStartSingleItem(UserRequest ureq, PracticeItem practiceItem) {
		fireEvent(ureq, new StartPracticeEvent(PlayMode.all, List.of(practiceItem)));
	}
	
	private void doStartSelectedItems(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<PracticeItem> items = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			PracticeComposeItemRow row = tableModel.getObject(selectedIndex.intValue());
			if(row != null) {
				items.add(row.getItem());
			}
		}
		fireEvent(ureq, new StartPracticeEvent(PlayMode.all, items));
	}
}
