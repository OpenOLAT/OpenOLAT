/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.test.util.selenium.olatapi.course.editor;


import org.olat.test.util.selenium.olatapi.lr.BlogResource;

import com.thoughtworks.selenium.Selenium;

/**
 * Blog element configuration page in course editor.
 * @author Sandra Arnold
 *
 */
public class BlogEditor extends CourseElementEditor {
	
	//identifies the blogResource (e.g. for closing)
	private String blogTitle;

	/**
	 * @param selenium
	 */
	public BlogEditor(Selenium selenium, String title) {
		super(selenium);
		blogTitle = title; 

        //Check that we're on the right place		
		if(!selenium.isElementPresent("ui=courseEditor::content_bbBlog_tabBlogLearningContent()")) {
			throw new IllegalStateException("This is not the - Blog Learning Content - page");
		}
	}

	/**
	 * Select, import, create, replace, edit blog
	 * 
	 * @param testTitle
	 */
	public void select(String blogTitle_) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbBlog_tabBlogLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbBlog_tabBlogLearningContent()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbBlog_selectCreateImportBlog()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_chooseBlog(nameOfBlog=" + blogTitle_ + ")");
		selenium.waitForPageToLoad("30000");	
		blogTitle = blogTitle_;
	}
	
	/**
	 * Creates new blog.
	 * @param blogTitle
	 * @param blogDescription
	 */
	public void create(String blogTitle, String blogDescription) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbBlog_tabBlogLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbBlog_tabBlogLearningContent()");
			selenium.waitForPageToLoad("30000");
		}	
		selenium.click("ui=courseEditor::content_bbBlog_selectCreateImportBlog()");
		selenium.waitForPageToLoad("30000");
		// TODO click "create", enter blogTitle and blogDescription, save, next, return to course editor, not clear where xpaths should be added
		selenium.click("ui=courseEditor::content_bbBlog_create()");
		selenium.waitForPageToLoad("30000");
		
		selenium.click("ui=learningResources::dialog_title()");
		selenium.type("ui=learningResources::dialog_title()", blogTitle);
	  		
		//SR:
		//selenium.setSpeed("1000");
		
		selenium.click("ui=learningResources::dialog_description()");
		selenium.type("ui=learningResources::dialog_description()", blogDescription);
		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("60000");
	}
	
	public void replace(String newBlogTitle) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbBlog_tabBlogLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbBlog_tabBlogLearningContent()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbBlog_replaceBlog()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_chooseBlog(nameOfBlog=" + newBlogTitle + ")");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 *  blog opens in new tab
	 * @return
	 */
	public BlogResource edit() {
		if(selenium.isElementPresent("ui=courseEditor::content_bbBlog_tabBlogLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbBlog_tabBlogLearningContent()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbBlog_editBlog()");
		selenium.waitForPageToLoad("30000");
		
		return new BlogResource(selenium, blogTitle);
	}
	
}
