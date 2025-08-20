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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 16 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BadgeIssueWizardPage {
	
	private WebDriver browser;
	
	public BadgeIssueWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * 
	 * @param user The user to select
	 * @return Itself
	 */
	public BadgeIssueWizardPage searchRecipient(UserVO user) {
		new MembersWizardPage(browser).searchMember(user, true);
		return this;
	}
	
	/**
	 * Go to the confirmation
	 * 
	 * @return Itself
	 */
	public BadgeIssueWizardPage nextConfirmation() {
		new MembersWizardPage(browser).nextUsers();
		return this;
	}
	
	public BadgeIssueWizardPage finish() {
		OOGraphene.finishStep(browser, false);
		return this;
	}
}
