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

package org.olat.course.nodes.scorm;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.scorm.ScormConstants;

/**
 * Description:<br>
 * Let the student decide in which mode he likes to run his scorm package.
 * 
 * <P>
 * Initial Date:  08.09.2005 <br>
 * @author guido
 */
public class ChooseScormRunModeForm extends FormBasicController {

	private SingleSelection mode;
	private boolean showOptions;
	private String[] modeKeys, modeValues;
	/**
	 * @param name
	 */
	public ChooseScormRunModeForm(UserRequest ureq, WindowControl wControl, boolean showOptions) {
		super(ureq, wControl);
		this.showOptions = showOptions;
		
		modeKeys = new String[] {
				ScormConstants.SCORM_MODE_NORMAL,
				ScormConstants.SCORM_MODE_BROWSE,
				ScormConstants.SCORM_MODE_NOCREDIT
		};
		
		modeValues = new String[] {
				translate("form.scormmode.normal"),
				translate("form.scormmode.browse"),
				translate("form.scormmode.nocredit")
		};
		
		initForm (ureq);
	}

	
	/**
	 * 
	 * @return the key of the selected element in the dropdown
	 */
	public String getSelectedElement(){
		return mode.getSelectedKey();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		mode = uifactory.addRadiosVertical("mode", "form.scormmode", formLayout, modeKeys, modeValues);
		mode.select(ScormConstants.SCORM_MODE_NORMAL, true);
		mode.setVisible(showOptions);
		FormSubmit showButton = uifactory.addFormSubmitButton("command.showscorm", formLayout);
		showButton.setElementCssClass("o_sel_start_scorm");
	}

	@Override
	protected void doDispose() {
		//
	}
}