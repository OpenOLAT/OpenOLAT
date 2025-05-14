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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: May 13, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBMaxEnrollmentsController extends FormBasicController {

	private SingleSelection maxEnrollmentsEl;

	private final TBBroker broker;
	private TBParticipant participant;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	protected TBMaxEnrollmentsController(UserRequest ureq, WindowControl wControl, TBParticipant participant) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.broker = participant.getBroker();
		this.participant = topicBrokerService.getOrCreateParticipant(getIdentity(), broker, getIdentity());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("selection.individual.enrollments.desc", null);
		
		SelectionValues maxEnrollmentsSV = new SelectionValues();
		for (int i = 0; i <= broker.getRequiredEnrollments().intValue(); i++) {
			maxEnrollmentsSV.add(SelectionValues.entry(String.valueOf(i), String.valueOf(i)));
		}
		if (participant.getRequiredEnrollments() != null && participant.getRequiredEnrollments() > broker.getRequiredEnrollments()) {
			maxEnrollmentsSV.add(SelectionValues.entry(String.valueOf(
					participant.getRequiredEnrollments()),
					String.valueOf(participant.getRequiredEnrollments())));
		}
		maxEnrollmentsEl = uifactory.addDropdownSingleselect("selection.individual.enrollments.my", formLayout,
				maxEnrollmentsSV.keys(), maxEnrollmentsSV.values());
		String selectedKey = participant.getRequiredEnrollments() != null
				? participant.getRequiredEnrollments().toString()
				: broker.getRequiredEnrollments().toString();
		maxEnrollmentsEl.select(selectedKey, true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		String cancelI18nKey = participant.getRequiredEnrollments() != null
				? "cancel"
				: "selection.individual.enrollments.later";
		uifactory.addFormCancelButton(cancelI18nKey, buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doUpdateParticipant();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doUpdateParticipant() {
		if (maxEnrollmentsEl.isOneSelected()) {
			participant = topicBrokerService.getOrCreateParticipant(getIdentity(), broker, getIdentity());
			Integer maxEnrollments = Integer.valueOf(maxEnrollmentsEl.getSelectedKey());
			participant.setRequiredEnrollments(maxEnrollments);
			participant = topicBrokerService.updateParticipant(getIdentity(), participant);
		}
	}

}
