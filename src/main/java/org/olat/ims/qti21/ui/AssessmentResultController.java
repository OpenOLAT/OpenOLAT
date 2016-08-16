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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.InteractionResultFormItem;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.PositionObjectInteraction;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.node.result.TestResult;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.AssessmentSectionSessionState;
import uk.ac.ed.ph.jqtiplus.state.ControlObjectSessionState;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 21.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentResultController extends FormBasicController {

	private final String mapperUri;
	private final ShowResultsOnFinish resultsOnfinish;
	
	private final TestSessionState testSessionState;
	private final AssessmentResult assessmentResult;
	private final CandidateSessionContext candidateSessionContext;

	private final URI assessmentObjectUri;
	private final ResourceLocator inputResourceLocator;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final UserShortDescription assessedIdentityInfosCtrl;
	
	private int count = 0;
	
	@Autowired
	private QTI21Service qtiService;
	
	public AssessmentResultController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity,
			AssessmentTestSession candidateSession, ShowResultsOnFinish resultsOnfinish, File fUnzippedDirRoot, String mapperUri) {
		super(ureq, wControl, "assessment_results");
		this.mapperUri = mapperUri;
		this.resultsOnfinish = resultsOnfinish;

		
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		
		assessedIdentityInfosCtrl = new UserShortDescription(ureq, getWindowControl(), assessedIdentity);
		listenTo(assessedIdentityInfosCtrl);
		
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false);
		
		testSessionState = qtiService.loadTestSessionState(candidateSession);
		assessmentResult = qtiService.getAssessmentResult(candidateSession);
		candidateSessionContext = new TerminatedStaticCandidateSessionContext(candidateSession);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.put("assessedIdentityInfos", assessedIdentityInfosCtrl.getInitialComponent());
			
			Results results = new Results(false, "o_qtiassessment_icon");
			results.setSessionState(testSessionState);
			
			layoutCont.contextPut("testResults", results);
			TestResult testResult = assessmentResult.getTestResult();
			if(testResult != null) {
				extractOutcomeVariable(testResult.getItemVariables(), results);
			}

			if(resultsOnfinish == ShowResultsOnFinish.sections || resultsOnfinish == ShowResultsOnFinish.details) {
				initFormSections(layoutCont);
			}
		}
	}
	
	private void initFormSections(FormLayoutContainer layoutCont) {
		List<Results> itemResults = new ArrayList<>();
		layoutCont.contextPut("itemResults", itemResults);
		
		Map<Identifier, AssessmentItemRef> identifierToRefs = new HashMap<>();
		for(AssessmentItemRef itemRef:resolvedAssessmentTest.getAssessmentItemRefs()) {
			identifierToRefs.put(itemRef.getIdentifier(), itemRef);
		}

		TestPlan testPlan = testSessionState.getTestPlan();
		List<TestPlanNode> nodes = testPlan.getTestPlanNodeList();
		for(TestPlanNode node:nodes) {
			TestPlanNodeKey testPlanNodeKey = node.getKey();
			TestNodeType testNodeType = node.getTestNodeType();
			if(testNodeType == TestNodeType.ASSESSMENT_SECTION) {
				Results r = new Results(true, node.getSectionPartTitle(), "o_mi_qtisection");
				AssessmentSectionSessionState sectionState = testSessionState.getAssessmentSectionSessionStates().get(testPlanNodeKey);
				if(sectionState != null) {
					r.setSessionState(sectionState);
				}
				itemResults.add(r);
			} else if(testNodeType == TestNodeType.ASSESSMENT_ITEM_REF) {
				if(resultsOnfinish == ShowResultsOnFinish.details) {
					Results results = initFormItemResult(layoutCont, node, identifierToRefs);
					if(results != null) {
						itemResults.add(results);
					}
				}
			}
		}
	}

	private Results initFormItemResult(FormLayoutContainer layoutCont, TestPlanNode node, Map<Identifier, AssessmentItemRef> identifierToRefs) {
		TestPlanNodeKey testPlanNodeKey = node.getKey();
		Identifier identifier = testPlanNodeKey.getIdentifier();
		AssessmentItemRef itemRef = identifierToRefs.get(identifier);
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		QTI21QuestionType type = QTI21QuestionType.getType(assessmentItem);
		
		Results r = new Results(false, type.getCssClass());
		r.setTitle(node.getSectionPartTitle());
		r.setSessionStatus("");//init
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);
		if(sessionState != null) {
			r.setSessionState(sessionState);
			SessionStatus sessionStatus = sessionState.getSessionStatus();
			if(sessionState != null) {
				r.setSessionStatus(translate("results.session.status." + sessionStatus.toQtiString()));
			}
		}
		
		ItemResult itemResult = assessmentResult.getItemResult(identifier.toString());
		if(itemResult != null) {
			extractOutcomeVariable(itemResult.getItemVariables(), r);
		}

		//loop interactions, show response and solution
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(interaction instanceof PositionObjectInteraction || interaction instanceof EndAttemptInteraction) {
				continue;
			}
			
			//response
			String responseId = "responseItem" + count++;
			InteractionResultFormItem responseFormItem = new InteractionResultFormItem(responseId, interaction, resolvedAssessmentItem);
			initInteractionResultFormItem(responseFormItem, sessionState);
			layoutCont.add(responseId, responseFormItem);
	
			//solution
			String solutionId = "solutionItem" + count++;
			InteractionResultFormItem solutionFormItem = new InteractionResultFormItem(solutionId, interaction, resolvedAssessmentItem);
			solutionFormItem.setShowSolution(true);
			initInteractionResultFormItem(solutionFormItem, sessionState);
			layoutCont.add(solutionId, solutionFormItem);
			
			r.getInteractionResults().add(new InteractionResults(responseFormItem, solutionFormItem));
		}
		return r;
	}
	
	private void initInteractionResultFormItem(InteractionResultFormItem responseFormItem, ItemSessionState sectionState) {
		responseFormItem.setItemSessionState(sectionState);
		responseFormItem.setCandidateSessionContext(candidateSessionContext);
		responseFormItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		responseFormItem.setResourceLocator(inputResourceLocator);
		responseFormItem.setAssessmentObjectUri(assessmentObjectUri);
		responseFormItem.setMapperUri(mapperUri);
	}
	
	private void extractOutcomeVariable(List<ItemVariable> itemVariables, Results results) {
		for(ItemVariable itemVariable:itemVariables) {
			if(itemVariable instanceof OutcomeVariable) {
				if(QTI21Constants.SCORE_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setScore(getOutcomeNumberVariable(itemVariable));
				} else if(QTI21Constants.MAXSCORE_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setMaxScore(getOutcomeNumberVariable(itemVariable));
				} else if(QTI21Constants.PASS_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setPass(getOutcomeBooleanVariable(itemVariable));
				}
			}
		}
	}
	
	private String getOutcomeNumberVariable(ItemVariable outcomeVariable) {
		Value value = outcomeVariable.getComputedValue();
		if(value instanceof NumberValue) {
			return AssessmentHelper.getRoundedScore(((NumberValue)value).doubleValue());
		}
		return null;
	}
	
	private Boolean getOutcomeBooleanVariable(ItemVariable outcomeVariable) {
		Value value = outcomeVariable.getComputedValue();
		if(value instanceof BooleanValue) {
			return ((BooleanValue)value).booleanValue();
		}
		return null;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public class InteractionResults {
		private InteractionResultFormItem responseFormItem;
		private InteractionResultFormItem solutionFormItem;

		public InteractionResults() {
			//
		}
		
		public InteractionResults(InteractionResultFormItem responseFormItem, InteractionResultFormItem solutionFormItem) {
			this.responseFormItem = responseFormItem;
			this.solutionFormItem = solutionFormItem;
		}
		
		public InteractionResultFormItem getResponseFormItem() {
			return responseFormItem;
		}
		
		public void setResponseFormItem(InteractionResultFormItem responseFormItem) {
			this.responseFormItem = responseFormItem;
		}
		
		public InteractionResultFormItem getSolutionFormItem() {
			return solutionFormItem;
		}
		
		public void setSolutionFormItem(InteractionResultFormItem solutionFormItem) {
			this.solutionFormItem = solutionFormItem;
		}
	}
	
	public class Results {
		
		private Date entryTime;
		private Date endTime;
		private Long duration;
		
		private String score;
		private String maxScore;
		
		private Boolean pass;
		
		private boolean section;
		private String title;
		private String cssClass;
		
		private String sessionStatus;
		
		private final List<InteractionResults> interactionResults = new ArrayList<>();
		
		public Results(boolean section, String cssClass) {
			this.section = section;
			this.cssClass = cssClass;
		}
		
		public Results(boolean section, String title, String cssClass) {
			this.section = section;
			this.title = title;
			this.cssClass = cssClass;
		}
		
		public void setSessionState(ControlObjectSessionState sessionState) {
			entryTime = sessionState.getEntryTime();
			endTime = sessionState.getEndTime();
			duration = sessionState.getDurationAccumulated();
		}
		
		public String getCssClass() {
			return cssClass;
		}
		
		public String getTitle() {
			return title;
		}
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public boolean isSection() {
			return section;
		}
		
		public void setSection(boolean section) {
			this.section = section;
		}

		public Date getEntryTime() {
			return entryTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public Long getDuration() {
			return duration;
		}
		
		public boolean hasScore() {
			return StringHelper.containsNonWhitespace(score);
		}
		
		public String getScore() {
			return score;
		}

		public void setScore(String score) {
			this.score = score;
		}
		
		public boolean hasMaxScore() {
			return StringHelper.containsNonWhitespace(maxScore);
		}
		
		public String getMaxScore() {
			return maxScore;
		}

		public void setMaxScore(String maxScore) {
			this.maxScore = maxScore;
		}

		public boolean hasPass() {
			return pass != null;
		}
		
		public Boolean getPass() {
			return pass;
		}

		public void setPass(Boolean pass) {
			this.pass = pass;
		}

		public String getSessionStatus() {
			return sessionStatus == null ? "" : sessionStatus;
		}

		public void setSessionStatus(String sessionStatus) {
			this.sessionStatus = sessionStatus;
		}

		public List<InteractionResults> getInteractionResults() {
			return interactionResults;
		}
	}
}