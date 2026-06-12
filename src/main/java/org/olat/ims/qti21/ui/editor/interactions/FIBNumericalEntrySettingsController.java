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
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.NumericalEntry;
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
	
	private TextElement solutionEl;
	private TextElement placeholderEl;
	private TextElement expectedLengthEl;
	private FormToggle toleranceEl;
	private SingleSelection toleranceModeEl;
	private TextElement lowerToleranceEl;
	private TextElement upperToleranceEl;
	private FormLayoutContainer toleranceCont;
	
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
		
		uifactory.addStaticTextElement("fib.input.type", translate("form.numerical"), formLayout);
		
		Double solution = interaction.getSolution();
		String solString = solution == null ? "" : solution.toString();
		solutionEl = uifactory.addTextElement("fib.solution", "fib.solution", 256, solString, formLayout);
		solutionEl.setElementCssClass("o_sel_gap_numeric_solution form-inline");
		solutionEl.setEnabled(!restrictedEdit && !readOnly);
		if(!restrictedEdit && !readOnly && !StringHelper.containsNonWhitespace(solString)) {
			solutionEl.setFocus(true);
		}
		
		toleranceEl = uifactory.addToggleButton("fib.tolerance", "fib.tolerance", translate("on"), translate("off"), formLayout);
		toleranceEl.toggle(interaction.getToleranceMode() != null && interaction.getToleranceMode() != ToleranceMode.EXACT);
		toleranceEl.setElementCssClass("o_sel_gap_numerical_tolerance_enable");
		toleranceEl.setHelpText(getToleranceHelp());
		toleranceEl.setHelpUrlForManualPage("manual_user/learningresources/Test_question_types/#ni");
		toleranceEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues tolerancePK = new SelectionValues();
		tolerancePK.add(SelectionValues.entry(ToleranceMode.ABSOLUTE.name(), translate("fib.tolerance.mode.absolute")));
		tolerancePK.add(SelectionValues.entry(ToleranceMode.RELATIVE.name(), translate("fib.tolerance.mode.relative")));
		toleranceModeEl = uifactory.addRadiosHorizontal("fib.tolerance.mode", "fib.tolerance.mode", formLayout,
				tolerancePK.keys(), tolerancePK.values());
		toleranceModeEl.setElementCssClass("o_sel_gap_numerical_tolerance_mode");
		toleranceModeEl.setEnabled(!restrictedEdit && !readOnly);
		
		if(interaction.getToleranceMode() == ToleranceMode.ABSOLUTE) {
			toleranceModeEl.select(ToleranceMode.ABSOLUTE.name(), true);
		} else if(interaction.getToleranceMode() == ToleranceMode.RELATIVE) {
			toleranceModeEl.select(ToleranceMode.RELATIVE.name(), true);
		} else if(!toleranceModeEl.isOneSelected()) {
			toleranceModeEl.select(ToleranceMode.ABSOLUTE.name(), true);
		}
		toleranceModeEl.addActionListener(FormEvent.ONCHANGE);
		
		String page = velocity_root + "/fib_tolerance.html";
		toleranceCont = uifactory.addCustomFormLayout("tolerance-up-low", null, page, formLayout);
		toleranceCont.setFormLayout("nolayout");
		
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
		lowerToleranceEl = uifactory.addTextElement("fib.tolerance.low", "fib.tolerance.low", 8, lowerToleranceString, toleranceCont);
		lowerToleranceEl.setDomReplacementWrapperRequired(false);
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
		upperToleranceEl = uifactory.addTextElement("fib.tolerance.up", "fib.tolerance.up", 8, upperToleranceString, toleranceCont);
		upperToleranceEl.setDomReplacementWrapperRequired(false);
		upperToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
		upperToleranceEl.setElementCssClass("o_sel_gap_numeric_upper_bound");
		upperToleranceEl.setEnabled(!restrictedEdit && !readOnly);
		updateToleranceUpAndLow();
		
		FormLayoutContainer displayCont = uifactory.addDefaultFormLayout("display-options", null, formLayout);
		displayCont.setFormLayout("nolayout");
		displayCont.setFormTitle(translate("fib.display.title"));
		
		String placeholder = interaction.getPlaceholder();
		placeholderEl = uifactory.addTextElement("fib.placeholder", "fib.placeholder", 256, placeholder, displayCont);
		placeholderEl.setElementCssClass("o_sel_gap_numeric_placeholder");
		placeholderEl.setEnabled(!restrictedEdit && !readOnly);
		
		Integer expectedLength = interaction.getExpectedLength();
		String expectedLengthStr = expectedLength == null ? null : expectedLength.toString();
		expectedLengthEl = uifactory.addTextElement("fib.expectedLength", "fib.expectedLength", 256, expectedLengthStr, displayCont);
		expectedLengthEl.setEnabled(!restrictedEdit && !readOnly);
		expectedLengthEl.setElementCssClass("form-inline");

		// Submit Button
		FormLayoutContainer buttonsContainer = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		if(!restrictedEdit && !readOnly) {
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
		boolean tolerance = toleranceEl.isOn();
		toleranceModeEl.setVisible(tolerance);
		toleranceCont.setVisible(tolerance);
		lowerToleranceEl.setVisible(tolerance);
		upperToleranceEl.setVisible(tolerance);

		if(!toleranceModeEl.isOneSelected()) {
			toleranceModeEl.select(ToleranceMode.ABSOLUTE.name(), true);
		}
		if(toleranceModeEl.isVisible() && toleranceModeEl.isOneSelected()) {
			String selectedKey = toleranceModeEl.getSelectedKey();
			ToleranceMode mode = ToleranceMode.valueOf(selectedKey);
			if(mode == ToleranceMode.RELATIVE) {
				lowerToleranceEl.setExampleKey("fib.tolerance.mode.relative.low.example", null);
				lowerToleranceEl.setTextAddOn("%", false);
				upperToleranceEl.setTextAddOn("%", false);
			} else if(mode == ToleranceMode.ABSOLUTE) {
				lowerToleranceEl.setExampleKey("fib.tolerance.mode.absolute.example", null);
				lowerToleranceEl.setTextAddOn("", false);
				upperToleranceEl.setTextAddOn("", false);
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
				solutionEl.setErrorKey("error.double");
				allOk &= false;
			}
		} else {
			solutionEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		expectedLengthEl.clearError();
		if(StringHelper.containsNonWhitespace(expectedLengthEl.getValue())) {
			if(StringHelper.isLong(expectedLengthEl.getValue())) {
				try {
					Integer.parseInt(expectedLengthEl.getValue());
				} catch(NumberFormatException e) {
					expectedLengthEl.setErrorKey("form.error.nointeger");
					allOk &= false;
				}
			} else {
				expectedLengthEl.setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		}
		
		toleranceModeEl.clearError();
		lowerToleranceEl.clearError();
		upperToleranceEl.clearError();
		if(!toleranceEl.isOn()) {
			//Do nothing
		} else if(!toleranceModeEl.isOneSelected()) {
			toleranceModeEl.setErrorKey("form.legende.mandatory");
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
						upperToleranceEl.setErrorKey("error.upper.tolerance");
						allOk &= false;
					}
					if(solution.subtract(lowerBound).compareTo(new BigDecimal("0.0")) < 0) {
						lowerToleranceEl.setErrorKey("error.lower.tolerance");
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
					element.setErrorKey("error.positive.double");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				element.setErrorKey("error.double");
				allOk &= false;
			}
		} else {
			element.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(toleranceEl == source || toleranceModeEl == source) {
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
		
		ToleranceMode toleranceMode = ToleranceMode.EXACT;
		if(toleranceEl.isOn() && toleranceModeEl.isOneSelected()) {
			toleranceMode = ToleranceMode.valueOf(toleranceModeEl.getSelectedKey());
		}
		
		interaction.setToleranceMode(toleranceMode);
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
