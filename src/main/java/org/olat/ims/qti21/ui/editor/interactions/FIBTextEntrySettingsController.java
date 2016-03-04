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
package org.olat.ims.qti21.ui.editor.interactions;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;

/**
 * 
 * Initial date: 24.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBTextEntrySettingsController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement solutionEl;
	private TextElement placeholderEl;
	private TextElement alternativeEl;
	private TextElement expectedLengthEl;
	private MultipleSelectionElement caseSensitiveEl;
	
	private final TextEntry interaction;
	
	public FIBTextEntrySettingsController(UserRequest ureq, WindowControl wControl, TextEntry interaction) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.interaction = interaction;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String solution = interaction.getSolution();
		solutionEl = uifactory.addTextElement("fib.solution", "fib.solution", 256, solution, formLayout);
		String placeholder = interaction.getPlaceholder();
		placeholderEl = uifactory.addTextElement("fib.placeholder", "fib.placeholder", 256, placeholder, formLayout);
		String alternatives = interaction.alternativesToString();
		alternativeEl = uifactory.addTextElement("fib.alternative", "fib.alternative", 256, alternatives, formLayout);
		
		Integer expectedLength = interaction.getExpectedLength();
		String expectedLengthStr = expectedLength == null ? null : expectedLength.toString();
		expectedLengthEl = uifactory.addTextElement("fib.expectedLength", "fib.expectedLength", 256, expectedLengthStr, formLayout);

		caseSensitiveEl = uifactory.addCheckboxesHorizontal("fib.caseSensitive", "fib.caseSensitive", formLayout, onKeys, new String[]{ "" });
		if(interaction.isCaseSensitive()) {
			caseSensitiveEl.select(onKeys[0], true);
		}

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		solutionEl.clearError();
		if(!StringHelper.containsNonWhitespace(solutionEl.getValue())) {
			solutionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		expectedLengthEl.clearError();
		if(StringHelper.containsNonWhitespace(expectedLengthEl.getValue())) {
			if(StringHelper.isLong(expectedLengthEl.getValue())) {
				try {
					Integer.parseInt(expectedLengthEl.getValue());
				} catch(NumberFormatException e) {
					expectedLengthEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				expectedLengthEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		interaction.setSolution(solutionEl.getValue());
		interaction.setPlaceholder(placeholderEl.getValue());
		interaction.stringToAlternatives(alternativeEl.getValue());
		interaction.setCaseSensitive(caseSensitiveEl.isAtLeastSelected(1));
		if(StringHelper.containsNonWhitespace(expectedLengthEl.getValue())) {
			interaction.setExpectedLength(new Integer(expectedLengthEl.getValue()));
		} else {
			interaction.setExpectedLength(null);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
