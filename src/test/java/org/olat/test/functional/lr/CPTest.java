package org.olat.test.functional.lr;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.CPResourceEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests the CP Editor.
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. clean-up: Delete CPs starting with CP_NAME_SUFFFIX. <br/>
 * 2. author creates cp CP_NAME  <br/>
 * 
 * Test case: <br/>
 * 1. login as author and go to tab learning resources <br/>
 * 2. click "cp" in toolbox "create" <br/>
 * 3. enter titel CPName	<br/>
 * 4. enter description CPDescription <br/>
 * 5. click save <br/>
 * 6. click next <br/>
 * 7. open the editor? click yes <br/>
 * 8. assure is visible "Lorem Ipsum" <br/>
 * 9. rename page "Neue Seite" to "fi&rst paäge" <br/> 
 * 10. click "add page" <br/> <br/>
 * 11. click edit "page properties" and rename added page to "renamed page" <br/>
 * 12. copy "renamed page" <br/>
 * 13. assure exists page "renamed page copy" <br/>
 * 12. move "renamed page copy" to the same hierarchy as the "fi&rst paäge" <br/> ??? possible to drag&drop with selenium?
 * 13. delete "renamed page copy" <br/>
 * 14. click "delete menu element and files" <br/>
 * 15. click "import page" <br/>
 * 16. click select file <br/> 
 * 17. choose cptest.html and click open <br/> 
 * 18. click "Import" <br/>
 * 19. assert that "cptest.html" is displayed (in the file navigation on the lefthand side)   <br/>
 * 20. click symbol "insert/edit image" <br/> 
 * 21. click "Browse" <br/> 
 * 22. click "Upload file" <br/> 
 * 23. click Select file and select OLATteam.jpg <br/> 
 * 24. click "Upload" <br/>
 * 25. add Image description = OLAT Team, click "Insert" <br/>
 * 26. click "save" <br/>
 * 27. delete cp  <br/>
 *  
 * </p>
 * 
 * @author Kristina Isacson / Lavinia Dumitrescu
 *
 */

public class CPTest extends BaseSeleneseTestCase {
	
	private final String CP_NAME = "CP"+System.currentTimeMillis();
	private final String CP_DESCRIPTION = "CPDescription"+System.currentTimeMillis();
	private final String CP_TEXT = "Lorem Ipsum";
	private final String CP_PAGE_DEFAULT_TITLE = "New page";
	private final String CP_PAGE_SPECIAL_TITLE = "fi&rst paäge";
	private final String CP_PAGE_TITLE1 = "renamed page";
	private final String CP_PAGE_CONTENT1 = "page content";
	private final String CP_PAGE_TITLE1_COPY = "renamed page copy"; //default title for o copy
	private final String FILE_NAME1 = "cptest.html";
	private final String FILE_NAME2 = "OLATteam.jpg";
	private final String IMAGE_DESCRIPTION = "OLAT Team";
	
	@Override
	public void setUp() throws Exception {		
		super.setUp();
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		// delete all LR from this author
		WorkflowHelper.deleteAllLearningResourcesFromAuthor(context.getStandardAuthorOlatLoginInfos(1).getUsername());
	}



	public void testCP() throws Exception {	
		
		OLATWorkflowHelper olatWorkflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		CPResourceEditor cPResourceEditor = olatWorkflow.getLearningResources().createCPAndStartEditing(CP_NAME, CP_DESCRIPTION);
		
		//select iframe 
		cPResourceEditor.getSelenium().selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		assertTrue("Asserts is text present: " + CP_TEXT, cPResourceEditor.isTextPresent(CP_TEXT));
		cPResourceEditor.getSelenium().selectFrame("relative=top");	
				
		cPResourceEditor.changeTitle(CP_PAGE_DEFAULT_TITLE, CP_PAGE_SPECIAL_TITLE);
		cPResourceEditor.addPageAndRename(CP_PAGE_SPECIAL_TITLE, CP_PAGE_TITLE1, CP_PAGE_CONTENT1);
		cPResourceEditor.copyPage(CP_PAGE_TITLE1);
		assertTrue("Asserts a copy exists", cPResourceEditor.isTextPresent(CP_PAGE_TITLE1_COPY));
		
		//move CP_PAGE_TITLE1_COPY as root child
		//cPResourceEditor.movePage(CP_PAGE_TITLE1, CP_PAGE_TITLE1_COPY);
		
		cPResourceEditor.deletePage(CP_PAGE_TITLE1_COPY, false);

		File file = WorkflowHelper.locateFile(FILE_NAME1);
		String remoteHtml = Context.getContext().provideFileRemotely(file);
		cPResourceEditor.importPage(CP_PAGE_SPECIAL_TITLE, remoteHtml);
		assertTrue("Asserts that file was imported", cPResourceEditor.isTextPresent(FILE_NAME1));
		
		//insert image		
		file = WorkflowHelper.locateFile(FILE_NAME2);
		remoteHtml = Context.getContext().provideFileRemotely(file);
		cPResourceEditor.insertImage(remoteHtml, IMAGE_DESCRIPTION);
		System.out.println("image inserted");		
		
	}



	@Override
	protected void cleanUpAfterRun() {
		WorkflowHelper.deleteLearningResources(Context.getContext().getStandardAuthorOlatLoginInfos(1).getUsername(), CP_NAME);
	}
	
	
}
