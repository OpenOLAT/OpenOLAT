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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationEditGeneralController extends FormBasicController {
	
	private TextElement nameEl;
	private TextElement durationEl;
	private MultipleSelectionElement resultsEl;
	
	private final RepositoryEntry entry;
	private AssessmentInspectionConfiguration configuration;
	
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionConfigurationEditGeneralController(UserRequest ureq, WindowControl wControl,
			AssessmentInspectionConfiguration configuration, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		this.configuration = configuration;
		
		initForm(ureq);
	}
	
	public AssessmentInspectionConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		String name = configuration == null ? null : configuration.getName();
		nameEl = uifactory.addTextElement("configuration.name", 255, name, formLayout);
		nameEl.setMandatory(true);
		
		String duration = configuration == null ? null : Integer.toString(configuration.getDuration() / 60);
		durationEl = uifactory.addTextElement("max.duration", 10, duration, formLayout);
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
		if(StringHelper.containsNonWhitespace(configuration.getOverviewOptions())) {
			List<String> options = configuration.getOverviewOptionsAsList();
			for(String option:options) {
				if(resultsKV.containsKey(option)) {
					resultsEl.select(option, true);
				}
			}
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(inspectionService.isInspectionConfigurationNameInUse(entry, nameEl.getValue(), configuration)) {
			nameEl.setErrorKey("error.configuration.name.unique");
			allOk &= false;
		}
		
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
		save();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void save() {
		if(configuration.getKey() != null) {
			configuration = inspectionService.getConfigurationById(configuration.getKey());
		}

		configuration.setName(nameEl.getValue());
		configuration.setDuration(Integer.parseInt(durationEl.getValue()) * 60);// In seconds
		
		Collection<String> results = resultsEl.getSelectedKeys();
		configuration.setOverviewOptions(String.join(",", results));
		
		configuration = inspectionService.saveConfiguration(configuration);
	}
}
