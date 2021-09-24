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
package org.olat.group.ui.wizard;

import java.util.Collection;

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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGEmailSelectReceiversStepController extends StepFormBasicController   {
	
	private MultipleSelectionElement receiversEl;
	private final String[] receiverKeys = new String[]{ "tutors", "participants" };
	
	public BGEmailSelectReceiversStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("email.select.receivers.desc");
		String[] receiverValues = new String[] {
				translate("email.select.receiver.tutor"),
				translate("email.select.receiver.participant")
		};
		receiversEl = uifactory.addCheckboxesVertical("select.receivers", "email.select.receivers", formLayout, receiverKeys, receiverValues, 1);
		receiversEl.select(receiverKeys[0], true);
		receiversEl.select(receiverKeys[1], true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return receiversEl.isAtLeastSelected(1) && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedKeys = receiversEl.getSelectedKeys();
		addToRunContext("tutors", Boolean.valueOf(selectedKeys.contains("tutors")));
		addToRunContext("participants", Boolean.valueOf(selectedKeys.contains("participants")));
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
