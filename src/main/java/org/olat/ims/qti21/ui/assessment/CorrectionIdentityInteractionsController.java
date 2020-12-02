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
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.assessment.model.SectionRubrics;
import org.olat.ims.qti21.ui.components.FeedbackResultFormItem;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.components.InteractionResultFormItem;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * This controller display a read only view of the interactions
 * of a specific identity and assessment item.
 * 
 * 
 * Initial date: 23 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityInteractionsController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };

	private TextElement scoreEl;
	private RichTextElement commentEl;
	private StaticTextElement statusEl;
	private FormLink viewSolutionButton;
	private FormLink overrideScoreButton;
	private FormLink viewCorrectSolutionButton;
	private FileElement uploadDocsEl;
	private ItemBodyResultFormItem solutionItem;
	private FeedbackResultFormItem correctSolutionItem;
	private MultipleSelectionElement toReviewEl;
	private FormLayoutContainer overrideScoreCont;
	private FormLayoutContainer docsLayoutCont;
	private FormLayoutContainer scoreCont;

	private DialogBoxController confirmDeleteDocCtrl;
	private OverrideScoreController overrideScoreCtrl;
	private CloseableCalloutWindowController overrideScoreCalloutCtrl;
	
	private final String mapperUri;
	private final URI assessmentObjectUri;
	private final ResourceLocator inputResourceLocator;

	private final File assessmentTestFile;
	private final AssessmentItem assessmentItem;
	private final List<Interaction> interactions;
	private final AssessmentItemCorrection correction;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final Map<Long, File> submissionDirectoryMaps;
	
	private BigDecimal overrideAutoScore;
	private boolean manualScore = false;
	private final boolean readOnly;
	
	private int count = 0;
	private final long id = CodeHelper.getRAMUniqueID();

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
	public CorrectionIdentityInteractionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			AssessmentItemCorrection correction, Map<Long, File> submissionDirectoryMaps, boolean readOnly,
			String mapperUri, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "correction_identity_interactions", rootForm);
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.readOnly = readOnly;
		this.mapperUri = mapperUri;
		this.correction = correction;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		assessmentTestFile = new File(testUri);
		resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(correction.getItemRef());
		assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		interactions = assessmentItem.getItemBody().findInteractions();
		this.submissionDirectoryMaps = submissionDirectoryMaps;
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		initForm(ureq);
		reloadAssessmentDocs();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		TestPlanNode node = correction.getItemNode();
		TestPlanNodeKey testPlanNodeKey = node.getKey();
		AssessmentItemSession itemSession = correction.getItemSession();
		AssessmentTestSession testSession = correction.getTestSession();
		TestSessionState testSessionState = correction.getTestSessionState();
		
		ItemBodyResultFormItem answerItem = initFormInteraction(testPlanNodeKey, testSessionState, testSession, formLayout, true, false);	
		formLayout.add("answer", answerItem);
		
		viewSolutionButton = uifactory.addFormLink("view.solution", formLayout);
		viewSolutionButton.setIconLeftCSS("o_icon o_icon_open_togglebox");
		viewSolutionButton.setVisible(hasSolution());
		
		solutionItem = initFormInteraction(testPlanNodeKey, testSessionState, testSession, formLayout, false, true);	
		solutionItem.setVisible(false);
		solutionItem.setShowSolution(true);
		formLayout.add("solution", solutionItem);
		
		if(hasCorrectSolution()) {
			viewCorrectSolutionButton = uifactory.addFormLink("view.correct.solution", formLayout);
			viewCorrectSolutionButton.setIconLeftCSS("o_icon o_icon_open_togglebox");
			
			correctSolutionItem = initFormCorrectSolution(testPlanNodeKey, testSessionState, testSession, formLayout);
			correctSolutionItem.setVisible(false);
			correctSolutionItem.setShowSolution(true);
			formLayout.add("correctSolution", correctSolutionItem);
		}

		List<InteractionResultFormItem> responseItems = new ArrayList<>(interactions.size());
		for(Interaction interaction:interactions) {
			if(interaction instanceof UploadInteraction
					|| interaction instanceof DrawingInteraction
					|| interaction instanceof ExtendedTextInteraction) {
				manualScore = true;
				File submissionDir = qtiService.getSubmissionDirectory(testSession);
				if(submissionDir != null) {
					submissionDirectoryMaps.put(testSession.getKey(), submissionDir);
				}
			}
		}

		String mScore = "";
		String coachComment = "";
		if(itemSession != null) {
			if(itemSession.getManualScore() != null) {
				mScore = AssessmentHelper.getRoundedScore(itemSession.getManualScore());
			}
			coachComment = itemSession.getCoachComment();
		}
		
		scoreCont = FormLayoutContainer.createDefaultFormLayout("score.container", getTranslator());
		formLayout.add("score.container", scoreCont);
		
		statusEl = uifactory.addStaticTextElement("status", "status", "", scoreCont);
		statusEl.setElementCssClass("o_sel_assessment_item_status");
		statusEl.setValue(getStatus());
		
		String fullname = userManager.getUserDisplayName(correction.getAssessedIdentity());
		if(manualScore) {
			scoreEl = uifactory.addTextElement("scoreItem", "score", 6, mScore, scoreCont);
			scoreEl.setElementCssClass("o_sel_assessment_item_score");
			scoreEl.setEnabled(!readOnly);
		} else {
			overrideAutoScore = itemSession == null ? null : itemSession.getManualScore();
			
			String page = velocity_root + "/override_score.html";
			overrideScoreCont = FormLayoutContainer.createCustomFormLayout("extra.score", getTranslator(), page);
			overrideScoreCont.setRootForm(mainForm);
			scoreCont.add(overrideScoreCont);
			overrideScoreCont.setLabel("score", null);
			
			BigDecimal score = null;
			if(itemSession != null) {
				score = itemSession.getManualScore();
				if(score == null) {
					score = itemSession.getScore();
				}
			}
			overrideScoreCont.contextPut("score", AssessmentHelper.getRoundedScore(score));
			
			overrideScoreButton = uifactory.addFormLink("override.score", overrideScoreCont, Link.BUTTON_SMALL);
			overrideScoreButton.setDomReplacementWrapperRequired(false);
			overrideScoreButton.setVisible(!readOnly);
		}
		commentEl = uifactory.addRichTextElementForStringData("commentItem", "comment", coachComment, 8, -1,
				false, null, null, null, scoreCont, ureq.getUserSession(), getWindowControl());
		commentEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		commentEl.getEditorConfiguration().setPathInStatusBar(false);
		commentEl.setHelpText(translate("comment.help"));
		commentEl.setEnabled(!readOnly);
		IdentityAssessmentItemWrapper wrapper = new IdentityAssessmentItemWrapper(fullname, assessmentItem, correction, responseItems,
				scoreEl, commentEl, statusEl);
		
		String page = velocity_root + "/item_assessment_docs.html"; 
		docsLayoutCont = FormLayoutContainer.createCustomFormLayout("assessment.item.docs", getTranslator(), page);
		docsLayoutCont.setLabel("assessment.item.docs", null);
		docsLayoutCont.contextPut("mapperUri", mapperUri);
		scoreCont.add(docsLayoutCont);

		uploadDocsEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "assessment.item.docs.upload", "assessment.item.docs.upload", scoreCont);
		uploadDocsEl.addActionListener(FormEvent.ONCHANGE);
		uploadDocsEl.setVisible(!readOnly);
		
		toReviewEl = uifactory.addCheckboxesHorizontal("to.review", "to.review", scoreCont, onKeys, new String[] { "" });
		toReviewEl.setEnabled(!readOnly);
		if(itemSession != null && itemSession.isToReview()) {
			toReviewEl.select(onKeys[0], true);
		}
	
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
			
			if(scoreEl != null) {
				scoreEl.setExampleKey("correction.min.max.score", new String[]{ wrapper.getMinScore(), wrapper.getMaxScore() });
			}
			if(overrideScoreCont != null) {
				overrideScoreCont.setExampleKey("correction.min.max.score", new String[]{ wrapper.getMinScore(), wrapper.getMaxScore() });
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("interactionWrapper", wrapper);
			layoutCont.contextPut("autoSaved", Boolean.valueOf(isAutoSaved(testPlanNodeKey, testSessionState)));
			
			List<SectionRubrics> sectionRubrics = initSectionsRubrics(layoutCont);
			layoutCont.contextPut("sectionRubrics", sectionRubrics);
		}
	}
	
	private List<SectionRubrics> initSectionsRubrics(FormItemContainer layoutCont) {
		List<SectionRubrics> sectionParentLine = new ArrayList<>();
		
		try {
			AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
			for(TestPlanNode parentNode=correction.getItemNode().getParent(); parentNode.getParent() != null; parentNode = parentNode.getParent()) {
				AbstractPart part = assessmentTest.lookupFirstDescendant(parentNode.getIdentifier());
				if(part instanceof AssessmentSection) {
					AssessmentSection section = (AssessmentSection)part;
					if(section.getVisible()) {
						boolean writeRubrics = false;
						for(RubricBlock rubric:section.getRubricBlocks()) {
							if(!rubric.getBlocks().isEmpty()) {
								writeRubrics = true;
							}
						}
						
						if(writeRubrics) {
							List<FlowFormItem> rubrics = getSectionRubrics(section, layoutCont);
							String openLabel;
							if(StringHelper.containsNonWhitespace(section.getTitle())) {
								openLabel = translate("show.rubric.with.title", new String[]{ section.getTitle() });
							} else {
								openLabel = translate("show.rubric");
							}
							sectionParentLine.add(new SectionRubrics(section.getIdentifier().toString(), section.getTitle(), rubrics, openLabel));
						}
					}
				}
			}
			
			if(sectionParentLine.size() > 1) {
				Collections.reverse(sectionParentLine);
			}
		} catch (Exception e) {
			logError("", e);
		}
		
		return sectionParentLine;
	}
	
	private List<FlowFormItem> getSectionRubrics(AssessmentSection section, FormItemContainer layoutCont) {
		List<FlowFormItem> rubricsEls = new ArrayList<>();
		for(RubricBlock rubricBlock:section.getRubricBlocks()) {

			String rubricElId = "section_rubric_" + (count++);
			FlowFormItem formItem = new FlowFormItem(rubricElId, assessmentTestFile);
			formItem.setBlocks(rubricBlock.getBlocks());
			formItem.setResourceLocator(inputResourceLocator);
			formItem.setAssessmentObjectUri(assessmentObjectUri);
			formItem.setMapperUri(mapperUri);

			rubricsEls.add(formItem);
			layoutCont.add(rubricElId, formItem);
		}
		return rubricsEls;
	}
	
	private ItemBodyResultFormItem initFormInteraction(TestPlanNodeKey testPlanNodeKey,
			TestSessionState testSessionState, AssessmentTestSession assessmentTestSession,
			FormItemContainer layoutCont, boolean correctionHelp, boolean correctionSolution) {
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);

		String responseId = "responseItem_" + id + "_" + count++;
		ItemBodyResultFormItem responseFormItem = new ItemBodyResultFormItem(responseId, resolvedAssessmentItem);
		responseFormItem.setItemSessionState(sessionState);
		responseFormItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(assessmentTestSession));
		responseFormItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		responseFormItem.setResourceLocator(inputResourceLocator);
		responseFormItem.setAssessmentObjectUri(assessmentObjectUri);
		responseFormItem.setMapperUri(mapperUri);
		responseFormItem.setCorrectionHelp(correctionHelp);
		responseFormItem.setCorrectionSolution(correctionSolution);
		layoutCont.add(responseFormItem);
		return responseFormItem;
	}
	
	private FeedbackResultFormItem initFormCorrectSolution(TestPlanNodeKey testPlanNodeKey,
			TestSessionState testSessionState, AssessmentTestSession assessmentTestSession, FormItemContainer layoutCont) {
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);

		String correctSolutionId = "correctSolutionItem" + count++;
		FeedbackResultFormItem feedbackItem = new FeedbackResultFormItem(correctSolutionId, resolvedAssessmentItem);
		feedbackItem.setItemSessionState(sessionState);
		feedbackItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(assessmentTestSession));
		feedbackItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		feedbackItem.setResourceLocator(inputResourceLocator);
		feedbackItem.setAssessmentObjectUri(assessmentObjectUri);
		feedbackItem.setMapperUri(mapperUri);
		layoutCont.add(feedbackItem);
		return feedbackItem;
	}
	
	private boolean isAutoSaved(TestPlanNodeKey testPlanNodeKey, TestSessionState testSessionState) {
		for(Interaction interaction:assessmentItem.getItemBody().findInteractions()) {
			if(!(interaction instanceof ExtendedTextInteraction) && !(interaction instanceof EndAttemptInteraction)) {
				return false;
			}
		}
		
		ItemSessionState sessionState = testSessionState.getItemSessionStates().get(testPlanNodeKey);
		if(sessionState == null) {
			return false;
		}
		Map<Identifier, ResponseData> raws = sessionState.getRawResponseDataMap();
		if(raws == null || raws.isEmpty()) {
			return false;
		}
		
		for(ResponseData data:raws.values()) {
			if(data instanceof StringResponseData) {
				StringResponseData stringData = (StringResponseData)data;
				if(stringData.getResponseData() != null) {
					for(String string:stringData.getResponseData()) {
						if(StringHelper.containsNonWhitespace(string)) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean hasSolution() {
		for(Interaction interaction:assessmentItem.getItemBody().findInteractions()) {
			if(!(interaction instanceof ExtendedTextInteraction)
					&& !(interaction instanceof DrawingInteraction)
					&& !(interaction instanceof UploadInteraction)
					&& !(interaction instanceof EndAttemptInteraction)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasCorrectSolution() {
		for(ModalFeedback modalFeedback:assessmentItem.getModalFeedbacks()) {
			Identifier outcomeIdentifier = modalFeedback.getOutcomeIdentifier();
			if(QTI21Constants.CORRECT_SOLUTION_IDENTIFIER.equals(outcomeIdentifier)) {
				return true;
			}
		}
		return false;
	}
	
	protected String getStatus() {
		AssessmentItemSession itemSession = correction.getItemSession();
		StringBuilder sb = new StringBuilder();
		if(itemSession != null) {
			if(itemSession.getManualScore() != null) {
				sb.append("<i class='o_icon o_icon_ok'> </i>");
			} else if(!correction.isItemSessionStatusFinal()) {
				sb.append("<i class='o_icon o_icon_warn'> </i> ").append(translate("warning.not.submitted"));
			} else if(manualScore) {
				sb.append("<i class='o_icon o_icon_warn'> </i>");
			} else {
				sb.append("<i class='o_icon o_icon_ok'> </i> <span class='badge'>").append(translate("correction.auto")).append("</span>");
			} 
		} else {
			sb.append("<i class='o_icon o_icon_warn'> </i> ").append(translate("warning.not.submitted"));
		}
		return sb.toString();
	}
	
	protected BigDecimal getManualScore() {
		if(scoreEl == null) {
			return overrideAutoScore;
		} else if (StringHelper.containsNonWhitespace(scoreEl.getValue())) {
			String mScore = scoreEl.getValue();
			if(mScore.indexOf(',') >= 0) {
				mScore = mScore.replace(",", ".");
			}
			return new BigDecimal(mScore);
		}
		return null;
	}
	
	protected String getComment() {
		return commentEl.getValue();
	}
	
	protected boolean isToReview() {
		return toReviewEl != null && toReviewEl.isAtLeastSelected(1);
	}
	
	protected void updateStatus() {
		statusEl.setValue(getStatus());
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(overrideScoreButton == source) {
			doOverrideScore(ureq);
		} else if(viewSolutionButton == source) {
			doToggleSolution();
		} else if(viewCorrectSolutionButton == source) {
			doToggleCorrectSolution();
		} else if(uploadDocsEl == source) {
			if(uploadDocsEl.getUploadFile() != null && StringHelper.containsNonWhitespace(uploadDocsEl.getUploadFileName())) {
				doUploadAssessmentDocument(uploadDocsEl.getUploadFile(), uploadDocsEl.getUploadFileName());
				reloadAssessmentDocs();
				uploadDocsEl.reset();
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(link.getCmd() != null && link.getCmd().startsWith("delete_doc_") && uobject instanceof DocumentWrapper) {
				DocumentWrapper wrapper = (DocumentWrapper)uobject;
				doConfirmDeleteAssessmentDocument(ureq, wrapper.getDocument());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(overrideScoreCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				doSetOverridenScore(overrideScoreCtrl.getNewScore());
			}
			overrideScoreCalloutCtrl.deactivate();
			cleanUp();
		} else if(overrideScoreCalloutCtrl == source) {
			overrideScoreCalloutCtrl.deactivate();
			cleanUp();
		} else if(source == confirmDeleteDocCtrl) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				File documentToDelete = (File)confirmDeleteDocCtrl.getUserObject();
				doDeleteAssessmentDocument(documentToDelete);
				reloadAssessmentDocs();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(overrideScoreCalloutCtrl);
		removeAsListenerAndDispose(overrideScoreCtrl);
		overrideScoreCalloutCtrl = null;
		overrideScoreCtrl = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if(readOnly) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		if(scoreEl != null) {
			allOk &= validateScore(scoreEl);
		}
		return allOk;
	}
	
	private boolean validateScore(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				// check with the parse algorithm of BigDecimal first
				new BigDecimal(el.getValue()).doubleValue();

				Double minScore = QtiNodesExtractor.extractMinScore(assessmentItem);
				Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
					
				double score = parseDouble(el);
				boolean boundariesOk = true;
				if(minScore != null && score < minScore.doubleValue()) {
					boundariesOk &= false;
				}
				if(maxScore != null && score > maxScore.doubleValue()) {
					boundariesOk &= false;
				}
				
				if(!boundariesOk) {
					el.setErrorKey("correction.min.max.score", new String[]{
						AssessmentHelper.getRoundedScore(minScore),  AssessmentHelper.getRoundedScore(maxScore)
					});
				}
				allOk &= boundariesOk;
			} catch (NumberFormatException e) {
				logWarn("Cannot parse the score: " + el.getValue(), null);
				el.setErrorKey("error.double.format", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private double parseDouble(TextElement textEl) throws NumberFormatException {
		String scoreStr = textEl.getValue();
		if(!StringHelper.containsNonWhitespace(scoreStr)) {
			throw new NumberFormatException();
		}
		int index = scoreStr.indexOf(',');
		if(index >= 0) {
			scoreStr = scoreStr.replace(',', '.');
			return Double.parseDouble(scoreStr);
		}
		return Double.parseDouble(scoreStr);
	}
	
	private void doUploadAssessmentDocument(File uploadedFile, String filename) {
		File directory = qtiService.getAssessmentDocumentsDirectory(correction.getTestSession(), correction.getItemSession());
		if(directory != null) {
			if(!directory.exists()) {
				directory.mkdirs();
			}
			
			File targetFile = new File(directory, filename);
			if(targetFile.exists()) {
				String newName = FileUtils.rename(targetFile);
				targetFile = new File(directory, newName);
			}
			try {
				Files.copy(uploadedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logError("", e);
			}
		}
	}
	
	private void reloadAssessmentDocs() {
		if(docsLayoutCont == null) return;

		List<DocumentWrapper> wrappers = new ArrayList<>();
		File directory = qtiService.getAssessmentDocumentsDirectory(correction.getTestSession(), correction.getItemSession());
		if(directory != null && directory.exists()) {
			File[] documents = directory.listFiles(SystemFileFilter.FILES_ONLY);
			if(documents != null) {
				for (File document : documents) {
					DocumentWrapper wrapper = new DocumentWrapper(document);
					wrappers.add(wrapper);
					
					if(!readOnly) {
						FormLink deleteButton = uifactory.addFormLink("delete_doc_" + (++count), "delete", null, docsLayoutCont, Link.BUTTON_XSMALL);
						deleteButton.setEnabled(true);  
						deleteButton.setVisible(true);
						wrapper.setDeleteButton(deleteButton);
					}
				}
			}
		}
		
		docsLayoutCont.contextPut("documents", wrappers);
		docsLayoutCont.contextPut("itemSessionKey", correction.getItemSession().getKey());
		docsLayoutCont.contextPut("testSessionKey", correction.getTestSession().getKey());
		docsLayoutCont.contextPut("documents", wrappers);
		docsLayoutCont.setVisible(!wrappers.isEmpty());

		if(uploadDocsEl != null && uploadDocsEl.isVisible()) {
			if(wrappers.isEmpty()) {
				uploadDocsEl.setLabel("assessment.item.docs.upload", null);
				scoreCont.setDirty(true);
			} else {
				uploadDocsEl.setLabel(null, null);
			}
		}
	}
	
	private void doConfirmDeleteAssessmentDocument(UserRequest ureq, File document) {
		String title = translate("warning.assessment.docs.delete.title");
		String text = translate("warning.assessment.docs.delete.text",
				new String[] { StringHelper.escapeHtml(document.getName()) });
		confirmDeleteDocCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteDocCtrl);
		confirmDeleteDocCtrl.setUserObject(document);
	}
	
	private void doDeleteAssessmentDocument(File document) {
		FileUtils.deleteFile(document);
	}
	
	private void doToggleSolution() {
		if(solutionItem.isVisible()) {
			viewSolutionButton.setIconLeftCSS("o_icon o_icon_open_togglebox");
			solutionItem.setVisible(false);
		} else {
			viewSolutionButton.setIconLeftCSS("o_icon o_icon_close_togglebox");
			solutionItem.setVisible(hasSolution());
		}
	}
	
	private void doToggleCorrectSolution() {
		if(correctSolutionItem.isVisible()) {
			viewCorrectSolutionButton.setIconLeftCSS("o_icon o_icon_open_togglebox");
			correctSolutionItem.setVisible(false);
		} else {
			viewCorrectSolutionButton.setIconLeftCSS("o_icon o_icon_close_togglebox");
			correctSolutionItem.setVisible(true);
		}
	}
	
	private void doSetOverridenScore(BigDecimal newScore) {
		overrideAutoScore = newScore;
		if(newScore == null) {
			AssessmentItemSession itemSession = correction.getItemSession();
			String score = itemSession == null ? "" : AssessmentHelper.getRoundedScore(itemSession.getScore());
			overrideScoreCont.contextPut("score", score);
		} else {
			overrideScoreCont.contextPut("score", AssessmentHelper.getRoundedScore(newScore));
		}
		
		String dirtyOnLoad = FormJSHelper.setFlexiFormDirtyOnLoad(flc.getRootForm());
		getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(dirtyOnLoad));
	}

	private void doOverrideScore(UserRequest ureq) {
		if(overrideScoreCtrl != null) return;

		overrideScoreCtrl = new OverrideScoreController(ureq, getWindowControl());
		listenTo(overrideScoreCtrl);

		overrideScoreCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				overrideScoreCtrl.getInitialComponent(), overrideScoreButton.getFormDispatchId(), "", true, "o_assessmentitem_scoring_override_window");
		listenTo(overrideScoreCalloutCtrl);
		overrideScoreCalloutCtrl.activate();
	}
	
	public class OverrideScoreController  extends FormBasicController {
		
		private TextElement newScoreEl;
		
		public OverrideScoreController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			initForm(ureq);
		}
		
		public BigDecimal getNewScore() {
			String mScore = newScoreEl.getValue();
			if(StringHelper.containsNonWhitespace(mScore)) {
				if(mScore.indexOf(',') >= 0) {
					mScore = mScore.replace(",", ".");
				}
				return new BigDecimal(mScore);
			}
			return null;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			String maScore = overrideAutoScore == null ? "" : AssessmentHelper.getRoundedScore(overrideAutoScore);
			newScoreEl = uifactory.addTextElement("new.score", "score", 6, maScore, formLayout);
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttonsCont", getTranslator());
			formLayout.add(buttonsCont);
			FormCancel cancel = uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			cancel.setElementCssClass("btn-xs");
			FormSubmit submit = uifactory.addFormSubmitButton("override.score", buttonsCont);
			submit.setElementCssClass("btn-xs");
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			allOk &= validateScore(newScoreEl);
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
	
	public static class DocumentWrapper {
		
		private final File document;
		private FormLink deleteButton;
		
		public DocumentWrapper(File document) {
			this.document = document;
		}
		
		public String getFilename() {
			return document.getName();
		}
		
		public String getLabel() {
			return document.getName() + " (" + Formatter.formatBytes(document.length()) + ")";
		}
		
		public File getDocument() {
			return document;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
			deleteButton.setUserObject(this);
		}
	}
}
