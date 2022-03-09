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

import java.math.BigDecimal;

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
import uk.ac.ed.ph.jqtiplus.types.Identifier;

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
	
	private final boolean restrictedEdit, readOnly;
	private final NumericalEntry interaction;
	
	public FIBNumericalEntrySettingsController(UserRequest ureq, WindowControl wControl, NumericalEntry interaction,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestEditorController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}
	
	public Identifier getResponseIdentifier() {
		return interaction.getResponseIdentifier();
	}
	
	public Double getSolution() {
		return interaction.getSolution();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_gap_numeric_form");
		
		Double solution = interaction.getSolution();
		String solString = solution == null ? "" : solution.toString();
		solutionEl = uifactory.addTextElement("fib.solution", "fib.solution", 256, solString, formLayout);
		solutionEl.setElementCssClass("o_sel_gap_numeric_solution");
		solutionEl.setEnabled(!restrictedEdit && !readOnly);
		if(!restrictedEdit && !readOnly && !StringHelper.containsNonWhitespace(solString)) {
			solutionEl.setFocus(true);
		}
		
		String placeholder = interaction.getPlaceholder();
		placeholderEl = uifactory.addTextElement("fib.placeholder", "fib.placeholder", 256, placeholder, formLayout);
		placeholderEl.setElementCssClass("o_sel_gap_numeric_placeholder");
		placeholderEl.setEnabled(!restrictedEdit && !readOnly);
		
		Integer expectedLength = interaction.getExpectedLength();
		String expectedLengthStr = expectedLength == null ? null : expectedLength.toString();
		expectedLengthEl = uifactory.addTextElement("fib.expectedLength", "fib.expectedLength", 256, expectedLengthStr, formLayout);
		expectedLengthEl.setEnabled(!restrictedEdit && !readOnly);
		
		String[] toleranceModeValues = new String[] {
			translate("fib.tolerance.mode.exact"), translate("fib.tolerance.mode.absolute"), translate("fib.tolerance.mode.relative")
		};
		toleranceModeEl = uifactory.addDropdownSingleselect("fib.tolerance.mode", "fib.tolerance.mode", formLayout, toleranceModeKeys, toleranceModeValues, null);
		toleranceModeEl.setEnabled(!restrictedEdit && !readOnly);
		toleranceModeEl.setHelpText(getToleranceHelp());
		toleranceModeEl.setHelpUrlForManualPage("manual_user/tests/Test_question_types/#ni");
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
		String lowerToleranceString;
		if(interaction.getToleranceMode() == ToleranceMode.ABSOLUTE) {
			if(lowerTolerance == null) {
				lowerToleranceString = "";
			} else if(solution != null) {
				BigDecimal solBig = BigDecimal.valueOf(solution);
				BigDecimal lowerToleranceBig = BigDecimal.valueOf(lowerTolerance);
				lowerToleranceString = solBig.subtract(lowerToleranceBig).toString();
			} else {
				lowerToleranceString = lowerTolerance.toString();
			}
		} else {
			lowerToleranceString = lowerTolerance == null ? "" : lowerTolerance.toString();
		}
		lowerToleranceEl = uifactory.addTextElement("fib.tolerance.low", "fib.tolerance.low", 8, lowerToleranceString, formLayout);
		lowerToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
		lowerToleranceEl.setElementCssClass("o_sel_gap_numeric_lower_bound");
		lowerToleranceEl.setEnabled(!restrictedEdit && !readOnly);
		
		Double upperTolerance = interaction.getUpperTolerance();
		String upperToleranceString;
		if(interaction.getToleranceMode() == ToleranceMode.ABSOLUTE) {
			if(upperTolerance == null) {
				upperToleranceString = "";
			} else if(solution != null) {
				BigDecimal solBig = BigDecimal.valueOf(solution);
				BigDecimal upperToleranceBig = BigDecimal.valueOf(upperTolerance);
				upperToleranceString = solBig.add(upperToleranceBig).toString();
			} else {
				upperToleranceString = upperTolerance.toString();
			}
		} else {
			upperToleranceString = upperTolerance == null ? "" : upperTolerance.toString();
		}
		upperToleranceEl = uifactory.addTextElement("fib.tolerance.up", "fib.tolerance.up", 8, upperToleranceString, formLayout);
		upperToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
		upperToleranceEl.setElementCssClass("o_sel_gap_numeric_upper_bound");
		upperToleranceEl.setEnabled(!restrictedEdit && !readOnly);
		updateToleranceUpAndLow();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
		if(!restrictedEdit && !readOnly) {
			uifactory.addFormSubmitButton("submit", buttonsContainer);
		}
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
				lowerToleranceEl.setExampleKey("fib.tolerance.mode.relative.low.example", null);
				upperToleranceEl.setExampleKey("fib.tolerance.mode.relative.up.example", null);
			} else if(mode == ToleranceMode.ABSOLUTE) {
				lowerToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
				upperToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

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
			if(mode == ToleranceMode.ABSOLUTE) {
				allOk &= validateDouble(lowerToleranceEl, false);
				allOk &= validateDouble(upperToleranceEl, false);
				
				if(allOk) {
					BigDecimal solution = new BigDecimal(solutionEl.getValue());
					BigDecimal upperBound = new BigDecimal(upperToleranceEl.getValue());
					BigDecimal lowerBound = new BigDecimal(lowerToleranceEl.getValue());
					if(upperBound.subtract(solution).compareTo(new BigDecimal("0.0")) < 0) {
						upperToleranceEl.setErrorKey("error.upper.tolerance", null);
						allOk &= false;
					}
					if(solution.subtract(lowerBound).compareTo(new BigDecimal("0.0")) < 0) {
						lowerToleranceEl.setErrorKey("error.lower.tolerance", null);
						allOk &= false;
					}
				}
			} else if(mode == ToleranceMode.RELATIVE) {
				allOk &= validateDouble(lowerToleranceEl, true);
				allOk &= validateDouble(upperToleranceEl, true);
			}
		}

		return allOk;
	}
	
	/**
	 * Check if the value is a positive one.
	 * 
	 * @param element The text element to validate
	 * @return true if the text is a positive double
	 */
	private boolean validateDouble(TextElement element, boolean onlyPositive) {
		boolean allOk = true;
		
		element.clearError();
		if(StringHelper.containsNonWhitespace(element.getValue())) {
			try {
				double val = Double.parseDouble(element.getValue());
				if(val < 0.0d && onlyPositive) {
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
		if(readOnly) return;
		
		interaction.setSolution(Double.valueOf(solutionEl.getValue()));
		interaction.setPlaceholder(placeholderEl.getValue());
		if(StringHelper.containsNonWhitespace(expectedLengthEl.getValue())) {
			interaction.setExpectedLength(Integer.valueOf(expectedLengthEl.getValue()));
		} else {
			interaction.setExpectedLength(null);
		}
		String toleranceMode = toleranceModeEl.getSelectedKey();
		interaction.setToleranceMode(ToleranceMode.valueOf(toleranceMode));
		
		if(interaction.getToleranceMode() == ToleranceMode.ABSOLUTE) {
			BigDecimal solution = new BigDecimal(solutionEl.getValue());
			BigDecimal upperBound = new BigDecimal(upperToleranceEl.getValue());
			BigDecimal lowerBound = new BigDecimal(lowerToleranceEl.getValue());
			String upperToleranceString = upperBound.subtract(solution).toString();
			String lowerToleranceString = solution.subtract(lowerBound).toString();
			interaction.setLowerTolerance(Double.parseDouble(lowerToleranceString));
			interaction.setUpperTolerance(Double.parseDouble(upperToleranceString));
		} else if(interaction.getToleranceMode() == ToleranceMode.RELATIVE) {
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
