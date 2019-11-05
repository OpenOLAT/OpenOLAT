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

import org.olat.selenium.page.core.FolderPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 5 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantFolderPage {
	
	private final WebDriver browser;
	
	public ParticipantFolderPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ParticipantFolderPage assertOnParticipantsList() {
		By listBy = By.cssSelector("div.o_sel_pf_participants_list table");
		OOGraphene.waitElement(listBy, browser);
		return this;
	}
	
	public ParticipantFolderPage assertOnParticipant(String firstName) {
		By inListBy = By.xpath("//div[contains(@class,'o_sel_pf_participants_list')]//table//td/a[contains(text(),'" + firstName + "')]");
		OOGraphene.waitElement(inListBy, browser);
		return this;
	}
	
	public ParticipantFolderPage openParticipantFolder(String firstName) {
		By inListBy = By.xpath("//div[contains(@class,'o_sel_pf_participants_list')]//table//tr[td/a[contains(text(),'" + firstName + "')]]/td/a[contains(@onclick,'open.box')][i]");
		OOGraphene.waitElement(inListBy, browser);
		browser.findElement(inListBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public FolderPage openReturnBox() {
		By returnBoxBy = By.xpath("//div[@class='o_briefcase_folder']//table/tbody/tr[1]/td/a[i[contains(@class,'o_filetype_folder')]]");
		OOGraphene.waitElement(returnBoxBy, browser);
		browser.findElement(returnBoxBy).click();
		OOGraphene.waitBusy(browser);
		return new FolderPage(browser);
	}
	
	public FolderPage openDropBox() {
		By returnBoxBy = By.xpath("//div[@class='o_briefcase_folder']//table/tbody/tr[2]/td/a[i[contains(@class,'o_filetype_folder')]]");
		OOGraphene.waitElement(returnBoxBy, browser);
		browser.findElement(returnBoxBy).click();
		OOGraphene.waitBusy(browser);
		return new FolderPage(browser);
	}
	

}
