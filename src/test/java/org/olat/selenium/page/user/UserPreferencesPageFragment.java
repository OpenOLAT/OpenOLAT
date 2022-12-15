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
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * The user system preferences.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserPreferencesPageFragment {
	
	public static final By noneRadio = By.xpath("//div[contains(@class,'o_sel_home_settings_resume')]//input[@type='radio' and @value='none']");
	public static final By autoRadio = By.xpath("//div[contains(@class,'o_sel_home_settings_resume')]//input[@type='radio' and @value='auto']");
	public static final By ondemandRadio = By.xpath("//div[contains(@class,'o_sel_home_settings_resume')]//input[@type='radio' and @value='ondemand']");
	
	public static final By saveSystemPrefsButton = By.xpath("//div[contains(@class,'o_sel_home_settings_prefs_buttons')]//button[@type='button']");
	public static final By resetPrefsButton = By.xpath("//div[contains(@class,'o_sel_home_settings_reset_sysprefs_buttons')]//button[@type='button']");
	
	private static final By resumeFieldsetBy = By.className("o_sel_home_settings_resume");
	
	private final WebDriver browser;
	
	public UserPreferencesPageFragment(WebDriver browser) {
		this.browser = browser;
	}

	/**
	 * Check that the user preferences page is displayed.
	 * 
	 * @return The user preferences page fragment
	 */
	public UserPreferencesPageFragment assertOnUserPreferences() {
		OOGraphene.waitElement(resumeFieldsetBy, browser);
		Assert.assertTrue(browser.findElement(resumeFieldsetBy).isDisplayed());
		return this;
	}
	
	/**
	 * Set and save the resume preferences.
	 * 
	 * @param resume
	 * @return
	 */
	public UserPreferencesPageFragment setResume(ResumeOption resume) {
		OOGraphene.waitElement(resumeFieldsetBy, browser);
		WebElement radio = null;
		switch(resume) {
			case none: radio = browser.findElement(noneRadio); break;
			case auto: radio = browser.findElement(autoRadio); break;
			case ondemand: radio = browser.findElement(ondemandRadio); break;
		}
		
		radio.click();
		OOGraphene.waitBusy(browser);
		
		By saveSystemSettingsButton = By.cssSelector("div.o_sel_home_settings_gui_buttons.form-inline button[type='button']");
		browser.findElement(saveSystemSettingsButton).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitBusyAndScrollTop(browser);//wait scroll top
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	/**
	 * Set the landing page
	 * @param businessPath
	 * @return
	 */
	public UserPreferencesPageFragment setLandingPage(String businessPath) {
		By landingPageBy = By.cssSelector("div.o_sel_home_settings_landing_page input[type='text']");
		browser.findElement(landingPageBy).sendKeys(businessPath);
		
		By saveSystemSettingsButton = By.cssSelector("div.o_sel_home_settings_gui_buttons.form-inline button[type='button']");
		browser.findElement(saveSystemSettingsButton).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitBusyAndScrollTop(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public UserPreferencesPageFragment setLanguage(String language) {
		By selectLangBy = By.cssSelector("div.o_sel_home_settings_language select");
		WebElement selectLang = browser.findElement(selectLangBy);
		new Select(selectLang).selectByValue(language);
		
		WebElement saveButton = browser.findElement(saveSystemPrefsButton);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitBusyAndScrollTop(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public UserPreferencesPageFragment resetPreferences() {
		By checksBy = By.cssSelector("fieldset.o_sel_home_settings_reset_sysprefs .checkbox input");
		List<WebElement> checks = browser.findElements(checksBy);
		Assert.assertEquals(3, checks.size());
		
		for(WebElement check:checks) {
			check.click();
		}
		
		WebElement saveButton = browser.findElement(resetPrefsButton);
		saveButton.click();
		OOGraphene.waitElement(LoginPage.loginFormBy, browser);
		return this;
	}
	
	public enum ResumeOption {
		none,
		auto,
		ondemand
	}
}
