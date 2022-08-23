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
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21InlineChoiceEditorPage extends QTI21AssessmentItemEditorPage {
	
	public QTI21InlineChoiceEditorPage(WebDriver browser) {
		super(browser);
	}
	
	public QTI21InlineChoiceEditorPage appendContent(String text) {
		String textSelector = ".o_sel_assessment_item_inlinechoice_text";
		OOGraphene.tinymceInsert(text, textSelector, browser);
		return this;
	}
	
	/**
	 * Add a new inline choice. Use the placeholder to locate
	 * the gap during the test.
	 * 
	 * @param solution The first answer
	 * @return Itself
	 */
	public QTI21InlineChoiceEditorPage addInlineChoice(String answer) {
		By addBy = By.xpath("//div[contains(@class,'o_sel_assessment_item_inlinechoice_text')]//button[contains(@title,'L\u00FCckentext')]");
		browser.findElement(addBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By firstAnswerBy = By.cssSelector("fieldset.o_sel_inlinechoice_form .o_sel_choice_1 input[type='text']");
		browser.findElement(firstAnswerBy).sendKeys(answer);
		return this;
	}
	
	public QTI21InlineChoiceEditorPage addChoice(String choice, int index) {
		By addChoiceBy = By.cssSelector("fieldset.o_sel_inlinechoice_form .o_sel_choice_" + (index -1) + " a.o_sel_add_choice");
		browser.findElement(addChoiceBy).click();

		By newChoiceBy = By.cssSelector("fieldset.o_sel_inlinechoice_form .o_sel_choice_" + index + " input[type='text']");
		OOGraphene.waitElement(newChoiceBy, browser);
		browser.findElement(newChoiceBy).sendKeys(choice);
		return this;
	}
	
	public QTI21InlineChoiceEditorPage setCorrect(int index) {
		By correctBy = By.cssSelector("fieldset.o_sel_inlinechoice_form .o_sel_choice_" + index + " input[type='radio']");
		browser.findElement(correctBy).click();
		return this;
	} 
	
	/**
	 * Edit an existing gap entry of type text. Use the placeholder to locate
	 * the gap during the test.
	 * 
	 * @param solution The solution
	 * @param placeholder The placeholder
	 * @param index The index of the entry in the paragraph
	 * @return Itself
	 */
	public QTI21InlineChoiceEditorPage editInlineChoice(String solution, String placeholder, int index) {
		By frameBy = By.cssSelector("div.o_sel_assessment_item_inlinechoice_text div.tox-edit-area iframe");
		WebElement frameEl = browser.findElement(frameBy);
		browser.switchTo().frame(frameEl);
		
		By inlineChoiceBy = By.xpath("//p/span[@class='inlinechoiceinteraction'][" + index + "]/a");
		browser.findElement(inlineChoiceBy).click();
		
		browser.switchTo().defaultContent();
		OOGraphene.waitModalDialog(browser);
		
		By firstAnswerBy = By.cssSelector("fieldset.o_sel_inlinechoice_form div.o_sel_choice_1 input[type=text]");
		WebElement answerEl = browser.findElement(firstAnswerBy);
		answerEl.clear();
		answerEl.sendKeys(solution);
		return this;
	}
	
	public QTI21InlineChoiceEditorPage saveInlineChoice() {
		By saveBy = By.cssSelector(".o_sel_inlinechoice_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Save the whole interaction.
	 * 
	 * @return Itself
	 */
	public QTI21InlineChoiceEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_inlinechoice_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the tab to edit the scores
	 * 
	 * @return The score page
	 */
	public QTI21InlineChoiceScoreEditorPage selectScores() {
		selectTab("o_sel_assessment_item_score", By.className("o_sel_assessment_item_options"));
		return new QTI21InlineChoiceScoreEditorPage(browser);
	}
	
	/**
	 * Select the tab to edit the feedbacks
	 * 
	 * @return the feedback page
	 */
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab("o_sel_assessment_item_feedback", By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}

}
