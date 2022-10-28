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
package org.olat.modules.audiovideorecording.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2022-10-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RecordingAdminController extends BasicController {
	private final SegmentViewComponent segmentView;
	private final Link configLink;
	private final Link transcodingsLink;
	private final VelocityContainer mainVC;
	private RecordingAdminConfigController configController;
	private RecordingAdminTranscodingsController transcodingsController;

	public RecordingAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("recording_admin");

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);

		configLink = LinkFactory.createLink("tab.admin.recording.configuration", mainVC, this);
		segmentView.addSegment(configLink, true);

		transcodingsLink = LinkFactory.createLink("tab.admin.transcodings", mainVC, this);
		segmentView.addSegment(transcodingsLink, false);

		doOpenConfig(ureq);

		segmentView.select(configLink);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configLink) {
					doOpenConfig(ureq);
				} else if (clickedLink == transcodingsLink) {
					doOpenTranscoding(ureq);
				}
			}
		}
	}

	private void doOpenConfig(UserRequest ureq) {
		if (configController == null) {
			configController = new RecordingAdminConfigController(ureq, getWindowControl());
			listenTo(configController);
		}
		mainVC.put("segmentCmp", configController.getInitialComponent());
	}

	private void doOpenTranscoding(UserRequest ureq) {
		if (transcodingsController == null) {
			transcodingsController = new RecordingAdminTranscodingsController(ureq, getWindowControl());
			listenTo(transcodingsController);
		}
		mainVC.put("segmentCmp", transcodingsController.getInitialComponent());
	}
}
