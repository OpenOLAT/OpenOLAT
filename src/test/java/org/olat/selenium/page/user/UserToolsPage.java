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
package org.olat.selenium.page.user;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.core.FolderPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.lecture.LecturesProfilePage;
import org.olat.selenium.page.portfolio.MediaCenterPage;
import org.olat.selenium.page.portfolio.PortfolioV2HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserToolsPage {
	
	public static final By mySettingsClassName = By.className("o_sel_user_tools-mysettings");

	private final WebDriver browser;
	
	public UserToolsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check if the notification panel is displayed in home.
	 * @return
	 */
	public UserToolsPage assertOnNotifications() {
		By notificationPanelBy = By.cssSelector("div.o_notifications_news_wrapper");
		OOGraphene.waitElement(notificationPanelBy, browser);
		WebElement notificationPanel = browser.findElement(notificationPanelBy);
		Assert.assertTrue(notificationPanel.isDisplayed());
		return this;
	}
	
	/**
	 * Check if we see the calendar.
	 * @return
	 */
	public UserToolsPage assertOnCalendar() {
		By calendarBy = By.cssSelector("div.o_cal div.fc-view-harness.fc-view-harness-active");
		OOGraphene.waitElement(calendarBy, browser);
		WebElement calendarEl = browser.findElement(calendarBy);
		Assert.assertTrue(calendarEl.isDisplayed());
		return this;
	}
	
	/**
	 * Check if we see the calendar.
	 * @return
	 */
	public FolderPage assertOnFolder() {
		return new FolderPage(browser)
			.assertOnFolderCmp();
	}
	
	/**
	 * Open the user menu with the tools.
	 * 
	 * @return The user menu page
	 */
	public UserToolsPage openUserToolsMenu() {
		By toolbarCaretBy = By.id("o_sel_navbar_my_menu_caret");
		OOGraphene.waitElement(toolbarCaretBy, browser);
		browser.findElement(toolbarCaretBy).click();
		
		try {
			OOGraphene.waitNavBarTransition(browser);
			OOGraphene.waitElement(mySettingsClassName, browser);
			
			By personnalToolsBy = By.className("o_sel_menu_tools");
			List<WebElement> personnalTools = browser.findElements(personnalToolsBy);
			Assert.assertEquals(1, personnalTools.size());
			Assert.assertTrue(personnalTools.get(0).isDisplayed());
		} catch (Exception | Error e) {
			OOGraphene.takeScreenshot("Assert user tool", browser);
			throw e;
		}
		
		return this;
	}
	
	/**
	 * Open the user settings.
	 * 
	 * @return The user sesstings page fragment
	 */
	public UserSettingsPage openMySettings() {
		WebElement mySettingsLink = browser.findElement(mySettingsClassName);
		Assert.assertTrue(mySettingsLink.isDisplayed());
		mySettingsLink.click();
		OOGraphene.waitBusy(browser);
		return new UserSettingsPage(browser);
	}
	
	public UserProfilePage openMyProfil() {
		By profilBy = By.cssSelector("li>a.o_sel_user_tools-profil");
		OOGraphene.waitElement(profilBy, browser);
		browser.findElement(profilBy).click();
		OOGraphene.waitBusy(browser);
		By userFormBy =  By.cssSelector("div.o_user_profile_form");
		OOGraphene.waitElement(userFormBy, browser);
		return new UserProfilePage(browser);
	}
	
	public EfficiencyStatementPage openMyEfficiencyStatement() {
		By efficiencyStatementsBy = By.className("o_sel_user_tools-effstatements");
		WebElement efficiencyStatementsLink = browser.findElement(efficiencyStatementsBy);
		Assert.assertTrue(efficiencyStatementsLink.isDisplayed());
		efficiencyStatementsLink.click();
		OOGraphene.waitBusy(browser);
		return new EfficiencyStatementPage(browser);
	}
	
	public UserSettingsPage openPassword() {
		WebElement passwordLink = browser.findElement(By.className("o_sel_user_tools-mypassword"));
		Assert.assertTrue(passwordLink.isDisplayed());
		passwordLink.click();
		OOGraphene.waitBusy(browser);
		return new UserSettingsPage(browser);
	}
	
	public PortfolioV2HomePage openPortfolioV2() {
		By linkBy = By.className("o_sel_user_tools-PortfolioV2");
		browser.findElement(linkBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeOffCanvas(browser);
		PortfolioV2HomePage page = new PortfolioV2HomePage(browser);
		page.assertHome();
		return page;
	}
	
	public LecturesProfilePage openLectures() {
		By myLecturesBy = By.className("o_sel_user_tools-mylectures");
		browser.findElement(myLecturesBy).click();
		OOGraphene.waitBusy(browser);
		return new LecturesProfilePage(browser);
	}
	
	public MediaCenterPage openMediaCenter() {
		By myLecturesBy = By.className("o_sel_user_tools-MediaCenter");
		browser.findElement(myLecturesBy).click();
		OOGraphene.waitBusy(browser);
		return new MediaCenterPage(browser);
	}
	
	/**
	 * Log out and wait until the login form appears
	 */
	public void logout() {
		openUserToolsMenu();

		By logoutBy = By.className("o_logout");
		browser.findElement(logoutBy).click();
		OOGraphene.waitElement(LoginPage.loginFormBy, browser);
	}
}