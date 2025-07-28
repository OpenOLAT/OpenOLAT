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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
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
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.components.scope.FormScopeSelection;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeFactory;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.ui.CoursesTableDataModel.Columns;
import org.olat.modules.coach.ui.ParticipantsTableDataModel.ParticipantCols;
import org.olat.modules.coach.ui.component.CompletionCellRenderer;
import org.olat.modules.coach.ui.component.LastVisitCellRenderer;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.TechnicalTypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CourseListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
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
	
	private static final String CMD_SELECT = "select";
	private static final String CMD_ASSESSMENT = "assessment";
	private static final String CMD_INFOS = "infos";
	
	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String FINISHED_TAB_ID = "Finished";
	private static final String ACCESS_FOR_COACH_TAB_ID = "AccessForCoach";

	private FlexiFiltersTab allTab;
	private FlexiTableElement tableEl;
	private CoursesTableDataModel tableModel;
	private List<Scope> scopes;
	private FormScopeSelection scopeEl;

	private int counter = 0;
	private final MapperKey mapperThumbnailKey;
	private List<RepositoryEntryEducationalType> educationalTypes;
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public CourseListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "course_list", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(210, 140));
		educationalTypes = repositoryManager.getAllEducationalTypes();
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initScopes(formLayout);
		initForm(formLayout, ureq);
	}
	
	private void initScopes(FormItemContainer formLayout) {
		// As coach / course owner
		scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(GroupRoles.coach.name(), translate("lectures.teacher.menu.title"), null, "o_icon o_icon_coaching_tool"));
		scopes.add(ScopeFactory.createScope(GroupRoles.owner.name(), translate("lectures.owner.menu.title"), null, "o_icon o_icon_coaching_tool"));
		scopeEl = uifactory.addScopeSelection("scopes", null, formLayout, scopes);
		scopeEl.setSelectedKey(GroupRoles.coach.name());
	}
	
	private void initForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel markColumn = new DefaultFlexiColumnModel(Columns.mark);
		markColumn.setIconHeader("o_icon o_icon_bookmark_header");
		markColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(markColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.key));
		DefaultFlexiColumnModel technicalTypeCol = new DefaultFlexiColumnModel(false, Columns.technicalType);
		technicalTypeCol.setCellRenderer(new TechnicalTypeRenderer());
		columnsModel.addFlexiColumnModel(technicalTypeCol);
		DefaultFlexiColumnModel nameCol = new DefaultFlexiColumnModel(Columns.name, CMD_SELECT);
		nameCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.externalId, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.externalRef, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lifecycleStart,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lifecycleEnd,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.access,
				new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.participants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.participantsVisited));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.participantsNotVisited));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastVisit,
				new LastVisitCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion,
				new CompletionCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.successStatus,
				new SuccessStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.statusPassed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.statusNotPassed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.statusUndefined));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.averageScore,
				new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificates));
		
		// Assessment tool
		DefaultFlexiColumnModel assessmentToolCol = new DefaultFlexiColumnModel(Columns.assessmentTool, CMD_ASSESSMENT,
				new StaticFlexiCellRenderer("", CMD_ASSESSMENT, null, "o_icon-lg o_icon_assessment_tool", translate("table.header.assessment.tool")));
		assessmentToolCol.setExportable(false);
		assessmentToolCol.setIconHeader("o_icon o_icon-lg o_icon_assessment_tool");
		columnsModel.addFlexiColumnModel(assessmentToolCol);
		
		// Infos
		DefaultFlexiColumnModel infosCol = new DefaultFlexiColumnModel(Columns.infos, CMD_INFOS,
				new StaticFlexiCellRenderer("", CMD_INFOS, null, "o_icon-lg o_icon_details", translate("table.header.infos")));
		infosCol.setExportable(false);
		infosCol.setIconHeader("o_icon o_icon-lg o_icon_details");
		columnsModel.addFlexiColumnModel(infosCol);
		
		// Tools
        ActionsColumnModel actionsCol = new ActionsColumnModel(ParticipantCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CoursesTableDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_CourseModule_icon");
		tableEl.setAndLoadPersistedPreferences(ureq, "courseListController-v3.3");
		
		VelocityContainer row = createVelocityContainer("row_1");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		row.setTranslator(getTranslator());
		tableEl.setRowRenderer(row, this);
		
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
				getLocale()));
		
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
		tableEl.setSelectedFilterTab(ureq, relevantTab);
    }
    
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> list = null;
		if(rowObject instanceof CourseStatEntryRow entryRow) {
			list = new ArrayList<>(7);
			list.add(entryRow.getSelectLink().getComponent());
			list.add(entryRow.getMarkLink().getComponent());
			list.add(entryRow.getInfosLink().getComponent());
			list.add(entryRow.getOpenLink().getComponent());
			if(entryRow.getRatingFormItem() != null) {
				list.add(entryRow.getRatingFormItem().getComponent());
			}
			if(entryRow.getCommentsLink() != null) {
				list.add(entryRow.getCommentsLink().getComponent());
			}
		}
		return list;
	}

	private void loadModel() {
		List<Mark> marks = markManager.getMarks(getIdentity(), List.of("RepositoryEntry"));
		Set<Long> markedKeys = marks.stream()
				.map(Mark::getOLATResourceable)
				.map(OLATResourceable::getResourceableId)
				.collect(Collectors.toSet());
		
		GroupRoles role = GroupRoles.valueOf(scopeEl.getSelectedKey());
		List<CourseStatEntry> courseStatistics = coachingService.getCoursesStatistics(getIdentity(), role);
		List<CourseStatEntryRow> rows = courseStatistics.stream()
				.map(stats -> forgeRow(stats, markedKeys.contains(stats.getRepoKey())))
				.collect(Collectors.toList());
		loadTaxonomy(rows);
		
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private void loadTaxonomy(List<CourseStatEntryRow> rows) {
		Map<RepositoryEntryRef,List<TaxonomyLevel>> taxonomy = repositoryService.getTaxonomy(rows, false);
		for(CourseStatEntryRow row:rows) {
			List<TaxonomyLevel> levels = taxonomy.get(row);
			int numOfLevels = levels == null ? 0 : levels.size();
			row.setNumOfTaxonomyLevels(numOfLevels);
		}
	}
	
	private CourseStatEntryRow forgeRow(CourseStatEntry entry, boolean marked) {
		RepositoryEntryEducationalType educationalType = getEducationalType(entry);
		CourseStatEntryRow row = new CourseStatEntryRow(entry, educationalType);
		row.setMarked(marked);
		
		forgeActionsLinks(row);
		forgeComments(entry, row);
		forgeRatings(entry, row);
		
		VFSLeaf image = repositoryManager.getImage(entry.getRepoKey(), OresHelper.createOLATResourceableInstance("CourseModule", entry.getResourceId()));
		if(image != null) {
			row.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image));
		}
		
		if(StringHelper.containsNonWhitespace(entry.getRepoTechnicalType())) {
			String translatedType = nodeAccessService.getNodeAccessTypeName(NodeAccessType.of(entry.getRepoTechnicalType()), getLocale());
			row.setTranslatedTechnicalType(translatedType);
		}
		
		return row;
	}
	
	private RepositoryEntryEducationalType getEducationalType(CourseStatEntry entry) {
		if(entry.getEducationalTypeKey() == null) return null;
		return educationalTypes.stream()
				.filter(type -> type.getKey().equals(entry.getEducationalTypeKey()))
				.findFirst().orElse(null);
	}
	
	private void forgeRatings(CourseStatEntry entry, CourseStatEntryRow row) {
		if(!repositoryModule.isRatingEnabled()) return;
		
		Integer myRating = entry.getMyRating();
		Double averageRating = entry.getAverageRating();
		long numOfRatings = entry.getNumOfRatings();

		float ratingValue = myRating == null ? 0f : myRating.floatValue();
		float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
		
		String id = "rat_" + row.getKey();
		RatingWithAverageFormItem ratingEl = new RatingWithAverageFormItem(id, ratingValue, averageRatingValue, 5, numOfRatings);
		ratingEl.setShowRatingAsText(false);
		ratingEl.setLargeIcon(false);
		row.setRatingFormItem(ratingEl);
		ratingEl.setUserObject(row);
		flc.add(id, ratingEl);
	}

	private void forgeComments(CourseStatEntry entry, CourseStatEntryRow row) {
		if(!repositoryModule.isCommentEnabled()) return;
		
		long numOfComments = entry.getNumOfComments();
		String title = "(" + numOfComments + ")";
		FormLink commentsLink = uifactory.addFormLink("comments_" + row.getKey(), CMD_INFOS, title, tableEl, Link.NONTRANSLATED);
		commentsLink.setUserObject(row);
		String css = numOfComments > 0 ? "o_icon o_icon_comments" : "o_icon o_icon_comments_none";
		commentsLink.setCustomEnabledLinkCSS("o_comments");
		commentsLink.setIconLeftCSS(css);
		row.setCommentsLink(commentsLink);
	}
	
	private void forgeActionsLinks(CourseStatEntryRow row) {
		//mark
		String count = Integer.toString(++counter);
		FormLink markLink = uifactory.addFormLink("mark_".concat(count), "mark", "", tableEl, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		
		String displayName = StringHelper.escapeHtml(row.getDisplayName());
		FormLink selectLink = uifactory.addFormLink("select_".concat(count), CMD_SELECT, displayName, tableEl, Link.NONTRANSLATED);
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
		
		FormLink detailsLink = uifactory.addFormLink("infos_".concat(count), CMD_INFOS, "learn.more", tableEl, Link.BUTTON);
		detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-default o_details o_button_ghost");
		detailsLink.setIconRightCSS("o_icon o_icon_details");
		detailsLink.setUserObject(row);
		detailsLink.setGhost(true);
		row.setInfosLink(detailsLink);
		
		FormLink openLink = uifactory.addFormLink("open_".concat(count), CMD_SELECT, "open", tableEl, Link.BUTTON);
		openLink.setCustomEnabledLinkCSS("btn btn-sm btn-primary o_start");
		openLink.setIconRightCSS("o_icon o_icon_start");
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		openLink.setUrl(BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathString(businessPath));
		openLink.setUserObject(row);
		row.setOpenLink(openLink);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = Long.valueOf(rowKeyStr);
						List<CourseStatEntryRow> rows = tableModel.getObjects();
						for(CourseStatEntryRow row:rows) {
							if(row != null && row.getKey().equals(rowKey)) {
								doOpenCourse(ureq, row);					
							}
						}
					} catch (NumberFormatException e) {
						getLogger().warn("Not a valid long: {}", rowKeyStr, e);
					}
				}
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
		if(source instanceof RatingWithAverageFormItem ratingItem && event instanceof RatingFormEvent ratingEvent
				&& ratingItem.getUserObject() instanceof CourseStatEntryRow row) {
			doRating(row, ratingEvent.getRating());
		} else if(scopeEl == source) {
			loadModel();
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(CMD_SELECT.equals(se.getCommand())) {
					CourseStatEntryRow courseStat = tableModel.getObject(se.getIndex());
					doOpenCourse(ureq, courseStat);
				} else if(CMD_ASSESSMENT.equals(se.getCommand())) {
					CourseStatEntryRow courseStat = tableModel.getObject(se.getIndex());
					doOpenAssessmentTool(ureq, courseStat);
				} else if(CMD_INFOS.equals(se.getCommand())) {
					CourseStatEntryRow courseStat = tableModel.getObject(se.getIndex());
					doOpenCourseInfos(ureq, courseStat);
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
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark" : "o_icon o_icon_bookmark_add");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			} else if(CMD_SELECT.equals(cmd) && link.getUserObject() instanceof CourseStatEntryRow row) {
				doOpenCourse(ureq, row);
			} else if(CMD_INFOS.equals(cmd) && link.getUserObject() instanceof CourseStatEntryRow row) {
				doOpenCourseInfos(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
        	cleanUp();
        } else if(toolsCtrl == source) {
        	if(event == Event.CLOSE_EVENT) {
        		calloutCtrl.deactivate();
        		cleanUp();
        	}
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
		// Load only on activation
		loadModel();
	}
	
	private void doOpenCourse(UserRequest ureq, CourseStatEntryRow courseStat) {
		String businessPath = "[RepositoryEntry:" + courseStat.getKey() +"]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenAssessmentTool(UserRequest ureq, CourseStatEntryRow courseStat) {
		String businessPath = "[RepositoryEntry:" + courseStat.getKey() +"][assessmentToolv2:0][Overview:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenCourseInfos(UserRequest ureq, CourseStatEntryRow courseStat) {
		String businessPath = "[RepositoryEntry:" + courseStat.getKey() +"][Infos:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private boolean doMark(UserRequest ureq, CourseStatEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			dbInstance.commit();//before sending, save the changes
			
			EntryChangedEvent e = new EntryChangedEvent(() -> row.getKey(), getIdentity(), Change.removeBookmark, "coaching.courses");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		}
		
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		dbInstance.commit();//before sending, save the changes
		
		EntryChangedEvent e = new EntryChangedEvent(() -> row.getKey(), getIdentity(), Change.addBookmark, "coaching.courses");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		return true;
	}
	
	protected void doRating(CourseStatEntryRow row, float rating) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("CourseModule", row.getResourceId());
		userRatingsDao.updateRating(getIdentity(), ores, null, Math.round(rating));
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
		
		private final Link infosLink;
		private final Link assessmentToolLink;
		
		private final CourseStatEntryRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CourseStatEntryRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tool_courses");
			
			assessmentToolLink = LinkFactory.createLink("assessment.tool", "assessment.tool", "assessment", mainVC, this);
			assessmentToolLink.setIconLeftCSS("o_icon o_icon-fw o_icon_assessment_tool");
			
			infosLink = LinkFactory.createLink("infos", "infos", "infos", mainVC, this);
			infosLink.setIconLeftCSS("o_icon o_icon-fw o_icon_details");
			
			String url = BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + row.getKey() + "]");
			ExternalLink openCourseLink = LinkFactory.createExternalLink("open.course", translate("open.course"), url);
			openCourseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_content_popup");
			openCourseLink.setName(translate("open.course"));
			mainVC.put("open.course", openCourseLink);
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(assessmentToolLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doOpenAssessmentTool(ureq, row);
			} else if(infosLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doOpenCourseInfos(ureq, row);
			}
		}
	}
}