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
package org.olat.modules.assessment.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.model.SearchAssessedIdentityParams.Passed;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListTableModel.IdentityCourseElementCols;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityListController extends FormBasicController implements Activateable2 {

	
	private final RepositoryEntry testEntry;
	private final AssessableResource element;
	private SearchAssessedIdentityParams searchParams;
	private final List<UserPropertyHandler> userPropertyHandlers;
	protected final AssessmentToolSecurityCallback assessmentCallback;
	
	private Link nextLink;
	private Link previousLink;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private AssessedIdentityListTableModel usersTableModel;

	private List<Controller> toolsCtrl;
	private AssessedIdentityController currentIdentityCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	protected AssessmentToolManager assessmentToolManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public AssessedIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry testEntry, AssessableResource element, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identity_element");
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.element = element;
		this.testEntry = testEntry;
		this.stackPanel = stackPanel;
		this.assessmentCallback = assessmentCallback;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return testEntry;
	}
	
	public SearchAssessedIdentityParams getSearchParameters() {
		return searchParams;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", testEntry.getDisplayname());
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(testEntry);
			layoutCont.contextPut("cssClass", RepositoyUIFactory.getIconCssClass(handler.getSupportedType()));
		}

		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select", true, "userProp-" + colIndex));
			colIndex++;
		}
			
		if(element.hasAttemptsConfigured()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.attempts, "select"));
		}
		if(element.hasScoreConfigured()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.min, "select", new ScoreCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.max, "select", new ScoreCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.score, "select", new ScoreCellRenderer()));
		}
		if(element.hasPassedConfigured()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.passed, new PassedCellRenderer(getLocale())));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.initialLaunchDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.lastModified, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.lastUserModified, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCourseElementCols.lastCoachModified, "select"));
		
		usersTableModel = new AssessedIdentityListTableModel(columnsModel, element);
		usersTableModel.setCertificateMap(new ConcurrentHashMap<>());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		initFilters();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// life-cycle
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry("notStarted", translate("filter.notStarted")));
		statusValues.add(SelectionValues.entry("inProgress", translate("filter.inProgress")));
		statusValues.add(SelectionValues.entry("inReview", translate("filter.inReview")));
		statusValues.add(SelectionValues.entry("done", translate("filter.done")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				AssessedIdentityListState.FILTER_STATUS, statusValues, true));
		
		// passed
		SelectionValues passedValues = new SelectionValues();
		passedValues.add(SelectionValues.entry(SearchAssessedIdentityParams.Passed.passed.name(), translate("filter.passed")));
		passedValues.add(SelectionValues.entry(SearchAssessedIdentityParams.Passed.failed.name(), translate("filter.failed")));
		passedValues.add(SelectionValues.entry(SearchAssessedIdentityParams.Passed.notGraded.name(), translate("filter.nopassed")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.passed.label"),
				AssessedIdentityListState.FILTER_PASSED, passedValues, true));
		
		if(assessmentCallback.canAssessNonMembers() || assessmentCallback.canAssessFakeParticipants()) {
			SelectionValues membersValues = new SelectionValues();
			membersValues.add(SelectionValues.entry(ParticipantType.member.name(), translate("filter.members")));
			if (assessmentCallback.canAssessNonMembers()) {
				membersValues.add(SelectionValues.entry(ParticipantType.nonMember.name(), translate("filter.other.users")));
			}
			if (assessmentCallback.canAssessFakeParticipants()) {
				membersValues.add(SelectionValues.entry(ParticipantType.fakeParticipant.name(), translate("filter.fake.participants")));
			}
			if (membersValues.size() > 1) {
				filters.add(new FlexiTableMultiSelectionFilter(translate("filter.members.label"),
						AssessedIdentityListState.FILTER_MEMBERS, membersValues, true));
				
			}
		}
		
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<BusinessGroup> coachedGroups;
			if(assessmentCallback.isAdmin()) {
				SearchBusinessGroupParams params = new SearchBusinessGroupParams();
				coachedGroups = businessGroupService.findBusinessGroups(params, testEntry, 0, -1);
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups();
			}

			if(coachedGroups != null && !coachedGroups.isEmpty()) {
				SelectionValues groupValues = new SelectionValues();
				for(BusinessGroup coachedGroup:coachedGroups) {
					String groupName = StringHelper.escapeHtml(coachedGroup.getName());
					groupValues.add(new SelectionValue(coachedGroup.getKey().toString(), groupName, null, "o_icon o_icon_group", null, true));
				}
				filters.add(new FlexiTableSingleSelectionFilter(translate("filter.groups"),
						"groups", groupValues, true));
			}
		}

		tableEl.setFilters(true, filters, false, true);
		if(assessmentCallback.canAssessNonMembers()) {
			tableEl.setFiltersValues(null, List.of(FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_MEMBERS, ParticipantType.member.name())));
		}
	}
	
	public class AToolsOptions extends AssessmentToolOptions {

		@Override
		public List<Identity> getIdentities() {
			return assessmentToolManager.getAssessedIdentities(getIdentity(), searchParams);
		}
	}
	
	protected void updateModel(String searchString, List<FlexiTableFilter> filters) {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(testEntry, null, testEntry, assessmentCallback);
		
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_STATUS);
		if (statusFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)statusFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<AssessmentEntryStatus> passed = filterValues.stream()
						.filter(AssessmentEntryStatus::isValueOf)
						.map(AssessmentEntryStatus::valueOf)
						.collect(Collectors.toList());
				params.setAssessmentStatus(passed);
			} else {
				params.setAssessmentStatus(null);
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
			} else {
				params.setPassed(null);
			}
		}
		
		FlexiTableFilter membersFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_MEMBERS);
		if(membersFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)membersFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				Set<ParticipantType> participants = filterValues.stream()
						.map(ParticipantType::valueOf)
						.collect(Collectors.toSet());
				params.setParticipantTypes(participants);
			}
		}
		
		// learn resources cannot have business groups or curriculums
		List<Long> businessGroupKeys = null;
		params.setBusinessGroupKeys(businessGroupKeys);
		params.setSearchString(searchString);
		
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentEntries.stream().filter(entry -> entry.getIdentity() != null)
			.forEach(entry -> entryMap.put(entry.getIdentity().getKey(), entry));
		
		// Apply filters
		assessedIdentities = applyFilters(assessedIdentities, entryMap, params);

		Map<Long,Date> initialLaunchDates = userInfosMgr.getInitialLaunchDates(testEntry.getOlatResource());

		List<AssessedIdentityElementRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			AssessedIdentityElementRow row = new AssessedIdentityElementRow(assessedIdentity, entry,
					null, null, null, null, userPropertyHandlers, getLocale());
			row.setInitialCourseLaunchDate(initialLaunchDates.get(assessedIdentity.getKey()));
			rows.add(row);
		}

		usersTableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		searchParams = params;
		updateTools(assessedIdentities);
	}
	
	private List<Identity> applyFilters(List<Identity> identities, Map<Long, AssessmentEntry> identityToEntry,
			SearchAssessedIdentityParams params) {
		if(hasFilter(params)) {
			List<Identity> filteredIdentities = new ArrayList<>();
			for(Identity assessedIdentity:identities) {
				AssessmentEntry entry = identityToEntry.get(assessedIdentity.getKey());
				if(matchesStatusFilter(params, entry) && matchesPassedFilter(params, entry)) {
					filteredIdentities.add(assessedIdentity);
				}
			}
			return filteredIdentities;
		}
		return identities;
	}
	
	private boolean hasFilter(SearchAssessedIdentityParams params) {
		return hasStatusFilter(params) 
				|| hasPassedFilter(params);
	}

	private boolean hasStatusFilter(SearchAssessedIdentityParams params) {
		return params.getAssessmentStatus() != null && !params.getAssessmentStatus().isEmpty();
	}
	
	private boolean matchesStatusFilter(SearchAssessedIdentityParams params, AssessmentEntry entry) {
		if (hasStatusFilter(params)) {
			return (entry == null && params.getAssessmentStatus().contains(AssessmentEntryStatus.notStarted)) 
					|| (entry != null && params.getAssessmentStatus().contains(entry.getAssessmentStatus()));
		}
		return true;
	}

	private boolean hasPassedFilter(SearchAssessedIdentityParams params) {
		return params.getPassed() != null && !params.getPassed().isEmpty();
	}
	
	private boolean matchesPassedFilter(SearchAssessedIdentityParams params, AssessmentEntry entry) {
		if (hasPassedFilter(params)) {
			return (params.getPassed().contains(Passed.passed) && entry != null && entry.getPassed() != null && entry.getPassed().booleanValue())
					|| (params.getPassed().contains(Passed.failed) && entry != null && entry.getPassed() != null && !entry.getPassed().booleanValue())
					|| (params.getPassed().contains(Passed.notGraded) && (entry == null || entry.getPassed() == null));
		}
		return true;
	}
	
	protected void updateTools(@SuppressWarnings("unused") List<Identity> assessedIdentities) {
		//to override
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof AssessedIdentityListState) {
			AssessedIdentityListState listState = (AssessedIdentityListState)state;
			listState.setValuesToFilter(tableEl.getExtendedFilters());
		}

		updateModel(null, tableEl.getFilters());
		
		if(entries != null && !entries.isEmpty()) {
			String resourceType = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Identity".equals(resourceType)) {
				Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
				for(int i=usersTableModel.getRowCount(); i--> 0; ) {
					AssessedIdentityElementRow row = usersTableModel.getObject(i);
					if(row.getIdentityKey().equals(identityKey)) {
						doSelect(ureq, row);
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
			if(event == Event.CHANGED_EVENT) {
				updateModel(null, null);
			} else if(event == Event.DONE_EVENT) {
				updateModel(null, null);
				stackPanel.popController(currentIdentityCtrl);
			} else if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(currentIdentityCtrl);
			}
		} else if(toolsCtrl != null && toolsCtrl.contains(source)) {
			if(event == Event.CHANGED_EVENT) {
				updateModel(null, null);
			}
		}
		super.event(ureq, source, event);
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
				updateModel(ftse.getSearch(), ftse.getFilters());
			}
		}
		
		super.formInnerEvent(ureq, source, event);
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
	
	private void doSelect(UserRequest ureq, AssessedIdentityElementRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		currentIdentityCtrl = new AssessedIdentityRepositoryEntryController(ureq, getWindowControl(), stackPanel,
				testEntry, assessedIdentity, element);
		listenTo(currentIdentityCtrl);
		stackPanel.pushController(fullName, currentIdentityCtrl);
		
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		stackPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));
		stackPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
	}
}
