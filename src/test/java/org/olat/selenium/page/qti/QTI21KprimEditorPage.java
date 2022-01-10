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
 * Initial date: 9 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21KprimEditorPage extends QTI21AssessmentItemEditorPage {
	
	public QTI21KprimEditorPage(WebDriver browser) {
		super(browser);
	}
	
	/**
	 * Move the window to the 4 choices configuration.
	 * 
	 * @return Itself
	 */
	public QTI21KprimEditorPage moveToChoices() {
		OOGraphene.waitTinymce(browser);
		OOGraphene.waitingALittleBit();// wait focus probably
		OOGraphene.scrollTo(By.cssSelector("fieldset.o_kprim_choices_editor"), browser);
		return this;
	}
	
	/**
	 * Set if the answer is correct or wrong.
	 * 
	 * @param position
	 * @param correct
	 * @return Itself
	 */
	public QTI21KprimEditorPage setCorrect(int position, boolean correct) {
		By answerBy;
		if(correct) {
			answerBy = By.xpath("//div[contains(@class,'o_sel_choice_" + position + "')]//input[contains(@id,'oo_correct-')]");
		} else {
			answerBy = By.xpath("//div[contains(@class,'o_sel_choice_" + position + "')]//input[contains(@id,'oo_wrong-')]");
		}
		browser.findElement(answerBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21KprimEditorPage setAnswer(int position, String answer) {
		By answerBy = By.cssSelector("div.o_sel_choice_" + position + " input[type='text']");
		WebElement answerEl = browser.findElement(answerBy);
		answerEl.clear();
		answerEl.sendKeys(answer);
		return this;
	}
	
	public QTI21KprimEditorPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_choices_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21ChoicesScoreEditorPage selectScores() {
		selectTab(By.className("o_sel_assessment_item_options"));
		return new QTI21ChoicesScoreEditorPage(browser);
	}
	
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab(By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}
}
