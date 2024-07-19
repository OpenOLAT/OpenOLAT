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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.topicbroker.TBGroupRestrictionCandidates;
import org.olat.modules.topicbroker.TBGroupRestrictionInfo;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicBulkGroupRestrictionController extends FormBasicController {
	
	private MultipleSelectionElement groupRestrictionsAddEl;
	private MultipleSelectionElement groupRestrictionsRemoveEl;

	private List<TBTopic> topics;
	private TBGroupRestrictionCandidates groupRestrictionCandidates;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	protected TBTopicBulkGroupRestrictionController(UserRequest ureq, WindowControl wControl,
			List<TBTopic> topics, TBGroupRestrictionCandidates groupRestrictionCandidates) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.topics = topics;
		this.groupRestrictionCandidates = groupRestrictionCandidates;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (groupRestrictionCandidates.getBusinessGroupKeys() != null && !groupRestrictionCandidates.getBusinessGroupKeys().isEmpty()) {
			List<TBGroupRestrictionInfo> groupInfoAdd = topicBrokerService
					.getGroupRestrictionInfos(getTranslator(), groupRestrictionCandidates.getBusinessGroupKeys());
			SelectionValues businessGroupSV = new SelectionValues();
			groupInfoAdd.stream()
					.sorted((i1, i2) -> i1.getGroupName().compareToIgnoreCase(i2.getGroupName()))
					.forEach(groupInfo -> businessGroupSV
							.add(SelectionValues.entry(groupInfo.getGroupKey().toString(), groupInfo.getGroupName())));
			
			groupRestrictionsAddEl = uifactory.addCheckboxesDropdown("topics.bulk.group.restriction.add",
					"topics.bulk.group.restriction.add", formLayout, businessGroupSV.keys(), businessGroupSV.values());
		} else {
			uifactory.addStaticTextElement("topics.bulk.group.restriction.add", "topics.bulk.group.restriction.add",
					translate("topic.group.restriction.no.candidates"), formLayout);
		}
		
		Set<Long> allGroupRestrictionKeys = topics.stream()
			.map(TBTopic::getGroupRestrictionKeys)
			.filter(Objects::nonNull)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
		if (!allGroupRestrictionKeys.isEmpty()) {
			List<TBGroupRestrictionInfo> groupInfoRemove = topicBrokerService
					.getGroupRestrictionInfos(getTranslator(), allGroupRestrictionKeys);
			SelectionValues businessGroupSV = new SelectionValues();
			groupInfoRemove.stream()
					.sorted((i1, i2) -> i1.getGroupName().compareToIgnoreCase(i2.getGroupName()))
					.forEach(businessGroup -> businessGroupSV.add(SelectionValues
							.entry(businessGroup.getGroupKey().toString(), businessGroup.getGroupName())));
			
			groupRestrictionsRemoveEl = uifactory.addCheckboxesDropdown("topics.bulk.group.restriction.remove",
					"topics.bulk.group.restriction.remove", formLayout, businessGroupSV.keys(), businessGroupSV.values());
		} else {
			uifactory.addStaticTextElement("topics.bulk.group.restriction.remove", "topics.bulk.group.restriction.remove",
					translate("topic.group.restriction.no.candidates"), formLayout);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Long> keysAdd = groupRestrictionsAddEl != null
				? groupRestrictionsAddEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet())
				: Set.of();
		Set<Long> keysRemove = groupRestrictionsRemoveEl != null
				? groupRestrictionsRemoveEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet())
				: Set.of();
		
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setTopics(topics);
		List<TBTopic> reloadedTopics = topicBrokerService.getTopics(searchParams);
		for (TBTopic topic : reloadedTopics) {
			Set<Long> groupRestricionKeys = new HashSet<>();
			if (topic.getGroupRestrictionKeys() != null) {
				groupRestricionKeys.addAll(topic.getGroupRestrictionKeys());
			}
			groupRestricionKeys.addAll(keysAdd);
			groupRestricionKeys.removeAll(keysRemove);
			topicBrokerService.updateTopic(getIdentity(), topic, topic.getIdentifier(), topic.getTitle(), topic.getDescription(), topic.getMinParticipants(), topic.getMaxParticipants(), groupRestricionKeys);
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
