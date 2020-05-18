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
package org.olat.course.nodes.appointments.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
import org.olat.course.nodes.appointments.Topic;

/**
 * 
 * Initial date: 16 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicRunCoachController extends BasicController {

	private TopicHeaderController headerCtrl;
	private Controller appointmentsCtrl;
	private Link backLink;

	public TopicRunCoachController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback, Configuration config) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("topic_run_coach");
		
		backLink = LinkFactory.createLinkBack(mainVC, this);
		
		headerCtrl = new TopicHeaderController(ureq, wControl, topic, false);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		appointmentsCtrl = new TopicCoachController(ureq, wControl, topic, secCallback, config);
		listenTo(appointmentsCtrl);
		mainVC.put("appointments", appointmentsCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == appointmentsCtrl) {
			if (event instanceof TopicChangedEvent) {
				TopicChangedEvent tce = (TopicChangedEvent)event;
				headerCtrl.setTopic(ureq, tce.getTopic());
			} else if (event instanceof OrganizersChangedEvent) {
				headerCtrl.reloadOrganizers();
			} else if (event == Event.BACK_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		//
	}

}
