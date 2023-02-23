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
package org.olat.course.nodes.gta.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.ui.CoachAssignmentListTableModel.CACols;
import org.olat.course.nodes.gta.ui.component.CoachSingleSelectionCellRenderer;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.curriculum.CurriculumElementShort;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachAssignmentListController extends FormBasicController {
	
	protected static final int COACH_OFFSET = 50000;
	
	protected static final String NOT_ASSIGNED = "-1";
	
	private static final String FILTER_OWNER = "owner";
	private static final String FILTER_COURSE_COACH = "coursecoach";
	private static final String FILTER_GROUP_COACH = "groupcoach";
	private static final String FILTER_CURRICULUM_COACH = "curriculumcoach";
	
	private static final String FILTER_PARTICIPANT = "participant";
	
	private FormLink backLink;
	private FlexiTableElement tableEl;
	private FormLink randomAssignmentButton;
	private FormSubmit applyAssignmentButton;
	private MultipleSelectionElement coachFilterEl;
	private CoachAssignmentListTableModel tableModel;
	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

	private SelectionValues coachKeyValues;
	private final List<MemberView> participantsViews;
	
	private int counter = 0;
	private final GTACourseNode gtaNode;
	private List<CoachColumn> coachesColumns;
	private final RepositoryEntry repoEntry;
	private final List<Identity> assessedIdentities;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<UserPropertyHandler> coachPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MemberViewQueries memberQueries;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public CoachAssignmentListController(UserRequest ureq, WindowControl wControl, List<Identity> assessedIdentities,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "coach_assignment");
		
		this.gtaNode = gtaNode;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedIdentities = new ArrayList<>(assessedIdentities);

		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		repoEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		SearchMembersParams params = new SearchMembersParams();
		params.setPending(false);
		params.setRoles(new GroupRoles[] { GroupRoles.participant });
		coachPropertyHandlers = List.of(userManager.getUserPropertiesConfig().getPropertyHandler(UserConstants.FIRSTNAME),
				userManager.getUserPropertiesConfig().getPropertyHandler(UserConstants.LASTNAME));
		participantsViews = memberQueries.getRepositoryEntryMembers(repoEntry, params, coachPropertyHandlers, getLocale());
		
		initForm(ureq);
		loadModel(this.assessedIdentities);
		loadNumberedCoachColumnHeaders();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer filterCont = uifactory.addDefaultFormLayout("filters", null, formLayout);
		initFilterForm(filterCont);
		String page = velocity_root + "/coach_assignment_table.html";
		FormLayoutContainer tableCont = uifactory.addCustomFormLayout("table", null, page, formLayout);
		initTableForm(tableCont);
		
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		randomAssignmentButton = uifactory.addFormLink("assign.random", formLayout, Link.BUTTON);
		randomAssignmentButton.setIconLeftCSS("o_icon o_icon_shuffle");
		
		applyAssignmentButton = uifactory.addFormSubmitButton("apply.assignement", formLayout);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void initFilterForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("bulk.coach.assignment"));
		
		SelectionValues filterValues = new SelectionValues();
		filterValues.add(SelectionValues.entry(FILTER_OWNER, translate("filter.coaches.by.role.course.owners")));
		filterValues.add(SelectionValues.entry(FILTER_COURSE_COACH, translate("filter.coaches.by.role.course.coaches")));
		filterValues.add(SelectionValues.entry(FILTER_GROUP_COACH, translate("filter.coaches.by.role.group.coaches")));
		filterValues.add(SelectionValues.entry(FILTER_CURRICULUM_COACH, translate("filter.coaches.by.role.curriculum.coaches")));
		coachFilterEl = uifactory.addCheckboxesVertical("filter.coaches.by.role", formLayout, filterValues.keys(), filterValues.values(), 1);
		coachFilterEl.addActionListener(FormEvent.ONCHANGE);
		
		// Default
		coachFilterEl.select("coursecoach", true);
		coachFilterEl.select("groupcoach", true);
		coachFilterEl.select("curriculumcoach", true);
	}
	
	private void initTableForm(FormItemContainer formLayout) {
		loadColumnsModel();

		tableModel = new CoachAssignmentListTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "coachTable", tableModel, 50, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		
		initFilters();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues assignedCoachValues = new SelectionValues();
		for(Identity assessedIdentity:assessedIdentities) {
			assignedCoachValues.add(SelectionValues.entry(assessedIdentity.getKey().toString(), userManager.getUserDisplayName(assessedIdentity)));
		}
		FlexiTableMultiSelectionFilter participantFilter = new FlexiTableMultiSelectionFilter(translate("filter.participants"),
				FILTER_PARTICIPANT, assignedCoachValues, true);
		filters.add(participantFilter);

		tableEl.setFilters(true, filters, false, true);
	}
	
	private void loadCoaches() {
		coachKeyValues = new SelectionValues();
		coachKeyValues.add(SelectionValues.entry(NOT_ASSIGNED, translate(CACols.notAssigned.i18nHeaderKey())));
		
		SearchMembersParams params = new SearchMembersParams();
		params.setPending(false);
		params.setRoles(new GroupRoles[] { GroupRoles.owner, GroupRoles.coach });
		List<MemberView> coachesViews = memberQueries.getRepositoryEntryMembers(repoEntry, params, coachPropertyHandlers, getLocale());
		
		coachesColumns = new ArrayList<>();
		
		Collection<String> selectedFilters = coachFilterEl.getSelectedKeys();
		boolean owners = selectedFilters.contains(FILTER_OWNER);
		boolean courseCoaches = selectedFilters.contains(FILTER_COURSE_COACH);
		boolean groupCoaches = selectedFilters.contains(FILTER_GROUP_COACH);
		boolean curriculumCoaches = selectedFilters.contains(FILTER_CURRICULUM_COACH);
		
		for(MemberView member:coachesViews) {
			if(acceptCoach(member, owners, courseCoaches, groupCoaches, curriculumCoaches)) {
				StringBuilder sb = new StringBuilder();
				for(String prop:member.getIdentityProps()) {
					if(sb.length() > 0) {
						sb.append(" ");
					}
					sb.append(prop);
				}
				
				String coachKey = member.getIdentityKey().toString();
				String fullName = sb.toString();
				coachesColumns.add(new CoachColumn(coachKey, fullName, member));
				coachKeyValues.add(SelectionValues.entry(coachKey, fullName));
			}
		}
	}
	
	private boolean acceptCoach(MemberView member, boolean owners, boolean courseCoaches, boolean groupCoaches, boolean curriculumCoaches) {
		CourseMembership membership = member.getMemberShip();
		return (membership.isRepositoryEntryOwner() && owners)
				|| (membership.isRepositoryEntryCoach() && courseCoaches)
				|| (membership.isBusinessGroupCoach() && groupCoaches)
				|| (membership.isCurriculumElementCoach() && curriculumCoaches);
	}
	
	private void loadColumnsModel() {
		columnsModel.clear();
		
		loadCoaches();
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(GTACoachedGroupGradingController.USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, userPropertyHandler.getName(), true, propName,
						new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CACols.taskTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, CACols.notAssigned, new CoachSingleSelectionCellRenderer(0)));

		int numOfCoaches = coachesColumns.size();
		for(int j=0; j<numOfCoaches; j++) {
			CoachColumn coachColumn = coachesColumns.get(j);
			
			int whichCoach = j + 1; // first is not assigned
			int colIndex = COACH_OFFSET + whichCoach;
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(true, null, colIndex, false, coachColumn.getCoachKey());
			col.setHeaderLabel(coachColumn.getFullName());
			col.setCellRenderer(new CoachSingleSelectionCellRenderer(whichCoach));
			coachColumn.setColumnModel(col);
			columnsModel.addFlexiColumnModel(col);
		}
	}
	
	private void loadModel(List<Identity> participants) {
		Map<Long,MemberView> participantsMap = participantsViews.stream()
				.collect(Collectors.toMap(MemberView::getIdentityKey, view -> view, (u, v) -> u));
		Map<String,MemberView> coachesStringMap = coachesColumns.stream().map(CoachColumn::getMemberView)
				.collect(Collectors.toMap(view -> view.getIdentityKey().toString(), view -> view, (u, v) -> u));

		RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<TaskLight> tasks = gtaManager.getTasksLight(entry, gtaNode);
		Map<Long,TaskLight> identityToTasks = new HashMap<>(tasks.size());
		for(TaskLight task:tasks) {
			if(task.getIdentityKey() != null) {
				identityToTasks.put(task.getIdentityKey(), task);
			}
		}
		
		List<AssessmentEntry> assessments = assessmentService.loadAssessmentEntriesBySubIdent(entry, gtaNode.getIdent());
		Map<Long, AssessmentEntry> identityToAssessments = assessments.stream()
				.filter(assessment -> assessment.getIdentity() != null)
				.collect(Collectors.toMap(assessment -> assessment.getIdentity().getKey(), assessment -> assessment, (u, v) -> u));
		
		String[] coachKeys = coachKeyValues.keys();
		String[] coachValues = coachKeyValues.values();
		
		List<IdentityAssignmentRow> rows = new ArrayList<>(participants.size());
		for(Identity assessedIdentity: participants) {
			TaskLight task = identityToTasks.get(assessedIdentity.getKey());
			String taskName = task == null ? null : task.getTaskName();
			AssessmentEntry assessmentEntry = identityToAssessments.get(assessedIdentity.getKey());
			
			SingleSelection coachChoices = uifactory
					.addRadiosHorizontal("coach_" + (++counter), null, flc, coachKeys, coachValues);
			IdentityAssignmentRow row = new IdentityAssignmentRow(taskName, coachChoices,
					assessmentEntry, assessedIdentity, userPropertyHandlers, getLocale());
			MemberView assessedMember = participantsMap.get(assessedIdentity.getKey());
			if(assessedMember != null) {
				enableDisable(assessedMember, coachChoices, assessmentEntry, coachesStringMap);
			}
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void loadNumberedCoachColumnHeaders() {
		List<IdentityAssignmentRow> rows = tableModel.getObjects();
		List<CoachCounter> counters = getCoachesStatistics(rows);
		for(int i=0; i<coachesColumns.size(); i++) {
			CoachCounter coachCounter = counters.get(i);
			CoachColumn column = coachesColumns.get(i);
			String header = translate("table.header.numbered.coach", column.getFullName(), Integer.toString(coachCounter.getAssignees()));
			column.getColumnModel().setHeaderLabel(header);
		}
	}
	
	private void enableDisable(MemberView assessedMember, SingleSelection coachChoices, AssessmentEntry assessmentEntry, Map<String,MemberView> membersStringMap) {
		String selectedCoach = null;
		if(assessmentEntry != null && assessmentEntry.getCoach() != null) {
			selectedCoach = assessmentEntry.getCoach().getKey().toString();
		}
		
		if(selectedCoach != null && coachKeyValues.containsKey(selectedCoach)) {
			coachChoices.select(selectedCoach, true);
		} else {
			coachChoices.select(NOT_ASSIGNED, true);
		}
	
		String[] coachKeys = coachChoices.getKeys();
		for(int i=coachKeys.length; i-->1; ) {
			String coachKey = coachKeys[i];
			MemberView coach = membersStringMap.get(coachKey);
			boolean enabled = accept(assessedMember, coach);
			coachChoices.setEnabled(i, enabled);
		}
	}
	
	private boolean accept(MemberView assessedMember, MemberView coach) {
		if(assessedMember == null || coach == null) return false;
		
		CourseMembership assessedMembership = assessedMember.getMemberShip();
		CourseMembership coachMembership = coach.getMemberShip();
		if(assessedMembership.isRepositoryEntryParticipant()
				&& (coachMembership.isRepositoryEntryCoach() || coachMembership.isRepositoryEntryOwner())) {
			return true;
		}
		
		if(assessedMembership.isBusinessGroupParticipant() && coachMembership.isBusinessGroupCoach()) {
			List<BusinessGroupShort> assessedGroups = assessedMember.getGroups();
			List<BusinessGroupShort> coachGroups = coach.getGroups();
			for(BusinessGroupShort assessedGroup:assessedGroups) {
				for(BusinessGroupShort coachGroup:coachGroups) {
					if(assessedGroup.getKey().equals(coachGroup.getKey())) {
						return true;
					}
				}
			}
		}
		
		if(assessedMembership.isCurriculumElementParticipant() && (coachMembership.isCurriculumElementCoach())) {
			List<CurriculumElementShort> assessedCurriculums = assessedMember.getCurriculumElements();
			List<CurriculumElementShort> coachCurriculums = coach.getCurriculumElements();
			for(CurriculumElementShort assessedCurriculum:assessedCurriculums) {
				for(CurriculumElementShort coachCurriculum:coachCurriculums) {
					if(assessedCurriculum.getKey().equals(coachCurriculum.getKey())) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(coachFilterEl == source) {
			doFilterCoaches();
		} else if(this.applyAssignmentButton == source) {
			doAssignCoaches();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(randomAssignmentButton == source) {
			doRandomAssignCoaches();
		} else if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent ftse) {
				doFilterParticipants(ftse.getFilters(), ftse.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doAssignCoaches();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doFilterParticipants(List<FlexiTableFilter> filters, String search) {
		Set<Long> identityKeys = null;
		FlexiTableFilter participantFilter = FlexiTableFilter.getFilter(filters, FILTER_PARTICIPANT);
		if(participantFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)participantFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				identityKeys = filterValues.stream()
						.map(Long::valueOf)
						.collect(Collectors.toSet());
			}
		}
		
		List<Identity> filteredIdentities;
		if(StringHelper.containsNonWhitespace(search) || (identityKeys != null && !identityKeys.isEmpty())) {
			search = StringHelper.containsNonWhitespace(search) ? search.toLowerCase() : null;
			filteredIdentities = new ArrayList<>(assessedIdentities.size());
			for(Identity assessedIdentity:assessedIdentities) {
				if(accept(assessedIdentity, identityKeys, search)) {
					filteredIdentities.add(assessedIdentity);
				}
			}
		} else {
			filteredIdentities = assessedIdentities;
		}
		loadModel(filteredIdentities);
	}
	
	private boolean accept(Identity identity, Set<Long> identityKeys, String search) {
		if(identityKeys != null && identityKeys.contains(identity.getKey())) {
			return true;
		}
		if(search != null) {
			for(UserPropertyHandler userPropHandler:userPropertyHandlers) {
				if(userPropHandler == null) continue;
				
				String val = identity.getUser().getProperty(userPropHandler.getName(), getLocale());
				if(val != null && val.toLowerCase().contains(search)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void doFilterCoaches() {
		loadColumnsModel();
		tableEl.reset(true, true, true);
	}
	
	private void doAssignCoaches() {
		List<IdentityAssignmentRow> rows = tableModel.getObjects();
		Map<String,Identity> identityKeyToCoach = new HashMap<>();
		
		for(IdentityAssignmentRow row:rows) {
			SingleSelection choice = row.getChoices();
			if(choice.isOneSelected()) {
				String selectedCoachKey = choice.getSelectedKey();
				AssessmentEntry assessmentEntry = row.getAssessmentEntry();
				if(NOT_ASSIGNED.equals(selectedCoachKey)) {
					if(assessmentEntry != null) {
						courseAssessmentService.unassignCoach(assessmentEntry, false, coachCourseEnv.getCourseEnvironment(), gtaNode);
					}
				} else {
					Identity currentCoach = assessmentEntry.getCoach();
					Identity selectedCoach = identityKeyToCoach.computeIfAbsent(selectedCoachKey,
							identityKey -> securityManager.loadIdentityByKey(Long.valueOf(selectedCoachKey)));
					if(!Objects.equals(currentCoach, selectedCoach)) {
						courseAssessmentService.assignCoach(assessmentEntry, selectedCoach, coachCourseEnv.getCourseEnvironment(), gtaNode);
					}	
				}	
			}
		}
		
		dbInstance.commitAndCloseSession();
	}
	
	private void doRandomAssignCoaches() {
		List<IdentityAssignmentRow> rows = tableModel.getObjects();
		List<CoachCounter> coaches = getCoachesStatistics(rows);
		
		for(IdentityAssignmentRow row:rows) {
			SingleSelection selection = row.getChoices();
			boolean notAssigned = selection.isSelected(0);
			if(notAssigned) {
				doRandomAssignCoach(selection, coaches);
			}
		}
		
		loadNumberedCoachColumnHeaders();
	}
	
	private void doRandomAssignCoach(SingleSelection selection, List<CoachCounter> coaches) {
		int numOfCoaches = coachesColumns.size();
		
		int whichCoach = -1;
		int numOfAssignees = Integer.MAX_VALUE;
		for(int i=0; i<numOfCoaches; i++) {
			int which = i + 1;
			boolean enabled = selection.isEnabled(which);
			if(enabled) {
				CoachCounter coachCounter = coaches.get(i);
				int numOfCoachAssignee = coachCounter.getAssignees();
				if(numOfCoachAssignee < numOfAssignees) {
					whichCoach = which;
					numOfAssignees = numOfCoachAssignee;
				}
			}
		}
		
		if(whichCoach > 0) {
			CoachCounter coachCounter = coaches.get(whichCoach - 1);
			selection.select(coachCounter.getCoachKey(), true);
			coachCounter.increment();
		}
	}
	
	private List<CoachCounter> getCoachesStatistics(List<IdentityAssignmentRow> rows) {
		int numOfCoaches = coachesColumns.size();
		List<CoachCounter> coaches = new ArrayList<>(numOfCoaches);
		for(int i=0; i<numOfCoaches;i++) {
			coaches.add(new CoachCounter(coachesColumns.get(i).getCoachKey()));
		}
		
		for(IdentityAssignmentRow row:rows) {
			SingleSelection selection = row.getChoices();
			int selected = selection.getSelected();
			if(selected > 0) {
				coaches.get(selected - 1).increment();
			}
		}
		
		return coaches;
	}
	
	private static class CoachColumn {

		private final String coachKey;
		private final String fullName;
		private final MemberView member;
		private DefaultFlexiColumnModel columnModel;
		
		public CoachColumn(String coachKey, String fullName, MemberView member) {
			this.coachKey = coachKey;
			this.fullName = fullName;
			this.member = member;
		}
		
		public String getCoachKey() {
			return coachKey;
		}
		
		public String getFullName() {
			return fullName;
		}
		
		public MemberView getMemberView() {
			return member;
		}
		
		public DefaultFlexiColumnModel getColumnModel() {
			return columnModel;
		}

		public void setColumnModel(DefaultFlexiColumnModel columnModel) {
			this.columnModel = columnModel;
		}
	}
	
	private static class CoachCounter {
		
		private int assignee = 0;
		private final String coachKey;
		
		public CoachCounter(String coachKey) {
			this.coachKey = coachKey;
		}
		
		public String getCoachKey() {
			return coachKey;
		}
		
		public int getAssignees() {
			return assignee;
		}
		
		public void increment() {
			assignee++;
		}
	}

}
