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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * @author srosse
 *
 */
public class ContactConfigPage {

	private final WebDriver browser;
	
	public ContactConfigPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ContactConfigPage selectConfiguration() {
		OOGraphene.scrollTop(browser);
		By configBy = By.className("o_sel_co_config_form");
		OOGraphene.selectTab(CourseEditorPageFragment.navBarNodeConfiguration, configBy, browser);
		return this;
	}
	
	public ContactConfigPage wantAllOwners() {
		By wantBy = By.cssSelector("div.o_sel_co_want_owners input[name='wantOwners']");
		OOGraphene.waitElement(wantBy, browser);
		WebElement wantEl = browser.findElement(wantBy);
		OOGraphene.check(wantEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ContactConfigPage wantAllCoaches() {
		By wantBy = By.cssSelector("div.o_sel_config_want_coaches input[type='checkbox'][name='coaches']");
		OOGraphene.waitElement(wantBy, browser);
		WebElement wantEl = browser.findElement(wantBy);
		OOGraphene.check(wantEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		By allBy = By.cssSelector("div.o_sel_config_coaches input[type='radio'][value='all']");
		OOGraphene.waitElement(allBy, browser);
		browser.findElement(allBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ContactConfigPage wantAllParticipants() {
		By wantBy = By.cssSelector("div.o_sel_config_want_participants input[type='checkbox'][name='participants']");
		OOGraphene.waitElement(wantBy, browser);
		WebElement wantEl = browser.findElement(wantBy);
		OOGraphene.check(wantEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		By allBy = By.cssSelector("div.o_sel_config_participants input[type='radio'][value='all']");
		OOGraphene.waitElement(allBy, browser);
		browser.findElement(allBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ContactConfigPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_co_config_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
}
