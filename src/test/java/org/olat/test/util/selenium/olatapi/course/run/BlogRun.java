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
import org.olat.test.util.selenium.olatapi.lr.BlogResource;

import com.thoughtworks.selenium.Selenium;

/**
 * Uses a <code>BlogResource</code> to delegate the implementation to it.
 * The difference between a <code>BlogRun</code> and a <code>BlogResource</code> is that a <code>BlogRun</code> instance
 * could only be constructed within a <code>CourseRun</code> whereas a <code>BlogResource</code> lives in a separate resource tab.
 * 
 * @author lavinia
 *
 */

public class BlogRun extends OLATSeleniumWrapper {
  
  private BlogResource blogResource;

  public BlogRun(Selenium selenium) {
    super(selenium);

    blogResource = new BlogResource(selenium,"");
  }
  
  public void createEntry(String title, String description, String content, boolean publish) {
    blogResource.createEntry(title, description, content, publish);
  }
  
  public boolean hasDraft(String title) {
    return blogResource.hasDraftEntry(title);
  }
  
  public void editEntry(String title, String description, String content, boolean publish) {
    blogResource.editEntry(title, description, content, publish);
  }
  
  public void commentEntry(String entryTitle, String comment) {
    blogResource.commentEntry(entryTitle, comment);
  }
  
  public boolean hasComments(String entryTitle, int numComments) {
    return blogResource.hasComments(entryTitle, numComments);
  }
}
