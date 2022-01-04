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

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 11.11.2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SinglePage {
	
	private WebDriver browser;
	
	public SinglePage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check if the specified content is in the iframe
	 * which display the single page.
	 * 
	 * @param content The content to find
	 * @return Itself
	 */
	public SinglePage assertInPage(String content) {
		By contentBy = By.xpath("//p[text()[contains(.,'" + content + "')]]");
		return assertInIFram(contentBy);
	}
	
	public SinglePage assertImageInPage(String name) {
		By contentBy = By.xpath("//p/img[contains(@src,'" + name + "')]");
		return assertInIFram(contentBy);
	}
	
	public SinglePage assertInIFram(By contentBy) {
		By iframeBy = By.xpath("//div[contains(@class,'o_iframedisplay')]//iframe");
		OOGraphene.waitElement(iframeBy, browser);
		List<WebElement> iframes = browser.findElements(iframeBy);
		browser = browser.switchTo().frame(iframes.get(0));
		
		OOGraphene.waitElement(contentBy, browser);

		browser = browser.switchTo().defaultContent();
		return this;
	}
	
	public SinglePage assertInFile(String filename) {
		By downloadBy = By.xpath("//div[contains(@class,'o_iframedisplay')]/a[contains(@class,'o_download')]/span[text()[contains(.,'" + filename + "')]]");
		OOGraphene.waitElement(downloadBy, browser);
		By iframeBy = By.xpath("//iframe[contains(@class,'o_iframe_rel')][contains(@src,'" + filename + "')]");
		OOGraphene.waitElement(iframeBy, browser);
		return this;
	}
	
	/**
	 * Edit a single page and replace the content.
	 * 
	 * @param content The content
	 * @return Itself
	 */
	public SinglePage edit(String content) {
		By editBy = By.cssSelector("div.o_singlepage a.o_edit");
		OOGraphene.waitElement(editBy, browser);
		browser.findElement(editBy).click();
		
		By editorBy = By.cssSelector(".o_htmleditor .o_editor");
		OOGraphene.waitElement(editorBy, browser);
		OOGraphene.waitTinymce(browser);
		
		OOGraphene.tinymce(content, browser);
		
		By saveBy = By.id("o_save");
		OOGraphene.moveTo(saveBy, browser);
		By saveAndCloseBy = By.cssSelector("#o_save #o_button_saveclose a");
		browser.findElement(saveAndCloseBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElementDisappears(editorBy, 5, browser);
		
		OOGraphene.scrollTop(browser);
		
		return this;
	}

}
