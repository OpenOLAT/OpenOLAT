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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskDefinitionList;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAParticipantController extends GTAAbstractController {
	
	private Link submitButton, openGroupButton, changeGroupLink;

	private MSCourseNodeRunController gradingCtrl;
	private SubmitDocumentsController submitDocCtrl;
	private DialogBoxController confirmSubmitDialog;
	private GTAAssignedTaskController assignedTaskCtrl;
	private GTAAvailableTaskController availableTaskCtrl;
	private CloseableCalloutWindowController chooserCalloutCtrl;
	private BusinessGroupChooserController businessGroupChooserCtrl;
	private GTAParticipantRevisionAndCorrectionsController revisionDocumentsCtrl;
	private DirectoryController submittedDocCtrl, correctionsCtrl, solutionsCtrl;

	private List<BusinessGroup> myGroups;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public GTAParticipantController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, gtaNode, userCourseEnv.getCourseEnvironment(), userCourseEnv, true, true);
	}
	
	@Override
	protected void initContainer(UserRequest ureq) {
		mainVC = createVelocityContainer("run");
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
			if(myGroups.size() == 0) {
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
			mainVC.contextPut("assignmentCssClass", "o_active");
			if(stepPreferences != null) {
				//assignment is very important, open it always
				stepPreferences.setAssignement(Boolean.TRUE);
			}
			
			//assignment open?
			Date dueDate = getAssignementDueDate();
			if(dueDate != null && dueDate.compareTo(new Date()) < 0) {
				//assignment is closed
				mainVC.contextPut("assignmentClosed", Boolean.TRUE);
			} else {
				TaskDefinitionList tasks = (TaskDefinitionList)config.get(GTACourseNode.GTASK_TASKS);
				List<TaskDefinition> availableTasks = new ArrayList<>(tasks.getTasks());
				
				//assignment auto or manual
				String assignmentType = config.getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE);
				if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO.equals(assignmentType)) {
					
					AssignmentResponse response;
					if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
						response = gtaManager.assignTaskAutomatically(taskList, assessedGroup, courseEnv, gtaNode);
					} else {
						response = gtaManager.assignTaskAutomatically(taskList, assessedIdentity, courseEnv, gtaNode);
					}
					
					if(response == null || response.getStatus() == AssignmentResponse.Status.error) {
						showError("task.assignment.error");
					} else if(response == null || response.getStatus() == AssignmentResponse.Status.noMoreTasks) {
						showError("error.nomoretasks");
					} else if(response == null || response.getStatus() == AssignmentResponse.Status.ok) {
						showInfo("task.successfully.assigned");
						showAssignedTask(ureq, assignedTask);
					}
				} else if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL.equals(assignmentType)) {
					availableTaskCtrl = new GTAAvailableTaskController(ureq, getWindowControl(), availableTasks,
							taskList, assessedGroup, assessedIdentity, courseEnv, gtaNode);
					listenTo(availableTaskCtrl);
					mainVC.put("availableTasks", availableTaskCtrl.getInitialComponent());
				}
			}	
		} else {
			mainVC.contextPut("assignmentCssClass", "o_done");
			showAssignedTask(ureq, assignedTask);
		}
		return assignedTask;
	}
	
	private void showAssignedTask(UserRequest ureq, Task assignedTask) {
		String message = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_USERS_TEXT);
		TaskDefinition taskDef = getTaskDefinition(assignedTask);
		assignedTaskCtrl = new GTAAssignedTaskController(ureq, getWindowControl(), assignedTask,
				taskDef, courseEnv, gtaNode,
				"task.assigned.description", message);
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
				mainVC.contextPut("submitCssClass", "");
			} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
				mainVC.contextPut("submitCssClass", "o_active");
				setSubmitController(ureq, assignedTask);
			} else {
				mainVC.contextPut("submitCssClass", "o_done");
				setSubmittedDocumentsController(ureq);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			mainVC.contextPut("submitCssClass", "o_active");
			setSubmitController(ureq, assignedTask);
		} else {
			mainVC.contextPut("submitCssClass", "o_done");
			setSubmittedDocumentsController(ureq);
		}
		
		return assignedTask;
	}
	
	private void setSubmitController(UserRequest ureq, Task task) {
		File documentsDir;
		VFSContainer documentsContainer;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, getIdentity());
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, getIdentity());
		}
		
		int maxDocs = config.getIntegerSafe(GTACourseNode.GTASK_MAX_SUBMITTED_DOCS, -1);
		submitDocCtrl = new SubmitDocumentsController(ureq, getWindowControl(), task, documentsDir, documentsContainer, maxDocs, config, "document");
		listenTo(submitDocCtrl);
		mainVC.put("submitDocs", submitDocCtrl.getInitialComponent());
		
		submitButton = LinkFactory.createCustomLink("run.submit.button", "submit", "run.submit.button", Link.BUTTON, mainVC, this);
		submitButton.setElementCssClass("o_sel_course_gta_submit_docs");
		submitButton.setCustomEnabledLinkCSS("btn btn-primary");
		submitButton.setIconLeftCSS("o_icon o_icon_submit");

	}
	
	private void setSubmittedDocumentsController(UserRequest ureq) {
		File documentsDir;
		VFSContainer documentsContainer = null;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, getIdentity());
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
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			text = translate("run.submit.confirm.group", new String[]{ StringHelper.escapeHtml(assessedGroup.getName()) });
		} else {
			text = translate("run.submit.confirm");
		}
		confirmSubmitDialog = activateOkCancelDialog(ureq, title, text, confirmSubmitDialog);
		confirmSubmitDialog.setUserObject(task);
	}
	
	private void doSubmitDocuments(UserRequest ureq, Task task) {
		TaskProcess review = gtaManager.nextStep(TaskProcess.submit, gtaNode);
		task = gtaManager.updateTask(task, review);
		showInfo("run.documents.successfully.submitted");
		
		gtaManager.log("Submit", "submit documents", task, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
		
		cleanUpProcess();
		process(ureq);
		doUpdateAttempts();

		//do send e-mail
		if(config.getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_MAIL_CONFIRMATION)) {
			doSubmissionEmail();
		}
	}
	
	private void doUpdateAttempts() {
		if(businessGroupTask) {
			List<Identity> identities = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
			AssessmentManager assessmentManager = courseEnv.getAssessmentManager();
			assessmentManager.preloadCache(identities);
			ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());

			for(Identity identity:identities) {
				UserCourseEnvironment uce = AssessmentHelper.createAndInitUserCourseEnvironment(identity, course);
				gtaNode.incrementUserAttempts(uce);
			}
		} else {
			gtaNode.incrementUserAttempts(userCourseEnv);
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
			MailTemplate template = new GTAMailTemplate(subject, body, files, getIdentity(), getLocale());
			
			MailerResult result = new MailerResult();
			MailBundle[] bundles = mailManager.makeMailBundles(context, recipientsTO, template, null, UUID.randomUUID().toString(), result);
			mailManager.sendMessage(bundles);
		}
	}
	
	@Override
	protected Task stepReviewAndCorrection(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepReviewAndCorrection(ureq, assignedTask);
		
		mainVC.contextPut("review", Boolean.FALSE);
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit) {
				mainVC.contextPut("reviewCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.review) {
				mainVC.contextPut("reviewCssClass", "o_active");
				setReviews(ureq, true, false);
			} else {
				mainVC.contextPut("reviewCssClass", "o_done");
				setReviews(ureq, false, (assignedTask.getRevisionLoop() > 0));
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.review) {
			mainVC.contextPut("reviewCssClass", "o_active");
			setReviews(ureq, true, false);
		} else {
			mainVC.contextPut("reviewCssClass", "o_done");
			setReviews(ureq, false, false);
		}
		
		return assignedTask;
	}
	
	private void setReviews(UserRequest ureq, boolean waiting, boolean hasRevisions) {
		File documentsDir;
		VFSContainer documentsContainer = null;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedGroup);
			documentsContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			documentsDir = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, getIdentity());
		}
		
		if(TaskHelper.hasDocuments(documentsDir)) {
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
	}

	@Override
	protected Task stepRevision(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepRevision(ureq, assignedTask);

		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review) {
				mainVC.contextPut("revisionCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
				mainVC.contextPut("revisionCssClass", "o_active");
				setRevisionsAndCorrections(ureq, assignedTask);
			} else {
				mainVC.contextPut("revisionCssClass", "o_done");
				setRevisionsAndCorrections(ureq, assignedTask);
			}
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.correction) {
			mainVC.contextPut("revisionCssClass", "o_active");
			setRevisionsAndCorrections(ureq, assignedTask);
		} else {
			mainVC.contextPut("revisionCssClass", "o_done");
			setRevisionsAndCorrections(ureq, assignedTask);
		}
		
		return assignedTask;
	}
	
	private void setRevisionsAndCorrections(UserRequest ureq, Task task) {
		if(task.getRevisionLoop() > 0) {
			revisionDocumentsCtrl = new GTAParticipantRevisionAndCorrectionsController(ureq, getWindowControl(), 
					userCourseEnv, task, gtaNode, assessedGroup);
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
				mainVC.contextPut("solutionCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.solution) {
				mainVC.contextPut("solutionCssClass", "o_active");
				setSolutions(ureq);
			} else {
				mainVC.contextPut("solutionCssClass", "o_done");
				setSolutions(ureq);
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.solution){
			mainVC.contextPut("solutionCssClass", "o_active");
			setSolutions(ureq);
		} else {
			mainVC.contextPut("solutionCssClass", "o_done");
			setSolutions(ureq);
		}
		
		return assignedTask;
	}
	
	private void setSolutions(UserRequest ureq) {
		Date availableDate = getSolutionDueDate();
		boolean visible = availableDate == null || availableDate.compareTo(new Date()) <= 0;
		if(visible) {
			File documentsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
			VFSContainer documentsContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
			if(TaskHelper.hasDocuments(documentsDir)) {
				solutionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer, "run.solutions.description", "bulk.solutions", "solutions");
				listenTo(solutionsCtrl);
				mainVC.put("solutions", solutionsCtrl.getInitialComponent());
			}
		} else {
			VelocityContainer waitVC = createVelocityContainer("wait_for_solutions");
			mainVC.put("solutions", waitVC);
		}
	}

	@Override
	protected Task stepGrading(UserRequest ureq, Task assignedTask) {
		assignedTask = super.stepGrading(ureq, assignedTask);
		
		if(businessGroupTask) {
			String userLog = courseEnv.getAuditManager().getUserNodeLog(gtaNode, getIdentity());
			if(StringHelper.containsNonWhitespace(userLog)) {
				mainVC.contextPut("userLog", userLog);
			} else {
				mainVC.contextRemove("userLog");
			}
		}
		
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
				mainVC.contextPut("gradingCssClass", "");
			} else if(assignedTask.getTaskStatus() == TaskProcess.graded || assignedTask.getTaskStatus() == TaskProcess.grading) {
				showGrading = true;
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.graded || assignedTask.getTaskStatus() == TaskProcess.grading){
			showGrading = true;
		}
		
		if(showGrading) {
			gradingCtrl = msCtrl;
			listenTo(gradingCtrl);
			mainVC.contextPut("gradingCssClass", "o_active");
			mainVC.put("grading", gradingCtrl.getInitialComponent());
			stepPreferences.setGrading(Boolean.TRUE);
		}

		return assignedTask;
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
	
	private void setGroupWarning() {
		mainVC.contextPut("groupWarning", Boolean.TRUE);
	}
	
	private void setGroupHeaders(BusinessGroup group) {
		mainVC.contextPut("groupName", group.getName());
		openGroupButton = LinkFactory.createButton("open.group", mainVC, this);
		openGroupButton.setIconLeftCSS("o_icon o_icon_group");
	}
	
	private void setMultiGroupsSelection() {
		changeGroupLink = LinkFactory.createLink("change.group", mainVC, this);
	}
	
	@Override
	protected void doDispose() {
		//
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
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(availableTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				cleanUpProcess();
				process(ureq);
			}
		} else if(revisionDocumentsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				cleanUpProcess();
				process(ureq);
			}
		} else if(businessGroupChooserCtrl == source) {
			if(event == Event.DONE_EVENT && businessGroupChooserCtrl.getSelectGroup() != null) {
				cleanUpProcess();
				resetDueDates();
				assessedGroup = businessGroupChooserCtrl.getSelectGroup();
				process(ureq);
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
		} else if(submitDocCtrl == source) {
			if(event instanceof SubmitEvent) {
				Task assignedTask = submitDocCtrl.getAssignedTask();
				gtaManager.log("Submit", (SubmitEvent)event, assignedTask, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
			}
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
		removeAsListenerAndDispose(businessGroupChooserCtrl);
		removeAsListenerAndDispose(confirmSubmitDialog);
		removeAsListenerAndDispose(chooserCalloutCtrl);
		businessGroupChooserCtrl = null;
		confirmSubmitDialog = null;
		chooserCalloutCtrl = null;
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
}