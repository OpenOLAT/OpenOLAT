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
package org.olat.course.assessment.ui.inspection.elements;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionMainController.InspectionStatus;

/**
 * 
 * Initial date: 17 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionTimerFormItem extends FormItemImpl  {
	
	private final AssessmentInspectionTimerComponent component;
	
	public AssessmentInspectionTimerFormItem(String name, InspectionStatus inspectionStatus) {
		super(name);
		component = new AssessmentInspectionTimerComponent(name, inspectionStatus, this);
		component.setDomReplacementWrapperRequired(false);
	}
	
	public InspectionStatus getInspectionStatus() {
		return component.getInspectionStatus();
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String command = ureq.getParameter("cid");
		if("timesUp".equals(command)) {
			getRootForm().fireFormEvent(ureq, new InspectionTimesUpEvent(this));
		}
	}

	@Override
	public void reset() {
		//
	}
}
