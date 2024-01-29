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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditExtraTimeController extends FormBasicController {
	
	private TextElement extraTimeEl;
	private List<AssessmentInspection> inspections;
	
	@Autowired
	private AssessmentInspectionService inspectionService;

	public EditExtraTimeController(UserRequest ureq, WindowControl wControl, List<AssessmentInspection> inspections) {
		super(ureq, wControl);
		this.inspections = inspections;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String time = "";
		if(inspections != null) {
			int maxTime = 0;
			for(AssessmentInspection inspection:inspections) {
				int extra = inspection.getExtraTime() == null ? 0 : inspection.getExtraTime().intValue();
				int duration = inspection.getConfiguration().getDuration();
				int extraTimeInMinutes = (duration + extra) / 60;
				if(extraTimeInMinutes > maxTime) {
					time = Integer.toString(extraTimeInMinutes);
					maxTime = extraTimeInMinutes;
				}
			}
		}
		
		extraTimeEl = uifactory.addTextElement("edit.extra.time", "edit.extra.time", 5, time, formLayout);
		extraTimeEl.setElementCssClass("form-inline");
		extraTimeEl.setDisplaySize(5);
		extraTimeEl.setDomReplacementWrapperRequired(false);
		extraTimeEl.setMandatory(true);
		extraTimeEl.setTextAddOn("edit.extra.time.unit");
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		extraTimeEl.clearError();
		if(StringHelper.containsNonWhitespace(extraTimeEl.getValue())) {
			if(StringHelper.isLong(extraTimeEl.getValue())) {
				for(AssessmentInspection inspection:inspections) {
					AssessmentInspectionConfiguration configuration = inspection.getConfiguration();
					int val = Integer.parseInt(extraTimeEl.getValue()) - (configuration.getDuration() / 60);
					if(val < 0) {
						extraTimeEl.setErrorKey("error.less.than.configuration.duration",
								Integer.toString(configuration.getDuration() / 60));
						allOk &= false;
						break;
					}
				}
			} else {
				extraTimeEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(AssessmentInspection inspection:inspections) {
			inspection = inspectionService.getInspection(inspection.getKey());
			if(inspection != null) {
				Integer extraTime = null;
				if(StringHelper.containsNonWhitespace(extraTimeEl.getValue())) {
					AssessmentInspectionConfiguration configuration = inspection.getConfiguration();
					int val = (Integer.parseInt(extraTimeEl.getValue()) * 60) - configuration.getDuration();
					if(val >= 0) {
						extraTime = Integer.valueOf(val);
					}
				}
				inspectionService.updateInspection(inspection, null, inspection.getFromDate(), inspection.getToDate(), extraTime,
						StringHelper.containsNonWhitespace(inspection.getAccessCode()), getIdentity());
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
