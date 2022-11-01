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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.AccessSearchParams;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.ui.tool.AssessedIdentityLargeInfosController;
import org.olat.course.assessment.ui.tool.AssessmentFormCallback;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskHelper.FilesLocked;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.events.NeedRevisionEvent;
import org.olat.course.nodes.gta.ui.events.ReviewedEvent;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.modules.co.ContactFormController;
import org.olat.resource.OLATResource;
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
	private DialogBoxController confirmReviewDocumentCtrl;
	private DialogBoxController confirmCollectCtrl;
	private DialogBoxController confirmBackToSubmissionCtrl;
	private DialogBoxController confirmResetTaskCtrl;
	private ContactFormController emailController;
	private CloseableModalController cmc;

	private Link emailLink;
	private Link resetTaskButton;
	private Link backToSubmissionLink;
	private Link collectSubmissionsLink;
	
	private final boolean isAdmin;
	private final boolean withReset;
	private final UserCourseEnvironment coachCourseEnv;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DocEditorService docEditorService;
	
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

		if(withTitle) {
			if(assessedGroup != null) {
				mainVC.contextPut("groupName", assessedGroup.getName());
				emailLink = LinkFactory.createButtonXSmall("mailto.group", mainVC, this);
				emailLink.setIconLeftCSS("o_icon o_icon_mail");				
			} else if(assessedIdentity != null) {
				AssessedIdentityLargeInfosController userInfosCtrl = new AssessedIdentityLargeInfosController(ureq, getWindowControl(),
						assessedIdentity, coachCourseEnv.getCourseEnvironment(), gtaNode);
				listenTo(userInfosCtrl);
				mainVC.put("userInfos", userInfosCtrl.getInitialComponent());
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
			setWaitingStatusAndCssClass("assignment");
			
			//assignment open?
			DueDate dueDate = getAssignementDueDate(assignedTask);
			if(dueDate != null && dueDate.getDueDate() != null && dueDate.getDueDate().compareTo(new Date()) < 0) {
				//assignment is closed
				mainVC.contextPut("assignmentClosed", Boolean.TRUE);
				boolean hasAssignment = assignedTask != null && StringHelper.containsNonWhitespace(assignedTask.getTaskName());
				mainVC.contextPut("assignmentClosedWithAssignment", Boolean.valueOf(hasAssignment));
				if(!hasAssignment) {
					setExpiredStatusAndCssClass("assignment");
				}
			}
		} else {
			setDoneStatusAndCssClass("assignment");

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
				setNotAvailableStatusAndCssClass("submit");
			} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
				if(isSubmissionLate(ureq, getSubmissionDueDate(assignedTask), getLateSubmissionDueDate(assignedTask))) {
					setLateStatusAndCssClass("submit");
					mainVC.contextPut("submitLate", Boolean.TRUE);
				} else {
					setWaitingStatusAndCssClass("submit");
				}
				collect(assignedTask);
			} else {
				setDoneStatusAndCssClass("submit");
				viewSubmittedDocument = true;
			}	
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			if(isSubmissionLate(ureq, getSubmissionDueDate(assignedTask), getLateSubmissionDueDate(assignedTask))) {
				setLateStatusAndCssClass("submit");
				mainVC.contextPut("submitLate", Boolean.TRUE);
			} else {
				setActiveStatusAndCssClass("submit");
			}
			collect(assignedTask);
		} else {
			setDoneStatusAndCssClass("submit");
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
			VFSContainer submitContainer;
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
				submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
			} else {
				documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
				submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);
			}
			boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
			mainVC.contextPut("hasUploadedDocs", hasDocuments);
			
			List<VFSMetadata> metadatas = submitContainer.getItems().stream()
					.filter(VFSLeaf.class::isInstance)
					.map(item -> ((VFSLeaf)item).getMetaInfo())
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			AccessSearchParams params = new AccessSearchParams();
			params.setMode(Mode.EDIT);
			params.setMetadatas(metadatas);
			List<Access> accesses = docEditorService.getAccesses(params);
			mainVC.contextPut("docsEdit", !accesses.isEmpty());
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
	protected Task stepReviewAndCorrection(UserRequest ureq, Task assignedTask, List<TaskRevision> taskRevisions) {
		assignedTask = super.stepReviewAndCorrection(ureq, assignedTask, taskRevisions);
		
		mainVC.contextPut("review", Boolean.FALSE);
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit) {
				setNotAvailableStatusAndCssClass("review");
			} else if(assignedTask.getTaskStatus() == TaskProcess.review) {
				setActiveStatusAndCssClass("review");
				setUploadCorrections(ureq, assignedTask, taskRevisions);
			} else {
				setDoneStatusAndCssClass("review");
				setCorrections(ureq, (assignedTask.getRevisionLoop() > 0), taskRevisions);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.review) {
			setActiveStatusAndCssClass("review");
			setUploadCorrections(ureq, assignedTask, taskRevisions);
		} else {
			setDoneStatusAndCssClass("review");
			setCorrections(ureq, false, taskRevisions);
		}
		return assignedTask;
	}
	
	private void setUploadCorrections(UserRequest ureq, Task task, List<TaskRevision> taskRevisions) {
		File documentsDir;
		VFSContainer correctionsContainer;
		VFSContainer submitContainer;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedGroup);
			correctionsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedGroup);
			submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
			correctionsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);
			submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);
		}
		
		TaskRevision taskRevision = getTaskRevision(taskRevisions, TaskProcess.review, 0);
		submitCorrectionsCtrl = new CoachSubmitCorrectionsController(ureq, getWindowControl(), task, taskRevision,
				assessedIdentity, assessedGroup, documentsDir, correctionsContainer, gtaNode, courseEnv,
				coachCourseEnv.isCourseReadOnly(), null, "coach.document", submitContainer,
				translate("copy.ending.review"), "copy.submission");
		listenTo(submitCorrectionsCtrl);
		mainVC.put("corrections", submitCorrectionsCtrl.getInitialComponent());	
	}
	
	private void setCorrections(UserRequest ureq, boolean hasRevisions, List<TaskRevision> taskRevisions) {
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
		
		TaskRevision taskRevision = getTaskRevision(taskRevisions, TaskProcess.correction, 0);
		if(taskRevision != null && StringHelper.containsNonWhitespace(taskRevision.getComment())) {
			String commentator = userManager.getUserDisplayName(taskRevision.getCommentAuthor());
			String commentDate = Formatter.getInstance(getLocale()).formatDate(taskRevision.getCommentLastModified());
			String infos = translate("run.corrections.comment.infos", commentDate, commentator);
			mainVC.contextPut("correctionMessage", taskRevision.getComment());
			mainVC.contextPut("correctionMessageInfos", infos);
		}
	}
	
	@Override
	protected Task stepRevision(UserRequest ureq, Task assignedTask, List<TaskRevision> taskRevisions) {
		assignedTask = super.stepRevision(ureq, assignedTask, taskRevisions);
		
		boolean revisions = false;
		
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review) {
				setNotAvailableStatusAndCssClass("revision");
			} else if(assignedTask.getTaskStatus() == TaskProcess.correction) {
				setActiveStatusAndCssClass("revision");
				revisions = true;
			} else if(assignedTask.getTaskStatus() == TaskProcess.revision) {
				setWaitingStatusAndCssClass("revision");
				revisions = true;
			} else if (assignedTask.getRevisionLoop() == 0) {
				mainVC.contextPut("skipRevisions", Boolean.TRUE);
				revisions = false;
			} else {
				setDoneStatusAndCssClass("revision");
				revisions = true;
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
			setActiveStatusAndCssClass("revision");
			revisions = true;
		} else {
			setDoneStatusAndCssClass("revision");
			revisions = true;
		}
		
		if(revisions) {
			if(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				revisionDocumentsCtrl = new GTACoachRevisionAndCorrectionsController(ureq, getWindowControl(),
					courseEnv, assignedTask, taskRevisions, gtaNode, coachCourseEnv, null, assessedIdentity, taskListEventResource);
			} else {
				revisionDocumentsCtrl = new GTACoachRevisionAndCorrectionsController(ureq, getWindowControl(),
					courseEnv, assignedTask, taskRevisions, gtaNode, coachCourseEnv, assessedGroup, null, taskListEventResource);
			}
			listenTo(revisionDocumentsCtrl);
			mainVC.put("revisionDocs", revisionDocumentsCtrl.getInitialComponent());
		}
		
		return assignedTask;
	}
	
	@Override
	protected Task stepSolution(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepSolution(ureq, assignedTask);
		
		DueDate availableDate = getSolutionDueDate(assignedTask);
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review || assignedTask.getTaskStatus() == TaskProcess.correction
					|| assignedTask.getTaskStatus() == TaskProcess.revision) {
				setNotAvailableStatusAndCssClass("solution");
			} else if(assignedTask.getTaskStatus() == TaskProcess.solution) {
				if(isSolutionVisible(ureq, assignedTask) && showSolutions(availableDate)) {
					setActiveStatusAndCssClass("solution", "msg.status.available");
				} else {
					setNotAvailableStatusAndCssClass("solution");
				}
			} else {
				setDoneStatusAndCssClass("solution", "msg.status.available");
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.solution) {
			if(isSolutionVisible(ureq, assignedTask) && showSolutions(availableDate)) {
				setActiveStatusAndCssClass("solution", "msg.status.available");
			} else {
				setNotAvailableStatusAndCssClass("solution");
			}
		} else {
			setDoneStatusAndCssClass("solution", "msg.status.available");
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
				setDoneStatusAndCssClass("grading");
			} else {
				setActiveStatusAndCssClass("grading");
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
			gtaManager.updateTask(task, TaskProcess.graded, gtaNode, false, getIdentity(), Role.coach);
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
			gtaManager.updateTask(task, TaskProcess.grading, gtaNode, false, getIdentity(), Role.coach);
			cleanUpProcess();
			process(ureq);
		}
	}
	
	@Override
	protected DueDateValues formatDueDate(DueDate dueDate, DueDate lateDueDate, Date now, boolean done, boolean userDeadLine) {
		Date refDate = dueDate.getReferenceDueDate();
		Date refLateDate = lateDueDate == null ? null : lateDueDate.getReferenceDueDate();
		Date extensionDate = dueDate.getOverridenDueDate();
		
		boolean dateOnly = refDate != null && isDateOnly(refDate);
		if(dateOnly && userDeadLine) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(refDate);
			cal.add(Calendar.DATE, -1);
			refDate = cal.getTime();
		}

		DueDateArguments dueDateArgs = null;
		DueDateArguments lateDueDateArgs = null;

		String text = null;
		// Extension date
		if(extensionDate != null && now.before(extensionDate)
				&& (refDate == null || refDate.before(extensionDate))
				&& (refLateDate == null || refLateDate.before(extensionDate))) {
			dueDateArgs = formatDueDateArguments(extensionDate, now, false, false, userDeadLine);
			
			String i18nKey = dateOnly ? "msg.extended.coach.dateonly" : "msg.extended.coach";
			text = translate(i18nKey, dueDateArgs.args());
		}
		// Late date
		else if(refLateDate != null && now.before(refLateDate)) {
			Date date = dueDate.getDueDate();
			
			dueDateArgs = formatDueDateArguments(date, now, false, false, userDeadLine);
			lateDueDateArgs = formatDueDateArguments(refLateDate, now, false, false, userDeadLine);
			
			if(now.before(date)) {
				String i18nKey = dateOnly ? "msg.end.dateonly.done" : "msg.end.done";
				text = translate(i18nKey, dueDateArgs.args());
				if(dueDate.getOverridenDueDate() != null) {
					i18nKey = dateOnly ? "msg.late.extended.coach.dateonly" : "msg.late.extended.coach";
				} else {
					i18nKey = dateOnly ? "msg.late.standard.coach.dateonly" : "msg.late.standard.coach";
				}
				text = translate(i18nKey, mergeArguments(lateDueDateArgs.args(), new String[] { text }));
				
			} else {
				String i18nKey = dateOnly ? "msg.end.dateonly.closed" : "msg.end.closed";
				text = translate(i18nKey, dueDateArgs.args());
				
				i18nKey = dateOnly ? "msg.late.coach.dateonly" : "msg.late.coach";
				text = translate(i18nKey, mergeArguments(lateDueDateArgs.args(), new String[] { text }));
			}
		}
		else if(refDate != null && now.before(refDate)) {
			dueDateArgs = formatDueDateArguments(dueDate.getDueDate(), now, false, false, userDeadLine);
			String i18nKey = dateOnly ? "msg.end.dateonly.done" : "msg.end.done";
			text = translate(i18nKey, dueDateArgs.args());
		} else if(dueDate.getDueDate() != null) {
			dueDateArgs = formatDueDateArguments(dueDate.getDueDate(), now, false, false, userDeadLine);
			String i18nKey = dateOnly ? "msg.end.dateonly.closed" : "msg.end.closed";
			text = translate(i18nKey, dueDateArgs.args());
		}
		
		long remainingTime = (dueDateArgs == null ? -1l : dueDateArgs.timeDiffInMillSeconds());
		long lateRemainingTime = (lateDueDateArgs == null ? -1l : lateDueDateArgs.timeDiffInMillSeconds());
		return new DueDateValues(text, remainingTime, lateRemainingTime);
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
		if (emailLink == source) {
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
			} else if(event instanceof ReviewedEvent) {
				Task assignedTask = submitCorrectionsCtrl.getAssignedTask();
				doConfirmReviewDocument(ureq, assignedTask);
			} else if(event instanceof NeedRevisionEvent) {
				Task assignedTask = submitCorrectionsCtrl.getAssignedTask();
				doConfirmRevisions(ureq, assignedTask);
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
		
		gtaManager.reviewedTask(task, gtaNode, getIdentity(), Role.coach);
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
		gtaManager.updateTask(task, TaskProcess.revision, 1, gtaNode, false, getIdentity(), Role.coach);
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
			String text = translate("coach.collect.confirm.text", toName);
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
		
		if(task == null && gtaNode.isOptional(courseEnv, userCourseEnv)) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createTask(null, taskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
		}
		
		int numOfDocs = submittedDocuments == null ? 0 : submittedDocuments.length;
		task = gtaManager.collectTask(task, gtaNode, numOfDocs, getIdentity());
		showInfo("run.documents.successfully.submitted");
		
		TaskMultiUserEvent event = new TaskMultiUserEvent(TaskMultiUserEvent.SUMBIT_TASK,
				assessedIdentity, assessedGroup, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, taskListEventResource);
		
		gtaManager.log("Collect", "collect documents", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
		
		cleanUpProcess();
		process(ureq);
	}
	
	private void doConfirmBackToSubmission(UserRequest ureq, Task assignedTask) {
		String toName = null;
		if (assessedGroup != null) {
			toName = assessedGroup.getName();
		} else if (assessedIdentity != null) {
			toName = userManager.getUserDisplayName(assessedIdentity);			
		}
		
		String title = translate("coach.back.to.submission.confirm.title");
		String text = translate("coach.back.to.submission.confirm.text", toName);
		text = "<div class='o_warning'>" + text + "</div>";
		confirmBackToSubmissionCtrl = activateOkCancelDialog(ureq, title, text, confirmBackToSubmissionCtrl);
		confirmBackToSubmissionCtrl.setUserObject(assignedTask);
		listenTo(confirmBackToSubmissionCtrl);
	}
	
	private void doBackToSubmission(UserRequest ureq, Task task) {
		TaskProcess submit = gtaManager.previousStep(TaskProcess.review, gtaNode);//only submit allowed
		if(submit == TaskProcess.submit) {
			task = gtaManager.updateTask(task, submit, gtaNode, false, getIdentity(), Role.coach);
			
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
		String text = translate("coach.reset.task.confirm.text", toName);
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
