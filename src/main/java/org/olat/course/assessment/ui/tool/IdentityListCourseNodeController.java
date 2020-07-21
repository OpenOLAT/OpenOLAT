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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.assessment.ui.tool.event.ShowDetailsEvent;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CalculatedAssessableCourseNode;
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
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessedIdentityController;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.CompletionItem;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.modules.assessment.ui.event.CompletionEvent;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
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
	implements Activateable2, GenericEventListener, AssessmentCourseNodeController {

	private int counter = 0;
	protected final BusinessGroup group;
	protected final CourseNode courseNode;
	protected final RepositoryEntry courseEntry;
	private final RepositoryEntry referenceEntry;
	private final CourseEnvironment courseEnv;
	private final boolean isAdministrativeUser;
	protected final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	protected final AssessmentToolSecurityCallback assessmentCallback;
	
	private Link nextLink;
	private Link previousLink;
	protected FlexiTableElement tableEl;
	private FormLink bulkDoneButton;
	private FormLink bulkVisibleButton;
	protected final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;
	protected IdentityListCourseNodeTableModel usersTableModel;
	
	private Controller toolsCtrl;
	protected CloseableModalController cmc;
	private List<Controller> bulkToolsList;
	private AssessedIdentityController currentIdentityCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmUserVisibilityController changeUserVisibilityCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public IdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identity_courseelement");
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.group = group;
		this.courseNode = courseNode;
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.toolContainer = toolContainer;
		this.coachCourseEnv = coachCourseEnv;
		this.assessmentCallback = assessmentCallback;
		courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
		
		if(courseNode.needsReferenceToARepositoryEntry()) {
			referenceEntry = courseNode.getReferencedRepositoryEntry();
		} else {
			referenceEntry = null;
		}
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		initMultiSelectionTools(ureq, flc);
		
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), courseEntry.getOlatResource());
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
		List<FlexiTableFilter> filters = tableEl.getSelectedFilters();
		String filter = null;
		if(filters != null && !filters.isEmpty()) {
			filter = filters.get(0).getFilter();
		}
		return new AssessedIdentityListState(filter, tableEl.getSelectedExtendedFilters());
	}

	@Override
	public List<IdentityRef> getSelectedIdentities() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessedIdentityElementRow> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if(row != null && row.getAssessmentStatus() != AssessmentEntryStatus.done) {
				rows.add(row);
			}
		}

		if(rows.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<IdentityRef> selectedIdentities = new ArrayList<>();
		for(AssessedIdentityElementRow row:rows) {
			selectedIdentities.add(new IdentityRefImpl(row.getIdentityKey()));
		}
		return selectedIdentities;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("courseNodeTitle", courseNode.getShortTitle());
			layoutCont.contextPut("courseNodeCssClass", CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());
		
			if(group != null) {
				layoutCont.contextPut("businessGroupName", group.getName());
			}
		}
		
		String select = isSelectable() ? "select" : null;

		//add the table
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			options.setDefaultOrderBy(new SortKey(IdentityCourseElementCols.username.sortKey(), true));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.username, select));
		}
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, select, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		initAssessmentColumns(columnsModel);
		initStatusColumns(columnsModel);
		initModificationDatesColumns(columnsModel);
		initCalloutColumns(columnsModel);

		AssessableCourseNode assessableNode = courseNode instanceof AssessableCourseNode ? (AssessableCourseNode)courseNode : null;
		usersTableModel = new IdentityListCourseNodeTableModel(columnsModel, assessableNode, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(new AssessedIdentityListProvider(getIdentity(), courseEntry, referenceEntry, courseNode.getIdent(), assessmentCallback), ureq.getUserSession());
		tableEl.setMultiSelect(!coachCourseEnv.isCourseReadOnly());
		tableEl.setSortSettings(options);
		tableEl.setSelectAllEnable(true);
		tableEl.setFilters("", getFilters(), false);
		List<FlexiTableFilter> extendedFilters = getExtendedFilters();
		if(!extendedFilters.isEmpty()) {
			tableEl.setExtendedFilterButton(translate("filter.groups"), extendedFilters);
		}
		tableEl.setAndLoadPersistedPreferences(ureq, getTableId());
	}
	
	protected List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("filter.passed"), "passed"));
		filters.add(new FlexiTableFilter(translate("filter.failed"), "failed"));
		filters.add(new FlexiTableFilter(translate("filter.inProgress"), "inProgress"));
		filters.add(new FlexiTableFilter(translate("filter.inReview"), "inReview"));
		filters.add(new FlexiTableFilter(translate("filter.done"), "done"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.showAll"), "showAll", true));
		return filters;
	}
	
	protected List<FlexiTableFilter> getExtendedFilters() {
		List<FlexiTableFilter> extendedFilters = new ArrayList<>();
		if(group == null) {
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
						extendedFilters.add(new FlexiTableFilter(groupName, "businessgroup-" + coachedGroup.getKey(), "o_icon o_icon_group"));
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
					if(!extendedFilters.isEmpty()) {
						extendedFilters.add(FlexiTableFilter.SPACER);
					}
					for(CurriculumElement coachedCurriculumElement:coachedCurriculumElements) {
						String groupName = StringHelper.escapeHtml(coachedCurriculumElement.getDisplayName());
						extendedFilters.add(new FlexiTableFilter(groupName, "curriculumelement-" + coachedCurriculumElement.getKey(), "o_icon o_icon_curriculum_element"));
					}
				}
			}
		}
		
		return extendedFilters;
	}
	
	protected String getTableId() {
		return "assessment-tool-identity-list";
	}
	
	protected void initAssessmentColumns(FlexiTableColumnModel columnsModel) {
		if(courseNode instanceof AssessableCourseNode) {
			AssessableCourseNode assessableNode = (AssessableCourseNode)courseNode;
			
			if(assessableNode.hasAttemptsConfigured()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.attempts));
			}
			if(!(courseNode instanceof CalculatedAssessableCourseNode)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.userVisibility,
						new UserVisibilityCellRenderer(getTranslator())));
			}
			if(assessableNode.hasScoreConfigured()) {
				if(!(assessableNode instanceof STCourseNode)) {
					if(assessableNode.getMinScoreConfiguration() != null) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.min, new ScoreCellRenderer()));
					}
					if(assessableNode.getMaxScoreConfiguration() != null) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.max, new ScoreCellRenderer()));
					}
					if(assessableNode.hasPassedConfigured() && assessableNode.getCutValueConfiguration() != null) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.cut, new ScoreCellRenderer()));
					}
				}
				initScoreColumns(columnsModel);
			}
			if(assessableNode.hasPassedConfigured()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.passed, new PassedCellRenderer()));
			}
			if(assessableNode.hasIndividualAsssessmentDocuments()) {
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
	
	protected void initCalloutColumns(FlexiTableColumnModel columnsModel) {
		if(courseNode instanceof AssessableCourseNode) {
			DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(IdentityCourseElementCols.tools);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
	}
	
	protected String getSelectAction() {
		return isSelectable() ? "select" : null;
	}
	
	protected boolean isSelectable() {
		return courseNode instanceof AssessableCourseNode;
	}
	
	protected void initMultiSelectionTools(@SuppressWarnings("unused") UserRequest ureq, FormLayoutContainer formLayout) {
		if(courseNode instanceof AssessableCourseNode) {
			bulkDoneButton = uifactory.addFormLink("bulk.done", formLayout, Link.BUTTON);
			bulkDoneButton.setElementCssClass("o_sel_assessment_bulk_done");
			bulkDoneButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			
			bulkVisibleButton = uifactory.addFormLink("bulk.visible", formLayout, Link.BUTTON);
			bulkVisibleButton.setElementCssClass("o_sel_assessment_bulk_visible");
			bulkVisibleButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		}
	}
	
	protected void loadModel(@SuppressWarnings("unused") UserRequest ureq) {
		SearchAssessedIdentityParams params = getSearchParameters();
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentEntries.stream()
			.filter(entry -> entry.getIdentity() != null)
			.forEach(entry -> entryMap.put(entry.getIdentity().getKey(), entry));

		List<AssessedIdentityElementRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			
			CompletionItem currentCompletion = new CompletionItem("current-completion-" + (++counter), getLocale());
			if(entry != null) {
				currentCompletion.setCompletion(entry.getCurrentRunCompletion());
				AssessmentRunStatus status = entry.getCurrentRunStatus();
				currentCompletion.setEnded(status != null && AssessmentRunStatus.done.equals(status));
			}
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		
			AssessedIdentityElementRow row = new AssessedIdentityElementRow(assessedIdentity, entry,
					currentCompletion, toolsLink, userPropertyHandlers, getLocale());
			toolsLink.setUserObject(row);
			rows.add(row);
		}

		usersTableModel.setObjects(rows);
		List<FlexiTableFilter> filters = tableEl.getSelectedFilters();
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			usersTableModel.filter(tableEl.getQuickSearchString(), Collections.singletonList(filters.get(0)));
		}
		tableEl.reset();
		tableEl.reloadData();
	}
	
	protected SearchAssessedIdentityParams getSearchParameters() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), referenceEntry, assessmentCallback);
		
		List<FlexiTableFilter> filters = tableEl.getSelectedFilters();
		List<FlexiTableFilter> extendedFilters = tableEl.getSelectedExtendedFilters();
		
		List<AssessmentEntryStatus> assessmentStatus = null;
		if(filters != null && !filters.isEmpty()) {
			assessmentStatus = new ArrayList<>(filters.size());
			for(FlexiTableFilter filter:filters) {
				if("passed".equals(filter.getFilter())) {
					params.setPassed(true);
				} else if("failed".equals(filter.getFilter())) {
					params.setFailed(true);
				} else if(AssessmentEntryStatus.isValueOf(filter.getFilter())){
					assessmentStatus.add(AssessmentEntryStatus.valueOf(filter.getFilter()));
				}
			}
		}
		params.setAssessmentStatus(assessmentStatus);
		
		List<Long> businessGroupKeys = null;
		List<Long> curriculumElementKeys = null;
		if(group != null) {
			businessGroupKeys = Collections.singletonList(group.getKey());
		} else if(extendedFilters != null && !extendedFilters.isEmpty()) {
			businessGroupKeys = new ArrayList<>();
			curriculumElementKeys = new ArrayList<>();
			for(FlexiTableFilter extendedFilter:extendedFilters) {
				String filter = extendedFilter.getFilter();
				int index = extendedFilter.getFilter().indexOf('-');
				if(index > 0) {
					Long key = Long.valueOf(filter.substring(index + 1));
					if(filter.startsWith("businessgroup-")) {
						businessGroupKeys.add(key);
					} else if(filter.startsWith("curriculumelement-")) {
						curriculumElementKeys.add(key);
					}
				}
				
				
			}
		}
		params.setBusinessGroupKeys(businessGroupKeys);
		params.setCurriculumElementKeys(curriculumElementKeys);
		params.setSearchString(tableEl.getQuickSearchString());
		return params;
	}
	
	protected AssessmentToolOptions getOptions() {
		SearchAssessedIdentityParams params = getSearchParameters();
		AssessmentToolOptions options = new AssessmentToolOptions();
		options.setAdmin(assessmentCallback.isAdmin());
		if(group == null) {
			if(assessmentCallback.isAdmin()) {
				options.setNonMembers(params.isNonMembers());
			} else {
				List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
				options.setIdentities(assessedIdentities);
				fillAlternativeToAssessableIdentityList(options, params);
			}
		} else {
			options.setGroup(group);
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
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof CompletionEvent) {
			CompletionEvent ce = (CompletionEvent)event;
			if(courseNode.getIdent().equals(ce.getSubIdent())) {
				doUpdateCompletion(ce.getCompletion(), ce.getStatus(), ce.getIdentityKey());
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		String filter = null;
		List<FlexiTableFilter> extendedFilters = null;
		if(state instanceof AssessedIdentityListState) {
			AssessedIdentityListState listState = (AssessedIdentityListState)state;
			if(StringHelper.containsNonWhitespace(listState.getFilter())) {
				filter = listState.getFilter();
			}
			extendedFilters = listState.getExtendedFilters();
		}

		tableEl.setSelectedFilterKey(filter);
		if(extendedFilters != null) {
			tableEl.setSelectedExtendedFilters(extendedFilters);
		}
		loadModel(ureq);
		
		if(entries != null && entries.size() > 0) {
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
				loadModel(ureq);
				if(aee.isClose()) {
					stackPanel.popController(currentIdentityCtrl);
				}
			} else if(event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				loadModel(ureq);
				stackPanel.popController(currentIdentityCtrl);
			}
		} else if(bulkToolsList != null && bulkToolsList.contains(source)) {
			if(event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			}
		} else if(changeUserVisibilityCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSetVisibility(ureq, changeUserVisibilityCtrl.getVisibility(), changeUserVisibilityCtrl.getRows());
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event instanceof ShowDetailsEvent) {
				doSelect(ureq, ((ShowDetailsEvent)event).getAssessedIdentity());
				toolsCalloutCtrl.deactivate();
				cleanUp();
			} else if(event == Event.CHANGED_EVENT) {
				loadModel(ureq);
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
		removeAsListenerAndDispose(changeUserVisibilityCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		changeUserVisibilityCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
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
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel(ureq);
			}
		} else if(bulkDoneButton == source) {
			doSetDone(ureq);
		} else if(bulkVisibleButton == source) {
			doConfirmVisible(ureq);
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
		return new IdentityListCourseNodeToolsController(ureq, getWindowControl(),
				(AssessableCourseNode)courseNode, assessedIdentity, coachCourseEnv);
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
					courseEntry, courseNode, coachCourseEnv, assessedIdentity, true);
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
	
	private void doConfirmVisible(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessedIdentityElementRow> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if(row != null) {
				rows.add(row);
			}
		}
		
		if(rows.isEmpty()) {
			showWarning("warning.bulk.done");
		} else {
			changeUserVisibilityCtrl = new ConfirmUserVisibilityController(ureq, getWindowControl(), rows);
			listenTo(changeUserVisibilityCtrl);
			
			String title = translate("change.visibility.title");
			cmc = new CloseableModalController(getWindowControl(), "close", changeUserVisibilityCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doSetVisibility(UserRequest ureq, Boolean visibility, List<AssessedIdentityElementRow> rows) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		AssessableCourseNode assessableCourseNode = (AssessableCourseNode)courseNode;
		
		for(AssessedIdentityElementRow row:rows) {
			Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
			
			Roles roles = securityManager.getRoles(assessedIdentity);
			
			IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
			UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(), coachCourseEnv.isCourseReadOnly());
			assessedUserCourseEnv.getScoreAccounting().evaluateAll();

			ScoreEvaluation scoreEval = assessableCourseNode.getUserScoreEvaluation(assessedUserCourseEnv);
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
					scoreEval.getAssessmentStatus(), visibility, scoreEval.getFullyAssessed(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			assessableCourseNode.updateUserScoreEvaluation(doneEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
			dbInstance.commitAndCloseSession();
		}
		loadModel(ureq);
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
		} else if(courseNode instanceof AssessableCourseNode) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			AssessableCourseNode assessableCourseNode = (AssessableCourseNode)courseNode;
			for(AssessedIdentityElementRow row:rows) {
				Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				doSetDone(assessedIdentity, assessableCourseNode, course);
				dbInstance.commitAndCloseSession();
			}
			loadModel(ureq);
		}
	}
	
	protected void doSetDone(Identity assessedIdentity, AssessableCourseNode assessableCourseNode, ICourse course) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(), coachCourseEnv.isCourseReadOnly());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = assessableCourseNode.getUserScoreEvaluation(assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
				AssessmentEntryStatus.done, null, scoreEval.getFullyAssessed(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		assessableCourseNode.updateUserScoreEvaluation(doneEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		
	}
	
	private void doUpdateCompletion(Double completion, AssessmentRunStatus status, Long assessedIdentityKey) {
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			if(assessedIdentityKey.equals(row.getIdentityKey())) {
				doUpdateCompletion(completion, status, row);
				break;
			}
		}
	}
	
	private void doUpdateCompletion(Double completion, AssessmentRunStatus status, AssessedIdentityElementRow row) {
		row.getCurrentCompletion().setCompletion(completion);
		boolean endedRow = row.getCurrentCompletion().isEnded();
		boolean endedEvent = status != null && AssessmentRunStatus.done.equals(status);
		row.getCurrentCompletion().setEnded(endedEvent);
		if(endedEvent && !endedRow) {
			IdentityRef assessedIdentity = new IdentityRefImpl(row.getIdentityKey());
			AssessmentEntry assessmentEntry = assessmentToolManager.getAssessmentEntries(assessedIdentity, courseEntry, courseNode.getIdent());
			row.setAssessmentEntry(assessmentEntry);
			tableEl.getComponent().setDirty(true);
		}
	}
}
