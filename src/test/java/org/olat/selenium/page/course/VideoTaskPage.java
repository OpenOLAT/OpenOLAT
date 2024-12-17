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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.repository.VideoPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 16 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskPage extends VideoPage {

	public VideoTaskPage(WebDriver browser) {
		super(browser);
	}
	
	public VideoTaskPage assertOnAssessedIdentities() {
		By assessmentTableBy = By.cssSelector("a.btn.btn-primary.o_sel_course_video_coaching");
		OOGraphene.waitElement(assessmentTableBy, browser);
		return this;
	}
	
	public VideoTaskPage assertOnStartTask() {
		By startBy = By.cssSelector("div.o_videotask_box a.btn-primary.o_sel_start_videotask");
		OOGraphene.waitElement(startBy, browser);
		return this;
	}
	
	public VideoTaskPage startTask() {
		By startBy = By.cssSelector("div.o_videotask_box a.btn-primary.o_sel_start_videotask");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		return this;
	}
	
	@Override
	public VideoTaskPage play() {
		return (VideoTaskPage)super.play();
	}
	
	/**
	 * Select the first segment.
	 * 
	 * @return Itself
	 */
	public VideoTaskPage selectFirstSegment() {
		By segmentBy = By.xpath("//div[@id='o_videotask_categories']/button[contains(@class,'o_video_marker')][1]");
		OOGraphene.waitElement(segmentBy, browser);
		browser.findElement(segmentBy).click();
		return this;
	}
	
	public VideoTaskPage assertOnSegmentCorrect() {
		By correctSegmentBy = By.xpath("//div[@id='o_videotask_categories']/button[contains(@class,'o_video_marker')][i[contains(@class,'o_icon_correct_answer')]]");
		OOGraphene.waitElement(correctSegmentBy, browser);
		return this;
	}

	@Override
	public VideoTaskPage reduceVideoWindow() {
		return (VideoTaskPage)super.reduceVideoWindow();
	}
	
	/**
	 * Confirm the submission in modal dialog.
	 * 
	 * @return Itself
	 */
	public VideoTaskPage submitTask() {
		By submitBy = By.xpath("//div[@class='o_videotask_run']/div[contains(@class,'o_button_group')]/a[contains(@class,'btn')][contains(@onclick,'close.video')]");
		OOGraphene.waitElement(submitBy, browser);
		browser.findElement(submitBy).click();
		
		// Confirm
		OOGraphene.waitModalDialog(browser);
		
		By confirmOkBy = By.cssSelector("dialog.dialog.show div.o_button_group>button.btn.btn-primary");
		browser.findElement(confirmOkBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
}
