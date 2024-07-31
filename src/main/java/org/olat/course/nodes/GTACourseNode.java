/**

 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes;

import static org.olat.modules.forms.EvaluationFormSessionStatus.done;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig.CoachAssignmentMode;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.gta.GTAAssessmentConfig;
import org.olat.course.nodes.gta.GTAEvaluationFormProvider;
import org.olat.course.nodes.gta.GTALearningPathNodeHandler;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAModule;
import org.olat.course.nodes.gta.GTAPeerReviewEvaluationFormProvider;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.ITALearningPathNodeHandler;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.manager.GTAResultsExport;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.rule.GTAReminderProvider;
import org.olat.course.nodes.gta.ui.GTADefaultsEditController;
import org.olat.course.nodes.gta.ui.GTAEditController;
import org.olat.course.nodes.gta.ui.GTARunController;
import org.olat.course.nodes.gta.ui.GTAScores;
import org.olat.course.nodes.ms.MSScoreEvaluationAndDataHelper;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GTACourseNode extends AbstractAccessableCourseNode
		implements CourseNodeWithFiles, CourseNodeWithDefaults {
	
	private static final String PACKAGE_GTA = Util.getPackageName(GTAEditController.class);

	private static final long serialVersionUID = 1L;
	
	/**
	 * Setting for group or individual task
	 */
	private static final int CURRENT_VERSION = 3;
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
	public static final String GTASK_LATE_SUBMIT = "grouptask.late.submit";
	public static final String GTASK_LATE_SUBMIT_DEADLINE = "grouptask.late.submit.deadline";
	public static final String GTASK_LATE_SUBMIT_DEADLINE_RELATIVE = "grouptask.late.submit.deadline.relative";
	public static final String GTASK_LATE_SUBMIT_DEADLINE_RELATIVE_TO = "grouptask.late.submit.deadline.relative.to";
	public static final String GTASK_REVIEW_AND_CORRECTION = "grouptask.review.and.correction";
	public static final String GTASK_PEER_REVIEW = "grouptask.peer.review";
	public static final String GTASK_REVISION_PERIOD = "grouptask.revision.period";
	public static final String GTASK_PEER_REVIEW_DEADLINE = "grouptask.peer.review.deadline";
	public static final String GTASK_PEER_REVIEW_DEADLINE_START = "grouptask.peer.review.deadline.start";
	public static final String GTASK_PEER_REVIEW_DEADLINE_RELATIVE = "grouptask.peer.review.deadline.relative";
	public static final String GTASK_PEER_REVIEW_DEADLINE_RELATIVE_TO = "grouptask.peer.review.deadline.relative.to";
	public static final String GTASK_PEER_REVIEW_DEADLINE_LENGTH = "grouptask.peer.review.deadline.length";
	public static final String GTASK_SAMPLE_SOLUTION = "grouptask.solution";
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER = "grouptask.solution.visible.after";
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_ALL = "grouptask.solution.visible.all";
	
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE = "grouptask.solution.visible.after.relative";
	public static final String GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO = "grouptask.solution.visible.after.relative.to";
	public static final String GTASK_GRADING = "grouptask.grading";
	
	public static final String GTASK_SCORE_PARTS = "grouptask.score.parts";
	public static final String GTASK_SCORE_PARTS_EVALUATION_FORM = "grouptask.score.parts.evaluation.form";
	public static final String GTASK_SCORE_PARTS_PEER_REVIEW = "grouptask.score.parts.peer.review";
	public static final String GTASK_SCORE_PARTS_REVIEW_SUBMITTED = "grouptask.score.parts.review.submitted";
	
	public static final String GTASK_TASKS = "grouptask.tasks";
	
	public static final String GTASK_OBLIGATION = "grouptask.obligation";
	public static final String GTASK_RELATIVE_DATES = "grouptask.rel.dates";
	public static final String TYPE_RELATIVE_TO_ASSIGNMENT = "assignment";
	
	public static final String GTASK_ASSIGNEMENT_TYPE = "grouptask.assignement.type";
	public static final String GTASK_ASSIGNEMENT_TYPE_AUTO = "auto";
	public static final String GTASK_ASSIGNEMENT_TYPE_MANUAL = "manual";
	
	public static final String GTASK_COACH_ASSIGNMENT = "grouptask.coach.assignment";
	public static final String GTASK_COACH_ASSIGNMENT_MODE = "grouptask.coach.assignment.mode";
	public static final String GTASK_COACH_ASSIGNMENT_MODE_DEFAULT = CoachAssignmentMode.manual.name();
	public static final String GTASK_COACH_ASSIGNMENT_OWNERS = "grouptask.coach.assignment.owners";
	
	public static final String GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT = "grouptask.coach.assignment.coach.notification.assignment";
	public static final String GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT = "grouptask.coach.assignment.coach.notification.unassignment";
	public static final String GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER = "grouptask.coach.assignment.coach.notification.neworder";
	public static final String GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT = "grouptask.coach.assignment.participant.notification.assignment";

	public static final String GTASK_USERS_TEXT = "grouptask.users.text";
	public static final String GTASK_PREVIEW = "grouptask.preview";
	
	public static final String GTASK_SAMPLING = "grouptask.sampling";
	public static final String GTASK_SAMPLING_REUSE = "reuse";
	public static final String GTASK_SAMPLING_UNIQUE = "unique";

	public static final String GTASK_EXTERNAL_EDITOR = "grouptask.external.editor";
	public static final String GTASK_EMBBEDED_EDITOR = "grouptask.embbeded.editor";
	public static final String GTASK_SUBMISSION_TEMPLATE = "grouptask.submission.template";
	public static final String GTASK_ALLOW_VIDEO_RECORDINGS = "grouptask.allow.video.recordings";
	public static final String GTASK_VIDEO_QUALITY = "grouptask.video.quality";
	public static final String GTASK_MAX_VIDEO_DURATION = "grouptask.max.video.duration";
	public static final String GTASK_ALLOW_AUDIO_RECORDINGS = "grouptask.allow.audio.recordings";
	public static final String GTASK_MAX_AUDIO_DURATION = "grouptask.max.audio.duration";
	public static final String GTASK_MIN_SUBMITTED_DOCS = "grouptask.min.submitted.docs";
	public static final String GTASK_MAX_SUBMITTED_DOCS = "grouptask.max.submitted.docs";
	
	public static final String GTASK_SUBMISSION_TEXT = "grouptask.submission.text";
	public static final String GTASK_SUBMISSION_MAIL_CONFIRMATION = "grouptask.submission.mail.confirmation";
	
	public static final String GTASK_PEER_REVIEW_MUTUAL_REVIEW = "grouptask.peer.review.mutual.review";
	public static final String GTASK_PEER_REVIEW_FORM_OF_REVIEW = "grouptask.peer.review.form.of.review";
	public static final String GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW = "grouptask.peer.review.double.blinded.review";
	public static final String GTASK_PEER_REVIEW_SINGLE_BLINDED_REVIEW = "grouptask.peer.review.single.blinded.review";
	public static final String GTASK_PEER_REVIEW_FORM_OF_REVIEW_DEFAULT = GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW;
	public static final String GTASK_PEER_REVIEW_OPEN_REVIEW = "grouptask.peer.review.open.review";
	
	public static final String GTASK_PEER_REVIEW_EVAL_FORM_SOFTKEY = "grouptask.peer.review.evaluation.form.softkey";
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT = "grouptask.peer.review.assignment";
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK = "grouptask.peer.review.assignment.same.task";
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT_OTHER_TASK = "grouptask.peer.review.assignment.other.task";
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT_RANDOM = "grouptask.peer.review.assignment.random";
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT_DEFAULT = GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK;
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT_PERMISSION = "grouptask.peer.review.assignment.permission";
	public static final String GTASK_PEER_REVIEW_ASSIGNMENT_PERMISSION_DEFAULT = "";
	
	public static final String GTASK_PEER_REVIEW_NUM_OF_REVIEWS = "grouptask.peer.review.num.of.reviews";
	public static final String GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT = "3";
	public static final String GTASK_PEER_REVIEW_SCORE_EVAL_FORM = "grouptask.peer.review.score.evaluation";
	public static final String GTASK_PEER_REVIEW_SCORE_EVAL_FORM_SCALE = "grouptask.peer.review.score.evaluation.form.scale";
	public static final String GTASK_PEER_REVIEW_SCORE_PRO_REVIEW = "grouptask.peer.review.score.pro.review";
	public static final String GTASK_PEER_REVIEW_MAX_NUMBER_CREDITABLE_REVIEWS = "grouptask.peer.review.max.number.creditable.reviews";
	
	public static final String GTASK_PEER_REVIEW_SCORE_MIN = "grouptask.peer.review.scoreMin";
	public static final String GTASK_PEER_REVIEW_SCORE_MAX = "grouptask.peer.review.scoreMax";
	
	public static final String GTASK_PEER_REVIEW_QUALITY_FEEDBACK = "grouptask.peer.review.quality.feedback";
	public static final String GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE = "grouptask.peer.review.quality.feedback.type";
	public static final String GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO = "grouptask.peer.review.quality.feedback.yes.no";
	public static final String GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_STARS = "grouptask.peer.review.quality.feedback.stars";

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
	
	/**
	 * Calculate the values min/max based on the configuration of the course element.
	 * 
	 * @return Min/max values
	 */
	public static MinMax calculateMinMax(ModuleConfiguration config) {
		Float peerReviewScoreScale = GTACourseNode.getFloatConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		Float evaluationScoreScale = GTACourseNode.getFloatConfiguration(config,
				MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		Float pointsProReview = GTACourseNode.getFloatConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW, null);
		
		Integer numOfReviews = GTACourseNode.getIntegerConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		Integer maxNumberCreditableReviews = GTACourseNode.getIntegerConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_MAX_NUMBER_CREDITABLE_REVIEWS,
				numOfReviews == null ? "1" : numOfReviews.toString());
		String scoreParts = config.getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
		
		return calculateMinMaxTotal(config, evaluationScoreScale, peerReviewScoreScale,
				maxNumberCreditableReviews, pointsProReview, scoreParts);
	}
	
	public static MinMax calculateMinMaxTotal(ModuleConfiguration config, Float evaluationScoreScale, Float peerReviewScoreScale,
			Integer maxNumberCreditableReviews, Float pointsProReview, String scoreParts) {
		MinMax evaluationMinMax = null;
		if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM)) {
			RepositoryEntry evaluationFormEntry = GTACourseNode.getEvaluationForm(config);
			evaluationMinMax = calculateMinMax(evaluationFormEntry, config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM),
					evaluationScoreScale);
		}
		// Min. max. from the peer review form
		MinMax peerReviewMinMax = null;
		if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW)) {
			RepositoryEntry peerReviewFormEntry = GTACourseNode.getPeerReviewEvaluationForm(config);
			peerReviewMinMax = calculateMinMax(peerReviewFormEntry, config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM),
					peerReviewScoreScale);
		}
		
		MinMax submittedReview = null;
		if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED)) {
			int numberOfReviews = maxNumberCreditableReviews == null ? 1 : maxNumberCreditableReviews.intValue();
			float pointsProReviewFloat = pointsProReview == null ? 0.0f : pointsProReview.floatValue();
			submittedReview = MinMax.of(0.0f, pointsProReviewFloat * numberOfReviews);
		}
	
		return MinMax.add(evaluationMinMax, peerReviewMinMax, submittedReview);
	}
	
	public static MinMax calculateMinMax(RepositoryEntry formEntry, String scoreKey, Float scalingFactor) {
		MinMax formMinMax = null;
		if (formEntry != null) {
			if(StringHelper.containsNonWhitespace(scoreKey)) {
				float scale = scalingFactor == null ? 1.0f : scalingFactor.floatValue();
				MSService msService = CoreSpringFactory.getImpl(MSService.class);
				
				switch (scoreKey) {
					case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM:
						formMinMax = msService.calculateMinMaxSum(formEntry, scale);
						break;
					case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG:
						formMinMax = msService.calculateMinMaxAvg(formEntry, scale);
						break;
					default:
						break;
				}
			}
		}
		return formMinMax;
	}
	
	public static Float getFloatConfiguration(ModuleConfiguration config, String key, String defaultValue) {
		String scaleConfig = config.getStringValue(key, defaultValue);
		if(StringHelper.containsNonWhitespace(scaleConfig)) {
			return Float.valueOf(scaleConfig);
		}
		return null;
	}
	
	public static Integer getIntegerConfiguration(ModuleConfiguration config, String key, String defaultValue) {
		String scaleConfig = config.getStringValue(key, defaultValue);
		if(StringHelper.containsNonWhitespace(scaleConfig)) {
			return Integer.valueOf(scaleConfig);
		}
		return null;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return getEvaluationForm(getModuleConfiguration());
	}
	
	@Override
	public boolean hasBusinessGroups() {
		List<Long> groupKeys = getModuleConfiguration().getList(GTACourseNode.GTASK_GROUPS, Long.class);
		return !groupKeys.isEmpty() || super.hasBusinessGroups();
	}

	@Override
	public boolean hasBusinessGroupAreas() {
		List<Long> areaKeys = getModuleConfiguration().getList(GTACourseNode.GTASK_AREAS, Long.class);
		return !areaKeys.isEmpty() || super.hasBusinessGroupAreas();
	}
	
	public static RepositoryEntry getEvaluationForm(ModuleConfiguration config) {
		return MSCourseNode.getEvaluationForm(config);
	}
	
	public static String getEvaluationFormReference(ModuleConfiguration moduleConfig) {
		return MSCourseNode.getEvaluationFormReference(moduleConfig);
	}
	
	public static void setEvaluationFormReference(RepositoryEntry re, ModuleConfiguration moduleConfig) {
		moduleConfig.set(MSCourseNode.CONFIG_KEY_EVAL_FORM_SOFTKEY, re.getSoftkey());
	}
	
	public static void removeEvaluationFormReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(MSCourseNode.CONFIG_KEY_EVAL_FORM_SOFTKEY);
	}
	
	public static RepositoryEntry getPeerReviewEvaluationForm(ModuleConfiguration config) {
		if (config == null) return null;
		
		String repoSoftkey = getPeerReviewEvaluationFormReference(config);
		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	public static String getPeerReviewEvaluationFormReference(ModuleConfiguration moduleConfig) {
		String repoSoftkey = moduleConfig.getStringValue(GTASK_PEER_REVIEW_EVAL_FORM_SOFTKEY);
		return StringHelper.containsNonWhitespace(repoSoftkey) ? repoSoftkey : null;
	}
	
	public static void setPeerReviewEvaluationFormReference(RepositoryEntry re, ModuleConfiguration moduleConfig) {
		moduleConfig.set(GTASK_PEER_REVIEW_EVAL_FORM_SOFTKEY, re.getSoftkey());
	}
	
	public static void removePeerReviewEvaluationFormReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(GTASK_PEER_REVIEW_EVAL_FORM_SOFTKEY);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);

		GTAModule gtaModule = CoreSpringFactory.getImpl(GTAModule.class);
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		if(isNewNode && gtaModule != null) {
			//group task
			if(getType().equals(TYPE_INDIVIDUAL)) {
				config.setStringValue(GTASK_TYPE, GTAType.individual.name());
			} else {
				config.setStringValue(GTASK_TYPE, GTAType.group.name());
			}

			//manual choice
			config.setStringValue(GTASK_ASSIGNEMENT_TYPE, GTASK_ASSIGNEMENT_TYPE_MANUAL);
			//all steps
			if (gtaModule.hasObligation()) {
				config.set(GTASK_OBLIGATION, AssessmentObligation.mandatory.name());
			} else {
				config.set(GTASK_OBLIGATION, AssessmentObligation.optional.name());
			}
			config.setBooleanEntry(GTASK_ASSIGNMENT, gtaModule.hasAssignment());
			config.setBooleanEntry(GTASK_SUBMIT, gtaModule.hasSubmission());
			config.setBooleanEntry(GTASK_REVIEW_AND_CORRECTION, gtaModule.hasReviewAndCorrection());
			config.setBooleanEntry(GTASK_PEER_REVIEW, false);
			config.setBooleanEntry(GTASK_REVISION_PERIOD, gtaModule.hasRevisionPeriod());
			config.setBooleanEntry(GTASK_SAMPLE_SOLUTION, gtaModule.hasSampleSolution());
			config.setBooleanEntry(GTASK_GRADING, gtaModule.hasGrading());
			config.setBooleanEntry(GTASK_COACH_ALLOWED_UPLOAD_TASKS, gtaModule.canCoachUploadTasks());
			config.setBooleanEntry(GTASK_COACH_ASSIGNMENT, gtaModule.canCoachAssign());
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
			// To-dos
			config.setBooleanEntry(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED, false);
		}
		if (version < 2 && config.get(GTASK_OBLIGATION) == null) {
			AssessmentObligation obligation = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_OPTIONAL, false)
					? AssessmentObligation.optional
					: AssessmentObligation.mandatory;
			config.set(GTASK_OBLIGATION, obligation.name());
		}
		if (version < 3) {
			config.setBooleanEntry(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED, false);
		}
		config.setConfigurationVersion(CURRENT_VERSION);
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
			
			if(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED)) {
				RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(config);
				if(formEntry == null) {
					addStatusErrorDescription("error.missing.score.form", GTAEditController.PANE_TAB_GRADING, sdList);
				}
			}
		}
		
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
			List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
			if(groupKeys.isEmpty() && areaKeys.isEmpty()) {
				addStatusErrorDescription("error.missing.group", GTAEditController.PANE_TAB_WORKLOW, sdList);
			}
		}
		
		//at least one step
		if(!config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT) && !config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				&& !config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION) && !config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW)
				&& !config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD) && !config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)
				&& !config.getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			addStatusErrorDescription("error.select.atleastonestep", GTAEditController.PANE_TAB_WORKLOW, sdList);
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
			
			// Check peer review
			if(config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW)) {
				RepositoryEntry formEntry = GTACourseNode.getPeerReviewEvaluationForm(getModuleConfiguration());
				if(formEntry == null) {
					addStatusErrorDescription("error.missing.peerreview.survey", GTAEditController.PANE_TAB_PEER_REVIEW, sdList);
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
					sb.append(StringHelper.escapeHtml(fullname));
				}

				String[] params = new String[] { getShortTitle(), sb.toString()  };
				StatusDescription sd = new StatusDescription(StatusDescription.WARNING, "error.duplicate.memberships", "error.duplicate.memberships", params, PACKAGE_GTA);
				sd.setDescriptionForUnit(getIdent());
				sd.setActivateableViewIdentifier(GTAEditController.PANE_TAB_WORKLOW);
				sdList.add(sd);
			}
			
			LearningPathConfigs lpConfigs = getLearningPathNodeHandler().getConfigs(this);
			GTAAssessmentConfig assessmentConfig = new GTAAssessmentConfig(new CourseEntryRef(cev), this);
			if (isFullyAssessedScoreConfigError(assessmentConfig, lpConfigs)) {
				addStatusErrorDescription("error.fully.assessed.score",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			if (isFullyAssessedPassedConfigError(assessmentConfig, lpConfigs)) {
				addStatusErrorDescription("error.fully.assessed.passed",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			
			if (config.getBooleanSafe(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED) && AssessmentObligation.optional == lpConfigs.getObligation()) {
				addStatusWarningDescription("error.todos.but.optional",
						NodeEditController.PANE_TAB_REMINDER_TODO, sdList);
			}
			
			// Grade
			if (hasScoring) {
				if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED) && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
					GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
					GradeScale gradeScale = gradeService.getGradeScale(cev.getCourseGroupManager().getCourseEntry(), getIdent());
					if (gradeScale == null) {
						addStatusErrorDescription("error.missing.grade.scale", GTAEditController.PANE_TAB_GRADING, sdList);
					}
				}
			}
		}

		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError(GTAAssessmentConfig assessmentConfig, LearningPathConfigs lpConfigs) {
		boolean hasScore = assessmentConfig.getScoreMode() != Mode.none;
		boolean isScoreTrigger = lpConfigs
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError(GTAAssessmentConfig assessmentConfig, LearningPathConfigs lpConfigs) {
		boolean hasPassed = assessmentConfig.getPassedMode() != Mode.none;
		boolean isPassedTrigger = lpConfigs
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
	public void exportNode(File exportDirectory, ICourse course, RepositoryEntryImportExportLinkEnum withReferences) {
		File fNodeExportDir = new File(exportDirectory, getIdent());
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
		
		// Export evaluation form
		RepositoryEntry re = MSCourseNode.getEvaluationForm(getModuleConfiguration());
		if (re != null && (withReferences == RepositoryEntryImportExportLinkEnum.WITH_REFERENCE || withReferences == RepositoryEntryImportExportLinkEnum.WITH_SOFT_KEY)) {
			RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fNodeExportDir);
			reie.exportDoExport(withReferences);
		}
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, RepositoryEntryImportExportLinkEnum withReferences) {
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
		
		// Import evaluation form
		
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences == RepositoryEntryImportExportLinkEnum.WITH_REFERENCE && rie.anyExportedPropertiesAvailable()) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(EvaluationFormResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), RepositoryEntryImportExportLinkEnum.NONE, organisation, locale, rie.importGetExportedFile(), null);
			setEvaluationFormReference(re, getModuleConfiguration());
		} else if(withReferences == RepositoryEntryImportExportLinkEnum.WITH_SOFT_KEY) {
			// Do nothing, keep the reference
		} else {
			removeEvaluationFormReference(getModuleConfiguration());
		}
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
			Date lateSubmitDeadline = config.getDateValue(GTASK_LATE_SUBMIT_DEADLINE);
			Date visibleAfter = config.getDateValue(GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
			
			if (assignmentDeadline != null) {
				assignmentDeadline.setTime(assignmentDeadline.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(GTASK_ASSIGNMENT_DEADLINE, assignmentDeadline);
			}
			
			if (submissionDeadline != null) {
				submissionDeadline.setTime(submissionDeadline.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(GTASK_SUBMIT_DEADLINE, submissionDeadline);
			}
			
			if (lateSubmitDeadline != null) {
				lateSubmitDeadline.setTime(lateSubmitDeadline.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(GTASK_LATE_SUBMIT_DEADLINE, lateSubmitDeadline);
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
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		gtaManager.createIfNotExists(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);
		
		if(settings.getCopyType() == CopyType.copy) {
			copyFiles((GTACourseNode)sourceCourseNode, sourceCourse, this, course);
		}

		getModuleConfiguration().remove(GTACourseNode.GTASK_GROUPS);
		getModuleConfiguration().remove(GTACourseNode.GTASK_AREAS);
	}
	
	/**
	 * Obligation is not removed, used by the old course too.s
	 */
	@Override
	protected void removeCourseNodeLearningPathsconfigs() {
		LearningPathService learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(this);
		learningPathConfigs.setExceptionalObligations(null);
	}

	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		GTACourseNode cNode = (GTACourseNode)super.createInstanceForCopy(isNewTitle, course, author);
		copyFiles(this, course, cNode, course);
		return cNode;
	}
	
	private static final void copyFiles(GTACourseNode sourceNode, ICourse sourceCourse, GTACourseNode targetNode, ICourse targetCourse) {
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		
		//copy tasks
		File taskDirectory = gtaManager.getTasksDirectory(sourceCourse.getCourseEnvironment(), sourceNode);
		File copyTaskDirectory = gtaManager.getTasksDirectory(targetCourse.getCourseEnvironment(), targetNode);
		FileUtils.copyDirContentsToDir(taskDirectory, copyTaskDirectory, false, "copy task course node");
		
		File taskDefinitions = new File(taskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
		if(taskDefinitions.exists()) {
			File copyTaskDefinitions = new File(copyTaskDirectory.getParentFile(), GTAManager.TASKS_DEFINITIONS);
			FileUtils.copyFileToFile(taskDefinitions, copyTaskDefinitions, false);
		}
		
		//copy solutions
		File solutionsDirectory = gtaManager.getSolutionsDirectory(sourceCourse.getCourseEnvironment(), sourceNode);
		File copySolutionsDirectory = gtaManager.getSolutionsDirectory(targetCourse.getCourseEnvironment(), targetNode);
		FileUtils.copyDirContentsToDir(solutionsDirectory, copySolutionsDirectory, false, "copy task course node solutions");

		File solutionDefinitions = new File(solutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
		if(solutionDefinitions.exists()) {
			File copySolutionDefinitions = new File(copySolutionsDirectory.getParentFile(), GTAManager.SOLUTIONS_DEFINITIONS);
			FileUtils.copyFileToFile(solutionDefinitions, copySolutionDefinitions, false);
		}
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
		
		boolean onlySubmitted = options != null && options.isOnlySubmitted();
		TaskList taskList = gtaManager.getTaskList(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);

		//save assessment datas
		List<Identity> users = null;
		if(config.getBooleanSafe(GTASK_GRADING)) {
			users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment(), options);
			
			String courseTitle = course.getCourseTitle();
			String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle, "xlsx");
			GTAResultsExport export = new GTAResultsExport(course, this, locale);
			export.export(dirName + "/" + fileName, users, null, exportStream);
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
					archiveNodeData(course.getCourseEnvironment(), businessGroup, taskList, onlySubmitted, dirName, exportStream);
				}
			} else {
				if(users == null) {
					users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment(), options);
				}
				
				Set<Identity> uniqueUsers = new HashSet<>(users);
				for(Identity user: uniqueUsers) {
					archiveNodeData(course, user, taskList, onlySubmitted, dirName, exportStream);
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
	
	private void archiveNodeData(ICourse course, Identity assessedIdentity, TaskList taskList, boolean onlySubmitted, String dirName, ZipOutputStream exportStream) {
		User user = assessedIdentity.getUser();
		String name = user.getLastName()
				+ "_" + user.getFirstName()
				+ "_" + (StringHelper.containsNonWhitespace(user.getNickName()) ? user.getNickName() : assessedIdentity.getName());
		
		String userDirName = dirName + "/" + StringHelper.transformDisplayNameToFileSystemName(name);
		archiveNodeUserData(course.getCourseEnvironment(), assessedIdentity, taskList, onlySubmitted, exportStream, userDirName);
	}
	
	private void archiveNodeUserData(CourseEnvironment courseEnv, Identity assessedIdentity, TaskList taskList, boolean onlySubmitted, ZipOutputStream exportStream, String userDirName) {	
		ModuleConfiguration config = getModuleConfiguration();
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();

		int flow = 0;//for beautiful ordering
		
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null && task.getTaskName() != null && config.getBooleanSafe(GTASK_ASSIGNMENT)) {
			File taskDirectory = gtaManager.getTasksDirectory(courseEnv, this);
			File taskFile = new File(taskDirectory, task.getTaskName());
			if(taskFile.exists()) {
				String path = userDirName + "/"  + (++flow) + "_task/" + taskFile.getName(); 
				ZipUtil.addFileToZip(path, taskFile, exportStream);
			}
		}
		
		if(config.getBooleanSafe(GTASK_SUBMIT)
				&& (!onlySubmitted || isSubmisssionSubmitted(task))) {
			File submitDirectory = gtaManager.getSubmitDirectory(courseEnv, this, assessedIdentity);
			String submissionDirName = userDirName + "/" + (++flow) + "_submissions";
			int files = ZipUtil.addDirectoryToZip(submitDirectory.toPath(), submissionDirName, exportStream);
			if(files > 0 && gtaManager.isSubmissionExtended(task, assessedIdentity, null, this, courseEntry, true)) {
				String extendedFile = submissionDirName + "/Extended_submission.txt";
				ZipUtil.addTextFileToZip(extendedFile, "Extended", exportStream);
			} else if(files > 0 && gtaManager.isSubmissionLate(task, assessedIdentity, null, this, courseEntry, true)) {
				String lateFile = submissionDirName + "/Late_submission.txt";
				ZipUtil.addTextFileToZip(lateFile, "Late", exportStream);
			}
		}

		if(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionsDir = gtaManager.getCorrectionDirectory(courseEnv, this, assessedIdentity);
			String correctionDirName = userDirName + "/" + (++flow) + "_corrections";
			ZipUtil.addDirectoryToZip(correctionsDir.toPath(), correctionDirName, exportStream);
		}
		
		if(task != null && config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			int numOfIteration = task.getRevisionLoop();
			for(int i=1; i<=numOfIteration; i++) {
				if(!onlySubmitted || isRevisionSubmitted(task, i))	{
					File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(courseEnv, this, i, assessedIdentity);
					String revisionDirName = userDirName + "/" + (++flow) + "_revisions_" + i;
					ZipUtil.addDirectoryToZip(revisionDirectory.toPath(), revisionDirName, exportStream);
				}
				
				File correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, this, i, assessedIdentity);
				String correctionDirName = userDirName + "/" + (++flow) + "_corrections_" + i;
				ZipUtil.addDirectoryToZip(correctionDirectory.toPath(), correctionDirName, exportStream);
			}
		}
		
		//assessment documents
		if(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false)) {
			List<File> assessmentDocuments = courseEnv.getAssessmentManager()
					.getIndividualAssessmentDocuments(this, assessedIdentity);
			if(assessmentDocuments != null && !assessmentDocuments.isEmpty()) {
				String assessmentDir = userDirName + "/"  + (++flow) + "_assessment/";
				for(File document:assessmentDocuments) {
					String path = assessmentDir + document.getName(); 
					ZipUtil.addFileToZip(path, document, exportStream);
				}
			}
		}
	}
	
	private boolean isSubmisssionSubmitted(Task task) {
		return task != null && task.getTaskStatus() != null
				&& task.getTaskStatus() != TaskProcess.assignment && task.getTaskStatus() != TaskProcess.submit;
	}
	
	private boolean isRevisionSubmitted(Task task, int iteration) {
		if(task == null || task.getTaskStatus() == null
				|| task.getTaskStatus() == TaskProcess.assignment
				|| task.getTaskStatus() == TaskProcess.submit) {
			return false;
		}
		if(task.getTaskStatus() == TaskProcess.grading || task.getTaskStatus() == TaskProcess.graded
				|| task.getTaskStatus() == TaskProcess.solution) {
			return true;
		}
		int currentIteration = task.getRevisionLoop();
		if(iteration < currentIteration) {
			return true;
		}
		if(iteration == currentIteration) {
			return task.getTaskStatus() == TaskProcess.correction;
		}
		
		return false;
	}
	
	public void archiveNodeData(CourseEnvironment courseEnv, BusinessGroup businessGroup, TaskList taskList,
			boolean onlySubmitted, String dirName, ZipOutputStream exportStream) {
		ModuleConfiguration config = getModuleConfiguration();
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		
		int flow = 0;//for beautiful ordering
		String groupDirName = ZipUtil.concat(dirName, StringHelper.transformDisplayNameToFileSystemName(businessGroup.getName())
				+ "_" + businessGroup.getKey());
		
		Task task = gtaManager.getTask(businessGroup, taskList);
		if(task != null && task.getTaskName() != null && config.getBooleanSafe(GTASK_ASSIGNMENT)) {
			File taskDirectory = gtaManager.getTasksDirectory(courseEnv, this);
			File taskFile = new File(taskDirectory, task.getTaskName());
			if(taskFile.exists()) {
				String path = groupDirName + "/"  + (++flow) + "_task/" + taskFile.getName(); 
				ZipUtil.addFileToZip(path, taskFile, exportStream);
			}
		}
		
		if(config.getBooleanSafe(GTASK_SUBMIT) && (!onlySubmitted || isSubmisssionSubmitted(task))) {
			File submitDirectory = gtaManager.getSubmitDirectory(courseEnv, this, businessGroup);
			String submissionDirName = groupDirName + "/" + (++flow) + "_submissions";
			int files = ZipUtil.addDirectoryToZip(submitDirectory.toPath(), submissionDirName, exportStream);
			if(files > 0 && gtaManager.isSubmissionExtended(task, null, businessGroup, this, courseEntry, true)) {
				String extendedFile = submissionDirName + "/Extended_submission.txt";
				ZipUtil.addTextFileToZip(extendedFile, "Extended", exportStream);
			} else if(files > 0 && gtaManager.isSubmissionLate(task, null, businessGroup, this, courseEntry, true)) {
				String lateFile = submissionDirName + "/Late_submission.txt";
				ZipUtil.addTextFileToZip(lateFile, "Late", exportStream);
			}
		}

		if(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionsDir = gtaManager.getCorrectionDirectory(courseEnv, this, businessGroup);
			String correctionDirName = groupDirName + "/" + (++flow) + "_corrections";
			ZipUtil.addDirectoryToZip(correctionsDir.toPath(), correctionDirName, exportStream);
		}
		
		if(task != null && config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			int numOfIteration = task.getRevisionLoop();
			for(int i=1; i<=numOfIteration; i++) {
				if(!onlySubmitted || isRevisionSubmitted(task, i))	{	
					File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(courseEnv, this, i, businessGroup);
					String revisionDirName = groupDirName + "/" + (++flow) + "_revisions_" + i;
					ZipUtil.addDirectoryToZip(revisionDirectory.toPath(), revisionDirName, exportStream);
				}
				
				File correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, this, i, businessGroup);
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
				List<File> assessmentDocuments = courseEnv.getAssessmentManager()
						.getIndividualAssessmentDocuments(this, assessedIdentity);
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
		
		// Delete GradeScales
		CoreSpringFactory.getImpl(GradeService.class).deleteGradeScale(entry, getIdent());
	}
	
	public boolean isNodeOptional(CourseEnvironment coursEnv, Identity doer) {
		NodeAccessType nodeAccessType = NodeAccessType.of(coursEnv);
		
		updateModuleConfigDefaults(false, getParent(), nodeAccessType, doer);

		return getModuleConfiguration().getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
	}
	
	public boolean isOptional(CourseEnvironment coursEnv, UserCourseEnvironment userCourseEnv) {
		NodeAccessType nodeAccessType = NodeAccessType.of(coursEnv);
		
		Identity doer = userCourseEnv != null ? userCourseEnv.getIdentityEnvironment().getIdentity() : null;
		updateModuleConfigDefaults(false, getParent(), nodeAccessType, doer);
		if (userCourseEnv != null && LearningPathNodeAccessProvider.TYPE.equals(nodeAccessType.getType())) {
			AssessmentEvaluation evaluation = userCourseEnv.getScoreAccounting().evalCourseNode(this);
			if (evaluation != null && evaluation.getObligation() != null && evaluation.getObligation().getCurrent() != null) {
				return AssessmentObligation.optional == evaluation.getObligation().getCurrent();
			}
		}
		return getModuleConfiguration().getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		GTAEditController editCtrl = new GTAEditController(ureq, wControl, stackPanel, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, editCtrl);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else {
			controller = new GTARunController(ureq, wControl, this, userCourseEnv);
		}
		 
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_gta_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		AssessmentService assessmentService = CoreSpringFactory.getImpl(AssessmentService.class);
		CoreSpringFactory.getImpl(GTAManager.class).createIfNotExists(courseEntry, this);
		
		super.updateOnPublish(locale, course, publisher, publishEvents);
		
		updateCoachAssignment(course);

		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdent(courseEntry, getIdent());
		for(AssessmentEntry assessmentEntry:assessmentEntries) {
			recalculateAndUpdateScore(course.getCourseEnvironment(), assessmentEntry, publisher, locale);
		}
		DBFactory.getInstance().commitAndCloseSession();
	}
	
	public void recalculateAndUpdateScore(CourseEnvironment courseEnv, Identity assessedIdentity, Identity doer, Locale locale) {
		AssessmentService assessmentService = CoreSpringFactory.getImpl(AssessmentService.class);

		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, courseEntry, getIdent());
		recalculateAndUpdateScore(courseEnv, assessmentEntry, doer, locale);
	}
	
	public void recalculateAndUpdateScore(CourseEnvironment courseEnv, AssessmentEntry assessmentEntry, Identity doer, Locale locale) {
		final RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		final Identity assessedIdentity = assessmentEntry.getIdentity();
		
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		GTAPeerReviewManager peerReviewManager = CoreSpringFactory.getImpl(GTAPeerReviewManager.class);
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		EvaluationFormProvider evaluationFormProvider = getEvaluationFormProvider();
		
		IdentityEnvironment ienv = new IdentityEnvironment(assessedIdentity, Roles.userRoles());
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, courseEnv);
	
		AssessmentEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(this, userCourseEnv);
		EvaluationFormSession session = msService.getSession(courseEntry, getIdent(), evaluationFormProvider, assessedIdentity, done);
	
		Float updatedScore = getScore(msService, gtaManager, peerReviewManager, userCourseEnv, currentEval, session).totalScore();
		Float updatedWeightedScore = null;
		BigDecimal updatedScoreScale = null;
		if(ScoreScalingHelper.isEnabled(courseEnv)) {
			updatedScoreScale = ScoreScalingHelper.getScoreScale(this);
			updatedWeightedScore = ScoreScalingHelper.getWeightedFloatScore(updatedScore, updatedScoreScale);
		}
		ScoreEvaluation updateScoreEvaluation = MSScoreEvaluationAndDataHelper.getUpdateScoreEvaluation(userCourseEnv, this, locale, updatedScore);
		String updateGrade = updateScoreEvaluation.getGrade();
		String updateGradeSystemIdent = updateScoreEvaluation.getGradeSystemIdent();
		String updatePerformanceClassIdent = updateScoreEvaluation.getPerformanceClassIdent();
		Boolean updatedPassed = updateScoreEvaluation.getPassed();
		
		boolean needUpdate = !Objects.equals(updatedScore, currentEval.getScore())
				|| !Objects.equals(updatedWeightedScore, currentEval.getWeightedScore())
				|| !Objects.equals(updateGrade, currentEval.getGrade())
				|| !Objects.equals(updateGradeSystemIdent, currentEval.getGradeSystemIdent())
				|| !Objects.equals(updatePerformanceClassIdent, currentEval.getPerformanceClassIdent())
				|| !Objects.equals(updatedPassed, currentEval.getPassed())
				|| !ScoreScalingHelper.equals(updatedScoreScale, currentEval.getScoreScale());
		
		if (needUpdate) {
			ScoreEvaluation scoreEval = new ScoreEvaluation(updatedScore, updatedWeightedScore,
					updatedScoreScale, updateGrade, updateGradeSystemIdent,
					updatePerformanceClassIdent, updatedPassed, currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
					currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
					currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(this, scoreEval, userCourseEnv, doer, false, Role.coach);
			
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	public GTAScores getScore(MSService msService, GTAManager gtaManager, GTAPeerReviewManager peerReviewManager,
			UserCourseEnvironment assessedUserCourseEnv, AssessmentEvaluation currentEval, EvaluationFormSession session) {
		
		Float evaluationFormScore = getEvaluationFormScore(msService, session);
		Float peerReviewScore = getPeerReviewScore(gtaManager, peerReviewManager, assessedUserCourseEnv);
		Float awardedReviewScore = getAwardedReviewsScore(gtaManager, peerReviewManager, assessedUserCourseEnv);

		Float score = MinMax.add(evaluationFormScore, peerReviewScore);
		score = MinMax.add(score, awardedReviewScore);
		if (score == null) {
			score = currentEval.getScore();
		}

		// Score has to be in configured range.
		MinMax minMax = calculateMinMax(getModuleConfiguration());
		if (score != null) {
			if(minMax.getMax().floatValue() < score.floatValue()) {
				score = minMax.getMax();
			}
			if(minMax.getMin().floatValue() > score.floatValue()) {
				score = minMax.getMin();
			}
		}
		return new GTAScores(evaluationFormScore, peerReviewScore, awardedReviewScore, score, minMax);
	}
	
	public Float getEvaluationFormScore(MSService msService, EvaluationFormSession session) {
		Float score = null;
		ModuleConfiguration config = getModuleConfiguration();
		String scoreParts = config.getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
		boolean evalFormEnabled = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED);
		if(evalFormEnabled && scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM)) {
			String scoreConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
			String scaleConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_EVAL_FORM_SCALE);
			float scale = Float.parseFloat(scaleConfig);
			if (MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreConfig)) {
				score = msService.calculateScoreByAvg(session);
				score = msService.scaleScore(score, scale);
			} else if (MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig)) {
				score = msService.calculateScoreBySum(session);
				score = msService.scaleScore(score, scale);
			}
		}
		return score;
	}
	
	public Float getPeerReviewScore(GTAManager gtaManager, GTAPeerReviewManager peerReviewManager,
			UserCourseEnvironment assessedUserCourseEnv) {
		Float score = null;
		int numOfReviews = 0;
		ModuleConfiguration config = getModuleConfiguration();
		
		String scoreParts = config.getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
		boolean peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		if(peerReviewEnabled && scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW)) {
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			TaskList taskList = gtaManager.getTaskList(assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);
			Task task = gtaManager.getTask(assessedIdentity, taskList);
			if(task != null) {
				String scoreKey = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);
				boolean addSum = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey);
				boolean addAverage = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey);
				
				// Only done review are taken into account
				List<TaskReviewAssignmentStatus> statusForScore = List.of(TaskReviewAssignmentStatus.done);
				List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsForTask(task, false);
				SessionParticipationListStatistics reviewsStatistics = peerReviewManager.loadStatistics(task, assignments, this, statusForScore);
				for(SessionParticipationStatistics reviewsStatistic:reviewsStatistics.participationsStatistics()) {
					if(addSum) {
						score = (float)reviewsStatistic.statistics().sum() + (score == null ? 0.0f : score.floatValue());
					} else if(addAverage) {
						score = (float)reviewsStatistic.statistics().average() + (score == null ? 0.0f : score.floatValue());
					}
					numOfReviews++;
				}
			}
		}
		
		if(numOfReviews > 1 && score != null) {
			score = score.floatValue() / numOfReviews;
		}
		
		return score;
	}
	
	public Float getAwardedReviewsScore(GTAManager gtaManager, GTAPeerReviewManager peerReviewManager,
			UserCourseEnvironment assessedUserCourseEnv) {
		Float score = null;
		ModuleConfiguration config = getModuleConfiguration();

		boolean peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		String scoreParts = config.getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
		String pointsProReview = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW, "");
		String maxNumberOfReviews = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_MAX_NUMBER_CREDITABLE_REVIEWS);
		if(peerReviewEnabled && scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED)
				&& StringHelper.containsNonWhitespace(pointsProReview)) {
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			TaskList taskList = gtaManager.getTaskList(assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);
			if(taskList != null) {
				int numOfReviewsDone = 0;
				List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsOfReviewer(taskList, assessedIdentity);
				for(TaskReviewAssignment assignment:assignments) {
					if(assignment.getStatus() == TaskReviewAssignmentStatus.done) {
						numOfReviewsDone++;
					}
				}
				
				if(numOfReviewsDone > 0) {
					if(StringHelper.isLong(maxNumberOfReviews)) {
						int maxReviews = Integer.parseInt(maxNumberOfReviews);
						if(numOfReviewsDone > maxReviews) {
							numOfReviewsDone = maxReviews;
						}
					}
					score = (Float.parseFloat(pointsProReview) * numOfReviewsDone) + (score == null ? 0.0f : score.floatValue());
				}
			}
		}
		
		return score;
	}
	
	public void updateScoreEvaluation(Identity identity, UserCourseEnvironment assessedUserCourseEnv,
			Role by, EvaluationFormSession session, Locale locale) {
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		GTAPeerReviewManager peerReviewManager = CoreSpringFactory.getImpl(GTAPeerReviewManager.class);
		
		AssessmentEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(this);
		GTAScores detailledScores = getScore(msService, gtaManager, peerReviewManager, assessedUserCourseEnv, currentEval, session);
		Float score = detailledScores.totalScore();
		ScoreEvaluation updateEval = MSScoreEvaluationAndDataHelper.getUpdateScoreEvaluation(assessedUserCourseEnv, this, locale, score);
		
		// save
		ScoreEvaluation scoreEvaluation = new ScoreEvaluation(updateEval.getScore(), updateEval.getWeightedScore(),
				updateEval.getScoreScale(), currentEval.getGrade(),
				currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(), currentEval.getPassed(),
				currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
				currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
				currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		courseAssessmentService.saveScoreEvaluation(this, identity, scoreEvaluation, assessedUserCourseEnv, false, by);
	}
	
	private void updateCoachAssignment(ICourse course) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentService assessmentService = CoreSpringFactory.getImpl(AssessmentService.class);
		
		boolean assign = courseAssessmentService.getAssessmentConfig(courseEntry, this).hasCoachAssignment();
		boolean auto = assign && courseAssessmentService
				.getAssessmentConfig(courseEntry, this).getCoachAssignmentMode() == CoachAssignmentMode.automatic;
		if(auto) {
			List<Identity> identities = course.getCourseEnvironment().getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(null);
			List<AssessmentEntry> entries = assessmentService.getAssessmentEntryCoachAssignment(courseEntry, getIdent(), true);
			List<Identity> alreadyCoachedIdentities = entries.stream()
					.filter(entry -> entry.getCoach() != null)
					.map(AssessmentEntry::getIdentity)
					.toList();
			identities.removeAll(alreadyCoachedIdentities);
			
			for(Identity identity:identities) {
				UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
						.createAndInitUserCourseEnvironment(identity, course.getCourseEnvironment());
				AssessmentEntry entry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
				if(entry != null && entry.getCoach() == null) {
					courseAssessmentService.assignCoach(entry, null, course.getCourseEnvironment(), this);
				}
			}
		} else if(!assign) {
			List<AssessmentEntry> entries = assessmentService.getAssessmentEntryCoachAssignment(courseEntry, getIdent(), true);
			for(AssessmentEntry entry:entries) {
				entry.setCoach(null);
				assessmentService.updateAssessmentEntry(entry);
				DBFactory.getInstance().commit();
			}
		}
		DBFactory.getInstance().commitAndCloseSession();
	}
	
	public static EvaluationFormProvider getEvaluationFormProvider() {
		return new GTAEvaluationFormProvider();
	}
	
	public static EvaluationFormProvider getPeerReviewEvaluationFormProvider() {
		return new GTAPeerReviewEvaluationFormProvider();
	}

	@Override
	public void archiveForResetUserData(UserCourseEnvironment assessedUserCourseEnv, ZipOutputStream archiveStream, String path, Identity doer, Role by) {
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		TaskList taskList = gtaManager.getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), this);
		
		if(GTAType.group.name().equals(getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<BusinessGroup> assessedUserGroups = assessedUserCourseEnv.getParticipatingGroups();
			if(assessedUserGroups != null && !assessedUserGroups.isEmpty()) {
				List<BusinessGroup> selectedGroups = gtaManager.getBusinessGroups(this);
				List<BusinessGroup> taskGroups = new ArrayList<>(assessedUserGroups);
				taskGroups.retainAll(selectedGroups);
				for(BusinessGroup businessGroup:taskGroups) {
					archiveNodeData(courseEnv, businessGroup, taskList, false, path, archiveStream);
				}
			}
		} else {
			archiveNodeUserData(courseEnv, assessedIdentity, taskList, false, archiveStream, path);
			MSScoreEvaluationAndDataHelper.archiveForResetUserData(assessedUserCourseEnv, archiveStream, path, this, getEvaluationFormProvider());
		}
		
		super.archiveForResetUserData(assessedUserCourseEnv, archiveStream, path, doer, by);
	}

	@Override
	public void resetUserData(UserCourseEnvironment assessedUserCourseEnv, Identity identity, Role by) {
		boolean resetTaskData = GTAType.individual.name().equals(getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE));
		if(resetTaskData) {
			MSScoreEvaluationAndDataHelper.resetUserData(assessedUserCourseEnv, this, getEvaluationFormProvider(), identity, by);
			resetUserTaskData(assessedUserCourseEnv, identity);
		}
		super.resetUserData(assessedUserCourseEnv, identity, by);
	}
	
	public void resetGroupTaskData(BusinessGroup businessGroup, CourseEnvironment courseEnv, Identity doer) {
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		TaskList taskList = gtaManager.getTaskList(courseEntry, this);
		if(taskList == null) {
			return; // No task configured, nothing to do
		}
		
		Task task = gtaManager.getTask(businessGroup, taskList);
		if(task != null) {
			gtaManager.resetCourseNode(task, null, businessGroup, this, courseEnv, doer);
		}
	}

	private void resetUserTaskData(UserCourseEnvironment assessedUserCourseEnv,  Identity identity) {
		GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		TaskList taskList = gtaManager.getTaskList(courseEntry, this);
		if(taskList == null) {
			return; // No task configured, nothing to do
		}

		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null) {
			gtaManager.resetCourseNode(task, assessedIdentity, null, this, courseEnv, identity);
		}
	}

	@Override
	public CourseNodeReminderProvider getReminderProvider(RepositoryEntryRef courseEntry, boolean rootNode) {
		return new GTAReminderProvider(courseEntry, this);
	}
	
	@Override
	public List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel() {
		return List.of(
				Map.entry("gtask.assignment.deadline", getDueDateConfig(GTASK_ASSIGNMENT_DEADLINE)),
				Map.entry("gtask.submission.deadline", getDueDateConfig(GTASK_SUBMIT_DEADLINE)),
				Map.entry("gtask.late.submit.deadline", getDueDateConfig(GTASK_LATE_SUBMIT_DEADLINE)),
				Map.entry("gtask.peerreview.deadline", getDueDateConfig(GTASK_PEER_REVIEW_DEADLINE)),
				Map.entry("gtask.submission.visibility", getDueDateConfig(GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER))
			);
	}
	
	@Override
	public DueDateConfig getDueDateConfig(String key) {
		if (GTASK_ASSIGNMENT_DEADLINE.equals(key)) {
			return getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
					? DueDateConfig.ofCourseNode(this, GTASK_RELATIVE_DATES, GTASK_ASSIGNMENT_DEADLINE,
							GTASK_ASSIGNMENT_DEADLINE_RELATIVE, GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO)
					: DueDateConfig.noDueDateConfig();
		} else if (GTASK_SUBMIT_DEADLINE.equals(key)) {
			return getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
					? DueDateConfig.ofCourseNode(this, GTASK_RELATIVE_DATES, GTASK_SUBMIT_DEADLINE,
							GTASK_SUBMIT_DEADLINE_RELATIVE, GTASK_SUBMIT_DEADLINE_RELATIVE_TO)
					: DueDateConfig.noDueDateConfig();
		} else if (GTASK_LATE_SUBMIT_DEADLINE.equals(key)) {
			return getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_LATE_SUBMIT)
					? DueDateConfig.ofCourseNode(this, GTASK_RELATIVE_DATES, GTASK_LATE_SUBMIT_DEADLINE,
							GTASK_LATE_SUBMIT_DEADLINE_RELATIVE, GTASK_LATE_SUBMIT_DEADLINE_RELATIVE_TO)
					: DueDateConfig.noDueDateConfig();
		} else if (GTASK_PEER_REVIEW_DEADLINE.equals(key)) {
			return getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW)
					? DueDateConfig.ofPeriodCourseNode(this, GTASK_RELATIVE_DATES,
							GTASK_PEER_REVIEW_DEADLINE_START, GTASK_PEER_REVIEW_DEADLINE,
							GTASK_PEER_REVIEW_DEADLINE_RELATIVE, GTASK_PEER_REVIEW_DEADLINE_RELATIVE_TO,
							GTASK_PEER_REVIEW_DEADLINE_LENGTH)
					: DueDateConfig.noDueDateConfig();
		} else if (GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER.equals(key)) {
			return getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)
					? DueDateConfig.ofCourseNode(this, GTASK_RELATIVE_DATES, GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER,
								GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE, GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO)
					: DueDateConfig.noDueDateConfig();
		}
		return super.getDueDateConfig(key);
	}

	/**
	 * @param courseEnv
	 * @param node
	 * @return the relative base path for this node
	 */
	public static String getGTasksNodePathRelToFolderBase(CourseEnvironment courseEnv, CourseNode node) {
		return getGTasksNodesPathRelToFolderBase(courseEnv) + "/" + node.getIdent();
	}

	/**
	 * @param courseEnv
	 * @return the relative base path for this node
	 */
	public static String getGTasksNodesPathRelToFolderBase(CourseEnvironment courseEnv) {
		return courseEnv.getCourseBaseContainer().getRelPath() + "/gtasks";
	}

	/**
	 *
	 * @param node
	 * @param courseEnv
	 * @return
	 */
	public static VFSContainer getGTasksFolderContainer(GTACourseNode node, CourseEnvironment courseEnv) {
		String path = getGTasksNodePathRelToFolderBase(courseEnv, node);
		VFSContainer rootFolder = VFSManager.olatRootContainer(path, null);
		return new NamedContainerImpl(node.getShortTitle(), rootFolder);
	}

	/**
	 *
	 * @param node
	 * @param courseEnv
	 * @return number of available assessmentDocuments for current node
	 */
	public static int getAssessmentDocsCount(GTACourseNode node, CourseEnvironment courseEnv) {
		return getAssessmentDocsRelPathToFolderBase(node, courseEnv).getItems().stream().mapToInt(l -> VFSManager.olatRootContainer(l.getRelPath()).getItems().size()).sum();
	}

	/**
	 *
	 * @param node
	 * @param courseEnv
	 * @return vfsContainer for assessmentDocuments
	 */
	public static VFSContainer getAssessmentDocsRelPathToFolderBase(GTACourseNode node, CourseEnvironment courseEnv) {
		String path = getGTasksAssessmentDocsPathRelToFolderBase(courseEnv);
		String pathToNodeContainer = VFSManager.olatRootContainer(path, null).getRelPath() + "/" + node.getIdent();
		VFSContainer rootFolder = VFSManager.olatRootContainer(pathToNodeContainer);

		return new NamedContainerImpl(node.getShortTitle(),rootFolder);
	}

	/**
	 *
	 * @param courseEnv
	 * @return relative base path for assessmentDocuments
	 */
	public static String getGTasksAssessmentDocsPathRelToFolderBase(CourseEnvironment courseEnv) {
		return courseEnv.getCourseBaseContainer().getRelPath() + "/assessmentdocs";
	}

	@Override
	public Quota getQuota(Identity identity, Roles roles, RepositoryEntry entry, QuotaManager quotaManager) {
		return null;
	}

	@Override
	public Long getUsageKb(CourseEnvironment courseEnvironment) {
		// return taskDocuments and assessmentDocuments as sum
		return VFSManager.getUsageKB(getNodeContainer(courseEnvironment))
				+ getAssessmentDocsRelPathToFolderBase(this, courseEnvironment).getItems()
				.stream()
				.map(ad -> VFSManager.getUsageKB(VFSManager.olatRootContainer(ad.getRelPath())))
				.findFirst()
				.orElse(0L);
	}

	@Override
	public String getRelPath(CourseEnvironment courseEnvironment) {
		return getNodeContainer(courseEnvironment).getRelPath();
	}

	@Override
	public Integer getNumOfFiles(CourseEnvironment courseEnvironment) {
		return getAssessmentDocsCount(this, courseEnvironment);
	}

	private VFSContainer getNodeContainer(CourseEnvironment courseEnvironment) {
		return getGTasksFolderContainer(this, courseEnvironment);
	}

	@Override
	public boolean isStorageExtern() {
		return false;
	}

	@Override
	public boolean isStorageInCourseFolder() {
		return false;
	}

	@Override
	public Controller createDefaultsController(UserRequest ureq, WindowControl wControl) {
		Controller controller;
		Translator fnGroupTranslator = Util.createPackageTranslator(EditorMainController.class, ureq.getLocale());
		CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(getType());
		String courseNodeTitle = fnGroupTranslator.translate(getGroup()) + " - <div class='" + CSSHelper.getIconCssClassFor(cnConfig.getIconCSSClass()) + "'></div>" + cnConfig.getLinkText(ureq.getLocale());
		controller = new GTADefaultsEditController(ureq, wControl, courseNodeTitle);
		return controller;
	}

	@Override
	public String getCourseNodeConfigManualUrl() {
		return "manual_user/learningresources/Course_Element_Task/#tab-workflow";
	}

	@Override
	public String getGroup() {
		return CourseNodeGroup.assessment.name();
	}
}