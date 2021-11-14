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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;

/**
 * Initial Date: 10.09.2007 <br>
 * 
 * @author patrickb
 */
public class GuiDemoFlexiFormSubworkflow extends FormBasicController {

	private GuiDemoFlexiFormPersonData personData;
	private TextElement firstName;
	private TextElement lastName;
	private FormLayoutContainer horizontalLayout;
	private TextElement institution;
	private FormLink choose;
	private FormSubmit submit;
	private String[] values;
	private GuiDemoFlexiFormSubworkflowTheChooser subworkflowTheChooser;
	private CloseableCalloutWindowController calloutWindowCtr;
		
	public GuiDemoFlexiFormSubworkflow(UserRequest ureq, WindowControl wControl, GuiDemoFlexiFormPersonData data) {
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
		values = new String[]{"Faculty of Medicine","Vetsuisse Faculty","Faculty of Arts","Faculty of Science","All institutes and clinics of UZH"};
		//
		// calls our initForm(formlayout,listener,ureq) with default values.
		initForm(ureq);
		//
		// after initialisation you may need to do some stuff
		// but typically initForm(..) is the last call in the constructor.
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

	@Override
	protected void  formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == choose){
			//choose link clicked
			//show a subworkflow in a modal dialog without loosing fromdata
			//
			// work around to disable orange button and alert box of "unsubmitted form onscreen"
			// this will change within the next release.
			this.flc.getRootForm().setDirtyMarking(false);
			//
			//
			subworkflowTheChooser = new GuiDemoFlexiFormSubworkflowTheChooser(ureq, getWindowControl(), values, institution.getValue());
			// get informed if subworkflow has submitted =>
			listenTo(subworkflowTheChooser);
			calloutWindowCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), subworkflowTheChooser.getInitialComponent(), choose, null, true, null);
			// get informed if modal dialog is closed => this means canceling the workflow 
			listenTo(calloutWindowCtr);
			// open callout window now
			calloutWindowCtr.activate();
			return;
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, org.olat.core.gui.control.Event event) {
		if(source == calloutWindowCtr){
			if(event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT){
				// work around to re-enable orange button and alert box of "unsubmitted form onscreen"
				// this will change within the next release.
				this.flc.getRootForm().setDirtyMarking(true);
				// modal dialog is closed
				// modal dialog has already deactivated itself (you would get a redscreen otherwise)
				removeAsListenerAndDispose(subworkflowTheChooser);
				subworkflowTheChooser = null;
				// cleanup callout window
				removeAsListenerAndDispose(calloutWindowCtr);
				calloutWindowCtr = null;
			}
		} else if(source == subworkflowTheChooser){
			//some value is choosen
			//set the value and close dialog
			institution.setValue(subworkflowTheChooser.getSelected());
			removeAsListenerAndDispose(subworkflowTheChooser);
			subworkflowTheChooser = null;	
			// cleanup callout window
			calloutWindowCtr.deactivate();
			removeAsListenerAndDispose(calloutWindowCtr);
			calloutWindowCtr = null;
			// work around to re-enable orange button and alert box of "unsubmitted form onscreen"
			// this will change within the next release.
			this.flc.getRootForm().setDirtyMarking(true);
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		/*
		 * create a form with a title and 4 input fields to enter some persons data
		 */
		// Example: set a form title and render within fieldsets
		setFormTitle("guidemo_flexi_form_withchooser");
		// Example2: Set a form description above the form
		setFormDescription("guidemo_flexi_form_withchooser.desc");
		// Example3: Set a context help link
		setFormContextHelp("Folders#Folders-MetaData");		
		
		final boolean inputMode = !personData.isReadOnly();
		
		firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, personData.getFirstName(), formLayout);
		firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		firstName.setMandatory(true);
		firstName.setEnabled(inputMode);
	
		lastName = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, personData.getLastName(), formLayout);
		lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		lastName.setMandatory(true);
		lastName.setEnabled(inputMode);

		/*
		 * - create a composite element
		 * - text element and to the left a choose link
		 * - the label of the textelement is set as the label of the layouting 
		 * container. 
		 */
		horizontalLayout = FormLayoutContainer.createHorizontalFormLayout("chooser", getTranslator());
		horizontalLayout.	setLabel("guidemo.flexi.form.institution", null);
		formLayout.add(horizontalLayout);
		
		institution = uifactory.addTextElement("institution", null, 256, personData.getInstitution(), horizontalLayout);
		institution.setEnabled(false);
		
		choose = uifactory.addFormLink("choose", horizontalLayout);
		
		if (inputMode) {
			// submit only if in input mode
			submit = new FormSubmit("submit", "submit");
			formLayout.add(submit);
		}
	}

	
	
	//this innerclass represents the subworkflow
	//but it could be any top level controller
	private class GuiDemoFlexiFormSubworkflowTheChooser extends FormBasicController {

		String[] entries;
		private SingleSelection entrySelector;
		private String selection;

		public GuiDemoFlexiFormSubworkflowTheChooser(UserRequest ureq, WindowControl wControl, String[] values, String selection) {
			super(ureq, wControl);
			this.entries = values;
			this.selection = selection;
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			fireEvent(ureq, Event.DONE_EVENT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			//radio buttons
			entrySelector = uifactory.addRadiosVertical("entries", null, formLayout, entries, entries);
			//select one
			if (selection != null && !selection.equals("")) {
				entrySelector.select(selection, true);
			}
			//on click do something -> see the formInnerEvent method
			entrySelector.addActionListener(FormEvent.ONCLICK);
			
		}
		
		
		String getSelected(){
			return entrySelector.getSelectedKey();
		}
	}

}
