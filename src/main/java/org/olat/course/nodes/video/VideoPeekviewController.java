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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * peekviewcontroller of videomodule
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoPeekviewController  extends BasicController implements Controller{

	@Autowired
	private VideoManager videoManager;

	public VideoPeekviewController(UserRequest ureq, WindowControl wControl,  OLATResource videoResource) {
		super(ureq, wControl);
		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		VFSContainer posterFolder = videoManager.getMasterContainer(videoResource);
		String masterMapperId = "master-" + videoResource.getResourceableId();
		String mediaUrl = registerCacheableMapper(ureq, masterMapperId, new VideoMediaMapper(posterFolder));
		peekviewVC.contextPut("mediaUrl", mediaUrl);
		peekviewVC.contextPut("nodeLink", posterFolder);
		putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//TODO: click on image should launch run view, activate in autoplay mode		
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}