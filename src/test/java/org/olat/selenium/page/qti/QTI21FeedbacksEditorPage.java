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
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition.Variable;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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
	
	public QTI21FeedbacksEditorPage addConditionalFeedback(int position, String title, String feedback) {
		openAddFeedbacksMenu().addFeedback("o_sel_add_conditional");
		
		String prefix = "fieldset.o_sel_assessment_item_additional_" + position;
		By titleBy = By.cssSelector(prefix + " div.o_sel_assessment_item_additional_feedback_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By emptyBy = By.cssSelector(prefix + " div.o_sel_assessment_item_additional_feedback input[type='text']");
		browser.findElement(emptyBy).sendKeys(feedback);
		return this;
	}
	
	public QTI21FeedbacksEditorPage setCondition(int feedbackPosition, int conditionPosition,
			ModalFeedbackCondition.Variable variable, ModalFeedbackCondition.Operator operator, String value) {

		String conditionPrefix = "//fieldset[contains(@class,'o_sel_assessment_item_additional_" + feedbackPosition + "')]"
				+ "//div[contains(@class,'o_condition_" + conditionPosition + "')]";
		
		By conditionBy = By.xpath(conditionPrefix);
		OOGraphene.waitElement(conditionBy, browser);
		
		By variableBy = By.xpath(conditionPrefix + "//select[contains(@id,'o_fiovar_')]");
		WebElement variableEl = browser.findElement(variableBy);
		new Select(variableEl).selectByValue(variable.name());
		OOGraphene.waitBusy(browser);
		
		By operatorBy = By.xpath(conditionPrefix + "//select[contains(@id,'o_fioope_')]");
		WebElement operatorEl = browser.findElement(operatorBy);
		new Select(operatorEl).selectByValue(operator.name());
		
		if(variable == Variable.attempts || variable == Variable.score) {
			By valueBy = By.xpath(conditionPrefix + "//input[@type='text']");
			WebElement valueEl = browser.findElement(valueBy);
			valueEl.clear();
			valueEl.sendKeys(value);
		} else if(variable == Variable.response) {
			By answerBy = By.xpath(conditionPrefix + "//select[contains(@id,'o_fioans_')]");
			WebElement answerEl = browser.findElement(answerBy);
			new Select(answerEl).selectByVisibleText(value);
		}
		return this;
	}
	
	public QTI21FeedbacksEditorPage addCondition(int feedbackPosition, int conditionPosition) {
		String conditionXpath = "//fieldset[contains(@class,'o_sel_assessment_item_additional_" + feedbackPosition + "')]"
				+ "//div[contains(@class,'o_condition_" + conditionPosition + "')]"
				+ "//a[contains(@class,'btn-default')][i[contains(@class,'o_icon_add')]]";
		
		By addConditionBy = By.xpath(conditionXpath);
		OOGraphene.waitElement(addConditionBy, browser);
		browser.findElement(addConditionBy).click();
		OOGraphene.waitBusy(browser);
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
		OOGraphene.scrollTop(browser);
		return this;
	}
}
