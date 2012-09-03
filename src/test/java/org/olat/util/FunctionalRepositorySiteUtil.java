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
import java.util.List;

import org.junit.Assert;
import org.olat.util.FunctionalUtil.OlatSite;
import org.olat.util.FunctionalUtil.WaitLimitAttribute;

import com.thoughtworks.selenium.Selenium;

/**
 * Description: <br>
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalRepositorySiteUtil {	
	
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
	
	public final static String CP_LEARNING_CONTENT_ALIAS = "cp";
	public final static String WIKI_ALIAS = "wiki";
	public final static String PODCAST_ALIAS = "podcast";
	public final static String BLOG_ALIAS = "blog";
	public final static String PORTFOLIO_ALIAS = "portfolio";
	public final static String IQTEST_ALIAS = "iqtest";
	public final static String QUESTIONAIRE_ALIAS = "iqsurv";
	public final static String RESOURCE_FOLDER_ALIAS = "bc";
	public final static String GLOSSARY_ALIAS = "glossary";
	
	public enum CourseNode {
		
	}

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
	
	private FunctionalUtil functionalUtil;
	
	
	public FunctionalRepositorySiteUtil(FunctionalUtil functionalUtil){
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
		
		setFunctionalUtil(functionalUtil);
	}
	
	public boolean displayColumns(Selenium browser, List<Column> column){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param action
	 * @return the matching CSS class
	 * 
	 * Find the corresponding CSS class for page.
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
	 * @param browser
	 * @param action
	 * @return true if match otherwise false
	 * 
	 * Check if the correct page is open.
	 */
	public boolean checkCurrentPage(Selenium browser, Object action){
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
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 * 
	 * Browse the learning resources site's navigation.
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){ //activateMenuTreeAction(browser, action)
		String selectedCss = findCssClassOfAction(action);
		
		if(selectedCss == null){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul .")
		.append(selectedCss)
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param key
	 * @return true on success otherwise false
	 * 
	 * Opens a course by using business path.
	 */
	public boolean openCourse(Selenium browser, long key){
		browser.open(functionalUtil.getDeploymentUrl() + "url/RepositoryEntry/" + key);
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}

	/**
	 * @param browser
	 * @param key
	 * @return true on success otherwise false
	 * 
	 * Opens a course matching key by using the search form.
	 */
	public boolean openCourseWithoutBusinessPath(Selenium browser, Long key){
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			return(false);

		if(!openActionByMenuTree(browser, RepositorySiteAction.SEARCH_FORM))
			return(false);

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
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("//form")
		.append("//tr[contains(@class,'b_first_child') and contains(@class, 'b_last_child')]")
		.append("//td[3]") //FIXME:JK: this isn't very safe
		.append("//a");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	public String findCssClassOfCourseAlias(String descriptor){
		//TODO:JK: implement me
		
		return(null);
	}
	
	public boolean createCourseNode(Selenium browser, String alias, String title, String description){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param nodeCss
	 * @return
	 * 
	 * Clicks create course node of given CSS class.
	 */
	private boolean clickCreate(Selenium browser, String nodeCss){
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
	 * @param browser
	 * @param title
	 * @param nth
	 * @return true on success
	 * 
	 * Opens the detail view of a specified learning resource.
	 */
	public boolean openDetailView(Selenium browser, String title, int nth){
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			return(false);
		
		//FIXME:JK: use CSS classes instead of ordinal
		int searchFormIndex = 0;

		/* open search form */
		functionalUtil.typeText(browser, SearchField.ID.getEntryCss(), title);

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
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("//form")
		.append("//tr[")
		.append(nth)
		.append("]")
		.append("//td[6]") //FIXME:JK: this isn't very safe
		.append("//a");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
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
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 */
	private boolean fillInTitleAndDescription(Selenium browser, String title, String description){
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
	 * @param browser
	 * @param title
	 * @param description
	 * @return the learning resource's id
	 * @throws IOException
	 * 
	 * Creates a new blog in the repository.
	 */
	public long createBlog(Selenium browser, String title, String description) throws IOException{
		if(!functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES))
			throw(new IOException("can't open olat site of learning resources"));
		
		/* open wizard */
		if(!clickCreate(browser, getToolboxBlogCss()))
			throw(new IOException("can't open create wizard of blog"));
		
		if(!openDetailView(browser, title, 0))
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

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
