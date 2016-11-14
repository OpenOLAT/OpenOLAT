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

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
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
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.InteractionResultFormItem;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 16.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesAssessmentItemCorrectionController extends FormBasicController {

	private FormLink nextQuestionButton;
	
	private int count;
	
	private final String mapperUri;
	private final URI assessmentObjectUri;
	private final ResourcesMapper resourcesMapper;
	private final ResourceLocator inputResourceLocator;

	private final AssessmentItemRef itemRef;
	private final AssessmentItem assessmentItem;
	private final List<Interaction> interactions;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	
	private final AssessmentTestCorrection testCorrections;
	private Map<Long, File> submissionDirectoryMaps = new HashMap<>();
	private final List<IdentityAssessmentItemWrapper> itemResults = new ArrayList<>();

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
	public IdentitiesAssessmentItemCorrectionController(UserRequest ureq, WindowControl wControl,
			AssessmentTestCorrection testCorrections, AssessmentItemRef itemRef,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest) {
		super(ureq, wControl, "users_interactions");
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		
		this.itemRef = itemRef;
		this.testCorrections = testCorrections;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		interactions = assessmentItem.getItemBody().findInteractions();
		
		resourcesMapper = new ResourcesMapper(assessmentObjectUri, submissionDirectoryMaps);
		mapperUri = registerCacheableMapper(null, "QTI21Resources::" + testEntry.getKey(), resourcesMapper);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("interactionWrappers", itemResults);
			List<AssessmentItemCorrection> corrections = testCorrections.getCorrections(itemRef);
			if(corrections != null) {
				for(AssessmentItemCorrection correction:corrections) {
					initFormItemResult(correction, layoutCont);
				}
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		nextQuestionButton = uifactory.addFormLink("save.next", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initFormItemResult(AssessmentItemCorrection correction, FormLayoutContainer layoutCont) {
		TestPlanNode node = correction.getItemNode();
		TestPlanNodeKey testPlanNodeKey = node.getKey();
		AssessmentItemSession itemSession = correction.getItemSession();
		AssessmentTestSession testSession = correction.getTestSession();
		TestSessionState testSessionState = correction.getTestSessionState();
		ItemSessionState sessionState = correction.getItemSessionState();
		
		List<InteractionResultFormItem> responseItems = new ArrayList<>(interactions.size());
		if(sessionState.isResponded()) {
			for(Interaction interaction:interactions) {
				if(interaction instanceof UploadInteraction
						|| interaction instanceof DrawingInteraction
						|| interaction instanceof ExtendedTextInteraction) {
					responseItems.add(initFormExtendedTextInteraction(testPlanNodeKey, interaction, testSessionState, testSession, layoutCont));
					
					File submissionDir = qtiService.getSubmissionDirectory(testSession);
					if(submissionDir != null) {
						submissionDirectoryMaps.put(testSession.getKey(), submissionDir);
					}
				}
			}
		}

		String mScore = "";
		if(itemSession != null && itemSession.getManualScore() != null) {
			mScore = AssessmentHelper.getRoundedScore(itemSession.getManualScore());
		}
		
		String fullname = userManager.getUserDisplayName(correction.getAssessedIdentity());
		TextElement scoreEl = uifactory.addTextElement("scoreItem" + count++, "score", 6, mScore, layoutCont);
		IdentityAssessmentItemWrapper wrapper = new IdentityAssessmentItemWrapper(fullname, assessmentItem, correction, responseItems, scoreEl);
		
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

		itemResults.add(wrapper);
	}
	
	private InteractionResultFormItem initFormExtendedTextInteraction(TestPlanNodeKey testPlanNodeKey, Interaction interaction,
			TestSessionState testSessionState, AssessmentTestSession assessmentTestSession, FormLayoutContainer layoutCont) {
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);

		String responseId = "responseItem" + count++;
		InteractionResultFormItem responseFormItem = new InteractionResultFormItem(responseId, interaction, resolvedAssessmentItem);
		responseFormItem.setItemSessionState(sessionState);
		responseFormItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(assessmentTestSession));
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
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nextQuestionButton == source) {
			doSave();
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSave() {
		if(itemResults != null) {
			
			
			for(IdentityAssessmentItemWrapper itemResult:itemResults) {
				AssessmentItemCorrection itemCorrection = itemResult.getCorrection();
				TestSessionState testSessionState = itemCorrection.getTestSessionState();
				AssessmentTestSession candidateSession = itemCorrection.getTestSession();
				AssessmentSessionAuditLogger candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(candidateSession, false);
				
				String scoreVal = itemResult.getScoreEl().getValue();
				if(StringHelper.containsNonWhitespace(scoreVal)) {
					BigDecimal mScore = new BigDecimal(scoreVal);
					String stringuifiedIdentifier = itemResult
							.getTestPlanNodeKey().getIdentifier().toString();
					
					ParentPartItemRefs parentParts = AssessmentTestHelper
							.getParentSection(itemResult.getTestPlanNodeKey(), testSessionState, resolvedAssessmentTest);
					AssessmentItemSession itemSession = qtiService
							.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier);
					itemSession.setManualScore(mScore);
					itemSession = qtiService.updateAssessmentItemSession(itemSession);
					itemCorrection.setItemSession(itemSession);
					
					candidateAuditLogger.logCorrection(candidateSession, itemSession, getIdentity());
				}
				
				BigDecimal totalScore = null;
				for(AssessmentItemCorrection corr:testCorrections.getCorrections(itemCorrection.getAssessedIdentity())) {
					BigDecimal mScore = corr.getManualScore();
					if(totalScore == null) {
						totalScore = mScore;
					} else if(mScore != null) {
						totalScore = totalScore.add(mScore);
					}
				}
				
				candidateSession.setManualScore(totalScore);
				candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
				itemCorrection.setTestSession(candidateSession);
				
				IOUtils.closeQuietly(candidateAuditLogger);
			}	
		}
	}
}