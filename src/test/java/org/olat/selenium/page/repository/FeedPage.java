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
import org.olat.selenium.page.graphene.OOGraphene;
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
	
	@Drone
	private WebDriver browser;
	
	public static FeedPage getFeedPage(WebDriver browser) {
		OOGraphene.waitElement(feedBy);
		WebElement main = browser.findElement(feedBy);
		return Graphene.createPageFragment(FeedPage.class, main);
	}
	
	/**
	 * Configure the podcast with an external feed.
	 * @param url
	 * @return
	 */
	public FeedPage newExternalPodcast(String url) {
		//click the button to create an external feed
		By lastButton = By.xpath("//div[contains(@class,'o_podcast_no_episodes')]//a[contains(@href,'feed.make.external')]");
		return newExternalFeed(lastButton, url);
	}
	
	public FeedPage newExternalBlog(String url) {
		//click the button to create an external feed
		By lastButton = By.xpath("//div[contains(@class,'o_blog_no_posts')]//a[contains(@href,'feed.make.external')]");
		return newExternalFeed(lastButton, url);
	}
	
	private FeedPage newExternalFeed(By configureExternalButton, String url) {
		browser.findElement(configureExternalButton).click();
		OOGraphene.waitBusy();
		//fill the URL input field
		By urlField = By.xpath("(//div[contains(@class,'modal-body ')]//form//input[@type='text'])[2]");
		browser.findElement(urlField).sendKeys(url);
		//write something in description
		OOGraphene.tinymce("...", browser);
		
		//save the settings
		By saveButton = By.xpath("//div[contains(@class,'modal-body')]//form//button[contains(@class,'btn-primary')]");
		browser.findElement(saveButton).click();
		OOGraphene.waitBusy();
		return this;
	}
}