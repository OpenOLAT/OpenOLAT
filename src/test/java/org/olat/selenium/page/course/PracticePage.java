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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.qti.QTI21Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 30 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticePage {
	
	private final WebDriver browser;
	private final QTI21Page qti21Page;
	
	public PracticePage(WebDriver browser) {
		this.browser = browser;
		qti21Page = new QTI21Page(browser);
	}
	
	public PracticePage assertOnPractice() {
		By startBy = By.cssSelector("div.o_practice_run div.o_practice_start a.btn-primary");
		OOGraphene.waitElement(startBy, browser);
		return this;
	}
	
	public PracticePage startShuffled() {
		By startBy = By.cssSelector("div.o_practice_shuffle_play a.btn-primary");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		
		By itemBy = By.cssSelector("div.qtiworks.o_assessmentitem #itemBody");
		OOGraphene.waitElement(itemBy, browser);
		return this;
	}
	
	public PracticePage answerSingleChoice(String answer) {
		qti21Page.answerSingleChoice(answer);
		return this;
	}
	
	public PracticePage answerSingleChoiceWithParagraph(String answer) {
		qti21Page.answerSingleChoiceWithParagraph(answer);
		return this;
	}
	
	public PracticePage saveAnswer() {
		By answerBy = By.cssSelector("div.o_assessmentitem_controls button.btn-primary");
		browser.findElement(answerBy).click();
		return this;
	}
	
	public PracticePage assertOnCorrect() {
		By solutionBy = By.cssSelector(".o_assessmentitem .o_practice_correct");
		OOGraphene.waitElement(solutionBy, browser);
		return this;
	}
	
	public PracticePage assertOnSolution() {
		By solutionBy = By.cssSelector("#solutionBody");
		OOGraphene.waitElement(solutionBy, browser);
		return this;
	}
	
	public PracticePage nextQuestion() {
		By nextQuestionBy = By.cssSelector("div.o_assessmentitem_controls button.btn-primary");
		browser.findElement(nextQuestionBy).click();
		By correctBy = By.xpath("//div[@id='solutionBody' or contains(@class,'o_practice_correct')]");
		OOGraphene.waitElementDisappears(correctBy, 5, browser);
		return this;
	}
	
	public PracticePage assertOnResults(int procent) {
		By scoreBy = By.xpath("//div[contains(@class,'o_practice_data_correct')]//span[text()[contains(.,'" + procent + "%')]]");
		OOGraphene.waitElement(scoreBy, browser);
		return this;
	}
	
	public PracticePage nextSerie() {
		//o_sel_practice_next_serie
		return this;
	}
	
	public PracticePage backToOverview() {
		By backBy = By.cssSelector("a.o_sel_practice_back");
		OOGraphene.waitElement(backBy, browser);
		browser.findElement(backBy).click();
		return assertOnPractice();
	}
}
