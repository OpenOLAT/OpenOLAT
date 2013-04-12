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
package org.olat.course.member.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.member.wizard.ImportMemberOverviewIdentitiesController.DataType;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberByUsernamesController extends StepFormBasicController {
	private final boolean typesEnabled;
	private TextElement idata;
	private SingleSelection typedata;
	
	private String[] typeKeys = new String[]{
		DataType.username.name(), DataType.email.name(), DataType.institutionalUserIdentifier.name()
	};

	public ImportMemberByUsernamesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, boolean typesEnabled) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.typesEnabled = typesEnabled;
		
		initForm (ureq);
	}

	public boolean validate() {
		return !idata.isEmpty("form.legende.mandatory");
	}

	public String getLoginsString() {
		return idata.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String logins = idata.getValue();
		addToRunContext("logins", logins);
		if(typedata != null && typedata.isOneSelected()) {
			addToRunContext("dataType", DataType.values()[typedata.getSelected()]);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(typesEnabled) {
			String[] theValues = new String[]{
					"Username", "Email", "MarkId"
			};
			typedata = uifactory.addDropdownSingleselect("form.addusers.type", formLayout, typeKeys, theValues, null);
		}

		idata = uifactory.addTextAreaElement("addusers", "form.addusers", -1, 15, 40, true, " ", formLayout);
		idata.setExampleKey ("form.names.example", null);
	}

	@Override
	protected void doDispose() {
		//
	}
}