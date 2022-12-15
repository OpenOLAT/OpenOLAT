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
package org.olat.selenium.page.lecture;

import java.util.Calendar;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 17 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditLectureBlockPage {
	
	private WebDriver browser;
	
	public EditLectureBlockPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EditLectureBlockPage setTitle(String title) {
		By titleBy = By.cssSelector("fieldset.o_sel_repo_edit_lecture_form div.o_sel_repo_lecture_title input[type=text]");
		browser.findElement(titleBy).sendKeys(title);
		return this;
	}
	
	public EditLectureBlockPage setTeacher(UserVO user) {
		By checkboxBy = By.xpath("//fieldset[contains(@class,'o_sel_repo_lecture_teachers')]//input[@type='checkbox'][@value='" + user.getKey() + "']");
		WebElement checkboxEl = browser.findElement(checkboxBy);
		OOGraphene.check(checkboxEl, Boolean.TRUE);
		return this;
	}
	
	/**
	 * Set the date of the lecture block to today, an hour before now.
	 * @return Itself
	 */
	public EditLectureBlockPage setDateOneHourBefore() {
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY) - 1;
		int endMinute = 59;
		if(hour < 0) {
			hour = 0;
			endMinute = 1;
		}
		return setDate(today, hour, 0, hour, endMinute);
	}
	
	public EditLectureBlockPage setDate(int day, int startHour, int startMinute, int endHour, int endMinute) {
		String firstDateMsXpath = "//fieldset[contains(@class,'o_sel_repo_edit_lecture_form')]//div[contains(@class,'o_date_ms')][contains(@class,'o_first_ms')]/input[@type='text']";
		By startHourBy = By.xpath(firstDateMsXpath + "[1]");
		setTimeField(startHourBy, startHour);
		By startMinuteBy = By.xpath(firstDateMsXpath + "[2]");
		setTimeField(startMinuteBy, startMinute);
		String secondDateMsXpath = "//fieldset[contains(@class,'o_sel_repo_edit_lecture_form')]//div[contains(@class,'o_date_ms')][contains(@class,'o_second_ms')]/input[@type='text']";
		By endHourBy = By.xpath(secondDateMsXpath + "[1]");
		setTimeField(endHourBy, endHour);
		By endMinuteBy = By.xpath(secondDateMsXpath + "[2]");
		setTimeField(endMinuteBy, endMinute);

		By untilAltBy = By.cssSelector("fieldset.o_sel_repo_edit_lecture_form div.o_sel_repo_lecture_date span.input-group-addon i");
		browser.findElement(untilAltBy).click();
		selectDayInDatePicker(day);
		return this;
	}
	
	private void setTimeField(By startHourBy, int value) {
		WebElement el = browser.findElement(startHourBy);
		el.clear();
		el.sendKeys(Integer.toString(value));
	}
	
	private EditLectureBlockPage selectDayInDatePicker(int day) {
		By datePickerBy = By.id("ui-datepicker-div");
		OOGraphene.waitElement(datePickerBy, 5, browser);
		
		By dayBy = By.xpath("//div[@id='ui-datepicker-div']//td//a[normalize-space(text())='" + day + "']");
		OOGraphene.waitElement(dayBy, 5, browser);
		browser.findElement(dayBy).click();
		
		OOGraphene.waitElementDisappears(datePickerBy, 5, browser);
		return this;
	}
	
	public EditLectureBlockPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_repo_edit_lecture_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
