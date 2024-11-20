/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 8 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoPage {

	private final By toolsMenu = By.cssSelector("ul.o_sel_repository_tools");
	
	private WebDriver browser;
	
	public VideoPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public VideoPage assertOnVideo() {
		By videoBy = By.xpath("//div[contains(@class,'o_video_run')]//mediaelementwrapper/video");
		OOGraphene.waitElement(videoBy, 10, browser);
		By videoControlsBy = By.cssSelector(".o_video_run .mejs__controls .mejs__button.mejs__playpause-button.mejs__play");
		OOGraphene.waitElement(videoControlsBy, browser);
		return this;
	}
	
	public VideoPage assertOnYoutubeVideo(String id) {
		By youtubeIframeBy = By.xpath("//div[contains(@class,'o_video_run')]//mediaelementwrapper//iframe[contains(@id,'_youtube_iframe')][contains(@src,'" + id + "')]");
		OOGraphene.waitElementSlowly(youtubeIframeBy, 15, browser);
		return this;
	}
	
	public VideoPage play() {
		By playBy = By.cssSelector(".o_video_run .mejs__controls .mejs__button.mejs__playpause-button.mejs__play");
		OOGraphene.waitElement(playBy, browser);
		browser.findElement(playBy).click();
		return this;
	}
	
	public VideoPage assetOnSegment() {
		By playBy = By.cssSelector("#o_videotask_segments #vt-marker-1");
		OOGraphene.waitElement(playBy, browser);
		return this;
	}
	
	/**
	 * Wait patiently the segment.
	 * 
	 * @param wait Time to wait in seconds
	 * @return Itself
	 */
	public VideoPage assetOnSegmentTooltip(int wait) {
		By playBy = By.cssSelector("#o_videotask_segments .tooltip.o_videotask_tooltip");
		OOGraphene.waitElementSlowly(playBy, wait, browser);
		return this;
	}
	
	/**
	 * Opens the video editor.
	 * 
	 * @return The video editor
	 */
	public VideoEditorPage edit() {
		openToolsMenu();

		By editVideoBy = By.cssSelector("ul.o_sel_repository_tools a.o_sel_video_edit");
		browser.findElement(editVideoBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_video_editor #o_video_editor_video"), browser);
		return new VideoEditorPage(browser);
	}
	
	private VideoPage openToolsMenu() {
		By toolsMenuCaret = By.cssSelector("a.o_sel_repository_tools");
		browser.findElement(toolsMenuCaret).click();
		OOGraphene.waitElement(toolsMenu, browser);
		return this;
	}
}
