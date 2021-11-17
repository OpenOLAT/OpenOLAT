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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
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

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExport2CoverAttributesStep extends BasicStep {
	
	private final TestsExportContext exportContext;
	
	public TestsExport2CoverAttributesStep(UserRequest ureq, TestsExportContext exportContext) {
		super(ureq);
		this.exportContext = exportContext;
		setNextStep(new TestsExport3CoverFieldsStep(ureq, exportContext));
		setI18nTitleAndDescr("wizard.cover.attributes.title", "wizard.cover.attributes.title");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CoverAttributesController(ureq, wControl, form, runContext);
	}
	
	class CoverAttributesController extends StepFormBasicController {
		
		private MultipleSelectionElement generalEl;
		private MultipleSelectionElement testEl;
		
		public CoverAttributesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormInfo("cover.attributes.explanation");
			
			SelectionValues generalValues = new SelectionValues();
			generalValues.add(SelectionValues.entry("serial.number", translate("attr.serial.number")));
			generalValues.add(SelectionValues.entry("names", translate("placeholder.first.last.names")));
			generalValues.add(SelectionValues.entry("candidate.number", translate("placeholder.candidate.number")));
			generalValues.add(SelectionValues.entry("date", translate("placeholder.date")));
			generalEl = uifactory.addCheckboxesVertical("attr.general", "cover.attributes.general", formLayout,
					generalValues.keys(), generalValues.values(), 1);
			generalEl.select("serial.number", exportContext.isSerialNumber());
			generalEl.select("names", exportContext.isPlaceholderNames());
			generalEl.select("date", exportContext.isPlaceholderDate());
			generalEl.select("candidate.number", exportContext.isPlaceholderCandidateNumber());

			SelectionValues testValues = new SelectionValues();
			testValues.add(SelectionValues.entry("time", translate("attr.time")));
			testValues.add(SelectionValues.entry("questions", translate("attr.num.questions")));
			testValues.add(SelectionValues.entry("maxscore", translate("attr.max.score")));
			testValues.add(SelectionValues.entry("cutvalue", translate("attr.cut.value")));
			testValues.add(SelectionValues.entry("description", translate("attr.description")));
			testEl = uifactory.addCheckboxesVertical("attr.test", "cover.attributes.test", formLayout,
					testValues.keys(), testValues.values(), 1);
			testEl.select("maxscore", exportContext.isMaxScore());
			testEl.select("cutvalue", exportContext.isCutValue());
			testEl.select("questions", exportContext.isNumOfQuestions());
			testEl.select("description", exportContext.isDescription());
			testEl.select("time", exportContext.isExpenditureOfTime());
		}

		@Override
		protected void formNext(UserRequest ureq) {
			Collection<String> generals = generalEl.getSelectedKeys();
			exportContext.setSerialNumber(generals.contains("serial.number"));
			exportContext.setPlaceholderNames(generals.contains("names"));
			exportContext.setPlaceholderDate(generals.contains("date"));
			exportContext.setPlaceholderCandidateNumber(generals.contains("candidate.number"));
			
			Collection<String> tests = testEl.getSelectedKeys();
			exportContext.setMaxScore(tests.contains("maxscore"));
			exportContext.setCutValue(tests.contains("cutvalue"));
			exportContext.setNumOfQuestions(tests.contains("questions"));
			exportContext.setDescription(tests.contains("description"));
			exportContext.setExpenditureOfTime(tests.contains("time"));
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
