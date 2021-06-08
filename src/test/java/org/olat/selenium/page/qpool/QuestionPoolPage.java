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
package org.olat.selenium.page.qpool;

import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 26 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolPage {
	
	private final WebDriver browser;
	
	public QuestionPoolPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QuestionPoolPage assertOnQuestionPool() {
		By mainBy = By.className("o_qpool");
		OOGraphene.waitElement(mainBy, browser);
		return this;
	}
	
	/**
	 * Select the node my questions.
	 * 
	 * @return Itself
	 */
	public QuestionPoolPage selectMyQuestions() {
		By myQuestionsBy = By.xpath("//div[contains(@class,'o_tree')]/ul[@class='o_tree_l0']//a[i[contains(@class,'o_sel_qpool_my_items')]]");
		OOGraphene.waitElement(myQuestionsBy, browser);
		browser.findElement(myQuestionsBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.id("qitems"), browser);
		return this;
	}
	
	/**
	 * Select a node in the review process.
	 * 
	 * @param name The name of the taxonomy level
	 * @return Itself
	 */
	public QuestionPoolPage selectTaxonomyLevel(String name) {
		By leafBy = By.xpath("//li[contains(@class,'o_sel_qpool_review_taxonomy_levels')]/ul/li/div[contains(@class,'o_tree_l1')]/span/a[contains(@onclick,'ctncl')][span[text()[contains(.,'" + name + "')]]]");
		OOGraphene.waitElement(leafBy, browser);
		browser.findElement(leafBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select a question in a list.
	 * 
	 * @param title The title of the question
	 * @return Itself
	 */
	public QuestionPoolPage selectQuestionInList(String title) {
		By rowBy = By.xpath("//div[contains(@class,'o_table_flexi')]//tr[td/a[text()[contains(.,'" + title + "')]]]/td/a[contains(@onclick,'select-item')]");
		OOGraphene.waitElement(rowBy, browser);
		browser.findElement(rowBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QuestionPoolPage assessQuestion(int numOfStars) {
		By assessmentBy = By.xpath("//div[contains(@class,'o_instruction')]/div[@class='o_button_group']/a[contains(@onclick,'process.activate.review')]");
		OOGraphene.waitElement(assessmentBy, browser);
		browser.findElement(assessmentBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By numOfStarsBy = By.xpath("//fieldset[contains(@class,'o_sel_qpool_review_form')]//div[contains(@class,'o_rating')]/div[contains(@class,'o_rating_items')]/a[" + numOfStars + "]");
		OOGraphene.waitElement(numOfStarsBy, browser);
		browser.findElement(numOfStarsBy).click();
		OOGraphene.waitBusy(browser);
		
		By saveBy = By.cssSelector("div.modal-content fieldset.o_sel_qpool_review_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public QuestionPoolPage newQuestion(String title, QTI21QuestionType type) {
		By newQuestionBy = By.xpath("//a[i[contains(@class,'o_icon_qitem_new')]]");
		browser.findElement(newQuestionBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_new_item_form div.o_sel_item_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By typeBy = By.cssSelector("fieldset.o_sel_new_item_form div.o_sel_item_type select");
		WebElement typeEl = browser.findElement(typeBy);
		new Select(typeEl).selectByValue("qti21_" + type.name());
		
		By saveBy = By.cssSelector("fieldset.o_sel_new_item_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		
		By assessmentItemBy = By.cssSelector("ul.o_sel_assessment_item_config");
		OOGraphene.waitElement(assessmentItemBy, browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	/**
	 * @return Access to the metadata panel
	 */
	public QuestionMetadataPage metadata() {
		return QuestionMetadataPage.getPage(browser);
	}
	
	/**
	 * Start the process, confirm with the first taxonomy level found.
	 * 
	 * @return Itself
	 */
	public QuestionPoolPage startReviewProcess() {
		By startProcessBy = By.xpath("//div[contains(@class,'o_button_group')]/a[contains(@onclick,'process.activate.start.review')]");
		OOGraphene.waitElement(startProcessBy, browser);
		browser.findElement(startProcessBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		By confirmBy = By.cssSelector("fieldset.o_sel_qpool_confirm_start_form button.btn.btn-primary");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public QuestionPoolPage assertQuestionInList(String title, String type) {
		By rowBy = By.xpath("//div[contains(@class,'o_table_flexi')]//tr[td[text()='" + type + "']]/td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(rowBy, browser);
		return this;
	}
	
	public QuestionPoolPage openQuickView(String title) {
		By quickViewBy = By.xpath("//div[contains(@class,'o_table_flexi')]//tr[td/a[text()[contains(.,'" + title + "')]]]/td/div/a[contains(@onclick,'tt-details')]");
		OOGraphene.waitElement(quickViewBy, browser);
		browser.findElement(quickViewBy).click();
		
		By viewBy = By.cssSelector("tr.o_table_row_details>td #o_qti_container");
		OOGraphene.waitElement(viewBy, browser);
		return this;
	}
	
	/**
	 * Check if the mark "Final" is visible in the toolbar.
	 * 
	 * @return Itself
	 */
	public QuestionPoolPage assertFinalOnQuestion() {
		By finalBy = By.cssSelector("li.o_tool a.o_qpool_tools_status.o_qpool_status_finalVersion");
		OOGraphene.waitElement(finalBy, browser);
		return this;
	}
	
	public QuestionPoolPage assertFinalQuestionInList(String title) {
		By rowBy = By.xpath("//div[contains(@class,'o_table_flexi')]//tr[td/div/span[contains(@class,'o_qpool_status_finalVersion_light')]]/td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(rowBy, browser);
		return this;
	}
	
	public QuestionPoolPage clickToolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		OOGraphene.waitBusy(browser);
		
		By tableBy = By.cssSelector("div.o_table_flexi.o_rendertype_classic");
		OOGraphene.waitElement(tableBy, browser);
		return this;
	}
}
