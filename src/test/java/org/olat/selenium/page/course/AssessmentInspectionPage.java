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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 3 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionPage {
	
	private WebDriver browser;
	
	public AssessmentInspectionPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentInspectionPage assertOnStartInspection(String name) {
		By inspectionBy = By.xpath("//dialog/div[contains(@class,'modal-dialog')]//h2[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElementSlowly(inspectionBy, 15, browser);
		return this;
	}
	
	public AssessmentInspectionPage startInspection() {
		By startBy = By.xpath("//div[contains(@class,'modal-dialog')]//div[contains(@class,'_button_group')]//a[contains(@class,'o_sel_assessment_start')]");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public AssessmentInspectionPage closeInspection() {
		By closeBy = By.xpath("//div[h2/i[contains(@class,'o_icon_inspection')]]/div/a[contains(@class,'o_sel_assessment_inspection_close')]");
		OOGraphene.waitElement(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public AssessmentInspectionPage confirmCloseInspection() {
		By confirmBy = By.cssSelector(".o_sel_assessment_inspection_confirm_close .o_button_group button.btn.btn-primary");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

}
