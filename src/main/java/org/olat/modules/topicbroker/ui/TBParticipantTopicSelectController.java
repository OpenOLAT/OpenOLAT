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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantTopicSelectController extends FormBasicController {

	private SingleSelection topicEl;
	private SingleSelection priorityEl;
	private MultipleSelectionElement enrollEl;
	
	private final TBBroker broker;
	private final Identity participantIdentity;
	private final List<TBSelection> selections;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	protected TBParticipantTopicSelectController(UserRequest ureq, WindowControl wControl, TBBroker broker,
			Identity participantIdentity, List<TBSelection> selections) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.broker = broker;
		this.participantIdentity = participantIdentity;
		this.selections = selections;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Set<Long> selectedTopicKeys = selections.stream()
				.map(selection -> selection.getTopic().getKey())
				.collect(Collectors.toSet());
		
		TBTopicSearchParams topicSearchParams = new TBTopicSearchParams();
		topicSearchParams.setBroker(broker);
		List<TBTopic> topics = topicBrokerService.getTopics(topicSearchParams);
		
		Set<Long> allGroupRestrictionKeys = topics.stream()
				.map(TBTopic::getGroupRestrictionKeys)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
		Set<Long> participantGroupKeys = topicBrokerService.filterMembership(participantIdentity, allGroupRestrictionKeys);
		topics.removeIf(topic -> topic.getGroupRestrictionKeys() != null
				&& topic.getGroupRestrictionKeys().stream().noneMatch(key -> participantGroupKeys.contains(key)));
		
		SelectionValues topicsSV = new SelectionValues();
		topics.stream()
				.filter(topic -> !selectedTopicKeys.contains(topic.getKey()))
				.sorted((t1, t2) -> Integer.compare(t1.getSortOrder(), t2.getSortOrder()))
				.forEach(topic -> topicsSV.add(SelectionValues.entry(topic.getKey().toString(), topic.getTitle())));
		topicEl = uifactory.addDropdownSingleselect("selection.topic", formLayout, topicsSV.keys(), topicsSV.values());
		topicEl.setMandatory(true);
		topicEl.setFocus(true);
		if (topicEl.getKeys().length > 0) {
			topicEl.select(topicEl.getKey(0), true);
		}
		
		SelectionValues prioritySV = new SelectionValues();
		for (int i = 0; i<= selections.size(); i++) {
			String priority = String.valueOf(i+1);
			prioritySV.add(SelectionValues.entry(priority, priority));
		}
		priorityEl = uifactory.addDropdownSingleselect("selection.priority", formLayout, prioritySV.keys(), prioritySV.values());
		priorityEl.setMandatory(true);
		priorityEl.select(String.valueOf(selections.size() + 1), true);
		
		enrollEl = uifactory.addCheckboxesHorizontal("selection.enroll", formLayout, new String[] { "enroll" },
				new String[] { translate("selection.enroll.value") });
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("participant.topic.select.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		topicEl.clearError();
		if (!topicEl.isOneSelected()) {
			topicEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		priorityEl.clearError();
		if (!priorityEl.isOneSelected()) {
			priorityEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		topicBrokerService.select(getIdentity(), participantIdentity, () -> Long.valueOf(topicEl.getSelectedKey()),
				Integer.valueOf(priorityEl.getSelectedKey()));
		if (enrollEl.isAtLeastSelected(1)) {
			topicBrokerService.enroll(getIdentity(), participantIdentity, () -> Long.valueOf(topicEl.getSelectedKey()), false);
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
