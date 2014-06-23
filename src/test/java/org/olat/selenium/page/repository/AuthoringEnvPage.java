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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jcodec.common.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page to control the author environnment.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEnvPage {
	
	public static final By createModal = By.cssSelector("div.modal.o_sel_author_create_popup");
	public static final By displayNameInput = By.cssSelector("div.o_sel_author_displayname input");
	public static final By createSubmit = By.className("o_sel_author_create_submit");
	
	@Drone
	private WebDriver browser;
	
	@FindBy(css="a.o_sel_author_create")
	private WebElement createMenuCaret;
	@FindBy(css="ul.o_sel_author_create")
	private WebElement createMenu;
	
	public RepositoryEditDescriptionPage createCP(String title) {
		return openCreateDropDown()
			.clickCreateCP()
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	public RepositoryEditDescriptionPage createCourse(String title) {
		return openCreateDropDown()
			.clickCreateCourse()
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	/**
	 * Open the drop-down to create a new resource.
	 * @return
	 */
	public AuthoringEnvPage openCreateDropDown() {
		Assert.assertTrue(createMenuCaret.isDisplayed());
		createMenuCaret.click();
		OOGraphene.waitElement(createMenu);
		return this;
	}
	
	/**
	 * Click the link to create a course in the create drop-down
	 * @return
	 */
	public AuthoringEnvPage clickCreateCourse() {
		Assert.assertTrue(createMenu.isDisplayed());
		return clickCreate("o_sel_author_create-CourseModule");
	}
	
	/**
	 * Click the link to create a CP in the create drop-down
	 * @return
	 */
	public AuthoringEnvPage clickCreateCP() {
		Assert.assertTrue(createMenu.isDisplayed());
		return clickCreate("o_sel_author_create-FileResource.IMSCP");
	}
	
	private AuthoringEnvPage clickCreate(String type) {
		Assert.assertTrue(createMenu.isDisplayed());
		WebElement createLink = createMenu.findElement(By.className(type));
		Assert.assertTrue(createLink.isDisplayed());
		createLink.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	/**
	 * Fil the create form
	 * @param displayName
	 * @return
	 */
	public RepositoryEditDescriptionPage fillCreateForm(String displayName) {
		WebElement modal = browser.findElement(createModal);
		WebElement input = modal.findElement(displayNameInput);
		input.sendKeys(displayName);
		WebElement submit = modal.findElement(createSubmit);
		submit.click();
		OOGraphene.waitElement(RepositoryEditDescriptionPage.generaltabBy);
		
		WebElement main = browser.findElement(By.id("o_main"));
		return Graphene.createPageFragment(RepositoryEditDescriptionPage.class, main);
	}
	/**
	 * Short cut to create quickly a course
	 * @param title
	 */
	public void quickCreateCourse(String title) {
		RepositoryEditDescriptionPage editDescription = openCreateDropDown()
			.clickCreateCourse()
			.fillCreateForm(title)
			.assertOnGeneralTab();
			
		//from description editor, back to details and launch the course
		editDescription
			.clickToolbarBack()
			.assertOnTitle(title)
			.launch();
	}
}
