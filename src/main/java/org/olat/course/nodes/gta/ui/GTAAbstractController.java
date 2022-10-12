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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class GTAAbstractController extends BasicController implements GenericEventListener {
	
	protected static final long TWO_DAYS_IN_MILLISEC = 2l * 24l * 60l * 60l * 1000l;
	protected static final long ONE_DAY_IN_MILLISEC = 24l * 60l * 60l * 1000l;
	protected static final long ONE_HOUR_IN_MILLISEC = 60l * 60l * 1000l;
	
	protected VelocityContainer mainVC;

	protected Identity assessedIdentity;
	protected BusinessGroup assessedGroup;
	protected CourseEnvironment courseEnv;
	protected UserCourseEnvironment userCourseEnv;
	
	protected final TaskList taskList;
	protected final GTACourseNode gtaNode;
	protected final ModuleConfiguration config;
	protected final RepositoryEntry courseEntry;
	
	private final boolean withSubscription;
	private final PublisherData publisherData;
	private final SubscriptionContext subsContext;

	protected final boolean withTitle;
	protected final boolean withGrading;
	
	protected final boolean businessGroupTask;
	
	protected final OLATResourceable taskListEventResource;
	
	protected GTAStepPreferences stepPreferences;
	
	private DueDate assignmentDueDate;
	private DueDate submissionDueDate;
	private DueDate lateSubmissionDueDate;
	private DueDate solutionDueDate;
	
	@Autowired
	protected GTAManager gtaManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected BusinessGroupService businessGroupService;
	@Autowired
	protected UserCourseInformationsManager userCourseInformationsManager;
	@Autowired
	protected CourseModule courseModule;
	
	public GTAAbstractController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, boolean withTitle, boolean withGrading, boolean withSubscription) {
		this(ureq, wControl, gtaNode, courseEnv, null, null, null, withTitle, withGrading, withSubscription);
	}
	
	public GTAAbstractController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, UserCourseEnvironment userCourseEnv, boolean withTitle, boolean withGrading, boolean withSubscription) {
		this(ureq, wControl, gtaNode, courseEnv, userCourseEnv, null, null, withTitle, withGrading, withSubscription);
	}

	public GTAAbstractController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, UserCourseEnvironment userCourseEnv,
			BusinessGroup assessedGroup, Identity assessedIdentity, boolean withTitle, boolean withGrading, boolean withSubscription) {
		super(ureq, wControl);
		
		this.withTitle = withTitle;
		this.withGrading = withGrading;
		this.withSubscription = withSubscription;
		
		this.gtaNode = gtaNode;
		this.config = gtaNode.getModuleConfiguration();

		this.userCourseEnv = userCourseEnv;
		this.courseEnv = courseEnv;
		this.courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		if (userCourseEnv == null && assessedIdentity != null) {
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(assessedIdentity);
			this.userCourseEnv = new UserCourseEnvironmentImpl(ienv, courseEnv);
		}
		
		this.assessedIdentity = assessedIdentity;
		this.assessedGroup = assessedGroup;
		
		businessGroupTask = GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE));
		
		taskList = gtaManager.createIfNotExists(courseEntry, gtaNode);
		publisherData = gtaManager.getPublisherData(courseEnv, gtaNode, false);
		subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode, false);
		
		stepPreferences = (GTAStepPreferences)ureq.getUserSession()
				.getGuiPreferences()
				.get(GTAStepPreferences.class, taskList.getKey().toString());
		if(stepPreferences == null) {
			stepPreferences = new GTAStepPreferences();
		}
		
		taskListEventResource = OresHelper.createOLATResourceableInstance("GTaskList", taskList.getKey());
		CoordinatorManager.getInstance().getCoordinator()
			.getEventBus().registerFor(this, getIdentity(), taskListEventResource);
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator()
			.getEventBus().deregisterFor(this, taskListEventResource);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof TaskMultiUserEvent) {
			TaskMultiUserEvent ste = (TaskMultiUserEvent)event;
			if(!getIdentity().getKey().equals(ste.getEmitterKey())
					&& ((assessedGroup != null && assessedGroup.getKey().equals(ste.getForGroupKey()))
							|| (assessedIdentity != null && assessedIdentity.getKey().equals(ste.getForIdentityKey())))) {
				processEvent(ste);
			}
		}
	}
	
	protected abstract void processEvent(TaskMultiUserEvent event);

	protected abstract void initContainer(UserRequest ureq);
	
	protected final void process(UserRequest ureq) {
		Task task;
		if(businessGroupTask) {
			task = gtaManager.getTask(assessedGroup, taskList);
		} else {
			task = gtaManager.getTask(assessedIdentity, taskList);
		}
		List<TaskRevision> taskRevisions = gtaManager.getTaskRevisions(task);
		
		if (withSubscription && subsContext != null) {
			ContextualSubscriptionController contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
			listenTo(contextualSubscriptionCtr);
			mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
		}
		
		boolean optional = gtaNode.isOptional(courseEnv, userCourseEnv);
		boolean assignment = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
		mainVC.contextPut("assignmentEnabled", assignment);
		if(assignment) {
			task = stepAssignment(ureq, task);
		} else if(task == null && !optional) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createTask(null, taskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
		}
		
		boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		mainVC.contextPut("submitEnabled", submit);
		if(submit) {
			task = stepSubmit(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.submit) {
			task = gtaManager.nextStep(task, gtaNode, false, null, Role.auto);
		}
		
		boolean reviewAndCorrection = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		mainVC.contextPut("reviewAndCorrectionEnabled", reviewAndCorrection);
		if(reviewAndCorrection) {
			task = stepReviewAndCorrection(ureq, task, taskRevisions);
		} else if(task != null && task.getTaskStatus() == TaskProcess.review) {
			task = gtaManager.nextStep(task, gtaNode, false, null, Role.auto);
		}
		
		boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
		mainVC.contextPut("revisionEnabled", reviewAndCorrection && revision);
		if(reviewAndCorrection && revision) {
			task = stepRevision(ureq, task, taskRevisions);
		} else if(task != null && (task.getTaskStatus() == TaskProcess.revision || task.getTaskStatus() == TaskProcess.correction)) {
			task = gtaManager.nextStep(task, gtaNode, false, null, Role.auto);
		}
		
		boolean solution = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
		mainVC.contextPut("solutionEnabled", solution);
		if(solution) {
			stepSolution(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.solution) {
			task = gtaManager.nextStep(task, gtaNode, false, null, Role.auto);
		}
		
		boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		mainVC.contextPut("gradingEnabled", grading);
		if(grading) {
			stepGrading(ureq, task);
		} else if(task != null && task.getTaskStatus() == TaskProcess.grading) {
			task = gtaManager.nextStep(task, gtaNode, false, null, Role.auto);
		}
		
		mainVC.contextPut("changelogconfig", courseModule.isDisplayChangeLog());
		
		resetTask(ureq, task);
		
		nodeLog(task);
		collapsedContents(task);
		
		if(userCourseEnv != null && getIdentity().equals(userCourseEnv.getIdentityEnvironment().getIdentity())) {
			// recalculate potential changes in status
			userCourseEnv.getScoreAccounting().evaluateAll();
		}
	}
	
	protected abstract void resetTask(UserRequest ureq, Task task);
	
	protected final void collapsedContents(Task currentTask) {
		TaskProcess status = null;
		TaskProcess previousStatus = null;
		if(currentTask != null) {
			status = currentTask.getTaskStatus();
			previousStatus = gtaManager.previousStep(status, gtaNode);
		}
		
		boolean assignment = Boolean.TRUE.equals(stepPreferences.getAssignement())
				|| TaskProcess.assignment.equals(status) || TaskProcess.assignment.equals(previousStatus);
		mainVC.contextPut("collapse_assignement", Boolean.valueOf(assignment));
		
		boolean submit = Boolean.TRUE.equals(stepPreferences.getSubmit())
				|| TaskProcess.submit.equals(status) || TaskProcess.submit.equals(previousStatus);
		mainVC.contextPut("collapse_submit", Boolean.valueOf(submit));
		
		boolean reviewAndCorrection = Boolean.TRUE.equals(stepPreferences.getReviewAndCorrection())
				|| TaskProcess.review.equals(status) || TaskProcess.review.equals(previousStatus);
		mainVC.contextPut("collapse_reviewAndCorrection", Boolean.valueOf(reviewAndCorrection));
		
		boolean revision = Boolean.TRUE.equals(stepPreferences.getRevision())
				|| TaskProcess.revision.equals(status) || TaskProcess.revision.equals(previousStatus)
				|| TaskProcess.correction.equals(status) || TaskProcess.correction.equals(previousStatus);
		mainVC.contextPut("collapse_revision", Boolean.valueOf(revision));
		
		boolean solution = Boolean.TRUE.equals(stepPreferences.getSolution())
				|| TaskProcess.solution.equals(status) || TaskProcess.solution.equals(previousStatus);
		mainVC.contextPut("collapse_solution", Boolean.valueOf(solution));
		
		boolean grading = Boolean.TRUE.equals(stepPreferences.getGrading())
				|| TaskProcess.grading.equals(status) || TaskProcess.grading.equals(previousStatus);
		mainVC.contextPut("collapse_grading", Boolean.valueOf(grading));
	}
	
	protected Task stepAssignment(UserRequest ureq, Task assignedTask) {
		DueDate dueDate = getAssignementDueDate(assignedTask);
		if(dueDate != null) {
			if(dueDate.getDueDate() != null) {
				Date date = dueDate.getDueDate();
				boolean done = isDone(assignedTask, TaskProcess.assignment);
				DueDateValues dueDateValues = formatDueDate(dueDate, null, ureq.getRequestTimestamp(), done, true);
				mainVC.contextPut("assignmentDueDate", dueDateValues.asString());
				mainVC.contextPut("assignmentRemainingTime", Long.toString(dueDateValues.remainingTime()));
				
				mainVC.contextRemove("assignmentDueDateMsg");
				// need an instantiated to go further (import for optional tasks)
				if(assignedTask != null && StringHelper.containsNonWhitespace(assignedTask.getTaskName())
						&& assignedTask.getTaskStatus() == TaskProcess.assignment && date.compareTo(new Date()) < 0) {
					//push to the next step if the task is blocked in assignment (it's a security)
					assignedTask = gtaManager.nextStep(assignedTask, gtaNode, false, null, Role.auto);
				}
			} else if(dueDate.getMessageKey() != null) {
				mainVC.contextPut("assignmentDueDateMsg", translate(dueDate.getMessageKey(), dueDate.getMessageArg()));
				mainVC.contextRemove("assignmentDueDate");
			}
		}
		
		if(assignedTask != null && assignedTask.getAssignmentDate() != null) {
			String date = Formatter.getInstance(getLocale()).formatDateAndTime(assignedTask.getAssignmentDate());
			mainVC.contextPut("assignmentDate", translate("msg.assignment.date", date));
		} else {
			mainVC.contextRemove("assignmentDate");
		}
		
		return assignedTask;
	}
	
	protected final boolean isDone(Task assignedTask, TaskProcess status) {
		if(assignedTask == null || assignedTask.getTaskStatus() == null) return false;
		return status.ordinal() < assignedTask.getTaskStatus().ordinal();
	}
	
	protected final void setDoneStatusAndCssClass(String stepPrefix) {
		setDoneStatusAndCssClass(stepPrefix, "msg.status.done");
	}
	
	protected final void setDoneStatusAndCssClass(String stepPrefix, String i18nKey) {
		setStatusAndCssClass(stepPrefix, "o_done", "o_process_status_done", i18nKey);
	}
	
	protected final void setActiveStatusAndCssClass(String stepPrefix) {
		setActiveStatusAndCssClass(stepPrefix, "msg.status.active");
	}
	
	protected final void setActiveStatusAndCssClass(String stepPrefix, String i18nKey) {
		setStatusAndCssClass(stepPrefix, "o_active", "o_process_status_active", i18nKey);
	}
	
	protected final void setNotAvailableStatusAndCssClass(String stepPrefix) {
		setStatusAndCssClass(stepPrefix, "o_notavailable", "o_process_status_notavailable", "msg.status.not.available");
	}
	
	protected final void setExpiredStatusAndCssClass(String stepPrefix) {
		setStatusAndCssClass(stepPrefix, "o_expired", "o_process_status_expired", "msg.status.expired");
	}

	protected final void setWaitingStatusAndCssClass(String stepPrefix) {
		setStatusAndCssClass(stepPrefix, "o_active", "o_process_status_waiting", "msg.status.waiting");
	}
	
	protected final void setReviewStatusAndCssClass(String stepPrefix) {
		setStatusAndCssClass(stepPrefix, "o_active", "o_process_status_review", "msg.status.review");
	}
	
	protected final void setStatusAndCssClass(String stepPrefix, String stepCssClass, String statusCssClass, String statusI18nKey) {
		mainVC.contextPut(stepPrefix.concat("CssClass"), stepCssClass);
		mainVC.contextPut(stepPrefix.concat("CssStatus"), statusCssClass);
		mainVC.contextPut(stepPrefix.concat("Status"), translate(statusI18nKey));
	}

	protected abstract DueDateValues formatDueDate(DueDate dueDate, DueDate lateDueDate, Date now, boolean done, boolean userDeadLine);
	
	protected boolean isDateOnly(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
	}
	
	protected DueDateArguments formatDueDateArguments(Date date, Date now, boolean late, boolean countDown, boolean userDeadLine) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		boolean dateOnly = (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
		if(dateOnly && userDeadLine) {
			cal.add(Calendar.DATE, -1);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			date = cal.getTime();
		}

		long timeDiffMilliSec = Math.abs(date.getTime() - now.getTime());
		long days = Math.abs(DateUtils.countDays(date, now));
		long diffDays = timeDiffMilliSec / ONE_DAY_IN_MILLISEC;
		long hours = ((timeDiffMilliSec - (diffDays * ONE_DAY_IN_MILLISEC)) / ONE_HOUR_IN_MILLISEC) % 24;
		long minutes = ((timeDiffMilliSec - (diffDays * ONE_DAY_IN_MILLISEC) - (hours * ONE_HOUR_IN_MILLISEC)) / (60l * 1000l)) % 60;

		Formatter formatter = Formatter.getInstance(getLocale());
		String suffix = (late ? "_late" : "");
		
		String hoursString = Long.toString(hours);
		String minutesString = Long.toString(minutes);
		if(countDown) {
			hoursString = "<span class='o_hours" + suffix + "'>" + hoursString + "</span>";
			minutesString = "<span class='o_minutes" + suffix + "'>" + minutesString + "</span>";
		}

		String[] args = new String[] {
			Long.toString(days),				// 0 Number of days
			hoursString,						// 1 Number of hours
			minutesString,						// 2 Number of minutes
			formatter.dayOfWeekName(date),		// 3 End day
			formatter.formatDate(date),			// 4 Date
			formatter.formatTimeShort(date)		// 5 Time
		};
		return new DueDateArguments(days, args, timeDiffMilliSec);
	}
	
	protected String[] mergeArguments(String[] args1, String[] args2) {
		String[] result = Arrays.copyOf(args1, args1.length + args2.length);
	    System.arraycopy(args2, 0, result, args1.length, args2.length);
	    return result;
	}
	
	protected void resetDueDates() {
		assignmentDueDate = null;
		submissionDueDate = null;
		solutionDueDate = null;
	}
	
	protected DueDate getAssignementDueDate(Task task) {
		if(assignmentDueDate == null) {
			assignmentDueDate = gtaManager.getAssignmentDueDate(task, assessedIdentity, assessedGroup, gtaNode, courseEntry, true);
		}
		return assignmentDueDate;
	}
	
	protected Task stepSubmit(UserRequest ureq, Task assignedTask) {
		DueDate dueDate = getSubmissionDueDate(assignedTask);
		DueDate lateDueDate = getLateSubmissionDueDate(assignedTask);
		
		if(dueDate != null) {
			if(dueDate.getDueDate() != null) {
				Date deadline = gtaManager.getDeadlineOf(dueDate, lateDueDate);
				boolean done = isDone(assignedTask, TaskProcess.submit);
				DueDateValues dueDateValues = formatDueDate(dueDate, lateDueDate, ureq.getRequestTimestamp(), done, true);
				mainVC.contextPut("submitRemainingTime", Long.toString(dueDateValues.remainingTime()));
				if(lateDueDate != null && lateDueDate.getDueDate() != null) {
					mainVC.contextPut("lateSubmitRemainingTime", Long.toString(dueDateValues.lateRemainingTime()));
				}
				mainVC.contextPut("submitDueDate", dueDateValues.asString());
				
				mainVC.contextRemove("submitDueDateMsg");
				// need an instantiated to go further (import for optional tasks)
				if(assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.submit
						&& deadline.compareTo(new Date()) < 0) {
					//push to the next step
					int numOfDocs = getNumberOfSubmittedDocuments();
					if (numOfDocs > 0 || getDoer() != Role.coach) {
						assignedTask = gtaManager.submitTask(assignedTask, gtaNode, numOfDocs, null, Role.auto);
						if (numOfDocs > 0) {
							showInfo("info.task.collected");
						}
					}
				}
			} else if(dueDate.getMessageKey() != null) {
				mainVC.contextPut("submitDueDateMsg", translate(dueDate.getMessageKey(), dueDate.getMessageArg()));
				mainVC.contextRemove("submitDueDate");
			}
		}
		
		if(assignedTask != null) {
			if(assignedTask.getCollectionDate() != null) {
				String date = Formatter.getInstance(getLocale()).formatDateAndTime(assignedTask.getCollectionDate());
				mainVC.contextPut("collectionDate", translate("msg.collection.date", date));
				mainVC.contextRemove("submissionDate");
			 } else if(assignedTask.getSubmissionDate() != null && assignedTask.getSubmissionDoerRole() == Role.auto) {
				String date = Formatter.getInstance(getLocale()).formatDateAndTime(assignedTask.getSubmissionDate());
				mainVC.contextPut("collectionDate", translate("msg.collection.date.auto", date));
				mainVC.contextRemove("submissionDate");
			 } else if(assignedTask.getSubmissionDate() != null) {
				SubmissionDateInfos submissionDateText = formatSubmissionDateMessage(assignedTask, dueDate, lateDueDate);
				mainVC.contextPut("submissionDate", submissionDateText);
				mainVC.contextRemove("collectionDate");
			} else {
				mainVC.contextRemove("submissionDate");
				mainVC.contextRemove("collectionDate");
			}
		} else {
			mainVC.contextRemove("submissionDate");
			mainVC.contextRemove("collectionDate");
		}
		
		return assignedTask;
	}
	
	protected SubmissionDateInfos formatSubmissionDateMessage(Task assignedTask, DueDate dueDate, DueDate lateDueDate) {
		Date submissionDate = assignedTask.getSubmissionDate();
		String date = Formatter.getInstance(getLocale()).formatDateAndTime(submissionDate);
		if(dueDate == null || dueDate.getReferenceDueDate() == null || submissionDate.before(dueDate.getReferenceDueDate())) {
			return new SubmissionDateInfos(translate("msg.submission.date", date), "o_icon_status_done", "");
		}
		if(assignedTask.getSubmissionDueDate() != null) {
			return new SubmissionDateInfos(translate("msg.extended.submission.date", date), "o_icon_description", "o_process_status_extended");
		}
		if(lateDueDate != null && lateDueDate.getReferenceDueDate() != null && submissionDate.after(dueDate.getReferenceDueDate())) {
			long lateInMilliSec = submissionDate.getTime() - dueDate.getReferenceDueDate().getTime();
			String duration = Formatter.formatDuration(lateInMilliSec);
			return new SubmissionDateInfos(translate("msg.late.submission.date", date, duration), "o_icon_description", "o_process_status_late");
		}

		return new SubmissionDateInfos(translate("msg.submission.date", date), "o_icon_status_done", "");
	}
	
	protected DueDate getSubmissionDueDate(Task assignedTask) {
		if(submissionDueDate == null) {
			submissionDueDate = gtaManager.getSubmissionDueDate(assignedTask, assessedIdentity, assessedGroup, gtaNode, courseEntry, true);
		}
		return submissionDueDate;
	}
	
	protected DueDate getLateSubmissionDueDate(Task assignedTask) {
		if(lateSubmissionDueDate == null) {
			lateSubmissionDueDate = gtaManager.getLateSubmissionDueDate(assignedTask, assessedIdentity, assessedGroup, gtaNode, courseEntry, true);
		}
		return lateSubmissionDueDate;
	}
	
	protected int getNumberOfSubmittedDocuments() {
		File[] submittedDocuments;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));

		} else {
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		}
		return submittedDocuments == null ? 0 : submittedDocuments.length;
	}
	
	protected Task stepReviewAndCorrection(@SuppressWarnings("unused")UserRequest ureq,
			Task assignedTask, @SuppressWarnings("unused")List<TaskRevision> taskRevisions) {
		return assignedTask;
	}
	
	protected Task stepRevision(UserRequest ureq, Task assignedTask,
			@SuppressWarnings("unused") List<TaskRevision> taskRevisions) {
		// need an instantiated to go further (import for optional tasks)
		if(assignedTask != null && assignedTask.getRevisionsDueDate() != null) {
			Date date =  assignedTask.getRevisionsDueDate();
			DueDate dueDate = new DueDate(false, date, null, date);
			boolean done = isDone(assignedTask, TaskProcess.revision);
			DueDateValues dueDateValues = formatDueDate(dueDate, null, ureq.getRequestTimestamp(), done, true);
			mainVC.contextPut("revisionDueDate", dueDateValues.asString());
			mainVC.contextPut("revisionRemainingTime", Long.toString(dueDateValues.remainingTime()));
			
			if(assignedTask.getTaskStatus() == TaskProcess.revision
					&& date.compareTo(new Date()) < 0) {
				//push to the next step
				int numOfDocs = getNumberOfRevisionDocuments(assignedTask);
				assignedTask = gtaManager.submitRevisions(assignedTask, gtaNode, numOfDocs, null, Role.auto);
			}
		}
		
		if(assignedTask != null) {
			TaskProcess process = assignedTask.getTaskStatus();
			if(process == TaskProcess.revision) {
				mainVC.contextRemove("submissionRevisionDate");
				mainVC.contextRemove("collectionRevisionDate");
			} else if(assignedTask.getCollectionRevisionsDate() != null) {
				String date = Formatter.getInstance(getLocale()).formatDateAndTime(assignedTask.getCollectionRevisionsDate());
				mainVC.contextPut("collectionRevisionDate", translate("msg.collection.date", date));
				mainVC.contextRemove("submissionRevisionDate");
			} else if(assignedTask.getSubmissionRevisionsDate() != null && assignedTask.getSubmissionRevisionsDoerRole() == Role.auto) {
				String date = Formatter.getInstance(getLocale()).formatDateAndTime(assignedTask.getSubmissionRevisionsDate());
				mainVC.contextPut("collectionRevisionDate", translate("msg.collection.date.auto", date));
				mainVC.contextRemove("submissionRevisionDate");
			} else if(assignedTask.getSubmissionRevisionsDate() != null) {
				String date = Formatter.getInstance(getLocale()).formatDateAndTime(assignedTask.getSubmissionRevisionsDate());
				mainVC.contextPut("submissionRevisionDate", translate("msg.revision.date", date));
				mainVC.contextRemove("collectionRevisionDate");
			}
		} else {
			mainVC.contextRemove("submissionRevisionDate");
			mainVC.contextRemove("collectionRevisionDate");
		}
		
		return assignedTask;
	}
	
	protected int getNumberOfRevisionDocuments(Task assignedTask) {
		File[] submittedDocuments;
		int iteration = assignedTask.getRevisionLoop();
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			File documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		} else {
			File documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, assessedIdentity);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		}
		return submittedDocuments == null ? 0 : submittedDocuments.length;
	}
	
	protected Task stepSolution(UserRequest ureq, Task assignedTask) {
		DueDate availableDate = getSolutionDueDate(assignedTask);
		if(availableDate != null) {
			if(availableDate.getDueDate() != null) {
				String date = formatSolutionDueDate(availableDate, ureq.getRequestTimestamp());
				mainVC.contextPut("solutionAvailableDate", date);
				mainVC.contextRemove("solutionAvailableDateMsg");
			} else if(availableDate.getMessageKey() != null) {
				mainVC.contextPut("solutionAvailableDateMsg", translate(availableDate.getMessageKey(), availableDate.getMessageArg()));
				mainVC.contextRemove("solutionAvailableDate");
			}
		}
		return assignedTask;
	}
	
	protected DueDate getSolutionDueDate(Task assignedTask) {
		if(solutionDueDate == null) {
			solutionDueDate = gtaManager.getSolutionDueDate(assignedTask, assessedIdentity, assessedGroup, gtaNode, courseEntry, true);
		}
		return solutionDueDate;
	}
	
	protected final boolean isSolutionVisible(UserRequest ureq, Task assignedTask) {
		DueDate availableDate = getSolutionDueDate(assignedTask);
		boolean visible = availableDate == null || 
				(availableDate.getDueDate() != null && availableDate.getDueDate().compareTo(ureq.getRequestTimestamp()) <= 0);

		File documentsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		return visible && TaskHelper.hasDocuments(documentsDir);
	}
	
	/**
	 * If the due date is not defined, the solutions are show the users with an uploaded
	 * solution or if the configuration is set to visible to all. If the due date is set
	 * but is not a relative date, the solution is shown to the users which uploaded a
	 * solution or if the configuration is set to visible to all.
	 * 
	 * @param availableDate The due date of the solutions (can be null)
	 * @return If the solutions are visible to the user
	 */
	protected final boolean showSolutions(DueDate availableDate) {
		boolean show = false;
		boolean optional = gtaNode.isOptional(courseEnv, userCourseEnv);
		if(config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			File submitDirectory;
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				submitDirectory = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			} else {
				submitDirectory = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
			}
			
			if(availableDate == null && !optional) {
				show = true;
			} else if(availableDate == null && optional
					&& (gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false) || TaskHelper.hasDocuments(submitDirectory))) {
				show = true;
			} else if(availableDate != null && (optional || !availableDate.isRelative())
					&& (gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false) || TaskHelper.hasDocuments(submitDirectory))) {
				show = true;
			}
		} else if(optional) {
			show = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false);
		} else {
			show = true;
		}
		return show;
	}
	
	protected String formatSolutionDueDate(DueDate dueDate, Date now) {
		Date date = dueDate.getDueDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		boolean dateOnly = (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
		DueDateArguments ddArgs = formatDueDateArguments(date, now, false, true, false);
		
		String i18nKey;
		if(now.before(date)) {
			i18nKey = dateOnly ? "msg.solution.from.dateonly" : "msg.solution.from";
		} else {
			i18nKey = dateOnly ? "msg.solution.view.dateonly" : "msg.solution.view";
		}
		return translate(i18nKey, ddArgs.args());
	}
	
	protected Task stepGrading(@SuppressWarnings("unused") UserRequest ureq, Task assignedTask) {
		return assignedTask;
	}
	
	protected void nodeLog(@SuppressWarnings("unused") Task assignedTask) {
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
				mainVC.contextPut("userLog", StringHelper.escapeHtml(userLog));
			} else {
				mainVC.contextRemove("userLog");
			}
		}
	}
	
	protected static final TaskRevision getTaskRevision(List<TaskRevision> revisionList, TaskProcess status, int revisionLoop) {
		TaskRevision revision = null;
		if(revisionList != null && !revisionList.isEmpty()) {
			for(TaskRevision taskRevision:revisionList) {
				if(status.equals(taskRevision.getTaskStatus()) && revisionLoop == taskRevision.getRevisionLoop()) {
					revision = taskRevision;
				}	
			}
		}
		return revision;
	}
	
	protected UserCourseEnvironment getAssessedUserCourseEnvironment() {
		if(userCourseEnv == null) {
			ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
			userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		}
		return userCourseEnv;
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
			case "assignment":
				stepPreferences.setAssignement(showHide);
				mainVC.contextPut("collapse_assignement", showHide);
				break;
			case "submit":
				stepPreferences.setSubmit(showHide);
				mainVC.contextPut("collapse_submit", showHide);
				break;
			case "reviewAndCorrection":
				stepPreferences.setReviewAndCorrection(showHide);
				mainVC.contextPut("collapse_reviewAndCorrection", showHide);
				break;
			case "revision":
				stepPreferences.setRevision(showHide);
				mainVC.contextPut("collapse_revision", showHide);
				break;
			case "solution":
				stepPreferences.setSolution(showHide);
				mainVC.contextPut("collapse_solution", showHide);
				break;
			case "grading":
				stepPreferences.setGrading(showHide);
				mainVC.contextPut("collapse_grading", showHide);
				break;
			default: {}
		}
		
		ureq.getUserSession().getGuiPreferences()
			.commit(GTAStepPreferences.class, taskList.getKey().toString(), stepPreferences);
	}
	
	protected abstract Role getDoer();
	
	public static class SubmissionDateInfos {
		
		private final String message;
		private final String iconCssClass;
		private final String cssClass;
		
		public SubmissionDateInfos(String message, String iconCssClass, String cssClass) {
			this.message = message;
			this.iconCssClass = iconCssClass;
			this.cssClass = cssClass;
		}

		public String getMessage() {
			return message;
		}

		public String getIconCssClass() {
			return iconCssClass;
		}

		public String getCssClass() {
			return cssClass;
		}
	}
	
	protected static final class DueDateValues {
		private final String dueDateString;
		private final long remainingTime;
		private final long lateRemainingTime;
		
		public DueDateValues(String dueDateString, long remainingTime, long lateRemainingTime) {
			this.dueDateString = dueDateString;
			this.remainingTime = remainingTime;
			this.lateRemainingTime = lateRemainingTime;
		}
		
		public String asString() {
			return dueDateString;
		}
		
		public long remainingTime() {
			return remainingTime;
		}
		
		public long lateRemainingTime() {
			return lateRemainingTime;
		}
	}
	
	protected static final class DueDateArguments {
		
		private final long days;
		private final String[] args;
		private final long timeDiffMilliSec;
		
		public DueDateArguments(long days, String[] args, long timeDiffMilliSec) {
			this.days = days;
			this.args = args;
			this.timeDiffMilliSec = timeDiffMilliSec;
		}
		
		public long days() {
			return days;
		}
		
		public String[] args() {
			return args;
		}
		
		public long timeDiffInMillSeconds() {
			return timeDiffMilliSec;
		}
	}
}