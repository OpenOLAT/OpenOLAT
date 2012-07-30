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

import com.thoughtworks.selenium.Selenium;

/**
 * Description: <br>
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalCourseUtil {
	public final static String FORUM_ICON_CSS = "o_fo_icon";
	public final static String FORUM_TOOLBAR_CSS = "o_forum_toolbar";
	
	private String forumIconCss;
	
	private FunctionalUtil functionalUtil;
	private FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	
	public FunctionalCourseUtil(FunctionalUtil functionalUtil, FunctionalRepositorySiteUtil functionalRepositorySiteUtil){
		this.functionalUtil = functionalUtil;
		this.functionalRepositorySiteUtil = functionalRepositorySiteUtil;
		
		setForumIconCss(FORUM_ICON_CSS);
	}
	
	/**
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @return true on success otherwise false
	 * 
	 * Opens the nth course element within the specified course.
	 */
	public boolean open(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourse(browser, courseId))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//ul[contains(@class, 'b_tree_l1']//li//a[")
		.append(nth + 1)
		.append("]");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @return true on success otherwise false
	 * 
	 * Opens the nth course element within the specified course
	 * without using business paths.
	 */
	public boolean openWithoutBusinessPath(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourseWithoutBusinessPath(browser, courseId))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//ul[contains(@class, 'b_tree_l1']//li//a[")
		.append(nth + 1)
		.append("]");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param courseId
	 * @param nth forum in the course
	 * @return true on success, otherwise false
	 * 
	 * Opens the course with courseId and nth forum within the specified
	 * course.
	 */
	public boolean openForum(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourse(browser, courseId))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//ul//li//a[contains(@class, ")
		.append(getForumIconCss())
		.append(")][")
		.append(nth + 1)
		.append("]");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param courseId
	 * @param nthForum
	 * @param title
	 * @param message
	 * @return true on success, otherwise false
	 * 
	 * Opens the specified forum in the course and posts a new topic.
	 */
	public boolean postForumMessage(Selenium browser, long courseId, int nthForum, String title, String message){
		if(!openForum(browser, courseId, nthForum))
			return(false);
		
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param id
	 * @return
	 * 
	 * Opens the wiki specified by id.
	 */
	public boolean openWiki(Selenium browser, long id){
		browser.open(functionalUtil.getDeploymentUrl() + "url/RepositoryEntry/" + id);
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param wikiId
	 * @param pagename
	 * @param content
	 * @return true on success, otherwise false
	 * 
	 * Creates a new wiki article.
	 */
	public boolean createWikiArticle(Selenium browser, long wikiId, String pagename, String content){
		if(!openWiki(browser, wikiId))
			return(false);
			
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param id
	 * @return true on success, otherwise false
	 * 
	 * Opens the blog specified by id.
	 */
	public boolean openBlog(Selenium browser, long id){
		browser.open(functionalUtil.getDeploymentUrl() + "url/RepositoryEntry/" + id);
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}

	/**
	 * @param browser
	 * @param blogId
	 * @param title
	 * @param description
	 * @param content
	 * @return true on success, otherwise false
	 * 
	 * Create a new blog entry.
	 */
	public boolean createBlogEntry(Selenium browser, long blogId, String title, String description, String content){
		if(!openBlog(browser, blogId))
			return(false);
		
		//TODO:JK: implement me
		
		return(false);
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

	public String getForumIconCss() {
		return forumIconCss;
	}

	public void setForumIconCss(String forumIconCss) {
		this.forumIconCss = forumIconCss;
	}
	
}
