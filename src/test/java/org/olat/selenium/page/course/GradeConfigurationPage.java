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
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 6 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradeConfigurationPage {
	
	private final WebDriver browser;
	
	public GradeConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GradeConfigurationPage assertOnConfiguration() {
		By generalBy = By.cssSelector("fieldset.o_sel_grade_scale_general");
		OOGraphene.waitElement(generalBy, browser);
		return this;
	}
	
	public GradeConfigurationPage selectSwissGradeSystem() {
		return selectGradeSystem("noten.ch", "grading.swiss");
	}
	
	public GradeConfigurationPage selectGradeSystem(String typeGerman, String typeEnglish) {
		By selectBy = By.cssSelector("fieldset.o_sel_grade_scale_general a.o_sel_grade_system_select");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_grade_select_system");
		
		By systemBy = By.xpath("//div[@id='o_cograde_system_SELBOX']//select");
		WebElement systemEl = browser.findElement(systemBy);
		By optionBy = By.xpath("//div[@id='o_cograde_system_SELBOX']//select/option[text()[contains(.,'" + typeGerman + "') or (contains(.,'" + typeEnglish+ "'))]]");
		WebElement optionEl = browser.findElement(optionBy);
		
		new Select(systemEl).selectByValue(optionEl.getAttribute("value"));

		By chooseBy = By.cssSelector("fieldset.o_sel_grade_select_system button.btn.btn-primary");
		browser.findElement(chooseBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_grade_select_system");
		
		return this;
	}
	
	public GradeConfigurationPage assertOnSwissNumericalGradeScale( ) {
		By sixBy = By.xpath("//table[contains(@class,'o_gr_grade_score_table')]//td/span[@class='o_grs_oo_grades_ch'][text()[contains(.,'6')]]");
		OOGraphene.waitElement(sixBy, browser);
		return this;
	}
	
	public GradeConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector("div.modal-body div.o_button_group button.o_sel_grade_scale_save");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

}
