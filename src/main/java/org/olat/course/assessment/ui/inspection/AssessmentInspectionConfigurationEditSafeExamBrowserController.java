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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.ui.mode.AbstractEditSafeExamBrowserController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationEditSafeExamBrowserController extends AbstractEditSafeExamBrowserController {
	
	private AssessmentInspectionConfiguration configuration;
	
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionConfigurationEditSafeExamBrowserController(UserRequest ureq, WindowControl wControl,
			AssessmentInspectionConfiguration configuration) {
		super(ureq, wControl, configuration);
		this.configuration = configuration;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected boolean isEditable() {
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		FormLayoutContainer buttonsWrapperCont = uifactory.addDefaultFormLayout("buttonsWrapper", null, formLayout);		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, buttonsWrapperCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(configuration.getKey() != null) {
			configuration = inspectionService.getConfigurationById(configuration.getKey());
		}
		commit(configuration);
		
		configuration = inspectionService.saveConfiguration(configuration);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
