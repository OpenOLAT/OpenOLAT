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
package org.olat.modules.video.ui.editor;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.ui.event.MarkerMovedEvent;
import org.olat.modules.video.ui.event.MarkerResizedEvent;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoEditorController extends BasicController {

	private final VideoController videoController;
	private final DetailsController detailsController;
	private final MasterController masterController;

	private final boolean isYoutube;

	@Autowired
	private VideoManager videoManager;

	public VideoEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("video_editor");

		VideoMeta videoMetadata = videoManager.getVideoMetadata(repositoryEntry.getOlatResource());
		isYoutube = videoMetadata.getVideoFormat() == VideoFormat.youtube;
		mainVC.contextPut("videoWidth", videoMetadata.getWidth());
		mainVC.contextPut("videoHeight", videoMetadata.getHeight());

		videoController = new VideoController(ureq, wControl, repositoryEntry, !isYoutube);
		listenTo(videoController);
		String videoElementId = videoController.getVideoElementId();
		mainVC.put("video", videoController.getInitialComponent());

		detailsController = new DetailsController(ureq, wControl, repositoryEntry,
				videoController.getDurationInSeconds());
		listenTo(detailsController);
		mainVC.put("detail", detailsController.getInitialComponent());

		masterController = new MasterController(ureq, wControl, repositoryEntry, List.of(), videoElementId);
		listenTo(masterController);
		mainVC.put("master", masterController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals("TimelineAvailableWidthEvent")) {
			String availableWidthString = ureq.getHttpReq().getParameter("availableWidth");
			int availableWidth = Integer.parseInt(availableWidthString);
			if (availableWidth != masterController.getAvailableWidth()) {
				masterController.setAvailableWidth(availableWidth);
				masterController.getInitialComponent().setDirty(true);
			}
			String videoViewWidthString = ureq.getHttpReq().getParameter("videoViewWidth");
			if (videoViewWidthString != null) {
				int videoViewWidth = Integer.parseInt(videoViewWidthString);
				videoController.setViewWidth(videoViewWidth);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (videoController == source) {
			if (event instanceof VideoEvent videoEvent) {
				detailsController.setCurrentTimeCode(videoEvent.getTimeCode());
				masterController.setCurrentTimeCode(videoEvent.getTimeCode());
			} else if (event instanceof MarkerResizedEvent markerResizedEvent) {
				detailsController.setAnnotationSize(markerResizedEvent.getMarkerId(), markerResizedEvent.getWidth(),
						markerResizedEvent.getHeight());
			} else if (event instanceof MarkerMovedEvent markerMovedEvent) {
				detailsController.setAnnotationPosition(markerMovedEvent.getMarkerId(), markerMovedEvent.getTop(),
						markerMovedEvent.getLeft());
			}
		} else if (detailsController == source) {
			if (event == AnnotationsController.RELOAD_MARKERS_EVENT) {
				videoController.reloadMarkers();
				masterController.reload();
			} else if (event == ChaptersController.RELOAD_CHAPTERS_EVENT) {
				videoController.reloadChapters();
				masterController.reload();
			} else if (event == QuestionsController.RELOAD_QUESTIONS_EVENT) {
				masterController.reload();
			} else if (event == SegmentsController.RELOAD_SEGMENTS_EVENT) {
				masterController.reload();
			} else if (event == CommentsController.RELOAD_COMMENTS_EVENT) {
				videoController.reloadComments();
				masterController.reload();
			} else if (event instanceof EditQuestionEvent editQuestionEvent) {
				fireEvent(ureq, editQuestionEvent);
			} else if (event instanceof SelectTimeEvent selectTimeCommand) {
				videoController.selectTime(selectTimeCommand.getTimeInSeconds(), isYoutube);
			} else if (event instanceof AnnotationSelectedEvent annotationSelectedEvent) {
				videoController.processTimelineEvent(ureq, annotationSelectedEvent, TimelineEventType.ANNOTATION, isYoutube);
				masterController.select(annotationSelectedEvent.getId());
			} else if (event instanceof SegmentSelectedEvent segmentSelectedEvent) {
				videoController.processTimelineEvent(ureq, segmentSelectedEvent, TimelineEventType.SEGMENT, isYoutube);
				masterController.select(segmentSelectedEvent.getId());
			} else if (event instanceof ChapterSelectedEvent chapterSelectedEvent) {
				videoController.processTimelineEvent(ureq, chapterSelectedEvent, TimelineEventType.CHAPTER, isYoutube);
				masterController.select(chapterSelectedEvent.getId());
			} else if (event instanceof QuestionSelectedEvent questionSelectedEvent) {
				videoController.processTimelineEvent(ureq, questionSelectedEvent, TimelineEventType.QUIZ, isYoutube);
				masterController.select(questionSelectedEvent.getId());
			} else if (event instanceof CommentSelectedEvent commentSelectedEvent) {
				videoController.processTimelineEvent(ureq, commentSelectedEvent, TimelineEventType.COMMENT, isYoutube);
				masterController.select(commentSelectedEvent.getId());
			}
		} else if (masterController == source) {
			if (event instanceof AnnotationSelectedEvent annotationSelectedEvent) {
				videoController.processTimelineEvent(ureq, annotationSelectedEvent, TimelineEventType.ANNOTATION, isYoutube);
				detailsController.showAnnotation(ureq, annotationSelectedEvent.getId());
			} else if (event instanceof ChapterSelectedEvent chapterSelectedEvent) {
				videoController.processTimelineEvent(ureq, chapterSelectedEvent, TimelineEventType.CHAPTER, isYoutube);
				detailsController.showChapters(ureq);
			} else if (event instanceof QuestionSelectedEvent questionSelectedEvent) {
				videoController.processTimelineEvent(ureq, questionSelectedEvent, TimelineEventType.QUIZ, isYoutube);
				detailsController.showQuestion(ureq, questionSelectedEvent.getId());
			} else if (event instanceof SegmentSelectedEvent segmentSelectedEvent) {
				videoController.processTimelineEvent(ureq, segmentSelectedEvent, TimelineEventType.SEGMENT, isYoutube);
				detailsController.showSegment(ureq, segmentSelectedEvent.getId());
			} else if (event instanceof CommentSelectedEvent commentSelectedEvent) {
				videoController.processTimelineEvent(ureq, commentSelectedEvent, TimelineEventType.COMMENT, isYoutube);
				detailsController.showComment(ureq, commentSelectedEvent.getId());
			} else if (event instanceof  TimelineEventDeletedEvent timelineEventDeletedEvent) {
				detailsController.handleDeleted(timelineEventDeletedEvent.getType(), timelineEventDeletedEvent.getId());
			}
		}
	}

	public void updateQuestion(String questionId) {
		detailsController.updateQuestion(questionId);
	}
}
