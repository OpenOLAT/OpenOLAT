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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskHelper.FilesLocked;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.ui.ConfirmRevisionsController;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.GTACoachRevisionAndCorrectionsController;
import org.olat.course.nodes.gta.ui.SubmitDocumentsController;
import org.olat.course.nodes.gta.ui.component.TaskReviewAndCorrectionFeedbackCellRenderer;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantTableModel.CoachCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachRevisionListController extends AbstractCoachWorkflowListController implements FlexiTableComponentDelegate {

	private final VelocityContainer detailsVC;
	
	private Map<Task,TaskRevision> loadCachedRevisions;
	
	private DialogBoxController confirmCollectCtrl;
	private DialogBoxController confirmCloseRevisionProcessCtrl;
	private ConfirmRevisionsController confirmReturnToRevisionsCtrl;
	
	public GTACoachRevisionListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, List<Identity> assessableIdentities, GTACourseNode gtaNode) {
		super(ureq, wControl, "revision_list", coachCourseEnv, assessableIdentities, gtaNode);

		detailsVC = createVelocityContainer("revision_details");
		
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
		
		int minDocs = getDefaultNumbersOfDocuments(GTACourseNode.GTASK_MIN_REVISED_DOCS, GTACourseNode.GTASK_MIN_SUBMITTED_DOCS);
		int maxDocs = getDefaultNumbersOfDocuments(GTACourseNode.GTASK_MAX_REVISED_DOCS, GTACourseNode.GTASK_MAX_SUBMITTED_DOCS);
		String numberOfDocs = this.numberOfDocuments(minDocs, maxDocs);
		if(StringHelper.containsNonWhitespace(numberOfDocs)) {
			infos.append("<p><i class='o_icon o_icon-fw o_icon_file'> </i> ").append(numberOfDocs).append("</p>");
		}
		
		panel.setInformations(infos.toString());
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		if(rowObject instanceof CoachedParticipantRow participantRow) {
			return participantRow.getStatus() != CoachedParticipantStatus.notAvailable;
		}
		return true;
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof CoachedParticipantRow participantRow) {
			if(participantRow.getDetailsCtrl() instanceof FormBasicController formCtrl) {
				components.add(formCtrl.getInitialFormItem().getComponent());
			} else if(participantRow.getDetailsCtrl() != null) {
				components.add(participantRow.getDetailsCtrl().getInitialComponent());
			}
		}
		return components;
	}

	@Override
	protected void initColumnsModel(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.revisionDocs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.revisionLoop));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.revisionAcceptationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.revisionFeedback,
				new TaskReviewAndCorrectionFeedbackCellRenderer(getTranslator())));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues needRevisionsValues = new SelectionValues();
		needRevisionsValues.add(SelectionValues.entry(FILTER_NEED_REVISIONS, translate("filter.need.revisions")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.need.revisions"),
				FILTER_NEED_REVISIONS, needRevisionsValues, true));
		
		SelectionValues reviewedValues = new SelectionValues();
		reviewedValues.add(SelectionValues.entry(FILTER_REVISIONS_REVIEWED, translate("filter.revision.reviewed")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.revision.reviewed"),
				FILTER_REVISIONS_REVIEWED, reviewedValues, true));
		
		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.open.name(), translate(CoachedParticipantStatus.open.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.waiting.name(), translate(CoachedParticipantStatus.waiting.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.done.name(), translate(CoachedParticipantStatus.done.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.notAvailable.name(), translate(CoachedParticipantStatus.notAvailable.i18nKey())));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.step.status"),
				FILTER_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
	}

	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab waitingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WAITING_TAB_ID, translate(CoachedParticipantStatus.waiting.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.waiting.name()))));
		tabs.add(waitingTab);
		
		FlexiFiltersTab openTab = FlexiFiltersTabFactory.tabWithImplicitFilters(OPEN_TAB_ID, translate(CoachedParticipantStatus.open.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.open.name()))));
		tabs.add(openTab);
		
		FlexiFiltersTab needRevisionsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NEED_REVISIONS_TAB_ID, translate("filter.need.revisions"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_NEED_REVISIONS, List.of(FILTER_NEED_REVISIONS))));
		tabs.add(needRevisionsTab);
		
		FlexiFiltersTab reviewedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(REVISIONS_REVIEWED_TAB_ID, translate("filter.reviewed"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_REVISIONS_REVIEWED, List.of(FILTER_REVISIONS_REVIEWED))));
		tabs.add(reviewedTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(DONE_TAB_ID, translate(CoachedParticipantStatus.done.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.done.name()))));
		tabs.add(doneTab);
		
		FlexiFiltersTab notAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.notAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.notAvailable.name()))));
		tabs.add(notAvailableTab);
	}
	
	@Override
	protected void initBulkTools(FormItemContainer formLayout) {
		initBulkExtendTool(formLayout);
		super.initBulkTools(formLayout);
	}

	@Override
	protected void loadModel() {
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<TaskRevision> revisions = gtaManager.getLatestTaskRevisions(courseEntry, gtaNode, assessedIdentities);
		loadCachedRevisions = revisions.stream()
				.collect(Collectors.toMap(TaskRevision::getTask, rev -> rev, (u, v) -> u));
		
		super.loadModel();
	}

	@Override
	protected CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry) {
		identityRow.setToolsLink(forgeToolsLink(identityRow));
		
		Task task = identityRow.getTask();
		if(task != null) {
			identityRow.setLastRevision(loadCachedRevisions.get(task));
		}
		status(identityRow);
		return identityRow;
	}
	
	private void status(CoachedParticipantRow identityRow) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		Task assignedTask = identityRow.getTask();
		
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review) {
				identityRow.setStatus(CoachedParticipantStatus.notAvailable);
			} else if(assignedTask.getTaskStatus() == TaskProcess.correction) {
				identityRow.setStatus(CoachedParticipantStatus.open);
				identityRow.setRevisions(true);
			} else if(assignedTask.getTaskStatus() == TaskProcess.revision) {
				identityRow.setStatus(CoachedParticipantStatus.waiting);
				identityRow.setRevisions(true);
			} else if (assignedTask.getRevisionLoop() == 0) {
				identityRow.setStatus(CoachedParticipantStatus.done);
				identityRow.setRevisions(false);
			} else {
				identityRow.setStatus(CoachedParticipantStatus.done);
				identityRow.setRevisions(true);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
			identityRow.setStatus(CoachedParticipantStatus.open);
			identityRow.setRevisions(true);
		} else {
			identityRow.setStatus(CoachedParticipantStatus.done);
			identityRow.setRevisions(true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCloseRevisionProcessCtrl == source) {
			if((DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event))
					&& confirmCloseRevisionProcessCtrl.getUserObject() instanceof CoachedParticipantRow row) {
				doCloseRevisionProcess(row);
				loadModel();
			}
		} else if(confirmCollectCtrl == source) {
			if((DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event))
					&& confirmCollectCtrl.getUserObject() instanceof CoachedParticipantRow row) {
				doCollect(row);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(confirmReturnToRevisionsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doReturnToRevisions(confirmReturnToRevisionsCtrl.getTask(), confirmReturnToRevisionsCtrl.getAssessedIdentity());
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof DetailsToggleEvent toggleEvent) {
				CoachedParticipantRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenDetails(ureq, row);
				} else {
					doCloseDetails(row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenDetails(UserRequest ureq, CoachedParticipantRow row) {
		Identity assessedIdentity = row.getAssessedIdentity();
		Task assignedTask = row.getTask();
		List<TaskRevision> taskRevisions = gtaManager.getTaskRevisions(assignedTask);
		
		GTACoachRevisionAndCorrectionsController revisionDocumentsCtrl = new GTACoachRevisionAndCorrectionsController(ureq, getWindowControl(),
				courseEnv, assignedTask, taskRevisions, gtaNode, coachCourseEnv, null, assessedIdentity, taskListEventResource, mainForm);
		listenTo(revisionDocumentsCtrl);
		row.setDetailsCtrl(revisionDocumentsCtrl);
		
		ComponentWrapperElement wrapperEl = new ComponentWrapperElement(revisionDocumentsCtrl.getInitialComponent());
		wrapperEl.setRootForm(mainForm);
		flc.add(wrapperEl);
		row.setDetailsControllerEl(wrapperEl);
		
		SubmitDocumentsController uploadCorrectionCtrl = revisionDocumentsCtrl.getUploadCorrections();
		if(uploadCorrectionCtrl != null) {
			flc.add(uploadCorrectionCtrl.getInitialFormItem());
		}
	}

	private void doCloseDetails(CoachedParticipantRow row) {
		Controller detailsCtrl = row.getDetailsCtrl();
		if(detailsCtrl instanceof GTACoachRevisionAndCorrectionsController revisionsCtrl) {
			flc.remove(row.getDetailsControllerEl());
			
			SubmitDocumentsController uploadCorrectionCtrl = revisionsCtrl.getUploadCorrections();
			if(uploadCorrectionCtrl != null) {
				flc.remove(uploadCorrectionCtrl.getInitialFormItem());
			}
		}
		
		removeAsListenerAndDispose(detailsCtrl);
		row.setDetailsCtrl(null);
		row.setDetailsControllerEl(null);
	}

	private void doConfirmCloseRevisionProcess(UserRequest ureq, CoachedParticipantRow row) {
		String title = translate("coach.reviewed.confirm.title");
		String text = translate("coach.reviewed.confirm.text");
		confirmCloseRevisionProcessCtrl = activateOkCancelDialog(ureq, title, text, confirmCloseRevisionProcessCtrl);
		confirmCloseRevisionProcessCtrl.setUserObject(row);
		listenTo(confirmCloseRevisionProcessCtrl);
	}
	
	private void doCloseRevisionProcess(CoachedParticipantRow row) {
		Task assignedTask = row.getTask();
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		assignedTask = gtaManager.reviewedTask(assignedTask, gtaNode, getIdentity(), Role.coach);
		gtaManager.log("Revision", "close revision", assignedTask,
				getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
	}
	
	private void doConfirmReturnToRevisions(UserRequest ureq, CoachedParticipantRow row) {
		Task assignedTask  = row.getTask();
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		
		confirmReturnToRevisionsCtrl = new ConfirmRevisionsController(ureq, getWindowControl(), assignedTask,
				assessedIdentity, null, gtaNode, courseEnv);
		listenTo(confirmReturnToRevisionsCtrl);
		
		String title = translate("coach.revisions.confirm.title"); // same title as link button
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmReturnToRevisionsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReturnToRevisions(Task task, Identity assessedIdentity) {
		int currentIteration = task.getRevisionLoop();
		Task assignedTask = gtaManager.updateTask(task, TaskProcess.revision, currentIteration + 1, gtaNode, false, getIdentity(), Role.coach);
		gtaManager.log("Revision", "need another revision", assignedTask,
				getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
	}
	
	private void doConfirmCollect(UserRequest ureq, CoachedParticipantRow row) {
		Task assignedTask = row.getTask();
		Identity assessedIdentity = row.getAssessedIdentity();
		String toName = userManager.getUserDisplayName(assessedIdentity);			
		
		int iteration = assignedTask.getRevisionLoop();
		VFSContainer documentsContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, iteration, getIdentity());
		File documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, getIdentity());
		File[] submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		
		FilesLocked lockedBy = TaskHelper.getDocumentsLocked(documentsContainer, submittedDocuments);
		if(lockedBy != null) {
			showWarning("warning.submit.documents.edited", new String[]{ lockedBy.getLockedBy(), lockedBy.getLockedFiles() });
		} else {
			String title = translate("coach.collect.revisions.confirm.title");
			String text = translate("coach.collect.revisions.confirm.text", toName);
			text = "<div class='o_warning'>" + text + "</div>";
			confirmCollectCtrl = activateOkCancelDialog(ureq, title, text, confirmCollectCtrl);
			confirmCollectCtrl.setUserObject(row);
			listenTo(confirmCollectCtrl);
		}
	}
	
	private void doCollect(CoachedParticipantRow row) {
		Task assignedTask = row.getTask();
		Identity assessedIdentity = row.getAssessedIdentity();
		int iteration = assignedTask.getRevisionLoop();
		File documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, getIdentity());
		File[] submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		
		int numOfDocs = submittedDocuments == null ? 0 : submittedDocuments.length;
		assignedTask = gtaManager.collectRevisionTask(assignedTask, gtaNode, numOfDocs, getIdentity());
		
		gtaManager.log("Collect revision", "revision collected", assignedTask,
				getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);

		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		courseAssessmentService.incrementAttempts(gtaNode, assessedUserCourseEnv, Role.coach);
		
		TaskMultiUserEvent event = new TaskMultiUserEvent(TaskMultiUserEvent.SUBMIT_REVISION,
				getIdentity(), null, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, taskListEventResource);
	}

	@Override
	protected Controller createToolsController(UserRequest ureq, CoachedParticipantRow row) {
		return new RevisionToolsController(ureq, getWindowControl(), row);
	}
	
	private class RevisionToolsController extends BasicController {

		private Link collectLink;
		private Link dueDatesLink;
		private Link closeRevisionsLink;
		private Link returnToRevisionsLink;
		
		private CoachedParticipantRow row;
		
		public RevisionToolsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			dueDatesLink = LinkFactory.createLink("duedates", "duedates",
					getTranslator(), mainVC, this, Link.LINK);
			dueDatesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			
			TaskProcess status = row.getTaskStatus();
			if(status == TaskProcess.revision) {
				collectLink = LinkFactory.createLink("coach.collect.revisions", "coach.collect.revisions",
						getTranslator(), mainVC, this, Link.LINK);
			} else if(status == TaskProcess.correction) {
				returnToRevisionsLink = LinkFactory.createLink("coach.submit.corrections.to.revision.button", "coach.submit.corrections.to.revision.button",
						getTranslator(), mainVC, this, Link.LINK);
				returnToRevisionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_rejected");
				
				closeRevisionsLink = LinkFactory.createLink("coach.close.revision.button", "coach.close.revision.button",
						getTranslator(), mainVC, this, Link.LINK);
				closeRevisionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_accepted");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(dueDatesLink == source) {
				doEditDueDate(ureq, row);
			} else if(returnToRevisionsLink == source) {
				doConfirmReturnToRevisions(ureq, row);
			} else if(closeRevisionsLink == source) {
				doConfirmCloseRevisionProcess(ureq, row);
			} else if(collectLink == source) {
				doConfirmCollect(ureq, row);
			}
		}
	}
}
