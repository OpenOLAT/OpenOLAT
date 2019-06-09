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
 * Initial date: 7 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberListConfigurationPage {
	
	private final WebDriver browser;
	
	public MemberListConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MemberListConfigurationPage selectSettings() {
		By configBy = By.cssSelector("fieldset.o_sel_cmembers_settings");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	public MemberListConfigurationPage setOwners(Boolean visible) {
		return setMembers(visible, "members.owners");
	}
	
	public MemberListConfigurationPage setCoaches(Boolean visible) {
		return setMembers(visible, "coaches");
	}
	
	public MemberListConfigurationPage setParticipants(Boolean visible) {
		return setMembers(visible, "participants");
	}

	private MemberListConfigurationPage setMembers(Boolean visible, String type) {
		By checkboxBy = By.xpath("//fieldset[contains(@class,'o_sel_cmembers_settings')]//input[@type='checkbox'][@name='" + type + "']");
		WebElement checkboxEl = browser.findElement(checkboxBy);
		OOGraphene.check(checkboxEl, visible);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MemberListConfigurationPage setCourseCoachesOnly() {
		By courseCoachBy = By.xpath("//fieldset[contains(@class,'o_sel_cmembers_settings')]//input[@type='radio'][@name='coachesChoice'][@value='course']");
		OOGraphene.waitElement(courseCoachBy, browser);
		browser.findElement(courseCoachBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MemberListConfigurationPage save() {
		By configBy = By.cssSelector("fieldset.o_sel_cmembers_settings button.btn-primary");
		OOGraphene.click(configBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
