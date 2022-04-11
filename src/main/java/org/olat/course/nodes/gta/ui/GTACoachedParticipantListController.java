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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
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
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
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
	
	private FormLink bulkDoneButton;
	private FormLink bulkEmailButton;
	private FormLink bulkExtendButton;
	private FormLink bulkVisibleButton;
	private FormLink bulkHiddenButton;
	private FormLink bulkDownloadButton;
	private FlexiTableElement tableEl;
	private CoachParticipantsTableModel tableModel;

	private List<UserPropertiesRow> assessableIdentities;
	private final UserCourseEnvironment coachCourseEnv;
	
	private int count;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean markedOnly;

	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private EditDueDatesController editDueDatesCtrl;
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
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GTACoachedParticipantListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode, boolean markedOnly) {
		super(ureq, wControl, coachCourseEnv.getCourseEnvironment(), gtaNode);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.coachCourseEnv = coachCourseEnv;
		this.markedOnly = markedOnly;

		assessableIdentities = getAssessableIdentities().stream()
				.map(participant -> new UserPropertiesRow(participant, userPropertyHandlers, getLocale()))
				.collect(Collectors.toList());
		
		initForm(ureq);
		updateModel(ureq);
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
		
		return coachCourseEnv.isAdmin()
				? repositoryService.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name())
						.stream().distinct().collect(Collectors.toList())
				: repositoryService.getCoachedParticipants(getIdentity(), re);
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

		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskTitle));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CGCols.taskName));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskStatus, new TaskStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.submissionDate, new SubmissionDateCellRenderer(getTranslator())));
		
		DefaultFlexiColumnModel userVisibilityCol = new DefaultFlexiColumnModel(CGCols.userVisibility, new UserVisibilityCellRenderer(false));
		userVisibilityCol.setIconHeader("o_icon o_icon-fw o_icon_results_hidden");

		columnsModel.addFlexiColumnModel(userVisibilityCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.passed, new PassedCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.numOfSubmissionDocs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		if(gtaManager.isDueDateEnabled(gtaNode) && !coachCourseEnv.isCourseReadOnly()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.duedates", translate("duedates"), "duedates"));
		}
		tableModel = new CoachParticipantsTableModel(getLocale(), columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_course_gta_coached_participants");
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "gta-coached-participants-v3-" + markedOnly);
		
		initBulkTools(ureq, formLayout);
	}
	
	protected void initBulkTools(@SuppressWarnings("unused") UserRequest ureq, FormItemContainer formLayout) {
		if(courseAssessmentService.getAssessmentConfig(gtaNode).isAssessable()) {
			bulkDoneButton = uifactory.addFormLink("bulk.done", formLayout, Link.BUTTON);
			bulkDoneButton.setElementCssClass("o_sel_assessment_bulk_done");
			bulkDoneButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
			bulkDoneButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			tableEl.addBatchButton(bulkDoneButton);
			
			boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
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
		
		initFilters();
	}
	
	private void initFilters() {
		if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(coachCourseEnv).getType())) {
			SelectionValues obligationValues = new SelectionValues();
			obligationValues.add(SelectionValues.entry(AssessmentObligation.mandatory.name(), translate("filter.mandatory")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.optional.name(), translate("filter.optional")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.excluded.name(), translate("filter.excluded")));
			FlexiTableMultiSelectionFilter obligationFilter = new FlexiTableMultiSelectionFilter(translate("filter.obligation"),
					AssessedIdentityListState.FILTER_OBLIGATION, obligationValues, true);
			obligationFilter.setValues(List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name()));
			tableEl.setFilters(true, List.of(obligationFilter), false, true);
		}
	}
	
	protected void updateModel(UserRequest ureq) {
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
		for(UserPropertiesRow assessableIdentity:assessableIdentities) {
			IdentityMark mark = identityToMarks.get(assessableIdentity.getIdentityKey());
			if (markedOnly && mark == null) continue;
			
			AssessmentEntry assessment = identityToAssessments.get(assessableIdentity.getIdentityKey());
			if (isExcludedByObligation(filterObligations, assessment)) continue;
			
			FormLink markLink = uifactory.addFormLink("mark_" + assessableIdentity.getIdentityKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(mark != null ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			markLink.setUserObject(assessableIdentity.getIdentityKey());
			
			TaskLight task = identityToTasks.get(assessableIdentity.getIdentityKey());
			Date submissionDueDate = null;
			if(task == null || task.getTaskStatus() == null || task.getTaskStatus() == TaskProcess.assignment) {
				IdentityRef identityRef = new IdentityRefImpl(assessableIdentity.getIdentityKey());
				DueDate dueDate = gtaManager.getSubmissionDueDate(task, identityRef, null, gtaNode, entry, true);
				if(dueDate != null) {
					submissionDueDate = dueDate.getDueDate();
				}
			} 

			Date syntheticSubmissionDate = null;
			boolean hasSubmittedDocument = false;
			if(task != null && task.getTaskStatus() != null && task.getTaskStatus() != TaskProcess.assignment && task.getTaskStatus() != TaskProcess.submit) {
				syntheticSubmissionDate = getSyntheticSubmissionDate(task);
				if(syntheticSubmissionDate != null) {
					hasSubmittedDocument = hasSubmittedDocument(task);
				}
			}
			
			int numSubmittedDocs = task != null && task.getSubmissionNumOfDocs() != null ? task.getSubmissionNumOfDocs().intValue() : 0;
			int numOfCollectedDocs = task != null && task.getCollectionNumOfDocs() != null ? task.getCollectionNumOfDocs().intValue() : 0;

			String taskName = task == null ? null : task.getTaskName();
			TaskDefinition taskDefinition = null;
			if(StringHelper.containsNonWhitespace(taskName)) {
				taskDefinition = fileNameToDefinitions.get(taskName);
			}
			
			CoachedIdentityRow row = new CoachedIdentityRow(assessableIdentity, task, taskDefinition, submissionDueDate, syntheticSubmissionDate,
					hasSubmittedDocument, markLink, assessment, numSubmittedDocs, numOfCollectedDocs);
			if(taskDefinition != null) {
				File file = new File(tasksFolder, taskDefinition.getFilename());
				DownloadLink downloadLink = uifactory.addDownloadLink("task_" + (count++), taskDefinition.getFilename(), null, file, tableEl);
				row.setDownloadTaskFileLink(downloadLink);
			}
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
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
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editDueDatesCtrl == source || editMultipleDueDatesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editMultipleDueDatesCtrl);
		removeAsListenerAndDispose(editDueDatesCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		editMultipleDueDatesCtrl = null;
		editDueDatesCtrl = null;
		contactCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
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
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("mark".equals(cmd)) {
				Long assessableIdentityKey = (Long)link.getUserObject();
				boolean marked = doToogleMark(ureq, assessableIdentityKey);
				link.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
				link.getComponent().setDirty(true);
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
	
	public List<Identity> getSelectedIdentities(Predicate<CoachedIdentityRow> filter) {
		List<CoachedIdentityRow> selectedRows = getSelectedRows(filter);
		List<IdentityRef> refs = new ArrayList<>(selectedRows.size());
		for(CoachedIdentityRow row:selectedRows) {
			refs.add(new IdentityRefImpl(row.getIdentityKey()));
		}
		return securityManager.loadIdentityByRefs(refs);
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
		cmc = new CloseableModalController(getWindowControl(), "close", editDueDatesCtrl.getInitialComponent(), true, title, true);
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
			cmc = new CloseableModalController(getWindowControl(), "close", editMultipleDueDatesCtrl.getInitialComponent(), true, title, true);
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
		} else if(courseAssessmentService.getAssessmentConfig(gtaNode).isAssessable()) {
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
}