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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
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

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExport4InstructionsStep extends BasicStep {
	
	private final TestsExportContext exportContext;
	
	public TestsExport4InstructionsStep(UserRequest ureq, TestsExportContext exportContext) {
		super(ureq);
		this.exportContext = exportContext;
		setNextStep(new TestsExport5OverviewStep(ureq, exportContext));
		setI18nTitleAndDescr("wizard.instructions.title", "wizard.instructions.title");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new AdditionalSheetController(ureq, wControl, form, runContext);
	}
	
	class AdditionalSheetController extends StepFormBasicController {
		
		private RichTextElement additionalSheetEl;
		
		public AdditionalSheetController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormInfo("instructions.explanation");
			
			String val;
			if(StringHelper.containsNonWhitespace(exportContext.getAdditionalSheetValue())) {
				val = exportContext.getAdditionalSheetValue();
			} else {
				val = Util.createPackageTranslator(AdditionalSheetController.class, exportContext.getLocale())
						.translate("admin.additional.sheet.description");
			}
			additionalSheetEl = uifactory.addRichTextElementForStringDataMinimalistic("field.instructions", "field.instructions",
					val, 12, 60, formLayout, getWindowControl());
		}

		@Override
		protected void formNext(UserRequest ureq) {
			exportContext.setAdditionalSheetValue(additionalSheetEl.getValue());
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
