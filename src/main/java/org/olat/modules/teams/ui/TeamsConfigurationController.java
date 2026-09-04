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
package org.olat.modules.teams.ui;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsConfigurationController extends FormBasicController {

	private static final String FOR_COURSES_KEY = "courses";
	private static final String FOR_APPOINTMENTS_KEY = "appointments";
	private static final String FOR_GROUPS_KEY = "groups";
	private static final String FOR_CHATEXAMS_KEY = "chatexams";
	private static final String FOR_LECTURES_KEY = "lectures";
	private static final String ON_KEY = "on";
	private static final String OFF_KEY = "off";

	private FormToggle moduleEnabled;
	private MultipleSelectionElement enabledForEl;
	private SingleSelection permanentMeetingsEl;
	private FormToggle recordingsEnabledEl;
	private SingleSelection recordingsDefaultEl;
	private SingleSelection recordingsStartModeEl;
	private MultipleSelectionElement publishRecordingsEl;
	private TextElement recordingsDeletionDaysEl;
	private SpacerElement recordingsSpacerEl;
	private FormLayoutContainer recordingCont;
	private StaticTextElement clientIdEl;
	private StaticTextElement secretEl;
	private StaticTextElement tenantEl;
	private SpacerElement spacerEl;
	
	@Autowired
	private TeamsModule teamsModule;
	
	public TeamsConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer configCont = uifactory.addDefaultFormLayout("config", null, formLayout);
		initConfigrationForm(configCont);
		
		recordingCont = uifactory.addDefaultFormLayout("recording", null, formLayout);
		initRecordingForm(recordingCont);
		
		//buttons save - check
		FormLayoutContainer buttonLayout = uifactory.addButtonsFormLayout("save", null, formLayout);
		buttonLayout.setFormLayout("3_9");
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void initConfigrationForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("teams.title"));
		formLayout.setFormInfo(translate("teams.intro"));
		formLayout.setFormContextHelp("manual_user/learningresources/Course_Element_Microsoft_Teams/");
		
		moduleEnabled =uifactory.addToggleButton("teams.module.enabled", "teams.module.enabled", translate("on"), translate("off"), formLayout);
		moduleEnabled.toggle(teamsModule.isEnabled());
		
		SelectionValues forPK = new SelectionValues();
		forPK.add(SelectionValues.entry(FOR_COURSES_KEY, translate("teams.module.enabled.for.courses")));
		forPK.add(SelectionValues.entry(FOR_LECTURES_KEY, translate("teams.module.enabled.for.lectures")));
		forPK.add(SelectionValues.entry(FOR_APPOINTMENTS_KEY, translate("teams.module.enabled.for.appointments")));
		forPK.add(SelectionValues.entry(FOR_GROUPS_KEY, translate("teams.module.enabled.for.groups")));
		forPK.add(SelectionValues.entry(FOR_CHATEXAMS_KEY, translate("teams.module.enabled.for.chat.exams")));
		enabledForEl = uifactory.addCheckboxesVertical("teams.module.enabled.for", formLayout, forPK.keys(), forPK.values(), 1);
		enabledForEl.select(FOR_COURSES_KEY, teamsModule.isCoursesEnabled());
		enabledForEl.select(FOR_LECTURES_KEY, teamsModule.isLecturesEnabled());
		enabledForEl.select(FOR_APPOINTMENTS_KEY, teamsModule.isAppointmentsEnabled());
		enabledForEl.select(FOR_GROUPS_KEY, teamsModule.isGroupsEnabled());
		enabledForEl.select(FOR_CHATEXAMS_KEY, teamsModule.isChatExamsEnabled());
		
		SelectionValues onOffPK = new SelectionValues();
		onOffPK.add(SelectionValues.entry(ON_KEY, translate("on")));
		onOffPK.add(SelectionValues.entry(OFF_KEY, translate("off")));
		permanentMeetingsEl = uifactory.addRadiosHorizontal("teams.module.permanent.meetings", formLayout, onOffPK.keys(), onOffPK.values());
		String permanentSelectedKey = teamsModule.isPermanentMeetingsEnabled() ? ON_KEY : OFF_KEY;
		permanentMeetingsEl.select(permanentSelectedKey, true);
		
		
		String clientId = teamsModule.getApiKey();
		boolean showOldConfiguration = StringHelper.containsNonWhitespace(clientId);
		spacerEl = uifactory.addSpacerElement("old-config-space", formLayout, false);
		spacerEl.setVisible(showOldConfiguration);
		clientIdEl = uifactory.addStaticTextElement("client.id", "azure.adfs.id", clientId, formLayout);
		clientIdEl.setVisible(showOldConfiguration);
		String clientSecret = teamsModule.getApiSecret();
		secretEl = uifactory.addStaticTextElement("secret", "azure.adfs.secret", clientSecret, formLayout);
		secretEl.setVisible(showOldConfiguration);
		String tenant = teamsModule.getTenantGuid();
		tenantEl = uifactory.addStaticTextElement("tenant", "azure.tenant.guid", tenant, formLayout);
		tenantEl.setVisible(showOldConfiguration);
	}
	
	private void initRecordingForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("teams.recordings.title"));
		formLayout.setFormInfo(translate("teams.recordings.intro"));
		boolean hasRecordingTokenKey = StringHelper.containsNonWhitespace(teamsModule.getRecordingTokenKey());
		if(!hasRecordingTokenKey) {
			formLayout.setFormWarning(translate("warning.recordings.token.key"));
		}
		
		recordingsEnabledEl = uifactory.addToggleButton("teams.recordings.enabled", "teams.recordings.enabled",
				translate("on"), translate("off"), formLayout);
		recordingsEnabledEl.toggle(teamsModule.isRecordingsEnabled());
		if(!teamsModule.isRecordingsEnabled()) {
			recordingsEnabledEl.setEnabled(hasRecordingTokenKey);
		}
		
		SelectionValues onOffPK = new SelectionValues();
		onOffPK.add(SelectionValues.entry(ON_KEY, translate("on")));
		onOffPK.add(SelectionValues.entry(OFF_KEY, translate("off")));
		recordingsDefaultEl = uifactory.addRadiosHorizontal("teams.recordings.default", formLayout, onOffPK.keys(), onOffPK.values());
		String permanentSelectedKey = teamsModule.isRecordingsDefaultEnabled() ? ON_KEY : OFF_KEY;
		recordingsDefaultEl.select(permanentSelectedKey, true);
		
		SelectionValues startPK = new SelectionValues();
		startPK.add(SelectionValues.entry(ON_KEY, translate("teams.recordings.auto.start.automatically")));
		startPK.add(SelectionValues.entry(OFF_KEY, translate("teams.recordings.auto.start.manually")));
		recordingsStartModeEl = uifactory.addRadiosVertical("teams.recordings.auto.start", formLayout, startPK.keys(), startPK.values());
		String autoStartKey = teamsModule.isRecordingsAutoStartEnabled() ? ON_KEY : OFF_KEY;
		recordingsStartModeEl.select(autoStartKey, true);
		
		SelectionValues publishingDefaultKV = new SelectionValues();
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.coach.name(), translate("teams.recordings.publish.to.coach")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.participant.name(), translate("teams.recordings.publish.to.participant")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.all.name(), translate("teams.recordings.publish.to.all")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.guest.name(), translate("teams.recordings.publish.to.guest")));
		publishRecordingsEl = uifactory.addCheckboxesVertical("teams.recordings.publishing.default", formLayout, publishingDefaultKV.keys(), publishingDefaultKV.values(), 1);
		for (TeamsRecordingsPublishedRoles publishedRole : teamsModule.getRecordingsDefaultPublicationSettings()) {
			publishRecordingsEl.select(publishedRole.name(), true);
		}
		
		recordingsSpacerEl = uifactory.addSpacerElement("recordings-config-space", formLayout, false);
		
		String deletionDays = teamsModule.getRecordingsDeletionDays() != null
				? teamsModule.getRecordingsDeletionDays().toString()
				: null;
		recordingsDeletionDaysEl = uifactory.addTextElement("teams.recordings.deletion.auto", "teams.recordings.deletion.auto", 5, deletionDays, formLayout);
		recordingsDeletionDaysEl.setDisplaySize(5);
		recordingsDeletionDaysEl.setElementCssClass("form-inline");
		recordingsDeletionDaysEl.setTextAddOn("teams.recordings.deletion.auto.addon");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(moduleEnabled == source || recordingsEnabledEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabled.isOn();
		enabledForEl.setVisible(enabled);
		permanentMeetingsEl.setVisible(enabled);
		recordingCont.setVisible(enabled);
		recordingsEnabledEl.setVisible(enabled);
		
		boolean recordingsEnabled = recordingsEnabledEl.isOn();
		
		recordingsDefaultEl.setVisible(enabled && recordingsEnabled);
		recordingsStartModeEl.setVisible(enabled && recordingsEnabled);
		publishRecordingsEl.setVisible(enabled && recordingsEnabled);
		recordingsSpacerEl.setVisible(enabled && recordingsEnabled);
		recordingsDeletionDaysEl.setVisible(enabled && recordingsEnabled);
		
		boolean showOldConfiguration = StringHelper.containsNonWhitespace(clientIdEl.getValue());
		spacerEl.setVisible(enabled && showOldConfiguration);
		clientIdEl.setVisible(enabled && showOldConfiguration);
		secretEl.setVisible(enabled && showOldConfiguration);
		tenantEl.setVisible(enabled && showOldConfiguration);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		recordingsDeletionDaysEl.clearError();
		if(recordingsDeletionDaysEl.isVisible()
				&& StringHelper.containsNonWhitespace(recordingsDeletionDaysEl.getValue())) {
			try {
				int v = Integer.parseInt(recordingsDeletionDaysEl.getValue());
				if(v <= 0) {
					recordingsDeletionDaysEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				recordingsDeletionDaysEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isOn();
		teamsModule.setEnabled(enabled);
		if(enabled) {
			Collection<String> selectedFor = enabledForEl.getSelectedKeys();
			teamsModule.setCoursesEnabled(selectedFor.contains(FOR_COURSES_KEY));
			teamsModule.setAppointmentsEnabled(selectedFor.contains(FOR_APPOINTMENTS_KEY));
			teamsModule.setLecturesEnabled(selectedFor.contains(FOR_LECTURES_KEY));
			teamsModule.setGroupsEnabled(selectedFor.contains(FOR_GROUPS_KEY));
			teamsModule.setChatExamsEnabled(selectedFor.contains(FOR_CHATEXAMS_KEY));
			
			boolean permanentMeetingsEnabled = permanentMeetingsEl.isOneSelected()
					&& ON_KEY.equals(permanentMeetingsEl.getSelectedKey());
			teamsModule.setPermanentMeetings(permanentMeetingsEnabled);
			showInfo("info.saved");
			
			boolean recordingsEnabled = recordingsEnabledEl.isOn();
			teamsModule.setRecordingsEnabled(recordingsEnabled);
			
			if(recordingsEnabled) {
				boolean recordingsDefaultEnabled = recordingsDefaultEl.isOneSelected()
						&& ON_KEY.equals(recordingsDefaultEl.getSelectedKey());
				teamsModule.setRecordingsDefaultEnabled(recordingsDefaultEnabled);
				boolean recordingsAutoStartEnabled = recordingsStartModeEl.isOneSelected()
						&& ON_KEY.equals(recordingsStartModeEl.getSelectedKey());
				teamsModule.setRecordingsAutoStartEnabled(recordingsAutoStartEnabled);
				teamsModule.setRecordingsDefaultPublicationSettings(toPublicationEnumSet(publishRecordingsEl.getSelectedKeys()));
				
				Integer deletionDays = StringHelper.isLong(recordingsDeletionDaysEl.getValue())
						? Integer.valueOf(recordingsDeletionDaysEl.getValue())
						: null;
				teamsModule.setRecordingsDeletionDays(deletionDays);
			}
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
	
	private Set<TeamsRecordingsPublishedRoles> toPublicationEnumSet(Collection<String> selectedKeys) {
		if (selectedKeys == null || selectedKeys.isEmpty()) {
			return Set.of();
		}
		try {
			return selectedKeys.stream().map(TeamsRecordingsPublishedRoles::valueOf).collect(Collectors.toSet());
		} catch (IllegalArgumentException e) {
			return Set.of();
		}
	}
}
