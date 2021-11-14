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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 26.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class SelectOrCreateSectionStep extends BasicStep {

	public SelectOrCreateSectionStep(UserRequest ureq) {
		super(ureq);
		setNextStep(Step.NOSTEP);
		setI18nTitleAndDescr("select.section.title", null);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new SelectOrCreateSectionStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class SelectOrCreateSectionStepController extends StepFormBasicController {

		private static final String NEW_SECTION = "create.new.section";
		
		private String[] sectionSelectionKeys;
		private String[] sectionSelectionValues;
		
		private PortfolioImportEntriesContext context; 
		private List<Section> sections;
		private boolean sectionsExist;
		
		private SingleSelection sectionSelectionEl;
		
		private SectionEditController sectionController;
		
		@Autowired
		private PortfolioService portfolioService;
		
		public SelectOrCreateSectionStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);		
			
			context = (PortfolioImportEntriesContext) runContext.get(PortfolioImportEntriesContext.CONTEXT_KEY);
			sections = context.getCurrentBinder() != null ? portfolioService.getSections(context.getCurrentBinder()) : new ArrayList<>();
			sectionsExist = !sections.isEmpty();
			
			sectionSelectionKeys = new String[sections.size() + (sectionsExist ? 2 : 1)];
			sectionSelectionValues = new String[sections.size() + (sectionsExist ? 2 : 1)];
			
			for (int i = 0; i < sections.size(); i++) {
				sectionSelectionKeys[i] = sections.get(i).getKey().toString();
				sectionSelectionValues[i] = sections.get(i).getTitle();
			}
			
			if (sectionsExist) {
				sectionSelectionKeys[sections.size()] = SingleSelection.SEPARATOR;
				sectionSelectionValues[sections.size()] = SingleSelection.SEPARATOR;
			}
			
			sectionSelectionKeys[sections.size() + (sectionsExist ? 1 : 0)] = NEW_SECTION;
			sectionSelectionValues[sections.size() + (sectionsExist ? 1 : 0)] = translate(NEW_SECTION);
			
			context.setNewSectionTitlePlaceHolder(translate("section"));
			
			sectionController = new SectionEditController(ureq, wControl, rootForm, context);
			initForm(ureq);
			loadData();
		}

		@Override
		public void dispose() {
			// Prevent dispose because of rich text element
		}

		@Override
		protected void formOK(UserRequest ureq) {
			if (sectionSelectionEl != null && !sectionSelectionEl.getSelectedKey().equals(NEW_SECTION)) {
				context.setCurrentSection(sections.stream().filter(section -> section.getKey().toString().equals(sectionSelectionEl.getSelectedKey())).findFirst().orElse(null));		
			}
			
			sectionController.saveDataInWizardContext();
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);	
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("select.section.desc");
			
			if (sectionsExist) {
				FormLayoutContainer elementsWrapper = FormLayoutContainer.createDefaultFormLayout_2_10("elementsWrapper", getTranslator());
				formLayout.add(elementsWrapper);
				sectionSelectionEl = uifactory.addDropdownSingleselect("sectionSelection", "new.section.title", elementsWrapper, sectionSelectionKeys, sectionSelectionValues);
				sectionSelectionEl.addActionListener(FormEvent.ONCHANGE);
			} else {
				// Show create new section controller
				formLayout.add("sectionController", sectionController.getInitialFormItem());
			}			
		}
		
		private void loadData() {
			if (sectionsExist && context.getCurrentSection() != null) {
				sectionSelectionEl.select(context.getCurrentSection().getKey().toString(), true);
			}
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == sectionSelectionEl) {
				if (sectionSelectionEl.getSelectedKey().equals(NEW_SECTION)) {
					flc.add("sectionController", sectionController.getInitialFormItem());
				} else {
					flc.remove("sectionController");
				}
			}
		}
	}
}
