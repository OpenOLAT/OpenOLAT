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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
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
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskHelper.FilesLocked;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAParticipantController extends GTAAbstractController implements Activateable2 {
	
	private Link submitButton;
	private Link openGroupButton;
	private Link changeGroupLink;
	private Link resetTaskButton;
	private Link optionalTaskButton;

	private CloseableModalController cmc;
	private DirectoryController solutionsCtrl;
	private DirectoryController correctionsCtrl;
	private DirectoryController submittedDocCtrl;
	private MSCourseNodeRunController gradingCtrl;
	private SubmitDocumentsController submitDocCtrl;
	private DialogBoxController confirmSubmitDialog;
	private GTAAssignedTaskController assignedTaskCtrl;
	private GTAAvailableTaskController availableTaskCtrl;
	private ConfirmResetTaskController confirmResetTaskCtrl;
	private CloseableCalloutWindowController chooserCalloutCtrl;
	private BusinessGroupChooserController businessGroupChooserCtrl;
	private GTAParticipantRevisionAndCorrectionsController revisionDocumentsCtrl;
	private ConfirmOptionalTaskAssignmentController confirmOptionalAssignmentCtrl;

	private List<BusinessGroup> myGroups;
	private boolean optionalTaskRefused = false;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public GTAParticipantController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, gtaNode, userCourseEnv.getCourseEnvironment(), userCourseEnv, true, true, true);
		initContainer(ureq);
		process(ureq, true);
	}
	
	@Override
	protected void initContainer(UserRequest ureq) {
		mainVC = createVelocityContainer("run");
		
		resetTaskButton = LinkFactory.createCustomLink("participant.reset.button", "reset", "participant.reset.button", Link.BUTTON, mainVC, this);
		resetTaskButton.setElementCssClass("o_sel_course_gta_reset");
		resetTaskButton.setVisible(false);
		
		putInitialPanel(mainVC);
		initFlow() ;
	}

	protected final void initFlow() {
		//this is an individual or a group task
		String type = config.getStringValue(GTACourseNode.GTASK_TYPE);
		if(GTAType.individual.name().equals(type)) {
			assessedIdentity = getIdentity();
		} else {
			//this is a group task
			myGroups = gtaManager.getParticipatingBusinessGroups(getIdentity(), gtaNode);
			if(myGroups.isEmpty()) {
				//show error
				mainVC.contextPut("noGroupError", Boolean.TRUE);
			} else if(myGroups.size() == 1) {
				setGroupWarning();
				assessedGroup = myGroups.get(0);
				setGroupHeaders(assessedGroup);
			} else {
				//show selection and first one
				setGroupWarning();
				setMultiGroupsSelection();
				if(assessedGroup == null || !myGroups.contains(assessedGroup)) {
					assessedGroup = myGroups.get(0);
				}
				setGroupHeaders(assessedGroup);
			}
		}
	}
	
	@Override
	protected Task stepAssignment(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepAssignment(ureq, assignedTask);
		
		if(TaskHelper.inOrNull(assignedTask, TaskProcess.assignment)) {
			setActiveStatusAndCssClass("assignment");
			
			if(stepPreferences != null) {
				//assignment is very important, open it always
				stepPreferences.setAssignement(Boolean.TRUE);
			}
			
			boolean optional = gtaNode.isOptional(courseEnv, userCourseEnv);
			if(optional) {
				mainVC.contextPut("assignmentOptional", Boolean.TRUE);
			}
			
			//assignment open?
			DueDate dueDate = getAssignementDueDate(assignedTask);
			if(dueDate != null && dueDate.getDueDate() != null && dueDate.getDueDate().compareTo(new Date()) < 0) {
				//assignment is closed
				mainVC.contextPut("assignmentClosed", Boolean.TRUE);
				boolean hasAssignment = assignedTask != null && StringHelper.containsNonWhitespace(assignedTask.getTaskName());
				mainVC.contextPut("assignmentClosedWithAssignment", Boolean.valueOf(hasAssignment));
				if(hasAssignment) {
					setDoneStatusAndCssClass("assignment");
				} else {
					setExpiredStatusAndCssClass("assignment");
				}
			} else if(userCourseEnv.isCourseReadOnly()) {
				showAssignedTask(ureq, assignedTask);
			} else {
				List<TaskDefinition> availableTasks = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
				
				//assignment auto or manual
				String assignmentType = config.getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE);
				if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO.equals(assignmentType)) {
					if(optionalTaskRefused) {
						if(optionalTaskButton == null) {
							optionalTaskButton = LinkFactory.createCustomLink("run.accept.optional", "accept.optional", "run.accept.optional", Link.BUTTON, mainVC, this);
						}
					} else if(optional) {
						doConfirmOptionalAssignment(ureq, assignedTask);
					} else {
						assignedTask = assignTaskAutomatically(ureq, assignedTask);
					}
				} else if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL.equals(assignmentType)) {
					availableTaskCtrl = new GTAAvailableTaskController(ureq, getWindowControl(), availableTasks,
							taskList, assessedGroup, assessedIdentity, courseEnv, gtaNode);
					listenTo(availableTaskCtrl);
					mainVC.put("availableTasks", availableTaskCtrl.getInitialComponent());
				}
			}	
		} else {
			setDoneStatusAndCssClass("assignment");
			showAssignedTask(ureq, assignedTask);
		}
		return assignedTask;
	}
	
	private void doConfirmOptionalAssignment(UserRequest ureq, Task assignedTask) {
		confirmOptionalAssignmentCtrl = new ConfirmOptionalTaskAssignmentController(ureq, getWindowControl(), assignedTask);
		listenTo(confirmOptionalAssignmentCtrl);
		String title = translate("participant.confirm.option.task.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmOptionalAssignmentCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private Task assignTaskAutomatically(UserRequest ureq, Task assignedTask) {
		AssignmentResponse response;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			response = gtaManager.assignTaskAutomatically(taskList, assessedGroup, courseEnv, gtaNode, getIdentity());
		} else {
			response = gtaManager.assignTaskAutomatically(taskList, assessedIdentity, courseEnv, gtaNode);
		}
		
		if(response == null || response.getStatus() == AssignmentResponse.Status.error) {
			showError("task.assignment.error");
		} else if(response.getStatus() == AssignmentResponse.Status.noMoreTasks) {
			showError("error.nomoretasks");
		} else if(response.getStatus() == AssignmentResponse.Status.ok) {
			assignedTask = response.getTask();
			showInfo("task.successfully.assigned");
			showAssignedTask(ureq, assignedTask);
		}
		return assignedTask;
	}
	
	private void showAssignedTask(UserRequest ureq, Task assignedTask) {
		String message = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_USERS_TEXT);
		TaskDefinition taskDef = getTaskDefinition(assignedTask);
		assignedTaskCtrl = new GTAAssignedTaskController(ureq, getWindowControl(), assignedTask,
				taskDef, courseEnv, gtaNode,
				"task.assigned.description", "warning.no.task.choosed", message);
		listenTo(assignedTaskCtrl);
		mainVC.put("myAssignedTask", assignedTaskCtrl.getInitialComponent());
	}
	
	@Override
	protected Task stepSubmit(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepSubmit(ureq, assignedTask);
		
		boolean embedded = config.getBooleanSafe(GTACourseNode.GTASK_EMBBEDED_EDITOR);
		boolean external = config.getBooleanSafe(GTACourseNode.GTASK_EXTERNAL_EDITOR);
		if(embedded && external) {
			mainVC.contextPut("sumbitWay", "all");
		} else if(embedded) {
			mainVC.contextPut("sumbitWay", "editor");
		} else if(external) {
			mainVC.contextPut("sumbitWay", "upload");
		}
		
		//calculate state
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
				setNotAvailableStatusAndCssClass("submit");
			} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
				if(isSubmissionLate(ureq, getSubmissionDueDate(assignedTask), getLateSubmissionDueDate(assignedTask))) {
					setLateStatusAndCssClass("submit");
					mainVC.contextPut("submitLate", Boolean.TRUE);
				} else {
					setActiveStatusAndCssClass("submit");
				}
				if(userCourseEnv.isCourseReadOnly()) {
					setSubmittedDocumentsController(ureq);
				} else {
					setSubmitController(ureq, assignedTask);
				}
			} else {
				setDoneStatusAndCssClass("submit");
				setSubmittedDocumentsController(ureq);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			if(isSubmissionLate(ureq, getSubmissionDueDate(assignedTask), getLateSubmissionDueDate(assignedTask))) {
				setLateStatusAndCssClass("submit");
				mainVC.contextPut("submitLate", Boolean.TRUE);
			} else {
				setActiveStatusAndCssClass("submit");
			}
			if(userCourseEnv.isCourseReadOnly()) {
				setSubmittedDocumentsController(ureq);
			} else {
				setSubmitController(ureq, assignedTask);
			}
		} else {
			setDoneStatusAndCssClass("submit");
			setSubmittedDocumentsController(ureq);
		}
		
		return assignedTask;
	}
	
	private void setSubmitController(UserRequest ureq, Task task) {
		File documentsDir;
		VFSContainer documentsContainer;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, getIdentity());
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, getIdentity());
		}
		
		DueDate dueDate = getSubmissionDueDate(task);
		DueDate lateDueDate = getLateSubmissionDueDate(task);
		Date deadline = gtaManager.getDeadlineOf(dueDate, lateDueDate);
		int minDocs = config.getIntegerSafe(GTACourseNode.GTASK_MIN_SUBMITTED_DOCS, -1);
		int maxDocs = config.getIntegerSafe(GTACourseNode.GTASK_MAX_SUBMITTED_DOCS, -1);
		submitDocCtrl = new SubmitDocumentsController(ureq, getWindowControl(), task, documentsDir, documentsContainer,
				minDocs, maxDocs, gtaNode, courseEnv, userCourseEnv.isCourseReadOnly(),
				config.getBooleanSafe(GTACourseNode.GTASK_EXTERNAL_EDITOR),
				config.getBooleanSafe(GTACourseNode.GTASK_EMBBEDED_EDITOR), deadline, "document", null, null, null);
		listenTo(submitDocCtrl);
		mainVC.put("submitDocs", submitDocCtrl.getInitialComponent());
		
		submitButton = LinkFactory.createCustomLink("run.submit.button", "submit", "run.submit.button", Link.BUTTON, mainVC, this);
		submitButton.setElementCssClass("o_sel_course_gta_submit_docs");
		submitButton.setCustomEnabledLinkCSS(submitDocCtrl.hasUploadDocuments() ? "btn btn-primary" : "btn btn-default");
		submitButton.setIconLeftCSS("o_icon o_icon_submit");
		submitButton.setVisible(!userCourseEnv.isCourseReadOnly());
	}
	
	private void setSubmittedDocumentsController(UserRequest ureq) {
		File documentsDir;
		VFSContainer documentsContainer = null;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, getIdentity());
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, getIdentity());
		}
		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		if(hasDocuments) {
			submittedDocCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"run.submitted.description");
			listenTo(submittedDocCtrl);
			mainVC.put("submittedDocs", submittedDocCtrl.getInitialComponent());
		} else {
			TextFactory.createTextComponentFromI18nKey("submittedDocs", "run.submitted.nofiles", getTranslator(), null, true, mainVC);	
		}
	}
	
	private void doConfirmSubmit(UserRequest ureq, Task task) {
		String title = translate("run.submit.button");
		String text;
		File[] submittedDocuments;
		VFSContainer documentsContainer;
		
		int minDocs = config.getIntegerSafe(GTACourseNode.GTASK_MIN_SUBMITTED_DOCS, -1);
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
			if(minDocs > 0 && submittedDocuments.length < minDocs) {
				showWarning("error.min.documents", new String[]{ Integer.toString(minDocs), Integer.toString(submittedDocuments.length) });
				return;
			} else if(submittedDocuments.length == 0) {
				text = "<div class='o_warning'>" + translate("run.submit.confirm.warning.group", new String[]{ StringHelper.escapeHtml(assessedGroup.getName()) }) + "</div>";
			} else {
				text = translate("run.submit.confirm.group", new String[]{ StringHelper.escapeHtml(assessedGroup.getName()) });
			}
		} else {
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, getIdentity());
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, getIdentity());
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
			if(minDocs > 0 && submittedDocuments.length < minDocs) {
				showWarning("error.min.documents", new String[]{ Integer.toString(minDocs), Integer.toString(submittedDocuments.length) });
				return;
			} else if(submittedDocuments.length == 0) {
				text = "<div class='o_warning'>" + translate("run.submit.confirm.warning") + "</div>";
			} else {
				text = translate("run.submit.confirm");
			}
		}
		
		FilesLocked lockedBy = TaskHelper.getDocumentsLocked(documentsContainer, submittedDocuments);
		if(lockedBy != null) {
			showWarning("warning.submit.documents.edited", new String[]{ lockedBy.getLockedBy(), lockedBy.getLockedFiles() });
		} else {
			confirmSubmitDialog = activateOkCancelDialog(ureq, title, text, confirmSubmitDialog);
			confirmSubmitDialog.setUserObject(task);
		}
	}
	
	private void doSubmitDocuments(UserRequest ureq, Task task) {
		int numOfDocs = getNumberOfSubmittedDocuments();
		if(task == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createTask(null, taskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
		}
		task = gtaManager.submitTask(task, gtaNode, numOfDocs, getIdentity(), Role.user);
		showInfo("run.documents.successfully.submitted");
		
		TaskMultiUserEvent event = new TaskMultiUserEvent(TaskMultiUserEvent.SUMBIT_TASK,
				assessedIdentity, assessedGroup, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, taskListEventResource);
		
		gtaManager.log("Submit", "submit documents", task,
				getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.user);
		
		cleanUpProcess();
		process(ureq, true);

		//do send e-mail
		if(config.getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_MAIL_CONFIRMATION)) {
			doSubmissionEmail();
		}
	}
	
	private void doSubmissionEmail() {
		String body = config.getStringValue(GTACourseNode.GTASK_SUBMISSION_TEXT);
		if(StringHelper.containsNonWhitespace(body)) {
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			List<Identity> recipientsTO;
			File submitDirectory;
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				recipientsTO = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
				submitDirectory = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			} else {
				recipientsTO = Collections.singletonList(assessedIdentity);
				submitDirectory = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
			}
			
			String subject = translate("submission.mail.subject");
			File[] files = TaskHelper.getDocuments(submitDirectory);
			MailTemplate template = new GTAMailTemplate(subject, body, files, getIdentity(), getTranslator());
			
			MailerResult result = new MailerResult();
			MailBundle[] bundles = mailManager.makeMailBundles(context, recipientsTO, template, null, UUID.randomUUID().toString(), result);
			mailManager.sendMessage(bundles);
		}
	}
	
	private void doConfirmResetTask(UserRequest ureq, Task task) {
		if(guardModalController(confirmResetTaskCtrl)) return;
		confirmResetTaskCtrl = new ConfirmResetTaskController(ureq, getWindowControl(), task, gtaNode, courseEnv);
		listenTo(confirmResetTaskCtrl);
		
		String title = translate("participant.confirm.reset.task.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetTaskCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
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
				setReviewStatusAndCssClass("review");
				setReviews(ureq, taskRevisions, true, false);
			} else {
				setDoneStatusAndCssClass("review");
				setReviews(ureq, taskRevisions, false, (assignedTask.getRevisionLoop() > 0));
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.review) {
			setReviewStatusAndCssClass("review");
			setReviews(ureq, taskRevisions, true, false);
		} else {
			setDoneStatusAndCssClass("review");
			setReviews(ureq, taskRevisions, false, false);
		}
		
		return assignedTask;
	}
	
	private void setReviews(UserRequest ureq,List<TaskRevision> taksRevisions, boolean waiting, boolean hasRevisions) {
		File documentsDir;
		VFSContainer documentsContainer = null;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, getIdentity());
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, getIdentity());
		}
		
		if(!waiting && TaskHelper.hasDocuments(documentsDir)) {
			correctionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"run.corrections.description", "bulk.review", "review");
			listenTo(correctionsCtrl);
			mainVC.put("corrections", correctionsCtrl.getInitialComponent());
		}
		String msg;
		if (hasRevisions) {
			msg = "<i class='o_icon o_icon_warn'> </i> " + translate("run.corrections.rejected");
		} else if (waiting) {
			msg = "<i class='o_icon o_icon_info'> </i> " + translate("run.review.waiting");
		} else {				
			msg = "<i class='o_icon o_icon_ok'> </i> " + translate("run.review.closed");				
		}
		
		mainVC.contextPut("reviewMessage", msg);
		
		if(!waiting) {
			TaskRevision taskRevision = getTaskRevision(taksRevisions, TaskProcess.correction, 0);
			if(taskRevision != null && StringHelper.containsNonWhitespace(taskRevision.getComment())) {
				mainVC.contextPut("correctionMessage", taskRevision.getComment());
				String commentator = userManager.getUserDisplayName(taskRevision.getCommentAuthor());
				String commentDate = Formatter.getInstance(getLocale()).formatDate(taskRevision.getCommentLastModified());
				String infos = translate("run.corrections.comment.infos", commentDate, commentator);
				mainVC.contextPut("correctionMessageInfos", infos);
			}
		}
	}

	@Override
	protected Task stepRevision(UserRequest ureq, Task assignedTask, List<TaskRevision> taskRevisions) {
		assignedTask = super.stepRevision(ureq, assignedTask, taskRevisions);

		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review) {
				setNotAvailableStatusAndCssClass("revision");
			} else if(assignedTask.getTaskStatus() == TaskProcess.correction) {
				setReviewStatusAndCssClass("revision");
				setRevisionsAndCorrections(ureq, assignedTask, taskRevisions);
			} else if(assignedTask.getTaskStatus() == TaskProcess.revision) {
				setActiveStatusAndCssClass("revision");
				setRevisionsAndCorrections(ureq, assignedTask, taskRevisions);
			} else {
				setDoneStatusAndCssClass("revision");
				setRevisionsAndCorrections(ureq, assignedTask, taskRevisions);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
			setActiveStatusAndCssClass("revision");
			setRevisionsAndCorrections(ureq, assignedTask, taskRevisions);
		} else {
			setDoneStatusAndCssClass("revision");
			setRevisionsAndCorrections(ureq, assignedTask, taskRevisions);
		}
		
		return assignedTask;
	}
	
	private void setRevisionsAndCorrections(UserRequest ureq, Task task, List<TaskRevision> taskRevisions) {
		if(task.getRevisionLoop() > 0) {
			revisionDocumentsCtrl = new GTAParticipantRevisionAndCorrectionsController(ureq, getWindowControl(), 
					userCourseEnv, task, taskRevisions, gtaNode, assessedGroup, taskListEventResource);
			listenTo(revisionDocumentsCtrl);
			mainVC.put("revisionDocs", revisionDocumentsCtrl.getInitialComponent());
			
			String msg = null;
			if (task.getTaskStatus() == TaskProcess.revision) {
				// message about rejected work is displayed in GTAParticipantRevisionAndCorrectionsController
			} else if (task.getTaskStatus() == TaskProcess.correction) {
				msg = "<i class='o_icon o_icon_info'> </i> " + translate("run.review.waiting");
			} else {				
				msg = "<i class='o_icon o_icon_ok'> </i> " + translate("run.review.closed");				
			}
			mainVC.contextPut("revisionMessage", msg);
			
		} else {
			TaskProcess status = task.getTaskStatus();
			if (status == TaskProcess.solution || status == TaskProcess.grading || status == TaskProcess.graded) {
				mainVC.contextPut("skipRevisions", Boolean.TRUE);
			}
		}
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
				if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false)) {
					setActiveStatusAndCssClass("solution", "msg.status.available");
					setSolutions(ureq, assignedTask);
				} else {
					setNotAvailableStatusAndCssClass("solution");
				}
			} else if(assignedTask.getTaskStatus() == TaskProcess.solution) {
				if(setSolutions(ureq, assignedTask)) {
					setActiveStatusAndCssClass("solution", "msg.status.available");
				} else {
					setNotAvailableStatusAndCssClass("solution");
				}
			} else {
				setDoneStatusAndCssClass("solution", "msg.status.available");
				setSolutions(ureq, assignedTask);
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.solution) {
			if(setSolutions(ureq, assignedTask)) {
				setActiveStatusAndCssClass("solution", "msg.status.available");
			} else {
				setNotAvailableStatusAndCssClass("solution");
			}
		} else {
			setDoneStatusAndCssClass("solution", "msg.status.available");
			setSolutions(ureq, assignedTask);
		}
		
		return assignedTask;
	}
	
	/**
	 * Set the solutions if the date permissions are meet.
	 * 
	 * @param ureq The user request
	 * @param assignedTask The task
	 * @return true if the solutions are visible
	 */
	private boolean setSolutions(UserRequest ureq, Task assignedTask) {
		boolean visible = isSolutionVisible(ureq, assignedTask);
		if(visible) {
			DueDate availableDate = getSolutionDueDate(assignedTask);
			if(showSolutions(availableDate)) {
				File documentsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
				VFSContainer documentsContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
				solutionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer, "run.solutions.description", "bulk.solutions", "solutions");
				listenTo(solutionsCtrl);
				mainVC.put("solutions", solutionsCtrl.getInitialComponent());
				
				if(gtaManager.firstStep(gtaNode) == TaskProcess.solution) {
					assignedTask = gtaManager.ensureTaskExists(assignedTask, assessedGroup, assessedIdentity, courseEntry, gtaNode);
					gtaManager.syncAssessmentEntry(assignedTask, gtaNode, userCourseEnv, false, getIdentity(), Role.user);
				}
			} else {
				VelocityContainer waitVC = createVelocityContainer("no_solutions_foryou");
				mainVC.put("solutions", waitVC);
			}
		} else {
			VelocityContainer waitVC = createVelocityContainer("wait_for_solutions");
			mainVC.put("solutions", waitVC);
		}
		return visible;
	}

	@Override
	protected Task stepGrading(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepGrading(ureq, assignedTask);
		
		String infoTextUser = config.getStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
	    if(StringHelper.containsNonWhitespace(infoTextUser)) {
	    	mainVC.contextPut("gradingInfoTextUser", StringHelper.xssScan(infoTextUser));
	    }
	    
	    boolean showGrading = false;
	    MSCourseNodeRunController msCtrl = new MSCourseNodeRunController(ureq, getWindowControl(), userCourseEnv, gtaNode, false, false);
	    if(msCtrl.hasScore() || msCtrl.hasPassed() || msCtrl.hasComment()) {
	    	showGrading = true; 
	    } else if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				|| config.getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review || assignedTask.getTaskStatus() == TaskProcess.correction
					|| assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.solution) {
				setNotAvailableStatusAndCssClass("grading");
			} else if(assignedTask.getTaskStatus() == TaskProcess.graded || assignedTask.getTaskStatus() == TaskProcess.grading) {
				showGrading = true;
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.graded || assignedTask.getTaskStatus() == TaskProcess.grading){
			showGrading = true;
		}
		
		if(showGrading) {
			gradingCtrl = msCtrl;
			listenTo(gradingCtrl);
			if (assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.graded) {
				setDoneStatusAndCssClass("grading");
			} else {
				setActiveStatusAndCssClass("grading");
			}
			
			mainVC.put("grading", gradingCtrl.getInitialComponent());
			stepPreferences.setGrading(Boolean.TRUE);
		}

		return assignedTask;
	}
	
	@Override
	protected void resetTask(UserRequest ureq, Task task) {
		resetTaskButton.setUserObject(task);

		DueDate assignmentDueDate = getAssignementDueDate(task);
		boolean allowed = task != null
				&& (StringHelper.containsNonWhitespace(task.getTaskName()) || (! StringHelper.containsNonWhitespace(task.getTaskName()) && task.getTaskStatus() == TaskProcess.submit))
				&& (task.getTaskStatus() == TaskProcess.assignment || task.getTaskStatus() == TaskProcess.submit)
				&& task.getAllowResetDate() != null 
				&& (assignmentDueDate == null || assignmentDueDate.getDueDate() == null || assignmentDueDate.getDueDate().after(new Date()))
				&& GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL.equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE));
		resetTaskButton.setVisible(allowed);
	}

	@Override
	protected void nodeLog(Task assignedTask) {
		if(isResultVisible(assignedTask)) {
			if(businessGroupTask) {
				String userLog = courseEnv.getAuditManager().getUserNodeLog(gtaNode, getIdentity());
				if(StringHelper.containsNonWhitespace(userLog)) {
					mainVC.contextPut("userLog", StringHelper.escapeHtml(userLog));
				} else {
					mainVC.contextRemove("userLog");
				}
			} else {
				super.nodeLog(assignedTask);
			}
		} else {
			mainVC.contextRemove("userLog");
		}
	}
	
	private boolean isResultVisible(Task assignedTask) {
		boolean isVisible = false;
		if(config.getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			if (assignedTask != null && (assignedTask.getTaskStatus() == TaskProcess.grading || assignedTask.getTaskStatus() == TaskProcess.graded)) {
				AssessmentEvaluation eval = courseAssessmentService.getAssessmentEvaluation(gtaNode, getAssessedUserCourseEnvironment());
				isVisible = eval.getUserVisible() != null && eval.getUserVisible().booleanValue();
			}
		} else {
			isVisible = true;
		}
		return isVisible;
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
	
	private void setGroupWarning() {
		mainVC.contextPut("groupWarning", Boolean.TRUE);
	}
	
	private void setGroupHeaders(BusinessGroup group) {
		mainVC.contextPut("groupName", group.getName());
		openGroupButton = LinkFactory.createButton("open.group", mainVC, this);
		openGroupButton.setIconLeftCSS("o_icon o_icon_group");
		openGroupButton.setVisible(BusinessGroup.BUSINESS_TYPE.equals(group.getTechnicalType()));
	}
	
	private void setMultiGroupsSelection() {
		changeGroupLink = LinkFactory.createLink("change.group", mainVC, this);
	}
	
	@Override
	protected DueDateValues formatDueDate(DueDate dueDate, DueDate lateDueDate, Date now, boolean done, boolean userDeadLine) {
		Date refDate = dueDate.getReferenceDueDate();
		Date refLateDate = lateDueDate == null ? null : lateDueDate.getReferenceDueDate();
		Date extensionDate = dueDate.getOverridenDueDate();

		String text = null;
		DueDateArguments dueDateArgs = null;
		DueDateArguments lateDueDateArgs = null;

		// Extension date
		if(extensionDate != null && now.before(extensionDate)
				&& (refDate == null || refDate.before(extensionDate))
				&& (refLateDate == null || refLateDate.before(extensionDate))) {
			if(done) {
				Date date = dueDate.getDueDate();
				boolean dateOnly = isDateOnly(date);
				dueDateArgs = formatDueDateArguments(date, now, false, true, userDeadLine);
				String i18nKey;
				if(now.before(date)) {
					i18nKey = dateOnly ? "msg.end.dateonly.done" : "msg.end.done";
				} else {
					i18nKey = dateOnly ? "msg.end.dateonly.closed" : "msg.end.closed";
				}
				text = translate(i18nKey, dueDateArgs.args());
			} else {
				boolean dateOnly = isDateOnly(extensionDate);
				dueDateArgs = formatDueDateArguments(extensionDate, now, false, true, userDeadLine);
				
				String i18nKey;
				if(dueDateArgs.days() > 1) {// 2 days
					i18nKey = dateOnly ? "msg.extended.dateonly.within.days" : "msg.extended.within.days";
				} else if(dueDateArgs.timeDiffInMillSeconds() < ONE_DAY_IN_MILLISEC) {
					// Less than a day, but perhaps still next day
					i18nKey = dateOnly ? "msg.extended.dateonly.within.hours" : "msg.extended.within.hours";
				} else if(dueDateArgs.days() == 1) {
					i18nKey = dateOnly ? "msg.extended.dateonly.within.day" : "msg.extended.within.day";
				} else {
					// some hours left
					i18nKey = dateOnly ? "msg.extended.dateonly.within.hours" : "msg.extended.within.hours";
				}
				text = translate(i18nKey, dueDateArgs.args());
			}
		}
		
		// Late date
		else if(refLateDate != null && now.before(refLateDate)) {
			Date date = dueDate.getDueDate();
			boolean dateOnly = isDateOnly(date);
			boolean lateDateOnly = isDateOnly(refLateDate);
			
			if(done) {
				dueDateArgs = formatDueDateArguments(date, now, false, true, userDeadLine);
				String i18nKey;
				if(now.before(refDate)) {
					i18nKey = dateOnly ? "msg.end.dateonly.done" : "msg.end.done";
				} else {
					i18nKey = dateOnly ? "msg.end.dateonly.closed" : "msg.end.closed";
				}
				text = translate(i18nKey, dueDateArgs.args());
			} else {
				dueDateArgs = formatDueDateArguments(date, now, false, true, userDeadLine);
				lateDueDateArgs = formatDueDateArguments(refLateDate, now, true, true, userDeadLine);
				
				// Late configured but we are still in the normal deadline
				if(now.before(date)) {
					String stdI18nKey;
					if(dueDateArgs.timeDiffInMillSeconds() < ONE_DAY_IN_MILLISEC) {
						stdI18nKey = dateOnly ? "msg.late.standard.dateonly.within.hours" : "msg.late.standard.within.hours";
					} else {
						stdI18nKey = dateOnly ? "msg.late.standard.dateonly.within.days" : "msg.late.standard.within.days";
					}
					text = translate(stdI18nKey, dueDateArgs.args());
					
					String lateI18nKey;
					if(dueDate.getOverridenDueDate() != null) {
						lateI18nKey = lateDateOnly ? "msg.late.extended.dateonly.late.part" : "msg.late.extended.late.part";
					} else {
						lateI18nKey = lateDateOnly ? "msg.late.standard.dateonly.late.part" : "msg.late.standard.late.part";
					}
					// standard is 6
					text = translate(lateI18nKey, mergeArguments(lateDueDateArgs.args(), new String[] {text}));
				}
				// We are late
				else {
					String i18nKey;
					if(lateDueDateArgs.days() > 1) {// 2 days
						i18nKey = lateDateOnly ? "msg.late.dateonly.within.days" : "msg.late.within.days";
					} else if(lateDueDateArgs.timeDiffInMillSeconds() < ONE_DAY_IN_MILLISEC) {
						// Less than a day, but perhaps still next day
						i18nKey = lateDateOnly ? "msg.late.dateonly.within.hours" : "msg.late.within.hours";
					} else if(lateDueDateArgs.days() == 1) {
						i18nKey = lateDateOnly ? "msg.late.dateonly.within.day" : "msg.late.within.day";
					} else {
						// some hours left
						i18nKey = lateDateOnly ? "msg.late.dateonly.within.hours" : "msg.late.within.hours";
					}
					text = translate(i18nKey, lateDueDateArgs.args());
				}
			}
		}
		
		// Standard date
		else if(refDate != null && now.before(refDate)) {
			boolean dateOnly = isDateOnly(refDate);
			dueDateArgs = formatDueDateArguments(refDate, now, false, true, userDeadLine);
			
			String i18nKey;
			if(done) {
				i18nKey = dateOnly ? "msg.end.dateonly.done" : "msg.end.done";
			} else if(dueDateArgs.days() > 1) {// 2 days
				i18nKey = dateOnly ? "msg.end.dateonly.within.days" : "msg.end.within.days";
			} else if(dueDateArgs.timeDiffInMillSeconds() < ONE_DAY_IN_MILLISEC) {
				// Less than a day, but perhaps still next day
				i18nKey = dateOnly ? "msg.end.dateonly.within.hours" : "msg.end.within.hours";
			} else if(dueDateArgs.days() == 1) {
				i18nKey = dateOnly ? "msg.end.dateonly.within.day" : "msg.end.within.day";
			} else {
				// some hours left
				i18nKey = dateOnly ? "msg.end.dateonly.within.hours" : "msg.end.within.hours";
			}
			text = translate(i18nKey, dueDateArgs.args());
		} else if (dueDate.getDueDate() != null) {
			Date date = dueDate.getDueDate();
			boolean dateOnly = isDateOnly(date);
			dueDateArgs = formatDueDateArguments(date, now, false, true, userDeadLine);
			String i18nKey = dateOnly ? "msg.end.dateonly.closed" : "msg.end.closed";
			text = translate(i18nKey, dueDateArgs.args());
		}

		long remainingTime = (dueDateArgs == null ? -1l : dueDateArgs.timeDiffInMillSeconds());
		long lateRemainingTime = (lateDueDateArgs == null ? -1l : lateDueDateArgs.timeDiffInMillSeconds());
		return new DueDateValues(text, remainingTime, lateRemainingTime);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Correction".equalsIgnoreCase(type)) {
				int revisionLoop = entries.get(0).getOLATResourceable().getResourceableId().intValue();
				if(revisionLoop == 0) {
					if(correctionsCtrl != null) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						correctionsCtrl.activate(ureq, subEntries, null);
					}
				} else if(revisionDocumentsCtrl != null) {
					revisionDocumentsCtrl.activate(ureq, entries, null);
				}
			} else if("Solution".equalsIgnoreCase(type)) {
				if(solutionsCtrl != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					solutionsCtrl.activate(ureq, subEntries, null);
				}
			} else if("Assessment".equalsIgnoreCase(type)) {
				if(gradingCtrl != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					gradingCtrl.activate(ureq, subEntries, null);
				}
			}
		}
	}

	@Override
	protected void processEvent(TaskMultiUserEvent event) {
		if(TaskMultiUserEvent.SUMBIT_TASK.equals(event.getCommand())) {
			if(submitDocCtrl != null) {
				submitDocCtrl.close();
			}
		} else if(TaskMultiUserEvent.SUBMIT_REVISION.equals(event.getCommand())) {
			if(revisionDocumentsCtrl != null) {
				revisionDocumentsCtrl.close();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(openGroupButton == source) {
			doOpenBusinessGroup(ureq);
		} else if(changeGroupLink == source) {
			doChangeBusinessGroup(ureq);
		} else if(submitButton == source) {
			Task assignedTask = submitDocCtrl.getAssignedTask();
			doConfirmSubmit(ureq, assignedTask);
		} else if(resetTaskButton == source) {
			doConfirmResetTask(ureq, (Task)resetTaskButton.getUserObject());
		} else if(optionalTaskButton == source) {
			doConfirmOptionalAssignment(ureq, null);
		} else if("reload".equals(event.getCommand())) {
			cleanUpProcess();
			resetDueDates();
			process(ureq, true);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(availableTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				cleanUpProcess();
				resetDueDates();
				process(ureq, true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(revisionDocumentsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				cleanUpProcess();
				process(ureq, true);
			}
		} else if(businessGroupChooserCtrl == source) {
			if(event == Event.DONE_EVENT && businessGroupChooserCtrl.getSelectGroup() != null) {
				cleanUpProcess();
				resetDueDates();
				assessedGroup = businessGroupChooserCtrl.getSelectGroup();
				setGroupHeaders(assessedGroup);
				process(ureq, true);
			}
			chooserCalloutCtrl.deactivate();
			cleanUpPopups();
		} else if(chooserCalloutCtrl == source) {
			cleanUpPopups();
		} else if(confirmSubmitDialog == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Task task = (Task)confirmSubmitDialog.getUserObject();
				doSubmitDocuments(ureq, task);
			}
			cleanUpPopups();
		} else if(confirmResetTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				cleanUpProcess();
				process(ureq, true);
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if(submitDocCtrl == source) {
			boolean hasUploadDocuments = submitDocCtrl.hasUploadDocuments();
			if(event instanceof SubmitEvent) {
				Task assignedTask = submitDocCtrl.getAssignedTask();
				gtaManager.log("Submit", (SubmitEvent)event, assignedTask,
						getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.user);
			} else if(event == Event.DONE_EVENT) {
				cleanUpProcess();
				process(ureq, true);
			}
			
			if(submitButton != null) {
				submitButton.setCustomEnabledLinkCSS(hasUploadDocuments ? "btn btn-primary" : "btn btn-default");
			}
		} else if(confirmOptionalAssignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				assignTaskAutomatically(ureq, confirmOptionalAssignmentCtrl.getTask());
				cleanUpProcess();
				process(ureq, true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				optionalTaskRefused = true;
				cleanUpProcess();
				process(ureq, true);
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if(cmc == source) {
			cleanUpPopups();
		}
		super.event(ureq, source, event);
	}
	
	/**
	 * Remove all the stuff in the main velocity template, discard all controllers
	 */
	private void cleanUpProcess() {
		if(availableTaskCtrl != null) {
			mainVC.remove(availableTaskCtrl.getInitialComponent());
		}
		if(submitDocCtrl != null) {
			mainVC.remove(submitDocCtrl.getInitialComponent());
		}
		if(assignedTaskCtrl != null) {
			mainVC.remove(assignedTaskCtrl.getInitialComponent());
		}
		if(correctionsCtrl != null) {
			mainVC.remove(correctionsCtrl.getInitialComponent());
		}
		if(solutionsCtrl != null) {
			mainVC.remove(solutionsCtrl.getInitialComponent());
		}
		if(gradingCtrl != null) {
			mainVC.remove(gradingCtrl.getInitialComponent());
		}
		
		removeAsListenerAndDispose(availableTaskCtrl);
		removeAsListenerAndDispose(assignedTaskCtrl);
		removeAsListenerAndDispose(correctionsCtrl);
		removeAsListenerAndDispose(solutionsCtrl);
		removeAsListenerAndDispose(submitDocCtrl);
		removeAsListenerAndDispose(gradingCtrl);
		availableTaskCtrl = null;
		assignedTaskCtrl = null;
		correctionsCtrl = null;
		solutionsCtrl = null;
		submitDocCtrl = null;
		gradingCtrl = null;
	}

	private void cleanUpPopups() {
		removeAsListenerAndDispose(confirmOptionalAssignmentCtrl);
		removeAsListenerAndDispose(businessGroupChooserCtrl);
		removeAsListenerAndDispose(confirmResetTaskCtrl);
		removeAsListenerAndDispose(confirmSubmitDialog);
		removeAsListenerAndDispose(chooserCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		confirmOptionalAssignmentCtrl = null;
		businessGroupChooserCtrl = null;
		confirmResetTaskCtrl = null;
		confirmSubmitDialog = null;
		chooserCalloutCtrl = null;
		cmc = null;
	}

	private void doOpenBusinessGroup(UserRequest ureq) {
		String businessPath = "[BusinessGroup:" + assessedGroup.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doChangeBusinessGroup(UserRequest ureq) {
		removeAsListenerAndDispose(businessGroupChooserCtrl);
		removeAsListenerAndDispose(chooserCalloutCtrl);

		businessGroupChooserCtrl = new BusinessGroupChooserController(ureq, getWindowControl(), myGroups);
		listenTo(businessGroupChooserCtrl);

		chooserCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				businessGroupChooserCtrl.getInitialComponent(), changeGroupLink, "", true, "");
		listenTo(chooserCalloutCtrl);
		chooserCalloutCtrl.activate();
	}

	@Override
	protected Role getDoer() {
		return Role.user;
	}	
}