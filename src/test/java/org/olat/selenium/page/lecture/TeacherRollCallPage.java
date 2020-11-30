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

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 26 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherRollCallPage {
	
	private final WebDriver browser;
	
	public TeacherRollCallPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TeacherRollCallPage assertOnRollCall() {
		By rollCallBy = By.cssSelector("div.o_lectures_rollcall>fieldset");
		OOGraphene.waitElement(rollCallBy, browser);
		return this;
	}
	
	public TeacherRollCallPage assertOnClosedTable() {
		By tableBy = By.cssSelector("fieldset.o_sel_lecture_table_closed table.table");
		OOGraphene.waitElement(tableBy, browser);
		return this;
	}
	
	/**
	 * Set an absence per user and lecture
	 * @param user The user to set an absence
	 * @param col The lecture number
	 * @return Itself
	 */
	public TeacherRollCallPage setAbsence(UserVO user, String col) {
		String name = user.getFirstName();
		//div[contains(@class,'o_rollcall_table')]//table//tr[td[contains(text(),'Kanu-Rnd-34a2154f8-7d48-4a97-8aad-740bff74bd2a')]]/td[count(//div[contains(@class,'o_rollcall_table')]//table//tr/th[a[text()='1']]/preceding-sibling::th)+1]/div/label/input
		By checkBy = By.xpath("//div[contains(@class,'o_rollcall_table')]//table//tr[td[contains(text(),'" + name + "')]]/td[count(//div[contains(@class,'o_rollcall_table')]//table//tr/th[a[text()='L. " + col + "']]/preceding-sibling::th)+1]/div/label/input");
		WebElement checkEl = browser.findElement(checkBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * The close button in the roll call list.
	 * @return Itself
	 */
	public TeacherRollCallPage closeRollCall() {
		By closeBy = By.cssSelector("div.o_lectures_rollcall a.o_sel_lecture_close");
		browser.findElement(closeBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Simply confirm the closing of the roll call and choose
	 * a reason if there is one.
	 * 
	 * @return Itself
	 */
	public TeacherRollCallPage confirmCloseRollCall() {
		//check reasons
		By reasonsBy = By.id("o_fioeffective_reason_SELBOX");
		List<WebElement> reasonsEls = browser.findElements(reasonsBy);
		if(reasonsEls.size() > 0) {
			new Select(reasonsEls.get(0)).selectByIndex(1);
		}
		
		By confirmCloseBy = By.cssSelector("fieldset.o_sel_lecture_confirm_close_form button.btn-primary");
		browser.findElement(confirmCloseBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
}
