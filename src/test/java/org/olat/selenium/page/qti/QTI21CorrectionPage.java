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
package org.olat.selenium.page.qti;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 23 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21CorrectionPage {

	private final WebDriver browser;
	
	public QTI21CorrectionPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21CorrectionPage startTestCorrection() {
		By startBy = By.cssSelector("a.btn.o_sel_correction");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		OOGraphene.waitBusy(browser);
		return assertOnAssessmentItemsSelected();
	}
	
	public QTI21CorrectionPage assertOnAssessmentItemsSelected() {
		By itemsBy = By.cssSelector("ul.o_tools_segments a.btn-primary.o_sel_correction_assessment_items");
		OOGraphene.waitElement(itemsBy, browser);
		return this;
	}
	
	public QTI21CorrectionPage selectAssessmentItem(String questionTitle) {
		By questionBy = By.xpath("//div[contains(@class,'o_sel_correction_assessment_items_list')]//td/a[text()[contains(.,'" + questionTitle + "')]]");
		browser.findElement(questionBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * The assessment items list after selecting an identity in the list.
	 * 
	 * @param questionTitle The question title
	 * @param numberOfErrors The number of errors
	 * @return
	 */
	public QTI21CorrectionPage assertOnAssessmentItemError(String questionTitle, int numberOfErrors) {
		By questionBy = By.xpath("//div[contains(@class,'o_sel_correction_assessment_items_list')]//tr[td/a[text()[contains(.,'" + questionTitle + "')]]]/td/a[text()[contains(.,'" + numberOfErrors + "')]]/i[contains(@class,'o_icon_error')]");
		OOGraphene.waitElement(questionBy, browser);
		return this;
	}
	
	/**
	 * Assert an assessment item which is not corrected.
	 * 
	 * @param questionTitle The question title
	 * @param numberOfErrors
	 * @return
	 */
	public QTI21CorrectionPage assertOnAssessmentItemNotCorrected(String questionTitle, int numberOfPoints) {
		By questionBy = By.xpath("//div[contains(@class,'o_sel_correction_assessment_items_list')]//tr[td/a[text()[contains(.,'" + questionTitle + "')]]][td[text()[contains(.,'" + numberOfPoints + "')]]]/td/a/i[contains(@class,'o_icon_error')]");
		OOGraphene.waitElement(questionBy, browser);
		return this;
	}
	
	public QTI21CorrectionPage setScore(String score) {
		By scoreBy = By.cssSelector("div.o_assessmentitem_wrapper .o_sel_assessment_item_score input[type='text']");
		OOGraphene.waitElement(scoreBy, browser);
		browser.findElement(scoreBy).sendKeys(score);
		return this;
	}
	
	public QTI21CorrectionPage assertOnStatusOk() {
		By scoreBy = By.cssSelector("div.o_assessmentitem_wrapper .o_sel_assessment_item_status i.o_icon_ok");
		OOGraphene.waitElement(scoreBy, browser);
		return this;
	}
	
	public QTI21CorrectionPage save() {
		By saveBy = By.cssSelector("div.o_assessmentitem_wrapper .o_assessmentitem_scoring_buttons button.btn");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21CorrectionPage back() {
		By backBy = By.cssSelector("div.o_correction_navigation a.o_link_back");
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * 
	 * @return Itself
	 */
	public QTI21CorrectionPage publishAll() {
		By saveBy = By.cssSelector("a.o_sel_correction_save_tests");
		OOGraphene.waitElement(saveBy, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Publish the results of a single test (grader).
	 * 
	 * @return Itself
	 */
	public QTI21CorrectionPage publish() {
		By saveBy = By.cssSelector("a.o_sel_correction_save_test");
		OOGraphene.waitElement(saveBy, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21CorrectionPage confirmDialog() {
		By confirmButtonBy = By.cssSelector("div.modal-dialog div.modal-body button.btn-primary");
		OOGraphene.waitElement(confirmButtonBy, browser);
		browser.findElement(confirmButtonBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
}
