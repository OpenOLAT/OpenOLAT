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

import com.thoughtworks.selenium.Selenium;

/**
 * Basic LTI run.
 * 
 * @author lavinia
 *
 */
public class LTIRun extends OLATSeleniumWrapper {

  public LTIRun(Selenium selenium) {
    super(selenium);
    // TODO Auto-generated constructor stub
  }
  
  public void launch() {
    selenium.selectFrame("//iframe[@id='IMSBasicLTIFrame']");
    if(selenium.isElementPresent("ui=course::content_lti_launch()")) {
      selenium.click("ui=course::content_lti_launch()");
      //TODO: LD: add security warning confirmation step!
    } else {
      throw new IllegalStateException("Cannot launch LTI, button missing!");
    }
    selenium.selectFrame("relative=top"); 
  }
}
