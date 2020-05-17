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
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 29 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderMemberWizardPage {
	
	private final WebDriver browser;
	private final MembersWizardPage delegate; 

	public BinderMemberWizardPage(WebDriver browser) {
		this.browser = browser;
		delegate = new MembersWizardPage(browser);
	}
	
	public BinderMemberWizardPage searchMember(UserVO user, boolean admin) {
		delegate.searchMember(user, admin);
		return this;
	}
	
	public BinderMemberWizardPage fillAccessRights(String name, Boolean check) {
		new BinderPublicationPage(browser).fillAccessRights(name, check);
		return this;
	}
	
	public BinderMemberWizardPage deSelectEmail() {
		By selectBy = By.cssSelector(".o_pf_sel_send_mail input[type='checkbox']");
		WebElement selectEl = browser.findElement(selectBy);
		OOGraphene.check(selectEl, Boolean.FALSE);
		return this;
	}
	
	public BinderMemberWizardPage nextUsers() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_user_import_overview"), browser);
		return this;
	}
	
	public BinderMemberWizardPage nextOverview() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_portfolio_rights"), browser);
		return this;
	}
	
	public BinderMemberWizardPage nextPermissions() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_contact_form"), browser);
		return this;
	}
	
	public BinderMemberWizardPage finish() {
		delegate.finish();
		return this;
	}

}
