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
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.modules.video.ui.event.MarkerMovedEvent;
import org.olat.modules.video.ui.event.MarkerResizedEvent;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.modules.video.ui.marker.ReloadMarkersCommand;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2022-11-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoController extends BasicController {
	private final VideoDisplayController videoDisplayController;
	private final String videoElementId;
	private final long durationInSeconds;
	private TimelineEventType timelineEventType;
	private TimelineEventSelectedEvent selectedTimelineEvent;
	private final SegmentLayerController segmentLayerController;
	private final CommentLayerController commentLayerController;

	public VideoController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
						   boolean showPoster) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("video_editor_player");

		VideoDisplayOptions displayOptions = VideoDisplayOptions.disabled();
		displayOptions.setDragAnnotations(true);
		displayOptions.setShowAnnotations(true);
		displayOptions.setSnapMarkerSizeToGrid(false);
		displayOptions.setAlwaysShowControls(true);
		displayOptions.setClickToPlayPause(false);
		displayOptions.setAuthorMode(true);
		displayOptions.setShowPoster(showPoster);
		displayOptions.setProgressFullWidth(true);
		videoDisplayController = new VideoDisplayController(ureq, wControl, repositoryEntry, null,
				null, displayOptions);
		listenTo(videoDisplayController);
		videoElementId = videoDisplayController.getVideoElementId();
		durationInSeconds = VideoHelper.durationInSeconds(repositoryEntry, videoDisplayController);

		segmentLayerController = new SegmentLayerController(ureq, wControl, repositoryEntry, videoElementId,
				durationInSeconds * 1000L);
		listenTo(segmentLayerController);
		videoDisplayController.addLayer(segmentLayerController);

		commentLayerController = new CommentLayerController(ureq, wControl, repositoryEntry, videoElementId);
		listenTo(commentLayerController);
		videoDisplayController.addLayer(commentLayerController);

		videoDisplayController.setTimeUpdateListener(true);
		mainVC.put("video", videoDisplayController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (videoDisplayController == source) {
			if (event instanceof VideoEvent videoEvent) {
				if ("timeupdate".equals(videoEvent.getCommand())) {
					long timeInMillis = (long)(Double.parseDouble(videoEvent.getTimeCode()) * 1000.0);
					if (selectedTimelineEvent != null) {
						long t0 = selectedTimelineEvent.getStartTimeInMillis();
						long t1 = t0 + 1000L * selectedTimelineEvent.getDurationInSeconds();
						if (timeInMillis < t0 || timeInMillis > t1) {
							if (videoDisplayController.isMarkerLayerSet()) {
								videoDisplayController.clearMarkerLayer();
							}
							segmentLayerController.hideSegment();
							commentLayerController.hideComment();
						} else {
							switch (timelineEventType) {
								case ANNOTATION -> videoDisplayController.loadMarker(ureq, videoEvent.getTimeCode(),
										selectedTimelineEvent.getId());
								case SEGMENT -> segmentLayerController.setSegment(selectedTimelineEvent.getId()
								);
								default -> {}
							}
						}
					} else {
						videoDisplayController.clearMarkerLayer();
					}
				}
				fireEvent(ureq, event);
			} else if (event instanceof MarkerMovedEvent) {
				fireEvent(ureq, event);
			} else if (event instanceof MarkerResizedEvent) {
				fireEvent(ureq, event);
			}
		} else if (commentLayerController == source) {
			if (event == Event.DONE_EVENT) {
				commentLayerController.hideComment();
				videoDisplayController.showHideProgressTooltip(true);
			}
		}
	}

	public String getVideoElementId() {
		return videoElementId;
	}

	public long getDurationInSeconds() {
		return durationInSeconds;
	}

	public void reloadMarkers(TimelineEventType timelineEventType) {
		List<VideoDisplayController.Marker> markers;
		if (TimelineEventType.COMMENT.equals(timelineEventType) && commentLayerController != null) {
			videoDisplayController.loadMarkers();
			markers = videoDisplayController.addMarkers(commentLayerController.getCommentsAsMarkers());
		} else {
			markers = videoDisplayController.loadMarkers();
		}
		ReloadMarkersCommand reloadMarkersCommand = new ReloadMarkersCommand(videoElementId, markers);
		getWindowControl().getWindowBackOffice().sendCommandTo(reloadMarkersCommand);
	}

	public void reloadChapters() {
		videoDisplayController.forceReload();
	}

	public void reloadComments() {
		commentLayerController.loadComments();
	}

	public void selectTime(long timeInSeconds, boolean isYoutube) {
		SelectTimeCommand selectTimeCommand = new SelectTimeCommand(videoElementId, timeInSeconds);
		getWindowControl().getWindowBackOffice().sendCommandTo(selectTimeCommand);

		if (isYoutube) {
			TimeUpdateCommand timeUpdateCommand = new TimeUpdateCommand(videoElementId, 200);
			getWindowControl().getWindowBackOffice().sendCommandTo(timeUpdateCommand);
		}
	}

	public void processTimelineEvent(UserRequest ureq, TimelineEventSelectedEvent selectedTimelineEvent,
									 TimelineEventType timelineEventType, boolean isYoutube) {
		this.timelineEventType = timelineEventType;
		this.selectedTimelineEvent = selectedTimelineEvent;

		videoDisplayController.setMode(timelineEventType == TimelineEventType.QUIZ,
				timelineEventType == TimelineEventType.ANNOTATION);

		reloadMarkers(timelineEventType);

		commentLayerController.hideComment();

		switch (timelineEventType) {
			case ANNOTATION, QUIZ -> {
				segmentLayerController.clearSegments();
				videoDisplayController.loadMarker(ureq,
						Double.toString((double) selectedTimelineEvent.getStartTimeInMillis() / 1000.0),
						selectedTimelineEvent.getId());
			}
			case CHAPTER -> {
				segmentLayerController.clearSegments();
				videoDisplayController.clearMarkerLayer();
			}
			case SEGMENT -> {
				videoDisplayController.clearMarkerLayer();
				segmentLayerController.setSegments();
				segmentLayerController.setSegment(selectedTimelineEvent.getId());
			}
			case COMMENT -> {
				segmentLayerController.clearSegments();
				videoDisplayController.clearMarkerLayer();
				commentLayerController.setComment(ureq, selectedTimelineEvent.getId());
				videoDisplayController.showHideProgressTooltip(false);
			}
			default -> {}
		}

		selectTime(selectedTimelineEvent.getStartTimeInMillis() / 1000, isYoutube);
	}

	public void setTypeOnly(TimelineEventType type) {
		commentLayerController.hideComment();
		segmentLayerController.clearSegments();
		videoDisplayController.clearMarkerLayer();
	}
}
