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
package org.olat.ims.qti21.ui.editor.testsexport;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryAssessmentTestSession;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.FeedbackResultFormItem;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestPlanVisitor;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 21.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21OfflineTestsPDFController extends FormBasicController {
	
	private final String mapperUri;
	
	private boolean solution;
	private final String serialNumber;
	private final TestSessionState testSessionState;
	private CandidateSessionContext candidateSessionContext;
	private final TestSessionController testSessionController;

	private final URI assessmentObjectUri;
	private final File assessmentTestFile;
	private final ResourceLocator inputResourceLocator;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final TestsExportContext testsExportContext;

	private int count = 0;
	/**
	 * Prevent to execute the dispose method.
	 */
	private boolean preventDispose = true;

	@Autowired
	private QTI21Service qtiService;
	
	QTI21OfflineTestsPDFController(UserRequest ureq, WindowControl wControl, 
			File fUnzippedDirRoot, String mapperUri, TestsExportContext testsExportContext,
			TestSessionController testSessionController, String serialNumber, boolean solution) {
		super(ureq, wControl, "offline_test", Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		
		this.solution = solution;
		this.mapperUri = mapperUri;
		this.serialNumber = serialNumber;
		this.testsExportContext = testsExportContext;

		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		assessmentTestFile = new File(testUri);
		
		this.testSessionController = testSessionController;
		this.testSessionState = testSessionController.getTestSessionState();
		InMemoryAssessmentTestSession candidateSession = new InMemoryAssessmentTestSession();
		candidateSessionContext = new TerminatedStaticCandidateSessionContext(candidateSession);

		initForm(ureq);
	}
	
	@Override
	public void dispose() {
		if(preventDispose) return;
		
		super.dispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("mapperUri", mapperUri);

			// Add some meta information about the context of this assessment
			RepositoryEntry testRE = testsExportContext.getTestEntry();
			layoutCont.contextPut("testTitle", testRE.getDisplayname());
			layoutCont.contextPut("testId", testRE.getResourceableId());
			layoutCont.contextPut("testExternalRef", testRE.getExternalRef());
			layoutCont.contextPut("exportContext", testsExportContext);
			layoutCont.contextPut("solution", Boolean.valueOf(solution));
			layoutCont.contextPut("serialNumber", serialNumber);
			

			TestPlanInfos testPlanInfos = new TestPlanInfos();
			testSessionController.visitTestPlan(testPlanInfos);
			layoutCont.contextPut("numOfQuestions", testPlanInfos.getNumOfItems());
			if(testPlanInfos.getMaxScore() > 0.0d) {
				layoutCont.contextPut("maxScore", AssessmentHelper.getRoundedScore(testPlanInfos.getMaxScore()));
			} else {
				layoutCont.contextPut("maxScore", testsExportContext.getMaxScoreValue());
			}
			
			Results testResults = new Results(false, "o_qtiassessment_icon", true);
			layoutCont.contextPut("testResults", testResults);

			testResults.setMaxScore(null);//reset max score and aggregate
			initFormSections(layoutCont, testResults);
		}
	}
	
	public void setSolution(boolean solution) {
		this.solution = solution;
		flc.contextPut("solution", Boolean.valueOf(solution));
	}
	
	public void setPreventDispose(boolean preventDispose) {
		this.preventDispose = preventDispose;
	}
	
	private void initFormSections(FormLayoutContainer layoutCont, Results testResults) {
		List<Results> itemResults = new ArrayList<>();
		layoutCont.contextPut("itemResults", itemResults);
		
		Map<Identifier, AssessmentItemRef> identifierToRefs = new HashMap<>();
		for(AssessmentItemRef itemRef:resolvedAssessmentTest.getAssessmentItemRefs()) {
			identifierToRefs.put(itemRef.getIdentifier(), itemRef);
		}

		Map<TestPlanNode, Results> resultsMap = new HashMap<>();

		TestPlan testPlan = testSessionState.getTestPlan();
		List<TestPlanNode> nodes = testPlan.getTestPlanNodeList();
		for(TestPlanNode node:nodes) {
			TestNodeType testNodeType = node.getTestNodeType();
			if(testNodeType == TestNodeType.ASSESSMENT_SECTION) {
				List<FlowFormItem> rubrics = getSectionRubric(layoutCont, node);
				Results r = new Results(true, node.getSectionPartTitle(), rubrics, "o_mi_qtisection", true);
				resultsMap.put(node, r);
				itemResults.add(r);
				testResults.setNumberOfSections(testResults.getNumberOfSections() + 1);
			} else if(testNodeType == TestNodeType.ASSESSMENT_ITEM_REF) {
				Results results = initFormItemResult(layoutCont, node, identifierToRefs, resultsMap);
				if(results != null) {
					itemResults.add(results);
				}
				testResults.setNumberOfQuestions(testResults.getNumberOfQuestions() + 1);
				if(results.hasMaxScore()) {
					testResults.addMaxScore(results);
				}
				
				if(node.getParent() != null) {
					Results parentResults = resultsMap.get(node.getParent());
					if(parentResults != null && parentResults.isSection()) {
						parentResults.setNumberOfQuestions(parentResults.getNumberOfQuestions() + 1);
					}
				}
			}
		}
		
		if(!itemResults.isEmpty()) {
			Results lastResults = itemResults.get(itemResults.size() - 1);
			lastResults.setLast(true);
		}
	}
	
	private List<FlowFormItem> getSectionRubric(FormLayoutContainer layoutCont, TestPlanNode node) {
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		
		List<FlowFormItem> rubricsEls = new ArrayList<>();
		AbstractPart part = assessmentTest.lookupFirstDescendant(node.getIdentifier());
		if(part instanceof AssessmentSection) {
			AssessmentSection section = (AssessmentSection)part;
			for(RubricBlock rubricBlock:section.getRubricBlocks()) {
				String rubricElId = "section_rubric_" + (count++);
				FlowFormItem formItem = new FlowFormItem(rubricElId, assessmentTestFile);
				formItem.setBlocks(rubricBlock.getBlocks());
				formItem.setCandidateSessionContext(candidateSessionContext);
				formItem.setResourceLocator(inputResourceLocator);
				formItem.setAssessmentObjectUri(assessmentObjectUri);
				formItem.setMapperUri(mapperUri);
				
				rubricsEls.add(formItem);
				layoutCont.add(rubricElId, formItem);
			}
		}
		
		return rubricsEls;
	}

	private Results initFormItemResult(FormLayoutContainer layoutCont, TestPlanNode node,
			Map<Identifier, AssessmentItemRef> identifierToRefs, Map<TestPlanNode, Results> resultsMap) {
		TestPlanNodeKey testPlanNodeKey = node.getKey();
		Identifier identifier = testPlanNodeKey.getIdentifier();
		AssessmentItemRef itemRef = identifierToRefs.get(identifier);
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem == null) {
			return new Results(true, node.getSectionPartTitle());
		}
		
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		QTI21QuestionType type = QTI21QuestionType.getType(assessmentItem);
		
		Results r = new Results(false, node.getSectionPartTitle(), null, type.getCssClass(), true);
		r.setItemIdentifier(node.getIdentifier().toString());
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);
		Map<Identifier,Value> outcomeValues = sessionState.getOutcomeValues();
		Double maxScore = getOutcomeNumberVariable(outcomeValues.get(QTI21Constants.MAXSCORE_IDENTIFIER));
		if(maxScore != null) {
			r.setMaxScore(maxScore);
		}
		
		//update max score of section
		InteractionResults interactionResults = initFormItemInteractions(layoutCont, sessionState, resolvedAssessmentItem);
		r.setInteractionResults(interactionResults);

		String correctSolutionId = "correctSolutionItem" + count++;
		FeedbackResultFormItem correctSolutionItem = new FeedbackResultFormItem(correctSolutionId, resolvedAssessmentItem);
		initInteractionResultFormItem(correctSolutionItem, sessionState);
		layoutCont.add(correctSolutionId, correctSolutionItem);
		r.setCorrectSolutionItem(correctSolutionItem);
		
		updateSectionScoreInformations(node, r, resultsMap);
		return r;
	}
	
	private void updateSectionScoreInformations(TestPlanNode node, Results assessmentItemResults, Map<TestPlanNode, Results> resultsMap) {
		if(node.getParent() == null && resultsMap.get(node.getParent()) == null) return;

		TestPlanNode section = node.getParent();
		Results sectionResults = resultsMap.get(section);
		if(sectionResults != null) {
			sectionResults.addSubResults(assessmentItemResults);
			if(assessmentItemResults.hasMaxScore()) {
				sectionResults.addMaxScore(assessmentItemResults);
			}
		}
	}

	private InteractionResults initFormItemInteractions(FormLayoutContainer layoutCont, ItemSessionState sessionState,
			ResolvedAssessmentItem resolvedAssessmentItem) {
		String solutionId = "solutionItem" + count++;
		ItemBodyResultFormItem solutionFormItem = new ItemBodyResultFormItem(solutionId, resolvedAssessmentItem);
		solutionFormItem.setShowSolution(true);
		solutionFormItem.setScorePerAnswers(solution);
		solutionFormItem.setReport(true);
		initInteractionResultFormItem(solutionFormItem, sessionState);
		layoutCont.add(solutionId, solutionFormItem);

		// response
		String responseId = "responseItem" + count++;
		ItemBodyResultFormItem responseFormItem = new ItemBodyResultFormItem(responseId, resolvedAssessmentItem);
		initInteractionResultFormItem(responseFormItem, sessionState);
		layoutCont.add(responseId, responseFormItem);

		return new InteractionResults(responseFormItem, solutionFormItem);
	}
	
	private void initInteractionResultFormItem(ItemBodyResultFormItem formItem, ItemSessionState sessionState) {
		formItem.setItemSessionState(sessionState);
		formItem.setCandidateSessionContext(candidateSessionContext);
		formItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		formItem.setResourceLocator(inputResourceLocator);
		formItem.setAssessmentObjectUri(assessmentObjectUri);
		formItem.setMapperUri(mapperUri);
	}
	
	private void initInteractionResultFormItem(FeedbackResultFormItem formItem, ItemSessionState sessionState) {
		formItem.setItemSessionState(sessionState);
		formItem.setCandidateSessionContext(candidateSessionContext);
		formItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		formItem.setResourceLocator(inputResourceLocator);
		formItem.setAssessmentObjectUri(assessmentObjectUri);
		formItem.setMapperUri(mapperUri);
	}
	
	private Double getOutcomeNumberVariable(Value value) {
		if(value instanceof NumberValue) {
			return ((NumberValue)value).doubleValue();
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public static class InteractionResults {
		private final FormItem responseFormItem;
		private final FormItem solutionFormItem;
		
		public InteractionResults(FormItem responseFormItem, FormItem solutionFormItem) {
			this.responseFormItem = responseFormItem;
			this.solutionFormItem = solutionFormItem;
		}
		
		public FormItem getResponseFormItem() {
			return responseFormItem;
		}
		
		public FormItem getSolutionFormItem() {
			return solutionFormItem;
		}
	}
	
	public class Results {
		
		private Double maxScore;
		private Double cutValue;
		private Boolean pass;
		private String comment;
		
		private String itemIdentifier;
		private final String title;
		private final String cssClass;
		private final boolean section;
		private final boolean metadataVisible;
		
		private Long itemSessionKey;
		
		private int numberOfSections = 0;
		private int numberOfQuestions = 0;
		
		private boolean deleted = false;
		private boolean last = false;
		
		private FormItem correctSolutionItem;
		private final List<FlowFormItem> rubrics;
		private InteractionResults interactionResults;
		private final List<Results> subResults = new ArrayList<>();
		
		public Results(boolean deleted, String title) {
			this.deleted = deleted;
			this.title = title;
			this.section = false;
			this.rubrics = null;
			this.metadataVisible = false;
			this.cssClass = null;
		}
		
		public Results(boolean section, String cssClass, boolean metadataVisible) {
			this.section = section;
			this.cssClass = cssClass;
			this.metadataVisible = metadataVisible;
			this.title = null;
			this.rubrics = null;
		}

		public Results(boolean section, String title, List<FlowFormItem> rubrics, String cssClass, boolean metadataVisible) {
			this.section = section;
			this.title = title;
			this.rubrics = rubrics;
			this.cssClass = cssClass;
			this.metadataVisible = metadataVisible;
		}
		
		public void setItemIdentifier(String itemIdentifier) {
			this.itemIdentifier = itemIdentifier;
		}
		
		public String getItemIdentifier() {
			return itemIdentifier;
		}

		public Long getItemSessionKey() {
			return itemSessionKey;
		}

		public void setItemSessionKey(Long itemSessionKey) {
			this.itemSessionKey = itemSessionKey;
		}
		
		public boolean isDeleted() {
			return deleted;
		}
		
		public boolean isMetadataVisible() {
			return metadataVisible;
		}
		
		public boolean hasInteractions() {
			return interactionResults != null
					&& (interactionResults.getResponseFormItem() != null || interactionResults.getSolutionFormItem() != null);
		}
		
		public boolean hasResponses() {
			return interactionResults != null && interactionResults.getResponseFormItem() != null;
		}
		
		public boolean hasSolutions() {
			return interactionResults != null && interactionResults.getSolutionFormItem() != null;
		}
		
		public String getCssClass() {
			return cssClass;
		}
		
		public String getTitle() {
			return title;
		}
		
		public List<FlowFormItem> getRubrics() {
			return rubrics;
		}
		
		public boolean isSection() {
			return section;
		}
		
		public boolean hasSectionInformations() {
			return numberOfQuestions > 0;
		}
		
		public boolean hasMaxScore() {
			return maxScore != null;
		}
		
		public boolean maxScoreSingular() {
			return maxScore < 1.1d; 
		}
		
		public String getMaxScore() {
			return maxScore == null ? "" : AssessmentHelper.getRoundedScore(maxScore);
		}

		public void setMaxScore(Double maxScore) {
			this.maxScore = maxScore;
		}
		
		public void addMaxScore(Results results) {
			if(results.hasMaxScore()) {
				if(maxScore == null) {
					maxScore = 0.0d;
				}
				maxScore = maxScore.doubleValue() + results.maxScore.doubleValue();
			}
		}
		
		public Double getCutValue() {
			return cutValue;
		}

		public String getCutPercent() {
			if(maxScore == null) return null;
			if(cutValue == null) return "0";
			
			double percent = (cutValue / maxScore) * 100.0d;
			long percentLong = Math.round(percent);	
			return Long.toString(percentLong);
		}
		
		public void setCutValue(Double cutValue) {
			this.cutValue = cutValue;
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

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public int getNumberOfQuestions() {
			return numberOfQuestions;
		}

		public void setNumberOfQuestions(int numberOfQuestions) {
			this.numberOfQuestions = numberOfQuestions;
		}
		
		public int getNumberOfSections() {
			return numberOfSections;
		}

		public void setNumberOfSections(int numberOfSections) {
			this.numberOfSections = numberOfSections;
		}

		public List<Results> getSubResults() {
			return subResults;
		}
		
		public void addSubResults(Results results) {
			subResults.add(results);
		}

		public FormItem getCorrectSolutionItem() {
			return correctSolutionItem;
		}

		public void setCorrectSolutionItem(FormItem correctSolutionItem) {
			this.correctSolutionItem = correctSolutionItem;
		}

		public InteractionResults getInteractionResults() {
			return interactionResults;
		}
		
		public void setInteractionResults(InteractionResults interactionResults) {
			this.interactionResults = interactionResults;
		}

		public boolean isLast() {
			return last;
		}

		public void setLast(boolean last) {
			this.last = last;
		}
	}
	
	public class TestPlanInfos implements TestPlanVisitor {
		
		private int numOfItems = 0;
		
		private double maxScore = 0.0d;
		
		@Override
		public void visit(TestPlanNode testPlanNode) {
			final TestNodeType type = testPlanNode.getTestNodeType();
			if(type == TestNodeType.ASSESSMENT_ITEM_REF) {
				numOfItems++;
				
				ItemSessionState state = testSessionController.getTestSessionState()
						.getItemSessionStates().get(testPlanNode.getKey());

				Value maxScoreValue = state.getOutcomeValue(QTI21Constants.MAXSCORE_IDENTIFIER);
				if(maxScoreValue instanceof FloatValue) {
					maxScore += ((FloatValue)maxScoreValue).doubleValue();
				}
			}
		}
		
		public int getNumOfItems() {
			return numOfItems;
		}
		
		public double getMaxScore() {
			return maxScore;
		}
	}
}