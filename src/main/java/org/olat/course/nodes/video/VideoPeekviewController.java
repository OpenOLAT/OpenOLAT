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
import org.olat.modules.video.manager.MediaMapper;

public class VideoPeekviewController  extends BasicController implements Controller{

	public VideoPeekviewController(UserRequest ureq, WindowControl wControl,  VFSContainer posterFolder) {
		super(ureq, wControl);
		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		String mediaUrl = registerMapper(ureq, new MediaMapper(posterFolder));
		peekviewVC.contextPut("mediaUrl", mediaUrl);
		peekviewVC.contextPut("nodeLink", posterFolder);
		putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
	}

}