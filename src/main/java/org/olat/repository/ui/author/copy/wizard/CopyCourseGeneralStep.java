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
package org.olat.repository.ui.author.copy.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.components.util.KeyValues.KeyValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.author.copy.wizard.dates.GeneralDatesStep;
import org.olat.repository.ui.settings.RepositoryEntryLifecycleController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 07.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseGeneralStep extends BasicStep {

	private final CopyCourseContext context;
	private final CopyCourseSteps steps;
	
	public CopyCourseGeneralStep(UserRequest ureq, CopyCourseSteps steps, CopyCourseContext context) {
		super(ureq);
		
		this.steps = steps;
		this.context = context;
		
		setI18nTitleAndDescr("general.title", null);
		
		steps.setEditDates(context.hasDateDependantNodes());
		
		setNextStep(GeneralDatesStep.create(ureq, null, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(CopyCourseSteps.CONTEXT_KEY, steps);
		stepsRunContext.put(CopyCourseContext.CONTEXT_KEY, context);
		
		return new GeneralStepController(ureq, windowControl, form, stepsRunContext);
	}

	private class GeneralStepController extends StepFormBasicController {

		private static final String CUSTOM_MODE = "wizard.mode.custom";
		private static final String AUTOMATIC_MODE = "wizard.mode.automatic";
		
		private TextElement externalRefEl;
		private TextElement displayNameEl;
		
		private RepositoryEntryLifecycleController lifecycleController;
		
		private SingleSelection copyModeEl;
		
		@Autowired
		private RepositoryService repositoryService;
		
		public GeneralStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
			
			lifecycleController = new RepositoryEntryLifecycleController(ureq, getWindowControl(), context.getRepositoryEntry(), rootForm);
			listenTo(lifecycleController);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			displayNameEl.clearError();
			if (!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
				displayNameEl.setErrorKey("input.mandatory", null);
			}
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setDisplayName(displayNameEl.getValue());
			context.setExternalRef(externalRefEl.getValue());
			
			if (lifecycleController.saveToContext(ureq, context)) {
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FormItemContainer referenceAndTitleLayout = FormLayoutContainer.createDefaultFormLayout_2_10("refernceAndTitleLayout", getTranslator());
			referenceAndTitleLayout.setRootForm(mainForm);
			formLayout.add(referenceAndTitleLayout);
			
			// Course name
			displayNameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, context.getRepositoryEntry().getDisplayname() + " " + translate("copy.suffix"), referenceAndTitleLayout);
			displayNameEl.setDisplaySize(30);
			displayNameEl.setMandatory(true);
			displayNameEl.addActionListener(FormEvent.ONCHANGE);
			
			// Course reference
			externalRefEl = uifactory.addTextElement("cif.externalref", "cif.externalref", 255, context.getRepositoryEntry().getExternalRef(), referenceAndTitleLayout);
			externalRefEl.setHelpText(translate("cif.externalref.hover"));
			externalRefEl.setHelpUrlForManualPage("Set up info page");
			externalRefEl.addActionListener(FormEvent.ONCHANGE);
			
			// Spacer
			uifactory.addSpacerElement("space_1", formLayout, false);
			
			// Execution
			formLayout.add(lifecycleController.getInitialFormItem());
			lifecycleController.loadFromContext(context);
			
			// Spacer
			uifactory.addSpacerElement("space_2", formLayout, false);
			
			FormItemContainer copyModeLayout = FormLayoutContainer.createDefaultFormLayout_2_10("copyModeLayout", getTranslator());
			copyModeLayout.setRootForm(mainForm);
			formLayout.add(copyModeLayout);
			
			// Copy mode
			KeyValue custom = new KeyValue(CUSTOM_MODE, translate(CUSTOM_MODE));
			KeyValue automatic = new KeyValue(AUTOMATIC_MODE, translate(AUTOMATIC_MODE));
			KeyValues modes = new KeyValues(automatic, custom);
			
			copyModeEl = uifactory.addRadiosVertical("wizard.mode", copyModeLayout, modes.keys(), modes.values());
			copyModeEl.select(steps.isAdvancedMode() ? custom.getKey() : automatic.getKey(), true);
			copyModeEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == copyModeEl) {
				steps.setAdvancedMode(copyModeEl.isKeySelected(CUSTOM_MODE));
				setNextStep(CopyCourseStepsStep.create(ureq, steps));
				fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			} else if (source == displayNameEl) {
				checkCourseAvailability(ureq, displayNameEl);
			} else if (source == externalRefEl) {
				checkCourseAvailability(ureq, externalRefEl);
			}
		};
		
		private void checkCourseAvailability(UserRequest ureq, TextElement textElement) {
			if (StringHelper.containsNonWhitespace(textElement.getValue())) {
				SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
				params.setClosed(false);
				params.setDeleted(false);
				params.setExactSearch(true);
				
				if (textElement == displayNameEl) {
					params.setDisplayname(textElement.getValue());
				} else if (textElement == externalRefEl) {
					params.setReference(textElement.getValue());
				}
				
				textElement.clearError();
				if (repositoryService.countAuthorView(params) > 0) {
					String errorKey = "input.existing";
					
					if (textElement == displayNameEl) {
						errorKey += ".name";
					} else if (textElement == externalRefEl) {
						errorKey += ".reference";
					}
					textElement.setErrorKey(errorKey, null);
				}
			} else if (textElement.isMandatory()) {
				textElement.setErrorKey("input.mandatory", null);
			} else {
				textElement.clearError();
			}
		}
	}
 }
