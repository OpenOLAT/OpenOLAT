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
package org.olat.gui.demo.guidemo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;

/**
 * Initial Date: 06.09.2007 <br>
 * 
 * @author patrickb
 */
public class GuiDemoFlexiForm extends FormBasicController {

	private TextElement firstName;
	private TextElement lastName;
	private TextElement institution;
	private FileElement fileElement;
	private Submit submit;
	private GuiDemoFlexiFormPersonData personData;
	private VelocityContainer confirm;
	private GuiDemoFlexiForm confirmController;
	private File tmpFile;
	private TextAreaElement stripedBackgroundAndLineNumbersEl;

	public GuiDemoFlexiForm(UserRequest ureq, WindowControl wControl, GuiDemoFlexiFormPersonData data) {
		super(ureq, wControl);
		// first you may preprocess data to fit into the form items
		// if all preprocessing is done, create the form items
		//
		// example for simple preprocessing - check for NULL
		if (data != null) {
			personData = data;
		} else {
			personData = new GuiDemoFlexiFormPersonData();
		}
		//
		// calls our initForm(formlayout,listener,ureq) with default values.
		initForm(ureq);
		//
		// after initialisation you may need to do some stuff
		// but typically initForm(..) is the last call in the constructor.
	}

	@Override
	protected void doDispose() {
		// cleanup temp files
		if (tmpFile != null) {
			FileUtils.deleteFile(tmpFile);
		}
        super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// this method is called if the form has validated
		// which means that all form items are filled without error
		// and all complex business rules validated also to true.
		//
		// typically the form values are now read out and persisted
		//
		// in our case, save value to data object and prepare a confirm page
		personData.setFirstName(firstName.getValue());
		personData.setLastName(lastName.getValue());
		personData.setInstitution(institution.getValue());
		personData.setReadOnly(true);

		// get file and store it in temporary location
		tmpFile = new File(WebappHelper.getTmpDir(), fileElement.getUploadFileName());
		fileElement.moveUploadFileTo(tmpFile);
		personData.setFile(tmpFile);
		
		// show the same form in readonly mode.
		confirmController = new GuiDemoFlexiForm(ureq, getWindowControl(), personData);
		listenTo(confirmController); // guarantees autodispose later
		confirm = createVelocityContainer("confirm");
		confirm.put("data", confirmController.getInitialComponent());

		initialPanel.pushContent(confirm);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		/*
		 * create a form with a title and 4 input fields to enter some persons data
		 */
		setFormTitle("guidemo_flexi_form_simpleform");
		final int defaultDisplaySize = 32;
		final boolean inputMode = !personData.isReadOnly();

		firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, personData.getFirstName(), formLayout);
		firstName.setDisplaySize(defaultDisplaySize);
		//firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		firstName.setMandatory(true);
		firstName.setEnabled(inputMode);
		firstName.setPlaceholderText("Hans");
		firstName.setHelpText("If you have a middle name, add it to the first name input field");
		firstName.setHelpUrlForManualPage("manual_user/personal/Configuration/#profile");

		lastName = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, personData.getLastName(), formLayout);
		lastName.setDisplaySize(defaultDisplaySize);
		lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		lastName.setEnabled(inputMode);
		lastName.setPlaceholderText("Muster");
		lastName.setHelpUrl("https://en.wikipedia.org/wiki/Family_name");

		fileElement = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "guidemo.flexi.form.file", formLayout);
		fileElement.setMaxUploadSizeKB(500, "guidemo.flexi.form.filetobig", null);
		Set<String> mimeTypes = new HashSet<>();
		mimeTypes.add("image/*");
		fileElement.limitToMimeType(mimeTypes, "guidemo.flexi.form.wrongfiletype", null);
		fileElement.setMandatory(true, "guidemo.flexi.form.mustbefilled");
		fileElement.setEnabled(inputMode);

		institution = uifactory.addTextElement("institution", "guidemo.flexi.form.institution", 256, personData.getInstitution(), formLayout);
		institution.setDisplaySize(defaultDisplaySize);
		institution.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		institution.setMandatory(true);
		institution.setEnabled(inputMode);
		institution.setHelpTextKey("guidemo.flexi.form.institution.help", null);
		
		stripedBackgroundAndLineNumbersEl = uifactory.addTextAreaElement("stripedAndLineNumbers", "guidemo.textarea.striped.line.numbers.label", -1, 10, -1, false, true, null, formLayout);
		stripedBackgroundAndLineNumbersEl.setOriginalLineBreaks(true);
		stripedBackgroundAndLineNumbersEl.setStripedBackgroundEnabled(true);
		stripedBackgroundAndLineNumbersEl.setLineNumbersEnbaled(true);
		stripedBackgroundAndLineNumbersEl.setEnabled(true);
		
		List<Integer> errors = new ArrayList<>();
		errors.add(4);
		errors.add(10);
		errors.add(60);
		errors.add(100);
		errors.add(1000);
		errors.add(1500);
		
		//stripedBackgroundAndLineNumbersEl.setErrors(errors);

		if (inputMode) {
			// submit only if in input mode
			submit = new FormSubmit("submit", "submit");
			formLayout.add(submit);
		}
	}

}
