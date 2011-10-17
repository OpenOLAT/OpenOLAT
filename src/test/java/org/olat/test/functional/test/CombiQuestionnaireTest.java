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
