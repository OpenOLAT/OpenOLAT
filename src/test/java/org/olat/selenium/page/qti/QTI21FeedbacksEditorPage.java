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

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 4 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21FeedbacksEditorPage {
	
	private final WebDriver browser;
	
	public QTI21FeedbacksEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21FeedbacksEditorPage setHint(String title, String hint) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_hint");
		
		By titleBy = By.cssSelector("div.o_sel_assessment_item_hint_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By hintBy = By.cssSelector("div.o_sel_assessment_item_hint_feedback input[type='text']");
		browser.findElement(hintBy).sendKeys(hint);
		return this;
	}
	
	public QTI21FeedbacksEditorPage setCorrectSolution(String title, String correctSolution) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_correct_solution");
		
		By titleBy = By.cssSelector("div.o_sel_assessment_item_correctSolution_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);

		By correctBy = By.cssSelector("div.o_sel_assessment_item_correctSolution_feedback input[type='text']");
		browser.findElement(correctBy).sendKeys(correctSolution);
		return this;
	}
	
	public QTI21FeedbacksEditorPage setCorrectFeedback(String title, String feedback) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_correct");
		
		By titleBy = By.cssSelector("div.o_sel_assessment_item_correct_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By correctBy = By.cssSelector("div.o_sel_assessment_item_correct_feedback input[type='text']");
		browser.findElement(correctBy).sendKeys(feedback);
		return this;
	}
	
	public QTI21FeedbacksEditorPage setIncorrectFeedback(String title, String feedback) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_incorrect");
		
		By titleBy = By.cssSelector("div.o_sel_assessment_item_incorrect_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By correctBy = By.cssSelector("div.o_sel_assessment_item_incorrect_feedback input[type='text']");
		browser.findElement(correctBy).sendKeys(feedback);
		return this;
	}

	public QTI21FeedbacksEditorPage setAnsweredFeedback(String title, String feedback) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_answered");
		
		By titleBy = By.cssSelector("div.o_sel_assessment_item_answered_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By answeredBy = By.cssSelector("div.o_sel_assessment_item_answered_feedback input[type='text']");
		browser.findElement(answeredBy).sendKeys(feedback);
		return this;
	}
	
	public QTI21FeedbacksEditorPage setEmpytFeedback(String title, String feedback) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_empty");
		
		By titleBy = By.cssSelector("div.o_sel_assessment_item_empty_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By emptyBy = By.cssSelector("div.o_sel_assessment_item_empty_feedback input[type='text']");
		browser.findElement(emptyBy).sendKeys(feedback);
		return this;
	}
	
	private QTI21FeedbacksEditorPage addFeedback(String cssSelector) {
		By addFeedbackBy = By.cssSelector("a." + cssSelector);
		browser.findElement(addFeedbackBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	private QTI21FeedbacksEditorPage openAddFeedbacksMenu() {
		By addMenuCaretBy = By.cssSelector("button.o_sel_add_feedbacks");
		WebElement addMenuCaret = browser.findElement(addMenuCaretBy);
		Assert.assertTrue(addMenuCaret.isDisplayed());
		addMenuCaret.click();

		By addMenuBy = By.cssSelector("ul.o_sel_add_feedbacks");
		OOGraphene.waitElement(addMenuBy, 5, browser);
		return this;
	}

	public QTI21FeedbacksEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_assessment_item_feedbacks button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
