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
package org.olat.modules.lecture.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.ContactTeachersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InformStepController extends StepFormBasicController {
	
	private final ContactTeachersController teachersCtrl;
	
	@Autowired
	private LectureService lectureService;

	public InformStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			LecturesSecurityCallback secCallback, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		EditAbsenceNoticeWrapper noticeWrapper = (EditAbsenceNoticeWrapper)getFromRunContext("absence");
		List<Identity> teachers = lectureService.getTeachers(noticeWrapper.getIdentity(), noticeWrapper.getLectureBlocks(),
				noticeWrapper.getEntries(), noticeWrapper.getStartDate(), noticeWrapper.getEndDate());
		teachersCtrl = new ContactTeachersController(ureq, getWindowControl(), teachers, secCallback, rootForm);
		listenTo(teachersCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(teachersCtrl);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("contact", teachersCtrl.getInitialFormItem());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= teachersCtrl.validateFormLogic(ureq);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		EditAbsenceNoticeWrapper noticeWrapper = (EditAbsenceNoticeWrapper)getFromRunContext("absence");
		List<Identity> teachers = teachersCtrl.getSelectedTeacher();
		if(!teachers.isEmpty() && teachersCtrl.isSendMail()) {
			noticeWrapper.setIdentitiesToContact(teachers);
			noticeWrapper.setContactSubject(teachersCtrl.getSubject());
			noticeWrapper.setContactBody(teachersCtrl.getBody());
		}

		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}	
}
