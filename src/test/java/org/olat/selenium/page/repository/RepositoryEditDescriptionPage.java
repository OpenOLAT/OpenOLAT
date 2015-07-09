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

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Page to control the different settings of a repository entry.
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEditDescriptionPage {
	
	public static final By generaltabBy = By.className("o_sel_edit_repositoryentry");

	@Drone
	private WebDriver browser;
	
	public static RepositoryEditDescriptionPage getPage(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		return Graphene.createPageFragment(RepositoryEditDescriptionPage.class, main);
	}
	
	public RepositoryEditDescriptionPage assertOnGeneralTab() {
		List<WebElement> generalTabs = browser.findElements(generaltabBy);
		Assert.assertFalse(generalTabs.isEmpty());
		Assert.assertTrue(generalTabs.get(0).isDisplayed());
		return this;
	}
	
	public void clickToolbarBack() {
		browser.findElement(NavigationPage.toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
	}
	
}
