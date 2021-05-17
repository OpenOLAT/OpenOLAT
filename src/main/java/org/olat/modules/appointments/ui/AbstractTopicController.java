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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractTopicController extends FormBasicController {

	protected static final String KEY_MULTI_PARTICIPATION = "multi.participation";
	protected static final String KEY_COACH_CONFIRMATION = "coach.confirmation";
	protected static final String KEY_PARTICIPATION_VISIBLE = "participation.visible";
	protected TextElement titleEl;
	protected TextElement descriptionEl;
	protected SingleSelection typeEl;
	protected MultipleSelectionElement configurationEl;
	protected MultipleSelectionElement organizerEl;
	
	private final TopicLight initialTopic;
	private final OrganizerCandidateSupplier organizerCandidateSupplier;
	protected Set<Identity> organizerCandidates;
	protected List<Organizer> organizers;
	protected boolean multiParticipationsSelected;
	protected boolean coachConfirmationSelected;
	protected boolean participationVisible;
	
	@Autowired
	protected AppointmentsService appointmentsService;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;

	public AbstractTopicController(UserRequest ureq, WindowControl wControl, TopicLight initialTopic,
			OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl);
		this.initialTopic = initialTopic;
		this.organizerCandidateSupplier = organizerCandidateSupplier;
	}
	
	public AbstractTopicController(UserRequest ureq, WindowControl wControl, Form rootForm, TopicLight initialTopic,
			OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		this.initialTopic = initialTopic;
		this.organizerCandidateSupplier = organizerCandidateSupplier;
	}

	protected void init(UserRequest ureq) {
		organizers = getCurrentOrganizers();
		List<Identity> loadedOrganizerCandidates = organizerCandidateSupplier.getOrganizerCandidates();
		loadedOrganizerCandidates.addAll(organizers.stream().map(Organizer::getIdentity).collect(Collectors.toList()));
		organizerCandidates = new HashSet<>(loadedOrganizerCandidates);
		
		multiParticipationsSelected = initialTopic.isMultiParticipation();
		coachConfirmationSelected = !initialTopic.isAutoConfirmation();
		participationVisible = initialTopic.isParticipationVisible();
		
		initForm(ureq);
		updateUI();
	}

	protected abstract RepositoryEntryRef getRepositoryEntry();

	protected abstract List<Organizer> getCurrentOrganizers();

	protected abstract void initButtons(FormItemContainer formLayout, UserRequest ureq);

	protected abstract boolean isConfigChangeable();

	protected abstract boolean isConfigChanged();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Topic
		String title = initialTopic == null ? "" : initialTopic.getTitle();
		titleEl = uifactory.addTextElement("topic.title", "topic.title", 128, title, formLayout);
		titleEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(title)) {
			titleEl.setFocus(true);
		}
		
		String description = initialTopic == null ? "" : initialTopic.getDescription();
		descriptionEl = uifactory.addTextAreaElement("topic.description", "topic.description", 2000, 4, 72, false,
				false, description, formLayout);
		
		// Configs
		KeyValues typeKV = new KeyValues();
		typeKV.add(entry(Topic.Type.enrollment.name(), translate("topic.type.enrollment")));
		typeKV.add(entry(Topic.Type.finding.name(), translate("topic.type.finding")));
		typeEl = uifactory.addRadiosHorizontal("topic.type", formLayout, typeKV.keys(), typeKV.values());
		typeEl.select(initialTopic.getType().name(), true);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		
		configurationEl = uifactory.addCheckboxesVertical("topic.configuration", formLayout, emptyStrings(),
				emptyStrings(), 1);
		configurationEl.addActionListener(FormEvent.ONCHANGE);
		
		// Organizers
		KeyValues organizerCandidateKV = new KeyValues();
		for (Identity organizerCandidate : organizerCandidates) {
			organizerCandidateKV.add(entry(organizerCandidate.getKey().toString(), userManager.getUserDisplayName(organizerCandidate.getKey())));
		}
		organizerCandidateKV.sort(VALUE_ASC);
		organizerEl = uifactory.addCheckboxesDropdown("organizer", "organizer", formLayout, organizerCandidateKV.keys(), organizerCandidateKV.values());
		for (Organizer organizer : organizers) {
			Long organizerKey = organizer.getIdentity().getKey();
			if (organizerCandidates.stream().anyMatch(organizerCandidate -> organizerKey.equals(organizerCandidate.getKey()))) {
				organizerEl.select(organizerKey.toString(), true);
			}
		}
		
		initButtons(formLayout, ureq);
	}

	private void updateUI() {
		updateUI(isConfigChangeable());
	}

	protected void updateUI(boolean configChangeable) {
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
				typeEl.select(initialTopic.getType().name(), true);
				configurationEl.select(KEY_MULTI_PARTICIPATION, initialTopic.isMultiParticipation());
				configurationEl.select(KEY_COACH_CONFIRMATION, !initialTopic.isAutoConfirmation());
				updateUI(false);
				showWarning("error.config.not.changeable");
			}
		
		return allOk;
	}
	
	public void updatedAttributes(TopicLight topic) {
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
	}
	
	public Collection<Identity> getOrganizers() {
		Collection<String> selectedOrganizerKeys = organizerEl.getSelectedKeys();
		return organizerCandidates.stream()
				.filter(i -> selectedOrganizerKeys.contains(i.getKey().toString()))
				.collect(Collectors.toList());
	}

	@Override
	protected void doDispose() {
		//
	}

}