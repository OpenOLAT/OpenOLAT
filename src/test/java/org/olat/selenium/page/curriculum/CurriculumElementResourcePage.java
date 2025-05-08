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
package org.olat.selenium.page.curriculum;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 7 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementResourcePage {
	
	private final WebDriver browser;
	
	public CurriculumElementResourcePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CurriculumElementResourcePage assertOnResourcesList() {
		By resourcesBy = By.cssSelector("fieldset.o_sel_curriculum_element_resources");
		OOGraphene.waitElement(resourcesBy, browser);
		return this;
	}
	
	public CurriculumElementResourcePage assertOnTemplatesList() {
		By resourcesBy = By.cssSelector("fieldset.o_sel_curriculum_element_templates");
		OOGraphene.waitElement(resourcesBy, browser);
		return this;
	}

	public CurriculumElementResourcePage assertOnCourseInResourcesList(String name) {
		By resourcesBy = By.xpath("//fieldset[@class='o_sel_curriculum_element_resources']//table//td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(resourcesBy, browser);
		return this;
	}
	
	public CurriculumElementResourcePage selectCourse(String name) {
		By addButtonBy = By.cssSelector("a.o_sel_curriculum_element_add_resource");
		browser.findElement(addButtonBy).click();
		OOGraphene.waitModalDialog(browser, "div.o_sel_author_env");
		
		By selectBy = By.xpath("//div[@class='o_sel_author_env']//td/a[text()[contains(.,'" + name + "')]]");
		browser.findElement(selectBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return assertOnCourseInResourcesList(name);
	}
	
	

}
