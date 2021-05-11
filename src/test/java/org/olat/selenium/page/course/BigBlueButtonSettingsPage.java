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

/**
 * 
 * Initial date: 11 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonSettingsPage {
	
	private final WebDriver browser;
	
	public BigBlueButtonSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Select the tab to configure the templates.
	 * 
	 * @return Itself
	 */
	public BigBlueButtonSettingsPage selectTemplates() {
		By templatesTabBy = By.cssSelector("div.o_segments a.o_sel_bbb_templates");
		OOGraphene.waitElement(templatesTabBy, browser);
		browser.findElement(templatesTabBy).click();
		
		By templatesListBy = By.cssSelector("div.o_sel_bbb_templates_list table");
		OOGraphene.waitElement(templatesListBy, browser);
		return this;
	}
	
	/**
	 * Open the panel to edit a template.
	 * 
	 * @param name The name of the template
	 * @return Itself
	 */
	public BigBlueButtonTemplatePage editTemplate(String name) {
		By editBy = By.xpath("//div[@class='o_sel_bbb_templates_list']//table//tr[td[contains(@class,'o_dnd_label')][text()[contains(.,'" + name + "')]]]/td/a[contains(@onclick,'edit')]");
		OOGraphene.waitElement(editBy, browser);
		browser.findElement(editBy).click();
		OOGraphene.waitModalDialog(browser);
		return new BigBlueButtonTemplatePage(browser);
	}

}
