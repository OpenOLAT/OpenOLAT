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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.modules.video.ui.editor.MasterController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentPlayController extends BasicController {
	
	private final Link backButton;
	private final VelocityContainer mainVC;
	
	private MasterController timelineCtrl;
	private VideoDisplayController videoDisplayController;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private VideoManager videoManager;
		
	public VideoTaskAssessmentPlayController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry videoEntry, VideoTaskSession taskSession, Identity assessedIdentity) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("play");
		
		backButton = LinkFactory.createLinkBack(mainVC, this);
		
		VideoMeta videoMetadata = videoManager.getVideoMetadata(videoEntry.getOlatResource());
		mainVC.contextPut("videoWidth", videoMetadata.getWidth());
		mainVC.contextPut("videoHeight", videoMetadata.getHeight());
		
		String fullname = userManager.getUserDisplayName(assessedIdentity);
		mainVC.contextPut("userFullname", fullname);
		
		VideoDisplayOptions displayOptions = VideoDisplayOptions.disabled();
		displayOptions.setDragAnnotations(true);
		displayOptions.setShowAnnotations(true);
		displayOptions.setSnapMarkerSizeToGrid(false);
		displayOptions.setAlwaysShowControls(true);
		displayOptions.setClickToPlayPause(false);
		displayOptions.setAuthorMode(true);
		videoDisplayController = new VideoDisplayController(ureq, wControl, videoEntry, null, null, displayOptions);
		listenTo(videoDisplayController);
		mainVC.put("video", videoDisplayController.getInitialComponent());

		String videoElementId = videoDisplayController.getVideoElementId();
		timelineCtrl = new MasterController(ureq, getWindowControl(), videoEntry.getOlatResource(), videoElementId);
		listenTo(timelineCtrl);
		mainVC.put("timeline", timelineCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (event.getCommand().equals("TimelineAvailableWidthEvent")) {
			String availableWidthString = ureq.getHttpReq().getParameter("availableWidth");
			int availableWidth = Integer.parseInt(availableWidthString);
			if (availableWidth != timelineCtrl.getAvailableWidth()) {
				timelineCtrl.setAvailableWidth(availableWidth);
				timelineCtrl.getInitialComponent().setDirty(true);
			}
		}
	}
}
