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
public class QTI21MatchEditorPage extends QTI21AssessmentItemEditorPage {
	
	public QTI21MatchEditorPage(WebDriver browser) {
		super(browser);
	}
	
	public QTI21MatchEditorPage setSource(int position, String text) {
		String containerCssSelector = "th.o_sel_match_source_" + position;
		OOGraphene.tinymce(text, containerCssSelector, browser);
		return this;
	}
	
	public QTI21MatchEditorPage setTarget(int position, String text) {
		String containerCssSelector = "th.o_sel_match_target_" + position;
		OOGraphene.tinymce(text, containerCssSelector, browser);
		return this;
	}
	
	/**
	 * Set if the answer is correct or wrong.
	 * 
	 * @param position
	 * @param correct
	 * @return Itself
	 */
	public QTI21MatchEditorPage setMatch(int source, int target, boolean correct) {
		By answerBy = By.xpath("//td[contains(@class,'o_sel_match_" + source + "_" + target + "')]/input[contains(@id,'oo_')]");
		WebElement matchEl = browser.findElement(answerBy);
		OOGraphene.check(matchEl, correct);
		return this;
	}
	
	public QTI21MatchEditorPage addColumn() {
		By columnsBy = By.xpath("//th[contains(@class,'o_sel_match_target_')]");
		int numOfColumns = browser.findElements(columnsBy).size();
		By saveBy = By.cssSelector("div.o_sel_match_save a.o_sel_match_add_column");
		OOGraphene.moveAndClick(saveBy, browser);
		OOGraphene.waitElement(By.cssSelector("th.o_sel_match_target_" + numOfColumns), browser);
		return this;
	}
	
	public QTI21MatchEditorPage addRow() {
		By rowsBy = By.xpath("//th[contains(@class,'o_sel_match_source_')]");
		int numOfRows = browser.findElements(rowsBy).size();
		By saveBy = By.cssSelector("div.o_sel_match_save a.o_sel_match_add_row");
		OOGraphene.moveAndClick(saveBy, browser);
		OOGraphene.waitElement(By.cssSelector("th.o_sel_match_source_" + numOfRows), browser);
		return this;
	}
	
	public QTI21MatchEditorPage setSingleChoices() {
		By singleBy = By.cssSelector("div.o_sel_match_single input[type='radio'][value='single']");
		browser.findElement(singleBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21MatchEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_match_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21MatchScoreEditorPage selectScores() {
		selectTab("o_sel_assessment_item_score", By.className("o_sel_assessment_item_options"));
		return new QTI21MatchScoreEditorPage(browser);
	}
	
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab("o_sel_assessment_item_feedback", By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}
}
