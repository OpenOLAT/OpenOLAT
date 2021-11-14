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
package org.olat.repository.ui.catalog;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.CatalogEntry;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 24 Feb 2020<br>
 * @author Alexander Boeckle
 */
public class CatalogEntryPositionDialogController extends FormBasicController {
	Long catalogEntryKey;
	int position;
	int smallest;
	int biggest;

	CatalogEntry catalogEntry;

	@Autowired
	CatalogManager catalogManager;
	
	FormLink smallestFormLink;
	FormLink smallerFormLink;
	FormLink biggestFormLink;
	FormLink biggerFormLink;
	TextElement positionTextElement;

	public CatalogEntryPositionDialogController(UserRequest ureq, WindowControl wControl, Long catalogEntryKey, int smallest, int biggest) {
		super(ureq, wControl, "catPosition", Util.createPackageTranslator(CatalogNodeManagerController.class, ureq.getLocale()));
		
		catalogEntry = catalogManager.getCatalogEntryByKey(catalogEntryKey);
		this.biggest = biggest + 1;
		this.smallest = smallest + 1;
		this.catalogEntryKey = catalogEntryKey;
		this.position = catalogEntry.getPosition() + 1;

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {	
		flc.contextPut("catalog_entry_name", catalogEntry.getName());
		
		smallestFormLink = uifactory.addFormLink("catalog.popup.position.smallest", formLayout, Link.BUTTON);
		smallerFormLink = uifactory.addFormLink("catalog.popup.position.smaller", formLayout, Link.BUTTON);
		positionTextElement = uifactory.addTextElement("catalog.popup.position.set", 5, String.valueOf(position), formLayout);
		positionTextElement.setDisplaySize(4);
		positionTextElement.setMandatory(true);
		positionTextElement.setElementCssClass("o_centered_form");
		biggerFormLink = uifactory.addFormLink("catalog.popup.position.bigger", formLayout, Link.BUTTON);
		biggestFormLink = uifactory.addFormLink("catalog.popup.position.biggest", formLayout, Link.BUTTON);
		
		FormLayoutContainer filterButtonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		filterButtonLayout.setRootForm(mainForm);
		formLayout.add(filterButtonLayout);
		
		uifactory.addFormSubmitButton("catalog.popup.position.save", filterButtonLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		try {
			position = Integer.parseInt(positionTextElement.getValue());
			
			if (position < smallest || position > biggest) {
				positionTextElement.setErrorKey("catalog.popup.position.error.number", new String[]{String.valueOf(smallest), String.valueOf(biggest)});
				allOk&=false;
			} 
		} catch (Exception e) {
			positionTextElement.setErrorKey("catalog.popup.position.error.number.format", null);
			allOk&=false;
		}
	
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == smallerFormLink && position > smallest) {
			position -= 1;
			positionTextElement.setValue(String.valueOf(position));
		} else if (source == smallestFormLink) {
			position = smallest + 1;
			positionTextElement.setValue(String.valueOf(smallest));
		} else if (source == biggerFormLink && position < biggest) {
			position += 1;
			positionTextElement.setValue(String.valueOf(position));
		} else if (source == biggestFormLink) {
			position = biggest + 1;
			positionTextElement.setValue(String.valueOf(biggest));
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		catalogManager.setPosition(catalogEntryKey, position - 1);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
