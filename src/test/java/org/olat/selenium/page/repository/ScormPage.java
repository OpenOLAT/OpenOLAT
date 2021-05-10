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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Drive the SCORM page
 * 
 * Initial date: 04.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScormPage {
	
	private WebDriver browser;
	
	private ScormPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static ScormPage getScormPage(WebDriver browser) {
		By mainBy = By.id("o_main_wrapper");
		OOGraphene.waitElement(mainBy, browser);
		return new ScormPage(browser);
	}
	
	public ScormPage start() {
		assertOnStart();
		By startBy = By.cssSelector("button.o_sel_start_scorm");
		browser.findElement(startBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ScormPage assertOnStart() {
		By startBy = By.cssSelector("button.o_sel_start_scorm");
		OOGraphene.waitElement(startBy, browser);
		return this;
	}
	
	public ScormPage passVerySimpleScorm() {
		By frameBy = By.cssSelector("iframe.o_iframe_rel");
		OOGraphene.waitElement(frameBy, browser);
		browser.switchTo().frame("scormContentFrame");
		
		By val0By = By.cssSelector("input[value='0']");
		OOGraphene.waitElement(val0By, browser);
		browser.findElement(val0By).click();
		By val3By = By.cssSelector("input[value='3']");
		browser.findElement(val3By).click();
		
		By submitBy = By.id("submit_scorm_datas");
		browser.findElement(submitBy).click();
		OOGraphene.waitingALittleBit();
		
		browser.switchTo().defaultContent();
		return this;
	}
	
	/**
	 * Check the score of SCORM on the start page of the
	 * course element.
	 * 
	 * @param score
	 * @return Itself
	 */
	public ScormPage assertOnScormScore(int score) {
		By resultsBy = By.xpath("//div[contains(@class,'o_personal')]//tr[contains(@class,'o_score')]/td[contains(text(),'" + score + "')]");
		OOGraphene.waitElement(resultsBy, browser);
		return this;
	}
	
	/**
	 * Check if the SCORM is passed on the course element
	 * start page.
	 * 
	 * @return Itself
	 */
	public ScormPage assertOnScormPassed() {
		By notPassedBy = By.cssSelector("div.o_personal tr.o_state.o_passed ");
		OOGraphene.waitElement(notPassedBy, 5, browser);
		return this;
	}
	
	/**
	 * Click the back top left of the toolbar of SCORM content
	 * 
	 * @return Itself
	 */
	public ScormPage back() {
		By backBy = By.className("o_link_back");
		OOGraphene.waitElement(backBy, browser);
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
