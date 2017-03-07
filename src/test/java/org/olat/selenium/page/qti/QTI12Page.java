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
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Drive the test QTI 1.2 from OpenOLAT
 * 
 * Initial date: 11.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12Page {
	
	private final WebDriver browser;
	
	private QTI12Page(WebDriver browser) {
		this.browser = browser;
	}
	
	public static QTI12Page getQTI12Page(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
		return new QTI12Page(browser);
	}
	
	public QTI12Page passE4(UserVO user) {
		start()
			.selectItem("Single choice")
			.answerSingleChoice("Correct answer")
			.saveAnswer()
			.selectItem("Multiple choice")
			.answerMultipleChoice("Correct answer", "The answer is correct")
			.saveAnswer()
			.selectItem("Kprim")
			.answerKPrim(true, false, true, false)
			.saveAnswer()
			.selectItem("Fill-in")
			.answerFillin("not")
			.saveAnswer()
			.endTest();
		
		//check results page
		By resultsBy = By.id("o_qti_results");
		OOGraphene.waitElement(resultsBy, browser);
		WebElement resultsEl = browser.findElement(resultsBy);
		Assert.assertTrue(resultsEl.getText().contains(user.getFirstName()));
		//close the test
		closeTest();
		//all answers are correct -> passed
		By passedBy = By.cssSelector("tr.o_state.o_passed");
		OOGraphene.waitElement(passedBy, 5, browser);
		WebElement passedEl = browser.findElement(passedBy);
		Assert.assertTrue(passedEl.isDisplayed());
		return this;
	}
	
	public QTI12Page start() {
		By startBy = By.cssSelector("a.o_sel_start_qti12_test");
		WebElement startButton = browser.findElement(startBy);
		startButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI12Page selectItem(String title) {
		By itemBy = By.xpath("//div[@id='o_qti_menu']//div[contains(@class,'o_qti_menu_item')]/a[contains(normalize-space(.),'" + title + "')]");
		OOGraphene.waitElement(itemBy, 5, browser);
		browser.findElement(itemBy).click();
		OOGraphene.waitBusy(browser);
		By nextItemBy = By.cssSelector("#ofo_iq_item>div.o_qti_item>h3");
		OOGraphene.waitElement(nextItemBy, 5, browser);
		return this;
	}
	
	public QTI12Page answerSingleChoice(String choice) {
		By choiceBy = By.xpath("//label[contains(@for,'QTI')][span[contains(normalize-space(.),'" + choice + "')]]/input[@type='radio']");
		browser.findElement(choiceBy).click();
		return this;
	}
	
	public QTI12Page answerMultipleChoice(String... choices) {
		for(String choice:choices) {
			By choiceBy = By.xpath("//label[contains(@for,'QTI')][span[contains(normalize-space(.),'" + choice + "')]]/input[@type='checkbox']");
			browser.findElement(choiceBy).click();
		}
		return this;
	}
	
	public QTI12Page answerKPrim(boolean... choices) {
		By itemsBy = By.cssSelector("table.o_qti_item_kprim input[type='radio']");
		List<WebElement> optionList = browser.findElements(itemsBy);
		Assert.assertEquals(choices.length * 2, optionList.size());
		for(int i=0; i<choices.length; i++) {
			WebElement optionTrueEl = optionList.get(i * 2);
			WebElement optionFalseEl = optionList.get((i * 2) + 1);
			if(choices[i]) {
				optionTrueEl.click();
			} else {
				optionFalseEl.click();
			}
		}
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI12Page answerFillin(String... texts) {
		By holesBy = By.cssSelector("div.o_qti_item input[type='text']");
		List<WebElement> holesList = browser.findElements(holesBy);
		Assert.assertEquals(texts.length, holesList.size());
		for(int i=0; i<texts.length; i++) {
			holesList.get(i).sendKeys(texts[i]);
		}
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI12Page saveAnswer() {
		By saveAnswerBy = By.name("olat_fosm");
		WebElement saveAnswerButton = browser.findElement(saveAnswerBy);
		saveAnswerButton.click();
		OOGraphene.waitBusy(browser);
		By confirmationBy = By.cssSelector("#o_qti_run_content>div.o_important");
		OOGraphene.waitElement(confirmationBy, 5, browser);
		return this;
	}
	
	public QTI12Page endTest() {
		By endBy = By.cssSelector("div.o_button_group_right a");
		OOGraphene.waitElement(endBy, 5, browser);
		browser.findElement(endBy).click();
		//accept and go further
		Alert alert = browser.switchTo().alert();
		alert.accept();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public QTI12Page closeTest() {
		By closeBy = By.cssSelector("div.o_button_group.o_button_group_right a");
		OOGraphene.waitElement(closeBy, 5, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
