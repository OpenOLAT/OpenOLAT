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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.render.ValidationResult;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingContext;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 01.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTreeComponent extends AssessmentObjectComponent {
	
	private static final AssessmentTreeComponentRenderer VELOCITY_RENDERER = new AssessmentTreeComponentRenderer();
	
	private TestSessionController testSessionController;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	
	private boolean showTitles;
	private final AssessmentTreeFormItem qtiItem;
	
	public AssessmentTreeComponent(String name, AssessmentTreeFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
	}
	
	public boolean isShowTitles() {
		return showTitles;
	}

	public void setShowTitles(boolean showTitles) {
		this.showTitles = showTitles;
	}
	
	@Override
	public String relativePathTo(ResolvedAssessmentItem resolvedAssessmentItem) {
		return "";
	}

	@Override
	public AssessmentTreeFormItem getQtiItem() {
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

	@Override
	public String getResponseUniqueIdentifier(ItemSessionState itemSessionState, Interaction interaction) {
		return null;
	}

	@Override
	public Interaction getInteractionOfResponseUniqueIdentifier(String responseUniqueId) {
		return null;
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
		return testPlanNodeKey == null ? null : sessionState.getTestPlan().getNode(testPlanNodeKey);
	}
	
	public ItemSessionState getItemSessionState(TestPlanNodeKey nodeKey) {
		TestSessionState sessionState = getTestSessionController().getTestSessionState();
		return sessionState.getItemSessionStates().get(nodeKey);
	}
	
	public ItemProcessingContext getItemSessionState(TestPlanNode itemRefNode) {
		ItemProcessingContext itemProcessingContext = getTestSessionController().getItemProcessingContext(itemRefNode);
		return itemProcessingContext;
	}

	@Override
	protected void loadJavascripts(ValidationResult vr) {
		//don't load them
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		super.doDispatchRequest(ureq);
	}

	@Override
	public AssessmentObjectComponentRenderer getHTMLRendererSingleton() {
		return VELOCITY_RENDERER;
	}
	
	

}
