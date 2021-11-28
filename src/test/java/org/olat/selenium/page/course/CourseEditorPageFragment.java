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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
	
	public static final By chooseCpButton = By.className("o_sel_cp_choose_repofile");
	public static final By chooseWikiButton = By.className("o_sel_wiki_choose_repofile");
	public static final By chooseTestButton = By.className("o_sel_test_choose_repofile");
	public static final By chooseFeedButton = By.className("o_sel_feed_choose_repofile");
	public static final By chooseScormButton = By.className("o_sel_scorm_choose_repofile");
	public static final By choosePortfolioButton = By.className("o_sel_map_choose_repofile");
	public static final By chooseSurveyButton = By.className("o_sel_survey_choose_repofile");
	
	public static final By changeNodeToolsMenu = By.cssSelector("ul.o_sel_course_editor_change_node");
	public static final By changeNodeToolsMenuCaret = By.cssSelector("a.o_sel_course_editor_change_node");
	
	public static final By tabNavTabsBy = By.cssSelector("ul.nav.nav-tabs");
	
	public static final List<By> chooseRepoEntriesButtonList = new ArrayList<>();
	static {
		chooseRepoEntriesButtonList.add(chooseCpButton);
		chooseRepoEntriesButtonList.add(chooseWikiButton);
		chooseRepoEntriesButtonList.add(chooseTestButton);
		chooseRepoEntriesButtonList.add(chooseFeedButton);
		chooseRepoEntriesButtonList.add(chooseScormButton);
		chooseRepoEntriesButtonList.add(choosePortfolioButton);
		chooseRepoEntriesButtonList.add(chooseSurveyButton);
	}
	
	private WebDriver browser;
	
	public CourseEditorPageFragment(WebDriver browser) {
		this.browser = browser;
	}
	
	public static CourseEditorPageFragment getEditor(WebDriver browser) {
		OOGraphene.waitElement(editorBy, browser);
		return new CourseEditorPageFragment(browser);
	}
	
	public CourseEditorPageFragment assertOnEditor() {
		OOGraphene.waitElement(editorBy, browser);
		List<WebElement> editorEls = browser.findElements(editorBy);
		Assert.assertFalse(editorEls.isEmpty());
		Assert.assertTrue(editorEls.get(0).isDisplayed());
		return this;
	}
	
	public CourseEditorPageFragment assertOnWarning() {
		By warningBy = By.cssSelector("div.modal-dialog div.alert.alert-warning");
		OOGraphene.waitElement(warningBy, browser);
		List<WebElement> warningEls = browser.findElements(warningBy);
		Assert.assertFalse(warningEls.isEmpty());
		OOGraphene.closeModalDialogWindow(browser);
		return this;
	}
	
	/**
	 * Select the root course element.
	 */
	public CourseEditorPageFragment selectRoot() {
		By rootNodeBy = By.cssSelector("span.o_tree_link.o_tree_l0>a");
		browser.findElement(rootNodeBy).click();
		OOGraphene.waitBusy(browser);
		By rootNodeActiveBy = By.cssSelector("span.o_tree_link.o_tree_l0.active");
		OOGraphene.waitElement(rootNodeActiveBy, browser);
		return this;
	}
	
	public EasyConditionConfigPage selectTabVisibility() {
		By passwordTabBy = By.cssSelector("fieldset.o_sel_course_visibility_condition_form");
		selectTab(passwordTabBy);
		return new EasyConditionConfigPage(browser);
	}
	
	/**
	 * Select the tab where the password setting are
	 * @return
	 */
	public CourseEditorPageFragment selectTabPassword() {
		By passwordTabBy = By.cssSelector("fieldset.o_sel_course_node_password_config");
		return selectTab(passwordTabBy);
	}
	
	public CourseEditorPageFragment setPassword(String password) {
		By switchBy = By.cssSelector(".o_sel_course_password_condition_switch input[type='checkbox']");
		browser.findElement(switchBy).click();
		OOGraphene.waitBusy(browser);
		
		By passwordBy = By.cssSelector(".o_sel_course_password_condition_value input[type='text']");
		browser.findElement(passwordBy).sendKeys(password);
		
		By saveBy = By.cssSelector("fieldset.o_sel_course_node_password_config button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the tab score in a structure node.
	 * 
	 */
	public CourseEditorPageFragment selectTabScore() {
		By scoreTabBy = By.cssSelector("fieldset.o_sel_structure_score");
		return selectTab(scoreTabBy);
	}
	
	public LearnPathCourseElementEditorPage selectTabLearnPath() {
		By learnPathTabBy = By.cssSelector("fieldset.o_sel_learnpath_element");
		selectTab(learnPathTabBy);
		return new LearnPathCourseElementEditorPage(browser);
	}
	
	private CourseEditorPageFragment selectTab(By tabBy) {
		//make sure the tab bar is loaded
		OOGraphene.selectTab("o_node_config", tabBy, browser);
		return this;
	}
	
	/**
	 * Enable passed and points by nodes
	 * @return
	 */
	public CourseEditorPageFragment enableRootScoreByNodes() {
		By enablePointBy = By.cssSelector("fieldset.o_sel_structure_score .o_sel_has_score input[type='checkbox']");
		browser.findElement(enablePointBy).click();
		OOGraphene.waitBusy(browser); //scform.scoreNodeIndents
		
		By enablePointNodesBy = By.cssSelector("fieldset.o_sel_structure_score input[type='checkbox'][name='scform.scoreNodeIndents']");
		List<WebElement> pointNodeEls = browser.findElements(enablePointNodesBy);
		for(WebElement pointNodeEl:pointNodeEls) {
			pointNodeEl.click();
			OOGraphene.waitBusy(browser);
		}
		
		By enablePassedBy = By.cssSelector("fieldset.o_sel_structure_score .o_sel_has_passed input[type='checkbox']");
		browser.findElement(enablePassedBy).click();
		OOGraphene.waitBusy(browser);
		
		By passedInheritBy = By.cssSelector("fieldset.o_sel_structure_score input[type='radio'][name='passedType'][value='inherit']");
		browser.findElement(passedInheritBy).click();
		OOGraphene.waitBusy(browser);
		
		By enablePassedNodesBy = By.cssSelector("fieldset.o_sel_structure_score input[type='checkbox'][name='scform.passedNodeIndents']");
		List<WebElement> enablePassedNodeEls = browser.findElements(enablePassedNodesBy);
		for(WebElement enablePassedNodeEl:enablePassedNodeEls) {
			enablePassedNodeEl.click();
			OOGraphene.waitBusy(browser);
		}
		
		//save
		By submitBy = By.cssSelector("fieldset.o_sel_structure_score button.btn.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Create a new course element
	 * @param nodeAlias The type of the course element
	 * @return
	 */
	public CourseEditorPageFragment createNode(String nodeAlias) {
		OOGraphene.waitElement(createNodeButton, browser);
		browser.findElement(createNodeButton).click();
		OOGraphene.waitModalDialog(browser);
		
		By nodeBy = By.cssSelector("div.modal-dialog div#o_course_editor_choose_nodetype a.o_sel_course_editor_node-" + nodeAlias);
		OOGraphene.moveAndClick(nodeBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return assertOnNodeTitle();
	}
	
	/**
	 * Wait until the short title and description field are
	 * there.
	 * 
	 * @return Itself
	 */
	public CourseEditorPageFragment assertOnNodeTitle() {
		By shortTitleBy = By.className("o_sel_node_editor_shorttitle");
		OOGraphene.waitElement(shortTitleBy, browser);
		OOGraphene.waitTinymce(browser);
		return this;
	}
	
	/**
	 * Set the course element title and short title, save
	 * and wait that the dialog disappears.
	 * 
	 * @param title The title of the course element
	 * @return Itself
	 */
	public CourseEditorPageFragment nodeTitle(String title) {
		assertOnNodeTitle();

		By shortTitleBy = By.cssSelector("div.o_sel_node_editor_shorttitle input[type='text']");
		WebElement shortTitleEl = browser.findElement(shortTitleBy);
		shortTitleEl.clear();
		shortTitleEl.sendKeys(title);
		
		By longtitle = By.cssSelector("div.o_sel_node_editor_title input");
		WebElement titleEl = browser.findElement(longtitle);
		titleEl.clear();
		titleEl.sendKeys(title);
		
		By saveButton = By.cssSelector("button.o_sel_node_editor_submit");
		OOGraphene.scrollTo(saveButton, browser);
		browser.findElement(saveButton).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);

		return this;
	}
	
	public String getRestUrl() {
		By openerBy = By.cssSelector("a.o_opener");
		browser.findElement(openerBy).click();

		By urlBy = By.cssSelector("div.o_copy_code input");
		OOGraphene.waitElement(urlBy, browser);
		
		String url = null;
		List<WebElement> urlEls = browser.findElements(urlBy);
		for(WebElement urlEl:urlEls) {
			String text = urlEl.getAttribute("value");
			if(text.contains("http")) {
				url = text.trim();
				break;
			}
		}
		Assert.assertNotNull(url);
		return url;
	}
	
	public CourseEditorPageFragment moveUnder(String targetNodeTitle) {
		if(!browser.findElement(changeNodeToolsMenu).isDisplayed()) {
			openChangeNodeToolsMenu();
		}
		By changeNodeLinkBy = By.cssSelector("a.o_sel_course_editor_move_node");
		browser.findElement(changeNodeLinkBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By targetNodeBy = By.xpath("//div[contains(@class,'o_tree_insert_tool')]//a[contains(@title,'" + targetNodeTitle + "')]");
		browser.findElement(targetNodeBy).click();
		OOGraphene.waitBusy(browser);
		
		By underBy = By.xpath("//div[contains(@class,'o_tree_insert_tool')]//a[i[contains(@class,'o_icon_node_under')]]");
		browser.findElement(underBy).click();
		OOGraphene.waitBusy(browser);
		
		By saveBy = By.cssSelector("div.modal-content div.o_button_group a.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public CourseEditorPageFragment deleteElement() {
		if(!browser.findElement(changeNodeToolsMenu).isDisplayed()) {
			openChangeNodeToolsMenu();
		}
		
		By deleteNodeLinkBy = By.cssSelector("a.o_sel_course_editor_delete_node");
		browser.findElement(deleteNodeLinkBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By confirmBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(confirmBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		By undeleteButtonBy = By.xpath("//a[contains(@onclick,'undeletenode.button')]");
		OOGraphene.waitElement(undeleteButtonBy, browser);
		
		return this;
	}
	
	public CourseEditorPageFragment selectNode(String nodeTitle) {
		By targetNodeBy = By.xpath("//div[contains(@class,'o_editor_menu')]//a[contains(@title,'" + nodeTitle + "')]");
		browser.findElement(targetNodeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Open the tools drop-down
	 * @return Itself
	 */
	public CourseEditorPageFragment openChangeNodeToolsMenu() {
		browser.findElement(changeNodeToolsMenuCaret).click();
		OOGraphene.waitElement(changeNodeToolsMenu, browser);
		return this;
	}
	
	/**
	 * Loop the tabs of the course element configuration to find
	 * the one with a button to select a repository entry.
	 * 
	 * @return Itself
	 */
	public CourseEditorPageFragment selectTabLearnContent() {
		OOGraphene.selectTab("o_node_config", b -> {
			for(By chooseRepoEntriesButton: chooseRepoEntriesButtonList) {
				List<WebElement> chooseRepoEntry = b.findElements(chooseRepoEntriesButton);
				if(!chooseRepoEntry.isEmpty()) {
					return true;
				}
			}
			return false;
		}, false, browser);
		return this;
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the CP
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseCP(String resourceTitle) {
		return chooseResource(chooseCpButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the wiki
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseWiki(String resourceTitle) {
		return chooseResource(chooseWikiButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the survey
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseSurvey(String resourceTitle) {
		return chooseResource(chooseSurveyButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the test
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseTest(String resourceTitle) {
		return chooseResource(chooseTestButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the SCORM
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseScorm(String resourceTitle) {
		return chooseResource(chooseScormButton, resourceTitle);
	}
	
	/**
	 * Choose a portfolio, v1.0 or v2.0
	 * 
	 * @param resourceTitle The name of the binder / portfolio
	 * @return
	 */
	public CourseEditorPageFragment choosePortfolio(String resourceTitle) {
		return chooseResource(choosePortfolioButton, resourceTitle);
	}
	
	/**
	 * Click the choose button, which open the resource chooser. Select
	 * the "My entries" segment, search the rows for the resource title,
	 * and select it.
	 * 
	 * 
	 * @param chooseButton The By of the choose button in the course node editor
	 * @param resourceTitle The resource title to find
	 * @return
	 */
	public CourseEditorPageFragment chooseResource(By chooseButton, String resourceTitle) {
		browser.findElement(chooseButton).click();
		OOGraphene.waitBusy(browser);
		//popup
		By referenceableEntriesBy = By.className("o_sel_search_referenceable_entries");
		OOGraphene.waitElement(referenceableEntriesBy, browser);
		WebElement popup = browser.findElement(referenceableEntriesBy);
		popup.findElement(By.cssSelector("a.o_sel_repo_popup_my_resources")).click();
		OOGraphene.waitBusy(browser);
		
		//find the row
		By rowBy = By.xpath("//div[contains(@class,'')]//div[contains(@class,'o_segments_content')]//table[contains(@class,'o_table')]//tr/td/a[text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElement(rowBy, browser);
		browser.findElement(rowBy).click();
		OOGraphene.waitBusy(browser);
		
		//double check that the resource is selected (search the preview link)
		By previewLink = By.xpath("//a/span[text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElement(previewLink, browser);
		return this;
	}
	
	/**
	 * Create a wiki from the chooser popup
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createWiki(String resourceTitle) {
		return createResource(chooseWikiButton, resourceTitle, null);
	}
	
	/**
	 * Create a QTI 1.2 test from the chooser popup
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createQTI12Test(String  resourceTitle) {
		return createResource(chooseTestButton, resourceTitle, "FileResource.TEST");
	}
	
	/**
	 * Create a podcast or a blog
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createFeed(String resourceTitle) {
		return createResource(chooseFeedButton, resourceTitle, null);
	}
	
	/**
	 * Create a portfolio template
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createPortfolio(String resourceTitle) {
		return createResource(choosePortfolioButton, resourceTitle, null);
	}
	
	private CourseEditorPageFragment createResource(By chooseButton, String resourceTitle, String resourceType) {
		browser.findElement(chooseButton).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		//popup
		By myResourcesBy = By.cssSelector(".modal-body .o_sel_search_referenceable_entries a.o_sel_repo_popup_my_resources");
		OOGraphene.waitElement(myResourcesBy, browser);
		browser.findElement(myResourcesBy).click();
		OOGraphene.waitBusy(browser);
		By mySelectedResourcesBy = By.cssSelector(".modal-body .o_sel_search_referenceable_entries a.btn-primary.o_sel_repo_popup_my_resources");
		OOGraphene.waitElement(mySelectedResourcesBy, browser);
		
		//click create
		By createResourceBy = By.cssSelector(".o_sel_search_referenceable_entries .o_sel_repo_popup_create_resource");
		List<WebElement> createEls = browser.findElements(createResourceBy);
		if(createEls.isEmpty()) {
			//open drop down
			By createResourcesBy = By.cssSelector("button.o_sel_repo_popup_create_resources");
			browser.findElement(createResourcesBy).click();
			//choose the right type
			By selectType = By.xpath("//ul[contains(@class,'o_sel_repo_popup_create_resources')]//a[contains(@onclick,'" + resourceType + "')]");
			OOGraphene.waitElement(selectType, browser);
			browser.findElement(selectType).click();
		} else {
			browser.findElement(createResourceBy).click();	
		}
		OOGraphene.waitBusy(browser);

		//fill the create form
		return fillCreateForm(resourceTitle);
	}
	
	private CourseEditorPageFragment fillCreateForm(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		OOGraphene.waitElement(inputBy, browser);
		browser.findElement(inputBy).sendKeys(displayName);
		By submitBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	/**
	 * Don't forget to set access.<br>
	 * Click the bread crumb and publish the course.
	 * 
	 * @return Itself
	 */
	public CoursePageFragment autoPublish() {
		//back
		By breadcrumpBackBy = By.cssSelector("#o_main_toolbar li.o_breadcrumb_back a");
		browser.findElement(breadcrumpBackBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//auto publish
		By autoPublishBy = By.cssSelector("div.modal  a.o_sel_course_quickpublish_auto");
		browser.findElement(autoPublishBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return new CoursePageFragment(browser);
	}

	/**
	 * Open the publish process
	 * @return
	 */
	public PublisherPageFragment publish() {
		OOGraphene.waitElement(publishButtonBy, browser);
		browser.findElement(publishButtonBy).click();
		OOGraphene.waitModalWizard(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_publish_nodes"), browser);
		return new PublisherPageFragment(browser);
	}
	
	/**
	 * Click the back button
	 * 
	 * @return Itself
	 */
	public CoursePageFragment clickToolbarBack() {
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		
		By mainId = By.id("o_main");
		OOGraphene.waitElement(mainId, 5, browser);
		return new CoursePageFragment(browser);
	}
}
