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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.dumbster.smtp.SmtpMessage;

/**
 * Drive the profile of a user.
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserProfilePage {
	
	private static final Logger log = Tracing.createLoggerFor(UserProfilePage.class);
	
	private WebDriver browser;
	
	public UserProfilePage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check if the profile panel is visible (but doesn't check if it is active)
	 * @return
	 */
	public UserProfilePage assertOnProfile() {
		By profileSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_usersettings_profile");
		try {
			OOGraphene.waitElement(profileSegmentBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Assertonprofile", browser);
			throw e;
		}
		WebElement profileSegmentEl = browser.findElement(profileSegmentBy);
		Assert.assertTrue(profileSegmentEl.isDisplayed());
		return this;
	}
	
	/**
	 * Check if the user name in the profile segment is visible.
	 * 
	 * @param username
	 * @return
	 */
	public UserProfilePage assertOnUsername(String username) {
		By usernameBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//input[@value='" + username + "']");
		OOGraphene.waitElement(usernameBy, browser);
		WebElement usernameEl = browser.findElement(usernameBy);
		Assert.assertTrue(usernameEl.isDisplayed());
		return this;
	}
	
	public UserProfilePage changeEmail(String newEmail) {
		By emailBy = By.cssSelector(".o_user_profile_form .o_user_profil_email input[type='text']");
		OOGraphene.waitElement(emailBy, browser);
		WebElement emailEl = browser.findElement(emailBy);
		emailEl.clear();
		emailEl.sendKeys(newEmail);
		return this;
	}
	
	public UserProfilePage assertOnEmail(String email) {
		By emailBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//div[contains(@class,'o_user_profil_email')]//input[@type='text'][@value='" + email + "']");
		OOGraphene.waitElement(emailBy, browser);
		return this;
	}
	
	public  UserProfilePage assertOnChangedEmail(String newEmail) {
		By emailBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//div[contains(@class,'o_user_profil_email')]/div[contains(@class,'o_form_example')]/div/b[text()[contains(.,'" + newEmail + "')]]");
		OOGraphene.waitElement(emailBy, browser);
		return this;
	}
	
	public UserProfilePage saveProfilAndConfirmEmail() {
		By saveBy = By.cssSelector(".o_user_profile_form button.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.scrollTop(browser);
		OOGraphene.waitModalDialog(browser);
		By yesBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(yesBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public String extractConfirmationLink(SmtpMessage message) {
		String body = message.getBody();
		byte[] bytes = message.toString().getBytes();
		try(InputStream in = new ByteArrayInputStream(bytes)) {
			body = IOUtils.toString(MimeUtility.decode(in, "quoted-printable"), "UTF-8");
		} catch(Exception e) {
			log.error("", e);
		}
		
		int index = body.indexOf("text/html");
		index = body.indexOf("http", index);
		if(index >= 0) {
			int lastIndex = body.indexOf('\n', index + 1);
			String link = body.substring(index, lastIndex);
			// hack
			link = link.replace("&amp;", "&")
					.replace("em=change", "emchange");
			return link;
		}
		return null;
	}
	
	public UserProfilePage loadConfirmationLink(String link) {
		browser.navigate().to(link);
		return this;
	}

}
