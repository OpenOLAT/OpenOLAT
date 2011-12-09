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
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates questionnaire, starts editor, closes editor, questionnaire is deleted <br/>
 * <p>
 * Test setup: <br/>
 * 1. Author creates questionnaire <br/>
 * 2. questionnaire is deleted <br/> 
 * 
 * Test case: <br/>
 * 1. Author creates questionnaire <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. questionnaire is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createQuestionnaireTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createQuestionnaireTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		OLATWorkflowHelper olatWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());		
		TestEditor testEditor = olatWorkflowHelper.getLearningResources().createQuestionnaireAndStartEditing("QuestionnaireName", "QuestionnaireDescription");
		LRDetailedView lRDetailedView = testEditor.close();
		
		//questionnaire is deleted
		lRDetailedView.deleteLR();		
	}
}
