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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 22 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectBrokerPage {
	
	private final WebDriver browser;
	
	public ProjectBrokerPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ProjectBrokerPage assertOnProjectBrokerList() {
		By startBy = By.cssSelector("div.o_project_broker_list");
		OOGraphene.waitElement(startBy, browser);
		return this;
	}
	
	public ProjectBrokerPage createNewProject(String title) {
		By createProjectBy = By.cssSelector("div.o_project_broker_list a.o_sel_broker_create_new_project");
		OOGraphene.waitElement(createProjectBy, browser);
		browser.findElement(createProjectBy).click();
		
		By projectBy = By.cssSelector(".o_project fieldset.o_sel_project_details_form");
		OOGraphene.waitElement(projectBy, browser);
		OOGraphene.waitTinymce(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_project_details_form .o_sel_project_title input[type='text']");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.clear();
		titleEl.sendKeys(title);
		
		By saveBy = By.cssSelector("fieldset.o_sel_project_details_form .o_sel_buttons button.btn-primary");
		browser.findElement(saveBy).sendKeys(title);
		
		By inListBy = By.xpath("//div[contains(@class,'o_project_broker_list')]//table//td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(inListBy, browser);
		
		// 
		return this;
	}

}
