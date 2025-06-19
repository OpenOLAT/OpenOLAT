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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressRadialCellRenderer;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.ui.CoursesTableDataModel.Columns;
import org.olat.modules.coach.ui.ParticipantsTableDataModel.ParticipantCols;
import org.olat.modules.coach.ui.component.LastVisitCellRenderer;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseListController extends FormBasicController implements Activateable2 {

	protected static final String FILTER_MARKED = "Marked";
	protected static final String FILTER_PERIOD = "Period";
	protected static final String FILTER_STATUS = "status";
	protected static final String FILTER_NOT_VISITED = "not-visited";
	protected static final String FILTER_LAST_VISIT = "last-visit";
	protected static final String FILTER_ASSESSMENT = "assessment";
	protected static final String FILTER_CERTIFICATES = "certificates";
	protected static final String FILTER_WITH_PARTICIPANTS = "WithParticipants";
	protected static final String FILTER_WITHOUT_PARTICIPANTS = "WithoutParticipants";
	
	protected static final String ASSESSMENT_PASSED_NONE = "assessment-passed-none";
	protected static final String ASSESSMENT_PASSED_PARTIALLY = "assessment-passed-partially";
	protected static final String ASSESSMENT_PASSED_ALL = "assessment-passed-all";
	protected static final String ASSESSMENT_NOT_PASSED_NONE = "assessment-not-passed-none";
	protected static final String ASSESSMENT_NOT_PASSED_PARTIALLY = "assessment-not-passed-partially";
	protected static final String ASSESSMENT_NOT_PASSED_ALL = "assessment-not-passed-all";
	protected static final String CERTIFICATES_WITHOUT = "without-certificates";
	protected static final String CERTIFICATES_WITH = "with-certificates";
	protected static final String CERTIFICATES_INVALID = "invalid-certificates";
	protected static final String VISIT_LESS_1_DAY = "less-1-day";
	protected static final String VISIT_LESS_1_WEEK = "less-1-week";
	protected static final String VISIT_LESS_4_WEEKS = "less-4-weeks";
	protected static final String VISIT_LESS_12_MONTHS = "less-12-months";
	protected static final String VISIT_MORE_12_MONTS = "more-12-months";
	
	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String FINISHED_TAB_ID = "Finished";
	private static final String ACCESS_FOR_COACH_TAB_ID = "AccessForCoach";

	private FlexiFiltersTab allTab;
	private FlexiTableElement tableEl;
	private CoursesTableDataModel tableModel;
	private final TooledStackedPanel stackPanel;

	private int counter = 0;
	private boolean hasChanged = false;
	
	private ToolsController toolsCtrl;
	private CourseController courseCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CourseListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "course_list", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);

		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel markColumn = new DefaultFlexiColumnModel(Columns.mark);
		markColumn.setIconHeader("o_icon o_icon_bookmark_header o_icon-lg");
		markColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(markColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.name, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.externalId, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.externalRef, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lifecycleStart,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lifecycleEnd,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.access,
				new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.participants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.participantsVisited));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.participantsNotVisited));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastVisit,
				new LastVisitCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion,
				new ProgressRadialCellRenderer(BarColor.success)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.successStatus,
				new SuccessStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.averageScore,
				new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificates));
		
        ActionsColumnModel actionsCol = new ActionsColumnModel(ParticipantCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CoursesTableDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_CourseModule_icon");
		tableEl.setAndLoadPersistedPreferences(ureq, "courseListController-v3");
		
		initFilters();
		initFiltersPresets(ureq);
	}
	
    private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues markedKeyValue = new SelectionValues();
		markedKeyValue.add(SelectionValues.entry(FILTER_MARKED, translate("search.mark")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("search.mark"),
				FILTER_MARKED, markedKeyValue, true));
    	
		filters.add(new FlexiTableDateRangeFilter(translate("filter.date.range"), FILTER_PERIOD, true, false,
				translate("filter.date.range.label"), translate("filter.date.to"), getLocale()));
		
		SelectionValues notVisitedPK = new SelectionValues();
		notVisitedPK.add(SelectionValues.entry(FILTER_NOT_VISITED, translate("filter.not.visited")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.not.visited"),
				FILTER_NOT_VISITED, notVisitedPK, true));
		
		SelectionValues lastVisitPK = new SelectionValues();
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_1_DAY, translate("filter.visit.less.1.day")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_1_WEEK, translate("filter.visit.less.1.week")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_4_WEEKS, translate("filter.visit.less.4.weeks")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_12_MONTHS, translate("filter.visit.less.12.months")));
		lastVisitPK.add(SelectionValues.entry(VISIT_MORE_12_MONTS, translate("filter.visit.more.12.months")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.last.visit"),
				FILTER_LAST_VISIT, lastVisitPK, true));

		SelectionValues assessmentPK = new SelectionValues();
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED_NONE, translate("filter.assessment.passed.none")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED_PARTIALLY, translate("filter.assessment.passed.partially")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED_ALL, translate("filter.assessment.passed.all")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED_NONE, translate("filter.assessment.not.passed.none")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED_PARTIALLY, translate("filter.assessment.not.passed.partially")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED_ALL, translate("filter.assessment.not.passed.all")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.assessment"),
				FILTER_ASSESSMENT, assessmentPK, true));
		
		SelectionValues statusPK = new SelectionValues();
		statusPK.add(SelectionValues.entry(RepositoryEntryStatusEnum.coachpublished.name(), translate("cif.status.coachpublished")));
		statusPK.add(SelectionValues.entry(RepositoryEntryStatusEnum.published.name(), translate("cif.status.published")));
		statusPK.add(SelectionValues.entry(RepositoryEntryStatusEnum.closed.name(), translate("status.closed")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusPK, false));
		
		SelectionValues withoutParticipantsPK = new SelectionValues();
		withoutParticipantsPK.add(SelectionValues.entry(FILTER_WITHOUT_PARTICIPANTS, translate("filter.without.participants")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.without.participants"),
				FILTER_WITHOUT_PARTICIPANTS, withoutParticipantsPK, false));
		
		SelectionValues withParticipantsPK = new SelectionValues();
		withParticipantsPK.add(SelectionValues.entry(FILTER_WITH_PARTICIPANTS, translate("filter.with.participants")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.with.participants"),
				FILTER_WITH_PARTICIPANTS, withParticipantsPK, false));

		SelectionValues certificatesPK = new SelectionValues();
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_WITHOUT, translate("filter.certificate.without")));
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_WITH, translate("filter.certificate.with")));
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_INVALID, translate("filter.certificate.invalid")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.certificates"),
				FILTER_CERTIFICATES, certificatesPK, false));
		
    	tableEl.setFilters(true, filters, true, false);
    }
	
    private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		FlexiFiltersTab relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						RepositoryEntryStatusEnum.published.name()), FlexiTableFilterValue.valueOf(FILTER_WITH_PARTICIPANTS, FILTER_WITH_PARTICIPANTS)));
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		
		FlexiFiltersTab accessForCoachTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACCESS_FOR_COACH_TAB_ID, translate("filter.access.for.coach"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						RepositoryEntryStatusEnum.coachpublished.name())));
		accessForCoachTab.setFiltersExpanded(true);
		tabs.add(accessForCoachTab);
		
		FlexiFiltersTab finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB_ID, translate("filter.finished"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						RepositoryEntryStatusEnum.closed.name())));
		finishedTab.setFiltersExpanded(true);
		tabs.add(finishedTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
    }

	private void loadModel() {
		List<Mark> marks = markManager.getMarks(getIdentity(), List.of("RepositoryEntry"));
		Set<Long> markedKeys = marks.stream()
				.map(Mark::getOLATResourceable)
				.map(OLATResourceable::getResourceableId)
				.collect(Collectors.toSet());
		
		List<CourseStatEntry> courseStatistics = coachingService.getCoursesStatistics(getIdentity());
		List<CourseStatEntryRow> rows = courseStatistics.stream()
				.map(stats -> forgeRow(stats, markedKeys.contains(stats.getRepoKey())))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private CourseStatEntryRow forgeRow(CourseStatEntry entry, boolean marked) {
		CourseStatEntryRow row = new CourseStatEntryRow(entry);
		row.setMarked(marked);
		
		//mark
		String count = Integer.toString(++counter);
		FormLink markLink = uifactory.addFormLink("mark_".concat(count), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		
		return row;
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}
	
	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
        super.doDispose();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent pe
					&& pe.getController() == courseCtrl && hasChanged) {
				reloadModel();
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("select".equals(se.getCommand())) {
					CourseStatEntryRow courseStat = tableModel.getObject(se.getIndex());
					selectCourse(ureq, courseStat);
				} else if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CourseStatEntryRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("mark".equals(cmd) && link.getUserObject() instanceof CourseStatEntryRow row) {
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == courseCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
			} else if("next.course".equals(event.getCommand())) {
				nextCourse(ureq);
			} else if("previous.course".equals(event.getCommand())) {
				previousCourse(ureq);
			}
		} else if(toolsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
        	cleanUp();
        }
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeControllerListener(calloutCtrl);
		removeControllerListener(toolsCtrl);
		calloutCtrl = null;
		toolsCtrl = null;
	}

	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("RepositoryEntry".equalsIgnoreCase(ores.getResourceableTypeName())) {
			Long repoKey = ores.getResourceableId();
			for(int i=tableModel.getRowCount(); i-->0; ) {
				CourseStatEntryRow courseStat = tableModel.getObject(i);
				if(repoKey.equals(courseStat.getRepoKey())) {
					selectCourse(ureq, courseStat);
					if(courseCtrl != null) {
						courseCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					}
					break;
				}
			}
		}
	}
	
	private void previousCourse(UserRequest ureq) {
		CourseStatEntryRow currentEntry = courseCtrl.getEntry();
		int previousIndex = tableModel.getIndexOfObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableModel.getRowCount()) {
			previousIndex = tableModel.getRowCount() - 1;
		}
		CourseStatEntryRow previousEntry = tableModel.getObject(previousIndex);
		selectCourse(ureq, previousEntry);
	}
	
	private void nextCourse(UserRequest ureq) {
		CourseStatEntryRow currentEntry = courseCtrl.getEntry();
		int nextIndex = tableModel.getIndexOfObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableModel.getRowCount()) {
			nextIndex = 0;
		}
		CourseStatEntryRow nextEntry = tableModel.getObject(nextIndex);
		selectCourse(ureq, nextEntry);
	}
	
	private void selectCourse(UserRequest ureq, CourseStatEntryRow courseStat) {
		removeAsListenerAndDispose(courseCtrl);
		courseCtrl = null;
		
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseStat.getRepoKey(), false);
		if(re != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, re.getKey());
			WindowControl bwControl = addToHistory(ureq, ores, null);
			
			int index = tableModel.getIndexOfObject(courseStat);
			courseCtrl = new CourseController(ureq, bwControl, stackPanel, re, courseStat, index, tableModel.getRowCount());
			listenTo(courseCtrl);
			stackPanel.pushController(re.getDisplayname(), courseCtrl);
		}
	}
	
	
	private boolean doMark(UserRequest ureq, CourseStatEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getRepoKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			dbInstance.commit();//before sending, save the changes
			
			EntryChangedEvent e = new EntryChangedEvent(() -> row.getRepoKey(), getIdentity(), Change.removeBookmark, "coaching.courses");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		}
		
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		dbInstance.commit();//before sending, save the changes
		
		EntryChangedEvent e = new EntryChangedEvent(() -> row.getRepoKey(), getIdentity(), Change.addBookmark, "coaching.courses");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		return true;
	}
	
	private void doOpenTools(UserRequest ureq, CourseStatEntryRow entry, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), entry);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CourseStatEntryRow entry) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("tool_courses");
			
			String url = BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + entry.getRepoKey() + "]");
			ExternalLink openCourseLink = LinkFactory.createExternalLink("open.course", translate("open.course"), url);
			openCourseLink.setIconLeftCSS("o_icon o_icon_content_popup");
			openCourseLink.setName(translate("open.course"));
			mainVC.put("open.course", openCourseLink);
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}