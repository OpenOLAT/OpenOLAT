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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.TextEntryAlternative;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;

import uk.ac.ed.ph.jqtiplus.internal.util.StringUtilities;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 24.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBTextEntrySettingsController extends FormBasicController {
	
	private static final String OPTION_CASE_SENSITIVITY_KEY = "case";
	
	private TextElement solutionEl;
	private TextElement placeholderEl;
	private TextElement expectedLengthEl;
	private FormToggle alternativesEl;
	private FormLink addMultipleAlternativesButton;
	private MultipleSelectionElement correctionsEl;
	private FormLayoutContainer alternativesCont;
	private final List<AlternativeRow> alternativeRows = new ArrayList<>();
	
	private int count = 0;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final TextEntry interaction;
	
	private FIBTextEntryAlternativesController alternativesCtrl;
	private CloseableCalloutWindowController alternativesCalloutCtrl;
	
	public FIBTextEntrySettingsController(UserRequest ureq, WindowControl wControl, TextEntry interaction,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.interaction = interaction;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_gap_entry_form");
		
		uifactory.addStaticTextElement("fib.input.type", translate("form.fib"), formLayout);
		
		String solution = interaction.getSolution();
		solutionEl = uifactory.addTextElement("fib.solution", "fib.solution", 256, solution, formLayout);
		solutionEl.setElementCssClass("o_sel_gap_entry_solution");
		solutionEl.setEnabled(!restrictedEdit && !readOnly);
		if(!StringHelper.containsNonWhitespace(solution)) {
			solutionEl.setFocus(true);
		}
		
		alternativesEl = uifactory.addToggleButton("add.alternative.toggle", "add.alternative.toggle", translate("on"), translate("off"), formLayout);
		alternativesEl.setEnabled(!restrictedEdit && !readOnly);
		alternativesEl.setHelpText(translate("fib.alternative.help"));
		alternativesEl.setHelpUrlForManualPage("manual_user/learningresources/Test_question_types/#fib");
		
		String alternativesPage = velocity_root + "/fib_alternatives.html";
		alternativesCont = uifactory.addCustomFormLayout("alternatives.list", null, alternativesPage, formLayout);
		alternativesCont.contextPut("alternatives", alternativeRows);
		
		addMultipleAlternativesButton = uifactory.addFormLink("add.multi.alternatives", "add.multi", "fib.add.multiple.alternatives", null, alternativesCont, Link.LINK);
		addMultipleAlternativesButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		addMultipleAlternativesButton.setVisible(!restrictedEdit && !readOnly);
		addMultipleAlternativesButton.setGhost(true);

		List<TextEntryAlternative> alternatives = interaction.getAlternatives();
		if(alternatives != null && !alternatives.isEmpty()) {
			for(TextEntryAlternative alternative:alternatives) {
				appendAlternative(alternative, null, false);
			}
		}
		
		FormLayoutContainer displayCont = uifactory.addDefaultFormLayout("display-options", null, formLayout);
		displayCont.setFormLayout("nolayout");
		displayCont.setFormTitle(translate("fib.display.title"));
		
		String placeholder = interaction.getPlaceholder();
		placeholderEl = uifactory.addTextElement("fib.placeholder", "fib.placeholder", 256, placeholder, displayCont);
		placeholderEl.setElementCssClass("o_sel_gap_entry_placeholder");
		placeholderEl.setEnabled(!restrictedEdit && !readOnly);
		
		Integer expectedLength = interaction.getExpectedLength();
		String expectedLengthStr = expectedLength == null ? null : expectedLength.toString();
		expectedLengthEl = uifactory.addTextElement("fib.expectedLength", "fib.expectedLength", 256, expectedLengthStr, displayCont);
		expectedLengthEl.setEnabled(!restrictedEdit && !readOnly);
		expectedLengthEl.setElementCssClass("form-inline");
		
		FormLayoutContainer optionsCont = uifactory.addDefaultFormLayout("options", null, formLayout);
		optionsCont.setFormLayout("nolayout");
		optionsCont.setFormTitle(translate("fib.options.title"));
		
		SelectionValues optionsPK = new SelectionValues();
		optionsPK.add(SelectionValues.entry(OPTION_CASE_SENSITIVITY_KEY, translate("fib.caseSensitive")));
		
		correctionsEl = uifactory.addCheckboxesHorizontal("fib.corrections", "fib.corrections", optionsCont, optionsPK.keys(), optionsPK.values());
		correctionsEl.setEnabled(!restrictedEdit && !readOnly);
		if(interaction.isCaseSensitive()) {
			correctionsEl.select(OPTION_CASE_SENSITIVITY_KEY, true);
		}

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		if(!restrictedEdit && !readOnly) {
			uifactory.addFormSubmitButton("submit", buttonsContainer);
		}
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
	}

	public String getSolution() {
		return interaction.getSolution();
	}
	
	public Identifier getResponseIdentifier() {
		return interaction.getResponseIdentifier();
	}
	
	private void appendAlternatives(List<String> newAlternatives) {
		Set<String> currentAlterantives = alternativeRows.stream()
				.map(row -> row.getAlternative().getAlternative())
				.collect(Collectors.toSet());
		
		for(String newAlternative:newAlternatives) {
			if(!currentAlterantives.contains(newAlternative)) {
				TextEntryAlternative alternative = new TextEntryAlternative();
				alternative.setAlternative(newAlternative);
				if(interaction.getScore() != null) {
					alternative.setScore(interaction.getScore().doubleValue());
				}
				appendAlternative(alternative, null, false);
			}
		}	
	}
	
	private void appendAlternative(TextEntryAlternative alternative, AlternativeRow previousRow, boolean focus) {
		String text = alternative.getAlternative();
		TextElement alternativeEl = uifactory.addTextElement("fib.alternative." + count++, "fib.alternative", 256, text, alternativesCont);
		alternativeEl.setDomReplacementWrapperRequired(false);
		alternativeEl.setEnabled(!restrictedEdit && !readOnly);
		
		if(focus) {
			solutionEl.setFocus(false);
			for(AlternativeRow row:alternativeRows) {
				row.getAlternativeEl().setFocus(false);
			}
			alternativeEl.setFocus(true);
		}

		FormLink addButton = null;
		if(!restrictedEdit && !readOnly) {
			addButton = uifactory.addFormLink("add.alternative." + count++, "add", "", null, alternativesCont, Link.BUTTON | Link.NONTRANSLATED);
			addButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			addButton.setVisible(!restrictedEdit && !readOnly);
			alternativesCont.add(addButton);
		}
		
		FormLink removeButton = null;
		if(!restrictedEdit && !readOnly) {
			removeButton = uifactory.addFormLink("remove.alternative." + count++, "rm", "", null, alternativesCont, Link.BUTTON | Link.NONTRANSLATED);
			removeButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
			removeButton.setVisible(!restrictedEdit && !readOnly);
			alternativesCont.add(removeButton);
		}
		
		AlternativeRow row = new AlternativeRow(alternative, alternativeEl, addButton, removeButton);
		if(previousRow == null) {
			alternativeRows.add(row);
		} else {
			int index = alternativeRows.indexOf(previousRow) + 1;
			if(index >= 0 && index < alternativeRows.size()) {
				alternativeRows.add(index, row);
			} else {
				alternativeRows.add(row);
			}
		}
		
		alternativesEl.toggle(!alternativeRows.isEmpty());
	}
	
	private void updateUI() {
		boolean hasAlternatives = alternativesEl.isOn();
		alternativesCont.setVisible(hasAlternatives);
		addMultipleAlternativesButton.setVisible(hasAlternatives);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
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

		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(alternativesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				appendAlternatives(alternativesCtrl.getAlternatives());
			}
			alternativesCalloutCtrl.deactivate();
			cleanUp();
		} else if(alternativesCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(alternativesCalloutCtrl);
		removeAsListenerAndDispose(alternativesCtrl);
		alternativesCalloutCtrl = null;
		alternativesCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == alternativesEl) {
			if(alternativesEl.isOn()) {
				doEnableAlternatives();
			}
			updateUI();
		} else if(source == addMultipleAlternativesButton) {
			doOpenAddVariants(ureq, addMultipleAlternativesButton);
		} else if(source instanceof FormLink) {
			for(AlternativeRow alternativeRow:alternativeRows) {
				if(alternativeRow.getRemoveButton() == source) {
					alternativeRows.remove(alternativeRow);
					alternativesEl.toggle(!alternativeRows.isEmpty());
					flc.setDirty(true);
					break;
				} else if(alternativeRow.getAddButton() == source) {
					TextEntryAlternative alternative = new TextEntryAlternative();
					if(interaction.getScore() != null) {
						alternative.setScore(interaction.getScore().doubleValue());
					}
					appendAlternative(alternative, alternativeRow, true);
					break;
				}
			}
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		interaction.setSolution(StringUtilities.trim(solutionEl.getValue()));
		interaction.setPlaceholder(placeholderEl.getValue());
		List<TextEntryAlternative> alternatives = new ArrayList<>(alternativeRows.size());
		for(AlternativeRow row:alternativeRows) {
			TextEntryAlternative alternative = row.getAlternative();
			String val = row.getAlternativeEl().getValue();
			alternative.setAlternative(StringUtilities.trim(val));
			alternatives.add(alternative);
		}
		interaction.setAlternatives(alternatives);
		
		Collection<String> selectedOptions = correctionsEl.getSelectedKeys();
		interaction.setCaseSensitive(selectedOptions.contains(OPTION_CASE_SENSITIVITY_KEY));
		if(StringHelper.containsNonWhitespace(expectedLengthEl.getValue())) {
			interaction.setExpectedLength(Integer.valueOf(expectedLengthEl.getValue()));
		} else {
			interaction.setExpectedLength(null);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doEnableAlternatives() {
		TextEntryAlternative alternative = new TextEntryAlternative();
		if(interaction.getScore() != null) {
			alternative.setScore(interaction.getScore().doubleValue());
		}
		appendAlternative(alternative, null, true);
	}
	
	private void doOpenAddVariants(UserRequest ureq, FormLink link) {
		alternativesCtrl = new FIBTextEntryAlternativesController(ureq, getWindowControl());
		listenTo(alternativesCtrl);
		
		alternativesCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				alternativesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(alternativesCalloutCtrl);
		alternativesCalloutCtrl.activate();
	}

	public class AlternativeRow {
		
		private final TextElement alternativeEl;
		private final FormLink addButton;
		private final FormLink removeButton;
		
		private final TextEntryAlternative alternative;
		
		public AlternativeRow(TextEntryAlternative alternative, TextElement alternativeEl,
				FormLink addButton, FormLink removeButton) {
			this.addButton = addButton;
			this.removeButton = removeButton;
			this.alternative = alternative;
			this.alternativeEl = alternativeEl;
		}
		
		public TextElement getAlternativeEl() {
			return alternativeEl;
		}
		
		public TextEntryAlternative getAlternative() {
			return alternative;
		}

		public FormLink getAddButton() {
			return addButton;
		}

		public FormLink getRemoveButton() {
			return removeButton;
		}
	}
}
