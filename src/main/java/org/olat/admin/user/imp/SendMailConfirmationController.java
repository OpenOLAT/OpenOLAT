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
package org.olat.admin.user.imp;

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
 * Initial date: 19.2.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendMailConfirmationController extends StepFormBasicController {
	
	private static final String[] keys = { "send" };
	
	private MultipleSelectionElement typEl;

	public SendMailConfirmationController(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext stepsRunContext) {
		super(ureq, wControl, form, stepsRunContext, LAYOUT_DEFAULT, null);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("step3.send.mail.description");
		formLayout.setElementCssClass("o_sel_users_import_contact");
		
		@SuppressWarnings("unchecked")
		List<Long> ownGroups = (List<Long>) getFromRunContext("ownerGroups");
		@SuppressWarnings("unchecked")
		List<Long> partGroups = (List<Long>) getFromRunContext("partGroups");
		@SuppressWarnings("unchecked")
		List<TransientIdentity> newIdents = (List<TransientIdentity>)getFromRunContext("newIdents");
		
		String[] values = new String[] { translate("step3.send.mail") };
		typEl = uifactory.addCheckboxesVertical("typ", "step3.send.label", formLayout, keys, values, 1);
		typEl.setEnabled((ownGroups != null && !ownGroups.isEmpty())
				|| (partGroups != null && !partGroups.isEmpty())
				|| (newIdents != null && !newIdents.isEmpty()));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		addToRunContext("sendMail", Boolean.valueOf(typEl.isSelected(0)));
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}