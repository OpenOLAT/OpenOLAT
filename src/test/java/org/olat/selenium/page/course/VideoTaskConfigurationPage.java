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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 16 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskConfigurationPage {
	
	private final WebDriver browser;
	
	public VideoTaskConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public VideoTaskConfigurationPage selectVideoTaskConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_video_configuration>a");
		OOGraphene.waitElement(tabBy, browser).click();
		By configBy = By.cssSelector("fieldset.o_video_configuration");
		OOGraphene.waitElement(configBy, browser);
		return this;
	}

	public VideoTaskConfigurationPage selectVideoResource(String resourceTitle) {
		By chooseVideoButton = By.className("o_sel_re_reference_select");
		CourseEditorPageFragment fragment = new CourseEditorPageFragment(browser);
		fragment.chooseResourceModern(chooseVideoButton, resourceTitle);
		return this;
	}
	
	public VideoTaskConfigurationPage selectSegmentsOption() {
		By segmentsBy = By.cssSelector(".o_sel_video_elements input[name='videoElements'][value='segments']");
		WebElement segmentsEl = browser.findElement(segmentsBy);
		OOGraphene.check(segmentsEl, Boolean.TRUE);
		return this;
	}
	
	public VideoTaskConfigurationPage assertSegmentsOption() {
		By segmentsEnabledBy = By.xpath("//fieldset[contains(@class,'o_sel_video_elements')]//input[@name='videoElements'][@value='segments'][@checked='checked']");
		OOGraphene.waitElement(segmentsEnabledBy, browser);
		return this;
	}
	
	public VideoTaskConfigurationPage save() {
		By saveBy = By.cssSelector(".o_sel_video_configuration_form button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.scrollTop(browser);
		return this;
	}

}
