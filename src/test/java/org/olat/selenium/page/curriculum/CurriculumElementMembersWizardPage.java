/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.curriculum;

import java.util.List;

import org.olat.modules.curriculum.ui.member.ConfirmationMembershipEnum;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 8 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementMembersWizardPage {
	
	private final WebDriver browser;
	
	public CurriculumElementMembersWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CurriculumElementMembersWizardPage searchMember(UserVO user, boolean admin) {
		searchMemberForm(user, admin);
		
		OOGraphene.nextStep(browser);

		// Select user
		By selectBy = By.xpath("//dialog[contains(@class,'modal')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td[contains(@class,'o_col_sticky_left')]/input[@name='tb_ms'][@type='checkbox']");
		WebElement selectEl = OOGraphene.waitElement(selectBy, browser);
		OOGraphene.check(selectEl, Boolean.TRUE);

		By oneSelectedBy = By.xpath("//dialog[contains(@class,'modal')]//div[contains(@class,'o_table_batch_buttons')]/span[contains(@id,'_mscount')][@class='o_table_batch_label']");
		OOGraphene.waitElement(oneSelectedBy, browser);
		
		OOGraphene.nextStep(browser);
		return this;
	}
	
	private CurriculumElementMembersWizardPage searchMemberForm(UserVO user, boolean admin) {
		//Search by username or first name
		By firstFieldBy = By.xpath("//fieldset[contains(@class,'o_sel_usersearch_searchform')]//input[@type='text'][1]");
		OOGraphene.waitElement(firstFieldBy, browser);
		By typeheadBy = By.xpath("//span[contains(@class,'twitter-typeahead')]/div[contains(@class,'tt-menu')]");
		OOGraphene.waitElementPresence(typeheadBy, 5, browser);
		
		String search = admin ? user.getLogin() : user.getFirstName();
		browser.findElement(firstFieldBy).sendKeys(search);
		OOGraphene.waitingALittleLonger();
		return this;
	}
	
	public CurriculumElementMembersWizardPage membership(ConfirmationMembershipEnum confirmation) {
		By settingsBy = By.cssSelector("fieldset.o_sel_curriculum_element_member_rights");
		OOGraphene.waitElement(settingsBy, browser);
		
		// Default is without
		if(confirmation == ConfirmationMembershipEnum.WITH) {
			By withBy = By.cssSelector("fieldset.o_sel_curriculum_element_member_rights input[type='radio'][name='confirmation.membership'][value='WITHOUT']");
			OOGraphene.waitElement(withBy, browser).click();
			
			By administrativeBy = By.cssSelector("fieldset.o_sel_curriculum_element_member_rights input[type='radio'][name='confirmation.membership.by'][value='ADMINISTRATIVE_ROLE']");
			OOGraphene.waitElement(administrativeBy, browser);
		}
		
		OOGraphene.nextStep(browser);
		return this;
	}
	
	public CurriculumElementMembersWizardPage membershipCommentOnly() {
		By commentBy = By.cssSelector("fieldset.o_sel_curriculum_element_member_rights textarea");
		OOGraphene.waitElement(commentBy, browser);
		
		OOGraphene.nextStep(browser);
		return this;
	}

	public CurriculumElementMembersWizardPage confirmation(UserVO user) {
		By overviewBy = By.xpath("//div[contains(@class,'o_sel_curriculum_element_member_overview')]//table//td/a[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(overviewBy, browser);
		
		OOGraphene.nextStep(browser);
		return this;
	}
	
	public CurriculumElementMembersWizardPage selectInvoiceOffer(String price, String addressId) {
		By invoiceBy = By.xpath("//dialog//fieldset[@id='o_cobooking_order']//label[span/span/span[text()[contains(.,'" + price + "')]]]/input[@type='radio']");
		WebElement invoiceEl = OOGraphene.waitElement(invoiceBy, browser);
		OOGraphene.check(invoiceEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		By selectAddress = By.cssSelector("dialog a.o_sel_billing_address_select");
		OOGraphene.waitElement(selectAddress, browser);
		
		// Check auto-select
		By addressSelectedBy = By.xpath("//div[contains(@class,'o_ac_billing_address_identifier')][text()[contains(.,'" + addressId + "')]]");
		List<WebElement> addressSelectedEls = browser.findElements(addressSelectedBy);
		if(addressSelectedEls.isEmpty()) {
			// select address
			OOGraphene.waitElement(selectAddress, browser).click();
			OOGraphene.waitModalDialog(browser, ".o_ac_billing_address_selection");
			
			By addressBy = By.xpath("//dialog//div[contains(@class,'o_ac_billing_address_selection')]//label[span/span/span[text()[contains(.,'" + addressId + "')]]]/input[@type='radio']");
			browser.findElement(addressBy).click();
			OOGraphene.waitBusy(browser);
			
			By selectBy = By.cssSelector("dialog div.o_ac_billing_address_selection button.btn.o_button_dirty");
			OOGraphene.waitElement(selectBy, browser).click();
			
			OOGraphene.waitModalDialogWithDivDisappears(browser, "o_ac_billing_address_selection");
		}
		
		OOGraphene.nextStep(browser);
		return this;
	}
	
	public CurriculumElementMembersWizardPage notification() {
		By contactBy = By.cssSelector("fieldset.o_sel_contact_form");
		OOGraphene.waitElement(contactBy, browser);
		
		OOGraphene.finishStep(browser);
		return this;
	}
	
	
}
