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
package org.olat.modules.video.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.ui.editor.CommentLayerController;
import org.olat.modules.video.ui.segment.SegmentsController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-03-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoDisplayAsRuntimeController extends VideoDisplayController {

	private final SegmentsController segmentsController;
	private final CommentLayerController commentLayerController;

	public VideoDisplayAsRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, repositoryEntry, null, null,
				VideoDisplayOptions.valueOf(false, false, false,
						false, true, true, true,
						repositoryEntry.getDescription(), false, false,
						false, true));

		long totalDurationInMillis = VideoHelper.durationInSeconds(repositoryEntry, this) * 1000L;
		segmentsController = new SegmentsController(ureq, wControl, repositoryEntry, getVideoElementId(),
				totalDurationInMillis);
		listenTo(segmentsController);
		addLayer(segmentsController);

		commentLayerController = new CommentLayerController(ureq, wControl, repositoryEntry, getVideoElementId());
		listenTo(commentLayerController);
		commentLayerController.loadComments();
		commentLayerController.addControllerListener(this);
		addLayer(commentLayerController);
		addMarkers(commentLayerController.getCommentsAsMarkers());
	}

	@Override
	protected void reloadVideo(UserRequest ureq) {
		super.reloadVideo(ureq);

		commentLayerController.loadComments();
		addMarkers(commentLayerController.getCommentsAsMarkers());

		segmentsController.loadSegments();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof VelocityContainer) {
			processVideoEvents(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (commentLayerController == source) {
			if (event == Event.DONE_EVENT) {
				commentLayerController.hideComment();
				showOtherLayers(commentLayerController);
				showHideProgressTooltip(true);
				play();
			}
		}
		super.event(ureq, source, event);
	}

	private void processVideoEvents(UserRequest ureq, Event event) {
		String cmd = event.getCommand();
		if (StringHelper.containsNonWhitespace(cmd)) {
			String currentTime = ureq.getHttpReq().getParameter("currentTime");
			switch (cmd) {
				case "play" -> {
					if (commentLayerController != null) {
						commentLayerController.hideComment();
						showOtherLayers(commentLayerController);
						showHideProgressTooltip(true);
					}
				}
				case "marker" -> {
					String markerId = ureq.getParameter("markerId");
					long timeInSeconds = Math.round(Double.parseDouble(currentTime));
					if (commentLayerController != null) {
						commentLayerController.setComment(ureq, markerId);
						if (commentLayerController.isCommentVisible()) {
							hideOtherLayers(commentLayerController);
							showHideProgressTooltip(false);
							pause(timeInSeconds);
						}
					}
				}
				default -> {
					// do nothing
				}
			}
		}
	}
}
