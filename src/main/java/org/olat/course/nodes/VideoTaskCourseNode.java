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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodes.video.VideoEditController;
import org.olat.course.nodes.videotask.VideoTaskAssessmentConfig;
import org.olat.course.nodes.videotask.VideoTaskLearningPathNodeHandler;
import org.olat.course.nodes.videotask.manager.VideoTaskArchiveFormat;
import org.olat.course.nodes.videotask.model.VideoTaskArchiveSearchParams;
import org.olat.course.nodes.videotask.ui.VideoTaskCoachRunController;
import org.olat.course.nodes.videotask.ui.VideoTaskEditController;
import org.olat.course.nodes.videotask.ui.VideoTaskRunController;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionComparator;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskScore;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 17 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = -1982808904929647643L;

	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(VideoTaskEditController.class);
	
	public static final String TYPE = "videotask";

	public VideoTaskCourseNode() {
		super(TYPE);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		VideoTaskEditController childTabCntrllr = new VideoTaskEditController(ureq, wControl, course, this);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}
	
	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return VideoTaskEditController.getVideoReference(getModuleConfiguration(), false);
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}
	
	@Override
	public CourseNodeReminderProvider getReminderProvider(RepositoryEntryRef courseEntry, boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new VideoTaskAssessmentConfig(courseEntry, this));
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd,
			VisibilityFilter visibilityFilter) {
		Controller controller;
		// Do not allow guests to start tests
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			controller = new VideoTaskCoachRunController(ureq, wControl, this, userCourseEnv);
		} else {
			controller = new VideoTaskRunController(ureq, wControl, this, userCourseEnv, true);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_icon_videotask");
		return new NodeRunConstructionResult(ctrl);
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
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(2);
		RepositoryEntry videoEntry = VideoTaskEditController.getVideoReference(getModuleConfiguration(), false);
		if (videoEntry == null) {
			addStatusErrorDescription("no.video.resource.selected", "error.noreference.long", VideoTaskEditController.PANE_TAB_VIDEOCONFIG, sdList);
		} else if (RepositoryEntryStatusEnum.deleted == videoEntry.getEntryStatus()
					|| RepositoryEntryStatusEnum.trash == videoEntry.getEntryStatus()) {	
			addStatusErrorDescription("video.deleted", "error.noreference.long", VideoTaskEditController.PANE_TAB_VIDEOCONFIG, sdList);
		}
		
		if (cev != null) {
			AssessmentConfig assessmentConfig = new VideoTaskAssessmentConfig(new CourseEntryRef(cev), this);
			
			if (isFullyAssessedScoreConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			if (isFullyAssessedPassedConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			
			if (getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED) && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
				GradeScale gradeScale = gradeService.getGradeScale(cev.getCourseGroupManager().getCourseEntry(), getIdent());
				if (gradeScale == null) {
					addStatusErrorDescription("error.missing.grade.scale", "error.fully.assessed.passed",
							VideoTaskEditController.PANE_TAB_ASSESSMENT, sdList);
				}
			}
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError(AssessmentConfig assessmentConfig) {
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(VideoTaskLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError(AssessmentConfig assessmentConfig) {
		boolean hasPassed = assessmentConfig.getPassedMode() != Mode.none;
		boolean isPassedTrigger = CoreSpringFactory.getImpl(VideoTaskLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}

	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
			List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(ValidationStatus.ERROR, shortDescKey, longDescKey, params,
				TRANSLATOR_PACKAGE);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}
	
	public VideoDisplayOptions getVideoDisplay(boolean readOnly, boolean startFullScreen) {
		// configure the display controller according to config
		ModuleConfiguration config = getModuleConfiguration();
		boolean forwardSeekingRestrictred = config.getBooleanSafe(VideoEditController.CONFIG_KEY_FORWARD_SEEKING_RESTRICTED);
		boolean title = config.getBooleanSafe(VideoEditController.CONFIG_KEY_TITLE);
		boolean showAnnotations = config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_ANNOTATIONS, false);
		boolean showQuestions = config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_QUESTIONS, false);
		boolean showOverlayComments = config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_OVERLAY_COMMENTS, true);

		VideoDisplayOptions displayOptions = VideoDisplayOptions.valueOf(false, false, false, false,
				title, false, false, null, false, readOnly, forwardSeekingRestrictred, false);
		displayOptions.setShowQuestions(showQuestions);
		displayOptions.setShowAnnotations(showAnnotations);
		displayOptions.setShowSegments(true);
		displayOptions.setShowOverlayComments(showOverlayComments);
		displayOptions.setShowDescription(false);
		displayOptions.setResponseAtEnd(true);
		displayOptions.setProgressFullWidth(true);
		displayOptions.setAlwaysShowControls(true);
		displayOptions.setStartFullScreen(startFullScreen);
		return displayOptions;
	}
	
	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		VideoAssessmentService videoAssessmentService = CoreSpringFactory.getImpl(VideoAssessmentService.class);
		
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEntry videoEntry = getReferencedRepositoryEntry();
		Long videoEntryKey = videoEntry.getKey();

		Float maxScore = (Float) getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		Float cutValue = (Float) getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		int rounding = getModuleConfiguration().getIntegerSafe(VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING,
				VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING_DEFAULT);

		AssessmentService assessmentService = CoreSpringFactory.getImpl(AssessmentService.class);
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, this);
		boolean gradeEnabled = CoreSpringFactory.getImpl(GradeModule.class).isEnabled();
		GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
		NavigableSet<GradeScoreRange> gradeScoreRanges = null;
		if (gradeEnabled && assessmentConfig.hasGrade()) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, this.getIdent());
			gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
		}
		
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdent(courseEntry, getIdent());
		List<VideoTaskSession> taskSessions = videoAssessmentService.getTaskSessions(courseEntry, getIdent());
		Map<Long, List<VideoTaskSession>> identityKeyToSessions = taskSessions.stream()
				.filter(session -> session.getFinishTime() != null)
				.collect(Collectors.groupingBy(session -> session.getIdentity().getKey()));
		
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			VideoTaskSession taskSession = getLastTaskSession(assessmentEntry, identityKeyToSessions);
			AssessmentEvaluation currentEval = courseAssessmentService.toAssessmentEvaluation(assessmentEntry, assessmentConfig);
			VideoTaskScore scoring = null;
			if(taskSession != null) {
				scoring = videoAssessmentService.calculateScore(taskSession, maxScore, cutValue, rounding);
			}
			
			String grade = null;
			String gradeSystemIdent = null;
			String performanceClassIdent = null;
			Boolean passed = null;
			BigDecimal score = scoring == null ? null : scoring.score();
			Float scoreAsFloat = scoring == null ? null : scoring.scoreAsFloat();
			
			if (gradeEnabled && assessmentConfig.hasGrade()) {
				if (assessmentConfig.isAutoGrade() || StringHelper.containsNonWhitespace(currentEval.getGrade())) {
					if (scoreAsFloat != null) {
						GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreAsFloat);
						grade = gradeScoreRange.getGrade();
						gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
						performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
						passed = gradeScoreRange.getPassed();
					}
				}
			} else {
				passed = scoring == null ? null : scoring.passed();
			}
			
			boolean hasChanges = !Objects.equals(grade, assessmentEntry.getGrade())
					|| !Objects.equals(gradeSystemIdent, assessmentEntry.getGradeSystemIdent())
					|| !Objects.equals(performanceClassIdent, assessmentEntry.getPerformanceClassIdent())
					|| !Objects.equals(passed, assessmentEntry.getPassed())
					|| !Objects.equals(score, assessmentEntry.getScore());
			
			if (hasChanges
					|| assessmentEntry.getReferenceEntry() == null
					|| !videoEntryKey.equals(assessmentEntry.getReferenceEntry().getKey())) {
				IdentityEnvironment ienv = new IdentityEnvironment(assessmentEntry.getIdentity(), Roles.userRoles());
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
				ScoreEvaluation scoreEval = new ScoreEvaluation(scoreAsFloat, grade, gradeSystemIdent,
						performanceClassIdent, passed, currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
						currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
						currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
				courseAssessmentService.updateScoreEvaluation(this, scoreEval, uce, publisher, false, Role.coach);
				
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
	private VideoTaskSession getLastTaskSession(AssessmentEntry entry, Map<Long, List<VideoTaskSession>> identityKeyToSessions) {
		List<VideoTaskSession> taskSessions = identityKeyToSessions.get(entry.getIdentity().getKey());
		if(taskSessions == null || taskSessions.isEmpty()) {
			return null;
		}
		
		if(taskSessions.size() > 1) {
			Collections.sort(taskSessions, new VideoTaskSessionComparator(true));
		}
		return taskSessions.get(0);
	}

	public void promoteTaskSession(VideoTaskSession taskSession, UserCourseEnvironment assessedUserCourseEnv,
			boolean updateScoring, Identity coachingIdentity, Role by, Locale locale) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentEntry currentAssessmentEntry = null;
		
		Float score = null;
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		Boolean passed = null;
		if(updateScoring) {
			Float cutValue = (Float)getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);

			BigDecimal finalScore = taskSession.getScore();
			score = finalScore == null ? null : finalScore.floatValue();
			passed = taskSession.getPassed();
			if(finalScore != null) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(assessedUserCourseEnv), this);
				if (assessmentConfig.hasGrade() && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
					currentAssessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
					if (assessmentConfig.isAutoGrade() || (currentAssessmentEntry != null && StringHelper.containsNonWhitespace(currentAssessmentEntry.getGrade()))) {
						GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
						GradeScale gradeScale = gradeService.getGradeScale(
								assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
								this.getIdent());
						NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
						GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
						grade = gradeScoreRange.getGrade();
						gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
						performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
						passed = gradeScoreRange.getPassed();
					}
				} else if (cutValue != null) {
					boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
					passed = Boolean.valueOf(calculated);
				}
			}
		}
		
		if (currentAssessmentEntry == null) {
			currentAssessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
		}
		boolean increment = currentAssessmentEntry.getAttempts() == null || currentAssessmentEntry.getAttempts() == 0;
		ScoreEvaluation sceval = new ScoreEvaluation(score, grade, gradeSystemIdent, performanceClassIdent, passed,
				null, null, null, 1.0d, AssessmentRunStatus.done, taskSession.getKey());
		courseAssessmentService.updateScoreEvaluation(this, sceval, assessedUserCourseEnv, coachingIdentity, increment, by);
	}
	
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEntry videoEntry = getReferencedRepositoryEntry();
		VideoTaskArchiveSearchParams searchParams = new VideoTaskArchiveSearchParams(courseEntry, videoEntry, this);
		VideoTaskArchiveFormat vaf = new VideoTaskArchiveFormat(locale, searchParams);
		vaf.exportCourseElement(exportStream, this, archivePath);
		return true;
	}

	@Override
	public void resetUserData(UserCourseEnvironment assessedUserCourseEnv, Identity identity, Role by) {
		super.resetUserData(assessedUserCourseEnv, identity, by);
		
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		
		VideoAssessmentService videoAssessmentService = CoreSpringFactory.getImpl(VideoAssessmentService.class);
		List<VideoTaskSession> sessions = videoAssessmentService.getTaskSessions(courseEntry, getIdent(), assessedIdentity);
		for(VideoTaskSession session:sessions) {
			if(!session.isCancelled()) {
				session.setCancelled(true);
				videoAssessmentService.updateTaskSession(session);
			}
		}
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);

		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VideoAssessmentService videoAssessmentService = CoreSpringFactory.getImpl(VideoAssessmentService.class);
		videoAssessmentService.deleteTaskSessions(courseEntry, getIdent());
	}
}
