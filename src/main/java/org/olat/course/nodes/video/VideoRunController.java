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

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.nodes.cp.CPRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.ui.VideoDisplayController;
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
	private static final OLog log = Tracing.createLoggerFor(CPRunController.class);

	private ModuleConfiguration config;
	private File videoRoot;
	private Panel main;
	
	private VideoDisplayController videoDispCtr;
	private VideoCourseNode videoNode;
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
		
		// assertion to make sure the moduleconfig is valid
		if (!VideoEditController.isModuleConfigValid(config)) throw new AssertException("videorun controller had an invalid module config:"	+ config.toString());
		this.config = config;
		this.videoNode = videoNode;
		this.userCourseEnv = userCourseEnv;
		addLoggingResourceable(LoggingResourceable.wrap(videoNode));

		
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null ) { // a context path is left for me
			if(log.isDebug()) log.debug("businesscontrol (for further jumps) would be:"+bc);
			OLATResourceable popOres = ce.getOLATResourceable();
			if(log.isDebug()) log.debug("OLATResourceable=" + popOres);
			String typeName = popOres.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if  (path.length() > 0) {
			  if(log.isDebug()) log.debug("direct navigation to container-path=" + path);
			}
		}
		
		
		main = new Panel("videorunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == videoDispCtr){
			if (event instanceof VideoEvent	) {
				VideoEvent videoEvent = (VideoEvent) event;
				if (videoEvent.getCommand().equals(VideoEvent.ENDED)) {
					//TODO: catch even fired when video ended
					// increment attempt variable
				}
			}
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// controllers auto-disposed
	}
	
	private void doLaunch(UserRequest ureq){
		VelocityContainer myContent = createVelocityContainer("run");
		if (videoRoot == null) {
			RepositoryEntry re = VideoEditController.getVideoReference(config, false);
			if (re == null) {
				showError(VideoEditController.NLS_ERROR_VIDEOREPOENTRYMISSING);
				return;
			}


		}
		RepositoryEntry videoEntry = videoNode.getReferencedRepositoryEntry();
		// configure the display controller according to config
		boolean autoplay = config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY);
		boolean comments = config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS);
		boolean ratings = config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING);
		String ident = videoNode.getIdent();
		String customtext = config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT);
		
		switch(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT,"none")) {
			case "resourceDescription":
					videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, autoplay, comments, ratings, true, ident,
							false, false, "", userCourseEnv.isCourseReadOnly());
					break;
			case "customDescription":
					videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, autoplay, comments, ratings, true, ident,
							true, false, customtext, userCourseEnv.isCourseReadOnly());
					break;
			case "none":
					videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, autoplay, comments, ratings, true, ident,
							true, false, "", userCourseEnv.isCourseReadOnly());
					break;
		}		
		listenTo(videoDispCtr);
		
		myContent.put("videoDisplay", videoDispCtr.getInitialComponent());
		main.setContent(myContent);
		
		// Update launch counter
		repositoryService.incrementLaunchCounter(videoEntry);
	}

	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq) {
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, videoNode, "o_icon_video");
		return new NodeRunConstructionResult(ctrl);
	}
}
