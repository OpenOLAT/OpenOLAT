package org.olat.test.functional.codepoints.cluster;

import org.junit.Ignore;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

import com.thoughtworks.selenium.Selenium;

/**
 * test groupChat stuff and polling (changing interval)
 * 
 * @author Guido
 *
 */
@Ignore
public class CourseGroupChatClusterTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = Context.DEMO_COURSE_NAME_1;
	
    protected Selenium selenium_1;
    protected Selenium selenium_2;
    private CodepointClient codepointClient_A, codepointClient_B;
   
    
    public void testCourseChat() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		OLATWorkflowHelper workflow_1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		OLATWorkflowHelper workflow_2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));
		
		CourseRun curseRun1 = workflow_1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		selenium_1 = curseRun1.getSelenium();
		
		CourseRun curseRun2 = workflow_2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		selenium_2 = curseRun2.getSelenium();
				
		codepointClient_A = context.createCodepointClient(1);
		CodepointRef beforeSyncA = codepointClient_A.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-before-sync.org.olat.instantMessaging.groupchat.GroupChatJoinTask.run");
		beforeSyncA.setHitCount(0);
		beforeSyncA.enableBreakpoint();
		
		codepointClient_B = context.createCodepointClient(2);
		CodepointRef beforeSyncB = codepointClient_B.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-before-sync.org.olat.instantMessaging.groupchat.GroupChatJoinTask.run");
		beforeSyncB.setHitCount(0);
		beforeSyncB.enableBreakpoint();
		
		CodepointRef inSyncA = codepointClient_A.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.instantMessaging.groupchat.GroupChatJoinTask.run");
		inSyncA.setHitCount(0);
		
		CodepointRef inSyncB = codepointClient_B.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.instantMessaging.groupchat.GroupChatJoinTask.run");
		inSyncB.setHitCount(0);
		
		selenium_1.mouseMoveAt("ui=courseChat::openCourseChat()", "300,300");
		selenium_1.click("ui=courseChat::openCourseChat()");
				
		selenium_2.mouseMoveAt("ui=courseChat::openCourseChat()", "300,300");
		selenium_2.click("ui=courseChat::openCourseChat()");
		
		//both node are waiting to entry sync block
		Thread.sleep(2000);
		inSyncB.assertHitCount(0);
		inSyncA.assertHitCount(0);
		
		beforeSyncA.assertHitCount(1);
		beforeSyncB.assertHitCount(1);
		
		TemporaryPausedThread[] threadsA = beforeSyncB.getPausedThreads();
		assertNotNull(threadsA);
		assertEquals(1, threadsA.length);
		
		TemporaryPausedThread[] threadsB = beforeSyncA.getPausedThreads();
		assertNotNull(threadsB);
		assertEquals(1, threadsB.length);
		
		// continue the first
		threadsA[0].continueThread();
		Thread.sleep(1500);
		
		// continue the second
		threadsB[0].continueThread();
		Thread.sleep(500);
		
		
		Thread.sleep(16000);//wait until course chat link is save (no reload warning)
		
		
		//open course chat 
		/*selenium_1.mouseMoveAt("ui=courseChat::openCourseChat()", "300,300");
		selenium_1.click("ui=courseChat::openCourseChat()");*/
		selenium_1.waitForPageToLoad("30000");
		Thread.sleep(1000);
		
		if(!selenium_1.isElementPresent("ui=courseChat::withinCourseChat()")) {			
			Thread.sleep(5000);	
		}
		selenium_1.click("ui=courseChat::withinCourseChat()");
		selenium_1.waitForPageToLoad("30000");
    
		selenium_1.click("ui=courseChat::toggleAnonymous()");
		selenium_1.waitForPageToLoad("30000");
		
		Thread.sleep(16000);
		
		//open course chat
		/*selenium_2.mouseMoveAt("ui=courseChat::openCourseChat()", "300,300");
		selenium_2.click("ui=courseChat::openCourseChat()");*/
		selenium_2.waitForPageToLoad("30000");
		Thread.sleep(3000);
		
		if(!selenium_2.isElementPresent("ui=courseChat::withinCourseChat()")) {     
      Thread.sleep(5000); 
    }
		selenium_2.click("ui=courseChat::withinCourseChat()");
		selenium_2.waitForPageToLoad("30000");
		selenium_2.click("ui=courseChat::toggleAnonymous()");
		selenium_2.waitForPageToLoad("30000");
		
		//if both name changed polling works and chat window is working
		Thread.sleep(3000);
		if (!selenium_1.isTextPresent("("+context.getStandardAdminOlatLoginInfos(1).getUsername()+")")) {
			Thread.sleep(6000); //wait for another 6s
		}
		assertTrue(selenium_1.isTextPresent("("+context.getStandardAdminOlatLoginInfos(1).getUsername()+")"));
		
		if (!selenium_2.isTextPresent("("+context.getStandardAuthorOlatLoginInfos(2).getUsername()+")")) {
			Thread.sleep(6000); //wait for another 6s
		}
		assertTrue(selenium_2.isTextPresent("("+context.getStandardAuthorOlatLoginInfos(2).getUsername()+")"));
		
		
		//send msg and check on second node if it arrived
		String msg = Long.valueOf(System.currentTimeMillis()).toString();
		selenium_1.type("ui=courseChat::sendMsgInputField()", msg);
		selenium_1.click("ui=courseChat::sendMsgButton()");
		selenium_1.waitForPageToLoad("30000");
		Thread.sleep(3000); //after 3s it should be there
		assertTrue(selenium_2.isTextPresent(msg));
		
    	
    	
		
    	
	}

}
