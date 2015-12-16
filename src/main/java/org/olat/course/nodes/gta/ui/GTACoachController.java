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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskDefinitionList;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
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
public class GTACoachController extends GTAAbstractController {

	private Link reviewedButton, needRevisionsButton;

	private DirectoryController solutionsCtrl;
	private DirectoryController correctionsCtrl;
	private DirectoryController submittedDocCtrl;
	private GTAAssignedTaskController assignedTaskCtrl;
	private SubmitDocumentsController submitCorrectionsCtrl;
	private GTACoachedGroupGradingController groupGradingCtrl;
	private GTACoachedParticipantGradingController participantGradingCtrl;
	private GTACoachRevisionAndCorrectionsController revisionDocumentsCtrl;
	private DialogBoxController confirmRevisionsCtrl, confirmReviewDocumentCtrl;
	private ContactFormController emailController;
	private CloseableModalController cmc;
	private Link emailLink;
	
	
	@Autowired
	private UserManager userManager;
	
	public GTACoachController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			BusinessGroup assessedGroup, boolean withTitle, boolean withGrading, boolean withSubscription) {
		this(ureq, wControl, courseEnv, gtaNode, assessedGroup, null, withTitle, withGrading, withSubscription);
	}
	
	public GTACoachController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			Identity assessedIdentity, boolean withTitle, boolean withGrading, boolean withSubscription) {
		this(ureq, wControl, courseEnv, gtaNode, null, assessedIdentity, withTitle, withGrading, withSubscription);
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
			BusinessGroup assessedGroup, Identity assessedIdentity, boolean withTitle, boolean withGrading, boolean withSubscription) {
		super(ureq, wControl, gtaNode, courseEnv, null, assessedGroup, assessedIdentity, withTitle, withGrading, withSubscription);
	}

	@Override
	protected void initContainer(UserRequest ureq) {
		mainVC = createVelocityContainer("coach");
		
		reviewedButton = LinkFactory.createCustomLink("coach.reviewed.button", "reviewed", "coach.reviewed.button", Link.BUTTON, mainVC, this);
		reviewedButton.setElementCssClass("o_sel_course_gta_reviewed");
		reviewedButton.setIconLeftCSS("o_icon o_icon_accepted");
		reviewedButton.setPrimary(true);
		if(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			needRevisionsButton = LinkFactory.createCustomLink("coach.need.revision.button", "need-revision", "coach.need.revision.button", Link.BUTTON, mainVC, this);
			needRevisionsButton.setElementCssClass("o_sel_course_gta_need_revision");
			needRevisionsButton.setPrimary(true);
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
		
		//calculate state
		boolean viewSubmittedDocument = false;
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
				mainVC.contextPut("submitCssClass", "");
			} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
				mainVC.contextPut("submitCssClass", "o_active");
			} else {
				mainVC.contextPut("submitCssClass", "o_done");
				viewSubmittedDocument = true;
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			mainVC.contextPut("submitCssClass", "o_active");
		} else {
			mainVC.contextPut("submitCssClass", "o_done");
			viewSubmittedDocument = true;
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
		}
		
		return assignedTask;
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
				gtaNode, courseEnv, "coach.document");
		listenTo(submitCorrectionsCtrl);
		mainVC.put("corrections", submitCorrectionsCtrl.getInitialComponent());
		
		reviewedButton.setVisible(true);
		if(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			needRevisionsButton.setVisible(true);
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
			revisionDocumentsCtrl = new GTACoachRevisionAndCorrectionsController(ureq, getWindowControl(),
					courseEnv, assignedTask, gtaNode, assessedGroup, assessedIdentity);
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
			mainVC.contextPut("gradingCssClass", "o_active");
			setGrading(ureq);
		} else {
			mainVC.contextPut("gradingEnabled", Boolean.FALSE);
		}
		
		return assignedTask;
	}

	private void setGrading(UserRequest ureq) {
		mainVC.put("grading", new Panel("empty"));
		if(assessedGroup != null) {
			groupGradingCtrl = new GTACoachedGroupGradingController(ureq, getWindowControl(), courseEnv, gtaNode, assessedGroup);
			listenTo(groupGradingCtrl);
			mainVC.put("grading", groupGradingCtrl.getInitialComponent());
		} else if(assessedIdentity != null) {
			OLATResource courseOres = courseEntry.getOlatResource();
			participantGradingCtrl = new GTACoachedParticipantGradingController(ureq, getWindowControl(), courseOres, gtaNode, assessedIdentity);
			listenTo(participantGradingCtrl);
			mainVC.put("grading", participantGradingCtrl.getInitialComponent());
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
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(revisionDocumentsCtrl == source) {
			cleanUpProcess();
			process(ureq);
		} else if(participantGradingCtrl == source) {
			cleanUpProcess();
			process(ureq);
		} else if(groupGradingCtrl == source) {
			cleanUpProcess();
			process(ureq);
		} else if(submitCorrectionsCtrl == source) {
			if(event instanceof SubmitEvent) {
				Task assignedTask = submitCorrectionsCtrl.getAssignedTask();
				gtaManager.log("Corrections", (SubmitEvent)event, assignedTask, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
			}
		} else if(confirmReviewDocumentCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task assignedTask = (Task)confirmReviewDocumentCtrl.getUserObject();
				doReviewedDocument(ureq, assignedTask);
			}
		} else if(confirmRevisionsCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task assignedTask = (Task)confirmRevisionsCtrl.getUserObject();
				doRevisions(ureq, assignedTask);
			}
		} else if(source == cmc) {
			doCloseMailForm(false);
		} else if (source == emailController) {
			doCloseMailForm(true);
		}
		super.event(ureq, source, event);
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
		TaskProcess nextStep = gtaManager.nextStep(TaskProcess.correction, gtaNode);
		gtaManager.updateTask(task, nextStep, gtaNode);
		showInfo("coach.documents.successfully.reviewed");
		gtaManager.log("Review", "documents reviewed", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
		
		cleanUpProcess();
		process(ureq);
	}
	
	private void doConfirmRevisions(UserRequest ureq, Task task) {
		String title = translate("coach.revisions.confirm.title");
		String text = translate("coach.revisions.confirm.text");

		File documentsDir;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
		}

		boolean hasDocument = TaskHelper.hasDocuments(documentsDir);
		if(!hasDocument) {
			String warning = translate("coach.revisions.confirm.text.warn");
			text = "<div class='o_warning'>" + warning + "</div>" + text;
		}

		confirmRevisionsCtrl = activateOkCancelDialog(ureq, title, text, confirmRevisionsCtrl);	
		listenTo(confirmRevisionsCtrl);
		confirmRevisionsCtrl.setUserObject(task);
	}
	
	private void doRevisions(UserRequest ureq, Task task) {
		gtaManager.updateTask(task, TaskProcess.revision, 1, gtaNode);
		gtaManager.log("Review", "need revision", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
		
		cleanUpProcess();
		process(ureq);
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
		if (contactList != null && contactList.getEmailsAsStrings().size() > 0) {
			removeAsListenerAndDispose(emailController);
			
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			cmsg.addEmailTo(contactList);
			
			emailController = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
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
		TaskDefinitionList tasks = (TaskDefinitionList)config.get(GTACourseNode.GTASK_TASKS);
		List<TaskDefinition> availableTasks = new ArrayList<>(tasks.getTasks());
		for(TaskDefinition availableTask:availableTasks) {
			if(availableTask.getFilename() != null && availableTask.getFilename().equals(task.getTaskName())) {
				taskDef = availableTask;
				break;
			}
		}
		return taskDef;
	}

}
