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
public class SurveyEditorPage {
	
	protected final By editFragmentBy = By.cssSelector("div.o_page_fragment_edit");
	
	private final WebDriver browser;

	public SurveyEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public SurveyEditorPage openElementsChooser() {
		By addBy = By.cssSelector("a.btn.o_sel_add_element_main");
		OOGraphene.waitElement(addBy, browser);
		browser.findElement(addBy).click();
		OOGraphene.waitBusy(browser);
		By addCalloutBy = By.cssSelector("div.popover div.o_sel_add_element_callout");
		OOGraphene.waitElement(addCalloutBy, browser);
		return this;
	}
	
	public SurveyPage close() {
		OOGraphene.scrollTop(browser);
		OOGraphene.clickBreadcrumbBack(browser);
		return new SurveyPage(browser);
	}
	
	/**
	 * Close the fragment editor.
	 * 
	 * @return Itself
	 */
	public SurveyEditorPage closeEditFragment() {
		OOGraphene.waitingALittleLonger();
		By closeBy = By.cssSelector("div.o_page_others_above>a.o_sel_save_element>span");
		OOGraphene.waitElement(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		
		By pageEditBy = By.cssSelector("div.o_page_part>div.o_page_fragment_edit>div.o_page_edit");
		OOGraphene.waitElementDisappears(pageEditBy, 5, browser);
		OOGraphene.waitingALittleLonger();
		return this;
	}
	
	public SurveyEditorPage addTitle(String title) {
		By addTitleBy = By.cssSelector("a#o_coadd_el_formhtitle");
		browser.findElement(addTitleBy).click();
		OOGraphene.waitElement(editFragmentBy, browser);
		OOGraphene.tinymce(title, ".o_page_part.o_page_edit", browser);
		return this;
	}
	
	/**
	 * Change the size of the title.
	 * 
	 * @param size A value between 1 and 6
	 * @return
	 */
	public SurveyEditorPage setTitleSize(int size) {
		By titleSize = By.xpath("//div[contains(@class,'o_page_edit_toolbar')]//a[span[contains(text(),'h" + size + "')]]");
		browser.findElement(titleSize).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Check that the title is on the page with the right size.
	 * 
	 * @param title The title
	 * @param size Its size (between 1 and 6)
	 * @return Itself
	 */
	public SurveyEditorPage assertOnTitle(String title, int size) {
		By titleBy = By.xpath("//div[contains(@class,'o_page_content_editor')]//h" + size + "[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
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
