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
package org.olat.ims.qti21.ui.assessment;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.InteractionResultFormItem;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 15.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentTestCorrectionController extends FormBasicController {
	
	private RepositoryEntry testEntry;
	private AssessmentEntry assessmentEntry;
	private AssessmentTestSession candidateSession;
	
	private int count = 0;
	private String mapperUri;
	private final URI assessmentObjectUri;
	private TestSessionState testSessionState;
	private final ResourceLocator inputResourceLocator;
	private final CandidateSessionContext candidateSessionContext;
	private final List<IdentityAssessmentItemWrapper> itemResults = new ArrayList<>();
	
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final Map<String,AssessmentItemSession> identifierToItemSession = new HashMap<>();
	
	private TextElement testCommentEl;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentService assessmentService;
	
	public IdentityAssessmentTestCorrectionController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session) {
		super(ureq, wControl, "user_interactions", Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.candidateSession = session;
		testEntry = session.getTestEntry();
		
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		File submissionDir = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(null, "QTI21CorrectionResources::" + session.getKey(),
				new ResourcesMapper(assessmentObjectUri, submissionDir));
		
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		testSessionState = qtiService.loadTestSessionState(candidateSession);
		candidateSessionContext = new TerminatedStaticCandidateSessionContext(candidateSession);
		
		assessmentEntry = assessmentService.loadAssessmentEntry(session.getIdentity(), session.getRepositoryEntry(), session.getSubIdent());

		initForm(ureq);
	}
	
	public AssessmentTestSession getAssessmentTestSession() {
		return candidateSession;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<AssessmentItemSession> itemSessions = qtiService.getAssessmentItemSessions(candidateSession);
		for(AssessmentItemSession itemSession:itemSessions) {
			identifierToItemSession.put(itemSession.getAssessmentItemIdentifier(), itemSession);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("interactionWrappers", itemResults);
			
			Map<Identifier, AssessmentItemRef> identifierToRefs = new HashMap<>();
			for(AssessmentItemRef itemRef:resolvedAssessmentTest.getAssessmentItemRefs()) {
				identifierToRefs.put(itemRef.getIdentifier(), itemRef);
			}
	
			TestPlan testPlan = testSessionState.getTestPlan();
			List<TestPlanNode> nodes = testPlan.getTestPlanNodeList();
			for(TestPlanNode node:nodes) {
				TestNodeType testNodeType = node.getTestNodeType();
				if(testNodeType == TestNodeType.ASSESSMENT_ITEM_REF) {
					initFormItemResult(layoutCont, node, identifierToRefs);
				}
			}
		}
		
		String comment = "";
		if(assessmentEntry != null && StringHelper.containsNonWhitespace(assessmentEntry.getComment())) {
			comment = assessmentEntry.getComment();
		}
		testCommentEl = uifactory.addTextAreaElement("comment.test", "comment.test", 2500, 4, 72, true, comment, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private void initFormItemResult(FormLayoutContainer layoutCont, TestPlanNode node,
			Map<Identifier, AssessmentItemRef> identifierToRefs) {
		TestPlanNodeKey testPlanNodeKey = node.getKey();
		Identifier identifier = testPlanNodeKey.getIdentifier();
		AssessmentItemRef itemRef = identifierToRefs.get(identifier);
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		
		boolean manualScore = false;
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		List<InteractionResultFormItem> responseItems = new ArrayList<>();
		for(Interaction interaction:interactions) {
			if(!(interaction instanceof EndAttemptInteraction)) {
				responseItems.add(initFormExtendedTextInteraction(resolvedAssessmentItem, testPlanNodeKey, interaction, layoutCont));
			}
			if(interaction instanceof UploadInteraction
					|| interaction instanceof DrawingInteraction
					|| interaction instanceof ExtendedTextInteraction) {
				manualScore = true;
			}
		}
		
		if(responseItems.size() > 0) {
			String mScore = "";
			String comment = "";
			String stringuifiedIdentifier = identifier.toString();
			AssessmentItemSession itemSession = null;
			if(identifierToItemSession.containsKey(stringuifiedIdentifier)) {
				itemSession = identifierToItemSession.get(stringuifiedIdentifier);
				comment = itemSession.getCoachComment();
				if(itemSession.getManualScore() != null) {
					mScore = AssessmentHelper.getRoundedScore(itemSession.getManualScore());
				}
			}
			
			ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);
			AssessmentItemCorrection infos = new AssessmentItemCorrection(candidateSession.getIdentity(),
					candidateSession, testSessionState, itemSession, itemSessionState, itemRef, node);
			
			String fullname = userManager.getUserDisplayName(candidateSession.getIdentity());
			TextElement scoreEl = null;
			if(manualScore) {
				scoreEl = uifactory.addTextElement("score_item_" + count++, "score", 6, mScore, layoutCont);	
			}
			TextElement commentEl = uifactory.addTextAreaElement("comment_item_" + count++, "comment", 2500, 4, 72, true, comment, layoutCont);
			IdentityAssessmentItemWrapper wrapper = new IdentityAssessmentItemWrapper(fullname, assessmentItem, infos, responseItems, scoreEl, commentEl);
			
			if(manualScore) {
				Double minScore = QtiNodesExtractor.extractMinScore(assessmentItem);
				Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
				if(maxScore != null) {
					if(minScore == null) {
						minScore = 0.0d;
					}

					wrapper.setMinScore(AssessmentHelper.getRoundedScore(minScore));
					wrapper.setMaxScore(AssessmentHelper.getRoundedScore(maxScore));
					wrapper.setMinScoreVal(minScore);
					wrapper.setMaxScoreVal(maxScore);
					scoreEl.setExampleKey("correction.min.max.score", new String[]{ wrapper.getMinScore(), wrapper.getMaxScore() });
				}
			}
			itemResults.add(wrapper);
		}
	}
	
	private InteractionResultFormItem initFormExtendedTextInteraction(ResolvedAssessmentItem resolvedAssessmentItem,
			TestPlanNodeKey testPlanNodeKey, Interaction interaction, FormLayoutContainer layoutCont) {
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);

		String responseId = "responseItem" + count++;
		InteractionResultFormItem responseFormItem = new InteractionResultFormItem(responseId, interaction, resolvedAssessmentItem);
		responseFormItem.setItemSessionState(sessionState);
		responseFormItem.setCandidateSessionContext(candidateSessionContext);
		responseFormItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		responseFormItem.setResourceLocator(inputResourceLocator);
		responseFormItem.setAssessmentObjectUri(assessmentObjectUri);
		responseFormItem.setMapperUri(mapperUri);
		layoutCont.add(responseFormItem);
		return responseFormItem;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if(itemResults != null) {
			for(IdentityAssessmentItemWrapper itemResult:itemResults) {
				if(itemResult.getScoreEl() != null) {
					allOk &= validateScore(itemResult);
				}
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateScore(IdentityAssessmentItemWrapper itemResult) {
		boolean allOk = true;
		
		String scoreVal = itemResult.getScoreEl().getValue();
		itemResult.getScoreEl().clearError();
		try {
			double score = Double.parseDouble(scoreVal);
			//check boundaries
			
			boolean boundariesOk = true;
			if(itemResult.getMinScore() != null && score < itemResult.getMinScoreVal().doubleValue()) {
				boundariesOk &= false;
			}
			if(itemResult.getMaxScore() != null && score > itemResult.getMaxScoreVal().doubleValue()) {
				boundariesOk &= false;
			}
			
			if(!boundariesOk) {
				itemResult.getScoreEl()
					.setErrorKey("correction.min.max.score", new String[]{ itemResult.getMinScore(), itemResult.getMaxScore() });
			}
			allOk &= boundariesOk;
		} catch(Exception e) {
			itemResult.getScoreEl().setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(itemResults != null) {
			AssessmentSessionAuditLogger candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(candidateSession, false);
			
			BigDecimal totalScore = new BigDecimal(0);
			for(IdentityAssessmentItemWrapper itemResult:itemResults) {
				totalScore = doUpdateItemSession(itemResult, totalScore, candidateAuditLogger);
			}
			candidateSession.setManualScore(totalScore);
			candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
			
			String comment = testCommentEl.getValue();
			if(assessmentEntry != null && !comment.equals(assessmentEntry.getComment())) {
				assessmentEntry = assessmentService.loadAssessmentEntry(candidateSession.getIdentity(), candidateSession.getRepositoryEntry(), candidateSession.getSubIdent());
				assessmentEntry.setComment(comment);
				assessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private BigDecimal doUpdateItemSession(IdentityAssessmentItemWrapper itemResult, BigDecimal totalScore, AssessmentSessionAuditLogger candidateAuditLogger) {
		String scoreVal = null;
		if(itemResult.getScoreEl() != null) {
			scoreVal = itemResult.getScoreEl().getValue();
		}
		String comment = null;
		if(itemResult.getCommentEl() != null) {
			comment = itemResult.getCommentEl().getValue();
		}

		if(StringHelper.containsNonWhitespace(scoreVal) || comment != null) {
			String stringuifiedIdentifier = itemResult
					.getTestPlanNodeKey().getIdentifier().toString();
			
			ParentPartItemRefs parentParts = getParentSection(itemResult.getTestPlanNodeKey());
			AssessmentItemSession itemSession = qtiService
					.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier);
			
			if(StringHelper.containsNonWhitespace(scoreVal)) {
				BigDecimal mScore = new BigDecimal(scoreVal);
				itemSession.setManualScore(mScore);
				totalScore = totalScore.add(mScore);
			}
			if(comment != null) {
				itemSession.setCoachComment(comment);
			}
			itemSession = qtiService.updateAssessmentItemSession(itemSession);

			candidateAuditLogger.logCorrection(candidateSession, itemSession, getIdentity());
		}
		
		return totalScore;
	}
	
	private  ParentPartItemRefs getParentSection(TestPlanNodeKey itemKey) {
		return AssessmentTestHelper.getParentSection(itemKey, testSessionState, resolvedAssessmentTest);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}