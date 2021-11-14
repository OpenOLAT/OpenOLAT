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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.wizard.GeneratorData;
import org.olat.modules.ModuleConfiguration;
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
	
	private MultipleSelectionElement dueDateEl, scoreGrantedEl, passedEl, commentEl, assessmentDocsEl;
	private SingleSelection outputEl, numOfCheckListEl, sumCheckboxEl;
	private TextElement minPointsEl, maxPointsEl, cutValueEl, titlePrefixEl;
	private RichTextElement tipUserEl, tipCoachEl;
	private DateChooser dueDateChooserEl;
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private SpacerElement ignoreInCourseAssessmentSpacer;
	
	private final ModuleConfiguration config;
	private final boolean inUse;
	private final boolean wizard;
	private final boolean ignoreInCourseAssessmentAvailable;
	private GeneratorData data;
	
	private static final String[] numOfKeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
	
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public CheckListConfigurationController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			NodeAccessType nodeAccessType, boolean inUse) {
		super(ureq, wControl);
		wizard = false;
		this.inUse = inUse;
		config = courseNode.getModuleConfiguration();
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		initForm(ureq);
	}
	
	public CheckListConfigurationController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config,
			NodeAccessType nodeAccessType, GeneratorData data, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		wizard = true;
		inUse = false;
		this.data = data;
		this.config = config;
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_cl_edit_assessment");
		if(wizard) {
			titlePrefixEl = uifactory.addTextElement("titelprefix", "title.prefix", 64, "", formLayout);
			titlePrefixEl.setMandatory(true);
			
			numOfCheckListEl = uifactory.addDropdownSingleselect("num.of.checklist", "num.of.checklist", formLayout, numOfKeys, numOfKeys, null);
			numOfCheckListEl.select(numOfKeys[0], true);
			numOfCheckListEl.setMandatory(true);

			uifactory.addSpacerElement("spacer-wiz", formLayout, false);
		} else {
			setFormTitle("config.title");
			setFormDescription("config.description");
			setFormContextHelp("Assessment#_checklist_kursbaustein");
			if(inUse) {
				setFormWarning("config.warning.inuse");
			}
		}
		
		//due date
		Boolean dueDateBool = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
		Date dueDate = (Date)config.get(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		String[] theValues = new String[] { "" };
		dueDateEl = uifactory.addCheckboxesHorizontal("duedate", "config.due.date.on", formLayout, onKeys, theValues);
		dueDateEl.addActionListener(FormEvent.ONCHANGE);
		if(dueDateBool != null && dueDateBool.booleanValue()) {
			dueDateEl.select(onKeys[0], true);
		}
		dueDateChooserEl = uifactory.addDateChooser("config.due.date", dueDate, formLayout);
		dueDateChooserEl.setDateChooserTimeEnabled(true);
		dueDateChooserEl.setMandatory(true);
		if(dueDateBool != null && dueDateBool.booleanValue()) {
			dueDateEl.select(onKeys[0], true);
			dueDateChooserEl.setVisible(true);
		} else {
			dueDateChooserEl.setVisible(false);
		}
		uifactory.addSpacerElement("spacer-duedate", formLayout, false);
		
		//points
		Boolean scoreGrantedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		Float minVal = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxVal = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		String[] pointsValues = new String[]{ translate("config.points.on") };
		scoreGrantedEl = uifactory.addCheckboxesHorizontal("points", "config.points", formLayout, onKeys, pointsValues);
		scoreGrantedEl.addActionListener(FormEvent.ONCHANGE);
		if(scoreGrantedBool == null || (scoreGrantedBool != null && scoreGrantedBool.booleanValue())) {
			scoreGrantedEl.select(onKeys[0], true);
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
		
		uifactory.addSpacerElement("spacer-points", formLayout, false);
		
		//passed
		Float cutVal = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		Boolean passedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		Boolean passedSum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
		Integer sumCutValue = (Integer)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
		Boolean manualCorr = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
		
		passedEl = uifactory.addCheckboxesHorizontal("passed", "config.passed", formLayout, onKeys, theValues);
		passedEl.addActionListener(FormEvent.ONCHANGE);
		if(passedBool == null || (passedBool != null && passedBool.booleanValue())) {
			passedEl.select(onKeys[0], true);
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
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", formLayout,
				new String[] { "xx" }, new String[] { null });
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		ignoreInCourseAssessmentEl.select(ignoreInCourseAssessmentEl.getKey(0), ignoreInCourseAssessment);
		
		ignoreInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer3", formLayout, false);

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
		
		uifactory.addSpacerElement("spacer-comment", formLayout, false);
		
		String iu = (String)config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		tipUserEl = uifactory.addRichTextElementForStringDataMinimalistic("tip.user", "config.tip.user", iu, 5, -1, formLayout,
				getWindowControl());
		
		String ic = (String)config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		tipCoachEl = uifactory.addRichTextElementForStringDataMinimalistic("tip.coach", "config.tip.coach", ic, 5, -1, formLayout,
				getWindowControl());
		
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
		boolean closeAfterDueDate = dueDateEl.isAtLeastSelected(1);
		config.set(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE, Boolean.valueOf(closeAfterDueDate));
		Date dueDate = dueDateChooserEl.getDate();
		if(dueDate != null) {
			config.set(CheckListCourseNode.CONFIG_KEY_DUE_DATE, dueDate);
		} else {
			config.remove(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		}
		//score
		Boolean sf = Boolean.valueOf(scoreGrantedEl.isSelected(0));
		config.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, sf);
		if (sf.booleanValue()) {
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, new Float(minPointsEl.getValue()));
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, new Float(maxPointsEl.getValue()));
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_MIN);
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		}
		
		// mandatory passed flag
		Boolean pf = Boolean.valueOf(passedEl.isSelected(0));
		config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, pf);
		config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		config.remove(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
		config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX, Boolean.FALSE);
		config.set(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION, Boolean.FALSE);
		if (pf.booleanValue()) {
			String output = outputEl.getSelectedKey();
			if("cutvalue".equals(output)) {
				config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, new Float(cutValueEl.getValue()));
			} else if("sum".equals(output)) {
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX, Boolean.TRUE);
				int sumCutValue = Integer.parseInt(sumCheckboxEl.getSelectedKey());
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE, Integer.valueOf(sumCutValue));
			} else if("coach".equals(output)) {
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION, Boolean.TRUE);
			}
		}
		
		// course assessment
		boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isVisible() && ignoreInCourseAssessmentEl.isAtLeastSelected(1);
		config.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);

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
				titlePrefixEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			numOfCheckListEl.clearError();
			if(!numOfCheckListEl.isOneSelected()) {
				numOfCheckListEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		// score flag
		minPointsEl.clearError();
		maxPointsEl.clearError();
		if (scoreGrantedEl.isSelected(0)) {
			float min = toFloat(minPointsEl.getValue());
			float max = toFloat(maxPointsEl.getValue());
			if (Float.isNaN(min) || min < 0.0f) {
				minPointsEl.setErrorKey("form.error.wrongFloat", null);
				allOk &= false;
			}
			if (Float.isNaN(max) || max < 0.0f) {
				maxPointsEl.setErrorKey("form.error.wrongFloat", null);
				allOk &= false;
			}
			if (!Float.isNaN(min) && !Float.isNaN(min) && min > max) {
				maxPointsEl.setErrorKey("form.error.minGreaterThanMax", null);
				allOk &= false;
			}
		}
		
		cutValueEl.clearError();
		outputEl.clearError();
		if(passedEl.isSelected(0)) {
			if(outputEl.isOneSelected()) {
				String selectKey = outputEl.getSelectedKey();
				if("cutvalue".equals(selectKey)) {
					float cut = toFloat(cutValueEl.getValue());
					if (Float.isNaN(cut) || cut < 0.0f) {
						cutValueEl.setErrorKey("form.error.wrongFloat", null);
						allOk &= false;
					}
				}
			} else {
				maxPointsEl.setErrorKey("form.legende.mandatory", null);
			}
		}
		
		dueDateChooserEl.clearError();
		if(dueDateEl.isAtLeastSelected(1)) {
			if (!wizard && dueDateChooserEl.isEmpty()) {
				dueDateChooserEl.setErrorKey("form.error.date", null);
				allOk &= false;
			}
		}
		
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(dueDateEl == source) {
			if(!wizard) {
				boolean selected = dueDateEl.isAtLeastSelected(1);
				dueDateChooserEl.setVisible(selected);
			}
		} else if(scoreGrantedEl == source) {
			updateScoreVisibility();
		} else if(passedEl == source) {
			updatePassedAndOutputVisibilty();
		} else if(outputEl == source) {
			updatePassedAndOutputVisibilty();
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
		}
		super.event(ureq, source, event);
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
		boolean granted = scoreGrantedEl.isSelected(0);
		minPointsEl.setVisible(granted);
		minPointsEl.setMandatory(granted);
		maxPointsEl.setVisible(granted);
		maxPointsEl.setMandatory(granted);
		updateIgnoreInCourseAssessmentVisibility();
	}
	
	private void updatePassedAndOutputVisibilty() {
		if(passedEl.isSelected(0)) {
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
		updateIgnoreInCourseAssessmentVisibility();
	}
	
	private void updateIgnoreInCourseAssessmentVisibility() {
		boolean ignoreInScoreVisible = ignoreInCourseAssessmentAvailable
				&& (scoreGrantedEl.isSelected(0) || passedEl.isSelected(0));
		ignoreInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		ignoreInCourseAssessmentSpacer.setVisible(ignoreInScoreVisible);
	}
	
}