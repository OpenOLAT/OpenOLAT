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
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.video.VideoEditController;
import org.olat.course.nodes.videotask.VideoTaskAssessmentConfig;
import org.olat.course.nodes.videotask.ui.VideoTaskCoachRunController;
import org.olat.course.nodes.videotask.ui.VideoTaskEditController;
import org.olat.course.nodes.videotask.ui.VideoTaskRunController;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.video.VideoTaskSession;
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
		return new AssessmentReminderProvider(getIdent(), new VideoTaskAssessmentConfig(this));
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
		
		List<StatusDescription> statusDescs = validateInternalConfiguration();
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
		sds.addAll(validateInternalConfiguration());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	private List<StatusDescription> validateInternalConfiguration() {
		List<StatusDescription> sdList = new ArrayList<>(2);
		RepositoryEntry videoEntry = VideoTaskEditController.getVideoReference(getModuleConfiguration(), false);
		if (videoEntry == null) {
			addStatusErrorDescription("no.video.chosen", "error.noreference.long", VideoTaskEditController.PANE_TAB_VIDEOCONFIG, sdList);
		} else if (RepositoryEntryStatusEnum.deleted == videoEntry.getEntryStatus()
					|| RepositoryEntryStatusEnum.trash == videoEntry.getEntryStatus()) {	
			addStatusErrorDescription("video.deleted", "error.noreference.long", VideoTaskEditController.PANE_TAB_VIDEOCONFIG, sdList);
		}
		return sdList;
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
	
	public VideoDisplayOptions getVideoDisplay(RepositoryEntry videoEntry, boolean readOnly) {
		// configure the display controller according to config
		ModuleConfiguration config = getModuleConfiguration();
		boolean forwardSeekingRestrictred = config.getBooleanSafe(VideoEditController.CONFIG_KEY_FORWARD_SEEKING_RESTRICTED);
		boolean title = config.getBooleanSafe(VideoEditController.CONFIG_KEY_TITLE);
		boolean showAnnotations = config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_ANNOTATIONS, true);
		boolean showQuestions = config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_QUESTIONS, true);
		boolean showSegments = config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_SEGMENTS, false);
		
		VideoDisplayOptions displayOptions = VideoDisplayOptions.valueOf(false, false, false, false,
				title, false, false, null, false, readOnly, forwardSeekingRestrictred);
		displayOptions.setShowQuestions(showQuestions);
		displayOptions.setShowAnnotations(showAnnotations);
		displayOptions.setShowSegments(showSegments);
		displayOptions.setShowDescription(false);
		displayOptions.setResponseAtEnd(true);
		return displayOptions;
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
		boolean increment = currentAssessmentEntry.getAttempts() == null || currentAssessmentEntry.getAttempts().intValue() == 0;
		ScoreEvaluation sceval = new ScoreEvaluation(score, grade, gradeSystemIdent, performanceClassIdent, passed,
				null, null, null, 1.0d, AssessmentRunStatus.done, taskSession.getKey());
		courseAssessmentService.updateScoreEvaluation(this, sceval, assessedUserCourseEnv, coachingIdentity, increment, by);
	}

	
}
