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
package org.olat.course.nodes.ms;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSConfigController extends FormBasicController {
	
	private static final String[] ENABLED_KEYS = new String[]{"on"};

	private FormToggle initialStatusEl;
	private FormToggle evaluationFormEnabledEl;
	private StaticTextElement evaluationFormNotChoosen;
	private FormLink evaluationFormLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	private SingleSelection scoreTypeEl;
	private FormToggle scoreEnableEl;
	private TextElement minEl;
	private TextElement maxEl;
	private TextElement scaleEl;
	private SpacerElement gradeSpacer;
	private FormToggle gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private StaticTextElement gradePassedEl;
	private SpacerElement passedSpacer;
	private FormToggle passedEl;
	private SingleSelection passedTypeEl;
	private String[] trueFalseKeys;
	private String[] passedTypeValues;
	private TextElement cutEl;
	private TextElement scoreScalingEl;
	private FormToggle incorporateInCourseAssessmentEl;
	private SpacerElement incorporateInCourseAssessmentSpacer;
	private MultipleSelectionElement commentFlagEl;
	private MultipleSelectionElement individualAssessmentDocsFlagEl;
	private RichTextElement infotextUserEl;
	private RichTextElement infotextCoachEl;
	private FormLink showInfoTextsLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private LayoutMain3ColsPreviewController previewCtr;
	private GradeScaleEditController gradeScaleCtrl;
	
	private final ModuleConfiguration config;
	private final RepositoryEntry ores;
	private final String nodeIdent;
	private boolean showInfoTexts;
	private final boolean showInitialStatus;
	private final boolean scoreScalingEnabled;
	private final boolean ignoreInCourseAssessmentAvailable;
	private RepositoryEntry formEntry;
	private MinMax formMinMax;
	private GradeScale gradeScale;
	
	@Autowired
	private MSService msService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;

	public MSConfigController(UserRequest ureq, WindowControl wControl, ICourse course,
			MSCourseNode courseNode) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.config = courseNode.getModuleConfiguration();
		this.ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		this.nodeIdent = courseNode.getIdent();
		this.showInitialStatus = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
		ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(NodeAccessType.of(course));
		scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
		this.formEntry = MSCourseNode.getEvaluationForm(config);
		doCalculateMinMax();
		
		trueFalseKeys = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		passedTypeValues = new String[] { translate("form.passedtype.cutval"), translate("form.passedtype.manual") };
		
		initForm(ureq);
	}
	
	public void setDisplayOnly(boolean displayOnly) {
		Map<String, FormItem> formItems = flc.getFormComponents();
		for (String formItemName : formItems.keySet()) {
			formItems.get(formItemName).setEnabled(!displayOnly);
		}
		if (gradeScaleButtonsCont != null) {
			gradeScaleButtonsCont.setVisible(!displayOnly);
		}
		if (!displayOnly) {
			updateUI();
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Initial status
		if (showInitialStatus) {
			initialStatusEl = uifactory.addToggleButton("form.initial.status", "form.initial.status",
					translate("on"), translate("off"), formLayout);
			initialStatusEl.setHelpText(translate("form.initial.status.help"));
			String initialStatus = config.getStringValue(MSCourseNode.CONFIG_KEY_INITIAL_STATUS);
			initialStatusEl.toggle(AssessmentEntryStatus.inReview.name().equals(initialStatus));
			uifactory.addSpacerElement("spacerzero", formLayout, false);
		}
		
		// Evaluation Form
		evaluationFormEnabledEl = uifactory.addToggleButton("form.evaluation.enabled", "form.evaluation.enabled",
				translate("on"), translate("off"), formLayout);
		evaluationFormEnabledEl.addActionListener(FormEvent.ONCHANGE);
		Boolean evalFormEnabled = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED);
		evaluationFormEnabledEl.toggle(evalFormEnabled != null && evalFormEnabled.booleanValue());
		
		evaluationFormNotChoosen = uifactory.addStaticTextElement("form.evaluation.not.choosen", "form.evaluation",
				translate("form.evaluation.not.choosen"), formLayout);
		evaluationFormLink = uifactory.addFormLink("form.evaluation", "", translate("form.evaluation"), formLayout,
				Link.NONTRANSLATED);
		evaluationFormLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		chooseLink = uifactory.addFormLink("form.evaluation.choose", buttonsCont, "btn btn-default o_xsmall");
		replaceLink = uifactory.addFormLink("form.evaluation.replace", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("form.evaluation.edit", buttonsCont, "btn btn-default o_xsmall");
		
		uifactory.addSpacerElement("spacerone", formLayout, false);
		
		String scoreKey = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		// Points
		scoreEnableEl = uifactory.addToggleButton("form.score", "form.score", translate("on"), translate("off"), formLayout);
		scoreEnableEl.setElementCssClass("o_sel_course_ms_score");
		scoreEnableEl.addActionListener(FormEvent.ONCHANGE);
		if(MSCourseNode.CONFIG_VALUE_SCORE_NONE.equals(scoreKey)) {
			scoreEnableEl.toggleOff();
		} else {
			scoreEnableEl.toggleOn();
		}
		
		SelectionValues scoreKV = new SelectionValues();
		scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_MANUAL, translate("form.score.manual")));
		scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM, translate("form.score.eval.sum")));
		scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG, translate("form.score.eval.avg")));
		scoreTypeEl = uifactory.addDropdownSingleselect("form.score.type", formLayout, scoreKV.keys(), scoreKV.values());
		scoreTypeEl.addActionListener(FormEvent.ONCHANGE);
		if(scoreKV.containsKey(scoreKey)) {
			scoreTypeEl.select(scoreKey, true);
		}
		
		// Scale
		String scale = config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE);
		scaleEl = uifactory.addTextElement("form.scale", "form.scale", 8, scale, formLayout);
		scaleEl.addActionListener(FormEvent.ONCHANGE);
		
		// Minimum
		Float min = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		min = min != null? min: MSCourseNode.CONFIG_DEFAULT_SCORE_MIN;
		minEl = uifactory.addTextElement("form.min", "form.min", 8, min.toString(), formLayout);
		minEl.setElementCssClass("o_sel_course_ms_min");
		minEl.setMandatory(true);
		
		// Maximim
		Float max = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		max = max != null? max: MSCourseNode.CONFIG_DEFAULT_SCORE_MAX;
		maxEl = uifactory.addTextElement("form.max", "form.max", 8, max.toString(), formLayout);
		maxEl.setElementCssClass("o_sel_course_ms_max");
		maxEl.setMandatory(true);
		
		if (gradeModule.isEnabled()) {
			gradeSpacer = uifactory.addSpacerElement("spacertwo", formLayout, false);
			
			gradeEnabledEl = uifactory.addToggleButton("node.grade.enabled", "node.grade.enabled", translate("on"), translate("off"), formLayout);
			gradeEnabledEl.setElementCssClass("o_sel_course_ms_grade");
			gradeEnabledEl.addActionListener(FormEvent.ONCLICK);
			boolean gradeEnabled = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeEnabledEl.toggle(gradeEnabled);
			
			SelectionValues autoSV = new SelectionValues();
			autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
			autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
			gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
			gradeAutoEl.setElementCssClass("o_sel_course_ms_grade_mode");
			gradeAutoEl.select(Boolean.toString(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)), true);
			
			gradeScale = gradeService.getGradeScale(ores, nodeIdent);
			gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
			
			gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
			gradeScaleButtonsCont.setRootForm(mainForm);
			formLayout.add(gradeScaleButtonsCont);
			gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
			gradeScaleEditLink.setElementCssClass("o_sel_grade_edit_scale");
			
			gradePassedEl = uifactory.addStaticTextElement("node.grade.passed", "form.passed", "", formLayout);
		}
		
		passedSpacer = uifactory.addSpacerElement("spacerthree", formLayout, false);
		
		// display passed / failed
		passedEl = uifactory.addToggleButton("form.passed", "form.passed", translate("on"), translate("off"), formLayout);
		passedEl.addActionListener(FormEvent.ONCHANGE);
		Boolean passedField = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		passedEl.toggle(passedField);

		// passed/failed manually or automatically
		passedTypeEl = uifactory.addRadiosVertical("form.passed.type", formLayout, trueFalseKeys, passedTypeValues);
		passedTypeEl.addActionListener(FormEvent.ONCLICK);
		passedTypeEl.setElementCssClass("o_sel_course_ms_display_type");

		Float cut = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		if (cut != null) {
			passedTypeEl.select(trueFalseKeys[0], true);
		} else {
			passedTypeEl.select(trueFalseKeys[1], true);
			cut = Float.valueOf(0.0f);
		}

		// Passing grade cut value
		cutEl = uifactory.addTextElement("form.cut", "form.cut", 8, cut.toString(), formLayout);
		cutEl.setElementCssClass("o_sel_course_ms_cut");

		uifactory.addSpacerElement("spacer2", formLayout, false);
		
		// Ignore in course assessment
		incorporateInCourseAssessmentEl = uifactory.addToggleButton("incorporate.in.course.assessment", "incorporate.in.course.assessment",
				translate("on"), translate("off"), formLayout);
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		incorporateInCourseAssessmentEl.toggle(!ignoreInCourseAssessment);
		
		String scaling = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		scoreScalingEl = uifactory.addTextElement("score.scaling", "score.scaling", 10, scaling, formLayout);
		scoreScalingEl.setExampleKey("score.scaling.example", null);
		
		incorporateInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer3", formLayout, false);

		// Comments
		commentFlagEl = uifactory.addCheckboxesHorizontal("form.comment", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		Boolean commentField = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		commentFlagEl.select(ENABLED_KEYS[0], commentField.booleanValue());
		
		individualAssessmentDocsFlagEl = uifactory.addCheckboxesHorizontal("form.individual.assessment.docs", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		Boolean docsCf = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
		individualAssessmentDocsFlagEl.select(ENABLED_KEYS[0], docsCf);

		showInfoTextsLink = uifactory.addFormLink("show.infotexts", "show.infotexts", null, formLayout, Link.LINK);
		showInfoTextsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_open_togglebox");

		// Create the rich text fields.
		String infoUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		infoUser = infoUser != null? infoUser: "";
		infotextUserEl = uifactory.addRichTextElementForStringDataMinimalistic("infotextUser", "form.infotext.user",
				infoUser, 10, -1, formLayout, getWindowControl());
		infotextUserEl.setVisible(false);

		String infoCoach = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		infoCoach = infoCoach != null? infoCoach: "";
		infotextCoachEl = uifactory.addRichTextElementForStringDataMinimalistic("infotextCoach", "form.infotext.coach",
				infoCoach, 10, -1, formLayout, getWindowControl());
		infotextCoachEl.setVisible(false);

		uifactory.addFormSubmitButton("save", formLayout);
		
		updateUI();
	}
	
	private void updateUI() {
		boolean scoreEnabled = scoreEnableEl.isOn();
		boolean formEnabled = evaluationFormEnabledEl.isOn();
		boolean replacePossible = !msService.hasSessions(ores, nodeIdent);

		if (formEntry != null) {
			String displayname = StringHelper.escapeHtml(formEntry.getDisplayname());
			evaluationFormLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		boolean hasFormConfig = formEntry != null;
		evaluationFormNotChoosen.setVisible(formEnabled && !hasFormConfig);
		chooseLink.setVisible(formEnabled && !hasFormConfig);
		evaluationFormLink.setVisible(formEnabled && hasFormConfig);
		replaceLink.setVisible(formEnabled && hasFormConfig && replacePossible);
		editLink.setVisible(formEnabled && hasFormConfig);

		// Score
		scoreTypeEl.setVisible(scoreEnableEl.isOn() && evaluationFormEnabledEl.isOn());
		if(!scoreTypeEl.isOneSelected()) {
			scoreTypeEl.select(MSCourseNode.CONFIG_VALUE_SCORE_MANUAL, true);
		}
		String scoreKey = getScoreKey();
		
		// min / max
		boolean minMaxVisible = scoreEnabled;
		minEl.setVisible(minMaxVisible);
		maxEl.setVisible(minMaxVisible);
		minEl.setEnabled(true);
		maxEl.setEnabled(true);
		if (MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)
				|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey)) {
			minEl.setValue(formMinMax.getMin().toString());
			minEl.setEnabled(false);
			maxEl.setValue(formMinMax.getMax().toString());
			maxEl.setEnabled(false);
		}
		
		// scaling factor
		boolean scaleVisible = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)
				|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey);
		scaleEl.setVisible(scaleVisible);
		
		if (gradeEnabledEl != null) {
			gradeSpacer.setVisible(scoreEnabled);
			gradeEnabledEl.setVisible(scoreEnabled);
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

		// passed
		passedSpacer.setVisible(gradeDisable);
		passedEl.setVisible(gradeDisable);
		boolean passedTypeVisible = scoreEnabled && gradeDisable && passedEl.isOn();
		passedTypeEl.setVisible(passedTypeVisible);

		// cut value
		boolean cutVisible = passedTypeVisible && passedTypeEl.isOneSelected() && passedTypeEl.getSelected() == 0;
		cutEl.setVisible(cutVisible);
		
		// ignore in course assessment
		boolean ignoreInScoreVisible = ignoreInCourseAssessmentAvailable
				&& (scoreEnabled || passedEl.isOn());
		incorporateInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		incorporateInCourseAssessmentSpacer.setVisible(ignoreInScoreVisible);
		scoreScalingEl.setVisible(incorporateInCourseAssessmentEl.isVisible()
				&& incorporateInCourseAssessmentEl.isOn() && scoreScalingEnabled);
		
		//info texts
		showInfoTextsLink.setVisible(!showInfoTexts);
		infotextUserEl.setVisible(showInfoTexts);
		infotextCoachEl.setVisible(showInfoTexts);
	}
	
	private String getScoreKey() {
		String scoreKey;
		if(scoreEnableEl.isOn()) {
			if(scoreTypeEl.isVisible() && scoreTypeEl.isOneSelected()) {
				scoreKey = scoreTypeEl.getSelectedKey();
			} else {
				scoreKey = MSCourseNode.CONFIG_VALUE_SCORE_MANUAL;
			}
		} else {
			scoreKey = MSCourseNode.CONFIG_VALUE_SCORE_NONE;
		}
		return scoreKey;
	}

	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc != gradeScaleEditLink) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == evaluationFormEnabledEl) {
			updateUI();
		} else if (source == chooseLink || source == replaceLink) {
			doChooseEvaluationForm(ureq);
		} else if (source == editLink) {
			doEditEvaluationForm(ureq);
		} else if (source == evaluationFormLink) {
			doPreviewEvaluationForm(ureq);
		} else if (source == scoreEnableEl || source == scoreTypeEl || source == scaleEl) {
			doCalculateMinMax();
			updateUI();
		} else if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		} else if (source == gradeEnabledEl || source == passedEl || source == passedTypeEl) {
			updateUI();
		} else if (source == showInfoTextsLink) {
			showInfoTexts = true;
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (searchCtrl == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doReplaceEvaluationForm();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == previewCtr) {
			cleanUp();
		} if (gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(ores, nodeIdent);
				updateUI();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(gradeScaleCtrl);
		removeAsListenerAndDispose(previewCtr);
		removeAsListenerAndDispose(searchCtrl);
		removeAsListenerAndDispose(cmc);
		gradeScaleCtrl = null;
		previewCtr = null;
		searchCtrl = null;
		cmc = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		evaluationFormNotChoosen.clearError();
		if (evaluationFormNotChoosen.isVisible() && formEntry == null) {
			evaluationFormNotChoosen.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		minEl.clearError();
		maxEl.clearError();
		boolean minIsFloat = isFloat(minEl.getValue());
		boolean maxIsFloat = isFloat(maxEl.getValue());
		if (minEl.isVisible() && minEl.isEnabled()) {
			if(!StringHelper.containsNonWhitespace(minEl.getValue())) {
				minEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (!minIsFloat) {
				minEl.setErrorKey("form.error.wrongFloat");
				allOk = false;
			}
			
			if(!StringHelper.containsNonWhitespace(maxEl.getValue())) {
				maxEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (!maxIsFloat) {
				maxEl.setErrorKey("form.error.wrongFloat");
				allOk = false;
			}
			if (minIsFloat && maxIsFloat && isNotGreaterFloat(minEl.getValue(), maxEl.getValue())) {
				maxEl.setErrorKey("form.error.minGreaterThanMax");
				allOk = false;
			}
		}
		
		passedTypeEl.clearError();
		if (passedTypeEl.isVisible()) {
			if (!passedTypeEl.isOneSelected()) {
				passedTypeEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}
		
		cutEl.clearError();
		if (cutEl.isVisible()) {
			boolean cutIsFloat = isFloat(cutEl.getValue());
			if (!cutIsFloat) {
				cutEl.setErrorKey("form.error.wrongFloat");
				allOk = false;
			}
			if (cutIsFloat && minIsFloat && maxIsFloat
					&& notInRange(minEl.getValue(), maxEl.getValue(), cutEl.getValue())) {
				cutEl.setErrorKey("form.error.cutOutOfRange");
				allOk = false;
			}
		}
		
		scaleEl.clearError();
		if (scaleEl.isVisible()) {
			boolean scaleIsFloat = isFloat(scaleEl.getValue());
			if (!scaleIsFloat) {
				scaleEl.setErrorKey("form.error.wrongFloat");
				allOk = false;
			}
		}
		
		allOk &= ScoreScalingHelper.validateScoreScaling(scoreScalingEl);
		
		infotextCoachEl.clearError();
		if (infotextCoachEl.getValue().length() > 4000) {
			infotextCoachEl.setErrorKey("input.toolong", "4000");
			allOk = false;
		}
		
		infotextUserEl.clearError();
		if (infotextUserEl.getValue().length() > 4000) {
			infotextUserEl.setErrorKey("input.toolong", "4000");
			allOk = false;
		}
		
		return allOk;
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
	
	private boolean isNotGreaterFloat(String min, String max) {
		return Float.parseFloat(min) >= Float.parseFloat(max);
	}
	
	private boolean notInRange(String min, String max, String cut) {
		return Float.parseFloat(cut) < Float.parseFloat(min)
				|| Float.parseFloat(cut) > Float.parseFloat(max);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateConfig();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void updateConfig() {
		if (initialStatusEl != null) {
			String initialStatus = initialStatusEl.isOn()
					? AssessmentEntryStatus.inReview.name()
					: AssessmentEntryStatus.notStarted.name();
			config.setStringValue(MSCourseNode.CONFIG_KEY_INITIAL_STATUS, initialStatus);
		}
		
		boolean evalFormEnabled = evaluationFormEnabledEl.isOn();
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED, evalFormEnabled);
		if (evalFormEnabled) {
			MSCourseNode.setEvaluationFormReference(formEntry, config);
		} else {
			MSCourseNode.removeEvaluationFormReference(config);
		}
		
		if(scoreEnableEl.isOn()) {
			if(scoreTypeEl.isVisible()) {
				config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, scoreTypeEl.getSelectedKey());
			} else {
				config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, MSCourseNode.CONFIG_VALUE_SCORE_MANUAL);
			}
		} else {
			config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, MSCourseNode.CONFIG_VALUE_SCORE_NONE);
		}

		Float minScore = minEl.isVisible()
				? Float.parseFloat(minEl.getValue())
				: MSCourseNode.CONFIG_DEFAULT_SCORE_MIN;
		config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, minScore);
		
		Float maxScore = maxEl.isVisible()
				? Float.parseFloat(maxEl.getValue())
				: MSCourseNode.CONFIG_DEFAULT_SCORE_MAX;
		config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, maxScore);
		
		String scale = scaleEl.isVisible()
				? scaleEl.getValue()
				: MSCourseNode.CONFIG_DEFAULT_EVAL_FORM_SCALE;
		config.setStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE, scale);
		
		// Grade
		if (gradeEnabledEl != null) {
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isOn());
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.parseBoolean(gradeAutoEl.getSelectedKey()));
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			config.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		}
		
		boolean showPassed = passedEl.isOn();
		config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.valueOf(showPassed));
		
		if (showPassed) {
			// do cut value
			boolean cutAutomatically = Boolean.parseBoolean(passedTypeEl.getSelectedKey());
			if (cutAutomatically && cutEl.isVisible()) {
				config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, Float.valueOf(cutEl.getValue()));
			} else {
				config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			}
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		}
		
		boolean ignoreInCourseAssessment = incorporateInCourseAssessmentEl.isVisible() && !incorporateInCourseAssessmentEl.isOn();
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		if(ignoreInCourseAssessment || !scoreScalingEnabled) {
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_SCALING);
		} else {
			config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, scoreScalingEl.getValue());
		}
		
		Boolean commentFieldEnabled = Boolean.valueOf(commentFlagEl.isSelected(0));
		config.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, commentFieldEnabled);
		
		Boolean individualAssessmentEnabled = Boolean.valueOf(individualAssessmentDocsFlagEl.isSelected(0));
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, individualAssessmentEnabled);

		String infoTextUser = infotextUserEl.getValue();
		if (StringHelper.containsNonWhitespace(infoTextUser)) {
			config.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, infoTextUser);
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		}

		String infoTextCoach = infotextCoachEl.getValue();
		if (StringHelper.containsNonWhitespace(infoTextCoach)) {
			config.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, infoTextCoach);
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		}
	}

	private void doChooseEvaluationForm(UserRequest ureq) {
		searchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("form.evaluation.choose"));
		this.listenTo(searchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				searchCtrl.getInitialComponent(), true, translate("form.evaluation.choose"));
		cmc.activate();
	}

	private void doReplaceEvaluationForm() {
		formEntry = searchCtrl.getSelectedEntry();
		doCalculateMinMax();
		updateUI();
		markDirty();
	}

	private void doCalculateMinMax() {
		if (formEntry == null) {
			formMinMax = MinMax.of(Float.valueOf(0), Float.valueOf(0));
			return;
		}

		String scoreKey = scoreEnableEl != null
				? getScoreKey()
				: config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		String scale = scaleEl != null
				? scaleEl.getValue()
				: config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE);
		float scalingFactor = floatOrZero(scale);
				
		switch (scoreKey) {
		case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM:
			formMinMax = msService.calculateMinMaxSum(formEntry, scalingFactor);
			break;
		case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG:
			formMinMax = msService.calculateMinMaxAvg(formEntry, scalingFactor);
			break;
		default:
			formMinMax = null;
		}
	}

	private float floatOrZero(String val) {
		try {
			return Float.parseFloat(val);
		} catch (NumberFormatException e) {
			// 
		}
		return 0;
	}

	private void doEditEvaluationForm(UserRequest ureq) {
		String bPath = "[RepositoryEntry:" + formEntry.getKey() + "][Editor:0]";
		NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		File repositoryDir = new File(
				FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()),
				FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(formEntry);
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage, null);

		previewCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
				controller.getInitialComponent(), null);
		previewCtr.addDisposableChildController(controller);
		previewCtr.activate();
		listenTo(previewCtr);
	}

	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		Float minScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if ((minScore == null || minScore.intValue() == 0) && (maxScore == null || maxScore.intValue() == 0)) {
			showWarning("error.score.min.max.not.set");
			return;
		}
		
		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), ores, nodeIdent, minScore, maxScore, false, true);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
