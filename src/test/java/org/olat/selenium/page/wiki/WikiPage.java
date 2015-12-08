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
package org.olat.selenium.page.wiki;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.ArtefactWizardPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Drive the wiki page GUI, editor...
 * 
 * Initial date: 02.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiPage {
	
	public static final By wikiWrapperBy = By.cssSelector("div.o_wiki_wrapper");
	
	@Drone
	private WebDriver browser;
	
	public WikiPage() {
		//
	}
	
	public WikiPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static WikiPage getWiki(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		return Graphene.createPageFragment(WikiPage.class, main);
	}
	
	public static WikiPage getGroupWiki(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
		return new WikiPage(browser);
	}
	
	public WikiPage createPage(String name, String content) {
		//open the create popover
		By createBy = By.className("o_sel_wiki_create_page");
		WebElement createButton = browser.findElement(createBy);
		createButton.click();
		
		//fill the name of the new page
		By pageNameBy = By.cssSelector("div.o_callout_content form input[type='text']");
		OOGraphene.waitElement(pageNameBy, browser);
		WebElement pageNameEl = browser.findElement(pageNameBy);
		pageNameEl.sendKeys(name);
		//search for it
		By searchBy = By.cssSelector("div.popover-content form .o_sel_wiki_search button");
		WebElement searchButton = browser.findElement(searchBy);
		searchButton.click();
		OOGraphene.waitBusy(browser);
		
		//not exist -> click the link to create the page
		By notExistingBy = By.xpath("//div[contains(@class,'o_wikimod-article-box')]//a[@title='" + name + "']");
		WebElement notExistingLink = browser.findElement(notExistingBy);
		notExistingLink.click();
		
		//fill the form
		By textBy = By.cssSelector("div.o_wikimod_editform_wrapper form textarea");
		OOGraphene.waitElement(textBy, browser);
		WebElement textEl = browser.findElement(textBy);
		textEl.sendKeys(content);
		//save the page
		By saveAndCloseBy = By.className("o_sel_wiki_save_and_close");
		WebElement saveAndCloseButton = browser.findElement(saveAndCloseBy);
		saveAndCloseButton.click();
		OOGraphene.waitBusy(browser);
		
		//assert
		By pageTitleBy = By.className("o_wikimod_heading");
		WebElement pageTitleEl = browser.findElement(pageTitleBy);
		Assert.assertTrue(pageTitleEl.getText().contains(name));
		
		By contentBy = By.className("o_wikimod-article-box");
		WebElement contentEl = browser.findElement(contentBy);
		Assert.assertTrue(contentEl.getText().contains(content));
		return this;
	}
	
	public WikiPage assertOnContent(String text) {
		By messageBodyBy = By.className("o_wikimod-article-box");
		List<WebElement> messages = browser.findElements(messageBodyBy);
		boolean found = false;
		for(WebElement message:messages) {
			if(message.getText().contains(text)) {
				found = true;
			}
		}
		Assert.assertTrue(found);
		return this;
	}
	
	/**
	 * Add the current page to my artefacts
	 * 
	 */
	public ArtefactWizardPage addAsArtfeact() {
		By addAsArtefactBy = By.className("o_eportfolio_add");
		WebElement addAsArtefactButton = browser.findElement(addAsArtefactBy);
		addAsArtefactButton.click();
		OOGraphene.waitBusy(browser);
		return ArtefactWizardPage.getWizard(browser);
	}

}
