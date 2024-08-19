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
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.user.UserInfoController;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;

/**
 * 
 * Initial date: 15 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoUserInfoController extends UserInfoController {

	public GuiDemoUserInfoController(UserRequest ureq, WindowControl wControl,
			UserInfoProfileConfig profileConfig, UserInfoProfile profile) {
		super(ureq, wControl, profileConfig, profile);
		initForm(ureq);
	}

	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		super.initFormItems(itemsCont, listener, ureq);

		uifactory.addStaticTextElement("user.info.hairs", translate("user.info.hairs.value"), itemsCont);
		
		uifactory.addStaticTextElement("user.info.season", translate("user.info.season.value"), itemsCont);
		
		uifactory.addTextElement("user.info.river", 100, "", itemsCont);
		
		SelectionValues values = new SelectionValues();
		values.add(SelectionValues.entry("1", translate("select.1")));
		values.add(SelectionValues.entry("2", translate("select.2")));
		values.add(SelectionValues.entry("3", translate("select.3")));
		values.add(SelectionValues.entry("4", translate("select.4")));
		values.add(SelectionValues.entry("5", translate("select.5")));
		values.add(SelectionValues.entry("6", translate("select.6")));
		values.add(SelectionValues.entry("7", translate("select.7")));
		values.add(SelectionValues.entry("8", translate("select.8")));
		uifactory.addDropdownSingleselect("user.info.planet", itemsCont, values.keys(), values.values());
	}
}
