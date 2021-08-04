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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.io.File;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.wizard.AssessmentModeDefaults;
import org.olat.course.wizard.IQTESTCourseNodeContext;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
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
	private static final String dateBase = "date.";
	private static final String[] dateKeys = new String[] { 
			"no",
			IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS,
			IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY,
			IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY,
			IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT,
			IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME,
	};
	private final String[] dateValues = new String[dateKeys.length];
	private final String ASSESSMENT_MODE_AUTO = "auto";
	private final String ASSESSMENT_MODE_MANUAL = "manual";
	private final String ASSESSMENT_MODE_NONE = "none";

	private SingleSelection correctionModeEl;
	private SingleSelection scoreVisibilityAfterCorrectionEl;
	private SingleSelection showResultsDateDependentEl;
	private MultipleSelectionElement scoreInfo;
	private DateChooser generalEndDateElement;
	private DateChooser generalStartDateElement;
	private DateChooser failedEndDateElement;
	private DateChooser failedStartDateElement;
	private DateChooser passedEndDateElement;
	private DateChooser passedStartDateElement;
	private MultipleSelectionElement testDateDependentEl;
	private DateChooser startTestDateElement;
	private DateChooser endTestDateElement;
	private SingleSelection assessmentModeEl;
	private IntegerElement leadTimeEl;
	private IntegerElement followupTimeEl;
	private StaticTextElement minScoreEl;
	private StaticTextElement maxScoreEl;
	private StaticTextElement passedTypeEl;
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement assessmentResultsOnFinishEl;
	private FormLayoutContainer reportLayout;
	private FormLayoutContainer testLayout;
	
	private final boolean selfAssessment;
	private final boolean needManualCorrection;
	private final ModuleConfiguration modConfig;
	private final boolean ignoreInCourseAssessmentAvailable;
	private final QTI21DeliveryOptions deliveryOptions;
	private final boolean wizard;
	private final AssessmentModeDefaults assessmentModeDefaults;
	
	private DialogBoxController confirmTestDateCtrl;

	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public QTI21EditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig,
			NodeAccessType nodeAccessType, QTI21DeliveryOptions deliveryOptions,
			boolean needManualCorrection, boolean selfAssessment) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.modConfig = modConfig;
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		this.deliveryOptions = (deliveryOptions == null ? new QTI21DeliveryOptions() : deliveryOptions);
		this.needManualCorrection = needManualCorrection;
		this.selfAssessment = selfAssessment;
		this.wizard = false;
		this.assessmentModeDefaults = null;
		initDateValues();
		initForm(ureq);
	}

	public QTI21EditForm(UserRequest ureq, WindowControl wControl, Form rootForm, IQTESTCourseNodeContext context,
			NodeAccessType nodeAccessType, boolean needManualCorrection, boolean selfAssessment) {
		super(ureq, wControl, LAYOUT_BAREBONE, null, rootForm);
		this.modConfig = context.getModuleConfig();
		this.assessmentModeDefaults = context;
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		this.deliveryOptions = new QTI21DeliveryOptions();
		this.needManualCorrection = needManualCorrection;
		this.selfAssessment = selfAssessment;
		this.wizard = true;
		initDateValues();
		initForm(ureq);
	}

	private void initDateValues() {
		for (int i = 0; i < dateKeys.length; i++) {
			dateValues[i] = translate(dateBase + dateKeys[i]);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		testLayout = FormLayoutContainer.createDefaultFormLayout("testInfos", getTranslator());
		testLayout.setRootForm(mainForm);
		if (wizard) {
			testLayout.setFormTitle(translate("execution"));
		}
		formLayout.add(testLayout);
		initFormAssessmentInfos(testLayout);

		FormLayoutContainer correctionLayout = FormLayoutContainer.createDefaultFormLayout("correction", getTranslator());
		correctionLayout.setElementCssClass("o_qti_21_correction");
		correctionLayout.setFormTitle(translate("correction.config"));
		correctionLayout.setRootForm(mainForm);
		formLayout.add(correctionLayout);
		if(selfAssessment) {
			if(needManualCorrection) {
				correctionLayout.contextPut("off_warn", translate("correction.manual.self"));
			} else {
				correctionLayout.setVisible(false);
			}
		}
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
		passedTypeEl = uifactory.addStaticTextElement("score.passed", "", formLayout);
		passedTypeEl.setVisible(!wizard);
		
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", formLayout,
				new String[] { "xx" }, new String[] { null });
		boolean ignoreInCourseAssessment = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		ignoreInCourseAssessmentEl.select(ignoreInCourseAssessmentEl.getKey(0), ignoreInCourseAssessment);
		ignoreInCourseAssessmentEl.setVisible(!wizard && ignoreInCourseAssessmentAvailable);
		
		boolean testDateDependent = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST);
		testDateDependentEl = uifactory.addCheckboxesHorizontal("qti_datetest", "qti.form.test.date", formLayout, new String[]{"xx"}, new String[]{null});
		testDateDependentEl.setElementCssClass("o_qti_21_datetest");
		testDateDependentEl.select("xx", testDateDependent);
		testDateDependentEl.setHelpTextKey("qti.form.test.date.help", null);
		testDateDependentEl.addActionListener(FormEvent.ONCLICK);
	
		Date startTestDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE);
		startTestDateElement = uifactory.addDateChooser("qti_form_start_test_date", "qti.form.date.start", startTestDate, formLayout);
		startTestDateElement.setElementCssClass("o_qti_21_datetest_start");
		startTestDateElement.setDateChooserTimeEnabled(true);
		startTestDateElement.setMandatory(true);
		
		Date endTestDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE);
		endTestDateElement = uifactory.addDateChooser("qti_form_end_test_date", "qti.form.date.end", endTestDate, formLayout);
		endTestDateElement.setElementCssClass("o_qti_21_datetest_end");
		endTestDateElement.setDateChooserTimeEnabled(true);
		endTestDateElement.setMandatory(wizard);
		endTestDateElement.setDefaultValue(startTestDateElement);
		
		if (wizard) {
			SelectionValues assessmentModeKV = new SelectionValues();
			assessmentModeKV.add(entry(ASSESSMENT_MODE_AUTO, translate("assessment.mode.auto")));
			assessmentModeKV.add(entry(ASSESSMENT_MODE_MANUAL, translate("assessment.mode.manual")));
			assessmentModeKV.add(entry(ASSESSMENT_MODE_NONE, translate("assessment.mode.none")));
			assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", formLayout, assessmentModeKV.keys(), assessmentModeKV.values());
			assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
			if (assessmentModeDefaults.isEnabled()) {
				if (assessmentModeDefaults.isManualBeginEnd()) {
					assessmentModeEl.select(ASSESSMENT_MODE_MANUAL, true);
				} else {
					assessmentModeEl.select(ASSESSMENT_MODE_AUTO, true);
				}
			} else {
				assessmentModeEl.select(ASSESSMENT_MODE_NONE, true);
			}
			
			leadTimeEl = uifactory.addIntegerElement("assessment.mode.leadTime", assessmentModeDefaults.getLeadTime(), formLayout);
			leadTimeEl.setDisplaySize(3);
			
			followupTimeEl = uifactory.addIntegerElement("assessment.mode.followupTime", assessmentModeDefaults.getFollowUpTime(), formLayout);
			followupTimeEl.setDisplaySize(3);
		}
	}
	
	protected void initFormCorrection(FormItemContainer formLayout) {
		String mode = modConfig.getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		
		SelectionValues correctionKeyValues = new SelectionValues();
		correctionKeyValues.add(SelectionValues.entry(correctionModeKeys[0], translate("correction.auto")));
		correctionKeyValues.add(SelectionValues.entry(correctionModeKeys[1], translate("correction.manual")));
		if(correctionModeKeys[2].equals(mode) || IQEditController.isGradingEnabled(modConfig)) {
			correctionKeyValues.add(SelectionValues.entry(correctionModeKeys[2], translate("correction.grading")));
		}
		
		correctionModeEl = uifactory.addRadiosVertical("correction.mode", "correction.mode", formLayout,
				correctionKeyValues.keys(), correctionKeyValues.values());
		correctionModeEl.addActionListener(FormEvent.ONCHANGE);
		correctionModeEl.setHelpText(translate("correction.mode.help"));
		correctionModeEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#details_testeditor_test_konf_kurs");
		correctionModeEl.setVisible(!selfAssessment);

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
		
		SelectionValues visibilityKeyValues = new SelectionValues();
		visibilityKeyValues.add(SelectionValues.entry(IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION, translate("results.visibility.after.correction.visible")));
		visibilityKeyValues.add(SelectionValues.entry(IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION, translate("results.visibility.after.correction.not.visible")));
		scoreVisibilityAfterCorrectionEl = uifactory.addRadiosVertical("results.visibility.after.correction", "results.visibility.after.correction", formLayout,
				visibilityKeyValues.keys(), visibilityKeyValues.values());
		scoreVisibilityAfterCorrectionEl.setVisible(!selfAssessment);
		String defVisibility = qtiModule.isResultsVisibleAfterCorrectionWorkflow()
				? IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION : IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION;
		String visibility = modConfig.getStringValue(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION, defVisibility);
		if(visibilityKeyValues.containsKey(visibility)) {
			scoreVisibilityAfterCorrectionEl.select(visibility, true);
		}
		updateScoreVisibility();
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
		String showResultsDateDependent = modConfig.getStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS);  
		showResultsDateDependentEl = uifactory.addDropdownSingleselect("qti_showresult", "qti.form.results.onhomepage", formLayout, dateKeys, dateValues);
		showResultsDateDependentEl.select(showResultOnHomePage ? showResultsDateDependent : "no", true);
		showResultsDateDependentEl.addActionListener(FormEvent.ONCHANGE);
		showResultsDateDependentEl.setElementCssClass("o_sel_results_on_homepage");
	
		Date generalStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		generalStartDateElement = uifactory.addDateChooser("qti_form__general_start_date", "qti.form.date.start", null, formLayout);
		generalStartDateElement.setDateChooserTimeEnabled(true);
		generalStartDateElement.setDate(generalStartDate);
		generalStartDateElement.setMandatory(true);
		
		Date generalEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		generalEndDateElement = uifactory.addDateChooser("qti_form_general_end_date", "qti.form.date.end", null, formLayout);
		generalEndDateElement.setDateChooserTimeEnabled(true);
		generalEndDateElement.setDate(generalEndDate);
		
		Date failedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		failedStartDateElement = uifactory.addDateChooser("qti_form_failed_start_date", "qti.form.date.failed.start", null, formLayout);
		failedStartDateElement.setDateChooserTimeEnabled(true);
		failedStartDateElement.setDate(failedStartDate);
		failedStartDateElement.setMandatory(true);
		
		Date failedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		failedEndDateElement = uifactory.addDateChooser("qti_form_failed_end_date", "qti.form.date.end", null, formLayout);
		failedEndDateElement.setDateChooserTimeEnabled(true);
		failedEndDateElement.setDate(failedEndDate);
		
		Date passedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		passedStartDateElement = uifactory.addDateChooser("qti_form_passed_start_date", "qti.form.date.passed.start", null, formLayout);
		passedStartDateElement.setDateChooserTimeEnabled(true);
		passedStartDateElement.setDate(passedStartDate);
		passedStartDateElement.setMandatory(true);
		
		Date passedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		passedEndDateElement = uifactory.addDateChooser("qti_form_passed_end_date", "qti.form.date.end", null, formLayout);
		passedEndDateElement.setDateChooserTimeEnabled(true);
		passedEndDateElement.setDate(passedEndDate);
		
		QTI21AssessmentResultsOptions resultsOptions = deliveryOptions.getAssessmentResultsOptions();
		if(!QTI21Constants.QMD_ENTRY_SUMMARY_COMPACT.equals(modConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY))) {
			resultsOptions = QTI21AssessmentResultsOptions.parseString(modConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, QTI21Constants.QMD_ENTRY_SUMMARY_COMPACT));
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
		
		if (!wizard) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
		
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
				testLayout.setDirty(true);
			} else {
				testDateDependentEl.uncheckAll();
			}
			updateAssessmentModeVisibility();
		}
		super.event(ureq, source, event);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
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
		endTestDateElement.clearError();
		if (wizard && testDateDependentEl.isSelected(0) && endTestDateElement.getDate() == null) {
			endTestDateElement.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		generalStartDateElement.clearError();
		
		switch (showResultsDateDependentEl.getSelectedKey()) {			
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
			if(passedStartDateElement.getDate() == null) {
				passedStartDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(passedEndDateElement.getDate() != null && passedStartDateElement.getDate().after(passedEndDateElement.getDate())) {
				passedStartDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			} else if (passedStartDateElement.getDate() != null && passedEndDateElement.getDate() != null && passedStartDateElement.getDate().equals(passedEndDateElement.getDate())) {
				passedEndDateElement.setErrorKey("error.begin.end.same", null);
				allOk &= false;
			}
			
			if(failedStartDateElement.getDate() == null) {
				failedStartDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(failedEndDateElement.getDate() != null && failedStartDateElement.getDate().after(failedEndDateElement.getDate())) {
				failedStartDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			} else if (failedStartDateElement.getDate() != null && failedEndDateElement.getDate() != null && failedStartDateElement.getDate().equals(failedEndDateElement.getDate())) {
				failedEndDateElement.setErrorKey("error.begin.end.same", null);
				allOk &= false;
			}
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
			if(failedStartDateElement.getDate() == null) {
				failedStartDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(failedEndDateElement.getDate() != null && failedStartDateElement.getDate().after(failedEndDateElement.getDate())) {
				failedStartDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			} else if (failedStartDateElement.getDate() != null && failedEndDateElement.getDate() != null && failedStartDateElement.getDate().equals(failedEndDateElement.getDate())) {
				failedEndDateElement.setErrorKey("error.begin.end.same", null);
				allOk &= false;
			}
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
			if(passedStartDateElement.getDate() == null) {
				passedStartDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(passedEndDateElement.getDate() != null && passedStartDateElement.getDate().after(passedEndDateElement.getDate())) {
				passedStartDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			} else if (passedStartDateElement.getDate() != null && passedEndDateElement.getDate() != null && passedStartDateElement.getDate().equals(passedEndDateElement.getDate())) {
				passedEndDateElement.setErrorKey("error.begin.end.same", null);
				allOk &= false;
			}
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
			if(generalStartDateElement.getDate() == null) {
				generalStartDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(generalEndDateElement.getDate() != null && generalStartDateElement.getDate().after(generalEndDateElement.getDate())) {
				generalStartDateElement.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			} else if (generalEndDateElement.getDate() != null && generalStartDateElement.getDate() != null && generalStartDateElement.getDate().equals(generalEndDateElement.getDate())) {
				generalEndDateElement.setErrorKey("error.begin.end.same", null);
				allOk &= false;
			}
			break;
		default:
			break;
		}
		
		return allOk;
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showResultsOnFinishEl == source || showResultsDateDependentEl == source) {
			update();
		} else if(testDateDependentEl == source) {
			if(testDateDependentEl.isAtLeastSelected(1)) {
				confirmTestDates(ureq);
			} else {
				update();
				updateAssessmentModeVisibility();
			}
		} else if(correctionModeEl == source) {
			updateScoreVisibility();
		} else if (assessmentModeEl == source) {
			updateAssessmentModeVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateScoreVisibility() {
		String correctionMode = correctionModeEl.getSelectedKey();
		scoreVisibilityAfterCorrectionEl.setVisible(!selfAssessment
				&& (correctionMode.equals(IQEditController.CORRECTION_MANUAL) || correctionMode.equals(IQEditController.CORRECTION_GRADING)));
	}
	
	private void updateAssessmentModeVisibility() {
		if (assessmentModeEl != null) {
			boolean testDateVisible = testDateDependentEl.isAtLeastSelected(1);
			if (testDateVisible) {
				assessmentModeEl.setVisible(true);
				boolean assessmentModeEnabled = assessmentModeEl.isOneSelected()
						&& !assessmentModeEl.getSelectedKey().equals(ASSESSMENT_MODE_NONE);
				leadTimeEl.setVisible(assessmentModeEnabled);
				followupTimeEl.setVisible(assessmentModeEnabled);
			} else {
				assessmentModeEl.setVisible(false);
				leadTimeEl.setVisible(false);
				followupTimeEl.setVisible(false);
			}
		}
	}

	private void update() {
		assessmentResultsOnFinishEl.setVisible(showResultsOnFinishEl.isSelected(0) || !showResultsDateDependentEl.isSelected(0));
		
		resetDateChooser(generalStartDateElement);
		resetDateChooser(generalEndDateElement);
		resetDateChooser(failedStartDateElement);
		resetDateChooser(failedEndDateElement);
		resetDateChooser(passedStartDateElement);
		resetDateChooser(passedEndDateElement);
		
		resetDateChooser(startTestDateElement, testDateDependentEl);
		resetDateChooser(endTestDateElement, testDateDependentEl);

		switch (showResultsDateDependentEl.getSelectedKey()) {
		case "no":
			generalStartDateElement.setVisible(false);
			generalEndDateElement.setVisible(false);
			failedStartDateElement.setVisible(false);
			failedEndDateElement.setVisible(false);
			passedStartDateElement.setVisible(false);
			passedEndDateElement.setVisible(false);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
			generalStartDateElement.setVisible(false);
			generalEndDateElement.setVisible(false);
			failedStartDateElement.setVisible(true);
			failedStartDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE));
			failedEndDateElement.setVisible(true);
			failedEndDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE));
			passedStartDateElement.setVisible(true);
			passedStartDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE));
			passedEndDateElement.setVisible(true);
			passedEndDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE));
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
			generalStartDateElement.setVisible(false);
			generalEndDateElement.setVisible(false);
			failedStartDateElement.setVisible(true);
			failedStartDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE));
			failedEndDateElement.setVisible(true);
			failedEndDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE));
			passedStartDateElement.setVisible(false);
			passedEndDateElement.setVisible(false);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
			generalStartDateElement.setVisible(false);
			generalEndDateElement.setVisible(false);
			failedStartDateElement.setVisible(false);
			failedEndDateElement.setVisible(false);
			passedStartDateElement.setVisible(true);
			passedStartDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE));
			passedEndDateElement.setVisible(true);
			passedEndDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE));
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS:
			generalStartDateElement.setVisible(false);
			generalEndDateElement.setVisible(false);
			failedStartDateElement.setVisible(false);
			failedEndDateElement.setVisible(false);
			passedStartDateElement.setVisible(false);
			passedEndDateElement.setVisible(false);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
			generalStartDateElement.setVisible(true);
			generalStartDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE));
			generalEndDateElement.setVisible(true);
			generalEndDateElement.setDate(modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE));
			failedStartDateElement.setVisible(false);
			failedEndDateElement.setVisible(false);
			passedStartDateElement.setVisible(false);
			passedEndDateElement.setVisible(false);
			break;
		default:
			break;
		}
	}
	
	private void resetDateChooser(DateChooser dateElement, MultipleSelectionElement parentEl) {
		dateElement.clearError();
		if (!dateElement.isVisible()){
			dateElement.setValue("");
		}
		dateElement.setVisible(parentEl.isVisible() && parentEl.isSelected(0));
	}  
	
	private void resetDateChooser(DateChooser dateElement) {
		dateElement.clearError();
		if (!dateElement.isVisible()){
			dateElement.setValue("");
		}
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
			maxValue = QtiMaxScoreEstimator.estimateMaxScore(resolvedAssessmentTest);
			if(maxValue == null) {
				maxValue = testBuilder.getMaxScore();
			}
			cutValue = testBuilder.getCutValue();
			if(maxValue != null && "OpenOLAT".equals(assessmentTest.getToolName())) {
				minValue = 0d;
			}
		}

		minScoreEl.setValue(minValue == null ? "" : AssessmentHelper.getRoundedScore(minValue));
		minScoreEl.setVisible(!wizard && minValue != null);
		maxScoreEl.setValue(maxValue == null ? "" : AssessmentHelper.getRoundedScore(maxValue));
		maxScoreEl.setVisible(!wizard && maxValue != null);
		
		PassedType passedType = deliveryOptions.getPassedType(cutValue);
		String passedTypeValue;
		switch (passedType) {
		case cutValue:
			passedTypeValue = translate("score.passed.cut.value", new String[] { AssessmentHelper.getRoundedScore(cutValue) });
			break;
		case manually:
			passedTypeValue = translate("score.passed.manually");
			break;
		default:
			passedTypeValue = translate("score.passed.none");
			break;
		}
		passedTypeEl.setValue(passedTypeValue);
		
		update();
	}
	
	private void confirmTestDates(UserRequest ureq) {
		String title = translate("qti.form.test.date");
		String text = translate("qti.form.test.date.confirm");
		confirmTestDateCtrl = activateOkCancelDialog(ureq, title, text, confirmTestDateCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateModuleConfig();
		if (assessmentModeDefaults != null) {
			updateAssessmentModeDefaults();
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public void updateModuleConfig() {
		boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isVisible() && ignoreInCourseAssessmentEl.isAtLeastSelected(1);
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, testDateDependentEl.isSelected(0));
		
		modConfig.setDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE, startTestDateElement.getDate());
		modConfig.setDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE, endTestDateElement.getDate());
		
		if(correctionModeEl.isOneSelected() && !selfAssessment) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}
		if(scoreVisibilityAfterCorrectionEl.isOneSelected() && scoreVisibilityAfterCorrectionEl.isVisible()) {
			modConfig.setStringValue(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION, scoreVisibilityAfterCorrectionEl.getSelectedKey());
		} else {
			modConfig.remove(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION);
		}

		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLESCOREINFO, scoreInfo.isSelected(0));
		modConfig.setStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, showResultsDateDependentEl.getSelectedKey());
		
		switch (showResultsDateDependentEl.getSelectedKey()) {
		case "no":
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS:
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE, failedStartDateElement.getDate());
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE, failedEndDateElement.getDate());
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE, passedStartDateElement.getDate());
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE, passedEndDateElement.getDate());
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE, failedStartDateElement.getDate());
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE, failedEndDateElement.getDate());
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE, passedStartDateElement.getDate());
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE, passedEndDateElement.getDate());
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			modConfig.remove(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE, generalStartDateElement.getDate());
			modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE, generalEndDateElement.getDate());
			break;
		default:
			break;
		}
		
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, !showResultsDateDependentEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_FINISH, showResultsOnFinishEl.isSelected(0));
		
		if(showResultsOnFinishEl.isSelected(0) || !showResultsDateDependentEl.isSelected(0)) {
			if(assessmentResultsOnFinishEl.isAtLeastSelected(1)) {
				String options = QTI21AssessmentResultsOptions.toString(assessmentResultsOnFinishEl.getSelectedKeys());
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, options);
			} else {
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, QTI21Constants.QMD_ENTRY_SUMMARY_NONE);
			}
		} else {
			modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, QTI21Constants.QMD_ENTRY_SUMMARY_NONE);
		}
	}
	
	private void updateAssessmentModeDefaults() {
		if (assessmentModeEl != null && assessmentModeEl.isVisible()) {
			
			boolean enabled = assessmentModeEl.isOneSelected()
					&& !assessmentModeEl.getSelectedKey().equals(ASSESSMENT_MODE_NONE);
			assessmentModeDefaults.setEnabled(enabled);
			
			boolean autoBeginEnd = assessmentModeEl.isOneSelected()
					&& assessmentModeEl.getSelectedKey().equals(ASSESSMENT_MODE_AUTO);
			assessmentModeDefaults.setManualBeginEnd(!autoBeginEnd);
			
			Date start = modConfig.getDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE);
			assessmentModeDefaults.setBegin(start);
			Date end = modConfig.getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE);
			assessmentModeDefaults.setEnd(end);
			
			int leadTime = leadTimeEl.getIntValue();
			assessmentModeDefaults.setLeadTime(leadTime);
			int followupTime = followupTimeEl.getIntValue();
			assessmentModeDefaults.setFollowUpTime(followupTime);
		}
	}

}
