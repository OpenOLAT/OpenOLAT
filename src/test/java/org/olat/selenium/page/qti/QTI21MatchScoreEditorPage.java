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

import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 10 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21MatchScoreEditorPage {
	
	private static final By choiceScoreTable = By.cssSelector("table.matchInteraction.score");
	
	private final WebDriver browser;
	
	public QTI21MatchScoreEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21MatchScoreEditorPage selectAssessmentMode(ScoreEvaluation mode) {
		By modeBy = By.cssSelector("#o_coassessment_mode input[value='" + mode.name() + "']");
		browser.findElement(modeBy).click();
		OOGraphene.waitBusy(browser);
		if(mode == ScoreEvaluation.allCorrectAnswers) {
			OOGraphene.waitElementDisappears(choiceScoreTable, 5, browser);
		} else if (mode == ScoreEvaluation.perAnswer) {
			OOGraphene.waitElement(choiceScoreTable, 5, browser);
		}
		return this;
	}
	
	public QTI21MatchScoreEditorPage setScore(int source, int target, String score) {
		By scoreBy = By.cssSelector("td.o_sel_match_" + source + "_" + target + " input[type='text']");
		WebElement scoreEl = browser.findElement(scoreBy);
		scoreEl.clear();
		scoreEl.sendKeys(score);
		return this;
	}
	
	public QTI21MatchScoreEditorPage setMaxScore(String maxScore) {
		By maxScoreBy = By.cssSelector("div.o_sel_assessment_item_max_score input[type='text']");
		WebElement maxScoreEl = browser.findElement(maxScoreBy);
		maxScoreEl.clear();
		maxScoreEl.sendKeys(maxScore);
		return this;
	}
	
	public QTI21MatchScoreEditorPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_assessment_item_options button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
