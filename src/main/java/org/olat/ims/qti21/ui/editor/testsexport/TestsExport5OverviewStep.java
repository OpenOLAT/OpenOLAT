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
package org.olat.ims.qti21.ui.editor.testsexport;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExport5OverviewStep extends BasicStep {

	private final TestsExportContext exportContext;
	
	public TestsExport5OverviewStep(UserRequest ureq, TestsExportContext exportContext) {
		super(ureq);
		this.exportContext = exportContext;
		setNextStep(NOSTEP);
		setI18nTitleAndDescr("wizard.overview.title", "wizard.overview.title");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_FINISH;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new OverviewController(ureq, wControl, form, runContext);
	}
	
	class OverviewController extends StepFormBasicController {
		
		private final String mapperUri;
		
		public OverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
			mapperUri = registerCacheableMapper(ureq, null, new PreviewMapper(getIdentity(), wControl, exportContext));
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			initGeneralForm(formLayout);
			initTestForm(formLayout);
			if(exportContext.isCoverSheet()) {
				initCoverSheetForm(formLayout);
			}
			if(exportContext.isAdditionalSheet()) {
				initAdditionalSheetForm(formLayout);
			}
			
			String page = this.velocity_root + "/preview.html";
			FormLayoutContainer previewCont = FormLayoutContainer.createCustomFormLayout("preview.cont", getTranslator(), page);
			formLayout.add(previewCont);
			
			ExternalLink link = new ExternalLink("preview", "preview");
			link.setElementCssClass("btn btn-default pull-right");
			link.setName(translate("preview"));
			link.setUrl(mapperUri + "/preview.pdf?test=" + CodeHelper.getForeverUniqueID());
			link.setTarget("_blank");
			previewCont.put("preview", link);
		}
		
		private void initGeneralForm(FormItemContainer formLayout) {
			FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
			formLayout.add(generalCont);
			generalCont.setFormTitle(translate("overview.general.title"));
			
			String numbers = translate("attr.numbers.val", Integer.toString(exportContext.getNumOfTests()));
			uifactory.addStaticTextElement("numbers", "attr.numbers", numbers, generalCont);
			uifactory.addStaticTextElement("format", "format", translate("format.pdf"), generalCont);
			Locale locale = exportContext.getLocale();
			String language = locale.getDisplayLanguage(getLocale());
			uifactory.addStaticTextElement("language", "overview.language", language, generalCont);
			
			String serialNumbers;
			String startSerialNumber = exportContext.getSerialNumber(1);
			if(exportContext.getNumOfTests() == 1) {
				serialNumbers = startSerialNumber;
			} else if(exportContext.getNumOfTests() > 1) {
				String endSerialNumber = exportContext.getSerialNumber(exportContext.getNumOfTests());
				serialNumbers = translate("overview.serial.number.val", startSerialNumber, endSerialNumber);
			} else {
				serialNumbers = "";
			}
			uifactory.addStaticTextElement("serial.number", "overview.serial.number", serialNumbers, generalCont);	
		}
		
		private void initTestForm(FormItemContainer formLayout) {
			FormLayoutContainer testCont = FormLayoutContainer.createDefaultFormLayout("test", getTranslator());
			formLayout.add(testCont);
			testCont.setFormTitle(translate("overview.test.title"));
			testCont.setVisible(exportContext.isExpenditureOfTime() || exportContext.isNumOfQuestions() || exportContext.isMaxScore() || exportContext.isCutValue());
			
			ResolvedAssessmentTest resolvedAssessmentTest = exportContext.getResolvedAssessmentTest();
			AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractAssumingSuccessful();
			TimeLimits timeLimits = assessmentTest.getTimeLimits();

			String time = "";
			if(timeLimits != null && timeLimits.getMaximum() != null && timeLimits.getMaximum().longValue() > 0) {
				long maxInSeconds = timeLimits.getMaximum().longValue();
				String timeInMinutes = Long.toString(maxInSeconds / 60);
				exportContext.setTimeValue(timeInMinutes);
				time = translate("attr.time.val", timeInMinutes);
			}
			StaticTextElement timeEl = uifactory.addStaticTextElement("attr.time", time, testCont);
			timeEl.setVisible(exportContext.isExpenditureOfTime());
			
			int numOfQuestions = QtiMaxScoreEstimator.estimateNumberOfQuestions(resolvedAssessmentTest);
			exportContext.setNumOfQuestionsValue(Integer.toString(numOfQuestions));
			StaticTextElement questionsEl = uifactory.addStaticTextElement("attr.num.questions",
					translate("attr.num.questions.val", exportContext.getNumOfQuestionsValue()), testCont);
			questionsEl.setVisible(exportContext.isNumOfQuestions());
			
			Double maxScore = QtiMaxScoreEstimator.estimateMaxScore(resolvedAssessmentTest);
			String maxScoreStr;
			if(maxScore != null) {
				String roundedMaxScore = AssessmentHelper.getRoundedScore(maxScore);
				maxScoreStr = translate("attr.max.score.val", roundedMaxScore);
				exportContext.setMaxScoreValue(roundedMaxScore);
			} else {
				maxScoreStr = "";
			}
			StaticTextElement maxScoreEl = uifactory.addStaticTextElement("attr.max.score", maxScoreStr, testCont);
			maxScoreEl.setVisible(exportContext.isMaxScore());
			
			Double cutValue = QtiNodesExtractor.extractCutValue(resolvedAssessmentTest.getRootNodeLookup().extractAssumingSuccessful());
			String cutValueStr;
			if(cutValue != null) {
				String roundedCutValue = AssessmentHelper.getRoundedScore(cutValue);
				cutValueStr = translate("attr.cut.value.val", roundedCutValue);
				exportContext.setCutValueValue(roundedCutValue);
			} else {
				cutValueStr = "";
			}
			StaticTextElement cutValueEl = uifactory.addStaticTextElement("attr.cut.value", cutValueStr, testCont);
			cutValueEl.setVisible(exportContext.isCutValue());
		}

		private void initCoverSheetForm(FormItemContainer formLayout) {
			FormLayoutContainer sheetCont = FormLayoutContainer.createDefaultFormLayout("sheet", getTranslator());
			formLayout.add(sheetCont);
			sheetCont.setFormTitle(translate("overview.cover.title"));
			
			String identifier = exportContext.getIdentifierValue();
			uifactory.addStaticTextElement("overview.cover.identifier", identifier, sheetCont);
			String procedure = exportContext.getProcedure();
			uifactory.addStaticTextElement("overview.cover.procedure", procedure, sheetCont);
			String title = exportContext.getTitleValue();
			uifactory.addStaticTextElement("overview.cover.title", title, sheetCont);
			
			String description = exportContext.getDescriptionValue();
			uifactory.addStaticTextElement("overview.cover.description", description, sheetCont);
		}
		
		private void initAdditionalSheetForm(FormItemContainer formLayout) {
			FormLayoutContainer sheetCont = FormLayoutContainer.createDefaultFormLayout("add.sheet", getTranslator());
			formLayout.add(sheetCont);
			sheetCont.setFormTitle(translate("additional.sheet.title"));
		
			String addSheetStr = exportContext.getAdditionalSheetValue();
			uifactory.addStaticTextElement("additional.sheet", addSheetStr, sheetCont);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}
	}
	
	private static class PreviewMapper implements Mapper {
		
		private final Identity identity;
		private final WindowControl wControl;
		private final TestsExportContext exportContext;
		
		public PreviewMapper(Identity identity, WindowControl wControl, TestsExportContext exportContext) {
			this.identity = identity;
			this.wControl = wControl;
			this.exportContext = exportContext;	
		}
	
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			return new QTI21OfflineTestsPDFPreviewMediaResource(identity, wControl, exportContext, "preview");
		}
	}
}
