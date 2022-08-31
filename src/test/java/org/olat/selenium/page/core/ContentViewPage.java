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

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 30 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentViewPage {

	public final By editFragmentBy = By.cssSelector("div.o_page_fragment_edit");
	
	protected final WebDriver browser;

	public ContentViewPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check that the title is on the page with the right size.
	 * 
	 * @param title The title
	 * @param size Its size (between 1 and 6)
	 * @return Itself
	 */
	public ContentViewPage assertOnTitle(String title, int size) {
		By titleBy = By.xpath("//h" + size + "[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public ContentViewPage assertOnImage(File image) {
		String filename = image.getName();
		int typePos = filename.lastIndexOf('.');
		if (typePos > 0) {
			String ending = filename.substring(typePos + 1).toLowerCase();
			filename = filename.substring(0, typePos + 1).concat(ending);
		}
		By titleBy = By.xpath("//figure[@class='o_image']/img[contains(@src,'" + filename + "')]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public ContentViewPage assertOnDocument(File file) {
		String filename = file.getName();
		By downloadLinkBy = By.xpath("//div[contains(@class,'o_download')]//a[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(downloadLinkBy, 5, browser);
		return this;
	}
	
	public ContentViewPage assertOnCitation(String citation) {
		By citationBy = By.xpath("//blockquote[contains(@class,'o_quote')]//p[contains(text(),'" + citation + "')]");
		OOGraphene.waitElement(citationBy, 5, browser);
		return this;
	}

}
