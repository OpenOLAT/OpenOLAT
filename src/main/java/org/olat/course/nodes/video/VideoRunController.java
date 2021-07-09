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
package org.olat.course.nodes.video;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */

public class VideoRunController extends BasicController {

	private Panel main;
	
	private VideoDisplayController videoDispCtr;

	private VideoCourseNode videoNode;
	private ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;

	@Autowired
	private RepositoryService repositoryService;

	/**
	 * single page run controller 
	 * @param wControl
	 * @param ureq
	 * @param userCourseEnv
	 * @param videoNode
	 */
	public VideoRunController(ModuleConfiguration config, WindowControl wControl, UserRequest ureq, UserCourseEnvironment userCourseEnv, VideoCourseNode videoNode) {
		super(ureq,wControl);
		
		this.config = config;
		this.videoNode = videoNode;
		this.userCourseEnv = userCourseEnv;
		addLoggingResourceable(LoggingResourceable.wrap(videoNode));
		
		main = new Panel("videorunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}
	
	@Override
	protected void doDispose() {
		// controllers auto-disposed
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == videoDispCtr) {
			if (event instanceof VideoEvent	) {
				VideoEvent videoEvent = (VideoEvent) event;
				if (videoEvent.getCommand().equals(VideoEvent.ENDED)) {
					// increment attempt variable
				}
			}
		}
	}
	
	private void doLaunch(UserRequest ureq){
		VelocityContainer myContent = createVelocityContainer("run");
		RepositoryEntry videoEntry = VideoEditController.getVideoReference(config, false);
		if (videoEntry == null) {
			showError(VideoEditController.NLS_ERROR_VIDEOREPOENTRYMISSING);
			return;
		}

		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VideoDisplayOptions displayOptions = videoNode.getVideoDisplay(videoEntry, userCourseEnv.isCourseReadOnly());
		videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, courseEntry, videoNode, displayOptions);
		listenTo(videoDispCtr);
		
		myContent.put("videoDisplay", videoDispCtr.getInitialComponent());
		main.setContent(myContent);
		
		// Update launch counter
		repositoryService.incrementLaunchCounter(videoEntry);
	}

	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq) {
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, userCourseEnv, videoNode, "o_icon_video");
		return new NodeRunConstructionResult(ctrl);
	}
}
