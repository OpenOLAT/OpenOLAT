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

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 6 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentPage {

	private final WebDriver browser;
	
	public AssessmentPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentPage assertOnFailed() {
		By failedBy = By.cssSelector("div.o_state.o_failed");
		OOGraphene.waitElement(failedBy, browser);
		WebElement failedEl = browser.findElement(failedBy);
		Assert.assertTrue(failedEl.isDisplayed());
		return this;
	}
	
	public AssessmentPage assertOnPassed() {
		By passedBy = By.cssSelector("div.o_state.o_passed");
		OOGraphene.waitElement(passedBy, browser);
		WebElement passedEl = browser.findElement(passedBy);
		Assert.assertTrue(passedEl.isDisplayed());
		return this;
	}
	
	public AssessmentPage assertOnSwissGrade(String grade) {
		By gradeBy = By.xpath("//div[contains(@class,'o_personal')]//span[@class='o_grs_oo_grades_ch'][text()[contains(.,'" + grade + "')]]");
		OOGraphene.waitElement(gradeBy, browser);
		return this;
	}
	
	
}
