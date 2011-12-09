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
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates glossary, starts editor, closes editor, glossary is deleted <br/>
 * <p>
 * Test setup: <br/>
 * 1. cleanup <br/>
 * 2. Author creates glossary <br/>
 * 3. glossary is deleted <br/>
 * 
 * Test case: <br/> 
 * 1. Author creates glossary <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. glossary is deleted<br/>
 * </p>
 * 
 * @author kristina
 */
public class lr_createGlossaryTest extends BaseSeleneseTestCase {
	
	private final String GLOSSARY_NAME = "GlossaryName" + System.currentTimeMillis();;
	
	
	public void testlr_createGlossaryTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		
		LearningResources learningResources = olatWorkflow.getLearningResources();
		learningResources.createGlossaryAndStartEditing(GLOSSARY_NAME, "GlossaryDescription");
		
		//cleanup
		learningResources = olatWorkflow.getLearningResources();
		LRDetailedView lRDetailedView = learningResources.searchMyResource(GLOSSARY_NAME);
		lRDetailedView.deleteLR();						
	}
}
