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
package org.olat.selenium.page.portfolio;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 23.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPublicationPage {
	
	private final WebDriver browser;

	public BinderPublicationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BinderPublicationPage openAccessMenu() {
		By accessBy = By.cssSelector("a.o_sel_pf_access");
		By accessMenuBy = By.cssSelector("ul.o_sel_pf_access");
		browser.findElement(accessBy).click();
		OOGraphene.waitElement(accessMenuBy, 5, browser);
		return this;
	}
	
	public BinderMemberWizardPage addMember() {
		By memberBy = By.cssSelector("a.o_sel_pf_access_member");
		browser.findElement(memberBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalWizard(browser);
		return new BinderMemberWizardPage(browser);
	}
	
	public BinderPublicationPage addInvitation(String email) {
		By invitationBy = By.cssSelector("a.o_sel_pf_access_invitation");
		browser.findElement(invitationBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By emailBy = By.cssSelector(".o_sel_pf_invitation_mail input[type='text']");
		OOGraphene.waitElement(emailBy, browser);
		browser.findElement(emailBy).sendKeys(email);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_invitation_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderPublicationPage fillInvitation(String firstName, String lastName) {
		OOGraphene.waitModalDialog(browser);
		
		By firstNameBy = By.cssSelector(".o_sel_pf_invitation_firstname input[type='text']");
		browser.findElement(firstNameBy).sendKeys(firstName);
		By lastNameBy = By.cssSelector(".o_sel_pf_invitation_lastname input[type='text']");
		browser.findElement(lastNameBy).sendKeys(lastName);
		return this;
	}
	
	public BinderPublicationPage fillAccessRights(String name, Boolean check) {
		By checkBy = By.xpath("//div[contains(@class,'o_portfolio_rights')]//table//tr[td[contains(text(),'" + name + "')]]/td//input[@type='checkbox']");
		WebElement checkEl = browser.findElement(checkBy);
		OOGraphene.scrollTo(checkBy, browser);
		OOGraphene.check(checkEl, check);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public String getInvitationURL() {
		By invitationUrl = By.cssSelector("p.o_sel_pf_invitation_url");
		String url = browser.findElement(invitationUrl).getText();
		return url.trim();
	}
	
	public BinderPublicationPage save() {
		By submitBy = By.cssSelector(".o_sel_pf_invitation_button_group button.btn-primary");
		OOGraphene.click(submitBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
}
