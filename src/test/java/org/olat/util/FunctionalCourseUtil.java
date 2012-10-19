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
package org.olat.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import com.thoughtworks.selenium.Selenium;

/**
 * Description: <br>
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalCourseUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalCourseUtil.class);
	
	private final static Pattern categoryPattern = Pattern.compile("/([^/]+)");
	
	public final static String COURSE_RUN_CSS = "o_course_run";
	public final static String COURSE_OPEN_EDITOR_CSS = "o_sel_course_open_editor";
	
	public final static String COURSE_TAB_ACTIVE_CSS = "b_nav_active";
	public final static String COURSE_TAB_CLOSE_CSS = "b_nav_tab_close";
	
	public final static String COURSE_EDITOR_NODE_LINKS_ID = "o_course_node_links";
	public final static String COURSE_EDITOR_PUBLISH_CSS = "b_toolbox_publish";
	public final static String COURSE_EDITOR_PUBLISH_WIZARD_SELECT_ALL_CSS = "o_sel_course_publish_selectall_cbb";
	public final static String COURSE_EDITOR_PUBLISH_WIZARD_ACCESS_ID = "o_fioaccessBox_SELBOX";
	public final static String COURSE_EDITOR_PUBLISH_WIZARD_CATALOG_ID = "o_fiocatalogBox_SELBOX";
	public final static String ADD_TO_CATALOG_YES_VALUE = "yes";
	public final static String ADD_TO_CATALOG_NO_VALUE = "no";
	public final static String CATALOG_SUBCATEGORY_ICON_CSS = "o_catalog_cat_icon";
	public final static String ADD_TO_CATALOG_CSS = "o_sel_publish_add_to_catalog";
	public final static String CATALOG_CSS = "o_sel_catalog_chooser_tree";
	
	public final static String COURSE_EDITOR_INSERT_CONTENT_CSS = "b_toolbox_content";
	public final static String CREATE_COURSE_NODE_TARGET_POSITION_ITEM_CSS = "b_selectiontree_item";
	
	public final static String COURSE_EDITOR_OVERVIEW_RADIO_GROUP_CSS = "";
	public final static String COURSE_EDITOR_CHOOSE_OVERVIEW_FILE_CSS = "o_sel_filechooser_create";
	public final static String COURSE_EDITOR_UPLOAD_OVERVIEW_FILE_CSS = "o_sel_upload_buttons";
	
	public final static String EPORTFOLIO_ADD_CSS = "b_eportfolio_add";
	
	public final static String STRUCTURE_ICON_CSS = "o_st_icon";
	public final static String FORUM_ICON_CSS = "o_fo_icon";
	public final static String BLOG_ICON_CSS = "o_blog_icon";
	
	public final static String FORUM_TOOLBAR_CSS = "o_forum_toolbar";
	public final static String FORUM_THREAD_NEW_CSS = "o_sel_forum_thread_new";
	public final static String FORUM_ARCHIVE_CSS = "o_sel_forum_archive";
	public final static String FORUM_FILTER_CSS = "o_sel_forum_filter";
	
	public final static String WIKI_CREATE_ARTICLE_CSS = "o_sel_wiki_search";
	public final static String WIKI_ARTICLE_BOX_CSS = "o_wikimod-article-box";
	public final static String WIKI_EDIT_FORM_WRAPPER_CSS = "o_wikimod_editform_wrapper";
	
	public final static String BLOG_CREATE_ENTRY_CSS = "o_sel_feed_item_new";
	public final static String BLOG_EDIT_ENTRY_CSS = "o_sel_feed_item_edit";
	public final static String BLOG_DELETE_ENTRY_CSS = "o_sel_feed_item_delete";
	public final static String BLOG_READ_ENTRY_CSS = "o_post-readmorelinks";
	public final static String BLOG_FORM_CSS = "o_sel_blog_form";
	public final static String BLOG_POST_CSS = "o_blog_post";
	public final static String BLOG_YEAR_CSS = "b_year";
	public final static String BLOG_MONTH_CSS = "b_month";
	
	public final static String TEST_CHOOSE_REPOSITORY_FILE_CSS = "o_sel_test_choose_repofile";
	public final static String CP_CHOOSE_REPOSITORY_FILE_CSS = "o_sel_cp_choose_repofile";
	public final static String WIKI_CHOOSE_REPOSITORY_FILE_CSS = "o_sel_wiki_choose_repofile";
	public final static String FEED_CHOOSE_REPOSITORY_FILE_CSS = "o_sel_feed_choose_repofile";
	public final static String MAP_CHOOSE_REPOSITORY_FILE_CSS = "o_sel_map_choose_repofile";
	
	public final static String REPOSITORY_POPUP_CSS = "o_sel_search_referenceable_entries";
	
	public final static String REPOSITORY_POPUP_CREATE_RESOURCE_CSS = "o_sel_repo_popup_create_resource";
	public final static String REPOSITORY_POPUP_IMPORT_RESOURCE_CSS = "o_sel_repo_popup_import_resource";
	
	public final static String REPOSITORY_POPUP_ALL_RESOURCES_CSS = "o_sel_repo_popup_all_resources";
	public final static String REPOSITORY_POPUP_MY_RESOURCES_CSS = "o_sel_repo_popup_my_resources";
	public final static String REPOSITORY_POPUP_SEARCH_RESOURCES_CSS = "o_sel_repo_popup_search_resources";
	
	public final static String MAP_EDIT_CSS = "o_sel_edit_map";
	
	public final static String BLOG_NO_POSTS_CSS = "o_blog_no_posts";
	
	public final static String PODCAST_NO_EPISODES_CSS = "o_podcast_no_episodes";
	
	public enum CourseNodeTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		CONTENT;
	};
	
	public enum VisibilityOption {
		BLOCKED_FOR_LEARNERS,
		DEPENDING_ON_DATE,
		DEPENDING_ON_GROUP,
		DEPENDING_ON_ASSESSMENT,
		APPLY_TO_OWNERS_AND_TUTORS(DEPENDING_ON_ASSESSMENT);
		
		private VisibilityOption requires;
		
		VisibilityOption(){
			this(null);
		}
		
		VisibilityOption(VisibilityOption requires){
			setRequires(requires);
		}

		public VisibilityOption getRequires() {
			return requires;
		}

		public void setRequires(VisibilityOption requires) {
			this.requires = requires;
		}
	};
	
	public enum AccessOption {
		BLOCKED_FOR_LEARNERS,
		DEPENDING_ON_DATE,
		DEPENDING_ON_GROUP,
		DEPENDING_ON_ASSESSMENT,
		APPLY_TO_OWNERS_AND_TUTORS(DEPENDING_ON_ASSESSMENT);
		
		private AccessOption requires;
		
		AccessOption(){
			this(null);
		}
		
		AccessOption(AccessOption requires){
			setRequires(requires);
		}

		public AccessOption getRequires() {
			return requires;
		}

		public void setRequires(AccessOption requires) {
			this.requires = requires;
		}
	}
	
	public enum CourseOverview {
		AUTOMATIC("system"),
		AUTOMATIC_AND_PREVIEW("peekview"),
		SINGLEPAGE("file"),
		NONE("delegate");
		
		private String value;
		
		CourseOverview(String value){
			setValue(value);
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public enum CourseNodeAlias {
		CP("o_cp_icon"),
		BLOG("o_blog_icon"),
		PODCAST("o_podcast_icon"),
		WIKI("o_wiki_icon"),
		PORTFOLIO_TASK("o_ep_icon"),
		IQ_TEST("o_iqtest_icon"),
		IQ_SELFTEST("o_iqself_icon"),
		IQ_QUESTIONAIRE("o_iqsurv_icon");
		
		private String iconCss;
		
		CourseNodeAlias(String iconCss){
			setIconCss(iconCss);
		}

		public String getIconCss() {
			return iconCss;
		}

		public void setIconCss(String iconCss) {
			this.iconCss = iconCss;
		}
	}
	
	public enum AccessSettings {
		OWNERS("1"),
		OWNERS_AND_AUTHORS("2"),
		ALL_REGISTERED_USERS("3"),
		REGISTERED_USERS_AND_GUESTS("4"),
		MEMBERS_ONLY("membersonly");
		
		private String accessValue;
		
		AccessSettings(String accessValue){
			setAccessValue(accessValue);
		}

		public String getAccessValue() {
			return accessValue;
		}

		public void setAccessValue(String accessValue) {
			this.accessValue = accessValue;
		}
	}
	
	public enum CourseEditorCourseTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		OVERVIEW,
		SCORE,
	}
	
	public enum CourseEditorIQTestTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		TEST_CONFIGURATION;
	}
	
	public enum CourseEditorCpTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		LEARNING_CONTENT;
	}
	
	public enum CourseEditorWikiTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		LEARNING_CONTENT;
	}
	
	public enum CourseEditorBlogTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		LEARNING_CONTENT;
	}
	
	public enum CourseEditorPodcastTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		LEARNING_CONTENT;
	}
	
	public enum CourseEditorPortfolioTaskTab {
		TITLE_AND_DESCRIPTION,
		VISIBILITY,
		ACCESS,
		LEARNING_CONTENT,
		ASSESSMENT;
	}
	
	public enum BlogEdit {
		TITLE,
		DESCRIPTION,
		CONTENT;
	}
	
	private String courseRunCss;
	private String courseOpenEditorCss;
	
	private String courseTabActiveCss;
	private String courseTabCloseCss;
	
	private String courseEditorNodeLinksId;
	private String courseEditorPublishCss;
	private String courseEditorPublishWizardSelectAllCss;
	private String courseEditorPublishWizardAccessId;
	private String courseEditorPublishWizardCatalogId;
	private String catalogSubcategoryIconCss;
	private String addToCatalogCss;
	private String catalogCss;
	
	private String courseEditorOverviewRadioGroupCss;
	private String courseEditorInsertContentCss;
	private String createCourseNodeTargetPositionItemCss;
	
	private String courseEditorChooseOverviewFileCss;
	private String courseEditorUploadOverviewFileCss;
	
	private String eportfolioAddCss;
	
	private String structureIconCss;
	private String forumIconCss;
	private String blogIconCss;
	
	private String forumToolbarCss;
	private String forumThreadNewCss;
	private String forumArchiveCss;
	private String forumFilterCss;
	
	private String wikiCreateArticleCss;
	private String wikiArticleBoxCss;
	private String wikiEditFormWrapperCss;
	
	private String blogCreateEntryCss;
	private String blogEditEntryCss;
	private String blogDeleteEntryCss;
	private String blogReadEntryCss;
	private String blogFormCss;
	private String blogPostCss;
	private String blogYearCss;
	private String blogMonthCss;
	
	private String testChooseRepositoryFileCss;
	private String cpChooseRepositoryFileCss;
	private String wikiChooseRepositoryFileCss;
	private String feedChooseRepositoryFileCss;
	private String mapChooseRepositoryFileCss;

	private String repositoryPopupCss;
	
	private String repositoryPopupCreateResourceCss;
	private String repositoryPopupImportResourceCss;
	
	private String repositoryPopupAllResourcesCss;
	private String repositoryPopupMyResourcesCss;
	private String repositoryPopupSearchResourcesCss;
	
	private String mapEditCss;
	
	private String blogNoPostsCss;
	private String podcastNoEpisodesCss;
	
	private FunctionalUtil functionalUtil;
	private FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	
	public FunctionalCourseUtil(FunctionalUtil functionalUtil, FunctionalRepositorySiteUtil functionalRepositorySiteUtil){
		this.functionalUtil = functionalUtil;
		this.functionalRepositorySiteUtil = functionalRepositorySiteUtil;
		
		setCourseRunCss(COURSE_RUN_CSS);
		setCourseOpenEditorCss(COURSE_OPEN_EDITOR_CSS);
		
		setCourseTabActiveCss(COURSE_TAB_ACTIVE_CSS);
		setCourseTabCloseCss(COURSE_TAB_CLOSE_CSS);
		
		setCourseEditorNodeLinksId(COURSE_EDITOR_NODE_LINKS_ID);
		setCourseEditorPublishCss(COURSE_EDITOR_PUBLISH_CSS);
		setCourseEditorPublishWizardSelectAllCss(COURSE_EDITOR_PUBLISH_WIZARD_SELECT_ALL_CSS);
		setCourseEditorPublishWizardAccessId(COURSE_EDITOR_PUBLISH_WIZARD_ACCESS_ID);
		setCourseEditorPublishWizardCatalogId(COURSE_EDITOR_PUBLISH_WIZARD_CATALOG_ID);
		setCatalogSubcategoryIconCss(CATALOG_SUBCATEGORY_ICON_CSS);
		setAddToCatalogCss(ADD_TO_CATALOG_CSS);
		setCatalogCss(CATALOG_CSS);
		
		setCourseEditorOverviewRadioGroupCss(COURSE_EDITOR_OVERVIEW_RADIO_GROUP_CSS);
		setCourseEditorInsertContentCss(COURSE_EDITOR_INSERT_CONTENT_CSS);
		setCreateCourseNodeTargetPositionItemCss(CREATE_COURSE_NODE_TARGET_POSITION_ITEM_CSS);
		
		setCourseEditorChooseOverviewFileCss(COURSE_EDITOR_CHOOSE_OVERVIEW_FILE_CSS);
		setCourseEditorUploadOverviewFileCss(COURSE_EDITOR_UPLOAD_OVERVIEW_FILE_CSS);
		
		setEportfolioAddCss(EPORTFOLIO_ADD_CSS);
		
		setStructureIconCss(STRUCTURE_ICON_CSS);
		setForumIconCss(FORUM_ICON_CSS);
		setBlogIconCss(BLOG_ICON_CSS);
		
		setForumToolbarCss(FORUM_TOOLBAR_CSS);
		setForumThreadNewCss(FORUM_THREAD_NEW_CSS);
		setForumArchiveCss(FORUM_ARCHIVE_CSS);
		setForumFilterCss(FORUM_FILTER_CSS);
		
		setWikiCreateArticleCss(WIKI_CREATE_ARTICLE_CSS);
		setWikiArticleBoxCss(WIKI_ARTICLE_BOX_CSS);
		setWikiEditFormWrapperCss(WIKI_EDIT_FORM_WRAPPER_CSS);
		
		setBlogCreateEntryCss(BLOG_CREATE_ENTRY_CSS);
		setBlogEditEntryCss(BLOG_EDIT_ENTRY_CSS);
		setBlogDeleteEntryCss(BLOG_DELETE_ENTRY_CSS);
		setBlogReadEntryCss(BLOG_READ_ENTRY_CSS);
		setBlogFormCss(BLOG_FORM_CSS);
		setBlogPostCss(BLOG_POST_CSS);
		setBlogYearCss(BLOG_YEAR_CSS);
		setBlogMonthCss(BLOG_MONTH_CSS);
		
		setTestChooseRepositoryFileCss(TEST_CHOOSE_REPOSITORY_FILE_CSS);
		setCpChooseRepositoryFileCss(CP_CHOOSE_REPOSITORY_FILE_CSS);
		setWikiChooseRepositoryFileCss(WIKI_CHOOSE_REPOSITORY_FILE_CSS);
		setFeedChooseRepositoryFileCss(FEED_CHOOSE_REPOSITORY_FILE_CSS);
		setMapChooseRepositoryFileCss(MAP_CHOOSE_REPOSITORY_FILE_CSS);
		
		setRepositoryPopupCss(REPOSITORY_POPUP_CSS);
		
		setRepositoryPopupCreateResourceCss(REPOSITORY_POPUP_CREATE_RESOURCE_CSS);
		setRepositoryPopupImportResourceCss(REPOSITORY_POPUP_IMPORT_RESOURCE_CSS);
		
		setRepositoryPopupAllResourcesCss(REPOSITORY_POPUP_ALL_RESOURCES_CSS);
		setRepositoryPopupMyResourcesCss(REPOSITORY_POPUP_MY_RESOURCES_CSS);
		setRepositoryPopupSearchResourcesCss(REPOSITORY_POPUP_SEARCH_RESOURCES_CSS);
		
		setMapEditCss(MAP_EDIT_CSS);
		
		setBlogNoPostsCss(BLOG_NO_POSTS_CSS);
		setPodcastNoEpisodesCss(PODCAST_NO_EPISODES_CSS);
	}
	
	/**
	 * Opens the nth course element within the specified course.
	 * 
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @return true on success otherwise false
	 */
	public boolean open(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourse(browser, courseId))
			return(false);
		
		return(open(browser, nth));
	}
	
	/**
	 * Opens the nth course element in the current course.
	 * 
	 * @param browser
	 * @param nth
	 * @return true on success
	 */
	public boolean open(Selenium browser, int nth){
		functionalUtil.idle(browser);

		StringBuffer selectorBuffer = new StringBuffer();

		if(nth == -1){
			selectorBuffer.append("xpath=(//ul[contains(@class, 'b_tree_l0')]//li)//a");
		}else{
			selectorBuffer.append("xpath=(//ul[contains(@class, 'b_tree_l1')]//li)[")
			.append(nth + 1)
			.append("]//a");
		}

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Opens the nth course element of course node type specified by alias in the current course.
	 * 
	 * @param browser
	 * @param alias
	 * @param nth
	 * @return true on success
	 */
	public boolean open(Selenium browser, CourseNodeAlias alias, int nth){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=(//ul[contains(@class, 'b_tree_l1')]//li]//a[contains(@class, '")
		.append(alias.getIconCss())
		.append("')])[")
		.append(nth + 1)
		.append("]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Opens the nth course element within the specified course
	 * without using business paths.
	 * 
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @return true on success otherwise false
	 */
	public boolean openWithoutBusinessPath(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourseWithoutBusinessPath(browser, courseId))
			return(false);

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//ul[contains(@class, 'b_tree_l1')]//li[")
		.append(nth + 1)
		.append("]//a");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * 
	 * @param browser
	 * @param title
	 * @return
	 */
	public boolean closeTab(Selenium browser, String title){
		functionalUtil.idle(browser);
		
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append("xpath=//li//div//a[@title='")
		.append(title)
		.append("']/../..//a[contains(@class, '")
		.append(getCourseTabCloseCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, stringBuffer.toString());
		browser.click(stringBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Closes the active tab.
	 * 
	 * @param browser
	 * @return
	 */
	public boolean closeActiveTab(Selenium browser){
		functionalUtil.idle(browser);
		
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append("xpath=//li[contains(@class, '")
		.append(getCourseTabActiveCss())
		.append("')]//a[contains(@class, '")
		.append(getCourseTabCloseCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, stringBuffer.toString());
		browser.click(stringBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Opens the course editor but the course must be opened.
	 * 
	 * @param browser
	 * @return true on success
	 */
	public boolean openCourseEditor(Selenium browser){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCourseOpenEditorCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Reads the external link of the currently open course node within the editor.
	 * 
	 * @param browser
	 * @return
	 */
	public String readExternalLink(Selenium browser){
		if(!functionalUtil.openContentTab(browser, 0)){
			return(null);
		}

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[@id='")
		.append(getCourseEditorNodeLinksId())
		.append("']//pre)[1]");
		
		return(browser.getText(selectorBuffer.toString()));
	}
	
	/**
	 * Reads the internal link of the currently open course node within the editor.
	 * 
	 * @param browser
	 * @return
	 */
	public String readInternalLink(Selenium browser){
		if(!functionalUtil.openContentTab(browser, 0)){
			return(null);
		}

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[@id='")
		.append(getCourseEditorNodeLinksId())
		.append("']//pre)[2]");
		
		return(browser.getText(selectorBuffer.toString()));
	}
	
	/**
	 * Disables the specified access option, the course editor should be open.
	 * 
	 * @param browser
	 * @param option
	 * @param nthForm
	 * @return true on success
	 */
	public boolean disableAccessOption(Selenium browser, AccessOption option, int nthForm){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * Enables the specified access option, the course editor should be open.
	 * 
	 * @param browser
	 * @param option
	 * @param nthForm
	 * @return true on success
	 */
	public boolean enableAccessOption(Selenium browser, AccessOption option, int nthForm){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * Uploads an individual overview page of course, the course editor should be open.
	 * 
	 * @param browser
	 * @param file
	 * @return true on success
	 * @throws MalformedURLException 
	 */
	public boolean uploadOverviewPage(Selenium browser, URI file) throws MalformedURLException{
		if(!openCourseEditorCourseTab(browser, CourseEditorCourseTab.OVERVIEW)){
			return(false);
		}
		
		/* configure course to display single page */
		if(!functionalUtil.clickRadio(browser, getCourseEditorOverviewRadioGroupCss(), CourseOverview.SINGLEPAGE.getValue())){
			return(false);
		}
		
		functionalUtil.idle(browser);
		
		/* click "select or create page" */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCourseEditorChooseOverviewFileCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* select file */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//input[@type='file']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.focus(selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), file.toURL().getPath());
		
		/* click upload */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(getCourseEditorUploadOverviewFileCss())
		.append("')]//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Creates xpath selectors to select catalog within the tree.
	 * 
	 * @param path
	 * @return
	 */
	public String[] createCatalogSelectors(String path){
		if(path == null ||
				!path.startsWith("/")){
			return(null);
		}
		
		Matcher categoryMatcher = categoryPattern.matcher(path);
		ArrayList<String> selectors = new ArrayList<String>();
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//li//a[contains(@class, '")
		.append(functionalUtil.getTreeNodeAnchorCss())
		.append("')]");
		
		selectors.add(selectorBuffer.toString());
		
		while(categoryMatcher.find()){
			StringBuffer selector = new StringBuffer();
			
			selector.append("xpath=//li//a[contains(@class, '")
			.append(functionalUtil.getTreeNodeCss())
			.append("')]//span[text()='")
			.append(categoryMatcher.group(1))
			.append("']/..");
			
			selectors.add(selector.toString());
		}
		
		return(selectors.toArray(new String[selectors.size()]));
	}
	
	/**
	 * Publishes the entire course.
	 * 
	 * @param browser
	 * @param access
	 * @param catalog
	 * @return true on success
	 */
	public boolean publishEntireCourse(Selenium browser, AccessSettings access, String catalog){
		functionalUtil.idle(browser);
		
		/* click publish */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCourseEditorPublishCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* select all course nodes */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCourseEditorPublishWizardSelectAllCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.clickWizardNext(browser);
		
		/* access options */
		functionalUtil.waitForPageToLoadElement(browser, "id=" + getCourseEditorPublishWizardAccessId());
		
		if(access != null){
			functionalUtil.selectOption(browser, getCourseEditorPublishWizardAccessId(), access.getAccessValue());
		}
		
		functionalUtil.clickWizardNext(browser);
		
		/* add to catalog or not */
		functionalUtil.waitForPageToLoadElement(browser, "id=" + getCourseEditorPublishWizardCatalogId());
		
		if(catalog != null){
			functionalUtil.selectOption(browser, getCourseEditorPublishWizardCatalogId(), ADD_TO_CATALOG_YES_VALUE);
			
			/* click add to catalog */
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//a[contains(@class, '")
			.append(getAddToCatalogCss())
			.append("')]");
			
			browser.click(selectorBuffer.toString());
			
			String[] catalogSelectors = createCatalogSelectors(catalog);
			
			for(String catalogSelector: catalogSelectors){
				functionalUtil.waitForPageToLoadElement(browser, catalogSelector);

				if(browser.isElementPresent(catalogSelector + "/../img[contains(@class, 'x-tree-elbow-end-plus')]")){
					browser.doubleClick(catalogSelector);
				}else{
					browser.click(catalogSelector);
				}
			}
			
			/* click choose */
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//div[contains(@class, '")
			.append(getCatalogCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getButtonDirtyCss())
			.append("')]");
			
			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			
			browser.focus(selectorBuffer.toString());
			browser.click(selectorBuffer.toString());
			
			functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		}else{
			functionalUtil.selectOption(browser, getCourseEditorPublishWizardCatalogId(), ADD_TO_CATALOG_NO_VALUE);
		}
		
		functionalUtil.clickWizardFinish(browser);
		functionalUtil.waitForPageToUnloadElement(browser, "id=" + getCourseEditorPublishWizardCatalogId());
		
		return(true);
	}

	/**
	 * Creates the specified course node in a opened course editor.
	 * 
	 * @param browser
	 * @param node
	 * @param title
	 * @param description
	 * @param position
	 * @return true on success otherwise false
	 */
	public boolean createCourseNode(Selenium browser, CourseNodeAlias node, String shortTitle, String longTitle, String description, int position){
		functionalUtil.idle(browser);
		
		/* click on the appropriate link to create node */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getCourseEditorInsertContentCss())
		.append("')]")
		.append("//a[contains(@class, '")
		.append(node.getIconCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());

		functionalUtil.waitForPageToLoad(browser);
		
		/* choose insertion point */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'b_window')]//form[@name='seltree']//div[contains(@class, '")
		.append(getCreateCourseNodeTargetPositionItemCss())
		.append("')]//input[@type='radio'])[")
		.append(position + 1)
		.append("]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'b_window')]//form[@name='seltree']//button[contains(@class, 'b_button_dirty')])[1]");
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		/* fill in short title */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'o_editor')]//form//input[@type='text'])[1]");
		
		browser.type(selectorBuffer.toString(), shortTitle);
		
		/* fill in long title */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'o_editor')]//form//input[@type='text'])[2]");
		
		browser.type(selectorBuffer.toString(), longTitle);
		
		/* fill in description */
		functionalUtil.typeMCE(browser, description);
		
		/* click save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'o_editor')]//form//button)[1]");
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Adds an artefact to eportfolio by clicking the appropriate
	 * button.
	 * 
	 * @param browser
	 * @return true on success
	 */
	public boolean addToEportfolio(Selenium browser, String binder, String page, String structure,
			String title, String description, String[] tags,
			FunctionalEPortfolioUtil functionalEPortfolioUtil){

		functionalUtil.idle(browser);
		
		/* open wizard */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getEportfolioAddCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		if(binder != null){
			/* fill in wizard - title & description */
			functionalEPortfolioUtil.fillInTitleAndDescription(browser, title, description);
			
			/* fill in wizard - tags */
			functionalEPortfolioUtil.fillInTags(browser, tags);
			
			/* fill in wizard - destination */
			functionalEPortfolioUtil.selectTree(browser, binder, page, structure);
		}

		/* click finish */
		functionalUtil.clickWizardFinish(browser, functionalEPortfolioUtil.getArtefactWizardCss());
		
		return(true);
	}
	
	/**
	 * Opens the course with courseId and nth forum within the specified
	 * course.
	 * 
	 * @param browser
	 * @param courseId
	 * @param nth forum in the course
	 * @return true on success, otherwise false
	 */
	public boolean openForum(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourse(browser, courseId))
			return(false);

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=(//ul//li//a[contains(@class, '")
		.append(getForumIconCss())
		.append("')])[")
		.append(nth + 1)
		.append("]")
		.append("");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Opens the specified forum in the course and posts a new topic.
	 * 
	 * @param browser
	 * @param courseId
	 * @param nthForum
	 * @param title
	 * @param message
	 * @return true on success, otherwise false
	 */
	public boolean postForumMessage(Selenium browser, long courseId, int nthForum, String title, String message){
		if(!openForum(browser, courseId, nthForum))
			return(false);

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		/* click open new topic */
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getForumToolbarCss())
		.append("')]//a[contains(@class, '")
		.append(getForumThreadNewCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in form - title */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getCourseRunCss())
		.append("')]//form//input[@type='text']");
		
		browser.type(selectorBuffer.toString(), title);
		
//		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in form - post */
		functionalUtil.typeMCE(browser, message);
		
		/* save form */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getCourseRunCss())
		.append("')]//form//button[last()]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Opens the wiki specified by id.
	 * 
	 * @param browser
	 * @param id
	 * @return
	 */
	public boolean openWiki(Selenium browser, long id){
		functionalUtil.openBusinessPath(browser, functionalUtil.getDeploymentUrl() + "/url/RepositoryEntry/" + id);
		
		return(true);
	}
	
	/**
	 * Creates a new wiki article.
	 * 
	 * @param browser
	 * @param wikiId
	 * @param pagename
	 * @param content
	 * @return true on success, otherwise false
	 */
	public boolean createWikiArticle(Selenium browser, long wikiId, String pagename, String content){
		if(!openWiki(browser, wikiId))
			return(false);

		functionalUtil.idle(browser);
		
		/* type pagename */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(getWikiCreateArticleCss())
		.append("')]/..//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.type(selectorBuffer.toString(), pagename);
		
		/* click create */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(getWikiCreateArticleCss())
		.append("')]//button");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* edit content */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getWikiArticleBoxCss())
		.append("')]//a");
		
		browser.click(selectorBuffer.toString());

		functionalUtil.waitForPageToLoad(browser);
		
		
		/* fill in text area */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getWikiEditFormWrapperCss())
		.append("')]//textarea");
		
		browser.type(selectorBuffer.toString(), content);
		
		/* click save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getWikiEditFormWrapperCss())
		.append("')]//button[last()]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Opens the blog specified by id
	 * 
	 * @param browser
	 * @param id
	 * @return true on success, otherwise false
	 */
	public boolean openBlog(Selenium browser, long id){
		functionalUtil.openBusinessPath(browser, functionalUtil.getDeploymentUrl() + "/url/RepositoryEntry/" + id);
		
		return(true);
	}
	
	/**
	 * Opens the course with courseId and nth blog within the specified
	 * course.
	 * 
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @return
	 */
	public boolean openBlogWithoutBusinessPath(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourse(browser, courseId))
			return(false);

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=(//ul//li//a[contains(@class, '")
		.append(getBlogIconCss())
		.append("')])[")
		.append(nth + 1)
		.append("]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}

	/**
	 * Imports an existing feed into blog.
	 * 
	 * @param browser
	 * @param url
	 * @return true on success
	 */
	public boolean importBlogFeed(Selenium browser, String url){

		functionalUtil.idle(browser);
		
		/* open popup to enter url */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getBlogNoPostsCss())
		.append("')]//a[contains(@class, 'b_button')])[last()]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* enter url */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'b_window_content')]//form//input[@type='text'])[2]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), url);
		
		/* click save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'b_window_content')]//form//button[contains(@class, 'b_button_dirty')])[last()]");
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}

	/**
	 * 
	 * @param browser
	 * @param nth
	 * @return
	 */
	public boolean openBlogEntry(Selenium browser, int nth){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//ul[contains(@class, '")
		.append(getBlogReadEntryCss())
		.append("')]//a)[")
		.append(nth + 1)
		.append("]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Clicks back button within open blog entry.
	 * 
	 * @param browser
	 * @return
	 */
	public boolean backBlogEntry(Selenium browser){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getBlogPostCss())
		.append("')]//a[contains(@class, '")
		.append(functionalUtil.getBackButtonCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Create a new blog entry.
	 * 
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @param title
	 * @param description
	 * @param content
	 * @return
	 */
	public boolean createBlogEntry(Selenium browser, long courseId, int nth,
			String title, String description, String content){
		return(editBlogEntry(browser, courseId, nth, title, description, content, -1, null));
	}
	
	
	/**
	 * Edit a blog entry.
	 * 
	 * @param browser
	 * @param blogId
	 * @param title
	 * @param description
	 * @param content
	 * @return true on success, otherwise false
	 */
	public boolean editBlogEntry(Selenium browser, long courseId, int nth,
			String title, String description, String content, int entry, BlogEdit[] edit){
		if(!openBlogWithoutBusinessPath(browser, courseId, nth))
			return(false);

		return(editBlogEntry(browser, title, description, content, entry, edit));
	}
	
	public boolean editBlogEntry(Selenium browser,
			String title, String description, String content, int entry, BlogEdit[] edit){
		StringBuffer selectorBuffer = new StringBuffer();
		
		if(edit == null){
			functionalUtil.idle(browser);
			
			/* open popup to enter url */
			selectorBuffer.append("xpath=(//div[contains(@class, '")
			.append(getBlogNoPostsCss())
			.append("')]//a[contains(@class, 'b_button')])");

			if(browser.isElementPresent(selectorBuffer.toString())){
				browser.click(selectorBuffer.toString());
			}else{
				/* click create */
				selectorBuffer = new StringBuffer();

				selectorBuffer.append("xpath=//a[contains(@class, '")
				.append(getBlogCreateEntryCss())
				.append("')]");

				browser.click(selectorBuffer.toString());
			}
		}else{
			if(edit.length > 0){
				selectorBuffer = new StringBuffer();
				
				selectorBuffer.append("xpath=(//a[contains(@class, '")
				.append(getBlogEditEntryCss())
				.append("')])[")
				.append(entry + 1)
				.append("]");
				
				functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
				browser.click(selectorBuffer.toString());
			}
		}
		
		/* fill in form - title */
		if(edit == null || ArrayUtils.contains(edit, BlogEdit.TITLE)){
			functionalUtil.idle(browser);

			selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=(//form//div[contains(@class, '")
			.append(getBlogFormCss())
			.append("')]//input[@type='text'])[1]");

			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());

			browser.type(selectorBuffer.toString(), title);
		}
		
		/* fill in form - description */
		if(edit == null || ArrayUtils.contains(edit, BlogEdit.DESCRIPTION)){
			functionalUtil.typeMCE(browser, getBlogFormCss(), 0, description);
		}
		
		/* fill in form - content */
		if(edit == null || ArrayUtils.contains(edit, BlogEdit.CONTENT)){
			functionalUtil.typeMCE(browser, getBlogFormCss(), 1, content);
		}
		
		/* save form */
		if(edit == null || edit.length > 0){
			selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=//form//div[contains(@class, '")
			.append(getBlogFormCss())
			.append("')]//button[last() and contains(@class, '")
			.append(functionalUtil.getButtonDirtyCss())
			.append("')]");

			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			browser.click(selectorBuffer.toString());
			functionalUtil.waitForPageToLoad(browser);
		}
		
		return(true);
	}
	
	/**
	 * Imports an existing feed into podcast.
	 * 
	 * @param browser
	 * @param url
	 * @return true on success
	 */
	public boolean importPodcastFeed(Selenium browser, String url){
		functionalUtil.idle(browser);
		
		/* open popup to enter url */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getPodcastNoEpisodesCss())
		.append("')]//a[contains(@class, 'b_button')])[last()]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* enter url */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'b_window_content')]//form//input[@type='text'])[2]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), url);
		
		/* click save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'b_window_content')]//form//button[contains(@class, 'b_button_dirty')])[last()]");
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/** 
	 * Opens the course configurations appropriate tab.
	 *
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorCourseTab(Selenium browser, CourseEditorCourseTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * Opens the test configurations appropriate tab.
	 * 
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorIQTestTab(Selenium browser, CourseEditorIQTestTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * Opens the content package configurations appropriate tab
	 * 
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorCpTab(Selenium browser, CourseEditorCpTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * Opens the wiki configurations appropriate tab.
	 * 
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorWikiTab(Selenium browser, CourseEditorWikiTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * Opens the blog configurations appropriate tab.
	 * 
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorBlogTab(Selenium browser, CourseEditorBlogTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * Opens the podcast configurations appropriate tab.
	 * 
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorPodcastTab(Selenium browser, CourseEditorPodcastTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * Opens the portfolio task configurations appropriate tab.
	 * 
	 * @param browser
	 * @param tab
	 * @return true on success
	 */
	public boolean openCourseEditorPortfolioTaskTab(Selenium browser, CourseEditorPortfolioTaskTab tab){
		functionalUtil.idle(browser);
		
		return(functionalUtil.openContentTab(browser, tab.ordinal()));
	}
	
	/**
	 * 
	 * @param browser
	 * @param chooseRepositoryCss
	 * @param key
	 * @return
	 */
	private boolean chooseRepositoryResource(Selenium browser, String chooseRepositoryCss, long key){
		functionalUtil.idle(browser);
		
		/* click on "choose, create or import file" button */
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(chooseRepositoryCss)
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* click search link */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getRepositoryPopupSearchResourcesCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* type key and search */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(FunctionalRepositorySiteUtil.SearchField.ID.getEntryCss())
		.append("')]//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), Long.toString(key));
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getRepositoryPopupCss())
		.append("')]//a[contains(@class, '")
		.append(functionalUtil.getButtonCss())
		.append("')])[last()]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* choose resource */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getRepositoryPopupCss())
		.append("')]//tr[contains(@class, '")
		.append(functionalUtil.getTableFirstChildCss())
		.append("') and contains(@class, '")
		.append(functionalUtil.getTableLastChildCss())
		.append("')]//td[contains(@class, '")
		.append(functionalUtil.getTableLastChildCss())
		.append("')]//a");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Opens and fills in the "create resource" popup.
	 * 
	 * @param browser
	 * @param chooseRepositoryCss
	 * @param title
	 * @param description
	 * @return true on success
	 */
	private boolean createRepositoryResource(Selenium browser, String chooseRepositoryCss, String title, String description){
		functionalUtil.idle(browser);
		
		/* click on "choose, create or import file" button */
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(chooseRepositoryCss)
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* click create button */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getRepositoryPopupCreateResourceCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* */
		return(functionalRepositorySiteUtil.fillInRepositoryEntryPopup(browser, title, description));
	}
	
	/**
	 * Creates a new test.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createQTITest(Selenium browser, String title, String description){
		if(!openCourseEditorIQTestTab(browser, CourseEditorIQTestTab.TEST_CONFIGURATION))
			return(false);

		if(!createRepositoryResource(browser, getTestChooseRepositoryFileCss(), title, description)){
			return(false);
		}
				
		return(true);
	}
	
	/**
	 * Creates a new CP learning content.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createCPLearningContent(Selenium browser, String title, String description){
		if(!openCourseEditorCpTab(browser, CourseEditorCpTab.LEARNING_CONTENT))
			return(false);
		
		if(!createRepositoryResource(browser, getCpChooseRepositoryFileCss(), title, description)){
			return(false);
		}
				
		return(true);
	}
	
	/**
	 * Creates a new wiki.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createWiki(Selenium browser, String title, String description){
		if(!openCourseEditorWikiTab(browser, CourseEditorWikiTab.LEARNING_CONTENT))
			return(false);
		
		if(!createRepositoryResource(browser, getWikiChooseRepositoryFileCss(), title, description)){
			return(false);
		}
				
		return(true);
	}
	
	/**
	 * Choose an existing wiki.
	 * 
	 * @param browser
	 * @param wikiId
	 * @return
	 */
	public boolean chooseWiki(Selenium browser, long wikiId){
		if(!openCourseEditorWikiTab(browser, CourseEditorWikiTab.LEARNING_CONTENT))
			return(false);
		
		if(!chooseRepositoryResource(browser, getWikiChooseRepositoryFileCss(), wikiId)){
			return(false);
		}
		
		return(true);
	}
	/**
	 * Creates a new blog.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createBlog(Selenium browser, String title, String description){
		if(!openCourseEditorBlogTab(browser, CourseEditorBlogTab.LEARNING_CONTENT))
			return(false);
		
		if(!createRepositoryResource(browser, getFeedChooseRepositoryFileCss(), title, description)){
			return(false);
		}
				
		return(true);
	}
	
	/**
	 * Creates a new podcast.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createPodcast(Selenium browser, String title, String description){
		if(!openCourseEditorPodcastTab(browser, CourseEditorPodcastTab.LEARNING_CONTENT))
			return(false);
		
		if(!createRepositoryResource(browser, getFeedChooseRepositoryFileCss(), title, description)){
			return(false);
		}
				
		return(true);
	}
	
	/**
	 * Creates a new podcast.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createPortfolioTask(Selenium browser, String title, String description){
		if(!openCourseEditorPortfolioTaskTab(browser, CourseEditorPortfolioTaskTab.LEARNING_CONTENT))
			return(false);
		
		if(!createRepositoryResource(browser, getMapChooseRepositoryFileCss(), title, description)){
			return(false);
		}
				
		return(true);
	}
	
	/**
	 * Opens the portfolio template editor in conjunction with this method the appropriate node and
	 * the appopriate tab should already be opened.
	 * 
	 * @param browser
	 * @return true on success
	 */
	public boolean openPortfolioTemplateEditor(Selenium browser){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getMapEditCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * 
	 * @param browser
	 * @param file
	 * @return
	 */
	public boolean uploadFileToCourseFolder(Selenium browser, URI file){
		/* open upload dialog */
		functionalUtil.idle(browser);

		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//a[contains(@class, 'b_briefcase_upload')]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());

		/* attach file */
		functionalUtil.idle(browser);

		selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//input[@type='file']");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.attachFile(selectorBuffer.toString(), file.toString());

		/* upload file */
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//button[last() and contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		return(true);
	}
	
	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}

	public FunctionalRepositorySiteUtil getFunctionalRepositorySiteUtil() {
		return functionalRepositorySiteUtil;
	}

	public void setFunctionalRepositorySiteUtil(
			FunctionalRepositorySiteUtil functionalRepositorySiteUtil) {
		this.functionalRepositorySiteUtil = functionalRepositorySiteUtil;
	}

	public String getCourseRunCss() {
		return courseRunCss;
	}

	public void setCourseRunCss(String courseRunCss) {
		this.courseRunCss = courseRunCss;
	}

	public String getCourseOpenEditorCss() {
		return courseOpenEditorCss;
	}

	public void setCourseOpenEditorCss(String courseOpenEditorCss) {
		this.courseOpenEditorCss = courseOpenEditorCss;
	}

	public String getCourseTabActiveCss() {
		return courseTabActiveCss;
	}

	public void setCourseTabActiveCss(String courseTabActiveCss) {
		this.courseTabActiveCss = courseTabActiveCss;
	}

	public String getCourseTabCloseCss() {
		return courseTabCloseCss;
	}

	public void setCourseTabCloseCss(String courseTabCloseCss) {
		this.courseTabCloseCss = courseTabCloseCss;
	}

	public String getCourseEditorNodeLinksId() {
		return courseEditorNodeLinksId;
	}

	public void setCourseEditorNodeLinksId(String courseEditorNodeLinksCss) {
		this.courseEditorNodeLinksId = courseEditorNodeLinksCss;
	}

	public String getCourseEditorPublishCss() {
		return courseEditorPublishCss;
	}

	public void setCourseEditorPublishCss(String courseEditorPublishCss) {
		this.courseEditorPublishCss = courseEditorPublishCss;
	}

	public String getCourseEditorPublishWizardSelectAllCss() {
		return courseEditorPublishWizardSelectAllCss;
	}

	public void setCourseEditorPublishWizardSelectAllCss(
			String courseEditorPublishWizardSelectAllCss) {
		this.courseEditorPublishWizardSelectAllCss = courseEditorPublishWizardSelectAllCss;
	}

	public String getCourseEditorPublishWizardAccessId() {
		return courseEditorPublishWizardAccessId;
	}

	public void setCourseEditorPublishWizardAccessId(
			String courseEditorPublishWizardAccessId) {
		this.courseEditorPublishWizardAccessId = courseEditorPublishWizardAccessId;
	}

	public String getCourseEditorPublishWizardCatalogId() {
		return courseEditorPublishWizardCatalogId;
	}

	public void setCourseEditorPublishWizardCatalogId(
			String courseEditorPublishWizardCatalogId) {
		this.courseEditorPublishWizardCatalogId = courseEditorPublishWizardCatalogId;
	}

	public String getAddToCatalogCss() {
		return addToCatalogCss;
	}

	public void setAddToCatalogCss(String addToCatalogCss) {
		this.addToCatalogCss = addToCatalogCss;
	}

	public String getCatalogCss() {
		return catalogCss;
	}

	public void setCatalogCss(String catalogCss) {
		this.catalogCss = catalogCss;
	}

	public String getCatalogSubcategoryIconCss() {
		return catalogSubcategoryIconCss;
	}

	public void setCatalogSubcategoryIconCss(String catalogSubcategoryIconCss) {
		this.catalogSubcategoryIconCss = catalogSubcategoryIconCss;
	}

	public String getCourseEditorOverviewRadioGroupCss() {
		return courseEditorOverviewRadioGroupCss;
	}

	public void setCourseEditorOverviewRadioGroupCss(
			String courseEditorOverviewRadioGroupCss) {
		this.courseEditorOverviewRadioGroupCss = courseEditorOverviewRadioGroupCss;
	}

	public String getCourseEditorInsertContentCss() {
		return courseEditorInsertContentCss;
	}

	public void setCourseEditorInsertContentCss(String courseEditorInsertContentCss) {
		this.courseEditorInsertContentCss = courseEditorInsertContentCss;
	}

	public String getCreateCourseNodeTargetPositionItemCss() {
		return createCourseNodeTargetPositionItemCss;
	}

	public void setCreateCourseNodeTargetPositionItemCss(
			String createCourseNodeTargetPositionItemCss) {
		this.createCourseNodeTargetPositionItemCss = createCourseNodeTargetPositionItemCss;
	}

	public String getCourseEditorChooseOverviewFileCss() {
		return courseEditorChooseOverviewFileCss;
	}

	public void setCourseEditorChooseOverviewFileCss(
			String courseEditorChooseOverviewFileCss) {
		this.courseEditorChooseOverviewFileCss = courseEditorChooseOverviewFileCss;
	}

	public String getCourseEditorUploadOverviewFileCss() {
		return courseEditorUploadOverviewFileCss;
	}

	public void setCourseEditorUploadOverviewFileCss(
			String courseEditorUploadOverviewFileCss) {
		this.courseEditorUploadOverviewFileCss = courseEditorUploadOverviewFileCss;
	}

	public String getEportfolioAddCss() {
		return eportfolioAddCss;
	}

	public void setEportfolioAddCss(String eportfolioAddCss) {
		this.eportfolioAddCss = eportfolioAddCss;
	}

	public String getStructureIconCss() {
		return structureIconCss;
	}

	public void setStructureIconCss(String structureIconCss) {
		this.structureIconCss = structureIconCss;
	}

	public String getForumIconCss() {
		return forumIconCss;
	}

	public void setForumIconCss(String forumIconCss) {
		this.forumIconCss = forumIconCss;
	}

	public String getBlogIconCss() {
		return blogIconCss;
	}

	public void setBlogIconCss(String blogIconCss) {
		this.blogIconCss = blogIconCss;
	}

	public String getForumToolbarCss() {
		return forumToolbarCss;
	}

	public void setForumToolbarCss(String forumToolbarCss) {
		this.forumToolbarCss = forumToolbarCss;
	}

	public String getForumThreadNewCss() {
		return forumThreadNewCss;
	}

	public void setForumThreadNewCss(String forumThreadNewCss) {
		this.forumThreadNewCss = forumThreadNewCss;
	}

	public String getForumArchiveCss() {
		return forumArchiveCss;
	}

	public void setForumArchiveCss(String forumArchiveCss) {
		this.forumArchiveCss = forumArchiveCss;
	}

	public String getForumFilterCss() {
		return forumFilterCss;
	}

	public void setForumFilterCss(String forumFilterCss) {
		this.forumFilterCss = forumFilterCss;
	}

	public String getWikiCreateArticleCss() {
		return wikiCreateArticleCss;
	}

	public void setWikiCreateArticleCss(String wikiCreateArticleCss) {
		this.wikiCreateArticleCss = wikiCreateArticleCss;
	}

	public String getWikiArticleBoxCss() {
		return wikiArticleBoxCss;
	}

	public void setWikiArticleBoxCss(String wikiArticleBoxCss) {
		this.wikiArticleBoxCss = wikiArticleBoxCss;
	}

	public String getWikiEditFormWrapperCss() {
		return wikiEditFormWrapperCss;
	}

	public void setWikiEditFormWrapperCss(String wikiEditFormWrapperCss) {
		this.wikiEditFormWrapperCss = wikiEditFormWrapperCss;
	}

	public String getBlogCreateEntryCss() {
		return blogCreateEntryCss;
	}

	public void setBlogCreateEntryCss(String blogCreateEntryCss) {
		this.blogCreateEntryCss = blogCreateEntryCss;
	}

	public String getBlogEditEntryCss() {
		return blogEditEntryCss;
	}

	public void setBlogEditEntryCss(String blogEditEntryCss) {
		this.blogEditEntryCss = blogEditEntryCss;
	}

	public String getBlogDeleteEntryCss() {
		return blogDeleteEntryCss;
	}

	public void setBlogDeleteEntryCss(String blogDeleteEntryCss) {
		this.blogDeleteEntryCss = blogDeleteEntryCss;
	}

	public String getBlogReadEntryCss() {
		return blogReadEntryCss;
	}

	public void setBlogReadEntryCss(String blogReadEntryCss) {
		this.blogReadEntryCss = blogReadEntryCss;
	}

	public String getBlogFormCss() {
		return blogFormCss;
	}

	public void setBlogFormCss(String blogFormCss) {
		this.blogFormCss = blogFormCss;
	}

	public String getBlogPostCss() {
		return blogPostCss;
	}

	public void setBlogPostCss(String blogPostCss) {
		this.blogPostCss = blogPostCss;
	}

	public String getBlogYearCss() {
		return blogYearCss;
	}

	public void setBlogYearCss(String blogYearCss) {
		this.blogYearCss = blogYearCss;
	}

	public String getBlogMonthCss() {
		return blogMonthCss;
	}

	public void setBlogMonthCss(String blogMonthCss) {
		this.blogMonthCss = blogMonthCss;
	}

	public String getTestChooseRepositoryFileCss() {
		return testChooseRepositoryFileCss;
	}

	public void setTestChooseRepositoryFileCss(String testChooseRepositoryFileCss) {
		this.testChooseRepositoryFileCss = testChooseRepositoryFileCss;
	}

	public String getCpChooseRepositoryFileCss() {
		return cpChooseRepositoryFileCss;
	}

	public void setCpChooseRepositoryFileCss(String cpChooseRepositoryFileCss) {
		this.cpChooseRepositoryFileCss = cpChooseRepositoryFileCss;
	}

	public String getWikiChooseRepositoryFileCss() {
		return wikiChooseRepositoryFileCss;
	}

	public void setWikiChooseRepositoryFileCss(String wikiChooseRepositoryFileCss) {
		this.wikiChooseRepositoryFileCss = wikiChooseRepositoryFileCss;
	}

	public String getFeedChooseRepositoryFileCss() {
		return feedChooseRepositoryFileCss;
	}

	public void setFeedChooseRepositoryFileCss(String feedChooseRepositoryFileCss) {
		this.feedChooseRepositoryFileCss = feedChooseRepositoryFileCss;
	}

	public String getMapChooseRepositoryFileCss() {
		return mapChooseRepositoryFileCss;
	}

	public void setMapChooseRepositoryFileCss(String mapChooseRepositoryFileCss) {
		this.mapChooseRepositoryFileCss = mapChooseRepositoryFileCss;
	}

	public String getRepositoryPopupCss() {
		return repositoryPopupCss;
	}

	public void setRepositoryPopupCss(String repositoryPopupCss) {
		this.repositoryPopupCss = repositoryPopupCss;
	}

	public String getRepositoryPopupCreateResourceCss() {
		return repositoryPopupCreateResourceCss;
	}

	public void setRepositoryPopupCreateResourceCss(String repositoryPopupCreateResourceCss) {
		this.repositoryPopupCreateResourceCss = repositoryPopupCreateResourceCss;
	}

	public String getRepositoryPopupImportResourceCss() {
		return repositoryPopupImportResourceCss;
	}

	public void setRepositoryPopupImportResourceCss(
			String repositoryPopupImportResourceCss) {
		this.repositoryPopupImportResourceCss = repositoryPopupImportResourceCss;
	}

	public String getRepositoryPopupAllResourcesCss() {
		return repositoryPopupAllResourcesCss;
	}

	public void setRepositoryPopupAllResourcesCss(
			String repositoryPopupAllResourcesCss) {
		this.repositoryPopupAllResourcesCss = repositoryPopupAllResourcesCss;
	}

	public String getRepositoryPopupMyResourcesCss() {
		return repositoryPopupMyResourcesCss;
	}

	public void setRepositoryPopupMyResourcesCss(
			String repositoryPopupMyResourcesCss) {
		this.repositoryPopupMyResourcesCss = repositoryPopupMyResourcesCss;
	}

	public String getRepositoryPopupSearchResourcesCss() {
		return repositoryPopupSearchResourcesCss;
	}

	public void setRepositoryPopupSearchResourcesCss(
			String repositoryPopupSearchResourcesCss) {
		this.repositoryPopupSearchResourcesCss = repositoryPopupSearchResourcesCss;
	}

	public String getMapEditCss() {
		return mapEditCss;
	}

	public void setMapEditCss(String mapEditCss) {
		this.mapEditCss = mapEditCss;
	}

	public String getBlogNoPostsCss() {
		return blogNoPostsCss;
	}

	public void setBlogNoPostsCss(String blogNoPostsCss) {
		this.blogNoPostsCss = blogNoPostsCss;
	}

	public String getPodcastNoEpisodesCss() {
		return podcastNoEpisodesCss;
	}

	public void setPodcastNoEpisodesCss(String podcastNoEpisodesCss) {
		this.podcastNoEpisodesCss = podcastNoEpisodesCss;
	}
	
}
