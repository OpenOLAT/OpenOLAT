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
package org.olat.course.nodes.videotask.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentEditController extends FormBasicController {
	
	public static final String ASSESSMENT_AUTO = "auto";
	public static final String ASSESSMENT_MANUAL = "manual";
	
	private FormToggle scoreEl;
	private TextElement minEl;
	private TextElement maxEl;
	private SingleSelection roundingEl;
	private SingleSelection ignoreInCourseAssessmentEl;
	private SpacerElement scoreSpacer;
	private FormToggle gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private SpacerElement gradingSpacer;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private StaticTextElement gradePassedEl;
	private SpacerElement passedSpacer;
	private FormToggle passedEl;
	private SingleSelection passedTypeEl;
	private TextElement cutEl;
	private SingleSelection weightingEl;

	private GradeScale gradeScale;
	private final String nodeIdent;
	private final RepositoryEntry ores;
	private final ModuleConfiguration config;
	private final boolean ignoreInCourseAssessmentAvailable;

	private CloseableModalController cmc;
	private GradeScaleEditController gradeScaleCtrl;
	
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public VideoTaskAssessmentEditController(UserRequest ureq, WindowControl wControl,
			ICourse course, VideoTaskCourseNode courseNode) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		config = courseNode.getModuleConfiguration();
		this.ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		this.nodeIdent = courseNode.getIdent();
		ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(NodeAccessType.of(course));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		scoreEl = uifactory.addToggleButton("form.score", translate("form.score"), "&nbsp;&nbsp;", formLayout, null, null);
		scoreEl.addActionListener(FormEvent.ONCHANGE);
		String scoreEnabled = config.getStringValue(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		if("true".equals(scoreEnabled)) {
			scoreEl.toggleOn();
		} else {
			scoreEl.toggleOff();
		}
		
		initFormScore(formLayout);
		scoreSpacer = uifactory.addSpacerElement("score-spacer", formLayout, false);

		if (gradeModule.isEnabled()) {
			initFormGrading(formLayout);
			gradingSpacer = uifactory.addSpacerElement("grading-spacer", formLayout, false);
		}
		
		initFormPassed(formLayout);
		passedSpacer = uifactory.addSpacerElement("passed-spacer", formLayout, false);
		initWeighting(formLayout);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void initFormScore(FormItemContainer formLayout) {
		// Minimum
		Float min = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		min = min != null? min: MSCourseNode.CONFIG_DEFAULT_SCORE_MIN;
		minEl = uifactory.addTextElement("form.min", "form.min", 8, min.toString(), formLayout);
		minEl.setElementCssClass("o_sel_course_video_min");
		
		// Maximum
		Float max = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		max = max != null? max: MSCourseNode.CONFIG_DEFAULT_SCORE_MAX;
		maxEl = uifactory.addTextElement("form.max", "form.max", 8, max.toString(), formLayout);
		maxEl.setElementCssClass("o_sel_course_video_max");
		
		int rounding = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING, 2);
		String roundingStr = Integer.toString(rounding);
		SelectionValues roundingValues = new SelectionValues();
		roundingValues.add(SelectionValues.entry("0", "0"));
		roundingValues.add(SelectionValues.entry("1", "1"));
		roundingValues.add(SelectionValues.entry("2", "2"));
		roundingValues.add(SelectionValues.entry("3", "3"));
		roundingEl = uifactory.addDropdownSingleselect("form.rounding", formLayout,
				roundingValues.keys(), roundingValues.values());
		roundingEl.setElementCssClass("o_sel_course_video_rounding");
		if(roundingValues.containsKey(roundingStr)) {
			roundingEl.select(roundingStr, true);
		} else {
			roundingEl.select("2", true);
		}

		// Negative form: label is ignore course assessment
		SelectionValues assessmentValues = new SelectionValues();
		assessmentValues.add(SelectionValues.entry("false", translate("yes")));
		assessmentValues.add(SelectionValues.entry("true", translate("no")));
		ignoreInCourseAssessmentEl = uifactory.addRadiosHorizontal("form.ignore.course.assessment", formLayout,
				assessmentValues.keys(), assessmentValues.values());
		String courseAssessment = Boolean.toString(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, true));
		if(assessmentValues.containsKey(courseAssessment)) {
			ignoreInCourseAssessmentEl.select(courseAssessment, true);
		} else {
			ignoreInCourseAssessmentEl.select("false", true);
		}
	}
	
	private void initFormGrading(FormItemContainer formLayout) {
		gradeEnabledEl = uifactory.addToggleButton("node.grade.enabled", translate("node.grade.enabled"), "&nbsp;&nbsp;", formLayout, null, null);
		gradeEnabledEl.setElementCssClass("o_sel_course_video_grade");
		scoreEl.addActionListener(FormEvent.ONCHANGE);
		String scoreEnabled = config.getStringValue(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
		if("true".equals(scoreEnabled)) {
			gradeEnabledEl.toggleOn();
		} else {
			gradeEnabledEl.toggleOff();
		}
		
		
		SelectionValues autoSV = new SelectionValues();
		autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
		autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
		gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
		gradeAutoEl.setElementCssClass("o_sel_course_ms_grade_mode");
		gradeAutoEl.select(Boolean.toString(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)), true);
		
		gradeScale = gradeService.getGradeScale(ores, nodeIdent);
		gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
		
		gradeScaleButtonsCont = uifactory.addButtonsFormLayout("gradeButtons", null, formLayout);
		gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
		gradeScaleEditLink.setElementCssClass("o_sel_grade_edit_scale");
		
		gradePassedEl = uifactory.addStaticTextElement("node.grade.passed", "form.passed", "", formLayout);
	}
	
	private void initFormPassed(FormItemContainer formLayout) {
		passedEl = uifactory.addToggleButton("form.passed", translate("form.passed"), "&nbsp;&nbsp;", formLayout, null, null);
		passedEl.addActionListener(FormEvent.ONCHANGE);
		boolean passedField = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, false);
		if(passedField) {
			passedEl.toggleOn();
		} else {
			passedEl.toggleOff();
		}
		
		SelectionValues passedModeValues = new SelectionValues();
		passedModeValues.add(SelectionValues.entry(ASSESSMENT_AUTO,
				translate("form.passed.auto"), translate("form.passed.auto.desc"), null, null, true));
		passedModeValues.add(SelectionValues.entry(ASSESSMENT_MANUAL,
				translate("form.passed.manual"), translate("form.passed.manual.desc"), null, null, true));
		
		passedTypeEl = uifactory.addCardSingleSelectHorizontal("form.passed.type", "form.passed.type", formLayout, passedModeValues);
		passedTypeEl.addActionListener(FormEvent.ONCLICK);
		passedTypeEl.setElementCssClass("o_sel_course_video_display_type");

		Float cut = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		if (cut != null) {
			passedTypeEl.select(ASSESSMENT_AUTO, true);
		} else {
			passedTypeEl.select(ASSESSMENT_MANUAL, true);
			cut = Float.valueOf(0.0f);
		}
		
		// Passing grade cut value
		cutEl = uifactory.addTextElement("form.cut", "form.cut", 8, cut.toString(), formLayout);
		cutEl.setElementCssClass("o_sel_course_ms_cut");
	}
	
	private void initWeighting(FormItemContainer formLayout) {
		SelectionValues weightValues = new SelectionValues();
		weightValues.add(SelectionValues.entry("1", translate("form.weight.wrong.1")));
		weightValues.add(SelectionValues.entry("0.5", translate("form.weight.wrong.05")));
		weightValues.add(SelectionValues.entry("0.25", translate("form.weight.wrong.025")));
		
		weightingEl = uifactory.addDropdownSingleselect("form.weight.wrong", formLayout,
				weightValues.keys(), weightValues.values());
		weightingEl.setHelpText(translate("form.weight.wrong.hint"));
		
		String weight = config.getStringValue(VideoTaskEditController.CONFIG_KEY_WEIGHT_WRONG_ANSWERS);
		if(weightValues.containsKey(weight)) {
			weightingEl.select(weight, true);
		} else {
			weightingEl.select("0.25", true);
		}
	}
	
	private void updateUI() {
		boolean scoreEnabled = scoreEl.isOn();
		minEl.setVisible(scoreEnabled);
		maxEl.setVisible(scoreEnabled);
		roundingEl.setVisible(scoreEnabled);
		scoreSpacer.setVisible(scoreEnabled);
		if(gradeEnabledEl != null) {
			gradingSpacer.setVisible(scoreEnabled);
			gradeEnabledEl.setVisible(scoreEnabled);
			
			boolean gradeEnabled = gradeEnabledEl.isOn();
			
			gradeAutoEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabled);
			String gradeScaleText = gradeScale == null
					? translate("node.grade.scale.not.available")
					: GradeUIFactory.translateGradeSystemName(getTranslator(), gradeScale.getGradeSystem());
			gradeScaleEl.setValue(gradeScaleText);
			gradeScaleEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabled);
			gradeScaleButtonsCont.setVisible(gradeEnabledEl.isVisible() && gradeEnabled);
			
			GradeScoreRange minRange = gradeService.getMinPassedGradeScoreRange(gradeScale, getLocale());
			gradePassedEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabled && minRange != null);
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
		ignoreInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		
		weightingEl.setVisible(scoreEnabled);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(ores, nodeIdent);
				updateUI();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(gradeScaleCtrl);
		removeAsListenerAndDispose(cmc);
		gradeScaleCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(scoreEl == source || passedEl == source || passedTypeEl == source || gradeEnabledEl == source) {
			updateUI();
		} else if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateConfig();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void updateConfig() {
		boolean scoreEnabled = scoreEl.isOn();
		config.setStringValue(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.toString(scoreEnabled));
		if(scoreEnabled) {
			Float min = Float.valueOf(minEl.getValue());
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, min);
			Float max = Float.valueOf(maxEl.getValue());
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, max);
			int rounding = Integer.parseInt(roundingEl.getSelectedKey());
			config.setIntValue(VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING, rounding);
			boolean courseAssessment = Boolean.parseBoolean(ignoreInCourseAssessmentEl.getSelectedKey());
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, courseAssessment);
			
			// Grade
			if (gradeEnabledEl != null) {
				config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isOn());
				config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.parseBoolean(gradeAutoEl.getSelectedKey()));
			} else {
				config.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
				config.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
			}
			
			boolean showPassed = passedEl.isOn();
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, showPassed);
			if (showPassed) {
				// do cut value
				boolean cutAutomatically = ASSESSMENT_AUTO.equals(passedTypeEl.getSelectedKey());
				if (cutAutomatically && cutEl.isVisible()) {
					config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, Float.valueOf(cutEl.getValue()));
				} else {
					config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
				}
			} else {
				config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			}
			
			String weight = weightingEl.getSelectedKey();
			config.setStringValue(VideoTaskEditController.CONFIG_KEY_WEIGHT_WRONG_ANSWERS, weight);
		} else {
			resetConfiguration();
		}
	}
	
	protected void resetConfiguration() {
		config.remove(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		config.remove(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		config.remove(VideoTaskEditController.CONFIG_KEY_SCORE_ROUNDING);
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, true);
		config.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
		config.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, false);
		config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		config.remove(VideoTaskEditController.CONFIG_KEY_WEIGHT_WRONG_ANSWERS);
	}
	
	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		Float minScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if ((minScore == null || minScore.intValue() == 0) && (maxScore == null || maxScore.intValue() == 0)) {
			showWarning("error.score.min.max.not.set");
			return;
		}
		
		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), ores, nodeIdent, minScore, maxScore, true);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	

}
