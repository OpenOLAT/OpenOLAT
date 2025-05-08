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
 * Initial date: 6 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumComposerPage {
	
	private final WebDriver browser;
	
	public CurriculumComposerPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CurriculumComposerPage assertOnCurriculumComposer() {
		By implementationsTitleBy = By.xpath("//div[@class='o_header_with_buttons']/h3[i[contains(@class,'o_icon_curriculum_implementations')]]");
		OOGraphene.waitElement(implementationsTitleBy, browser);
		return this;
	}
	
	public CurriculumComposerPage addCurriculumElement(String displayName, String externalRef, String type) {
		By dropdowBy = By.cssSelector(".o_button_group button.o_sel_curriculum_new_elements");
		OOGraphene.waitElement(dropdowBy, browser).click();
		
		By menuBy = By.cssSelector("ul.dropdown-menu.o_sel_curriculum_new_elements");
		OOGraphene.waitElement(menuBy, browser);
		
		By elementWithTypeBy = By.xpath("//ul[contains(@class,'o_sel_curriculum_new_elements')]/li/a[span[text()[contains(.,'" + type + "')]]]");
		OOGraphene.waitElement(elementWithTypeBy, browser).click();
		
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_curriculum_element_metadata");
		
		By displayNameBy = By.cssSelector("fieldset.o_sel_curriculum_element_metadata .o_sel_curriculum_element_displayname input[type='text']");
		browser.findElement(displayNameBy).sendKeys(displayName);
		
		By identifierBy = By.cssSelector("fieldset.o_sel_curriculum_element_metadata .o_sel_curriculum_element_identifier input[type='text']");
		browser.findElement(identifierBy).sendKeys(externalRef);
		
		By saveBy = By.cssSelector("fieldset.o_sel_curriculum_element_metadata button.btn.o_button_dirty");
		OOGraphene.click(saveBy, browser);
		
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}
	
	public CurriculumComposerPage assertOnCurriculumElementInTable(String name) {
		By elementBy = By.xpath("//div[contains(@class,'o_curriculum_el_listing')]//table//td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(elementBy, browser);
		return this;
	}
	
	public CurriculumElementPage selectCurriculumElementInTable(String name) {
		By elementBy = By.xpath("//div[contains(@class,'o_curriculum_el_listing')]//table//td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(elementBy, browser);
		browser.findElement(elementBy).click();
		return new CurriculumElementPage(browser);
	}


}
