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
	
	public static final String quickStartBy = "o_portlet_quickstart";
	public static final String notesBy = "o_portlet_notes";
	
	public static final By inactiveBy = By.cssSelector(".o_portal .o_inactive");
	public static final By deleteBy = By.className("o_portlet_edit_delete");
	public static final By addBy = By.className("o_portlet_edit_add");
	
	private final WebDriver browser;
	
	public PortalPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PortalPage edit() {
		By editBy = By.cssSelector(".o_home_portaleditlink a.btn-default");
		browser.findElement(editBy).click();
		OOGraphene.waitElement(By.cssSelector(".o_portlet .o_edit_shim"), browser);
		return this;
	}
	
	public PortalPage finishEditing() {
		By editBy = By.cssSelector(".o_home_portaleditlink a.btn-primary");
		WebElement editButton = browser.findElement(editBy);
		editButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PortalPage assertPortlet(String portlet) {
		OOGraphene.waitElement(By.className(portlet), browser);
		return this;
	}
	
	public PortalPage assertNotPortlet(String portlet) {
		OOGraphene.waitElementAbsence(By.className(portlet), 5, browser);
		return this;
	}
	
	public PortalPage enable(String portletBy) {
		return doAction(portletBy, "o_icon_add");
	}
	
	public PortalPage disable(String portletBy) {
		return doAction(portletBy, "o_icon_delete_item");
	}
	
	public PortalPage moveUp(String portletBy) {
		return doAction(portletBy, "o_icon_move_up");
	}

	public PortalPage moveDown(String portletBy) {
		return doAction(portletBy, "o_icon_move_down");
	}
	
	public PortalPage moveRight(String portletBy) {
		return doAction(portletBy, "o_icon_move_right");
	}
	
	public PortalPage moveLeft(String portlet) {
		return doAction(portlet, "o_icon_move_left");
	}
	
	private PortalPage doAction(String portletClassname, String iconAction) {
		String xpathSelector = "//div[contains(@class,'" + portletClassname + "')]//a[i[contains(@class,'" + iconAction + "')]]";
		By actionBy = By.xpath(xpathSelector);
		OOGraphene.waitElement(actionBy, browser);
		browser.findElement(actionBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
