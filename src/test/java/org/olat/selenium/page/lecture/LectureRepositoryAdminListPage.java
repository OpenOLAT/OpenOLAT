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
package org.olat.selenium.page.lecture;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 15 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositoryAdminListPage {
	
	private final WebDriver browser;
	
	public LectureRepositoryAdminListPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LectureRepositoryAdminListPage asssertOnLectureList() {
		By lecturesBy = By.cssSelector("div.o_sel_repo_lectures_list");
		OOGraphene.waitElement(lecturesBy, browser);
		return this;
	}
	
	public LectureRepositoryAdminListPage assertOnLectureBlock(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_table_flexi')]//td[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public EditLectureBlockPage newLectureBlock() {
		By addLectureBy = By.cssSelector("div.o_sel_repo_lectures_list a.o_sel_repo_add_lecture");
		browser.findElement(addLectureBy).click();
		OOGraphene.waitModalDialog(browser);
		return new EditLectureBlockPage(browser);
	}
	
	public ImportLecturesBlocksWizard importLecturesBlocks() {
		By importLecturesBy = By.cssSelector("div.o_sel_repo_lectures_list a.o_sel_repo_import_lectures");
		browser.findElement(importLecturesBy).click();
		OOGraphene.waitModalDialog(browser);
		return new ImportLecturesBlocksWizard(browser);
	}
	

	
	

}
