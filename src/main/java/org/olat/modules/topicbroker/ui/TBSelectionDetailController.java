/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.topicbroker.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 6 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBSelectionDetailController extends BasicController {

	private final TBSelectionDetailHeaderController headerCtrl;
	private final TBTopicDescriptionController descriptionCtrl;

	protected TBSelectionDetailController(UserRequest ureq, WindowControl wControl, TBBroker broker,
			TBParticipant participant, TBTopic topic, List<TBCustomField> customFields) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("selection_detail");
		putInitialPanel(mainVC);
		
		headerCtrl = new TBSelectionDetailHeaderController(ureq, wControl, broker, participant, topic);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		descriptionCtrl = new TBTopicDescriptionController(ureq, wControl, topic, customFields);
		listenTo(descriptionCtrl);
		mainVC.put("description", descriptionCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public void setBroker(TBBroker broker) {
		headerCtrl.setBroker(broker);
	}

}
