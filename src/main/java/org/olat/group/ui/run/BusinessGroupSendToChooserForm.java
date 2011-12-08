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
* <p>
*/ 

package org.olat.group.ui.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
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
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description: <BR>
 * TODO: Class Description for BusinessGroupSendToChooserForm
 * <P>
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
	
	private boolean showChooseOwners;
	private boolean showChoosePartips;
	private boolean showWaitingList;
	
	private boolean isAdmin;
	
	public final static String NLS_RADIO_ALL = "all";
	public final static String NLS_RADIO_NOTHING = "nothing";
	public final static String NLS_RADIO_CHOOSE = "choose";

	/**
	 * @param name
	 * @param translator
	 */
	public BusinessGroupSendToChooserForm(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup, boolean isAdmin) {
		super(ureq, wControl);

		this.businessGroup = businessGroup;
		this.isAdmin = isAdmin;
		
		// check 'members can see owners' and 'members can see participants' 
		BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessGroup);
		BaseSecurity scrtMngr = BaseSecurityManager.getInstance();

		showChooseOwners  = bgpm.showOwners();
		showChoosePartips = bgpm.showPartips();
		showWaitingList = isAdmin && businessGroup.getWaitingListEnabled().booleanValue();
		
		if (isRightGroup() || isMultiSelectionOwnerKeys())  {
			
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
			if (!isRightGroup()) {
				SecurityGroup owners = businessGroup.getOwnerGroup();			
				keysOwner = getMemberKeys(ureq, owners);
				valuesOwner = getMemberValues(ureq, owners); 
				ArrayHelper.sort(keysOwner, valuesOwner, false, true, false);
			}
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
			if (isRightGroup()) {
				radioKeysPartips = new String[]{
						NLS_RADIO_ALL,
						NLS_RADIO_CHOOSE
				};
				radioValuesPartips = new String[]{
						translate("sendtochooser.form.radio.partip.rightgroup.all"),
						translate("sendtochooser.form.radio.partip.rightgroup.choose")
				};
				
				// Participant MultiSelection
				SecurityGroup participants = businessGroup.getPartipiciantGroup();
				keysPartips = getMemberKeys(ureq, participants);
				valuesPartips = getMemberValues(ureq, participants); 
								
			} else {
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
				SecurityGroup participants = businessGroup.getPartipiciantGroup();
				keysPartips = getMemberKeys(ureq, participants);
				valuesPartips = getMemberValues(ureq, participants); 
				ArrayHelper.sort(keysPartips, valuesPartips, false, true, false);
			}
		} else {
			if (isRightGroup()) {
				radioKeysPartips = new String[]{
						NLS_RADIO_ALL
				};
				radioValuesPartips = new String[]{
						translate("sendtochooser.form.radio.partip.all.rightgroup")
				};
			} else {
				radioKeysPartips = new String[]{
						NLS_RADIO_ALL,
						NLS_RADIO_NOTHING
				};
				radioValuesPartips = new String[]{
						translate("sendtochooser.form.radio.partip.all"),
						translate("sendtochooser.form.radio.partip.nothing")
				};
			}
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
			SecurityGroup waitingList = businessGroup.getWaitingGroup();
			keysWaitings = this.getMemberKeys(ureq, waitingList);
			valuesWaitings = this.getMemberValues(ureq, waitingList);			
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
	 * fxdiff: FXOLAT-227 get properties and order by user-properties-context.
	 */
	private String[] getMemberValues(UserRequest ureq, SecurityGroup securityGroup) {
		String[] values = new String[0];		
		List<UserPropertyHandler> propHandlers = UserManager.getInstance().getUserPropertyHandlersFor(this.getClass().getCanonicalName(), ureq.getUserSession().getRoles().isOLATAdmin());
		List<Identity> membersList = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(securityGroup);		
		values = new String[membersList.size()];
		for (int i = 0; i < membersList.size(); i++) {			
			User currentUser = membersList.get(i).getUser();
			StringBuffer userInfo = new StringBuffer();
			for (UserPropertyHandler userProp : propHandlers) {
				userInfo.append(userProp.getUserProperty(currentUser, getLocale()));
				userInfo.append(" ");
			}
			userInfo.append("[").append(membersList.get(i).getName().toString()).append("]");
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
	private String[] getMemberKeys(UserRequest ureq, SecurityGroup securityGroup) {
		String[] keys = new String[0];		
		List<Identity> membersList = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(securityGroup);		
		keys = new String[membersList.size()];
		for (int i = 0; i < membersList.size(); i++) {
			keys[i] = membersList.get(i).getKey().toString();			
		}
		return keys;
	}
		
	
	/**
	 * @param businessGroup
	 * @return
	 */
	private boolean isRightGroup() {
		return this.businessGroup.getType().equals(businessGroup.TYPE_RIGHTGROUP);
	}

	protected boolean validateFormLogic(UserRequest ureq) {
		
		errorKeyDisplay.clearError();
		if (multiSelectionOwnerKeys != null) {
			multiSelectionOwnerKeys.clearError();
			if (multiSelectionOwnerKeys.isVisible() && multiSelectionOwnerKeys.getSelectedKeys().size() == 0) {
				multiSelectionOwnerKeys.setErrorKey("sendtochooser.form.error.norecipent", null);
				return false;
			}
		}
		if (multiSelectionPartipKeys != null) {
			multiSelectionPartipKeys.clearError();
			if (multiSelectionPartipKeys.isVisible() && multiSelectionPartipKeys.getSelectedKeys().size() == 0) {
				multiSelectionPartipKeys.setErrorKey("sendtochooser.form.error.norecipent", null);
				return false;
			}
		}
		if (multiSelectionWaitingKeys != null) {
			multiSelectionWaitingKeys.clearError();
			if (multiSelectionWaitingKeys.isVisible() && multiSelectionWaitingKeys.getSelectedKeys().size() == 0) {
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
			if (isRightGroup()) {
				if (radioButtonPartips.isSelected(0)
						|| (isMultiSelectionPartipKeys() ? (radioButtonPartips.isSelected(1) && (multiSelectionPartipKeys != null) && multiSelectionPartipKeys.getSelectedKeys().size() > 0 ? true
								: false)
								: false)) {
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
		return isAdmin ? true : showChooseOwners;
	}
	
	/**
	 * @return
	 */
	private boolean isMultiSelectionPartipKeys() {
		return isAdmin ? true : showChoosePartips;
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
			return new ArrayList<Long>();
		}
		Set selectedKeys = multiSelectionElements.getSelectedKeys();
		List<Long> selectedKeysLong = new ArrayList<Long>();
		for (Object key : selectedKeys) {
			selectedKeysLong.add(Long.parseLong(key.toString()));
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
		if (!isRightGroup()) {
			radioButtonOwner = uifactory.addRadiosVertical("radioButtonOwner", "sendtochooser.form.radio.owners", formLayout, radioKeysOwners, radioValuesOwners);
			radioButtonOwner.select(NLS_RADIO_ALL, true);
			radioButtonOwner.addActionListener(listener, FormEvent.ONCLICK);
			if ( (keysOwner != null) && (valuesOwner != null) ) {
				multiSelectionOwnerKeys = uifactory.addCheckboxesVertical("multiSelectionOwnerKeys", "", formLayout, keysOwner, valuesOwner, null, 1);
			}
		}
		
		radioButtonPartips = uifactory.addRadiosVertical("radioButtonPartip", "sendtochooser.form.radio.rightgroup", formLayout, radioKeysPartips, radioValuesPartips);
		radioButtonPartips.select(NLS_RADIO_ALL, true);
		radioButtonPartips.addActionListener(listener, FormEvent.ONCLICK);
		if ( (keysPartips != null) && (valuesPartips != null) ) {
			multiSelectionPartipKeys = uifactory.addCheckboxesVertical("multiSelectionPartipKeys", "", formLayout, keysPartips, valuesPartips, null, 1);
		} 
			
		radioButtonWaitings = uifactory.addRadiosVertical("radioButtonWaiting", "sendtochooser.form.radio.waitings", formLayout, radioKeysWaitings, radioValuesWaitings);
		radioButtonWaitings.select(NLS_RADIO_NOTHING, true);
		radioButtonWaitings.addActionListener(listener, FormEvent.ONCLICK);
		if ( (keysWaitings != null) && (valuesWaitings != null) ) {
			multiSelectionWaitingKeys = uifactory.addCheckboxesVertical("multiSelectionWaitingKeys", "", formLayout, keysWaitings, valuesWaitings, null, 1);
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
		
		List k;
		
		if ( (radioKeysOwners != null) && (multiSelectionOwnerKeys != null) ) {
			k = Arrays.asList(radioKeysOwners);
			multiSelectionOwnerKeys.setVisible(
					k.contains(NLS_RADIO_CHOOSE) && 
					radioButtonOwner.isSelected(k.indexOf(NLS_RADIO_CHOOSE))
			);
		}
		
		if ( (radioKeysPartips != null) && (multiSelectionPartipKeys != null) ) {		
			k = Arrays.asList(radioKeysPartips);
			multiSelectionPartipKeys.setVisible(
					k.contains(NLS_RADIO_CHOOSE) && 
					radioButtonPartips.isSelected(k.indexOf(NLS_RADIO_CHOOSE))
			);
		}
		
		if ( (radioButtonWaitings != null) && (multiSelectionWaitingKeys != null) ) {		
			radioButtonWaitings.setVisible(showWaitingList);
			k = Arrays.asList(radioKeysWaitings);
			multiSelectionWaitingKeys.setVisible(
					showWaitingList &&
					k.contains(NLS_RADIO_CHOOSE) && 
					radioButtonWaitings.isSelected(k.indexOf(NLS_RADIO_CHOOSE))
			);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

}