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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

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
	private MultipleSelectionElement passedEnabledEl;
	private SingleSelection passedTypeEl;
	private TextElement titleEl;
	private TextElement cutValueEl;
	private TextElement maxTimeHourEl;
	private TextElement maxTimeMinuteEl;
	
	private final RepositoryEntry testEntry;
	private final boolean restrictedEdit;
	private final AssessmentTest assessmentTest;
	private final AssessmentTestBuilder testBuilder;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private QTI21DeliveryOptions deliveryOptions;
	
	@Autowired
	private QTI21Service qti21Service;
	
	public AssessmentTestOptionsEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			AssessmentTest assessmentTest, ResolvedAssessmentTest resolvedAssessmentTest, AssessmentTestBuilder testBuilder, boolean restrictedEdit) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.testEntry = testEntry;
		this.assessmentTest = assessmentTest;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		this.testBuilder = testBuilder;
		this.restrictedEdit = restrictedEdit;
		this.deliveryOptions = qti21Service.getDeliveryOptions(testEntry);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/tests/Configure_tests/");
		
		String title = assessmentTest.getTitle();
		titleEl = uifactory.addTextElement("title", "form.metadata.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		titleEl.setEnabled(testBuilder.isEditable());

		// max score estimated with shuffled and randomized sections, items
		String estimatedMaxScore = getEstimatedMaxScoreText();
		uifactory.addStaticTextElement("max.score", estimatedMaxScore, formLayout);
		if(Settings.isDebuging()) {
			// mostly for me
			Double absolutMaxScore = testBuilder.getMaxScore();
			uifactory.addStaticTextElement("absolut.max.score", AssessmentHelper.getRoundedScore(absolutMaxScore), formLayout);
		}

		Double cutValue = testBuilder.getCutValue();
		PassedType passedType = deliveryOptions.getPassedType(cutValue);
		
		passedEnabledEl = uifactory.addCheckboxesHorizontal("passed.enabled", "passed.enabled", formLayout, onKeys, onValues);
		passedEnabledEl.select(passedEnabledEl.getKey(0), passedType != PassedType.none);
		passedEnabledEl.addActionListener(FormEvent.ONCHANGE);
		passedEnabledEl.setEnabled(!restrictedEdit && testBuilder.isEditable());
		
		SelectionValues passeddTypeKV = new SelectionValues();
		passeddTypeKV.add(SelectionValues.entry(PassedType.manually.name(), translate("passed.manually")));
		passeddTypeKV.add(SelectionValues.entry(PassedType.cutValue.name(), translate("passed.cut.value")));
		passedTypeEl = uifactory.addRadiosVertical("passed.type", formLayout, passeddTypeKV.keys(), passeddTypeKV.values());
		passedTypeEl.addActionListener(FormEvent.ONCHANGE);
		if (passedType != PassedType.none) {
			passedTypeEl.select(passedType.name(), true);
		}
		passedTypeEl.setEnabled(!restrictedEdit && testBuilder.isEditable());
		
		String cutValueStr = cutValue == null ? "" : cutValue.toString();
		cutValueEl = uifactory.addTextElement("cut.value", "cut.value", 8, cutValueStr, formLayout);
		cutValueEl.setMandatory(true);
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
		
		updateUI();
	}
	
	private String getEstimatedMaxScoreText() {
		Double estimatedMaxScore = QtiMaxScoreEstimator.estimateMaxScore(resolvedAssessmentTest);
		if(estimatedMaxScore == null) {
			estimatedMaxScore = testBuilder.getMaxScore();
		}
		StringBuilder sb = new StringBuilder();
		if(estimatedMaxScore != null) {
			sb.append(AssessmentHelper.getRoundedScore(estimatedMaxScore));
		}
		return sb.toString();
	}
	
	private void updateUI() {
		boolean passedTypeVisible = passedEnabledEl.isAtLeastSelected(1);
		passedTypeEl.setVisible(passedTypeVisible);
		
		boolean cutValueVisible = passedTypeEl.isVisible()
				&& passedTypeEl.isOneSelected()
				&& passedTypeEl.getSelectedKey().equals(PassedType.cutValue.name());
		cutValueEl.setVisible(cutValueVisible);
	}

	public String getTitle() {
		return titleEl.getValue();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		passedTypeEl.clearError();
		if (passedTypeEl.isVisible() && passedTypeEl.isEnabled() && !passedTypeEl.isOneSelected()) {
			passedTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		cutValueEl.clearError();
		if (cutValueEl.isVisible() && cutValueEl.isEnabled()) {
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
			} else {
				cutValueEl.setErrorKey("form.legende.mandatory", null);
			}
		}

		
		maxTimeCont.clearError();
		if(maxTimeEl.isAtLeastSelected(1)) {
			allOk &= validateTime(maxTimeHourEl);
			allOk &= validateTime(maxTimeMinuteEl);
		}
		
		return allOk;
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
		} else if(passedEnabledEl == source) {
			updateUI();
		} else if(passedTypeEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean passedEnabled = passedEnabledEl.isAtLeastSelected(1);
		if (passedEnabled) {
			String passedTypeKey = passedTypeEl.isOneSelected()
					? passedTypeEl.getSelectedKey()
					: PassedType.none.name();
			PassedType passedType = PassedType.valueOf(passedTypeKey);
			deliveryOptions.setPassedType(passedType);
		} else {
			deliveryOptions.setPassedType(PassedType.none);
		}
		qti21Service.setDeliveryOptions(testEntry, deliveryOptions);
		
		String title = titleEl.getValue();
		assessmentTest.setTitle(title);
		
		String cutValue = cutValueEl.isVisible()? cutValueEl.getValue(): null;
		if(StringHelper.containsNonWhitespace(cutValue)) {
			testBuilder.setCutValue(Double.valueOf(cutValue));
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
