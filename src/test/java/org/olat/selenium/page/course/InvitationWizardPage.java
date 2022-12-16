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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 16 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationWizardPage {
	
	private WebDriver browser;
	
	public InvitationWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public InvitationWizardPage newInvitation(String email) {
		By emailBy = By.cssSelector(".o_sel_invitation_email input[type='text']");
		browser.findElement(emailBy).sendKeys(email);
		return this;
	}
	
	public InvitationWizardPage nextUserInfos(String firstName, String lastName, String email) {
		OOGraphene.nextStep(browser);	
		By firstNameBy = By.cssSelector(".o_sel_user_firstName input[type='text']");
		OOGraphene.waitElement(firstNameBy, browser);
		browser.findElement(firstNameBy).sendKeys(firstName);
		By lastNameBy = By.cssSelector(".o_sel_user_lastName input[type='text']");
		browser.findElement(lastNameBy).sendKeys(lastName);
		By emailBy = By.xpath("//div[contains(@class,'o_sel_user_email')]//input[@type='text'][@value='" + email + "']");
		OOGraphene.waitElement(emailBy, browser);
		return this;
	}
	

	public InvitationWizardPage nextPermissions(boolean coach, boolean participant) {
		OOGraphene.nextStep(browser);
		By permissionsBy = By.cssSelector(".o_sel_edit_permissions fieldset");
		OOGraphene.waitElement(permissionsBy, browser);
		
		if(coach) {
			//
		}
		
		if(participant) {
			//
		}
		
		return this;
	}
	
	public InvitationWizardPage nextEmail() {
		OOGraphene.nextStep(browser);
		By contactBy = By.cssSelector("fieldset.o_sel_contact_form");
		OOGraphene.waitElement(contactBy, browser);
		OOGraphene.waitTinymce(browser);
		OOGraphene.finishStep(browser);
		return this;
	}
}
