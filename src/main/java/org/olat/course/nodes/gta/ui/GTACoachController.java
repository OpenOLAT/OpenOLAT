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
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.ui.tool.AssessmentFormCallback;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskHelper.FilesLocked;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.modules.co.ContactFormController;
import org.olat.resource.OLATResource;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachController extends GTAAbstractController implements AssessmentFormCallback, Activateable2 {

	private DirectoryController solutionsCtrl;
	private DirectoryController correctionsCtrl;
	private DirectoryController submittedDocCtrl;
	private GTAAssignedTaskController assignedTaskCtrl;
	private SubmitDocumentsController submitCorrectionsCtrl;
	private GTACoachedGroupGradingController groupGradingCtrl;
	private GTACoachedParticipantGradingController participantGradingCtrl;
	private GTACoachRevisionAndCorrectionsController revisionDocumentsCtrl;
	private ConfirmRevisionsController confirmRevisionsCtrl;
	private DialogBoxController confirmReviewDocumentCtrl, confirmCollectCtrl, confirmBackToSubmissionCtrl, confirmResetTaskCtrl;
	private ContactFormController emailController;
	private CloseableModalController cmc;

	private Link reviewedButton, needRevisionsButton, emailLink, collectSubmissionsLink, backToSubmissionLink, resetTaskButton;
	
	
	private final boolean isAdmin;
	private final boolean withReset;
	private final UserCourseEnvironment coachCourseEnv;
	
	@Autowired
	private UserManager userManager;
	
	public GTACoachController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			UserCourseEnvironment coachCourseEnv, BusinessGroup assessedGroup,
			boolean withTitle, boolean withGrading, boolean withSubscription, boolean withReset) {
		this(ureq, wControl, courseEnv, gtaNode, coachCourseEnv, assessedGroup, null,
				withTitle, withGrading, withSubscription, withReset);
	}
	
	public GTACoachController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			UserCourseEnvironment coachCourseEnv, Identity assessedIdentity,
			boolean withTitle, boolean withGrading, boolean withSubscription, boolean withReset) {
		this(ureq, wControl, courseEnv, gtaNode, coachCourseEnv, null, assessedIdentity,
				withTitle, withGrading, withSubscription, withReset);
	}

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseEnv
	 * @param gtaNode
	 * @param assessedGroup
	 * @param assessedIdentity
	 * @param withTitle Allow to remove the title in assessment tool
	 * @param withGrading Allow to remove the grading panel in assessment tool
	 */
	private GTACoachController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			UserCourseEnvironment coachCourseEnv, BusinessGroup assessedGroup, Identity assessedIdentity,
			boolean withTitle, boolean withGrading, boolean withSubscription, boolean withReset) {
		super(ureq, wControl, gtaNode, courseEnv, null, assessedGroup, assessedIdentity, withTitle, withGrading, withSubscription);
		this.coachCourseEnv = coachCourseEnv;
		this.withReset = withReset;
		isAdmin = coachCourseEnv.isAdmin();
		initContainer(ureq);
		process(ureq);
	}

	@Override
	protected void initContainer(UserRequest ureq) {
		mainVC = createVelocityContainer("coach");
		
		reviewedButton = LinkFactory.createCustomLink("coach.reviewed.button", "reviewed", "coach.reviewed.button", Link.BUTTON, mainVC, this);
		reviewedButton.setElementCssClass("o_sel_course_gta_reviewed");
		reviewedButton.setIconLeftCSS("o_icon o_icon_accepted");
		reviewedButton.setPrimary(true);
		reviewedButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		if(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			needRevisionsButton = LinkFactory.createCustomLink("coach.need.revision.button", "need-revision", "coach.need.revision.button", Link.BUTTON, mainVC, this);
			needRevisionsButton.setElementCssClass("o_sel_course_gta_need_revision");
			needRevisionsButton.setPrimary(true);
			needRevisionsButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			needRevisionsButton.setIconLeftCSS("o_icon o_icon_rejected");
		}
	
		if(withTitle) {
			if(assessedGroup != null) {
				mainVC.contextPut("groupName", assessedGroup.getName());
				emailLink = LinkFactory.createButtonXSmall("mailto.group", mainVC, this);
				emailLink.setIconLeftCSS("o_icon o_icon_mail");				
			} else if(assessedIdentity != null) {
				mainVC.contextPut("identityFullName", userManager.getUserDisplayName(assessedIdentity));
				Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), assessedIdentity, false, true, true, true);
				listenTo(dpc); // auto dispose, no need to keep local reference
				mainVC.put("image", dpc.getInitialComponent());
				emailLink = LinkFactory.createButtonXSmall("mailto.user", mainVC, this);
				emailLink.setIconLeftCSS("o_icon o_icon_mail");
			}
		}
		
		if(withReset) {
			resetTaskButton = LinkFactory.createCustomLink("coach.reset.button", "reset", "coach.reset.button", Link.BUTTON, mainVC, this);
			resetTaskButton.setElementCssClass("o_sel_course_gta_reset");
			resetTaskButton.setVisible(false);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected Task stepAssignment(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepAssignment(ureq, assignedTask);
		
		if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
			mainVC.contextPut("assignmentCssClass", "o_active");
		} else {
			mainVC.contextPut("assignmentCssClass", "o_done");
			
			TaskDefinition taskDef = getTaskDefinition(assignedTask);
			assignedTaskCtrl = new GTAAssignedTaskController(ureq, getWindowControl(), assignedTask,
					taskDef, courseEnv, gtaNode,
					"coach.task.assigned.description", "warning.no.task.choosed.coach", null);
			listenTo(assignedTaskCtrl);
			mainVC.put("assignedTask", assignedTaskCtrl.getInitialComponent());
		}

		return assignedTask;
	}
	
	@Override
	protected Task stepSubmit(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepSubmit(ureq, assignedTask);
		if(collectSubmissionsLink != null) {
			mainVC.remove(collectSubmissionsLink);//clean up
		}
		if(backToSubmissionLink != null) {
			mainVC.remove(backToSubmissionLink);
		}
		
		//calculate state
		boolean viewSubmittedDocument = false;
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
				mainVC.contextPut("submitCssClass", "");
			} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
				mainVC.contextPut("submitCssClass", "o_active");
				collect(assignedTask);
			} else {
				mainVC.contextPut("submitCssClass", "o_done");
				viewSubmittedDocument = true;
			}	
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			mainVC.contextPut("submitCssClass", "o_active");
			collect(assignedTask);
		} else {
			mainVC.contextPut("submitCssClass", "o_done");
			viewSubmittedDocument = true;
		}
		if (assignedTask == null || (assignedTask.getTaskStatus() != TaskProcess.submit)) {
			backToSubmission(assignedTask);
		}
		
		if(viewSubmittedDocument) {
			File documentsDir;
			VFSContainer documentsContainer = null;
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
				documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
			} else {
				documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
				documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);
			}
			boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
			if(hasDocuments) {
				submittedDocCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
						"coach.submitted.documents.description", "bulk.submitted.documents", "submission");
				listenTo(submittedDocCtrl);
				mainVC.put("submittedDocs", submittedDocCtrl.getInitialComponent());
			}  else {
				TextFactory.createTextComponentFromI18nKey("submittedDocs", "coach.submitted.nofiles", getTranslator(), null, true, mainVC);			
			}
		} else {
			File documentsDir;
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			} else {
				documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
			}
			boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
			mainVC.contextPut("hasUploadedDocs", hasDocuments);
		}
		return assignedTask;
	}
	
	private void backToSubmission(Task assignedTask) {
		if(config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT) && assignedTask != null) {
			Date now = new Date();
			DueDate dueDate = getSubmissionDueDate(assignedTask);
			if(!coachCourseEnv.isCourseReadOnly() && (dueDate == null || dueDate.getDueDate() == null || now.before(dueDate.getDueDate()))) {
				backToSubmissionLink = LinkFactory.createButton("coach.back.to.submission", mainVC, this);
				backToSubmissionLink.setUserObject(assignedTask);
			}
		}
	}
	
	private void collect(Task assignedTask) {
		if(!coachCourseEnv.isCourseReadOnly()) {
			collectSubmissionsLink = LinkFactory.createButton("coach.collect.task", mainVC, this);
			collectSubmissionsLink.setUserObject(assignedTask);
		}
	}
	
	@Override
	protected Task stepReviewAndCorrection(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepReviewAndCorrection(ureq, assignedTask);
		
		reviewedButton.setVisible(false);
		if(needRevisionsButton != null) {
			needRevisionsButton.setVisible(false);
		}
		
		mainVC.contextPut("review", Boolean.FALSE);
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit) {
				mainVC.contextPut("reviewCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.review) {
				mainVC.contextPut("reviewCssClass", "o_active");
				setUploadCorrections(ureq, assignedTask);
			} else {
				mainVC.contextPut("reviewCssClass", "o_done");
				setCorrections(ureq, (assignedTask.getRevisionLoop() > 0));
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.review) {
			mainVC.contextPut("reviewCssClass", "o_active");
			setUploadCorrections(ureq, assignedTask);
		} else {
			mainVC.contextPut("reviewCssClass", "o_done");
			setCorrections(ureq, false);
		}
		
		return assignedTask;
	}
	
	private void setUploadCorrections(UserRequest ureq, Task task) {
		File documentsDir;
		VFSContainer documentsContainer;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);
		}
		
		submitCorrectionsCtrl = new SubmitDocumentsController(ureq, getWindowControl(), task, documentsDir, documentsContainer, -1,
				gtaNode, courseEnv, coachCourseEnv.isCourseReadOnly(), null, "coach.document");
		listenTo(submitCorrectionsCtrl);
		mainVC.put("corrections", submitCorrectionsCtrl.getInitialComponent());
		
		reviewedButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		if(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			needRevisionsButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		}
	}
	
	private void setCorrections(UserRequest ureq, boolean hasRevisions) {
		File documentsDir;
		VFSContainer documentsContainer;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);
		}
		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		if(hasDocuments) {
			correctionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"coach.corrections.description", "bulk.review", "review");
			listenTo(correctionsCtrl);
			mainVC.put("corrections", correctionsCtrl.getInitialComponent());			
		} else if (hasRevisions) {
			String msg = "<i class='o_icon o_icon_warn'> </i> " + translate("coach.corrections.rejected");
			TextFactory.createTextComponentFromString("corrections", msg, null, true, mainVC);			
		} else {
			String msg = "<i class='o_icon o_icon_ok'> </i> " + translate("coach.corrections.closed");
			TextFactory.createTextComponentFromString("corrections", msg, null, true, mainVC);			
		}
	}
	
	@Override
	protected Task stepRevision(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepRevision(ureq, assignedTask);
		
		boolean revisions = false;
		
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review) {
				mainVC.contextPut("revisionCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
				mainVC.contextPut("revisionCssClass", "o_active");
				revisions = true;
			} else if (assignedTask.getRevisionLoop() == 0) {
				mainVC.contextPut("skipRevisions", Boolean.TRUE);
				revisions = false;
			} else {
				mainVC.contextPut("revisionCssClass", "o_done");
				revisions = true;
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
			mainVC.contextPut("revisionCssClass", "o_active");
			revisions = true;
		} else {
			mainVC.contextPut("revisionCssClass", "o_done");
			revisions = true;
		}
		
		if(revisions) {
			if(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				UserCourseEnvironment assessedUserCourseEnv = getAssessedUserCourseEnvironment();
				revisionDocumentsCtrl = new GTACoachRevisionAndCorrectionsController(ureq, getWindowControl(),
					courseEnv, assignedTask, gtaNode, coachCourseEnv, assessedGroup, assessedIdentity, assessedUserCourseEnv, taskListEventResource);
			} else {
				revisionDocumentsCtrl = new GTACoachRevisionAndCorrectionsController(ureq, getWindowControl(),
					courseEnv, assignedTask, gtaNode, coachCourseEnv, assessedGroup, null, null, taskListEventResource);
			}
			listenTo(revisionDocumentsCtrl);
			mainVC.put("revisionDocs", revisionDocumentsCtrl.getInitialComponent());
		}
		
		return assignedTask;
	}
	
	@Override
	protected Task stepSolution(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepSolution(ureq, assignedTask);

		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review || assignedTask.getTaskStatus() == TaskProcess.correction
					|| assignedTask.getTaskStatus() == TaskProcess.revision) {
				mainVC.contextPut("solutionCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.solution) {
				mainVC.contextPut("solutionCssClass", "o_active");
			} else {
				mainVC.contextPut("solutionCssClass", "o_done");
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.solution) {
			mainVC.contextPut("solutionCssClass", "o_active");
		} else {
			mainVC.contextPut("solutionCssClass", "o_done");
		}
		
		File documentsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		VFSContainer documentsContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
		solutionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer, "run.solutions.description", "bulk.solutions", "solutions");
		listenTo(solutionsCtrl);
		mainVC.put("solutions", solutionsCtrl.getInitialComponent());
		
		return assignedTask;
	}
	
	@Override
	protected Task stepGrading(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepGrading(ureq, assignedTask);
		if(withGrading) {
			if(assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.graded) {
				mainVC.contextPut("gradingCssClass", "o_done");
			} else {
				mainVC.contextPut("gradingCssClass", "o_active");
			}
			setGrading(ureq, assignedTask);
		} else {
			mainVC.contextPut("gradingEnabled", Boolean.FALSE);
		}
		
		return assignedTask;
	}

	private void setGrading(UserRequest ureq, Task assignedTask) {
		mainVC.put("grading", new Panel("empty"));
		if(assessedGroup != null) {
			groupGradingCtrl = new GTACoachedGroupGradingController(ureq, getWindowControl(),
					coachCourseEnv, courseEnv, gtaNode, assessedGroup, taskList, assignedTask);
			listenTo(groupGradingCtrl);
			mainVC.put("grading", groupGradingCtrl.getInitialComponent());
		} else if(assessedIdentity != null) {
			OLATResource courseOres = courseEntry.getOlatResource();
			participantGradingCtrl = new GTACoachedParticipantGradingController(ureq, getWindowControl(),
					courseOres, gtaNode, assignedTask, coachCourseEnv, assessedIdentity);
			listenTo(participantGradingCtrl);
			mainVC.put("grading", participantGradingCtrl.getInitialComponent());
		}
	}
	
	@Override
	protected void resetTask(UserRequest ureq, Task task) {
		if(resetTaskButton != null) {
			resetTaskButton.setUserObject(task);
			boolean allowed = isAdmin && task != null
					&& (StringHelper.containsNonWhitespace(task.getTaskName()) || (task.getTaskStatus() == TaskProcess.submit && !StringHelper.containsNonWhitespace(task.getTaskName())))
					&& (task.getTaskStatus() == TaskProcess.assignment || task.getTaskStatus() == TaskProcess.submit)
					&& GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL.equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE));
			resetTaskButton.setVisible(allowed);
		}
	}

	@Override
	protected void processEvent(TaskMultiUserEvent event) {
		//
	}

	@Override
	public void assessmentDone(UserRequest ureq) {
		Task task;
		if(businessGroupTask) {
			task = gtaManager.getTask(assessedGroup, taskList);
		} else {
			task = gtaManager.getTask(assessedIdentity, taskList);
		}
		if(task != null) {
			task = gtaManager.updateTask(task, TaskProcess.graded, gtaNode, Role.coach);
			cleanUpProcess();
			process(ureq);
		}
	}

	@Override
	public void assessmentReopen(UserRequest ureq) {
		Task task;
		if(businessGroupTask) {
			task = gtaManager.getTask(assessedGroup, taskList);
		} else {
			task = gtaManager.getTask(assessedIdentity, taskList);
		}
		if(task != null && task.getTaskStatus() == TaskProcess.graded) {
			task = gtaManager.updateTask(task, TaskProcess.grading, gtaNode, Role.coach);
			cleanUpProcess();
			process(ureq);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.size() <= 1) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Submit".equalsIgnoreCase(type)) {
			if(submittedDocCtrl != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				submittedDocCtrl.activate(ureq, subEntries, null);
			}	
		} else if("Revision".equalsIgnoreCase(type)) {
			if(revisionDocumentsCtrl != null) {
				revisionDocumentsCtrl.activate(ureq, entries, null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(reviewedButton == source) {
			if(submitCorrectionsCtrl != null) {
				Task assignedTask = submitCorrectionsCtrl.getAssignedTask();
				doConfirmReviewDocument(ureq, assignedTask);
			}
		} else if(needRevisionsButton == source) {
			if(submitCorrectionsCtrl != null) {
				Task assignedTask = submitCorrectionsCtrl.getAssignedTask();
				doConfirmRevisions(ureq, assignedTask);
			}
		} else if (emailLink == source) {
			doOpenMailForm(ureq);
		} else if(collectSubmissionsLink == source) {
			doConfirmCollectTask(ureq, (Task)collectSubmissionsLink.getUserObject());
		} else if(backToSubmissionLink == source) {
			doConfirmBackToSubmission(ureq, (Task)backToSubmissionLink.getUserObject());
		} else if(resetTaskButton == source) {
			doConfirmResetTask(ureq, (Task)resetTaskButton.getUserObject());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(revisionDocumentsCtrl == source
				|| participantGradingCtrl == source
				|| groupGradingCtrl == source) {
			cleanUpProcess();
			process(ureq);
		} else if(submitCorrectionsCtrl == source) {
			if(event instanceof SubmitEvent) {
				Task assignedTask = submitCorrectionsCtrl.getAssignedTask();
				gtaManager.log("Corrections", (SubmitEvent)event, assignedTask,
						getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
			}
		} else if(confirmReviewDocumentCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task assignedTask = (Task)confirmReviewDocumentCtrl.getUserObject();
				doReviewedDocument(ureq, assignedTask);
			}
		} else if(confirmRevisionsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doRevisions(ureq, confirmRevisionsCtrl.getTask());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCollectCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task assignedTask = (Task)confirmCollectCtrl.getUserObject();
				doCollectTask(ureq, assignedTask);
			}
		} else if(confirmBackToSubmissionCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task assignedTask = (Task)confirmBackToSubmissionCtrl.getUserObject();
				doBackToSubmission(ureq, assignedTask);
			}
		}  else if(confirmResetTaskCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task assignedTask = (Task)confirmResetTaskCtrl.getUserObject();
				doAllowResetTask(ureq, assignedTask);
			}
		} else if(source == cmc) {
			doCloseMailForm(false);
		} else if (source == emailController) {
			doCloseMailForm(true);
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRevisionsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRevisionsCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void cleanUpProcess() {
		if(solutionsCtrl != null) {
			mainVC.remove(solutionsCtrl.getInitialComponent());
		}
		if(correctionsCtrl != null) {
			mainVC.remove(correctionsCtrl.getInitialComponent());
		}
		if(submittedDocCtrl != null) {
			mainVC.remove(submittedDocCtrl.getInitialComponent());
		}
		if(assignedTaskCtrl != null) {
			mainVC.remove(assignedTaskCtrl.getInitialComponent());
		}
		if(submitCorrectionsCtrl != null) {
			mainVC.remove(submitCorrectionsCtrl.getInitialComponent());
		}
		if(revisionDocumentsCtrl != null) {
			mainVC.remove(revisionDocumentsCtrl.getInitialComponent());
		}
		removeAsListenerAndDispose(solutionsCtrl);
		removeAsListenerAndDispose(correctionsCtrl);
		removeAsListenerAndDispose(submittedDocCtrl);
		removeAsListenerAndDispose(assignedTaskCtrl);
		removeAsListenerAndDispose(submitCorrectionsCtrl);
		removeAsListenerAndDispose(revisionDocumentsCtrl);
		solutionsCtrl = null;
		correctionsCtrl = null;
		submittedDocCtrl = null;
		assignedTaskCtrl = null;
		submitCorrectionsCtrl = null;
		revisionDocumentsCtrl = null;
	}
	
	private void doConfirmReviewDocument(UserRequest ureq, Task task) {
		String title = translate("coach.reviewed.confirm.title");
		String text = translate("coach.reviewed.confirm.text");
		confirmReviewDocumentCtrl = activateOkCancelDialog(ureq, title, text, confirmReviewDocumentCtrl);	
		listenTo(confirmReviewDocumentCtrl);
		confirmReviewDocumentCtrl.setUserObject(task);
	}
	
	private void doReviewedDocument(UserRequest ureq, Task task) {
		//go to solution, grading or graded
		if(task == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			TaskList reloadedTaskList = gtaManager.getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
			task = gtaManager.createAndPersistTask(null, reloadedTaskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
		}
		
		gtaManager.reviewedTask(task, gtaNode, Role.coach);
		showInfo("coach.documents.successfully.reviewed");
		gtaManager.log("Review", "documents reviewed", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
		
		cleanUpProcess();
		process(ureq);
	}
	
	private void doConfirmRevisions(UserRequest ureq, Task task) {
		confirmRevisionsCtrl = new ConfirmRevisionsController(ureq, getWindowControl(), task,
				assessedIdentity, assessedGroup, gtaNode, courseEnv);
		listenTo(confirmRevisionsCtrl);
		
		String title = translate("coach.revisions.confirm.title"); // same title as link button
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRevisionsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRevisions(UserRequest ureq, Task task) {
		gtaManager.updateTask(task, TaskProcess.revision, 1, gtaNode, Role.coach);
		gtaManager.log("Review", "need revision", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
		
		cleanUpProcess();
		process(ureq);
	}
	
	private void doConfirmCollectTask(UserRequest ureq, Task assignedTask) {
		String toName = null;
		if (assessedGroup != null) {
			toName = assessedGroup.getName();
		} else if (assessedIdentity != null) {
			toName = userManager.getUserDisplayName(assessedIdentity);			
		}

		File[] submittedDocuments;
		VFSContainer documentsContainer;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		} else {
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, getIdentity());
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, getIdentity());
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		}

		FilesLocked lockedBy = TaskHelper.getDocumentsLocked(documentsContainer, submittedDocuments);
		if(lockedBy != null) {
			showWarning("warning.submit.documents.edited", new String[]{ lockedBy.getLockedBy(), lockedBy.getLockedFiles() });
		} else {
			String title = translate("coach.collect.confirm.title");
			String text = translate("coach.collect.confirm.text", new String[]{ toName });
			text = "<div class='o_warning'>" + text + "</div>";
			confirmCollectCtrl = activateOkCancelDialog(ureq, title, text, confirmCollectCtrl);
			confirmCollectCtrl.setUserObject(assignedTask);
			listenTo(confirmCollectCtrl);
		}
	}
	
	private void doCollectTask(UserRequest ureq, Task task) {
		File[] submittedDocuments;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		} else {
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		}
		
		if(task == null && gtaNode.isOptional()) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createTask(null, taskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
		}
		
		int numOfDocs = submittedDocuments == null ? 0 : submittedDocuments.length;
		task = gtaManager.collectTask(task, gtaNode, numOfDocs);
		showInfo("run.documents.successfully.submitted");
		
		TaskMultiUserEvent event = new TaskMultiUserEvent(TaskMultiUserEvent.SUMBIT_TASK,
				assessedIdentity, assessedGroup, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, taskListEventResource);
		
		gtaManager.log("Collect", "collect documents", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
		
		cleanUpProcess();
		process(ureq);
		doUpdateAttempts();
	}
	
	private void doConfirmBackToSubmission(UserRequest ureq, Task assignedTask) {
		String toName = null;
		if (assessedGroup != null) {
			toName = assessedGroup.getName();
		} else if (assessedIdentity != null) {
			toName = userManager.getUserDisplayName(assessedIdentity);			
		}
		
		String title = translate("coach.back.to.submission.confirm.title");
		String text = translate("coach.back.to.submission.confirm.text", new String[]{ toName });
		text = "<div class='o_warning'>" + text + "</div>";
		confirmBackToSubmissionCtrl = activateOkCancelDialog(ureq, title, text, confirmBackToSubmissionCtrl);
		confirmBackToSubmissionCtrl.setUserObject(assignedTask);
		listenTo(confirmBackToSubmissionCtrl);
	}
	
	private void doBackToSubmission(UserRequest ureq, Task task) {
		TaskProcess submit = gtaManager.previousStep(TaskProcess.review, gtaNode);//only submit allowed
		if(submit == TaskProcess.submit) {
			task = gtaManager.updateTask(task, submit, gtaNode, Role.coach);
			
			gtaManager.log("Back to submission", "revert status of task back to submission", task,
					getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
			
			cleanUpProcess();
			process(ureq);
		}
	}
	
	private void doConfirmResetTask(UserRequest ureq, Task assignedTask) {
		String toName = null;
		if (assessedGroup != null) {
			toName = assessedGroup.getName();
		} else if (assessedIdentity != null) {
			toName = userManager.getUserDisplayName(assessedIdentity);			
		}
		
		String title = translate("coach.reset.task.confirm.title");
		String text = translate("coach.reset.task.confirm.text", new String[]{ toName });
		confirmResetTaskCtrl = activateOkCancelDialog(ureq, title, text, confirmResetTaskCtrl);
		confirmResetTaskCtrl.setUserObject(assignedTask);
		listenTo(confirmResetTaskCtrl);
	}
	
	private void doAllowResetTask(UserRequest ureq, Task assignedTask) {
		gtaManager.allowResetTask(assignedTask, getIdentity(), gtaNode);
		gtaManager.log("Allow reset task", "Allow the user to reset the task", assignedTask,
				getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
		cleanUpProcess();
		process(ureq);
		showInfo("info.task.reset.allowed", (String)null);
	}
	
	private void doOpenMailForm(UserRequest ureq) {
		// build recipient list
		ContactList contactList = null;
		if (assessedGroup != null) {
			String toName = assessedGroup.getName();
			contactList = new ContactList(toName);
			List<Identity> memberList = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
			contactList.addAllIdentites(memberList);
			
		} else if (assessedIdentity != null) {
			String toName = userManager.getUserDisplayName(assessedIdentity);
			contactList = new ContactList(toName);
			contactList.add(assessedIdentity);			
		}
		// open dialog with mail form
		if (contactList != null && !contactList.getEmailsAsStrings().isEmpty()) {
			removeAsListenerAndDispose(emailController);
			
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			cmsg.addEmailTo(contactList);
			
			emailController = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg, null);
			listenTo(emailController);
			
			removeAsListenerAndDispose(cmc);
			String title = translate(emailLink.getI18n()); // same title as link button
			cmc = new CloseableModalController(getWindowControl(), translate("close"), emailController.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doCloseMailForm(boolean closeDialog) {
		if (closeDialog) {
			cmc.deactivate();
		}
		removeAsListenerAndDispose(emailController);
		removeAsListenerAndDispose(cmc);
		emailController = null;
		cmc = null;
	}
	
	private TaskDefinition getTaskDefinition(Task task) {
		if(task == null) return null;
		
		TaskDefinition taskDef = null;
		List<TaskDefinition> availableTasks = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		for(TaskDefinition availableTask:availableTasks) {
			if(availableTask.getFilename() != null && availableTask.getFilename().equals(task.getTaskName())) {
				taskDef = availableTask;
				break;
			}
		}
		return taskDef;
	}
	
	@Override
	protected Role getDoer() {
		return Role.coach;
	}
}
