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
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionMainController.InspectionStatus;

/**
 * 
 * Initial date: 17 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionTimerComponent extends FormBaseComponentImpl {

	private static final AssessmentInspectionTimerComponentRenderer RENDERER = new AssessmentInspectionTimerComponentRenderer();
	
	private AssessmentInspectionTimerFormItem item;
	
	private final InspectionStatus inspectionStatus;
	
	public AssessmentInspectionTimerComponent(String name, InspectionStatus inspectionStatus, AssessmentInspectionTimerFormItem item) {
		super(name);
		this.item = item;
		this.inspectionStatus = inspectionStatus;
	}
	
	@Override
	public AssessmentInspectionTimerFormItem getFormItem() {
		return item;
	}

	public InspectionStatus getInspectionStatus() {
		return inspectionStatus;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// 
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
