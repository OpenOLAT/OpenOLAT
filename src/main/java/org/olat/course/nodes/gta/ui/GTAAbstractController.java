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

import java.util.Date;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
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
	
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	@Autowired
	protected GTAManager gtaManager;
	
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
		
		taskList = gtaManager.getTaskList(courseEntry, gtaNode);
		publisherData = gtaManager.getPublisherData(courseEnv, gtaNode);
		subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode);
		
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
		}
		
		boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		mainVC.contextPut("submitEnabled", submit);
		if(submit) {
			task = stepSubmit(ureq, task);
		}
		
		boolean reviewAndCorrection = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		mainVC.contextPut("reviewAndCorrectionEnabled", reviewAndCorrection);
		if(reviewAndCorrection) {
			task = stepReviewAndCorrection(ureq, task);
		}
		
		boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
		mainVC.contextPut("revisionEnabled", revision);
		if(revision) {
			task = stepRevision(ureq, task);
		}
		
		boolean solution = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
		mainVC.contextPut("solutionEnabled", solution);
		if(solution) {
			stepSolution(ureq, task);
		}
		
		boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		mainVC.contextPut("gradingEnabled", grading);
		if(grading) {
			stepGrading(ureq, task);
		}
	}
	
	protected Task stepAssignment(@SuppressWarnings("unused") UserRequest ureq, Task assignedTask) {
		Date dueDate = gtaNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
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
	
	protected Task stepSubmit(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		Date dueDate = gtaNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE);
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
	
	protected Task stepReviewAndCorrection(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		return assignedTask;
	}
	
	protected Task stepRevision(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		return assignedTask;
	}
	
	protected Task stepSolution(@SuppressWarnings("unused")UserRequest ureq, Task assignedTask) {
		Date availableDate = gtaNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		if(availableDate != null) {
			String date = Formatter.getInstance(getLocale()).formatDateAndTime(availableDate);
			mainVC.contextPut("solutionAvailableDate", date);
		}
		return assignedTask;
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
}