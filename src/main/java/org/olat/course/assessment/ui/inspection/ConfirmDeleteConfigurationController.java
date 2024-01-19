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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteConfigurationController extends FormBasicController {
	
	private final AssessmentInspectionConfiguration configuration;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public ConfirmDeleteConfigurationController(UserRequest ureq, WindowControl wControl, AssessmentInspectionConfiguration configuration) {
		super(ureq, wControl, "confirm_delete_configuration");
		this.configuration = configuration;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormSubmit deleteButton = uifactory.addFormSubmitButton("delete.configuration", formLayout);
		deleteButton.setElementCssClass("btn btn-default btn-danger");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		inspectionService.deleteConfiguration(configuration);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
