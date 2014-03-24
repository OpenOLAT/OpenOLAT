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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.coach.CoachingModule;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoachAdminController extends FormBasicController {
	
	private MultipleSelectionElement enabled;
	
	private String[] values = {""};
	private String[] keys = {"on"};
	
	public CoachAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		values = new String[] {
				getTranslator().translate("coaching.on")
			};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("coaching.title");
		setFormContextHelp(CoachAdminController.class.getPackage().getName(), "coaching.html", "help.hover.coaching");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			
			boolean restEnabled = isEnabled();

			enabled = uifactory.addCheckboxesHorizontal("coaching.enabled", formLayout, keys, values, null);
			enabled.select(keys[0], restEnabled);

	
			final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			layoutContainer.add(buttonGroupLayout);

			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean on = !enabled.getSelectedKeys().isEmpty();
		setEnabled(on);
		getWindowControl().setInfo("saved");
	}
	
	private boolean isEnabled() {
		CoachingModule config = CoreSpringFactory.getImpl(CoachingModule.class);
		return config.isEnabled();
	}
	
	private void setEnabled(boolean enabled) {
		CoachingModule config = CoreSpringFactory.getImpl(CoachingModule.class);
		config.setEnabled(enabled);
	}
}
