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
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCEConfigurationPage {

	private final WebDriver browser;
	
	public AssessmentCEConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentCEConfigurationPage selectConfiguration() {
		By configBy = By.className("o_sel_course_ms_score");
		return selectTab(configBy);
	}
	
	public AssessmentCEConfigurationPage setScoreAuto(float minVal, float maxVal, float cutVal) {
		By scoreBy = By.cssSelector(".o_sel_course_ms_score input[type='checkbox']");
		browser.findElement(scoreBy).click();
		OOGraphene.waitBusy(browser);
		
		By minValBy = By.cssSelector(".o_sel_course_ms_min_val input[type='text']");
		WebElement minValEl = browser.findElement(minValBy);
		minValEl.clear();
		minValEl.sendKeys(Float.toString(minVal));
		
		By maxValBy = By.cssSelector(".o_sel_course_ms_max_val input[type='text']");
		WebElement maxValEl = browser.findElement(maxValBy);
		maxValEl.clear();
		maxValEl.sendKeys(Float.toString(maxVal));
		
		By displayAutoBy = By.cssSelector("#o_coform_passed_type input[type='radio'][value='true']");
		browser.findElement(displayAutoBy).click();
		OOGraphene.waitBusy(browser);

		By cutValBy = By.cssSelector(".o_sel_course_ms_cut_val input[type='text']");
		WebElement cutValEl = browser.findElement(cutValBy);
		cutValEl.clear();
		cutValEl.sendKeys(Float.toString(cutVal));
		
		By saveBy = By.cssSelector(".o_sel_course_ms_form button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	private AssessmentCEConfigurationPage selectTab(By tabBy) {
		OOGraphene.selectTab("o_node_config", tabBy, browser);
		return this;
	}
}
