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

import java.util.List;

import com.thoughtworks.selenium.Selenium;

/**
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

	public final static String TITLE_OF_LEARNING_RESOURCE_CSS = "";
	public final static String AUTHOR_CSS = "";
	public final static String DESCRIPTION_CSS = "";
	public final static String ID_CSS = "";
	public final static String LIMIT_SEARCH_CSS = "";
	
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
	public boolean activateMenuTreeAction(Selenium browser, Object action){ //activateMenuTreeAction(browser, action)
		String selectedCss = findCssClassOfAction(action);
		
		if(selectedCss == null){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul .")
		.append(selectedCss)
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param key
	 * @return true on success otherwise false
	 * 
	 * Opens a course matching key by using the search form.
	 */
	public boolean openCourse(Selenium browser, Long key){
		
		if(!activateMenuTreeAction(browser, RepositorySiteAction.SEARCH_FORM))
			return(false);
		
		//FIXME:JK: use CSS classes instead of ordinal
		int searchFormIndex = 0;
		
		/* open search form */
		functionalUtil.typeText(browser, SearchField.ID.getEntryCss(), key.toString());
		
		/* click search */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form[")
		.append(searchFormIndex)
		.append("]")
		.append("//div[@class='b_form_element']")
		.append("a[@class='b_button']");
		
		browser.click(selectorBuffer.toString());
		
		/* click course */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("//form")
		.append("//tr[@class='b_first_child b_last_child']")
		.append("//a");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
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

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
