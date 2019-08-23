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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestOptionsEditorController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[] { "" };

	private FormLayoutContainer maxTimeCont;
	private MultipleSelectionElement maxTimeEl;
	private TextElement maxTimeHourEl, maxTimeMinuteEl;
	private TextElement titleEl, maxScoreEl, cutValueEl;
	
	private final boolean restrictedEdit;
	private final AssessmentTest assessmentTest;
	private final AssessmentTestBuilder testBuilder;
	
	public AssessmentTestOptionsEditorController(UserRequest ureq, WindowControl wControl,
			AssessmentTest assessmentTest, AssessmentTestBuilder testBuilder, boolean restrictedEdit) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.assessmentTest = assessmentTest;
		this.testBuilder = testBuilder;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Configure tests");
		
		String title = assessmentTest.getTitle();
		titleEl = uifactory.addTextElement("title", "form.metadata.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		titleEl.setEnabled(testBuilder.isEditable());

		//score
		String maxScore = testBuilder.getMaxScore() == null ? "" : AssessmentHelper.getRoundedScore(testBuilder.getMaxScore());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxScore, formLayout);
		maxScoreEl.setEnabled(false);
		
		Double cutValue = testBuilder.getCutValue();
		String cutValueStr = cutValue == null ? "" : cutValue.toString();
		cutValueEl = uifactory.addTextElement("cut.value", "cut.value", 8, cutValueStr, formLayout);
		cutValueEl.setEnabled(!restrictedEdit && testBuilder.isEditable());
		
		TimeLimits timeLimits = assessmentTest.getTimeLimits();
		
		long maxInSeconds = -1;
		String timeMaxHour = "";
		String timeMaxMinute = "";
		if(timeLimits != null && timeLimits.getMaximum() != null && timeLimits.getMaximum().longValue() > 0) {
			maxInSeconds = timeLimits.getMaximum().longValue();
			timeMaxHour = Long.toString(maxInSeconds / 3600);
			timeMaxMinute = Long.toString((maxInSeconds % 3600) / 60);
		}
		
		maxTimeEl = uifactory.addCheckboxesVertical("time.limit.enable", "time.limit.max", formLayout, onKeys, onValues, 1);
		maxTimeEl.addActionListener(FormEvent.ONCHANGE);
		if(maxInSeconds > 0) {
			maxTimeEl.select(onKeys[0], true);
		}
		
		String page = velocity_root + "/max_time_limit.html";
		maxTimeCont = FormLayoutContainer.createCustomFormLayout("time.limit.cont", getTranslator(), page);
		maxTimeCont.setVisible(maxTimeEl.isAtLeastSelected(1));
		formLayout.add(maxTimeCont);
		
		timeMaxHour = timeMaxHour.equals("0") ? "" : timeMaxHour;
		maxTimeHourEl = uifactory.addTextElement("time.limit.hour", "time.limit.max", 4, timeMaxHour, maxTimeCont);
		maxTimeHourEl.setDomReplacementWrapperRequired(false);
		maxTimeHourEl.setDisplaySize(4);
		maxTimeHourEl.setEnabled(!restrictedEdit);
		
		maxTimeMinuteEl = uifactory.addTextElement("time.limit.minute", "time.limit.max", 4, timeMaxMinute, maxTimeCont);
		maxTimeMinuteEl.setDomReplacementWrapperRequired(false);
		maxTimeMinuteEl.setDisplaySize(4);
		maxTimeMinuteEl.setEnabled(!restrictedEdit);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("butons", getTranslator());
		formLayout.add(buttonsCont);
		FormSubmit submit = uifactory.addFormSubmitButton("save", "save", buttonsCont);
		submit.setEnabled(testBuilder.isEditable());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	public String getTitle() {
		return titleEl.getValue();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		cutValueEl.clearError();
		if(StringHelper.containsNonWhitespace(cutValueEl.getValue())) {
			String cutValue = cutValueEl.getValue();
			try {
				double val = Double.parseDouble(cutValue);
				if(val < 0.0) {
					cutValueEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				cutValueEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		
		maxTimeCont.clearError();
		if(maxTimeEl.isAtLeastSelected(1)) {
			allOk &= validateTime(maxTimeHourEl);
			allOk &= validateTime(maxTimeMinuteEl);
		}
		
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateTime(TextElement timeEl) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(timeEl.getValue())) {
			try {
				double val = Long.parseLong(timeEl.getValue());
				if(val < 0l) {
					maxTimeCont.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxTimeCont.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(maxTimeEl == source) {
			maxTimeCont.setVisible(maxTimeEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		assessmentTest.setTitle(title);
		
		String cutValue = cutValueEl.getValue();
		if(StringHelper.containsNonWhitespace(cutValue)) {
			testBuilder.setCutValue(new Double(cutValue));
		} else {
			testBuilder.setCutValue(null);
		}
		
		if(maxTimeEl.isAtLeastSelected(1)) {
			long maxTime = 0;
			if(StringHelper.containsNonWhitespace(maxTimeHourEl.getValue())) {
				maxTime += Long.parseLong(maxTimeHourEl.getValue()) * 3600;
			}
			if(StringHelper.containsNonWhitespace(maxTimeMinuteEl.getValue())) {
				maxTime += Long.parseLong(maxTimeMinuteEl.getValue()) * 60;
			}
			if(maxTime > 0) {
				testBuilder.setMaximumTimeLimits(maxTime);
			} else {
				testBuilder.setMaximumTimeLimits(null);
			}
		} else {
			testBuilder.setMaximumTimeLimits(null);
		}
		
		fireEvent(ureq, AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT);
	}
}
