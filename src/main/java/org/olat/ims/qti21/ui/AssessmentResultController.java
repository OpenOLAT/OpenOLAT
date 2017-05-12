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

import javax.servlet.http.HttpServletRequest;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.InteractionResultFormItem;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.PositionObjectInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
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
import uk.ac.ed.ph.jqtiplus.state.marshalling.ItemSessionStateXmlMarshaller;
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
	private String signatureMapperUri;
	private final String submissionMapperUri;
	private final QTI21AssessmentResultsOptions options;
	
	private final boolean anonym;
	private final boolean withPrint;
	private final boolean withTitle;
	private final Identity assessedIdentity;
	private final TestSessionState testSessionState;
	private final AssessmentResult assessmentResult;
	private final AssessmentTestSession candidateSession;
	private final CandidateSessionContext candidateSessionContext;

	private final File fUnzippedDirRoot;
	private final URI assessmentObjectUri;
	private final ResourceLocator inputResourceLocator;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private UserShortDescription assessedIdentityInfosCtrl;
	
	private int count = 0;
	
	@Autowired
	private QTI21Service qtiService;
	
	public AssessmentResultController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean anonym,
			AssessmentTestSession candidateSession, File fUnzippedDirRoot, String mapperUri, String submissionMapperUri,
			QTI21AssessmentResultsOptions options, boolean withPrint, boolean withTitle) {
		super(ureq, wControl, "assessment_results");
		
		this.anonym = anonym;
		this.options = options;
		this.mapperUri = mapperUri;
		this.withPrint = withPrint;
		this.withTitle = withTitle;
		this.assessedIdentity = assessedIdentity;
		this.candidateSession = candidateSession;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.submissionMapperUri = submissionMapperUri;

		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		if(!anonym && assessedIdentity != null) {
			assessedIdentityInfosCtrl = new UserShortDescription(ureq, getWindowControl(), assessedIdentity);
			listenTo(assessedIdentityInfosCtrl);
		}
		
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		
		File signature = qtiService.getAssessmentResultSignature(candidateSession);
		if(signature != null) {
			signatureMapperUri = registerCacheableMapper(ureq, "QTI21Signature::" + CodeHelper.getForeverUniqueID(),
					new SignatureMapper(signature));
		}
		
		testSessionState = qtiService.loadTestSessionState(candidateSession);
		assessmentResult = qtiService.getAssessmentResult(candidateSession);
		candidateSessionContext = new TerminatedStaticCandidateSessionContext(candidateSession);

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", new Boolean(withTitle));
			layoutCont.contextPut("print", new Boolean(withPrint));
			layoutCont.contextPut("printCommand", Boolean.FALSE);
			if(withPrint) {
				layoutCont.contextPut("winid", "w" + layoutCont.getFormItemComponent().getDispatchID());
				layoutCont.getFormItemComponent().addListener(this);
			}

			if(assessedIdentityInfosCtrl != null) {
				layoutCont.put("assessedIdentityInfos", assessedIdentityInfosCtrl.getInitialComponent());
			} else if(anonym) {
				layoutCont.contextPut("anonym", Boolean.TRUE);
			}
			
			Results results = new Results(false, "o_qtiassessment_icon", options.isMetadata());
			results.setSessionState(testSessionState);
			
			layoutCont.contextPut("testResults", results);
			TestResult testResult = assessmentResult.getTestResult();
			if(testResult != null) {
				extractOutcomeVariable(testResult.getItemVariables(), results);
			}
			
			if(signatureMapperUri != null) {
				String signatureUrl = signatureMapperUri + "/assessmentResultSignature.xml";
				layoutCont.contextPut("signatureUrl", signatureUrl);
			}

			initFormSections(layoutCont);
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
				Results r = new Results(true, node.getSectionPartTitle(), "o_mi_qtisection", options.isSectionSummary());
				AssessmentSectionSessionState sectionState = testSessionState.getAssessmentSectionSessionStates().get(testPlanNodeKey);
				if(sectionState != null) {
					r.setSessionState(sectionState);
				}
				itemResults.add(r);
			} else if(testNodeType == TestNodeType.ASSESSMENT_ITEM_REF) {
				Results results = initFormItemResult(layoutCont, node, identifierToRefs);
				if(results != null) {
					itemResults.add(results);
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
		
		Results r = new Results(false, node.getSectionPartTitle(), type.getCssClass(), options.isQuestionSummary());
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
		
		if(options.isQuestions()) {
			FormItem questionItem = initQuestionItem(layoutCont, sessionState, resolvedAssessmentItem);
			r.setQuestionItem(questionItem);
		}
		
		if(options.isUserSolutions() || options.isCorrectSolutions()) {
			List<InteractionResults> interactionResults = initFormItemInteractions(layoutCont, sessionState, assessmentItem, resolvedAssessmentItem);
			r.getInteractionResults().addAll(interactionResults);
		} 
		
		return r;
	}
	
	private FormItem initQuestionItem(FormLayoutContainer layoutCont, ItemSessionState sessionState,
			ResolvedAssessmentItem resolvedAssessmentItem) {
		
		FormItem responseFormItem = null;
		if(options.isQuestions()) {
			String responseId = "responseBody" + count++;
			ItemBodyResultFormItem formItem = new ItemBodyResultFormItem(responseId, resolvedAssessmentItem);
			
			Document clonedState = ItemSessionStateXmlMarshaller.marshal(sessionState);
			ItemSessionState clonedSessionState = ItemSessionStateXmlMarshaller.unmarshal(clonedState.getDocumentElement());
			clonedSessionState.resetResponses();
			formItem.setItemSessionState(clonedSessionState);
			formItem.setCandidateSessionContext(candidateSessionContext);
			formItem.setResolvedAssessmentTest(resolvedAssessmentTest);
			formItem.setResourceLocator(inputResourceLocator);
			formItem.setAssessmentObjectUri(assessmentObjectUri);
			formItem.setMapperUri(mapperUri);
			layoutCont.add(responseId, formItem);
			responseFormItem = formItem;
		}
		return responseFormItem;
	}

	private List<InteractionResults> initFormItemInteractions(FormLayoutContainer layoutCont, ItemSessionState sessionState,
			AssessmentItem assessmentItem, ResolvedAssessmentItem resolvedAssessmentItem) {
		//loop interactions, show response and solution
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		List<InteractionResults> interactionResults = new ArrayList<>();
		for(Interaction interaction:interactions) {
			if(interaction instanceof PositionObjectInteraction || interaction instanceof EndAttemptInteraction) {
				continue;
			}
			
			FormItem responseFormItem = null;
			if(options.isUserSolutions()) {
				//response
				String responseId = "responseItem" + count++;
				InteractionResultFormItem formItem = new InteractionResultFormItem(responseId, interaction, resolvedAssessmentItem);
				initInteractionResultFormItem(formItem, sessionState);
				layoutCont.add(responseId, formItem);
				responseFormItem = formItem;
			}
	
			//solution
			FormItem solutionFormItem = null;
			if(interaction instanceof ExtendedTextInteraction || interaction instanceof UploadInteraction || interaction instanceof DrawingInteraction) {
				// OO correct solution only for Word
			} else if(options.isCorrectSolutions()) {
				String solutionId = "solutionItem" + count++;
				InteractionResultFormItem formItem = new InteractionResultFormItem(solutionId, interaction, resolvedAssessmentItem);
				formItem.setShowSolution(true);
				initInteractionResultFormItem(formItem, sessionState);
				layoutCont.add(solutionId, formItem);
				solutionFormItem = formItem;
			}
			
			interactionResults.add(new InteractionResults(responseFormItem, solutionFormItem));
		}
		return interactionResults;
	}
	
	private void initInteractionResultFormItem(InteractionResultFormItem formItem, ItemSessionState sectionState) {
		formItem.setItemSessionState(sectionState);
		formItem.setCandidateSessionContext(candidateSessionContext);
		formItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		formItem.setResourceLocator(inputResourceLocator);
		formItem.setAssessmentObjectUri(assessmentObjectUri);
		formItem.setMapperUri(mapperUri);
		if(submissionMapperUri != null) {
			formItem.setSubmissionMapperUri(submissionMapperUri);
		}
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

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(flc.getFormItemComponent() == source && "print".equals(event.getCommand())) {
			doPrint(ureq);
		}
		super.event(ureq, source, event);
	}

	private void doPrint(UserRequest ureq) {
		ControllerCreator creator = new ControllerCreator() {
			@Override
			public Controller createController(UserRequest uureq, WindowControl wwControl) {
				AssessmentResultController printViewCtrl = new AssessmentResultController(uureq, wwControl, assessedIdentity, anonym,
						candidateSession, fUnzippedDirRoot, mapperUri, submissionMapperUri, options, false, true);
				printViewCtrl.flc.contextPut("printCommand", Boolean.TRUE);
				listenTo(printViewCtrl);
				return printViewCtrl;
			}
		};
		openInNewBrowserWindow(ureq, creator);
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
	
	public static class Results {
		
		private Date entryTime;
		private Date endTime;
		private Long duration;
		
		private String score;
		private String maxScore;
		private Boolean pass;
		
		private final String title;
		private final String cssClass;
		private final boolean section;
		private final boolean metadataVisible;
		
		private String sessionStatus;
		
		private FormItem questionItem;
		private final List<InteractionResults> interactionResults = new ArrayList<>();
		
		public Results(boolean section, String cssClass, boolean metadataVisible) {
			this.section = section;
			this.cssClass = cssClass;
			this.metadataVisible = metadataVisible;
			this.title = null;
		}
		
		public Results(boolean section, String title, String cssClass, boolean metadataVisible) {
			this.section = section;
			this.title = title;
			this.cssClass = cssClass;
			this.metadataVisible = metadataVisible;
		}
		
		public void setSessionState(ControlObjectSessionState sessionState) {
			entryTime = sessionState.getEntryTime();
			endTime = sessionState.getEndTime();
			duration = sessionState.getDurationAccumulated();
		}
		
		public boolean isMetadataVisible() {
			return metadataVisible;
		}
		
		public boolean hasInteractions() {
			for(InteractionResults interactionResult:interactionResults) {
				if(interactionResult.getResponseFormItem() != null || interactionResult.getSolutionFormItem() != null) {
					return true;
				}
			}
			return false;
		}
		
		public String getCssClass() {
			return cssClass;
		}
		
		public String getTitle() {
			return title;
		}
		
		public boolean isSection() {
			return section;
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

		public FormItem getQuestionItem() {
			return questionItem;
		}

		public void setQuestionItem(FormItem questionItem) {
			this.questionItem = questionItem;
		}

		public List<InteractionResults> getInteractionResults() {
			return interactionResults;
		}
	}
	
	public class SignatureMapper implements Mapper {
		
		private final File signature;
		
		public SignatureMapper(File signature) {
			this.signature = signature;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {

			MediaResource resource;
			if(signature.exists()) {
				resource = new DownloadeableMediaResource(signature);
			} else {
				resource = new NotFoundMediaResource(relPath);
			}
			return resource;
		}
	}
}