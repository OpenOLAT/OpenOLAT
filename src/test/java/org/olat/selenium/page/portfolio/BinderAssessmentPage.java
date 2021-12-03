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
package org.olat.selenium.page.portfolio;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * 
 * Initial date: 30 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderAssessmentPage {
	
	private final WebDriver browser;

	public BinderAssessmentPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BinderAssessmentPage passed(String section) {
		By checkBy = By.xpath("//div[contains(@class,'o_table_edit')]//tr[td[contains(text(),'" + section +"')]]/td/div/label/input[@type='checkbox']");
		WebElement checkEl = browser.findElement(checkBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderAssessmentPage assertPassed(int numOfPassed) {
		By passedBy = By.cssSelector("div.o_state.o_passed");
		List<WebElement> passedEls = browser.findElements(passedBy);
		Assert.assertEquals(numOfPassed, passedEls.size());
		return this;
	}
	
	public BinderAssessmentPage close(String section) {
		By checkBy = By.xpath("//div[contains(@class,'o_table_edit')]//tr[td[contains(text(),'" + section +"')]]/td/a[contains(@class,'o_sel_pf_close_section')]");
		browser.findElement(checkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderAssessmentPage save() {
		By saveBy = By.cssSelector("button.o_sel_pf_assessment_save");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderAssessmentPage done() {
		By saveBy = By.cssSelector("a.o_sel_pf_assessment_save_done");
		OOGraphene.waitElement(saveBy, 5, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		By reopenBy = By.cssSelector("a.o_sel_pf_assessment_reopen");
		OOGraphene.waitElement(reopenBy, 5, browser);
		return this;
	}
}
