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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium.olatapi.course.editor;


import org.olat.test.util.selenium.olatapi.lr.PodcastResource;

import com.thoughtworks.selenium.Selenium;

/**
 * Podcast element configuration page in course editor.
 * @author Sandra Arnold
 *
 */
public class PodcastEditor extends CourseElementEditor {
	
	private String podcastTitle;

	/**
	 * @param selenium
	 */
	public PodcastEditor(Selenium selenium, String title) {
		super(selenium);
		podcastTitle = title;
		
		//Check that we're on the right place
		//TODO tab
		if(!selenium.isElementPresent("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()")) {
			throw new IllegalStateException("This is not the - Podcast Learning Content - page");
		}
	}

	/**
	 * Select, import, create, replace, edit podcast
	 * 
	 * @param testTitle
	 */
	public void select(String podcastTitle) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbPodcast_selectCreateImportPodcast()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_choosePodcast(nameOfPodcast=" + podcastTitle + ")");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Creates resource without starting editing.
	 * @param podcastTitle
	 * @param podcastDescription
	 */
	public void create(String podcastTitle, String podcastDescription) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()");
			selenium.waitForPageToLoad("30000");
		}	
		selenium.click("ui=courseEditor::content_bbPodcast_selectCreateImportPodcast()");
		selenium.waitForPageToLoad("30000");
		// TODO click "create", enter PodcastTitle and PodcastDescription, save, next, return to course editor, not clear where xpaths should be added
		selenium.click("ui=courseEditor::content_bbPodcast_create()");
		selenium.waitForPageToLoad("30000");
		
		selenium.click("ui=learningResources::dialog_title()");
		selenium.type("ui=learningResources::dialog_title()", podcastTitle);
	  		
		//SR:
		//selenium.setSpeed("1000");
		
		selenium.click("ui=learningResources::dialog_description()");
		selenium.type("ui=learningResources::dialog_description()", podcastDescription);
		
		//TODO: LD: add image for this learning resource
		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("60000");
	}
	
	/**
	 * Choose new podcast.
	 * @param newPodcastTitle
	 */
	public void replace(String newPodcastTitle) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbPodcast_replacePodcast()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_choosePodcast(nameOfPodcast=" + newPodcastTitle + ")");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Start editing resource. Podcast opens in new tab.
	 * @return
	 */
	public PodcastResource edit() {
		if(selenium.isElementPresent("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()")) {
			selenium.click("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbPodcast_editPodcast()");
		selenium.waitForPageToLoad("30000");
		
		return new PodcastResource(selenium, podcastTitle);
	}



	
}
