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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.ui.components.AssessmentObjectComponentRenderer.RenderingRequest;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingContext;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
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
	private static final Logger log = Tracing.createLoggerFor(AssessmentTestComponent.class);
	
	private TestSessionController testSessionController;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	
	private boolean renderNavigation;
	private boolean showTitles;
	private boolean personalNotes;
	private final AssessmentTestFormItem qtiItem;
	private final Map<String,Interaction> responseIdentifiersMap = new HashMap<>();
	
	public AssessmentTestComponent(String name, AssessmentTestFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
	}

	public AssessmentTestFormItem getQtiItem() {
		return qtiItem;
	}
	
	@Override
	public boolean isSilentlyDynamicalCmp() {
		return true;
	}

	public boolean isRenderNavigation() {
		return renderNavigation;
	}

	public void setRenderNavigation(boolean renderNavigation) {
		this.renderNavigation = renderNavigation;
	}

	public boolean isShowTitles() {
		return showTitles;
	}

	public void setShowTitles(boolean showTitles) {
		this.showTitles = showTitles;
	}

	public boolean isPersonalNotes() {
		return personalNotes;
	}

	public void setPersonalNotes(boolean personalNotes) {
		this.personalNotes = personalNotes;
	}

	public TestSessionController getTestSessionController() {
		return testSessionController;
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		this.testSessionController = testSessionController;
	}
	
	@Override
	public String getResponseUniqueIdentifier(ItemSessionState itemSessionState, Interaction interaction) {
		TestPlanNodeKey tpnk = null;
		for(Map.Entry<TestPlanNodeKey, ItemSessionState> entry:testSessionController.getTestSessionState().getItemSessionStates().entrySet()) {
			if(entry.getValue() == itemSessionState) {
				tpnk = entry.getKey();
				break;
			}
		}
		
		String id = "oo" + (tpnk.toString().replace(":", "_")) + "_" + interaction.getResponseIdentifier().toString();
		responseIdentifiersMap.put(id, interaction);
		return id;
	}
	
	public boolean validateCommand(String cmd, TestPlanNodeKey tpnk) {
		String id = "oo" + (tpnk.toString().replace(":", "_")) + "_";
		return cmd.contains(id);
	}
	
	public boolean validateRequest(TestPlanNodeKey tpnk) {
		if(tpnk == null) return false;
		
		String id = "oo" + (tpnk.toString().replace(":", "_")) + "_";
		for(String parameter:qtiItem.getRootForm().getRequestParameterSet()) {
			if(parameter.contains(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Interaction getInteractionOfResponseUniqueIdentifier(String responseUniqueId) {
		return responseIdentifiersMap.get(responseUniqueId);
	}

	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return resolvedAssessmentTest;
	}

	public void setResolvedAssessmentTest(ResolvedAssessmentTest resolvedAssessmentTest) {
		this.resolvedAssessmentTest = resolvedAssessmentTest;
	}
	
	/**
	 * Check if the assessment item will show some
	 * form of feedback like feedbackElement, modalFeedback
	 * or message as invalid or bad response.
	 * 
	 * @param itemNode
	 * @return
	 */
	public boolean willShowFeedbacks(TestPlanNode itemNode) {
		if(isHideFeedbacks()) {
			return false;
		}
		
		try {
			URI itemSystemId = itemNode.getItemSystemId();
			ResolvedAssessmentItem resolvedAssessmentItem = getResolvedAssessmentTest()
					.getResolvedAssessmentItemBySystemIdMap().get(itemSystemId);
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			if(assessmentItem.getAdaptive()) {
				return true;
			}
			
			ItemSessionState itemSessionState = getItemSessionState(itemNode.getKey());
			if(!itemSessionState.isResponded()) {
				return true;
			}

			ItemProcessingContext itemContext = getTestSessionController().getItemProcessingContext(itemNode);
			if(itemContext instanceof ItemSessionController) {
				ItemSessionController itemSessionController = (ItemSessionController)itemContext;
				List<Interaction> interactions = itemSessionController.getInteractions();
				for(Interaction interaction:interactions) {
					if(AssessmentRenderFunctions.isBadResponse(itemSessionState, interaction.getResponseIdentifier())) {
						return true;
					}
					if(AssessmentRenderFunctions.isInvalidResponse(itemSessionState, interaction.getResponseIdentifier())) {
						return true;
					}
				}
			}

			if(assessmentItem.getItemBody().willShowFeedback(itemContext)) {
				return true;
			}
			
			List<ModalFeedback> modalFeedbacks = assessmentItem.getModalFeedbacks();
			for(ModalFeedback modalFeedback:modalFeedbacks) {
				if(isFeedback(modalFeedback, itemSessionState)) {
					return true;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		
		return false;
	}
	
	@Override
	public String relativePathTo(ResolvedAssessmentItem resolvedAssessmentItem) {
		if(resolvedAssessmentItem == null) {
			return "/";
		} else {
			URI itemUri = resolvedAssessmentItem.getItemLookup().getSystemId();
			File itemFile = new File(itemUri);
			URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
			File testFile = new File(testUri);
			Path relativePath = testFile.toPath().getParent().relativize(itemFile.toPath().getParent());
			String relativePathString = relativePath.toString();
			if(relativePathString.isEmpty()) {
				return relativePathString;
			} else if(relativePathString.endsWith("/")) {
				return relativePathString;
			}
			return relativePathString.concat("/");
		}
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
			AbstractPart section = testPart.lookupFirstDescendant(identifier);
			if(section instanceof AssessmentSection) {
				return (AssessmentSection)section;
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
