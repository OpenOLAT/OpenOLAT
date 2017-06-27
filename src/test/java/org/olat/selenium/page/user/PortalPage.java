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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the portal in view and edit mode
 * 
 * 
 * Initial date: 04.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortalPage {
	
	public static final By quickStartBy = By.className("o_portlet_quickstart");
	public static final By notesBy = By.className("o_portlet_notes");
	
	public static final By inactiveBy = By.cssSelector(".o_portal .o_inactive");
	public static final By deleteBy = By.className("o_portlet_edit_delete");
	public static final By addBy = By.className("o_portlet_edit_add");
	private static final By moveRightBy = By.className("o_portlet_edit_right");
	private static final By moveLeftBy = By.className("o_icon_move_left");
	private static final By moveUpBy = By.className("o_portlet_edit_up");
	private static final By moveDownBy = By.className("o_portlet_edit_down");
	
	private final WebDriver browser;
	
	public PortalPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PortalPage edit() {
		By editBy = By.cssSelector(".o_home_portaleditlink a.btn-default");
		WebElement editButton = browser.findElement(editBy);
		editButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PortalPage finishEditing() {
		By editBy = By.cssSelector(".o_home_portaleditlink a.btn-primary");
		WebElement editButton = browser.findElement(editBy);
		editButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PortalPage assertPortlet(By portletBy) {
		List<WebElement> panels = browser.findElements(portletBy);
		Assert.assertFalse(panels.isEmpty());
		return this;
	}
	
	public PortalPage assertNotPortlet(By portletBy) {
		List<WebElement> panels = browser.findElements(portletBy);
		Assert.assertTrue(panels.isEmpty());
		return this;
	}
	
	public PortalPage enable(By portletBy) {
		return doAction(portletBy, addBy);
	}
	
	public PortalPage disable(By portletBy) {
		return doAction(portletBy, deleteBy);
	}
	
	public PortalPage moveUp(By portletBy) {
		return doAction(portletBy, moveUpBy);
	}

	public PortalPage moveDown(By portletBy) {
		return doAction(portletBy, moveDownBy);
	}
	
	public PortalPage moveRight(By portletBy) {
		return doAction(portletBy, moveRightBy);
	}
	
	public PortalPage moveLeft(By portletBy) {
		return doAction(portletBy, moveLeftBy);
	}
	
	private PortalPage doAction(By portletBy, By action) {
		List<WebElement> panels = browser.findElements(portletBy);
		Assert.assertFalse(panels.isEmpty());
		WebElement panel = panels.get(0);
		
		List<WebElement> buttons = panel.findElements(action);
		Assert.assertEquals(1, buttons.size());
		buttons.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
