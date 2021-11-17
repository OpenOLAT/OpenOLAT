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
package org.olat.ims.qti21.ui.editor.testsexport;

import java.util.Collection;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExport1OptionsStep extends BasicStep {
	
	private final TestsExportContext exportContext;
	
	public TestsExport1OptionsStep(UserRequest ureq, TestsExportContext exportContext) {
		super(ureq);
		this.exportContext = exportContext;
		setNextStep(new TestsExport2CoverAttributesStep(ureq, exportContext));
		setI18nTitleAndDescr("wizard.options.title", "wizard.options.title");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new OptionsController(ureq, wControl, form, runContext);
	}
	
	class OptionsController extends StepFormBasicController {
		
		private TextElement prefixEl;
		private TextElement numOfTestsEl;
		private SingleSelection languageEl;
		private MultipleSelectionElement coverEl;
		
		@Autowired
		private I18nModule i18nModule;
		@Autowired
		private I18nManager i18nManager;
		
		public OptionsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormInfo("options.explanations");
			
			// number of test
			String numOfTests = exportContext.getNumOfTests() > 0 ? Integer.toString(exportContext.getNumOfTests()) : "";
			numOfTestsEl = uifactory.addTextElement("num.of.tests", 4, numOfTests, formLayout);
			numOfTestsEl.setMaxLength(4);
			numOfTestsEl.setDisplaySize(4);
			
			// languages
			SelectionValues languageValues = new SelectionValues();
			Collection<String> languageKeys = i18nModule.getEnabledLanguageKeys();
			for(String languageKey:languageKeys) {
				Locale locale = i18nManager.getLocaleOrDefault(languageKey);
				String language = locale.getDisplayLanguage(getLocale());
				languageValues.add(SelectionValues.entry(languageKey, language));
			}
			languageEl = uifactory.addRadiosVertical("languages", "language", formLayout, languageValues.keys(), languageValues.values());
			if(exportContext.getLanguageKey() != null && languageValues.containsKey(exportContext.getLanguageKey())) {
				languageEl.select(exportContext.getLanguageKey(), true);
			} else if(languageValues.size() > 0) {
				languageEl.select(languageValues.keys()[0], true);
			}
			
			String prefix = exportContext.getFilePrefix();
			prefixEl = uifactory.addTextElement("file.prefix", 42, prefix, formLayout);
			prefixEl.setDisplaySize(42);
			prefixEl.setMaxLength(42);
			prefixEl.setElementCssClass("form-inline");
			prefixEl.setTextAddOn("file.prefix.addon");
			prefixEl.setPlaceholderKey("file.prefix.placeholder", null);
			
			// cover: cover sheet, instructions
			SelectionValues coverValues = new SelectionValues();
			coverValues.add(SelectionValues.entry("cover.sheet", translate("cover.sheet")));
			coverValues.add(SelectionValues.entry("additional.sheet", translate("additional.sheet")));
			coverEl = uifactory.addCheckboxesVertical("covers", "covers", formLayout, coverValues.keys(), coverValues.values(), 1);
			coverEl.select("cover.sheet", exportContext.isCoverSheet());
			coverEl.select("covers", exportContext.isAdditionalSheet());
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			allOk &= validateMandatory(languageEl);
			allOk &= validateMandatory(prefixEl);
			
			numOfTestsEl.clearError();
			if(!StringHelper.containsNonWhitespace(numOfTestsEl.getValue())) {
				numOfTestsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else {
				try {
					Integer.parseInt(numOfTestsEl.getValue());
				} catch(Exception e) {
					numOfTestsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			}
			
			return allOk;
		}
		
		private boolean validateMandatory(SingleSelection el) {
			boolean allOk = true;
			
			el.clearError();
			if(!el.isOneSelected()) {
				el.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			return allOk;
		}
		
		private boolean validateMandatory(TextElement el) {
			boolean allOk = true;
			
			el.clearError();
			if(!StringHelper.containsNonWhitespace(el.getValue())) {
				el.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			return allOk;
		}

		@Override
		protected void formNext(UserRequest ureq) {		
			String num = numOfTestsEl.getValue();
			exportContext.setNumOfTests(Integer.parseInt(num));
			
			String languageKey = languageEl.getSelectedKey();
			Locale locale = i18nManager.getLocaleOrDefault(languageKey);
			exportContext.setLanguage(languageKey, locale);
			
			String prefix = prefixEl.getValue();
			exportContext.setFilePrefix(prefix);
			
			Collection<String> selectedCovers = coverEl.getSelectedKeys();
			exportContext.setCoverSheet(selectedCovers.contains("cover.sheet"));
			exportContext.setAdditionalSheet(selectedCovers.contains("additional.sheet"));
			
			if(exportContext.isCoverSheet()) {
				setNextStep(new TestsExport2CoverAttributesStep(ureq, exportContext));
			} else if(exportContext.isAdditionalSheet()) { 
				setNextStep(new TestsExport4InstructionsStep(ureq, exportContext));
			} else {
				setNextStep(new TestsExport5OverviewStep(ureq, exportContext));
			}
			
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
