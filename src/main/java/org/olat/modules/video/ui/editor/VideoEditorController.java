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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoEditorController extends BasicController {

	private final VelocityContainer mainVC;
	private final VideoController videoController;
	private final DetailsController detailsController;
	private final MasterController masterController;

	@Autowired
	private VideoManager videoManager;

	private String currentTimeCode;

	public VideoEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("video_editor");

		VideoMeta videoMetadata = videoManager.getVideoMetadata(repositoryEntry.getOlatResource());
		mainVC.contextPut("videoWidth", videoMetadata.getWidth());
		mainVC.contextPut("videoHeight", videoMetadata.getHeight());

		videoController = new VideoController(ureq, wControl, repositoryEntry);
		listenTo(videoController);
		String videoElementId = videoController.getVideoElementId();
		mainVC.put("video", videoController.getInitialComponent());

		detailsController = new DetailsController(ureq, wControl, repositoryEntry, videoElementId,
				videoController.getDurationInSeconds());
		listenTo(detailsController);
		mainVC.put("detail", detailsController.getInitialComponent());

		masterController = new MasterController(ureq, wControl, repositoryEntry.getOlatResource(), videoMetadata,
				videoElementId);
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
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (videoController == source) {
			if (event instanceof VideoEvent) {
				VideoEvent videoEvent = (VideoEvent) event;
				String currentTimeCode = videoEvent.getTimeCode();
				this.currentTimeCode = currentTimeCode;
				detailsController.setCurrentTimeCode(currentTimeCode);
			}
		}
		if (detailsController == source) {
			if (event == AnnotationsController.RELOAD_MARKERS_EVENT) {
				videoController.reloadMarkers();
				masterController.reload();
			} else if (event == ChaptersController.RELOAD_CHAPTERS_EVENT) {
				videoController.reloadChapters();
				masterController.reload();
			} else if (event instanceof AnnotationSelectedEvent) {
			} else if (event instanceof SegmentSelectedEvent) {
			}
		}
		if (masterController == source) {
			if (event instanceof AnnotationSelectedEvent) {
				detailsController.setAnnotationId(((AnnotationSelectedEvent)event).getAnnotationId());
				detailsController.showAnnotations(ureq);
				videoController.setAnnotationId(((AnnotationSelectedEvent)event).getAnnotationId());
			} else if (event instanceof ChapterSelectedEvent) {
				detailsController.showChapters(ureq);
				videoController.selectTime(((ChapterSelectedEvent) event).getStartTimeInMillis() / 1000);
			}
		}
	}
}
