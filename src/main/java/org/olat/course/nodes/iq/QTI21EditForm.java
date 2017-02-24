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
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
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
	private static final String[] correctionModeKeys = new String[]{ "auto", "manual" };

	private SingleSelection correctionModeEl;
	private SelectionElement showResultsOnHomePage;
	private SelectionElement scoreInfo, showResultsDateDependentButton;
	private MultipleSelectionElement showResultsOnFinishEl;
	private SingleSelection typeShowResultsOnFinishEl;
	private DateChooser startDateElement, endDateElement;
	private StaticTextElement minScoreEl, maxScoreEl, cutValueEl;
	
	private final boolean needManulCorrection;
	private final ModuleConfiguration modConfig;
	private final QTI21DeliveryOptions deliveryOptions;

	@Autowired
	private QTI21Service qtiService;
	
	public QTI21EditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig,
			QTI21DeliveryOptions deliveryOptions, boolean needManulCorrection) {
		super(ureq, wControl);
		
		this.modConfig = modConfig;
		this.deliveryOptions = (deliveryOptions == null ? new QTI21DeliveryOptions() : deliveryOptions);
		this.needManulCorrection = needManulCorrection;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		minScoreEl = uifactory.addStaticTextElement("score.min", "", formLayout);
		minScoreEl.setVisible(false);
		maxScoreEl = uifactory.addStaticTextElement("score.max", "", formLayout);
		maxScoreEl.setVisible(false);
		cutValueEl = uifactory.addStaticTextElement("score.cut", "", formLayout);
		cutValueEl.setVisible(false);
		
		String [] correctionModeValues = new String[]{
			translate("correction.auto"),
			translate("correction.manual")
		};
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
			if(needManulCorrection) {
				correctionModeEl.select(correctionModeKeys[1], true);
			} else {
				correctionModeEl.select(correctionModeKeys[0], true);
			}
		}

		//Show score informations on start page
		boolean enableScoreInfos = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		scoreInfo = uifactory.addCheckboxesHorizontal("qti_scoreInfo", "qti.form.scoreinfo", formLayout, new String[]{"xx"}, new String[]{null});
		scoreInfo.select("xx", enableScoreInfos);
		scoreInfo.addActionListener(FormEvent.ONCLICK);
		
		boolean showResultOnHomePage = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		showResultsOnHomePage = uifactory.addCheckboxesHorizontal("qti_enableResultsOnHomePage", "qti.form.results.onhomepage", formLayout, new String[]{"xx"}, new String[]{null});
		showResultsOnHomePage.select("xx", showResultOnHomePage);
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
		
		boolean configRef = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
		
		showResultsOnFinishEl = uifactory.addCheckboxesHorizontal("resultOnFinish", "qti.form.results.onfinish", formLayout, onKeys, onValues);
		showResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		showResultsOnFinishEl.setEnabled(!configRef);
		
		ShowResultsOnFinish showSummary = deliveryOptions.getShowResultsOnFinish();
		String defaultConfSummary = showSummary == null ? AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT : showSummary.getIQEquivalent();
		String confSummary = modConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, defaultConfSummary);
		if(!AssessmentInstance.QMD_ENTRY_SUMMARY_NONE.equals(confSummary)) {
			showResultsOnFinishEl.select(onKeys[0], true);
		}

		String[] typeShowResultsOnFinishKeys = new String[] {
				AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT, AssessmentInstance.QMD_ENTRY_SUMMARY_SECTION, AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED
		};
		String[] typeShowResultsOnFinishValues = new String[] {
			translate("qti.form.summary.compact"), translate("qti.form.summary.section"), translate("qti.form.summary.detailed")
		};
		typeShowResultsOnFinishEl = uifactory.addRadiosVertical("typeResultOnFinish", "qti.form.summary", formLayout, typeShowResultsOnFinishKeys, typeShowResultsOnFinishValues);
		typeShowResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		typeShowResultsOnFinishEl.setEnabled(!configRef);
		if(StringHelper.containsNonWhitespace(confSummary)) {
			for(String typeShowResultsOnFinishKey:typeShowResultsOnFinishKeys) {
				if(typeShowResultsOnFinishKey.equals(confSummary)) {
					typeShowResultsOnFinishEl.select(typeShowResultsOnFinishKey, true);
				}
			}
		} 
		if(!typeShowResultsOnFinishEl.isOneSelected()) {
			typeShowResultsOnFinishEl.select(AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT, true);
		}
		
		uifactory.addFormSubmitButton("submit", formLayout);
		
		update();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		startDateElement.clearError();
		if(showResultsDateDependentButton.isSelected(0)) {
			if(startDateElement.getDate() == null) {
				startDateElement.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showResultsOnFinishEl == source) {
			update();
		} else if(showResultsDateDependentButton == source || showResultsOnHomePage == source) {
			update();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void update() {
		showResultsDateDependentButton.setVisible(showResultsOnHomePage.isSelected(0));
		typeShowResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1) || showResultsOnHomePage.isSelected(0));
		
		if (!startDateElement.isVisible()) {
			startDateElement.setValue("");
		}
		startDateElement.clearError();
		startDateElement.setVisible(showResultsDateDependentButton.isVisible() && showResultsDateDependentButton.isSelected(0));
		
		endDateElement.clearError();
		if (!endDateElement.isVisible()){
			endDateElement.setValue("");
		}
		endDateElement.setVisible(startDateElement.isVisible());
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
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(correctionModeEl.isOneSelected()) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}

		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLESCOREINFO, scoreInfo.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, showResultsDateDependentButton.isSelected(0));
		
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE, startDateElement.getDate());
		modConfig.setDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE, endDateElement.getDate());
		
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, showResultsOnHomePage.isSelected(0));
		
		if(showResultsOnFinishEl.isAtLeastSelected(1) || showResultsOnHomePage.isSelected(0)) {
			if(typeShowResultsOnFinishEl.isOneSelected()) {
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, typeShowResultsOnFinishEl.getSelectedKey());
			} else {
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
			}
		} else {
			modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
