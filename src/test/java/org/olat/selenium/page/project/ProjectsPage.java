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
package org.olat.selenium.page.project;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 21 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectsPage {
	
	private final WebDriver browser;
	
	public ProjectsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ProjectsPage assertOnMyProjectList() {
		By projectListBy = By.cssSelector(".o_proj_projects .o_proj_project_list");
		OOGraphene.waitElement(projectListBy, browser);
		return this;
	}
	
	public ProjectPage createNewProject() {
		By newProjectBy = By.cssSelector(".o_proj_project_list .o_sel_proj_create_project");
		OOGraphene.waitElement(newProjectBy, browser);
		browser.findElement(newProjectBy).click();
		OOGraphene.waitModalDialog(browser);
		return new ProjectPage(browser);
	}
	
	public ProjectsPage assertOnMyProject(String title) {
		By projectBy = By.xpath("//div[contains(@class,'o_proj_projects')]//div[contains(@class,'o_proj_project_list')]//h3[contains(@class,'o_proj_project_title')][text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(projectBy, browser);
		return this;
	}
	
	public ProjectPage openProject(String title) {
		By projectBy = By.xpath("//div[contains(@class,'o_proj_projects')]//div[contains(@class,'o_proj_project_list')]//div[contains(@class,'o_proj_project_row')][div/div/div/h3[contains(@class,'o_proj_project_title')][text()[contains(.,'" + title + "')]]]//div[contains(@class,'o_start_buttons')]/a");
		OOGraphene.waitElement(projectBy, browser);
		browser.findElement(projectBy).click();
		return new ProjectPage(browser).assertOnDashboard(title);
	}

}
