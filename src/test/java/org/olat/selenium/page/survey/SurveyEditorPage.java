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

import org.olat.selenium.page.core.ContentEditorPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 27 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SurveyEditorPage extends ContentEditorPage {
	

	public SurveyEditorPage(WebDriver browser) {
		super(browser, true);
	}
	
	public SurveyPage close() {
		OOGraphene.scrollTop(browser);
		OOGraphene.clickBreadcrumbBack(browser);
		return new SurveyPage(browser);
	}
	
	@Override
	public SurveyEditorPage openElementsChooser(int container, int slot) {
		return (SurveyEditorPage)super.openElementsChooser(container, slot);
	}
	
	public SurveyEditorPage addRubricElement() {
		By addTitleBy = By.cssSelector("a#o_coadd_el_formrubric");
		browser.findElement(addTitleBy).click();
		OOGraphene.waitElement(editFragmentBy, browser);
		return this;
	}
	
	public SurveyEditorPage addSingleChoiceElement() {
		By addTitleBy = By.cssSelector("a#o_coadd_el_formsinglechoice");
		browser.findElement(addTitleBy).click();
		OOGraphene.waitElement(editFragmentBy, browser);
		return this;
	}
	
	public SurveyEditorPage addSingleChoice(String choice, int pos) {
		return addChoice(choice, pos, "o_sel_add_single_choice");
	}
	
	public SurveyEditorPage addMultipleChoiceElement() {
		By addTitleBy = By.cssSelector("a#o_coadd_el_formmultiplechoice");
		browser.findElement(addTitleBy).click();
		OOGraphene.waitElement(editFragmentBy, browser);
		return this;
	}
	
	public SurveyEditorPage addMultipleChoice(String choice, int pos) {
		return addChoice(choice, pos, "o_sel_add_multiple_choice");
	}
	
	private SurveyEditorPage addChoice(String choice, int pos, String buttonClass) {
		By addButtonBy = By.xpath("//div[contains(@class,'o_evaluation_editor_form')]//a[contains(@class,'btn')][contains(@class,'" + buttonClass + "')]/span");
		OOGraphene.waitElement(addButtonBy, browser);
		browser.findElement(addButtonBy).click();
		if(pos > 2) {// why oh why
			OOGraphene.waitingALittleBit();
			browser.findElement(addButtonBy).click();
		}
		OOGraphene.waitBusy(browser);
		
		By choiceBy = By.xpath("//div[contains(@class,'o_evaluation_editor_form')]//table[contains(@class,'table-condensed')]/tbody/tr[" + pos + "]/td/input[@type='text']");
		OOGraphene.waitElement(choiceBy, browser);
		browser.findElement(choiceBy).clear();
		OOGraphene.waitBusy(browser);
		browser.findElement(choiceBy).sendKeys(choice);
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	


}
