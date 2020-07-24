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
 * Initial date: 3 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListConfigPage {
	
	private final WebDriver browser;
	
	public CheckListConfigPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CheckListConfigPage selectListConfiguration() {
		OOGraphene.scrollTop(browser);
		By configBy = By.className("o_sel_cl_edit_checklist");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	public CheckListConfigPage addCheckbox(String title, int score) {
		By addCheckboxBy = By.className("o_sel_cl_new_checkbox");
		OOGraphene.waitElement(addCheckboxBy, browser);
		browser.findElement(addCheckboxBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_cl_edit_checkbox_form input.o_sel_cl_checkbox_title[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		if(score > 0) {
			By awardPoints = By.cssSelector("fieldset.o_sel_cl_edit_checkbox_form div.o_sel_cl_checkbox_award_points input[type='checkbox']");
			browser.findElement(awardPoints).click();
			OOGraphene.waitBusy(browser);
			
			By pointsBy = By.cssSelector("fieldset.o_sel_cl_edit_checkbox_form input.o_sel_cl_checkbox_points[type='text']");
			OOGraphene.waitElement(pointsBy, browser);
			browser.findElement(pointsBy).sendKeys(Integer.toString(score));
		}
		
		By saveBy = By.cssSelector("fieldset.o_sel_cl_edit_checkbox_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CheckListConfigPage assertOnCheckboxInList(String title) {
		By checkboxBy = By.xpath("//fieldset[contains(@class,'o_sel_cl_edit_checklist')]//div[contains(@class,'o_table_flexi')]/table//td[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(checkboxBy, browser);
		return this;
	}
	
	public CheckListConfigPage selectAssessmentConfiguration() {
		By configBy = By.className("o_sel_cl_edit_assessment");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	public CheckListConfigPage setScoring(int minScore, int maxScore, int cutValue) {
		By minScoreBy = By.cssSelector("fieldset.o_sel_cl_edit_assessment input.o_sel_cl_min_score[type='text']");
		OOGraphene.waitElement(minScoreBy, browser);
		browser.findElement(minScoreBy).sendKeys(Integer.toString(minScore));
		
		By maxScoreBy = By.cssSelector("fieldset.o_sel_cl_edit_assessment input.o_sel_cl_max_score[type='text']");
		browser.findElement(maxScoreBy).sendKeys(Integer.toString(maxScore));
		
		By cutValueScoreBy = By.cssSelector("fieldset.o_sel_cl_edit_assessment input.o_sel_cl_cut_value[type='text']");
		browser.findElement(cutValueScoreBy).sendKeys(Integer.toString(cutValue));
		
		return this;
	}
	
	public CheckListConfigPage saveAssessmentConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_cl_edit_assessment button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}

}
