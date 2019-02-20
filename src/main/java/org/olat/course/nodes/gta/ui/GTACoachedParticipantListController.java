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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.ui.tool.UserVisibilityCellRenderer;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.CoachParticipantsTableModel.CGCols;
import org.olat.course.nodes.gta.ui.component.SubmissionDateCellRenderer;
import org.olat.course.nodes.gta.ui.component.TaskStatusCellRenderer;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
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
	
	private FlexiTableElement tableEl;
	private CoachParticipantsTableModel tableModel;

	private List<UserPropertiesRow> assessableIdentities;
	private final UserCourseEnvironmentImpl coachCourseEnv;
	
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean markedOnly;

	private CloseableModalController cmc;
	private EditDueDatesController editDueDatesCtrl;
	private EditMultipleDueDatesController editMultipleDueDatesCtrl;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentService assessmentService;
	
	private FormLink extendButton;
	
	public GTACoachedParticipantListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, GTACourseNode gtaNode, boolean markedOnly) {
		super(ureq, wControl, userCourseEnv.getCourseEnvironment(), gtaNode);
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		coachCourseEnv = (UserCourseEnvironmentImpl)userCourseEnv;
		this.markedOnly = markedOnly;

		assessableIdentities = new ArrayList<>();
		collectIdentities((participant) ->
				assessableIdentities.add(new UserPropertiesRow(participant, userPropertyHandlers, getLocale())));
		
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
		List<Identity> identities = new ArrayList<>();
		collectIdentities(new Consumer<Identity>() {
			@Override
			public void accept(Identity participant) {
				identities.add(participant);
			}
		});
		return identities;
	}
	
	private void collectIdentities(Consumer<Identity> participantCollector) {
		Set<Identity> duplicateKiller = new HashSet<>();
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		boolean admin = coachCourseEnv.isAdmin();
		
		List<BusinessGroup> coachedGroups = admin ? cgm.getAllBusinessGroups() : coachCourseEnv.getCoachedGroups();
		List<Identity> participants = businessGroupService.getMembers(coachedGroups, GroupRoles.participant.name());
		for(Identity participant:participants) {
			if(!duplicateKiller.contains(participant)) {
				participantCollector.accept(participant);
				duplicateKiller.add(participant);
			}
		}
		
		RepositoryEntry re = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		boolean repoTutor = admin || (coachedGroups.isEmpty() && repositoryService.hasRole(getIdentity(), re, GroupRoles.coach.name()));
		if(repoTutor) {
			List<Identity> courseParticipants = repositoryService.getMembers(re, GroupRoles.participant.name());
			for(Identity participant:courseParticipants) {
				if(!duplicateKiller.contains(participant)) {
					participantCollector.accept(participant);
					duplicateKiller.add(participant);
				}
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.mark.i18nKey(), CGCols.mark.ordinal(),
				true, CGCols.mark.name()));
		
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.username.i18nKey(), CGCols.username.ordinal(),
					true, CGCols.username.name()));
		}
		
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
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskName.i18nKey(), CGCols.taskName.ordinal(),
					true, CGCols.taskName.name()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskStatus.i18nKey(), CGCols.taskStatus.ordinal(),
				true, CGCols.taskStatus.name(), new TaskStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.submissionDate.i18nKey(), CGCols.submissionDate.ordinal(),
				true, CGCols.submissionDate.name(), new SubmissionDateCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.userVisibility.i18nKey(), CGCols.userVisibility.ordinal(),
				true, CGCols.userVisibility.name(), new UserVisibilityCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.score.i18nKey(), CGCols.score.ordinal(),
				true, CGCols.score.name(), new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.passed.i18nKey(), CGCols.passed.ordinal(),
				true, CGCols.passed.name(), new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.numOfSubmissionDocs.i18nKey(), CGCols.numOfSubmissionDocs.ordinal(),
				true, CGCols.numOfSubmissionDocs.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		if(gtaManager.isDueDateEnabled(gtaNode)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.duedates", translate("duedates"), "duedates"));
		}
		tableModel = new CoachParticipantsTableModel(userPropertyHandlers, getLocale(), columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, 10, false, getTranslator(), formLayout);
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "gta-coached-participants-" + markedOnly);
		if(gtaManager.isDueDateEnabled(gtaNode) && !gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES)) {
			tableEl.setMultiSelect(true);
			extendButton = uifactory.addFormLink("extend.list", "duedates", "duedates", formLayout, Link.BUTTON);
		}
	}
	
	protected void updateModel(UserRequest ureq) {
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

			AssessmentEntry assessment = identityToAssessments.get(assessableIdentity.getIdentityKey());
			Boolean userVisibility = assessment!=null? assessment.getUserVisibility(): null;
			BigDecimal score = assessment!=null? assessment.getScore(): null;
			Boolean passed = assessment!=null? assessment.getPassed(): null;
			
			rows.add(new CoachedIdentityRow(assessableIdentity, task, submissionDueDate, syntheticSubmissionDate,
					hasSubmittedDocument, markLink, userVisibility, score, passed,
					numSubmittedDocs, numOfCollectedDocs));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editDueDatesCtrl == source || editMultipleDueDatesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel(ureq);
			}
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
		removeAsListenerAndDispose(cmc);
		editMultipleDueDatesCtrl = null;
		editDueDatesCtrl = null;
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
				} else if(StringHelper.containsNonWhitespace(cmd)) {
					fireEvent(ureq, new SelectIdentityEvent(row.getIdentity().getIdentityKey()));	
				}
			}
		} else if(extendButton == source) {
			List<CoachedIdentityRow> rows = getSelectedRows();
			doEditMultipleDueDates(ureq, rows);
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
	
	private List<CoachedIdentityRow> getSelectedRows() {
		Set<Integer> selectedItems = tableEl.getMultiSelectedIndex();
		List<CoachedIdentityRow> rows = new ArrayList<>(selectedItems.size());
		if(!selectedItems.isEmpty()) {
			for(Integer i:selectedItems) {
				int index = i.intValue();
				if(index >= 0 && index < tableModel.getRowCount()) {
					CoachedIdentityRow row = tableModel.getObject(index);
					if(row != null) {
						rows.add(row);
					}
				}
			}
		}
		return rows;
	}
	
	private void doEditDueDate(UserRequest ureq, CoachedIdentityRow row) {
		if(editDueDatesCtrl != null) return;
		
		Task task;
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentity().getIdentityKey());
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
		String title = translate("duedates.user", new String[] { fullname });
		cmc = new CloseableModalController(getWindowControl(), "close", editDueDatesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditMultipleDueDates(UserRequest ureq, List<CoachedIdentityRow> rows) {
		if(editMultipleDueDatesCtrl != null) return;
		
		if(rows.isEmpty()) {
			showWarning("error.atleast.task");
		} else {
			List<Task> tasks = new ArrayList<>(rows.size());
			RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			for (CoachedIdentityRow row : rows) {
				if(row.getTask() == null) {
					TaskProcess firstStep = gtaManager.firstStep(gtaNode);
					TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
					tasks.add(gtaManager.createAndPersistTask(null, taskList, firstStep, null, securityManager.loadIdentityByKey(row.getIdentity().getIdentityKey()), gtaNode));
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
}