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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Drive the assessment settings
 * 
 * Initial date: 13.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModePage {
	
	private final WebDriver browser;
	
	public AssessmentModePage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Create a new assessment mode settings in the list
	 * of assessment.
	 * 
	 * @return
	 */
	public AssessmentModePage createAssessmentMode() {
		By addBy = By.className("o_sel_assessment_mode_add");
		browser.findElement(addBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * 
	 * Fill the settings for the assessment.
	 * 
	 * @param name
	 * @param begin
	 * @param end
	 * @param manual
	 * @return
	 */
	public AssessmentModePage editAssessment(String name, Date begin, Date end, boolean manual) {
		By nameBy = By.cssSelector("div.o_sel_assessment_mode_name input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(name);
		//begin
		setDateTime(begin, "o_sel_assessment_mode_begin");
		//end
		setDateTime(end, "o_sel_assessment_mode_end");
		//start mode
		By startBy = By.cssSelector("div.o_sel_assessment_mode_start_mode select");
		WebElement startEl = browser.findElement(startBy);
		new Select(startEl).selectByValue(manual ? "manual" : "automatic");

		return this;
	}
	
	private final void setDateTime(Date date, String seleniumCssClass) {
		Locale locale = OOGraphene.getLocale(browser);
		String dateText = OOGraphene.formatDate(date, locale);
		By dateBy = By.cssSelector("div." + seleniumCssClass + " input.o_date_day");
		browser.findElement(dateBy).clear();
		browser.findElement(dateBy).sendKeys(dateText);
		OOGraphene.waitBusy(browser);
		
		By hourBy = By.xpath("//div[contains(@class,'" + seleniumCssClass + "')]//input[contains(@class,'o_date_ms')][1]");
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		browser.findElement(By.cssSelector(".o_sel_assessment_mode_name")).click();
		
		By datePickerBy = By.cssSelector("div." + seleniumCssClass + " div.datepicker-dropdown.active");
		OOGraphene.waitElementDisappears(datePickerBy, 5, browser);
		
		browser.findElement(hourBy).clear();
		browser.findElement(hourBy).sendKeys(Integer.toString(hour));
		
		By minuteBy = By.xpath("//div[contains(@class,'" + seleniumCssClass + "')]//input[contains(@class,'o_date_ms')][2]");
		browser.findElement(minuteBy).clear();
		browser.findElement(minuteBy).sendKeys(Integer.toString(minute));
	}
	
	public AssessmentModePage audienceCourse() {
		By audienceBy = By.xpath("//div[contains(@class,'o_sel_assessment_mode_audience')]//input[@value='course']");
		OOGraphene.waitElement(audienceBy, browser);
		browser.findElement(audienceBy).click();
		return this;
	}
	
	/**
	 * Save the assessment mode settings.
	 * 
	 * @return
	 */
	public AssessmentModePage save(String assessmentModeTitle) {
		By saveButtonBy = By.cssSelector(".o_sel_assessment_mode_edit_form button.btn-primary");
		browser.findElement(saveButtonBy).click();
		By startBy = By.xpath("//fieldset/legend[text()[contains(.,'" + assessmentModeTitle + "')]]");
		OOGraphene.waitElement(startBy, browser);
		return this;
	}
	
	public AssessmentModePage clickToolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		return this;
	}
	
	public AssessmentModePage assertAssessmentModeList() {
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_assessment_mode_list"), browser);
		return this;
	}
	
	/**
	 * Start an assessment in the list by its name
	 * 
	 * @param name
	 * @return
	 */
	public AssessmentModePage start(String name) {
		By startBy = By.xpath("//fieldset[contains(@class,'o_sel_assessment_mode_list')]//table//tr/td/a[contains(@onclick,'start')]");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Confirm the start of the assessment.
	 * 
	 * @return
	 */
	public AssessmentModePage confirmStart() {
		By confirmButtonBy = By.cssSelector("div.modal-dialog div.modal-footer a");
		List<WebElement> buttonsEl = browser.findElements(confirmButtonBy);
		buttonsEl.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Stop an assessment in the list by its name
	 * 
	 * @param name
	 * @return
	 */
	public AssessmentModePage stop(String name) {
		By stopBy = By.xpath("//fieldset[contains(@class,'o_sel_assessment_mode_list')]//table//tr/td/a[contains(@onclick,'stop')]");
		OOGraphene.waitElement(stopBy, browser);
		browser.findElement(stopBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentModePage confirmStop() {
		By confirmButtonBy = By.cssSelector("div.modal-dialog div.modal-body button.btn-primary");
		OOGraphene.waitElement(confirmButtonBy, browser);
		browser.findElement(confirmButtonBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * A student can start its assessment
	 * 
	 * @return
	 */
	public AssessmentModePage startAssessment(boolean wait) {
		By startBy = By.cssSelector("div.modal-dialog div.modal-body div.o_button_group a.o_sel_assessment_start");
		if(wait) {
			OOGraphene.waitElement(startBy, browser);
		}
		List<WebElement> buttonsEl = browser.findElements(startBy);
		buttonsEl.get(0).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public AssessmentModePage waitBackToOpenOlat() {
		By continueBy = By.xpath("//div[@class='modal-content']//a[contains(@class,'o_sel_assessment_continue')]");
		OOGraphene.waitElementSlowly(continueBy, 20, browser);
		return this;
	}
	
	/**
	 * After an assessment, go back to OpenOLAT.
	 */
	public void backToOpenOLAT() {
		By continueBy = By.cssSelector("div.modal-dialog div.modal-body div.o_button_group a.o_sel_assessment_continue");
		OOGraphene.waitElement(continueBy, browser);
		browser.findElement(continueBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
	}
}
