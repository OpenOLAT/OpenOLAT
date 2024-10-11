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
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.ui.CoachSubmitCorrectionsController;
import org.olat.course.nodes.gta.ui.ConfirmRevisionsController;
import org.olat.course.nodes.gta.ui.DirectoryController;
import org.olat.course.nodes.gta.ui.GTAAbstractController;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.component.TaskReviewAndCorrectionFeedbackCellRenderer;
import org.olat.course.nodes.gta.ui.events.NeedRevisionEvent;
import org.olat.course.nodes.gta.ui.events.ReviewedEvent;
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
public class GTACoachReviewAndCorrectionListController extends AbstractCoachWorkflowListController implements FlexiTableComponentDelegate {

	private final VelocityContainer detailsVC;

	private DialogBoxController confirmReviewDocumentCtrl;
	private ConfirmRevisionsController confirmRevisionsCtrl;
	
	public GTACoachReviewAndCorrectionListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, List<Identity> assessableIdentities, GTACourseNode gtaNode) {
		super(ureq, wControl, "review_list", coachCourseEnv, assessableIdentities, gtaNode);

		detailsVC = createVelocityContainer("review_details");
		
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
			return participantRow.getStatus() == CoachedParticipantStatus.revisionAvailable
					|| participantRow.getStatus() == CoachedParticipantStatus.done;
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.reviewAndCorrectionAcceptationDate));		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.reviewAndCorrectionFeedback,
				new TaskReviewAndCorrectionFeedbackCellRenderer(getTranslator())));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues needRevisionsValues = new SelectionValues();
		needRevisionsValues.add(SelectionValues.entry(FILTER_NEED_REVISIONS, translate("filter.need.revisions")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.need.revisions"),
				FILTER_NEED_REVISIONS, needRevisionsValues, true));
		
		SelectionValues reviewedValues = new SelectionValues();
		reviewedValues.add(SelectionValues.entry(FILTER_REVIEWED, translate("filter.reviewed")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.reviewed"),
				FILTER_REVIEWED, reviewedValues, true));

		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.revisionAvailable.name(), translate(CoachedParticipantStatus.revisionAvailable.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.expired.name(), translate(CoachedParticipantStatus.expired.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.notAvailable.name(), translate(CoachedParticipantStatus.notAvailable.i18nKey())));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.step.status"),
				FILTER_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
	}

	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab needRevisionsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NEED_REVISIONS_TAB_ID, translate("filter.need.revisions"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_NEED_REVISIONS, List.of(FILTER_NEED_REVISIONS))));
		tabs.add(needRevisionsTab);
		
		FlexiFiltersTab reviewedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(REVIEWED_TAB_ID, translate("filter.reviewed"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_REVIEWED, List.of(FILTER_REVIEWED))));
		tabs.add(reviewedTab);
		
		FlexiFiltersTab revisionsAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(REVISION_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.revisionAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.revisionAvailable.name()))));
		tabs.add(revisionsAvailableTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(DONE_TAB_ID, translate(CoachedParticipantStatus.done.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.done.name()))));
		tabs.add(doneTab);
		
		FlexiFiltersTab notAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.notAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.notAvailable.name()))));
		tabs.add(notAvailableTab);
	}

	@Override
	protected CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry) {
		identityRow.setToolsLink(forgeToolsLink(identityRow));
		status(identityRow);
		return identityRow;
	}
	
	private void status(CoachedParticipantRow identityRow) {
		Task assignedTask = identityRow.getTask();
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		identityRow.setRevisions(false);
		
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit) {
				identityRow.setStatus(CoachedParticipantStatus.notAvailable);
			} else if(assignedTask.getTaskStatus() == TaskProcess.review) {
				identityRow.setStatus(CoachedParticipantStatus.revisionAvailable);
			} else if(assignedTask.getTaskStatus() == TaskProcess.revision) {
				identityRow.setRevisions(true);
				identityRow.setStatus(CoachedParticipantStatus.done);
			} else {
				identityRow.setStatus(CoachedParticipantStatus.done);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.review) {
			identityRow.setStatus(CoachedParticipantStatus.revisionAvailable);
		} else {
			identityRow.setStatus(CoachedParticipantStatus.done);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof CoachSubmitCorrectionsController submitCtrl && submitCtrl.getUserObject() instanceof CoachedParticipantRow row) {
			if(event instanceof NeedRevisionEvent) {
				doConfirmRevisions(ureq, row);
			} else if(event instanceof ReviewedEvent) {
				doConfirmReviewDocument(ureq, row);
			}
		} else if(source == confirmRevisionsCtrl) {
			if(event == Event.DONE_EVENT) {
				doRevisions(confirmRevisionsCtrl.getTask());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		}	else if(confirmReviewDocumentCtrl == source) {
			if((DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event))
					&& confirmReviewDocumentCtrl.getUserObject() instanceof CoachedParticipantRow row) {
				doReviewedDocument(row);
				loadModel();
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmRevisionsCtrl);
		confirmRevisionsCtrl = null;
		super.cleanUp();
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
		CoachedParticipantStatus status = row.getStatus();
		if(status == CoachedParticipantStatus.revisionAvailable) {
			doOpenUploadCorrections(ureq, row);
		} else if(status == CoachedParticipantStatus.done) {
			doOpenCorrections(ureq, row);
		}
	}
	
	private void doCloseDetails(CoachedParticipantRow row) {
		if(row.getDetailsCtrl() == null) return;
		
		removeAsListenerAndDispose(row.getDetailsCtrl());
		row.setDetailsCtrl(null);
		flc.remove(row.getDetailsControllerEl());
	}
	
	private void doOpenCorrections(UserRequest ureq, CoachedParticipantRow row) {
		Identity assessedIdentity = row.getAssessedIdentity();
		File documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
		VFSContainer documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);

		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		row.setCorrectionsDoneWithoutDocuments(!hasDocuments);
		if(hasDocuments) {
			DirectoryController correctionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"coach.corrections.description", "bulk.review", "review");
			listenTo(correctionsCtrl);
			row.setDetailsCtrl(correctionsCtrl);
			
			ComponentWrapperElement wrapperEl = new ComponentWrapperElement(correctionsCtrl.getInitialComponent());
			wrapperEl.setRootForm(mainForm);
			flc.add(wrapperEl);
			row.setDetailsControllerEl(wrapperEl);
		}
	}
	
	private void doOpenUploadCorrections(UserRequest ureq, CoachedParticipantRow row) {
		if(row.getDetailsCtrl() != null) {
			removeAsListenerAndDispose(row.getDetailsCtrl());
			
		}
		
		Task task = row.getTask();
		Identity assessedIdentity = row.getAssessedIdentity();
		File documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
		VFSContainer correctionsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);
		VFSContainer submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);

		List<TaskRevision> taskRevisions = gtaManager.getTaskRevisions(task);
		TaskRevision taskRevision = GTAAbstractController.getTaskRevision(taskRevisions, TaskProcess.review, 0);
		CoachSubmitCorrectionsController submitCorrectionsCtrl = new CoachSubmitCorrectionsController(ureq, getWindowControl(), task, taskRevision,
				assessedIdentity, null, documentsDir, correctionsContainer, gtaNode, courseEnv,
				coachCourseEnv.isCourseReadOnly(), null, "coach.document", submitContainer,
				translate("copy.ending.review"), "copy.submission", mainForm);
		submitCorrectionsCtrl.setUserObject(row);
		listenTo(submitCorrectionsCtrl);
		row.setDetailsCtrl(submitCorrectionsCtrl);
		flc.add(submitCorrectionsCtrl.getInitialFormItem());
		
		row.setDetailsControllerEl(submitCorrectionsCtrl.getInitialFormItem());	
	}
	
	private void doConfirmRevisions(UserRequest ureq, CoachedParticipantRow row) {
		confirmRevisionsCtrl = new ConfirmRevisionsController(ureq, getWindowControl(), row.getTask(),
				row.getAssessedIdentity(), null, gtaNode, courseEnv);
		listenTo(confirmRevisionsCtrl);
		
		String title = translate("coach.revisions.confirm.title"); // same title as link button
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRevisionsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRevisions(Task task) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(task.getIdentity().getKey());
		gtaManager.updateTask(task, TaskProcess.revision, 1, gtaNode, false, getIdentity(), Role.coach);
		gtaManager.log("Review", "need revision", task, getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
	}
	
	private void doConfirmReviewDocument(UserRequest ureq, CoachedParticipantRow row) {
		String title = translate("coach.reviewed.confirm.title");
		String text = translate("coach.reviewed.confirm.text");
		confirmReviewDocumentCtrl = activateOkCancelDialog(ureq, title, text, confirmReviewDocumentCtrl);	
		listenTo(confirmReviewDocumentCtrl);
		confirmReviewDocumentCtrl.setUserObject(row);
	}
	
	private void doReviewedDocument(CoachedParticipantRow row) {
		Task task = row.getTask();
		Identity assessedIdentity = row.getAssessedIdentity();
		
		//go to solution, grading or graded
		if(task == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			TaskList reloadedTaskList = gtaManager.getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
			task = gtaManager.createAndPersistTask(null, reloadedTaskList, firstStep, null, assessedIdentity, gtaNode);
		}
		
		gtaManager.reviewedTask(task, gtaNode, getIdentity(), Role.coach);
		showInfo("coach.documents.successfully.reviewed");
		gtaManager.log("Review", "documents reviewed", task, getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
	}

	@Override
	protected Controller createToolsController(UserRequest ureq, CoachedParticipantRow row) {
		return new ReviewAndCorrectionToolsController(ureq, getWindowControl(), row);
	}
	
	private class ReviewAndCorrectionToolsController extends BasicController {

		private Link needRevisionLink;
		private Link acceptSubmissionLink;
		
		private CoachedParticipantRow row;
		
		public ReviewAndCorrectionToolsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			needRevisionLink = LinkFactory.createLink("coach.need.revision.button", "coach.need.revision.button", getTranslator(), mainVC, this, Link.LINK);
			needRevisionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			
			acceptSubmissionLink = LinkFactory.createLink("coach.reviewed.button", "coach.reviewed.button", getTranslator(), mainVC, this, Link.LINK);
			acceptSubmissionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(needRevisionLink == source) {
				doConfirmRevisions(ureq, row);
			} else if(acceptSubmissionLink == source) {
				doConfirmReviewDocument(ureq, row);
			}
		}
	}
}
