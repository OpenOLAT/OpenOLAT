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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumElementCalendarController;
import org.olat.modules.curriculum.ui.ImplementationsListConfig;
import org.olat.modules.curriculum.ui.component.CurriculumRolesComparator;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.member.RolesFlexiCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryMyImplementationsQueries;
import org.olat.repository.ui.list.ImplementationsListDataModel.ImplementationsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationsListController extends FormBasicController implements Activateable2 {

	private final String CMD_SELECT  = "iselect";
	
	private static final String ALL_TAB_ID = "All";
	private static final String FAVORITE_TAB_ID = "Marks";
	private static final String FINISHED_TAB_ID = "Finished";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String PREPARATION_TAB_ID = "Preparation";
	
	static final String FILTER_MARKED = "Marked";
	static final String FILTER_STATUS = "Status";
	static final String FILTER_PERIOD = "Period";
	static final String FILTER_CURRICULUM = "Curriculum";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab favoriteTab;
	private FlexiFiltersTab finishedTab;
	private FlexiFiltersTab relevantTab;
	private FlexiFiltersTab preparationTab;
	
	private FlexiTableElement tableEl;
	private ImplementationsListDataModel tableModel;
	private final BreadcrumbedStackedPanel stackPanel;
	
	private final Identity assessedIdentity;
	private final List<CurriculumElementStatus> status;
	private final ImplementationsListConfig config;
	
	private int counter;
	private ImplementationController implementationCtrl;
	private CurriculumElementCalendarController calendarsCtrl;


	@Autowired
	private MarkManager markManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;

	public ImplementationsListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			Identity assessedIdentity, ImplementationsListConfig config) {
		super(ureq, wControl, "implementations");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(),
				Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator())));
		this.stackPanel = stackPanel;
		this.assessedIdentity = assessedIdentity;
		this.config = config;
		status = config.withPreparation()
				? RepositoryEntryMyImplementationsQueries.STATUS_WITH_PREPARATION
				: RepositoryEntryMyImplementationsQueries.STATUS_WITHOUT_PREPARATION;
		
		initForm(ureq);
		loadModel();
		
		initFilter();
		initFilterPresets();
		
		if(favoriteTab != null && tableModel.hasMarked()) {
			tableEl.setSelectedFilterTab(ureq, favoriteTab);
		} else if(allTab != null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
		}
		
		filterModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("withTitle", Boolean.valueOf(config.withFormTitle()));
			layoutCont.contextPut("helpUrl", config.helpUrl());
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		if (config.withBookmarks()) {
			DefaultFlexiColumnModel markColModel = new DefaultFlexiColumnModel(ImplementationsCols.mark);
			markColModel.setIconHeader("o_icon o_icon_bookmark_header o_icon-lg");
			markColModel.setExportable(false);
			columnsModel.addFlexiColumnModel(markColModel);
		}
		
		if (config.withId()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImplementationsCols.key));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.extRefVisibilityDefault(), ImplementationsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.curriculum,
				new CurriculumCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.lifecycleStart,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.lifecycleEnd,
				new DateFlexiCellRenderer(getLocale())));
		if (config.withRoles()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.roles,
					new RolesFlexiCellRenderer(getTranslator())));
		}
		if (config.withStatus()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImplementationsCols.elementStatus,
					new CurriculumStatusCellRenderer(getTranslator())));
		}
		if (config.withCompletion()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.completion));
		}
		if (config.withCalendar()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.calendars));
		}

		tableModel = new ImplementationsListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setSearchEnabled(true);
	}
	
	private void initFilter() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
	
		if (config.withBookmarks()) {
			SelectionValues markedKeyValue = new SelectionValues();
			markedKeyValue.add(SelectionValues.entry(FILTER_MARKED, translate("search.mark")));
			filters.add(new FlexiTableOneClickSelectionFilter(translate("search.mark"),
					FILTER_MARKED, markedKeyValue, true));
		}
		
		Set<Curriculum> curriculums = tableModel.getObjects().stream().map(ImplementationRow::getCurriculum).collect(Collectors.toSet());
		if(!curriculums.isEmpty()) {
			SelectionValues curriculumValues = new SelectionValues();
			for(Curriculum cur:curriculums) {
				String key = cur.getKey().toString();
				String value = StringHelper.escapeHtml(cur.getDisplayName());
				if(StringHelper.containsNonWhitespace(cur.getIdentifier())) {
					value += " <small class=\"mute\"> \u00B7 " + StringHelper.escapeHtml(cur.getIdentifier()) + "</small>";
				}
				curriculumValues.add(SelectionValues.entry(key, value));
			}
			curriculumValues.sort(SelectionValues.VALUE_ASC);
			
			FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
					FILTER_CURRICULUM, curriculumValues, true);
			filters.add(curriculumFilter);
		}
		
		SelectionValues statusValues = new SelectionValues();
		if(config.withPreparation()) {
			statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		}
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		if(config.withCancelledFilter()) {
			statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		}
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		FlexiTableDateRangeFilter periodFilter = new FlexiTableDateRangeFilter(translate("filter.date.range"),
				FILTER_PERIOD, true, false, getLocale());
		filters.add(periodFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		if (config.withBookmarks()) {
			favoriteTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FAVORITE_TAB_ID, translate("filter.mark"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_MARKED, FILTER_MARKED)));
			tabs.add(favoriteTab);
		}
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.provisional.name(), CurriculumElementStatus.confirmed.name(),
								CurriculumElementStatus.active.name()))));
		tabs.add(relevantTab);
		if (config.withPreparation()) {
			preparationTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PREPARATION_TAB_ID, translate("filter.preparation"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
							List.of(CurriculumElementStatus.preparation.name()))));
			tabs.add(preparationTab);
		}
		
		finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB_ID, translate("filter.finished"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.finished.name()))));
		tabs.add(finishedTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	private void loadModel() {
		Set<Long> markedElementKeys = config.withBookmarks()
				? markManager.getMarkResourceIds(assessedIdentity, "CurriculumElement", List.of())
				: Set.of();
		List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(assessedIdentity,
				false, config.asRoles(), status);
		if (config.coachIdentity() != null) {
			Set<Long> coachImplementationKeys = myImplementationsQueries
					.searchImplementations(config.coachIdentity(), false, List.of(GroupRoles.coach), status).stream()
					.map(CurriculumElement::getKey)
					.collect(Collectors.toSet());
			implementations = implementations.stream()
					.filter(impl -> coachImplementationKeys.contains(impl.getKey()))
					.toList();
		}

		Map<Long, List<CurriculumRoles>> eleKeyToRoles = Map.of();
		if (config.withRoles()) {
			eleKeyToRoles = curriculumService.getCurriculumElementMemberships(implementations, List.of(assessedIdentity)).stream()
				.collect(Collectors.toMap(CurriculumElementMembership::getCurriculumElementKey, CurriculumElementMembership::getRoles));
		}
		
		List<ImplementationRow> rows = new ArrayList<>();
		for(CurriculumElement implementation:implementations) {
			boolean marked = markedElementKeys.contains(implementation.getKey());
			Curriculum curriculum = implementation.getCurriculum();
			CurriculumRolesComparator rolesComparator = new CurriculumRolesComparator();
			List<CurriculumRoles> roles = eleKeyToRoles.getOrDefault(implementation.getKey(), List.of()).stream()
					.sorted((r1, r2) -> rolesComparator.compare(r1, r2))
					.toList();
			rows.add(forgeRow(implementation, curriculum, roles, marked));
		}
		forgeCalendarsLinks(rows);
		if (config.withCompletion()) {
			forgeCurriculumCompletions(rows);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ImplementationRow forgeRow(CurriculumElement implementation, Curriculum curriculum,
			List<CurriculumRoles> roles, boolean marked) {
		ImplementationRow row = new ImplementationRow(implementation, curriculum, roles);
		
		if (config.withBookmarks()) {
			FormLink markLink = uifactory.addFormLink("mark_" + implementation.getResource().getKey(), "mark", "", tableEl, Link.NONTRANSLATED);
			decoratedMarkLink(markLink, marked);
			row.setMarkLink(markLink);
			markLink.setUserObject(row);
		}
		
		return row;
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private void decoratedMarkLink(FormLink markLink, boolean marked) {
		markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setAriaLabel(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("CurriculumElement".equalsIgnoreCase(type)) {
			ImplementationRow row = tableModel.getObjectByKey(entry.getOLATResourceable().getResourceableId());
			if(row != null) {
				doOpenImplementation(ureq, row);
			}
		} else if(ALL_TAB_ID.equalsIgnoreCase(type) && allTab != null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			filterModel();
		} else if(FAVORITE_TAB_ID.equalsIgnoreCase(type) && favoriteTab != null) {
			tableEl.setSelectedFilterTab(ureq, favoriteTab);
			filterModel();
		} else if(RELEVANT_TAB_ID.equalsIgnoreCase(type) && relevantTab != null) {
			tableEl.setSelectedFilterTab(ureq, relevantTab);
			filterModel();
		} else if(FINISHED_TAB_ID.equalsIgnoreCase(type) && finishedTab != null) {
			tableEl.setSelectedFilterTab(ureq, finishedTab);
			filterModel();
		} else if(PREPARATION_TAB_ID.equalsIgnoreCase(type) && preparationTab != null) {
			tableEl.setSelectedFilterTab(ureq, preparationTab);
			filterModel();
		} else if(allTab != null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			filterModel();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(implementationCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				ImplementationRow row = tableModel.getObject(se.getIndex());
				doOpenImplementation(ureq, row);
			} else if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof ImplementationRow row) {
			if("mark".equals(link.getCmd())) {
				boolean marked = doMark(ureq, row);
				decoratedMarkLink(link, marked);
				link.getComponent().setDirty(true);
			} else if("calendars".equals(link.getCmd())) {
				doOpenCalendars(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenImplementation(UserRequest ureq, ImplementationRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		
		CurriculumSecurityCallback secCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Implementation", element.getKey());
		WindowControl swControl = addToHistory(ureq, ores, null);
		implementationCtrl = new ImplementationController(ureq, swControl, stackPanel, assessedIdentity,
				element.getCurriculum(), element, secCallback, config);
		listenTo(implementationCtrl);
		stackPanel.pushController(element.getDisplayName(), implementationCtrl);
	}
	
	private void forgeCalendarsLinks(List<ImplementationRow> rows) {
		for (ImplementationRow row : rows) {
			if (row.isCalendarsEnabled()) {
				FormLink calendarLink = uifactory.addFormLink("cals_" + (++counter), "calendars", "calendars", null, null, Link.LINK);
				calendarLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
				calendarLink.setUserObject(row);
				row.setCalendarsLink(calendarLink);
			}
		}
	}

	private void forgeCurriculumCompletions(List<ImplementationRow> rows) {
		Collection<Long> curEleKeys = rows.stream()
				.filter(ImplementationRow::isLearningProgressEnabled)
				.map(ImplementationRow::getKey)
				.collect(Collectors.toList());
		if (curEleKeys.isEmpty()) return;

		List<AssessmentEntryCompletion> completionsList = assessmentService
				.loadAvgCompletionsByCurriculumElements(assessedIdentity, curEleKeys);
		Map<Long, Double> completions = new HashMap<>();
		for (AssessmentEntryCompletion c : completionsList) {
			if (c.getCompletion() != null) {
				completions.put(c.getKey(), c.getCompletion());
			}
		}
		for (ImplementationRow row : rows) {
			Double completion = completions.get(row.getKey());
			if (completion != null) {
				forgeCompletion(row, completion);
			}
		}
	}

	private void forgeCompletion(ImplementationRow row, Double completion) {
		ProgressBarItem completionItem = new ProgressBarItem("completion_" + row.getKey(), 100,
				completion.floatValue(), Float.valueOf(1), null);
		completionItem.setWidthInPercent(true);
		completionItem.setLabelAlignment(LabelAlignment.none);
		completionItem.setRenderStyle(RenderStyle.radial);
		completionItem.setRenderSize(RenderSize.inline);
		completionItem.setBarColor(BarColor.neutral);
		row.setCompletionItem(completionItem);
		row.setCompletion(completion);
	}

	private void doOpenCalendars(UserRequest ureq, ImplementationRow row) {
		removeAsListenerAndDispose(calendarsCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntriesWithDescendants(element).stream()
				.filter(e -> "CourseModule".equals(e.getOlatResource().getResourceableTypeName()))
				.collect(Collectors.toList());

		CurriculumSecurityCallback secCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Calendars", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		calendarsCtrl = new CurriculumElementCalendarController(ureq, bwControl, element, entries, secCallback);
		listenTo(calendarsCtrl);
		stackPanel.pushController(translate("calendars"), calendarsCtrl);
	}

	private boolean doMark(UserRequest ureq, ImplementationRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("CurriculumElement", row.getKey());
		String businessPath = "[MyCoursesSite:0][CurriculumElement:" + item.getResourceableId() + "]";

		boolean marked;
		if(markManager.isMarked(item, assessedIdentity, null)) {
			markManager.removeMark(item, assessedIdentity, null);
			marked = false;
		} else {
			markManager.setMark(item, assessedIdentity, null, businessPath);
			marked = true;
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
		return marked;
	}
}
