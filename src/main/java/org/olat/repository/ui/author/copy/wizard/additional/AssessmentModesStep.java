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
package org.olat.repository.ui.author.copy.wizard.additional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel.Cols;
import org.olat.course.assessment.ui.mode.ModeStatusCellRenderer;
import org.olat.course.assessment.ui.mode.TargetAudienceCellRenderer;
import org.olat.course.assessment.ui.mode.TimeCellRenderer;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.author.copy.wizard.dates.MoveDateConfirmController;
import org.olat.repository.ui.author.copy.wizard.dates.MoveDatesEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 11.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class AssessmentModesStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditAssessmentModes()) {
			return new AssessmentModesStep(ureq, stepCollection, steps);
		} else {
			return NOSTEP;
		}
	}
	
	public AssessmentModesStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("assessment.modes", null);
	
		// Stepcollection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "steps.additional");
		}
		setStepCollection(stepCollection);
		
		setNextStep(NOSTEP);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new AssessmentModesStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class AssessmentModesStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private FlexiTableElement tableEl;
		private AssessmentModeListModel model;
		
		private CloseableModalController cmc;
		private MoveDateConfirmController moveDateConfirmController;
		private boolean askForDateMove = true;
		private boolean moveDates = false;
		
		@Autowired
		private AssessmentModeManager assessmentModeMgr;
		@Autowired
		private AssessmentModeCoordinationService assessmentModeCoordinationService;
		
		public AssessmentModesStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(AssessmentModeListController.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.status, new ModeStatusCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.nameElement));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.beginChooser));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.endChooser));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.leadTime, new TimeCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.followupTime, new TimeCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.target, new TargetAudienceCellRenderer(getTranslator())));
			
			model = new AssessmentModeListModel(columnsModel, getTranslator(), assessmentModeCoordinationService);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
			
			loadModel(formLayout);
		}
		
		private void loadModel(FormItemContainer formLayout) {
			if (context.getAssessmentCopyInfos() == null || context.getAssessmentModeRows() == null) {
				// Add items to formLayout -> catch events in formInnerEvent
				// Set empty page as template -> items are only shown in table
				FormItemContainer tableItems = FormLayoutContainer.createCustomFormLayout("formItemsTableLayout", getTranslator(), velocity_root + "/table_formitems.html");
				tableItems.setRootForm(mainForm);
				formLayout.add(tableItems);
				
				List<AssessmentMode> modes = assessmentModeMgr.getAssessmentModeFor(context.getRepositoryEntry());
				Map<AssessmentMode, AssessmentModeCopyInfos> copyInfos = new HashMap<>();
				
				int counter = 0;
				
				for (AssessmentMode mode : modes) {
					TextElement nameElement = uifactory.addTextElement("description_" + counter, -1, mode.getName(), tableItems);
					DateChooser beginDateChooser = uifactory.addDateChooser("begin_date_" + counter, mode.getBegin(), tableItems);
					beginDateChooser.setInitialDate(mode.getBegin());
					beginDateChooser.addActionListener(FormEvent.ONCHANGE);
					DateChooser endDateChooser = uifactory.addDateChooser("end_date_" + counter++, mode.getEnd(), tableItems);
					endDateChooser.setInitialDate(mode.getEnd());
					endDateChooser.addActionListener(FormEvent.ONCHANGE);
					
					AssessmentModeCopyInfos copyInfo = new AssessmentModeCopyInfos(nameElement, beginDateChooser, endDateChooser);
					copyInfos.put(mode, copyInfo);
					
				}
				
				context.setAssessmentModeRows(modes);
				context.setAssessmentCopyInfos(copyInfos);
			}
			
			model.setObjects(context.getAssessmentModeRows());
			model.setCopyInfos(context.getAssessmentCopyInfos());
			tableEl.reloadData();
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source instanceof DateChooser) {
				if (source.getName().startsWith("begin_date_") || source.getName().startsWith("end_date_")) {	
					DateChooser sourceDateChooser = (DateChooser) source;
					boolean hasInitialDate = sourceDateChooser.getInitialDate() != null;
					
					if (hasInitialDate) {
						if (askForDateMove) {
							doAskForDateMove(ureq, sourceDateChooser);
						} else if (moveDates) {
							moveAllDates(sourceDateChooser, model);
						}
					} else {
						sourceDateChooser.setInitialDate(sourceDateChooser.getDate());
					}
				}
			}
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if (source == moveDateConfirmController) {
				if (event instanceof MoveDatesEvent) {
					MoveDatesEvent moveDatesEvent = (MoveDatesEvent) event;
					
					if (moveDatesEvent.isMoveDates()) {
						// Move other dates
						moveAllDates(moveDatesEvent.getDateChooser(), model);					
					} 
					
					askForDateMove = !moveDatesEvent.isRememberChoice();
					moveDates = moveDatesEvent.isMoveDates();
				}
				
				cmc.deactivate();
				cleanUp();
			} else if (source == cmc) {
				cmc.deactivate();
				cleanUp();
			}
		}
		
		private void doAskForDateMove(UserRequest ureq, DateChooser dateChooser) {
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			moveDateConfirmController = new MoveDateConfirmController(ureq, getWindowControl(), dateChooser);
			listenTo(moveDateConfirmController);
			
			cmc = new CloseableModalController(getWindowControl(), "close", moveDateConfirmController.getInitialComponent(), true, translate("dates.update.others"));
			listenTo(cmc);
			cmc.activate();
		}
		
		private void moveAllDates(DateChooser dateChooser, AssessmentModeListModel model) {	
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			long difference = dateChooser.getDate().getTime() - dateChooser.getInitialDate().getTime();
			
			for (AssessmentMode row : model.getObjects()) {
				DateChooser beginDateChooser = model.getCopyInfos().get(row).getBeginDateChooser();
				DateChooser endDateChooser = model.getCopyInfos().get(row).getEndDateChooser();
				
				if (beginDateChooser != null && !beginDateChooser.equals(dateChooser) && beginDateChooser.getDate() != null) {
					Date startDate = beginDateChooser.getInitialDate();
					startDate.setTime(startDate.getTime() + difference);
					beginDateChooser.setDate(startDate);
					beginDateChooser.setInitialDate(startDate);
				}
				
				if (endDateChooser != null && !endDateChooser.equals(dateChooser) && endDateChooser.getDate() != null) {
					Date endDate = endDateChooser.getInitialDate();
					endDate.setTime(endDate.getTime() + difference);
					endDateChooser.setDate(endDate);
					endDateChooser.setInitialDate(endDate);
				}
			}
			
			dateChooser.setInitialDate(dateChooser.getDate());
		}
		
		private void cleanUp() {
			removeAsListenerAndDispose(moveDateConfirmController);
			removeAsListenerAndDispose(cmc);
			
			moveDateConfirmController = null;
			cmc = null;
		}
		
	}

}
