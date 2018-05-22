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
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 30.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAdminPage {

	public static final By menuTreeeBy = By.className("o_sel_useradmin_search");
	
	private WebDriver browser;
	
	public UserAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static UserAdminPage getUserAdminPage(WebDriver browser) {
		OOGraphene.waitElement(menuTreeeBy, browser);
		WebElement main = browser.findElement(By.id("o_main"));
		Assert.assertTrue(main.isDisplayed());
		return new UserAdminPage(browser);
	}
	
	public UserAdminPage openCreateUser() {
		By createBy = By.cssSelector(".o_tree li.o_sel_useradmin_create>div>span.o_tree_link>a");
		WebElement createMenuItem = browser.findElement(createBy);
		createMenuItem.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public UserAdminPage openSearchUser() {
		//In case it stay in the way
		OOGraphene.closeBlueMessageWindow(browser);
		By createBy = By.cssSelector(".o_tree li.o_sel_useradmin_search>div>span.o_tree_link>a");
		OOGraphene.waitElement(createBy, 5, browser);
		browser.findElement(createBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public UserAdminPage openDirectDeleteUser() {
		By createBy = By.cssSelector(".o_tree li.o_sel_useradmin_direct_delete>div>span.o_tree_link>a");
		WebElement createMenuItem = browser.findElement(createBy);
		createMenuItem.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Search a user in the search form of the direct delete
	 * workflow.
	 * 
	 * @param username
	 * @return
	 */
	public UserAdminPage searchUserToDelete(String username) {
		OOGraphene.closeBlueMessageWindow(browser);
		By createBy = By.cssSelector("fieldset.o_sel_user_search_form div.o_sel_user_search_username input[type='text']");
		OOGraphene.waitElement(createBy, 5, browser);
		browser.findElement(createBy).sendKeys(username);
		
		//search
		By searchBy = By.cssSelector("fieldset.o_sel_user_search_form a.o_sel_user_search_button");
		browser.findElement(searchBy).click();
		OOGraphene.waitBusy(browser);	
		return this;
	}
	
	/**
	 * After searching a user, you can select it and delete it.
	 * 
	 * @param lastName
	 * @return
	 */
	public UserAdminPage selectAndDeleteUser(String lastName) {
		By checkBy = By.cssSelector("fieldset.o_sel_usersearch_searchform table input[type='checkbox']");
		browser.findElement(checkBy).click();
		OOGraphene.waitBusy(browser);
		
		//select
		By selectBy = By.cssSelector("fieldset.o_sel_usersearch_searchform div.o_table_wrapper div.o_table_buttons button.btn.btn-default");
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		//confirm
		By usernameBy = By.xpath("//div[contains(@class,'modal-dialog')]//div[@class='o_error']/strong[text()[contains(.,'" + lastName + "')]]");
		List<WebElement> confirmUserEls = browser.findElements(usernameBy);
		Assert.assertFalse(confirmUserEls.isEmpty());
		
		By confirmCheckBy = By.cssSelector("div.o_sel_confirm_delete_user input[type='checkbox']");
		WebElement confirmCheckEl = browser.findElement(confirmCheckBy);
		OOGraphene.check(confirmCheckEl, Boolean.TRUE);
		
		By buttonsBy = By.cssSelector("div.modal-dialog div.modal-body a.btn.o_sel_delete_user");
		browser.findElement(buttonsBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public UserAdminPage openImportUsers() {
		By importBy = By.cssSelector(".o_tree li.o_sel_useradmin_import>div>span.o_tree_link>a");
		browser.findElement(importBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ImportUserPage startImport() {
		By importBy = By.cssSelector("a.o_sel_id_start_import_user_button.btn-primary");
		browser.findElement(importBy).click();
		OOGraphene.waitBusy(browser);
		By dataBy = By.cssSelector("fieldset.o_sel_import_users_data");
		OOGraphene.waitElement(dataBy, browser);
		return new ImportUserPage(browser);
	}
	
	public static UserVO createUserVO(String username, String firstName, String lastName, String email, String password) {
		UserVO userVo = new UserVO();
		userVo.setLogin(username);
		userVo.setFirstName(firstName);
		userVo.setLastName(lastName);
		userVo.setEmail(email);
		userVo.setPassword(password);
		return userVo;
	}
	
	public UserAdminPage fillUserForm(String username, String firstName, String lastName, String email, String password) {
		UserVO userVo = createUserVO(username, firstName, lastName, email, password);
		return fillUserForm(userVo);
	}
	
	public UserAdminPage fillUserForm(UserVO userVo) {
		By usernameBy = By.cssSelector(".o_sel_id_create .o_sel_id_username input[type='text']");
		WebElement usernameEl = browser.findElement(usernameBy);
		usernameEl.sendKeys(userVo.getLogin());
		
		By firstNameBy = By.cssSelector(".o_sel_id_create .o_sel_id_firstname input[type='text']");
		WebElement firstNameEL = browser.findElement(firstNameBy);
		firstNameEL.sendKeys(userVo.getFirstName());
		
		By lastNameBy = By.cssSelector(".o_sel_id_create .o_sel_id_lastname input[type='text']");
		WebElement lastNameEl = browser.findElement(lastNameBy);
		lastNameEl.sendKeys(userVo.getLastName());
		
		By emailBy = By.cssSelector(".o_sel_id_create .o_sel_id_email input[type='text']");
		WebElement emailEl = browser.findElement(emailBy);
		emailEl.sendKeys(userVo.getEmail());

		By password1By = By.cssSelector(".o_sel_id_create .o_sel_id_password1 input[type='password']");
		WebElement password1El = browser.findElement(password1By);
		password1El.sendKeys(userVo.getPassword());

		By password2By = By.cssSelector(".o_sel_id_create .o_sel_id_password2 input[type='password']");
		WebElement password2El = browser.findElement(password2By);
		password2El.sendKeys(userVo.getPassword());
		
		By saveBy = By.cssSelector(".o_sel_id_create button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public UserAdminPage assertOnUserEditView(String username) {
		By userInfoTdBy = By.cssSelector(".o_user_infos table tr td");
		List<WebElement> tds = browser.findElements(userInfoTdBy);
		boolean found = false;
		for(WebElement td:tds) {
			String text = td.getText();
			if(text != null && text.equals(username)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		return this;
	}
	
	public UserAdminPage searchByUsername(String username) {
		By usernameBy = By.cssSelector(".o_sel_user_search_form .o_sel_user_search_username input[type='text']");
		WebElement usernameEl = browser.findElement(usernameBy);
		usernameEl.sendKeys(username);
		
		By searchBy = By.cssSelector(".o_sel_user_search_form a.btn-default");
		browser.findElement(searchBy).click();
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	
	public UserAdminPage assertOnUserInList(String username) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr//td//a[text()[contains(.,'" + username + "')]]");
		List<WebElement> usernameEls = browser.findElements(userLinksBy);
		Assert.assertFalse(usernameEls.isEmpty());
		return this;
	}
	
	public UserAdminPage assertNotInUserList(String username) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr//td//a[text()[contains(.,'" + username + "')]]");
		List<WebElement> usernameEls = browser.findElements(userLinksBy);
		Assert.assertTrue(usernameEls.isEmpty());
		return this;
	}
	
	public UserAdminPage selectByUsername(String username) {
		By rows = By.cssSelector("div.o_table_wrapper table tbody tr");
		By usernameLinksBy = By.xpath("td//a[text()[contains(.,'" + username + "')]]");
		By selectBy = By.xpath("td//a[contains(@href,'select.user')]");
		
		WebElement selectEl = null;
		List<WebElement> rowEls = browser.findElements(rows);
		for(WebElement rowEl:rowEls) {
			List<WebElement> usernameLinkEls = rowEl.findElements(usernameLinksBy);
			if(usernameLinkEls.size() > 0) {
				List<WebElement> selectEls = rowEl.findElements(selectBy);
				if(selectEls.size() > 0) {
					selectEl = selectEls.get(0);
				}
				
			}
		}
		
		Assert.assertNotNull(selectEl);
		selectEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
