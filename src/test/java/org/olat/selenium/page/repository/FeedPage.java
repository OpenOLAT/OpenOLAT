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
import java.util.List;

import org.junit.Assert;
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
	
	/**
	 * Check that the post is visible
	 * @param title
	 * @return
	 */
	public FeedPage assertOnBlogPost(String title) {
		//assert on post
		boolean found = false;
		By postTitleBy = By.cssSelector(".o_post h3.o_title>a>span");
		List<WebElement> postTitleEls = browser.findElements(postTitleBy);
		for(WebElement postTitleEl:postTitleEls) {
			if(postTitleEl.getText().contains(title)) {
				found = true;
			}
		}
		Assert.assertTrue(found);
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
		browser.findElement(configureExternalButton).click();
		OOGraphene.waitBusy(browser);
		By popupBy = By.cssSelector("div.modal-dialog");
		OOGraphene.waitElement(popupBy, 5, browser);
		
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
		OOGraphene.waitBusy(browser, Duration.ofSeconds(20));
		return this;
	}
	
	public FeedPage newBlog() {
		//click the button to create a feed
		By feedButton = By.xpath("//div[contains(@class,'o_blog_no_posts')]//a[contains(@onclick,'feed.make.internal')]");
		browser.findElement(feedButton).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public FeedPage addBlogPost() {
		By newItemButton = By.className("o_sel_feed_item_new");
		browser.findElement(newItemButton).click();
		OOGraphene.waitBusy(browser);
		By postForm = By.className("o_sel_feed_form");
		OOGraphene.waitElement(postForm, 1, browser);
		return this;
	}
	
	public FeedPage fillPostForm(String title, String summary, String content) {
		//wait that the popup is available
		By postFormBy = By.cssSelector("fieldset.o_sel_feed_form");
		OOGraphene.waitElement(postFormBy, 2, browser);

		By titleBy = By.cssSelector("div.o_sel_feed_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		OOGraphene.tinymce(summary, "div.o_sel_feed_description", browser);
		
		OOGraphene.tinymce(content, "div.o_sel_feed_content", browser);
		
		return this;
	}
	
	public FeedPage publishPost() {
		By publishButton = By.cssSelector(".o_sel_feed_form button.btn-primary");
		OOGraphene.moveAndClick(publishButton, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Click the button to add a blog entry as media
	 * to your portfolio.
	 * 
	 * @return Itself
	 */
	public MediaPage addAsMedia() {
		By addAsMediaBy = By.cssSelector(".o_post .o_portfolio_collector");
		browser.findElement(addAsMediaBy).click();
		OOGraphene.waitBusy(browser);
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