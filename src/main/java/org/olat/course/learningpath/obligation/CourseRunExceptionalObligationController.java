/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.learningpath.obligation;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;

/**
 * 
 * Initial date: Mar 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseRunExceptionalObligationController extends FormBasicController implements ExceptionalObligationController {
	
	private static final String[] OPERATORS = new String[] {"<", "<=", "=", "=>", ">"};

	private FormLayoutContainer runCont;
	private SingleSelection operatorEl;
	private TextElement operandEl;

	protected CourseRunExceptionalObligationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(LearningPathNodeConfigController.class, ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		CourseRunExceptionalObligation obligation = new CourseRunExceptionalObligation();
		obligation.setType(CourseRunExceptionalObligationHandler.TYPE);
		obligation.setOperator(operatorEl.getSelectedKey());
		obligation.setOperand(Integer.parseInt(operandEl.getValue()));
		return List.of(obligation);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		runCont = FormLayoutContainer.createCustomFormLayout("runCont", getTranslator(), velocity_root + "/course_run_config.html");
		runCont.setLabel("exceptional.obligation.course.run.execution", null);
		runCont.setMandatory(true);
		runCont.setRootForm(mainForm);
		formLayout.add(runCont);
		
		operatorEl = uifactory.addDropdownSingleselect("operator", null, runCont, OPERATORS, OPERATORS);
		operatorEl.select(operatorEl.getValue(0), true);
		
		operandEl = uifactory.addTextElement("operand", null, 10, null, runCont);
		operandEl.setDisplaySize(10);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setElementCssClass("o_button_group_right o_block_top");
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("config.exceptional.obligation.add.button", buttonCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		runCont.clearError();
		if (StringHelper.containsNonWhitespace(operandEl.getValue())) {
			try {
				int operand = Integer.parseInt(operandEl.getValue());
				if (operand < 1) {
					runCont.setErrorKey("form.error.positive.integer");
					allOk = false;
				}
			} catch (NumberFormatException e) {
				runCont.setErrorKey("form.error.positive.integer");
				allOk = false;
			}
		} else {
			runCont.setErrorKey("form.mandatory.hover");
			allOk = false;
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

}
