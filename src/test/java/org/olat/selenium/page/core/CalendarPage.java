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

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the calendar
 * 
 * Initial date: 27.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPage {
	
	public static final By calendarToolbatBy = By.className("o_cal_toptoolbar");
	
	@Drone
	private WebDriver browser;
	
	public CalendarPage() {
		//
	}
	
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
		List<WebElement> calendarToolbarsEl = browser.findElements(calendarToolbatBy);
		Assert.assertFalse(calendarToolbarsEl.isEmpty());
		return this;
	}

}
