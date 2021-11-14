/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.ui.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.ArrayHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author patrick
 */
public class BusinessGroupSendToChooserForm extends FormBasicController {

	private BusinessGroup businessGroup;
	// Owners
	private MultipleSelectionElement multiSelectionOwnerKeys;
	private SingleSelection radioButtonOwner;
	private String[] radioKeysOwners,radioValuesOwners;
	private String[] keysOwner;
	private String[] valuesOwner;
	
	// Participants 
	private MultipleSelectionElement multiSelectionPartipKeys;
	private SingleSelection radioButtonPartips;
	private String[] radioKeysPartips,radioValuesPartips;
	private String[] keysPartips; 
	private String[] valuesPartips;
		
  // Waiting 
	private MultipleSelectionElement multiSelectionWaitingKeys;
	private SingleSelection radioButtonWaitings;
	private String[] radioKeysWaitings,radioValuesWaitings;
	private String[] keysWaitings; 
	private String[] valuesWaitings;
	
	private FormItem errorKeyDisplay; // using this for common errorKey output
	
	private final boolean showChooseOwners;
	private final boolean showChoosePartips;
	private final boolean showWaitingList;
	
	private final boolean isAdmin;
	private final boolean isAdministrativeUser;
	
	public static final String NLS_RADIO_ALL = "all";
	public static final String NLS_RADIO_NOTHING = "nothing";
	public static final String NLS_RADIO_CHOOSE = "choose";
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	/**
	 * @param name
	 * @param translator
	 */
	public BusinessGroupSendToChooserForm(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup, boolean isAdmin) {
		super(ureq, wControl);

		this.businessGroup = businessGroup;
		this.isAdmin = isAdmin;
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());

		// check 'members can see owners' and 'members can see participants' 
		showChooseOwners  = businessGroup.isOwnersVisibleIntern();
		showChoosePartips = businessGroup.isParticipantsVisibleIntern();
		showWaitingList = isAdmin && businessGroup.getWaitingListEnabled().booleanValue();
		
		if (isMultiSelectionOwnerKeys())  {
			
			radioKeysOwners = new String[] {
					NLS_RADIO_ALL,
					NLS_RADIO_NOTHING,
					NLS_RADIO_CHOOSE
			};
			
			radioValuesOwners = new String[] { 
					translate("sendtochooser.form.radio.owners.all"),
					translate("sendtochooser.form.radio.owners.nothing"),
					translate("sendtochooser.form.radio.owners.choose")
			};

			// Owner MultiSelection
			List<Identity> owners = businessGroupService.getMembers(businessGroup, GroupRoles.coach.name());
			keysOwner = getMemberKeys(owners);
			valuesOwner = getMemberValues(owners); 
			ArrayHelper.sort(keysOwner, valuesOwner, false, true, false);
		} else {

			radioKeysOwners = new String[]{
					NLS_RADIO_ALL,
					NLS_RADIO_NOTHING
			};
			
			radioValuesOwners = new String[]{
					translate("sendtochooser.form.radio.owners.all"),
					translate("sendtochooser.form.radio.owners.nothing")
			};
		}
		
		if (isMultiSelectionPartipKeys()) {
			radioKeysPartips = new String[]{
					NLS_RADIO_ALL,
					NLS_RADIO_NOTHING,
					NLS_RADIO_CHOOSE
			};
			radioValuesPartips = new String[]{
					translate("sendtochooser.form.radio.partip.all"),
					translate("sendtochooser.form.radio.partip.nothing"),
					translate("sendtochooser.form.radio.partip.choose")
			};
			
			// Participant MultiSelection
			List<Identity> participants = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
			keysPartips = getMemberKeys(participants);
			valuesPartips = getMemberValues(participants); 
			ArrayHelper.sort(keysPartips, valuesPartips, false, true, false);
		} else {
			radioKeysPartips = new String[]{ NLS_RADIO_ALL, NLS_RADIO_NOTHING };
			radioValuesPartips = new String[]{
					translate("sendtochooser.form.radio.partip.all"),
					translate("sendtochooser.form.radio.partip.nothing")
			};
		}		

		radioKeysWaitings = new String[]{
				NLS_RADIO_ALL,
				NLS_RADIO_NOTHING,
				NLS_RADIO_CHOOSE
		};
		radioValuesWaitings = new String[]{
				translate("sendtochooser.form.radio.waitings.all"),
				translate("sendtochooser.form.radio.waitings.nothing"),
				translate("sendtochooser.form.radio.waitings.choose")
		};
				
		if (showWaitingList) {
		  // Waitings MultiSelection
			List<Identity> waitingList = businessGroupService.getMembers(businessGroup, GroupRoles.waiting.name());
			keysWaitings = getMemberKeys(waitingList);
			valuesWaitings = getMemberValues(waitingList);			
			ArrayHelper.sort(keysWaitings, valuesWaitings, false, true, false);
		} else {
			keysWaitings = new String[]{};
			valuesWaitings = new String[]{};
		}
		
		initForm (ureq);
	}
	
	/**
	 * Get identities for this securityGroup and create values (labels) 
	 * with firstname + lastname + [username].
	 * @param ureq
	 * @param securityGroup
	 * @return
	 */
	private String[] getMemberValues(List<Identity> membersList) {
		List<UserPropertyHandler> propHandlers = userManager.getUserPropertyHandlersFor(this.getClass().getCanonicalName(), isAdministrativeUser);	
		String[] values = new String[membersList.size()];
		for (int i = 0; i < membersList.size(); i++) {			
			User currentUser = membersList.get(i).getUser();
			StringBuilder userInfo = new StringBuilder();
			for (UserPropertyHandler userProp : propHandlers) {
				userInfo.append(userProp.getUserProperty(currentUser, getLocale()));
				userInfo.append(" ");
			}
			values[i] = userInfo.toString();	
		}
		return values;
	}
	
	/**
	 * Get identities for this securityGroup and return the keys as array.
	 * @param ureq
	 * @param securityGroup
	 * @return
	 */
	private String[] getMemberKeys(List<Identity> membersList ) {
		String[] keys = new String[membersList.size()];
		for (int i = 0; i < membersList.size(); i++) {
			keys[i] = membersList.get(i).getKey().toString();			
		}
		return keys;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		
		errorKeyDisplay.clearError();
		if (multiSelectionOwnerKeys != null) {
			multiSelectionOwnerKeys.clearError();
			if (multiSelectionOwnerKeys.isVisible() && multiSelectionOwnerKeys.getSelectedKeys().isEmpty()) {
				multiSelectionOwnerKeys.setErrorKey("sendtochooser.form.error.norecipent", null);
				return false;
			}
		}
		if (multiSelectionPartipKeys != null) {
			multiSelectionPartipKeys.clearError();
			if (multiSelectionPartipKeys.isVisible() && multiSelectionPartipKeys.getSelectedKeys().isEmpty()) {
				multiSelectionPartipKeys.setErrorKey("sendtochooser.form.error.norecipent", null);
				return false;
			}
		}
		if (multiSelectionWaitingKeys != null) {
			multiSelectionWaitingKeys.clearError();
			if (multiSelectionWaitingKeys.isVisible() && multiSelectionWaitingKeys.getSelectedKeys().isEmpty()) {
				multiSelectionWaitingKeys.setErrorKey("sendtochooser.form.error.norecipent", null);
				return false;
			}
		}
		
		
		if (businessGroup.getWaitingListEnabled().booleanValue()) {
			// there is a waiting list checkbox
			if (radioButtonOwner.isSelected(0)
					|| radioButtonPartips.isSelected(0)
					|| radioButtonWaitings.isSelected(0)
					|| (isMultiSelectionOwnerKeys() ? (radioButtonOwner.isSelected(2) && (multiSelectionOwnerKeys != null) && multiSelectionOwnerKeys.getSelectedKeys().size() > 0 ? true : false) : false)
					|| (isMultiSelectionPartipKeys() ? (radioButtonPartips.isSelected(2) && (multiSelectionPartipKeys != null) && multiSelectionPartipKeys.getSelectedKeys().size() > 0 ? true : false) : false)
					|| (radioButtonWaitings.isSelected(2) && (multiSelectionWaitingKeys != null) && multiSelectionWaitingKeys.getSelectedKeys().size() > 0 ? true : false)) {
				return true;
			} else {
				errorKeyDisplay.setErrorKey("sendtochooser.form.error.nonselected", null);
				return false;
			}
		} else {
			if (radioButtonOwner.isSelected(0)
					|| radioButtonPartips.isSelected(0)
					|| (isMultiSelectionOwnerKeys() ? (radioButtonOwner.isSelected(2) && (multiSelectionOwnerKeys != null) && multiSelectionOwnerKeys.getSelectedKeys().size() > 0 ? true
							: false)
							: false)
					|| (isMultiSelectionPartipKeys() ? (radioButtonPartips.isSelected(2) && (multiSelectionPartipKeys != null) && multiSelectionPartipKeys.getSelectedKeys().size() > 0 ? true
							: false)
							: false)) {
				return true;
			} else {
				errorKeyDisplay.setErrorKey("sendtochooser.form.error.nonselected", null);
				return false;
			}
		}
	}

	/**
	 * @return true if owner is checked
	 */
	public String ownerChecked() {
		return radioButtonOwner == null?BusinessGroupSendToChooserForm.NLS_RADIO_NOTHING:radioButtonOwner.getSelectedKey();
	}

	/**
	 * @return true if participant is checked
	 */
	public String participantChecked() {
		return radioButtonPartips.getSelectedKey();
	}

	/**
	 * @return true if waiting-list is checked
	 */
	public String waitingListChecked() {
		return radioButtonWaitings.getSelectedKey();
	}
	
	/**
	 * @return
	 */
	private boolean isMultiSelectionOwnerKeys() {
		return isAdmin || showChooseOwners;
	}
	
	/**
	 * @return
	 */
	private boolean isMultiSelectionPartipKeys() {
		return isAdmin || showChoosePartips;
	}

	/**
	 * @return
	 */
	public List<Long> getSelectedOwnerKeys() {
		return getSelectedKeyOf(multiSelectionOwnerKeys);
	}
	
	/**
	 * @return
	 */
	public List<Long> getSelectedPartipKeys() {
		return getSelectedKeyOf(multiSelectionPartipKeys);
	}
	
	/**
	 * @return
	 */
	public List<Long> getSelectedWaitingKeys() {
		return getSelectedKeyOf(multiSelectionWaitingKeys);
	}
	
	private List<Long> getSelectedKeyOf(MultipleSelectionElement multiSelectionElements) {
		if (multiSelectionElements == null) {
			return new ArrayList<>();
		}
		Collection<String> selectedKeys = multiSelectionElements.getSelectedKeys();
		List<Long> selectedKeysLong = new ArrayList<>();
		for (String key : selectedKeys) {
			selectedKeysLong.add(Long.parseLong(key));
		}
		return selectedKeysLong;
	}
	
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sendtochooser.form.header");
		flc.setElementCssClass("o_sel_contact_form");

		radioButtonOwner = uifactory.addRadiosVertical("radioButtonOwner", "sendtochooser.form.radio.owners", formLayout, radioKeysOwners, radioValuesOwners);
		radioButtonOwner.select(NLS_RADIO_ALL, true);
		radioButtonOwner.addActionListener(FormEvent.ONCLICK);
		if ( (keysOwner != null) && (valuesOwner != null) ) {
			multiSelectionOwnerKeys = uifactory.addCheckboxesVertical("multiSelectionOwnerKeys", "", formLayout, keysOwner, valuesOwner, 1);
		}
		
		radioButtonPartips = uifactory.addRadiosVertical("radioButtonPartip", "sendtochooser.form.radio.rightgroup", formLayout, radioKeysPartips, radioValuesPartips);
		radioButtonPartips.select(NLS_RADIO_ALL, true);
		radioButtonPartips.addActionListener(FormEvent.ONCLICK);
		if ( (keysPartips != null) && (valuesPartips != null) ) {
			multiSelectionPartipKeys = uifactory.addCheckboxesVertical("multiSelectionPartipKeys", "", formLayout, keysPartips, valuesPartips, 1);
		} 
			
		radioButtonWaitings = uifactory.addRadiosVertical("radioButtonWaiting", "sendtochooser.form.radio.waitings", formLayout, radioKeysWaitings, radioValuesWaitings);
		radioButtonWaitings.select(NLS_RADIO_NOTHING, true);
		radioButtonWaitings.addActionListener(FormEvent.ONCLICK);
		if ( (keysWaitings != null) && (valuesWaitings != null) ) {
			multiSelectionWaitingKeys = uifactory.addCheckboxesVertical("multiSelectionWaitingKeys", "", formLayout, keysWaitings, valuesWaitings, 1);
		} 
		
		uifactory.addSpacerElement("space", formLayout, true);
		errorKeyDisplay = uifactory.addStaticExampleText ("", "", formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("sendtochooser.form.submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		update();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		
		if (source == radioButtonOwner && radioButtonOwner.getSelectedKey() != NLS_RADIO_NOTHING) {
			radioButtonWaitings.select(NLS_RADIO_NOTHING, true);
		}
		if (source == radioButtonPartips && radioButtonPartips.getSelectedKey() != NLS_RADIO_NOTHING) {
			radioButtonWaitings.select(NLS_RADIO_NOTHING, true);
		}
		if (source == radioButtonWaitings && radioButtonWaitings.getSelectedKey() != NLS_RADIO_NOTHING) {
			radioButtonOwner.select(NLS_RADIO_NOTHING, true);
			radioButtonPartips.select(NLS_RADIO_NOTHING, true);
		}
		
		errorKeyDisplay.clearError();

		update();
	}
	
	private void update() {
		if ( (radioKeysOwners != null) && (multiSelectionOwnerKeys != null) ) {
			List<String> k = Arrays.asList(radioKeysOwners);
			multiSelectionOwnerKeys.setVisible(
					k.contains(NLS_RADIO_CHOOSE) && 
					radioButtonOwner.isSelected(k.indexOf(NLS_RADIO_CHOOSE))
			);
		}
		
		if ( (radioKeysPartips != null) && (multiSelectionPartipKeys != null) ) {		
			List<String> k = Arrays.asList(radioKeysPartips);
			multiSelectionPartipKeys.setVisible(
					k.contains(NLS_RADIO_CHOOSE) && 
					radioButtonPartips.isSelected(k.indexOf(NLS_RADIO_CHOOSE))
			);
		}
		
		if ( (radioButtonWaitings != null) && (multiSelectionWaitingKeys != null) ) {		
			radioButtonWaitings.setVisible(showWaitingList);
			List<String> k = Arrays.asList(radioKeysWaitings);
			multiSelectionWaitingKeys.setVisible(
					showWaitingList &&
					k.contains(NLS_RADIO_CHOOSE) && 
					radioButtonWaitings.isSelected(k.indexOf(NLS_RADIO_CHOOSE))
			);
		}
	}

}