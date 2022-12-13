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
package org.olat.selenium.page.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

import org.junit.Assert;
import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Drive the calendar
 * 
 * Initial date: 27.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPage {
	
	public static final By calendarToolbatBy = By.className("o_cal_toptoolbar");
	private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.appendValue(ChronoField.YEAR, 4)
			.appendLiteral('-')
			.appendValue(ChronoField.MONTH_OF_YEAR, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.DAY_OF_MONTH, 2)
			.toFormatter();
	
	private final DateTimeFormatter oocurenceIdFormatter = new DateTimeFormatterBuilder()
			.appendValue(ChronoField.YEAR, 4)
			.appendValue(ChronoField.MONTH_OF_YEAR, 2)
			.appendValue(ChronoField.DAY_OF_MONTH, 2)
			.toFormatter();
	
	private final WebDriver browser;
	
	public CalendarPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Don't forget that the calendar is a javascript application. You need
	 * to wait until scripts, css and data are loaded before asserting.
	 * 
	 * @return The calendar page
	 */
	public CalendarPage assertOnCalendar() {
		OOGraphene.waitElement(calendarToolbatBy, browser);
		return this;
	}
	
	/**
	 * Add an event but don't fill the form.
	 * 
	 * @param day
	 * @return
	 */
	public CalendarPage addEvent(int day) {
		LocalDate date = LocalDate.now().withDayOfMonth(day);
		String dateString = date.format(formatter);
		By cellBy = By.xpath("//div[contains(@class,'o_cal')]//td[contains(@data-date,'" + dateString + "')][contains(@class,'fc-day')]/div");
		OOGraphene.waitElement(cellBy, browser);
		browser.findElement(cellBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public CalendarPage setDescription(String subject, String description, String location) {
		if(StringHelper.containsNonWhitespace(subject)) {
			By subjectBy = By.cssSelector("fieldset.o_sel_cal_entry_form div.o_sel_cal_subject input[type='text']");
			WebElement subjectEl = browser.findElement(subjectBy);
			subjectEl.clear();
			subjectEl.sendKeys(subject);
		}
		
		if(StringHelper.containsNonWhitespace(description)) {
			By descriptionBy = By.cssSelector("fieldset.o_sel_cal_entry_form div.o_sel_cal_description textarea");
			browser.findElement(descriptionBy).sendKeys(description);
		}
		
		if(StringHelper.containsNonWhitespace(location)) {
			By locationBy = By.cssSelector("fieldset.o_sel_cal_entry_form div.o_sel_cal_location input[type='text']");
			browser.findElement(locationBy).sendKeys(location);
		}
		return this;
	}
	
	public CalendarPage setAllDay(boolean allDay) {
		By locationBy = By.xpath("//fieldset[contains(@class,'o_sel_cal_entry_form')]//div[contains(@class,'o_sel_cal_all_day')]//input[@type='checkbox']");
		
		WebElement allDayEl = browser.findElement(locationBy);
		OOGraphene.scrollTo(locationBy, browser);
		OOGraphene.check(allDayEl, Boolean.valueOf(allDay));
		
		if(!allDay) {
			By hourBy = By.xpath("//fieldset[contains(@class,'o_sel_cal_entry_form')]//div[contains(@class,'o_sel_cal_begin')]//input[contains(@id,'o_dch_')]");
			OOGraphene.waitElement(hourBy, 5, browser);
		}
		return this;
	}
	
	public CalendarPage setBeginEnd(int beginHour, int endHour) {
		By beginHourBy = By.xpath("//fieldset[contains(@class,'o_sel_cal_entry_form')]//div[contains(@class,'o_sel_cal_begin')]//input[starts-with(@id,'o_dch_')]");
		WebElement beginHourEl = browser.findElement(beginHourBy);
		beginHourEl.clear();
		beginHourEl.sendKeys(Integer.toString(beginHour));
		
		By endHourBy = By.xpath("//fieldset[contains(@class,'o_sel_cal_entry_form')]//div[contains(@class,'o_sel_cal_end')]//input[starts-with(@id,'o_dch_')]");
		WebElement endHourEl = browser.findElement(endHourBy);
		endHourEl.clear();
		endHourEl.sendKeys(Integer.toString(endHour));
		return this;
	}
	
	public CalendarPage setRecurringEvent(String recur, int day) {
		By recurrenceBy = By.id("o_fiocal_form_recurrence_SELBOX");
		WebElement recurrenceEl = browser.findElement(recurrenceBy);
		new Select(recurrenceEl).selectByValue(recur);
		OOGraphene.waitBusy(browser);
		
		By untilAltBy = By.cssSelector("fieldset.o_sel_cal_entry_form div.o_sel_cal_until span.input-group-addon i");
		OOGraphene.waitElement(untilAltBy, browser);
		browser.findElement(untilAltBy).click();
		selectDayInDatePicker(day);
		return this;
	}
	
	private CalendarPage selectDayInDatePicker(int day) {
		By datePickerBy = By.id("ui-datepicker-div");
		OOGraphene.waitElement(datePickerBy, browser);
		
		By dayBy = By.xpath("//div[@id='ui-datepicker-div']//td//a[normalize-space(text())='" + day + "']");
		OOGraphene.waitElement(dayBy, browser);
		browser.findElement(dayBy).click();
		OOGraphene.waitElementDisappears(datePickerBy, 5, browser);
		return this;
	}
	
	public CalendarPage save(boolean closeModal) {
		By saveBy = By.cssSelector("fieldset.o_sel_cal_entry_form button.btn.btn-primary span");
		OOGraphene.waitElement(saveBy, browser);
		OOGraphene.click(saveBy, browser);
		if(closeModal) {
			OOGraphene.waitModalDialogDisappears(browser);
		} else {
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	public CalendarPage confirmModifyOneOccurence() {
		By saveOneBy = By.cssSelector("div.modal-dialog a.o_sel_cal_update_one");
		OOGraphene.waitElement(saveOneBy, browser);
		browser.findElement(saveOneBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CalendarPage confirmModifyAllOccurences() {
		By saveAllBy = By.cssSelector("div.modal-dialog a.o_sel_cal_update_all");
		OOGraphene.waitElement(saveAllBy, browser);
		browser.findElement(saveAllBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CalendarPage delete() {
		By deleteBy = By.cssSelector("fieldset.o_sel_cal_entry_form a.btn.o_sel_cal_delete");
		OOGraphene.waitElement(deleteBy, 5, browser);
		OOGraphene.clickAndWait(deleteBy, browser);
		return this;
	}
	
	public CalendarPage confirmDeleteOneOccurence() {
		By deleteOneBy = By.cssSelector("div.modal-dialog a.o_sel_cal_delete_one");
		OOGraphene.waitElement(deleteOneBy, browser);
		browser.findElement(deleteOneBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CalendarPage confirmDeleteFuturOccurences() {
		By deleteFutureBy = By.cssSelector("div.modal-dialog a.o_sel_cal_delete_future_events");
		OOGraphene.waitElement(deleteFutureBy, browser);
		browser.findElement(deleteFutureBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CalendarPage confirmDeleteAllOccurences() {
		By deleteAllBy = By.cssSelector("div.modal-dialog a.o_sel_cal_delete_all");
		OOGraphene.waitElement(deleteAllBy, browser);
		browser.findElement(deleteAllBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CalendarPage assertOnEvent(String subject) {
		By titleBy = By.xpath("//div[@class='o_cal']//div[contains(@class,'fc-event-title')][contains(text(),'" + subject + "')]");
		OOGraphene.waitElement(titleBy, browser);
		Assert.assertNotNull(browser.findElement(titleBy));
		return this;
	}
	
	public CalendarPage assertOnEvents(String subject, int numOfEvents) {
		By titleBy = By.xpath("//div[@class='o_cal']//div[contains(@class,'fc-event-title')][contains(text(),'" + subject + "')]");
		OOGraphene.waitElement(titleBy, browser);
		List<WebElement> eventEls = browser.findElements(titleBy);
		Assert.assertEquals(numOfEvents, eventEls.size());
		return this;
	}
	
	public CalendarPage assertOnEventsAt(String subject, int numOfEvents, int atHour) {
		By titleBy = By.xpath("//div[@class='o_cal']//a[contains(@class,'fc-event')][descendant::div[contains(text(),'" + atHour + ":00')]][descendant::div[contains(text(),'" + subject + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		List<WebElement> eventEls = browser.findElements(titleBy);
		Assert.assertEquals(numOfEvents, eventEls.size());
		return this;
	}
	
	public CalendarPage assertZeroEvent() {
		By eventsBy = By.xpath("//div[@class='fc-daygrid-day-events']/div");
		List<WebElement> eventEls = browser.findElements(eventsBy);
		Assert.assertEquals(0, eventEls.size());
		return this;
	}
	
	public CalendarPage openDetails(String subject) {
		By titleBy = By.xpath("//div[@class='o_cal']//a[descendant::div[contains(@class,'fc-event-title')][contains(text(),'" + subject + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).click();
		OOGraphene.waitCallout(browser);
		return this;
	}
	
	public CalendarPage openDetailsOccurence(String subject, int day) {
		LocalDate date = LocalDate.now().withDayOfMonth(day);
		String dateString = date.format(oocurenceIdFormatter);

		By titleBy = By.xpath("//div[@class='o_cal']//a[contains(@id,'xOccOOccOx_" + dateString + "')][descendant::div[contains(@class,'fc-event-title')][contains(text(),'" + subject + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).click();
		OOGraphene.waitCallout(browser);
		return this;
	}
	
	/**
	 * The details callout need to be open.
	 * 
	 * @return Itself
	 */
	public CalendarPage edit() {
		By editBy = By.cssSelector("div.popover-content div.o_callout_content div.o_cal_tooltip_buttons a.btn.btn-default");
		OOGraphene.waitElement(editBy, browser);
		browser.findElement(editBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
}
