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
package org.olat.admin.sysinfo;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.LogFileParser;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ErrorSearchController extends FormBasicController {

	private TextElement errorNumberEl;
	private DateChooser dateChooserEl;
	private VelocityContainer errorCont;
	
	public ErrorSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "errors");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("error.title");

		FormLayoutContainer fieldsCont = FormLayoutContainer.createDefaultFormLayout("fields", getTranslator());
		formLayout.add(fieldsCont);
		formLayout.add("fields", fieldsCont);
		
		String lastError = "-";
		uifactory.addStaticTextElement("error.last", lastError, fieldsCont);
		errorNumberEl = uifactory.addTextElement("error.number", "error.number", 32, "", fieldsCont);
		dateChooserEl = uifactory.addDateChooser("error.date", "error.date", null, fieldsCont);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		fieldsCont.add(buttonCont);
		uifactory.addFormSubmitButton("search", "error.retrieve", buttonCont);
		
		if(formLayout instanceof FormLayoutContainer) {
			errorCont = createVelocityContainer("error_list");
			((FormLayoutContainer)formLayout).put("errors", errorCont);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String errorNr = errorNumberEl.getValue();
		Date date = dateChooserEl.getDate();
		errorCont.contextPut("errormsgs", LogFileParser.getError(errorNr, date, true));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		errorNumberEl.clearError();
		if(!StringHelper.containsNonWhitespace(errorNumberEl.getValue())) {
			errorNumberEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}
}