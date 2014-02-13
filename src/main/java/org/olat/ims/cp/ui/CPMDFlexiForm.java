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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
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
 */
public class CPMDFlexiForm extends FormBasicController {

	private TextElement title;
	// private TextElement description;
	// private TextElement keywords;
	// private TextElement version;
	// private TextElement status;
	// private DateChooser date;
	// private TextElement format;
	// private TextElement author;

	private FormLink lSave;
	private FormLink lSaveandclose;

	private CPMetadata metadata;
	private CPPage page;

	private int buttonClickedIndex; // used to store which button was pressed (0 =save, 1 = save and close)

	public CPMDFlexiForm(UserRequest ureq, WindowControl control, CPPage page) {
		super(ureq, control);
		this.page = page;
		if (page.getMetadata() != null) {
			this.metadata = page.getMetadata();
		} else {
			this.metadata = new CPMetadata();
		}
		initForm(ureq);

	}

	public CPMetadata getMetadata() {
		return metadata;
	}

	@Override
	protected void doDispose() {
	// nothing to do
	}

	/**
	 * this methode is invoked, when all fields are filled in properly
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	protected void formOK(UserRequest ureq) {
		// TODO:GW Support metadata
		metadata.setTitle(title.getValue());
		// metadata.setDescription(description.getValue());
		// metadata.setKeywords(keywords.getValue());
		// metadata.setVersion(version.getValue());
		// metadata.setStatus(status.getValue());
		// metadata.setFormat(format.getValue());
		// metadata.setAuthor(author.getValue());
		// metadata.setData(date.getValue());

		page.setTitle(title.getValue());
		page.setMetadata(metadata);
		
		if (buttonClickedIndex == 0) {
			this.flc.setDirty(true);
			fireEvent(ureq, new Event("saved"));
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	
	protected void formNOK(UserRequest ureq) {
		this.flc.setDirty(true);
	}

	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		buttonClickedIndex = (source.equals(lSave)) ? 0 : 1;
		mainForm.submit(ureq);
	}

	protected boolean validateFormLogic(UserRequest ureq) {
//		return !title.isEmpty();
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("cpmd.flexi.formtitle");
		final int defaultDisplaySize = 32;

		// title-field
		String titleString = page.getTitle();
		title = uifactory.addTextElement("title", "cpmd.flexi.title", 256, titleString, formLayout);
		title.setDisplaySize(defaultDisplaySize);
		title.setMandatory(true);
		title.setNotEmptyCheck("cpmd.flexi.mustbefilled");

//		description = formUIf.addTextAreaElement("descr", 256, 5, 2, true, metadata.getDescription(), "cpmd.flexi.descr", formLayout);
//		description.setDisplaySize(defaultDisplaySize);
//
//		// keywords-field
//		keywords = formUIf.addTextElement("keywords", 256, metadata.getKeywords(), "cpmd.flexi.keywords", formLayout);
//		keywords.setDisplaySize(defaultDisplaySize);
//
//		formUIf.addStaticTextElement("hr", "<hr />", formLayout);
//
//		version = formUIf.addTextElement("version", 256, metadata.getVersion(), "cpmd.flexi.version", formLayout);
//		status = formUIf.addTextElement("status", 256, metadata.getStatus(), "cpmd.flexi.status", formLayout);
//		date = formUIf.addDateChooser("date", metadata.getDate(), formLayout);
//		format = formUIf.addTextElement("format", 256, metadata.getFormat(), "cpmd.flexi.format", formLayout);
//		author = formUIf.addTextElement("author", 256, metadata.getAuthor(), "cpmd.flexi.author", formLayout);

		FormItemContainer buttonContainer = FormLayoutContainer.createHorizontalFormLayout("buttonContainer", getTranslator());
		formLayout.add(buttonContainer);

		// Submit buttons
		lSave = new FormLinkImpl("lSave", "lSave", "submit", Link.BUTTON);
		lSave.addActionListener(FormEvent.ONCLICK);

		lSaveandclose = new FormLinkImpl("lSaveandclose", "lSaveandclose", "saveandclose", Link.BUTTON);

		lSaveandclose.addActionListener(FormEvent.ONCLICK);
		buttonContainer.add(lSave);
		buttonContainer.add(lSaveandclose);

	}

	/**
	 * Gets the CPPage this Flexiform is editing
	 * 
	 * @return the CPPage
	 */
	public CPPage getPage() {
		return page;
	}

}
