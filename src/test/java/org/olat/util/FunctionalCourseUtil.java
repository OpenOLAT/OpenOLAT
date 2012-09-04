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

import org.olat.util.FunctionalUtil.WaitLimitAttribute;

import com.thoughtworks.selenium.Selenium;

/**
 * Description: <br>
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalCourseUtil {
	public final static String COURSE_RUN_CSS = "o_course_run";
	
	public final static String EPORTFOLIO_ADD_CSS = "b_eportfolio_add";
	
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
	public final static String BLOG_FORM_CSS = "o_sel_blog_form";
	
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
	
	private String courseRunCss;
	
	private String eportfolioAddCss;
	
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
	private String blogFormCss;
	
	private FunctionalUtil functionalUtil;
	private FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	
	public FunctionalCourseUtil(FunctionalUtil functionalUtil, FunctionalRepositorySiteUtil functionalRepositorySiteUtil){
		this.functionalUtil = functionalUtil;
		this.functionalRepositorySiteUtil = functionalRepositorySiteUtil;
		
		setCourseRunCss(COURSE_RUN_CSS);
		
		setEportfolioAddCss(EPORTFOLIO_ADD_CSS);
		
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
		setBlogFormCss(BLOG_FORM_CSS);
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

		selectorBuffer.append("xpath=//ul[contains(@class, 'b_tree_l1')]//li[")
		.append(nth + 1)
		.append("]//a");
		
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

		selectorBuffer.append("xpath=//ul[contains(@class, 'b_tree_l1')]//li[")
		.append(nth + 1)
		.append("]//a");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return true on success
	 * 
	 * Opens the course editor but the course must be open.
	 */
	public boolean openCourseEditor(Selenium browser){
		//TODO:JK: implement me
		return(false);
	}
	
	/**
	 * @param browser
	 * @param option
	 * @param nthForm
	 * @return true on success
	 * 
	 * Disables the specified access option, the course editor should be open.
	 */
	public boolean disableAccessOption(Selenium browser, AccessOption option, int nthForm){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param option
	 * @param nthForm
	 * @return true on success
	 * 
	 * Enables the specified access option, the course editor should be open.
	 */
	public boolean enableAccessOption(Selenium browser, AccessOption option, int nthForm){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @return true on success
	 * 
	 * Adds an artefact to eportfolio by clicking the appropriate
	 * button.
	 */
	public boolean addToEportfolio(Selenium browser, String binder, String page, String structure,
			String title, String description, String[] tags,
			FunctionalEPortfolioUtil functionalEPortfolioUtil){
		
		/* open wizard */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getEportfolioAddCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		if(binder != null){
			/* fill in wizard - title & description */
			functionalEPortfolioUtil.fillInTitleAndDescription(browser, title, description);
			
			/* fill in wizard - tags */
			functionalEPortfolioUtil.fillInTags(browser, tags);
			
			/* fill in wizard - destination */
			String selector = functionalEPortfolioUtil.createSelector(binder, page, structure);
			
			functionalUtil.waitForPageToLoadElement(browser, selector);
			
			browser.click(selector);
			
			/* click finish */
			functionalUtil.clickWizardFinish(browser);
			functionalUtil.waitForPageToUnloadElement(browser, selector);
		}

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
	 * @param browser
	 * @param id
	 * @return
	 * 
	 * Opens the wiki specified by id.
	 */
	public boolean openWiki(Selenium browser, long id){
		browser.open(functionalUtil.getDeploymentPath() + "/url/RepositoryEntry/" + id);
		functionalUtil.waitForPageToLoad(browser);
		
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
	 * @param browser
	 * @param id
	 * @return true on success, otherwise false
	 * 
	 * Opens the blog specified by id.
	 */
	public boolean openBlog(Selenium browser, long id){
		browser.open(functionalUtil.getDeploymentPath() + "/url/RepositoryEntry/" + id);
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param courseId
	 * @param nth
	 * @return
	 * 
	 * Opens the course with courseId and nth blog within the specified
	 * course.
	 */
	public boolean openBlogWithoutBusinessPath(Selenium browser, long courseId, int nth){
		if(!functionalRepositorySiteUtil.openCourse(browser, courseId))
			return(false);
		
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
	 * @param browser
	 * @param blogId
	 * @param title
	 * @param description
	 * @param content
	 * @return true on success, otherwise false
	 * 
	 * Create a new blog entry.
	 */
	public boolean createBlogEntry(Selenium browser, long courseId, int nth,
			String title, String description, String content){
		if(!openBlogWithoutBusinessPath(browser, courseId, nth))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getBlogCreateEntryCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		/* fill in form - title */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//form//div[contains(@class, '")
		.append(getBlogFormCss())
		.append("')]//input[@type='text'])[1]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.type(selectorBuffer.toString(), title);
		
		/* fill in form - description */
		functionalUtil.typeMCE(browser, getBlogFormCss(), description);
		
		/* fill in form - content */
		functionalUtil.typeMCE(browser, getBlogFormCss(), content);
		
		/* save form */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(getBlogFormCss())
		.append("')]//button[last()]");
		
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
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

	public String getEportfolioAddCss() {
		return eportfolioAddCss;
	}

	public void setEportfolioAddCss(String eportfolioAddCss) {
		this.eportfolioAddCss = eportfolioAddCss;
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

	public String getBlogFormCss() {
		return blogFormCss;
	}

	public void setBlogFormCss(String blogFormCss) {
		this.blogFormCss = blogFormCss;
	}
	
}
