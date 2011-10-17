package org.olat.test.functional.test;

import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * 
 * Combi test for test editor. See respective test classes for extensive documentation. 
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. CreateTstInEditor.java creates test in test editor. <br/>
 * 2. ChangeTstInEditor changes test in test editor. <br/>
 * 3. IntegrateTstInCourse.java integrates test in course editor. <br/>
 * 4. TstRun.java tests how student solves test in test run. <br/>
 * 
 * @author sandra
 * 
 */
public class TestEditorCombiTest extends TestSuite {

	
	
	protected final static String COURSE_NAME = "CourseTest_" + System.currentTimeMillis();
	protected final static String TEST_NAME = "TestTest_" + System.currentTimeMillis();

	final static String FIRST = "TestEditorCombiTest.FIRST";
	final static String SECOND = "TestEditorCombiTest.SECOND";
	final static String THIRD = "TestEditorCombiTest.THIRD";

	
    public static Test suite() { 
    	Context context = Context.setupContext("TestEditorCombiTest", SetupType.SINGLE_VM);
    	//context.deleteAllLearningResourcesFromMyAuthors();
    	Context.tearDown();
    	
        TestSuite suite = new TestSuite("CombiTest");

        suite.addTestSuite(CreateTstInEditor.class);
        suite.addTestSuite(ChangeTstInEditor.class);
        suite.addTestSuite(IntegrateTstInCourse.class);
        suite.addTestSuite(TstRun.class);

        return suite; 
   }
	
}
