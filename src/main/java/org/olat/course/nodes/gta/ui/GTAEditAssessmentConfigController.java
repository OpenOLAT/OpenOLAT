/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.SettingsContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 avr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEditAssessmentConfigController extends FormBasicController implements ReferenceContentProvider, SettingsContentProvider {

	private static final List<String> RESOURCE_TYPES = List.of(EvaluationFormResource.TYPE_NAME);
	private static final String scoreRex = "^[0-9]+(\\.[0-9]+)?$";

	private final BreadcrumbPanel stackPanel;
	
	private FormToggle scoreGranted;
	private FormToggle displayPassed;
	private TextElement cutVal;
	private TextElement minValEl;
	private TextElement maxValEl;
	private TextElement scoreScalingEl;
	private SingleSelection scoreTypeEl;
	private MultipleSelectionElement scoresSumEl;
	
	private TextElement evaluationScoreScalingEl;
	private TextElement peerReviewScoreScalingEl;
	private TextElement pointsProReviewEl;
	private TextElement maxNumberCreditableReviewsEl;
	
	private SpacerElement passedSpacer;
	
	private SingleSelection displayType;
	private MultipleSelectionElement commentFlag;
	private MultipleSelectionElement individualAssessmentDocsFlag;
	
	private FormToggle incorporateInCourseAssessmentEl;
	private SpacerElement incorporateInCourseAssessmentSpacer;
	/** Grade */
	private SpacerElement gradeSpacer;
	private FormToggle gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private StaticTextElement gradePassedEl;
	/** Evaluation */
	private FormToggle evaluationFormEnabledEl;
	private ComponentWrapperElement referenceEl;
	private IconPanelLabelTextContent iconPanelContent;
	private IconPanelLabelTextContent iconPanelSettings;
	
	/** Notice for users and coaches */
	private FormLink showInfoTextsLink;
	private RichTextElement infotextUser;
	private RichTextElement infotextCoach;

	private GradeScale gradeScale;
	private GTACourseNode gtaNode;
	private int numberOfAssessments = 0;
	private boolean peerReviewEnabled = false;
	
	private ModuleConfiguration config;
	private final boolean individualTask;
	private final NodeAccessType nodeAccessType;
	private final RepositoryEntry courseEntry;
	
	private boolean showInfoTexts = false;
	private final boolean scoreScalingEnabled;
	private final boolean ignoreInCourseAssessmentAvailable;
	
	/** The keys for true / false dropdowns. */
	private String[] trueFalseKeys;

	/** The keys for manual/automatic scoring dropdown. */
	private String[] passedTypeValues;
	
	private CloseableModalController cmc;
	private GradeScaleEditController gradeScaleCtrl;
	private EvaluationFormExecutionController previewCtr;
	private RepositoryEntryReferenceController referenceCtrl;
	
	@Autowired
	private MSService msService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public GTAEditAssessmentConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			GTACourseNode gtaNode, ICourse course) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(),
			Util.createPackageTranslator(MSEditFormController.class, getLocale(), getTranslator())));
		
		config = gtaNode.getModuleConfiguration();
		nodeAccessType = NodeAccessType.of(course);
		individualTask = gtaNode.getType().equals(GTACourseNode.TYPE_INDIVIDUAL);
		this.gtaNode = gtaNode;
		this.stackPanel = stackPanel;
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		numberOfAssessments = getNumberOfAssessments();
		ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
		
		trueFalseKeys = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		passedTypeValues = new String[] { translate("form.passedtype.cutval"), translate("form.passedtype.manual") };

		initForm(ureq);
		update(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer evaluationFormContainer = uifactory.addDefaultFormLayout("enable.evaluation", null, formLayout);
		evaluationFormContainer.setFormTitle(translate("form.evalutation.title"));
		evaluationFormContainer.setFormContextHelp("manual_user/learningresources/Course_Element_Task/");
		initEvaluationFormForm(evaluationFormContainer);
		evaluationFormContainer.setVisible(individualTask);

		FormLayoutContainer evaluationReferenceContainer = uifactory.addVerticalFormLayout("evaluation.form.entry", null, formLayout);
		initReferenceForm(evaluationReferenceContainer, ureq);
		evaluationReferenceContainer.setVisible(individualTask);
		
		FormLayoutContainer assessmentContainer = uifactory.addDefaultFormLayout("assessment.form", null, formLayout);
		assessmentContainer.setFormTitle(translate("grading.configuration.title"));
		assessmentContainer.setElementCssClass("o_sel_course_ms_form");
		initAssessmentForm(assessmentContainer, ureq);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, assessmentContainer);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initEvaluationFormForm(FormItemContainer formLayout) {
		evaluationFormEnabledEl = uifactory.addToggleButton("evaluation.enable", "evaluation.enable", translate("on"), translate("off"), formLayout);
		evaluationFormEnabledEl.addActionListener(FormEvent.ONCHANGE);
		Boolean evalFormEnabled = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED);
		evaluationFormEnabledEl.toggle(individualTask && evalFormEnabled != null && evalFormEnabled.booleanValue());
	}

	private void initReferenceForm(FormItemContainer formLayout, UserRequest ureq) {
		iconPanelContent = new IconPanelLabelTextContent("content");
		iconPanelContent.setColumnWidth(6);
		iconPanelSettings = new IconPanelLabelTextContent("content");
		iconPanelSettings.setColumnWidth(6);
		
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.form.resource.selected"))
				.withIconCss("o_icon o_FileResource-FORM_icon")
				.build();
		String selectionTitle = translate("select.form");
		CourseNodeReferenceProvider referenceProvider = new GTACourseNodeReferenceProvider(repositoryService,
				RESOURCE_TYPES, emptyStateConfig, selectionTitle, this, this);
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(config);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, getWindowControl(), formEntry, referenceProvider);
		listenTo(referenceCtrl);

		referenceEl = new ComponentWrapperElement(referenceCtrl.getInitialComponent());
		formLayout.add(referenceEl);
	}
	
	private void initAssessmentForm(FormLayoutContainer formLayout, UserRequest ureq) {
		// Create the "score granted" field...
		scoreGranted = uifactory.addToggleButton("form.score", "form.score", translate("on"), translate("off"), formLayout);
		scoreGranted.addActionListener(FormEvent.ONCHANGE);
		boolean sf = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		scoreGranted.toggle(sf);
		scoreGranted.setElementCssClass("o_sel_course_ms_score");
		
		SelectionValues scoringPK = new SelectionValues();
		scoringPK.add(SelectionValues.entry("automatic", translate("form.score.type.scoring.automatic")));
		scoringPK.add(SelectionValues.entry(MSCourseNode.CONFIG_VALUE_SCORE_MANUAL, translate("form.score.type.scoring.manual")));
		scoreTypeEl = uifactory.addRadiosVertical("form.score.type.scoring", "form.score.type.scoring", formLayout,
				scoringPK.keys(), scoringPK.values());
		scoreTypeEl.addActionListener(FormEvent.ONCHANGE);
		if(MSCourseNode.CONFIG_VALUE_SCORE_MANUAL.equals(config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE))) {
			scoreTypeEl.select(MSCourseNode.CONFIG_VALUE_SCORE_MANUAL, true);
		} else {
			scoreTypeEl.select("automatic", true);
		}
		
		SelectionValues scoreSumsOptions = getScoreSumsOptions();
		scoresSumEl = uifactory.addCheckboxesVertical("form.score.total.of", formLayout, scoreSumsOptions.keys(), scoreSumsOptions.values(), 1);
		scoresSumEl.addActionListener(FormEvent.ONCHANGE);
		String scoreParts = config.getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
		if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM)) {
			scoresSumEl.select(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM, true);
		}
		if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW)) {
			scoresSumEl.select(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW, true);
		}
		if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED)) {
			scoresSumEl.select(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED, true);
		}
		
		String evaluationScale = config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		evaluationScoreScalingEl = uifactory.addTextElement("form.score.type.scoring.scale", "form.score.type.scoring.scale", 8, evaluationScale, formLayout);
		evaluationScoreScalingEl.setDisplaySize(5);
		evaluationScoreScalingEl.setElementCssClass("o_sel_course_ms_evaluation_scale");
		evaluationScoreScalingEl.setMandatory(true);
		evaluationScoreScalingEl.addActionListener(FormEvent.ONCHANGE);
		
		String peerReviewScale = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		peerReviewScoreScalingEl = uifactory.addTextElement("form.peer.review.score.scale", "form.peer.review.score.scale", 8, peerReviewScale, formLayout);
		peerReviewScoreScalingEl.setDisplaySize(5);
		peerReviewScoreScalingEl.setElementCssClass("o_sel_course_ms_peer_review_scale");
		peerReviewScoreScalingEl.setMandatory(true);
		
		String pointsProReview = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW);
		pointsProReviewEl = uifactory.addTextElement("form.score.pro.review", "form.score.pro.review", 8, pointsProReview, formLayout);
		pointsProReviewEl.setDisplaySize(5);

		String maxNumberCreditableReviews = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_MAX_NUMBER_CREDITABLE_REVIEWS);
		maxNumberCreditableReviewsEl = uifactory.addTextElement("form.max.number.creditable.review", "form.max.number.creditable.review", 8, maxNumberCreditableReviews, formLayout);
		maxNumberCreditableReviewsEl.setDisplaySize(5);
	
		// ...minimum value...
		Float min = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		if (min == null) {
			min = Float.valueOf(0);
		}
		minValEl = uifactory.addTextElement("form.min", "form.min", 8, min.toString(), formLayout);
		minValEl.setDisplaySize(5);
		minValEl.setRegexMatchCheck(scoreRex, "form.error.wrongFloat");
		minValEl.setElementCssClass("o_sel_course_ms_min_val");
		
		Float max = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if (max == null) {
			max = Float.valueOf(0);
		}
		// ...and maximum value input.
		maxValEl = uifactory.addTextElement("form.max", "form.max", 8, max.toString(), formLayout);
		maxValEl.setDisplaySize(5);
		maxValEl.setRegexMatchCheck(scoreRex, "form.error.wrongFloat");
		maxValEl.setElementCssClass("o_sel_course_ms_max_val");
		
		if (gradeModule.isEnabled()) {
			gradeSpacer = uifactory.addSpacerElement("spacer0", formLayout, false);
			
			gradeEnabledEl = uifactory.addToggleButton("node.grade.enabled", "node.grade.enabled", translate("on"), translate("off"), formLayout);
			gradeEnabledEl.setElementCssClass("o_sel_course_ms_grade");
			gradeEnabledEl.addActionListener(FormEvent.ONCLICK);
			boolean gradeEnabled = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeEnabledEl.toggle(gradeEnabled);
			
			SelectionValues autoSV = new SelectionValues();
			autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
			autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
			gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
			gradeAutoEl.select(Boolean.toString(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)), true);
			
			gradeScale = gradeService.getGradeScale(courseEntry, gtaNode.getIdent());
			gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
			
			gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
			gradeScaleButtonsCont.setRootForm(mainForm);
			formLayout.add(gradeScaleButtonsCont);
			gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
			gradeScaleEditLink.setElementCssClass("o_sel_grade_edit_scale");
			
			gradePassedEl = uifactory.addStaticTextElement("node.grade.passed", "form.passed", "", formLayout);
		}
		
		passedSpacer = uifactory.addSpacerElement("spacer1", formLayout, false);

		// Create the "display passed / failed"
		displayPassed = uifactory.addToggleButton("form.passed", "form.passed", translate("on"), translate("off"), formLayout);
		displayPassed.setElementCssClass("o_sel_course_ms_display_passed");
		displayPassed.addActionListener(FormEvent.ONCLICK);
		Boolean pf = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if (pf == null) pf = Boolean.TRUE;
		displayPassed.toggle(pf.booleanValue());

		// ...the automatic / manual dropdown (note that TRUE means automatic and
		// FALSE means manually)...
		Float cut = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		displayType = uifactory.addRadiosVertical("form.passed.type", formLayout, trueFalseKeys, passedTypeValues);
		displayType.addActionListener(FormEvent.ONCLICK);
		displayType.setElementCssClass("o_sel_course_ms_display_type");

		displayType.select(trueFalseKeys[1], true);
		if (cut != null) {
			displayType.select(trueFalseKeys[0], true);
		}

		// ...and the passing grade input field.
		if (cut == null) cut = Float.valueOf(0);
		cutVal = uifactory.addTextElement("form.cut", "form.cut", 8, cut.toString(), formLayout);
		cutVal.setDisplaySize(5);
		cutVal.setRegexMatchCheck(scoreRex, "form.error.wrongFloat");
		cutVal.setElementCssClass("o_sel_course_ms_cut_val");

		uifactory.addSpacerElement("spacer2", formLayout, false);
		
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		incorporateInCourseAssessmentEl = uifactory.addToggleButton("incorporate.in.course.assessment", "incorporate.in.course.assessment",
				translate("on"), translate("off"), formLayout);
		incorporateInCourseAssessmentEl.addActionListener(FormEvent.ONCHANGE);
		incorporateInCourseAssessmentEl.toggle(!ignoreInCourseAssessment);
		
		String scaling = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		scoreScalingEl = uifactory.addTextElement("score.scaling", "score.scaling", 10, scaling, formLayout);
		scoreScalingEl.setExampleKey("score.scaling.example", null);
		scoreScalingEl.addActionListener(FormEvent.ONCHANGE);
		
		incorporateInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer3", formLayout, false);
		
		// Create the "individual comment" dropdown.
		commentFlag = uifactory.addCheckboxesHorizontal("form.comment", formLayout, new String[]{"xx"}, new String[]{null});
		Boolean cf = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		if (cf == null) cf = Boolean.TRUE;
		commentFlag.select("xx", cf.booleanValue());
		
		individualAssessmentDocsFlag = uifactory.addCheckboxesHorizontal("form.individual.assessment.docs", formLayout, new String[]{"xx"}, new String[]{null});
		boolean docsCf = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
		if(docsCf) {
			individualAssessmentDocsFlag.select("xx", true);
		}

		showInfoTextsLink = uifactory.addFormLink("show.infotexts", "show.infotexts", null, formLayout, Link.LINK);
		showInfoTextsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_open_togglebox");

		// Create the rich text fields.
		String infoUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		infotextUser = uifactory.addRichTextElementForStringDataMinimalistic("infotextUser", "form.infotext.user", infoUser, 10, -1,
				formLayout, getWindowControl());

		String infoCoach = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		infotextCoach = uifactory.addRichTextElementForStringDataMinimalistic("infotextCoach", "form.infotext.coach", infoCoach, 10, -1,
				formLayout, getWindowControl());
		showInfoTexts = StringHelper.containsNonWhitespace(infoUser) || StringHelper.containsNonWhitespace(infoCoach);

		update(ureq);
	}
	
	private SelectionValues getScoreSumsOptions() {
		peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		
		SelectionValues optionsPK = new SelectionValues();
		if(individualTask && evaluationFormEnabledEl.isOn()) {
			optionsPK.add(SelectionValues.entry(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM, translate("form.score.total.of.evaluation.form")));
		}
		if(peerReviewEnabled) {
			optionsPK.add(SelectionValues.entry(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW, translate("form.score.total.of.peer.review")));
			optionsPK.add(SelectionValues.entry(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED, translate("form.score.total.of.submitted.review")));
		}
		return optionsPK;
	}
	
	protected void update(UserRequest ureq) {
		update(ureq, false, false);
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param grantScore The toggle to grant the score has changed
	 */
	private void update(UserRequest ureq, boolean grantScore, boolean forceMinMax) {
		peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		
		boolean evaluationEnabled = evaluationFormEnabledEl.isVisible() && evaluationFormEnabledEl.isOn();
		referenceCtrl.getInitialComponent().setVisible(evaluationEnabled);

		boolean scoreEnable = scoreGranted.isOn();
		
		minValEl.setVisible(scoreEnable);
		maxValEl.setVisible(scoreEnable);
		minValEl.setMandatory(minValEl.isVisible());
		maxValEl.setMandatory(maxValEl.isVisible());
		
		if (gradeEnabledEl != null) {
			gradeSpacer.setVisible(scoreGranted.isOn());
			gradeEnabledEl.setVisible(scoreGranted.isOn());
			gradeAutoEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isOn());
			String gradeScaleText = gradeScale == null
					? translate("node.grade.scale.not.available")
					: GradeUIFactory.translateGradeSystemName(getTranslator(), gradeScale.getGradeSystem());
			gradeScaleEl.setValue(gradeScaleText);
			gradeScaleEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isOn());
			gradeScaleButtonsCont.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isOn());
			
			GradeScoreRange minRange = gradeService.getMinPassedGradeScoreRange(gradeScale, getLocale());
			gradePassedEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isOn() && minRange != null);
			gradePassedEl.setValue(GradeUIFactory.translateMinPassed(getTranslator(), minRange));
		}
		
		boolean gradeDisable = gradeEnabledEl == null || !gradeEnabledEl.isVisible() || !gradeEnabledEl.isOn();
		
		passedSpacer.setVisible(gradeDisable);
		displayPassed.setVisible(gradeDisable);
		displayType.setVisible(displayPassed.isOn() && gradeDisable);
		cutVal.setVisible(displayType.isVisible() && displayType.isSelected(0));
		cutVal.setMandatory(cutVal.isVisible());

		scoreTypeEl.setVisible(scoreGranted.isOn() && evaluationEnabled);
		boolean scoreAuto = isScoreAuto();
		minValEl.setEnabled(!scoreAuto);
		maxValEl.setEnabled(!scoreAuto);
		evaluationScoreScalingEl.setVisible(scoreGranted.isOn() && scoreAuto && evaluationEnabled);
		
		scoresSumEl.setVisible(scoreGranted.isOn() && scoreAuto && (evaluationEnabled || peerReviewEnabled));
		SelectionValues scoresSumPK = getScoreSumsOptions();
		Collection<String> selectedScoresForSum = scoresSumEl.getSelectedKeys();
		scoresSumEl.setKeysAndValues(scoresSumPK.keys(), scoresSumPK.values());
		for(String selectedScoreForSum:selectedScoresForSum) {
			if(scoresSumPK.containsKey(selectedScoreForSum)) {
				scoresSumEl.select(selectedScoreForSum, true);
			}
		}
		
		// Set default value
		if(grantScore && scoreEnable && evaluationEnabled
				&& scoresSumPK.containsKey(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM)) {
			scoresSumEl.select(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM, true);
		}
		
		MinMax formMinMax = calculateMinMax();
		if(formMinMax != null && (scoreAuto || forceMinMax)) {
			minValEl.setValue(AssessmentHelper.getRoundedScore(formMinMax.getMin()));
			maxValEl.setValue(AssessmentHelper.getRoundedScore(formMinMax.getMax()));
		}
		boolean scoreEvaluationForm = scoresSumEl.isKeySelected(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM);
		evaluationScoreScalingEl.setVisible(scoreEnable && scoreAuto && evaluationEnabled && scoreEvaluationForm);
		if(evaluationScoreScalingEl.isVisible() && !StringHelper.containsNonWhitespace(evaluationScoreScalingEl.getValue())) {
			evaluationScoreScalingEl.setValue("1.0");
		}
		
		boolean scorePeerReview = scoresSumEl.isKeySelected(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW);
		peerReviewScoreScalingEl.setVisible(scoreEnable && scoreAuto && peerReviewEnabled && scorePeerReview);
		if(peerReviewScoreScalingEl.isVisible() && !StringHelper.containsNonWhitespace(peerReviewScoreScalingEl.getValue())) {
			peerReviewScoreScalingEl.setValue("1.0");
		}

		boolean scoreReview = scoresSumEl.isKeySelected(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED);
		pointsProReviewEl.setVisible(scoreEnable && peerReviewEnabled && scoreReview);
		maxNumberCreditableReviewsEl.setVisible(scoreEnable && peerReviewEnabled && scoreReview);
				
		boolean ignoreInScoreVisible = ignoreInCourseAssessmentAvailable
				&& (scoreGranted.isOn() || displayPassed.isOn());
		incorporateInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		incorporateInCourseAssessmentSpacer.setVisible(ignoreInScoreVisible);
		
		scoreScalingEl.setVisible(incorporateInCourseAssessmentEl.isVisible()
				&& incorporateInCourseAssessmentEl.isOn()
				&& scoreGranted.isOn() && scoreScalingEnabled);
		
		showInfoTextsLink.setVisible(!showInfoTexts);
		infotextUser.setVisible(showInfoTexts);
		infotextCoach.setVisible(showInfoTexts);
		
		validateFormLogic(ureq);
	}
	
	private boolean isScoreAuto() {
		return scoreTypeEl.isVisible() && scoreTypeEl.isOneSelected() && "automatic".equals(scoreTypeEl.getSelectedKey());
	}
	
	private void updateMinMax() {
		boolean scoreAuto = isScoreAuto();
		MinMax formMinMax = calculateMinMax();
		if(formMinMax != null && scoreAuto) {
			minValEl.setValue(AssessmentHelper.getRoundedScore(formMinMax.getMin()));
			maxValEl.setValue(AssessmentHelper.getRoundedScore(formMinMax.getMax()));
		}
	}

	@Override
	public Controller getEditSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		EvaluationFormSettingsController settingsCtrl = new EvaluationFormSettingsController(ureq, wControl, config,
				MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
		listenTo(settingsCtrl);
		return settingsCtrl;
	}

	@Override
	public Component getSettingsContent(RepositoryEntry repositoryEntry) {
		updateSettingsPanel();
		return iconPanelSettings;
	}
	
	private void updateSettingsPanel() {
		String scoreKey = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
		String i18nScoreKey;
		if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.sum";
		} else if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.avg";
		} else {
			i18nScoreKey = "score.evaluation.points.undefined";
		}

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.points"), translate(i18nScoreKey)));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.of.form.assessment"), Integer.toString(numberOfAssessments)));
		iconPanelSettings.setLabelTexts(labelTexts);
	}

	@Override
	public void refreshSettings(Component cmp, RepositoryEntry repositoryEntry) {
		//
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		MinMax minMaxAvg = msService.calculateMinMaxAvg(repositoryEntry, 1.0f);
		MinMax minMaxSum = msService.calculateMinMaxSum(repositoryEntry, 1.0f);
		
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		if (minMaxSum != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.sum"), AssessmentHelper.getRoundedScore(minMaxSum.getMax())));
		}
		if (minMaxAvg != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.avg"), AssessmentHelper.getRoundedScore(minMaxAvg.getMax())));
		}
		iconPanelContent.setLabelTexts(labelTexts);
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(courseEntry, gtaNode.getIdent());
				update(ureq);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (referenceCtrl == source) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doSaveEvaluation(ureq, referenceCtrl.getRepositoryEntry());
				update(ureq);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq, referenceCtrl.getRepositoryEntry());
			}
		} else if(source instanceof EvaluationFormSettingsController) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				updateSettingsPanel();
				update(ureq);
			}
			removeAsListenerAndDispose(source);
		} else if (cmc == source) {
			cleanUp();
		} 
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(gradeScaleCtrl);
		removeAsListenerAndDispose(cmc);
		gradeScaleCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		// coach info text
		infotextCoach.clearError();
		if (infotextCoach.getValue().length() > 4000) {
			infotextCoach.setErrorKey("input.toolong", "4000");
			allOk &= false;
		}
		
		// user info text
		infotextUser.clearError();
		if (infotextUser.getValue().length() > 4000) {
			infotextUser.setErrorKey("input.toolong", "4000");
			allOk &= false;
		}
		
		scoresSumEl.clearError();
		if(scoresSumEl.isVisible() && scoreTypeEl.isVisible() && scoreTypeEl.isOneSelected()
				&& !MSCourseNode.CONFIG_VALUE_SCORE_MANUAL.equals(scoreTypeEl.getSelectedKey())
				&& scoresSumEl.getSelectedKeys().isEmpty()) {
			scoresSumEl.setErrorKey("error.score.sum.at.least.one");
			allOk &= false;
		}
		
		// score flag
		minValEl.clearError();
		maxValEl.clearError();
		evaluationScoreScalingEl.clearError();
		if (scoreGranted.isOn()) {
			if (!minValEl.getValue().matches(scoreRex)) {
				minValEl.setErrorKey("form.error.wrongFloat");
				allOk &= false;
			}
			if (!maxValEl.getValue().matches(scoreRex)) {
				maxValEl.setErrorKey("form.error.wrongFloat");
				allOk &= false;
			} else if (Float.parseFloat(minValEl.getValue()) > Float.parseFloat(maxValEl.getValue())) {
				maxValEl.setErrorKey("form.error.minGreaterThanMax");
				allOk &= false;
			}
			
			if(individualTask && evaluationFormEnabledEl.isOn() && !evaluationScoreScalingEl.getValue().matches(scoreRex)) {
				evaluationScoreScalingEl.setErrorKey("form.error.wrongFloat");
				allOk &= false;
			}
		}
		
		// display flag
		cutVal.clearError();
		displayType.clearError();
		if (displayPassed.isOn() && displayType.isSelected(0)) {
			if (Boolean.parseBoolean(displayType.getSelectedKey()) && !scoreGranted.isOn()) {
				displayType.setErrorKey("form.error.cutButNoScore");
				allOk &= false;
			}
			if (!cutVal.getValue().matches(scoreRex)) {
				cutVal.setErrorKey("form.error.wrongFloat");
				allOk &= false;
			} else if (Float.parseFloat(cutVal.getValue()) < Float.parseFloat(minValEl.getValue())
					|| Float.parseFloat(cutVal.getValue()) > Float.parseFloat(maxValEl.getValue())) {
				cutVal.setErrorKey("form.error.cutOutOfRange");
				allOk &= false;
			}
		}
		
		allOk &= ScoreScalingHelper.validateScoreScaling(scoreScalingEl);
		allOk &= ScoreScalingHelper.validateScoreScaling(evaluationScoreScalingEl);
		allOk &= ScoreScalingHelper.validateScoreScaling(peerReviewScoreScalingEl);
		
		allOk &= validateFloat(pointsProReviewEl, true);
		allOk &= validateFloat(maxNumberCreditableReviewsEl, false);
		
		return allOk;
	}
	
	private boolean validateFloat(TextElement el, boolean floatAllow) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				if(floatAllow) {
					try {
						Float.parseFloat(value);
					} catch(Exception e) {
						el.setErrorKey("form.error.wrongFloat");
						allOk = false;
					}
				} else {
					try {
						Integer.parseInt(value);
					} catch(Exception e) {
						el.setErrorKey("form.error.positive.integer");
						allOk = false;
					}
				}
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc == evaluationScoreScalingEl || fiSrc == scoreScalingEl) {
			//Don't refresh
		} else if(fiSrc == evaluationFormEnabledEl) {
			flc.setDirty(true);
		} else if (fiSrc != gradeScaleEditLink) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		} else if (source == showInfoTextsLink) {
			showInfoTexts = true;
			update(ureq);
		} else if(evaluationFormEnabledEl == source) {
			update(ureq);
		} else if(evaluationScoreScalingEl == source || scoreScalingEl == source) {
			updateMinMax();
		} else if(scoreGranted == source) {
			update(ureq, scoreGranted.isOn(), true);	
		} else if(scoreTypeEl == source) {
			boolean grantScore = isScoreAuto() && scoreGranted.isVisible() && scoreGranted.isOn();
			update(ureq, grantScore, true);	
		} else {
			update(ureq);
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public void updateModuleConfiguration(ModuleConfiguration moduleConfiguration) {
		this.config = moduleConfiguration;
		peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		
		updateModuleConfigurationEvaluationForm();

		// mandatory score flag
		Boolean scoreEnabled = Boolean.valueOf(scoreGranted.isOn());
		moduleConfiguration.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, scoreEnabled);

		if (scoreEnabled.booleanValue()) {
			// do min/max value
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, Float.valueOf(minValEl.getValue()));
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, Float.valueOf(maxValEl.getValue()));
			if(individualTask && evaluationFormEnabledEl.isOn()) {
				String scale = evaluationScoreScalingEl.isVisible()
						? evaluationScoreScalingEl.getValue()
						: MSCourseNode.CONFIG_DEFAULT_EVAL_FORM_SCALE;
				moduleConfiguration.setStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE, scale);
			}
			
			final String formEvaluationScoreCalculation = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
			final boolean manualScore = !scoreTypeEl.isVisible() || (scoreTypeEl.isOneSelected() && MSCourseNode.CONFIG_VALUE_SCORE_MANUAL.equals(scoreTypeEl.getSelectedKey()));
			if(manualScore) {
				moduleConfiguration.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, MSCourseNode.CONFIG_VALUE_SCORE_MANUAL);
			} else if(StringHelper.containsNonWhitespace(formEvaluationScoreCalculation)) {
				moduleConfiguration.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, formEvaluationScoreCalculation);
			} else {
				moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_SCORE);
			}
			
			if(peerReviewEnabled) {
				String scale = peerReviewScoreScalingEl.isVisible()
						? peerReviewScoreScalingEl.getValue()
						: MSCourseNode.CONFIG_DEFAULT_EVAL_FORM_SCALE;
				moduleConfiguration.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM_SCALE, scale);
				
				String pointsProReview = pointsProReviewEl.isVisible()
						? pointsProReviewEl.getValue()
						: null;
				if(StringHelper.containsNonWhitespace(pointsProReview)) {
					moduleConfiguration.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW, pointsProReview);
				}
				
				String maxNumberCreditableReviews = maxNumberCreditableReviewsEl.isVisible()
						? maxNumberCreditableReviewsEl.getValue()
						: null;
				if(StringHelper.containsNonWhitespace(maxNumberCreditableReviews)) {
					moduleConfiguration.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_MAX_NUMBER_CREDITABLE_REVIEWS, maxNumberCreditableReviews);
				}
			}

			List<String> scoreParts = new ArrayList<>(scoresSumEl.getSelectedKeys());
			if(!scoresSumEl.isVisible() && !manualScore) {
				if(evaluationFormEnabledEl.isOn()) {
					scoreParts.add(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM);
				}
				if(peerReviewEnabled) {
					scoreParts.add(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW);
				}	
			}
			if(scoreParts.isEmpty()) {
				moduleConfiguration.remove(GTACourseNode.GTASK_SCORE_PARTS);
			} else {
				moduleConfiguration.setStringValue(GTACourseNode.GTASK_SCORE_PARTS, String.join(",", scoreParts));
			}
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_SCORE_MIN);
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_SCORE_MAX);
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE);
			moduleConfiguration.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, MSCourseNode.CONFIG_VALUE_SCORE_NONE);
			moduleConfiguration.remove(GTACourseNode.GTASK_SCORE_PARTS);
		}
		
		// Grade
		if (gradeEnabledEl != null) {
			moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isOn());
			moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.parseBoolean(gradeAutoEl.getSelectedKey()));
		} else {
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		}
		
		// mandatory passed flag
		Boolean pf = Boolean.valueOf(displayPassed.isOn());
		moduleConfiguration.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, pf);
		if (pf.booleanValue()) {
			// do cut value
			Boolean cf = Boolean.valueOf(displayType.getSelectedKey());
			if (cf.booleanValue() && cutVal.isVisible()) {
				moduleConfiguration.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, Float.valueOf(cutVal.getValue()));
			} else {
				// remove old config
				moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			}
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		}
		
		boolean ignoreInCourseAssessment = incorporateInCourseAssessmentEl.isVisible() && !incorporateInCourseAssessmentEl.isOn();
		moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		if(ignoreInCourseAssessment || !scoreScalingEnabled) {
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_SCALING);
		} else {
			config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, scoreScalingEl.getValue());
		}

		// mandatory comment flag
		moduleConfiguration.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.valueOf(commentFlag.isSelected(0)));
		// individual assessment docs
		boolean withAssessmentDocs = individualAssessmentDocsFlag.isVisible() && individualAssessmentDocsFlag.isSelected(0);
		moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, withAssessmentDocs);

		// set info text only if something is in there
		String iu = infotextUser.getValue();
		if (StringHelper.containsNonWhitespace(iu)) {
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, iu);
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		}

		String ic = infotextCoach.getValue();
		if (StringHelper.containsNonWhitespace(ic)) {
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, ic);
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		}
	}
	
	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		Float minScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if ((minScore == null || minScore.intValue() == 0) && (maxScore == null || maxScore.intValue() == 0)) {
			showWarning("error.score.min.max.not.set");
			return;
		}
		
		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), courseEntry, gtaNode.getIdent(),
				minScore, maxScore, false, true);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private int getNumberOfAssessments() {	
		List<EvaluationFormSession> sessions = msService.getSessions(courseEntry, gtaNode.getIdent(), GTACourseNode.getEvaluationFormProvider());
		return sessions.size();
	}
	
	private MinMax calculateMinMax() {
		MinMax formMinMax = calculateMinMax(MSCourseNode.getEvaluationForm(config),
				config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM), evaluationScoreScalingEl);
		MinMax peerReviewMinMax = calculateMinMax(GTACourseNode.getPeerReviewEvaluationForm(config),
				config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM), peerReviewScoreScalingEl);
		
		Float min = formMinMax == null ? 0.0f : formMinMax.getMin();
		Float max = formMinMax == null ? 0.0f : formMinMax.getMax();
		if(peerReviewMinMax != null) {
			min = add(min, peerReviewMinMax.getMin());
			max = add(max, peerReviewMinMax.getMax());
		}
		
		if(maxNumberCreditableReviewsEl.isVisible() && StringHelper.isLong(maxNumberCreditableReviewsEl.getValue())
				&& pointsProReviewEl.isVisible() && isFloat(pointsProReviewEl.getValue())) {
			int maxNumber = Integer.parseInt(maxNumberCreditableReviewsEl.getValue());
			float pointsPerReview = Float.parseFloat(pointsProReviewEl.getValue());
			min = add(min, 0.0f);
			max = add(max, maxNumber * pointsPerReview);
			
		}
		return MinMax.of(min, max);
	}
	
	private Float add(Float val1, Float val2) {
		return val1 == null ? val2 : (val2 == null ? val1 : Float.valueOf(val1.floatValue() + val2.floatValue()));
	}
	
	private boolean isFloat(String val) {
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				Float.parseFloat(val);
				return true;
			} catch (NumberFormatException e) {
				// 
			}
		}
		return false;
	}
	
	private MinMax calculateMinMax(RepositoryEntry formEntry, String scoreKey, TextElement scaleEl) {
		MinMax formMinMax = null;
		if (formEntry != null) {
			if(StringHelper.containsNonWhitespace(scoreKey)) {
				Float scalingFactor = scaleEl.isVisible() ? getFloat(scaleEl) : null;
				float scale = scalingFactor == null ? 1.0f : scalingFactor.floatValue();
				switch (scoreKey) {
					case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM:
						formMinMax = msService.calculateMinMaxSum(formEntry, scale);
						break;
					case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG:
						formMinMax = msService.calculateMinMaxAvg(formEntry, scale);
						break;
					default:
						break;
				}
			}
		}
		return formMinMax;
	}
	
	private Float getFloat(TextElement el) {
		String val = el != null && el.isVisible() ? el.getValue() : null;	
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				return Float.valueOf(val);
			} catch (NumberFormatException e) {
				// 
			}
		}
		return null;
	}
	
	private void doPreview(UserRequest ureq, RepositoryEntry formEntry) {
		
		File repositoryDir = FileResourceManager.getInstance().getFileResourceZipDir(formEntry.getOlatResource());
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(formEntry);
		previewCtr = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage,
				FormCourseNode.EMPTY_STATE);
		listenTo(previewCtr);

		stackPanel.pushController(translate("preview"), previewCtr);
	}
	
	private void doSaveEvaluation(UserRequest ureq, RepositoryEntry formEntry) {
		boolean evalFormEnabled = evaluationFormEnabledEl.isOn();
		if (evalFormEnabled) {
			MSCourseNode.setEvaluationFormReference(formEntry, config);
		} else {
			MSCourseNode.removeEvaluationFormReference(config);
		}
		updateModuleConfigurationEvaluationForm();
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		updateSettingsPanel();
	}

	private void updateModuleConfigurationEvaluationForm() {
		if(individualTask) {
			boolean evalFormEnabled = evaluationFormEnabledEl.isOn();
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED, evalFormEnabled);
			String currentScoringMethod = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
			String currentEvalScoringMethod = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
			if (evalFormEnabled) {
				if(!StringHelper.containsNonWhitespace(currentEvalScoringMethod)) {
					config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM, MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM);
				}
				
				MinMax minMax = calculateMinMax();
				if(minMax != null) {
					config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, minMax.getMin());
					config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, minMax.getMax());
				}
			} else {
				config.remove(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
				
				if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(currentScoringMethod)
						|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(currentScoringMethod)) {
					config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, MSCourseNode.CONFIG_VALUE_SCORE_MANUAL);
				}
			}
		} else {
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED, false);
		}
	}
	
	public class GTACourseNodeReferenceProvider extends CourseNodeReferenceProvider {
		
		private final SettingsContentProvider settingsProvider;
		
		public GTACourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
				EmptyStateConfig emptyStateConfig, String selectionTitle, ReferenceContentProvider referenceContentProvider,
				SettingsContentProvider settingsProvider) {
			super(repositoryService, resourceTypes,  emptyStateConfig, selectionTitle, referenceContentProvider);
			this.settingsProvider = settingsProvider;
		}

		@Override
		public String getWarningMessage() {
			if(numberOfAssessments > 0) {
				return translate("warning.form.in.use");
			}
			return null;
		}

		@Override
		public boolean hasSettings() {
			return true;
		}

		@Override
		public SettingsContentProvider getSettingsContentProvider() {
			return settingsProvider;
		}

		@Override
		public boolean canCreate() {
			return numberOfAssessments == 0;
		}

		@Override
		public boolean canImport() {
			return numberOfAssessments == 0;
		}

		@Override
		public boolean isReplaceable(RepositoryEntry repositoryEntry) {
			return numberOfAssessments == 0 && super.isReplaceable(repositoryEntry);
		}

		@Override
		public boolean isEditable(RepositoryEntry repositoryEntry, Identity identity) {
			return numberOfAssessments == 0 && super.isEditable(repositoryEntry, identity);
		}

		@Override
		public boolean isSettingsEditable(RepositoryEntry repositoryEntry, Identity identity) {
			return true;
		}
	}
}
