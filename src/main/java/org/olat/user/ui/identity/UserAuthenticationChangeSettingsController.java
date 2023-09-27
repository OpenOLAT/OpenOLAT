/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.identity;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.login.webauthn.PasskeyLevels;

/**
 * 
 * Initial date: 25 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAuthenticationChangeSettingsController extends FormBasicController {
	
	private SingleSelection levelEl;
	
	private final PasskeyLevels currentLevel;
	private final List<PasskeyLevels> levels;
	
	public UserAuthenticationChangeSettingsController(UserRequest ureq, WindowControl wControl,
			PasskeyLevels currentLevel, List<PasskeyLevels> levels) {
		super(ureq, wControl);
		this.currentLevel = currentLevel;
		this.levels = List.copyOf(levels);
		
		initForm(ureq);
	}
	
	public PasskeyLevels getSelectedLevel() {
		return levelEl.isOneSelected() ? PasskeyLevels.valueOf(levelEl.getSelectedKey()) : null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("current.level", "current.level", translate("security.level." + currentLevel.name()), formLayout);
		
		SelectionValues levelPK = new SelectionValues();
		for(PasskeyLevels level: levels) {
			if(level != currentLevel) {
				String value = translate("security.level." + level.name());
				String description = translate("security.level." + currentLevel + "." + level + ".option");
				levelPK.add(SelectionValues.entry(level.name(), value, description, null, null, true));
			}
		}
		levelEl = uifactory.addCardSingleSelectHorizontal("choose.level", "choose.level", formLayout,
				levelPK.keys(), levelPK.values(), levelPK.descriptions(), null);
		if(levelPK.containsKey(PasskeyLevels.level2.name())) {
			levelEl.select(PasskeyLevels.level2.name(), true);
		} else if(levelPK.containsKey(PasskeyLevels.level3.name())) {
			levelEl.select(PasskeyLevels.level3.name(), true);
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("change.authentication.settings", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		levelEl.clearError();
		if(!levelEl.isOneSelected()) {
			levelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
