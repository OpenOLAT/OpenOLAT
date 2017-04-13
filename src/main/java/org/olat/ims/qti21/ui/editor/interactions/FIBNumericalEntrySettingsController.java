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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;

/**
 * 
 * Initial date: 24.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBNumericalEntrySettingsController extends FormBasicController {
	
	private static final String[] toleranceModeKeys = new String[]{
		ToleranceMode.EXACT.name(), ToleranceMode.ABSOLUTE.name(), ToleranceMode.RELATIVE.name()
	};
	
	private TextElement solutionEl;
	private TextElement placeholderEl;
	private TextElement expectedLengthEl;
	private SingleSelection toleranceModeEl;
	private TextElement lowerToleranceEl, upperToleranceEl;
	
	private final boolean restrictedEdit;
	private final NumericalEntry interaction;
	
	public FIBNumericalEntrySettingsController(UserRequest ureq, WindowControl wControl, NumericalEntry interaction, boolean restrictedEdit) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestEditorController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Double solution = interaction.getSolution();
		String solString = solution == null ? "" : solution.toString();
		solutionEl = uifactory.addTextElement("fib.solution", "fib.solution", 256, solString, formLayout);
		solutionEl.setEnabled(!restrictedEdit);
		String placeholder = interaction.getPlaceholder();
		placeholderEl = uifactory.addTextElement("fib.placeholder", "fib.placeholder", 256, placeholder, formLayout);
		placeholderEl.setEnabled(!restrictedEdit);
		
		Integer expectedLength = interaction.getExpectedLength();
		String expectedLengthStr = expectedLength == null ? null : expectedLength.toString();
		expectedLengthEl = uifactory.addTextElement("fib.expectedLength", "fib.expectedLength", 256, expectedLengthStr, formLayout);
		expectedLengthEl.setEnabled(!restrictedEdit);
		
		String[] toleranceModeValues = new String[] {
			translate("fib.tolerance.mode.exact"), translate("fib.tolerance.mode.absolute"), translate("fib.tolerance.mode.relative")
		};
		toleranceModeEl = uifactory.addDropdownSingleselect("fib.tolerance.mode", "fib.tolerance.mode", formLayout, toleranceModeKeys, toleranceModeValues, null);
		toleranceModeEl.setEnabled(!restrictedEdit);
		toleranceModeEl.setHelpText(getToleranceHelp());
		toleranceModeEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#details_testeditor_fragetypen_ni");
		if(interaction.getToleranceMode() != null) {
			for(String toleranceModeKey:toleranceModeKeys) {
				if(toleranceModeKey.equals(interaction.getToleranceMode().name())) {
					toleranceModeEl.select(toleranceModeKey, true);
				}
			}
		}
		if(!toleranceModeEl.isOneSelected()) {
			toleranceModeEl.select(toleranceModeKeys[0], true);
		}
		toleranceModeEl.addActionListener(FormEvent.ONCHANGE);
		
		Double lowerTolerance = interaction.getLowerTolerance();
		String lowerToleranceString = lowerTolerance == null ? "" : lowerTolerance.toString();
		lowerToleranceEl = uifactory.addTextElement("fib.tolerance.low", "fib.tolerance.low", 8, lowerToleranceString, formLayout);
		lowerToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
		lowerToleranceEl.setEnabled(!restrictedEdit);
		
		Double upperTolerance = interaction.getUpperTolerance();
		String upperToleranceString = upperTolerance == null ? "" : upperTolerance.toString();
		upperToleranceEl = uifactory.addTextElement("fib.tolerance.up", "fib.tolerance.up", 8, upperToleranceString, formLayout);
		upperToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
		upperToleranceEl.setEnabled(!restrictedEdit);
		updateToleranceUpAndLow();

		
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		if(!restrictedEdit) {
			uifactory.addFormSubmitButton("submit", buttonsContainer);
		}
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
	}
	
	private String getToleranceHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul class='list-unstyled'>")
		  .append("<li><strong>").append(translate("fib.tolerance.mode.exact")).append(":</strong> ").append(translate("fib.tolerance.mode.exact.help"))
		  .append("<li><strong>").append(translate("fib.tolerance.mode.absolute")).append(":</strong> ").append(translate("fib.tolerance.mode.absolute.help"))
		  .append("<li><strong>").append(translate("fib.tolerance.mode.relative")).append(":</strong> ").append(translate("fib.tolerance.mode.relative.help"))
		  .append("</ul>");
		return sb.toString();
	}
	
	private void updateToleranceUpAndLow() {
		if(toleranceModeEl.isOneSelected()) {
			String selectedKey = toleranceModeEl.getSelectedKey();
			ToleranceMode mode = ToleranceMode.valueOf(selectedKey);
			boolean visible = mode == ToleranceMode.ABSOLUTE || mode == ToleranceMode.RELATIVE;
			lowerToleranceEl.setVisible(visible);
			upperToleranceEl.setVisible(visible);
			if(mode == ToleranceMode.RELATIVE) {
				lowerToleranceEl.setExampleKey("fib.tolerance.mode.relative.example", null);
				upperToleranceEl.setExampleKey("fib.tolerance.mode.relative.example", null);
			} else if(mode == ToleranceMode.ABSOLUTE) {
				lowerToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
				upperToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		solutionEl.clearError();
		if(StringHelper.containsNonWhitespace(solutionEl.getValue())) {
			try {
				Double.parseDouble(solutionEl.getValue());
			} catch (NumberFormatException e) {
				solutionEl.setErrorKey("error.double", null);
				allOk &= false;
			}
		} else {
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
		
		toleranceModeEl.clearError();
		lowerToleranceEl.clearError();
		upperToleranceEl.clearError();
		if(!toleranceModeEl.isOneSelected()) {
			toleranceModeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			String selectedKey = toleranceModeEl.getSelectedKey();
			ToleranceMode mode = ToleranceMode.valueOf(selectedKey);
			if(mode == ToleranceMode.ABSOLUTE || mode == ToleranceMode.RELATIVE) {
				allOk &= validateDouble(lowerToleranceEl);
				allOk &= validateDouble(upperToleranceEl);
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}
	
	/**
	 * Check if the value is a positive one.
	 * 
	 * @param element The text element to validate
	 * @return true if the text is a positive double
	 */
	private boolean validateDouble(TextElement element) {
		boolean allOk = true;
		
		element.clearError();
		if(StringHelper.containsNonWhitespace(element.getValue())) {
			try {
				double val = Double.parseDouble(element.getValue());
				if(val < 0.0d) {
					element.setErrorKey("error.positive.double", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				element.setErrorKey("error.double", null);
				allOk &= false;
			}
		} else {
			element.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == toleranceModeEl) {
			updateToleranceUpAndLow();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		interaction.setSolution(new Double(solutionEl.getValue()));
		interaction.setPlaceholder(placeholderEl.getValue());
		if(StringHelper.containsNonWhitespace(expectedLengthEl.getValue())) {
			interaction.setExpectedLength(new Integer(expectedLengthEl.getValue()));
		} else {
			interaction.setExpectedLength(null);
		}
		String toleranceMode = toleranceModeEl.getSelectedKey();
		interaction.setToleranceMode(ToleranceMode.valueOf(toleranceMode));
		
		if(interaction.getToleranceMode() == ToleranceMode.ABSOLUTE || interaction.getToleranceMode() == ToleranceMode.RELATIVE) {
			interaction.setLowerTolerance(Double.parseDouble(lowerToleranceEl.getValue()));
			interaction.setUpperTolerance(Double.parseDouble(upperToleranceEl.getValue()));
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
