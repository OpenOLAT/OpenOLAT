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
package org.olat.selenium.page;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupsPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.user.PortalPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page fragment which control the navigation bar with the static sites.
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NavigationPage {
	
	public static final By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");

	@Drone
	private WebDriver browser;
	
	private By navigationSitesBy = By.cssSelector("ul.o_navbar_sites");
	private By authoringEnvTabBy = By.cssSelector("li.o_site_author_env > a");
	private By portalBy = By.cssSelector("li.o_site_portal > a");
	private By myCoursesBy = By.cssSelector("li.o_site_repository > a");
	private By userManagementBy = By.cssSelector("li.o_site_useradmin > a");
	private By administrationBy = By.cssSelector("li.o_site_admin > a");
	private	By groupsBy = By.cssSelector("li.o_site_groups > a");
	
	public static final By myCoursesAssertBy = By.xpath("//div[contains(@class,'o_segments')]//a[contains(@href,'search.mycourses.student')]");
	public static final By portalAssertBy = By.className("o_portal");
	
	public NavigationPage assertOnNavigationPage() {
		WebElement navigationSites = browser.findElement(navigationSitesBy);
		Assert.assertTrue(navigationSites.isDisplayed());
		return this;
	}
	
	public AuthoringEnvPage openAuthoringEnvironment() {
		navigate(authoringEnvTabBy);
		
		backToTheTop();
		OOGraphene.closeBlueMessageWindow(browser);
		
		WebElement main = browser.findElement(By.id("o_main"));
		return Graphene.createPageFragment(AuthoringEnvPage.class, main);
	}
	
	public PortalPage openPortal() {
		navigate(portalBy);
		WebElement main = browser.findElement(By.id("o_main"));
		return Graphene.createPageFragment(PortalPage.class, main);
	}
	
	public void openMyCourses() {
		navigate(myCoursesBy);
		OOGraphene.waitElement(myCoursesAssertBy);
	}
	
	public void openUserManagement() {
		navigate(userManagementBy);
	}
	
	public void openAdministration() {
		navigate(administrationBy);
	}
	
	public GroupsPage openGroups(WebDriver browser) {
		navigate(groupsBy);
		return new GroupsPage(browser);
	}
	
	private void navigate(By linkBy) {
		OOGraphene.closeBlueMessageWindow(browser);
		List<WebElement> links = browser.findElements(linkBy);
		Assert.assertFalse(links.isEmpty());

		if(!links.get(0).isDisplayed()) {
			 openNavigationSites();
		}

		links = browser.findElements(linkBy);
		Assert.assertFalse(links.isEmpty());
		OOGraphene.waitElement(links.get(0));
		links.get(0).click();
		OOGraphene.waitBusy();
		OOGraphene.waitingTransition();
	}
	
	private void openNavigationSites() {
		List<WebElement> openNavigations = browser.findElements(By.id("o_navbar_right-toggle"));
		if(openNavigations.size() > 0 || openNavigations.get(0).isDisplayed()) {
			//too small, open the black panel
			openNavigations.get(0).click();
			OOGraphene.waitingTransition();
		} 
	}
	
	public void openCourse(String title) {
		By courseTab = By.xpath("//li/a[@title='" + title + "']");
		WebElement courseLink = browser.findElement(courseTab);
		
		courseLink.click();
		OOGraphene.waitBusy();
		OOGraphene.closeBlueMessageWindow(browser);
	}
	
	public NavigationPage backToTheTop() {
		List<WebElement> backList = browser.findElements(toolbarBackBy);
		
		int count = 0;
		while(backList.size() > 0) {
			backList.get(count).click();
			OOGraphene.waitBusy();
			backList = browser.findElements(toolbarBackBy);
			
			Assert.assertTrue(count++ < 3);
		}

		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
}
