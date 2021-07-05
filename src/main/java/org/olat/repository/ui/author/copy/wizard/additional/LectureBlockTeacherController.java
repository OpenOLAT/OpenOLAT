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
package org.olat.repository.ui.author.copy.wizard.additional;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;

/**
 * Initial date: 09.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class LectureBlockTeacherController extends FormBasicController {

	private MultipleSelectionElement teachersEl;
	private CopyCourseContext context;
	private LectureBlockRow row;
	
	public LectureBlockTeacherController(UserRequest ureq, WindowControl wControl, CopyCourseContext context, LectureBlockRow row) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		
		this.context = context;
		this.row = row;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (context.getNewCoaches() != null && !context.getNewCoaches().isEmpty()) {
			SelectionValues teachers = new SelectionValues();
			
			for (Identity coach : context.getNewCoaches()) {
				teachers.add(SelectionValues.entry(coach.getKey().toString(), coach.getUser().getFirstName() + " " + coach.getUser().getLastName()));
			}
			
			teachersEl = uifactory.addCheckboxesVertical("noticed.teachers", formLayout, teachers.keys(), teachers.values(), 1);
			teachersEl.setAjaxOnly(true);
			
			if (row.getTeachersList() != null && !row.getTeachersList().isEmpty()) {
				for (Identity teacher : row.getTeachersList()) {
					if (teachers.containsKey(teacher.getKey().toString())) {
						teachersEl.select(teacher.getKey().toString(), true);
					}
				}
			}
		} else {
			setFormDescription("coaches.empty");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to do here
	}

	@Override
	protected void doDispose() {
		// Nothing to do here		
	}
	
	public void saveToContext() {
		if (teachersEl != null) {
			List<Identity> newTeachers = new ArrayList<>();
			
			for (String key : teachersEl.getSelectedKeys()) {
				long teacherKey = Long.valueOf(key);
				Identity newTeacher = context.getNewCoaches().stream().filter(owner -> owner.getKey().equals(teacherKey)).findFirst().orElse(null);
				
				if (newTeacher != null) {
					newTeachers.add(newTeacher);
				}
			}
			
			row.setTeachersList(newTeachers);
		}
	}

}
