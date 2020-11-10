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
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.FeedbackResultFormItem;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.node.result.TestResult;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
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
	
	private Link pdfLink;
	
	private final String mapperUri;
	private String signatureMapperUri;
	private final String submissionMapperUri;
	private final QTI21AssessmentResultsOptions options;
	private boolean testSessionError = false;
	
	private final boolean anonym;
	private final boolean withPrint;
	private final boolean withTitle;
	private final boolean toggleSolution;
	private final Identity assessedIdentity;
	private TestSessionState testSessionState;
	private AssessmentResult assessmentResult;
	private AssessmentTestSession candidateSession;
	private CandidateSessionContext candidateSessionContext;

	private final File fUnzippedDirRoot;
	private final URI assessmentObjectUri;
	private final File assessmentTestFile;
	private final ResourceLocator inputResourceLocator;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private UserShortDescription assessedIdentityInfosCtrl;
	private final Map<String,AssessmentItemSession> identifierToItemSession = new HashMap<>();
	
	private int count = 0;

	@Autowired
	private UserManager userMgr;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private QTI21Service qtiService;
	
	public AssessmentResultController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean anonym,
			AssessmentTestSession candidateSession, File fUnzippedDirRoot, String mapperUri, String submissionMapperUri,
			QTI21AssessmentResultsOptions options, boolean withPrint, boolean withTitle, boolean toggleSolution) {
		this(ureq, wControl, assessedIdentity, anonym, candidateSession, fUnzippedDirRoot, mapperUri, submissionMapperUri,
				options, withPrint, withTitle, toggleSolution, null);
	}
	
	public AssessmentResultController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean anonym,
			AssessmentTestSession candidateSession, File fUnzippedDirRoot, String mapperUri, String submissionMapperUri,
			QTI21AssessmentResultsOptions options, boolean withPrint, boolean withTitle, boolean toggleSolution, String exportUri) {
		super(ureq, wControl, "assessment_results");
		
		this.anonym = anonym;
		this.options = options;
		this.mapperUri = mapperUri;
		this.withPrint = withPrint;
		this.withTitle = withTitle;
		this.toggleSolution = toggleSolution;
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
			assessedIdentityInfosCtrl.setUsernameAtBottom();
			listenTo(assessedIdentityInfosCtrl);
		}
		
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		assessmentTestFile = new File(testUri);
		
		File signature = qtiService.getAssessmentResultSignature(candidateSession);
		if (signature != null) {
			if (exportUri != null) {
				signatureMapperUri = exportUri;
			} else {
				signatureMapperUri = registerCacheableMapper(ureq, "QTI21Signature::" + CodeHelper.getForeverUniqueID(),
						new SignatureMapper(signature));
			}
		}
		
		try {
			testSessionState = qtiService.loadTestSessionState(candidateSession);
			assessmentResult = qtiService.getAssessmentResult(candidateSession);
			candidateSessionContext = new TerminatedStaticCandidateSessionContext(candidateSession);
			List<AssessmentItemSession> itemSessions = qtiService.getAssessmentItemSessions(candidateSession);
			for(AssessmentItemSession itemSession:itemSessions) {
				identifierToItemSession.put(itemSession.getAssessmentItemIdentifier(), itemSession);
			}
		} catch(Exception e) {
			logError("Cannot show results", e);
			testSessionError = true;
		}

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("options", options);
			layoutCont.contextPut("mapperUri", mapperUri);
			// mapperUri is the default
			layoutCont.contextPut("submissionMapperUri", submissionMapperUri == null ? mapperUri : submissionMapperUri);
			if(assessedIdentity != null) {
				layoutCont.contextPut("userDisplayName", userMgr.getUserDisplayName(assessedIdentity.getKey()));
			} else {
				layoutCont.contextPut("userDisplayName", Boolean.FALSE);
			}

			if(testSessionState == null || assessmentResult == null) {
				// An author has deleted the test session before the user end it.
				// It can happen with time limited tests.
				Results results = new Results(false, "o_qtiassessment_icon", false);
				layoutCont.contextPut("testResults", results);
				layoutCont.contextPut("itemResults", new ArrayList<>());
				layoutCont.contextPut("testSessionNotFound", Boolean.TRUE);
				if(testSessionError) {
					layoutCont.contextPut("testSessionError", Boolean.TRUE);
				}
			} else {
				
				if (candidateSession != null) {
					layoutCont.contextPut("candidateSessionKey", candidateSession.getKey());
					// Add some meta information about the context of this assessment
					RepositoryEntry contextRE = candidateSession.getRepositoryEntry();
					RepositoryEntry testRE = candidateSession.getTestEntry();
					if (contextRE != null && !contextRE.equals(testRE)) { 
						// Show context only when embedded in course. When launching from RE itself,
						// contextRE and testRE are the same.
						layoutCont.contextPut("contextTitle", contextRE.getDisplayname());						
						layoutCont.contextPut("contextId", contextRE.getResourceableId());						
						layoutCont.contextPut("contextExternalRef", contextRE.getExternalRef());						
						// ID of course element
						layoutCont.contextPut("contextSubId", candidateSession.getSubIdent());						
					}
					
					if (testRE != null) {
						layoutCont.contextPut("testTitle", testRE.getDisplayname());
						layoutCont.contextPut("testId", testRE.getResourceableId());
						layoutCont.contextPut("testExternalRef", testRE.getExternalRef());
					}
					
					if(candidateSession.getExtraTime() != null && candidateSession.getExtraTime().intValue() > 0) {
						int extraTimeInMinutes = candidateSession.getExtraTime().intValue() / 60;
						layoutCont.contextPut("extraTimeInMinutes", Integer.toString(extraTimeInMinutes));
					}
					if(candidateSession.getCompensationExtraTime() != null && candidateSession.getCompensationExtraTime().intValue() > 0) {
						int extraTimeInMinutes = candidateSession.getCompensationExtraTime().intValue() / 60;
						layoutCont.contextPut("compensationExtraTimeInMinutes", Integer.toString(extraTimeInMinutes));
					}
				}
				
				layoutCont.contextPut("title", Boolean.valueOf(withTitle));
				layoutCont.contextPut("print", Boolean.valueOf(withPrint));
				layoutCont.contextPut("pdf", Boolean.valueOf(withPrint && pdfModule.isEnabled()));
				layoutCont.contextPut("printCommand", Boolean.FALSE);
				layoutCont.contextPut("toggleSolution", Boolean.valueOf(toggleSolution));
				if(withPrint) {
					layoutCont.contextPut("winid", "w" + layoutCont.getFormItemComponent().getDispatchID());
					layoutCont.getFormItemComponent().addListener(this);
					pdfLink = LinkFactory.createButton("download.pdf", layoutCont.getFormItemComponent(), this);
					pdfLink.setIconLeftCSS("o_icon o_filetype_pdf");
				}

				if(assessedIdentityInfosCtrl != null) {
					layoutCont.put("assessedIdentityInfos", assessedIdentityInfosCtrl.getInitialComponent());
				} else if(anonym) {
					layoutCont.contextPut("anonym", Boolean.TRUE);
				}
				
				Results testResults = new Results(false, "o_qtiassessment_icon", options.isMetadata());
				testResults.setSessionState(testSessionState);
				
				layoutCont.contextPut("testResults", testResults);
				TestResult testResult = assessmentResult.getTestResult();
				if(testResult != null) {
					if(candidateSession.getManualScore() != null) {
						testResults.setScore(candidateSession.getScore());
						testResults.addScore(candidateSession.getManualScore());
						testResults.setManualScore(candidateSession.getManualScore());
					} else {
						extractOutcomeVariable(testResult.getItemVariables(), testResults);
					}
					
					AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
					Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
					if(cutValue != null) {
						testResults.setCutValue(cutValue);
					}
				}
				
				if(signatureMapperUri != null) {
					String signatureUrl = signatureMapperUri + "/assessmentResultSignature.xml";
					layoutCont.contextPut("signatureUrl", signatureUrl);
				}
				
				testResults.setMaxScore(null);//reset max score and aggregate
				initFormSections(layoutCont, testResults);
			}
		}
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
			TestPlanNodeKey testPlanNodeKey = node.getKey();
			TestNodeType testNodeType = node.getTestNodeType();
			if(testNodeType == TestNodeType.ASSESSMENT_SECTION) {
				List<FlowFormItem> rubrics = getSectionRubric(layoutCont, node);
				Results r = new Results(true, node.getSectionPartTitle(), rubrics, "o_mi_qtisection", options.isSectionSummary());
				AssessmentSectionSessionState sectionState = testSessionState.getAssessmentSectionSessionStates().get(testPlanNodeKey);
				if(sectionState != null) {
					r.setSessionState(sectionState);
				}
				resultsMap.put(node, r);
				itemResults.add(r);
				testResults.setNumberOfSections(testResults.getNumberOfSections() + 1);
			} else if(testNodeType == TestNodeType.ASSESSMENT_ITEM_REF) {
				Results results = initFormItemResult(layoutCont, node, identifierToRefs, resultsMap);
				if(results != null) {
					itemResults.add(results);
				}
				testResults.setNumberOfQuestions(testResults.getNumberOfQuestions() + 1);
				if(results.sessionStatus == SessionStatus.FINAL) {
					testResults.setNumberOfAnsweredQuestions(testResults.getNumberOfAnsweredQuestions() + 1);
				}
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
				if(submissionMapperUri != null) {
					formItem.setSubmissionMapperUri(submissionMapperUri);
				}
				
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
		AssessmentItemSession itemSession = identifierToItemSession.get(identifier.toString());

		Results r = new Results(false, node.getSectionPartTitle(), null, type.getCssClass(), options.isQuestionSummary());
		r.setSessionStatus(null);//init
		r.setItemIdentifier(node.getIdentifier().toString());
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);
		if(sessionState != null) {
			r.setSessionState(sessionState);
			SessionStatus sessionStatus = sessionState.getSessionStatus();
			if(sessionStatus != null) {
				r.setSessionStatus(sessionStatus);
			}
		}
		
		ItemResult itemResult = assessmentResult.getItemResult(identifier.toString());
		if(itemResult != null) {
			extractOutcomeVariable(itemResult.getItemVariables(), r);
		}
		if(itemSession != null) {
			if(itemSession.getManualScore() != null) {
				r.setScore(itemSession.getManualScore());
				r.setManualScore(itemSession.getManualScore());
			}

			String comment = itemSession.getCoachComment();
			if(comment != null) {
				if(StringHelper.isHtml(comment)) {
					comment = StringHelper.xssScan(comment);
					comment = Formatter.formatLatexFormulas(comment);
				} else {
					comment = Formatter.escWithBR(comment).toString();
				}
			}
			r.setComment(comment);
			r.setItemSessionKey(itemSession.getKey());
			
			File assessmentDocsDir = qtiService.getAssessmentDocumentsDirectory(candidateSession, itemSession);
			if(assessmentDocsDir != null && assessmentDocsDir.exists()) {
				File[] assessmentDocs = assessmentDocsDir.listFiles(SystemFileFilter.FILES_ONLY);
				if(assessmentDocs != null) {
					for(File assessmentDoc:assessmentDocs) {
						DocumentWrapper dw = new DocumentWrapper(assessmentDoc.getName());
						r.getAssessmentDocuments().add(dw);
					}
				}
			}
		}
		
		//update max score of section
		if(options.isUserSolutions() || options.isCorrectSolutions()) {
			InteractionResults interactionResults = initFormItemInteractions(layoutCont, sessionState, resolvedAssessmentItem);
			r.setInteractionResults(interactionResults);
			
			if(options.isCorrectSolutions()) {
				String correctSolutionId = "correctSolutionItem" + count++;
				FeedbackResultFormItem correctSolutionItem = new FeedbackResultFormItem(correctSolutionId, resolvedAssessmentItem);
				initInteractionResultFormItem(correctSolutionItem, sessionState);
				layoutCont.add(correctSolutionId, correctSolutionItem);
				r.setCorrectSolutionItem(correctSolutionItem);
			}
		}
		
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
				if(assessmentItemResults.hasScore()) {
					sectionResults.addScore(assessmentItemResults);
				}
			}
		}
	}

	private InteractionResults initFormItemInteractions(FormLayoutContainer layoutCont, ItemSessionState sessionState,
			ResolvedAssessmentItem resolvedAssessmentItem) {
		FormItem responseFormItem = null;
		if(options.isUserSolutions()) {
			//response
			String responseId = "responseItem" + count++;
			ItemBodyResultFormItem formItem = new ItemBodyResultFormItem(responseId, resolvedAssessmentItem);
			initInteractionResultFormItem(formItem, sessionState);
			layoutCont.add(responseId, formItem);
			responseFormItem = formItem;
		}

		//solution
		FormItem solutionFormItem = null;
		if(options.isCorrectSolutions()) {
			String solutionId = "solutionItem" + count++;
			ItemBodyResultFormItem formItem = new ItemBodyResultFormItem(solutionId, resolvedAssessmentItem);
			formItem.setShowSolution(true);
			formItem.setReport(true);
			initInteractionResultFormItem(formItem, sessionState);
			layoutCont.add(solutionId, formItem);
			solutionFormItem = formItem;
		}
		return new InteractionResults(responseFormItem, solutionFormItem);
	}
	
	private void initInteractionResultFormItem(ItemBodyResultFormItem formItem, ItemSessionState sessionState) {
		formItem.setItemSessionState(sessionState);
		formItem.setCandidateSessionContext(candidateSessionContext);
		formItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		formItem.setResourceLocator(inputResourceLocator);
		formItem.setAssessmentObjectUri(assessmentObjectUri);
		formItem.setMapperUri(mapperUri);
		if(submissionMapperUri != null) {
			formItem.setSubmissionMapperUri(submissionMapperUri);
		}
	}
	
	private void initInteractionResultFormItem(FeedbackResultFormItem formItem, ItemSessionState sessionState) {
		formItem.setItemSessionState(sessionState);
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
					Double score = getOutcomeNumberVariable(itemVariable);
					results.setScore(score);
					results.setAutoScore(score);
				} else if(QTI21Constants.MAXSCORE_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setMaxScore(getOutcomeNumberVariable(itemVariable));
				} else if(QTI21Constants.PASS_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setPass(getOutcomeBooleanVariable(itemVariable));
				}
			}
		}
	}
	
	private Double getOutcomeNumberVariable(ItemVariable outcomeVariable) {
		Value value = outcomeVariable.getComputedValue();
		if(value instanceof NumberValue) {
			return ((NumberValue)value).doubleValue();
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
		if(flc.getFormItemComponent() == source) {
			if("print".equals(event.getCommand())) {
				doPrint(ureq);
			} else if("pdf".equals(event.getCommand())) {
				doPdf(ureq);
			}
		} else if(pdfLink == source) {
			doPdf(ureq);
		}
		super.event(ureq, source, event);
	}

	private void doPrint(UserRequest ureq) {
		ControllerCreator creator = getResultControllerCreator();
		openInNewBrowserWindow(ureq, creator);
	}
	
	private void doPdf(UserRequest ureq) {
		ControllerCreator creator = (uureq, wwControl) -> {
			File submissionDir = qtiService.getSubmissionDirectory(candidateSession);
			String mapperUriForPdf = registerCacheableMapper(uureq, "QTI21DetailsResources::" + candidateSession.getKey(),
					new ResourcesMapper(assessmentObjectUri, submissionDir));
			AssessmentResultController printViewCtrl = new AssessmentResultController(uureq, wwControl, assessedIdentity, anonym,
					candidateSession, fUnzippedDirRoot, mapperUriForPdf, submissionMapperUri, options, false, true, false);
			printViewCtrl.flc.contextPut("printCommand", Boolean.TRUE);
			listenTo(printViewCtrl);
			return printViewCtrl;
		};
		
		String filename = generateDownloadName(candidateSession);
		MediaResource pdf = pdfService.convert(filename, getIdentity(), creator, getWindowControl());
		ureq.getDispatchResult().setResultingMediaResource(pdf);
	}
	
	private String generateDownloadName(AssessmentTestSession session) {
		String filename = "results_";
		if(session.getAnonymousIdentifier() != null) {
			filename += session.getAnonymousIdentifier();
		} else {
			filename += session.getIdentity().getUser().getFirstName()
					+ "_" + session.getIdentity().getUser().getLastName();
		}
		filename += "_" + session.getRepositoryEntry().getDisplayname();
		
		String subIdent = session.getSubIdent();
		if(StringHelper.containsNonWhitespace(subIdent)) {
			ICourse course = CourseFactory.loadCourse(session.getRepositoryEntry());
			CourseNode node = course.getRunStructure().getNode(subIdent);
			if(node != null) {
				if(StringHelper.containsNonWhitespace(node.getShortTitle())) {
					filename += "_" + node.getShortTitle();
				} else {
					filename += "_" + node.getLongTitle();
				}
			}
		}
		return filename;
	}
	
	private ControllerCreator getResultControllerCreator() {
		return (uureq, wwControl) -> {
			AssessmentResultController printViewCtrl = new AssessmentResultController(uureq, wwControl, assessedIdentity, anonym,
					candidateSession, fUnzippedDirRoot, mapperUri, submissionMapperUri, options, false, true, false);
			printViewCtrl.flc.contextPut("printCommand", Boolean.TRUE);
			listenTo(printViewCtrl);
			return printViewCtrl;
		};
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

		private Date entryTime;
		private Date endTime;
		private Long duration;
		private Integer extraTime;
		private Integer compensationExtraTime;
		
		private BigDecimal score;
		private Double manualScore;
		private Double autoScore;
		
		private Double maxScore;
		private Double cutValue;
		private Boolean pass;
		private String comment;
		
		private String itemIdentifier;
		private final String title;
		private final String cssClass;
		private final boolean section;
		private final boolean metadataVisible;
		
		private SessionStatus sessionStatus;
		
		private Long itemSessionKey;
		
		private int numberOfSections = 0;
		private int numberOfQuestions = 0;
		private int numberOfAnsweredQuestions = 0;
		
		private boolean deleted = false;
		
		private FormItem questionItem;
		private FormItem correctSolutionItem;
		private final List<FlowFormItem> rubrics;
		private InteractionResults interactionResults;
		private final List<Results> subResults = new ArrayList<>();
		private final List<DocumentWrapper> assessmentDocuments = new ArrayList<>(1);
		
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

		public void setSessionState(ControlObjectSessionState sessionState) {
			entryTime = sessionState.getEntryTime();
			endTime = sessionState.getEndTime();
			duration = sessionState.getDurationAccumulated();
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
			return numberOfQuestions > 0 || hasScore();
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
		
		public Integer getExtraTime() {
			return extraTime;
		}
		
		public Integer getCompensationExtraTime() {
			return compensationExtraTime;
		}
		
		public boolean hasScore() {
			return score != null;
		}
		
		public String getScore() {
			return AssessmentHelper.getRoundedScore(score);
		}

		public void setScore(BigDecimal score) {
			if(score != null) {
				this.score = score;
			}
		}
		
		public void setScore(Double score) {
			if(score != null) {
				this.score = BigDecimal.valueOf(score);
			}
		}
		
		public void addScore(Results results) {
			if(results.hasScore()) {
				if(score == null) {
					score = BigDecimal.valueOf(0.0d);
				}
				score = score.add(results.score);
			}
		}
		
		public void addScore(BigDecimal additionalScore) {
			if(score == null) {
				score = BigDecimal.valueOf(0.0d);
			}
			score = score.add(additionalScore);
		}
		
		public String getAutoScore() {
			return AssessmentHelper.getRoundedScore(autoScore);
		}
		
		public void setAutoScore(Double autoScore) {
			if(autoScore != null) {
				this.autoScore = autoScore;
			}
		}
		
		public String getManualScore() {
			return AssessmentHelper.getRoundedScore(manualScore);
		}
		
		public void setManualScore(BigDecimal manualScore) {
			if(manualScore != null) {
				this.manualScore = manualScore.doubleValue();
			}
		}
		
		public boolean hasMaxScore() {
			return maxScore != null;
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
		
		public String getScorePercent() {
			if(maxScore == null) {
				return null;
			}
			if(score == null) {
				return "0";
			}
			
			double percent = (score.doubleValue() / maxScore) * 100.0d;
			long percentLong = Math.round(percent);	
			return Long.toString(percentLong);
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
		
		public Boolean getCalculatedPassed() {
			Boolean passed = pass;
			if(score != null && manualScore != null && cutValue != null) {
				boolean calculated = score.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
			return passed;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getSessionStatus() {
			return sessionStatus == null ? "" : translate("results.session.status." + sessionStatus.toQtiString());
		}

		public void setSessionStatus(SessionStatus sessionStatus) {
			this.sessionStatus = sessionStatus;
		}
		
		public boolean isEnded() {
			return sessionStatus == SessionStatus.FINAL;
		}
		
		public boolean isCorrect() {
			return sessionStatus == SessionStatus.FINAL &&
					(maxScore != null && score != null && Math.abs(maxScore.doubleValue() - score.doubleValue()) < 0.0001);
		}

		public int getNumberOfQuestions() {
			return numberOfQuestions;
		}

		public void setNumberOfQuestions(int numberOfQuestions) {
			this.numberOfQuestions = numberOfQuestions;
		}

		public int getNumberOfAnsweredQuestions() {
			return numberOfAnsweredQuestions;
		}

		public void setNumberOfAnsweredQuestions(int numberOfAnsweredQuestions) {
			this.numberOfAnsweredQuestions = numberOfAnsweredQuestions;
		}
		
		public String getNumberOfAnsweredQuestionsPercent() {
			if(numberOfAnsweredQuestions <= 0) return "0";
			if(numberOfQuestions <= 0) return "100";
			
			double val = ((double)numberOfAnsweredQuestions / (double)numberOfQuestions) * 100.0d;
			long percent = Math.round(val);
			return Long.toString(percent);
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

		public FormItem getQuestionItem() {
			return questionItem;
		}

		public void setQuestionItem(FormItem questionItem) {
			this.questionItem = questionItem;
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
		
		public List<DocumentWrapper> getAssessmentDocuments() {
			return assessmentDocuments;
		}
	}
	
	public class DocumentWrapper {
		
		private final String filename;
		
		public DocumentWrapper(String filename) {
			this.filename = filename;
		}
		
		public String getFilename() {
			return filename;
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
				resource = new NotFoundMediaResource();
			}
			return resource;
		}
	}
}