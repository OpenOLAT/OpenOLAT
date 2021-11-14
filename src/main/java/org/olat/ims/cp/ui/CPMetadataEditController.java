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
* <p>
*/
package org.olat.ims.cp.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.cp.objects.CPMetadata;

/**
 * 
 * Description:<br>
 * This Class represents the Metadata-Form. (used in the page-editor)
 * 
 * <P>
 * Initial Date: 11.09.2008 <br>
 * 
 * @author sergio
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CPMetadataEditController extends FormBasicController {

	private TextElement title;

	private final CPMetadata metadata;
	private final CPPage page;

	public CPMetadataEditController(UserRequest ureq, WindowControl control, CPPage page) {
		super(ureq, control);
		this.page = page;
		if (page.getMetadata() != null) {
			metadata = page.getMetadata();
		} else {
			metadata = new CPMetadata();
		}
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_cp_metadata");
		// title-field
		String titleString = page.getTitle();
		title = uifactory.addTextElement("title", "cpmd.flexi.title", 256, titleString, formLayout);
		title.setElementCssClass("o_sel_cp_title");
		title.setDisplaySize(32);
		title.setMandatory(true);
		title.setNotEmptyCheck("cpmd.flexi.mustbefilled");

		FormItemContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add(buttonContainer);
		uifactory.addFormSubmitButton("save", buttonContainer);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	/**
	 * Gets the CPPage this Flexiform is editing
	 * 
	 * @return the CPPage
	 */
	public CPPage getPage() {
		return page;
	}

	public CPMetadata getMetadata() {
		return metadata;
	}
	
	protected void newPageAdded(String newNodeID) {
		page.setIdentifier(newNodeID);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		metadata.setTitle(title.getValue());
		page.setTitle(title.getValue());
		page.setMetadata(metadata);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}