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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.videotask.ui.components.FinishEvent;
import org.olat.course.nodes.videotask.ui.components.RestartEvent;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentResult;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.ui.VideoDisplayController;
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
	private final boolean assessmentType;
	private VideoTaskSession taskSession;
	private final VideoSegments segments;
	private final int maxAttempts;
	private final int currentAttempt;
	private final String mode;

	private CloseableModalController cmc;
	private Controller confirmEndTaskCtrl;
	private final VideoDisplayController displayCtrl;
	private ConfirmFinishTaskController confirmFinishTaskCtrl;
	private ConfirmNextAttemptController confirmNextAttemptCtrl;
	
	private final List<String> categoriesIds;
	private final List<VideoTaskSegmentResult> segmentSelections = new ArrayList<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public VideoTaskDisplayController(UserRequest ureq, WindowControl wControl, VideoDisplayController displayCtrl,
			RepositoryEntry videoEntry, RepositoryEntry entry, CourseNode courseNode, boolean assessmentType,
			List<String> categories, boolean authorMode, boolean anonym, int currentAttempt) {
		super(ureq, wControl);
		this.entry = entry;
		this.subIdent = courseNode.getIdent();
		this.videoEntry = videoEntry;
		this.authorMode = authorMode;
		this.displayCtrl = displayCtrl;
		this.currentAttempt = currentAttempt;
		this.assessmentType = assessmentType;
		this.categoriesIds = List.copyOf(categories);
		maxAttempts = courseNode.getModuleConfiguration()
				.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, 0);
		mode = courseNode.getModuleConfiguration()
				.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE, VideoTaskEditController.CONFIG_KEY_MODE_DEFAULT);
		
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
		if(assessmentType) {
			initAssessment();
		}
		
		closeButton = LinkFactory.createButton("close.video", mainVC, this);
		segments = videoManager.loadSegments(videoEntry.getOlatResource());
		
		int counter = 0;
		List<Link> categoriesLink = new ArrayList<>();
		List<VideoSegmentCategory> categoriesList = VideoTaskHelper.getSelectedCategories(segments, categoriesIds);
		for(VideoSegmentCategory category:categoriesList) {
			String catId = "cat_" + (++counter);
			String catName = category.getLabelAndTitle();
			Link categoryLink = LinkFactory.createLink(catId, catId, "category", catName, getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
			categoryLink.setUserObject(category);
			categoriesLink.add(categoryLink);
		}
		mainVC.contextPut("categories", categoriesLink);

		mainVC.put("video", displayCtrl.getInitialComponent());
		listenTo(displayCtrl);
		mainPanel = putInitialPanel(mainVC);
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
		} else if(source instanceof Link link && "category".equals(link.getCommand())
				&& link.getUserObject() instanceof VideoSegmentCategory category) {
			doSegmentSelection(category);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(displayCtrl == source) {
			if(event instanceof VideoEvent ve) {
				if(VideoEvent.ENDED.equals(ve.getCommand())) {
					doEnd(ureq);
				} else {
					currentPosition(ve);
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
			}
		} else if(confirmNextAttemptCtrl == source) {
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT) {
				doFinishTask(ureq, true);
			}
		} else if(confirmFinishTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinishTask(ureq, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void doSegmentSelection(VideoSegmentCategory category) {
		long positionInMilliSeconds = positionInSeconds * 1000l;
		VideoSegment segment = getSegment(positionInMilliSeconds);
		String segmentId = segment == null ? null : segment.getId();
		Boolean correct = Boolean.valueOf(segment != null && segment.getCategoryId().equals(category.getId()));
		
		synchronized(segmentSelections) {
			segmentSelections.add(new Result(positionInMilliSeconds, correct.booleanValue(), segmentId, category.getId()));
		}
		
		if(assessmentType) {
			videoAssessmentService.createTaskSegmentSelection(taskSession, segmentId, category.getId(), correct,
				positionInMilliSeconds, rawPosition);
		}
	}
	
	public List<VideoTaskSegmentResult> getResults() {
		synchronized(segmentSelections) {
			return List.copyOf(segmentSelections);
		}
	}
	
	private VideoSegment getSegment(long positionInMilliSeconds) {
		for(VideoSegment segment:segments.getSegments()) {
			Date begin = segment.getBegin();
			long startInMilliSeconds = begin.getTime();
			long endInMilliSeconds = startInMilliSeconds + (segment.getDuration() * 1000l);
			if(startInMilliSeconds <= positionInMilliSeconds && (endInMilliSeconds >= positionInMilliSeconds)) {
				return segment;
			}	
		}
		return null;
	}
	
	private String rawPosition;
	private long positionInSeconds;
	
	private void currentPosition(VideoEvent ve) {
		String cmd = ve.getCommand();
		String duration = ve.getDuration();
		String timecode = ve.getTimeCode();

		if(StringHelper.containsNonWhitespace(ve.getTimeCode()) && !"NaN".equals(ve.getTimeCode())) {
			try {
				rawPosition = ve.getTimeCode();
				positionInSeconds = Math.round(Double.parseDouble(ve.getTimeCode()));
			} catch (NumberFormatException e) {
				//don't panic
			}
		}
		
		getLogger().debug("Video event: {} (timecode: {}, duration {})", cmd, duration, timecode);
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
					getResults(), segmentsList, currentAttempt, maxAttempts);
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
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmNextAttempt(UserRequest ureq) {
		removeAsListenerAndDispose(confirmNextAttemptCtrl);
		
		confirmNextAttemptCtrl = new ConfirmNextAttemptController(ureq, getWindowControl());
		listenTo(confirmNextAttemptCtrl);
		
		String title = translate("confirm.next.attempt.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmNextAttemptCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmFinishTask(UserRequest ureq) {
		confirmFinishTaskCtrl = new ConfirmFinishTaskController(ureq, getWindowControl());
		listenTo(confirmFinishTaskCtrl);
		
		String title = translate("confirm.finish.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmFinishTaskCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doFinishTask(UserRequest ureq, boolean startNextAttempt) {
		if(assessmentType) {
			setFinishTaskSession();
			fireEvent(ureq, new FinishEvent(taskSession, startNextAttempt));
		} else {
			fireEvent(ureq, new FinishEvent(startNextAttempt));
		}
	}
	
	private void setFinishTaskSession() {
		taskSession.setFinishTime(new Date());
		taskSession = videoAssessmentService.updateTaskSession(taskSession);
		dbInstance.commitAndCloseSession();
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
	
	public static class Result implements VideoTaskSegmentResult {
		
		private final long positionInMilliSeconds;
		private final boolean correct;
		private final String segmentId;
		private final String categoryId;
		
		public Result(long positionInMilliSeconds, boolean correct, String segmentId, String categoryId) {
			this.positionInMilliSeconds = positionInMilliSeconds;
			this.correct = correct;
			this.segmentId = segmentId;
			this.categoryId = categoryId;
		}

		public long getPositionInMilliSeconds() {
			return positionInMilliSeconds;
		}

		@Override
		public boolean isCorrect() {
			return correct;
		}

		@Override
		public String getSegmentId() {
			return segmentId;
		}

		@Override
		public String getCategoryId() {
			return categoryId;
		}
	}
}
