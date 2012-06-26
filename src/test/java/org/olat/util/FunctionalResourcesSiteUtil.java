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
	
	public final static String LEARNING_RESOURCES_CATALOG = "";
	public final static String LEARNING_RESOURCES_SEARCH_FORM = "";
	public final static String LEARNING_RESOURCES_MY_ENTRIES = "";
	public final static String LEARNING_RESOURCES_MY_COURSES = "";
	public final static String LEARNING_RESOURCES_MY_SUPERVISED_COURSES = "";
	public final static String LEARNING_RESOURCES_COURSES = "";
	public final static String LEARNING_RESOURCES_CP_LEARNING_CONTENT = "";
	public final static String LEARNING_RESOURCES_SCORM_LEARNING_CONTENT = "";
	public final static String LEARNING_RESOURCES_WIKIS = "";
	public final static String LEARNING_RESOURCES_PODCASTS = "";
	public final static String LEARNING_RESOURCES_BLOGS = "";
	public final static String LEARNING_RESOURCES_PORTFOLIO_TEMPLATE = "";
	public final static String LEARNING_RESOURCES_TESTS = "";
	public final static String LEARNING_RESOURCES_QUESTIONAIRES = "";
	public final static String LEARNING_RESOURCES_RESOURCE_FOLDER = "";
	public final static String LEARNING_RESOURCES_GLOSSARY = "";
	
	public enum Column {
		AC,
		TYPE,
		TITLE_OF_LEARNING_RESOURCE,
		AUTHOR,
		ACCESS,
		CREATED,
		LAST_ACCESS,
		DETAILED_VIEW,
	}
	
	private String learningResourcesCatalog;
	private String learningResourcesSearchForm;
	private String learningResourcesMyEntries;
	private String learningResourcesMyCourses;
	private String learningResourcesMySupervisedCourses;
	private String learningResourcesCourses;
	private String learningResourcesCPLearningContent;
	private String learningResourcesScormLearningContent;
	private String learningResourcesWikis;
	private String learningResourcesPodcasts;
	private String learningResourcesBlogs;
	private String learningResourcesPortfolioTemplates;
	private String learningResourcesTests;
	private String learningResourcesQuestionaires;
	private String learningResourcesResourceFolder;
	private String learningResourcesGlossary;
	
	
	private FunctionalUtil functionalUtil;
	
	
	public FunctionalResourcesSiteUtil(FunctionalUtil functionalUtil){
		setFunctionalUtil(functionalUtil);
	}
	
	public boolean displayColumns(Selenium browser, List<Column> column){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param title
	 * @param key
	 * @return
	 * 
	 * Opens a course matching title and key.
	 */
	public boolean openCourse(Selenium browser, String title, Long key){
		//TODO:JK: implement me
		
		
		return(true);
	}
	
	public String getLearningResourcesCatalog() {
		return learningResourcesCatalog;
	}

	public void setLearningResourcesCatalog(String learningResourcesCatalog) {
		this.learningResourcesCatalog = learningResourcesCatalog;
	}

	public String getLearningResourcesSearchForm() {
		return learningResourcesSearchForm;
	}

	public void setLearningResourcesSearchForm(String learningResourcesSearchForm) {
		this.learningResourcesSearchForm = learningResourcesSearchForm;
	}

	public String getLearningResourcesMyEntries() {
		return learningResourcesMyEntries;
	}

	public void setLearningResourcesMyEntries(String learningResourcesMyEntries) {
		this.learningResourcesMyEntries = learningResourcesMyEntries;
	}

	public String getLearningResourcesMyCourses() {
		return learningResourcesMyCourses;
	}

	public void setLearningResourcesMyCourses(String learningResourcesMyCourses) {
		this.learningResourcesMyCourses = learningResourcesMyCourses;
	}

	public String getLearningResourcesMySupervisedCourses() {
		return learningResourcesMySupervisedCourses;
	}

	public void setLearningResourcesMySupervisedCourses(
			String learningResourcesMySupervisedCourses) {
		this.learningResourcesMySupervisedCourses = learningResourcesMySupervisedCourses;
	}

	public String getLearningResourcesCourses() {
		return learningResourcesCourses;
	}

	public void setLearningResourcesCourses(String learningResourcesCourses) {
		this.learningResourcesCourses = learningResourcesCourses;
	}

	public String getLearningResourcesCPLearningContent() {
		return learningResourcesCPLearningContent;
	}

	public void setLearningResourcesCPLearningContent(
			String learningResourcesCPLearningContent) {
		this.learningResourcesCPLearningContent = learningResourcesCPLearningContent;
	}

	public String getLearningResourcesScormLearningContent() {
		return learningResourcesScormLearningContent;
	}

	public void setLearningResourcesScormLearningContent(
			String learningResourcesScormLearningContent) {
		this.learningResourcesScormLearningContent = learningResourcesScormLearningContent;
	}

	public String getLearningResourcesWikis() {
		return learningResourcesWikis;
	}

	public void setLearningResourcesWikis(String learningResourcesWikis) {
		this.learningResourcesWikis = learningResourcesWikis;
	}

	public String getLearningResourcesPodcasts() {
		return learningResourcesPodcasts;
	}

	public void setLearningResourcesPodcasts(String learningResourcesPodcasts) {
		this.learningResourcesPodcasts = learningResourcesPodcasts;
	}

	public String getLearningResourcesBlogs() {
		return learningResourcesBlogs;
	}

	public void setLearningResourcesBlogs(String learningResourcesBlogs) {
		this.learningResourcesBlogs = learningResourcesBlogs;
	}

	public String getLearningResourcesPortfolioTemplates() {
		return learningResourcesPortfolioTemplates;
	}

	public void setLearningResourcesPortfolioTemplates(
			String learningResourcesPortfolioTemplates) {
		this.learningResourcesPortfolioTemplates = learningResourcesPortfolioTemplates;
	}

	public String getLearningResourcesTests() {
		return learningResourcesTests;
	}

	public void setLearningResourcesTests(String learningResourcesTests) {
		this.learningResourcesTests = learningResourcesTests;
	}

	public String getLearningResourcesQuestionaires() {
		return learningResourcesQuestionaires;
	}

	public void setLearningResourcesQuestionaires(
			String learningResourcesQuestionaires) {
		this.learningResourcesQuestionaires = learningResourcesQuestionaires;
	}

	public String getLearningResourcesResourceFolder() {
		return learningResourcesResourceFolder;
	}

	public void setLearningResourcesResourceFolder(
			String learningResourcesResourceFolder) {
		this.learningResourcesResourceFolder = learningResourcesResourceFolder;
	}

	public String getLearningResourcesGlossary() {
		return learningResourcesGlossary;
	}

	public void setLearningResourcesGlossary(String learningResourcesGlossary) {
		this.learningResourcesGlossary = learningResourcesGlossary;
	}

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
