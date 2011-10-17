package org.olat.test.functional.course.chat;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.components.ChatComponent;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * test groupChat stuff and polling (changing interval)
 * 
 * @author Guido
 *
 */
public class CourseGroupChatAndPollingTest extends BaseSeleneseTestCase {
	
  private final String COURSE_NAME = Context.DEMO_COURSE_NAME_1;


  public void testCourseChat() throws Exception {
    Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

    // login first
    OLATWorkflowHelper workflowAdmin = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    LearningResources learningResources = workflowAdmin.getLearningResources();

    for (int i = 0; i < 10; i++) {
      System.out.println("run: "+i);
      sendMessagesInCourseChat(learningResources, i);
    }

    workflowAdmin.logout();
  }

  /**
   * Open course, open chat, send messages, close chat, close course.
   * @param learningResources
   * @param run
   * @throws InterruptedException
   */
  private void sendMessagesInCourseChat(LearningResources learningResources, int run) throws InterruptedException {
    CourseRun courseRun = learningResources.searchAndShowCourseContent(COURSE_NAME);   
    Thread.sleep(16000);//wait until course chat link is save (no reload warning)

    ChatComponent chatComponent = courseRun.getChatComponent();
    chatComponent.openStatusChanger();
    chatComponent.closeExtWindow();    
    Thread.sleep(2000);

    chatComponent.openChat(true);
    Thread.sleep(3000);
    
    String username = Context.getContext().getStandardAdminOlatLoginInfos(1).getUsername();
    if (!chatComponent.isTextPresent("("+ username +")")) {
      Thread.sleep(6000); //wait for another 6s
    }
    assertTrue("Run number: "+run, chatComponent.isTextPresent("("+username+")"));
    String msg = Long.valueOf(System.currentTimeMillis()).toString();
    
    chatComponent.sendMessage(msg);
    Thread.sleep(3000); //after 3s it should be here
    
    assertTrue("Run number: "+run+"Didn't find message as expected: "+msg, chatComponent.isTextPresent(msg));
    chatComponent.toggleAnonymous();
    Thread.sleep(3000); //after 3s it should be here
    
    msg = Long.valueOf(System.currentTimeMillis()).toString();
    chatComponent.sendMessage(msg);
    Thread.sleep(3000); //after 3s it should be here
    assertTrue("Run number: "+run+"Didn't find message: "+msg, chatComponent.isTextPresent(msg));
    
    chatComponent.closeExtWindow();
    courseRun.close(COURSE_NAME);
    Thread.sleep(3000);    
    
  }

}
