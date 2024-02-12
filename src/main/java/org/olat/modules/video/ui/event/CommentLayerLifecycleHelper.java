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
package org.olat.modules.video.ui.event;

import java.util.function.Supplier;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.functions.VideoCommands;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.editor.CommentLayerController;

/**
 * Helps avoid duplicate code for handling events from and updating the two classes
 * {@link org.olat.modules.video.ui.VideoDisplayController} and
 * {@link org.olat.modules.video.ui.editor.CommentLayerController}.
 * <br/>
 * Initial date: 2023-04-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CommentLayerLifecycleHelper {
	private final VideoDisplayController videoDisplayController;
	private final CommentLayerController commentLayerController;
	private final Supplier<WindowControl> windowControlSupplier;

	public CommentLayerLifecycleHelper(VideoDisplayController videoDisplayController,
									   CommentLayerController commentLayerController,
									   Supplier<WindowControl> windowControlSupplier) {
		this.videoDisplayController = videoDisplayController;
		this.commentLayerController = commentLayerController;
		this.windowControlSupplier = windowControlSupplier;
	}

	public void init() {
		commentLayerController.loadComments();
		videoDisplayController.addLayer(commentLayerController);
		videoDisplayController.addMarkers(commentLayerController.getCommentsAsMarkers());
	}

	public void handleEvent(UserRequest ureq, Controller source, Event event) {
		if (videoDisplayController == source) {
			if (event instanceof VideoEvent videoEvent) {
				if (videoEvent.getCommand().equals(VideoEvent.PLAY)) {
					doHideComment();
				}
			} else if (event instanceof VideoDisplayController.MarkerReachedEvent markerReachedEvent) {
				commentLayerController.setComment(ureq, markerReachedEvent.getMarkerId());
				if (commentLayerController.isCommentVisible()) {
					videoDisplayController.hideOtherLayers(commentLayerController);
					videoDisplayController.showHideProgressTooltip(false);
					doPause(markerReachedEvent.getTimeInSeconds());
				}
			}
		} else if (commentLayerController == source) {
			if (event == Event.DONE_EVENT) {
				doHideComment();
				doContinue();
			}
		}
	}

	private void doHideComment() {
		commentLayerController.hideComment();
		videoDisplayController.showOtherLayers(commentLayerController);
		videoDisplayController.showHideProgressTooltip(true);
	}

	private void doContinue() {
		windowControlSupplier.get().getWindowBackOffice().sendCommandTo(VideoCommands
				.videoContinue(videoDisplayController.getVideoElementId()));
	}

	private void doPause(long timeInSeconds) {
		windowControlSupplier.get().getWindowBackOffice().sendCommandTo(VideoCommands
				.pause(videoDisplayController.getVideoElementId(), timeInSeconds));
	}
}
