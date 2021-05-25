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
package org.olat.modules.bigbluebutton.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.JoinPolicyEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditBigBlueButtonTemplateController extends FormBasicController {

	private static final String[] yesNoKeys = new String[] { "yes", "no" };
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement rolesEl;
	private MultipleSelectionElement externalEl;
	
	private TextElement maxConcurrentMeetingsEl;
	private TextElement maxParticipantsEl;
	private TextElement maxDurationEl;
	
	private SingleSelection recordEl;
	private SingleSelection breakoutEl;
	private SingleSelection joinPolicyEl;
	private SingleSelection muteOnStartEl;
	private SingleSelection autoStartRecordingEl;
	private SingleSelection allowStartStopRecordingEl;
	private SingleSelection webcamsOnlyForModeratorEl;
	private SingleSelection allowModsToUnmuteUsersEl;
	
	private SingleSelection lockSettingsDisableCamEl;
	private SingleSelection lockSettingsDisableMicEl;
	private SingleSelection lockSettingsDisablePrivateChatEl;
	private SingleSelection lockSettingsDisablePublicChatEl;
	private SingleSelection lockSettingsDisableNoteEl;
	private SingleSelection lockSettingsLockedLayoutEl;
	
	private SingleSelection lockSettingsHideUserListEl;
	private SingleSelection lockSettingsLockOnJoinEl;
	private SingleSelection lockSettingsLockOnJoinConfigurableEl;
	
	private final boolean readOnly;
	private BigBlueButtonMeetingTemplate template;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonTemplateController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		readOnly = false;
		initForm(ureq);
		updateUI();
	}
	
	public EditBigBlueButtonTemplateController(UserRequest ureq, WindowControl wControl,
			BigBlueButtonMeetingTemplate template, boolean readOnly) {
		super(ureq, wControl);
		this.readOnly = readOnly;
		this.template = template;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = template == null ? "" : template.getName();
		nameEl = uifactory.addTextElement("template.name", "template.name", 128, name, formLayout);
		nameEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		String description = template == null ? "" : template.getDescription();
		descriptionEl = uifactory.addTextAreaElement("template.description", "template.description", 2000, 4, 72, false, false, description, formLayout);
		
		String maxParticipants = template == null || template.getMaxParticipants() == null ? null : template.getMaxParticipants().toString();
		maxParticipantsEl = uifactory.addTextElement("template.maxParticipants", "template.maxParticipants", 8, maxParticipants, formLayout);
		maxParticipantsEl.setMandatory(true);
		
		String maxDuration = null;
		if(template == null) {
			maxDuration = "240";
		} else if(template.getMaxDuration() != null) {
			maxDuration = template.getMaxDuration().toString();
		}
		maxDurationEl = uifactory.addTextElement("template.maxDuration", "template.maxDuration", 8, maxDuration, formLayout);

		boolean enable = template == null || template.isEnabled();
		enableEl = uifactory.addCheckboxesHorizontal("template.enabled", "template.enabled", formLayout, onKeys, new String[] { "" });
		enableEl.select(onKeys[0], enable);
		
		/* ----------- */
		uifactory.addSpacerElement("sp1", formLayout, false);

		String maxConcurrentMeetings = template == null || template.getMaxConcurrentMeetings() == null ? "" : template.getMaxConcurrentMeetings().toString();
		maxConcurrentMeetingsEl = uifactory.addTextElement("template.max.concurrent.meetings", "template.max.concurrent.meetings", 8, maxConcurrentMeetings, formLayout);
		
		boolean external = template != null && template.isExternalUsersAllowed();
		externalEl = uifactory.addCheckboxesHorizontal("template.external.enabled", "template.external.enabled", formLayout, onKeys, new String[] { "" });
		externalEl.select(onKeys[0], external);
		
		KeyValues joinKeyValues = new KeyValues();
		joinKeyValues.add(KeyValues.entry(JoinPolicyEnum.disabled.name(), translate("join.users.control.disabled")));
		joinKeyValues.add(KeyValues.entry(JoinPolicyEnum.guestsApproval.name(), translate("join.users.control.guests")));
		joinKeyValues.add(KeyValues.entry(JoinPolicyEnum.allUsersApproval.name(), translate("join.users.control.users")));
		joinPolicyEl = uifactory.addDropdownSingleselect("template.join.policy", "template.join.policy", formLayout,
				joinKeyValues.keys(), joinKeyValues.values());
		if(template != null && template.getJoinPolicyEnum() != null) {
			joinPolicyEl.select(template.getJoinPolicyEnum().name(), true);
		} else {
			joinPolicyEl.select(JoinPolicyEnum.disabled.name(), true);
		}
		
		KeyValues rolesKeyValues = new KeyValues();
		for(BigBlueButtonTemplatePermissions role:BigBlueButtonTemplatePermissions.values()) {
			rolesKeyValues.add(KeyValues.entry(role.name(), translate("role.".concat(role.name()))));
		}
		rolesEl = uifactory.addCheckboxesVertical("template.roles", "template.roles", formLayout,
				rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		List<BigBlueButtonTemplatePermissions> roles;
		if(template != null) {
			roles = template.getPermissions();
		} else {
			roles = BigBlueButtonTemplatePermissions.valuesAsList();
		}
		for(BigBlueButtonTemplatePermissions role:roles) {
			rolesEl.select(role.name(), true);
		}

		/* ----------- */
		uifactory.addSpacerElement("sp2", formLayout, false);

		String[] onValues = new String[] { translate("yes"), translate("no")  };
		
		Boolean webcamsOnlyForModerator = template == null ? null : template.getWebcamsOnlyForModerator();
		webcamsOnlyForModeratorEl = uifactory.addRadiosHorizontal("template.webcamsOnlyForModerator", formLayout, yesNoKeys, onValues);
		select(webcamsOnlyForModerator, webcamsOnlyForModeratorEl, true);
		
		Boolean muteOnStart = template == null ? null : template.getMuteOnStart();
		muteOnStartEl = uifactory.addRadiosHorizontal("template.muteOnStart", formLayout, yesNoKeys, onValues);
		select(muteOnStart, muteOnStartEl, false);
		
		Boolean allowModsToUnmuteUsers = template == null ? null : template.getAllowModsToUnmuteUsers();
		allowModsToUnmuteUsersEl = uifactory.addRadiosHorizontal("template.allowModsToUnmuteUsers", formLayout, yesNoKeys, onValues);
		select(allowModsToUnmuteUsers, allowModsToUnmuteUsersEl, false);
		
		Boolean record = template == null ? null : template.getRecord();
		recordEl = uifactory.addRadiosHorizontal("template.record", formLayout, yesNoKeys, onValues);
		recordEl.addActionListener(FormEvent.ONCHANGE);
		select(record, recordEl, false);
		
		Boolean autoStartRecording = template == null ? null : template.getAutoStartRecording();
		autoStartRecordingEl = uifactory.addRadiosHorizontal("template.autoStartRecording", formLayout, yesNoKeys, onValues);
		select(autoStartRecording, autoStartRecordingEl, false);
		
		Boolean allowStartStopRecording = template == null ? null : template.getAllowStartStopRecording();
		allowStartStopRecordingEl = uifactory.addRadiosHorizontal("template.allowStartStopRecording", formLayout, yesNoKeys, onValues);
		select(allowStartStopRecording, allowStartStopRecordingEl, true);
		
		Boolean breakout = template == null ? null : template.getBreakoutRoomsEnabled();
		breakoutEl = uifactory.addRadiosHorizontal("template.breakout", formLayout, yesNoKeys, onValues);
		breakoutEl.addActionListener(FormEvent.ONCHANGE);
		select(breakout, breakoutEl, true);
		
		Boolean lockSettingsLockOnJoin = template == null ? null : template.getLockSettingsLockOnJoin();
		lockSettingsLockOnJoinEl = uifactory.addRadiosHorizontal("template.lockSettingsLockOnJoin", formLayout, yesNoKeys, onValues);
		lockSettingsLockOnJoinEl.addActionListener(FormEvent.ONCHANGE);
		select(lockSettingsLockOnJoin, lockSettingsLockOnJoinEl, true);
		
		Boolean lockSettingsLockOnJoinConfigurable = template == null ? null : template.getLockSettingsLockOnJoinConfigurable();
		lockSettingsLockOnJoinConfigurableEl = uifactory.addRadiosHorizontal("template.lockSettingsLockOnJoinConfigurable", formLayout, yesNoKeys, onValues);
		select(lockSettingsLockOnJoinConfigurable, lockSettingsLockOnJoinConfigurableEl, false);
			
		/* ----------- */
		uifactory.addSpacerElement("sp2", formLayout, false);
		uifactory.addStaticTextElement("template.lock", "template.lock", null, formLayout);		
		
		Boolean lockSettingsDisableCam = template == null ? null : template.getLockSettingsDisableCam();
		lockSettingsDisableCamEl = uifactory.addRadiosHorizontal("template.lockSettingsDisableCam", formLayout, yesNoKeys, onValues);
		select(lockSettingsDisableCam, lockSettingsDisableCamEl, false);
		
		Boolean lockSettingsDisableMic = template == null ? null : template.getLockSettingsDisableMic();
		lockSettingsDisableMicEl = uifactory.addRadiosHorizontal("template.lockSettingsDisableMic", formLayout, yesNoKeys, onValues);
		select(lockSettingsDisableMic, lockSettingsDisableMicEl, false);
		
		Boolean lockSettingsDisablePublicChat = template == null ? null : template.getLockSettingsDisablePublicChat();
		lockSettingsDisablePublicChatEl =  uifactory.addRadiosHorizontal("template.lockSettingsDisablePublicChat", formLayout, yesNoKeys, onValues);
		select(lockSettingsDisablePublicChat, lockSettingsDisablePublicChatEl, false);
		
		Boolean lockSettingsDisablePrivateChat = template == null ? null : template.getLockSettingsDisablePrivateChat();
		lockSettingsDisablePrivateChatEl =  uifactory.addRadiosHorizontal("template.lockSettingsDisablePrivateChat", formLayout, yesNoKeys, onValues);
		select(lockSettingsDisablePrivateChat, lockSettingsDisablePrivateChatEl, false);
		
		Boolean lockSettingsDisableNote = template == null ? null : template.getLockSettingsDisableNote();
		lockSettingsDisableNoteEl =  uifactory.addRadiosHorizontal("template.lockSettingsDisableNote", formLayout, yesNoKeys, onValues);
		select(lockSettingsDisableNote, lockSettingsDisableNoteEl, false);
		
		Boolean lockSettingsHideUserList = template == null ? null : template.getLockSettingsHideUserList();
		lockSettingsHideUserListEl = uifactory.addRadiosHorizontal("template.lockSettingsHideUserList", formLayout, yesNoKeys, onValues);
		select(lockSettingsHideUserList, lockSettingsHideUserListEl, false);
		
		Boolean lockSettingsLockedLayout = template == null ? null : template.getLockSettingsLockedLayout();
		lockSettingsLockedLayoutEl = uifactory.addRadiosHorizontal("template.lockSettingsLockedLayout", formLayout, yesNoKeys, onValues);
		select(lockSettingsLockedLayout, lockSettingsLockedLayoutEl, false);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		if(readOnly) {
			disableSystemTemplate();
		} else {
			uifactory.addFormSubmitButton("save", buttonLayout);
		}
	}
	
	private void updateUI() {
		boolean record = recordEl.isOneSelected() && recordEl.isSelected(0);
		autoStartRecordingEl.setVisible(record);
		allowStartStopRecordingEl.setVisible(record);
		
		boolean lockOnJoin = lockSettingsLockOnJoinEl.isOneSelected() && lockSettingsLockOnJoinEl.isSelected(0);
		lockSettingsLockOnJoinConfigurableEl.setVisible(lockOnJoin);	
	}
	
	private void disableSystemTemplate() {
		nameEl.setEnabled(false);
		descriptionEl.setEnabled(false);
		enableEl.setEnabled(false);
		recordEl.setEnabled(false);
		muteOnStartEl.setEnabled(false);
		maxConcurrentMeetingsEl.setEnabled(false);
		maxParticipantsEl.setEnabled(false);
		maxDurationEl.setEnabled(false);
		autoStartRecordingEl.setEnabled(false);
		allowStartStopRecordingEl.setEnabled(false);
		webcamsOnlyForModeratorEl.setEnabled(false);
		allowModsToUnmuteUsersEl.setEnabled(false);
		lockSettingsDisableCamEl.setEnabled(false);
		lockSettingsDisableMicEl.setEnabled(false);
		lockSettingsDisablePrivateChatEl.setEnabled(false);
		lockSettingsDisablePublicChatEl.setEnabled(false);
		lockSettingsDisableNoteEl.setEnabled(false);
		lockSettingsLockedLayoutEl.setEnabled(false);
		lockSettingsHideUserListEl.setEnabled(false);
		lockSettingsLockOnJoinEl.setEnabled(false);
		lockSettingsLockOnJoinConfigurableEl.setEnabled(false);
	}
	
	private void select(Boolean val, SingleSelection selectEl, boolean defaultValue) {
		if(val == null) {
			val = Boolean.valueOf(defaultValue);
		}
		if(val.booleanValue()) {
			selectEl.select(yesNoKeys[0], true);
		} else {
			selectEl.select(yesNoKeys[1], true);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(nameEl.getValue().length() > 128) {
			nameEl.setErrorKey("form.error.toolong", new String[] { "128" });
			allOk &= false;
		}
		
		descriptionEl.clearError();
		if(!StringHelper.containsNonWhitespace(descriptionEl.getValue()) && descriptionEl.getValue().length() > 2000) {
			descriptionEl.setErrorKey("form.error.toolong", new String[] { "2000" });
			allOk &= false;
		}

		allOk &= validateLong(maxConcurrentMeetingsEl, false);
		allOk &= validateLong(maxParticipantsEl, true);
		allOk &= validateLong(maxDurationEl, false);
				
		return allOk;
	}
	
	private boolean validateLong(TextElement el, boolean mandatory) {
		boolean allOk = true;
		
		el.clearError();
		if(mandatory && !StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(el.getValue())
				&& (!StringHelper.isLong(el.getValue())
						|| Long.parseLong(el.getValue()) < 1)) {
			el.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(recordEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(template == null) {
			template = bigBlueButtonManager.createAndPersistTemplate(nameEl.getValue());
		} else {
			template.setName(nameEl.getValue());
		}
		template.setDescription(descriptionEl.getValue());
		template.setEnabled(enableEl.isAtLeastSelected(1));
		template.setExternalUsersAllowed(externalEl.isAtLeastSelected(1));
		
		List<BigBlueButtonTemplatePermissions> roles = rolesEl.getSelectedKeys().stream()
				.map(BigBlueButtonTemplatePermissions::valueOf).collect(Collectors.toList());
		template.setPermissions(roles);
		
		if(StringHelper.containsNonWhitespace(maxConcurrentMeetingsEl.getValue())
				&& StringHelper.isLong(maxConcurrentMeetingsEl.getValue())) {
			template.setMaxConcurrentMeetings(Long.valueOf(maxConcurrentMeetingsEl.getValue()).intValue());
		} else {
			template.setMaxConcurrentMeetings(null);
		}
		
		if(StringHelper.containsNonWhitespace(maxParticipantsEl.getValue())
				&& StringHelper.isLong(maxParticipantsEl.getValue())) {
			template.setMaxParticipants(Long.valueOf(maxParticipantsEl.getValue()).intValue());
		} else {
			template.setMaxParticipants(null);
		}
		
		if(StringHelper.containsNonWhitespace(maxDurationEl.getValue())
				&& StringHelper.isLong(maxDurationEl.getValue())) {
			template.setMaxDuration(Long.valueOf(maxDurationEl.getValue()).intValue());
		} else {
			template.setMaxDuration(null);
		}
		
		JoinPolicyEnum joinPolicy = JoinPolicyEnum.valueOf(joinPolicyEl.getSelectedKey());
		template.setJoinPolicyEnum(joinPolicy);

		boolean record = getSelected(recordEl);
		template.setRecord(record);
		template.setBreakoutRoomsEnabled(getSelected(breakoutEl));
		template.setMuteOnStart(getSelected(muteOnStartEl));
		template.setAutoStartRecording(record && getSelected(autoStartRecordingEl));
		template.setAllowStartStopRecording(record && getSelected(allowStartStopRecordingEl));
		template.setWebcamsOnlyForModerator(getSelected(webcamsOnlyForModeratorEl));
		template.setAllowModsToUnmuteUsers(getSelected(allowModsToUnmuteUsersEl));
		
		template.setLockSettingsDisableCam(getSelected(lockSettingsDisableCamEl));
		template.setLockSettingsDisableMic(getSelected(lockSettingsDisableMicEl));
		template.setLockSettingsDisablePrivateChat(getSelected(lockSettingsDisablePrivateChatEl));
		template.setLockSettingsDisablePublicChat(getSelected(lockSettingsDisablePublicChatEl));
		template.setLockSettingsDisableNote(getSelected(lockSettingsDisableNoteEl));
		template.setLockSettingsLockedLayout(getSelected(lockSettingsLockedLayoutEl));
		
		template.setLockSettingsHideUserList(getSelected(lockSettingsHideUserListEl));
		template.setLockSettingsLockOnJoin(getSelected(lockSettingsLockOnJoinEl));
		template.setLockSettingsLockOnJoinConfigurable(getSelected(lockSettingsLockOnJoinConfigurableEl));
		
		template = bigBlueButtonManager.updateTemplate(template);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);

		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
	
	private Boolean getSelected(SingleSelection selectEl) {
		Boolean val = Boolean.FALSE;
		if(selectEl.isOneSelected()) {
			String selected = selectEl.getSelectedKey();
			val = Boolean.valueOf("yes".equals(selected));
		}
		return val;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
