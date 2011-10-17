package org.olat.test.functional.group.management;

import org.junit.Ignore;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * test groupChat stuff and polling (changing interval)
 * 
 * @author Guido
 *
 */
@Ignore
public class CollaborationToolsTest extends BaseSeleneseTestCase {
	
	
    private OLATWorkflowHelper workflow1;
    private OLATWorkflowHelper workflow2;
    
    public void testCourseChat() throws Exception {
    	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

    	workflow1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    	workflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));

    	String nameOfGroup ="junittest-"+System.currentTimeMillis();
    	//browser 1
    	GroupAdmin groupAdmin1 = workflow1.getGroups().createProjectGroup(nameOfGroup, "junittest");
    	groupAdmin1.setTools(true, true, true, true, true, false, true);
    	String[] owners = {context.getStandardAuthorOlatLoginInfos(2).getUsername()};
    	groupAdmin1.addMembers(new String[0], owners);
    	groupAdmin1.close(nameOfGroup);

    	//browser 2
    	GroupAdmin groupAdmin2 = workflow2.getGroups().selectGroup(nameOfGroup).selectAdministration();
    	assertTrue(groupAdmin2.isChatSelected());
    	groupAdmin2.setTools(false, false, false, false, false, true, false);
    	groupAdmin2.close(nameOfGroup);

    	//browser 1
    	groupAdmin1 = workflow1.getGroups().selectGroup(nameOfGroup).selectAdministration();
    	assertTrue(groupAdmin1.isWikiSelected());

    }

}
