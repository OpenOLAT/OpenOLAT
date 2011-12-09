/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.test.functional.portfolio;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.portfolio.EPCollectWizard;
import org.olat.test.util.selenium.olatapi.portfolio.EPExtensions;
import org.olat.test.util.selenium.olatapi.portfolio.EPMapEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * ePortfolio: this is testing the following scenarios:
 * - creates a map
 * - add pages and structures to this map
 * - link artefacts in map
 * - share map to others
 * - edit a map
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 *
 */
public class CreateFillAndShareMapTest extends BaseSeleneseTestCase {

	private static final String SELENIUM_TEST_MAP_TITLE = "selenium test map";

	public void setUp() throws Exception {		
		Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);		
	}
	@Test
	public void testCreateDefaultMap(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		selenium = workflow.getSelenium();
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		EPMapEditor epMap = epExt.getMapEditor();
		epMap.createDefaultMap(SELENIUM_TEST_MAP_TITLE, "its cold outside, fill this map with funny stuff ;)");
		epMap.toggleEditMode(false);		
	}
	
	@Test(dependsOnMethods ={"testCreateDefaultMap"})
	public void testAddPagesToMap(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		selenium = workflow.getSelenium();
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		EPMapEditor epMap = epExt.getMapEditor();
		epMap.openMapByTitle(SELENIUM_TEST_MAP_TITLE);
		epMap.addPages(2);		
	}
	@Test(dependsOnMethods ={"testAddPagesToMap"})
	public void testAddStructuresToMap(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		selenium = workflow.getSelenium();
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		EPMapEditor epMap = epExt.getMapEditor();
		epMap.openMapByTitle(SELENIUM_TEST_MAP_TITLE);
		epMap.openFirstPage();
		epMap.addStructures(3);		
	}
	@Test(dependsOnMethods ={"testAddStructuresToMap"})
	public void testFillMap(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		selenium = workflow.getSelenium();
		EPExtensions epExt = workflow.getHome().getEPortfolio();		
		// create an artefact to be sure one exists
		EPCollectWizard epWizz = epExt.createTextArtefactAndOpenWizard();
		epWizz.filloutWizardForTextArtefact("a great content for an artefact...", "text artefact", "how great it is");
		EPMapEditor epMap = epExt.getMapEditor();
		epMap.openMapByTitle(SELENIUM_TEST_MAP_TITLE);
		epMap.openFirstPage();
		// attach to first page
		selenium.click("ui=epMap::attachArtefactToPageLink()");
		selenium.waitForPageToLoad("30000");
		// set view to details mode!
		selenium.click("ui=epArtefactSearch::viewModeDetails()");
		selenium.click("ui=epArtefactSearch::firstArtefactChooseLink()");
		selenium.waitForPageToLoad("30000");
		// attach to a structure
		selenium.click("ui=epMap::attachArtefactToStructLink()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=epArtefactSearch::firstArtefactChooseLink()");
		selenium.waitForPageToLoad("30000");
	}
	@Test(dependsOnMethods ={"testFillMap"})
	public void testShareMap(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		selenium = workflow.getSelenium();
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		EPMapEditor epMap = epExt.getMapEditor();
		epMap.openShareDialogForMapByTitle(SELENIUM_TEST_MAP_TITLE);
		epMap.shareToOtherUserWithName("administrator");
	}
	@Test(dependsOnMethods ={"testShareMap"})
	public void testEditMap(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		selenium = workflow.getSelenium();
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		EPMapEditor epMap = epExt.getMapEditor();
		epMap.openMapByTitle(SELENIUM_TEST_MAP_TITLE);
		epMap.openFirstPage();
		epMap.toggleEditMode(true);
		// first page was selected before, edit title&desc
		epMap.changeElementTitleDescription("a new title for this page", "and even a better description! how great is this!");
		// delete all pages
		while (!workflow.isTextPresent("at least one page")){
			if (!epMap.deleteActiveTOCElement()) break;
			// workaround to get to a page again
			epMap.toggleEditMode(false); 
			epMap.openFirstPage();
			epMap.toggleEditMode(true);
		}
		assertTrue(workflow.isTextPresent("at least one page"));
		epMap.toggleEditMode(false);
	}
	
}
