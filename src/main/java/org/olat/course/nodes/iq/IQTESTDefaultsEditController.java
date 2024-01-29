/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 07, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IQTESTDefaultsEditController extends FormBasicController {

	private static final String dateBase = "date.";
	private static final String[] dateKeys = new String[]{
			"no",
			IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS,
	};
	private static final String[] resultsOptionsKeys = new String[]{
			QTI21AssessmentResultsOptions.METADATA, QTI21AssessmentResultsOptions.SECTION_SUMMARY,
			QTI21AssessmentResultsOptions.QUESTION_SUMMARY,
			QTI21AssessmentResultsOptions.USER_SOLUTIONS, QTI21AssessmentResultsOptions.CORRECT_SOLUTIONS
	};
	private final String[] dateValues = new String[dateKeys.length];

	private MultipleSelectionElement scoreInfo;
	private SingleSelection showResultsDateDependentEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement assessmentResultsOnFinishEl;
	private FormLink resetDefaultsButton;
	private FormLink backLink;

	private DialogBoxController confirmReset;

	@Autowired
	private IQTESTModule iqtestModule;

	public IQTESTDefaultsEditController(UserRequest ureq, WindowControl wControl, String title) {
		super(ureq, wControl, "iqtest_def_conf");
		flc.contextPut("title", title);
		initDateValues();
		initForm(ureq);
		loadDefaultConfigValues();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		resetDefaultsButton = uifactory.addFormLink("reset", "course.node.reset.defaults", null, flc, Link.BUTTON);
		resetDefaultsButton.setElementCssClass("o_sel_cal_delete pull-right");
		backLink = uifactory.addFormLink("back", flc);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		FormLayoutContainer testCont = FormLayoutContainer.createDefaultFormLayout("tests", getTranslator());
		testCont.setFormTitle(translate("report.config"));
		testCont.setRootForm(mainForm);
		formLayout.add(testCont);

		scoreInfo = uifactory.addCheckboxesHorizontal("qti_scoreInfo", "qti.form.scoreinfo", testCont, new String[]{"on"}, new String[]{""});

		showResultsDateDependentEl = uifactory.addDropdownSingleselect("qti_showresult", "qti.form.results.onhomepage", testCont, dateKeys, dateValues);
		showResultsDateDependentEl.addActionListener(FormEvent.ONCHANGE);
		showResultsDateDependentEl.setElementCssClass("o_sel_results_on_homepage");

		showResultsOnFinishEl = uifactory.addCheckboxesHorizontal("resultOnFinish", "qti.form.results.onfinish", testCont, new String[]{"on"}, new String[]{""});
		showResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results");
		showResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);

		String[] resultsOptionsValues = new String[]{
				translate("qti.form.summary.metadata"), translate("qti.form.summary.sections"),
				translate("qti.form.summary.questions.metadata"),
				translate("qti.form.summary.responses"), translate("qti.form.summary.solutions")
		};
		assessmentResultsOnFinishEl = uifactory.addCheckboxesVertical("typeResultOnFinish", "qti.form.summary", testCont, resultsOptionsKeys, resultsOptionsValues, 1);
		assessmentResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		assessmentResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results_options");
		assessmentResultsOnFinishEl.setHelpText(translate("qti.form.summary.help"));
		assessmentResultsOnFinishEl.setHelpUrlForManualPage("manual_user/learningresources/Test_settings/#results");

		// Create submit button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		testCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");
	}

	private void updateDefaultConfigValues() {
		iqtestModule.setScoreInfoEnabled(scoreInfo.isSelected(0));
		iqtestModule.setDateDependentResults(showResultsDateDependentEl.getSelectedKey());
		iqtestModule.setShowResultOnFinish(showResultsOnFinishEl.isSelected(0));
		if (showResultsOnFinishEl.isSelected(0) || !showResultsDateDependentEl.isSelected(0)) {
			String options = QTI21AssessmentResultsOptions.toString(assessmentResultsOnFinishEl.getSelectedKeys());
			iqtestModule.setQtiResultsSummary(options);
		}
	}

	private void initDateValues() {
		for (int i = 0; i < dateKeys.length; i++) {
			dateValues[i] = translate(dateBase + dateKeys[i]);
		}
	}

	private void updateAssessmentResultsOnFinish(QTI21AssessmentResultsOptions resultsOptions) {
		if (!resultsOptions.none()) {
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[0], resultsOptions.isMetadata());
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[1], resultsOptions.isSectionSummary());
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[2], resultsOptions.isQuestionSummary());
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[3], resultsOptions.isUserSolutions());
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[4], resultsOptions.isCorrectSolutions());
		} else {
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[0], false);
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[1], false);
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[2], false);
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[3], false);
			assessmentResultsOnFinishEl.select(resultsOptionsKeys[4], false);
		}
	}

	private void loadDefaultConfigValues() {
		scoreInfo.select("on", iqtestModule.isScoreInfoEnabled());
		showResultsOnFinishEl.select("on", iqtestModule.isShowResultOnFinish());
		showResultsDateDependentEl.select(iqtestModule.getDateDependentResults(), true);
		QTI21AssessmentResultsOptions resultsOptions = QTI21AssessmentResultsOptions.parseString(iqtestModule.getQtiResultsSummary());
		updateAssessmentResultsOnFinish(resultsOptions);
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == resetDefaultsButton) {
			confirmReset = activateYesNoDialog(ureq, null, translate("course.node.confirm.reset"), confirmReset);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateDefaultConfigValues();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmReset) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				iqtestModule.resetProperties();
				loadDefaultConfigValues();
			}
			// Fire this event regardless of yes, no or close
			// Little hack to prevent a dirty form after pressing reset button
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
}
