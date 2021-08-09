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
package org.olat.modules.appointments.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.spacesaver.ToggleBoxController;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Topic;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicHeaderController extends BasicController {
	
	private static final String CMD_EMAIL = "email";
	
	private VelocityContainer mainVC;
	private Link emailLink;

	private ToggleBoxController descriptionCtrl;
	private CloseableModalController cmc;
	private OrganizerMailController mailCtrl;
	
	private final boolean email;
	private Topic topic;
	private List<Organizer> organizers;

	@Autowired
	private AppointmentsService appointmentsService;

	public TopicHeaderController(UserRequest ureq, WindowControl wControl, Topic topic, boolean email) {
		super(ureq, wControl);
		this.email = email;
		
		mainVC = createVelocityContainer("topic_header");
		setTopic(ureq, topic);
		reloadOrganizers();
		
		putInitialPanel(mainVC);
	}

	public void setTopic(UserRequest ureq, Topic topic) {
		this.topic = topic;
		putTopicToVC(ureq);
	}

	public void reloadOrganizers() {
		organizers = appointmentsService.getOrganizers(topic);
		putOrganizersToVC(organizers);
	}
	
	private void putTopicToVC(UserRequest ureq) {
		mainVC.contextPut("title", topic.getTitle());
		
		if (StringHelper.containsNonWhitespace(topic.getDescription())) {
			VelocityContainer descriptionCont = createVelocityContainer("topic_description");
			descriptionCont.setDomReplacementWrapperRequired(false);
			descriptionCont.contextPut("description", AppointmentsUIFactory.lineBreakToBr(topic.getDescription()));
			descriptionCtrl = new ToggleBoxController(ureq, getWindowControl(), topic.getKey().toString(),
					translate("topic.description.closed"), translate("topic.description.opened"), descriptionCont);
			mainVC.put("description", descriptionCtrl.getInitialComponent());
		} else {
			mainVC.remove("description");
		}
	}
	
	private void putOrganizersToVC(List<Organizer> organizers) {
		mainVC.contextPut("organizerNames", AppointmentsUIFactory.formatOrganizers(organizers));
		
		if (email && !organizers.isEmpty()) {
			emailLink = LinkFactory.createCustomLink("email", CMD_EMAIL, null, Link.NONTRANSLATED, mainVC, this);
			emailLink.setIconLeftCSS("o_icon o_icon_mail");
			emailLink.setElementCssClass("o_mail");
			mainVC.contextPut("email", emailLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == emailLink) {
			doOrganizerEmail(ureq);
		}
	}
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mailCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(mailCtrl);
		removeAsListenerAndDispose(cmc);
		mailCtrl = null;
		cmc = null;
	}
	
	private void doOrganizerEmail(UserRequest ureq) {
		mailCtrl = new OrganizerMailController(ureq, getWindowControl(), topic, organizers);
		listenTo(mailCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), mailCtrl.getInitialComponent(), true,
				translate("email.title"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void doDispose() {
		//
	}

}
