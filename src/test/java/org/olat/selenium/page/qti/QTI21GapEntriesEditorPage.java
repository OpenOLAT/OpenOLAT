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
import org.openqa.selenium.support.ui.Select;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;

/**
 * 
 * Initial date: 2 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21GapEntriesEditorPage extends QTI21AssessmentItemEditorPage {

	public QTI21GapEntriesEditorPage(WebDriver browser) {
		super(browser);
	}
	
	public QTI21GapEntriesEditorPage appendContent(String text) {
		String textSelector = ".o_sel_assessment_item_fib_text";
		OOGraphene.tinymceInsert(text, textSelector, browser);
		return this;
	}
	
	/**
	 * Add a new gap entry of type text. Use the placeholder to locate
	 * the gap during the test.
	 * 
	 * @param solution The solution
	 * @param placeholder The placeholder
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage addGapEntry(String solution, String placeholder) {
		By addGapBy = By.xpath("//div[contains(@class,'o_sel_assessment_item_fib_text')]//button[contains(@title,'L\u00FCckentext')]");
		browser.findElement(addGapBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By solutionBy = By.cssSelector("fieldset.o_sel_gap_entry_form div.o_sel_gap_entry_solution input[type=text]");
		OOGraphene.waitElement(solutionBy, browser);
		browser.findElement(solutionBy).sendKeys(solution);
		
		By placeholderBy = By.cssSelector("fieldset.o_sel_gap_entry_form div.o_sel_gap_entry_placeholder input[type=text]");
		browser.findElement(placeholderBy).sendKeys(placeholder);
		return this;
	}
	
	/**
	 * Edit an existing gap entry of type text. Use the placeholder to locate
	 * the gap during the test.
	 * 
	 * @param solution The solution
	 * @param placeholder The placeholder
	 * @param index The index of the entry in the paragraph
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage editGapEntry(String solution, String placeholder, int index) {
		By frameBy = By.cssSelector("div.o_sel_assessment_item_fib_text div.tox-edit-area iframe");
		WebElement frameEl = browser.findElement(frameBy);
		browser.switchTo().frame(frameEl);
		
		By gapEntryBy = By.xpath("//p/span[@class='textentryinteraction'][" + index + "]/a");
		browser.findElement(gapEntryBy).click();
		
		browser.switchTo().defaultContent();
		OOGraphene.waitModalDialog(browser);
		
		By solutionBy = By.cssSelector("fieldset.o_sel_gap_entry_form div.o_sel_gap_entry_solution input[type=text]");
		WebElement solutionEl = browser.findElement(solutionBy);
		solutionEl.clear();
		solutionEl.sendKeys(solution);
		
		By placeholderBy = By.cssSelector("fieldset.o_sel_gap_entry_form div.o_sel_gap_entry_placeholder input[type=text]");
		browser.findElement(placeholderBy).sendKeys(placeholder);
		return this;
	}
	
	/**
	 * Save and close the modal dialog to edit the gap entry.
	 * 
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage saveGapEntry() {
		By saveBy = By.cssSelector(".o_sel_gap_entry_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Add a new numerical input. Use the placeholder to locate
	 * the gap during the test.
	 * 
	 * @param solution The solution
	 * @param placeholder The placeholder
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage addNumericalInput(String solution, String placeholder,
			ToleranceMode toleranceMode, String uperrBound, String lowerBound) {
		By addGapBy = By.xpath("//div[contains(@class,'o_sel_assessment_item_fib_text')]//button[contains(@title,'Numeri')]");
		browser.findElement(addGapBy).click();
		OOGraphene.waitModalDialog(browser);
		return fillNumericalInputSettings(solution, placeholder, toleranceMode, uperrBound, lowerBound);
	}
	
	/**
	 * Edit an existing numerical input of type text. Use the placeholder to locate
	 * the gap during the test.
	 * 
	 * @param solution The solution
	 * @param placeholder The placeholder
	 * @param index The index of the entry in the paragraph
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage editNumericalInput(String solution, String placeholder,
			ToleranceMode toleranceMode, String uperrBound, String lowerBound, int index) {
		By frameBy = By.cssSelector("div.o_sel_assessment_item_fib_text div.tox-edit-area iframe");
		WebElement frameEl = browser.findElement(frameBy);
		browser.switchTo().frame(frameEl);
		
		By gapEntryBy = By.xpath("//p/span[@class='textentryinteraction'][" + index + "]/a");
		browser.findElement(gapEntryBy).click();
		
		browser.switchTo().defaultContent();
		OOGraphene.waitModalDialog(browser);
		return fillNumericalInputSettings(solution, placeholder, toleranceMode, uperrBound, lowerBound);
	}
	
	private QTI21GapEntriesEditorPage fillNumericalInputSettings(String solution, String placeholder,
			ToleranceMode toleranceMode, String upperBound, String lowerBound) {
		By solutionBy = By.cssSelector("fieldset.o_sel_gap_numeric_form div.o_sel_gap_numeric_solution input[type=text]");
		WebElement solutionEl = browser.findElement(solutionBy);
		solutionEl.clear();
		solutionEl.sendKeys(solution);
		
		By placeholderBy = By.cssSelector("fieldset.o_sel_gap_numeric_form div.o_sel_gap_numeric_placeholder input[type=text]");
		browser.findElement(placeholderBy).sendKeys(placeholder);
		
		By toleranceModeBy = By.xpath("//select[contains(@id,'fib_tolerance_mode')]");
		WebElement toleranceModeEl = browser.findElement(toleranceModeBy);
		new Select(toleranceModeEl).selectByValue(toleranceMode.name());
		OOGraphene.waitBusy(browser);
		
		By upperBoundBy = By.cssSelector("fieldset.o_sel_gap_numeric_form div.o_sel_gap_numeric_upper_bound input[type=text]");
		if(toleranceMode == ToleranceMode.EXACT) {
			OOGraphene.waitElementDisappears(upperBoundBy, 5, browser);
		} else {
			OOGraphene.waitElement(upperBoundBy, browser);
			
			browser.findElement(upperBoundBy).sendKeys(upperBound);
			By lowerBoundBy = By.cssSelector("fieldset.o_sel_gap_numeric_form div.o_sel_gap_numeric_lower_bound input[type=text]");
			browser.findElement(lowerBoundBy).sendKeys(lowerBound);
		}
		return this;
	}
	
	/**
	 * Save and close the modal dialog to edit the numerical input.
	 * 
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage saveNumericInput() {
		By saveBy = By.cssSelector("fieldset.o_sel_gap_numeric_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

	
	/**
	 * Save the whole interaction.
	 * 
	 * @return Itself
	 */
	public QTI21GapEntriesEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_fib_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the tab to edit the scores
	 * 
	 * @return The score page
	 */
	public QTI21GapEntriesScoreEditorPage selectScores() {
		selectTab("o_sel_assessment_item_score", By.className("o_sel_assessment_item_options"));
		return new QTI21GapEntriesScoreEditorPage(browser);
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
