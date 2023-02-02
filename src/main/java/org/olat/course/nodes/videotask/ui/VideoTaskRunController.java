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
package org.olat.course.nodes.videotask.ui;


import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.gradeSystem;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.AssessmentDocumentsSupplier;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.PanelInfo;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.videotask.ui.components.FinishEvent;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskRunController extends BasicController implements GenericEventListener, Activateable2, AssessmentDocumentsSupplier {
	
	private Link startButton;
	private final VelocityContainer myContent;
	
	private boolean showLog;
	private final boolean anonym;
	private final PanelInfo panelInfo;
	private final boolean assessmentType;
	private final VideoSegments segments;
	private EventBus singleUserEventCenter;
	private final RepositoryEntry videoEntry;
	private AssessmentEvaluation assessmentEval;
	private UserCourseEnvironment userCourseEnv;
	private final VideoTaskCourseNode courseNode;
	private final AssessmentConfig assessmentConfig;

	private VideoDisplayController displayCtrl;
	private VideoTaskDisplayController displayContainerCtrl;
	private AssessmentParticipantViewController assessmentParticipantViewCtrl;

	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public VideoTaskRunController(UserRequest ureq, WindowControl wControl,
			VideoTaskCourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean showLog) {
		super(ureq, wControl);
		this.showLog = showLog;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		assessmentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(userCourseEnv), courseNode);
		this.panelInfo = new PanelInfo(VideoTaskRunController.class,
				"::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent());
		this.assessmentType = assessmentConfig.isAssessable();
		videoEntry = courseNode.getReferencedRepositoryEntry();
		segments = videoManager.loadSegments(videoEntry.getOlatResource());
		
		UserSession usess = ureq.getUserSession();
		anonym = usess.getRoles().isGuestOnly();
		singleUserEventCenter = usess.getSingleUserEventCenter();
		
		myContent = createVelocityContainer("run");
		
		assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, wControl, assessmentEval,
				assessmentConfig, this, gradeSystem(userCourseEnv, courseNode), panelInfo);
		listenTo(assessmentParticipantViewCtrl);
		myContent.put("assessment", assessmentParticipantViewCtrl.getInitialComponent());
		
		exposeUserDataToVC();

		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());
		
		init(ureq);
		putInitialPanel(myContent);
	}
	
	@Override
	public boolean isDownloadEnabled() {
		return true;
	}
	
	@Override
	public List<File> getIndividualAssessmentDocuments() {
		return courseAssessmentService.getIndividualAssessmentDocuments(courseNode, userCourseEnv);
	}
	
	private void init(UserRequest ureq) {
		startButton = LinkFactory.createButton("start.video", myContent, this);
		startButton.setElementCssClass("o_sel_start_videotask");
		startButton.setPrimary(true);
		startButton.setVisible(!userCourseEnv.isCourseReadOnly());
		if (!userCourseEnv.isParticipant() && !anonym) {
			startButton.setCustomDisplayText(translate("preview"));
		}
		
		myContent.contextPut("assessmentType", Boolean.valueOf(assessmentType));
		
		initCategories();
		initMetadata();

		if (assessmentType) {
			checkChats(ureq);
			singleUserEventCenter.registerFor(this, getIdentity(), InstantMessagingService.TOWER_EVENT_ORES);
		}
	}
	
	private void initMetadata() {
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		int maxAttempts = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, 0);
		if(maxAttempts > 0) {
			int attempts = assessmentEval.getAttempts() == null ? 0 : assessmentEval.getAttempts().intValue();
			myContent.contextPut("attempts", translate("informations.attempts",
					Integer.toString(attempts), Integer.toString(maxAttempts)));
			myContent.contextPut("maxAttempts", Integer.valueOf(maxAttempts));
			myContent.contextPut("numOfAttempts", Integer.valueOf(attempts));
			myContent.contextPut("attemptsConfig", Boolean.TRUE);
		} else {
			myContent.contextPut("attemptsConfig", Boolean.FALSE);
		}
		
		String mode = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE);
		if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode)) {
			initElements();
			myContent.contextPut("modeTitle", translate("practice.assign.title"));
			myContent.contextPut("modeMsg", translate("practice.assign.description"));
		} else if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS.equals(mode)) {
			myContent.contextPut("modeTitle", translate("practice.identify.title"));
			myContent.contextPut("modeMsg", translate("practice.identify.description"));
		} else if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)) {
			myContent.contextPut("modeTitle", translate("test.title"));
			myContent.contextPut("modeMsg", translate("test.description"));
		}
		
		String instruction = courseNode.getInstruction();
		if(StringHelper.containsNonWhitespace(instruction) && StringHelper.isHtml(instruction)) {
			myContent.contextPut("instructions", instruction);
		} else {
			myContent.contextPut("instructions", "<p>" + instruction + "</p>");
		}
		
		VideoMeta metadata = videoManager.getVideoMetadata(videoEntry.getOlatResource());
		if(metadata != null) {
			long timeSec = VideoHelper.durationInSeconds(metadata.getLength());
			if(timeSec > 0) {
				long minutes = (timeSec  / 60l) % 60;
				long seconds = timeSec - (minutes * 60);
				myContent.contextPut("videoLength", translate("informations.length",
						Long.toString(minutes), Long.toString(seconds)));
			}
		}
	}
	
	/**
	 * Instructions with number of segments, categories...
	 */
	private void initElements() {
		int numOfSegments = 0;

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		final List<String> categories = config.getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		Set<String> selectedCategories = new HashSet<>();
		if(segments != null) {
			for(VideoSegmentCategory category:segments.getCategories()) {
				if(categories.contains(category.getId())) {
					selectedCategories.add(category.getId());
				}
			}
			
			for(VideoSegment segment:segments.getSegments()) {
				if(selectedCategories.contains(segment.getCategoryId())) {
					numOfSegments++;
				}
			}
		} 

		String i18nCategoriesKey = selectedCategories.size() <= 1 ? "elements.instructions.category" : "elements.instructions.categories";
		String elements = translate(i18nCategoriesKey, Integer.toString(selectedCategories.size()));
		String i18nSegmentKey = numOfSegments <= 1 ? "elements.instructions.segment" : "elements.instructions.segments";
		elements += " / " + translate(i18nSegmentKey, Integer.toString(numOfSegments));
		int attemptsPerSegmentInt = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT,
				VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT_DEFAULT);
		if(attemptsPerSegmentInt > 0) {
			String i18nAttemptsKey = attemptsPerSegmentInt <= 1 ? "elements.instructions.attempt" : "elements.instructions.attempts";
			elements += " / " + translate(i18nAttemptsKey, Integer.toString(attemptsPerSegmentInt));
		}
		myContent.contextPut("elements", elements);
	}
	
	private int initCategories() {
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		final List<String> categories = config.getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		List<VideoSegmentCategory> selectedSegmentCategories;
		if(segments != null) {
			selectedSegmentCategories = segments.getCategories().stream()
					.filter(cat -> categories.contains(cat.getId()))
					.collect(Collectors.toList());
			VideoTaskHelper.sortCategories(selectedSegmentCategories, courseNode, getLocale());
		} else {
			selectedSegmentCategories = List.of();
		}
		myContent.contextPut("segmentCategories", selectedSegmentCategories);
		return selectedSegmentCategories.size();
	}
	
	private void exposeUserDataToVC() {
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		if(resultsVisible && showLog) {
			UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
			String userLog = am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity());
			myContent.contextPut("log", StringHelper.escapeHtml(userLog));
		}
	}
	
	private void checkChats(UserRequest ureq) {
		List<?> allChats = null;
		if (ureq != null) {
			allChats = ureq.getUserSession().getChats();
		}
		if (allChats == null || allChats.isEmpty()) {
			startButton.setEnabled(true);
			myContent.contextPut("hasChatWindowOpen", false);
		} else {
			startButton.setEnabled(false);
			myContent.contextPut("hasChatWindowOpen", true);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	public void event(Event event) {
		if (assessmentType &&event.getCommand().startsWith("ChatWindow")) {
			checkChats(null);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(startButton == source) {
			doStart(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(displayContainerCtrl == source) {
			if(event instanceof FinishEvent fe) {
				submit(fe.getTaskSession());
				doFinishTask(ureq);
				if(fe.isStartNextAttempt()) {
					doStart(ureq);
				}
			}
		}
	}
	
	private void doStart(UserRequest ureq) {
		removeAsListenerAndDispose(displayCtrl);
		removeAsListenerAndDispose(displayContainerCtrl);

		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck("Play");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		boolean readOnly = userCourseEnv.isCourseReadOnly();
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		displayCtrl = new VideoDisplayController(ureq, bwControl, videoEntry, courseEntry,
				courseNode, courseNode.getVideoDisplay(readOnly));
		listenTo(displayCtrl);
		
		List<String> categoriesIds = courseNode.getModuleConfiguration()
				.getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		
		Integer attempt = courseAssessmentService.getAttempts(courseNode, userCourseEnv);
		int currentAttempt = attempt == null ? 0 : attempt.intValue();
		displayContainerCtrl = new VideoTaskDisplayController(ureq, bwControl, displayCtrl,
				videoEntry, courseEntry, courseNode, categoriesIds, false, false, currentAttempt);
		listenTo(displayContainerCtrl);
		
		displayContainerCtrl.setAsFullscreen(ureq);
		displayContainerCtrl.activate();
	}
	
	private void submit(VideoTaskSession taskSession) {
		// Session only in test / assessment mode, not in practice
		if(assessmentType) {
			submitTask(taskSession);
		} else {
			courseAssessmentService.incrementAttempts(courseNode, userCourseEnv, Role.user);
		}

		AssessmentEvaluation assessmentEvaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		AssessmentRunStatus runStatus = assessmentEvaluation.getAttempts() != null && assessmentEvaluation.getAttempts() > 0
				? AssessmentRunStatus.done: null;
		courseAssessmentService.updateCurrentCompletion(courseNode, userCourseEnv, null, null, runStatus, Role.user);
	}
	
	private void submitTask(VideoTaskSession taskSession) {
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		Boolean pass = taskSession.getPassed();
		
		float fScore = taskSession.getScore() == null ? 0.0f : taskSession.getScore().floatValue();
		Float score = Float.valueOf(fScore);
		Boolean updatePass = pass;
		if(assessmentConfig.hasGrade() && score != null && gradeModule.isEnabled()) {
			if (assessmentConfig.isAutoGrade() || isAssessmentWithGrade()) {
				GradeScale gradeScale = gradeService.getGradeScale(
						userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
						courseNode.getIdent());
				NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
				GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
				grade = gradeScoreRange.getGrade();
				gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
				performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
				updatePass = gradeScoreRange.getPassed();
			} else {
				updatePass = null;
			}
		}
		
		Boolean visibility;
		AssessmentEntryStatus assessmentStatus;
		String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		if(IQEditController.CORRECTION_MANUAL.equals(correctionMode) || IQEditController.CORRECTION_GRADING.equals(correctionMode)) {
			assessmentStatus = AssessmentEntryStatus.inReview;
			visibility = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv).getUserVisible();
		} else {
			assessmentStatus = AssessmentEntryStatus.done;
			visibility = Boolean.TRUE;
		}
		
		ScoreEvaluation sceval = new ScoreEvaluation(score, grade, gradeSystemIdent, performanceClassIdent,
				updatePass, assessmentStatus, visibility, taskSession.getCreationDate(), null,
				AssessmentRunStatus.done, taskSession.getKey());
		
		courseAssessmentService.updateScoreEvaluation(courseNode, sceval, userCourseEnv, getIdentity(),
				true, Role.user);
	}
	
	private boolean isAssessmentWithGrade() {
		return StringHelper.containsNonWhitespace(courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv).getGrade());
	}
	
	private void doFinishTask(UserRequest ureq) {
		displayContainerCtrl.deactivate(ureq);
		removeAsListenerAndDispose(assessmentParticipantViewCtrl);
		removeAsListenerAndDispose(displayContainerCtrl);
		removeAsListenerAndDispose(displayCtrl);
		displayContainerCtrl = null;
		displayCtrl = null;
		
		assessmentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);

		assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, getWindowControl(), assessmentEval,
				assessmentConfig, this, gradeSystem(userCourseEnv, courseNode), panelInfo);
		listenTo(assessmentParticipantViewCtrl);
		myContent.put("assessment", assessmentParticipantViewCtrl.getInitialComponent());
		
		initMetadata();
	}
}
