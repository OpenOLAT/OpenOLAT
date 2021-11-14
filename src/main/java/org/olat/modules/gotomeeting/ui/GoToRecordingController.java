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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.gotomeeting.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;

/**
 * 
 * The width and height of the video is fixed.
 * 
 * Initial date: 31.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToRecordingController extends BasicController {

	private final VelocityContainer mainVC;
	private Link downloadLink;
	
	private final GoToRecordingsG2T recording;
	
	public GoToRecordingController(UserRequest ureq, WindowControl wControl, GoToRecordingsG2T recording) {
		super(ureq, wControl);
		this.recording = recording;
		
		mainVC = createVelocityContainer("recording");
		downloadLink = LinkFactory.createLink("download", mainVC, this);
		downloadLink.setTarget("_blank");
		downloadLink.setCustomEnabledLinkCSS("o_content_download");

		String url = recording.getDownloadUrl();
		mainVC.contextPut("recordingUrl", url);
		mainVC.contextPut("width", "1024");
		mainVC.contextPut("height", "768");
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == downloadLink) {
			String url = recording.getDownloadUrl();
			MediaResource downloadUrl = new RedirectMediaResource(url);
			ureq.getDispatchResult().setResultingMediaResource(downloadUrl);
		}
	}
}
