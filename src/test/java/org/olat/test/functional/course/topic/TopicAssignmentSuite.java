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
package org.olat.test.functional.course.topic;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;



/**
 * 
 * @author lavinia
 *
 */
public class TopicAssignmentSuite extends TestSuite {
	
  protected final static String COURSE_NAME = "TOPIC_ASSIGNMENT_COURSE"; //+System.currentTimeMillis();
  protected final static String TUTOR1 = "selenium_tutor1";
  protected final static String TUTOR2 = "selenium_tutor2";
  protected final static String TUTOR3 = "selenium_tutor3";
  protected final static String TOPIC_ASSIGNMENT_1 = "TOPIC_ASSIGNMENT_1";
  protected final static String TOPIC_TITLE_1 = "Topic_Tutor1";
  protected final static String TOPIC_TITLE_2 = "Topic_Tutor2";
  protected final static String TOPIC_TITLE_3 = "Topic_Tutor3";

  protected final static String STUDENT1 = "selenium_topic_student1";
  protected final static String STUDENT2 = "selenium_topic_student2";
  protected final static String STUDENT3 = "selenium_topic_student3";

  protected final static String HAND_IN_TOPIC1 = "handInTopic1.pdf";
  protected final static String HAND_IN_TOPIC2 = "handInTopic2.pdf";
  protected final static String HAND_IN_TOPIC3 = "handInTopic3.pdf";
  protected final static String HAND_IN_TOPIC4 = "handInTopic4.pdf";

  protected final static String HAND_BACK_TOPIC1 = "handBackTopic1.pdf";
  protected final static String HAND_BACK_TOPIC3 = "handBackTopic3.pdf";


	final static String FIRST = "TopicAssignmentSuite.FIRST";
	final static String SECOND = "TopicAssignmentSuite.SECOND";
	final static String THIRD = "TopicAssignmentSuite.THIRD";
  
  
  public static Test suite() { 
    Context context = Context.setupContext("TopicAssignmentSuite", SetupType.TWO_NODE_CLUSTER);
    WorkflowHelper.deleteAllCoursesNamed(COURSE_NAME);
    Context.tearDown();

    TestSuite suite = new TestSuite("TopicAssignmentSuite");

    suite.addTestSuite(CreateAndConfigureTopicAssignmentTest.class);
    suite.addTestSuite(CreateTopicInTopicAssignmentTest.class);
    suite.addTestSuite(SelectTopicsAndAcceptCandidatesTest.class);
    suite.addTestSuite(UploadInTopicsTest.class);

    return suite; 
  }

	 

}
