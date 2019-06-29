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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 26 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RollCallInterceptorPage {
	
	private final WebDriver browser;
	
	public RollCallInterceptorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TeacherRollCallPage start() {
		By formBy = By.cssSelector("div.o_sel_lecture_start_wizard");
		OOGraphene.waitElement(formBy, browser);
		By startBy = By.cssSelector("div.o_sel_lecture_start_wizard button.btn-primary");
		browser.findElement(startBy).click();
		OOGraphene.waitBusy(browser);
		return new TeacherRollCallPage(browser)
				.assertOnRollCall();
	}
	
	public TeacherRollCallWizardPage startMobile() {
		By formBy = By.cssSelector("div.o_sel_lecture_start_wizard");
		OOGraphene.waitElement(formBy, browser);
		By startWizardBy = By.cssSelector("div.o_sel_lecture_start_wizard a.o_sel_lecture_start_wizard");
		browser.findElement(startWizardBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.className("o_rollcall_wizard"), browser);
		return new TeacherRollCallWizardPage(browser);
	}

}
