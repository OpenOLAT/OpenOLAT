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
 * Initial date: 27 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageElementConfigurationPage {
	
	private final WebDriver browser;
	
	public PageElementConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PageElementConfigurationPage selectConfiguration() {
		By pageConfigBy = By.cssSelector("li.o_sel_cep_content_config>a");
		OOGraphene.waitElement(pageConfigBy, browser);
		browser.findElement(pageConfigBy).click();
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_page_settings"), browser);
		return this;
	}
	
	public PageElementConfigurationPage enableCoachEditing() {
		By coachBy = By.xpath("//fieldset[contains(@class,'o_sel_node_rights')]//input[@type='checkbox'][@value='coach']");
		WebElement coachEl = browser.findElement(coachBy);
		OOGraphene.check(coachEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
