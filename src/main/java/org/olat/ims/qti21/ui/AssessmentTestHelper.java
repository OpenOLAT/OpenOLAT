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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.model.ParentPartItemRefs;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 15.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestHelper {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentTestHelper.class);
	
	
	public static ParentPartItemRefs getParentSection(TestPlanNodeKey itemKey,
			TestSessionState testSessionState,
			ResolvedAssessmentTest resolvedAssessmentTest) {
		
		ParentPartItemRefs parentParts = new ParentPartItemRefs();

		try {
			TestPlanNode currentItem = testSessionState.getTestPlan().getNode(itemKey);
			List<AssessmentItemRef> itemRefs = resolvedAssessmentTest
					.getItemRefsBySystemIdMap().get(currentItem.getItemSystemId());
			
			AssessmentItemRef itemRef = null;
			if(itemRefs.size() == 1) {
				itemRef = itemRefs.get(0);
			} else {
				Identifier itemId = itemKey.getIdentifier();
				for(AssessmentItemRef ref:itemRefs) {
					if(ref.getIdentifier().equals(itemId)) {
						itemRef = ref;
						break;
					}
				}
			}
			
			if(itemRef != null) {
				for(QtiNode parentPart=itemRef.getParent(); parentPart != null; parentPart = parentPart.getParent()) {
					if(parentParts.getSectionIdentifier() == null && parentPart instanceof AssessmentSection) {
						AssessmentSection section = (AssessmentSection)parentPart;
						parentParts.setSectionIdentifier(section.getIdentifier().toString());
					} else if(parentParts.getTestPartIdentifier() == null && parentPart instanceof TestPart) {
						TestPart testPart = (TestPart)parentPart;
						parentParts.setTestPartIdentifier(testPart.getIdentifier().toString());
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		
		return parentParts;
		
	}

}
