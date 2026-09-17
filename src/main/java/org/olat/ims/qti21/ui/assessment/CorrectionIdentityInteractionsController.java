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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
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
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder;
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
	
	public static final Event DOWNLOAD_PDF = new Event("download.pdf");
	private static final String[] onKeys = new String[] { "on" };

	private FormLink downloadPdfButton;
	private TextElement scoreEl;
	private RichTextElement commentEl;
	private StaticTextElement statusEl;
	private StaticTextElement scoreAutoEl;
	private FormLink viewSolutionButton;
	private FormLink adjustScoreButton;
	private FormLink resetAdjustementButton;
	private FormLink viewCorrectSolutionButton;
	private FileElement uploadDocsEl;
	private ItemBodyResultFormItem solutionItem;
	private FeedbackResultFormItem correctSolutionItem;
	private MultipleSelectionElement toReviewEl;
	private FormLayoutContainer overrideScoreCont;
	private FormLayoutContainer docsLayoutCont;
	private FormLayoutContainer scoreCont;

	private DialogBoxController confirmDeleteDocCtrl;
	private AdjustmentScoreController adjustCtrl;
	private CloseableCalloutWindowController adjustScoreCalloutCtrl;
	
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
	private final boolean downloadEnabled;
	
	private int count = 0;
	private final long id = CodeHelper.getRAMUniqueID();

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
	public CorrectionIdentityInteractionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			AssessmentItemCorrection correction, Map<Long, File> submissionDirectoryMaps, boolean readOnly,
			String mapperUri, boolean downloadEnabled, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "correction_identity_interactions", rootForm);
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.readOnly = readOnly;
		this.mapperUri = mapperUri;
		this.downloadEnabled = downloadEnabled;
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
		downloadPdfButton = uifactory.addFormLink("download.as.pdf", formLayout, Link.BUTTON);
		downloadPdfButton.setIconLeftCSS("o_icon o_icon_lg o_icon_download");
		downloadPdfButton.setVisible(downloadEnabled);
		
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
		solutionItem.setScorePerAnswers(true);
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

		String score = "";
		String coachComment = "";
		if(itemSession != null) {
			score = score(itemSession);
			coachComment = itemSession.getCoachComment();
		}
		
		scoreCont = FormLayoutContainer.createDefaultFormLayout("score.container", getTranslator());
		formLayout.add("score.container", scoreCont);
		
		statusEl = uifactory.addStaticTextElement("status", "status", "", scoreCont);
		
		String fullname = userManager.getUserDisplayName(correction.getAssessedIdentity());
		if(manualScore || correction.getItemSessionState() == null || !correction.getItemSessionState().isPresented()) {
			scoreEl = uifactory.addTextElement("scoreItem", "score", 6, score, scoreCont);
		} else {
			overrideAutoScore = itemSession == null ? null : itemSession.getManualScore();
			
			String page = velocity_root + "/override_score.html";
			overrideScoreCont = uifactory.addCustomFormLayout("extra.score", "score", page, scoreCont);
			scoreEl = uifactory.addTextElement("score", "score", 6, score, overrideScoreCont);
			scoreEl.setEnabled(false);

			adjustScoreButton = uifactory.addFormLink("adjust.score", overrideScoreCont, Link.BUTTON);
			adjustScoreButton.setIconLeftCSS("o_icon o_icon_overridden");
			adjustScoreButton.setDomReplacementWrapperRequired(false);
			adjustScoreButton.setElementCssClass("input-group-addon");
			adjustScoreButton.setVisible(!readOnly && itemSession != null && itemSession.getManualScore() == null);
			adjustScoreButton.setAriaDialogOpener();
			
			resetAdjustementButton = uifactory.addFormLink("reset.adjustement", overrideScoreCont, Link.BUTTON);
			resetAdjustementButton.setIconLeftCSS("o_icon o_icon_reset_data");
			resetAdjustementButton.setDomReplacementWrapperRequired(false);
			resetAdjustementButton.setElementCssClass("input-group-addon");
			resetAdjustementButton.setVisible(!readOnly && itemSession != null && itemSession.getManualScore() != null);
			
			scoreAutoEl = uifactory.addStaticTextElement("annotated.score.auto", "annotated.score.auto", "", scoreCont);
			String annotatedScore = getAnnotatedScoreAuto();
			scoreAutoEl.setValue(annotatedScore);
			scoreAutoEl.setVisible(StringHelper.containsNonWhitespace(annotatedScore));
			scoreEl.setEnabled(false);
		}

		statusEl.setElementCssClass("o_sel_assessment_item_status");
		statusEl.setValue(getStatus());// Need override score
		
		scoreEl.setElementCssClass("o_assessment_item_score o_sel_assessment_item_score");
		scoreEl.setMaxLength(8);
		scoreEl.setDisplaySize(8);
		
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
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("interactionWrapper", wrapper);
			layoutCont.contextPut("autoSaved", Boolean.valueOf(isAutoSaved(testPlanNodeKey, testSessionState)));
			
			List<SectionRubrics> sectionRubrics = initSectionsRubrics(layoutCont);
			layoutCont.contextPut("sectionRubrics", sectionRubrics);
			
			if(QTI21QuestionType.getTypeRelax(assessmentItem) == QTI21QuestionType.fib
					&& !(new GapAssessmentItemBuilder(assessmentItem, qtiService.qtiSerializer()).isAllowDuplicatedAnswers())) {
				setFormWarning("warning.duplicate.not.allowed");
			}
			
		}
	}
	
	private void updateScoreUI() {
		if(adjustScoreButton != null) {
			adjustScoreButton.setVisible(!readOnly && overrideAutoScore == null);
		}
		if(resetAdjustementButton != null) {
			resetAdjustementButton.setVisible(!readOnly && overrideAutoScore != null);
		}
		statusEl.setValue(getStatus());
		if(scoreAutoEl != null) {
			String scoreAuto = getAnnotatedScoreAuto();
			scoreAutoEl.setValue(scoreAuto);
			scoreAutoEl.setVisible(StringHelper.containsNonWhitespace(scoreAuto));
		}
	}
	
	private List<SectionRubrics> initSectionsRubrics(FormItemContainer layoutCont) {
		List<SectionRubrics> sectionParentLine = new ArrayList<>();
		
		try {
			AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
			for(TestPlanNode parentNode=correction.getItemNode().getParent(); parentNode.getParent() != null; parentNode = parentNode.getParent()) {
				AbstractPart part = assessmentTest.lookupFirstDescendant(parentNode.getIdentifier());
				if(part instanceof AssessmentSection section) {
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
		
		String status;
		String statusCssClass;
		String iconCssClass;
		if(itemSession == null
				|| correction.getItemSessionState() == null 
				|| !correction.getItemSessionState().isResponded()
				|| !correction.isItemSessionStatusFinal()) {
			status = "status.not.answered";
			statusCssClass = "notAnswered";
			iconCssClass = "o_icon_warning";
		} else {
			if(manualScore) {
				if(itemSession.getManualScore() == null) {
					status = "status.to.correct";
					statusCssClass = "toCorrect";
					iconCssClass = "o_icon_correction_to_correct";
				} else {
					status = "status.manual";
					statusCssClass = "manual";
					iconCssClass = "o_icon_correction_manual";
				}
			} else if(overrideAutoScore != null) {
				status = "status.adjusted";
				statusCssClass = "adjusted";
				iconCssClass = "o_icon_correction_adjusted";
			} else {
				status = "status.auto";
				statusCssClass = "auto";
				iconCssClass = "o_icon_correction_auto";
			}
		}
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("<span class=\"o_labeled_light o_assessmentitem_status ").append(statusCssClass).append("\">")
		  .append("<i class=\"o_icon o_icon-fw ").append(iconCssClass).append("\"> </i> ").append(translate(status)).append("</span>");
		return sb.toString();
	}
	
	protected String getAnnotatedScoreAuto() {
		AssessmentItemSession itemSession = correction.getItemSession();
		if(overrideAutoScore != null) {
			StringBuilder sb = new StringBuilder();
			BigDecimal score = itemSession.getScore();
			if(score == null) {
				score = BigDecimal.ZERO;
			}
			sb.append(AssessmentHelper.getRoundedScore(score));
			
			BigDecimal diff = overrideAutoScore.subtract(score);
			int comparison = diff.compareTo(BigDecimal.ZERO);
			if(comparison != 0) {
				sb.append(" (");
				if(comparison > 0) {
					sb.append("+");
				}
				sb.append(AssessmentHelper.getRoundedScore(diff)).append(")");	
			}
			return sb.toString();
		}
		
		return null;
	}
	
	protected BigDecimal getManualScore() {
		if(overrideAutoScore != null) {
			return overrideAutoScore;
		}
		if(scoreEl.isEnabled() && StringHelper.containsNonWhitespace(scoreEl.getValue())) {
			String mScore = scoreEl.getValue();
			if(mScore.indexOf(',') >= 0) {
				mScore = mScore.replace(",", ".");
			}
			// If the field has exactly the score, it's not a manually set score
			BigDecimal bScore = new BigDecimal(mScore);
			if(!manualScore && correction.getItemSession() != null
					&& correction.getItemSession().getScore() != null
					&& bScore.compareTo(correction.getItemSession().getScore()) == 0) {
				return null;
			}
			return bScore;
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
	
	private void cleanUp() {
		removeAsListenerAndDispose(adjustScoreCalloutCtrl);
		removeAsListenerAndDispose(adjustCtrl);
		adjustScoreCalloutCtrl = null;
		adjustCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == downloadPdfButton) {
			fireEvent(ureq, DOWNLOAD_PDF);
		} else if(adjustScoreButton == source) {
			doOpenAdjustment(ureq);
		} else if(resetAdjustementButton == source) {
			doResetAdjustement();
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
		if(adjustCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				doAdjustScore(adjustCtrl.getNewScore());
			}
			adjustScoreCalloutCtrl.deactivate();
			cleanUp();
		} else if(adjustScoreCalloutCtrl == source) {
			adjustScoreCalloutCtrl.deactivate();
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
				String val = el.getValue().replace(',', '.');
				new BigDecimal(val).doubleValue();

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
					el.setErrorKey("correction.min.max.score",
						AssessmentHelper.getRoundedScore(minScore),  AssessmentHelper.getRoundedScore(maxScore));
				}
				allOk &= boundariesOk;
			} catch (NumberFormatException e) {
				logWarn("Cannot parse the score: " + el.getValue(), null);
				el.setErrorKey("error.double.format");
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

	private void doOpenAdjustment(UserRequest ureq) {
		if(adjustCtrl != null) return;

		adjustCtrl = new AdjustmentScoreController(ureq, getWindowControl());
		listenTo(adjustCtrl);

		adjustScoreCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				adjustCtrl.getInitialComponent(), adjustScoreButton, "", true, "o_assessmentitem_scoring_override_window");
		listenTo(adjustScoreCalloutCtrl);
		adjustScoreCalloutCtrl.activate();
	}
	
	private void doAdjustScore(BigDecimal newScore) {
		overrideAutoScore = newScore;
		String score;
		if(newScore == null) {
			AssessmentItemSession itemSession = correction.getItemSession();
			score = itemSession == null ? "" : AssessmentHelper.getRoundedScore(itemSession.getScore());
		} else {
			score = AssessmentHelper.getRoundedScore(newScore);
		}
		scoreEl.setValue(score);
		updateScoreUI();
	}
	
	private void doResetAdjustement() {
		overrideAutoScore = null;
		BigDecimal score = correction.getItemSession().getScore();
		if(score != null) {
			scoreEl.setValue(AssessmentHelper.getRoundedScore(score));
		} else {
			scoreEl.setValue("");
		}
		updateScoreUI();
	}
	
	private String score(AssessmentItemSession itemSession) {
		if(itemSession.getManualScore() != null) {
			return AssessmentHelper.getRoundedScore(itemSession.getManualScore());
		} 
		return AssessmentHelper.getRoundedScore(itemSession.getScore());
	}
	
	public class AdjustmentScoreController extends FormBasicController {
		
		private TextElement newScoreEl;
		
		public AdjustmentScoreController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, LAYOUT_VERTICAL);
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
			
			FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttonscont", null, formLayout);
			uifactory.addFormSubmitButton("adjust.score", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
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
