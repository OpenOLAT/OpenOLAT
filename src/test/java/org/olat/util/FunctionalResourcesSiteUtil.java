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
public class FunctionalResourcesSiteUtil {	
	
	public final static String LEARNING_RESOURCES_PAGE_NAVIGATION_SELECTED_CSS = "b_tree_selected";
	
	public final static String LEARNING_RESOURCES_CATALOG_CSS = "o_sel_repo_catalog";
	public final static String LEARNING_RESOURCES_SEARCH_FORM_CSS = "o_sel_repo_search_generic";
	public final static String LEARNING_RESOURCES_MY_ENTRIES_CSS = "o_sel_repo_my";
	public final static String LEARNING_RESOURCES_MY_COURSES_CSS = "o_sel_repo_my_student";
	public final static String LEARNING_RESOURCES_MY_SUPERVISED_COURSES_CSS = "o_sel_repo_my_teacher";
	public final static String LEARNING_RESOURCES_COURSES_CSS = "o_sel_repo_course";
	public final static String LEARNING_RESOURCES_CP_LEARNING_CONTENT_CSS = "o_sel_repo_cp";
	public final static String LEARNING_RESOURCES_SCORM_LEARNING_CONTENT_CSS = "o_sel_repo_scorm";
	public final static String LEARNING_RESOURCES_WIKIS_CSS = "o_sel_repo_wiki";
	public final static String LEARNING_RESOURCES_PODCASTS_CSS = "o_sel_repo_podcast";
	public final static String LEARNING_RESOURCES_BLOGS_CSS = "o_sel_repo_blog";
	public final static String LEARNING_RESOURCES_PORTFOLIO_TEMPLATES_CSS = "o_sel_repo_portfolio";
	public final static String LEARNING_RESOURCES_TESTS_CSS = "o_sel_repo_test";
	public final static String LEARNING_RESOURCES_QUESTIONAIRES_CSS = "o_sel_repo_survey";
	public final static String LEARNING_RESOURCES_RESOURCE_FOLDER_CSS = "o_sel_repo_sharefolder";
	public final static String LEARNING_RESOURCES_GLOSSARY_CSS = "o_sel_repo_glossary";
	
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
	
	public enum LearningResourcesPage {
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
	
	private String learningResourcesPageNavigationSelectedCss;
	
	private String learningResourcesCatalogCss;
	private String learningResourcesSearchFormCss;
	private String learningResourcesMyEntriesCss;
	private String learningResourcesMyCoursesCss;
	private String learningResourcesMySupervisedCoursesCss;
	private String learningResourcesCoursesCss;
	private String learningResourcesCPLearningContentCss;
	private String learningResourcesScormLearningContentCss;
	private String learningResourcesWikisCss;
	private String learningResourcesPodcastsCss;
	private String learningResourcesBlogsCss;
	private String learningResourcesPortfolioTemplatesCss;
	private String learningResourcesTestsCss;
	private String learningResourcesQuestionairesCss;
	private String learningResourcesResourceFolderCss;
	private String learningResourcesGlossaryCss;
	
	
	private FunctionalUtil functionalUtil;
	
	
	public FunctionalResourcesSiteUtil(FunctionalUtil functionalUtil){
		setLearningResourcesPageNavigationSelectedCss(LEARNING_RESOURCES_PAGE_NAVIGATION_SELECTED_CSS);
		
		setLearningResourcesCatalogCss(LEARNING_RESOURCES_CATALOG_CSS);
		setLearningResourcesSearchFormCss(LEARNING_RESOURCES_SEARCH_FORM_CSS);
		setLearningResourcesMyEntriesCss(LEARNING_RESOURCES_MY_ENTRIES_CSS);
		setLearningResourcesMyCoursesCss(LEARNING_RESOURCES_MY_COURSES_CSS);
		setLearningResourcesMySupervisedCoursesCss(LEARNING_RESOURCES_MY_SUPERVISED_COURSES_CSS);
		setLearningResourcesCoursesCss(LEARNING_RESOURCES_COURSES_CSS);
		setLearningResourcesCPLearningContentCss(LEARNING_RESOURCES_CP_LEARNING_CONTENT_CSS);
		setLearningResourcesScormLearningContentCss(LEARNING_RESOURCES_SCORM_LEARNING_CONTENT_CSS);
		setLearningResourcesWikisCss(LEARNING_RESOURCES_WIKIS_CSS);
		setLearningResourcesPodcastsCss(LEARNING_RESOURCES_PODCASTS_CSS);
		setLearningResourcesBlogsCss(LEARNING_RESOURCES_BLOGS_CSS);
		setLearningResourcesPortfolioTemplatesCss(LEARNING_RESOURCES_PORTFOLIO_TEMPLATES_CSS);
		setLearningResourcesTestsCss(LEARNING_RESOURCES_TESTS_CSS);
		setLearningResourcesQuestionairesCss(LEARNING_RESOURCES_QUESTIONAIRES_CSS);
		setLearningResourcesResourceFolderCss(LEARNING_RESOURCES_RESOURCE_FOLDER_CSS);
		setLearningResourcesGlossaryCss(LEARNING_RESOURCES_GLOSSARY_CSS);
		
		setFunctionalUtil(functionalUtil);
	}
	
	public boolean displayColumns(Selenium browser, List<Column> column){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param page
	 * @return the matching CSS class
	 * 
	 * Find the corresponding CSS class for page.
	 */
	public String findCssClassOfPage(Object page){
		if(page == null)
			return(null);
		
		String selectedCss = null;
		
		switch((LearningResourcesPage) page){
		case CATALOG:
		{
			selectedCss = getLearningResourcesCatalogCss();
			break;
		}
		case SEARCH_FORM:
		{
			selectedCss = getLearningResourcesSearchFormCss();
			break;
		}
		case MY_ENTRIES:
		{
			selectedCss = getLearningResourcesMyEntriesCss();
			break;
		}
		case MY_COURSES:
		{
			selectedCss = getLearningResourcesMyCoursesCss();
			break;
		}
		case MY_SUPERVISED_COURSES:
		{
			selectedCss = getLearningResourcesMySupervisedCoursesCss();
			break;
		}
		case COURSES:
		{
			selectedCss = getLearningResourcesCoursesCss();
			break;
		}
		case CP_LEARNING_CONTENT:
		{
			selectedCss = getLearningResourcesCPLearningContentCss();
			break;
		}
		case SCORM_LEARNING_CONTENT:
		{
			selectedCss = getLearningResourcesScormLearningContentCss();
			break;
		}
		case WIKIS:
		{
			selectedCss = getLearningResourcesWikisCss();
			break;
		}
		case PODCASTS:
		{
			selectedCss = getLearningResourcesPodcastsCss();
			break;
		}
		case BLOGS:
		{
			selectedCss = getLearningResourcesBlogsCss();
			break;
		}
		case PORTFOLIO_TEMPLATES:
		{
			selectedCss = getLearningResourcesPortfolioTemplatesCss();
			break;
		}
		case TESTS:
		{
			selectedCss = getLearningResourcesTestsCss();
			break;
		}
		case QUESTIONAIRES:
		{
			selectedCss = getLearningResourcesQuestionairesCss();
			break;
		}
		case RESOURCE_FOLDER:
		{
			selectedCss = getLearningResourcesResourceFolderCss();
			break;
		}
		case GLOSSARY:
		{
			selectedCss = getLearningResourcesGlossaryCss();
			break;
		}
		}
		
		return(selectedCss);
	}
	
	/**
	 * @param browser
	 * @param page
	 * @return true if match otherwise false
	 * 
	 * Check if the correct page is open.
	 */
	public boolean checkCurrentPage(Selenium browser, Object page){
		String selectedCss = findCssClassOfPage(page);
		
		if(selectedCss == null)
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul .")
		.append(selectedCss)
		.append('.')
		.append(getLearningResourcesPageNavigationSelectedCss())
		.append(" * a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param page
	 * @return true on success otherwise false
	 * 
	 * Browse the learning resources site's navigation.
	 */
	public boolean openPageByNavigation(Selenium browser, Object page){
		String selectedCss = findCssClassOfPage(page);
		
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
		
		if(!openPageByNavigation(browser, LearningResourcesPage.SEARCH_FORM))
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

	public String getLearningResourcesPageNavigationSelectedCss() {
		return learningResourcesPageNavigationSelectedCss;
	}

	public void setLearningResourcesPageNavigationSelectedCss(
			String learningResourcesPageNavigationSelectedCss) {
		this.learningResourcesPageNavigationSelectedCss = learningResourcesPageNavigationSelectedCss;
	}

	public String getLearningResourcesCatalogCss() {
		return learningResourcesCatalogCss;
	}

	public void setLearningResourcesCatalogCss(String learningResourcesCatalogCss) {
		this.learningResourcesCatalogCss = learningResourcesCatalogCss;
	}

	public String getLearningResourcesSearchFormCss() {
		return learningResourcesSearchFormCss;
	}

	public void setLearningResourcesSearchFormCss(
			String learningResourcesSearchFormCss) {
		this.learningResourcesSearchFormCss = learningResourcesSearchFormCss;
	}

	public String getLearningResourcesMyEntriesCss() {
		return learningResourcesMyEntriesCss;
	}

	public void setLearningResourcesMyEntriesCss(
			String learningResourcesMyEntriesCss) {
		this.learningResourcesMyEntriesCss = learningResourcesMyEntriesCss;
	}

	public String getLearningResourcesMyCoursesCss() {
		return learningResourcesMyCoursesCss;
	}

	public void setLearningResourcesMyCoursesCss(
			String learningResourcesMyCoursesCss) {
		this.learningResourcesMyCoursesCss = learningResourcesMyCoursesCss;
	}

	public String getLearningResourcesMySupervisedCoursesCss() {
		return learningResourcesMySupervisedCoursesCss;
	}

	public void setLearningResourcesMySupervisedCoursesCss(
			String learningResourcesMySupervisedCoursesCss) {
		this.learningResourcesMySupervisedCoursesCss = learningResourcesMySupervisedCoursesCss;
	}

	public String getLearningResourcesCoursesCss() {
		return learningResourcesCoursesCss;
	}

	public void setLearningResourcesCoursesCss(String learningResourcesCoursesCss) {
		this.learningResourcesCoursesCss = learningResourcesCoursesCss;
	}

	public String getLearningResourcesCPLearningContentCss() {
		return learningResourcesCPLearningContentCss;
	}

	public void setLearningResourcesCPLearningContentCss(
			String learningResourcesCPLearningContentCss) {
		this.learningResourcesCPLearningContentCss = learningResourcesCPLearningContentCss;
	}

	public String getLearningResourcesScormLearningContentCss() {
		return learningResourcesScormLearningContentCss;
	}

	public void setLearningResourcesScormLearningContentCss(
			String learningResourcesScormLearningContentCss) {
		this.learningResourcesScormLearningContentCss = learningResourcesScormLearningContentCss;
	}

	public String getLearningResourcesWikisCss() {
		return learningResourcesWikisCss;
	}

	public void setLearningResourcesWikisCss(String learningResourcesWikisCss) {
		this.learningResourcesWikisCss = learningResourcesWikisCss;
	}

	public String getLearningResourcesPodcastsCss() {
		return learningResourcesPodcastsCss;
	}

	public void setLearningResourcesPodcastsCss(String learningResourcesPodcastsCss) {
		this.learningResourcesPodcastsCss = learningResourcesPodcastsCss;
	}

	public String getLearningResourcesBlogsCss() {
		return learningResourcesBlogsCss;
	}

	public void setLearningResourcesBlogsCss(String learningResourcesBlogsCss) {
		this.learningResourcesBlogsCss = learningResourcesBlogsCss;
	}

	public String getLearningResourcesPortfolioTemplatesCss() {
		return learningResourcesPortfolioTemplatesCss;
	}

	public void setLearningResourcesPortfolioTemplatesCss(
			String learningResourcesPortfolioTemplatesCss) {
		this.learningResourcesPortfolioTemplatesCss = learningResourcesPortfolioTemplatesCss;
	}

	public String getLearningResourcesTestsCss() {
		return learningResourcesTestsCss;
	}

	public void setLearningResourcesTestsCss(String learningResourcesTestsCss) {
		this.learningResourcesTestsCss = learningResourcesTestsCss;
	}

	public String getLearningResourcesQuestionairesCss() {
		return learningResourcesQuestionairesCss;
	}

	public void setLearningResourcesQuestionairesCss(
			String learningResourcesQuestionairesCss) {
		this.learningResourcesQuestionairesCss = learningResourcesQuestionairesCss;
	}

	public String getLearningResourcesResourceFolderCss() {
		return learningResourcesResourceFolderCss;
	}

	public void setLearningResourcesResourceFolderCss(
			String learningResourcesResourceFolderCss) {
		this.learningResourcesResourceFolderCss = learningResourcesResourceFolderCss;
	}

	public String getLearningResourcesGlossaryCss() {
		return learningResourcesGlossaryCss;
	}

	public void setLearningResourcesGlossaryCss(String learningResourcesGlossaryCss) {
		this.learningResourcesGlossaryCss = learningResourcesGlossaryCss;
	}

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
