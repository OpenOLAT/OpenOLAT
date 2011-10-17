package org.olat.test.functional.group.management;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * Combi test for group management. See respective test classes for extensive documentation. 
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. CreateLearningGroupLearningArea.java checks if enrolment course element with learning groups and learning areas works. <br/>
 * 2. EnrolmentLearningGroup.java checks if student subscribed to waiting list moves up to regular group member when other student cancels his enrolment. <br/>
 * 3. ConfigureToolsLearningGroup adds all collaborative tools to learning group and checks their configuration.<br/>
 * 
 * @author sandra
 * 
 */

public class GroupManagementCombiTest extends TestSuite {
	
	protected final static String COURSE_NAME = "Course_for_Group_Management" + System.currentTimeMillis();
	protected final static String GROUP_NAME_1 = "learning group selenium 1";
	
	

	final static String FIRST = "GroupManagementCombiTest.FIRST";
	final static String SECOND = "GroupManagementCombiTest.SECOND";
	
  //seleniumload: make sure that this is not a user with reusable urls!!!
	//TODO:LD: temporary  changed usernames - workaround for OLAT-5249
	//protected final static String STUDENT_USER_NAME = "srenrolstudi_02";
	protected final static String STUDENT_USER_NAME = "srenrolstudi02"; 

    public static Test suite() { 
        TestSuite suite = new TestSuite("CombiTest");

        suite.addTestSuite(CreateLearningGroupLearningArea.class);
        suite.addTestSuite(EnrolmentLearningGroup.class);
        suite.addTestSuite(ConfigureToolsLearningGroup.class);

        return suite; 
   }
	
}
