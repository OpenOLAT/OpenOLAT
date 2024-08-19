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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.user.UserInfoController;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantController extends UserInfoController {

	private SingleSelection boostEl;
	private SingleSelection requiredEnrollmentsEl;
	
	private final TBBroker broker;
	private final TBParticipant participant;
	private final boolean canEditParticipant;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBParticipantController(UserRequest ureq, WindowControl wControl, Form mainForm, TBBroker broker,
			TBParticipant participant, UserInfoProfileConfig profileConfig, UserInfoProfile profile,
			boolean canEditParticipant) {
		super(ureq, wControl, mainForm, profileConfig, profile);
		this.broker = broker;
		this.participant = participant;
		this.canEditParticipant = canEditParticipant;
		
		initForm(ureq);
	}

	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		super.initFormItems(itemsCont, listener, ureq);
		SelectionValues boostSV = new SelectionValues();
		boostSV.add(SelectionValues.entry("0", translate("participant.boost.0")));
		boostSV.add(SelectionValues.entry("1", translate("participant.boost.1")));
		boostSV.add(SelectionValues.entry("2", translate("participant.boost.2")));
		String boostElName = "boost_" + participant.getKey();
		boostEl = uifactory.addDropdownSingleselect(boostElName, boostElName, "participant.boost", itemsCont,
				boostSV.keys(), boostSV.values(), null);
		boostEl.setMandatory(true);
		boostEl.setEnabled(canEditParticipant);
		boostEl.addActionListener(FormEvent.ONCHANGE);
		if (participant.getBoost() != null) {
			boostEl.select(participant.getBoost().toString(), true);
		} else {
			boostEl.select("0", true);
		}
		
		SelectionValues requiredEnrollmentsSV = new SelectionValues();
		for (int i = 0; i < broker.getRequiredEnrollments(); i++) {
			requiredEnrollmentsSV.add(SelectionValues.entry(String.valueOf(i), String.valueOf(i)));
		}
		requiredEnrollmentsSV.add(SelectionValues.entry(
				String.valueOf(broker.getRequiredEnrollments()),
				translate("participant.max.enrollments.default", String.valueOf(broker.getRequiredEnrollments()))));
		if (participant.getRequiredEnrollments() != null && participant.getRequiredEnrollments() > broker.getRequiredEnrollments()) {
			requiredEnrollmentsSV.add(SelectionValues.entry(String.valueOf(
					participant.getRequiredEnrollments()),
					String.valueOf(participant.getRequiredEnrollments())));
		}
		String enrollemntsElName = "enrollments_" + participant.getKey();
		requiredEnrollmentsEl = uifactory.addDropdownSingleselect(enrollemntsElName, enrollemntsElName, "participant.max.enrollments",
				itemsCont, requiredEnrollmentsSV.keys(), requiredEnrollmentsSV.values(), null);
		requiredEnrollmentsEl.setMandatory(true);
		requiredEnrollmentsEl.setEnabled(canEditParticipant);
		requiredEnrollmentsEl.addActionListener(FormEvent.ONCHANGE);
		if (participant.getRequiredEnrollments() != null) {
			requiredEnrollmentsEl.select(participant.getRequiredEnrollments().toString(), true);
		} else {
			requiredEnrollmentsEl.select(String.valueOf(broker.getRequiredEnrollments()), true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == boostEl) {
			doUpdateParticipant(ureq);
		} else if (source == requiredEnrollmentsEl) {
			doUpdateParticipant(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doUpdateParticipant(UserRequest ureq) {
		Integer boost = null;
		if (boostEl.isOneSelected() && !"0".equals(boostEl.getSelectedKey())) {
			boost = Integer.valueOf(boostEl.getSelectedKey());
		}
		participant.setBoost(boost);
		
		Integer maxEnrollments = null;
		if (requiredEnrollmentsEl.isOneSelected() && !requiredEnrollmentsEl.getSelectedKey().equals(String.valueOf(broker.getRequiredEnrollments()))) {
			Integer selectedMaxEnrollments = Integer.valueOf(requiredEnrollmentsEl.getSelectedKey());
			if (broker.getRequiredEnrollments().intValue() != selectedMaxEnrollments.intValue()) {
				maxEnrollments = selectedMaxEnrollments;
			}
		}
		participant.setRequiredEnrollments(maxEnrollments);
		
		topicBrokerService.updateParticipant(getIdentity(), participant);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
