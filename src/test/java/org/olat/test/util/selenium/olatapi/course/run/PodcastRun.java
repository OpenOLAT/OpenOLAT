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
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.lr.PodcastResource;

import com.thoughtworks.selenium.Selenium;

public class PodcastRun extends OLATSeleniumWrapper {

  private PodcastResource podcastResource;
  
  public PodcastRun(Selenium selenium) {
    super(selenium);

    podcastResource = new PodcastResource(selenium, "");
  }
  
  public void createEpisode(String title, String description, String fileName) {
    podcastResource.createEpisode(title, description, fileName);
  }
  
  public void editEpisode(String title, String description, String fileName) {
    podcastResource.editEpisode(title, description, fileName);
  }
  
  public void commentEpisode(String title,String comment) {
    podcastResource.commentEpisode(title, comment);
  }
  
  public boolean hasComments(String entryTitle, int numComments) {
    return podcastResource.hasComments(entryTitle, numComments);
  }

}
