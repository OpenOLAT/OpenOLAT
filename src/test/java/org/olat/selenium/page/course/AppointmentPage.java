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

import org.olat.selenium.page.core.UserSearchPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 3 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentPage {
	
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
	
	public AppointmentPage assertOnTopic(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_appointment_header')]/h3[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
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
	

	public AppointmentPage assertOnConfirmAppointment(int day) {
		String dayStr = dayToString(day);
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		return this;
	}
	
	public AppointmentPage confirmAppointment(int day) {
		String dayStr = dayToString(day);
		By confirmBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_main_cont')][div[contains(@class,'o_datecomp')]/div[contains(@class,'o_day_" + dayStr + "')]]//a[contains(@class,'o_button_confirm')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		
		By confirmedBy = By.xpath("//div[contains(@class,'o_appointments')]//div[contains(@class,'o_table_row')]/div[contains(@class,'o_ap_confirmed')]//div[contains(@class,'o_day_" + dayStr + "')]");
		OOGraphene.waitElement(confirmedBy, browser);
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
