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

package org.olat.repository;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class DetailsReadOnlyForm extends FormBasicController {

	private RepositoryEntry entry;
	private String typeName;
	
	final public static int MAX_DISPLAYNAME = 100;
	private SelectionElement canDownload;
	
	/**
	 * The details form is initialized with data collected from entry and typeName. Handler is looked-up by
	 * the given handlerName and not by the entry's resourceableType. This is to allow for an entry with no resourceable
	 * to initialize correctly (c.f. RepositoryAdd workflow). The typeName may be null.
	 * 
	 * @param name
	 * @param t
	 * @param entry
	 * @param typeName
	 * @param enableAuthorView
	 */
	public DetailsReadOnlyForm(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String typeName) {
		super(ureq, wControl);
		this.entry = entry;
		this.typeName = typeName;
		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("cif_displayname", "cif.displayname", entry.getDisplayname(), formLayout);
		uifactory.addStaticTextElement("cif_initialAuthor", "cif.initialAuthor", entry.getInitialAuthor(), formLayout);
		uifactory.addStaticTextElement("cif_description", "cif.description", (entry.getDescription() != null) ? entry.getDescription() : " ", formLayout);
		uifactory.addStaticTextElement("cif_resourcename", "cif.resourcename", (entry.getResourcename() == null) ? "-" : entry.getResourcename(), formLayout);
	
		StringBuilder typeDisplayText = new StringBuilder(100);
		if (typeName != null) { // add image and typename code
			typeDisplayText.append("<span class=\"b_with_small_icon_left ");
			typeDisplayText.append(RepositoyUIFactory.getIconCssClass(entry));
			typeDisplayText.append("\">");
			String tName = NewControllerFactory.translateResourceableTypeName(typeName, getLocale());
			typeDisplayText.append(tName);
			typeDisplayText.append("</span>"); 
		} else {
			typeDisplayText.append(translate("cif.type.na"));
		}
		
		uifactory.addStaticTextElement("cif_type", "cif.type", typeDisplayText.toString(), formLayout);
		
		RepositoryHandler handler = null;
		if (typeName != null) handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(typeName);
		
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", "cif.canDownload", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canDownload.select("xx", entry.getCanDownload());
		canDownload.setVisible(handler != null && handler.supportsDownload(this.entry));
		canDownload.setEnabled(false); // It is readonly
		if (handler == null || !handler.supportsDownload(this.entry)) {
			canDownload.setExampleKey("cif.canDownload.na", null);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}