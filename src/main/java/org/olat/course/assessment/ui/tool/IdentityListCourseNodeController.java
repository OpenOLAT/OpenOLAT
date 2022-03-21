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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.TimeElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.bulk.PassedOverridenCellRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.model.SearchAssessedIdentityParams.Passed;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.assessment.ui.tool.event.ShowDetailsEvent;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessedIdentityController;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.CompletionItem;
import org.olat.modules.assessment.ui.component.GradeCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.modules.assessment.ui.event.CompletionEvent;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.wizard.GradeScaleAdjustCallback;
import org.olat.modules.grade.ui.wizard.GradeScaleAdjustStep;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the "abstract" class of the assessed identities list. If you want
 * to inherit from it, don't forget to copy the velocity template and adapt
 * it to your need.
 * 
 * Initial date: 06.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListCourseNodeController extends FormBasicController
	implements GenericEventListener, AssessmentCourseNodeController {

	public static final String TO_REVIEW_TAB_ID = "ToReview";
	public static final String TO_RELEASE_TAB_ID = "ToRelease";
	public static final String PASSED_TAB_ID = "Passed";
	public static final String FAILED_TAB_ID = "Failed";
	public static final String ALL_TAB_ID = "All";

	private int counter = 0;
	protected final CourseNode courseNode;
	protected final RepositoryEntry courseEntry;
	private final RepositoryEntry referenceEntry;
	private final CourseEnvironment courseEnv;
	protected final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private final boolean showTitle;
	private final boolean learningPath;
	private final AssessmentConfig assessmentConfig;
	protected final boolean canEditUserVisibility;

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab toReviewTab;
	private FlexiFiltersTab toReleaseTab;
	private FlexiFiltersTab passedTab;
	private FlexiFiltersTab failedTab;
	
	private Link nextLink;
	private Link previousLink;
	protected FlexiTableElement tableEl;
	private FormLink gradeScaleButton;
	private FormLink bulkDoneButton;
	private FormLink bulkEmailButton;
	private FormLink bulkApplyGradeButton;
	private FormLink bulkVisibleButton;
	private FormLink bulkHiddenButton;
	protected final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;
	protected IdentityListCourseNodeTableModel usersTableModel;
	
	private Controller toolsCtrl;
	protected CloseableModalController cmc;
	private List<Controller> bulkToolsList;
	private AssessedIdentityController currentIdentityCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ContactFormController contactCtrl;
	private StepsMainRunController gradeScaleEditCtrl;
	
	@Autowired
	protected DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private GradeModule gradeModuel;
	@Autowired
	private GradeService gradeService;
	
	public IdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, "identity_courseelement");
		this.showTitle = showTitle;
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(ContactFormController.class, getLocale(), getTranslator()));		
		
		this.courseNode = courseNode;
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.toolContainer = toolContainer;
		this.coachCourseEnv = coachCourseEnv;
		this.assessmentCallback = assessmentCallback;
		courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
		learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(courseEnv).getType());
		canEditUserVisibility = coachCourseEnv.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		
		if(courseNode.needsReferenceToARepositoryEntry()) {
			referenceEntry = courseNode.getReferencedRepositoryEntry();
		} else {
			referenceEntry = null;
		}
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		initMultiSelectionTools(ureq, flc);
		
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), courseEntry.getOlatResource());
	}
	
	
	@Override
	public AssessmentToolSecurityCallback getAssessmentCallback() {
		return assessmentCallback;
	}

	public RepositoryEntry getCourseRepositoryEntry() {
		return courseEntry;
	}
	
	public RepositoryEntry getReferencedRepositoryEntry() {
		return referenceEntry;
	}
	
	public CourseEnvironment getCourseEnvironment() {
		return courseEnv;
	}
	
	public AssessmentToolContainer getToolContainer() {
		return toolContainer;
	}

	@Override
	public AssessedIdentityListState getListState() {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		List<FlexiTableFilter> filters = tableEl.getFilters();
		return AssessedIdentityListState.valueOf(selectedTab, filters, tableEl.isFiltersExpanded());
	}
	
	/**
	 * Collect the user selected identities in the table as references.
	 * 
	 * @param filter A predicate to filter the rows (mandatory)
	 * @return A list of identities
	 */
	public List<IdentityRef> getSelectedIdentitiesRef(Predicate<AssessedIdentityElementRow> filter) {
		List<AssessedIdentityElementRow> rows = getSelectedRows(filter);
		if(rows.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<IdentityRef> selectedIdentities = new ArrayList<>();
		for(AssessedIdentityElementRow row:rows) {
			selectedIdentities.add(new IdentityRefImpl(row.getIdentityKey()));
		}
		return selectedIdentities;
	}
	
	/**
	 * Collect the user selected identities in the table and load them
	 * on the database.
	 * 
	 * @param filter A predicate to filter the rows (mandatory)
	 * @return A list of identities
	 */
	public List<Identity> getSelectedIdentities(Predicate<AssessedIdentityElementRow> filter) {
		List<IdentityRef> refs = getSelectedIdentitiesRef(filter);
		return securityManager.loadIdentityByRefs(refs);
	}
	
	public List<AssessedIdentityElementRow> getSelectedRows(Predicate<AssessedIdentityElementRow> filter) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessedIdentityElementRow> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if(row != null && filter.test(row)) {
				rows.add(row);
			}
		}
		return rows;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("showTitle", showTitle);
			layoutCont.contextPut("courseNodeTitle", courseNode.getShortTitle());
			layoutCont.contextPut("courseNodeCssClass", CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());
		}
		
		String select = isSelectable() ? "select" : null;

		//add the table
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, select, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		initAssessmentColumns(columnsModel);
		initStatusColumns(columnsModel);
		initModificationDatesColumns(columnsModel);
		initExternalGradingColumns(columnsModel);
		initCalloutColumns(columnsModel);

		usersTableModel = new IdentityListCourseNodeTableModel(columnsModel, courseNode, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_assessment_tool_table");
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(!coachCourseEnv.isCourseReadOnly());
		tableEl.setSortSettings(options);
		tableEl.setSelectAllEnable(true);
		initFilters();
		initFiltersPresets();
		tableEl.setAndLoadPersistedPreferences(ureq, getTableId());
	}
	
	protected final void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_assessment_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		if(Mode.setByNode == assessmentConfig.getScoreMode() || Mode.setByNode == assessmentConfig.getPassedMode()) {
			toReviewTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TO_REVIEW_TAB_ID, translate("filter.to.review"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_STATUS, "inReview")));
			toReviewTab.setFiltersExpanded(true);
			tabs.add(toReviewTab);
		
			toReleaseTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TO_RELEASE_TAB_ID, translate("filter.to.release"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_STATUS, "done"),
							FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_USER_VISIBILITY, "notReleased")));
			toReleaseTab.setFiltersExpanded(true);
			tabs.add(toReleaseTab);
		}
		
		if(Mode.none != assessmentConfig.getPassedMode()) {
			passedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PASSED_TAB_ID, translate("filter.passed"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_PASSED, "passed")));
			passedTab.setElementCssClass("o_sel_assessment_passed");
			passedTab.setFiltersExpanded(true);
			tabs.add(passedTab);
			
			failedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FAILED_TAB_ID, translate("filter.failed"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_PASSED, "failed")));
			failedTab.setElementCssClass("o_sel_assessment_failed");
			failedTab.setFiltersExpanded(true);
			tabs.add(failedTab);
		}
		
		if (learningPath) {
			tabs.forEach(tab -> {
				tab.addDefaultFilterValue(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_OBLIGATION, 
						List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name())));
			});
		}
		if (assessmentCallback.canAssessNonMembers()) {
			tabs.forEach(tab -> {
				tab.addDefaultFilterValue(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_MEMBERS, "membersOnly"));
			});
		}
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// life-cycle
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry("notReady", translate("filter.notReady")));
		statusValues.add(SelectionValues.entry("notStarted", translate("filter.notStarted")));
		statusValues.add(SelectionValues.entry("inProgress", translate("filter.inProgress")));
		statusValues.add(SelectionValues.entry("inReview", translate("filter.inReview")));
		statusValues.add(SelectionValues.entry("done", translate("filter.done")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				AssessedIdentityListState.FILTER_STATUS, statusValues, true));
		
		// passed
		if(Mode.none != assessmentConfig.getPassedMode()) {
			SelectionValues passedValues = new SelectionValues();
			passedValues.add(SelectionValues.entry(SearchAssessedIdentityParams.Passed.passed.name(), translate("filter.passed")));
			passedValues.add(SelectionValues.entry(SearchAssessedIdentityParams.Passed.failed.name(), translate("filter.failed")));
			passedValues.add(SelectionValues.entry(SearchAssessedIdentityParams.Passed.notGraded.name(), translate("filter.nopassed")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.passed.label"),
					AssessedIdentityListState.FILTER_PASSED, passedValues, true));
		}
		
		// user visibility
		if(Mode.setByNode == assessmentConfig.getScoreMode() || Mode.setByNode == assessmentConfig.getPassedMode()) {
			SelectionValues userVisibilityValues = new SelectionValues();
			userVisibilityValues.add(SelectionValues.entry("released", translate("filter.released")));
			userVisibilityValues.add(SelectionValues.entry("notReleased", translate("filter.not.released")));
			filters.add(new FlexiTableSingleSelectionFilter(translate("filter.release"),
					AssessedIdentityListState.FILTER_USER_VISIBILITY, userVisibilityValues, true));
		}
		
		// obligation
		if (learningPath) {
			SelectionValues obligationValues = new SelectionValues();
			obligationValues.add(SelectionValues.entry(AssessmentObligation.mandatory.name(), translate("filter.mandatory")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.optional.name(), translate("filter.optional")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.excluded.name(), translate("filter.excluded")));
			FlexiTableMultiSelectionFilter obligationFilter = new FlexiTableMultiSelectionFilter(translate("filter.obligation"),
					AssessedIdentityListState.FILTER_OBLIGATION, obligationValues, true);
			filters.add(obligationFilter);
		}
		
		// members
		if (assessmentCallback.canAssessNonMembers()) {
			SelectionValues memebersValues = new SelectionValues();
			memebersValues.add(SelectionValues.entry("membersOnly", translate("filter.members")));
			memebersValues.add(SelectionValues.entry("nonMembersOnly", translate("filter.other.users")));
			filters.add(new FlexiTableSingleSelectionFilter(translate("filter.members.label"),
					AssessedIdentityListState.FILTER_MEMBERS, memebersValues, true));
		}

		// groups
		SelectionValues groupValues = new SelectionValues();
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<BusinessGroup> coachedGroups;
			if(assessmentCallback.isAdmin()) {
				coachedGroups = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups();
			}

			if(!coachedGroups.isEmpty()) {
				for(BusinessGroup coachedGroup:coachedGroups) {
					String groupName = StringHelper.escapeHtml(coachedGroup.getName());
					groupValues.add(new SelectionValue("businessgroup-" + coachedGroup.getKey(), groupName, null,
							"o_icon o_icon_group", null, true));
				}
			}
		}
		
		if(assessmentCallback.canAssessCurriculumMembers()) {
			List<CurriculumElement> coachedCurriculumElements;
			if(assessmentCallback.isAdmin()) {
				coachedCurriculumElements = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllCurriculumElements();
			} else {
				coachedCurriculumElements = coachCourseEnv.getCoachedCurriculumElements();
			}
			
			if(!coachedCurriculumElements.isEmpty()) {
				for(CurriculumElement coachedCurriculumElement:coachedCurriculumElements) {
					String name = CurriculumHelper.getLabel(coachedCurriculumElement, getTranslator());
					groupValues.add(new SelectionValue("curriculumelement-" + coachedCurriculumElement.getKey(), name, null,
							"o_icon o_icon_curriculum_element", null, true));
				}
			}
		}
		
		if(!groupValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.groups"),
					AssessedIdentityListState.FILTER_GROUPS, groupValues, true));
		}
		
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			String propName = userPropertyHandler.getName();
			if(!UserConstants.FIRSTNAME.equals(propName) && !UserConstants.LASTNAME.equals(propName)
					&& !UserConstants.EMAIL.equals(propName) && !UserConstants.NICKNAME.equals(propName)
					&& userPropertyHandler instanceof Generic127CharTextPropertyHandler) {
				filters.add(new FlexiTableTextFilter(translate(userPropertyHandler.i18nColumnDescriptorLabelKey()), "user-prop-".concat(propName), true));
			}
		}

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if(tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		reload(ureq);
	}
	
	protected String getTableId() {
		return "assessment-tool-identity-list-v2";
	}
	
	protected void initAssessmentColumns(FlexiTableColumnModel columnsModel) {
		if(assessmentConfig.isAssessable()) {
			if(assessmentConfig.hasAttempts()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.attempts));
			}
			if(Mode.setByNode == assessmentConfig.getScoreMode() || Mode.setByNode == assessmentConfig.getPassedMode()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.userVisibility, new UserVisibilityCellRenderer(false)));
			}
			if(Mode.none != assessmentConfig.getScoreMode()) {
				boolean hasGrade = gradeModuel.isEnabled() && assessmentConfig.hasGrade();
				if(Mode.setByNode == assessmentConfig.getScoreMode()) {
					if(assessmentConfig.getMinScore() != null) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.min, new ScoreCellRenderer()));
					}
					if(assessmentConfig.getMaxScore() != null) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.max, new ScoreCellRenderer()));
					}
					if(Mode.none != assessmentConfig.getPassedMode() && assessmentConfig.getCutValue() != null && !hasGrade) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.cut, new ScoreCellRenderer()));
					}
				}
				initScoreColumns(columnsModel);
				if(hasGrade) {
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.grade, new GradeCellRenderer(getLocale())));
				}
			}
			if(assessmentConfig.isPassedOverridable()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.passedOverriden, new PassedOverridenCellRenderer()));
			}
			if(Mode.none != assessmentConfig.getPassedMode()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.passed, new PassedCellRenderer(getLocale())));
			}
			if(assessmentConfig.hasIndividualAsssessmentDocuments()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.numOfAssessmentDocs));
			}
		}
	}

	protected void initScoreColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.score, new ScoreCellRenderer()));
	}
	
	protected void initStatusColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));
	}
	
	protected void initModificationDatesColumns(FlexiTableColumnModel columnsModel) {
		String select = getSelectAction();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.lastModified, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.lastUserModified, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.lastCoachModified, select));
	}
	
	protected void initExternalGradingColumns(FlexiTableColumnModel columnsModel) {
		if(assessmentConfig.isExternalGrading()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.externalGrader));
		}
	}
	
	protected void initCalloutColumns(FlexiTableColumnModel columnsModel) {
		if(assessmentConfig.isAssessable()) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(IdentityCourseElementCols.tools);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
	}
	
	protected String getSelectAction() {
		return isSelectable() ? "select" : null;
	}
	
	protected boolean isSelectable() {
		return assessmentConfig.isAssessable();
	}
	
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		initBulkStatusTools(ureq, formLayout);
		initBulkEmailTool(ureq, formLayout);
	}

	protected void initBulkStatusTools(@SuppressWarnings("unused") UserRequest ureq, FormLayoutContainer formLayout) {
		if(assessmentConfig.isAssessable()) {
			bulkDoneButton = uifactory.addFormLink("bulk.done", formLayout, Link.BUTTON);
			bulkDoneButton.setElementCssClass("o_sel_assessment_bulk_done");
			bulkDoneButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
			bulkDoneButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			tableEl.addBatchButton(bulkDoneButton);
			
			if (gradeModuel.isEnabled() && Mode.none != assessmentConfig.getScoreMode() && assessmentConfig.hasGrade() && !assessmentConfig.isAutoGrade() 
					&& (coachCourseEnv.isAdmin() || coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY))) {
				bulkApplyGradeButton = uifactory.addFormLink("bulk.apply.grade", formLayout, Link.BUTTON);
				bulkApplyGradeButton.setElementCssClass("o_sel_assessment_apply_grade");
				bulkApplyGradeButton.setIconLeftCSS("o_icon o_icon-fw");
				bulkApplyGradeButton.setVisible(!coachCourseEnv.isCourseReadOnly());
				tableEl.addBatchButton(bulkApplyGradeButton);
			}
			
			if (canEditUserVisibility) {
				bulkVisibleButton = uifactory.addFormLink("bulk.visible", formLayout, Link.BUTTON);
				bulkVisibleButton.setElementCssClass("o_sel_assessment_bulk_visible");
				bulkVisibleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_results_visible");
				bulkVisibleButton.setVisible(!coachCourseEnv.isCourseReadOnly());
				tableEl.addBatchButton(bulkVisibleButton);
				
				bulkHiddenButton = uifactory.addFormLink("bulk.hidden", formLayout, Link.BUTTON);
				bulkHiddenButton.setElementCssClass("o_sel_assessment_bulk_hidden");
				bulkHiddenButton.setIconLeftCSS("o_icon o_icon-fw o_icon_results_hidden");
				bulkHiddenButton.setVisible(!coachCourseEnv.isCourseReadOnly());
				tableEl.addBatchButton(bulkHiddenButton);
			}
		}
	}
	
	protected void initBulkEmailTool(@SuppressWarnings("unused") UserRequest ureq, FormLayoutContainer formLayout) {
		if(assessmentConfig.isAssessable()) {
			bulkEmailButton = uifactory.addFormLink("bulk.email", formLayout, Link.BUTTON);
			bulkEmailButton.setElementCssClass("o_sel_assessment_bulk_email");
			bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			bulkEmailButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			tableEl.addBatchButton(bulkEmailButton);
		}
	}
	
	protected void initGradeScaleEditButton(FormLayoutContainer formLayout) {
		if (Mode.none != assessmentConfig.getScoreMode() && assessmentConfig.hasGrade()) {
			gradeScaleButton = uifactory.addFormLink("tool.grade.scale", formLayout, Link.BUTTON);
			gradeScaleButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		}
	}
	
	@Override
	public void reload(UserRequest ureq) {
		SearchAssessedIdentityParams params = getSearchParameters();
		
		// Get the identities and remove identity without assessment entry.
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);

		// Get the assessment entries and put it in a map.
		// Obligation filter is applied in this query.
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentToolManager.getAssessmentEntries(getIdentity(), params, null).stream()
			.filter(entry -> entry.getIdentity() != null)
			.forEach(entry -> entryMap.put(entry.getIdentity().getKey(), entry));
		
		// Apply filters
		assessedIdentities = applyFilters(assessedIdentities, entryMap, params);
		
		Map<Long,String> assessmentEntriesKeysToGraders = Collections.emptyMap();
		if(assessmentConfig.isExternalGrading()) {
			List<GradingAssignment> assignments = assessmentToolManager.getGradingAssignments(getIdentity(), params, null);
			assessmentEntriesKeysToGraders = assignments.stream()
					.collect(Collectors.toMap(assignment -> assignment.getAssessmentEntry().getKey(),
							assignment -> userManager.getUserDisplayName(assignment.getGrader().getIdentity()), (u, v) -> u));
		}

		List<AssessedIdentityElementRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			if(entry != null) {
				String grader = null;
				TimeElement currentStart = new TimeElement("current-start-" + (++counter), getLocale());
				CompletionItem currentCompletion = new CompletionItem("current-completion-" + (++counter), getLocale());
				currentStart.setDate(entry.getCurrentRunStartDate());
				currentCompletion.setCompletion(entry.getCurrentRunCompletion());
				AssessmentRunStatus status = entry.getCurrentRunStatus();
				currentCompletion.setEnded(status != null && AssessmentRunStatus.done.equals(status));
				grader = assessmentEntriesKeysToGraders.get(entry.getKey());
				
				FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
				toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			
				AssessedIdentityElementRow row = new AssessedIdentityElementRow(assessedIdentity, entry, grader,
						currentStart, currentCompletion, toolsLink, userPropertyHandlers, getLocale());
				toolsLink.setUserObject(row);
				rows.add(row);
			}
		}

		usersTableModel.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private List<Identity> applyFilters(List<Identity> identities, Map<Long, AssessmentEntry> identityToEntry,
			SearchAssessedIdentityParams params) {
		if(hasFilter(params)) {
			List<Identity> filteredIdentities = new ArrayList<>();
			for(Identity assessedIdentity:identities) {
				AssessmentEntry entry = identityToEntry.get(assessedIdentity.getKey());
				if(entry != null && matchesStatusFilter(params, entry) && matchesPassedFilter(params, entry) && matchesUserVisibilityFilter(params, entry)) {
					filteredIdentities.add(assessedIdentity);
				}
			}
			return filteredIdentities;
		}
		return identities;
	}
	
	private boolean hasFilter(SearchAssessedIdentityParams params) {
		return hasStatusFilter(params) 
				|| hasPassedFilter(params)
				|| hasUserVisibilityFilter(params);
	}

	private boolean hasStatusFilter(SearchAssessedIdentityParams params) {
		return params.getAssessmentStatus() != null && !params.getAssessmentStatus().isEmpty();
	}
	
	private boolean matchesStatusFilter(SearchAssessedIdentityParams params, AssessmentEntry entry) {
		if (hasStatusFilter(params)) {
			return params.getAssessmentStatus().contains(entry.getAssessmentStatus())
					// AssessmentEntry without status is displayed as notStarted
					|| (params.getAssessmentStatus().contains(AssessmentEntryStatus.notStarted) && entry.getAssessmentStatus() == null);
		}
		return true;
	}

	private boolean hasPassedFilter(SearchAssessedIdentityParams params) {
		return params.getPassed() != null && !params.getPassed().isEmpty();
	}
	
	private boolean matchesPassedFilter(SearchAssessedIdentityParams params, AssessmentEntry entry) {
		if (hasPassedFilter(params)) {
			return (params.getPassed().contains(Passed.passed) && entry.getPassed() != null && entry.getPassed().booleanValue())
					|| (params.getPassed().contains(Passed.failed) && entry.getPassed() != null && !entry.getPassed().booleanValue())
					|| (params.getPassed().contains(Passed.notGraded) && entry.getPassed() == null);
		}
		return true;
	}

	private boolean hasUserVisibilityFilter(SearchAssessedIdentityParams params) {
		return params.getUserVisibility() != null;
	}
	
	private boolean matchesUserVisibilityFilter(SearchAssessedIdentityParams params, AssessmentEntry entry) {
		if (hasUserVisibilityFilter(params)) {
			return (params.getUserVisibility().booleanValue() && entry.getUserVisibility().booleanValue())
					|| (!params.getUserVisibility().booleanValue() && !entry.getUserVisibility().booleanValue());
		}
		return true;
	}
	
	protected SearchAssessedIdentityParams getSearchParameters() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), null, assessmentCallback);
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_STATUS);
		if (statusFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)statusFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<AssessmentEntryStatus> passed = filterValues.stream()
						.filter(AssessmentEntryStatus::isValueOf)
						.map(AssessmentEntryStatus::valueOf)
						.collect(Collectors.toList());
				params.setAssessmentStatus(passed);
			}
		}
		
		FlexiTableFilter passedFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_PASSED);
		if (passedFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)passedFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<SearchAssessedIdentityParams.Passed> passed = filterValues.stream()
						.map(SearchAssessedIdentityParams.Passed::valueOf)
						.collect(Collectors.toList());
				params.setPassed(passed);
			}
		}
		
		FlexiTableFilter userVisibilityFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_USER_VISIBILITY);
		if(userVisibilityFilter != null) {
			String filterValue = ((FlexiTableExtendedFilter)userVisibilityFilter).getValue();
			if("released".equals(filterValue)) {
				params.setUserVisibility(Boolean.TRUE);
			} else if("notReleased".equals(filterValue)) {
				params.setUserVisibility(Boolean.FALSE);
			}
		}
		
		FlexiTableFilter membersFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_MEMBERS);
		if(membersFilter != null) {
			String filterValue = ((FlexiTableExtendedFilter)membersFilter).getValue();
			if("membersOnly".equals(filterValue)) {
				params.setMemebersOnly(true);
			} else if("nonMembersOnly".equals(filterValue)) {
				params.setNonMemebersOnly(true);
			}
		}
		
		FlexiTableFilter obligationFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_OBLIGATION);
		if (obligationFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)obligationFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<AssessmentObligation> assessmentObligations = filterValues.stream()
						.map(AssessmentObligation::valueOf)
						.collect(Collectors.toList());
				params.setAssessmentObligations(assessmentObligations);
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
		
		Map<String,String> userProps = new HashMap<>();
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			String propName = userPropertyHandler.getName();
			if(!UserConstants.FIRSTNAME.equals(propName) && !UserConstants.LASTNAME.equals(propName)
					&& !UserConstants.EMAIL.equals(propName) && !UserConstants.NICKNAME.equals(propName)
					&& userPropertyHandler instanceof Generic127CharTextPropertyHandler) {
				FlexiTableFilter userPropFilter = FlexiTableFilter.getFilter(filters, "user-prop-".concat(propName));
				if(userPropFilter != null && StringHelper.containsNonWhitespace(userPropFilter.getValue())) {
					userProps.put(propName, userPropFilter.getValue());
				}
			}
		}

		params.setBusinessGroupKeys(businessGroupKeys);
		params.setCurriculumElementKeys(curriculumElementKeys);
		params.setSearchString(tableEl.getQuickSearchString());
		params.setUserProperties(userProps);
		return params;
	}
	
	protected AssessmentToolOptions getOptions() {
		SearchAssessedIdentityParams params = getSearchParameters();
		AssessmentToolOptions options = new AssessmentToolOptions();
		options.setAdmin(assessmentCallback.isAdmin());
		if(assessmentCallback.isAdmin()) {
			options.setNonMembers(params.isNonMembers());
		} else {
			List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
			options.setIdentities(assessedIdentities);
			fillAlternativeToAssessableIdentityList(options, params);
		}
		return options;
	}
	
	private void fillAlternativeToAssessableIdentityList(AssessmentToolOptions options, SearchAssessedIdentityParams params) {
		List<Group> baseGroups = new ArrayList<>();
		if(assessmentCallback.canAssessRepositoryEntryMembers() || assessmentCallback.canAssessNonMembers()) {
			baseGroups.add(repositoryService.getDefaultGroup(courseEntry));
		}
		if(assessmentCallback.canAssessBusinessGoupMembers() && assessmentCallback.getCoachedGroups() != null && !assessmentCallback.getCoachedGroups().isEmpty()) {
			for(BusinessGroup coachedGroup:assessmentCallback.getCoachedGroups()) {
				baseGroups.add(coachedGroup.getBaseGroup());
			}
		}
		if(assessmentCallback.canAssessCurriculumMembers()) {
			List<CurriculumElement> coachedCurriculumElements = coachCourseEnv.getCoachedCurriculumElements();
			for(CurriculumElement coachedCurriculumElement:coachedCurriculumElements) {
				baseGroups.add(coachedCurriculumElement.getGroup());
			}
		}
		options.setAlternativeGroupsOfIdentities(baseGroups);
		options.setNonMembers(params.isNonMembers());
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		coordinatorManager.getCoordinator().getEventBus()
			.deregisterFor(this, courseEntry.getOlatResource());
        super.doDispose();
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof CompletionEvent) {
			CompletionEvent ce = (CompletionEvent)event;
			if(courseNode.getIdent().equals(ce.getSubIdent())) {
				doUpdateCompletion(ce.getStart(), ce.getCompletion(), ce.getStatus(), ce.getIdentityKey());
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof AssessedIdentityListState) {
			AssessedIdentityListState listState = (AssessedIdentityListState)state;
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

		reload(ureq);
		
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String resourceType = entry.getOLATResourceable().getResourceableTypeName();
			if("Identity".equals(resourceType)) {
				Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
				for(int i=usersTableModel.getRowCount(); i--> 0; ) {
					AssessedIdentityElementRow row = usersTableModel.getObject(i);
					if(row.getIdentityKey().equals(identityKey)) {
						Controller ctrl = doSelect(ureq, row);
						if(ctrl instanceof Activateable2) {
							List<ContextEntry> subEntries = entries.subList(1, entries.size());
							((Activateable2)ctrl).activate(ureq, subEntries, entry.getTransientState());
						}
					}
				}
			}	
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(previousLink == source) {
			doPrevious(ureq);
		} else if(nextLink == source) {
			doNext(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(currentIdentityCtrl == source) {
			if(event instanceof AssessmentFormEvent) {
				AssessmentFormEvent aee = (AssessmentFormEvent)event;
				reload(ureq);
				if(aee.isClose()) {
					stackPanel.popController(currentIdentityCtrl);
				}
			} else if(event == Event.CHANGED_EVENT) {
				reload(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				reload(ureq);
				stackPanel.popController(currentIdentityCtrl);
			}
		} else if(gradeScaleEditCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reload(ureq);
				}
				cleanUp();
			}
		} else if(bulkToolsList != null && bulkToolsList.contains(source)) {
			if(event == Event.CHANGED_EVENT) {
				reload(ureq);
			}
		} else if (source == contactCtrl) {
			if(cmc != null) {
				cmc.deactivate();
			}
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event instanceof ShowDetailsEvent) {
				doSelect(ureq, ((ShowDetailsEvent)event).getAssessedIdentity());
				toolsCalloutCtrl.deactivate();
				cleanUp();
			} else if(event == Event.CHANGED_EVENT) {
				reload(ureq);
				toolsCalloutCtrl.deactivate();
				cleanUp();
			} else if(event == Event.CLOSE_EVENT) {
				//don't dispose it, there are some popup window at work
				toolsCalloutCtrl.deactivate();
			} else if(event == Event.CANCELLED_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(gradeScaleEditCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		gradeScaleEditCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		contactCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessedIdentityElementRow row = usersTableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				reload(ureq);
			}
		} else if(gradeScaleButton == source) {
			doEditGradeScale(ureq);
		} else if(bulkDoneButton == source) {
			doSetDone(ureq);
		} else if(bulkApplyGradeButton == source) {
			doApplyGrade(ureq);
		} else if(bulkVisibleButton == source) {
			doSetUserVisibility(ureq, true);
		} else if(bulkHiddenButton == source) {
			doSetUserVisibility(ureq, false);
		} else if(bulkEmailButton == source) {
			doEmail(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, (AssessedIdentityElementRow)link.getUserObject(), link);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenTools(UserRequest ureq, AssessedIdentityElementRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		toolsCtrl = createCalloutController(ureq, assessedIdentity);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	protected Controller createCalloutController(UserRequest ureq, Identity assessedIdentity) {
		return new IdentityListCourseNodeToolsController(ureq, getWindowControl(), courseNode, assessedIdentity,
				coachCourseEnv);
	}
	
	private void doNext(UserRequest ureq) {
		stackPanel.popController(currentIdentityCtrl);
		
		Identity currentIdentity = currentIdentityCtrl.getAssessedIdentity();
		int index = getIndexOf(currentIdentity);
		if(index >= 0) {
			int nextIndex = index + 1;//next
			if(nextIndex >= 0 && nextIndex < usersTableModel.getRowCount()) {
				doSelect(ureq, usersTableModel.getObject(nextIndex));
			} else if(usersTableModel.getRowCount() > 0) {
				doSelect(ureq, usersTableModel.getObject(0));
			}
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		stackPanel.popController(currentIdentityCtrl);
		
		Identity currentIdentity = currentIdentityCtrl.getAssessedIdentity();
		int index = getIndexOf(currentIdentity);
		if(index >= 0) {
			int previousIndex = index - 1;//next
			if(previousIndex >= 0 && previousIndex < usersTableModel.getRowCount()) {
				doSelect(ureq, usersTableModel.getObject(previousIndex));
			} else if(usersTableModel.getRowCount() > 0) {
				doSelect(ureq, usersTableModel.getObject(usersTableModel.getRowCount() - 1));
			}
		}
	}
	
	private int getIndexOf(Identity identity) {
		for(int i=usersTableModel.getRowCount(); i-->0; ) {
			Long rowIdentityKey = usersTableModel.getObject(i).getIdentityKey();
			if(rowIdentityKey.equals(identity.getKey())) {
				return i;
			}
		}
		return -1;
	}
	
	private void doSelect(UserRequest ureq, Identity assessedIdentity) {
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		AssessedIdentityElementRow selectedRow = null;
		for(AssessedIdentityElementRow row:rows) {
			if(assessedIdentity.getKey().equals(row.getIdentityKey())) {
				selectedRow = row;
				break;
			}
		}
		
		if(selectedRow != null && !isAssessedIdentityLocked(ureq, assessedIdentity)) {
			doSelect(ureq, selectedRow);
		}
	}
	
	/**
	 * Preventive check if the identity is already locked by an other
	 * user and show a warning message if needed.
	 *  
	 * @param ureq The user request
	 * @param assessedIdentity The identity to assess
	 * @return
	 */
	private boolean isAssessedIdentityLocked(UserRequest ureq, Identity assessedIdentity) {
		if(courseNode.getParent() == null) return false;

		ICourse course = CourseFactory.loadCourse(courseEntry);
		String locksubkey = AssessmentIdentityCourseNodeController.lockKey(courseNode, assessedIdentity);
		if(CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(course, locksubkey)) {
			LockEntry lock = CoordinatorManager.getInstance().getCoordinator().getLocker().getLockEntry(course, locksubkey);
			if(lock != null && lock.getOwner() != null && !lock.getOwner().equals(getIdentity())) {
				String msg = DialogBoxUIFactory.getLockedMessage(ureq, lock, "assessmentLock", getTranslator());
				getWindowControl().setWarning(msg);
				return true;
			}
		}
		
		return false;
	}
	
	private Controller doSelect(UserRequest ureq, AssessedIdentityElementRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(assessedIdentity);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Identity", assessedIdentity.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		if(courseNode.getParent() == null) {
			currentIdentityCtrl = new AssessmentIdentityCourseController(ureq, bwControl, stackPanel,
					courseEntry, coachCourseEnv, assessedIdentity, true);
		} else {
			currentIdentityCtrl = new AssessmentIdentityCourseNodeController(ureq, getWindowControl(), stackPanel,
					courseEntry, courseNode, coachCourseEnv, assessedIdentity, true, showTitle);
		}
		listenTo(currentIdentityCtrl);
		stackPanel.pushController(fullName, currentIdentityCtrl);
		
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		stackPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));
		stackPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
		return currentIdentityCtrl;
	}
	
	private void doSetUserVisibility(UserRequest ureq, boolean visible) {
		RepositoryEntry cEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		ICourse course = CourseFactory.loadCourse(cEntry);
		Boolean visibility = Boolean.valueOf(visible);
		
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if(row != null) {
				doSetUserVisibility(course, row.getIdentityKey(), visibility);
			}
		}
		reload(ureq);
	}
	
	private void doSetUserVisibility(ICourse course, Long identityKey, Boolean userVisibility) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), scoreEval.getAssessmentStatus(),
				userVisibility, scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv, getIdentity(),
				false, Role.coach);
		dbInstance.commitAndCloseSession();
	}
	
	private void doEmail(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<Identity> identities = new ArrayList<>(selections.size());
		for (Integer i : selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if (row != null) {
				Identity identity = securityManager.loadIdentityByKey(row.getIdentityKey());
				identities.add(identity);
			}
		}

		if (identities.isEmpty()) {
			showWarning("error.msg.send.no.rcps");
		} else {
			ContactMessage contactMessage = new ContactMessage(getIdentity());
			String name = this.courseEntry.getDisplayname();
			ContactList contactList = new ContactList(name);
			contactList.addAllIdentites(identities);
			contactMessage.addEmailTo(contactList);

			removeAsListenerAndDispose(contactCtrl);
			contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
			listenTo(contactCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					contactCtrl.getInitialComponent(), true, translate("bulk.email"));
			cmc.activate();
			listenTo(cmc);
		}

	}
	
	private void doSetDone(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessedIdentityElementRow> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if(row != null && row.getAssessmentStatus() != AssessmentEntryStatus.done) {
				rows.add(row);
			}
		}

		if(rows.isEmpty()) {
			showWarning("warning.bulk.done");
		} else if(assessmentConfig.isAssessable()) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			for(AssessedIdentityElementRow row:rows) {
				Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				doSetStatus(assessedIdentity, AssessmentEntryStatus.done, courseNode, course);
				dbInstance.commitAndCloseSession();
			}
			reload(ureq);
		}
	}
	
	protected void doSetStatus(Identity assessedIdentity, AssessmentEntryStatus status, CourseNode cNode, ICourse course) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(cNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), status, null,
				scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(cNode, doneEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);
	}
	
	private void doApplyGrade(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessedIdentityElementRow> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if(row != null && !StringHelper.containsNonWhitespace(row.getGrade())) {
				rows.add(row);
			}
		}

		if(assessmentConfig.isAssessable()) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
			for(AssessedIdentityElementRow row:rows) {
				Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				doApplyGrade(assessedIdentity, courseNode, course, gradeScoreRanges);
				dbInstance.commitAndCloseSession();
			}
			reload(ureq);
		}
	}
	
	protected void doApplyGrade(Identity assessedIdentity, CourseNode cNode, ICourse course, NavigableSet<GradeScoreRange> gradeScoreRanges) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(cNode, assessedUserCourseEnv);
		String grade = scoreEval.getGrade();
		String performanceClassIdent = scoreEval.getPerformanceClassIdent();
		if (scoreEval.getScore() != null) {
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
			grade = gradeScoreRange.getGrade();
			performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
		}
		ScoreEvaluation applyGradeEval = new ScoreEvaluation(scoreEval.getScore(), grade,
				performanceClassIdent, scoreEval.getPassed(), scoreEval.getAssessmentStatus(), null,
				scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(cNode, applyGradeEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);
	}
	
	private void doUpdateCompletion(Date start, Double completion, AssessmentRunStatus status, Long assessedIdentityKey) {
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			if(assessedIdentityKey.equals(row.getIdentityKey())) {
				doUpdateCompletion(start, completion, status, row);
				break;
			}
		}
	}
	
	private void doUpdateCompletion(Date start, Double completion, AssessmentRunStatus status, AssessedIdentityElementRow row) {
		row.getCurrentRunStart().setDate(start);
		row.getCurrentCompletion().setCompletion(completion);
		boolean endedRow = row.getCurrentCompletion().isEnded();
		boolean endedEvent = status != null && AssessmentRunStatus.done.equals(status);
		row.getCurrentCompletion().setEnded(endedEvent);
		IdentityRef assessedIdentity = new IdentityRefImpl(row.getIdentityKey());
		AssessmentEntry assessmentEntry = assessmentToolManager.getAssessmentEntries(assessedIdentity, courseEntry, courseNode.getIdent());
		boolean statusChanged = assessmentEntry.getAssessmentStatus() != row.getAssessmentStatus();
		if(statusChanged || (endedEvent && !endedRow)) {
			String grader = null;
			if(assessmentConfig.isExternalGrading()) {
				RepositoryEntry testEntry = referenceEntry == null ? courseEntry : referenceEntry;
				GradingAssignment assignment = gradingService.getGradingAssignment(testEntry, assessmentEntry);
				if(assignment != null && assignment.getGrader() != null) {
					grader = userManager.getUserDisplayName(assignment.getGrader().getIdentity());
				}
			}
			row.setAssessmentEntry(assessmentEntry, grader);
			tableEl.getComponent().setDirty(true);
		}
	}
	
	private void doEditGradeScale(UserRequest ureq) {
		GradeScaleAdjustStep step = new GradeScaleAdjustStep(ureq, courseEntry, courseNode, assessmentConfig.isAutoGrade());
		StepRunnerCallback finish = new GradeScaleAdjustCallback(coachCourseEnv, getLocale());
		
		removeAsListenerAndDispose(gradeScaleEditCtrl);
		gradeScaleEditCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finish, null, translate("tool.grade.scale"), "");
		listenTo(gradeScaleEditCtrl);
		getWindowControl().pushAsModalDialog(gradeScaleEditCtrl.getInitialComponent());
	}
}
