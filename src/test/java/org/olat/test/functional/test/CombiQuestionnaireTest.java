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
package org.olat.test.functional.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Combi test for questionnaire editor and attempts check. See respective test classes for extensive documentation. 
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. CreateQuestionnaireWithAllQuestionTypes.java creates a questionnaire with all kinds of question types. <br/>
 * 2. IntegrateQuestionnaireAndCheckAttempts.java adds the questionnaire element to a course and tests the different configurations in the questionnaire run. <br/>
 * 
 * @author sandra
 * 
 */

public class CombiQuestionnaireTest extends TestSuite {
	
	protected final static String QUESTIONNAIRE_NAME = "QuestionnaireAttempts"+System.currentTimeMillis();
	protected final static String QUESTIONNAIRE_DESCRIPTION = "QuestionnaireAttemptsDesc";
	protected final static String COURSE_NAME = "QuestionnaireAttemptsCourse"+System.currentTimeMillis();
	protected final static String COURSE_DESCRIPTION = "QuestionnaireAttemptsCourseDesc";

    public static Test suite() { 
        TestSuite suite = new TestSuite("CombiQuestionnaireTest");

        suite.addTestSuite(CreateQuestionnaireWithAllQuestionTypes.class);
        suite.addTestSuite(IntegrateQuestionnaireAndCheckAttempts.class);

        return suite; 
   }
	
}
