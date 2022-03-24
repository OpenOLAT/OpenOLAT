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
package org.olat.selenium.page.core;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 22.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdministrationMessagesPage {

	private final WebDriver browser;
	
	public AdministrationMessagesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AdministrationMessagesPage newMaintenanceMessage(String text) {
		By newMessageButtonBy = By.cssSelector("a.o_sel_maintenance_msg_edit");
		browser.findElement(newMessageButtonBy).click();
		OOGraphene.waitBusy(browser);
		
		OOGraphene.tinymce(text, browser);
		
		By saveBy = By.cssSelector(".o_sel_maintenance_msg_form button.btn-primary");
		browser.findElement(saveBy).click();
		By textBy = By.xpath("//div[@id='o_main_center_content_inner']//div[@id='o_msg_sticky_preview']/p[text()[contains(.,'" + text + "')]]");
		OOGraphene.waitElement(textBy, browser);
		OOGraphene.scrollTop(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public AdministrationMessagesPage clearMaintenanceMessage() {
		By clearBy = By.cssSelector("a.o_sel_maintenance_msg_clear");
		browser.findElement(clearBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
