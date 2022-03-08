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

import java.util.Calendar;
import java.util.Date;

import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 2 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ConfigurationCEPage {
	
	public static final By chooseTestButton = By.className("o_sel_test_choose_repofile");
	
	private final WebDriver browser;
	
	public QTI21ConfigurationCEPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21ConfigurationCEPage selectConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_repo_entry>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		OOGraphene.waitElement(By.className("o_qti_21_configuration"), browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage showScoreOnHomepage(boolean showResults) {
		By scoreBy = By.cssSelector(".o_sel_results_on_homepage select#o_fioqti_showresult_SELBOX");
		OOGraphene.waitElementPresence(scoreBy, 5, browser);
		OOGraphene.scrollTo(scoreBy, browser);
		OOGraphene.waitElement(scoreBy, browser);
		WebElement scoreEl = browser.findElement(scoreBy);
		String val = showResults ? "false" : "no";
		new Select(scoreEl).selectByValue(val);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage assertShowResultsOptions() {
		By optionsBy = By.cssSelector("div.o_sel_qti_show_results_options label>input[type='checkbox']");
		OOGraphene.waitElement(optionsBy, browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage showResultsOnHomepage(Boolean show, QTI21AssessmentResultsOptions options) {
		By showResultsBy = By.cssSelector("div.o_sel_qti_show_results input[type='checkbox']");
		WebElement showResultsEl = browser.findElement(showResultsBy);
		OOGraphene.check(showResultsEl, show);
		OOGraphene.waitBusy(browser);
		
		By resultsLevelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox']");
		OOGraphene.waitElement(resultsLevelBy, 5, browser);

		if(options.isMetadata()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='metadata']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isSectionSummary()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='sectionsSummary']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isQuestionSummary()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='questionSummary']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isUserSolutions()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='userSolutions']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isCorrectSolutions()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='correctSolutions']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		return this;
	}
	
	/**
	 * Set the correction mode.
	 * 
	 * @param mode The mode defined by IQEditController.CORRECTION_AUTO,
	 * 		IQEditController.CORRECTION_MANUAL or IQEditController.CORRECTION_GRADING
	 * @return Itself
	 */
	public QTI21ConfigurationCEPage setCorrectionMode(String mode) {
		By correctionBy = By.xpath("//fieldset[contains(@class,'o_qti_21_correction')]//div[@id='o_cocorrection_mode']//input[@value='" + mode + "'][@name='correction.mode'][@type='radio']");
		OOGraphene.waitElement(correctionBy, browser);
		browser.findElement(correctionBy).click();
		return this;
	}
	
	public QTI21ConfigurationCEPage setTime(Date start, Date end) {
		By enableDateTest = By.cssSelector(".o_qti_21_datetest input[type='checkbox']");
		browser.findElement(enableDateTest).click();
		OOGraphene.waitBusy(browser);
		
		// confirm
		By confirmBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitingALittleLonger();
		OOGraphene.moveTo(By.cssSelector("div.o_qti_21_datetest_end"), browser);
		
		// set dates
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		setTime("o_qti_21_datetest_start", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
		cal.setTime(end);
		setTime("o_qti_21_datetest_end", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
		return this;
	}
	
	private QTI21ConfigurationCEPage setTime(String fieldClass, int hour, int minutes, boolean waitBusy) {
		try {
			By untilAltBy = By.cssSelector("div." + fieldClass + " div.o_date_picker span.input-group-addon i");
			OOGraphene.waitElement(untilAltBy, browser);
			browser.findElement(untilAltBy).click();
			OOGraphene.waitingALittleLonger();//SEL wait animation
			
			By todayBy = By.xpath("//div[@id='ui-datepicker-div']//td[contains(@class,'ui-datepicker-today')]/a");
			OOGraphene.waitElement(todayBy, browser);
			
			browser.findElement(todayBy).click();
			OOGraphene.waitElementDisappears(todayBy, 5, browser);
			OOGraphene.waitingLong();//SEL wait animation
		
			if(waitBusy) {
				OOGraphene.waitBusy(browser);
			}
			
			By hourBy = By.xpath("//div[contains(@class,'" + fieldClass + "')]//div[contains(@class,'o_first_ms')]/input[contains(@id,'o_dch_o_')]");
			WebElement hourEl = browser.findElement(hourBy);
			hourEl.clear();
			hourEl.sendKeys(Integer.toString(hour));

			By minuteBy = By.xpath("//div[contains(@class,'" + fieldClass + "')]//div[contains(@class,'o_first_ms')]/input[contains(@id,'o_dcm_o_')]");
			WebElement minuteEl = browser.findElement(minuteBy);
			minuteEl.clear();
			minuteEl.sendKeys(Integer.toString(minutes));
		} catch (Exception | Error e) {
			OOGraphene.takeScreenshot("Datetest", browser);
			throw e;
		}
		return this;
	}
	
	public QTI21ConfigurationCEPage saveConfiguration() {
		By saveBy = By.cssSelector(".o_qti_21_configuration button");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21LayoutConfigurationCEPage selectLayoutConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_qti_layout>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		OOGraphene.waitElement(By.className("o_qti_21_layout_configuration"), browser);
		return new QTI21LayoutConfigurationCEPage(browser);
	}
	
	public QTI21ConfigurationCEPage selectLearnContent() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_repo_entry>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		OOGraphene.waitElement(chooseTestButton, browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage chooseTest(String resourceTitle, boolean closeWarning) {
		CourseEditorPageFragment fragment = new CourseEditorPageFragment(browser);
		fragment.chooseResource(chooseTestButton, resourceTitle);
		if(closeWarning) {
			//close the warning
			OOGraphene.closeWarningBox(browser);
		}
		return this;
	}
}
