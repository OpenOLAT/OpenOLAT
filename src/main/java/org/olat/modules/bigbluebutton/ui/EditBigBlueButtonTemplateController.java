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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.GuestPolicyEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditBigBlueButtonTemplateController extends FormBasicController {

	private static final String[] maxParticipantsKeys = new String[] { "2", "5", "10", "25", "50", "100" };
	private static final String[] onKeys = new String[] { "yes", "no", "default" };
	private static final String[] guestPolicyKeys = new String[] {
			GuestPolicyEnum.ALWAYS_DENY.name(), GuestPolicyEnum.ASK_MODERATOR.name(), GuestPolicyEnum.ALWAYS_ACCEPT.name()
		};
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	private SingleSelection muteOnStartEl;
	private SingleSelection maxParticipantsEl;
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

	private SingleSelection guestPolicyEl;
	
	private BigBlueButtonMeetingTemplate template;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonTemplateController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
	
	public EditBigBlueButtonTemplateController(UserRequest ureq, WindowControl wControl, BigBlueButtonMeetingTemplate template) {
		super(ureq, wControl);
		this.template = template;

		initForm(ureq);
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
		
		String maxParticipants = template == null || template.getMaxParticipants() == null ? "-" : template.getMaxParticipants().toString();
		KeyValues maxParticipantsKeyValues = new KeyValues();
		maxParticipantsKeyValues.add(KeyValues.entry("-", translate("template.maxParticipants.default")));
		for(String maxParticipantsKey:maxParticipantsKeys) {
			maxParticipantsKeyValues.add(KeyValues.entry(maxParticipantsKey, maxParticipantsKey));
		}
		if(!maxParticipantsKeyValues.containsKey(maxParticipants)) {
			maxParticipantsKeyValues.add(KeyValues.entry(maxParticipants, maxParticipants));
		}
		maxParticipantsEl = uifactory.addDropdownSingleselect("template.maxParticipants", formLayout,
				maxParticipantsKeyValues.keys(), maxParticipantsKeyValues.values());
		maxParticipantsEl.select(maxParticipants, true);
		
		String[] onValues = new String[] { "Yes", "No", "Default" };
		
		Boolean muteOnStart = template == null ? null : template.getMuteOnStart();
		muteOnStartEl = uifactory.addRadiosHorizontal("template.muteOnStart", formLayout, onKeys, onValues);
		select(muteOnStart, muteOnStartEl);
		
		Boolean autoStartRecording = template == null ? null : template.getAutoStartRecording();
		autoStartRecordingEl = uifactory.addRadiosHorizontal("template.autoStartRecording", formLayout, onKeys, onValues);
		select(autoStartRecording, autoStartRecordingEl);
		
		Boolean allowStartStopRecording = template == null ? null : template.getAllowStartStopRecording();
		allowStartStopRecordingEl = uifactory.addRadiosHorizontal("template.allowStartStopRecording", formLayout, onKeys, onValues);
		select(allowStartStopRecording, allowStartStopRecordingEl);
		
		Boolean webcamsOnlyForModerator = template == null ? null : template.getWebcamsOnlyForModerator();
		webcamsOnlyForModeratorEl = uifactory.addRadiosHorizontal("template.webcamsOnlyForModerator", formLayout, onKeys, onValues);
		select(webcamsOnlyForModerator, webcamsOnlyForModeratorEl);
		
		Boolean allowModsToUnmuteUsers = template == null ? null : template.getAllowModsToUnmuteUsers();
		allowModsToUnmuteUsersEl = uifactory.addRadiosHorizontal("template.allowModsToUnmuteUsers", formLayout, onKeys, onValues);
		select(allowModsToUnmuteUsers, allowModsToUnmuteUsersEl);
		
		Boolean lockSettingsDisableCam = template == null ? null : template.getLockSettingsDisableCam();
		lockSettingsDisableCamEl = uifactory.addRadiosHorizontal("template.lockSettingsDisableCam", formLayout, onKeys, onValues);
		select(lockSettingsDisableCam, lockSettingsDisableCamEl);
		
		Boolean lockSettingsDisableMic = template == null ? null : template.getLockSettingsDisableMic();
		lockSettingsDisableMicEl = uifactory.addRadiosHorizontal("template.lockSettingsDisableMic", formLayout, onKeys, onValues);
		select(lockSettingsDisableMic, lockSettingsDisableMicEl);
		
		Boolean lockSettingsDisablePrivateChat = template == null ? null : template.getLockSettingsDisablePrivateChat();
		lockSettingsDisablePrivateChatEl =  uifactory.addRadiosHorizontal("template.lockSettingsDisablePrivateChat", formLayout, onKeys, onValues);
		select(lockSettingsDisablePrivateChat, lockSettingsDisablePrivateChatEl);
		
		Boolean lockSettingsDisablePublicChat = template == null ? null : template.getLockSettingsDisablePublicChat();
		lockSettingsDisablePublicChatEl =  uifactory.addRadiosHorizontal("template.lockSettingsDisablePublicChat", formLayout, onKeys, onValues);
		select(lockSettingsDisablePublicChat, lockSettingsDisablePublicChatEl);
		
		Boolean lockSettingsDisableNote = template == null ? null : template.getLockSettingsDisableNote();
		lockSettingsDisableNoteEl =  uifactory.addRadiosHorizontal("template.lockSettingsDisableNote", formLayout, onKeys, onValues);
		select(lockSettingsDisableNote, lockSettingsDisableNoteEl);
		
		Boolean lockSettingsLockedLayout = template == null ? null : template.getLockSettingsLockedLayout();
		lockSettingsLockedLayoutEl = uifactory.addRadiosHorizontal("template.lockSettingsLockedLayout", formLayout, onKeys, onValues);
		select(lockSettingsLockedLayout, lockSettingsLockedLayoutEl);

		GuestPolicyEnum guestPolicy = template == null ? GuestPolicyEnum.ALWAYS_DENY : template.getGuestPolicyEnum();
		String[] guestPolicyValues = new String[] {
				translate("guest.policy.always.deny"), translate("guest.policy.ask.moderator"),
				translate("guest.policy.always.accept")
			};
		guestPolicyEl = uifactory.addRadiosHorizontal("template.guestPolicy", formLayout, guestPolicyKeys, guestPolicyValues);
		guestPolicyEl.select(guestPolicy.name(), true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		if(template == null ||  !template.isSystem()) {
			uifactory.addFormSubmitButton("save", buttonLayout);
		} else {
			disableSystemTemplate();
		}
	}
	
	private void disableSystemTemplate() {
		nameEl.setEnabled(false);
		descriptionEl.setEnabled(false);
		muteOnStartEl.setEnabled(false);
		maxParticipantsEl.setEnabled(false);
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
		guestPolicyEl.setEnabled(false);
	}
	
	private void select(Boolean val, SingleSelection selectEl) {
		if(val == null) {
			selectEl.select(onKeys[2], true);
		} else if(val.booleanValue()) {
			selectEl.select(onKeys[0], true);
		} else {
			selectEl.select(onKeys[1], true);
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
			nameEl.setErrorKey("form.error.toolong", new String[] { "2000" });
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(template == null) {
			template = bigBlueButtonManager.createAndPersistTemplate(nameEl.getValue());
		} else {
			template.setName(nameEl.getValue());
		}
		template.setDescription(descriptionEl.getValue());
		
		if(maxParticipantsEl.isOneSelected() && !"-".equals(maxParticipantsEl.getSelectedKey())) {
			template.setMaxParticipants(Integer.parseInt(maxParticipantsEl.getSelectedKey()));
		} else {
			template.setMaxParticipants(null);
		}
		
		template.setMuteOnStart(getSelected(muteOnStartEl));
		template.setAutoStartRecording(getSelected(autoStartRecordingEl));
		template.setAllowStartStopRecording(getSelected(allowStartStopRecordingEl));
		template.setWebcamsOnlyForModerator(getSelected(webcamsOnlyForModeratorEl));
		template.setAllowModsToUnmuteUsers(getSelected(allowModsToUnmuteUsersEl));
		template.setLockSettingsDisableCam(getSelected(lockSettingsDisableCamEl));
		template.setLockSettingsDisableMic(getSelected(lockSettingsDisableMicEl));
		template.setLockSettingsDisablePrivateChat(getSelected(lockSettingsDisablePrivateChatEl));
		template.setLockSettingsDisablePublicChat(getSelected(lockSettingsDisablePublicChatEl));
		template.setLockSettingsDisableNote(getSelected(lockSettingsDisableNoteEl));
		template.setLockSettingsLockedLayout(getSelected(lockSettingsLockedLayoutEl));
		
		if(guestPolicyEl.isOneSelected()) {
			template.setGuestPolicyEnum(GuestPolicyEnum.valueOf(guestPolicyEl.getSelectedKey()));
		}
		template = bigBlueButtonManager.updateTemplate(template);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private Boolean getSelected(SingleSelection selectEl) {
		Boolean val = null;
		if(selectEl.isOneSelected()) {
			String selected = selectEl.getSelectedKey();
			if("yes".equals(selected)) {
				val = Boolean.TRUE;
			} else if("no".equals(selected)) {
				val = Boolean.FALSE;
			}
		}
		return val;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
