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
package org.olat.course.learningpath.obligation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserPropertyExceptionalObligationController extends FormBasicController
		implements ExceptionalObligationController {
	
	private SingleSelection propertyNameEl;
	private TextElement valueEl;
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	
	public UserPropertyExceptionalObligationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		
		userPropertyHandlers = new ArrayList<>(userManager.getUserPropertyHandlersFor(UserPropertyExceptionalObligationHandler.USER_PROPS_ID, false));
		
		initForm(ureq);
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		String propertyName = propertyNameEl.getSelectedKey();
		String value = valueEl.getValue();
		
		UserPropertyExceptionalObligation exceptionalObligation = new UserPropertyExceptionalObligation();
		exceptionalObligation.setType(UserPropertyExceptionalObligationHandler.TYPE);
		exceptionalObligation.setPropertyName(propertyName);
		exceptionalObligation.setValue(value);
		return Collections.singletonList(exceptionalObligation);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues handlerKV = new SelectionValues();
		userPropertyHandlers.forEach(handler -> handlerKV.add(new SelectionValue(
				handler.getName(),
				translate(handler.i18nFormElementLabelKey()))));
		propertyNameEl = uifactory.addDropdownSingleselect("handler", "config.exceptional.obligation.user.property", formLayout, handlerKV.keys(), handlerKV.values());
		propertyNameEl.setMandatory(true);
		propertyNameEl.select(propertyNameEl.getKey(0), true);
		
		valueEl = uifactory.addTextElement("value", "config.exceptional.obligation.user.property.value", 128, null, formLayout);
		valueEl.setMandatory(true);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setElementCssClass("o_button_group_right o_block_top");
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("config.exceptional.obligation.add.button", buttonCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  super.validateFormLogic(ureq);
		
		propertyNameEl.clearError();
		if (!propertyNameEl.isOneSelected()) {
			propertyNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		valueEl.clearError();
		if (!StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
