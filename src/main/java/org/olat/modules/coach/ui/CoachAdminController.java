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
package org.olat.modules.coach.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.coach.CoachingModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoachAdminController extends FormBasicController {
	
	private MultipleSelectionElement enabled;
	private static final String[] keys = {"on"};
	
	@Autowired
	private CoachingModule coachingModule;
	
	public CoachAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("coaching.title");
		setFormContextHelp("manual_user/e-assessment/Coaching/");

		boolean restEnabled = coachingModule.isEnabled();
		String[] values = new String[] { translate("coaching.on") };
		enabled = uifactory.addCheckboxesHorizontal("coaching.enabled", formLayout, keys, values);
		enabled.addActionListener(FormEvent.ONCLICK);
		enabled.select(keys[0], restEnabled);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabled == source) {
			boolean on = !enabled.getSelectedKeys().isEmpty();
			coachingModule.setEnabled(on);
			getWindowControl().setInfo("saved");
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
