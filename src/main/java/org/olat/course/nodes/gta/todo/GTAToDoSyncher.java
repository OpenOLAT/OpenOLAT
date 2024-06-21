/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.gta.todo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRef;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.ui.GTAUIFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.course.todo.CourseNodeToDoSyncher;
import org.olat.course.todo.CourseToDoEnvironment;
import org.olat.course.todo.ui.CourseToDoUIFactory;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 19 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTAToDoSyncher implements CourseNodeToDoSyncher {

	private static final Logger log = Tracing.createLoggerFor(GTAToDoSyncher.class);

	private static final List<TaskProcess> OPEN_ASSIGNMENT = Arrays.stream(TaskProcess.values()).takeWhile(p -> p != TaskProcess.submit).toList();
	private static final List<TaskProcess> OPEN_SUBMIT = Arrays.stream(TaskProcess.values()).takeWhile(p -> p != TaskProcess.review).toList();
	//TODO task
	private static final List<TaskProcess> OPEN_REVISION = List.of(TaskProcess.revision);
	
	private final GTAManager gtaManager;
	private final DueDateService dueDateService;
	private final I18nManager i18nManager;
	private final Set<Identity> identities;
	private Map<Long, TaskLight> identityToTask;
	private Map<Long, TaskRevision> identityToRevision;
	private Map<Long, Date> identityToAssignmentDueDate;
	private Map<Long, Date> identityToSubmitDueDate;

	public GTAToDoSyncher(GTAManager gtaManager, DueDateService dueDateService, I18nManager i18nManager, Set<Identity> identities) {
		this.gtaManager = gtaManager;
		this.dueDateService = dueDateService;
		this.i18nManager = i18nManager;
		this.identities = identities;
	}
	
	@Override
	public void reset() {
		identityToTask = null;
		identityToRevision = null;
		identityToAssignmentDueDate = null;
		identityToSubmitDueDate = null;
	}

	@Override
	public void synch(CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseToDoEnvironment toDoEnv) {
		boolean prevToDoDone = syncAssignmentToDo(courseNode, userCourseEnv, toDoEnv);
		prevToDoDone = syncSubmitToDo(courseNode, userCourseEnv, toDoEnv, prevToDoDone);
		prevToDoDone = syncRevisionToDo(courseNode, userCourseEnv, toDoEnv, prevToDoDone);
	}
	
	private boolean syncAssignmentToDo(CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseToDoEnvironment toDoEnv) {
		boolean stepEnabled = courseNode.getModuleConfiguration().getBooleanSafe(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED)
				&& isSynchAssignmentEnabled(courseNode);
		DueDateConfig dueDateConfig = ((GTACourseNode)courseNode).getDueDateConfig(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		
		return syncStepToDo(courseNode, userCourseEnv, toDoEnv, stepEnabled, false, true, OPEN_ASSIGNMENT, dueDateConfig,
				GTAAssignmentToDoProvider.TYPE, "todo.assignment.title", null, task -> getAssignmentDueDate(courseNode, userCourseEnv, dueDateConfig));
	}

	public static final boolean isSynchAssignmentEnabled(CourseNode courseNode) {
		return courseNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
	}
	
	private boolean syncSubmitToDo(CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseToDoEnvironment toDoEnv, boolean prevToDoDone) {
		boolean stepEnabled = courseNode.getModuleConfiguration().getBooleanSafe(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED)
				&& isSynchSubmitEnabled(courseNode);
		DueDateConfig dueDateConfig = ((GTACourseNode)courseNode).getDueDateConfig(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		boolean needsPrevStep = isSynchAssignmentEnabled(courseNode);
		
		return syncStepToDo(courseNode, userCourseEnv, toDoEnv, stepEnabled, needsPrevStep, prevToDoDone, OPEN_SUBMIT, dueDateConfig,
				GTASubmitToDoProvider.TYPE, "todo.submit.title", null, task -> getSubmitDueDate(courseNode, userCourseEnv, dueDateConfig));
	}

	public static final boolean isSynchSubmitEnabled(CourseNode courseNode) {
		return courseNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
	}
	
	private boolean syncRevisionToDo(CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseToDoEnvironment toDoEnv, boolean prevToDoDone) {
		boolean stepEnabled = courseNode.getModuleConfiguration().getBooleanSafe(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED)
				&& isSynchRevisionEnabled(courseNode);
		
		return syncStepToDo(courseNode, userCourseEnv, toDoEnv, stepEnabled, true, prevToDoDone, OPEN_REVISION,
				DueDateConfig.noDueDateConfig(), GTARevisionToDoProvider.TYPE, "todo.revision.title", "todo.revision.desc",
				task -> (task != null ? task.getRevisionsDueDate() : null));
	}

	public static final boolean isSynchRevisionEnabled(CourseNode courseNode) {
		return courseNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				&& courseNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				&& (isSynchAssignmentEnabled(courseNode) || isSynchSubmitEnabled(courseNode));
	}

	/**
	 * 
	 * @return if the step is done and the task is ready for the to-do of the next step
	 */
	private boolean syncStepToDo(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			CourseToDoEnvironment toDoEnv, boolean stepEnabled, boolean needsPrevStep, boolean prevToDoDone,
			List<TaskProcess> stepsOpen, DueDateConfig dueDateConfig, String providerType, String titleKey,
			String descKey, Function<TaskRef, Date> toDoDueDate) {
		
		boolean contextValid = isContextValid(courseNode, userCourseEnv, toDoEnv, dueDateConfig, providerType);
		ToDoTask toDoTask = toDoEnv.getToDoTask(userCourseEnv, courseNode, providerType);
		ToDoStatus toDoStatus = null;
		
		// Sync the task with the to-do
		if (stepEnabled) {
			// Context is not valid yet and user has no to-do yet. No to-do needed.
			if (toDoTask == null && !contextValid) {
				return false;
			}
			
			if (toDoTask == null && !prevToDoDone) {
				return false;
			}
			
			TaskRef task = getTask(courseNode, userCourseEnv);
			if (toDoTask == null && task == null && needsPrevStep) {
				return false;
			}
			
			
			// User has step done, but no to-do. No to-do needed.
			if (toDoTask == null && task != null && !stepsOpen.contains(task.getTaskStatus())) {
				return true;
			}
			
			// User has to do the step, but has no to-do yet. => Create a to-do.
			if (toDoTask == null) {
				toDoTask = toDoEnv.createToDoTask(userCourseEnv, courseNode, providerType,
						getTitle(courseNode, userCourseEnv, getUserTranslator(userCourseEnv), titleKey));
			}
			
			if (!prevToDoDone) {
				toDoStatus = ToDoStatus.deleted;
			} else if (task == null || stepsOpen.contains(task.getTaskStatus())) {
				toDoStatus = ToDoStatus.open;
			} else {
				toDoStatus = ToDoStatus.done;
			}
		} else if (toDoTask != null) {
			toDoStatus = ToDoStatus.deleted;
		}
		
		if (toDoTask != null) {
			// Sync the to-do content
			Translator translator = getUserTranslator(userCourseEnv);
			String title = getTitle(courseNode, userCourseEnv, translator, titleKey);
			String description = getDescription(courseNode, userCourseEnv, translator, descKey);
			Date dueDate = toDoDueDate.apply(getTask(courseNode, userCourseEnv));
			String originTitle = CourseToDoUIFactory.getOriginTitle(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
			String originSubTitle = CourseToDoUIFactory.getOriginSubTitle(courseNode);
			toDoEnv.updateToDoTask(toDoTask, title, description, toDoStatus, dueDate, originTitle, originSubTitle);
			
			// Sync the status of context
			if (contextValid && toDoTask.isOriginDeleted()) {
				toDoEnv.updateOriginDeleted(toDoTask, false);
			} else if (!contextValid && !toDoTask.isOriginDeleted()) {
				toDoEnv.updateOriginDeleted(toDoTask, true);
			}
			
			if (toDoTask.isOriginDeleted()) {
				return false;
			} else if (ToDoStatus.done == toDoStatus) {
				return true;
			} else if (ToDoStatus.deleted == toDoStatus) {
				return prevToDoDone;
			}
			// open, inProgress
			return false;
		}
		return prevToDoDone;
	}

	private String getTitle(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Translator translator, String titleKey) {
		String title = translator.translate(titleKey, courseNode.getLongTitle());
		
		ObligationOverridable obligation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode).getObligation();
		if (obligation != null && obligation.getCurrent() != null && obligation.getCurrent() == AssessmentObligation.optional) {
			title = title + " " + translator.translate("course.node.todo.optional");
		}
		
		return title;
	}
	
	private String getDescription(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Translator translator, String descKey) {
		if (!StringHelper.containsNonWhitespace(descKey)) {
			return null;
		}
		
		String coachComment = "";
		TaskRevision revision = getTaskRevision(courseNode, userCourseEnv);
		if (revision != null && StringHelper.containsNonWhitespace(revision.getComment())) {
			coachComment = "\n\n" + revision.getComment();
		}
		
		return translator.translate(descKey, coachComment);
	}

	private Translator getUserTranslator(UserCourseEnvironment userCourseEnv) {
		String language = userCourseEnv.getIdentityEnvironment().getIdentity().getUser().getPreferences().getLanguage();
		Locale locale = i18nManager.getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(CourseToDoUIFactory.class, locale);
		return Util.createPackageTranslator(GTAUIFactory.class, locale, translator);
	}
	
	private boolean isContextValid(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			CourseToDoEnvironment toDoEnv, DueDateConfig dueDateConfig, String providerType) {
		logContextValid(courseNode, userCourseEnv, toDoEnv, dueDateConfig, providerType);
		return isLearningPathCourse(userCourseEnv)
				&& isCourseStatusPublished(userCourseEnv)
				&& isCourseNodeNotExcluded(courseNode, userCourseEnv)
				&& isCourseNodeStatusReady(courseNode, userCourseEnv)
				&& isCourseNodeStarted(courseNode, userCourseEnv)
				&& isCourseVisited(userCourseEnv, toDoEnv, dueDateConfig)
				&& toDoEnv.isCourseParticipantMember(userCourseEnv);
	}

	private void logContextValid(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			CourseToDoEnvironment toDoEnv, DueDateConfig dueDateConfig, String providerType) {
		if (log.isDebugEnabled()) {
			Long repoKey = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
			String ident = courseNode.getIdent();
			Long identityKey = userCourseEnv.getIdentityEnvironment().getIdentity().getKey();
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isLearningPathCourse: {}",
					repoKey, ident, identityKey, providerType, isLearningPathCourse(userCourseEnv));
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isCourseStatusPublished: {}",
					repoKey, ident, identityKey, providerType, isCourseStatusPublished(userCourseEnv));
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isCourseNodeNotExcluded: {}",
					repoKey, ident, identityKey, providerType, isCourseNodeNotExcluded(courseNode, userCourseEnv));
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isCourseNodeStatusReady: {}",
					repoKey, ident, identityKey, providerType, isCourseNodeStatusReady(courseNode, userCourseEnv));
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isCourseNodeStarted: {}",
					repoKey, ident, identityKey, providerType, isCourseNodeStarted(courseNode, userCourseEnv));
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isCourseVisited: {}",
					repoKey, ident, identityKey, providerType, isCourseVisited(userCourseEnv, toDoEnv, dueDateConfig));
			log.debug("Context of entry {}, node {}, identity {}, providerType {}, isCourseParticipantMember: {}",
					repoKey, ident, identityKey, providerType, toDoEnv.isCourseParticipantMember(userCourseEnv));
		}
	}

	private boolean isLearningPathCourse(UserCourseEnvironment userCourseEnv) {
		return LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(userCourseEnv).getType());
	}

	private boolean isCourseNodeNotExcluded(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		ObligationOverridable obligation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode).getObligation();
		return obligation == null || obligation.getCurrent() == null || obligation.getCurrent() != AssessmentObligation.excluded;
	}
	
	private boolean isCourseNodeStatusReady(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		AssessmentEntryStatus status = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode).getAssessmentStatus();
		return status != null && AssessmentEntryStatus.notReady != status;
	}
	
	private boolean isCourseNodeStarted(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		// If course node has start date configured, but it's over, the node evaluation has no start date anymore.
		return userCourseEnv.getScoreAccounting().evalCourseNode(courseNode).getStartDate() == null;
	}

	private boolean isCourseStatusPublished(UserCourseEnvironment userCourseEnv) {
		RepositoryEntryStatusEnum entryStatus = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getEntryStatus();
		return RepositoryEntryStatusEnum.isInArray(entryStatus, RepositoryEntryStatusEnum.publishedAndClosed());
	}
	
	private boolean isCourseVisited(UserCourseEnvironment userCourseEnv, CourseToDoEnvironment toDoEnv, DueDateConfig dueDateConfig) {
		if (DueDateConfig.isRelative(dueDateConfig)) {
			// If relative date the to-do has to be created when the user has visited the course at least once.
			Date courseLaunchDate = toDoEnv.getCourseLaunchDate(userCourseEnv);
			return courseLaunchDate != null;
		}
		// If no due date or absolute due date the to-do has to be created immediately.
		return true;
	}

	private TaskRef getTask(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		if (identityToTask == null) {
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			identityToTask = gtaManager.getTasksLight(entry, (GTACourseNode)courseNode, identities).stream()
					.filter(task -> task.getIdentityKey() != null)
					.collect(Collectors.toMap(TaskLight::getIdentityKey, Function.identity()));
		}
		
		return identityToTask.get(userCourseEnv.getIdentityEnvironment().getIdentity().getKey());
	}

	private TaskRevision getTaskRevision(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		if (identityToRevision == null) {
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			identityToRevision = gtaManager.getLatestTaskRevisions(entry, (GTACourseNode)courseNode, identities).stream()
					.filter(revision -> revision.getTask().getIdentity().getKey() != null)
					.collect(Collectors.toMap(revision -> revision.getTask().getIdentity().getKey(), Function.identity()));
		}
		
		return identityToRevision.get(userCourseEnv.getIdentityEnvironment().getIdentity().getKey());
	}

	private Date getAssignmentDueDate(CourseNode courseNode, UserCourseEnvironment userCourseEnv, DueDateConfig dueDateConfig) {
		TaskRef task = getTask(courseNode, userCourseEnv);
		if (task != null) {
			Date dueDate = task.getAssignmentDueDate();
			if (dueDate != null) {
				return dueDate;
			}
		}
		
		if (identityToAssignmentDueDate == null) {
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			identityToAssignmentDueDate = dueDateService.getIdentityKeyToDueDate(dueDateConfig, entry, identities);
		}
		
		return identityToAssignmentDueDate.get(userCourseEnv.getIdentityEnvironment().getIdentity().getKey());
	}
	
	private Date getSubmitDueDate(CourseNode courseNode, UserCourseEnvironment userCourseEnv, DueDateConfig dueDateConfig) {
		TaskRef task = getTask(courseNode, userCourseEnv);
		if (task != null) {
			Date dueDate = task.getSubmissionDueDate();
			if (dueDate != null) {
				return dueDate;
			}
		}
		
		if (identityToSubmitDueDate == null) {
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			identityToSubmitDueDate = dueDateService.getIdentityKeyToDueDate(dueDateConfig, entry, identities);
		}
		
		return identityToSubmitDueDate.get(userCourseEnv.getIdentityEnvironment().getIdentity().getKey());
	}

}
