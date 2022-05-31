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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntryAlternative;
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
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement solutionEl;
	private TextElement placeholderEl;
	private TextElement expectedLengthEl;
	private FormLink addFirstAlternative;
	private FormLink addMultipleAlternativesButton;
	private MultipleSelectionElement caseSensitiveEl;
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
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_gap_entry_form");
		
		String solution = interaction.getSolution();
		solutionEl = uifactory.addTextElement("fib.solution", "fib.solution", 256, solution, formLayout);
		solutionEl.setElementCssClass("o_sel_gap_entry_solution");
		solutionEl.setEnabled(!restrictedEdit && !readOnly);
		if(!StringHelper.containsNonWhitespace(solution)) {
			solutionEl.setFocus(true);
		}
		
		String placeholder = interaction.getPlaceholder();
		placeholderEl = uifactory.addTextElement("fib.placeholder", "fib.placeholder", 256, placeholder, formLayout);
		placeholderEl.setElementCssClass("o_sel_gap_entry_placeholder");
		placeholderEl.setEnabled(!restrictedEdit && !readOnly);
		
		String alternativesPage = velocity_root + "/fib_alternatives.html";
		alternativesCont = FormLayoutContainer.createCustomFormLayout("alternatives.list", getTranslator(), alternativesPage);
		alternativesCont.setRootForm(mainForm);
		formLayout.add(alternativesCont);
		alternativesCont.setLabel("fib.alternative", null);
		alternativesCont.setHelpText(translate("fib.alternative.help"));
		alternativesCont.setHelpUrlForManualPage("manual_user/tests/Test_question_types/#fib");
		alternativesCont.contextPut("alternatives", alternativeRows);
		
		addFirstAlternative = uifactory.addFormLink("add.first.alternative", "add", "", null, alternativesCont, Link.LINK | Link.NONTRANSLATED);
		addFirstAlternative.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		addFirstAlternative.setVisible(!restrictedEdit && !readOnly);
		
		addMultipleAlternativesButton = uifactory.addFormLink("add.multi.alternatives", "add.multi", "fib.add.multiple.alternatives", null, alternativesCont, Link.BUTTON);
		addMultipleAlternativesButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		addMultipleAlternativesButton.setVisible(!restrictedEdit && !readOnly);

		List<TextEntryAlternative> alternatives = interaction.getAlternatives();
		if(alternatives != null && !alternatives.isEmpty()) {
			for(TextEntryAlternative alternative:alternatives) {
				appendAlternative(alternative, null, false);
			}
		}
		
		Integer expectedLength = interaction.getExpectedLength();
		String expectedLengthStr = expectedLength == null ? null : expectedLength.toString();
		expectedLengthEl = uifactory.addTextElement("fib.expectedLength", "fib.expectedLength", 256, expectedLengthStr, formLayout);
		expectedLengthEl.setEnabled(!restrictedEdit && !readOnly);
		
		caseSensitiveEl = uifactory.addCheckboxesHorizontal("fib.caseSensitive", "fib.caseSensitive", formLayout, onKeys, new String[]{ "" });
		caseSensitiveEl.setEnabled(!restrictedEdit && !readOnly);
		if(interaction.isCaseSensitive()) {
			caseSensitiveEl.select(onKeys[0], true);
		}

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
		if(!restrictedEdit && !readOnly) {
			uifactory.addFormSubmitButton("submit", buttonsContainer);
		}
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
			addButton = uifactory.addFormLink("add.alternative." + count++, "add", "", null, alternativesCont, Link.NONTRANSLATED);
			addButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			addButton.setVisible(!restrictedEdit && !readOnly);
			alternativesCont.add(addButton);
		}
		
		FormLink removeButton = null;
		if(!restrictedEdit && !readOnly) {
			removeButton = uifactory.addFormLink("remove.alternative." + count++, "rm", "", null, alternativesCont, Link.NONTRANSLATED);
			removeButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
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
					expectedLengthEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				expectedLengthEl.setErrorKey("form.error.nointeger", null);
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
		if(source == addFirstAlternative) {
			TextEntryAlternative alternative = new TextEntryAlternative();
			if(interaction.getScore() != null) {
				alternative.setScore(interaction.getScore().doubleValue());
			}
			appendAlternative(alternative, null, true);
		} else if(source == addMultipleAlternativesButton) {
			doOpenAddVariants(ureq, addMultipleAlternativesButton);
		} else if(source instanceof FormLink) {
			for(AlternativeRow alternativeRow:alternativeRows) {
				if(alternativeRow.getRemoveButton() == source) {
					alternativeRows.remove(alternativeRow);
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
			int indexSeparator = val.indexOf(';');
			// Don't split single ;, or &auml;
			if(alternativeRows.size() == 1 && indexSeparator >= 0 && val.length() > 1 && indexSeparator != val.length() - 1) {
				String[] valArr = val.split("[;]");
				for(int i=0;i<valArr.length; i++) {
					if(i==0) {
						alternative.setAlternative(StringUtilities.trim(valArr[i]));
						alternatives.add(alternative);
					} else {
						TextEntryAlternative newAlternative = new TextEntryAlternative();
						newAlternative.setAlternative(StringUtilities.trim(valArr[i]));
						newAlternative.setScore(alternative.getScore());
						alternatives.add(newAlternative);
					}
				}
			} else {
				alternative.setAlternative(StringUtilities.trim(val));
				alternatives.add(alternative);
			}
		}
		interaction.setAlternatives(alternatives);
		interaction.setCaseSensitive(caseSensitiveEl.isAtLeastSelected(1));
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
