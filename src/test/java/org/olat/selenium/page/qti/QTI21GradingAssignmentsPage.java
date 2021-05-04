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
package org.olat.selenium.page.qti;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 12 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21GradingAssignmentsPage {
	
	private final WebDriver browser;
	
	public QTI21GradingAssignmentsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21GradingAssignmentsPage assertAssignmentUnassigned(String testNodeTitle) {
		By graderBy = By.xpath("//div[contains(@class,'o_sel_grading_assignments_list')]//table//tr[td/span/i[contains(@class,'o_grad_assignment_unassigned')]]/td/a[text()[contains(.,'" + testNodeTitle + "')]]");
		OOGraphene.waitElement(graderBy, browser);
		return this;
	}
	
	public QTI21GradingAssignmentsPage openAssignmentUnassignedTool(String testNodeTitle) {
		By toolBy = By.xpath("//div[contains(@class,'o_sel_grading_assignments_list')]//table//tr[td/span/i[contains(@class,'o_grad_assignment_unassigned')]][td/a[text()[contains(.,'" + testNodeTitle + "')]]]/td/a[i[contains(@class,'o_icon_actions o_icon-fws')]]");
		OOGraphene.waitElement(toolBy, browser);
		OOGraphene.scrollTo(toolBy, browser);
		browser.findElement(toolBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitCallout(browser);
		return this;
	}
	
	public QTI21GradingAssignmentsPage addGrader(UserVO user) {
		By graderBy = By.xpath("//ul[contains(@class,'o_dropdown')]/li/a[contains(@onclick,'assign_grader')]");
		OOGraphene.waitElement(graderBy, browser);
		browser.findElement(graderBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalWizard(browser);
		
		MembersWizardPage wizard = new MembersWizardPage(browser)
			.searchOneMember(user, true);
		
		OOGraphene.nextStep(browser);
		wizard.finish();
		return this;
	}
	

}
