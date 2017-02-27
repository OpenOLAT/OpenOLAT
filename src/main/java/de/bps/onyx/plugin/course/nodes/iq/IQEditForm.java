/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
* <p>
*/
package de.bps.onyx.plugin.course.nodes.iq;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.iq.IQ12EditForm;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.OnyxModule.PlayerTemplate;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;

/**
 * Test configuration form. Used for configuring Test, Self-test, and Questionnaire(aka Survey).
 * <p>
 * Initial Date: Mar 3, 2004
 * 
 * @author Mike Stock
 */
public class IQEditForm extends FormBasicController {

	//<OLATCE-982>
	private static final String ALLOW = "allow";
	private MultipleSelectionElement allowShowSolutionBox;
	//</OLATCE-982>
	//<OLATCE-2009>
	private MultipleSelectionElement allowSuspensionBox;
	//</OLATCE-2009>
	private SelectionElement enableMenu;
	private SelectionElement displayMenu;
	private SelectionElement displayScoreProgress;
	private SelectionElement displayQuestionProgress;
	private SelectionElement displayQuestionTitle;
	private SingleSelection sequence;
	private SelectionElement enableCancel;
	private SelectionElement enableSuspend;
	private SingleSelection summary;
	private SelectionElement limitAttempts;
	private IntegerElement attempts;
	private SingleSelection menuRenderOptions;
	private SelectionElement scoreInfo;
	private SelectionElement showResultsDateDependentButton;
	private DateChooser startDateElement;
	private DateChooser endDateElement;
	private SelectionElement showResultsAfterFinishTest;
	private SelectionElement showResultsOnHomePage;
	private FormLayoutContainer variablesCont;

	private ModuleConfiguration modConfig;

	private String configKeyType;

	private SingleSelection template;
	private TextElement cutValue;

	private final CourseNode courseNode;
	private boolean isAssessment, isSelfTest, isSurvey;
	//<OLATCE-1012>
	private RepositoryEntry repoEntry;
	private final static OLog log = Tracing.createLoggerFor(IQEditForm.class);
	//</OLATCE-1012>

	/**
	 * Constructor for the qti configuration form
	 * 
	 * @param ureq
	 * @param wControl
	 * @param modConfig
	 */
	// <OLATCE-654>
	public IQEditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig, CourseNode courseNode, RepositoryEntry repoEntry) {
	// </OLATCE-654>
		super (ureq, wControl);

		//<OLATCE-1012>		
		this.repoEntry = repoEntry;
		//</OLATCE-1012>
		Translator translator = Util.createPackageTranslator(IQ12EditForm.class, getLocale(), getTranslator());
		setTranslator(translator);
		this.courseNode = courseNode;
		this.modConfig = modConfig;

		configKeyType = (String) modConfig.get(IQEditController.CONFIG_KEY_TYPE);

		isAssessment = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
		isSelfTest = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF);
		isSurvey = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);

		initForm(ureq);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		startDateElement.clearError();
		endDateElement.clearError();

		if (startDateElement.isVisible()) {
			if (startDateElement.isEmpty()) {
				startDateElement.setErrorKey("qti.form.date.start.error.mandatory", null);
				return false;
			} else {
				if (startDateElement.getDate() == null) {
					startDateElement.setErrorKey("qti.form.date.error.format", null);
					return false;
				}
			}

			if (!endDateElement.isEmpty()) {
				if (endDateElement.getDate() == null) {
					endDateElement.setErrorKey("qti.form.date.error.format", null);
					return false;
				}

				if (endDateElement.getDate().before(startDateElement.getDate())) {
					endDateElement.setErrorKey("qti.form.date.error.endbeforebegin", null);
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		modConfig.set(IQEditController.CONFIG_KEY_TEMPLATE, getTemplate());
		if (!isSurvey) {
			modConfig.set(IQEditController.CONFIG_KEY_ATTEMPTS, getAttempts());
			modConfig.set(IQEditController.CONFIG_KEY_CUTVALUE, getCutValue());
		}
		modConfig.set(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, new Boolean(isShowResultsDateDependent()));
		modConfig.set(IQEditController.CONFIG_KEY_RESULTS_START_DATE, getShowResultsStartDate());
		modConfig.set(IQEditController.CONFIG_KEY_RESULTS_END_DATE, getShowResultsEndDate());
		modConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, isShowResultsOnHomePage());
		//<OLATCE-982>
		modConfig.set(IQEditController.CONFIG_KEY_ALLOW_SHOW_SOLUTION, allowShowSolution());
		//</OLATCE-982>
		//<OLATCE-2009>
		modConfig.set(IQEditController.CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED, allowSuspension());
		//</OLATCE-2009>
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		String variablePages = velocity_root + "/variables.html";
		variablesCont = FormLayoutContainer.createCustomFormLayout("variables", getTranslator(), variablePages);
		variablesCont.setLabel("outcomes.title", null);
		variablesCont.setVisible(false);
		variablesCont.setRootForm(mainForm);
		formLayout.add(variablesCont);

		limitAttempts = uifactory.addCheckboxesVertical("limitAttempts", "qti.form.limit.attempts", formLayout, new String[] { "xx" }, new String[] { null }, 1);

		Integer confAttempts = (Integer) modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS);
		if (confAttempts == null) {
			confAttempts = new Integer(0);
		}
		attempts = uifactory.addIntegerElement("qti.form.attempts", confAttempts, formLayout);
		attempts.setDisplaySize(2);
		attempts.setMinValueCheck(1, null);
		attempts.setMaxValueCheck(20, null);

		// Only assessments have a limitation on number of attempts
		if (isAssessment) {
			limitAttempts.select("xx", confAttempts > 0);
			limitAttempts.addActionListener(FormEvent.ONCLICK);
		} else {
			limitAttempts.select("xx", false);
			limitAttempts.setVisible(false);
			attempts.setVisible(false);
		}

		//<OLATCE-982>
		Boolean confAllowShowSolution = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_SHOW_SOLUTION);
		String[] allowShowSolution=new String[]{ALLOW};
		String[] valuesShowSolution = new String[]{""};
		//Surveys do not have a solution
		if(!isSurvey){
			allowShowSolutionBox = uifactory.addCheckboxesVertical("allowShowSolution", "qti.form.allowShowSolution", formLayout, allowShowSolution, valuesShowSolution, 1);
			allowShowSolutionBox.addActionListener(FormEvent.ONCLICK);
			if(confAllowShowSolution!=null){
				allowShowSolutionBox.select(ALLOW, confAllowShowSolution);
			} else if (isSelfTest){
				allowShowSolutionBox.select(ALLOW, true);
			}
		}
		//</OLATCE-982>
		//<OLATCE-2009>
		Boolean confAllowSuspension = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED);
		String[] allowSuspension = new String[] { ALLOW };
		String[] valuesSuspesion = new String[] { "" };
		allowSuspensionBox = uifactory.addCheckboxesHorizontal("allowSuspension", "qti.form.allowSuspension", formLayout, allowSuspension,
				valuesSuspesion);
		allowSuspensionBox.addActionListener(FormEvent.ONCLICK);
		if (confAllowSuspension != null) {
			allowSuspensionBox.select(ALLOW, confAllowSuspension);
		} else if (isSelfTest) {
			allowSuspensionBox.select(ALLOW, false);
		}
		//</OLATCE-2009>
		//select onyx template
		String[] values = new String[OnyxModule.PLAYERTEMPLATES.size()];
		String[] keys = new String[OnyxModule.PLAYERTEMPLATES.size()];

		int i = 0;
		for (PlayerTemplate pt : OnyxModule.PLAYERTEMPLATES) {
			keys[i] = pt.id;
			values[i] = getTranslator().translate(pt.i18nkey);
			++i;
		}
		template = uifactory.addDropdownSingleselect("qti.form.onyx.template", formLayout, keys, values, null);
		try {
			if (modConfig.get(IQEditController.CONFIG_KEY_TEMPLATE) != null) {
				template.select(modConfig.get(IQEditController.CONFIG_KEY_TEMPLATE).toString(), true);
			}
		} catch (RuntimeException e) {
			log.warn("Template not found", e);
		}

		//<OLATCE-1342>
		if(repoEntry == null || repoEntry.getOlatResource() == null){
			getWindowControl().setWarning(translate("qti.form.onyx.nocontent"));
		} else {
		//the cutvalue
		if (!isSurvey) {
			cutValue = uifactory.addTextElement("qti.form.onyx.cutvalue", "qti.form.onyx.cutvalue", 20, "", formLayout);
			if (modConfig.get(IQEditController.CONFIG_KEY_CUTVALUE) != null) {
				cutValue.setValue(modConfig.get(IQEditController.CONFIG_KEY_CUTVALUE).toString());
			} else {
				cutValue.setValue("");
			}
			//<OLATCE-1012>				
	
			try {
				OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
				Map<String, String> outcomes = onyxReporter.getPossibleOutcomeVariables(repoEntry);
				if(outcomes.containsKey("PASS")){
					uifactory.addStaticTextElement("qti.form.onyx.cutvalue.passed.overwrite", null, translate("qti.form.onyx.cutvalue.passed.overwrite"), formLayout);
				}
			} catch (OnyxReporterException e) {
				log.warn("Unable to get outcome variables for the test!", e);
			}

			//</OLATCE-1012>
		}
			//</OLATCE-1342>
		}

		// migration: check if old tests have no summary
		String configuredSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
		boolean noSummary = configuredSummary != null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE) ? true : false;

		Boolean showResultOnHomePage = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		boolean confEnableShowResultOnHomePage = (showResultOnHomePage != null) ? showResultOnHomePage.booleanValue() : false;
		confEnableShowResultOnHomePage = !noSummary && confEnableShowResultOnHomePage;
		showResultsOnHomePage = uifactory.addCheckboxesVertical("qti_enableResultsOnHomePage", "qti.form.results.onhomepage", formLayout, new String[] { "xx" },
				new String[] { null }, 1);
		showResultsOnHomePage.select("xx", confEnableShowResultOnHomePage);
		showResultsOnHomePage.addActionListener(FormEvent.ONCLICK);
		showResultsOnHomePage.setVisible(!isSurvey);

		Boolean showResultsActive = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		boolean showResultsDateDependent = false; // default false
		if (showResultsActive != null) {
			showResultsDateDependent = showResultsActive.booleanValue();
		}

		showResultsDateDependentButton = uifactory.addCheckboxesVertical("qti_showresult", "qti.form.show.results", formLayout, new String[] { "xx" },
				new String[] { null }, 1);
		if (isAssessment || isSelfTest) {
			showResultsDateDependentButton.select("xx", showResultsDateDependent);
			showResultsDateDependentButton.addActionListener(FormEvent.ONCLICK);
		} else {
			showResultsDateDependentButton.setEnabled(false);
		}

		Date startDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		startDateElement = uifactory.addDateChooser("qti_form_start_date", "qti.form.date.start", null, formLayout);
		startDateElement.setDateChooserTimeEnabled(true);
		if (startDate != null) {
			startDateElement.setDate(startDate);
		}
		startDateElement.setMandatory(true);

		Date endDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		endDateElement = uifactory.addDateChooser("qti_form_end_date", "qti.form.date.end", null, formLayout);
		endDateElement.setDateChooserTimeEnabled(true);
		if (endDate != null) {
			endDateElement.setDate(endDate);
		}
		
		uifactory.addFormSubmitButton("submit", formLayout);

		update();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		update();
	}

	private void update() {
		if (!limitAttempts.isSelected(0)) {
			attempts.setIntValue(0);
		}
		attempts.setVisible(limitAttempts.isVisible() && limitAttempts.isSelected(0));
		attempts.setMandatory(attempts.isVisible());
		attempts.clearError();

		showResultsDateDependentButton.setVisible(showResultsOnHomePage.isSelected(0));

		if (!startDateElement.isVisible()) {
			startDateElement.setValue("");
		}
		startDateElement.clearError();
		startDateElement.setVisible(showResultsDateDependentButton.isVisible() && showResultsDateDependentButton.isSelected(0));

		endDateElement.clearError();
		if (!endDateElement.isVisible()) {
			endDateElement.setValue("");
		}
		endDateElement.setVisible(startDateElement.isVisible());
	}
	
	public void update(RepositoryEntry testEntry) {
		variablesCont.contextPut("onyxDisplayName", testEntry.getDisplayname());
		variablesCont.contextPut("showOutcomes", Boolean.TRUE);
		Map<String, String> outcomes = new HashMap<String, String>();
		try {
			OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
			outcomes = onyxReporter.getPossibleOutcomeVariables(courseNode);
		} catch (OnyxReporterException e) {
			getWindowControl().setWarning(translate("reporter.unavailable"));
		}
		variablesCont.contextPut("outcomes", outcomes);
		variablesCont.setVisible(outcomes.size() > 0);
	}

	/**
	 * @return true: menu is enabled
	 */
	public boolean isEnableMenu() {
		return enableMenu.isSelected(0);
	}

	/**
	 * @return true: menu should be displayed
	 */
	public boolean isDisplayMenu() {
		return displayMenu.isSelected(0);
	}

	/**
	 * @return true: score progress is enabled
	 */
	public boolean isDisplayScoreProgress() {
		return displayScoreProgress.isSelected(0);
	}

	/**
	 * @return true: score progress is enabled
	 */
	public boolean isDisplayQuestionProgress() {
		return displayQuestionProgress.isSelected(0);
	}

	/**
	 * @return true: question title is enabled
	 */
	public boolean isDisplayQuestionTitle() {
		return displayQuestionTitle.isSelected(0);
	}

	/**
	 * @return sequence configuration: section or item
	 */
	public String getSequence() {
		return sequence.getSelectedKey();
	}

	/**
	 * @return true: cancel is enabled
	 */
	public boolean isEnableCancel() {
		return enableCancel.isSelected(0);
	}

	/**
	 * @return true: suspend is enabled
	 */
	public boolean isEnableSuspend() {
		return enableSuspend.isSelected(0);
	}

	/**
	 * @return summary type: compact or detailed
	 */
	public String getSummary() {
		return summary.getSelectedKey();
	}

	/**
	 * @return number of max attempts
	 */
	public Integer getAttempts() {
		final Integer a = attempts.getIntValue();
		return a == 0 ? null : attempts.getIntValue();
	}

	/**
	 * @return true if only section title should be rendered
	 */
	public Boolean isMenuRenderSectionsOnly() {
		return Boolean.valueOf(menuRenderOptions.getSelectedKey());
	}

	/**
	 * @return true: score-info on start-page is enabled
	 */
	public boolean isEnableScoreInfo() {
		return scoreInfo.isSelected(0);
	}

	/**
	 * @return true is the results are shown date dependent
	 */
	public boolean isShowResultsDateDependent() {
		return showResultsDateDependentButton.isSelected(0);
	}

	/**
	 * @return Returns the start date for the result visibility.
	 */
	public Date getShowResultsStartDate() {
		return startDateElement.getDate();
	}

	/**
	 * @return Returns the end date for the result visibility.
	 */
	public Date getShowResultsEndDate() {
		return endDateElement.getDate();
	}

	/**
	 * @return Returns true if the results are shown after test finished.
	 */
	public boolean isShowResultsAfterFinishTest() {
		return showResultsAfterFinishTest.isSelected(0);
	}

	/**
	 * @return Returns true if the results are shown on the test home page.
	 */
	public boolean isShowResultsOnHomePage() {
		return showResultsOnHomePage.isSelected(0);
	}

	/**
	 * @return Returns the points needes to pass an onyx test.
	 */
	public Float getCutValue() {
		String val = cutValue.getValue();
		Float f = Float.NaN;
		try {
			f = Float.valueOf(val);
		} catch (Exception e) {
			try {
				NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
				f = Float.valueOf(nf.parse(val).floatValue());
			} catch (ParseException e1) {
				//ooops
			}
		}
		cutValue.setValue(f.toString());
		return f;
	}

	/**
	 * @return Returns the chosen template of an onyx test.
	 */
	public String getTemplate() {
		return template.getSelectedKey();
	}

	@Override
	protected void doDispose() {
		//
	}
	
	//<OLATCE-982>
	public boolean allowShowSolution(){
		boolean allow = false;
		if(allowShowSolutionBox != null){
			allow = allowShowSolutionBox.isAtLeastSelected(1);			
		}
		return allow;
	}
	//</OLATCE-982>

	//<OLATCE-2009>
	public boolean allowSuspension() {
		boolean allow = false;
		if (allowSuspensionBox != null && allowSuspensionBox.isVisible()) {
			allow = allowSuspensionBox.isAtLeastSelected(1);
		}
		return allow;
	}
	//</OLATCE-2009>
	
}

