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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.assessment.ui.tool.AssignCoachController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.UserVisibilityCellRenderer;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.CoachParticipantsTableModel.CGCols;
import org.olat.course.nodes.gta.ui.component.SubmissionDateCellRenderer;
import org.olat.course.nodes.gta.ui.component.TaskStatusCellRenderer;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.IdentityComporatorFactory;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedParticipantListController extends GTACoachedListController {
	
	public static final String MARKED_TAB_ID = "Marked";
	public static final String ALL_TAB_ID = "All";
	public static final String ASSIGNED_TO_ME_TAB_ID = "AssignedToMe";
	
	private FormLink bulkDoneButton;
	private FormLink bulkEmailButton;
	private FormLink bulkExtendButton;
	private FormLink bulkVisibleButton;
	private FormLink bulkHiddenButton;
	private FormLink bulkDownloadButton;
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab markedTab;
	private FlexiFiltersTab assignedToMeTab;
	private FlexiTableElement tableEl;
	private CoachParticipantsTableModel tableModel;

	private final List<UserPropertiesRow> assessableIdentities;
	private final UserCourseEnvironment coachCourseEnv;
	
	private int count;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean markedDefault;
	private final AssessmentConfig assessmentConfig;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private final Set<Long> fakeParticipantKeys;
	private Map<String, List<Long>> groupKeyToIdentityKeys;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private EditDueDatesController editDueDatesCtrl;
	private AssignCoachController assignCoachCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditMultipleDueDatesController editMultipleDueDatesCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GTACoachedParticipantListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode, boolean markedDefault) {
		super(ureq, wControl, coachCourseEnv.getCourseEnvironment(), gtaNode);
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.coachCourseEnv = coachCourseEnv;
		this.markedDefault = markedDefault;
		
		assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(coachCourseEnv), gtaNode);
		assessmentCallback = courseAssessmentService.createCourseNodeRunSecurityCallback(ureq, coachCourseEnv);
		fakeParticipantKeys = assessmentCallback.getFakeParticipants().stream().map(IdentityRef::getKey).collect(Collectors.toSet());
		
		Set<Identity> identities = new HashSet<>(getAssessableIdentities());
		identities.addAll(securityManager.loadIdentityByRefs(assessmentCallback.getFakeParticipants()));
		
		assessableIdentities = identities.stream()
				.map(participant -> new UserPropertiesRow(participant, userPropertyHandlers, getLocale()))
				.collect(Collectors.toList());
		
		initForm(ureq);
		int rows = updateModel(ureq);
		if(rows == 0 && tableEl.getSelectedFilterTab() != allTab) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			updateModel(ureq);
		}
	}
	
	public boolean hasIdentityKey(Long identityKey) {
		if(assessableIdentities != null) {
			for(UserPropertiesRow row:assessableIdentities) {
				if(row.getIdentityKey().equals(identityKey)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Identity> getAssessableIdentities() {
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry re = cgm.getCourseEntry();
		
		return assessmentCallback.isAdmin()
				? repositoryService.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name())
						.stream().distinct().collect(Collectors.toList())
				: repositoryService.getCoachedParticipants(getIdentity(), re);
	}
	
	public boolean isMarkedFilterSelected() {
		return tableEl.getSelectedFilterTab() == markedTab;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel markCol = new DefaultFlexiColumnModel(CGCols.mark);
		markCol.setExportable(false);
		columnsModel.addFlexiColumnModel(markCol);

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
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.coachAssignment));
		}
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskTitle));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CGCols.taskName));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskStatus, new TaskStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.submissionDate, new SubmissionDateCellRenderer(gtaManager, getTranslator())));
		
		DefaultFlexiColumnModel userVisibilityCol = new DefaultFlexiColumnModel(CGCols.userVisibility, new UserVisibilityCellRenderer(false));
		userVisibilityCol.setIconHeader("o_icon o_icon-fw o_icon_results_hidden");

		columnsModel.addFlexiColumnModel(userVisibilityCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.passed, new PassedCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.numOfSubmissionDocs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CGCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new CoachParticipantsTableModel(getLocale(), columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_course_gta_coached_participants");
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "gta-coached-participants-v4-false");
		
		initBulkTools(ureq, formLayout);
		initFiltersPresets(ureq);
		initFilters();
	}
	
	protected void initBulkTools(@SuppressWarnings("unused") UserRequest ureq, FormItemContainer formLayout) {
		if(assessmentConfig.isAssessable()) {
			bulkDoneButton = uifactory.addFormLink("bulk.done", formLayout, Link.BUTTON);
			bulkDoneButton.setElementCssClass("o_sel_assessment_bulk_done");
			bulkDoneButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
			bulkDoneButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			tableEl.addBatchButton(bulkDoneButton);
			
			boolean canChangeUserVisibility = assessmentCallback.isAdmin()
					|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
			
			if (canChangeUserVisibility) {
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
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(gtaManager.isDueDateEnabled(gtaNode) && !config.getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES)) {
			bulkExtendButton = uifactory.addFormLink("extend.list", "duedates", "duedates", formLayout, Link.BUTTON);
			bulkExtendButton.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			tableEl.addBatchButton(bulkExtendButton);
		}
		
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			bulkDownloadButton = uifactory.addFormLink("batch.download", "bulk.download.title", null, formLayout, Link.BUTTON);
			bulkDownloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			tableEl.addBatchButton(bulkDownloadButton);
		}
		
		bulkEmailButton = uifactory.addFormLink("bulk.email", formLayout, Link.BUTTON);
		bulkEmailButton.setElementCssClass("o_sel_assessment_bulk_email");
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		bulkEmailButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkEmailButton);
	}
	
	protected final void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(2);
		
		markedTab = FlexiFiltersTabFactory.tabWithFilters(MARKED_TAB_ID, translate("filter.marked"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(
								AssessedIdentityListState.FILTER_OBLIGATION,
								List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name())),
						FlexiTableFilterValue.valueOf(
								AssessedIdentityListState.FILTER_MEMBERS,
								List.of(ParticipantType.member.name()))));
		markedTab.setFiltersExpanded(true);
		tabs.add(markedTab);
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(
								AssessedIdentityListState.FILTER_OBLIGATION,
								List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name())),
						FlexiTableFilterValue.valueOf(
								AssessedIdentityListState.FILTER_MEMBERS,
								List.of(ParticipantType.member.name()))));
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		if(assessmentConfig.hasCoachAssignment()) {
			assignedToMeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ASSIGNED_TO_ME_TAB_ID, translate("filter.assigned.to.me"),
					TabSelectionBehavior.clear, List.of(
							FlexiTableFilterValue.valueOf(AssessedIdentityListState.FILTER_ASSIGNED_COACH, List.of(getIdentity().getKey().toString()))));
			assignedToMeTab.setFiltersExpanded(true);
			tabs.add(assignedToMeTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, markedDefault? markedTab: allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(coachCourseEnv).getType())) {
			SelectionValues obligationValues = new SelectionValues();
			obligationValues.add(SelectionValues.entry(AssessmentObligation.mandatory.name(), translate("filter.mandatory")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.optional.name(), translate("filter.optional")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.excluded.name(), translate("filter.excluded")));
			FlexiTableMultiSelectionFilter obligationFilter = new FlexiTableMultiSelectionFilter(translate("filter.obligation"),
					AssessedIdentityListState.FILTER_OBLIGATION, obligationValues, true);
			obligationFilter.setValues(List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name()));
			filters.add(obligationFilter);
		}
		
		if (!fakeParticipantKeys.isEmpty()) {
			SelectionValues membersValues = new SelectionValues();
			membersValues.add(SelectionValues.entry(ParticipantType.member.name(), translate("filter.members")));
			membersValues.add(SelectionValues.entry(ParticipantType.fakeParticipant.name(), translate("filter.fake.participants")));
			FlexiTableMultiSelectionFilter membersFilter = new FlexiTableMultiSelectionFilter(translate("filter.members.label"),
					AssessedIdentityListState.FILTER_MEMBERS, membersValues, true);
			membersFilter.setValues(List.of(ParticipantType.member.name()));
			filters.add(membersFilter);
		}
		
		if (assessmentConfig.hasCoachAssignment()) {
			SelectionValues assignedCoachValues = new SelectionValues();
			assignedCoachValues.add(SelectionValues.entry("-1", translate("filter.coach.not.assigned")));
			
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			List<Identity> coaches = repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name(), GroupRoles.coach.name());
			Collections.sort(coaches, IdentityComporatorFactory.createLastnameFirstnameComporator());
			for(Identity coach:coaches) {
				assignedCoachValues.add(SelectionValues.entry(coach.getKey().toString(), userManager.getUserDisplayName(coach)));
			}

			FlexiTableMultiSelectionFilter assignedCoachFilter = new FlexiTableMultiSelectionFilter(translate("filter.coach.assigned"),
					AssessedIdentityListState.FILTER_ASSIGNED_COACH, assignedCoachValues, true);

			filters.add(assignedCoachFilter);
		}
		
		// groups
		SelectionValues groupValues = new SelectionValues();
		groupKeyToIdentityKeys = new HashMap<>();
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<BusinessGroup> coachedGroups;
			if(assessmentCallback.isAdmin()) {
				coachedGroups = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups();
			}
			
			if(coachedGroups != null && !coachedGroups.isEmpty()) {
				List<Group> baseGroups = coachedGroups.stream().map(BusinessGroup::getBaseGroup).toList();
				List<GroupMembership> memberships = groupDao.getMemberships(baseGroups, GroupRoles.participant.name());
				Map<Group, List<GroupMembership>> groupToMembership = memberships.stream().collect(Collectors.groupingBy(GroupMembership::getGroup));
				
				for(BusinessGroup coachedGroup:coachedGroups) {
					String key = "businessgroup-" + coachedGroup.getKey();
					String groupName = StringHelper.escapeHtml(coachedGroup.getName());
					groupValues.add(new SelectionValue(key, groupName, null, "o_icon o_icon_group", null, true));
					
					List<GroupMembership> groupMemberships = groupToMembership.get(coachedGroup.getBaseGroup());
					List<Long> identities = groupMemberships != null && !groupMemberships.isEmpty()
							? groupMemberships.stream().map(GroupMembership::getIdentity).map(Identity::getKey).toList()
							: List.of();
					groupKeyToIdentityKeys.put(key, identities);
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
				List<Group> baseGroups = coachedCurriculumElements.stream().map(CurriculumElement::getGroup).toList();
				List<GroupMembership> memberships = groupDao.getMemberships(baseGroups, GroupRoles.participant.name());
				Map<Group, List<GroupMembership>> groupToMembership = memberships.stream().collect(Collectors.groupingBy(GroupMembership::getGroup));
				
				for(CurriculumElement coachedCurriculumElement:coachedCurriculumElements) {
					String key = "curriculumelement-" + coachedCurriculumElement.getKey();
					String name = StringHelper.escapeHtml(CurriculumHelper.getLabel(coachedCurriculumElement, getTranslator()));
					groupValues.add(new SelectionValue(key, name, null,
							"o_icon o_icon_curriculum_element", null, true));
					
					List<GroupMembership> groupMemberships = groupToMembership.get(coachedCurriculumElement.getGroup());
					List<Long> identities = groupMemberships != null && !groupMemberships.isEmpty()
							? groupMemberships.stream().map(GroupMembership::getIdentity).map(Identity::getKey).toList()
							: List.of();
					groupKeyToIdentityKeys.put(key, identities);
				}
			}
		}
		
		if(!groupValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.groups"),
					AssessedIdentityListState.FILTER_GROUPS, groupValues, true));
		}
		
		if (!filters.isEmpty()) {
			tableEl.setFilters(true, filters, false, false);
		}
	}
	
	protected int updateModel(UserRequest ureq) {
		List<AssessmentObligation> filterObligations = getFilterObligations();
		Set<ParticipantType> filterParticipants = getFilterParticipants();
		List<String> filterGroupKeys = getFilterGroupKeys();
		Set<Long> assignmentKeys = getFilterAssignmentKeys();
		
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		Map<String,TaskDefinition> fileNameToDefinitions = taskDefinitions.stream()
				.filter(def -> Objects.nonNull(def.getFilename()))
				.collect(Collectors.toMap(TaskDefinition::getFilename, Function.identity(), (u, v) -> u));
		File tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		
		
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<TaskLight> tasks = gtaManager.getTasksLight(entry, gtaNode);
		Map<Long,TaskLight> identityToTasks = new HashMap<>(tasks.size());
		for(TaskLight task:tasks) {
			if(task.getIdentityKey() != null) {
				identityToTasks.put(task.getIdentityKey(), task);
			}
		}
		List<IdentityMark> marks = gtaManager.getMarks(entry, gtaNode, ureq.getIdentity());
		Map<Long,IdentityMark> identityToMarks= new HashMap<>(marks.size());
		for(IdentityMark mark:marks) {
			if(mark.getParticipant() != null) {
				identityToMarks.put(mark.getParticipant().getKey(), mark);
			}
		}
		
		List<AssessmentEntry> assessments = assessmentService.loadAssessmentEntriesBySubIdent(entry, gtaNode.getIdent());
		Map<Long, AssessmentEntry> identityToAssessments = new HashMap<>(assessments.size());
		for(AssessmentEntry assessment:assessments) {
			if(assessment.getIdentity() != null) {
				identityToAssessments.put(assessment.getIdentity().getKey(), assessment);
			}
		}
		
		List<CoachedIdentityRow> rows = new ArrayList<>(assessableIdentities.size());
		boolean markedOnly = MARKED_TAB_ID.equals(tableEl.getSelectedFilterTab().getId());
		for(UserPropertiesRow assessableIdentity:assessableIdentities) {
			IdentityMark mark = identityToMarks.get(assessableIdentity.getIdentityKey());
			if (markedOnly && mark == null) {
				continue;
			}
			
			AssessmentEntry assessment = identityToAssessments.get(assessableIdentity.getIdentityKey());
			if (isExcludedByObligation(filterObligations, assessment)
					|| isExcludedByParticipant(filterParticipants, assessableIdentity)
					|| isExcludedByGroup(filterGroupKeys, assessableIdentity)
					|| isExcludedByAssignment(assignmentKeys, assessment)) {
				continue;
			}
			
			TaskLight task = identityToTasks.get(assessableIdentity.getIdentityKey());
			CoachedIdentityRow row = forgeRow(assessableIdentity, mark, task, assessment, entry, fileNameToDefinitions, tasksFolder);
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
		return rows.size();
	}
	
	private CoachedIdentityRow forgeRow(UserPropertiesRow assessableIdentity, IdentityMark mark, TaskLight task, AssessmentEntry assessment,
			RepositoryEntry entry, Map<String,TaskDefinition> fileNameToDefinitions, File tasksFolder) {
		FormLink markLink = uifactory.addFormLink("mark_" + assessableIdentity.getIdentityKey(), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(mark != null ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setUserObject(assessableIdentity.getIdentityKey());

		Date syntheticSubmissionDate = null;
		boolean hasSubmittedDocument = false;
		if(task != null && task.getTaskStatus() != null && task.getTaskStatus() != TaskProcess.assignment && task.getTaskStatus() != TaskProcess.submit) {
			syntheticSubmissionDate = getSyntheticSubmissionDate(task);
			if(syntheticSubmissionDate != null) {
				hasSubmittedDocument = hasSubmittedDocument(task);
			}
		}
		
		DueDate submissionDueDate = null;
		DueDate lateSubmissionDueDate = null;
		if(task != null && syntheticSubmissionDate != null) {
			IdentityRef identityRef = new IdentityRefImpl(assessableIdentity.getIdentityKey());
			DueDate dueDate = gtaManager.getSubmissionDueDate(task, identityRef, null, gtaNode, entry, true);
			if(dueDate != null && dueDate.getDueDate() != null) {
				submissionDueDate = dueDate;
				DueDate lateDueDate = gtaManager.getLateSubmissionDueDate(task, identityRef, null, gtaNode, entry, true);
				if(lateDueDate != null && lateDueDate.getDueDate() != null) {
					lateSubmissionDueDate = lateDueDate;
				}
			}
		}
		
		int numSubmittedDocs = task != null && task.getSubmissionNumOfDocs() != null ? task.getSubmissionNumOfDocs().intValue() : 0;
		int numOfCollectedDocs = task != null && task.getCollectionNumOfDocs() != null ? task.getCollectionNumOfDocs().intValue() : 0;

		String taskName = task == null ? null : task.getTaskName();
		TaskDefinition taskDefinition = null;
		if(StringHelper.containsNonWhitespace(taskName)) {
			taskDefinition = fileNameToDefinitions.get(taskName);
		}
		
		String coach = null;
		Long coachKey = null;
		if(assessment != null && assessment.getCoach() != null) {
			coach = userManager.getUserDisplayName(assessment.getCoach());
			coachKey = assessment.getCoach().getKey();
		}
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++count), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		
		CoachedIdentityRow row = new CoachedIdentityRow(assessableIdentity, task, taskDefinition,
				submissionDueDate, lateSubmissionDueDate, syntheticSubmissionDate, hasSubmittedDocument,
				markLink, toolsLink, assessment, numSubmittedDocs, numOfCollectedDocs, coach, coachKey);
		toolsLink.setUserObject(row);
		if(taskDefinition != null) {
			File file = new File(tasksFolder, taskDefinition.getFilename());
			DownloadLink downloadLink = uifactory.addDownloadLink("task_" + (count++), taskDefinition.getFilename(), null, file, tableEl);
			row.setDownloadTaskFileLink(downloadLink);
		}
		return row;
	}

	private List<AssessmentObligation> getFilterObligations() {
		List<AssessmentObligation> filterObligations = null;
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters != null && !filters.isEmpty()) {
			FlexiTableFilter obligationFilter = FlexiTableFilter.getFilter(filters, "obligation");
			if (obligationFilter != null) {
				List<String> filterValues = ((FlexiTableExtendedFilter)obligationFilter).getValues();
				if (filterValues != null && !filterValues.isEmpty()) {
					filterObligations = filterValues.stream()
							.map(AssessmentObligation::valueOf)
							.collect(Collectors.toList());
				}
			}
		}
		return filterObligations;
	}
	
	private boolean isExcludedByObligation(List<AssessmentObligation> filterObligations, AssessmentEntry assessmentEntry) {
		if (filterObligations == null || filterObligations.isEmpty() || assessmentEntry == null) {
			return false;
		}
		
		AssessmentObligation obligation = assessmentEntry.getObligation() != null
				? assessmentEntry.getObligation().getCurrent()
				: null;
		if (obligation != null && !filterObligations.contains(obligation)) {
			return true;
		}
		if (obligation == null && !filterObligations.contains(AssessmentObligation.mandatory)) {
			return true;
		}
		
		return false;
	}
	
	private Set<ParticipantType> getFilterParticipants() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter membersFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_MEMBERS);
		if(membersFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)membersFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(ParticipantType::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return null;
	}
	
	private boolean isExcludedByParticipant(Set<ParticipantType> filterParticipants, UserPropertiesRow assessableIdentity) {
		if (filterParticipants != null && filterParticipants.size() == 1) {
			if (filterParticipants.contains(ParticipantType.fakeParticipant)) {
				return !fakeParticipantKeys.contains(assessableIdentity.getIdentityKey());
			}
			return fakeParticipantKeys.contains(assessableIdentity.getIdentityKey());
		}
		return false;
	}
	
	private List<String> getFilterGroupKeys() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter groupFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_GROUPS);
		if(groupFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)groupFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues;
			}
		}
		return null;
	}
	
	private boolean isExcludedByGroup(List<String> filterGroupKeys, UserPropertiesRow assessableIdentity) {
		if (filterGroupKeys != null && !filterGroupKeys.isEmpty()) {
			for (String groupKey : filterGroupKeys) {
				List<Long> identities = groupKeyToIdentityKeys.get(groupKey);
				if (identities != null && identities.contains(assessableIdentity.getIdentityKey())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private Set<Long> getFilterAssignmentKeys() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter assignmentFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_ASSIGNED_COACH);
		if(assignmentFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)assignmentFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(Long::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return null;
	}
	
	private boolean isExcludedByAssignment(Set<Long> filterAssignmentKeys, AssessmentEntry entry) {
		if(filterAssignmentKeys == null || entry == null) return false;
		
		Identity assignedCoach = entry.getCoach();
		return (assignedCoach == null && !filterAssignmentKeys.contains(Long.valueOf(-1l)))
					|| (assignedCoach != null && !filterAssignmentKeys.contains(assignedCoach.getKey()));
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editDueDatesCtrl == source || editMultipleDueDatesCtrl == source || assignCoachCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editMultipleDueDatesCtrl);
		removeAsListenerAndDispose(editDueDatesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(assignCoachCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		editMultipleDueDatesCtrl = null;
		editDueDatesCtrl = null;
		toolsCalloutCtrl = null;
		assignCoachCtrl = null;
		contactCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				CoachedIdentityRow row = tableModel.getObject(se.getIndex());
				if("duedates".equals(cmd)) {
					doEditDueDate(ureq, row);
				} else if(StringHelper.containsNonWhitespace(cmd)
						&& !FlexiTableElement.ROW_CHECKED_EVENT.equals(cmd)
						&& !FlexiTableElement.ROW_UNCHECKED_EVENT.equals(cmd)) {
					fireEvent(ureq, new SelectIdentityEvent(row.getIdentityKey()));	
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				updateModel(ureq);
			} else if(event instanceof FlexiTableFilterTabEvent) {
				updateModel(ureq);
				fireEvent(ureq, new MakedEvent(MARKED_TAB_ID.equals(tableEl.getSelectedFilterTab().getId())));
			}
		} else if(bulkExtendButton == source) {
			List<CoachedIdentityRow> rows = getSelectedRows(row -> true);
			doEditMultipleDueDates(ureq, rows);
		} else if(bulkDoneButton == source) {
			doSetDone(ureq);
		} else if(bulkVisibleButton == source) {
			doSetUserVisibility(ureq, true);
		} else if(bulkHiddenButton == source) {
			doSetUserVisibility(ureq, false);
		} else if(bulkEmailButton == source) {
			doEmail(ureq);
		} else if(bulkDownloadButton == source) {
			doBulkDownload(ureq);
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("mark".equals(cmd)) {
				Long assessableIdentityKey = (Long)link.getUserObject();
				boolean marked = doToogleMark(ureq, assessableIdentityKey);
				link.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
				link.getComponent().setDirty(true);
			} else if("tools".equals(cmd) && link.getUserObject() instanceof CoachedIdentityRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private List<CoachedIdentityRow> getSelectedRows(Predicate<CoachedIdentityRow> filter) {
		Set<Integer> selectedItems = tableEl.getMultiSelectedIndex();
		List<CoachedIdentityRow> rows = new ArrayList<>(selectedItems.size());
		if(!selectedItems.isEmpty()) {
			for(Integer i:selectedItems) {
				int index = i.intValue();
				if(index >= 0 && index < tableModel.getRowCount()) {
					CoachedIdentityRow row = tableModel.getObject(index);
					if(row != null && filter.test(row)) {
						rows.add(row);
					}
				}
			}
		}
		return rows;
	}
	
	public int indexOfIdentity(IdentityRef id) {
		return tableModel.indexOf(id);
	}
	
	public int numOfIdentities() {
		return tableModel.getRowCount();
	}
	
	public IdentityRef getIdentity(int index) {
		CoachedIdentityRow row = tableModel.getObject(index);
		return new IdentityRefImpl(row.getIdentityKey());
	}
	
	public List<Identity> getSelectedIdentities(Predicate<CoachedIdentityRow> filter) {
		List<CoachedIdentityRow> selectedRows = getSelectedRows(filter);
		List<IdentityRef> refs = new ArrayList<>(selectedRows.size());
		for(CoachedIdentityRow row:selectedRows) {
			refs.add(new IdentityRefImpl(row.getIdentityKey()));
		}
		return securityManager.loadIdentityByRefs(refs);
	}
	
	private void doAssignCoach(UserRequest ureq, CoachedIdentityRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		Identity currentCoach = null;
		if(row.getCoachKey() != null) {
			currentCoach = securityManager.loadIdentityByKey(row.getCoachKey());
		}
		assignCoachCtrl = new AssignCoachController(ureq, getWindowControl(), assessedIdentity, currentCoach, courseEnv, gtaNode);
		listenTo(assignCoachCtrl);

		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("assign.coach.to", fullname);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), assignCoachCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditDueDate(UserRequest ureq, CoachedIdentityRow row) {
		if(guardModalController(editDueDatesCtrl)) return;
		
		Task task;
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(row.getTask() == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
			task = gtaManager.createAndPersistTask(null, taskList, firstStep, null, assessedIdentity, gtaNode);
		} else {
			task = gtaManager.getTask(row.getTask());
		}

		editDueDatesCtrl = new EditDueDatesController(ureq, getWindowControl(), task, assessedIdentity, null, gtaNode, entry, courseEnv);
		listenTo(editDueDatesCtrl);
		
		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("duedates.user", fullname);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editDueDatesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditMultipleDueDates(UserRequest ureq, List<CoachedIdentityRow> rows) {
		if(guardModalController(editMultipleDueDatesCtrl)) return;
		
		if(rows.isEmpty()) {
			showWarning("error.atleast.task");
		} else {
			List<Task> tasks = new ArrayList<>(rows.size());
			RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			for (CoachedIdentityRow row : rows) {
				if(row.getTask() == null) {
					TaskProcess firstStep = gtaManager.firstStep(gtaNode);
					TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
					tasks.add(gtaManager.createAndPersistTask(null, taskList, firstStep, null, securityManager.loadIdentityByKey(row.getIdentityKey()), gtaNode));
				} else {
					tasks.add(gtaManager.getTask(row.getTask()));
				}
			}
	
			editMultipleDueDatesCtrl = new EditMultipleDueDatesController(ureq, getWindowControl(), tasks, gtaNode, entry, courseEnv);
			listenTo(editMultipleDueDatesCtrl);
			
			String title = translate("duedates.multiple.user");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editMultipleDueDatesCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private boolean doToogleMark(UserRequest ureq, Long particiantKey) {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		Identity participant = securityManager.loadIdentityByKey(particiantKey);
		return gtaManager.toggleMark(entry, gtaNode, ureq.getIdentity(), participant);
	}
	
	private void doSetDone(UserRequest ureq) {
		List<CoachedIdentityRow> rows = getSelectedRows(row -> row.getAssessmentStatus() != AssessmentEntryStatus.done);
		if(rows.isEmpty()) {
			showWarning("warning.bulk.done");
		} else if(assessmentConfig.isAssessable()) {
			ICourse course = CourseFactory.loadCourse(courseEnv.getCourseGroupManager().getCourseEntry());
			
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
			if(taskList == null) {
				taskList = gtaManager.createIfNotExists(entry, gtaNode);
			}
			
			for(CoachedIdentityRow row:rows) {
				Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				doSetStatus(assessedIdentity, AssessmentEntryStatus.done, gtaNode, taskList, course);
				dbInstance.commitAndCloseSession();
			}
			updateModel(ureq);
		}
	}
	
	private void doSetStatus(Identity assessedIdentity, AssessmentEntryStatus status, CourseNode cNode, TaskList taskList, ICourse course) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(cNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), status,
				scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(cNode, doneEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);
		
		Task assignedTask = gtaManager.getTask(assessedIdentity, taskList);
		if(assignedTask == null) {
			gtaManager.createTask(null, taskList, TaskProcess.graded, null, assessedIdentity, gtaNode);
		} else {
			gtaManager.updateTask(assignedTask, TaskProcess.graded, gtaNode, false, getIdentity(), Role.coach);
		}
	}
	
	private void doSetUserVisibility(UserRequest ureq, boolean visible) {
		List<Identity> rows = getSelectedIdentities(row -> true);
		if(rows.isEmpty()) {
			showWarning("warning.bulk.empty");
		} else {
			RepositoryEntry courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			ICourse course = CourseFactory.loadCourse(courseEntry);
			Boolean visibility = Boolean.valueOf(visible);
			rows.forEach(identity -> doSetUserVisibility(course, identity, visibility));
			updateModel(ureq);
		}
	}
	
	private void doSetUserVisibility(ICourse course, Identity assessedIdentity, Boolean userVisibility) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(gtaNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
				scoreEval.getAssessmentStatus(), userVisibility, scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(gtaNode, doneEval, assessedUserCourseEnv, getIdentity(),
				false, Role.coach);
		dbInstance.commitAndCloseSession();
	}
	
	private void doBulkDownload(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities(row -> true);
		ArchiveOptions options = new ArchiveOptions();
		options.setIdentities(identities);
		
		OLATResource courseOres = coachCourseEnv.getCourseEnvironment()
				.getCourseGroupManager().getCourseResource();
		ArchiveResource resource = new ArchiveResource(gtaNode, courseOres, options, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doEmail(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities(row -> true);
		if (identities.isEmpty()) {
			showWarning("error.msg.send.no.rcps");
		} else {
			ContactMessage contactMessage = new ContactMessage(getIdentity());
			String name = courseEnv.getCourseGroupManager().getCourseEntry().getDisplayname();
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
	
	private void doOpenTools(UserRequest ureq, CoachedIdentityRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doSelect(UserRequest ureq, CoachedIdentityRow row) {
		fireEvent(ureq, new SelectIdentityEvent(row.getIdentityKey()));	
	}
	
	public static final class MakedEvent extends Event {

		private static final long serialVersionUID = 1916268792292314400L;
		
		private final boolean marked;
		
		public MakedEvent(boolean marked) {
			super("marked");
			this.marked = marked;
		}

		public boolean isMarked() {
			return marked;
		}
		
	}
	
	private class ToolsController extends BasicController {
		
		private Link assignCoachLink;
		private final Link selectLink;
		private final Link dueDatesLink;
		
		private final CoachedIdentityRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CoachedIdentityRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			selectLink = LinkFactory.createLink("select.assess", "select", getTranslator(), mainVC, this, Link.LINK);
			selectLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			dueDatesLink = LinkFactory.createLink("duedates", "duedates", getTranslator(), mainVC, this, Link.LINK);
			dueDatesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");

			if (coachCourseEnv.isCourseReadOnly()) {
				dueDatesLink.setVisible(false);
			}
			
			if(assessmentConfig.hasCoachAssignment() && assessmentCallback.canAssignCoaches()) {
				assignCoachLink = LinkFactory.createLink("assign.coach", "assign.coach", getTranslator(), mainVC, this, Link.LINK);
				assignCoachLink.setIconLeftCSS("o_icon o_icon-fw o_icon_coach");
			}

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(selectLink == source) {
				doSelect(ureq, row);
			} else if(dueDatesLink == source) {
				doEditDueDate(ureq, row);
			} else if(assignCoachLink == source) {
				doAssignCoach(ureq, row);
			}
		}
	}
}