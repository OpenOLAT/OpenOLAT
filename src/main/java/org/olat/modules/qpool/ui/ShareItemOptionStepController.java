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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 30.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShareItemOptionStepController extends StepFormBasicController {

	private static final String[] keys = {"yes","no"};
	private SingleSelection editableEl;
	
	public ShareItemOptionStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {	
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "share_options");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("shares", "");
		}

		FormLayoutContainer mailCont = FormLayoutContainer.createDefaultFormLayout("editable", getTranslator());
		formLayout.add(mailCont);
		String[] values = new String[]{
				translate("yes"),
				translate("no")
		};
		editableEl = uifactory.addRadiosVertical("share.editable", "share.editable", mailCont, keys, values);
		editableEl.select("no", true);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		addToRunContext("editable", Boolean.valueOf(editableEl.isSelected(0)));
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
