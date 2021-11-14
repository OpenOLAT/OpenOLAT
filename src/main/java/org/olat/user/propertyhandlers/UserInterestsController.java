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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.user.UserPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description:<br>
 * Provides a controller which lets the user choose from a set of interests. The interests to choose from are defined in an XML configuration file.
 * 
 * <P>
 * Initial Date:  Aug 5, 2009 <br>
 * @author twuersch
 */
public class UserInterestsController extends FormBasicController {

	/** The user interests checkboxes. Note that there is one list element per <b>group</b> of checkboxes, not per checkbox. */
	private List<MultipleSelectionElement> checkboxGroups;
	
	/** The available user interests loaded from the XML file. */
	private List<UserInterestsCategory> availableUserInterests;
	
	/** The selected user interests */
	private String selectedInterestsIDs;
	
	private final int maxNumOfInterests;

	@Autowired
	private UserPropertiesConfig userPropertiesConfig;
	
	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 */
	public UserInterestsController(UserRequest ureq, WindowControl wControl,
			String selectedInterestsIDs, List<UserInterestsCategory> availableUserInterests) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(UserInterestsPropertyHandler.PACKAGE_UINTERESTS, getLocale(), getTranslator()));
		
		this.checkboxGroups = new ArrayList<>();
		this.availableUserInterests = availableUserInterests;
		this.selectedInterestsIDs = selectedInterestsIDs; 
		maxNumOfInterests = userPropertiesConfig.getMaxNumOfInterests();
		initForm(ureq);
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
		setFormDescription("userinterests.description");
		
		// Draw the checkboxes
		for (int i = 0; i < this.availableUserInterests.size(); i++) {
			UserInterestsCategory category = this.availableUserInterests.get(i);
			Vector<String> keys = new Vector<>();
			for (UserInterestsCategory subcategory : category.getSubcategories()) {
				keys.add(UserInterestsPropertyHandler.SUBCATEGORY_I18N_PREFIX + subcategory.getId());
			}
			String[] values = new String[keys.size()];
			for (int j = 0; j < values.length; j++) {
				values[j] = translate(keys.get(j));
			}
			MultipleSelectionElement interestCheckboxes = uifactory.addCheckboxesVertical("interest_" + category.getId(), formLayout, keys.toArray(new String[0]), values, null, 2);
			this.checkboxGroups.add(interestCheckboxes);
			interestCheckboxes.setLabel(UserInterestsPropertyHandler.CATEGORY_I18N_PREFIX + category.getId(), null);
			interestCheckboxes.addActionListener(FormEvent.ONCHANGE);
			if (i != this.availableUserInterests.size() - 1) {
				uifactory.addSpacerElement("spacer_" + category.getId(), formLayout, false);
			}
		}
		
		// Check boxes for the given interests
		this.setSelectedInterests(this.selectedInterestsIDs);
		
		// Disable the checkboxes if already the maximum number is selected.
		if (getSelectionCount() >= maxNumOfInterests) {
			Set<String> currentSelection = getCurrentSelection();
			for (MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
				MultipleSelectionElementImpl multipleSelectionElementImpl = (MultipleSelectionElementImpl)checkboxGroup;
				Set<String> allUncheckedCheckboxes = new HashSet<>(multipleSelectionElementImpl.getKeys());
				allUncheckedCheckboxes.removeAll(currentSelection);
				multipleSelectionElementImpl.setEnabled(allUncheckedCheckboxes, false);
			}
			showWarning("form.warning.maxNumber");
		}

		// Add submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("finish", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		// did the user click on a check box?
		if (this.checkboxGroups.contains(source)) {
			
			// find out which checkboxes are selected at the moment.
			Set<String> currentSelection = getCurrentSelection();
			
			// if the user has not selected the maximum number of boxes...
			if (getSelectionCount() < maxNumOfInterests) {
				// ...enable all checkboxes
				for (MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
					MultipleSelectionElementImpl multipleSelectionElementImpl = (MultipleSelectionElementImpl)checkboxGroup;
					multipleSelectionElementImpl.setEnabled(multipleSelectionElementImpl.getKeys(),	true);
				}
			} else {
				// ... otherwise, disable all checkboxes except the selected ones.
				for (MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
					MultipleSelectionElementImpl multipleSelectionElementImpl = (MultipleSelectionElementImpl)checkboxGroup;
					Set<String> allUncheckedCheckboxes = new HashSet<>(multipleSelectionElementImpl.getKeys());
					allUncheckedCheckboxes.removeAll(currentSelection);
					multipleSelectionElementImpl.setEnabled(allUncheckedCheckboxes, false);
				}
				showWarning("form.warning.maxNumber");
			}
		}
	}
	
	/**
	 * Returns a string which contains the identifiers of the selected checkboxes, delimited by colons.
	 * 
	 * @return a string which contains the i18n keys of the selected checkboxes, delimited by colons.
	 */
	public String getSelectedInterests() {
		// Retrieve the i18n keys of the selected checkboxes
		Set<String> selectedInterestsKeysAsSet = new HashSet<>(); 
		for (MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
			selectedInterestsKeysAsSet.addAll(checkboxGroup.getSelectedKeys());
		}
		
		if (selectedInterestsKeysAsSet.size() > 0) {
			// convert these keys into IDs
			@SuppressWarnings("hiding")
			Set<String> selectedInterestsIDs = new HashSet<>();
			for (String selectedInterestKey : selectedInterestsKeysAsSet) {
				selectedInterestsIDs.add(selectedInterestKey.replace(UserInterestsPropertyHandler.SUBCATEGORY_I18N_PREFIX, ""));
			}
			
			StringBuffer selectedInterestsKeys = new StringBuffer();
			selectedInterestsKeys.append(":");
			for (String selectedInterestID : selectedInterestsIDs) {
				selectedInterestsKeys.append(selectedInterestID);
				selectedInterestsKeys.append(":");
			}
			return selectedInterestsKeys.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * Selects all checkboxes whose i18n keys are contained in the space-delimited <code>selectedInterestsKeys</code>.
	 * 
	 * @param selectedInterestsIDs
	 */
	public void setSelectedInterests(String selectedInterestsIDs) {
		if (selectedInterestsIDs != null) {
			Set<String> selectedInterestsIDsAsSet = new HashSet<>(Arrays.asList(selectedInterestsIDs.trim().split(":")));
			// for each ID we get, ...
			for (String selectedInterestID : selectedInterestsIDsAsSet) {
				if (!selectedInterestID.equals("")) {
					// ... we have to loop over all checkbox groups since we don't know which one the ID belongs to. 
					for (MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
						String key = UserInterestsPropertyHandler.SUBCATEGORY_I18N_PREFIX + selectedInterestID;
						MultipleSelectionElementImpl checkboxGroupImpl = (MultipleSelectionElementImpl)checkboxGroup;
						if (checkboxGroupImpl.getKeys().contains(key)) {
							checkboxGroup.select(key, true);
						}
					}
				}
			}
		}
	}
	
	private int getSelectionCount() {
		int selectionCount = 0;
		for(MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
			selectionCount += checkboxGroup.getSelectedKeys().size();
		}
		return selectionCount;
	}
	
	private Set<String> getCurrentSelection() {
		Set<String> currentSelection = new HashSet<>();
		for (MultipleSelectionElement checkboxGroup : this.checkboxGroups) {
			currentSelection.addAll(checkboxGroup.getSelectedKeys());
		}
		return currentSelection;
	}
}
