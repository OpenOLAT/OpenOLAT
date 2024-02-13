/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.ui.inspection.CreateInspectionContext.NewInspectionConfiguration;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;

/**
 * 
 * Initial date: 13 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateInspectionConfigurationController extends StepFormBasicController {
	
	private TextElement durationEl;
	private MultipleSelectionElement resultsEl;
	
	private final CreateInspectionContext context;
	
	public CreateInspectionConfigurationController(UserRequest ureq, WindowControl wControl,
			CreateInspectionContext context, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.context = context;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("infos.inspection.configuration");
		
		String message = translate("standard.configuration.message");
		uifactory.addStaticTextElement("standard.configuration", "standard.configuration", message, formLayout);

		durationEl = uifactory.addTextElement("max.duration", 10, "15", formLayout);
		durationEl.setElementCssClass("form-inline");
		durationEl.setTextAddOn("max.duration.unit");
		durationEl.setMandatory(true);
		
		SelectionValues resultsKV = new SelectionValues();
		resultsKV.add(SelectionValues.entry(QTI21AssessmentResultsOptions.METADATA, translate("qti.form.summary.metadata")));
		resultsKV.add(SelectionValues.entry(QTI21AssessmentResultsOptions.SECTION_SUMMARY, translate("qti.form.summary.sections")));
		resultsKV.add(SelectionValues.entry(QTI21AssessmentResultsOptions.QUESTION_SUMMARY, translate("qti.form.summary.questions.metadata")));
		resultsKV.add(SelectionValues.entry(QTI21AssessmentResultsOptions.USER_SOLUTIONS, translate("qti.form.summary.responses")));
		resultsKV.add(SelectionValues.entry(QTI21AssessmentResultsOptions.CORRECT_SOLUTIONS, translate("qti.form.summary.solutions")));
		resultsEl = uifactory.addCheckboxesVertical("typeResultOnFinish", "qti.form.summary", formLayout, resultsKV.keys(), resultsKV.values(), 1);
		resultsEl.setHelpText(translate("qti.form.summary.help"));
		resultsEl.setMandatory(true);

	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		durationEl.clearError();
		if(StringHelper.containsNonWhitespace(durationEl.getValue())) {
			try {
				int value = Integer.parseInt(durationEl.getValue());
				if(value < 1 || value > 10000) {
					durationEl.setErrorKey("form.error.nointeger.between", "1", "10000");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				durationEl.setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		} else {
			durationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		resultsEl.clearError();
		if(!resultsEl.isAtLeastSelected(1)) {
			resultsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		int duration = (Integer.parseInt(durationEl.getValue()) * 60);// In seconds
		Collection<String> results = resultsEl.getSelectedKeys();
		String options = String.join(",", results);
		context.setNewConfiguration(new NewInspectionConfiguration(duration, options));
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
