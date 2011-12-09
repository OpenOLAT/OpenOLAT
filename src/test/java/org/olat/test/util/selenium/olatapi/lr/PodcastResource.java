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
*/
package org.olat.test.util.selenium.olatapi.lr;

import com.thoughtworks.selenium.Selenium;

public class PodcastResource extends ResourceEditor {

	public PodcastResource(Selenium selenium, String title) {
		super(selenium, title);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param title
	 * @param description
	 * @param imageName
	 */
	public void editPodcast(String title, String description, String imageName) {
		selenium.click("ui=podcast::edit()");
		selenium.waitForPageToLoad("30000");
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);	  			
		selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Description)", description);
				
		if(imageName!=null) {
			//TODO: LD: add image
		}
		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * An episode requires an audio or video file that can be used with Flash.
	 * @param title
	 * @param description
	 * @param fileName
	 */
	public void createEpisode(String title, String description, String fileName) {
	  if(selenium.isElementPresent("ui=podcast::create()")) {
		  selenium.click("ui=podcast::create()");
		  selenium.waitForPageToLoad("30000");
	  } else if(selenium.isElementPresent("ui=podcast::addEpisode()")) {
	    selenium.click("ui=podcast::addEpisode()");
	    selenium.waitForPageToLoad("30000");
	  }
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);

		editEpisodeForm(description, fileName);
	}
	
	/**
	 * FIXME LD: this doesn't use the episode title! 
	 * description or fileName could be null, if so they won't change.
	 * @param title
	 * @param description
	 * @param fileName
	 */
	public void editEpisode(String title, String description, String fileName) {
	  //selenium.click("ui=podcast::editEpisode(episodeTitle=" + title + ")");
	  selenium.click("ui=podcast::editEpisode()");
    selenium.waitForPageToLoad("30000");
    
    //we don't change the title
    
    //selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);      
    editEpisodeForm(description, fileName);
	}
	
	private void editEpisodeForm(String description, String fileName) {
	  if(description!=null) {
      selenium.type("ui=commons::tinyMce_styledTextArea()", description);
    }
    
    if(fileName!=null) {
      //an episode requires an audio or video file that can be used with Flash.
      selenium.type("ui=upload::fileChooser()", fileName);    
      sleepThread(5000);   
    }
    
    selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Publish)");
    selenium.waitForPageToLoad("30000");
	}
	
	public void commentEpisode(String title,String comment) {
	  selenium.click("ui=podcast::addComment(entryTitle=" + title + ")");
    selenium.waitForPageToLoad("30000");
    this.sleepThread(5000);
    
    //typeInRichText(comment);
    selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
    selenium.type("ui=commons::tinyMce_styledTextArea()", comment);
    selenium.selectFrame("relative=top"); 
    
    selenium.click("ui=commons::save()");
    selenium.waitForPageToLoad("30000");
    selenium.click("ui=commons::backLink()");
    selenium.waitForPageToLoad("30000");
	}
	
	public boolean hasComments(String entryTitle, int num) {
    return selenium.isTextPresent("Comments (" + num + ")");
  }
	
	public void includeExternal(String title, String description, String feedURL) {
		selenium.click("ui=podcast::includeExternal()");
		selenium.waitForPageToLoad("30000");
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);	  			
		selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Description)", description);
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Feed URL)", feedURL);	  	
				
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		//TODO: LD: if no valid URL is provided, cancel or enter a new URL
	}
}
