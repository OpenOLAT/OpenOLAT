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
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeAdminSettingsController extends FormBasicController {
	
	private FormToggle enableAssessmentModeEl;
	private FormToggle enableAssessmentInspectionEl;
	
	@Autowired
	private AssessmentModule assessmentModule;
	
	public AssessmentModeAdminSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("assessment.mode.title");
		
		enableAssessmentModeEl = uifactory.addToggleButton("modeenabled", "assessment.mode.enabled", translate("on"), translate("off"), formLayout);
		enableAssessmentModeEl.toggle(assessmentModule.isAssessmentModeEnabled());
		
		enableAssessmentInspectionEl = uifactory.addToggleButton("inspectionenabled", "assessment.inspection.enabled", translate("on"), translate("off"), formLayout);
		enableAssessmentInspectionEl.toggle(assessmentModule.isAssessmentInspectionEnabled());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableAssessmentModeEl == source) {
			assessmentModule.setAssessmentModeEnabled(enableAssessmentModeEl.isOn());
		} else if(enableAssessmentInspectionEl == source) {
			assessmentModule.setAssessmentInspectionEnabled(enableAssessmentInspectionEl.isOn());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
