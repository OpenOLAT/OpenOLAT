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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * Description: <br>
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalRepositorySiteUtil {	
	private final static OLog log = Tracing.createLoggerFor(FunctionalRepositorySiteUtil.class);
	
	private final static Pattern categoryPattern = Pattern.compile("/([^/]+)");
	
	public final static String REPOSITORY_POPUP_CSS = "o_sel_edit_repositoryentry_popup";
	public final static String REPOSITORY_SAVE_DETAILS_CSS = "o_sel_repo_save_details";
	public final static String REPOSITORY_ADD_FORWARD_CSS = "o_sel_repo_add_forward";
	public final static String REPOSITORY_ADD_TEMPLATE_FORWARD_CSS = "o_sel_repo_add_template_forward";
	
	public final static String COURSE_WIZARD_PUBLISH_CHECKBOX = "publishCheckbox";
	public final static String COURSE_WIZARD_ACCESS_OPTION_ID = "o_fioaccessChooser_SELBOX";
	
	public final static String REPOSITORY_SITE_MENU_TREE_SELECTED_CSS = "b_tree_selected";
	
	public final static String REPOSITORY_SITE_CATALOG_CSS = "o_sel_repo_catalog";
	public final static String REPOSITORY_SITE_SEARCH_FORM_CSS = "o_sel_repo_search_generic";
	public final static String REPOSITORY_SITE_MY_ENTRIES_CSS = "o_sel_repo_my";
	public final static String REPOSITORY_SITE_MY_COURSES_CSS = "o_sel_repo_my_student";
	public final static String REPOSITORY_SITE_MY_SUPERVISED_COURSES_CSS = "o_sel_repo_my_teacher";
	public final static String REPOSITORY_SITE_COURSES_CSS = "o_sel_repo_course";
	public final static String REPOSITORY_SITE_CP_LEARNING_CONTENT_CSS = "o_sel_repo_cp";
	public final static String REPOSITORY_SITE_SCORM_LEARNING_CONTENT_CSS = "o_sel_repo_scorm";
	public final static String REPOSITORY_SITE_WIKIS_CSS = "o_sel_repo_wiki";
	public final static String REPOSITORY_SITE_PODCASTS_CSS = "o_sel_repo_podcast";
	public final static String REPOSITORY_SITE_BLOGS_CSS = "o_sel_repo_blog";
	public final static String REPOSITORY_SITE_PORTFOLIO_TEMPLATES_CSS = "o_sel_repo_portfolio";
	public final static String REPOSITORY_SITE_TESTS_CSS = "o_sel_repo_test";
	public final static String REPOSITORY_SITE_QUESTIONAIRES_CSS = "o_sel_repo_survey";
	public final static String REPOSITORY_SITE_RESOURCE_FOLDER_CSS = "o_sel_repo_sharefolder";
	public final static String REPOSITORY_SITE_GLOSSARY_CSS = "o_sel_repo_glossary";
	
	public final static String CATALOG_NAVIGATION_CSS = "o_catalog_nav";
	public final static String CATALOG_ADD_SUBCATEGORY = "o_sel_catalog_add_category";
	public final static String CATALOG_ADD_LEARNING_RESOURCE = "o_sel_catalog_add_link_to_resource";
	public final static String CATALOG_ADD_SUBCATEGORY_POPUP_CSS = "o_sel_catalog_add_category_popup";
	public final static String CATALOG_SUBCATEGORY_ICON_CSS = "o_catalog_sub_icon";
	public final static String CATALOG_SUBCATEGORY_LIST_CSS = "o_catalog_itemlist";
	
	public final static String COURSE_MODULE_ICON_CSS = "o_CourseModule_icon";
	
	public enum Column {
		AC,
		TYPE,
		TITLE_OF_LEARNING_RESOURCE,
		AUTHOR,
		ACCESS,
		CREATED,
		LAST_ACCESS,
		DETAILED_VIEW;
	}
	
	public final static String IMPORT_COURSE_CSS = "o_sel_repo_import_course";
	public final static String IMPORT_CP_LEARNING_CONTENT_CSS = "o_sel_repo_import_cp";
	public final static String IMPORT_SCORM_CSS = "o_sel_repo_import_scorm";
	public final static String IMPORT_WIKI_CSS = "o_sel_repo_import_wiki";
	public final static String IMPORT_PODCAST_CSS = "o_sel_repo_import_podcast";
	public final static String IMPORT_BLOG_CSS = "o_sel_repo_import_blog";
	public final static String IMPORT_TEST_CSS = "o_sel_repo_import_test";
	public final static String IMPORT_QUESTIONAIRE_CSS = "o_sel_repo_import_questionnaire";
	public final static String IMPORT_GLOSSARY_CSS = "o_sel_repo_import_glossary";
	public final static String IMPORT_OTHER_CSS = "o_sel_repo_import_doc";
	
	public final static String CREATE_COURSE_CSS = "o_sel_repo_new_course";
	public final static String CREATE_CP_LEARNING_CONTENT_CSS = "o_sel_repo_new_cp";
	public final static String CREATE_WIKI_CSS = "o_sel_repo_new_wiki";
	public final static String CREATE_PODCAST_CSS = "o_sel_repo_new_podcast";
	public final static String CREATE_BLOG_CSS = "o_sel_repo_new_blog";
	public final static String CREATE_PORTFOLIO_CSS = "o_sel_repo_new_portfolio";
	public final static String CREATE_TEST_CSS = "o_sel_repo_new_test";
	public final static String CREATE_QUESTIONAIRE_CSS = "o_sel_repo_new_questionnaire";
	public final static String CREATE_FOLDER_CSS = "o_sel_repo_new_sharedfolder";
	public final static String CREATE_GLOSSARY_CSS = "o_sel_repo_new_glossary";
	
	public final static String CP_LEARNING_CONTENT_ALIAS = "cp";
	public final static String WIKI_ALIAS = "wiki";
	public final static String PODCAST_ALIAS = "podcast";
	public final static String BLOG_ALIAS = "blog";
	public final static String PORTFOLIO_ALIAS = "portfolio";
	public final static String IQTEST_ALIAS = "iqtest";
	public final static String QUESTIONAIRE_ALIAS = "iqsurv";
	public final static String RESOURCE_FOLDER_ALIAS = "bc";
	public final static String GLOSSARY_ALIAS = "glossary";

	public final static String TITLE_OF_LEARNING_RESOURCE_CSS = "o_sel_repo_search_displayname";
	public final static String AUTHOR_CSS = "o_sel_repo_search_author";
	public final static String DESCRIPTION_CSS = "o_sel_repo_search_description";
	public final static String ID_CSS = "o_sel_repo_search_id";
	public final static String LIMIT_SEARCH_CSS = "o_sel_repo_search_type_limit";
	
	public enum SearchField {	
		TITLE_OF_LEARNING_RESOURCE(TITLE_OF_LEARNING_RESOURCE_CSS),
		AUTHOR(AUTHOR_CSS),
		DESCRIPTION(DESCRIPTION_CSS),
		ID(ID_CSS),
		LIMIT_SEARCH(LIMIT_SEARCH_CSS);
		
		private String entryCss;
		
		SearchField(String entryCss){
			setEntryCss(entryCss);
		}

		public String getEntryCss() {
			return entryCss;
		}

		public void setEntryCss(String entryCss) {
			this.entryCss = entryCss;
		}
	}
	
	public enum RepositorySiteAction {
		CATALOG,
		SEARCH_FORM,
		MY_ENTRIES,
		MY_COURSES,
		MY_SUPERVISED_COURSES,
		COURSES,
		CP_LEARNING_CONTENT,
		SCORM_LEARNING_CONTENT,
		WIKIS,
		PODCASTS,
		BLOGS,
		PORTFOLIO_TEMPLATES,
		TESTS,
		QUESTIONAIRES,
		RESOURCE_FOLDER,
		GLOSSARY;
	}
	
	public enum AccessSettings {
		ONLY_OWNERS,
		OWNERS_AND_AUTHORS,
		USERS,
		USERS_AND_GUESTS,
		MEMBERS_ONLY;
	}
	
	public enum NextSteps {
		WIZARD("sw"),
		COURSE_EDITOR("ce"),
		DETAILED_VIEW("dv");
		
		private String value;
		
		NextSteps(String value){
			setValue(value);
		}
		
		public String getValue(){
			return(value);
		}
		
		public void setValue(String value){
			this.value = value;
		}
	}
	
	public enum CourseWizardElement {
		INFO_PAGE("sp"),
		ENROLLMENT("en"),
		DOWNLOAD_FOLDER("bc"),
		FORUM("fo"),
		EMAIL("co");

		private String value;
		
		CourseWizardElement(String value){
			setValue(value);
		}
		
		public String getValue(){
			return(value);
		}
		
		public void setValue(String value){
			this.value = value;
		}
	}
	
	public enum CourseWizardAccess {
		USERS("acl_olat"),
		USERS_AND_GUESTS("acl_guest");
		
		private String value;

		CourseWizardAccess(String value){
			setValue(value);
		}
		
		public String getValue(){
			return(value);
		}
		
		public void setValue(String value){
			this.value = value;
		}
	}
	
	//TODO:JK: add CSS classes
	public enum DetailedViewAction{
		CLOSE(null),
		COPY(null),
		DELETE(null);
		
		private String actionCss;
		
		DetailedViewAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public final static String TOOLBOX_CONTENT_CSS = "b_toolbox_content";
	public final static String TOOLBOX_COURSE_CSS = "o_toolbox_course";
	public final static String TOOLBOX_CONTENT_PACKAGE_CSS = "o_toolbox_content";
	public final static String TOOLBOX_WIKI_CSS = "o_toolbox_wiki";
	public final static String TOOLBOX_PODCAST_CSS = "o_toolbox_podcast";
	public final static String TOOLBOX_BLOG_CSS = "o_toolbox_blog";
	public final static String TOOLBOX_PORTFOLIO_CSS = "o_toolbox_portfolio";
	public final static String TOOLBOX_IQTEST_CSS = "o_toolbox_test";
	public final static String TOOLBOX_QUESTIONNAIRE_CSS = "o_toolbox_questionnaire";
	public final static String TOOLBOX_SHAREDFOLDER_CSS = "o_toolbox_sharedfolder";
	public final static String TOOLBOX_GLOSSARY_CSS = "o_toolbox_glossary";
	
	private String repositoryPopupCss;
	private String repositorySaveDetailsCss;
	private String repositoryAddForwardCss;
	private String repositoryAddTemplateForwardCss;
	
	private String courseWizardPublishCheckbox;
	private String courseWizardAccessOptionCss;
	
	private String repositorySiteMenuTreeSelectedCss;
	
	private String repositorySiteCatalogCss;
	private String repositorySiteSearchFormCss;
	private String repositorySiteMyEntriesCss;
	private String repositorySiteMyCoursesCss;
	private String repositorySiteMySupervisedCoursesCss;
	private String repositorySiteCoursesCss;
	private String repositorySiteCPLearningContentCss;
	private String repositorySiteScormLearningContentCss;
	private String repositorySiteWikisCss;
	private String repositorySitePodcastsCss;
	private String repositorySiteBlogsCss;
	private String repositorySitePortfolioTemplatesCss;
	private String repositorySiteTestsCss;
	private String repositorySiteQuestionairesCss;
	private String repositorySiteResourceFolderCss;
	private String repositorySiteGlossaryCss;

	private String catalogNavigationCss;
	private String catalogAddSubcategoryCss;
	private String catalogAddLearningResourceCss;
	private String catalogAddSubcategoryPopupCss;
	private String catalogSubcategoryListCss;
	private String catalogSubcategoryIconCss;
	
	private String courseModuleIconCss;
	
	private String importCourseCss;
	private String importCPLearningContentCss;
	private String importScormCss;
	private String importWikiCss;
	private String importPodcastCss;
	private String importBlogCss;
	private String importTestCss;
	private String importQuestionaireCss;
	private String importGlossaryCss;
	private String importOtherCss;
	
	private String createCourseCss;
	private String createCPLearningContentCss;
	private String createWikiCss;
	private String createPodcastCss;
	private String createBlogCss;
	private String createPortfolioCss;
	private String createTestCss;
	private String createQuestionaireCss;
	private String createFolderCss;
	private String createGlossaryCss;
	
	private String toolboxContentCss;
	private String toolboxCourseCss;
	private String toolboxContentPackageCss;
	private String toolboxWikiCss;
	private String toolboxPodcastCss;
	private String toolboxBlogCss;
	private String toolboxPortfolioCss;
	private String toolboxIQTestCss;
	private String toolboxQuestionnaireCss;
	private String toolboxSharedfolderCss;
	private String toolboxGlossaryCss;
	
	private String courseTabActiveCss;
	private String courseTabCloseCss;
	
	private FunctionalUtil functionalUtil;
	private FunctionalCourseUtil functionalCourseUtil;
	
	
	public FunctionalRepositorySiteUtil(FunctionalUtil functionalUtil){
		setRepositoryPopupCss(REPOSITORY_POPUP_CSS);
		setRepositorySaveDetailsCss(REPOSITORY_SAVE_DETAILS_CSS);
		setRepositoryAddForwardCss(REPOSITORY_ADD_FORWARD_CSS);
		setRepositoryAddTemplateForwardCss(REPOSITORY_ADD_TEMPLATE_FORWARD_CSS);
		
		setCourseWizardPublishCheckbox(COURSE_WIZARD_PUBLISH_CHECKBOX);
		setCourseWizardAccessOptionId(COURSE_WIZARD_ACCESS_OPTION_ID);
		
		setRepositorySiteMenuTreeSelectedCss(REPOSITORY_SITE_MENU_TREE_SELECTED_CSS);
		
		setRepositorySiteCatalogCss(REPOSITORY_SITE_CATALOG_CSS);
		setRepositorySiteSearchFormCss(REPOSITORY_SITE_SEARCH_FORM_CSS);
		setRepositorySiteMyEntriesCss(REPOSITORY_SITE_MY_ENTRIES_CSS);
		setRepositorySiteMyCoursesCss(REPOSITORY_SITE_MY_COURSES_CSS);
		setRepositorySiteMySupervisedCoursesCss(REPOSITORY_SITE_MY_SUPERVISED_COURSES_CSS);
		setRepositorySiteCoursesCss(REPOSITORY_SITE_COURSES_CSS);
		setRepositorySiteCPLearningContentCss(REPOSITORY_SITE_CP_LEARNING_CONTENT_CSS);
		setRepositorySiteScormLearningContentCss(REPOSITORY_SITE_SCORM_LEARNING_CONTENT_CSS);
		setRepositorySiteWikisCss(REPOSITORY_SITE_WIKIS_CSS);
		setRepositorySitePodcastsCss(REPOSITORY_SITE_PODCASTS_CSS);
		setRepositorySiteBlogsCss(REPOSITORY_SITE_BLOGS_CSS);
		setRepositorySitePortfolioTemplatesCss(REPOSITORY_SITE_PORTFOLIO_TEMPLATES_CSS);
		setRepositorySiteTestsCss(REPOSITORY_SITE_TESTS_CSS);
		setRepositorySiteQuestionairesCss(REPOSITORY_SITE_QUESTIONAIRES_CSS);
		setRepositorySiteResourceFolderCss(REPOSITORY_SITE_RESOURCE_FOLDER_CSS);
		setRepositorySiteGlossaryCss(REPOSITORY_SITE_GLOSSARY_CSS);

		setCatalogNavigationCss(CATALOG_NAVIGATION_CSS);
		setCatalogAddSubcategoryCss(CATALOG_ADD_SUBCATEGORY);
		setCatalogAddLearningResourceCss(CATALOG_ADD_LEARNING_RESOURCE);
		setCatalogAddSubcategoryPopupCss(CATALOG_ADD_SUBCATEGORY_POPUP_CSS);
		setCatalogSubcategoryListCss(CATALOG_SUBCATEGORY_LIST_CSS);
		setCatalogSubcategoryIconCss(CATALOG_SUBCATEGORY_ICON_CSS);
		
		setCourseModuleIconCss(COURSE_MODULE_ICON_CSS);
		
		setImportCourseCss(IMPORT_COURSE_CSS);
		setImportCPLearningContentCss(IMPORT_CP_LEARNING_CONTENT_CSS);
		setImportScormCss(IMPORT_SCORM_CSS);
		setImportWikiCss(IMPORT_WIKI_CSS);
		setImportPodcastCss(IMPORT_PODCAST_CSS);
		setImportBlogCss(IMPORT_BLOG_CSS);
		setImportTestCss(IMPORT_TEST_CSS);
		setImportQuestionaireCss(IMPORT_QUESTIONAIRE_CSS);
		setImportGlossaryCss(IMPORT_GLOSSARY_CSS);
		setImportOtherCss(IMPORT_OTHER_CSS);

		setCreateCourseCss(CREATE_COURSE_CSS);
		setCreateCPLearningContentCss(CREATE_CP_LEARNING_CONTENT_CSS);
		setCreateWikiCss(CREATE_WIKI_CSS);
		setCreatePodcastCss(CREATE_PODCAST_CSS);
		setCreateBlogCss(CREATE_BLOG_CSS);
		setCreatePortfolioCss(CREATE_PORTFOLIO_CSS);
		setCreateTestCss(CREATE_TEST_CSS);
		setCreateQuestionaireCss(CREATE_QUESTIONAIRE_CSS);
		setCreateFolderCss(CREATE_FOLDER_CSS);
		setCreateGlossaryCss(CREATE_GLOSSARY_CSS);
		
		setToolboxContentCss(TOOLBOX_CONTENT_CSS);
		setToolboxCourseCss(TOOLBOX_COURSE_CSS);
		setToolboxContentPackageCss(TOOLBOX_CONTENT_PACKAGE_CSS);
		setToolboxWikiCss(TOOLBOX_WIKI_CSS);
		setToolboxPodcastCss(TOOLBOX_PODCAST_CSS);
		setToolboxBlogCss(TOOLBOX_BLOG_CSS);
		setToolboxPortfolioCss(TOOLBOX_PORTFOLIO_CSS);
		setToolboxIQTestCss(TOOLBOX_IQTEST_CSS);
		setToolboxQuestionnaireCss(TOOLBOX_QUESTIONNAIRE_CSS);
		setToolboxSharedfolderCss(TOOLBOX_SHAREDFOLDER_CSS);
		setToolboxGlossaryCss(TOOLBOX_GLOSSARY_CSS);
		
		setCourseTabActiveCss(FunctionalCourseUtil.COURSE_TAB_ACTIVE_CSS);
		setCourseTabCloseCss(FunctionalCourseUtil.COURSE_TAB_CLOSE_CSS);
		
		setFunctionalUtil(functionalUtil);
		functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, this);
	}
	
	/**
	 * creates a single selector.
	 * 
	 * @param name
	 * @return
	 */
	private String createCatalogSelector(String name){
		StringBuffer selector = new StringBuffer();
		
		selector.append("xpath=//div[contains(@class, '")
		.append(getCatalogSubcategoryListCss())
		.append("')]//a//span[contains(@class, '")
		.append(getCatalogSubcategoryIconCss())
		.append("') and text()='")
		.append(name)
		.append("']/..");
		
		return(selector.toString());
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
		
		while(categoryMatcher.find()){
			selectors.add(createCatalogSelector(categoryMatcher.group(1)));
		}
		
		return(selectors.toArray(new String[selectors.size()]));
	}
	
	/**
	 * Adds a subcategory to catalog on the specified path.
	 * 
	 * @param browser
	 * @param path
	 * @param name
	 * @param description
	 * @return
	 */
	public boolean createCatalogSubcategory(Selenium browser, String path, String name, String description){
		if(!functionalUtil.openSite(browser, FunctionalUtil.OlatSite.LEARNING_RESOURCES)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, RepositorySiteAction.CATALOG)){
			return(false);
		}
		
		/* click on catalog root */
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getCatalogNavigationCss())
		.append("')]//a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			browser.click(selectorBuffer.toString());
		}
		
		/* create selectors to open desired path within catalog and open it */
		String[] selectors = createCatalogSelectors(path);
		
		if(selectors != null){
			for(String currentSelector: selectors){
				functionalUtil.waitForPageToLoadElement(browser, currentSelector);
				browser.click(currentSelector);
			}
			
			functionalUtil.waitForPageToUnloadElement(browser, selectors[selectors.length - 1]);
		}
		
		/* check if catalog already exists */
		functionalUtil.idle(browser);
		
		String selector = createCatalogSelector(name);
		
		if(browser.isElementPresent(selector)){
			return(true);
		}
		
		/* click create */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCatalogAddSubcategoryCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		/* fill in name */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(getCatalogAddSubcategoryPopupCss())
		.append("')]//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), name);
		
		/* fill in description */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(getCatalogAddSubcategoryPopupCss())
		.append("')]//textarea");
		
		browser.type(selectorBuffer.toString(), description);
		
		/* click save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//form//div[contains(@class, '")
		.append(getCatalogAddSubcategoryPopupCss())
		.append("')]//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')])[last()]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Select displayed columns of repository table.
	 * 
	 * @param browser
	 * @param column
	 * @return
	 */
	public boolean displayColumns(Selenium browser, List<Column> column){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * Find the corresponding CSS class for page.
	 * 
	 * @param action
	 * @return the matching CSS class
	 */
	public String findCssClassOfAction(Object action){
		if(action == null)
			return(null);
		
		String selectedCss = null;
		
		switch((RepositorySiteAction) action){
		case CATALOG:
		{
			selectedCss = getRepositorySiteCatalogCss();
			break;
		}
		case SEARCH_FORM:
		{
			selectedCss = getRepositorySiteSearchFormCss();
			break;
		}
		case MY_ENTRIES:
		{
			selectedCss = getRepositorySiteMyEntriesCss();
			break;
		}
		case MY_COURSES:
		{
			selectedCss = getRepositorySiteMyCoursesCss();
			break;
		}
		case MY_SUPERVISED_COURSES:
		{
			selectedCss = getRepositorySiteMySupervisedCoursesCss();
			break;
		}
		case COURSES:
		{
			selectedCss = getRepositorySiteCoursesCss();
			break;
		}
		case CP_LEARNING_CONTENT:
		{
			selectedCss = getRepositorySiteCPLearningContentCss();
			break;
		}
		case SCORM_LEARNING_CONTENT:
		{
			selectedCss = getRepositorySiteScormLearningContentCss();
			break;
		}
		case WIKIS:
		{
			selectedCss = getRepositorySiteWikisCss();
			break;
		}
		case PODCASTS:
		{
			selectedCss = getRepositorySitePodcastsCss();
			break;
		}
		case BLOGS:
		{
			selectedCss = getRepositorySiteBlogsCss();
			break;
		}
		case PORTFOLIO_TEMPLATES:
		{
			selectedCss = getRepositorySitePortfolioTemplatesCss();
			break;
		}
		case TESTS:
		{
			selectedCss = getRepositorySiteTestsCss();
			break;
		}
		case QUESTIONAIRES:
		{
			selectedCss = getRepositorySiteQuestionairesCss();
			break;
		}
		case RESOURCE_FOLDER:
		{
			selectedCss = getRepositorySiteResourceFolderCss();
			break;
		}
		case GLOSSARY:
		{
			selectedCss = getRepositorySiteGlossaryCss();
			break;
		}
		}
		
		return(selectedCss);
	}
	
	/**
	 * Check if the correct page is open.
	 * 
	 * @param browser
	 * @param action
	 * @return true if match otherwise false
	 */
	public boolean checkCurrentPage(Selenium browser, Object action){
		functionalUtil.idle(browser);
		
		String selectedCss = findCssClassOfAction(action);
		
		if(selectedCss == null)
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul .")
		.append(selectedCss)
		.append('.')
		.append(getRepositorySiteMenuTreeSelectedCss())
		.append(" * a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * Browse the learning resources site's navigation.
	 * 
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){ //activateMenuTreeAction(browser, action)
		functionalUtil.idle(browser);
		
		String selectedCss = findCssClassOfAction(action);
		
		if(selectedCss == null){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul .")
		.append(selectedCss)
		.append(" * a");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * Waits until course has been loaded.
	 * 
	 * @param browser
	 * @return
	 */
	private boolean waitForPageToLoadCourse(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		//FIXME:JK: this isn't very safe because there may be more than one open courses
		selectorBuffer.append("xpath=//li[contains(@class, '")
		.append(getCourseTabActiveCss())
		.append("')]//a[contains(@class, '")
		.append(getCourseTabCloseCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Opens a course by using business path.
	 * 
	 * @param browser
	 * @param key
	 * @return true on success otherwise false
	 */
	public boolean openCourse(Selenium browser, long key){
		functionalUtil.openBusinessPath(browser, functionalUtil.getDeploymentUrl() + "/url/RepositoryEntry/" + key);
		
		return(true);
	}

	/**
	 * Opens a course matching key by using the search form.
	 * 
	 * @param browser
	 * @param key
	 * @return true on success otherwise false
	 */
	public boolean openCourseWithoutBusinessPath(Selenium browser, Long key){
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			return(false);

		if(!openActionByMenuTree(browser, RepositorySiteAction.SEARCH_FORM))
			return(false);

		functionalUtil.idle(browser);
		
		//FIXME:JK: use CSS classes instead of ordinal
		int searchFormIndex = 0;

		/* open search form */
		functionalUtil.typeText(browser, SearchField.ID.getEntryCss(), key.toString());

		/* click search */
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form[")
		.append(searchFormIndex + 1)
		.append("]")
		.append("//div[@class='b_form_element']")
		.append("//a[@class='b_button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);

		/* click course */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("//form")
		.append("//tr[contains(@class,'b_first_child') and contains(@class, 'b_last_child')]")
		.append("//td[3]") //FIXME:JK: this isn't very safe
		.append("//a");

		browser.click(selectorBuffer.toString());

		waitForPageToLoadCourse(browser);
		
		return(true);
	}
	
	/**
	 * Opens a course by its title. The nth search result will be opened.
	 * 
	 * @param browser
	 * @param title
	 * @param nth
	 * @return
	 */
	public boolean openCourse(Selenium browser, String title, int nth){
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			return(false);

		if(!openActionByMenuTree(browser, RepositorySiteAction.SEARCH_FORM))
			return(false);

		functionalUtil.idle(browser);
		
		//FIXME:JK: use CSS classes instead of ordinal
		int searchFormIndex = 0;

		/* open search form */
		functionalUtil.typeText(browser, SearchField.TITLE_OF_LEARNING_RESOURCE.getEntryCss(), title);

		/* click search */
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form[")
		.append(searchFormIndex + 1)
		.append("]")
		.append("//div[@class='b_form_element']")
		.append("//a[@class='b_button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);

		/* click course */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("//form")
		.append("//tr")
		.append("//td[3]") //FIXME:JK: this isn't very safe
		.append("//a");

		browser.click(selectorBuffer.toString());

		waitForPageToLoadCourse(browser);
		
		return(true);
	}
	
	/**
	 * 
	 * @param browser
	 * @param settings
	 * @return
	 */
	public boolean modifySettings(Selenium browser, AccessSettings settings){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * 
	 * @param descriptor
	 * @return
	 */
	public String findCssClassOfCourseAlias(String descriptor){
		//TODO:JK: implement me
		
		return(null);
	}
	
	/**
	 * 
	 * @param browser
	 * @param alias
	 * @param title
	 * @param description
	 * @return
	 */
	public boolean createCourseNode(Selenium browser, String alias, String title, String description){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * Clicks create course node of given CSS class.
	 * 
	 * @param browser
	 * @param nodeCss
	 * @return
	 */
	private boolean clickCreate(Selenium browser, String nodeCss){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getToolboxContentCss())
		.append("')]//ul//li//a[contains(@class, '")
		.append(nodeCss)
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Opens the appropriate detailed view.
	 * 
	 * @param browser
	 * @param key
	 * @return
	 */
	public boolean openDetailedView(Selenium browser, Long key){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * Opens the detail view of a specified learning resource.
	 * 
	 * @param browser
	 * @param title
	 * @param nth
	 * @return true on success
	 */
	public boolean openDetailedView(Selenium browser, String title, int nth){
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			return(false);

		functionalUtil.idle(browser);
		
		//FIXME:JK: use CSS classes instead of ordinal
		int searchFormIndex = 0;

		/* open search form */
		functionalUtil.typeText(browser, SearchField.TITLE_OF_LEARNING_RESOURCE.getEntryCss(), title);

		/* click search */
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form[")
		.append(searchFormIndex + 1)
		.append("]")
		.append("//div[@class='b_form_element']")
		.append("//a[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());

		/* click course */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("//form")
		.append("//tr[")
		.append(nth)
		.append("]")
		.append("//td[6]") //FIXME:JK: this isn't very safe
		.append("//a");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * 
	 * @param browser
	 * @return the id
	 */
	private long readIdFromDetailView(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=");
		//TODO:JK: implement me		
		
		long id = Long.parseLong(browser.getText(selectorBuffer.toString()));
		
		return(id);
	}
	
	/**
	 * Clicks the appropriate action defined by parameter actionCss.
	 * 
	 * @param browser
	 * @param actionCss
	 * @return
	 */
	private boolean clickDetailedViewAction(Selenium browser, String actionCss){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//ul//li//a[contains(@class, '")
		.append(actionCss)
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Fills in title and description of a newly created repository entry.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	private boolean fillInTitleAndDescription(Selenium browser, String title, String description){
		functionalUtil.idle(browser);
		
		/* fill in wizard - title */
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("')]//input[@type='text']");
		
		browser.type(locatorBuffer.toString(), title);
		
		/* fill in wizard - description */
		functionalUtil.typeMCE(browser, description);

		/* fill in wizard - click save */
		functionalUtil.saveForm(browser, 0);
		
		return(true);
	}
	
	/**
	 * Creates a new blog in the repository.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return the learning resource's id
	 * @throws IOException
	 */
	public long createBlog(Selenium browser, String title, String description) throws IOException{
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			throw(new IOException("can't open olat site of learning resources"));
		
		/* open wizard */
		if(!clickCreate(browser, getCreateBlogCss()))
			throw(new IOException("can't open create wizard of blog"));
		
		if(!openDetailedView(browser, title, 0))
			throw(new IOException("can't open detail view"));
			
		long id = readIdFromDetailView(browser);
		
		/* fill in wizard - title and description */
		if(!fillInTitleAndDescription(browser, title, description))
			throw(new IOException("failed to fill in title and description"));
		
		/* fill in wizard - click next */
		functionalUtil.clickWizardNext(browser);
		
		/* click no, we don't want to open the editor */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("']//form//");//TODO:JK: implement me
		
		browser.click(selectorBuffer.toString());
		
		return(id);
	}
	
	/**
	 * Fills in the title and description of a newly created course.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return
	 */
	public boolean fillInRepositoryEntryPopup(Selenium browser, String title, String description){
		functionalUtil.idle(browser);
		
		/* fill in title */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getRepositoryPopupCss())
		.append("')]//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), title);
		
		/* fill in description */
		functionalUtil.typeMCE(browser, getRepositoryPopupCss(), description);
		
		/* click save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getRepositoryPopupCss())
		.append("')]//div[contains(@class, '")
		.append(getRepositorySaveDetailsCss())
		.append("')]//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')])[1]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());
		
		/* click next */
		functionalUtil.idle(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, 'b_window')]//a[contains(@class, '")
		.append(getRepositoryAddForwardCss())
		.append("')]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());

		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Creates an empty course afterwards the course editor is opened.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	public boolean createCourseUsingEditor(Selenium browser, String title, String description){
		if(!clickCreate(browser, getCreateCourseCss())){
			return(false);
		}
		
		if(!fillInRepositoryEntryPopup(browser, title, description)){
			return(false);
		}
		
		/* select course editor */
		functionalUtil.clickRadio(browser, null, NextSteps.COURSE_EDITOR.getValue());
		
		/* click next */
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getRepositoryAddTemplateForwardCss())
		.append("')]//button)[last()]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Creates a course using the wizard whereas the specified settings
	 * will applied.
	 * 
	 * @param browser
	 * @param title
	 * @param description
	 * @param element
	 * @param catalog
	 * @param publish
	 * @param access
	 * @return true on success
	 */
	public boolean createCourseUsingWizard(Selenium browser, String title, String description,
			CourseWizardElement[] element, String catalog, boolean  publish, CourseWizardAccess access){
		if(!clickCreate(browser, getCreateCourseCss())){
			return(false);
		}
		
		if(!fillInRepositoryEntryPopup(browser, title, description)){
			return(false);
		}
		
		/* select wizard */
		functionalUtil.clickRadio(browser, null, NextSteps.WIZARD.getValue());
		
		/* click next */
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getRepositoryAddTemplateForwardCss())
		.append("')]//button)[last()]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		/* select course element */
		for(CourseWizardElement current: element){
			functionalUtil.clickCheckbox(browser, null, current.getValue());
		}

		functionalUtil.idle(browser);
		
		functionalUtil.clickWizardNext(browser);
		functionalUtil.waitForPageToUnloadElement(browser, "//div[contains(@class, 'b_wizard')]//input[@type='checkbox']");
		
		/* catalog */
		if(catalog != null){
			String catalogSelectors = functionalCourseUtil.createCatalogSelectors(browser, catalog);
		
			functionalUtil.idle(browser);
			functionalUtil.waitForPageToLoadElement(browser, catalogSelectors);
			browser.click(catalogSelectors);
		}
		
		functionalUtil.clickWizardNext(browser);
		functionalUtil.waitForPageToUnloadElement(browser, "//div[contains(@class, 'b_wizard')]//div[contains(@class, 'x-tree-node')]");
		
		/* publish */
		if(!publish){
			functionalUtil.idle(browser);
			
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//div[contains(@class, 'b_wizard')]//input[@type='checkbox' and @name='")
			.append(getCourseWizardPublishCheckbox())
			.append("']");
			
			browser.click(selectorBuffer.toString());
		}
		
		if(access != null){
			functionalUtil.selectOption(browser, getCourseWizardAccessOptionId(), access.getValue());
		}
		
		functionalUtil.clickWizardFinish(browser);
		
		//TODO:JK: catch error message in case of wrong configured mail
		
		return(true);
	}

	public boolean copyRepositoryEntry(Selenium browser, String title, String description) {
		if(!clickDetailedViewAction(browser, DetailedViewAction.COPY.getActionCss())){
			return(false);
		}
		
		if(!fillInRepositoryEntryPopup(browser, title, description)){
			return(false);
		}
		
		return(true);
	}
	
	public String getRepositoryPopupCss() {
		return repositoryPopupCss;
	}

	public void setRepositoryPopupCss(String repositoryPopupCss) {
		this.repositoryPopupCss = repositoryPopupCss;
	}
	
	public String getRepositorySaveDetailsCss() {
		return repositorySaveDetailsCss;
	}

	public void setRepositorySaveDetailsCss(String repositorySaveDetailsCss) {
		this.repositorySaveDetailsCss = repositorySaveDetailsCss;
	}

	public String getRepositoryAddForwardCss() {
		return repositoryAddForwardCss;
	}

	public void setRepositoryAddForwardCss(String repositoryAddForwardCss) {
		this.repositoryAddForwardCss = repositoryAddForwardCss;
	}
	
	public String getRepositoryAddTemplateForwardCss() {
		return repositoryAddTemplateForwardCss;
	}

	public void setRepositoryAddTemplateForwardCss(
			String repositoryAddTemplateForwardCss) {
		this.repositoryAddTemplateForwardCss = repositoryAddTemplateForwardCss;
	}

	public String getCourseWizardPublishCheckbox() {
		return courseWizardPublishCheckbox;
	}

	public void setCourseWizardPublishCheckbox(String courseWizardPublishCheckbox) {
		this.courseWizardPublishCheckbox = courseWizardPublishCheckbox;
	}

	public String getCourseWizardAccessOptionId() {
		return courseWizardAccessOptionCss;
	}

	public void setCourseWizardAccessOptionId(String courseWizardAccessOptionCss) {
		this.courseWizardAccessOptionCss = courseWizardAccessOptionCss;
	}

	public String getRepositorySiteMenuTreeSelectedCss() {
		return repositorySiteMenuTreeSelectedCss;
	}

	public void setRepositorySiteMenuTreeSelectedCss(
			String repositorySitePageNavigationSelectedCss) {
		this.repositorySiteMenuTreeSelectedCss = repositorySitePageNavigationSelectedCss;
	}

	public String getRepositorySiteCatalogCss() {
		return repositorySiteCatalogCss;
	}

	public void setRepositorySiteCatalogCss(String repositorySiteCatalogCss) {
		this.repositorySiteCatalogCss = repositorySiteCatalogCss;
	}

	public String getRepositorySiteSearchFormCss() {
		return repositorySiteSearchFormCss;
	}

	public void setRepositorySiteSearchFormCss(
			String repositorySiteSearchFormCss) {
		this.repositorySiteSearchFormCss = repositorySiteSearchFormCss;
	}

	public String getRepositorySiteMyEntriesCss() {
		return repositorySiteMyEntriesCss;
	}

	public void setRepositorySiteMyEntriesCss(
			String repositorySiteMyEntriesCss) {
		this.repositorySiteMyEntriesCss = repositorySiteMyEntriesCss;
	}

	public String getRepositorySiteMyCoursesCss() {
		return repositorySiteMyCoursesCss;
	}

	public void setRepositorySiteMyCoursesCss(
			String repositorySiteMyCoursesCss) {
		this.repositorySiteMyCoursesCss = repositorySiteMyCoursesCss;
	}

	public String getRepositorySiteMySupervisedCoursesCss() {
		return repositorySiteMySupervisedCoursesCss;
	}

	public void setRepositorySiteMySupervisedCoursesCss(
			String repositorySiteMySupervisedCoursesCss) {
		this.repositorySiteMySupervisedCoursesCss = repositorySiteMySupervisedCoursesCss;
	}

	public String getRepositorySiteCoursesCss() {
		return repositorySiteCoursesCss;
	}

	public void setRepositorySiteCoursesCss(String repositorySiteCoursesCss) {
		this.repositorySiteCoursesCss = repositorySiteCoursesCss;
	}

	public String getRepositorySiteCPLearningContentCss() {
		return repositorySiteCPLearningContentCss;
	}

	public void setRepositorySiteCPLearningContentCss(
			String repositorySiteCPLearningContentCss) {
		this.repositorySiteCPLearningContentCss = repositorySiteCPLearningContentCss;
	}

	public String getRepositorySiteScormLearningContentCss() {
		return repositorySiteScormLearningContentCss;
	}

	public void setRepositorySiteScormLearningContentCss(
			String repositorySiteScormLearningContentCss) {
		this.repositorySiteScormLearningContentCss = repositorySiteScormLearningContentCss;
	}

	public String getRepositorySiteWikisCss() {
		return repositorySiteWikisCss;
	}

	public void setRepositorySiteWikisCss(String repositorySiteWikisCss) {
		this.repositorySiteWikisCss = repositorySiteWikisCss;
	}

	public String getRepositorySitePodcastsCss() {
		return repositorySitePodcastsCss;
	}

	public void setRepositorySitePodcastsCss(String repositorySitePodcastsCss) {
		this.repositorySitePodcastsCss = repositorySitePodcastsCss;
	}

	public String getRepositorySiteBlogsCss() {
		return repositorySiteBlogsCss;
	}

	public void setRepositorySiteBlogsCss(String repositorySiteBlogsCss) {
		this.repositorySiteBlogsCss = repositorySiteBlogsCss;
	}

	public String getRepositorySitePortfolioTemplatesCss() {
		return repositorySitePortfolioTemplatesCss;
	}

	public void setRepositorySitePortfolioTemplatesCss(
			String repositorySitePortfolioTemplatesCss) {
		this.repositorySitePortfolioTemplatesCss = repositorySitePortfolioTemplatesCss;
	}

	public String getRepositorySiteTestsCss() {
		return repositorySiteTestsCss;
	}

	public void setRepositorySiteTestsCss(String repositorySiteTestsCss) {
		this.repositorySiteTestsCss = repositorySiteTestsCss;
	}

	public String getRepositorySiteQuestionairesCss() {
		return repositorySiteQuestionairesCss;
	}

	public void setRepositorySiteQuestionairesCss(
			String repositorySiteQuestionairesCss) {
		this.repositorySiteQuestionairesCss = repositorySiteQuestionairesCss;
	}

	public String getRepositorySiteResourceFolderCss() {
		return repositorySiteResourceFolderCss;
	}

	public void setRepositorySiteResourceFolderCss(
			String repositorySiteResourceFolderCss) {
		this.repositorySiteResourceFolderCss = repositorySiteResourceFolderCss;
	}

	public String getRepositorySiteGlossaryCss() {
		return repositorySiteGlossaryCss;
	}

	public void setRepositorySiteGlossaryCss(String repositorySiteGlossaryCss) {
		this.repositorySiteGlossaryCss = repositorySiteGlossaryCss;
	}

	public String getCatalogNavigationCss() {
		return catalogNavigationCss;
	}

	public void setCatalogNavigationCss(String catalogNavigationCss) {
		this.catalogNavigationCss = catalogNavigationCss;
	}

	public String getCatalogAddSubcategoryCss() {
		return catalogAddSubcategoryCss;
	}

	public void setCatalogAddSubcategoryCss(String catalogAddSubcategoryCss) {
		this.catalogAddSubcategoryCss = catalogAddSubcategoryCss;
	}

	public String getCatalogAddLearningResourceCss() {
		return catalogAddLearningResourceCss;
	}

	public void setCatalogAddLearningResourceCss(
			String catalogAddLearningResourceCss) {
		this.catalogAddLearningResourceCss = catalogAddLearningResourceCss;
	}

	public String getCatalogAddSubcategoryPopupCss() {
		return catalogAddSubcategoryPopupCss;
	}

	public void setCatalogAddSubcategoryPopupCss(
			String catalogAddSubcategoryPopupCss) {
		this.catalogAddSubcategoryPopupCss = catalogAddSubcategoryPopupCss;
	}

	public String getCatalogSubcategoryListCss() {
		return catalogSubcategoryListCss;
	}

	public void setCatalogSubcategoryListCss(String catalogSubcategoryListCss) {
		this.catalogSubcategoryListCss = catalogSubcategoryListCss;
	}

	public String getCatalogSubcategoryIconCss() {
		return catalogSubcategoryIconCss;
	}

	public void setCatalogSubcategoryIconCss(String catalogSubcategoryIconCss) {
		this.catalogSubcategoryIconCss = catalogSubcategoryIconCss;
	}

	public String getCourseModuleIconCss() {
		return courseModuleIconCss;
	}

	public void setCourseModuleIconCss(String courseModuleIconCss) {
		this.courseModuleIconCss = courseModuleIconCss;
	}

	public String getImportCourseCss() {
		return importCourseCss;
	}

	public void setImportCourseCss(String importCourseCss) {
		this.importCourseCss = importCourseCss;
	}

	public String getImportCPLearningContentCss() {
		return importCPLearningContentCss;
	}

	public void setImportCPLearningContentCss(String importCPLearningContentCss) {
		this.importCPLearningContentCss = importCPLearningContentCss;
	}

	public String getImportScormCss() {
		return importScormCss;
	}

	public void setImportScormCss(String importScormCss) {
		this.importScormCss = importScormCss;
	}

	public String getImportWikiCss() {
		return importWikiCss;
	}

	public void setImportWikiCss(String importWikiCss) {
		this.importWikiCss = importWikiCss;
	}

	public String getImportPodcastCss() {
		return importPodcastCss;
	}

	public void setImportPodcastCss(String importPodcastCss) {
		this.importPodcastCss = importPodcastCss;
	}

	public String getImportBlogCss() {
		return importBlogCss;
	}

	public void setImportBlogCss(String importBlogCss) {
		this.importBlogCss = importBlogCss;
	}

	public String getImportTestCss() {
		return importTestCss;
	}

	public void setImportTestCss(String importTestCss) {
		this.importTestCss = importTestCss;
	}

	public String getImportQuestionaireCss() {
		return importQuestionaireCss;
	}

	public void setImportQuestionaireCss(String importQuestionaireCss) {
		this.importQuestionaireCss = importQuestionaireCss;
	}

	public String getImportGlossaryCss() {
		return importGlossaryCss;
	}

	public void setImportGlossaryCss(String importGlossaryCss) {
		this.importGlossaryCss = importGlossaryCss;
	}

	public String getImportOtherCss() {
		return importOtherCss;
	}

	public void setImportOtherCss(String importOtherCss) {
		this.importOtherCss = importOtherCss;
	}

	public String getCreateCourseCss() {
		return createCourseCss;
	}

	public void setCreateCourseCss(String createCourseCss) {
		this.createCourseCss = createCourseCss;
	}

	public String getCreateCPLearningContentCss() {
		return createCPLearningContentCss;
	}

	public void setCreateCPLearningContentCss(String createCPLearningContentCss) {
		this.createCPLearningContentCss = createCPLearningContentCss;
	}

	public String getCreateWikiCss() {
		return createWikiCss;
	}

	public void setCreateWikiCss(String createWikiCss) {
		this.createWikiCss = createWikiCss;
	}

	public String getCreatePodcastCss() {
		return createPodcastCss;
	}

	public void setCreatePodcastCss(String createPodcastCss) {
		this.createPodcastCss = createPodcastCss;
	}

	public String getCreateBlogCss() {
		return createBlogCss;
	}

	public void setCreateBlogCss(String createBlogCss) {
		this.createBlogCss = createBlogCss;
	}

	public String getCreatePortfolioCss() {
		return createPortfolioCss;
	}

	public void setCreatePortfolioCss(String createPortfolioCss) {
		this.createPortfolioCss = createPortfolioCss;
	}

	public String getCreateTestCss() {
		return createTestCss;
	}

	public void setCreateTestCss(String createTestCss) {
		this.createTestCss = createTestCss;
	}

	public String getCreateQuestionaireCss() {
		return createQuestionaireCss;
	}

	public void setCreateQuestionaireCss(String createQuestionaireCss) {
		this.createQuestionaireCss = createQuestionaireCss;
	}

	public String getCreateFolderCss() {
		return createFolderCss;
	}

	public void setCreateFolderCss(String createFolderCss) {
		this.createFolderCss = createFolderCss;
	}

	public String getCreateGlossaryCss() {
		return createGlossaryCss;
	}

	public void setCreateGlossaryCss(String createGlossaryCss) {
		this.createGlossaryCss = createGlossaryCss;
	}

	public String getToolboxContentCss() {
		return toolboxContentCss;
	}

	public void setToolboxContentCss(String toolboxContentCss) {
		this.toolboxContentCss = toolboxContentCss;
	}

	public String getToolboxCourseCss() {
		return toolboxCourseCss;
	}

	public void setToolboxCourseCss(String toolboxCourseCss) {
		this.toolboxCourseCss = toolboxCourseCss;
	}

	public String getToolboxContentPackageCss() {
		return toolboxContentPackageCss;
	}

	public void setToolboxContentPackageCss(String toolboxContentPackageCss) {
		this.toolboxContentPackageCss = toolboxContentPackageCss;
	}

	public String getToolboxWikiCss() {
		return toolboxWikiCss;
	}

	public void setToolboxWikiCss(String toolboxWikiCss) {
		this.toolboxWikiCss = toolboxWikiCss;
	}

	public String getToolboxPodcastCss() {
		return toolboxPodcastCss;
	}

	public void setToolboxPodcastCss(String toolboxPodcastCss) {
		this.toolboxPodcastCss = toolboxPodcastCss;
	}

	public String getToolboxBlogCss() {
		return toolboxBlogCss;
	}

	public void setToolboxBlogCss(String toolboxBlogCss) {
		this.toolboxBlogCss = toolboxBlogCss;
	}

	public String getToolboxPortfolioCss() {
		return toolboxPortfolioCss;
	}

	public void setToolboxPortfolioCss(String toolboxPortfolioCss) {
		this.toolboxPortfolioCss = toolboxPortfolioCss;
	}

	public String getToolboxIQTestCss() {
		return toolboxIQTestCss;
	}

	public void setToolboxIQTestCss(String toolboxIQTestCss) {
		this.toolboxIQTestCss = toolboxIQTestCss;
	}

	public String getToolboxQuestionnaireCss() {
		return toolboxQuestionnaireCss;
	}

	public void setToolboxQuestionnaireCss(String toolboxQuestionnaireCss) {
		this.toolboxQuestionnaireCss = toolboxQuestionnaireCss;
	}

	public String getToolboxSharedfolderCss() {
		return toolboxSharedfolderCss;
	}

	public void setToolboxSharedfolderCss(String toolboxSharedfolderCss) {
		this.toolboxSharedfolderCss = toolboxSharedfolderCss;
	}

	public String getToolboxGlossaryCss() {
		return toolboxGlossaryCss;
	}

	public void setToolboxGlossaryCss(String toolboxGlossaryCss) {
		this.toolboxGlossaryCss = toolboxGlossaryCss;
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

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}

	public FunctionalCourseUtil getFunctionalCourseUtil() {
		return functionalCourseUtil;
	}

	public void setFunctionalCourseUtil(FunctionalCourseUtil functionalCourseUtil) {
		this.functionalCourseUtil = functionalCourseUtil;
	}
}
