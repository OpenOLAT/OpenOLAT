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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.ui.DueDateConfigFormItem;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
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
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
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
	private static final String ASSESSMENT_MODE_AUTO = "auto";
	private static final String ASSESSMENT_MODE_MANUAL = "manual";
	private static final String ASSESSMENT_MODE_NONE = "none";
	private SelectionValues relativeToDatesKV;
	
	private SingleSelection correctionModeEl;
	private SingleSelection scoreVisibilityAfterCorrectionEl;
	private SingleSelection showResultsDateDependentEl;
	private MultipleSelectionElement scoreInfo;
	private MultipleSelectionElement relativeDatesEl;
	private MultipleSelectionElement testDateDependentEl;
	private DueDateConfigFormItem testStartDateEl;
	private DueDateConfigFormItem testEndDateEl;
	private TextElement assessmentModeNameEl;
	private SingleSelection assessmentModeEl;
	private IntegerElement leadTimeEl;
	private IntegerElement followupTimeEl;
	private StaticTextElement minScoreEl;
	private StaticTextElement maxScoreEl;
	private MultipleSelectionElement gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private StaticTextElement passedGradeEl;
	private StaticTextElement passedTypeEl;
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement assessmentResultsOnFinishEl;
	private DueDateConfigFormItem resultStartDateEl;
	private DueDateConfigFormItem resultEndDateEl;
	private DueDateConfigFormItem resultFailedStartDateEl;
	private DueDateConfigFormItem resultFailedEndDateEl;
	private DueDateConfigFormItem resultPassedStartDateEl;
	private DueDateConfigFormItem resultPassedEndDateEl;
	private FormLayoutContainer reportLayout;
	private FormLayoutContainer testLayout;
	
	private final boolean selfAssessment;
	private final boolean needManualCorrection;
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final ModuleConfiguration modConfig;
	private final boolean ignoreInCourseAssessmentAvailable;
	private final QTI21DeliveryOptions deliveryOptions;
	private final boolean wizard;
	private final IQTESTCourseNodeContext assessmentModeDefaults;
	private GradeScale gradeScale;
	
	private CloseableModalController cmc;
	private GradeScaleEditController gradeScaleCtrl;
	private DialogBoxController confirmGradeCtrl;
	private DialogBoxController confirmTestDateCtrl;

	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private DueDateService dueDateService;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;
	
	public QTI21EditForm(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, NodeAccessType nodeAccessType,
			QTI21DeliveryOptions deliveryOptions, boolean needManualCorrection, boolean selfAssessment) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(getTranslator(), DueDateConfigFormItem.class, getLocale()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.modConfig = courseNode.getModuleConfiguration();
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		this.deliveryOptions = (deliveryOptions == null ? new QTI21DeliveryOptions() : deliveryOptions);
		this.needManualCorrection = needManualCorrection;
		this.selfAssessment = selfAssessment;
		this.wizard = false;
		this.assessmentModeDefaults = null;
		initDateValues();
		initRelativeToDateKV();
		initForm(ureq);
		updateShowResultsWarning();
	}

	public QTI21EditForm(UserRequest ureq, WindowControl wControl, Form rootForm, RepositoryEntry courseEntry,
			IQTESTCourseNodeContext context, NodeAccessType nodeAccessType, boolean needManualCorrection,
			boolean selfAssessment) {
		super(ureq, wControl, LAYOUT_BAREBONE, null, rootForm);
		setTranslator(Util.createPackageTranslator(getTranslator(), DueDateConfigFormItem.class, getLocale()));
		this.courseEntry = courseEntry;
		this.courseNode = context.getCourseNode();
		this.modConfig = context.getModuleConfig();
		this.assessmentModeDefaults = context;
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		this.deliveryOptions = new QTI21DeliveryOptions();
		this.needManualCorrection = needManualCorrection;
		this.selfAssessment = selfAssessment;
		this.wizard = true;
		initDateValues();
		initRelativeToDateKV();
		initForm(ureq);
		updateShowResultsWarning();
	}

	private void initDateValues() {
		for (int i = 0; i < dateKeys.length; i++) {
			dateValues[i] = translate(dateBase + dateKeys[i]);
		}
	}
	
	private void initRelativeToDateKV() {
		relativeToDatesKV = new SelectionValues();
		List<String> relativeToDates = dueDateService.getCourseRelativeToDateTypes(courseEntry);
		DueDateConfigFormatter.create(getLocale()).addCourseRelativeToDateTypes(relativeToDatesKV, relativeToDates);
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
		
		if (gradeModule.isEnabled() && !wizard && !selfAssessment) {
			gradeEnabledEl = uifactory.addCheckboxesHorizontal("node.grade.enabled", formLayout, new String[]{"xx"}, new String[]{null});
			gradeEnabledEl.addActionListener(FormEvent.ONCLICK);
			boolean gradeEnabled = modConfig.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeEnabledEl.select("xx", gradeEnabled);
			
			SelectionValues autoSV = new SelectionValues();
			autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
			autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
			gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
			gradeAutoEl.select(Boolean.toString(modConfig.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)), true);
			
			gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
			
			gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
			gradeScaleButtonsCont.setRootForm(mainForm);
			formLayout.add(gradeScaleButtonsCont);
			gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
			
			passedGradeEl = uifactory.addStaticTextElement("score.passed.grade", "score.passed", translate("score.passed.grade"), formLayout);
		}
		
		passedTypeEl = uifactory.addStaticTextElement("score.passed", "", formLayout);
		passedTypeEl.setVisible(!wizard);
		
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", formLayout,
				new String[] { "xx" }, new String[] { null });
		boolean ignoreInCourseAssessment = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		ignoreInCourseAssessmentEl.select(ignoreInCourseAssessmentEl.getKey(0), ignoreInCourseAssessment);
		ignoreInCourseAssessmentEl.setVisible(!wizard && ignoreInCourseAssessmentAvailable);
		
		relativeDatesEl = uifactory.addCheckboxesHorizontal("relative.dates", "relative.dates", formLayout, onKeys, onValues);
		relativeDatesEl.addActionListener(FormEvent.ONCHANGE);
		boolean useRelativeDates = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_RELATIVE_DATES);
		relativeDatesEl.select(onKeys[0], useRelativeDates);
		
		boolean testDateDependent = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST);
		testDateDependentEl = uifactory.addCheckboxesHorizontal("qti_datetest", "qti.form.test.date", formLayout, new String[]{"xx"}, new String[]{null});
		testDateDependentEl.setElementCssClass("o_qti_21_datetest");
		testDateDependentEl.select("xx", testDateDependent);
		testDateDependentEl.setHelpTextKey("qti.form.test.date.help", null);
		testDateDependentEl.addActionListener(FormEvent.ONCLICK);
		
		testStartDateEl = DueDateConfigFormItem.create("qti.form.date.start", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_START_TEST_DATE));
		testStartDateEl.setLabel("qti.form.date.start", null);
		testStartDateEl.setElementCssClass("o_qti_21_datetest_start");
		testStartDateEl.setMandatory(true);
		formLayout.add(testStartDateEl);
	
		testEndDateEl = DueDateConfigFormItem.create("qti.form.date.end", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_END_TEST_DATE));
		testEndDateEl.setLabel("qti.form.date.end", null);
		testEndDateEl.setElementCssClass("o_qti_21_datetest_end");
		testEndDateEl.setMandatory(wizard);
		testStartDateEl.setPushDateValueTo(testEndDateEl);
		formLayout.add(testEndDateEl);
		
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
			
			assessmentModeNameEl = uifactory.addTextElement("assessment.mode.name", "assessment.mode.name", 255, assessmentModeDefaults.getName(), formLayout);
			assessmentModeNameEl.setMandatory(true);
			
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
		correctionModeEl.setHelpUrlForManualPage("manual_user/tests/Tests_at_course_level/#correction");
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
		
		SelectionValues visibilitySV = new SelectionValues();
		visibilitySV.add(new SelectionValue(IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION,
				translate("results.user.visibility.hidden"), translate("results.user.visibility.hidden.desc"),
				"o_icon o_icon_results_hidden", null, true));
		visibilitySV.add(new SelectionValue(IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION,
				translate("results.user.visibility.visible"), translate("results.user.visibility.visible.desc"),
				"o_icon o_icon_results_visible", null, true));
		scoreVisibilityAfterCorrectionEl = uifactory.addCardSingleSelectHorizontal("results.user.visibility",
				formLayout, visibilitySV.keys(), visibilitySV.values(), visibilitySV.descriptions(), visibilitySV.icons());
		String defVisibility = qtiModule.isResultsVisibleAfterCorrectionWorkflow()
				? IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION : IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION;
		String visibility = modConfig.getStringValue(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION, defVisibility);
		if(scoreVisibilityAfterCorrectionEl.containsKey(visibility)) {
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
		
		resultStartDateEl = DueDateConfigFormItem.create("qti.form.date.general.start", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_RESULTS_START_DATE));
		resultStartDateEl.setLabel("qti.form.date.start", null);
		resultStartDateEl.setMandatory(true);
		formLayout.add(resultStartDateEl);
	
		resultEndDateEl = DueDateConfigFormItem.create("qti.form.date.general.end", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_RESULTS_END_DATE));
		resultEndDateEl.setLabel("qti.form.date.end", null);
		formLayout.add(resultEndDateEl);
		
		resultFailedStartDateEl = DueDateConfigFormItem.create("qti.form.date.failed.start", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE));
		resultFailedStartDateEl.setLabel("qti.form.date.failed.start", null);
		resultFailedStartDateEl.setMandatory(true);
		formLayout.add(resultFailedStartDateEl);
		
		resultFailedEndDateEl = DueDateConfigFormItem.create("qti.form.date.failed.end", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE));
		resultFailedEndDateEl.setLabel("qti.form.date.end", null);
		formLayout.add(resultFailedEndDateEl);
		
		resultPassedStartDateEl = DueDateConfigFormItem.create("qti.form.date.passed.start", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE));
		resultPassedStartDateEl.setLabel("qti.form.date.passed.start", null);
		resultPassedStartDateEl.setMandatory(true);
		formLayout.add(resultPassedStartDateEl);
		
		resultPassedEndDateEl = DueDateConfigFormItem.create("qti.form.date.passed.end", relativeToDatesKV,
				relativeDatesEl.isAtLeastSelected(1), courseNode.getDueDateConfig(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE));
		resultPassedEndDateEl.setLabel("qti.form.date.end", null);
		formLayout.add(resultPassedEndDateEl);
		
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
		assessmentResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		assessmentResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results_options");
		assessmentResultsOnFinishEl.setHelpText(translate("qti.form.summary.help"));
		assessmentResultsOnFinishEl.setHelpUrlForManualPage("manual_user/tests/Test_settings/#results");
		
		if (!wizard) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
		
		//setup the values
		update();
		
		updateAssessmentResultsOnFinish(resultsOptions);
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
		} else if (confirmGradeCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				markDirty();
			} else {
				gradeEnabledEl.select(gradeEnabledEl.getKey(0), !gradeEnabledEl.isAtLeastSelected(1));
			}
			flc.setDirty(true);
			updateGradeUI();
		} else if (gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
				updateGradeUI();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
			markDirty();
		} else if (cmc == source) {
			cleanUp();
			markDirty();
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
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateDueDateConfig(testStartDateEl, testEndDateEl);
		allOk &= validateDueDateConfig(resultStartDateEl, resultEndDateEl);
		allOk &= validateDueDateConfig(resultFailedStartDateEl, resultFailedEndDateEl);
		allOk &= validateDueDateConfig(resultPassedStartDateEl, resultPassedEndDateEl);
		
		if (assessmentModeNameEl != null) {
			assessmentModeNameEl.clearError();
			if (assessmentModeNameEl.isVisible() && !StringHelper.containsNonWhitespace(assessmentModeNameEl.getValue())) {
				assessmentModeNameEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateDueDateConfig(DueDateConfigFormItem startEl, DueDateConfigFormItem endEl) {
		boolean allOk = true;
		
		List<ValidationStatus> validation = new ArrayList<>(1);
		startEl.clearError();
		endEl.clearError();
		if(startEl.isVisible()) {
			startEl.validate(validation);
			if (!validation.isEmpty()) {
				allOk &= false;
			}
			if (startEl.isMandatory() && !startEl.hasError() && !DueDateConfig.isDueDate(startEl.getDueDateConfig())) {
				startEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			
			validation = new ArrayList<>(1);
			endEl.validate(validation);
			if (!validation.isEmpty()) {
				allOk &= false;
			}
			
			if (endEl.isMandatory() && !endEl.hasError() && !DueDateConfig.isDueDate(endEl.getDueDateConfig())) {
				endEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if (!startEl.hasError() && !endEl.hasError()) {
				DueDateConfig startDateConfig = startEl.getDueDateConfig();
				DueDateConfig endDateConfig = endEl.getDueDateConfig();
				if (startDateConfig.getAbsoluteDate() != null && endDateConfig.getAbsoluteDate() != null) {
					if (endDateConfig.getAbsoluteDate().before(startDateConfig.getAbsoluteDate())) {
						endEl.setErrorKey("error.begin.after.end", null);
						allOk &= false;
					} else if (endDateConfig.getAbsoluteDate().equals(startDateConfig.getAbsoluteDate())) {
						endEl.setErrorKey("error.begin.end.same", null);
						allOk &= false;
					}
				}
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc != gradeScaleEditLink) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showResultsOnFinishEl == source || showResultsDateDependentEl == source || relativeDatesEl == source) {
			update();
			updateShowResultsWarning();
			updateAssessmentModeVisibility();
			markDirty();
		} else if(testDateDependentEl == source) {
			if(testDateDependentEl.isAtLeastSelected(1)) {
				confirmTestDates(ureq);
			} else {
				update();
				updateAssessmentModeVisibility();
				markDirty();
			}
 		} else if(correctionModeEl == source) {
			updateScoreVisibility();
			updateShowResultsWarning();
			markDirty();
		} else if (assessmentModeEl == source) {
			updateAssessmentModeVisibility();
			updateShowResultsWarning();
			markDirty();
		} else if(assessmentResultsOnFinishEl == source) {
			updateShowResultsWarning();
			markDirty();
		} else if (source == gradeEnabledEl) {
			doConfirmGrades(ureq);
		} else if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateShowResultsWarning() {
		if(showResultsOnFinishEl.isAtLeastSelected(1)
				&& assessmentResultsOnFinishEl.isAtLeastSelected(1)
				&& !correctionModeKeys[0].equals(correctionModeEl.getSelectedKey())) {
			reportLayout.setFormWarning(translate("warning.show.results"));
		} else {
			reportLayout.setFormWarning(null);
		}
	}
	
	private void updateScoreVisibility() {
		String correctionMode = correctionModeEl.getSelectedKey();
		scoreVisibilityAfterCorrectionEl.setVisible(!selfAssessment
				&& (correctionMode.equals(IQEditController.CORRECTION_MANUAL) || correctionMode.equals(IQEditController.CORRECTION_GRADING)));
	}
	
	private void updateAssessmentModeVisibility() {
		if (assessmentModeEl != null) {
			boolean testDateVisible = testDateDependentEl.isAtLeastSelected(1);
			boolean absoluteDates = !relativeDatesEl.isAtLeastSelected(1);
			if (testDateVisible && absoluteDates) {
				assessmentModeEl.setVisible(true);
				boolean assessmentModeEnabled = assessmentModeEl.isOneSelected()
						&& !assessmentModeEl.getSelectedKey().equals(ASSESSMENT_MODE_NONE);
				assessmentModeNameEl.setVisible(assessmentModeEnabled);
				leadTimeEl.setVisible(assessmentModeEnabled);
				followupTimeEl.setVisible(assessmentModeEnabled);
				testEndDateEl.setMandatory(true);
			} else {
				assessmentModeEl.setVisible(false);
				assessmentModeNameEl.setVisible(false);
				leadTimeEl.setVisible(false);
				followupTimeEl.setVisible(false);
				testEndDateEl.setMandatory(false);
			}
		}
	}

	private void update() {
		boolean testDateDependend = testDateDependentEl.isVisible() && testDateDependentEl.isSelected(0);
		testStartDateEl.setVisible(testDateDependend);
		testEndDateEl.setVisible(testDateDependend);
		
		assessmentResultsOnFinishEl.setVisible(showResultsOnFinishEl.isSelected(0) || !showResultsDateDependentEl.isSelected(0));
		switch (showResultsDateDependentEl.getSelectedKey()) {
		case "no":
			resultStartDateEl.setVisible(false);
			resultEndDateEl.setVisible(false);
			resultFailedStartDateEl.setVisible(false);
			resultFailedEndDateEl.setVisible(false);
			resultPassedStartDateEl.setVisible(false);
			resultPassedEndDateEl.setVisible(false);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
			resultStartDateEl.setVisible(false);
			resultEndDateEl.setVisible(false);
			resultFailedStartDateEl.setVisible(true);
			resultFailedEndDateEl.setVisible(true);
			resultPassedStartDateEl.setVisible(true);
			resultPassedEndDateEl.setVisible(true);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
			resultStartDateEl.setVisible(false);
			resultEndDateEl.setVisible(false);
			resultFailedStartDateEl.setVisible(true);
			resultFailedEndDateEl.setVisible(true);
			resultPassedStartDateEl.setVisible(false);
			resultPassedEndDateEl.setVisible(false);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
			resultStartDateEl.setVisible(false);
			resultEndDateEl.setVisible(false);
			resultFailedStartDateEl.setVisible(false);
			resultFailedEndDateEl.setVisible(false);
			resultPassedStartDateEl.setVisible(true);
			resultPassedEndDateEl.setVisible(true);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS:
			resultStartDateEl.setVisible(false);
			resultEndDateEl.setVisible(false);
			resultFailedStartDateEl.setVisible(false);
			resultFailedEndDateEl.setVisible(false);
			resultPassedStartDateEl.setVisible(false);
			resultPassedEndDateEl.setVisible(false);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
			resultStartDateEl.setVisible(true);
			resultEndDateEl.setVisible(true);
			resultFailedStartDateEl.setVisible(false);
			resultFailedEndDateEl.setVisible(false);
			resultPassedStartDateEl.setVisible(false);
			resultPassedEndDateEl.setVisible(false);
			break;
		default:
			break;
		}
		
		boolean relativeDates = relativeDatesEl.isAtLeastSelected(1);
		testStartDateEl.setRelative(relativeDates);
		testEndDateEl.setRelative(relativeDates);
		resultStartDateEl.setRelative(relativeDates);
		resultEndDateEl.setRelative(relativeDates);
		resultFailedStartDateEl.setRelative(relativeDates);
		resultPassedStartDateEl.setRelative(relativeDates);
		testStartDateEl.setRelative(relativeDates);
		resultPassedEndDateEl.setRelative(relativeDates);
		
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
			passedTypeValue = translate("score.passed.cut.value", AssessmentHelper.getRoundedScore(cutValue));
			break;
		case manually:
			passedTypeValue = translate("score.passed.manually");
			break;
		default:
			passedTypeValue = translate("score.passed.none");
			break;
		}
		passedTypeEl.setValue(passedTypeValue);
		
		updateGradeUI();
		update();
	}
	
	private void updateGradeUI() {
		if (gradeEnabledEl != null) {
			boolean hasScore = minScoreEl.isVisible();
			gradeEnabledEl.setVisible(hasScore);
			gradeAutoEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
			String gradeScaleText = gradeScale == null
					? translate("node.grade.scale.not.available")
					: GradeUIFactory.translateGradeSystemName(getTranslator(), gradeScale.getGradeSystem());
			gradeScaleEl.setValue(gradeScaleText);
			boolean hasGrade = gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1);
			gradeScaleEl.setVisible(hasGrade);
			gradeScaleButtonsCont.setVisible(hasGrade);
			
			GradeScoreRange minRange = gradeService.getMinPassedGradeScoreRange(gradeScale, getLocale());
			passedGradeEl.setVisible(hasGrade && minRange != null);
			passedGradeEl.setValue(GradeUIFactory.translateMinPassed(getTranslator(), minRange));
			
			passedTypeEl.setVisible(!hasGrade);
		}
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
		if (gradeEnabledEl != null) {
			modConfig.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isAtLeastSelected(1));
			modConfig.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.valueOf(gradeAutoEl.getSelectedKey()).booleanValue());
		} else {
			modConfig.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			modConfig.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		}
		
		boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isVisible() && ignoreInCourseAssessmentEl.isAtLeastSelected(1);
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, testDateDependentEl.isSelected(0));
		
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_RELATIVE_DATES, relativeDatesEl.isAtLeastSelected(1));

		DueDateConfig startTestConfig = testStartDateEl.isVisible()? testStartDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE, startTestConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_START_TEST_DATE_REL, startTestConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_START_TEST_DATE_REL_TO, startTestConfig.getRelativeToType());

		DueDateConfig endTestConfig = testEndDateEl.isVisible()? testEndDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE, endTestConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_END_TEST_DATE_REL, endTestConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_END_TEST_DATE_REL_TO, endTestConfig.getRelativeToType());
		
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
		
		DueDateConfig generalStartConfig = resultStartDateEl.isVisible()? resultStartDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE, generalStartConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE_REL, generalStartConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE_REL_TO, generalStartConfig.getRelativeToType());
		
		DueDateConfig generalEndConfig = resultEndDateEl.isVisible()? resultEndDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE, generalEndConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE_REL, generalEndConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE_REL_TO, generalEndConfig.getRelativeToType());
		
		DueDateConfig failedStartConfig = resultFailedStartDateEl.isVisible()? resultFailedStartDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE, failedStartConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE_REL, failedStartConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE_REL_TO, failedStartConfig.getRelativeToType());
		
		DueDateConfig failedEndConfig = resultFailedEndDateEl.isVisible()? resultFailedEndDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE, failedEndConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE_REL, failedEndConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE_REL_TO, failedEndConfig.getRelativeToType());
		
		DueDateConfig passedStartConfig = resultPassedStartDateEl.isVisible()? resultPassedStartDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE, passedStartConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE_REL, passedStartConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE_REL_TO, passedStartConfig.getRelativeToType());
		
		DueDateConfig passedEndConfig = resultPassedEndDateEl.isVisible()? resultPassedEndDateEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE, passedEndConfig.getAbsoluteDate());
		modConfig.setIntValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE_REL, passedEndConfig.getNumOfDays());
		modConfig.setStringValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE_REL_TO, passedEndConfig.getRelativeToType());

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
			
			assessmentModeDefaults.setName(assessmentModeNameEl.getValue());
			
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
	
	private void doConfirmGrades(UserRequest ureq) {
		if (assessmentService.getScoreCount(courseEntry, courseNode.getIdent()) > 0) {
			String title = translate("node.grade.enabled");
			String text = translate("qti.form.grade.confirm.text");
			confirmGradeCtrl = activateOkCancelDialog(ureq, title, text, confirmGradeCtrl);
		} else {
			updateGradeUI();
			markDirty();
		}
	}

	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		if (!minScoreEl.isVisible() || !maxScoreEl.isVisible()) {
			showWarning("error.no.grade.no.score");
			return;
		}
		
		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), courseEntry, courseNode.getIdent(),
				Float.valueOf(minScoreEl.getValue()), Float.valueOf(maxScoreEl.getValue()), true);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
