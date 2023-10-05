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
package org.olat.selenium.page.user;

import org.olat.login.webauthn.PasskeyLevels;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 15 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PasskeyAdminPage {
	
	private final WebDriver browser;
	
	public PasskeyAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PasskeyAdminPage enablePasskey(boolean enable) {
		By enableBy = By.cssSelector("fieldset.o_sel_passkey_admin_configuration button.o_button_toggle");
		OOGraphene.waitElement(enableBy, browser);
		
		String toggleButtonBy = "fieldset.o_sel_passkey_admin_configuration button.o_button_toggle";
		OOGraphene.toggle(toggleButtonBy, enable, true, browser);
		return this;
	}
	
	public PasskeyAdminPage enablePasskeyLevel(PasskeyLevels level) {
		By calloutButtonBy = By.cssSelector("button.o_sel_passkey_level_all_roles");
		OOGraphene.waitElement(calloutButtonBy, browser);
		browser.findElement(calloutButtonBy).click();

		By calloutBy = By.cssSelector("ul.o_sel_passkey_level_all_roles");
		OOGraphene.waitElement(calloutBy, browser);

		By applyLevel = By.cssSelector("ul.o_sel_passkey_level_all_roles li>a.o_sel_passkey_" + level.name());
		browser.findElement(applyLevel).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		By adminLevelBy = By.cssSelector("input[type='radio'][checked='checked'][value='administrator." + level.name() + "']");
		OOGraphene.waitElement(adminLevelBy, browser);
		return this;
	}
}
