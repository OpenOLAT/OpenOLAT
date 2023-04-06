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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.videotask.ui.components.FinishEvent;
import org.olat.course.nodes.videotask.ui.components.RestartEvent;
import org.olat.course.nodes.videotask.ui.components.ShowSolutionEvent;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskScore;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskDisplayController extends BasicController {
	
	private final Link closeButton;
	private ChiefController thebaseChief;
	private final StackedPanel mainPanel;
	private final VelocityContainer mainVC;
	
	private final Identity assessedIdentity;
	private final String anonymousIdentifier;
	
	private String subIdent;
	private boolean fullScreen;
	private RepositoryEntry entry;
	private final boolean authorMode;
	private RepositoryEntry videoEntry;
	private VideoTaskSession taskSession;
	private final VideoSegments segments;
	private final int maxAttempts;
	private final int currentAttempt;
	private final String mode;
	private final Float maxScore;
	private final Float cutValue;
	private final int rounding;
	private final int maxAttemptsPerSegments;
	private final Double weightWrongAnswers;

	private CloseableModalController cmc;
	private Controller confirmEndTaskCtrl;
	private final SegmentsController segmentsCtrl;
	private final VideoDisplayController displayCtrl;
	private ConfirmFinishTaskController confirmFinishTaskCtrl;
	private ConfirmNextAttemptController confirmNextAttemptCtrl;
	
	private int count = 0;
	private final List<String> categoriesIds;
	private final long totalDurationInMillis;
	private final List<SegmentMarker> segmentSelections = new ArrayList<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public VideoTaskDisplayController(UserRequest ureq, WindowControl wControl, VideoDisplayController displayCtrl,
			RepositoryEntry videoEntry, RepositoryEntry entry, CourseNode courseNode,
			List<String> categories, boolean authorMode, boolean anonym, int currentAttempt) {
		super(ureq, wControl);
		this.entry = entry;
		this.subIdent = courseNode.getIdent();
		this.videoEntry = videoEntry;
		this.authorMode = authorMode;
		this.displayCtrl = displayCtrl;
		this.currentAttempt = currentAttempt;
		this.categoriesIds = List.copyOf(categories);
		maxAttempts = courseNode.getModuleConfiguration()
				.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, 0);
		mode = courseNode.getModuleConfiguration()
				.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE, VideoTaskEditController.CONFIG_KEY_MODE_DEFAULT);
		maxScore = (Float) courseNode.getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_SCORE_MAX);

		cutValue = (Float) courseNode.getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		rounding = courseNode.getModuleConfiguration().getIntegerSafe(VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING,
				VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING_DEFAULT);
		weightWrongAnswers = courseNode.getModuleConfiguration().getDoubleEntry(VideoTaskEditController.CONFIG_KEY_WEIGHT_WRONG_ANSWERS);
	
		if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode)) {
			maxAttemptsPerSegments = courseNode.getModuleConfiguration().getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT,
					VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT_DEFAULT);
		} else {
			maxAttemptsPerSegments = 0;
		}

		UserSession usess = ureq.getUserSession();
		boolean guestOnly = usess.getRoles().isGuestOnly();
		if(guestOnly || anonym) {
			assessedIdentity = null;
			anonymousIdentifier = getAnonymousIdentifier(usess);
		} else {
			assessedIdentity = getIdentity();
			anonymousIdentifier = null;
		}
		
		mainVC = createVelocityContainer("display_container");
		initAssessment();
		
		closeButton = LinkFactory.createButton("close.video", mainVC, this);
		closeButton.setElementCssClass("btn-primary");
		segments = videoManager.loadSegments(videoEntry.getOlatResource());

		mainVC.put("video", displayCtrl.getInitialComponent());
		listenTo(displayCtrl);
		mainPanel = putInitialPanel(mainVC);
		
		totalDurationInMillis = VideoHelper.durationInSeconds(videoEntry, displayCtrl) * 1000l;
		
		segmentsCtrl = new SegmentsController(ureq, getWindowControl(), displayCtrl.getVideoElementId(), segmentSelections);
		listenTo(segmentsCtrl);
		initSegmentsController(courseNode);
		displayCtrl.addLayer(segmentsCtrl);
	}
	
	private void initAssessment() {
		Boolean rootEntry = subIdent == null? Boolean.TRUE: Boolean.FALSE;
		AssessmentEntry assessmentEntry = assessmentService
				.getOrCreateAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent, rootEntry, videoEntry);
		
		VideoTaskSession lastSession = videoAssessmentService
				.getResumableTaskSession(assessedIdentity, anonymousIdentifier, entry, subIdent, videoEntry, authorMode);
		if(lastSession == null) {
			taskSession = videoAssessmentService.createTaskSession(assessedIdentity, anonymousIdentifier, assessmentEntry,
					entry, subIdent, videoEntry, authorMode);
		} else {
			taskSession = lastSession;
		}
	}
	
	private void initSegmentsController(CourseNode courseNode) {
		List<VideoSegmentCategory> segmentCategoriesList = VideoTaskHelper.getSelectedCategories(segments, categoriesIds);
		VideoTaskHelper.sortCategories(segmentCategoriesList, courseNode, getLocale());
		List<Category> categoriesList = segmentCategoriesList.stream()
				.map(Category::new)
				.toList();
		segmentsCtrl.setCategories(categoriesList);
		
		if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode)) {
			List<Segment> segmentsList = new ArrayList<>();
			List<VideoSegment> availableSegmentsList = VideoTaskHelper.getSelectedSegments(segments, categoriesIds);
			for(VideoSegment videoSegment:availableSegmentsList) {
				segmentsList.add(Segment.valueOf(videoSegment.getBegin(), videoSegment.getDuration(), totalDurationInMillis, "o_video_marker_gray", ""));
			}
			segmentsCtrl.setSegments(segmentsList, "", true);
		} else {
			segmentsCtrl.setSegments(List.of(Segment.fullWidth()), "o_videotask_segments_with_markers", false);
		}

		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)
				|| VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS.equals(mode)) {
			segmentsCtrl.setMessage(translate("feedback.test.initial"), "o_note", false);
		}
	}
	
	private String getAnonymousIdentifier(UserSession usess) {
		String sessionId = usess.getSessionInfo().getSession().getId();
		String testKey = (entry == null ? videoEntry.getKey() : entry.getKey()) + "-" + subIdent +"-" + videoEntry.getKey() + "-" + sessionId;
		Object id = usess.getEntry(testKey);
		if(id instanceof String idString) {
			return idString;
		}

		String newId = UUID.randomUUID().toString();
		usess.putEntryInNonClearedStore(testKey, newId);
		return newId;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeButton == source) {
			doConfirmFinishTask(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(displayCtrl == source) {
			if(event instanceof VideoEvent ve) {
				if(VideoEvent.ENDED.equals(ve.getCommand())) {
					doEnd(ureq);
				} 
			}
		} else if(confirmEndTaskCtrl == source) {
			// Close first modals
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doConfirmFinishTask(ureq);
			} else if(event instanceof RestartEvent) {
				doConfirmNextAttempt(ureq);
			} else if(event instanceof ShowSolutionEvent) {
				doShowSolution();
			}
		} else if(confirmNextAttemptCtrl == source) {
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT) {
				doFinishTask(ureq, true);
			}
		} else if(confirmFinishTaskCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doFinishTask(ureq, false);
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void doSegmentSelection(String categoryId, long positionInSeconds) {
		VideoSegmentCategory category = segments.getCategory(categoryId).orElse(null);
		if(category != null) {
			long positionInMilliSeconds = positionInSeconds * 1000l;
			VideoSegment segment = getSegment(positionInMilliSeconds);
			String segmentId = segment == null ? null : segment.getId();
			long currentSegmentAttempt = getAttemptOnSegment(segmentId);
			if(maxAttemptsPerSegments <= 0 || currentSegmentAttempt < maxAttemptsPerSegments) {
				boolean correct = segment != null && segment.getCategoryId().equals(category.getId());
				VideoTaskSegmentSelection selection = videoAssessmentService.createTaskSegmentSelection(taskSession,
						segmentId, category.getId(), correct, positionInMilliSeconds, Long.toString(positionInSeconds));
				SegmentMarker segmentMarker = getMarker(selection, category, segment, correct);
				synchronized(segmentSelections) {
					segmentSelections.add(segmentMarker);
				}
				
				feedback(category, segmentId, correct);
			} else {
				segmentsCtrl.setMessage(translate("feedback.no.attempts.left"), "o_warning", false);
			}
		}
	}
	
	private SegmentMarker getMarker(VideoTaskSegmentSelection selection, VideoSegmentCategory category, VideoSegment videoSegment, boolean correct) {
		if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode)) {
			return SegmentMarker.valueOfAssign(selection, category, "o_segment_correct", videoSegment, totalDurationInMillis, "", correct);
		}
		if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS.equals(mode)) {
			String resultCss = correct ? "o_segment_marker_correct" : "o_segment_marker_not_correct";
			return SegmentMarker.valueOfIdentify(selection, category, resultCss, totalDurationInMillis);
		}
		return SegmentMarker.valueOfTest(selection, category, totalDurationInMillis);
	}
	
	private void feedback(VideoSegmentCategory category, String segmentId, boolean correct) {
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)) {
			segmentsCtrl.setMessage("", "", false);
			return;
		}
		
		String i18nKey;
		String cssClass;
		long attemptsLeft = 0;
		boolean timer = false;
		if(correct) {
			timer = true;
			cssClass = "o_correct";
			i18nKey = "feedback.correct";
		} else if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode) && maxAttemptsPerSegments > 0) {
			cssClass = "o_warning";
			attemptsLeft = maxAttemptsPerSegments - getAttemptOnSegment(segmentId);
			if(attemptsLeft <= 0l) {
				i18nKey = "feedback.not.correct.assign.no.attempts.left";
			} else if(attemptsLeft == 1l) {
				i18nKey = "feedback.not.correct.assign.attempts.left.singular";
			} else {
				i18nKey = "feedback.not.correct.assign.attempts.left.plural";
			}
		} else {
			if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS.equals(mode)) {
				timer = true;
			}
			cssClass = "o_warning";
			i18nKey = "feedback.not.correct";
		}
		
		if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode)) {
			String status = correct ? "correct" : "incorrect";
			segmentsCtrl.setCategoryStatus(category, segmentId, status);
			if(correct) {
				segmentsCtrl.temporaryDisableCategories();
			}
		}
		segmentsCtrl.setMessage(translate(i18nKey, Long.toString(attemptsLeft)), cssClass, timer);
	}
	
	private long getAttemptOnSegment(String segment) {
		if(!StringHelper.containsNonWhitespace(segment)) return 0;
		
		return getResults().stream()
				.filter(selection -> segment.equals(selection.getSegmentId()))
				.count();
	}
	
	public List<VideoTaskSegmentSelection> getResults() {
		synchronized(segmentSelections) {
			return segmentSelections.stream()
					.map(SegmentMarker::segmentSelection)
					.toList();
		}
	}
	
	private VideoSegment getSegment(long positionInMilliSeconds) {
		for(VideoSegment segment:segments.getSegments()) {
			Date begin = segment.getBegin();
			long startInMilliSeconds = begin.getTime();
			long endInMilliSeconds = startInMilliSeconds + (segment.getDuration() * 1000l);
			if(startInMilliSeconds <= positionInMilliSeconds
					&& (endInMilliSeconds >= positionInMilliSeconds)
					&& categoriesIds.contains(segment.getCategoryId())) {
				return segment;
			}	
		}
		return null;
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmNextAttemptCtrl);
		removeAsListenerAndDispose(confirmFinishTaskCtrl);
		removeAsListenerAndDispose(confirmEndTaskCtrl);
		removeAsListenerAndDispose(cmc);
		confirmNextAttemptCtrl = null;
		confirmFinishTaskCtrl = null;
		confirmEndTaskCtrl = null;
		cmc = null;
	}
	
	private void doEnd(UserRequest ureq) {
		String titleI18nKey;
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)) {
			confirmEndTaskCtrl = new ConfirmEndTestTaskController(ureq, getWindowControl(), currentAttempt, maxAttempts);
			titleI18nKey = "confirm.end.test.title";
		} else if(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode)) {
			List<VideoSegment> segmentsList = VideoTaskHelper.getSelectedSegments(segments, categoriesIds);
			confirmEndTaskCtrl = new ConfirmEndPracticeAssignTaskController(ureq, getWindowControl(),
					List.copyOf(segmentSelections), segmentsList, currentAttempt, maxAttempts);
			titleI18nKey = "confirm.end.practice.title";
		} else {
			List<VideoSegment> segmentsList = VideoTaskHelper.getSelectedSegments(segments, categoriesIds);
			confirmEndTaskCtrl = new ConfirmEndPracticeIdentifyTaskController(ureq, getWindowControl(),
					getResults(), segmentsList, currentAttempt, maxAttempts);
			titleI18nKey = "confirm.end.practice.title";
		}
		listenTo(confirmEndTaskCtrl);
		
		String title = translate(titleI18nKey, Integer.toString(currentAttempt + 1));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmEndTaskCtrl.getInitialComponent(), title);
		cmc.setCustomWindowCSS("o_modal_small");
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmNextAttempt(UserRequest ureq) {
		removeAsListenerAndDispose(confirmNextAttemptCtrl);
		
		confirmNextAttemptCtrl = new ConfirmNextAttemptController(ureq, getWindowControl(), mode);
		listenTo(confirmNextAttemptCtrl);
		
		String title = translate("confirm.next.attempt.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmNextAttemptCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmFinishTask(UserRequest ureq) {
		confirmFinishTaskCtrl = new ConfirmFinishTaskController(ureq, getWindowControl(), mode);
		listenTo(confirmFinishTaskCtrl);
		
		String title = translate("confirm.finish.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmFinishTaskCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doFinishTask(UserRequest ureq, boolean startNextAttempt) {
		setFinishTaskSession();
		fireEvent(ureq, new FinishEvent(taskSession, startNextAttempt));
	}
	
	private void setFinishTaskSession() {
		taskSession.setFinishTime(new Date());
		
		if(maxScore != null) {
			VideoTaskScore score = videoAssessmentService
					.calculateScore(segments, categoriesIds, maxScore, cutValue, weightWrongAnswers, rounding, getResults());
			taskSession.setScore(score.score());
			taskSession.setPassed(score.passed());
			taskSession.setMaxScore(BigDecimal.valueOf(maxScore.doubleValue()));
			taskSession.setResult(score.results());
			taskSession.setSegments(score.segments());
		}
		
		taskSession = videoAssessmentService.updateTaskSession(taskSession);
		dbInstance.commitAndCloseSession();
	}
	
	private void doShowSolution() {
		List<SegmentMarker> solutionList = new ArrayList<>();
		List<VideoSegment> segmentsList = VideoTaskHelper.getSelectedSegments(segments, categoriesIds);
		for(VideoSegment videoSegment:segmentsList) {
			VideoSegmentCategory category = segments.getCategory(videoSegment.getCategoryId()).orElse(null);
			String durationString = translate("duration.description", Long.toString(videoSegment.getDuration()));
			
			SegmentMarker solution = SegmentMarker.valueOfAssign(null, category, "o_segment_solution",
					videoSegment, totalDurationInMillis, durationString, true);
			solutionList.add(solution);
		}
		
		segmentsCtrl.setSegmentsSelections(solutionList);
		segmentsCtrl.setCategories(List.of());
		segmentsCtrl.setMessage("", "", false);
		segmentsCtrl.setShowSolution(true);
		segmentsCtrl.setShowTooltips(true);
		segmentsCtrl.setEnableDisableCategories(false);
	}
	
	public boolean isFullScreen() {
		return fullScreen;
	}
	
	public void setAsFullscreen(UserRequest ureq) {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			thebaseChief = cc;
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			thebaseChief.getScreenMode().setMode(Mode.full, businessPath);
		} else {
			Windows.getWindows(ureq).setFullScreen(Boolean.TRUE);
		}
		fullScreen = true;
	}
	
	@SuppressWarnings("deprecation")
	public void activate() {
		getWindowControl().pushToMainArea(mainPanel);
	}
	
	public void deactivate(UserRequest ureq) {
		getWindowControl().pop();
		if (fullScreen) {
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			if(thebaseChief != null) {
				thebaseChief.getScreenMode().setMode(Mode.standard, businessPath);
			} else if (ureq != null){
				ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
				if (cc != null) {
					thebaseChief = cc;
					thebaseChief.getScreenMode().setMode(Mode.standard, businessPath);
				}
			}
		}
	}
	
	public static class Category {
		
		private String status;
		private String segmentId;
		private final VideoSegmentCategory segmentCategory;
		
		public Category(VideoSegmentCategory segmentCategory) {
			this.segmentCategory = segmentCategory;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String segmentId, String status) {
			this.status = status;
			this.segmentId = segmentId;
		}
		
		public String getSegmentId() {
			return segmentId;
		}
		
		public String getId() {
			return segmentCategory.getId();
		}
		
		public String getColor() {
			return segmentCategory.getStyle();
		}
		
		public String getTitle() {
			return segmentCategory.getTitle();
		}
		
		public String getLabel() {
			return segmentCategory.getLabel();
		}
		
		public String getLabelAndTitle() {
			return segmentCategory.getLabelAndTitle();
		}
		
		public VideoSegmentCategory getCategory() {
			return segmentCategory;
		}
	}
	
	public record SegmentMarker(VideoTaskSegmentSelection segmentSelection, VideoSegmentCategory category,
			String resultCssClass, String width, String left, double start, double duration, String durationString, boolean visible) {
		
		public static SegmentMarker valueOfAssign(VideoTaskSegmentSelection segmentSelection, VideoSegmentCategory category,
				String resultCssClass, VideoSegment segment, long totalDurationInMillis, String durationString, boolean visible) {
			double dleft = (double) segment.getBegin().getTime() / totalDurationInMillis;
			double dwidth = (segment.getDuration() * 1000.0d) / totalDurationInMillis;
			double startInSeconds = segment.getBegin().getTime() / 1000.0d;
			return new SegmentMarker(segmentSelection, category, "o_videotask_segment " + resultCssClass,
					String.format("%.2f%%", dwidth * 100), String.format("%.2f%%", dleft * 100),
					startInSeconds, segment.getDuration(), durationString, visible);
		}
		
		public static SegmentMarker valueOfIdentify(VideoTaskSegmentSelection segmentSelection, VideoSegmentCategory category,
				String resultCssClass, long totalDurationInMillis) {
			double dleft = (double) segmentSelection.getTime().longValue() / totalDurationInMillis;
			return new SegmentMarker(segmentSelection, category, "o_videotask_marker " + resultCssClass, "", String.format("%.2f%%", dleft * 100),
					0d, 0d, "", true);
		}
		
		public static SegmentMarker valueOfTest(VideoTaskSegmentSelection segmentSelection, VideoSegmentCategory category,
				 long totalDurationInMillis) {
			double dleft = (double) segmentSelection.getTime().longValue() / totalDurationInMillis;
			return new SegmentMarker(segmentSelection, category, "o_videotask_marker", "", String.format("%.2f%%", dleft * 100),
					0d, 0d, "", true);
		}

		public String getCategoryLabel() {
			return category.getLabel();
		}
		
		public String getCategoryLabelAndTitle() {
			return category.getLabelAndTitle();
		}
		
		public String getCategoryColor() {
			return category.getStyle();
		}
	}
	
	public static record Segment(String width, String left, double start, double duration, String color, String label, String labelAndTitle, String durationString) {

		public static Segment valueOf(Date begin, long durationInSeconds, long totalDurationInMillis, String color, String label) {
			double dwidth = (durationInSeconds * 1000.0) / totalDurationInMillis;
			double dleft = (double) begin.getTime() / totalDurationInMillis;
			double startInSeconds = begin.getTime() / 1000d;
			return new Segment(String.format("%.2f%%", dwidth * 100), String.format("%.2f%%", dleft * 100),
					startInSeconds, durationInSeconds, color, label, "", "");
		}
		
		public static Segment valueOf(VideoSegment videoSegment, VideoSegmentCategory category, long totalDurationInMillis) {
			long durationInSeconds = videoSegment.getDuration();
			double dwidth = (durationInSeconds * 1000.0) / totalDurationInMillis;
			double dleft = (double) videoSegment.getBegin().getTime() / totalDurationInMillis;
			double startInSeconds = videoSegment.getBegin().getTime() / 1000d;
			return new Segment(String.format("%.2f%%", dwidth * 100), String.format("%.2f%%", dleft * 100),
					startInSeconds, durationInSeconds, category.getStyle(), category.getLabel(), category.getLabelAndTitle(), "");
		}
		
		public static Segment fullWidth() {
			return new Segment("100%", "0%", -1d, -1d, "grey", "", "", "");
		}
	}
	
	private class SegmentsController extends FormBasicController {

		private final String videoElementId;
		private List<Category> categoriesList;
		private List<SegmentMarker> segmentsSelections;
		
		public SegmentsController(UserRequest ureq, WindowControl wControl, String videoElementId, List<SegmentMarker> segmentsSelections) {
			super(ureq, wControl, "display_segments");
			this.videoElementId = videoElementId;
			this.segmentsSelections = segmentsSelections;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer layoutCont) {
				layoutCont.contextPut("message", "");
				layoutCont.contextPut("segmentsCssClass", "");
				layoutCont.contextPut("videoElementId", videoElementId);
				layoutCont.contextPut("segmentsSelections", segmentsSelections);
				layoutCont.contextPut("showSolution", Boolean.FALSE);
				layoutCont.contextPut("showTooltips", Boolean.FALSE);
			}
		}
		
		public void setSegmentsSelections(List<SegmentMarker> segmentsSelections) {
			flc.contextPut("segmentsSelections", segmentsSelections);
		}
		
		public void setSegments(List<Segment> segments, String segmentsCssClass, boolean enableDisableCategories) {
			flc.contextPut("segments", segments);
			flc.contextPut("segmentsCssClass", segmentsCssClass);
			flc.contextPut("enableDisableCategories", Boolean.valueOf(enableDisableCategories));
		}
		
		public void setEnableDisableCategories(boolean enableDisableCategories) {
			flc.contextPut("enableDisableCategories", Boolean.valueOf(enableDisableCategories));
		}
		
		public void setCategories(List<Category> categoriesList) {
			this.categoriesList = categoriesList;
			flc.contextPut("categories", categoriesList);
		}
		
		public void setCategoryStatus(VideoSegmentCategory segmentCategory, String segmentId, String status) {
			if(segmentCategory == null || categoriesList == null || categoriesList.isEmpty()) return;
			
			for(Category category:categoriesList) {
				if(category.getCategory().equals(segmentCategory)) {
					category.setStatus(segmentId, status);
				} else if(category.getSegmentId() != null && !category.getSegmentId().equals(segmentId)) {
					category.setStatus(null, null);
				}
			}
		}
		
		public void temporaryDisableCategories() {
			flc.contextPut("temporaryDisableCategories", new ConsumableBoolean(true));
		}
		
		public void setShowSolution(boolean showSolution) {
			flc.contextPut("showSolution", Boolean.valueOf(showSolution));
		}
		
		public void setShowTooltips(boolean showTooltips) {
			flc.contextPut("showTooltips", Boolean.valueOf(showTooltips));
		}

		public void setMessage(String message, String cssClass, boolean timer) {
			flc.contextPut("message", message);
			flc.contextPut("messageCssClass", cssClass);
			if(timer) {
				flc.contextPut("messageTimerId", "timer_" + (++count));
			} else {
				flc.contextRemove("messageTimerId");
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(flc == source) {
				String cid = ureq.getParameter("cid");
				String categoryId = ureq.getParameter("category-id");
				long position = toPosition(ureq.getParameter("position"));
				if("category".equals(cid) && StringHelper.containsNonWhitespace(categoryId) && position >= 0) {
					doSegmentSelection(categoryId, position);
				}
			}
			super.formInnerEvent(ureq, source, event);
		}
		
		private long toPosition(String val) {
			try {
				return Math.round(Double.parseDouble(val));
			} catch (NumberFormatException e) {
				getLogger().warn("Cannot parse position: {}", val);
				return -1l;
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
