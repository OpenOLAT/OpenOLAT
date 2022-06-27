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
 * Initial date: 03 may 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21MultipleChoiceEditorPage extends QTI21AssessmentItemEditorPage {
	
	public QTI21MultipleChoiceEditorPage(WebDriver browser) {
		super(browser);
	}
	
	/**
	 * Add a new choice.
	 * 
	 * @return Itself
	 */
	public QTI21MultipleChoiceEditorPage addChoice(int position) {
		By addBy = By.xpath("//div[contains(@class,'o_sel_add_choice_" + position + "')]/a");
		OOGraphene.moveAndClick(addBy, browser);
		//wait the next element
		By addedBy = By.xpath("//div[contains(@class,'o_sel_add_choice_" + (position + 1) + "')]/a");
		OOGraphene.waitElement(addedBy, browser);
		return this;
	}
	
	public QTI21MultipleChoiceEditorPage setCorrect(int position) {
		By correctCheckBy = By.xpath("//div[contains(@class,'o_sel_choice_" + position + "')]//input[contains(@id,'oo_correct-')]");
		WebElement correctCheckEl = browser.findElement(correctCheckBy);
		OOGraphene.check(correctCheckEl, true);
		return this;
	}
	
	public QTI21MultipleChoiceEditorPage setAnswer(int position, String answer) {
		By oneLineInputBy = By.cssSelector("div.o_sel_choice_" + position + " input[type='text']");
		OOGraphene.waitElement(oneLineInputBy, browser);
		WebElement oneLineInputEl = browser.findElement(oneLineInputBy);
		oneLineInputEl.clear();
		oneLineInputEl.sendKeys(answer);
		
		//String containerCssSelector = "div.o_sel_choice_" + position;
		//OOGraphene.tinymce(answer, containerCssSelector, browser);
		return this;
	}
	
	public QTI21MultipleChoiceEditorPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_choices_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21ChoicesScoreEditorPage selectScores() {
		selectTab("o_sel_assessment_item_score", By.className("o_sel_assessment_item_options"));
		return new QTI21ChoicesScoreEditorPage(browser);
	}
	
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab("o_sel_assessment_item_feedback", By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}
}
