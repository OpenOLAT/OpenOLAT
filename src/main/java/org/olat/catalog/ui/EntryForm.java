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

package org.olat.catalog.ui;

import org.olat.catalog.CatalogEntry;
import org.olat.core.gui.UserRequest;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * Description: <br>
 * The form allows to edit, create respective, a new catalog entry, which is
 * either a category or an alias for the linked repository entry. Further it is
 * abused as input form for import feature within the catalog.
 * <p>
 * 
 * Initial Date: Oct 3, 2004 <br>
 * @author patrick
 */

class EntryForm extends FormBasicController {
	
	// NLS support
	
	private static final String NLS_ENTRY_LEAF = "entry.leaf";
	private static final String NLS_ENTRY_CATEGORY = "entry.category";
	private static final String NLS_ENTRY_DESCRIPTION = "entry.description";
	private static final String NLS_FORM_LEGENDE_MANDATORY = "form.legende.mandatory";
	private static final String NLS_INPUT_TOOLONG = "input.toolong";

	// private stuff
	
	private TextElement tName;
	private TextElement taDescription;
	private boolean isLeaf;

	/**
	 * @param name
	 * @param isLeaf
	 */
	public EntryForm(UserRequest ureq, WindowControl wControl, boolean isLeaf) {
		super(ureq, wControl);
		this.isLeaf = isLeaf;
		initForm (ureq);
	}

	/**
	 * fills the supplied CatalogEntry entry object with the values from the form
	 * fields
	 * 
	 * @param ce
	 */
	public void fillEntry(CatalogEntry ce) {
		ce.setName(tName.getValue());
		ce.setDescription(taDescription.getValue());
	}

	/**
	 * fills the form fields with the catalog entry data
	 * 
	 * @param ce
	 */
	public void setFormFields(CatalogEntry ce) {
		tName.setValue(ce.getName());
		taDescription.setValue(ce.getDescription());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tName = uifactory.addTextElement("name", isLeaf?NLS_ENTRY_LEAF:NLS_ENTRY_CATEGORY, 255, "", formLayout);
		tName.setMandatory(true);
		tName.setNotEmptyCheck(NLS_FORM_LEGENDE_MANDATORY);
		taDescription = uifactory.addTextAreaElement("description", NLS_ENTRY_DESCRIPTION, 255, 5, 60, true, "", formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
}

