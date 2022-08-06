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

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.selenium.page.core.UserSearchPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.wildfly.common.Assert;

/**
 * 
 * Initial date: 3 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentPage {
	
	private static final By datePickerBy = By.id("ui-datepicker-div");
	
	private final WebDriver browser;
	
	public AppointmentPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Create a new topic.
	 * 
	 * @param title The title of the topic
	 * @return Itself
	 */
	public AppointmentPage addTopic(String title) {
		By addTopicBy = By.cssSelector("a.o_sel_app_add_topic");
		OOGraphene.waitElement(addTopicBy, browser);
		browser.findElement(addTopicBy).click();
		
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.xpath("//div[contains(@class,'modal-dialog')]//div[contains(@class,'o_sel_app_topic_title')]//input[@type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		return this;
	}
	
	/**
	 * Set the "Finding" option.
	 * $
	 * @return Itself
	 */
	public AppointmentPage setFinding() {
		By findingBy = By.cssSelector("div#o_cotopic_type input[name='topic.type'][value='finding']");
		browser.findElement(findingBy).click();
		OOGraphene.waitBusy(browser);	
		return this;
	}
	
	public AppointmentPage setRecurringTopic(int firstDay, int lastDay, int startHour, int endHour, DayOfWeek day) {
		By recurringBy = By.cssSelector("#o_coappointment_input_type input[name='appointment.input.type'][value='recurring']");
		browser.findElement(recurringBy).click();
		OOGraphene.waitElement(By.cssSelector("div.o_sel_app_topic_recurring_day"), browser);
		
		By firstBy = By.cssSelector("div.o_sel_app_topic_recurring_first span.input-group-addon i");
		OOGraphene.waitElement(firstBy, browser);
		if(browser instanceof FirefoxDriver) {
			browser.findElement(firstBy).click();
			OOGraphene.waitElement(datePickerBy, browser);
			OOGraphene.selectNextMonthInDatePicker(browser);
			OOGraphene.selectDayInDatePicker(firstDay, browser);
		} else {
			Date firstDate = getRecurringDate(firstDay);
			OOGraphene.date(firstDate, "o_sel_app_topic_recurring_first", browser);
		}
		
		By startHourBy = By.xpath("//div[contains(@class,'o_sel_app_topic_recurring_first')]//div[contains(@class,'o_first_ms')]/input[@type='text'][1]");
		WebElement startHourEl = browser.findElement(startHourBy);
		startHourEl.clear();
		startHourEl.sendKeys(Integer.toString(startHour));
		
		By endHourBy = By.xpath("//div[contains(@class,'o_sel_app_topic_recurring_first')]//div[contains(@class,'o_second_ms')]/input[@type='text'][1]");
		WebElement endHourEl = browser.findElement(endHourBy);
		endHourEl.clear();
		endHourEl.sendKeys(Integer.toString(endHour));
		
		By dayBy = By.xpath("//div[contains(@class,'o_sel_app_topic_recurring_day')]//input[@name='appointments.recurring.days.of.week'][@value='" + day.name() + "']");
		WebElement dayEl = browser.findElement(dayBy);
		OOGraphene.check(dayEl, Boolean.TRUE);
		
		if(browser instanceof FirefoxDriver) {
			By lastBy = By.cssSelector("div.o_sel_app_topic_recurring_last span.input-group-addon i");
			browser.findElement(lastBy).click();
			OOGraphene.waitElement(datePickerBy, browser);
			OOGraphene.selectNextMonthInDatePicker(browser);
			OOGraphene.selectDayInDatePicker(lastDay, browser);
		} else {
			Date lastDate = getRecurringDate(lastDay);
			OOGraphene.date(lastDate, "o_sel_app_topic_recurring_last", browser);	
		}
		
		return this;
	}
	
	private Date getRecurringDate(int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}
	
	/**
	 * Save (and close) the dialog to create a topic.
	 * 
	 * @return Itself
	 */
	public AppointmentPage saveTopic()  {
		By saveBy = By.xpath("//div[contains(@class,'modal-dialog')]//fieldset//button[contains(@class,'btn-primary')]");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Check that the topic with the specified title is in the list
	 * of appointments.
	 * 
	 * @param title The topic title
	 * @return Itself
	 */
	public AppointmentPage assertOnTopic(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_appointment_header')]/h3[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @param title The title of the topic
	 * @return Itself
	 */
	public AppointmentPage selectTopicAsCoach(String title) {
		By selectTopicBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'panel-default')][div[@class='panel-heading']/h4/div[text()[contains(.,'" + title + "')]]]//a[i[contains(@class,'o_icon_start')]]");
		OOGraphene.waitElement(selectTopicBy, browser);
		browser.findElement(selectTopicBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AppointmentPage selectTopicAsParticipant(String title) {
		By selectTopicBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'panel-default')][div[@class='panel-heading']/h4[text()[contains(.,'" + title + "')]]]//a[i[contains(@class,'o_icon_start')]]");
		OOGraphene.waitElement(selectTopicBy, browser);
		browser.findElement(selectTopicBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Check that the topic with the specified title is in the list
	 * of appointments.
	 * 
	 * @param title The title of the topics
	 * @param minMeetings The minimal number of appointments in the list
	 * @return Itself
	 */
	public AppointmentPage assertOnTopicMultipleMeetings(String title, int minNumOfAppointments) {
		By titleBy = By.xpath("//div[contains(@class,'o_appointment_header')]/h3[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		By appointmentsBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')]");
		OOGraphene.waitElement(appointmentsBy, browser);
		List<WebElement> appointmentsEl = browser.findElements(appointmentsBy);
		int numOfAppointments = appointmentsEl.size();
		Assert.assertTrue(minNumOfAppointments <= numOfAppointments);
		return this;
	}
	
	public UserSearchPage addUser(int day) {
		String dayStr = dayToString(day);
		By addDropMenuBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//button[contains(@class,'dropdown-toggle')]");
		browser.findElement(addDropMenuBy).click();
		By addUserBy = By.xpath("//div[contains(@class,'o_appointments')]//ul[contains(@class,'dropdown-menu')]/li/a[i[contains(@class,'o_icon_add_member')]]");
		OOGraphene.waitElement(addUserBy, browser);
		browser.findElement(addUserBy).click();
		OOGraphene.waitModalDialog(browser);
		
		return new UserSearchPage(browser);
	}
	
	/**
	 * 
	 * @param posInList The position in appointments list (start with  1)
	 * @return Itself
	 */
	public UserSearchPage addUserToAppointment(int posInList) {
		By addDropMenuBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]//div[contains(@class,'o_main_cont')]//button[contains(@class,'dropdown-toggle')]");
		browser.findElement(addDropMenuBy).click();
		By addUserBy = By.xpath("//div[contains(@class,'o_appointments')]//ul[contains(@class,'dropdown-menu')]/li/a[i[contains(@class,'o_icon_add_member')]]");
		OOGraphene.waitElement(addUserBy, browser);
		browser.findElement(addUserBy).click();
		OOGraphene.waitModalDialog(browser);
		
		return new UserSearchPage(browser);
	}

	/**
	 * Assert on the confirmation button.
	 * 
	 * @param day The day
	 * @return Itself
	 */
	public AppointmentPage assertOnConfirmAppointmentByDay(int day) {
		String dayStr = dayToString(day);
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		return this;
	}
	
	/**
	 * Check if the confirmation button is there.
	 * 
	 * @param posInList Position in the list (starts with 1)
	 * @return Itself
	 */
	public AppointmentPage assertOnConfirmAppointmentByPosition(int posInList) {
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]//div[contains(@class,'o_main_cont')]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		return this;
	}
	
	/**
	 * Assert on the status of the appointment.
	 * 
	 * @param day The day
	 * @return Itself
	 */
	public AppointmentPage assertOnConfirmedAppointmentByDay(int day) {
		String dayStr = dayToString(day);
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_ap_confirmed')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]");
		OOGraphene.waitElement(confirmBy, browser);
		return this;
	}
	
	/**
	 * Check if the appointment is confirmed.
	 * 
	 * @param posInList Position in the list (starts with 1)
	 * @return Itself
	 */
	public AppointmentPage assertOnConfirmedAppointmentByPosition(int posInList) {
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]/div[contains(@class,'o_ap_confirmed')]//div[contains(@class,'o_main_cont')]");
		OOGraphene.waitElement(confirmBy, browser);
		return this;
	}
	
	/**
	 * Confirm the appointment and assert that the appointment is successfully confirmed.
	 * 
	 * @param day The day of the month
	 * @return Itself
	 */
	public AppointmentPage confirmAppointmentByDay(int day) {
		String dayStr = dayToString(day);
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		
		By confirmedBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_table_row')]/div[contains(@class,'o_ap_confirmed')]//div[contains(@class,'o_day_" + dayStr + "')]");
		OOGraphene.waitElement(confirmedBy, browser);
		return this;
	}
	
	/**
	 * Confirm the appointment and assert that the appointment is successfully confirmed.
	 * 
	 * @param posInList Position in the list (starts with 1)
	 * @return Itself
	 */
	public AppointmentPage confirmAppointmentByPosition(int posInList) {
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]//div[contains(@class,'o_main_cont')]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		
		By confirmedBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_table_row')][" + posInList + "]/div[contains(@class,'o_ap_confirmed')]");
		OOGraphene.waitElement(confirmedBy, browser);
		return this;
	}
	
	/**
	 * Assert on the status of the appointment.
	 * 
	 * @param posInList Position in the list (starts with 1)
	 * @return Itself
	 */
	public AppointmentPage assertOnPlannedAppointmentByPosition(int posInList) {
		try {
			By plannedBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]/div[contains(@class,'o_ap_planned')]//div[contains(@class,'o_main_cont')]");
			OOGraphene.waitElement(plannedBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("App_pos_" + posInList + "_", browser);
			throw e;
		}
		return this;
	}
	
	public AppointmentPage confirmPlannedAppointmentByPosition(int posInList) {
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]/div[contains(@class,'o_ap_planned')]//div[contains(@class,'o_main_cont')]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		
		By confirmedBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_table_row')][" + posInList + "]/div[contains(@class,'o_ap_confirmed')]");
		OOGraphene.waitElement(confirmedBy, browser);
		return this;
	}
	
	public AppointmentPage assertOnSelectAppointmentByPosition(int posInList) {
		By selectBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]//div[contains(@class,'o_main_cont')]//a[contains(@class,'o_sel_appointment_select')][contains(@class,'btn-primary')]");
		OOGraphene.waitElement(selectBy, browser);
		return this;
	}
	
	public AppointmentPage selectAppointmentByDay(int day) {
		String dayStr = dayToString(day);
		By selectBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//a[contains(@class,'o_sel_appointment_select')][contains(@class,'btn-primary')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		
		By confirmedBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_table_row')]/div[contains(@class,'o_ap_planned')]//div[contains(@class,'o_day_" + dayStr + "')]");
		OOGraphene.waitElement(confirmedBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @param posInList The position of the appointment in list (start with 1)
	 * @return Itself
	 */
	public AppointmentPage selectAppointmentByPosition(int posInList) {
		By selectBy = By.xpath("//div[contains(@class,'o_appointments')]/div/div[contains(@class,'o_table_row')][" + posInList + "]//div[contains(@class,'o_main_cont')]//a[contains(@class,'o_sel_appointment_select')][contains(@class,'btn-primary')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		
		By plannedBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_table_row')][" + posInList + "]/div[contains(@class,'o_ap_planned')]");
		OOGraphene.waitElement(plannedBy, browser);
		return this;
	}
	
	public AppointmentPage confirmAppointmentFindingByDay(int day, UserVO user) {
		String dayStr = dayToString(day);
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By userBy = By.xpath("//div[@class='o_sel_appointment_confirm_finding']//table//td[text()[contains(.,'" + user.getFirstName() +"')]]");
		OOGraphene.waitElement(userBy, browser);
		
		By saveBy = By.xpath("//div[@class='o_sel_appointment_confirm_finding']//button[contains(@class,'btn-primary')]");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}
	
	public static final String dayToString(int day) {
		if(day < 10) {
			return "0" + day;
		}
		return Integer.toString(day);
	}
	
	public static final int getDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

}
