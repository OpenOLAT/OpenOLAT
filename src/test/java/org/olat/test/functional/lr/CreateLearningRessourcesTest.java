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
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.lr.CPResourceEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.ResourceEditor;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates learning resources, learning resources are deleted <br/> 
 * <p>
 * Test setup: <br/>
 * 1. Author creates all learning resources <br/>
 * 2. Cleanup in the beginning and end: All learning resources are deleted <br/>
 * 
 * Test case: <br/>
 * 1. Author creates learning resource (course, glossary, questionnaire, resource folder, test, wiki, cp, blog, podcast, todo: eportfolio template)<br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. Learning resources course, questionnaire and test are copied
 * 5. Learning resources are deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class CreateLearningRessourcesTest extends BaseSeleneseTestCase {
	private final String GLOSSARY_NAME = "GlossaryName" + System.currentTimeMillis();;	
	private final String CP_NAME = "CP"+System.currentTimeMillis();
	private final String CP_DESCRIPTION = "CPDescription"+System.currentTimeMillis();
	private final String BLOG_TITLE = "My blog title";
	private final String PODCAST_TITLE = "My podcast title";
	private final String DESC = "My first lr";
	private final String COPY_TITLE = "Copy";
	private final String COPY_DESCRIPTION = "Copy Description";
	
	//create course 
	public void testCreateCourseTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		LearningResources learningResources = olatWorkflow.getLearningResources();
		CourseEditor courseEditor = learningResources.createCourseAndStartEditing("CourseName", "CourseDescription");
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		//copy course
		lRDetailedView.copyLR(COPY_TITLE, COPY_DESCRIPTION);
    			
	}


	
	//create glossary
	public void testCreateGlossaryTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);	
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		LearningResources learningResources = olatWorkflow.getLearningResources();
		learningResources.createGlossaryAndStartEditing(GLOSSARY_NAME, "GlossaryDescription");
		
		//cleanup
		learningResources = olatWorkflow.getLearningResources();
		learningResources.searchMyResource(GLOSSARY_NAME);
								
	}	

	//create questionnaire
	public void testCreateQuestionnaireTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		OLATWorkflowHelper olatWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());		
		TestEditor testEditor = olatWorkflowHelper.getLearningResources().createQuestionnaireAndStartEditing("QuestionnaireName", "QuestionnaireDescription");
		LRDetailedView lRDetailedView = testEditor.close();
		//copy questionnaire
		lRDetailedView.copyLR(COPY_TITLE, COPY_DESCRIPTION);		
	}
	
	//create resource folder
	public void testCreateResourcefolderTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		ResourceEditor resourceEditor = olatWorkflow.getLearningResources().createResourceFolderAndStartEditing("ResourcefolderName", "ResourcefolderDescription");
		resourceEditor.close();	
		
	}
	
	//create test
	public void testCreateTestTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		OLATWorkflowHelper olatWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());	
		TestEditor testEditor = olatWorkflowHelper.getLearningResources().createTestAndStartEditing("TestName", "TestDescription");
		LRDetailedView lRDetailedView = testEditor.close();	
		//copy test
		lRDetailedView.copyLR(COPY_TITLE, COPY_DESCRIPTION);
	}

	//create wiki
	public void testCreateWikiTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
				
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		olatWorkflow.getLearningResources().createResource("WikiName", "WikiDescription", LR_Types.WIKI);

	}

	//create cp content
	public void testCP() throws Exception {	
		
		OLATWorkflowHelper olatWorkflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		olatWorkflow.getLearningResources().createCPAndStartEditing(CP_NAME, CP_DESCRIPTION);
	}	
	
	//create blog and podcast
	public void testCreateLRBlogPodcast() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		// create blog 
		LearningResources lr1 = olatWorkflow.getLearningResources();
		lr1.createResource(BLOG_TITLE, DESC, LearningResources.LR_Types.BLOG);
		
		//create podcast
		lr1.createResource(PODCAST_TITLE, DESC, LearningResources.LR_Types.PODCAST);
	}

}
