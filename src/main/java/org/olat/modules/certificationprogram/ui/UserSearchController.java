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
package org.olat.modules.certificationprogram.ui;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;

/**
 * 
 * Initial date: 24 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserSearchController extends StepFormBasicController {

	private UserSearchFlexiController searchController; 

	public UserSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchController = new UserSearchFlexiController(ureq, getWindowControl(), mainForm, null, null, true, true);
		listenTo(searchController);
		formLayout.add("search", searchController.getInitialFormItem());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof SingleIdentityChosenEvent) {
			fireEvent(ureq, event);
		} else if(event instanceof MultiIdentityChosenEvent) {
			fireEvent(ureq, event);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//do nothing
	}
}