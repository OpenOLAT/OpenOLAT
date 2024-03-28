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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;

/**
 * Initial date: 2024-03-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChooseImageController extends BasicController {

	private final MediaCenterController mediaCenterController;
	private Media mediaReference;

	public ChooseImageController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		MediaCenterConfig mediaCenterConfig = new MediaCenterConfig(true, false, false,
				true, true, true, "image",
				MediaCenterController.ALL_TAB_ID, SearchMediaParameters.Access.DIRECT, null);
		mediaCenterController = new MediaCenterController(ureq, wControl, null, mediaCenterConfig);
		listenTo(mediaCenterController);

		putInitialPanel(mediaCenterController.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (mediaCenterController == source) {
			if (event instanceof MediaSelectionEvent mediaSelectionEvent) {
				if (mediaSelectionEvent.getMedia() != null) {
					mediaReference = mediaSelectionEvent.getMedia();
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}
		}
		super.event(ureq, source, event);
	}

	public Media getMediaReference() {
		return mediaReference;
	}
}
