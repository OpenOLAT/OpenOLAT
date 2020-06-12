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
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21GradingGradersPage {
	
	private final WebDriver browser;
	
	public QTI21GradingGradersPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21GradingGradersPage addGrader(UserVO user) {
		By addGraderBy = By.cssSelector("div.o_button_group a.o_sel_repo_grading_add_graders");
		OOGraphene.waitElement(addGraderBy, browser);
		browser.findElement(addGraderBy).click();
		
		MembersWizardPage wizard = new MembersWizardPage(browser)
			.searchMember(user, true);
		
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_grader_import_overview"), browser);

		OOGraphene.nextStep(browser);
		wizard.finish();
		return this;
	}
	
	public QTI21GradingGradersPage assertGrader(UserVO user) {
		By graderBy = By.xpath("//div[@class='o_sel_graders_list']//table//tr/td/a[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(graderBy, browser);
		return this;
	}
	
	/**
	 * The method is not precise.
	 * 
	 * @param user The grader
	 * @param assignments A number
	 * 
	 * @return Itself
	 */
	public QTI21GradingGradersPage assertGraderAssignmentsDone(UserVO user, int assignments) {
		By graderBy = By.xpath("//div[@class='o_sel_graders_list']//table//tr[td/a[contains(@onclick,'done')][text()[contains(.,'" + assignments + "')]]]/td/a[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(graderBy, browser);
		return this;
	}
	

}
