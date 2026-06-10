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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Segmented admin controller for the AI module. Provides four segments:
 * providers, features, processing pools and usage log.
 * <p>
 * The segment controller is recreated on every activation so each view
 * always shows the current configuration — e.g. a provider added in the
 * first segment is immediately selectable in the features segment, and
 * the pool statistics are a fresh snapshot on every visit.
 *
 * Initial date: 07.04.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiAdminController extends BasicController {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link providersLink;
	private final Link featuresLink;
	private final Link poolsLink;
	private final Link usageLogLink;

	private Controller segmentCtrl;

	public AiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("ai_admin");
		putInitialPanel(mainVC);

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		providersLink = LinkFactory.createLink("segment.providers", mainVC, this);
		segmentView.addSegment(providersLink, true);
		featuresLink = LinkFactory.createLink("segment.features", mainVC, this);
		segmentView.addSegment(featuresLink, false);
		poolsLink = LinkFactory.createLink("segment.pools", mainVC, this);
		segmentView.addSegment(poolsLink, false);
		usageLogLink = LinkFactory.createLink("usagelog", mainVC, this);
		segmentView.addSegment(usageLogLink, false);

		doOpenProviders(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == providersLink) {
					doOpenProviders(ureq);
				} else if (clickedLink == featuresLink) {
					doOpenFeatures(ureq);
				} else if (clickedLink == poolsLink) {
					doOpenPools(ureq);
				} else if (clickedLink == usageLogLink) {
					doOpenUsageLog(ureq);
				}
			}
		}
	}

	private void doOpenProviders(UserRequest ureq) {
		setSegmentController(new AiProvidersAdminController(ureq, getWindowControl()));
	}

	private void doOpenFeatures(UserRequest ureq) {
		setSegmentController(new AiFeaturesAdminController(ureq, getWindowControl()));
	}

	private void doOpenPools(UserRequest ureq) {
		setSegmentController(new AiTaskPoolAdminController(ureq, getWindowControl()));
	}

	private void doOpenUsageLog(UserRequest ureq) {
		setSegmentController(new AiUsageLogAdminController(ureq, getWindowControl()));
	}

	/**
	 * Replace the active segment controller. The previous controller is
	 * disposed and a fresh one rendered — never reuse a cached instance,
	 * its form values would show stale configuration.
	 */
	private void setSegmentController(Controller ctrl) {
		removeAsListenerAndDispose(segmentCtrl);
		segmentCtrl = ctrl;
		listenTo(segmentCtrl);
		mainVC.put("segmentCmp", segmentCtrl.getInitialComponent());
	}
}
