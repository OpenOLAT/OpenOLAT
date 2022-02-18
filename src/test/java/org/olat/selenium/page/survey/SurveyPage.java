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
package org.olat.selenium.page.survey;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 27 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SurveyPage {

	private final By toolsMenu = By.cssSelector("ul.o_sel_repository_tools");
	
	private final WebDriver browser;
	
	public SurveyPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static SurveyPage loadPage(WebDriver browser) {
		return new SurveyPage(browser)
				.assertOnSurvey();
	}
	
	public SurveyPage assertOnSurvey() {
		By pageBy = By.cssSelector("div.o_evaluation_execution.o_page_content");
		OOGraphene.waitElement(pageBy, browser);
		return this;
	}
	
	public SurveyEditorPage edit() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}

		By editBy = By.xpath("//ul[contains(@class,'o_sel_repository_tools')]//a[contains(@onclick,'edit.cmd')]");
		browser.findElement(editBy).click();
		OOGraphene.waitBusy(browser);
		
		By contentEditorBy = By.cssSelector("div.o_page_content_editor");
		OOGraphene.waitElementPresence(contentEditorBy, 5, browser);
		return new SurveyEditorPage(browser);
	}
	
	/**
	 * Click the editor link in the tools drop-down
	 * @return Itself
	 */
	public SurveyPage openToolsMenu() {
		By toolsMenuCaret = By.cssSelector("a.o_sel_repository_tools");
		browser.findElement(toolsMenuCaret).click();
		OOGraphene.waitElement(toolsMenu, browser);
		return this;
	}
	
	public SurveyPage answerSingleChoice(String choice) {
		By choiceBy = By.xpath("//div[contains(@class,'o_ed_formsinglechoice')]//label[text()[contains(.,'" + choice + "')]]/input[@type='radio']");
		OOGraphene.waitElement(choiceBy, browser);
		browser.findElement(choiceBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public SurveyPage answerMultipleChoice(String choice) {
		By choiceBy = By.xpath("//div[contains(@class,'o_ed_formmultiplechoice')]//label[text()[contains(.,'" + choice + "')]]/input[@type='checkbox']");
		OOGraphene.waitElement(choiceBy, browser);
		browser.findElement(choiceBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public SurveyPage saveAndCloseSurvey() {
		By saveBy = By.xpath("//div[contains(@class,'o_evaluation_form')]//button[contains(@class,'btn-primary')]");
		browser.findElement(saveBy).click();
		
		OOGraphene.waitModalDialog(browser);
		By yesBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(yesBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public SurveyPage assertOnSurveyClosed() {
		By infoPanelBy = By.cssSelector(".o_surv_run #o_msg_info .panel-body>h4");
		OOGraphene.waitElement(infoPanelBy, browser);
		return this;
	}

}
