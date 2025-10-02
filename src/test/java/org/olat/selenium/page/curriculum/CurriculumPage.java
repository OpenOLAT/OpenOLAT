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
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 30 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumPage {
	
	private final WebDriver browser;
	
	public CurriculumPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CurriculumPage assertOnCurriculumList() {
		By curriculumListBy = By.cssSelector(".o_sel_curriculum_management");
		OOGraphene.waitElement(curriculumListBy, browser);
		return this;
	}
	
	public CurriculumPage assertOnCurriculumInTable(String name) {
		By curriculumBy = By.xpath("//div[@class='o_sel_curriculum_management']//table//td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(curriculumBy, browser);
		return this;
	}
	
	public CurriculumPage addCurriculum(String name, String externalRef, String organisation) {
		By addBy = By.cssSelector(".o_sel_curriculum_management a.o_sel_add_curriculum");
		OOGraphene.waitElement(addBy, browser).click();
		
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_curriculum_form");
		OOGraphene.waitTinymce(browser);
		
		By displayNameBy = By.cssSelector("fieldset.o_sel_curriculum_form .o_sel_curriculum_displayname input[type='text']");
		browser.findElement(displayNameBy).sendKeys(name);
		
		By identifierBy = By.cssSelector("fieldset.o_sel_curriculum_form .o_sel_curriculum_identifier input[type='text']");
		browser.findElement(identifierBy).sendKeys(externalRef);
		
		// Select organisation
		By organisationBy = By.cssSelector("div.o_sel_curriculum_organisation button.o_selection_display");
		browser.findElement(organisationBy).click();
		OOGraphene.waitCallout(browser, ".o_object_selection");
		
		By openOlatOrgBy = By.xpath("//div[contains(@class,'popover')]//div[@class='o_object_selection']//label[div/div/div[contains(text(),'" + organisation + "')]]/input[@type='radio']");
		WebElement openOlatOrgEl = browser.findElement(openOlatOrgBy);
		OOGraphene.check(openOlatOrgEl, Boolean.TRUE);
		
		By openOlatOrgCheckedBy = By.xpath("//div[contains(@class,'popover')]//div[@class='o_object_selection']//label[div/div/div[contains(text(),'" + organisation + "')]]/input[@type='radio'][@checked='checked']");
		OOGraphene.waitElement(openOlatOrgCheckedBy, browser);
		
		By transferBy = By.cssSelector(".popover .o_object_selection a.o_object_selection_apply"); 
		OOGraphene.waitElement(transferBy, browser).click();
		OOGraphene.waitElementDisappears(openOlatOrgBy, 5, browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_curriculum_form button.btn.o_button_dirty");
		OOGraphene.click(saveBy, browser);
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CurriculumPage openCurriculum(String name) {
		By curriculumBy = By.xpath("//div[@class='o_sel_curriculum_management']//table//td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(curriculumBy, browser).click();
		
		By titleBy = By.xpath("//div[@class='o_curriculum_dashboard_title']/h2[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public CurriculumComposerPage openImplementationsTab() {
		By composerBy = By.xpath("//div[div[contains(@class,'o_curriculum_title')]]//ul/li[@class='o_sel_curriculum_composer']/a");
		OOGraphene.waitElement(composerBy, browser);
		browser.findElement(composerBy).click();
		
		return new CurriculumComposerPage(browser).assertOnCurriculumComposer();
	}
}
