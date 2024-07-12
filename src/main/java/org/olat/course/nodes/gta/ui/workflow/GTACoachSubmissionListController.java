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
package org.olat.course.nodes.gta.ui.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskHelper.FilesLocked;
import org.olat.course.nodes.gta.TaskLateStatus;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.DirectoryController;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.component.TaskStepAdditionalInfosCellRenderer;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantTableModel.CoachCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachSubmissionListController extends AbstractCoachWorkflowListController implements FlexiTableComponentDelegate {

	private final VelocityContainer detailsVC;
	
	private FormLink bulkBackToSubmissionButton;
	private FormLink bulkCollectSubmissionsLink;
	
	private DialogBoxController confirmCollectCtrl;
	private DialogBoxController confirmBulkCollectCtrl;
	private DialogBoxController confirmBackToSubmissionCtrl;
	private DialogBoxController confirmBulkBackToSubmissionCtrl;
	
	public GTACoachSubmissionListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, List<Identity> assessableIdentities, GTACourseNode gtaNode) {
		super(ureq, wControl, "submission_list", coachCourseEnv, assessableIdentities, gtaNode);
		
		detailsVC = createVelocityContainer("submission_details");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
 
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}

	@Override
	protected void initConfigurationInfos(InfoPanelItem panel) {
		StringBuilder infos = new StringBuilder();
		
		String deadlineInfos = null;
		String lateDeadlineInfos = null;
		DueDateConfig dueDateConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		if(dueDateConfig != DueDateConfig.noDueDateConfig()) {
			String dueDateVal = dueDateConfigToString(dueDateConfig);
			if(StringHelper.containsNonWhitespace(dueDateVal)) {
				deadlineInfos = translate("workflow.deadline.submission", dueDateVal);
			}
		}
			
		DueDateConfig lateDueDateConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_LATE_SUBMIT_DEADLINE);
		if(lateDueDateConfig != DueDateConfig.noDueDateConfig()) {
			String lateDueDateVal = dueDateConfigToString(dueDateConfig);
			if(StringHelper.containsNonWhitespace(lateDueDateVal)) {
				lateDeadlineInfos = translate("workflow.deadline.submission.late", lateDueDateVal);
			}
		}

		if(deadlineInfos != null || lateDeadlineInfos != null) {
			String allDeadlinesInfos = deadlineInfos;
			if(allDeadlinesInfos == null) {
				allDeadlinesInfos = lateDeadlineInfos;
			} else if(lateDeadlineInfos != null) {
				allDeadlinesInfos += " - " + lateDeadlineInfos;
			}
			infos.append("<p><i class='o_icon o_icon-fw o_icon_timelimit'> </i> ").append(allDeadlinesInfos).append("</p>");
		}
	
		// Number of documents
		int minDocs = gtaNode.getModuleConfiguration().getIntegerSafe(GTACourseNode.GTASK_MIN_SUBMITTED_DOCS, -1);
		int maxDocs = gtaNode.getModuleConfiguration().getIntegerSafe(GTACourseNode.GTASK_MAX_SUBMITTED_DOCS, -1);
		String numberOfDocs = numberOfDocuments(minDocs, maxDocs);
		if(StringHelper.containsNonWhitespace(numberOfDocs)) {
			infos.append("<p><i class='o_icon o_icon-fw o_icon_file'> </i> ").append(numberOfDocs).append("</p>");
		}
		
		boolean emailConfirmation = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_MAIL_CONFIRMATION);
		if(emailConfirmation) {
			infos.append("<p><i class='o_icon o_icon-fw o_icon_message_open'> </i> ").append(translate("workflow.infos.email.confirmation")).append("</p>");
		}
		panel.setInformations(infos.toString());
	}

	@Override
	protected void initColumnsModel(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.numOfSubmissionDocs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.submissionOverrideDueDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.submissionDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.additionalInfosStatus,
				new TaskStepAdditionalInfosCellRenderer(getTranslator())));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.waiting.name(), translate(CoachedParticipantStatus.waiting.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.done.name(), translate(CoachedParticipantStatus.done.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.expired.name(), translate(CoachedParticipantStatus.expired.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.notAvailable.name(), translate(CoachedParticipantStatus.notAvailable.i18nKey())));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.assignment.status"),
				FILTER_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
	}
	
	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab waitingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WAITING_TAB_ID, translate(CoachedParticipantStatus.waiting.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.waiting.name()))));
		tabs.add(waitingTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(DONE_TAB_ID, translate(CoachedParticipantStatus.done.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.done.name()))));
		tabs.add(doneTab);
		
		FlexiFiltersTab expiredTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EXPIRED_TAB_ID, translate(CoachedParticipantStatus.expired.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.expired.name()))));
		tabs.add(expiredTab);
		
		FlexiFiltersTab notAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.notAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.notAvailable.name()))));
		tabs.add(notAvailableTab);
	}
	
	@Override
	protected void initBulkTools(FormItemContainer formLayout) {
		initBulkExtendTool(formLayout);
		
		bulkBackToSubmissionButton = uifactory.addFormLink("coach.back.to.submission", formLayout, Link.BUTTON);
		bulkBackToSubmissionButton.setElementCssClass("o_sel_assessment_bulk_back_to_submission");
		bulkBackToSubmissionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_in_progress");
		bulkBackToSubmissionButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkBackToSubmissionButton);
		
		bulkCollectSubmissionsLink = uifactory.addFormLink("coach.collect.task", formLayout, Link.BUTTON);
		bulkCollectSubmissionsLink.setElementCssClass("o_sel_assessment_bulk_collect_submission");
		bulkCollectSubmissionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
		bulkCollectSubmissionsLink.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkCollectSubmissionsLink);
		
		super.initBulkTools(formLayout);
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof CoachedParticipantRow participantRow) {
			if(participantRow.getDetailsControllerEl() != null) {
				components.add(participantRow.getDetailsControllerEl().getComponent());
			}
			if(participantRow.getCollectDocumentsLink() != null) {
				components.add(participantRow.getCollectDocumentsLink().getComponent());
			}
			if(participantRow.getBackToSubmissionLink() != null) {
				components.add(participantRow.getBackToSubmissionLink().getComponent());
			}
		}
		return components;
	}

	@Override
	protected void loadModel() {
		super.loadModel();
	}

	@Override
	protected CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry) {
		identityRow.setToolsLink(forgeToolsLink(identityRow));
		
		// late date
		Task assignedTask = identityRow.getTask();
		IdentityRef assessedIdentity = new IdentityRefImpl(identityRow.getIdentityKey());
		DueDate submissionDueDate = gtaManager.getSubmissionDueDate(assignedTask, assessedIdentity, null, gtaNode, entry, true);
		identityRow.setSubmissionDueDate(submissionDueDate);
		DueDate lateSubmissionDueDate = gtaManager.getLateSubmissionDueDate(assignedTask, assessedIdentity, null, gtaNode, entry, true);
		identityRow.setLateSubmissionDueDate(lateSubmissionDueDate);
		
		status(identityRow);
		lateStatus(identityRow);
		
		return identityRow;
	}
	
	private void status(CoachedParticipantRow identityRow) {
		Task assignedTask = identityRow.getTask();

		//calculate state
		identityRow.setCanViewSubmittedDocuments(false);
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
				identityRow.setStatus(CoachedParticipantStatus.notAvailable);
			} else if (assignedTask.getTaskStatus() == TaskProcess.submit) {
				if(isSubmissionLate(identityRow.getSubmissionDueDate(), identityRow.getLateSubmissionDueDate())) {
					identityRow.setStatus(CoachedParticipantStatus.late);
				} else {
					identityRow.setStatus(CoachedParticipantStatus.waiting);
				}
				identityRow.setCanCollectSubmission(true);
			} else {
				identityRow.setStatus(CoachedParticipantStatus.done);
				identityRow.setCanViewSubmittedDocuments(true);
			}	
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			if(isSubmissionLate(identityRow.getSubmissionDueDate(), identityRow.getLateSubmissionDueDate())) {
				identityRow.setStatus(CoachedParticipantStatus.late);
			} else {
				identityRow.setStatus(CoachedParticipantStatus.open);
			}

			identityRow.setCanCollectSubmission(true);
		} else {
			identityRow.setStatus(CoachedParticipantStatus.done);
			identityRow.setCanViewSubmittedDocuments(true);
		}
		
		if (assignedTask == null || (assignedTask.getTaskStatus() != TaskProcess.submit)) {
			backToSubmission(identityRow, assignedTask);
		}
		if(identityRow.isCanCollectSubmission() && !coachCourseEnv.isCourseReadOnly()) {
			FormLink collectSubmissionsLink = uifactory.addFormLink("collect-" + (++count), "collect-documents", "coach.collect.task", null, flc, Link.BUTTON);
			collectSubmissionsLink.setUserObject(identityRow);
			identityRow.setCollectDocumentsLink(collectSubmissionsLink);
		}
	}
	
	private void lateStatus(CoachedParticipantRow identityRow) {
		if(identityRow.getTaskStatus() != null && identityRow.getTaskStatus() != TaskProcess.assignment && identityRow.getTaskStatus() != TaskProcess.submit) {
			Task task = identityRow.getTask();
			int numOfSubmittedDocs = numOfSubmittedDocs(task);
			Date syntheticSubmissionDate = gtaManager.getSyntheticSubmissionDate(identityRow.getTask());
			if(syntheticSubmissionDate != null && task.getTaskStatus() != null && numOfSubmittedDocs > 0) {
				RepositoryEntry courseEntry = null;
				DueDate submissionDueDate = gtaManager.getSubmissionDueDate(task, identityRow.getAssessedIdentity(), null, gtaNode, courseEntry, true);
				if(submissionDueDate != null) {
					Date refDate = submissionDueDate.getReferenceDueDate();
					Date extensionDate = submissionDueDate.getOverridenDueDate();
					Date refLateDate = null;
					if(refDate != null) {
						DueDate lateSubmissionDueDate =  gtaManager.getLateSubmissionDueDate(task, identityRow.getAssessedIdentity(), null, gtaNode, courseEntry, true);
						refLateDate = lateSubmissionDueDate == null ? null : lateSubmissionDueDate.getReferenceDueDate();
					}
		
					TaskLateStatus lateStatus = gtaManager.evaluateSubmissionLateStatus(syntheticSubmissionDate, refDate, refLateDate, extensionDate);
					identityRow.setLateStatus(lateStatus);
				}
			}
		}
	}
	
	private int numOfSubmittedDocs(Task task) {
		Integer numOfDocs = task.getSubmissionNumOfDocs();
		Date date = task.getSubmissionDate();
		if(date == null || (task.getCollectionDate() != null && task.getCollectionDate().after(date))) {
			numOfDocs = task.getCollectionNumOfDocs();
		}
		return numOfDocs == null ? 0 : numOfDocs.intValue();

	}
	
	protected final boolean isSubmissionLate(DueDate dueDate, DueDate lateDueDate) {
		if(dueDate == null || dueDate.getDueDate() == null
				|| lateDueDate == null || lateDueDate.getDueDate() == null) {
			return false;
		}
		Date refDate = dueDate.getDueDate();
		return now.after(refDate);
	}
	
	private void backToSubmission(CoachedParticipantRow identityRow, Task assignedTask) {
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT) && assignedTask != null) {
			Date now = new Date();
			DueDate dueDate = identityRow.getSubmissionDueDate();
			if(!coachCourseEnv.isCourseReadOnly() && (dueDate == null || dueDate.getDueDate() == null || now.before(dueDate.getDueDate()))) {
				FormLink backToSubmissionLink = uifactory.addFormLink("back-" + (++count), "back-submisssion", "coach.back.to.submission", null, flc, Link.BUTTON);
				backToSubmissionLink.setUserObject(identityRow);
				identityRow.setBackToSubmissionLink(backToSubmissionLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCollectCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				CoachedParticipantRow row = (CoachedParticipantRow)confirmCollectCtrl.getUserObject();
				doCollectTask(row);
				loadModel();
			}
		} else if(confirmBulkCollectCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<CoachedParticipantRow> rows = (List<CoachedParticipantRow>)confirmBulkCollectCtrl.getUserObject();
				for(CoachedParticipantRow row:rows) {
					doCollectTask(row);
				}
				loadModel();
			}
		} else if(confirmBackToSubmissionCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				CoachedParticipantRow row = (CoachedParticipantRow)confirmBackToSubmissionCtrl.getUserObject();
				doFinalizeBackToSubmission(row);
				loadModel();
			}
		} else if(confirmBulkBackToSubmissionCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<CoachedParticipantRow> rows = (List<CoachedParticipantRow>)confirmBulkBackToSubmissionCtrl.getUserObject();
				for(CoachedParticipantRow row:rows) {
					doFinalizeBackToSubmission(row);
				}
				loadModel();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bulkBackToSubmissionButton == source) {
			doConfirmBulkBackToSubmission(ureq);
		} else if(bulkCollectSubmissionsLink == source) {
			doConfirmBulkCollectTask(ureq);
		} else if(tableEl == source) {
			if(event instanceof DetailsToggleEvent toggleEvent) {
				CoachedParticipantRow row = tableModel.getObject(toggleEvent.getRowIndex());
				doOpenDetails(ureq, row);
			}
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("back-submisssion".equals(cmd) && link.getUserObject() instanceof CoachedParticipantRow row) {
				doConfirmBackToSubmission(ureq, row);
			} else if("collect-documents".equals(cmd) && link.getUserObject() instanceof CoachedParticipantRow row) {
				doConfirmCollectTask(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected Controller createToolsController(UserRequest ureq, CoachedParticipantRow row) {
		return new SubmissionToolsController(ureq, getWindowControl(), row);
	}
	
	private void doOpenDetails(UserRequest ureq, CoachedParticipantRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
		VFSContainer documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);
		
		if(row.getCollectDocumentsLink() != null) {
			detailsVC.put(row.getCollectDocumentsLinkName(), row.getCollectDocumentsLink().getComponent());
		}
		if(row.getBackToSubmissionLink() != null) {
			detailsVC.put(row.getBackToSubmissionLinkName(), row.getBackToSubmissionLink().getComponent());
		}
		
		if(row.getDetailsCtrl() != null) {
			removeAsListenerAndDispose(row.getDetailsCtrl());
			row.setDetailsCtrl(null);
			row.setDetailsControllerEl(null);
		}
		
		if(row.isCanViewSubmittedDocuments() && !documentsContainer.getItems(new VFSSystemItemFilter()).isEmpty()) {
			DirectoryController submittedDocCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"coach.submitted.documents.description", "bulk.submitted.documents", "submission");
			listenTo(submittedDocCtrl);
			ComponentWrapperElement wrapperEl = new ComponentWrapperElement(submittedDocCtrl.getInitialComponent());
			wrapperEl.setRootForm(mainForm);
			flc.add(wrapperEl);
			row.setDetailsControllerEl(wrapperEl);
			row.setDetailsCtrl(submittedDocCtrl);
		}
	}
	
	private void doConfirmCollectTask(UserRequest ureq, CoachedParticipantRow row) {
		Identity assessedIdentity = row.getAssessedIdentity();
		VFSContainer documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);
		File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
		File[] submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));

		FilesLocked lockedBy = TaskHelper.getDocumentsLocked(documentsContainer, submittedDocuments);
		if(lockedBy != null) {
			showWarning("warning.submit.documents.edited", new String[]{ lockedBy.getLockedBy(), lockedBy.getLockedFiles() });
		} else {
			String fullName = userManager.getUserDisplayName(assessedIdentity);	
			String title = translate("coach.collect.confirm.title");
			String text = translate("coach.collect.confirm.text", fullName);
			text = "<div class='o_warning'>" + text + "</div>";
			confirmCollectCtrl = activateOkCancelDialog(ureq, title, text, confirmCollectCtrl);
			confirmCollectCtrl.setUserObject(row);
			listenTo(confirmCollectCtrl);
		}
	}
	
	private void doConfirmBulkCollectTask(UserRequest ureq) {
		List<CoachedParticipantRow> rows = getSelectedRows(row -> row.isCanCollectSubmission());
		if(rows.isEmpty()) {
			showWarning("warning.bulk.empty");
		} else {
			boolean locked = false;
			for(CoachedParticipantRow row:rows) {
				VFSContainer documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, row.getAssessedIdentity());
				File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, row.getAssessedIdentity());
				File[] submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
				FilesLocked lockedBy = TaskHelper.getDocumentsLocked(documentsContainer, submittedDocuments);
				if(lockedBy != null) {
					showWarning("warning.submit.documents.edited", new String[]{ lockedBy.getLockedBy(), lockedBy.getLockedFiles() });
					locked |= true;
				}
			}
			
			if(!locked) {
				String fullNames = getFullNames(rows);
				String title = translate("coach.collect.confirm.title");
				String text = translate("coach.collect.confirm.text", fullNames);
				text = "<div class='o_warning'>" + text + "</div>";
				confirmBulkCollectCtrl = activateOkCancelDialog(ureq, title, text, confirmBulkCollectCtrl);
				confirmBulkCollectCtrl.setUserObject(rows);
				listenTo(confirmBulkCollectCtrl);
			}
		}
	}
	
	private void doCollectTask(CoachedParticipantRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		doCollectTask(row.getTask(), assessedIdentity);
	}
	
	private void doCollectTask(Task task, Identity assessedIdentity) {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		TaskList taskList = gtaManager.createIfNotExists(entry, gtaNode);
		File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
		File[] submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		
		if(task == null && gtaNode.isOptional(courseEnv, userCourseEnv)) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createTask(null, taskList, firstStep, null, assessedIdentity, gtaNode);
		} else {
			task = gtaManager.getTask(task);
		}
		
		int numOfDocs = submittedDocuments == null ? 0 : submittedDocuments.length;
		task = gtaManager.collectTask(task, gtaNode, numOfDocs, getIdentity());
		showInfo("run.documents.successfully.submitted");
		
		TaskMultiUserEvent event = new TaskMultiUserEvent(TaskMultiUserEvent.SUMBIT_TASK,
				assessedIdentity, null, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, taskListEventResource);
		
		gtaManager.log("Collect", "collect documents", task, getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
	}
	
	private void doConfirmBackToSubmission(UserRequest ureq, CoachedParticipantRow row) {
		String fullName = userManager.getUserDisplayName(row.getIdentityKey());			

		String title = translate("coach.back.to.submission.confirm.title");
		String text = translate("coach.back.to.submission.confirm.text", fullName);
		text = "<div class='o_warning'>" + text + "</div>";
		confirmBackToSubmissionCtrl = activateOkCancelDialog(ureq, title, text, confirmBackToSubmissionCtrl);
		confirmBackToSubmissionCtrl.setUserObject(row);
		listenTo(confirmBackToSubmissionCtrl);
	}
	
	private void doConfirmBulkBackToSubmission(UserRequest ureq) {
		List<CoachedParticipantRow> rows = getSelectedRows(row -> canBackToSubmission(row));
		if(rows.isEmpty()) {
			showWarning("warning.bulk.empty");
		} else {
			String fullNames = getFullNames(rows);
			String title = translate("coach.back.to.submission.confirm.title");
			String text = translate("coach.back.to.submission.confirm.text", fullNames);
			text = "<div class='o_warning'>" + text + "</div>";
			confirmBulkBackToSubmissionCtrl = activateOkCancelDialog(ureq, title, text, confirmBulkBackToSubmissionCtrl);
			confirmBulkBackToSubmissionCtrl.setUserObject(rows);
			listenTo(confirmBulkBackToSubmissionCtrl);
		}
	}
	
	private void doFinalizeBackToSubmission(CoachedParticipantRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		TaskProcess submit = gtaManager.previousStep(TaskProcess.review, gtaNode);//only submit allowed
		if(submit == TaskProcess.submit) {
			Task task = row.getTask();
			task = gtaManager.updateTask(task, submit, gtaNode, false, getIdentity(), Role.coach);
			
			gtaManager.log("Back to submission", "revert status of task back to submission", task,
					getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
		}
	}
	
	private boolean canBackToSubmission(CoachedParticipantRow row) {
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT) &&
				(row.getTask() == null || row.getTask().getTaskStatus() != TaskProcess.submit)) {
			Date now = new Date();
			DueDate dueDate = row.getSubmissionDueDate();
			if(!coachCourseEnv.isCourseReadOnly() && (dueDate == null || dueDate.getDueDate() == null || now.before(dueDate.getDueDate()))) {
				return true;
			}
		}
		return false;
	}
	
	private class SubmissionToolsController extends BasicController {

		private Link dueDatesLink;
		private Link backToSubmissionLink;
		private Link collectSubmissionsLink;
		
		private CoachedParticipantRow row;
		
		public SubmissionToolsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			dueDatesLink = LinkFactory.createLink("duedates", "duedates", getTranslator(), mainVC, this, Link.LINK);
			dueDatesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			
			if(row.isCanCollectSubmission()) {
				collectSubmissionsLink = LinkFactory.createLink("coach.collect.task", mainVC, this);
				collectSubmissionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
			}
			if(canBackToSubmission(row)) {
				backToSubmissionLink = LinkFactory.createLink("coach.back.to.submission", mainVC, this);
				backToSubmissionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_in_progress");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(dueDatesLink == source) {
				doEditDueDate(ureq, row);
			} else if(collectSubmissionsLink == source) {
				doConfirmCollectTask(ureq, row);
			} else if(backToSubmissionLink == source) {
				doConfirmBackToSubmission(ureq, row);
			}
		}
	}
}
