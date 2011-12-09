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
 * Represents a TopicAssignment course element in course run.
 * 
 * @author lavinia
 *
 */
public class TopicAssignmentRun extends CourseElement {

	public TopicAssignmentRun(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @param limitVacancies
	 * @param vacanciesNumber
	 * @return
	 */
	public TopicEditor createTopic(String title, String description, boolean limitVacancies, int vacanciesNumber) {		
		selenium.click("ui=projectBroker::createTopic()");
		selenium.waitForPageToLoad("30000");
		if(title!=null) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);
		}
		if(description!=null) {
		  selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Description)", description);
		}
				
		//add limit vacancies, attach file, select email notification
		selenium.click("ui=projectBroker::doLimitVacancies()");
		selenium.waitForPageToLoad("30000");
		if(vacanciesNumber>1) {
			//change limit
		  selenium.type("ui=projectBroker::vacanciesNumber()", String.valueOf(vacanciesNumber));
		}
		
		//save
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
		
		return new TopicEditor(selenium);
	}

	/**
	 * Accepts null as input params. If a parameter is null, do not change the defaults.
	 * @param title
	 * @param description
	 * @param faculty
	 * @param registrationStartDate
	 * @param registrationEndDate
	 * @param dueStartDate
	 * @param dueEndDate
	 * @param limitVacancies
	 * @param vacanciesNumber
	 * @param attachment
	 * @param emailNotification
	 * @return
	 */
	@Deprecated
	public TopicEditor createTopic(String title, String description, String faculty, 
			String registrationStartDate, String registrationEndDate, String dueStartDate, String dueEndDate,
			boolean limitVacancies, int vacanciesNumber, String attachment, boolean emailNotification) {
		
		selenium.click("ui=projectBroker::createTopic()");
		selenium.waitForPageToLoad("30000");
		if(title!=null) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);
		}
		if(description!=null) {
		  selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Description)", description);
		}
		
		//TODO: LD choose faculty
		
		//add registration start and end date
		//WARNING: NO DATE CORRECTNESS CHECK IS PERFORMED!
		if(registrationStartDate!=null) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Registration)", registrationStartDate);		 
		}
		if(registrationEndDate!=null) {
		  selenium.type("ui=projectBroker::registrationDeadline()",registrationEndDate);
		}
		//add due date starting and end date
		if(dueStartDate!=null) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Due date)", dueStartDate);
		}
		if(dueEndDate!=null) {
		  selenium.type("ui=projectBroker::dueDeadline()",dueEndDate);
		}
		
		//TODO: LD: add limit vacancies, attach file, select email notification
		
		//save
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
		
		return new TopicEditor(selenium);
	}
	
	public boolean hasTopic(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::topicLink(nameOfTopic=" + topicName + ")")) {
		  return true;
		}
		return false;
	}
	
	public TopicEditor openTopic(String topicName) {
		selenium.click("ui=projectBroker::topicLink(nameOfTopic=" + topicName + ")");
		selenium.waitForPageToLoad("30000");
		return new TopicEditor(selenium);
	}
	
	public boolean hasVacancies(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::hasVacancies(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean isFilled(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::filled(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean isTemporaryRegisterd(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::temporaryRegistration(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean isRegistered(String topicName) {
    if(selenium.isElementPresent("ui=projectBroker::isRegistered(nameOfTopic=" + topicName + ")")) {
      return true;
    }
    return false;
  }
	
	public boolean hasParticipantsAccepted(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::participantsAccepted(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean hasCheckParticipants(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::checkParticipants(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean canSelectTopic(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::selectTopic(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public void selectTopic(String topicName) {
		selenium.click("ui=projectBroker::selectTopic(nameOfTopic=" + topicName + ")");
		selenium.waitForPageToLoad("30000");		
	}
	
	public boolean canDeselect(String topicName) {
		if(selenium.isElementPresent("ui=projectBroker::deselectTopic(nameOfTopic=" + topicName + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean canCreateTopic() {
		if(selenium.isElementPresent("ui=projectBroker::createTopic()")) {
			return true;
		}
		return false;
	}
		
	public boolean hasTutoredGroup(String groupName) {
		return getCourseRun().hasTutoredGroup(groupName);
	}
}
