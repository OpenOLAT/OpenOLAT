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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumAdminConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement curriculumMyCoursesEl;
	
	@Autowired
	private CurriculumModule curriculumModule;
	
	public CurriculumAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		update();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("admin.description");
		setFormContextHelp("Modules: Curriculum");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("curriculum.admin.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(curriculumModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		curriculumMyCoursesEl = uifactory.addCheckboxesHorizontal("curriculum.in.my.courses.enabled", formLayout, onKeys, onValues);
		curriculumMyCoursesEl.addActionListener(FormEvent.ONCHANGE);
		if(curriculumModule.isEnabled()) {
			curriculumMyCoursesEl.select(onKeys[0], true);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			curriculumModule.setEnabled(enableEl.isAtLeastSelected(1));
			update();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(curriculumMyCoursesEl == source) {
			curriculumModule.setCurriculumInMyCourses(curriculumMyCoursesEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void update() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		curriculumMyCoursesEl.setVisible(enabled);
	}
}
