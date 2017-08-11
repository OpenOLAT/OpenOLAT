/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.commons.info.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMailStepController extends StepFormBasicController {
	
	private String[] sendOptionKeys;
	private String[] sendOptionValues;
	
	private MultipleSelectionElement sendSelection;
	
	public SendMailStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			List<SendMailOption> options, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		
		sendOptionKeys = new String[options.size()];
		sendOptionValues = new String[options.size()];
		int count = 0;
		for(SendMailOption option:options) {
			sendOptionKeys[count] = option.getOptionKey();
			sendOptionValues[count++] = option.getOptionName();
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_info_contact");
		setFormTitle("wizard.step1.title");
		setFormDescription("wizard.step1.form_description");
		sendSelection = uifactory.addCheckboxesVertical("wizard.step1.send_option", formLayout, sendOptionKeys, sendOptionValues, 1);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedOptions = sendSelection.getSelectedKeys();
		addToRunContext(WizardConstants.SEND_MAIL, selectedOptions);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}