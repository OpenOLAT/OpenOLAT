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
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.MSCourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
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
	
	private static final String[] EMPTY_ARRAY = new String[]{};
	private static final String[] ENABLED_KEYS = new String[]{"on"};

	private MultipleSelectionElement initialStatusEl;
	private MultipleSelectionElement evaluationFormEnabledEl;
	private StaticTextElement evaluationFormNotChoosen;
	private FormLink evaluationFormLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	private SingleSelection scoreEl;
	private TextElement minEl;
	private TextElement maxEl;
	private TextElement scaleEl;
	private SpacerElement gradeSpacer;
	private MultipleSelectionElement gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private MultipleSelectionElement passedEl;
	private SingleSelection passedTypeEl;
	private String[] trueFalseKeys;
	private String[] passedTypeValues;
	private TextElement cutEl;
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private SpacerElement ignoreInCourseAssessmentSpacer;
	private MultipleSelectionElement commentFlagEl;
	private MultipleSelectionElement individualAssessmentDocsFlagEl;
	private RichTextElement infotextUserEl;
	private RichTextElement infotextCoachEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private LayoutMain3ColsPreviewController previewCtr;
	private GradeScaleEditController gradeScaleCtrl;
	
	private final ModuleConfiguration config;
	private final RepositoryEntry ores;
	private final String nodeIdent;
	private final boolean showInitialStatus;
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
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(NodeAccessType.of(course));
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
		formLayout.setElementCssClass("o_sel_course_ms");
		
		// Initial status
		if (showInitialStatus) {
			initialStatusEl = uifactory.addCheckboxesHorizontal("form.initial.status", formLayout, ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
			initialStatusEl.setHelpUrl("form.initial.status.help");
			String initialStatus = config.getStringValue(MSCourseNode.CONFIG_KEY_INITIAL_STATUS);
			initialStatusEl.select(ENABLED_KEYS[0], AssessmentEntryStatus.inReview.name().equals(initialStatus));
		}
		
		// Evaluation Form
		evaluationFormEnabledEl = uifactory.addCheckboxesHorizontal("form.evaluation.enabled", formLayout,
				ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
		evaluationFormEnabledEl.addActionListener(FormEvent.ONCHANGE);
		Boolean evalFormEnabled = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED);
		evaluationFormEnabledEl.select(ENABLED_KEYS[0], evalFormEnabled);
		
		evaluationFormNotChoosen = uifactory.addStaticTextElement("form.evaluation.not.choosen", "form.evaluation",
				translate("form.evaluation.not.choosen"), formLayout);
		evaluationFormLink = uifactory.addFormLink("form.evaluation", "", translate("form.evaluation"), formLayout,
				Link.NONTRANSLATED);
		evaluationFormLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("form.evaluation.choose", buttonsCont, "btn btn-default o_xsmall");
		replaceLink = uifactory.addFormLink("form.evaluation.replace", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("form.evaluation.edit", buttonsCont, "btn btn-default o_xsmall");
		
		uifactory.addSpacerElement("spacer0", formLayout, false);
		
		// Points
		scoreEl = uifactory.addDropdownSingleselect("form.score", formLayout, EMPTY_ARRAY, EMPTY_ARRAY);
		scoreEl.setElementCssClass("o_sel_course_ms_score");
		scoreEl.addActionListener(FormEvent.ONCHANGE);
		
		// Scale
		String scale = config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE);
		scaleEl = uifactory.addTextElement("form.scale", "form.scale", 8, scale, formLayout);
		scaleEl.addActionListener(FormEvent.ONCHANGE);
		
		// Minimum
		Float min = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		min = min != null? min: MSCourseNode.CONFIG_DEFAULT_SCORE_MIN;
		minEl = uifactory.addTextElement("form.min", "form.min", 8, min.toString(), formLayout);
		minEl.setElementCssClass("o_sel_course_ms_min");
		
		// Maximim
		Float max = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		max = max != null? max: MSCourseNode.CONFIG_DEFAULT_SCORE_MAX;
		maxEl = uifactory.addTextElement("form.max", "form.max", 8, max.toString(), formLayout);
		maxEl.setElementCssClass("o_sel_course_ms_max");
		
		if (gradeModule.isEnabled()) {
			gradeSpacer = uifactory.addSpacerElement("spacer0", formLayout, false);
			
			gradeEnabledEl = uifactory.addCheckboxesHorizontal("node.grade.enabled", formLayout, ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
			gradeEnabledEl.addActionListener(FormEvent.ONCLICK);
			boolean gradeEnabled = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeEnabledEl.select(gradeEnabledEl.getKey(0), gradeEnabled);
			
			SelectionValues autoSV = new SelectionValues();
			autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
			autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
			gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
			gradeAutoEl.select(Boolean.valueOf(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)).toString(), true);
			
			gradeScale = gradeService.getGradeScale(ores, nodeIdent);
			gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
			
			gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
			gradeScaleButtonsCont.setRootForm(mainForm);
			formLayout.add(gradeScaleButtonsCont);
			gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
		}
		
		uifactory.addSpacerElement("spacer1", formLayout, false);
		
		// display passed / failed
		passedEl = uifactory.addCheckboxesHorizontal("form.passed", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		passedEl.addActionListener(FormEvent.ONCLICK);
		Boolean passedField = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		passedEl.select(ENABLED_KEYS[0], passedField);

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
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", formLayout,
				new String[] { "xx" }, new String[] { null });
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		ignoreInCourseAssessmentEl.select(ignoreInCourseAssessmentEl.getKey(0), ignoreInCourseAssessment);
		
		ignoreInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer3", formLayout, false);

		// Comments
		commentFlagEl = uifactory.addCheckboxesHorizontal("form.comment", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		Boolean commentField = config.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		commentFlagEl.select(ENABLED_KEYS[0], commentField.booleanValue());
		
		individualAssessmentDocsFlagEl = uifactory.addCheckboxesHorizontal("form.individual.assessment.docs", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		Boolean docsCf = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
		individualAssessmentDocsFlagEl.select(ENABLED_KEYS[0], docsCf);

		uifactory.addSpacerElement("spacer4", formLayout, false);

		// Create the rich text fields.
		String infoUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		infoUser = infoUser != null? infoUser: "";
		infotextUserEl = uifactory.addRichTextElementForStringDataMinimalistic("infotextUser", "form.infotext.user",
				infoUser, 10, -1, formLayout, getWindowControl());

		String infoCoach = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		infoCoach = infoCoach != null? infoCoach: "";
		infotextCoachEl = uifactory.addRichTextElementForStringDataMinimalistic("infotextCoach", "form.infotext.coach",
				infoCoach, 10, -1, formLayout, getWindowControl());

		uifactory.addFormSubmitButton("save", formLayout);
		
		updateUI();
	}
	
	private void updateUI() {
		boolean formEnabled = evaluationFormEnabledEl.isAtLeastSelected(1);
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
		String scoreKey = scoreEl.isOneSelected()
				? scoreEl.getSelectedKey()
				: config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		SelectionValues scoreKV = new SelectionValues();
		scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_NONE, translate("form.score.none")));
		scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_MANUAL, translate("form.score.manual")));
		if (evaluationFormEnabledEl.isAtLeastSelected(1)) {
			scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM, translate("form.score.eval.sum")));
			scoreKV.add(entry(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG, translate("form.score.eval.avg")));
		}
		scoreKey = scoreKV.containsKey(scoreKey) ? scoreKey : MSCourseNode.CONFIG_VALUE_SCORE_NONE;
		scoreEl.setKeysAndValues(scoreKV.keys(), scoreKV.values(), null);
		scoreEl.select(scoreKey, true);

		// min / max
		boolean minMaxVisible = !MSCourseNode.CONFIG_VALUE_SCORE_NONE.equals(scoreKey);
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
		boolean scoreEnabled = !MSCourseNode.CONFIG_VALUE_SCORE_NONE.equals(scoreKey);

		// scaling factor
		boolean scaleVisible = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)
				|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey);
		scaleEl.setVisible(scaleVisible);
		
		if (gradeEnabledEl != null) {
			gradeSpacer.setVisible(scoreEnabled);
			gradeEnabledEl.setVisible(scoreEnabled);
			gradeAutoEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
			String gradeScaleText = gradeScale == null
					? translate("node.grade.scale.not.available")
					: translate("node.grade.scale.available");
			gradeScaleEl.setValue(gradeScaleText);
			gradeScaleEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
			gradeScaleButtonsCont.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
		}
		
		boolean gradeDisable = gradeEnabledEl == null || !gradeEnabledEl.isVisible() || !gradeEnabledEl.isAtLeastSelected(1);

		// passed
		boolean passedTypeVisible = scoreEnabled && gradeDisable && passedEl.isAtLeastSelected(1);
		passedTypeEl.setVisible(passedTypeVisible);

		// cut value
		boolean cutVisible = passedTypeVisible && passedTypeEl.isOneSelected() && passedTypeEl.getSelected() == 0;
		cutEl.setVisible(cutVisible);
		
		// ignore in course assessment
		boolean hasScore = scoreEl.isOneSelected() && !scoreEl.getSelectedKey().equals(MSCourseNode.CONFIG_VALUE_SCORE_NONE);
		boolean ignoreInScoreVisible = ignoreInCourseAssessmentAvailable
				&& (hasScore || passedEl.isAtLeastSelected(1));
		ignoreInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		ignoreInCourseAssessmentSpacer.setVisible(ignoreInScoreVisible);
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
		} else if (source == scoreEl) {
			doCalculateMinMax();
			updateUI();
		} else if (source == scaleEl) {
			doCalculateMinMax();
			updateUI();
		} else if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		} else if (source == gradeEnabledEl) {
			updateUI();
		} else if (source == passedEl) {
			updateUI();
		} else if (source == passedTypeEl) {
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
				flc.setDirty(true);
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
			evaluationFormNotChoosen.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		minEl.clearError();
		maxEl.clearError();
		boolean minIsFloat = isFloat(minEl.getValue());
		boolean maxIsFloat = isFloat(maxEl.getValue());
		if (minEl.isVisible() && minEl.isEnabled()) {
			if (!minIsFloat) {
				minEl.setErrorKey("form.error.wrongFloat", null);
				allOk = false;
			}
			if (!maxIsFloat) {
				maxEl.setErrorKey("form.error.wrongFloat", null);
				allOk = false;
			}
			if (minIsFloat && maxIsFloat && isNotGreaterFloat(minEl.getValue(), maxEl.getValue())) {
				maxEl.setErrorKey("form.error.minGreaterThanMax", null);
				allOk = false;
			}
		}
		
		passedTypeEl.clearError();
		if (passedTypeEl.isVisible()) {
			if (!passedTypeEl.isOneSelected()) {
				passedTypeEl.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}
		}
		
		cutEl.clearError();
		if (cutEl.isVisible()) {
			boolean cutIsFloat = isFloat(cutEl.getValue());
			if (!cutIsFloat) {
				cutEl.setErrorKey("form.error.wrongFloat", null);
				allOk = false;
			}
			if (cutIsFloat && minIsFloat && maxIsFloat
					&& notInRange(minEl.getValue(), maxEl.getValue(), cutEl.getValue())) {
				cutEl.setErrorKey("form.error.cutOutOfRange", null);
				allOk = false;
			}
		}
		
		scaleEl.clearError();
		if (scaleEl.isVisible()) {
			boolean scaleIsFloat = isFloat(scaleEl.getValue());
			if (!scaleIsFloat) {
				scaleEl.setErrorKey("form.error.wrongFloat", null);
				allOk = false;
			}
		}
		
		infotextCoachEl.clearError();
		if (infotextCoachEl.getValue().length() > 4000) {
			infotextCoachEl.setErrorKey("input.toolong", new String[] {"4000"});
			allOk = false;
		}
		
		infotextUserEl.clearError();
		if (infotextUserEl.getValue().length() > 4000) {
			infotextUserEl.setErrorKey("input.toolong", new String[] {"4000"});
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
			String initialStatus = initialStatusEl.isAtLeastSelected(1)
					? AssessmentEntryStatus.inReview.name()
					: AssessmentEntryStatus.notStarted.name();
			config.setStringValue(MSCourseNode.CONFIG_KEY_INITIAL_STATUS, initialStatus);
		}
		
		boolean evalFormEnabled = evaluationFormEnabledEl.isAtLeastSelected(1);
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED, evalFormEnabled);
		if (evalFormEnabled) {
			MSCourseNode.setEvaluationFormReference(formEntry, config);
		} else {
			MSCourseNode.removeEvaluationFormReference(config);
		}
		
		config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, scoreEl.getSelectedKey());
		
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
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isAtLeastSelected(1));
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.valueOf(gradeAutoEl.getSelectedKey()).booleanValue());
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			config.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		}
		
		boolean showPassed = passedEl.isAtLeastSelected(1);
		config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.valueOf(showPassed));
		
		if (showPassed) {
			// do cut value
			Boolean cutAutomatically = Boolean.valueOf(passedTypeEl.getSelectedKey());
			if (cutAutomatically.booleanValue() && cutEl.isVisible()) {
				config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, Float.valueOf(cutEl.getValue()));
			} else {
				config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			}
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		}
		
		boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isVisible() && ignoreInCourseAssessmentEl.isAtLeastSelected(1);
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		
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
	}

	private void doCalculateMinMax() {
		if (formEntry == null) {
			formMinMax = MinMax.of(Float.valueOf(0), Float.valueOf(0));
			return;
		}
		
		String scoreKey = scoreEl != null && scoreEl.isOneSelected()
				? scoreEl.getSelectedKey()
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
		
		String gradeSystemKey = config.getStringValue(MSCourseNode.CONFIG_KEY_GRADE_SYSTEM);
		Long defautGradesystemKey = StringHelper.isLong(gradeSystemKey)? Long.valueOf(gradeSystemKey): null;

		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), ores, nodeIdent,
				minScore, maxScore, defautGradesystemKey);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
