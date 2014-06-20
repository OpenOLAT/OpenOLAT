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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jcodec.common.Assert;
import org.olat.selenium.page.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseEditorPageFragment {
	
	public static final By editorBy = By.className("o_course_editor");
	public static final By createNodeButton = By.className("o_sel_course_editor_create_node");
	public static final By createNodeModalBy = By.id("o_course_editor_choose_nodetype");
	
	public static final By publishButtonBy = By.className("o_sel_course_editor_publish");

	public static final By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
	
	@Drone
	private WebDriver browser;
	
	@FindBy(id="o_main_left_content")
	private WebElement treeContainer;
	
	@FindBy(className="o_course_editor")
	private WebElement editor;
	
	public CourseEditorPageFragment assertOnEditor() {
		Assert.assertTrue(editor.isDisplayed());
		return this;
	}
	
	
	public CourseEditorPageFragment openCreateNode(String nodeAlias) {
		WebElement createButton = browser.findElement(createNodeButton);
		Assert.assertTrue(createButton.isDisplayed());
		createButton.click();
		OOGraphene.waitElement(createNodeModalBy);
		
		//modal
		WebElement createNodeModal = browser.findElement(createNodeModalBy);
		
		//create the node
		By node = By.className("o_sel_course_editor_node-" + nodeAlias);
		WebElement createNodeLink = createNodeModal.findElement(node);
		Assert.assertTrue(createNodeLink.isDisplayed());
		createNodeLink.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	public PublisherPageFragment publish() {
		WebElement publishButton = browser.findElement(publishButtonBy);
		Assert.assertTrue(publishButton.isDisplayed());
		publishButton.click();
		
		By modalBy = By.className("modal");
		OOGraphene.waitElement(modalBy);
		WebElement modal = browser.findElement(By.className("modal"));
		return Graphene.createPageFragment(PublisherPageFragment.class, modal);
	}
	
	public CoursePageFragment clickToolbarBack() {
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy();
		
		WebElement main = browser.findElement(By.id("o_main"));
		return Graphene.createPageFragment(CoursePageFragment.class, main);
	}
	

}
