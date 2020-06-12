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
package org.olat.selenium.page.coaching;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.qti.QTI21CorrectionPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingPage {
	
	private final WebDriver browser;
	
	public CoachingPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CoachingPage assertOnGrading() {
		By gradingBy = By.cssSelector("fieldset.o_sel_grading_assignments");
		OOGraphene.waitElement(gradingBy, browser);
		return this;
	}
	
	public QTI21CorrectionPage startGrading(String nodeTitle) {
		By gradingBy = By.xpath("//fieldset[@class='o_sel_grading_assignments']//table//tr[td/a[text()[contains(.,'" + nodeTitle + "')]]]/td/a[contains(@onclick,'grade')]");
		OOGraphene.waitElement(gradingBy, browser);
		browser.findElement(gradingBy).click();
		OOGraphene.waitBusy(browser);
		return new QTI21CorrectionPage(browser);
	}

}
