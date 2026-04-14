/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.ui;

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
 * Segmented admin controller for the AI module. Provides two segments:
 * configuration and usage log.
 *
 * Initial date: 07.04.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiAdminController extends BasicController {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink;
	private final Link usageLogLink;

	private AiConfigurationAdminController configurationCtrl;
	private AiUsageLogAdminController usageLogCtrl;

	public AiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("ai_admin");
		putInitialPanel(mainVC);

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		usageLogLink = LinkFactory.createLink("usagelog", mainVC, this);
		segmentView.addSegment(usageLogLink, false);

		doOpenConfiguration(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == usageLogLink) {
					doOpenUsageLog(ureq);
				}
			}
		}
	}

	private void doOpenConfiguration(UserRequest ureq) {
		if (configurationCtrl == null) {
			configurationCtrl = new AiConfigurationAdminController(ureq, getWindowControl());
			listenTo(configurationCtrl);
		}
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}

	private void doOpenUsageLog(UserRequest ureq) {
		if (usageLogCtrl == null) {
			usageLogCtrl = new AiUsageLogAdminController(ureq, getWindowControl());
			listenTo(usageLogCtrl);
		}
		mainVC.put("segmentCmp", usageLogCtrl.getInitialComponent());
	}
}
