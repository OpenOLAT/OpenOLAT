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
package org.olat.selenium.page.course;

import org.olat.selenium.page.core.ContentEditorPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 27 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageElementPage {
	
	private final WebDriver browser;
	
	public PageElementPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PageElementPage assertOnPageElement() {
		By pageBy = By.cssSelector("div.o_course_node div.o_ce_wrapper");
		OOGraphene.waitElement(pageBy, browser);
		return this;
	}
	
	public ContentEditorPage openEditor() {
		By openBy = By.cssSelector("button.o_sel_page_edit[aria-checked=\"false\"]");
		OOGraphene.waitElement(openBy, browser);
		browser.findElement(openBy).click();
		OOGraphene.waitElement(By.cssSelector(".o_page_content .o_page_content_editor"), browser);
		return contentEditor();
	}
	
	public PageElementPage closeEditor() {
		By closeBy = By.cssSelector("button.o_sel_page_edit[aria-checked=\"true\"]");
		OOGraphene.waitElement(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitElementAbsence(By.cssSelector(".o_page_content .o_page_content_editor"), 5, browser);
		return this;
	}
	
	/**
	 * 
	 * @return The content editor
	 */
	public ContentEditorPage contentEditor() {
		return new ContentEditorPage(browser, false);
	}
	
	

}
