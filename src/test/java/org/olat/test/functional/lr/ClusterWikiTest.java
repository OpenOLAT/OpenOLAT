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
package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.WikiRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * test wiki functionality (editing wikipages, check GUI lock, select all menu options like Index A to Z etc. show VErsion and Compare) within a cluster <br/>
 * <p>
 * Test setup: <br/>
 * 1. Login as Admin in browser 1 <br/>
 * 2. Login as Author in browser 2 <br/>
 * 3. cleanup at the end <br/>
 * 
 * Test case: <br/>
 * 1. Admin logs in in browser 1 <br/>
 * 2. Author logs in in browser 2 <br/>
 * 3. Admin creates a wiki and changes access to all registered users <br/>
 * 4. Wiki is schown, Admin inserts link test in wiki and clicks it <br/>
 * 5. Admin enters word "testing" in wiki page and it is checked whether "testing" is present <br/>
 * 6. Author displays the wiki (on a different node), checks whether "test"-page is there <br/>
 * 7. Author searches for "test" and checks that "testing" is displayed <br/>
 * 8. Admin edits wiki <br/>
 * 9. check that Author gets the message "is currently modified by" <br/>
 * 10. show article "testing" in both browsers <br/>
 * 11. Admin adds "lorem ibsum" <br/>
 * 12. check that Author sees "lorem ibsum" <br/>
 * 13. Admin clicks create or search "test", check if page is displayed <br/>
 * 14. Admin clicks links in the navigation box <br/>
 * 15. cleanup <br/>
 * 
 * </p>
 * 
 * @author guido
 *
 */
public class ClusterWikiTest extends BaseSeleneseTestCase {
	
    private OLATWorkflowHelper workflow1;
    private OLATWorkflowHelper workflow2;
    
    
	  public void testMultiBrowserClusterWiki() throws Exception {
	  	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
	  	String wikiName = "wikijunit-"+System.currentTimeMillis();

	  	// login first
	  	System.out.println("logging in browser 1...");
	  	workflow1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));

	  	System.out.println("logging in browser 2...");
	  	workflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));

	  	//browser 1		
	  	//create a wiki and change access for all users
	  	LRDetailedView lRDetailedView = workflow1.getLearningResources().createResource(wikiName, "test", LR_Types.WIKI);
	  	lRDetailedView.modifyProperties(LRDetailedView.ACCESS_ALL_REGISTERED);
	  	WikiRun wikiRun1 = lRDetailedView.showWikiContent();
	  	wikiRun1.editPage("[[test]]");
	  	wikiRun1.getSelenium().click("ui=wiki::article_testLink()");
	  	//wikiRun1.getSelenium().waitForPageToLoad("30000");
	  	wikiRun1.editPage("testing");				
	  	assertTrue(wikiRun1.isTextPresent("testing"));				

	  	//browser 2
	  	//start the wiki with the second user on the second node and check content				
	  	WikiRun wikiRun2 = workflow2.getLearningResources().showWikiContent(wikiName);
	  	assertTrue(wikiRun2.isTextPresent("test")); //true: successfully loaded the page in the second node
	  	//wikiRun2.searchArticle("test");
	  	wikiRun2.createOrSearchArticle("test", null);
	  	assertTrue(wikiRun2.isTextPresent("testing")); //true: successfully loaded the page in the second node

	  	//browser 1
	  	wikiRun1.openEditor();//try to edit page. Second user should have GUI lock on this page	
	  	Thread.sleep(2000);

	  	//browser 2
	  	wikiRun2.openEditor();
	  	assertTrue(wikiRun2.isTextPresent("is being modified by")); //WARNING: HERE POTENTIAL FAILURE EACH TIME THE TRANSLATION CHANGES!

	  	//continue both

	  	//browser 1
	  	wikiRun1.showArticle();
	  	assertTrue(wikiRun1.isTextPresent("testing"));				

	  	//browser 2
	  	wikiRun2.showArticle();
	  	assertTrue(wikiRun2.isTextPresent("testing"));

	  	//browser 1
	  	//edit page: second user on node 2 should see new content
	  	wikiRun1.editPage("lorem ibsum");				


	  	//browser 2
	  	wikiRun2.createOrSearchArticle("test", null);
	  	assertTrue(wikiRun2.isTextPresent("lorem ibsum"));//true: new content visible on node 2				


	  	//browser 1
	  	//continue with basic tests
	  	wikiRun1.createOrSearchArticle("test", null);
	  	assertTrue(wikiRun1.isTextPresent("lorem ibsum"));
	  	wikiRun1.selectIndex();
	  	wikiRun1.selectFromAToZ();
	  	wikiRun1.editMenu("* [[Index]]\n* [[Index|Your link]]\n* [[test]]");
	  	wikiRun1.selectIndex();
	  	wikiRun1.createOrSearchArticle("test", null);
	  	wikiRun1.showVersionsAndCompare();
	  	assertTrue(wikiRun1.isTextPresent("+ testing"));


	  }

		@Override
		public void cleanUpAfterRun() {					
			workflow1.logout();
			workflow2.logout();			
		}
    

}
