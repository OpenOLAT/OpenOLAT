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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryInfosPage {
	
	private final WebDriver browser;
	
	public RepositoryInfosPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public RepositoryInfosPage setAuthors(String authors) {
		By authorsBy = By.cssSelector("div.o_sel_repo_authors input.o_sel_repo_authors[type='text']");
		OOGraphene.waitElement(authorsBy, browser).sendKeys(authors);
		return this;
	}
	
	public RepositoryInfosPage save() {
		By saveBy = By.cssSelector("div.o_sel_repo_save_details button.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
