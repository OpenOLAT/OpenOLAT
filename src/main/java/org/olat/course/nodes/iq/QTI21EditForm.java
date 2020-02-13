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
package org.olat.course.nodes.iq;

import java.io.File;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 26.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditForm extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	private static final String[] correctionModeKeys = new String[] { 
			IQEditController.CORRECTION_AUTO, IQEditController.CORRECTION_MANUAL, IQEditController.CORRECTION_GRADING
	};
	private static final String[] resultsOptionsKeys = new String[] {
			QTI21AssessmentResultsOptions.METADATA, QTI21AssessmentResultsOptions.SECTION_SUMMARY,
			QTI21AssessmentResultsOptions.QUESTION_SUMMARY,
			QTI21AssessmentResultsOptions.USER_SOLUTIONS, QTI21AssessmentResultsOptions.CORRECT_SOLUTIONS
	};

	private SingleSelection correctionModeEl;
	private MultipleSelectionElement showResultsOnHomePage;
	private MultipleSelectionElement scoreInfo;
	private MultipleSelectionElement showResultsDateDependentButton;
	private DateChooser endDateElement;
	private DateChooser startDateElement;
	private MultipleSelectionElement testDateDependentEl;
	private DateChooser startTestDateElement;
	private DateChooser endTestDateElement;
	private StaticTextElement minScoreEl;
	private StaticTextElement maxScoreEl;
	private StaticTextElement cutValueEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement assessmentResultsOnFinishEl;
	private FormLayoutContainer reportLayout;
	
	private final boolean needManualCorrection;
	private final ModuleConfiguration modConfig;
	private final QTI21DeliveryOptions deliveryOptions;
	
	private DialogBoxController confirmTestDateCtrl;

	@Autowired
	private QTI21Service qtiService;
	
	public QTI21EditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig,
			QTI21DeliveryOptions deliveryOptions, boolean needManualCorrection) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		this.modConfig = modConfig;
		this.deliveryOptions = (deliveryOptions == null ? new QTI21DeliveryOptions() : deliveryOptions);
		this.needManualCorrection = needManualCorrection;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer testLayout = FormLayoutContainer.createDefaultFormLayout("testInfos", getTranslator());
		testLayout.setRootForm(mainForm);
		formLayout.add(testLayout);
		initFormAssessmentInfos(testLayout);

		FormLayoutContainer correctionLayout = FormLayoutContainer.createDefaultFormLayout("correction", getTranslator());
		correctionLayout.setElementCssClass("o_qti_21_correction");
		correctionLayout.setFormTitle(translate("correction.config"));
		correctionLayout.setRootForm(mainForm);
		formLayout.add(correctionLayout);
		initFormCorrection(correctionLayout);
		
		reportLayout = FormLayoutContainer.createDefaultFormLayout("report", getTranslator());
		reportLayout.setElementCssClass("o_qti_21_configuration");
		reportLayout.setFormTitle(translate("report.config"));
		reportLayout.setRootForm(mainForm);
		formLayout.add(reportLayout);
		initFormReport(reportLayout);
	}
	
	protected void initFormAssessmentInfos(FormItemContainer formLayout) {
		minScoreEl = uifactory.addStaticTextElement("score.min", "", formLayout);
		minScoreEl.setVisible(false);
		maxScoreEl = uifactory.addStaticTextElement("score.max", "", formLayout);
		maxScoreEl.setVisible(false);
		cutValueEl = uifactory.addStaticTextElement("score.cut", "", formLayout);
		cutValueEl.setVisible(false);
		
		boolean testDateDependent = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST);
		testDateDependentEl = uifactory.addCheckboxesHorizontal("qti_datetest", "qti.form.test.date", formLayout, new String[]{"xx"}, new String[]{null});
		testDateDependentEl.select("xx", testDateDependent);
		testDateDependentEl.setHelpTextKey("qti.form.test.date.help", null);
		testDateDependentEl.addActionListener(FormEvent.ONCLICK);
	
		Date startTestDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_TEST_DATE);
		startTestDateElement = uifactory.addDateChooser("qti_form_start_test_date", "qti.form.date.start", startTestDate, formLayout);
		startTestDateElement.setDateChooserTimeEnabled(true);
		startTestDateElement.setMandatory(true);
		
		Date endTestDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_TEST_DATE);
		endTestDateElement = uifactory.addDateChooser("qti_form_end_test_date", "qti.form.date.end", endTestDate, formLayout);
		endTestDateElement.setDateChooserTimeEnabled(true);
	}
	
	protected void initFormCorrection(FormItemContainer formLayout) {
		String mode = modConfig.getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		
		KeyValues correctionKeyValues = new KeyValues();
		correctionKeyValues.add(KeyValues.entry(correctionModeKeys[0], translate("correction.auto")));
		correctionKeyValues.add(KeyValues.entry(correctionModeKeys[1], translate("correction.manual")));
		if(correctionModeKeys[2].equals(mode) || IQEditController.isGradingEnabled(modConfig)) {
			correctionKeyValues.add(KeyValues.entry(correctionModeKeys[2], translate("correction.grading")));
		}
		
		correctionModeEl = uifactory.addRadiosVertical("correction.mode", "correction.mode", formLayout,
				correctionKeyValues.keys(), correctionKeyValues.values());
		correctionModeEl.setHelpText(translate("correction.mode.help"));
		correctionModeEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#details_testeditor_test_konf_kurs");

		boolean selected = false;
		for(String correctionModeKey:correctionKeyValues.keys()) {
			if(correctionModeKey.equals(mode)) {
				correctionModeEl.select(correctionModeKey, true);
				selected = true;
			}
		}
		if(!selected) {
			if(needManualCorrection) {
				correctionModeEl.select(correctionModeKeys[1], true);
			} else {
				correctionModeEl.select(correctionModeKeys[0], true);
			}
		}
	}

	protected void initFormReport(FormItemContainer formLayout) {
		//Show score informations on start page
		boolean enableScoreInfos = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		scoreInfo = uifactory.addCheckboxesHorizontal("qti_scoreInfo", "qti.form.scoreinfo", formLayout, new String[]{"xx"}, new String[]{null});
		if(enableScoreInfos) {
			scoreInfo.select("xx", enableScoreInfos);
		}
		scoreInfo.addActionListener(FormEvent.ONCLICK);
		
		boolean showResultOnHomePage = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		showResultsOnHomePage = uifactory.addCheckboxesHorizontal("qti_enableResultsOnHomePage", "qti.form.results.onhomepage", formLayout, new String[]{"xx"}, new String[]{null});
		if(showResultOnHomePage) {
			showResultsOnHomePage.select("xx", showResultOnHomePage);
		}
		showResultsOnHomePage.setElementCssClass("o_sel_results_on_homepage");
		showResultsOnHomePage.addActionListener(FormEvent.ONCLICK);
		
		boolean showResultsDateDependent = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		showResultsDateDependentButton = uifactory.addCheckboxesHorizontal("qti_showresult", "qti.form.show.results", formLayout, new String[]{"xx"}, new String[]{null});
		showResultsDateDependentButton.select("xx", showResultsDateDependent);
		showResultsDateDependentButton.addActionListener(FormEvent.ONCLICK);
	
		Date startDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		startDateElement = uifactory.addDateChooser("qti_form_start_date", "qti.form.date.start", null, formLayout);
		startDateElement.setDateChooserTimeEnabled(true);
		startDateElement.setDate(startDate);
		startDateElement.setMandatory(true);
		
		Date endDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		endDateElement = uifactory.addDateChooser("qti_form_end_date", "qti.form.date.end", null, formLayout);
		endDateElement.setDateChooserTimeEnabled(true);
		endDateElement.setDate(endDate);
		
		QTI21AssessmentResultsOptions resultsOptions = deliveryOptions.getAssessmentResultsOptions();
		if(!AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT.equals(modConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY))) {
			resultsOptions = QTI21AssessmentResultsOptions.parseString(modConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT));
		}
		
		showResultsOnFinishEl = uifactory.addCheckboxesHorizontal("resultOnFinish", "qti.form.results.onfinish", formLayout, onKeys, onValues);
		showResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results");
		showResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		showResultsOnFinishEl.setHelpText(translate("qti.form.results.onfinish.help"));
		Boolean showResultOnFinish = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
		if(showResultOnFinish == null) {//not set -> back the default
			if(!resultsOptions.none()) {
				showResultsOnFinishEl.select(onKeys[0], true);
			}
		} else if(showResultOnFinish.booleanValue()) {
			showResultsOnFinishEl.select(onKeys[0], true);
		}

		String[] resultsOptionsValues = new String[] {
				translate("qti.form.summary.metadata"), translate("qti.form.summary.sections"),
				translate("qti.form.summary.questions.metadata"),
				translate("qti.form.summary.responses"), translate("qti.form.summary.solutions")
		};
		assessmentResultsOnFinishEl = uifactory.addCheckboxesVertical("typeResultOnFinish", "qti.form.summary", formLayout, resultsOptionsKeys, resultsOptionsValues, 1);
		assessmentResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results_options");
		assessmentResultsOnFinishEl.setHelpText(translate("qti.form.summary.help"));
		assessmentResultsOnFinishEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#overview_results");
		
		uifactory.addFormSubmitButton("submit", formLayout);
		
		//setup the values
		update();
		
		updateAssessmentResultsOnFinish(resultsOptions);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmTestDateCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				update();
				reportLayout.setDirty(true);
			} else {
				testDateDependentEl.uncheckAll();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startTestDateElement.clearError();
		if(testDateDependentEl.isSelected(0)) {
			if(startTestDateElement.getDate() == null) {
				startTestDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(endTestDateElement.getDate() != null && startTestDateElement.getDate().after(endTestDateElement.getDate())) {
				startTestDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			}
		}

		startDateElement.clearError();
		if(showResultsDateDependentButton.isSelected(0)) {
			if(startDateElement.getDate() == null) {
				startDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(endDateElement.getDate() != null && startDateElement.getDate().after(endDateElement.getDate())) {
				startDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showResultsOnFinishEl == source
				|| showResultsOnHomePage == source
				|| showResultsDateDependentButton == source) {
			update();
		} else if(testDateDependentEl == source) {
			if(testDateDependentEl.isAtLeastSelected(1)) {
				confirmTestDates(ureq);
			} else {
				update();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void update() {
		showResultsDateDependentButton.setVisible(showResultsOnHomePage.isSelected(0));

		assessmentResultsOnFinishEl.setVisible(showResultsOnFinishEl.isSelected(0) || showResultsOnHomePage.isSelected(0));

		resetDateChooser(startDateElement, showResultsDateDependentButton);
		resetDateChooser(endDateElement, showResultsDateDependentButton);
		
		resetDateChooser(startTestDateElement, testDateDependentEl);
		resetDateChooser(endTestDateElement, testDateDependentEl);
	}
	
	private void resetDateChooser(DateChooser dateElement, MultipleSelectionElement parentEl) {
		dateElement.clearError();
		if (!dateElement.isVisible()){
			dateElement.setValue("");
		}
		dateElement.setVisible(parentEl.isVisible() && parentEl.isSelected(0));
	}
	
	private void updateAssessmentResultsOnFinish(QTI21AssessmentResultsOptions resultsOptions) {
		if(!resultsOptions.none()) {
			if(resultsOptions.isMetadata()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[0], true);
			}
			if(resultsOptions.isSectionSummary()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[1], true);
			}
			if(resultsOptions.isQuestionSummary()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[2], true);
			}
			if(resultsOptions.isUserSolutions()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[3], true);
			}
			if(resultsOptions.isCorrectSolutions()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[4], true);
			}
		}
	}
	
	protected void update(RepositoryEntry testEntry) {
		Double minValue = null;
		Double maxValue = null;
		Double cutValue = null;
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		if(assessmentTest != null) {
			AssessmentTestBuilder testBuilder = new AssessmentTestBuilder(assessmentTest);
			maxValue = testBuilder.getMaxScore();
			cutValue = testBuilder.getCutValue();
			if(maxValue != null && "OpenOLAT".equals(assessmentTest.getToolName())) {
				minValue = 0d;
			}
		}

		// Put values to module configuration
		minScoreEl.setValue(minValue == null ? "" : AssessmentHelper.getRoundedScore(minValue));
		minScoreEl.setVisible(minValue != null);
		maxScoreEl.setValue(maxValue == null ? "" : AssessmentHelper.getRoundedScore(maxValue));
		maxScoreEl.setVisible(maxValue != null);
		cutValueEl.setValue(cutValue == null ? "" : AssessmentHelper.getRoundedScore(cutValue));
		cutValueEl.setVisible(cutValue != null);
		
		update();
	}
	
	private void confirmTestDates(UserRequest ureq) {
		String title = translate("qti.form.test.date");
		String text = translate("qti.form.test.date.confirm");
		confirmTestDateCtrl = activateOkCancelDialog(ureq, title, text, confirmTestDateCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, testDateDependentEl.isSelected(0));
		
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_START_TEST_DATE, startTestDateElement.getDate());
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_END_TEST_DATE, endTestDateElement.getDate());
		
		if(correctionModeEl.isOneSelected()) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}

		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLESCOREINFO, scoreInfo.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, showResultsDateDependentButton.isSelected(0));
		
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE, startDateElement.getDate());
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE, endDateElement.getDate());
		
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, showResultsOnHomePage.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_FINISH, showResultsOnFinishEl.isSelected(0));
		
		if(showResultsOnFinishEl.isSelected(0) || showResultsOnHomePage.isSelected(0)) {
			if(assessmentResultsOnFinishEl.isAtLeastSelected(1)) {
				String options = QTI21AssessmentResultsOptions.toString(assessmentResultsOnFinishEl.getSelectedKeys());
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, options);
			} else {
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
			}
		} else {
			modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
