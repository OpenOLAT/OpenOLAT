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
package org.olat.ims.qti21.ui.components;

import java.util.List;

import org.olat.ims.qti21.ui.components.AssessmentTestComponentRenderer.RenderingRequest;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestComponent extends AssessmentObjectComponent  {
	
	private static final AssessmentTestComponentRenderer VELOCITY_RENDERER = new AssessmentTestComponentRenderer();
	
	private TestSessionController testSessionController;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	
	private final AssessmentTestFormItem qtiItem;
	
	public AssessmentTestComponent(String name, AssessmentTestFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
	}

	public AssessmentTestFormItem getQtiItem() {
		return qtiItem;
	}

	public TestSessionController getTestSessionController() {
		return testSessionController;
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		this.testSessionController = testSessionController;
	}

	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return resolvedAssessmentTest;
	}

	public void setResolvedAssessmentTest(ResolvedAssessmentTest resolvedAssessmentTest) {
		this.resolvedAssessmentTest = resolvedAssessmentTest;
	}

	public AssessmentTest getAssessmentTest() {
		return getResolvedAssessmentTest().getRootNodeLookup().extractIfSuccessful();
	}
	
	public boolean hasMultipleTestParts() {
		AssessmentTest assessmentTest = getAssessmentTest();	
		if(assessmentTest.getTestParts().size() > 1) {
			return true;
		}
		return false;
	}
	
	public TestPlanNode getCurrentTestPartNode() {
		TestSessionState sessionState = getTestSessionController().getTestSessionState();
		TestPlanNodeKey testPlanNodeKey = sessionState.getCurrentTestPartKey();
		return sessionState.getTestPlan().getNode(testPlanNodeKey);
	}
	
	public TestPart getTestPart(Identifier identifier) {
		List<TestPart> testParts = getAssessmentTest().getTestParts();
		for(TestPart testPart:testParts) {
			if(testPart.getIdentifier().equals(identifier)) {
				return testPart;
			}
		}
		return null;
	}
	
	public AssessmentSection getAssessmentSection(Identifier identifier) {
		List<TestPart> testParts = getAssessmentTest().getTestParts();
		for(TestPart testPart:testParts) {
			List<AssessmentSection> sections = testPart.getAssessmentSections();
			for(AssessmentSection section:sections) {
				if(section.getIdentifier().equals(identifier)) {
					return section;
				}
			}	
		}
		return null;
	}
	
	public ItemSessionState getItemSessionState(TestPlanNodeKey nodeKey) {
		TestSessionState sessionState = getTestSessionController().getTestSessionState();
		return sessionState.getItemSessionStates().get(nodeKey);
	}
	
	//<xsl:if test="$itemFeedbackAllowed and $sessionStatus='final'">
	//<xsl:variable name="itemFeedbackAllowed" as="xs:boolean"
	//	    select="if ($reviewMode)
	//	      then (/qti:assessentItem/@adaptive='true' or $showFeedback)
	//	      else (not($solutionMode))"/>
	public boolean isItemFeedbackAllowed(TestPlanNode itemNode, AssessmentItem assessmentItem, RenderingRequest options) {
		ItemSessionState itemSessionState = getItemSessionState(itemNode.getKey());
		
		boolean itemFeedbackAllowed = false;
		if(itemSessionState.getSessionStatus() == SessionStatus.FINAL) {
			if(options.isReviewMode()) {
				if(assessmentItem.getAdaptive() || itemNode.getEffectiveItemSessionControl().isShowFeedback()) {
					itemFeedbackAllowed = true;
				}
			} else if(!options.isSolutionMode()) {
				itemFeedbackAllowed = true;
			}
		}
		return itemFeedbackAllowed;
	}
	


	@Override
	public AssessmentTestComponentRenderer getHTMLRendererSingleton() {
		return VELOCITY_RENDERER;
	}
}
