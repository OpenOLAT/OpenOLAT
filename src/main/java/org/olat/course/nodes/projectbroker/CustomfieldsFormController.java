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

package org.olat.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.projectbroker.datamodel.CustomField;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;

/**
 * 
 * @author guretzki
 */

public class CustomfieldsFormController extends FormBasicController {

	private static final int MAX_NBR_CUSTOM_FIELDS = 5;

	private ProjectBrokerModuleConfiguration config;
	
	private final String ADD_FIELD_LINK = "customfield.add.field.link";

	String[] keys   = new String[] {"customfield.table.enabled"};
	String[] values = new String[] {translate("customfield.table.enabled")};

	private FormSubmit formSubmit;
	private List<TextElement> customFieldNameElementList;
	private List<TextElement> customFieldValueElementList;
	private List<MultipleSelectionElement> customFieldTableFlagElementList;
	private List<FormLink> customFieldLinkElementList;
	private List<SpacerElement> customFieldSpacerElementList;
	
	private List<CustomField> customFields;
	/**
	 * Modules selection form.
	 * @param name
	 * @param config
	 */
	public CustomfieldsFormController(UserRequest ureq, WindowControl wControl, ProjectBrokerModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		customFieldNameElementList      = new ArrayList<>();
		customFieldValueElementList     = new ArrayList<>();
		customFieldTableFlagElementList = new ArrayList<>();
		customFieldLinkElementList      = new ArrayList<>();
		customFieldSpacerElementList    = new ArrayList<>();
	  customFields = config.getCustomFields();
		initForm(this.flc, this, ureq);
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest, Identity)
	 */
	public boolean validate() {
		return true;
	}

	/**
	 * Initialize form.
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addFormLink(ADD_FIELD_LINK, formLayout,Link.BUTTON_SMALL);
		uifactory.addSpacerElement("spacer", formLayout, false);

		createFormElements(formLayout);
	}

	private void createFormElements(FormItemContainer formLayout) {
	  
		//create form elements
		int i = 0;
		for (Iterator<CustomField> iterator = customFields.iterator(); iterator.hasNext();) {
			CustomField customField = iterator.next();
			createFormElemente(formLayout, i++, customField);
		}		
		formSubmit = uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// loop over all Element to store values
		for (int i = 0; i < customFields.size(); i++) {
			TextElement nameTextElement = customFieldNameElementList.get(i);
			if ( !customFields.get(i).getName().equals(nameTextElement.getValue()) ) {
				customFields.get(i).setName(nameTextElement.getValue());
			}
			TextElement valueTextElement = customFieldValueElementList.get(i);
			if ( !customFields.get(i).getValue().equals(valueTextElement.getValue()) ) {
				customFields.get(i).setValue(valueTextElement.getValue());
			}
			MultipleSelectionElement tableViewElement = customFieldTableFlagElementList.get(i);
			if ( customFields.get(i).isTableViewEnabled() != tableViewElement.isSelected(0) ) {
				customFields.get(i).setTableViewEnabled(tableViewElement.isSelected(0));
			}
		}
		config.setCustomFields(customFields);
		fireEvent(ureq, Event.DONE_EVENT);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {

		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (link.getName().equals(ADD_FIELD_LINK)) {
				if (customFields.size() < MAX_NBR_CUSTOM_FIELDS) {
					// Add new custom-field
					int indexNewCustomField = customFields.size();
					customFields.add(new CustomField("","",false));
					// first remove existing submit button, add it again at the end
					flc.remove(formSubmit);
					createFormElemente( this.flc, indexNewCustomField, customFields.get(indexNewCustomField));						
					formSubmit = uifactory.addFormSubmitButton("save", this.flc);
				} else {
					this.showInfo("info.max.nbr.custom.fields");
				}
			} else {
				int deleteElementNumber = ((Integer)link.getUserObject()).intValue();
				getLogger().debug("remove customfield #=" + deleteElementNumber);
				customFields.remove(deleteElementNumber);
				initFormElements(flc);
			}
		}

		this.flc.setDirty(true);
	}

	private void initFormElements(FormLayoutContainer flc) {
		removeAllFormElements(flc);
		createFormElements(flc);
	}

	private void createFormElemente(FormItemContainer formLayout, int i, CustomField customField) {
		TextElement nameElement = uifactory.addTextElement("customfield_name_" + i, "-", 50, customField.getName(), formLayout);
		nameElement.setLabel("customfield.name.label", null);
		if (i == 0) nameElement.setExampleKey("customfield.example.name", null);
		customFieldNameElementList.add(nameElement);
		
		TextElement valueElement = uifactory.addTextAreaElement("customfield_value_" + i, "-", 2500, 5, 2, true, false, customField.getValue(), formLayout);
		valueElement.setLabel("customfield.value.label", null);
		if (i == 0)  valueElement.setExampleKey("customfield.example.value", null);
		customFieldValueElementList.add(valueElement);
		
		MultipleSelectionElement tableEnabledElement = uifactory.addCheckboxesHorizontal("customfield.table.enabled." + i, null, formLayout, keys, values);
		tableEnabledElement.select(keys[0], customField.isTableViewEnabled());
		customFieldTableFlagElementList.add(tableEnabledElement);

		FormLink deleteLink = uifactory.addFormLink("customfield.delete.link." + i, formLayout,Link.BUTTON_SMALL);
		deleteLink.setUserObject(new Integer(i));
		customFieldLinkElementList.add(deleteLink);
		
		SpacerElement spacerElement = uifactory.addSpacerElement("spacer" + i, formLayout, false);
		customFieldSpacerElementList.add(spacerElement);
	}

	private void removeAllFormElements(FormLayoutContainer flc) {
		// remove all name fields
		for (Iterator<TextElement> iterator = customFieldNameElementList.iterator(); iterator.hasNext();) {
			flc.remove(iterator.next());
		}
		customFieldNameElementList.clear();
		// remove all value fields
		for (Iterator<TextElement> iterator = customFieldValueElementList.iterator(); iterator.hasNext();) {
			flc.remove(iterator.next());
		}
		customFieldValueElementList.clear();
		// remove all table view checkboxes
		for (Iterator<MultipleSelectionElement> iterator = customFieldTableFlagElementList.iterator(); iterator.hasNext();) {
			flc.remove(iterator.next());
		}
		customFieldTableFlagElementList.clear();
		// remove all delete links
		for (Iterator<FormLink> iterator = customFieldLinkElementList.iterator(); iterator.hasNext();) {
			flc.remove(iterator.next());
		}
		customFieldLinkElementList.clear();
		// remove all spacer elements
		for (Iterator<SpacerElement> iterator = customFieldSpacerElementList.iterator(); iterator.hasNext();) {
			flc.remove(iterator.next());
		}
		customFieldSpacerElementList.clear();
		flc.remove(formSubmit);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//
	}

}
