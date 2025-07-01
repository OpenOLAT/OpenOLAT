/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: Jun 27, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class FlexiFilterExtendedController extends FormBasicController {
	
	public static Event CLEAR_BUTTON_UI_EVENT = new Event("update-clear-button");
	
	private boolean clearButtonEnabled = false;

	public FlexiFilterExtendedController(UserRequest ureq, WindowControl wControl, int layout,
			String customLayoutPageName, Form externalMainForm) {
		super(ureq, wControl, layout, customLayoutPageName, externalMainForm);
	}

	public abstract void doUpdate(UserRequest ureq);

	public abstract void doClear(UserRequest ureq);
	
	public boolean isClearButtonEnabled() {
		return clearButtonEnabled;
	}
	
	protected void updateClearButtonUI(UserRequest ureq, boolean enabled) {
		clearButtonEnabled = enabled;
		fireEvent(ureq, CLEAR_BUTTON_UI_EVENT);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		// public to get access from everywhere.
		return super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
