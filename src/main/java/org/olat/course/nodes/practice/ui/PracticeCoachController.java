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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.gta.ui.GTACoachedGroupGradingController;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.SeriesCount;
import org.olat.course.nodes.practice.ui.PracticeIdentityTableModel.PracticeIdentityCols;
import org.olat.course.nodes.practice.ui.renders.PracticeChallengeCellRenderer;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeCoachController extends FormBasicController implements Activateable2 {

	protected static final String USER_PROPS_ID = GTACoachedGroupGradingController.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private PracticeIdentityTableModel tableModel;
	private TooledStackedPanel stackPanel;

	private RepositoryEntry courseEntry;
	private PracticeCourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public PracticeCoachController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "practice_coach");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		assessmentCallback = getAssessmentToolSecurityCallback(ureq, coachCourseEnv);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	private AssessmentToolSecurityCallback getAssessmentToolSecurityCallback(UserRequest ureq, UserCourseEnvironment userCourseEnv) {
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, courseEntry);
		boolean admin = reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach();
		boolean nonMembers = reSecurity.isEntryAdmin();
		List<BusinessGroup> coachedGroups = null;
		if(reSecurity.isGroupCoach()) {
			coachedGroups = userCourseEnv.getCoachedGroups();
		}
		Set<IdentityRef> fakeParticipants = assessmentToolManager.getFakeParticipants(courseEntry,
				userCourseEnv.getIdentityEnvironment().getIdentity(), nonMembers, !nonMembers);
		return new AssessmentToolSecurityCallback(admin, nonMembers, reSecurity.isCourseCoach(),
				reSecurity.isGroupCoach(), reSecurity.isCurriculumCoach(), coachedGroups, fakeParticipants);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select", true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeIdentityCols.status,
				new AssessmentStatusCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeIdentityCols.challenges,
				new PracticeChallengeCellRenderer()));
		if(ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PracticeIdentityCols.score,
					new ScoreCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("details", translate("details"), "details"));
		
		tableModel = new PracticeIdentityTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_practice_coach_table");
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		tableEl.setSortSettings(options);
		tableEl.setSelectAllEnable(true);
		
		initFilters();
	}
	
	private void initFilters() {
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
		
		// members
		if (assessmentCallback.canAssessNonMembers() || assessmentCallback.canAssessFakeParticipants()) {
			SelectionValues membersValues = new SelectionValues();
			membersValues.add(SelectionValues.entry(ParticipantType.member.name(), translate("filter.members")));
			if (assessmentCallback.canAssessNonMembers()) {
				membersValues.add(SelectionValues.entry(ParticipantType.nonMember.name(), translate("filter.other.users")));
			}
			if (assessmentCallback.canAssessFakeParticipants()) {
				membersValues.add(SelectionValues.entry(ParticipantType.fakeParticipant.name(), translate("filter.fake.participants")));
			}
			if (membersValues.size() > 1) {
				FlexiTableMultiSelectionFilter filter = new FlexiTableMultiSelectionFilter(translate("filter.members.label"),
						AssessedIdentityListState.FILTER_MEMBERS, membersValues, true);
				filter.setValues(List.of(ParticipantType.member.name()));
				filters.add(filter);
			}
		}

		tableEl.setFilters(true, filters, false, false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		// TODO practice
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("details".equals(se.getCommand()) || "select".equals(se.getCommand())) {
					doDetails(ureq, tableModel.getObject(se.getIndex()));
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel() {
		SearchAssessedIdentityParams params = getSearchParameters();

		// Get the identities and remove identity without assessment entry.
		List<Identity> practicingIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);

		// Get the assessment entries and put it in a map.
		// Obligation filter is applied in this query.
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentToolManager.getAssessmentEntries(getIdentity(), params, null).stream()
			.filter(entry -> entry.getIdentity() != null)
			.forEach(entry -> entryMap.put(entry.getIdentity().getKey(), entry));
		
		List<SeriesCount> numOfSeriesList = practiceService.getCountOfCompletedSeries(courseEntry, courseNode.getIdent());
		Map<Long,SeriesCount> numOfSeriesMap = numOfSeriesList.stream()
				.collect(Collectors.toMap(SeriesCount::getIdentityKey, series -> series, (u, v) -> u));
		
		// Apply filters
		final int seriesPerChallenge = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, 2);
		
		List<PracticeIdentityRow> rows = new ArrayList<>(practicingIdentities.size());
		for(Identity practicingIdentity:practicingIdentities) {
			AssessmentEntry entry = entryMap.get(practicingIdentity.getKey());
			if(entry != null) {
				SeriesCount series = numOfSeriesMap.get(practicingIdentity.getKey());
				long numOfSeries = series == null || series.getCount() < 0l ? 0l: series.getCount();
				long challenges = PracticeHelper.completedChalllenges(numOfSeries, seriesPerChallenge);
				rows.add(new PracticeIdentityRow(practicingIdentity, entry.getAssessmentStatus(), entry.getScore(),
						numOfSeries, challenges, userPropertyHandlers, getLocale()));
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private SearchAssessedIdentityParams getSearchParameters() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), null, assessmentCallback);
		params.setUserPropertyHandlers(userPropertyHandlers);

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
		
		return params;
	}
	
	private void doDetails(UserRequest ureq, PracticeIdentityRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		PracticeCoachedIdentityController detailsCtrl = new PracticeCoachedIdentityController(ureq, getWindowControl(),
				coachCourseEnv, courseNode, assessedIdentity);
		listenTo(detailsCtrl);
		
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		stackPanel.pushController(fullName, detailsCtrl);
	}
}
