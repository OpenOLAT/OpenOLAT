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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 04.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListConfigurationController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] outputKeys = new String[]{ "cutvalue", "sum", "coach"};
	
	private MultipleSelectionElement dueDateEl, scoreGrantedEl, passedEl, commentEl;
	private SingleSelection outputEl;
	private TextElement minPointsEl, maxPointsEl, cutValueEl;
	private RichTextElement tipUserEl, tipCoachEl;
	private DateChooser dueDateChooserEl;
	
	private ModuleConfiguration config;
	
	
	public CheckListConfigurationController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		super(ureq, wControl);
		
		config = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.title");
		setFormDescription("config.description");
		setFormContextHelp("org.olat.course.nodes.cl.ui", "cl-config.html", "help.hover.config");
		
		//due date
		Boolean dueDateBool = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
		Date dueDate = (Date)config.get(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		String[] theValues = new String[] { "" };
		dueDateEl = uifactory.addCheckboxesHorizontal("duedate", "config.due.date.on", formLayout, onKeys, theValues, null);
		dueDateEl.addActionListener(this, FormEvent.ONCHANGE);
		if(dueDateBool != null && dueDateBool.booleanValue()) {
			dueDateEl.select(onKeys[0], true);
		}
		dueDateChooserEl = uifactory.addDateChooser("config.due.date", dueDate, formLayout);
		dueDateChooserEl.setDateChooserTimeEnabled(true);
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
		scoreGrantedEl = uifactory.addCheckboxesHorizontal("points", "config.points", formLayout, onKeys, pointsValues, null);
		if(scoreGrantedBool != null && scoreGrantedBool.booleanValue()) {
			scoreGrantedEl.select(onKeys[0], true);
		}
		String minValStr = minVal == null ? "" : Float.toString(minVal.floatValue());
		minPointsEl = uifactory.addTextElement("pointsmin", "config.points.min", 4, minValStr, formLayout);
		minPointsEl.setDisplaySize(5);
		String maxValStr = maxVal == null ? "" : Float.toString(maxVal.floatValue());
		maxPointsEl = uifactory.addTextElement("pointsmax", "config.points.max", 4, maxValStr, formLayout);
		maxPointsEl.setDisplaySize(5);
		uifactory.addSpacerElement("spacer-points", formLayout, false);
		
		//passed
		Boolean passedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		passedEl = uifactory.addCheckboxesHorizontal("passed", "config.passed", formLayout, onKeys, theValues, null);
		if(passedBool != null && passedBool.booleanValue()) {
			passedEl.select(onKeys[0], true);
		}
		String[] outputValues = new String[]{
			translate("config.output.cutvalue"), translate("config.output.sum"), translate("config.output.coach")
		};
		outputEl = uifactory.addRadiosVertical("output", "config.output", formLayout, outputKeys, outputValues);
		Boolean passedSum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
		if(passedSum != null && passedSum.booleanValue()) {
			outputEl.select(outputKeys[1], true);
		}
		Boolean manualCorr = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
		if(manualCorr != null && manualCorr.booleanValue()) {
			outputEl.select(outputKeys[2], true);
		}
		Float cutVal = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		if(cutVal != null && cutVal.floatValue() > -0.1) {
			outputEl.select(outputKeys[0], true);
		}
		String cutValStr = cutVal == null ? "" : Float.toString(cutVal.floatValue());
		cutValueEl = uifactory.addTextElement("cutvalue", "config.cutvalue", 4, cutValStr, formLayout);
		cutValueEl.setDisplaySize(5);
		uifactory.addSpacerElement("spacer-passed", formLayout, false);
		
		//comment
		commentEl = uifactory.addCheckboxesHorizontal("comment", "config.comment", formLayout, onKeys, theValues, null);
		Boolean commentBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		if(commentBool != null && commentBool.booleanValue()) {
			commentEl.select(onKeys[0], true);
		}
		
		String iu = (String)config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		tipUserEl = uifactory.addRichTextElementForStringDataMinimalistic("tip.user", "config.tip.user", iu, 5, -1, formLayout,
				ureq.getUserSession(), getWindowControl());
		
		String ic = (String)config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		tipCoachEl = uifactory.addRichTextElementForStringDataMinimalistic("tip.coach", "config.tip.coach", ic, 5, -1, formLayout,
				ureq.getUserSession(), getWindowControl());
		
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("submit", "submit", buttonsLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//due date
		boolean closeAfterDueDate = dueDateEl.isAtLeastSelected(1);
		config.set(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE, new Boolean(closeAfterDueDate));
		Date dueDate = dueDateChooserEl.getDate();
		if(dueDate != null) {
			config.set(CheckListCourseNode.CONFIG_KEY_DUE_DATE, dueDate);
		} else {
			config.remove(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		}
		//score
		Boolean sf = new Boolean(scoreGrantedEl.isSelected(0));
		config.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, sf);
		if (sf.booleanValue()) {
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, new Float(minPointsEl.getValue()));
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, new Float(maxPointsEl.getValue()));
		} else {
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_MIN);
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		}
		
		// mandatory passed flag
		Boolean pf = new Boolean(passedEl.isSelected(0));
		config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, pf);
		config.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX, Boolean.FALSE);
		config.set(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION, Boolean.FALSE);
		if (pf.booleanValue()) {
			String output = outputEl.getSelectedKey();
			if("cutvalue".equals(output)) {
				config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, new Float(cutValueEl.getValue()));
			} else if("sum".equals(output)) {
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX, Boolean.TRUE);
			} else if("coach".equals(output)) {
				config.set(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION, Boolean.TRUE);
			}
		}

		// mandatory comment flag
		config.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, new Boolean(commentEl.isSelected(0)));

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
		boolean allOk = true;
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
						minPointsEl.setErrorKey("form.error.wrongFloat", null);
						allOk &= false;
					}
				}
			} else {
				maxPointsEl.setErrorKey("form.legende.mandatory", null);
			}
		}
		return allOk & super.validateFormLogic(ureq);
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
			boolean selected = dueDateEl.isAtLeastSelected(1);
			dueDateChooserEl.setVisible(selected);
			
			
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	
}
