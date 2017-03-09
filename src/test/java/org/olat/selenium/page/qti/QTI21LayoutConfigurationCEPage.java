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
package org.olat.selenium.page.qti;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 2 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21LayoutConfigurationCEPage {
	
	private final WebDriver browser;
	
	public QTI21LayoutConfigurationCEPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21LayoutConfigurationCEPage overrideConfiguration() {
		By settingsBy = By.cssSelector(".o_qti_21_configuration_settings input[type='radio'][value='node']");
		browser.findElement(settingsBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21LayoutConfigurationCEPage fullWindow() {
		By fullWindowBy = By.cssSelector(".o_qti_21_configuration_full_window input[type='checkbox']");
		browser.findElement(fullWindowBy).click();
		return this;
	}
	
	public QTI21LayoutConfigurationCEPage enableSuspend() {
		By suspendBy = By.cssSelector(".o_sel_qti_enable_suspend input[type='checkbox']");
		WebElement suspendEl = browser.findElement(suspendBy);
		OOGraphene.check(suspendEl, Boolean.TRUE);
		return this;
	}

	public QTI21LayoutConfigurationCEPage saveLayoutConfiguration() {
		By saveBy = By.cssSelector(".o_qti_21_layout_configuration button");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
