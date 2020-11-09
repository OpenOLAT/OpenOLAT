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

import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.appointments.TopicRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicEditController extends FormBasicController {
	
	private static final String KEY_MULTI_PARTICIPATION = "multi.participation";
	private static final String KEY_COACH_CONFIRMATION = "coach.confirmation";
	private static final String KEY_PARTICIPATION_VISIBLE = "participation.visible";
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	private SingleSelection typeEl;
	private MultipleSelectionElement configurationEl;
	private MultipleSelectionElement organizerEl;
	
	private Topic topic;
	private List<Organizer> organizers;
	private List<Identity> coaches;
	private boolean multiParticipationsSelected;
	private boolean coachConfirmationSelected;
	private boolean participationVisible;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;

	public TopicEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		this.topic = topic;
		organizers = appointmentsService.getOrganizers(topic);
		coaches = repositoryService.getMembers(topic.getEntry(), RepositoryEntryRelationType.all,
				GroupRoles.coach.name());
		
		multiParticipationsSelected = topic.isMultiParticipation();
		coachConfirmationSelected = !topic.isAutoConfirmation();
		participationVisible = topic.isParticipationVisible();
		
		initForm(ureq);
		updateUI();
	}
	
	public TopicRef getTopic() {
		return topic;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Topic
		String title = topic == null ? "" : topic.getTitle();
		titleEl = uifactory.addTextElement("topic.title", "topic.title", 128, title, formLayout);
		titleEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(title)) {
			titleEl.setFocus(true);
		}
		
		String description = topic == null ? "" : topic.getDescription();
		descriptionEl = uifactory.addTextAreaElement("topic.description", "topic.description", 2000, 4, 72, false,
				false, description, formLayout);
		
		// Configs
		KeyValues typeKV = new KeyValues();
		typeKV.add(entry(Topic.Type.enrollment.name(), translate("topic.type.enrollment")));
		typeKV.add(entry(Topic.Type.finding.name(), translate("topic.type.finding")));
		typeEl = uifactory.addRadiosHorizontal("topic.type", formLayout, typeKV.keys(), typeKV.values());
		typeEl.select(topic.getType().name(), true);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		
		configurationEl = uifactory.addCheckboxesVertical("topic.configuration", formLayout, emptyStrings(),
				emptyStrings(), 1);
		configurationEl.addActionListener(FormEvent.ONCHANGE);
		
		// Organizers
		KeyValues coachesKV = new KeyValues();
		for (Identity coach : coaches) {
			coachesKV.add(entry(coach.getKey().toString(), userManager.getUserDisplayName(coach.getKey())));
		}
		coachesKV.sort(VALUE_ASC);
		organizerEl = uifactory.addCheckboxesDropdown("organizer", "organizer", formLayout, coachesKV.keys(), coachesKV.values());
		for (Organizer organizer : organizers) {
			Long organizerKey = organizer.getIdentity().getKey();
			if (coaches.stream().anyMatch(coach -> organizerKey.equals(coach.getKey()))) {
				organizerEl.select(organizerKey.toString(), true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		
		updateUI();
	}

	private void updateUI() {
		updateUI(isConfigChangeable());
	}

	private void updateUI(boolean configChangeable) {
		typeEl.setEnabled(configChangeable);
		
		boolean enrollment = typeEl.isOneSelected() && Type.valueOf(typeEl.getSelectedKey()) != Type.finding;
		
		KeyValues configKV = new KeyValues();
		configKV.add(entry(KEY_MULTI_PARTICIPATION, translate("topic.multi.participation")));
		if (enrollment) {
			configKV.add(entry(KEY_COACH_CONFIRMATION, translate("topic.coach.confirmation")));
		}
		configKV.add(entry(KEY_PARTICIPATION_VISIBLE, translate("topic.participation.visible")));
		configurationEl.setKeysAndValues(configKV.keys(), configKV.values());
		configurationEl.select(KEY_MULTI_PARTICIPATION, multiParticipationsSelected);
		configurationEl.setEnabled(KEY_MULTI_PARTICIPATION, configChangeable);
		configurationEl.select(KEY_COACH_CONFIRMATION, coachConfirmationSelected);
		configurationEl.setEnabled(KEY_COACH_CONFIRMATION, configChangeable);
		configurationEl.select(KEY_PARTICIPATION_VISIBLE, participationVisible);
		configurationEl.setEnabled(KEY_PARTICIPATION_VISIBLE, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == typeEl) {
			updateUI();
		} else if (source == configurationEl) {
			Collection<String> configKeys = configurationEl.getSelectedKeys();
			multiParticipationsSelected = configKeys.contains(KEY_MULTI_PARTICIPATION);
			coachConfirmationSelected = configKeys.contains(KEY_COACH_CONFIRMATION);
			participationVisible = configKeys.contains(KEY_PARTICIPATION_VISIBLE);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (isConfigChanged() && !isConfigChangeable()) {
				typeEl.select(topic.getType().name(), true);
				configurationEl.select(KEY_MULTI_PARTICIPATION, topic.isMultiParticipation());
				configurationEl.select(KEY_COACH_CONFIRMATION, !topic.isAutoConfirmation());
				updateUI(false);
				showWarning("error.config.not.changeable");
			}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSaveTopic();
		doSaveOrgianzers();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSaveTopic() {
		String title = titleEl.getValue();
		topic.setTitle(title);
		
		String description = descriptionEl.getValue();
		topic.setDescription(description);
		
		Type type = typeEl.isOneSelected() ? Type.valueOf(typeEl.getSelectedKey()) : Type.enrollment;
		topic.setType(type);
		
		Collection<String> configKeys = configurationEl.getSelectedKeys();
		boolean multiParticipation = configKeys.contains(KEY_MULTI_PARTICIPATION);
		topic.setMultiParticipation(multiParticipation);
		
		boolean autoConfirmation = Type.finding == type
				? false
				: !configKeys.contains(KEY_COACH_CONFIRMATION);
		topic.setAutoConfirmation(autoConfirmation);
		
		boolean participationVisible = configKeys.contains(KEY_PARTICIPATION_VISIBLE);
		topic.setParticipationVisible(participationVisible);
		
		topic = appointmentsService.updateTopic(topic);
	}
	
	private void doSaveOrgianzers() {
		Collection<String> selectedOrganizerKeys = organizerEl.getSelectedKeys();
		List<Identity> selectedOrganizers = coaches.stream()
				.filter(i -> selectedOrganizerKeys.contains(i.getKey().toString()))
				.collect(Collectors.toList());
		appointmentsService.updateOrganizers(topic, selectedOrganizers);
	}
	
	private boolean isConfigChanged() {
		Type type = typeEl.isOneSelected() ? Type.valueOf(typeEl.getSelectedKey()) : Type.enrollment;
		Collection<String> configKeys = configurationEl.getSelectedKeys();
		boolean multiParticipation = configKeys.contains(KEY_MULTI_PARTICIPATION);
		boolean autoConfirmation = Type.finding == type
				? false
				: !configKeys.contains(KEY_COACH_CONFIRMATION);
		
		return type != topic.getType()
				|| multiParticipation != topic.isMultiParticipation()
				|| autoConfirmation != topic.isAutoConfirmation();
	}

	private boolean isConfigChangeable() {
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setTopic(topic);
		Long participationCount = appointmentsService.getParticipationCount(params);
		return participationCount.longValue() == 0;
	}

	@Override
	protected void doDispose() {
		//
	}

}
