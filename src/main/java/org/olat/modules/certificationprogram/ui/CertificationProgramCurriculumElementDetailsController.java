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
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.coach.ui.CourseListConfig;
import org.olat.modules.coach.ui.CourseListController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramCurriculumElementDetailsController extends FormBasicController {
	
	private final CurriculumElement curriculumElement;
	private final CurriculumElementRow curriculumElementRow;
	
	private CourseListController courseListCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CertificationProgramCurriculumElementDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CurriculumElementRow curriculumElementRow) {
		super(ureq, wControl, LAYOUT_CUSTOM, "program_implementation_details_view", rootForm);
		this.curriculumElementRow = curriculumElementRow;
		curriculumElement = curriculumService.getCurriculumElement(curriculumElementRow);

		initForm(ureq);
	}

	public CurriculumElementRow getUserObject() {
		return curriculumElementRow;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		CourseListConfig config = CourseListConfig.minimalConfig();
		courseListCtrl = new CourseListController(ureq, getWindowControl(), mainForm, curriculumElement, config);
		listenTo(courseListCtrl);
		formLayout.add("courseDetails", courseListCtrl.getInitialFormItem());
		courseListCtrl.activate(ureq, List.of(), null);
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("numOfEntries", courseListCtrl.getNumOfCourses());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
