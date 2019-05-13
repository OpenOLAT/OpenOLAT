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
package org.olat.ims.qti21;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.model.ParentPartItemRefs;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
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
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentTestHelper.class);
	
	
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
	
	public static String getAssessmentItemTitle(AssessmentItemRef itemRef, ResolvedAssessmentTest resolvedAssessmentTest) {
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem != null
				&& resolvedAssessmentItem.getItemLookup() != null
				&& resolvedAssessmentItem.getItemLookup().getRootNodeHolder() != null) {
			return resolvedAssessmentItem.getItemLookup().extractIfSuccessful().getTitle();
		}
		return "ERROR";
	}
	
	public static boolean needManualCorrection(ResolvedAssessmentTest resolvedAssessmentTest) {
		AssessmentTest test = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();

		boolean needManualCorrection = false; 
		List<TestPart> parts = test.getChildAbstractParts();
		for(TestPart part:parts) {
			List<AssessmentSection> sections = part.getAssessmentSections();
			for(AssessmentSection section:sections) {
				if(needManualCorrection(section, resolvedAssessmentTest)) {
					needManualCorrection = true;
					break;
				}
			}
		}
		return needManualCorrection;
	}
	
	private static boolean needManualCorrection(AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest) {
		for(SectionPart part: section.getSectionParts()) {
			if(part instanceof AssessmentItemRef) {
				if(needManualCorrection((AssessmentItemRef)part, resolvedAssessmentTest)) {
					return true;
				}
			} else if(part instanceof AssessmentSection) {
				if(needManualCorrection((AssessmentSection) part, resolvedAssessmentTest)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean needManualCorrection(AssessmentItemRef itemRef, ResolvedAssessmentTest resolvedAssessmentTest) {
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem != null
				&& resolvedAssessmentItem.getItemLookup() != null
				&& resolvedAssessmentItem.getItemLookup().getRootNodeHolder() != null) {
			AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
			List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
			for(Interaction interaction:interactions) {
				if(interaction instanceof UploadInteraction
						|| interaction instanceof DrawingInteraction
						|| interaction instanceof ExtendedTextInteraction) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Go through the assessmentTest, visit recursively its test parts, sections and
	 * assessment item refs.
	 * 
	 * @param assessmentTest The assessment test to visit
	 * @param visitor The visitor
	 */
	public static void visit(AssessmentTest assessmentTest, AssessmentTestVisitor visitor) {
		List<TestPart> testParts = assessmentTest.getTestParts();
		if(testParts != null && testParts.size() > 0) {
			for(TestPart testPart:testParts) {
				visitor.visit(testPart);
				
				List<AssessmentSection> sections = testPart.getAssessmentSections();
				if(sections != null && sections.size() > 0) {
					for(AssessmentSection section:sections) {
						visit(section, visitor);
					}
				}
			}
		}
	}
	
	/**
	 * Go through the section part (assessmentSection or assessmentItemRef), visit recursively
	 * the sections and assessment item refs.
	 * 
	 * @param sectionPart
	 * @param visitor
	 */
	public static void visit(SectionPart sectionPart, AssessmentTestVisitor visitor) {
		visitor.visit(sectionPart);
		if(sectionPart instanceof AssessmentSection) {
			AssessmentSection section = (AssessmentSection)sectionPart;
			List<SectionPart> childParts = section.getChildAbstractParts();
			if(childParts != null && childParts.size() > 0) {
				for(SectionPart childPart:childParts) {
					visit(childPart, visitor);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * Initial date: 21 nov. 2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	public interface AssessmentTestVisitor {
		
		public void visit(TestPart testPart);
		
		public void visit(SectionPart sectionPart);

	}
}
