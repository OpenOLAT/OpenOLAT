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

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTARelativeToDates;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class GTAAbstractController extends BasicController {
	
	protected VelocityContainer mainVC;

	protected Identity assessedIdentity;
	protected BusinessGroup assessedGroup;
	protected CourseEnvironment courseEnv;
	protected UserCourseEnvironment userCourseEnv;
	
	protected final TaskList taskList;
	protected final GTACourseNode gtaNode;
	protected final ModuleConfiguration config;
	protected final RepositoryEntry courseEntry;
	
	protected final PublisherData publisherData;
	protected final SubscriptionContext subsContext;

	protected final boolean withTitle;
	protected final boolean withGrading;
	
	protected final boolean businessGroupTask;
	
	protected GTAStepPreferences stepPreferences;
	
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	private Date assignmentDueDate;
	private Date submissionDueDate;
	private Date solutionDueDate;
	
	@Autowired
	protected GTAManager gtaManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected UserCourseInformationsManager userCourseInformationsManager;
	
	public GTAAbstractController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, boolean withTitle, boolean withGrading) {
		this(ureq, wControl, gtaNode, courseEnv, null, null, null, withTitle, withGrading);
	}
	
	public GTAAbstractController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, UserCourseEnvironment userCourseEnv, boolean withTitle, boolean withGrading) {
		this(ureq, wControl, gtaNode, courseEnv, userCourseEnv, null, null, withTitle, withGrading);
	}

	public GTAAbstractController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, UserCourseEnvironment userCourseEnv,
			BusinessGroup assessedGroup, Identity assessedIdentity, boolean withTitle, boolean withGrading) {
		super(ureq, wControl);
		
		this.withTitle = withTitle;
		this.withGrading = withGrading;
		
		this.gtaNode = gtaNode;
		this.config = gtaNode.getModuleConfiguration();

		this.userCourseEnv = userCourseEnv;
		this.courseEnv = courseEnv;
		this.courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		
		this.assessedIdentity = assessedIdentity;
		this.assessedGroup = assessedGroup;
		
		businessGroupTask = GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE));
		
		taskList = gtaManager.createIfNotExists(courseEntry, gtaNode);
		publisherData = gtaManager.getPublisherData(courseEnv, gtaNode);
		subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode);
		
		stepPreferences = (GTAStepPreferences)ureq.getUserSession()
				.getGuiPreferences()
				.get(GTAStepPreferences.class, taskList.getKey().toString());
		if(stepPreferences == null) {
			stepPreferences = new GTAStepPreferences();
		}
		
		initContainer(ureq);
		process(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	protected abstract void initContainer(UserRequest ureq);
	
	protected final void process(UserRequest ureq) {
		Task task;
		if(businessGroupTask) {
			task = gtaManager.getTask(assessedGroup, taskList);
		} else {
			task = gtaManager.getTask(assessedIdentity, taskList);
		}
		
		if (subsContext != null) {
			contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
			listenTo(contextualSubscriptionCtr);
			mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
		}
		
		boolean assignment = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
		mainVC.contextPut("assignmentEnabled", assignment);
		if(assignment) {
			task = stepAssignment(ureq, task);
		} else if(task == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createTask(null, taskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
		}
		
		boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		mainVC.contextPut("submitEnabled", submit);
		if(submit) {
			task = stepSubmit(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.submit) {
			task = gtaManager.nextStep(task, gtaNode);
		}
		
		boolean reviewAndCorrection = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		mainVC.contextPut("reviewAndCorrectionEnabled", reviewAndCorrection);
		if(reviewAndCorrection) {
			task = stepReviewAndCorrection(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.review) {
			task = gtaManager.nextStep(task, gtaNode);
		}
		
		boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
		mainVC.contextPut("revisionEnabled", reviewAndCorrection && revision);
		if(reviewAndCorrection && revision) {
			task = stepRevision(ureq, task);
		} else if(task != null && (task.getTaskStatus() == TaskProcess.revision || task.getTaskStatus() == TaskProcess.correction)) {
			task = gtaManager.nextStep(task, gtaNode);
		}
		
		boolean solution = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
		mainVC.contextPut("solutionEnabled", solution);
		if(solution) {
			stepSolution(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.solution) {
			task = gtaManager.nextStep(task, gtaNode);
		}
		
		boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		mainVC.contextPut("gradingEnabled", grading);
		if(grading) {
			stepGrading(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.grading) {
			task = gtaManager.nextStep(task, gtaNode);
		}
		
		collapsedContents(task);
	}
	
	protected final void collapsedContents(Task currentTask) {
		TaskProcess status = null;
		TaskProcess previousStatus = null;
		if(currentTask != null) {
			status = currentTask.getTaskStatus();
			previousStatus = gtaManager.previousStep(status, gtaNode);
		}
		
		boolean assignment = Boolean.TRUE.equals(stepPreferences.getAssignement())
				|| TaskProcess.assignment.equals(status) || TaskProcess.assignment.equals(previousStatus);
		mainVC.contextPut("collapse_assignement", new Boolean(assignment));
		
		boolean submit = Boolean.TRUE.equals(stepPreferences.getSubmit())
				|| TaskProcess.submit.equals(status) || TaskProcess.submit.equals(previousStatus);
		mainVC.contextPut("collapse_submit", new Boolean(submit));
		
		boolean reviewAndCorrection = Boolean.TRUE.equals(stepPreferences.getReviewAndCorrection())
				|| TaskProcess.review.equals(status) || TaskProcess.review.equals(previousStatus);
		mainVC.contextPut("collapse_reviewAndCorrection", new Boolean(reviewAndCorrection));
		
		boolean revision = Boolean.TRUE.equals(stepPreferences.getRevision())
				|| TaskProcess.revision.equals(status) || TaskProcess.revision.equals(previousStatus)
				|| TaskProcess.correction.equals(status) || TaskProcess.correction.equals(previousStatus);
		mainVC.contextPut("collapse_revision", new Boolean(revision));
		
		boolean solution = Boolean.TRUE.equals(stepPreferences.getSolution())
				|| TaskProcess.solution.equals(status) || TaskProcess.solution.equals(previousStatus);
		mainVC.contextPut("collapse_solution", new Boolean(solution));
		
		boolean grading = Boolean.TRUE.equals(stepPreferences.getGrading())
				|| TaskProcess.grading.equals(status) || TaskProcess.grading.equals(previousStatus);
		mainVC.contextPut("collapse_grading", new Boolean(grading));
	}
	
	protected Task stepAssignment(@SuppressWarnings("unused") UserRequest ureq, Task assignedTask) {
		Date dueDate = getAssignementDueDate();
		if(dueDate != null) {
			String date = Formatter.getInstance(getLocale()).formatDateAndTime(dueDate);
			mainVC.contextPut("assignmentDueDate", date);
			
			if(assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.assignment
					&& dueDate.compareTo(new Date()) < 0) {
				//push to the next step
				assignedTask = gtaManager.nextStep(assignedTask, gtaNode);
			}
		}
		return assignedTask;
	}
	
	protected void resetDueDates() {
		assignmentDueDate = null;
		submissionDueDate = null;
		solutionDueDate = null;
	}
	
	protected Date getAssignementDueDate() {
		if(assignmentDueDate == null) {
			Date dueDate = gtaNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
			boolean relativeDate = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES);
			if(relativeDate) {
				int numOfDays = gtaNode.getModuleConfiguration().getIntegerSafe(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, -1);
				String relativeTo = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO);
				if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
					assignmentDueDate = getReferenceDate(numOfDays, relativeTo);
				}
			} else if(dueDate != null) {
				assignmentDueDate = dueDate;
			}
		}
		return assignmentDueDate;
	}
	
	protected Date getReferenceDate(int numOfDays, String relativeTo) {
		Date dueDate = null;
		if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
			GTARelativeToDates rel = GTARelativeToDates.valueOf(relativeTo);
			Date referenceDate = null;
			switch(rel) {
				case courseStart: {
					RepositoryEntryLifecycle lifecycle = courseEntry.getLifecycle();
					if(lifecycle != null && lifecycle.getValidFrom() != null) {
						referenceDate = lifecycle.getValidFrom();
					}
					break;
				}
				case courseLaunch: {
					referenceDate = userCourseInformationsManager
							.getInitialLaunchDate(courseEnv.getCourseResourceableId(), assessedIdentity);
					break;
				}
				case enrollment: {
					referenceDate = repositoryService
							.getEnrollmentDate(courseEntry, assessedIdentity);
					break;
				}
			}
			
			if(referenceDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(referenceDate);
				cal.add(Calendar.DATE, numOfDays);
				dueDate = cal.getTime();
			}
		}
		return dueDate;
	}
	
	protected Task stepSubmit(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		Date dueDate = getSubmissionDueDate();
		if(dueDate != null) {
			String date = Formatter.getInstance(getLocale()).formatDateAndTime(dueDate);
			mainVC.contextPut("submitDueDate", date);
			
			if(assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.submit
					&& dueDate.compareTo(new Date()) < 0) {
				//push to the next step
				assignedTask = gtaManager.nextStep(assignedTask, gtaNode);
			}
		}
		
		return assignedTask;
	}
	
	protected Date getSubmissionDueDate() {
		if(submissionDueDate == null) {
			Date dueDate = gtaNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE);
			boolean relativeDate = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES);
			if(relativeDate) {
				int numOfDays = gtaNode.getModuleConfiguration().getIntegerSafe(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE, -1);
				String relativeTo = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO);
				if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
					submissionDueDate = getReferenceDate(numOfDays, relativeTo);
				}
			} else if(dueDate != null) {
				submissionDueDate = dueDate;
			}
		}
		return submissionDueDate;
	}
	
	protected Task stepReviewAndCorrection(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		return assignedTask;
	}
	
	protected Task stepRevision(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		return assignedTask;
	}
	
	protected Task stepSolution(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		Date availableDate = getSolutionDueDate();
		if(availableDate != null) {
			String date = Formatter.getInstance(getLocale()).formatDateAndTime(availableDate);
			mainVC.contextPut("solutionAvailableDate", date);
		}
		return assignedTask;
	}
	
	protected Date getSolutionDueDate() {
		if(solutionDueDate == null) {
			boolean relativeDate = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES);
			Date dueDate = gtaNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
			if(relativeDate) {
				int numOfDays = gtaNode.getModuleConfiguration().getIntegerSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE, -1);
				String relativeTo = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO);
				if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
					solutionDueDate = getReferenceDate(numOfDays, relativeTo);
				}
			} else if(dueDate != null) {
				solutionDueDate = dueDate;
			}
		}
		return solutionDueDate;
	}
	
	protected Task stepGrading(@SuppressWarnings("unused") UserRequest ureq, Task assignedTask) {
		if(businessGroupTask) {
			String groupLog = courseEnv.getAuditManager().getUserNodeLog(gtaNode, assessedGroup);
			if(StringHelper.containsNonWhitespace(groupLog)) {
				mainVC.contextPut("groupLog", groupLog);
			} else {
				mainVC.contextRemove("groupLog");
			}
		} else {
			String userLog = courseEnv.getAuditManager().getUserNodeLog(gtaNode, assessedIdentity);
			if(StringHelper.containsNonWhitespace(userLog)) {
				mainVC.contextPut("userLog", userLog);
			} else {
				mainVC.contextRemove("userLog");
			}
		}
		return assignedTask;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if("show".equals(event.getCommand())) {
			doShow(ureq);
		} else if("hide".equals(event.getCommand())) {
			doHide(ureq);
		}
	}
	
	private void doShow(UserRequest ureq) {
		String step = ureq.getParameter("step");
		doSaveStepPreferences(ureq, step, Boolean.TRUE);
	}
	
	private void doHide(UserRequest ureq) {
		String step = ureq.getParameter("step");
		doSaveStepPreferences(ureq, step, Boolean.FALSE);
	}

	private void doSaveStepPreferences(UserRequest ureq, String step, Boolean showHide) {
		if(step == null) return;
		switch(step) {
			case "assignment": stepPreferences.setAssignement(showHide); break;
			case "submit": stepPreferences.setSubmit(showHide); break;
			case "reviewAndCorrection": stepPreferences.setReviewAndCorrection(showHide); break;
			case "revision": stepPreferences.setRevision(showHide); break;
			case "solution": stepPreferences.setSolution(showHide); break;
			case "grading": stepPreferences.setGrading(showHide); break;
			default: {};
		}
		
		ureq.getUserSession().getGuiPreferences()
			.putAndSave(GTAStepPreferences.class, taskList.getKey().toString(), stepPreferences);
	}
}