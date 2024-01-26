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
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.PortraitCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.model.AssessmentEntryInspection;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionOverviewListModel.OverviewCols;
import org.olat.course.assessment.ui.inspection.elements.AssessmentStatusInspectionCellRenderer;
import org.olat.course.assessment.ui.inspection.elements.CourseNodeCellRenderer;
import org.olat.course.assessment.ui.inspection.elements.InspectionPeriodCellRenderer;
import org.olat.course.assessment.ui.inspection.elements.InspectionStatusCellRenderer;
import org.olat.course.assessment.ui.inspection.elements.MinuteCellRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.ui.component.RosterEntryStatusCellRenderer;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionOverviewController extends FormBasicController implements Activateable2 {
	
	public static final String FILTER_INSPECTION_STATUS = "inspection-status";
	public static final String FILTER_INSPECTION_CONFIGURATION = "inspection-configuration";
	public static final String FILTER_ASSESSMENT_STATUS = "assessment-status";
	public static final String FILTER_COURSE_NODE = "course-node";
	public static final String ALL_TAB_ID = "All";
	public static final String SCHEDULED_TAB_ID = "Scheduled";
	public static final String ACTIVE_TAB_ID = "Active";
	public static final String INPROGRESS_TAB_ID = "InProgress";
	public static final String CARRIEDOUT_TAB_ID = "Carriedout";
	public static final String NO_SHOW_TAB_ID = "NoShow";
	public static final String CANCELLED_TAB_ID = "Cancelled";
	public static final String WITHDRAWN_TAB_ID = "Withdrawn";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab inProgressTab;
	private FlexiFiltersTab scheduledTab;
	
	private FormLink addMembersButton;
	private FormLink bulkCancelButton;
	private FormLink bulkWithdrawButton;
	private FormLink bulkExtendDurationButton;
	private FlexiTableElement tableEl;
	private AssessmentInspectionOverviewListModel tableModel;
	
	private int counter = 0;
	private final CourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback secCallback;
	private final MapperKey avatarMapperKey;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private StepsMainRunController addMembersWizard;
	private EditExtraTimeController editExtraTimeCtrl;
	private StepsMainRunController editInspectionWizard;
	private AssessmentInspectionLogController activityLogCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmCancelInspectionController confirmCancelCtrl;
	private ConfirmWithdrawInspectionController confirmWithdrawCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private AssessmentInspectionService inspectionService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public AssessmentInspectionOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback secCallback) {
		this(ureq, wControl, courseEntry, null, secCallback);
	}
	
	public AssessmentInspectionOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode, AssessmentToolSecurityCallback secCallback) {
		super(ureq, wControl, "inspection_overview");
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		this.secCallback = secCallback;
		avatarMapperKey = mapperService.register(null, "avatars-members", new UserAvatarMapper(false));
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(courseNode == null) {
			setFormTitle("inspection.overview.title");
		}
		
		addMembersButton = uifactory.addFormLink("add.members", formLayout, Link.BUTTON);
		addMembersButton.setIconLeftCSS("o_icon o_icon_add");
		
		boolean courseOverview = (courseNode == null);
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!courseOverview, OverviewCols.portrait,
				new PortraitCellRenderer(avatarMapperKey)));
		if(imModule.isOnlineStatusEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!courseOverview, OverviewCols.onlineStatus,
					new RosterEntryStatusCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.participant));
		if(courseOverview) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.courseNode,
					new CourseNodeCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!courseOverview, OverviewCols.assessmentStatus,
				new AssessmentStatusInspectionCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionPeriod,
				new InspectionPeriodCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionDuration,
				new MinuteCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionStatus,
				new InspectionStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OverviewCols.comment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!courseOverview, OverviewCols.effectiveDuration,
				new MinuteCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.accessCode));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(courseOverview, OverviewCols.configuration));
		
		StickyActionColumnModel cancelCol = new StickyActionColumnModel(OverviewCols.cancel);
		cancelCol.setExportable(false);
		cancelCol.setIconHeader("o_icon o_icon_cancel o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(cancelCol);
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(OverviewCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
	
		tableModel = new AssessmentInspectionOverviewListModel(columnsModel, getIdentity(), sessionManager, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setCssDelegate(new AssessmentInspectionTableCSSDelegate(tableModel));
		
		initFiltersPresets();
		initFilters();
		
		String tableId = "assessment-inspection-overview-" + (courseNode == null ? "root" : "node");
		tableEl.setAndLoadPersistedPreferences(ureq, tableId);
		
		bulkCancelButton = uifactory.addFormLink("cancel", "cancel", "bulk.cancel", null, formLayout, Link.BUTTON);
		bulkCancelButton.setIconLeftCSS("o_icon o_icon_cancel");
		tableEl.addBatchButton(bulkCancelButton);

		bulkExtendDurationButton = uifactory.addFormLink("extend", "extend", "bulk.extend", null, formLayout, Link.BUTTON);
		bulkExtendDurationButton.setIconLeftCSS("o_icon o_icon_extra_time");
		tableEl.addBatchButton(bulkExtendDurationButton);
		
		bulkWithdrawButton = uifactory.addFormLink("withdraw", "withdraw", "bulk.withdraw", null, formLayout, Link.BUTTON);
		tableEl.addBatchButton(bulkWithdrawButton);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_inspection_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		scheduledTab = FlexiFiltersTabFactory.tabWithImplicitFilters(SCHEDULED_TAB_ID, translate("filter.status.scheduled"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, AssessmentInspectionStatusEnum.scheduled.name())));
		scheduledTab.setFiltersExpanded(true);
		tabs.add(scheduledTab);
		
		FlexiFiltersTab activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACTIVE_TAB_ID, translate("filter.status.active"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, "active")));
		activeTab.setFiltersExpanded(true);
		tabs.add(activeTab);
		
		inProgressTab = FlexiFiltersTabFactory.tabWithImplicitFilters(INPROGRESS_TAB_ID, translate("filter.status.inProgress"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, AssessmentInspectionStatusEnum.inProgress.name())));
		inProgressTab.setFiltersExpanded(true);
		tabs.add(inProgressTab);
		
		FlexiFiltersTab carriedOutTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CARRIEDOUT_TAB_ID, translate("filter.status.carriedOut"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, AssessmentInspectionStatusEnum.carriedOut.name())));
		carriedOutTab.setFiltersExpanded(true);
		tabs.add(carriedOutTab);
		
		FlexiFiltersTab cancelledTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CANCELLED_TAB_ID, translate("filter.status.cancelled"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, AssessmentInspectionStatusEnum.cancelled.name())));
		cancelledTab.setFiltersExpanded(true);
		tabs.add(cancelledTab);
		
		FlexiFiltersTab noShowTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NO_SHOW_TAB_ID, translate("filter.status.noShow"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, AssessmentInspectionStatusEnum.noShow.name())));
		noShowTab.setFiltersExpanded(true);
		tabs.add(noShowTab);
		
		FlexiFiltersTab withdrawnTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WITHDRAWN_TAB_ID, translate("filter.status.withdrawn"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_INSPECTION_STATUS, AssessmentInspectionStatusEnum.withdrawn.name())));
		withdrawnTab.setFiltersExpanded(true);
		tabs.add(withdrawnTab);

		tableEl.setFilterTabs(true, tabs);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(courseNode == null) {
			SelectionValues courseNodesValues = new SelectionValues();
			ICourse course = CourseFactory.loadCourse(courseEntry);
			initCourseElementValues(courseNodesValues, course.getRunStructure().getRootNode());
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.course.node"),
					FILTER_COURSE_NODE, courseNodesValues, true));
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(AssessmentInspectionStatusEnum.scheduled.name(), translate("inspection.status.".concat(AssessmentInspectionStatusEnum.scheduled.name()))));
		statusValues.add(SelectionValues.entry("active", translate("inspection.status.active")));
		statusValues.add(SelectionValues.entry(AssessmentInspectionStatusEnum.inProgress.name(), translate("inspection.status.".concat(AssessmentInspectionStatusEnum.inProgress.name()))));
		statusValues.add(SelectionValues.entry(AssessmentInspectionStatusEnum.carriedOut.name(), translate("inspection.status.".concat(AssessmentInspectionStatusEnum.carriedOut.name()))));
		statusValues.add(SelectionValues.entry(AssessmentInspectionStatusEnum.cancelled.name(), translate("inspection.status.".concat(AssessmentInspectionStatusEnum.cancelled.name()))));
		statusValues.add(SelectionValues.entry(AssessmentInspectionStatusEnum.noShow.name(), translate("inspection.status.".concat(AssessmentInspectionStatusEnum.noShow.name()))));
		statusValues.add(SelectionValues.entry(AssessmentInspectionStatusEnum.withdrawn.name(), translate("inspection.status.".concat(AssessmentInspectionStatusEnum.withdrawn.name()))));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_INSPECTION_STATUS, statusValues, true));
		
		SelectionValues assessmentStatusValues = new SelectionValues();
		assessmentStatusValues.add(SelectionValues.entry("notReady", translate("filter.notReady")));
		assessmentStatusValues.add(SelectionValues.entry("notStarted", translate("filter.notStarted")));
		assessmentStatusValues.add(SelectionValues.entry("inProgress", translate("filter.inProgress")));
		assessmentStatusValues.add(SelectionValues.entry("inReview", translate("filter.inReview")));
		assessmentStatusValues.add(SelectionValues.entry("done", translate("filter.done")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.assessment.status"),
				FILTER_ASSESSMENT_STATUS, assessmentStatusValues, true));
		
		List<AssessmentInspectionConfiguration> configurations = inspectionService.getInspectionConfigurations(courseEntry);
		SelectionValues configurationsValues = new SelectionValues();
		for(AssessmentInspectionConfiguration configuration:configurations) {
			configurationsValues.add(SelectionValues.entry(configuration.getKey().toString(), configuration.getName()));
		}
		if(!configurationsValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.configuration"),
					FILTER_INSPECTION_CONFIGURATION, configurationsValues, false));
		}

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initCourseElementValues(SelectionValues courseNodesValues, CourseNode cNode) {
		if(cNode instanceof IQTESTCourseNode) {
			courseNodesValues.add(SelectionValues.entry(cNode.getIdent(), cNode.getShortTitle()));
		}
		
		int numOfChildren = cNode.getChildCount();
		for(int i=0; i<numOfChildren; i++) {
			INode child = cNode.getChildAt(i);
			if(child instanceof CourseNode childCourseNode) {
				initCourseElementValues(courseNodesValues, childCourseNode);
			}
		}
	}
	
	private void loadModel() {
		SearchAssessmentInspectionParameters params = getSearchParameters();
		ICourse course = CourseFactory.loadCourse(courseEntry);

		List<AssessmentEntryInspection> inspectionEntryList = inspectionService.searchInspection(params);
		List<AssessmentInspectionRow> rows = new ArrayList<>(inspectionEntryList.size());
		for(AssessmentEntryInspection inspectionEntry:inspectionEntryList) {
			rows.add(forgeRow(inspectionEntry, course));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AssessmentInspectionRow forgeRow(AssessmentEntryInspection inspectionEntry, ICourse course) {
		AssessmentInspection inspection = inspectionEntry.inspection();
		String courseNodeIdent = inspection.getSubIdent();
		CourseNode cNode = course.getRunStructure().getNode(courseNodeIdent);
		CourseNodeConfiguration courseNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(cNode.getType());
		
		String fullName = userManager.getUserDisplayName(inspectionEntry.identity());
		AssessmentInspectionRow row = new AssessmentInspectionRow(fullName, inspectionEntry.inspection(),
				inspectionEntry.assessmentStatus(), cNode.getShortTitle(), courseNodeConfig.getIconCSSClass());
		
		FormLink cancelLink = uifactory.addFormLink("cancel_" + (++counter), "cancel", "", null, null, Link.NONTRANSLATED);
		cancelLink.setIconLeftCSS("o_icon o_icon_cancel o_icon-fws o_icon-lg");
		cancelLink.setVisible(inspection.getInspectionStatus() == AssessmentInspectionStatusEnum.inProgress);
		row.setCancelButton(cancelLink);
		cancelLink.setUserObject(row);
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		row.setToolsButton(toolsLink);
		toolsLink.setUserObject(row);
		
		return row;
	}
	
	private SearchAssessmentInspectionParameters getSearchParameters() {
		SearchAssessmentInspectionParameters params = new SearchAssessmentInspectionParameters();
		params.setEntry(courseEntry);

		List<FlexiTableFilter> filters = tableEl.getFilters();
		if(courseNode != null) {
			params.setSubIdents(List.of(courseNode.getIdent()));
		} else {
			FlexiTableFilter courseNodeFilter = FlexiTableFilter.getFilter(filters, FILTER_COURSE_NODE);
			if (courseNodeFilter != null) {
				List<String> filterValues = ((FlexiTableExtendedFilter)courseNodeFilter).getValues();
				if (filterValues != null && !filterValues.isEmpty()) {
					List<String> subIdents = filterValues.stream()
							.toList();
					params.setSubIdents(subIdents);
				}
			}
		}
		
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, FILTER_INSPECTION_STATUS);
		if (statusFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)statusFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<AssessmentInspectionStatusEnum> statusList = filterValues.stream()
						.filter(AssessmentInspectionStatusEnum::isValueOf)
						.map(AssessmentInspectionStatusEnum::valueOf)
						.collect(Collectors.toList());
				if(filterValues.contains("active")) {
					params.setActiveInspections(Boolean.TRUE);
					statusList.add(AssessmentInspectionStatusEnum.scheduled);
				}
				params.setInspectionStatus(statusList);
				
			}
		}
		
		FlexiTableFilter assessmentStatusFilter = FlexiTableFilter.getFilter(filters, FILTER_ASSESSMENT_STATUS);
		if (assessmentStatusFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)assessmentStatusFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<AssessmentEntryStatus> statusList = filterValues.stream()
						.filter(AssessmentEntryStatus::isValueOf)
						.map(AssessmentEntryStatus::valueOf)
						.toList();
				params.setAssessmentStatus(statusList);
			}
		}
		
		FlexiTableFilter configurationFilter = FlexiTableFilter.getFilter(filters, FILTER_INSPECTION_CONFIGURATION);
		if (configurationFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)configurationFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<Long> configurationsList = filterValues.stream()
						.map(Long::valueOf)
						.toList();
				params.setConfigurationsKeys(configurationsList);
			}
		}
		
		return params;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		FlexiFiltersTab activatesTab = activateTab(entries);
		if(activatesTab != null) {
			tableEl.setSelectedFilterTab(ureq, activatesTab);
			loadModel();
		} else if(state instanceof AssessedIdentityListState listState) {
			FlexiFiltersTab tab = tableEl.getFilterTabById(listState.getTabId());
			if(tab != null) {
				tableEl.setSelectedFilterTab(ureq, tab);
			} else {
				tableEl.setSelectedFilterTab(ureq, allTab);
			}
			
			List<FlexiTableExtendedFilter> filters = tableEl.getExtendedFilters();
			listState.setValuesToFilter(filters);
			tableEl.setFilters(true, filters, false, false);
			tableEl.expandFilters(listState.isFiltersExpanded());
		} else {
			tableEl.setSelectedFilterTab(ureq, allTab);
		}
	}
	
	private FlexiFiltersTab activateTab(List<ContextEntry> entries) {
		if(entries == null || entries.isEmpty()) return null;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(AssessmentInspectionStatusEnum.inProgress.name().equals(type)) {
			return inProgressTab;
		}
		if(AssessmentInspectionStatusEnum.scheduled.name().equals(type)) {
			return scheduledTab;
		}
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == addMembersWizard || source == editInspectionWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
				cleanUp();
			}
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(confirmCancelCtrl == source || confirmWithdrawCtrl == source || editExtraTimeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editInspectionWizard);
		removeAsListenerAndDispose(editExtraTimeCtrl);
		removeAsListenerAndDispose(addMembersWizard);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		editInspectionWizard = null;
		editExtraTimeCtrl = null;
		addMembersWizard = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addMembersButton) {
			doAddMembers(ureq);
		} else if(source == bulkCancelButton) {
			doBulkCancel(ureq);
		} else if(source == bulkExtendDurationButton) {
			//
		} else if(source == bulkWithdrawButton) {
			doBulkWithdraw(ureq);
		} else if(source == tableEl) {
			if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if(source instanceof FormLink link) {
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof AssessmentInspectionRow row) {
				doOpenTools(ureq, row);
			} else if("cancel".equals(link.getCmd()) && link.getUserObject() instanceof AssessmentInspectionRow row) {
				doConfirmCancelInspection(ureq, row.getInspection());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doBulkCancel(UserRequest ureq) {
		List<AssessmentInspection> inspectionList = getSelectedInspectionList();
		if(inspectionList.isEmpty()) {
			showWarning("warning.atleastone");
		} else {
			confirmCancelCtrl = new ConfirmCancelInspectionController(ureq, getWindowControl(), inspectionList);
			listenTo(confirmCancelCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCancelCtrl.getInitialComponent(), true, translate("bulk.cancel"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doBulkWithdraw(UserRequest ureq) {
		List<AssessmentInspection> inspectionList = getSelectedInspectionList();
		if(inspectionList.isEmpty()) {
			showWarning("warning.atleastone");
		} else {
			confirmWithdrawCtrl = new ConfirmWithdrawInspectionController(ureq, getWindowControl(), inspectionList);
			listenTo(confirmWithdrawCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmWithdrawCtrl.getInitialComponent(), true, translate("bulk.withdraw"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private List<AssessmentInspection> getSelectedInspectionList() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		return selectedIndexes.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(AssessmentInspectionRow::getInspection)
				.collect(Collectors.toList());
	}
	
	private void doAddMembers(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);
		
		CreateInspectionContext context = new CreateInspectionContext(courseEntry, secCallback);
		context.setCourseNode(courseNode);
		
		Step start;
		if(courseNode == null) {
			start = new CreateInspection_1_CourseElementStep(ureq, context);
		} else {
			start = new CreateInspection_2_ParticipantsStep(ureq, context);
		}
		CreateInspectionFinishStepCallback finish = new CreateInspectionFinishStepCallback(context);
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("wizard.add.members.title"), "");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
	}
	
	private void doOpenTools(UserRequest ureq, AssessmentInspectionRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), row.getToolsButton().getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doConfirmCancelInspection(UserRequest ureq, AssessmentInspection inspection) {
		List<AssessmentInspection> inspectionList = List.of(inspection);
		confirmCancelCtrl = new ConfirmCancelInspectionController(ureq, getWindowControl(), inspectionList);
		listenTo(confirmCancelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCancelCtrl.getInitialComponent(), true, translate("bulk.cancel"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmWithdrawInspection(UserRequest ureq, AssessmentInspection inspection) {
		List<AssessmentInspection> inspectionList = List.of(inspection);
		confirmWithdrawCtrl = new ConfirmWithdrawInspectionController(ureq, getWindowControl(), inspectionList);
		listenTo(confirmWithdrawCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmWithdrawCtrl.getInitialComponent(), true, translate("bulk.withdraw"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doExtendDuration(UserRequest ureq, AssessmentInspection inspection) {
		editExtraTimeCtrl = new EditExtraTimeController(ureq, getWindowControl(),
				inspection, inspection.getConfiguration());
		listenTo(editExtraTimeCtrl);
		
		String fullName = userManager.getUserDisplayName(inspection.getIdentity());
		String title = translate("edit.extra.time.title", fullName);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editExtraTimeCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doShowActivityLog(UserRequest ureq, AssessmentInspection inspection) {
		activityLogCtrl = new AssessmentInspectionLogController(ureq, getWindowControl(), inspection);
		listenTo(activityLogCtrl);
		
		String fullName = userManager.getUserDisplayName(inspection.getIdentity());
		String title = translate("activity.log.inspection.title", fullName);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), activityLogCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditInspection(UserRequest ureq, AssessmentInspection inspection) {
		removeAsListenerAndDispose(editInspectionWizard);
		
		CreateInspectionContext context = new CreateInspectionContext(courseEntry, secCallback);
		if(courseNode != null) {
			context.setCourseNode(courseNode);
		} else {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			CourseNode node = course.getRunStructure().getNode(inspection.getSubIdent());
			context.setCourseNode(node);
		}

		Identity assessedIdentity = inspection.getIdentity();
		DisadvantageCompensation compensation = disadvantageCompensationService
				.getActiveDisadvantageCompensation(assessedIdentity, context.getCourseEntry(), context.getCourseNode().getIdent());
		context.setEditedInspection(inspection, compensation);
		context.setInspectionConfiguration(inspection.getConfiguration());
		
		Step start = new CreateInspection_3_InspectionStep(ureq, context);
		CreateInspectionFinishStepCallback finish = new CreateInspectionFinishStepCallback(context);
		editInspectionWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("wizard.add.members.title"), "");
		listenTo(editInspectionWizard);
		getWindowControl().pushAsModalDialog(editInspectionWizard.getInitialComponent());
	}

	private class ToolsController extends BasicController {
		
		private Link editLink;
		private Link activityLogLink;
		private Link extendDurationLink;
		private Link cancelInspectionLink;
		private Link withdrawInspectionLink;
		
		private final AssessmentInspection inspection;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AssessmentInspectionRow row) {
			super(ureq, wControl);
			inspection = inspectionService.getInspection(row.getInspection().getKey());
			final AssessmentInspectionStatusEnum status = inspection.getInspectionStatus();
			
			VelocityContainer mainVC = createVelocityContainer("tools");

			if(status == AssessmentInspectionStatusEnum.inProgress) {
				cancelInspectionLink = LinkFactory.createLink("cancel.inspection", getTranslator(), this);
				cancelInspectionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_cancel");
				mainVC.put("cancel.inspection", cancelInspectionLink);
			}
			
			if(status == AssessmentInspectionStatusEnum.scheduled
					&& (inspection.getToDate() == null || ureq.getRequestTimestamp().before(inspection.getToDate()))) {
				editLink = LinkFactory.createLink("edit", getTranslator(), this);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				mainVC.put("edit.inspection", editLink);

				withdrawInspectionLink = LinkFactory.createLink("withdraw.inspection", getTranslator(), this);
				withdrawInspectionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_deactivate");
				mainVC.put("withdraw.inspection", withdrawInspectionLink);
			}

			if(status == AssessmentInspectionStatusEnum.scheduled || status == AssessmentInspectionStatusEnum.inProgress) {
				extendDurationLink = LinkFactory.createLink("extend.duration.inspection", getTranslator(), this);
				extendDurationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
				mainVC.put("extend.duration.inspection", extendDurationLink);
			}
			
			activityLogLink = LinkFactory.createLink("activity.log.inspection", getTranslator(), this);
			activityLogLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log");
			mainVC.put("activity.log.inspection", activityLogLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(cancelInspectionLink == source) {
				doConfirmCancelInspection(ureq, inspection);
			} else if(withdrawInspectionLink == source) {
				doConfirmWithdrawInspection(ureq, inspection);
			} else if(activityLogLink == source) {
				doShowActivityLog(ureq, inspection);
			} else if(editLink == source) {
				doEditInspection(ureq, inspection);
			} else if(extendDurationLink == source) {
				doExtendDuration(ureq, inspection);
			}
		}
	}
}
