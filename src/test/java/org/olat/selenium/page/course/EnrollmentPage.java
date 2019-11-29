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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 09.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EnrollmentPage {

	private final WebDriver browser;
	
	public EnrollmentPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EnrollmentPage assertOnEnrolmentPage() {
		By pageBy = By.className("o_course_run_statusinfo");
		List<WebElement> pageEls = browser.findElements(pageBy);
		Assert.assertFalse(pageEls.isEmpty());
		return this;
	}
	
	public EnrollmentPage assertNoEnrollmentAllowed(){
		By enrollBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr//td//a[contains(@onclick,'cmd.enroll.in.group')]");
		List<WebElement> pageEls = browser.findElements(enrollBy);
		Assert.assertTrue(pageEls.isEmpty());
		return this;
	}
	/**
	 * Enroll without wait busy to make them very quick.
	 * 
	 * @return
	 */
	public EnrollmentPage enrollNoWait() {
		By enrollBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr//td//a[contains(@onclick,'cmd.enroll.in.group')]");
		List<WebElement> pageEls = browser.findElements(enrollBy);
		if(pageEls.size() > 0) {
			pageEls.get(0).click();
		}
		return this;
	}
	
	/**
	 * Enroll to multiple groups
	 * 
	 * @return
	 */
	public EnrollmentPage multiEnroll(int enrollCount) {
		for(int i = 1;i<=enrollCount; i++){
			WebElement selectLink = browser.findElement(By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr["+i+"]//td//a[contains(@onclick,'cmd.enroll.in.group')]"));
			selectLink.click();
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	/**
	 * Check if the enrollment return an error message or if the cancel
	 * link appears.
	 * 
	 * @return
	 */
	public boolean hasError() {
		OOGraphene.waitBusy(browser);
		By errorMsgBy = By.cssSelector("div.modal-body.alert.alert-danger");
		By cancelBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr//td//a[contains(@onclick,'cmd.enrolled.cancel')]");
		
		List<WebElement> errorEls = browser.findElements(errorMsgBy);
		List<WebElement> cancelLinkEls = browser.findElements(cancelBy);
		
		boolean error = false;
		for(int i=20; i-->0; ) {
		
			if(cancelLinkEls.size() > 0) {
				error = false;
				break;
			} else if (errorEls.size() > 0) {
				error = true;
				break;
			}
			
			OOGraphene.waitingALittleBit();
			errorEls = browser.findElements(errorMsgBy);
			cancelLinkEls = browser.findElements(cancelBy);
		}
		return error;
	}

}
