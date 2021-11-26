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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExport3CoverFieldsStep extends BasicStep {
	
	private final TestsExportContext exportContext;
	
	public TestsExport3CoverFieldsStep(UserRequest ureq, TestsExportContext exportContext) {
		super(ureq);
		this.exportContext = exportContext;
		
		if(exportContext.isAdditionalSheet()) {
			setNextStep(new TestsExport4InstructionsStep(ureq, exportContext));
		} else {
			setNextStep(new TestsExport5OverviewStep(ureq, exportContext));
		}
		
		setI18nTitleAndDescr("wizard.cover.fields.title", "wizard.cover.fields.title");
		
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form) {
		return new CoverFieldsController(ureq, wControl, form, runContext);
	}
	
	class CoverFieldsController extends StepFormBasicController {
		
		private TextElement titleEl;
		private TextElement procedureEl;
		private RichTextElement descriptionEl;
		
		public CoverFieldsController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormInfo("fields.explanation");

			titleEl = uifactory.addTextElement("field.title", 255, exportContext.getTitleValue(), formLayout);
			procedureEl = uifactory.addTextElement("field.procedure", 255, exportContext.getProcedure(), formLayout);
			
			descriptionEl = uifactory.addRichTextElementForStringDataCompact("field.informations", "field.informations", exportContext.getDescriptionValue(), 12, 60,
							null, formLayout, ureq.getUserSession(), getWindowControl());
			descriptionEl.getEditorConfiguration().disableImageAndMovie();
			descriptionEl.getEditorConfiguration().disableSmileys();
			descriptionEl.getEditorConfiguration().disableMedia();
			descriptionEl.setVisible(exportContext.isDescription());
		}

		@Override
		protected void formNext(UserRequest ureq) {
			exportContext.setTitleValue(titleEl.getValue());
			exportContext.setProcedure(procedureEl.getValue());
			exportContext.setDescriptionValue(descriptionEl.getValue());
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
