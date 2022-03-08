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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * I cannot totally control TinyMCE with the WebDriver. Here you need
 * to add your content and hottext first, than check and uncheck the
 * hottexts.
 * 
 * Initial date: 6 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21HottextEditorPage extends QTI21AssessmentItemEditorPage {

	public QTI21HottextEditorPage(WebDriver browser) {
		super(browser);
	}
	
	public QTI21HottextEditorPage appendContent(String text) {
		String textSelector = ".o_sel_assessment_item_hottext_text";
		OOGraphene.tinymceInsert(text, textSelector, browser);
		return this;
	}
	
	/**
	 * Add a new hot text
	 * 
	 * @return Itself
	 */
	public QTI21HottextEditorPage addHottext() {
		By hottextBy = By.xpath("//div[contains(@class,'o_sel_assessment_item_hottext_text')]//button[i[contains(@class,'mce-i-hottext')]]");
		browser.findElement(hottextBy).click();
		return this;
	}
	
	/**
	 * Check an hottext.
	 * 
	 * @param index The position of the hottext in the paragraph (start with 1)
	 * @return Itself
	 */
	public QTI21HottextEditorPage check(int index) {
		By frameBy = By.cssSelector("div.o_sel_assessment_item_hottext_text div.mce-edit-area iframe");
		WebElement frameEl = browser.findElement(frameBy);
		browser.switchTo().frame(frameEl);
		
		By gapEntryBy = By.xpath("//p/span[@class='hottext'][" + index + "]/a[contains(@class,'o_check')]");
		List<WebElement> checkedEls = browser.findElements(gapEntryBy);
		if(checkedEls.size() == 1) {
			checkedEls.get(0).click();
		}

		browser.switchTo().defaultContent();
		return this;
	}
	
	/**
	 * Uncheck an hottext.
	 * 
	 * @param index The position of the hottext in the paragraph (start with 1)
	 * @return Itself
	 */
	public QTI21HottextEditorPage uncheck(int index) {
		By frameBy = By.cssSelector("div.o_sel_assessment_item_hottext_text div.mce-edit-area iframe");
		WebElement frameEl = browser.findElement(frameBy);
		browser.switchTo().frame(frameEl);
		
		By gapEntryBy = By.xpath("//p/span[@class='hottext'][" + index + "]/a[contains(@class,'o_check')][contains(@class,'checked')]");
		browser.findElement(gapEntryBy).click();

		browser.switchTo().defaultContent();
		return this;
	}
	
	/**
	 * Save the whole interaction.
	 * 
	 * @return Itself
	 */
	public QTI21HottextEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_hottext_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the tab to edit the scores
	 * 
	 * @return The score page
	 */
	public QTI21ChoicesScoreEditorPage selectScores() {
		selectTab("o_sel_assessment_item_score", By.className("o_sel_assessment_item_options"));
		return new QTI21ChoicesScoreEditorPage(browser);
	}
	
	/**
	 * Select the tab to edit the feedbacks
	 * 
	 * @return the feedback page
	 */
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab("o_sel_assessment_item_feedback", By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}
}
