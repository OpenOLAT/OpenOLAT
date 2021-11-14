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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSmallDetailsController extends FormBasicController {
	
	private final List<String> remindersInfos = new ArrayList<>();
	
	@Autowired
	private ReminderService reminderService;
	
	public CourseSmallDetailsController(UserRequest ureq, WindowControl wControl, Form mainForm, RepositoryEntry courseEntry) {
		super(ureq, wControl, LAYOUT_CUSTOM, "small_course_details", mainForm);

		List<Reminder> reminders = reminderService.getReminders(courseEntry);
		for(int i=0; i<reminders.size(); i++) {
			remindersInfos.add(reminders.get(i).getDescription());
		}
		
		initForm(ureq);
	}
	
	public boolean hasDetails() {
		return !remindersInfos.isEmpty();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			List<String> informations = new ArrayList<>();
			informations.addAll(remindersInfos);
			layoutCont.contextPut("informations", informations);	
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
