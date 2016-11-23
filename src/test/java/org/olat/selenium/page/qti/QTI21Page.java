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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 06.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21Page {
	
	private final By toolsMenu = By.cssSelector("ul.o_sel_repository_tools");
	private final By settingsMenu = By.cssSelector("ul.o_sel_course_settings");
	
	private WebDriver browser;
	
	private QTI21Page(WebDriver browser) {
		this.browser = browser;
	}
	
	public static QTI21Page getQTI12Page(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
		return new QTI21Page(browser);
	}
	
	public QTI21Page start() {
		By startBy = By.cssSelector("a.o_sel_start_qti21assessment");
		WebElement startButton = browser.findElement(startBy);
		startButton.click();
		OOGraphene.waitBusy(browser);
		By mainBy = By.cssSelector("div.qtiworks.o_assessmenttest");
		OOGraphene.waitElement(mainBy, 5, browser);
		return this;
	}
	
	public QTI21Page assertOnAttempts(int numOfAttemtps) {
		By attemptBy = By.xpath("//div[contains(@class,'o_course_run')]//table//tr[contains(@class,'o_attempts')]//td[text()[contains(.,'" + numOfAttemtps + "')]]");
		OOGraphene.waitElement(attemptBy, 5, browser);
		WebElement attemptEl = browser.findElement(attemptBy);
		Assert.assertTrue(attemptEl.isDisplayed());
		return this;
	}
	
	public QTI21Page assertOnAssessmentItem() {
		By assessmentItemBy = By.cssSelector("div.qtiworks.o_assessmentitem.o_assessmenttest");
		OOGraphene.waitElement(assessmentItemBy, 5, browser);
		return this;
	}
	
	//TODO still qti 1.2
	public QTI21Page selectItem(int position) {
		By itemsBy = By.cssSelector("a.o_sel_qti_menu_item");
		List<WebElement> itemList = browser.findElements(itemsBy);
		Assert.assertTrue(itemList.size() > position);
		WebElement itemEl = itemList.get(position);
		itemEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21Page answerSingleChoice(int selectPosition) {
		By itemsBy = By.cssSelector("div.choiceInteraction input[type='radio']");
		List<WebElement> optionList = browser.findElements(itemsBy);
		Assert.assertTrue(optionList.size() > selectPosition);
		WebElement optionEl = optionList.get(selectPosition);
		optionEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21Page answerSingleChoice(String answer) {
		By choiceBy = By.xpath("//tr[contains(@class,'choiceinteraction')][td[contains(@class,'choiceInteraction')][p[contains(text(),'" + answer + "')]]]/td[contains(@class,'control')]/input[@type='radio']");
		browser.findElement(choiceBy).click();
		return this;
	}

	public QTI21Page answerMultipleChoice(String... answers) {
		for(String answer:answers) {
			By choiceBy = By.xpath("//tr[contains(@class,'choiceinteraction')][td[contains(@class,'choiceInteraction')][p[contains(text(),'" + answer + "')]]]/td[contains(@class,'control')]/input[@type='checkbox']");
			browser.findElement(choiceBy).click();
		}
		return this;
	}
	
	public QTI21Page answerHotspot(String shape) {
		By areaBy = By.cssSelector("div.hotspotInteraction area[shape='" + shape + "']");
		OOGraphene.waitElement(areaBy, 5, browser);
		browser.findElement(areaBy).click();
		return this;
	}

	public QTI21Page answerCorrectKPrim(String... correctChoices) {
		for(String correctChoice:correctChoices) {
			By correctBy = By.xpath("//tr[td/p[contains(text(),'" + correctChoice + "')]]/td[contains(@class,'o_qti_item_kprim_input o_qti_item_kprim_input_correct')]/input[@type='checkbox']");
			WebElement option = browser.findElement(correctBy);
			OOGraphene.check(option, Boolean.TRUE);
		}
		return this;
	}
	
	public QTI21Page answerIncorrectKPrim(String... incorrectChoices) {
		for(String incorrectChoice:incorrectChoices) {
			By correctBy = By.xpath("//tr[td/p[contains(text(),'" + incorrectChoice + "')]]/td[contains(@class,'o_qti_item_kprim_input o_qti_item_kprim_input_wrong')]/input[@type='checkbox']");
			WebElement option = browser.findElement(correctBy);
			OOGraphene.check(option, Boolean.TRUE);
		}
		return this;
	}
	
	public QTI21Page answerGapText(String text, String responseId) {
		By gapBy = By.xpath("//span[contains(@class,'textEntryInteraction')]/input[@type='text'][contains(@name,'" + responseId + "')]");
		WebElement gapEl = browser.findElement(gapBy);
		gapEl.clear();
		gapEl.sendKeys(text);
		return this;
	}
	
	public QTI21Page saveAnswer() {
		By saveAnswerBy = By.cssSelector("button.o_sel_assessment_item_submit");
		browser.findElement(saveAnswerBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21Page nextAnswer() {
		By nextAnswerBy = By.cssSelector("button.o_sel_next_question");
		browser.findElement(nextAnswerBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21Page assertFeedback(String feedback) {
		By feedbackBy = By.xpath("//div[contains(@class,'modalFeedback')]/h4[contains(text(),'" + feedback + "')]");
		OOGraphene.waitElement(feedbackBy, 5, browser);
		List<WebElement> feedbackEls = browser.findElements(feedbackBy);
		Assert.assertEquals(1, feedbackEls.size());
		return this;
	}
	
	public QTI21Page endTest() {
		By endBy = By.cssSelector("a.o_sel_end_testpart");
		browser.findElement(endBy).click();
		OOGraphene.waitBusy(browser);
		confirm();
		return this;
	}
	
	public QTI21Page closeTest() {
		By endBy = By.cssSelector("a.o_sel_close_test");
		browser.findElement(endBy).click();
		OOGraphene.waitBusy(browser);
		confirm();
		return this;
	}
	
	public QTI21Page assertOnResults() {
		By resultsBy = By.cssSelector("div.o_sel_results_details");
		OOGraphene.waitElement(resultsBy, 5, browser);
		return this;
	}
	
	public QTI21Page assertOnAssessmentTestScore(int score) {
		By resultsBy = By.xpath("//div[contains(@class,'o_sel_results_details')]//tr[contains(@class,'o_sel_assessmenttest_score')]/td[contains(text(),'" + score + "')]");
		OOGraphene.waitElement(resultsBy, 5, browser);
		return this;
	}
	
	public QTI21Page assertOnAssessmentTestMaxScore(int score) {
		By resultsBy = By.xpath("//div[contains(@class,'o_sel_results_details')]//tr[contains(@class,'o_sel_assessmenttest_maxscore')]/td[contains(text(),'" + score + "')]");
		OOGraphene.waitElement(resultsBy, 5, browser);
		return this;
	}
	
	/**
	 * Yes in a dialog box controller.
	 */
	private void confirm() {
		// confirm
		By confirmButtonBy = By.cssSelector("div.modal-dialog div.modal-footer a");
		OOGraphene.waitElement(confirmButtonBy, 5, browser);
		List<WebElement> buttonsEl = browser.findElements(confirmButtonBy);
		buttonsEl.get(0).click();
		OOGraphene.waitBusy(browser);
	}
	
	public QTI21EditorPage edit() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}

		By editBy = By.xpath("//ul[contains(@class,'o_sel_repository_tools')]//a[contains(@onclick,'edit.cmd')]");
		browser.findElement(editBy).click();
		OOGraphene.waitBusy(browser);
		QTI21EditorPage editor = new QTI21EditorPage(browser);
		editor.assertOnEditor();
		return editor;
	}
	
	public QTI21OptionsPage options() {
		if(!browser.findElement(settingsMenu).isDisplayed()) {
			openSettingsMenu();
		}
		
		By optionsBy = By.cssSelector("ul.o_sel_course_settings a.o_sel_qti_resource_options");
		browser.findElement(optionsBy).click();
		OOGraphene.waitBusy(browser);
		return new QTI21OptionsPage(browser);
	}
	
	/**
	 * Click the editor link in the tools drop-down
	 * @return
	 */
	public QTI21Page openToolsMenu() {
		By toolsMenuCaret = By.cssSelector("a.o_sel_repository_tools");
		browser.findElement(toolsMenuCaret).click();
		OOGraphene.waitElement(toolsMenu, browser);
		return this;
	}
	
	public QTI21Page openSettingsMenu() {
		By settingsMenuCaret = By.cssSelector("a.o_sel_course_settings");
		browser.findElement(settingsMenuCaret).click();
		OOGraphene.waitElement(settingsMenu, browser);
		return this;
	}
	
	public QTI21Page clickToolbarBack() {
		OOGraphene.closeBlueMessageWindow(browser);
		browser.findElement(NavigationPage.toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return QTI21Page.getQTI12Page(browser);
	}
}
