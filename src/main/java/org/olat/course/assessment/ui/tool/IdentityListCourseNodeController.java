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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.assessment.ui.tool.event.ShowDetailsEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CalculatedAssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListCourseNodeController extends FormBasicController implements Activateable2, GenericEventListener, IdentityListCourseNodeProvider {

	private int counter = 0;
	private final BusinessGroup group;
	private final CourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final RepositoryEntry referenceEntry;
	private final boolean isAdministrativeUser;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private Link nextLink, previousLink;
	private FlexiTableElement tableEl;
	private FormLink bulkDoneButton, bulkVisibleButton;
	private final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;
	private IdentityListCourseNodeTableModel usersTableModel;
	
	private Controller toolsCtrl;
	private CloseableModalController cmc;
	private List<Controller> bulkToolsList;
	private ToolsControllerCreator toolsCtrlCreator;
	private AssessedIdentityController currentIdentityCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmUserVisibilityController changeUserVisibilityCtrl;
	
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
	private CertificatesManager certificatesManager;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public IdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identity_courseelement");
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.group = group;
		this.courseNode = courseNode;
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.toolContainer = toolContainer;
		this.coachCourseEnv = coachCourseEnv;
		this.assessmentCallback = assessmentCallback;
		
		if(courseNode.needsReferenceToARepositoryEntry()) {
			referenceEntry = courseNode.getReferencedRepositoryEntry();
		} else {
			referenceEntry = null;
		}
		if(courseNode instanceof AssessableCourseNode) {
			toolsCtrlCreator = ((AssessableCourseNode)courseNode).getAssessmentToolsCreator();
		}
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		initMultiSelectionTools(ureq, flc);
		
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), courseEntry.getOlatResource());
	}
	
	public AssessedIdentityListState getListState() {
		List<FlexiTableFilter> filters = tableEl.getSelectedFilters();
		String filter = null;
		if(filters != null && filters.size() > 0) {
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

		if(rows == null || rows.isEmpty()) {
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
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		String select = (courseNode instanceof AssessableCourseNode
				&& (courseNode.getParent() == null || !(courseNode instanceof STCourseNode)))
				? "select" : null;

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
		AssessableCourseNode assessableNode = null;
		if(courseNode instanceof AssessableCourseNode) {
			assessableNode = (AssessableCourseNode)courseNode;
			
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
				}
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.score, new ScoreCellRenderer()));
			}
			if(assessableNode.hasPassedConfigured()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.passed, new PassedCellRenderer()));
			}
			if(assessableNode.hasIndividualAsssessmentDocuments()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.numOfAssessmentDocs));
			}
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));
		if(assessableNode != null && assessableNode.hasCompletion()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.currentCompletion));
		}
		
		if(courseNode.getParent() == null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.initialLaunchDate, select));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.lastModified, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.lastUserModified, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.lastCoachModified, select));
		
		if(course.getCourseConfig().isCertificateEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.certificate, new DownloadCertificateCellRenderer()));
			if(course.getCourseConfig().isRecertificationEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.recertification, new DateFlexiCellRenderer(getLocale())));
			}
		}
		if(toolsCtrlCreator != null && toolsCtrlCreator.hasCalloutTools()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.tools));
		}

		usersTableModel = new IdentityListCourseNodeTableModel(columnsModel, assessableNode, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(new AssessedIdentityListProvider(getIdentity(), courseEntry, referenceEntry, courseNode.getIdent(), assessmentCallback), ureq.getUserSession());
		tableEl.setMultiSelect(!coachCourseEnv.isCourseReadOnly());
		tableEl.setSortSettings(options);
		tableEl.setSelectAllEnable(true);

		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("filter.showAll"), "showAll", true));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.passed"), "passed"));
		filters.add(new FlexiTableFilter(translate("filter.failed"), "failed"));
		filters.add(new FlexiTableFilter(translate("filter.inProgress"), "inProgress"));
		filters.add(new FlexiTableFilter(translate("filter.inReview"), "inReview"));
		filters.add(new FlexiTableFilter(translate("filter.done"), "done"));
		tableEl.setFilters("", filters, false);
		
		if(assessmentCallback.canAssessBusinessGoupMembers() && group == null) {
			List<BusinessGroup> coachedGroups = null;
			if(assessmentCallback.isAdmin()) {
				coachedGroups = course.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups(); 
			}

			if(coachedGroups.size() > 0) {
				List<FlexiTableFilter> groupFilters = new ArrayList<>();
				for(BusinessGroup coachedGroup:coachedGroups) {
					String groupName = StringHelper.escapeHtml(coachedGroup.getName());
					groupFilters.add(new FlexiTableFilter(groupName, coachedGroup.getKey().toString(), "o_icon o_icon_group"));
				}
				
				tableEl.setExtendedFilterButton(translate("filter.groups"), groupFilters);
			}
		}

		tableEl.setAndLoadPersistedPreferences(ureq, "assessment-tool-identity-list");
		
		
	}
	
	private void initMultiSelectionTools(UserRequest ureq, FormItemContainer formLayout) {
		if(courseNode instanceof AssessableCourseNode && !(courseNode instanceof CalculatedAssessableCourseNode)) {
			bulkDoneButton = uifactory.addFormLink("bulk.done", formLayout, Link.BUTTON);
			bulkDoneButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			
			bulkVisibleButton = uifactory.addFormLink("bulk.visible", formLayout, Link.BUTTON);
			bulkVisibleButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		}
		
		List<String> cmpNames = new ArrayList<>();
		if(toolsCtrlCreator != null) {
			List<Controller> tools = toolsCtrlCreator.createMultiSelectionTools(ureq, getWindowControl(), coachCourseEnv, this);
			cmpNames = putToolsInContainer(tools);
		}
		flc.contextPut("multiSelectionCmpNames", cmpNames);
	}
	
	private void updateModel(UserRequest ureq, String searchString, List<FlexiTableFilter> filters, List<FlexiTableFilter> extendedFilters) {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), referenceEntry, assessmentCallback);
		
		List<AssessmentEntryStatus> assessmentStatus = null;
		if(filters != null && filters.size() > 0) {
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
		if(group != null) {
			businessGroupKeys = Collections.singletonList(group.getKey());
		} else if(extendedFilters != null && extendedFilters.size() > 0) {
			businessGroupKeys = new ArrayList<>(extendedFilters.size());
			for(FlexiTableFilter extendedFilter:extendedFilters) {
				if(StringHelper.isLong(extendedFilter.getFilter())) {
					businessGroupKeys.add(Long.parseLong(extendedFilter.getFilter()));
				}
			}
		}
		params.setBusinessGroupKeys(businessGroupKeys);
		params.setSearchString(searchString);
		
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentEntries.stream()
			.filter(entry -> entry.getIdentity() != null)
			.forEach((entry) -> entryMap.put(entry.getIdentity().getKey(), entry));
		Map<Long,Date> initialLaunchDates = userInfosMgr.getInitialLaunchDates(courseEntry.getOlatResource());

		List<AssessedIdentityElementRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			Date initialLaunchDate = initialLaunchDates.get(assessedIdentity.getKey());

			CompletionItem currentCompletion = new CompletionItem("current-completion-" + (++counter), getLocale());
			if(entry != null) {
				currentCompletion.setCompletion(entry.getCurrentRunCompletion());
				AssessmentRunStatus status = entry.getCurrentRunStatus();
				currentCompletion.setEnded(status != null && AssessmentRunStatus.done.equals(status));
			}
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		
			AssessedIdentityElementRow row = new AssessedIdentityElementRow(assessedIdentity, entry, initialLaunchDate,
					currentCompletion, toolsLink, userPropertyHandlers, getLocale());
			toolsLink.setUserObject(row);
			rows.add(row);
		}
		
		if(toolContainer.getCertificateMap() == null) {
			List<CertificateLight> certificates = certificatesManager.getLastCertificates(courseEntry.getOlatResource());
			ConcurrentMap<Long, CertificateLight> certificateMap = new ConcurrentHashMap<>();
			for(CertificateLight certificate:certificates) {
				certificateMap.put(certificate.getIdentityKey(), certificate);
			}
			toolContainer.setCertificateMap(certificateMap);
		}
		usersTableModel.setCertificateMap(toolContainer.getCertificateMap());
		usersTableModel.setObjects(rows);
		if(filters != null && filters.size() > 0 && filters.get(0) != null) {
			usersTableModel.filter(Collections.singletonList(filters.get(0)));
		}
		tableEl.reset();
		tableEl.reloadData();

		initTopBulkTools(ureq, params, assessedIdentities);
	}
	
	private void initTopBulkTools(UserRequest ureq, SearchAssessedIdentityParams params, List<Identity> assessedIdentities) {
		List<String> toolCmpNames = new ArrayList<>();
		if(toolsCtrlCreator != null) {
			AssessmentToolOptions options = new AssessmentToolOptions();
			options.setAdmin(assessmentCallback.isAdmin());
			if(group == null) {
				if(assessmentCallback.isAdmin()) {
					options.setNonMembers(params.isNonMembers());
				} else {
					options.setIdentities(assessedIdentities);
					fillAlternativeToAssessableIdentityList(options, params);
				}
			} else {
				options.setGroup(group);
			}
			
			//TODO qti filter by group?
			List<Controller> tools = toolsCtrlCreator.createAssessmentTools(ureq, getWindowControl(), stackPanel, coachCourseEnv, options);
			toolCmpNames = putToolsInContainer(tools);
			bulkToolsList = tools;
		}
		flc.contextPut("toolCmpNames", toolCmpNames);
	}
	
	private List<String> putToolsInContainer(List<Controller> tools) {
		List<String> toolCmpNames = new ArrayList<>();
		if(tools != null && !tools.isEmpty()) {
			for(Controller tool:tools) {
				listenTo(tool);
				String toolCmpName = "ctrl_" + (counter++);
				flc.put(toolCmpName, tool.getInitialComponent());
				toolCmpNames.add(toolCmpName);
				if(tool instanceof BreadcrumbPanelAware) {
					((BreadcrumbPanelAware)tool).setBreadcrumbPanel(stackPanel);
				}
			}
		}
		return toolCmpNames;
	}
	
	private void fillAlternativeToAssessableIdentityList(AssessmentToolOptions options, SearchAssessedIdentityParams params) {
		List<Group> baseGroups = new ArrayList<>();
		if((assessmentCallback.canAssessRepositoryEntryMembers()
				&& (assessmentCallback.getCoachedGroups() == null || assessmentCallback.getCoachedGroups().isEmpty()))
				|| assessmentCallback.canAssessNonMembers()) {
			baseGroups.add(repositoryService.getDefaultGroup(courseEntry));
		}
		if(assessmentCallback.getCoachedGroups() != null && assessmentCallback.getCoachedGroups().size() > 0) {
			for(BusinessGroup coachedGroup:assessmentCallback.getCoachedGroups()) {
				baseGroups.add(coachedGroup.getBaseGroup());
			}
		}
		options.setGroups(baseGroups);
		options.setNonMembers(params.isNonMembers());
	}

	@Override
	protected void doDispose() {
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
		updateModel(ureq, null, tableEl.getSelectedFilters(), tableEl.getSelectedExtendedFilters());
		
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
				updateModel(ureq, null, null, null);
				if(aee.isClose()) {
					stackPanel.popController(currentIdentityCtrl);
				}
			} else if(event == Event.CHANGED_EVENT) {
				updateModel(ureq, null, null, null);
			} else if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(currentIdentityCtrl);
			}
		} else if(bulkToolsList != null && bulkToolsList.contains(source)) {
			if(event == Event.CHANGED_EVENT) {
				updateModel(ureq, null, null, null);
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
				updateModel(ureq, null, null, null);
				toolsCalloutCtrl.deactivate();
				cleanUp();
			} else if(event == Event.CLOSE_EVENT) {
				//don't dispose it, there are some popup window at work
				toolsCalloutCtrl.deactivate();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
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
				FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
				updateModel(ureq, ftse.getSearch(), ftse.getFilters(), ftse.getExtendedFilters());
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
		if(toolsCtrlCreator == null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		toolsCtrl = toolsCtrlCreator.createCalloutController(ureq, getWindowControl(), coachCourseEnv, assessedIdentity);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
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
		for(AssessedIdentityElementRow row:rows) {
			if(assessedIdentity.getKey().equals(row.getIdentityKey())) {
				doSelect(ureq, row);
				break;
			}
		}
	}
	
	private Controller doSelect(UserRequest ureq, AssessedIdentityElementRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(assessedIdentity);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Identity", assessedIdentity.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		if(courseNode.getParent() == null) {
			currentIdentityCtrl = new AssessmentIdentityCourseController(ureq, bwControl, stackPanel,
					courseEntry, coachCourseEnv, assessedIdentity);
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
		
		if(rows == null || rows.isEmpty()) {
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
		}
		updateModel(ureq, null, null, null);
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

		if(rows == null || rows.isEmpty()) {
			showWarning("warning.bulk.done");
		} else if(courseNode instanceof AssessableCourseNode) {
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
						AssessmentEntryStatus.done, null, scoreEval.getFullyAssessed(),
						scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
				assessableCourseNode.updateUserScoreEvaluation(doneEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
			}
			
			updateModel(ureq, null, null, null);
		}
	}
	
	private void doUpdateCompletion(Double completion, AssessmentRunStatus status, Long assessedIdentityKey) {
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			if(assessedIdentityKey.equals(row.getIdentityKey())) {
				row.getCurrentCompletion().setCompletion(completion);
				row.getCurrentCompletion().setEnded(status != null && AssessmentRunStatus.done.equals(status));
			}
		}
	}
}
