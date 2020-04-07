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
package org.olat.course.assessment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.CourseModule;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Initial date: 08.05.2017<br>
 * @author fkiefer
 *
 */
public class AssessableCourseNodeAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private final String[] onValues;
	
	private MultipleSelectionElement infoBoxEl;
	private MultipleSelectionElement changeLogEl;
	private MultipleSelectionElement disclaimerEnabledEl;

	@Autowired
	private CourseModule courseModule;
	
	public AssessableCourseNodeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		onValues = new String[]{ translate("on") };

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
	    setFormTitle("admin.assessable.coursenode");
		
		infoBoxEl = uifactory.addCheckboxesHorizontal("admin.info.box", formLayout, onKeys, onValues);
		infoBoxEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisplayInfoBox()) {
			infoBoxEl.select(onKeys[0], true);
		}
		
		changeLogEl = uifactory.addCheckboxesHorizontal("admin.user.changelog", formLayout, onKeys, onValues);
		changeLogEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisplayChangeLog()) {
			changeLogEl.select(onKeys[0], true);
		}
		
		disclaimerEnabledEl = uifactory.addCheckboxesHorizontal("admin.disclaimer.enabled", formLayout, onKeys, onValues);
		disclaimerEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisclaimerEnabled()) {
			disclaimerEnabledEl.select(onKeys[0], true);
		}
		

	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == infoBoxEl) {
			courseModule.setDisplayInfoBox(infoBoxEl.isSelected(0));
		} else if (source == changeLogEl) {
			courseModule.setDisplayChangeLog(changeLogEl.isSelected(0));
		} else if (source == disclaimerEnabledEl) {
			courseModule.setDisclaimerEnabled(disclaimerEnabledEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing
	}

	@Override
	protected void doDispose() {
		// nothing
	}

}
