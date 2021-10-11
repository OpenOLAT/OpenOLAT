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
package org.olat.course.nodes;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.gta.GTAAssessmentConfig;
import org.olat.course.nodes.gta.GTALearningPathNodeHandler;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.ITALearningPathNodeHandler;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.rule.GTAReminderProvider;
import org.olat.course.nodes.gta.ui.GTACoachedGroupListController;
import org.olat.course.nodes.gta.ui.GTAEditController;
import org.olat.course.nodes.gta.ui.GTARunController;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(GTACourseNode.class);
	private static final String PACKAGE_GTA = Util.getPackageName(GTAEditController.class);

	private static final long serialVersionUID = 1L;
	
	/**
	 * Setting for group or individual task
	 */
	public static final String GTASK_TYPE = "grouptask.type";
	public static final String GTASK_GROUPS = "grouptask.groups";
	public static final String GTASK_AREAS = "grouptask.areas";
	public static final String GTASK_ASSIGNMENT = "grouptask.assignement";
	public static final String GTASK_ASSIGNMENT_DEADLINE = "grouptask.assignment.deadline";
	public static final String GTASK_ASSIGNMENT_DEADLINE_RELATIVE = "grouptask.assignment.deadline.relative";
	public static final String GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO = "grouptask.assignment.deadline.relative.to";
	public static final String GTASK_COACH_ALLOWED_UPLOAD_TASKS = "grouptask.coach.allowed.upload.tasks";
	public static final String GTASK_SUBMIT = "grouptask.submit";
	public static final String GTASK_SUBMIT_DEADLINE = "grouptask.submit.deadline";
	public static final String GTASK_SUBMIT_DEADLINE_RELATIVE = "grouptask.submit.deadline.relative";
	public static final String GTASK_SUBMIT_DEADLINE_RELATIVE_TO = "grouptask.submit.deadline.relative.to";
	public static final String GTASK_REVIEW_AND_CORRECTION = "grouptask.review.and.correction";
	public static final String GTASK_REVISION_PERIOD = "grouptask.revision.period";
	public static final String GTASK_SAMPLE_SOLUTION = "grouptask.solution";
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER = "grouptask.solution.visible.after";
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_ALL = "grouptask.solution.visible.all";
	
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE = "grouptask.solution.visible.after.relative";
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO = "grouptask.solution.visible.after.relative.to";
	public static final String GTASK_GRADING = "grouptask.grading";
	
	public static final String GTASK_TASKS = "grouptask.tasks";
	
	public static final String GTASK_RELATIVE_DATES = "grouptask.rel.dates";
	
	public static final String GTASK_ASSIGNEMENT_TYPE = "grouptask.assignement.type";
	public static final String GTASK_ASSIGNEMENT_TYPE_AUTO = "auto";
	public static final String GTASK_ASSIGNEMENT_TYPE_MANUAL = "manual";

	public static final String GTASK_USERS_TEXT = "grouptask.users.text";
	public static final String GTASK_PREVIEW = "grouptask.preview";
	
	public static final String GTASK_SAMPLING = "grouptask.sampling";
	public static final String GTASK_SAMPLING_REUSE = "reuse";
	public static final String GTASK_SAMPLING_UNIQUE = "unique";

	public static final String GTASK_EXTERNAL_EDITOR = "grouptask.external.editor";
	public static final String GTASK_EMBBEDED_EDITOR = "grouptask.embbeded.editor";
	public static final String GTASK_SUBMISSION_TEMPLATE = "grouptask.submission.template";
	public static final String GTASK_MIN_SUBMITTED_DOCS = "grouptask.min.submitted.docs";
	public static final String GTASK_MAX_SUBMITTED_DOCS = "grouptask.max.submitted.docs";
	
	public static final String GTASK_SUBMISSION_TEXT = "grouptask.submission.text";
	public static final String GTASK_SUBMISSION_MAIL_CONFIRMATION = "grouptask.submission.mail.confirmation";

	public static final String GTASK_MIN_REVISED_DOCS = "grouptask.min.revised.docs";
	public static final String GTASK_MAX_REVISED_DOCS = "grouptask.max.revised.docs";
	
	public static final String GTASK_SOLUTIONS = "grouptask.solutions";
	

	public static final String TYPE_GROUP = "gta";
	public static final String TYPE_INDIVIDUAL = "ita";

	public GTACourseNode() {
		super(TYPE_GROUP);
	}
	
	public GTACourseNode(String type) {
		super(type);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		if(isNewNode) {
			//setup default configuration
			ModuleConfiguration config = getModuleConfiguration();
			//group task
			if(getType().equals(TYPE_INDIVIDUAL)) {
				config.setStringValue(GTASK_TYPE, GTAType.individual.name());
			} else {
				config.setStringValue(GTASK_TYPE, GTAType.group.name());
			}

			//manual choice
			config.setStringValue(GTASK_ASSIGNEMENT_TYPE, GTASK_ASSIGNEMENT_TYPE_MANUAL);
			//all steps
			config.setBooleanEntry(GTASK_ASSIGNMENT, true);
			config.setBooleanEntry(GTASK_SUBMIT, true);
			config.setBooleanEntry(GTASK_REVIEW_AND_CORRECTION, true);
			config.setBooleanEntry(GTASK_REVISION_PERIOD, true);
			config.setBooleanEntry(GTASK_SAMPLE_SOLUTION, true);
			config.setBooleanEntry(GTASK_GRADING, true);
			//editors
			config.setBooleanEntry(GTASK_EXTERNAL_EDITOR, true);
			config.setBooleanEntry(GTASK_EMBBEDED_EDITOR, true);
			config.setBooleanEntry(GTASK_SUBMISSION_TEMPLATE, false);
			config.setIntValue(GTASK_MIN_SUBMITTED_DOCS, 1);
			//reuse tasks
			config.setStringValue(GTASK_SAMPLING, GTASK_SAMPLING_REUSE);
			//configure grading
			config.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE);
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, Float.valueOf(0.0f));
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, Float.valueOf(0.0f));
			config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.TRUE);
			config.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.TRUE);
		}
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}
		
		List<StatusDescription> statusDescs = validateInternalConfiguration(null);
		if(statusDescs.isEmpty()) {
			statusDescs.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache[0];
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;//delete the cache
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_GTA, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(5);

		ModuleConfiguration config = getModuleConfiguration();
		
		boolean hasScoring = config.getBooleanSafe(GTASK_GRADING);
		if (hasScoring) {
			if(!config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD)
					&& !config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD)
					&& !config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD)) {

				addStatusErrorDescription("error.missing.score.config", GTAEditController.PANE_TAB_GRADING, sdList);
			}
		}
		
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
			List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
			if(groupKeys.isEmpty() && areaKeys.isEmpty()) {
				addStatusErrorDescription("error.missing.group", GTAEditController.PANE_TAB_GRADING, sdList);
			}
		}
		
		//at least one step
		if(!config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT) && !config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				&& !config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION) && !config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				&& !config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION) && !config.getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			addStatusErrorDescription("error.select.atleastonestep", GTAEditController.PANE_TAB_WORKLOW, sdList);
		}
		
		LearningPathNodeHandler lpNodeHandler = getLearningPathNodeHandler();
		if (isFullyAssessedScoreConfigError(lpNodeHandler)) {
			addStatusErrorDescription("error.fully.assessed.score",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		if (isFullyAssessedPassedConfigError(lpNodeHandler)) {
			addStatusErrorDescription("error.fully.assessed.passed",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		
		if(cev != null) {
			//check assignment
			GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
			RepositoryEntry courseRe = cev.getCourseGroupManager().getCourseEntry();
			ICourse course = CourseFactory.loadCourse(courseRe);
			if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
				File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
				if(!TaskHelper.hasDocuments(taskDirectory)) {
					if(config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false)) {
						addStatusWarningDescription("error.missing.tasks", GTAEditController.PANE_TAB_ASSIGNMENT, sdList);
					} else {
						addStatusErrorDescription("error.missing.tasks", GTAEditController.PANE_TAB_ASSIGNMENT, sdList);
					}
				} else {
					List<TaskDefinition> taskList = gtaManager.getTaskDefinitions(course.getCourseEnvironment(), this);
					if(taskList == null || taskList.isEmpty()) {
						if(config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false)) {
							addStatusWarningDescription("error.missing.tasks", GTAEditController.PANE_TAB_ASSIGNMENT, sdList);
						} else {
							addStatusErrorDescription("error.missing.tasks", GTAEditController.PANE_TAB_ASSIGNMENT, sdList);
						}
					} else {
						String[] filenames = taskDirectory.list();
						for(TaskDefinition taskDef: taskList) {
							boolean found = false;
							for(String filename:filenames) {
								if(filename.equals(taskDef.getFilename())) {
									found = true;
									break;
								}
							}
							
							if(!found) {
								addStatusWarningDescription("error.missing.file", GTAEditController.PANE_TAB_ASSIGNMENT, sdList);
							}
						}
					}
				}
			}
			
			//check solutions
			if(config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
				File solutionDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
				if(!TaskHelper.hasDocuments(solutionDirectory)) {
					if(config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false)) {
						addStatusWarningDescription("error.missing.solutions", GTAEditController.PANE_TAB_SOLUTIONS, sdList);
					} else {
						addStatusErrorDescription("error.missing.solutions", GTAEditController.PANE_TAB_SOLUTIONS, sdList);
					}
				}
			}
			
			List<IdentityRef> participants = gtaManager.getDuplicatedMemberships(this);
			if(!participants.isEmpty()) {
				UserManager um = CoreSpringFactory.getImpl(UserManager.class);
				StringBuilder sb = new StringBuilder();
				for(IdentityRef participant:participants) {
					String fullname = um.getUserDisplayName(participant.getKey());
					if(sb.length() > 0) sb.append(", ");
					sb.append(fullname);
				}

				String[] params = new String[] { getShortTitle(), sb.toString()  };
				StatusDescription sd = new StatusDescription(StatusDescription.WARNING, "error.duplicate.memberships", "error.duplicate.memberships", params, PACKAGE_GTA);
				sd.setDescriptionForUnit(getIdent());
				sd.setActivateableViewIdentifier(GTAEditController.PANE_TAB_WORKLOW);
				sdList.add(sd);
			}
		}

		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError(LearningPathNodeHandler lpNodeHandler) {
		boolean hasScore = new GTAAssessmentConfig(getModuleConfiguration()).getScoreMode() != Mode.none;
		boolean isScoreTrigger = lpNodeHandler
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError(LearningPathNodeHandler lpNodeHandler) {
		boolean hasPassed = new GTAAssessmentConfig(getModuleConfiguration()).getPassedMode() != Mode.none;
		boolean isPassedTrigger = lpNodeHandler
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}

	private LearningPathNodeHandler getLearningPathNodeHandler() {
		return TYPE_GROUP.equals(this.getType())
				? CoreSpringFactory.getImpl(GTALearningPathNodeHandler.class)
				: CoreSpringFactory.getImpl(ITALearningPathNodeHandler.class);
	}
	
	private void addStatusErrorDescription(String key, String pane, List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, key, key, params, PACKAGE_GTA);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}
	
	private void addStatusWarningDescription(String key, String pane, List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.WARNING, key, key, params, PACKAGE_GTA);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}
	
	/**
	 * 
	 * The files are exported in export/{node ident}/tasks and export/{node ident}/solutions
	 * 
	 */
	@Override
	public void exportNode(File fExportDirectory, ICourse course) {
		File fNodeExportDir = new File(fExportDirectory, getIdent());
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		// export the tasks
		File tasksExportDir = new File(fNodeExportDir, "tasks");
		File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
		fNodeExportDir.mkdirs();
		FileUtils.copyDirContentsToDir(taskDirectory, tasksExportDir, false, "export task course node");

		File taskDefinitions = new File(taskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
		if(taskDefinitions.exists()) {
			File copyTaskDefinitions = new File(tasksExportDir.getParentFile(), GTAManager.TASKS_DEFINITIONS);
			FileUtils.copyFileToFile(taskDefinitions, copyTaskDefinitions, false);
		}
		
		//export the solutions
		File fSolExportDir = new File(fNodeExportDir, "solutions");
		File solutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
		fSolExportDir.mkdirs();
		FileUtils.copyDirContentsToDir(solutionsDirectory, fSolExportDir, false, "export task course node solutions");
		
		File solutionDefinitions = new File(solutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
		if(solutionDefinitions.exists()) {
			File copySolutionDefinitions = new File(fSolExportDir.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
			FileUtils.copyFileToFile(solutionDefinitions, copySolutionDefinitions, false);
		}
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		File fNodeImportDir = new File(importDirectory, getIdent());
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		//import tasks
		File tasksImportDir = new File(fNodeImportDir, "tasks");
		File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
		FileUtils.copyDirContentsToDir(tasksImportDir, taskDirectory, false, "import task course node");
		
		File taskDefinitions = new File(tasksImportDir.getParentFile(), GTAManager.TASKS_DEFINITIONS);
		if(taskDefinitions.exists()) {
			File copyTaskDefinitions = new File(taskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
			FileUtils.copyFileToFile(taskDefinitions, copyTaskDefinitions, false);
		}
	
		//import solutions
		File fSolImportDir = new File(fNodeImportDir, "solutions");
		File solutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
		FileUtils.copyDirContentsToDir(fSolImportDir, solutionsDirectory, false, "import task course node solutions");
		
		File solutionDefinitions = new File(fSolImportDir.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
		if(solutionDefinitions.exists()) {
			File copySolutionDefinitions = new File(solutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
			FileUtils.copyFileToFile(solutionDefinitions, copySolutionDefinitions, false);
		}
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		gtaManager.createIfNotExists(entry, this);
	}

	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		//change groups and areas mapping
		postImportCopy(envMapper);
		
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		if (context != null) {
			CopyType taskCopyType = null;
			if (context.isCustomConfigsLoaded()) {
				CopyCourseOverviewRow nodeSettings = context.getCourseNodesMap().get(getIdent());
				
				if (nodeSettings != null) {
					taskCopyType = nodeSettings.getResourceCopyType();
				}
			} else if (context.getTaskCopyType() != null) {
				taskCopyType = context.getTaskCopyType();				
			}
			
			if (taskCopyType == null || taskCopyType.equals(CopyType.copy)) {
				//copy tasks
				File sourceTaskDirectory = gtaManager.getTasksDirectory(sourceCourse.getCourseEnvironment(), this);
				File copyTaskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
				FileUtils.copyDirContentsToDir(sourceTaskDirectory, copyTaskDirectory, false, "copy task course node");
				
				File taskDefinitions = new File(sourceTaskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
				if(taskDefinitions.exists()) {
					File copyTaskDefinitions = new File(copyTaskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
					FileUtils.copyFileToFile(taskDefinitions, copyTaskDefinitions, false);
				}
				
				//copy solutions
				File sourceSolutionsDirectory = gtaManager.getSolutionsDirectory(sourceCourse.getCourseEnvironment(), this);
				File copySolutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
				FileUtils.copyDirContentsToDir(sourceSolutionsDirectory, copySolutionsDirectory, false, "copy task course node solutions");
		
				File solutionDefinitions = new File(sourceSolutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
				if(solutionDefinitions.exists()) {
					File copySolutionDefinitions = new File(copySolutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
					FileUtils.copyFileToFile(solutionDefinitions, copySolutionDefinitions, false);
				}
			}
			
			// Move dates
			ModuleConfiguration config = getModuleConfiguration();
			
			Date assignmentDeadline = config.getDateValue(GTASK_ASSIGNMENT_DEADLINE);
			Date submissionDeadline = config.getDateValue(GTASK_SUBMIT_DEADLINE);
			Date visibleAfter = config.getDateValue(GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
			
			if (assignmentDeadline != null) {
				assignmentDeadline.setTime(assignmentDeadline.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(GTASK_ASSIGNMENT_DEADLINE, assignmentDeadline);
			}
			
			if (submissionDeadline != null) {
				submissionDeadline.setTime(submissionDeadline.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(GTASK_SUBMIT_DEADLINE, submissionDeadline);
			}
			
			if (visibleAfter != null) {
				visibleAfter.setTime(visibleAfter.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER, visibleAfter);
			}
		} else {
			// Note from 11. Oct 2021: Used by regular copy method and synchers
			//copy tasks
			File sourceTaskDirectory = gtaManager.getTasksDirectory(sourceCourse.getCourseEnvironment(), this);
			File copyTaskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
			FileUtils.copyDirContentsToDir(sourceTaskDirectory, copyTaskDirectory, false, "copy task course node");
			
			File taskDefinitions = new File(sourceTaskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
			if(taskDefinitions.exists()) {
				File copyTaskDefinitions = new File(copyTaskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
				FileUtils.copyFileToFile(taskDefinitions, copyTaskDefinitions, false);
			}
			
			//copy solutions
			File sourceSolutionsDirectory = gtaManager.getSolutionsDirectory(sourceCourse.getCourseEnvironment(), this);
			File copySolutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
			FileUtils.copyDirContentsToDir(sourceSolutionsDirectory, copySolutionsDirectory, false, "copy task course node solutions");
	
			File solutionDefinitions = new File(sourceSolutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
			if(solutionDefinitions.exists()) {
				File copySolutionDefinitions = new File(copySolutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
				FileUtils.copyFileToFile(solutionDefinitions, copySolutionDefinitions, false);
			}
		}
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		gtaManager.createIfNotExists(entry, this);
	}
	
    @Override	
    public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
    	super.postImport(importDirectory, course, envMapper, processType);
     	postImportCopy(envMapper);
    }
	
	private void postImportCopy(CourseEnvironmentMapper envMapper) {
		ModuleConfiguration mc = getModuleConfiguration();
		List<Long> groupKeys = mc.getList(GTACourseNode.GTASK_GROUPS, Long.class);
		if(groupKeys != null) {
			groupKeys = envMapper.toGroupKeyFromOriginalKeys(groupKeys);
		}
		mc.set(GTACourseNode.GTASK_GROUPS, groupKeys);
	
		List<Long> areaKeys =  mc.getList(GTACourseNode.GTASK_AREAS, Long.class);
		if(areaKeys != null) {
			areaKeys = envMapper.toAreaKeyFromOriginalKeys(areaKeys);
		}
		mc.set(GTACourseNode.GTASK_AREAS, areaKeys);
	}

	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		GTACourseNode cNode = (GTACourseNode)super.createInstanceForCopy(isNewTitle, course, author);
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		//copy tasks
		File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
		File copyTaskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), cNode);
		FileUtils.copyDirContentsToDir(taskDirectory, copyTaskDirectory, false, "copy task course node");
		
		File taskDefinitions = new File(taskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
		if(taskDefinitions.exists()) {
			File copyTaskDefinitions = new File(copyTaskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
			FileUtils.copyFileToFile(taskDefinitions, copyTaskDefinitions, false);
		}
		
		//copy solutions
		File solutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
		File copySolutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), cNode);
		FileUtils.copyDirContentsToDir(solutionsDirectory, copySolutionsDirectory, false, "copy task course node solutions");

		File solutionDefinitions = new File(solutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
		if(solutionDefinitions.exists()) {
			File copySolutionDefinitions = new File(copySolutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
			FileUtils.copyFileToFile(solutionDefinitions, copySolutionDefinitions, false);
		}
		
		return cNode;
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String path, String charset) {
		final GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		final ModuleConfiguration config =  getModuleConfiguration();

		String prefix;
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			prefix = "grouptask_";
		} else {
			prefix = "ita_";
		}
	
		String dirName;
		if(StringHelper.containsNonWhitespace(path)) {
			dirName = path;
		} else {
			dirName = prefix
				+ StringHelper.transformDisplayNameToFileSystemName(getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		}
		
		TaskList taskList = gtaManager.getTaskList(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);

		//save assessment datas
		List<Identity> users = null;
		if(config.getBooleanSafe(GTASK_GRADING)) {
			users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment(), options);
			
			String courseTitle = course.getCourseTitle();
			String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle, "xlsx");
			List<CourseNode> nodes = Collections.singletonList(this);
			try(OutputStream out = new ShieldOutputStream(exportStream)) {
				exportStream.putNextEntry(new ZipEntry(dirName + "/" + fileName));
				ScoreAccountingHelper.createCourseResultsOverviewXMLTable(users, nodes, course, locale, out);
				exportStream.closeEntry();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		//copy tasks
		if(taskList != null) {
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				List<BusinessGroup> selectedGroups;
				if(options != null && options.getGroup() != null) {
					selectedGroups = Collections.singletonList(options.getGroup());
				} else {
					selectedGroups = gtaManager.getBusinessGroups(this);
				}
				
				for(BusinessGroup businessGroup:selectedGroups) {
					archiveNodeData(course, businessGroup, taskList, dirName, exportStream);
				}
			} else {
				if(users == null) {
					users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment(), options);
				}
				
				Set<Identity> uniqueUsers = new HashSet<>(users);
				for(Identity user: uniqueUsers) {
					archiveNodeData(course, user, taskList, dirName, exportStream);
				}
			}
		}

		//copy solutions
		if(config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
			VFSContainer solutions = gtaManager.getSolutionsContainer(course.getCourseEnvironment(), this);
			if (solutions.exists()) {
				String solutionDirName = dirName + "/solutions";
				for(VFSItem solution:solutions.getItems(new VFSSystemItemFilter())) {
					ZipUtil.addToZip(solution, solutionDirName, exportStream, new VFSSystemItemFilter(), false);
				}
			}
		}
		
		return true;
	}
	
	private void archiveNodeData(ICourse course, Identity assessedIdentity, TaskList taskList, String dirName, ZipOutputStream exportStream) {
		ModuleConfiguration config = getModuleConfiguration();
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		User user = assessedIdentity.getUser();
		String name = user.getLastName()
				+ "_" + user.getFirstName()
				+ "_" + (StringHelper.containsNonWhitespace(user.getNickName()) ? user.getNickName() : assessedIdentity.getName());
		
		int flow = 0;//for beautiful ordering
		String userDirName = dirName + "/" + StringHelper.transformDisplayNameToFileSystemName(name);
		
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null && task.getTaskName() != null && config.getBooleanSafe(GTASK_ASSIGNMENT)) {
			File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
			File taskFile = new File(taskDirectory, task.getTaskName());
			if(taskFile.exists()) {
				String path = userDirName + "/"  + (++flow) + "_task/" + taskFile.getName(); 
				ZipUtil.addFileToZip(path, taskFile, exportStream);
			}
		}
		
		if(config.getBooleanSafe(GTASK_SUBMIT)) {
			File submitDirectory = gtaManager.getSubmitDirectory(course.getCourseEnvironment(), this, assessedIdentity);
			String submissionDirName = userDirName + "/" + (++flow) + "_submissions";
			ZipUtil.addDirectoryToZip(submitDirectory.toPath(), submissionDirName, exportStream);
		}

		if(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionsDir = gtaManager.getCorrectionDirectory(course.getCourseEnvironment(), this, assessedIdentity);
			String correctionDirName = userDirName + "/" + (++flow) + "_corrections";
			ZipUtil.addDirectoryToZip(correctionsDir.toPath(), correctionDirName, exportStream);
		}
		
		if(task != null && config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			int numOfIteration = task.getRevisionLoop();
			for(int i=1; i<=numOfIteration; i++) {
				File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(course.getCourseEnvironment(), this, i, assessedIdentity);
				String revisionDirName = userDirName + "/" + (++flow) + "_revisions_" + i;
				ZipUtil.addDirectoryToZip(revisionDirectory.toPath(), revisionDirName, exportStream);
				
				File correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(course.getCourseEnvironment(), this, i, assessedIdentity);
				String correctionDirName = userDirName + "/" + (++flow) + "_corrections_" + i;
				ZipUtil.addDirectoryToZip(correctionDirectory.toPath(), correctionDirName, exportStream);
			}
		}
		
		//assessment documents
		if(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false)) {
			List<File> assessmentDocuments = course.getCourseEnvironment()
					.getAssessmentManager().getIndividualAssessmentDocuments(this, assessedIdentity);
			if(assessmentDocuments != null && !assessmentDocuments.isEmpty()) {
				String assessmentDir = userDirName + "/"  + (++flow) + "_assessment/";
				for(File document:assessmentDocuments) {
					String path = assessmentDir + document.getName(); 
					ZipUtil.addFileToZip(path, document, exportStream);
				}
			}
		}
	}
	
	public void archiveNodeData(ICourse course, BusinessGroup businessGroup, TaskList taskList, String dirName, ZipOutputStream exportStream) {
		ModuleConfiguration config = getModuleConfiguration();
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		int flow = 0;//for beautiful ordering
		String groupDirName = (StringHelper.containsNonWhitespace(dirName) ? dirName + "/" : "")
				+ StringHelper.transformDisplayNameToFileSystemName(businessGroup.getName())
				+ "_" + businessGroup.getKey();
		
		Task task = gtaManager.getTask(businessGroup, taskList);
		if(task != null && task.getTaskName() != null && config.getBooleanSafe(GTASK_ASSIGNMENT)) {
			File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
			File taskFile = new File(taskDirectory, task.getTaskName());
			if(taskFile.exists()) {
				String path = groupDirName + "/"  + (++flow) + "_task/" + taskFile.getName(); 
				ZipUtil.addFileToZip(path, taskFile, exportStream);
			}
		}
		
		if(config.getBooleanSafe(GTASK_SUBMIT)) {
			File submitDirectory = gtaManager.getSubmitDirectory(course.getCourseEnvironment(), this, businessGroup);
			String submissionDirName = groupDirName + "/" + (++flow) + "_submissions";
			ZipUtil.addDirectoryToZip(submitDirectory.toPath(), submissionDirName, exportStream);
		}

		if(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionsDir = gtaManager.getCorrectionDirectory(course.getCourseEnvironment(), this, businessGroup);
			String correctionDirName = groupDirName + "/" + (++flow) + "_corrections";
			ZipUtil.addDirectoryToZip(correctionsDir.toPath(), correctionDirName, exportStream);
		}
		
		if(task != null && config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			int numOfIteration = task.getRevisionLoop();
			for(int i=1; i<=numOfIteration; i++) {
				File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(course.getCourseEnvironment(), this, i, businessGroup);
				String revisionDirName = groupDirName + "/" + (++flow) + "_revisions_" + i;
				ZipUtil.addDirectoryToZip(revisionDirectory.toPath(), revisionDirName, exportStream);
				
				File correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(course.getCourseEnvironment(), this, i, businessGroup);
				String correctionDirName = groupDirName + "/" + (++flow) + "_corrections_" + i;
				ZipUtil.addDirectoryToZip(correctionDirectory.toPath(), correctionDirName, exportStream);
			}
		}
		
		//assessment documents for all participants of the group
		if(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false)) {
			List<Identity> assessedIdentities = CoreSpringFactory.getImpl(BusinessGroupService.class)
					.getMembers(businessGroup, GroupRoles.participant.name());
			String assessmentDirName = groupDirName + "/"  + (++flow) + "_assessment";
			for(Identity assessedIdentity:assessedIdentities) {
				List<File> assessmentDocuments = course.getCourseEnvironment()
						.getAssessmentManager().getIndividualAssessmentDocuments(this, assessedIdentity);
				if(assessmentDocuments != null && !assessmentDocuments.isEmpty()) {
					String name = assessedIdentity.getUser().getLastName()
							+ "_" + assessedIdentity.getUser().getFirstName()
							+ "_" + assessedIdentity.getName();
					String userDirName = assessmentDirName + "/" + StringHelper.transformDisplayNameToFileSystemName(name);
					for(File document:assessmentDocuments) {
						String path = userDirName + "/" + document.getName(); 
						ZipUtil.addFileToZip(path, document, exportStream);
					}
				}
			}
		}
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		//tasks
		File taskDirectory = gtaManager.getTasksDirectory(course.getCourseEnvironment(), this);
		FileUtils.deleteDirsAndFiles(taskDirectory, true, true);
		
		//solutions
		File solutionsDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), this);
		FileUtils.deleteDirsAndFiles(solutionsDirectory, true, true);
		
		//clean up database
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		gtaManager.deleteTaskList(entry, this);
		
		//clean subscription
		SubscriptionContext markedSubscriptionContext = gtaManager.getSubscriptionContext(course.getCourseEnvironment(), this, true);
		NotificationsManager notificationsManager = CoreSpringFactory.getImpl(NotificationsManager.class);
		notificationsManager.delete(markedSubscriptionContext);
		SubscriptionContext subscriptionContext = gtaManager.getSubscriptionContext(course.getCourseEnvironment(), this, false);
		notificationsManager.delete(subscriptionContext);
	}
	
	public boolean isOptional(UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv != null && LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(userCourseEnv).getType())) {
			AssessmentEvaluation evaluation = userCourseEnv.getScoreAccounting().evalCourseNode(this);
			if (evaluation != null && evaluation.getObligation() != null && evaluation.getObligation().getCurrent() != null) {
				return AssessmentObligation.optional == evaluation.getObligation().getCurrent();
			}
		}
		return getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_OPTIONAL);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		GTAEditController editCtrl = new GTAEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, editCtrl);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(GTACourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new GTARunController(ureq, wControl, this, userCourseEnv);
		}
		 
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_gta_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(GTAManager.class).createIfNotExists(re, this);
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
	public GTACoachedGroupListController getCoachedGroupListController(UserRequest ureq, WindowControl wControl,
			BreadcrumbPanel stackPanel, UserCourseEnvironment coachCourseEnv, boolean admin, List<BusinessGroup> coachedGroups) {
		
		List<BusinessGroup> groups;
		CourseGroupManager gm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		if(admin) {
			groups = gm.getAllBusinessGroups();
		} else {
			groups = coachedGroups;
		}
		groups = CoreSpringFactory.getImpl(GTAManager.class).filterBusinessGroups(groups, this);
		return new GTACoachedGroupListController(ureq, wControl, stackPanel, coachCourseEnv, this, groups);
	}

	@Override
	public CourseNodeReminderProvider getReminderProvider(boolean rootNode) {
		return new GTAReminderProvider(this);
	}
	
	@Override
	public List<Map.Entry<String, Date>> getNodeSpecificDatesWithLabel() {
		List<Map.Entry<String, Date>> nodeSpecificDates = new ArrayList<>();
		ModuleConfiguration config = getModuleConfiguration();
		
		Date assignmentDeadline = config.getDateValue(GTASK_ASSIGNMENT_DEADLINE);
		Date submissionDeadline = config.getDateValue(GTASK_SUBMIT_DEADLINE);
		Date visibleAfter = config.getDateValue(GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		
		if (assignmentDeadline != null) {
			nodeSpecificDates.add(Map.entry("gtask.assignment.deadline", assignmentDeadline));
		}
		
		if (submissionDeadline != null) {
			nodeSpecificDates.add(Map.entry("gtask.submission.deadline", submissionDeadline));
		}
		
		if (visibleAfter != null) {
			nodeSpecificDates.add(Map.entry("gtask.submission.visibility", visibleAfter));
		}
		
		return nodeSpecificDates;
	}

}