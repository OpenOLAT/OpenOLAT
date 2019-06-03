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
package org.olat.course.nodes.livestream.ui;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.livestream.LiveStreamEvent;

/**
 * 
 * Initial date: 29 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamVideoController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(LiveStreamVideoController.class);
	
	private final VelocityContainer mainVC;
	private Link retryLink;
	
	private String url;
	private boolean error = false;

	protected LiveStreamVideoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("video");
		updateUI();
		putInitialPanel(mainVC);
	}
	
	public void setEvent(LiveStreamEvent event) {
		String newUrl = event != null? event.getLiveStreamUrl(): null;
		if (newUrl == null || !newUrl.equalsIgnoreCase(url)) {
			url = newUrl;
			error = Boolean.FALSE;
			updateUI();
		}
	}

	private void updateUI() {
		if (error) {
			mainVC.contextRemove("id");
			mainVC.contextPut("error", error);
			retryLink = LinkFactory.createButton("viewer.retry", mainVC, this);
		} else {
			mainVC.contextRemove("error");
			if (StringHelper.containsNonWhitespace(url)) {
				mainVC.contextPut("id", CodeHelper.getRAMUniqueID());
				mainVC.contextPut("src", url);
			} else {
				mainVC.contextRemove("id");
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("error".equals(event.getCommand())) {
			log.debug("Error when open a video from {}", url);
			error = true;
			updateUI();
		} else if (source == retryLink) {
			error = false;
			updateUI();
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
