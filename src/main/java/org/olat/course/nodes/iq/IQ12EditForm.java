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
*/

package org.olat.course.nodes.iq;

import java.io.File;
import java.util.Date;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.modules.ModuleConfiguration;
import org.olat.resource.OLATResource;

/**
 * Test configuration form. 
 * Used for configuring Test, Self-test, and Questionnaire(aka Survey).
 * <p>  
 * Initial Date:  Mar 3, 2004
 *
 * @author Mike Stock
 */
public class IQ12EditForm extends FormBasicController {
	
	private static final String[] correctionModeKeys = new String[] { "auto", "manual" };
	
	private SingleSelection correctionModeEl;
	private SelectionElement scoreInfo;
	private SingleSelection summary;
	private SelectionElement showResultsDateDependentButton;
	private DateChooser startDateElement;
	private DateChooser endDateElement;
	private SelectionElement showResultsAfterFinishTest;
	private SelectionElement showResultsOnHomePage;
	private StaticTextElement minScoreEl, maxScoreEl, cutValueEl;
	
	private ModuleConfiguration modConfig;
	
	private String[] correctionModeValues;
	private String configKeyType;
	
	private boolean isAssessment, isSelfTest, isSurvey;
	private final boolean hasEssay;
	
	/**
	 * Constructor for the qti configuration form
	 * @param ureq
	 * @param wControl
	 * @param modConfig
	 */
	IQ12EditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig, boolean hasEssay) {
		super (ureq, wControl);
		this.modConfig = modConfig;
		this.hasEssay = hasEssay;
		
		configKeyType = (String)modConfig.get(IQEditController.CONFIG_KEY_TYPE);
		
		isAssessment = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
		isSelfTest   = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF);
		isSurvey     = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);

		correctionModeValues = new String[]{
				translate("correction.auto"),
				translate("correction.manual")
		};
		
		initForm(ureq);
	}
	
	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		boolean allOk = true;
		
		if(summary != null && summary.isEnabled()) {
			summary.clearError();
			if(!summary.isOneSelected()) {
				summary.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		startDateElement.clearError();
		endDateElement.clearError();
		if (startDateElement.isVisible()) {
			if (startDateElement.isEmpty()) {
				startDateElement.setErrorKey("qti.form.date.start.error.mandatory", null);
				allOk &= false;
			} else {
				if (startDateElement.getDate() == null) {
					startDateElement.setErrorKey("qti.form.date.error.format", null);
					allOk &= false;
				}
			}

			if (!endDateElement.isEmpty()) {
				if (endDateElement.getDate() == null) {
					endDateElement.setErrorKey("qti.form.date.error.format", null);
					allOk &= false;
				}

				if (endDateElement.getDate().before(startDateElement.getDate())) {
					endDateElement.setErrorKey("qti.form.date.error.endbeforebegin", null);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(correctionModeEl != null &&correctionModeEl.isOneSelected()) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}

		// Only tests and selftests have summaries and score progress
		if (!isSurvey) {
			modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, getSummary());
			modConfig.set(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, String.valueOf(isShowResultsDateDependent()));
			modConfig.set(IQEditController.CONFIG_KEY_RESULTS_START_DATE, getShowResultsStartDate()); 
			modConfig.set(IQEditController.CONFIG_KEY_RESULTS_END_DATE, getShowResultsEndDate());
			modConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_FINISH, isShowResultsAfterFinishTest());
			modConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, isShowResultsOnHomePage());
			modConfig.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, isEnableScoreInfo());
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		minScoreEl = uifactory.addStaticTextElement("score.min", "", formLayout);
		minScoreEl.setVisible(false);
		maxScoreEl = uifactory.addStaticTextElement("score.max", "", formLayout);
		maxScoreEl.setVisible(false);
		cutValueEl = uifactory.addStaticTextElement("score.cut", "", formLayout);
		cutValueEl.setVisible(false);
		
		if (isAssessment) {
			correctionModeEl = uifactory.addRadiosVertical("correction.mode", "correction.mode", formLayout, correctionModeKeys, correctionModeValues);
			String mode = modConfig.getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
			boolean selected = false;
			for(String correctionModeKey:correctionModeKeys) {
				if(correctionModeKey.equals(mode)) {
					correctionModeEl.select(correctionModeKey, true);
					selected = true;
				}
			}
			if(!selected) {
				if(hasEssay) {
					correctionModeEl.select(correctionModeKeys[1], true);
				} else {
					correctionModeEl.select(correctionModeKeys[0], true);
				}
			}
		}

		//Show score infos on start page
		Boolean bEnableScoreInfos = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		boolean enableScoreInfos = (bEnableScoreInfos != null) ? bEnableScoreInfos.booleanValue() : true;
		scoreInfo = uifactory.addCheckboxesHorizontal("qti_scoreInfo", "qti.form.scoreinfo", formLayout, new String[]{"xx"}, new String[]{null});
		scoreInfo.select("xx", enableScoreInfos);
		if (isAssessment || isSelfTest) {
			scoreInfo.select("xx", enableScoreInfos);
			scoreInfo.addActionListener(FormEvent.ONCLICK);
		} else {// isSurvey
			scoreInfo.setVisible(false);
		}
		
		//migration: check if old tests have no summary 
		String configuredSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
		boolean noSummary = configuredSummary!=null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE) ? true : false;

		Boolean showResultOnHomePage = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		boolean confEnableShowResultOnHomePage = (showResultOnHomePage != null) ? showResultOnHomePage.booleanValue() : false;
		confEnableShowResultOnHomePage = !noSummary && confEnableShowResultOnHomePage;
		showResultsOnHomePage = uifactory.addCheckboxesHorizontal("qti_enableResultsOnHomePage", "qti.form.results.onhomepage", formLayout, new String[]{"xx"}, new String[]{null});
		showResultsOnHomePage.select("xx", confEnableShowResultOnHomePage);
		showResultsOnHomePage.addActionListener(FormEvent.ONCLICK);
		showResultsOnHomePage.setVisible(!isSurvey);
		
		Boolean showResultsActive = Boolean.valueOf(modConfig.getStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS));
		boolean showResultsDateDependent = false; // default false
		if (showResultsActive != null) {
			showResultsDateDependent = showResultsActive.booleanValue();
		}

		showResultsDateDependentButton = uifactory.addCheckboxesHorizontal("qti_showresult", "qti.form.show.results", formLayout, new String[]{"xx"}, new String[]{null});
		if (isAssessment || isSelfTest) {
			showResultsDateDependentButton.select("xx", showResultsDateDependent);
			showResultsDateDependentButton.addActionListener(FormEvent.ONCLICK);
		} else {
			showResultsDateDependentButton.setEnabled(false);
		}
	
		Date startDate = null;
		if(modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE) instanceof Date) { 
			startDate = (Date)modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE); 
		}
		startDateElement = uifactory.addDateChooser("qti_form_start_date", "qti.form.date.start", null, formLayout);
		startDateElement.setDateChooserTimeEnabled(true);
		startDateElement.setDate(startDate);
		startDateElement.setMandatory(true);
		
		Date endDate = null;
		if(modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE) instanceof Date) {
			endDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		}
		endDateElement = uifactory.addDateChooser("qti_form_end_date", "qti.form.date.end", null, formLayout);
		endDateElement.setDateChooserTimeEnabled(true);
		endDateElement.setDate(endDate);

		Boolean showResultOnFinish = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
		boolean confEnableShowResultOnFinish = (showResultOnFinish != null) ? showResultOnFinish.booleanValue() : true;
		confEnableShowResultOnFinish = !noSummary && confEnableShowResultOnFinish;
		showResultsAfterFinishTest = uifactory.addCheckboxesHorizontal("qti_enableResultsOnFinish", "qti.form.results.onfinish", formLayout, new String[]{"xx"}, new String[]{null});
		showResultsAfterFinishTest.select("xx", confEnableShowResultOnFinish);
		showResultsAfterFinishTest.addActionListener(FormEvent.ONCLICK);
		showResultsAfterFinishTest.setVisible(!isSurvey);
			
		String[] summaryKeys = new String[] { 
				AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT, 
				AssessmentInstance.QMD_ENTRY_SUMMARY_SECTION,
				AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED
		};
		
		String[] summaryValues = new String[] {
				translate("qti.form.summary.compact"),
				translate("qti.form.summary.section"),
				translate("qti.form.summary.detailed")
		};

		summary = uifactory.addRadiosVertical("qti_form_summary", "qti.form.summary", formLayout, summaryKeys, summaryValues);
		String confSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
		if (confSummary == null || noSummary) {
			confSummary = AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT;
		}
		if (isAssessment || isSelfTest) {
			for(String summaryKey:summaryKeys) {
				if(summaryKey.equals(confSummary)) {
					summary.select(summaryKey, true);
				}
			}
		} else {
			summary.setEnabled(false);
		}

		uifactory.addFormSubmitButton("submit", formLayout);
		update();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		update();
	}

	private void update() {
		summary.setVisible(showResultsAfterFinishTest.isSelected(0) || showResultsOnHomePage.isSelected(0) );
		
		showResultsDateDependentButton.setVisible(showResultsOnHomePage.isSelected(0));
		
		if (!startDateElement.isVisible()) {
			startDateElement.setValue("");
		}
		startDateElement.clearError();
		startDateElement.setVisible(
				showResultsDateDependentButton.isVisible() &&
				showResultsDateDependentButton.isSelected(0)
		);
		
		endDateElement.clearError();
		if (!endDateElement.isVisible()) endDateElement.setValue("");
		endDateElement.setVisible(startDateElement.isVisible());

		flc.setDirty(true);
	}
	
	/**
	 * Update the module configuration from the qti file: read min/max/cut values
	 * @param res
	 */
	protected void update(OLATResource res) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedRoot = frm.unzipFileResource(res);
		//with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if no longer needed.
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedRoot);
		VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
		if (vfsQTI==null){
			throw new AssertException("qti file did not exist even it should be guaranteed by repositor check-in ");
		}
		//ensures that InputStream is closed in every case.
		Document doc = QTIHelper.getDocument((LocalFileImpl)vfsQTI);
		if(doc == null){
			//error reading qti file (existence check was made before)
			throw new AssertException("qti file could not be read " + ((LocalFileImpl)vfsQTI).getBasefile().getAbsolutePath());
		}
		// Extract min, max and cut value
		Float minValue = null, maxValue = null, cutValue = null;
		Element decvar = (Element) doc.selectSingleNode("questestinterop/assessment/outcomes_processing/outcomes/decvar");
		if (decvar != null) {
			Attribute minval = decvar.attribute("minvalue");
			if (minval != null) {
				String mv = minval.getValue();
				try {
					minValue = Float.valueOf(mv);
				} catch (NumberFormatException e1) {
					// if not correct in qti file -> ignore
				}
			}
			Attribute maxval = decvar.attribute("maxvalue");
			if (maxval != null) {
				String mv = maxval.getValue();
				try {
					maxValue = Float.valueOf(mv);
				} catch (NumberFormatException e1) {
					// if not correct in qti file -> ignore
				}
			}
			Attribute cutval = decvar.attribute("cutvalue");
			if (cutval != null) {
				String cv = cutval.getValue();
				try {
					cutValue = Float.valueOf(cv);
				} catch (NumberFormatException e1) {
					// if not correct in qti file -> ignore
				}
			}
		}
		// Put values to module configuration
		minScoreEl.setValue(minValue == null ? "" : AssessmentHelper.getRoundedScore(minValue));
		minScoreEl.setVisible(minValue != null);
		maxScoreEl.setValue(maxValue == null ? "" : AssessmentHelper.getRoundedScore(maxValue));
		maxScoreEl.setVisible(maxValue != null);
		cutValueEl.setValue(cutValue == null ? "" : AssessmentHelper.getRoundedScore(cutValue));
		cutValueEl.setVisible(cutValue != null);
	}
	
	/**
	 * @return summary type: compact or detailed
	 */
	private String getSummary() { return summary.getSelectedKey();}

	
	/**
	 * 
	 * @return true is the results are shown date dependent
	 */
	private boolean isShowResultsDateDependent() { return showResultsDateDependentButton.isSelected(0); }
	
	/**
	 * 
	 * @return Returns the start date for the result visibility.
	 */
	private Date getShowResultsStartDate() { return startDateElement.getDate(); }
	
	/**
	 * 
	 * @return Returns the end date for the result visibility.
	 */
	private Date getShowResultsEndDate() { return endDateElement.getDate(); }
	
	/**
	 * 
	 * @return Returns true if the results are shown after test finished.
	 */
	private boolean isShowResultsAfterFinishTest() { return showResultsAfterFinishTest.isSelected(0); }
	
	/**
	 * 
	 * @return Returns true if the results are shown on the test home page.
	 */
	private boolean isShowResultsOnHomePage() { return showResultsOnHomePage.isSelected(0); }
	/**
	 * @return true: score-info on start-page is enabled
	 */
	private boolean isEnableScoreInfo() { return scoreInfo.isSelected(0); }	
	
	@Override
	protected void doDispose() {
		//
	}
	
}

