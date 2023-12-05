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
package org.olat.course.nodes.cl.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
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
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.wizard.GeneratorData;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListConfigurationController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] outputKeys = new String[]{ "cutvalue", "sum", "coach"};
	
	private FormToggle passedEl;
	private FormToggle dueDateEl;
	private FormToggle scoreGrantedEl;
	private MultipleSelectionElement commentEl;
	private MultipleSelectionElement assessmentDocsEl;
	private SingleSelection outputEl, numOfCheckListEl, sumCheckboxEl;
	private TextElement minPointsEl, maxPointsEl, cutValueEl, titlePrefixEl;
	private RichTextElement tipUserEl;
	private RichTextElement tipCoachEl;
	private DateChooser dueDateChooserEl;
	private SpacerElement passedSpacer;
	private FormToggle incorporateInCourseAssessmentEl;
	private TextElement scoreScalingEl;
	private SpacerElement incorporateInCourseAssessmentSpacer;
	private SpacerElement gradeSpacer;
	private FormToggle gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private StaticTextElement gradePassedEl;
	private FormLink showTipsLink;
	
	private CloseableModalController cmc;
	private GradeScaleEditController gradeScaleCtrl;
	
	private boolean showTips = false;
	private RepositoryEntry courseEntry;
	private CourseNode courseNode;
	private final ModuleConfiguration config;
	private final boolean inUse;
	private final boolean wizard;
	private final boolean scoreScalingEnabled;
	private final boolean ignoreInCourseAssessmentAvailable;
	private GeneratorData data;
	private GradeScale gradeScale;
	
	private static final String[] numOfKeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
	
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	
	public CheckListConfigurationController(UserRequest ureq, WindowControl wControl, ICourse course, CourseNode courseNode,
			NodeAccessType nodeAccessType, boolean inUse) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.courseNode = courseNode;
		wizard = false;
		this.inUse = inUse;
		config = courseNode.getModuleConfiguration();
		ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
		initForm(ureq);
	}
	
	public CheckListConfigurationController(UserRequest ureq, WindowControl wControl, ICourse course, ModuleConfiguration config,
			NodeAccessType nodeAccessType, GeneratorData data, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		wizard = true;
		inUse = false;
		this.data = data;
		this.config = config;
		ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_cl_edit_assessment");
		if(wizard) {
			FormLayoutContainer wizardCont = uifactory.addDefaultFormLayout("config.wizard", null, formLayout);
			initWizardForm(wizardCont);
		}

		FormLayoutContainer dueDatesCont = uifactory.addDefaultFormLayout("config.due.dates", null, formLayout);
		if(!wizard) {
			dueDatesCont.setFormInfo(translate("config.description"));
			dueDatesCont.setFormContextHelp("manual_user/learningresources/Course_Element_Checklist/");
			if(inUse) {
				dueDatesCont.setFormWarning("config.warning.inuse");
			}
		}
		initDueDatesForm(dueDatesCont);

		FormLayoutContainer gradingCont = uifactory.addDefaultFormLayout("config.grading", null, formLayout);
		initGradingForm(gradingCont);
	}

	private void initWizardForm(FormItemContainer formLayout) {
		titlePrefixEl = uifactory.addTextElement("titelprefix", "title.prefix", 64, "", formLayout);
		titlePrefixEl.setMandatory(true);
		
		numOfCheckListEl = uifactory.addDropdownSingleselect("num.of.checklist", "num.of.checklist", formLayout, numOfKeys, numOfKeys, null);
		numOfCheckListEl.select(numOfKeys[0], true);
		numOfCheckListEl.setMandatory(true);
	}

	/**
	 * Due date
	 * 
	 * @param formLayout The layout container
	 */
	private void initDueDatesForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("config.title"));
		
		Boolean dueDateBool = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
		Date dueDate = (Date)config.get(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		dueDateEl = uifactory.addToggleButton("duedate", "config.due.date.on", translate("on"), translate("off"), formLayout);
		dueDateEl.addActionListener(FormEvent.ONCHANGE);
		if(dueDateBool != null && dueDateBool.booleanValue()) {
			dueDateEl.toggle(true);
		}
		dueDateChooserEl = uifactory.addDateChooser("config.due.date", dueDate, formLayout);
		dueDateChooserEl.setDateChooserTimeEnabled(true);
		dueDateChooserEl.setMandatory(true);
		if(dueDateBool != null && dueDateBool.booleanValue()) {
			dueDateEl.toggle(true);
			dueDateChooserEl.setVisible(true);
		} else {
			dueDateChooserEl.setVisible(false);
		}
	}

	private void initGradingForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("grading.configuration.title"));
		String[] theValues = new String[] { "" };
		//points
		Boolean scoreGrantedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		Float minVal = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxVal = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		
		scoreGrantedEl = uifactory.addToggleButton("points", "config.points", translate("on"), translate("off"), formLayout);
		scoreGrantedEl.addActionListener(FormEvent.ONCHANGE);
		if(scoreGrantedBool == null || (scoreGrantedBool != null && scoreGrantedBool.booleanValue())) {
			scoreGrantedEl.toggle(true);
		}
		String minValStr = minVal == null ? "" : Float.toString(minVal.floatValue());
		minPointsEl = uifactory.addTextElement("pointsmin", "config.points.min", 4, minValStr, formLayout);
		minPointsEl.setElementCssClass("o_sel_cl_min_score");
		minPointsEl.setMandatory(true);
		minPointsEl.setDisplaySize(5);
		String maxValStr = maxVal == null ? "" : Float.toString(maxVal.floatValue());
		maxPointsEl = uifactory.addTextElement("pointsmax", "config.points.max", 4, maxValStr, formLayout);
		maxPointsEl.setElementCssClass("o_sel_cl_max_score");
		maxPointsEl.setMandatory(true);
		maxPointsEl.setDisplaySize(5);
		
		if (gradeModule.isEnabled() && !wizard) {
			gradeSpacer = uifactory.addSpacerElement("spacer0", formLayout, false);
			
			gradeEnabledEl = uifactory.addToggleButton("node.grade.enabled", "node.grade.enabled", translate("on"), translate("off"), formLayout);
			gradeEnabledEl.addActionListener(FormEvent.ONCHANGE);
			boolean gradeEnabled = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeEnabledEl.toggle(gradeEnabled);
			
			SelectionValues autoSV = new SelectionValues();
			autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
			autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
			gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
			gradeAutoEl.select(Boolean.valueOf(config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)).toString(), true);
			
			gradeScale = courseNode != null? gradeService.getGradeScale(courseEntry, courseNode.getIdent()): null;
			gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
			
			gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
			gradeScaleButtonsCont.setRootForm(mainForm);
			formLayout.add(gradeScaleButtonsCont);
			gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
			
			gradePassedEl = uifactory.addStaticTextElement("node.grade.passed", "config.passed", "", formLayout);
		}
		
		passedSpacer = uifactory.addSpacerElement("spacer-points", formLayout, false);
		
		//passed
		Float cutVal = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		Boolean passedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		Boolean passedSum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
		Integer sumCutValue = (Integer)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
		Boolean manualCorr = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
		
		passedEl = uifactory.addToggleButton("passed", "config.passed", translate("on"), translate("off"), formLayout);
		passedEl.addActionListener(FormEvent.ONCHANGE);
		if(passedBool == null || (passedBool != null && passedBool.booleanValue())) {
			passedEl.toggle(true);
		}
		String[] outputValues = new String[]{
			translate("config.output.cutvalue"), translate("config.output.sum"), translate("config.output.coach")
		};
		outputEl = uifactory.addRadiosVertical("output", "config.output", formLayout, outputKeys, outputValues);
		outputEl.addActionListener(FormEvent.ONCHANGE);
		
		String cutValStr = cutVal == null ? "" : Float.toString(cutVal.floatValue());
		cutValueEl = uifactory.addTextElement("cutvalue", "config.cutvalue", 4, cutValStr, formLayout);
		cutValueEl.setElementCssClass("o_sel_cl_cut_value");
		cutValueEl.setDisplaySize(5);
		cutValueEl.setMandatory(true);
		
		String[] numKeys = getAvailableSumCutValues();
		sumCheckboxEl = uifactory.addDropdownSingleselect("sum.cutvalue", "sum.cutvalue", formLayout, numKeys, numKeys, null);
		if(sumCutValue == null || sumCutValue.intValue() <= 0) {
			sumCheckboxEl.select(numKeys[0], true);
		} else if(sumCutValue.intValue() > 0 && sumCutValue.intValue() < numKeys.length) {
			sumCheckboxEl.select(numKeys[sumCutValue.intValue()], true);
		} else {
			sumCheckboxEl.select(numKeys[numKeys.length - 1], true);
		}
		if(passedSum != null && passedSum.booleanValue()) {
			outputEl.select(outputKeys[1], true);
		}
		if(manualCorr != null && manualCorr.booleanValue()) {
			outputEl.select(outputKeys[2], true);
		}
		if((cutVal != null && cutVal.floatValue() > -0.1) || !outputEl.isOneSelected()) {
			outputEl.select(outputKeys[0], true);
		}
		
		uifactory.addSpacerElement("spacer-passed", formLayout, false);
		
		// course assesment
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		incorporateInCourseAssessmentEl = uifactory.addToggleButton("incorporate.in.course.assessment", "incorporate.in.course.assessment",
				translate("on"), translate("off"), formLayout);
		incorporateInCourseAssessmentEl.toggle(!ignoreInCourseAssessment);
		
		String scaling = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		scoreScalingEl = uifactory.addTextElement("score.scaling", "score.scaling", 10, scaling, formLayout);
		scoreScalingEl.setExampleKey("score.scaling.example", null);
		
		incorporateInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer3", formLayout, false);

		//comment
		commentEl = uifactory.addCheckboxesHorizontal("comment", "config.comment", formLayout, onKeys, theValues);
		Boolean commentBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		if(commentBool != null && commentBool.booleanValue()) {
			commentEl.select(onKeys[0], true);
		}
		
		assessmentDocsEl = uifactory.addCheckboxesHorizontal("form.individual.assessment.docs", formLayout, onKeys, new String[]{null});
		boolean docsCf = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
		if(docsCf) {
			assessmentDocsEl.select(onKeys[0], true);
		}
		
		showTipsLink = uifactory.addFormLink("show.tips", "show.tips", null, formLayout, Link.LINK);
		showTipsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_open_togglebox");
		
		String iu = (String)config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		tipUserEl = uifactory.addRichTextElementForStringDataMinimalistic("tip.user", "config.tip.user", iu, 5, -1, formLayout,
				getWindowControl());
		tipUserEl.getEditorConfiguration().enableMathEditor();
		tipUserEl.setVisible(false);
		
		String ic = (String)config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		tipCoachEl = uifactory.addRichTextElementForStringDataMinimalistic("tip.coach", "config.tip.coach", ic, 5, -1, formLayout,
				getWindowControl());
		tipCoachEl.getEditorConfiguration().enableMathEditor();
		tipCoachEl.setVisible(false);
		
		if(!wizard) {
			FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsLayout);
			uifactory.addFormSubmitButton("submit", "submit", buttonsLayout);
		}
		
		updateScoreVisibility();
		updatePassedAndOutputVisibilty();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(wizard) {
			String prefix = titlePrefixEl.getValue();
			data.setNodePrefix(prefix);
			int numOfChecklist = numOfCheckListEl.getSelected();
			data.setNumOfNodes(numOfChecklist + 1);
		}

		//due date
		boolean closeAfterDueDate = dueDateEl.isOn();
		config.set(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE, Boolean.valueOf(closeAfterDueDate));
		Date dueDate = dueDateChooserEl.getDate();
		if(dueDate != null) {
			config.set(CheckListCourseNode.CONFIG_KEY_DUE_DATE, dueDate);
		} else {
			config.remove(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		}
		//score
		Boolean sf = Boolean.valueOf(scoreGrantedEl.isOn());
		config.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, sf);
		if (sf.booleanValue()) {
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, Float.valueOf(minPointsEl.getValue()));
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, Float.valueOf(maxPointsEl.getValue()));
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_MIN);
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		}
		
		if (gradeEnabledEl != null) {
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isOn());
			config.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.valueOf(gradeAutoEl.getSelectedKey()).booleanValue());
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			config.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		}
		
		// mandatory passed flag
		Boolean pf = Boolean.valueOf(passedEl.isOn());
		config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, pf);
		config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		config.remove(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
		config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX, Boolean.FALSE);
		config.set(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION, Boolean.FALSE);
		if (pf.booleanValue() && outputEl.isVisible()) {
			String output = outputEl.getSelectedKey();
			if("cutvalue".equals(output)) {
				config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, Float.valueOf(cutValueEl.getValue()));
			} else if("sum".equals(output)) {
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX, Boolean.TRUE);
				int sumCutValue = Integer.parseInt(sumCheckboxEl.getSelectedKey());
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE, Integer.valueOf(sumCutValue));
			} else if("coach".equals(output)) {
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION, Boolean.TRUE);
			}
		}
		
		// course assessment
		boolean ignoreInCourseAssessment = incorporateInCourseAssessmentEl.isVisible() && !incorporateInCourseAssessmentEl.isOn();
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		if(ignoreInCourseAssessment || !scoreScalingEnabled) {
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_SCALING);
		} else {
			config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, scoreScalingEl.getValue());
		}

		// mandatory comment flag
		config.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.valueOf(commentEl.isSelected(0)));
		// individual assessment docs
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, assessmentDocsEl.isSelected(0));

		// set info text only if something is in there
		String iu = tipUserEl.getValue();
		if (StringHelper.containsNonWhitespace(iu)) {
			config.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, iu);
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		}
		String ic = tipCoachEl.getValue();
		if (StringHelper.containsNonWhitespace(ic)) {
			config.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, ic);
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//wizardery need title prefix and number of checklist to be defined
		if(wizard) {
			titlePrefixEl.clearError();
			if(!StringHelper.containsNonWhitespace(titlePrefixEl.getValue())) {
				titlePrefixEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
			
			numOfCheckListEl.clearError();
			if(!numOfCheckListEl.isOneSelected()) {
				numOfCheckListEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		// score flag
		minPointsEl.clearError();
		maxPointsEl.clearError();
		if (scoreGrantedEl.isOn()) {
			float min = toFloat(minPointsEl.getValue());
			float max = toFloat(maxPointsEl.getValue());
			if (Float.isNaN(min) || min < 0.0f) {
				minPointsEl.setErrorKey("form.error.wrongFloat");
				allOk &= false;
			}
			if (Float.isNaN(max) || max < 0.0f) {
				maxPointsEl.setErrorKey("form.error.wrongFloat");
				allOk &= false;
			}
			if (!Float.isNaN(min) && !Float.isNaN(max) && min > max) {
				maxPointsEl.setErrorKey("form.error.minGreaterThanMax");
				allOk &= false;
			}
		}
		
		cutValueEl.clearError();
		outputEl.clearError();
		if(passedEl.isOn()) {
			if(outputEl.isVisible() && outputEl.isOneSelected()) {
				String selectKey = outputEl.getSelectedKey();
				if("cutvalue".equals(selectKey)) {
					float cut = toFloat(cutValueEl.getValue());
					if (Float.isNaN(cut) || cut < 0.0f) {
						cutValueEl.setErrorKey("form.error.wrongFloat");
						allOk &= false;
					}
				}
			}
		}
		
		dueDateChooserEl.clearError();
		if(dueDateEl.isOn()) {
			if (!wizard && dueDateChooserEl.isEmpty()) {
				dueDateChooserEl.setErrorKey("form.error.date");
				allOk &= false;
			}
		}

		allOk &= ScoreScalingHelper.validateScoreScaling(scoreScalingEl);
		
		return allOk;
	}
	
	private float toFloat(String val) {
		float ret;
		try {
			ret = Float.parseFloat(val);
		} catch (NumberFormatException e) {
			ret = Float.NaN;
		}
		return ret;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc != gradeScaleEditLink) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(dueDateEl == source) {
			if(!wizard) {
				boolean selected = dueDateEl.isOn();
				dueDateChooserEl.setVisible(selected);
			}
		} else if(scoreGrantedEl == source) {
			updateScoreVisibility();
		} else if(passedEl == source) {
			updatePassedAndOutputVisibilty();
		} else if(outputEl == source) {
			updatePassedAndOutputVisibilty();
		} else if (source == gradeEnabledEl) {
			updateGradeVisibility();
			updatePassedAndOutputVisibilty();
		} else if(incorporateInCourseAssessmentEl == source) {
			updateIncorporateInCourseAssessmentVisibility();
		} else if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		} else if (source == showTipsLink) {
			showTips = true;
			updateTips();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof CheckListBoxListEditController) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				//update the number of box available
				String[] numKeys = getAvailableSumCutValues();
				int currentSelection = sumCheckboxEl.getSelected();
				sumCheckboxEl.setKeysAndValues(numKeys, numKeys, null);
				if(currentSelection >= numKeys.length) {
					//if the number is smaller, update the selection to fit in the new range
					sumCheckboxEl.select(numKeys[numKeys.length - 1], true);
				} else {
					sumCheckboxEl.select(numKeys[currentSelection], true);
				}
			}
		} else if (gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
				updateGradeVisibility();
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
		removeAsListenerAndDispose(cmc);
		gradeScaleCtrl = null;
		cmc = null;
	}
	
	private String[] getAvailableSumCutValues() {
		int currentNumOfCheckbox;
		if(data == null) {
			CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
			currentNumOfCheckbox = list == null ? 0 : list.getNumOfCheckbox();
		} else {
			currentNumOfCheckbox = data.getNumOfCheckbox();
		}
		String[] numKeys = new String[currentNumOfCheckbox + 1];
		for(int i=0; i<=currentNumOfCheckbox; i++) {
			numKeys[i] = Integer.toString(i);
		}
		return numKeys;
	}

	private void updateScoreVisibility() {
		boolean granted = scoreGrantedEl.isOn();
		minPointsEl.setVisible(granted);
		minPointsEl.setMandatory(granted);
		maxPointsEl.setVisible(granted);
		maxPointsEl.setMandatory(granted);
		updateGradeVisibility();
		updateIncorporateInCourseAssessmentVisibility();
	}
	
	private void updateGradeVisibility() {
		if (gradeEnabledEl != null) {
			gradeSpacer.setVisible(scoreGrantedEl.isOn());
			gradeEnabledEl.setVisible(scoreGrantedEl.isOn());
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
	}

	private void updatePassedAndOutputVisibilty() {
		boolean gradeDisable = gradeEnabledEl == null || !gradeEnabledEl.isVisible() || !gradeEnabledEl.isOn();
		passedSpacer.setVisible(gradeDisable);
		passedEl.setVisible(gradeDisable);
		if(passedEl.isOn() && gradeDisable) {
			outputEl.setVisible(true);
			String selectKey = outputEl.getSelectedKey();
			if("cutvalue".equals(selectKey)) {
				cutValueEl.setVisible(true);
				sumCheckboxEl.setVisible(false);
			} else if("sum".equals(selectKey)) {
				cutValueEl.setVisible(false);
				sumCheckboxEl.setVisible(true);
			} else if("coach".equals(selectKey)) {
				cutValueEl.setVisible(false);
				sumCheckboxEl.setVisible(false);
			}
		} else {
			outputEl.setVisible(false);
			cutValueEl.setVisible(false);
			sumCheckboxEl.setVisible(false);
		}
		updateIncorporateInCourseAssessmentVisibility();
	}
	
	private void updateIncorporateInCourseAssessmentVisibility() {
		boolean ignoreInScoreVisible = ignoreInCourseAssessmentAvailable
				&& (scoreGrantedEl.isOn() || passedEl.isOn());
		incorporateInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		incorporateInCourseAssessmentSpacer.setVisible(ignoreInScoreVisible);
		
		scoreScalingEl.setVisible(incorporateInCourseAssessmentEl.isVisible()
				&& incorporateInCourseAssessmentEl.isOn() && scoreScalingEnabled);
	}
	
	private void updateTips() {
		showTipsLink.setVisible(!showTips);
		tipUserEl.setVisible(showTips);
		tipCoachEl.setVisible(showTips);
	}
	
	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		Float minScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxScore = config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if ((minScore == null || minScore.intValue() == 0) && (maxScore == null || maxScore.intValue() == 0)) {
			showWarning("error.score.min.max.not.set");
			return;
		}
		
		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), courseEntry, courseNode.getIdent(),
				minScore, maxScore, false, true);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
}