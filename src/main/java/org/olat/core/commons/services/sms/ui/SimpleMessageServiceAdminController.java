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
package org.olat.core.commons.services.sms.ui;

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
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SimpleMessageServiceAdminController extends BasicController {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink, statisticsLink;
	
	private MessagesStatisticsController statisticsCtrl;
	private SimpleMessageServiceAdminConfigurationController configCtrl;
	
	public SimpleMessageServiceAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);

		configurationLink = LinkFactory.createLink("admin.settings", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		statisticsLink = LinkFactory.createLink("admin.statistics", mainVC, this);
		segmentView.addSegment(statisticsLink, false);
		
		doOpenConfiguration(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == statisticsLink) {
					doOpenStatistics(ureq);
				}
			}
		}
	}

	private void doOpenStatistics(UserRequest ureq) {
		if(statisticsCtrl == null) {
			statisticsCtrl = new MessagesStatisticsController(ureq, getWindowControl());
			listenTo(statisticsCtrl);
		}
		mainVC.put("segmentCmp", statisticsCtrl.getInitialComponent());
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configCtrl == null) {
			configCtrl = new SimpleMessageServiceAdminConfigurationController(ureq, getWindowControl());
			listenTo(configCtrl);
		}
		mainVC.put("segmentCmp", configCtrl.getInitialComponent());
	}
}
