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

import java.time.Duration;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.MediaPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * To drive the page of feed, blog and podcast
 * 
 * Initial date: 24.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedPage {
	
	public static final By feedBy = By.className("o_feed");

	public static final By newExternalFeedBy = By.className("o_feed");
	
	private final WebDriver browser;
	
	public FeedPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static FeedPage getFeedPage(WebDriver browser) {
		OOGraphene.waitElement(feedBy, browser);
		return new FeedPage(browser);
	}
	
	public FeedPage assertOnBlogPostInClassicTable(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_feed')]//div[contains(@class,'o_table_flexi')]//table//td//div/a/span[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public FeedPage assertOnBlogPostInCardTable(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_feed')]//div[contains(@class,'o_table_flexi')]//table//td//div/a/span[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	/**
	 * Check that the post is visible
	 * @param title
	 * @return
	 */
	public FeedPage assertOnBlogPost(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_post')]//h3[contains(@class,'o_title')][text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public FeedPage assertOnBlogPostTitle() {
		By episodeTitleby = By.cssSelector("div.o_post h3.o_title");
		OOGraphene.waitElementSlowly(episodeTitleby, 20, browser);
		return this;
	}
	

	public FeedPage assertOnBlogEpisodeInClassicTable() {
		return assertOnPodcastEpisodeInClassicTable();
	}
	
	public FeedPage assertOnPodcastEpisodeInClassicTable() {
		By episodeTitleby = By.cssSelector("div.o_feed table td>div>a");
		OOGraphene.waitElementSlowly(episodeTitleby, 20, browser);
		return this;
	}
	
	/**
	 * Configure the podcast with an external feed.
	 * @param url
	 * @return
	 */
	public FeedPage newExternalPodcast(String title, String url) {
		//click the button to create an external feed
		By lastButton = By.xpath("//div[contains(@class,'o_podcast_no_episodes')]//a[contains(@onclick,'feed.make.external')]");
		return newExternalFeed(lastButton, title, url);
	}
	
	/**
	 * Create a new external blog.
	 * @param url
	 * @return
	 */
	public FeedPage newExternalBlog(String title, String url) {
		//click the button to create an external feed
		By lastButton = By.xpath("//div[contains(@class,'o_blog_no_posts')]//a[contains(@onclick,'feed.make.external')]");
		return newExternalFeed(lastButton, title, url);
	}
	
	private FeedPage newExternalFeed(By configureExternalButton, String title, String url) {
		OOGraphene.waitElement(configureExternalButton, browser);
		browser.findElement(configureExternalButton).click();
		OOGraphene.waitModalDialog(browser);
		OOGraphene.waitTinymce(browser);

		if(title != null) {
			By titleBy = By.cssSelector("div.o_sel_feed_title input[type='text']");
			WebElement titleEl = browser.findElement(titleBy);
			titleEl.clear();
			titleEl.sendKeys(title);
		}
		
		//fill the URL input field
		By urlField = By.cssSelector("div.modal-dialog div.o_sel_feed_url input[type='text']");
		WebElement urlEl = browser.findElement(urlField);
		urlEl.sendKeys(url);
		
		//write something in description
		OOGraphene.tinymce("...", browser);
		
		//save the settings
		By saveButton = By.xpath("//div[contains(@class,'modal-body')]//form//button[contains(@class,'btn-primary')]");
		browser.findElement(saveButton).click();
		OOGraphene.waitModalDialogDisappears(browser, Duration.ofSeconds(30));
		return this;
	}
	
	public FeedPage backToList() {
		By backBy = By.xpath("//div[contains(@class,'o_post')]//a[i[contains(@class,'o_icon_back')]]");
		OOGraphene.waitElement(backBy, browser);
		browser.findElement(backBy).click();
		return this;
	}
	
	public FeedPage allTableFilter() {
		By allBy = By.xpath("//div[contains(@class,'o_feed')]//div[@class='o_table_tabs']//a[contains(@href,'tab')][contains(@href,'all')]");
		OOGraphene.waitElement(allBy, browser);
		browser.findElement(allBy).click();
		return this;
	}
	
	/**
	 * Create the first post of a blog.
	 * 
	 * @return Itself
	 */
	public FeedPage newBlogPost() {
		//click the button to create a feed
		By newPostBy = By.cssSelector("div.o_feed .o_empty_state .o_empty_action a.btn-primary");
		OOGraphene.waitElement(newPostBy, browser);
		browser.findElement(newPostBy).click();
		return this;
	}
	
	/**
	 * Create additional post in a blog.
	 * 
	 * @return Itself
	 */
	public FeedPage addBlogPost() {
		By addPostBy = By.cssSelector("a.o_sel_feed_add_item");
		OOGraphene.waitElement(addPostBy, browser);
		browser.findElement(addPostBy).click();
		return this;
	}
	
	public FeedPage fillPostForm(String title, String summary, String content) {
		//wait that the popup is available
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_feed_form div.o_sel_feed_title input:focus[type='text']");

		By titleBy = By.cssSelector("div.o_sel_feed_title input:focus[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By contentBy = By.cssSelector("div.o_sel_feed_content div.o_richtext_mce textarea");
		browser.findElement(contentBy).sendKeys(content);
		
		By summaryBy = By.cssSelector("div.o_sel_feed_summary div.o_richtext_mce textarea");
		browser.findElement(summaryBy).sendKeys(summary);
		
		return this;
	}
	
	public FeedPage publishPost() {
		By publishButton = By.cssSelector(".o_sel_feed_form button.btn-primary");
		OOGraphene.click(publishButton, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Click the button to add a blog entry as media
	 * to your portfolio.
	 * 
	 * @return Itself
	 */
	public MediaPage addAsMediaInTable() {
		By toolsBy = By.xpath("//div[contains(@class,'o_feed')]//div[contains(@class,'o_table')]//tr/td/div/a[i[contains(@class,'o_icon_actions')]]");
		OOGraphene.waitElement(toolsBy, browser);
		browser.findElement(toolsBy).click();
		OOGraphene.waitCallout(browser);
		
		By addAsMediaBy = By.xpath("//dialog[contains(@class,'popover')]//ul[contains(@class,'o_dropdown')]//a[i[contains(@class,'o_icon_eportfolio_add')]]");
		browser.findElement(addAsMediaBy).click();
		OOGraphene.waitModalDialog(browser);
		return new MediaPage(browser);
	}
	
	/**
	 * Click the first month in the pager
	 * @return
	 */
	public FeedPage clickFirstMonthOfPager() {
		By monthBy = By.xpath("//div[contains(@class,'o_year_navigation')]//li[contains(@class,'o_month')][1]/a");
		OOGraphene.waitElement(monthBy, browser);
		browser.findElement(monthBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}