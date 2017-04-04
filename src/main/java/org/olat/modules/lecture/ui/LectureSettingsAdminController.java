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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configure the lecture module.
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureSettingsAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement attendanceRateEl;
	private MultipleSelectionElement enableEl, authorizedAbsenceEnableEl;
	
	@Autowired
	private LectureModule lectureModule;
	
	public LectureSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("lecture.admin.title");
		
		String[] onValues = new String[] { "" };
		enableEl = uifactory.addCheckboxesHorizontal("lecture.admin.enabled", formLayout, onKeys, onValues);
		if(lectureModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		authorizedAbsenceEnableEl = uifactory.addCheckboxesHorizontal("lecture.authorized.absence.enabled", formLayout, onKeys, onValues);
		if(lectureModule.isAuthorizedAbsenceEnabled()) {
			authorizedAbsenceEnableEl.select(onKeys[0], true);
		}
		
		long attendanceRate = Math.round(lectureModule.getRequiredAttendanceRateDefault() * 100.0d);
		String attendanceRateStr = Long.toString(attendanceRate);
		attendanceRateEl = uifactory.addTextElement("lecture.attendance.rate.default", "lecture.attendance.rate.default", 2, attendanceRateStr, formLayout);
		attendanceRateEl.setDisplaySize(2);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		attendanceRateEl.clearError();
		if(StringHelper.containsNonWhitespace(attendanceRateEl.getValue())) {
			try {
				int val = Integer.parseInt(attendanceRateEl.getValue());
				if(val <= 0 && val > 100) {
					attendanceRateEl.setErrorKey("error.integer.between", new String[] {"1", "100"});
					allOk &= false;
				}
			} catch (Exception e) {
				attendanceRateEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			attendanceRateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lectureModule.setEnabled(enableEl.isAtLeastSelected(1));
		lectureModule.setAuthorizedAbsenceEnabled(authorizedAbsenceEnableEl.isAtLeastSelected(1));
		
		String attendanceRateInPercent = attendanceRateEl.getValue();
		if(StringHelper.containsNonWhitespace(attendanceRateInPercent)) {
			double val = Double.parseDouble(attendanceRateInPercent) / 100.0d;
			lectureModule.setRequiredAttendanceRateDefault(val);
		}
	}
}