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
package org.olat.repository.ui.author.copy.wizard.dates;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
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
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.overview.OverviewDataModel;
import org.olat.course.editor.overview.OverviewDataModel.OverviewCols;
import org.olat.course.editor.overview.OverviewRow;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.author.copy.wizard.general.MetadataStep;

/**
 * Initial date: 10.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class GeneralDatesStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditDates()) {
			return new GeneralDatesStep(ureq, stepCollection, steps);
		} else {
			return MetadataStep.create(ureq, null, steps);
		}
	}
	
	public GeneralDatesStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.leaninpath.dates", null);
		
		// Check or create step collection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "steps.move.dates.title");
		}
		setStepCollection(stepCollection);
		
		// Next step
		setNextStep(MetadataStep.create(ureq, null, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new GeneralDatesStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class GeneralDatesStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private FlexiTableElement tableEl;
		private OverviewDataModel dataModel;
		
		private CloseableModalController cmc;
		private MoveDateConfirmController moveDateConfirmController;
		private boolean askForDateMove = true;
		private boolean moveDates = false;
		
		public GeneralDatesStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
			
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
			
			IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
			intendedNodeRenderer.setIndentationEnabled(false);
			FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer);
			
			DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(OverviewCols.node, nodeRenderer);
			nodeModel.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(nodeModel);
						
			if (context.isLearningPath()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.obligationChooser));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.startChooser));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.endChooser));
			}
									
			dataModel = new OverviewDataModel(columnsModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
			tableEl.setEmptyTableMessageKey("table.empty");
			tableEl.setExportEnabled(false);
			tableEl.setMultiSelect(false);
			tableEl.setBordered(false);
			tableEl.setCustomizeColumns(false);
			
			forgeRows(formLayout);
		}
		
		private void forgeRows(FormItemContainer formLayout) {
			// Add items to formLayout -> catch events in formInnerEvent
			// Set empty page as template -> items are only shown in table
			FormItemContainer tableItems = FormLayoutContainer.createCustomFormLayout("dateTableLayout", getTranslator(), velocity_root + "/table_formitems.html");
			tableItems.setRootForm(mainForm);
			formLayout.add(tableItems);
			
			SelectionValues obligationModes = new SelectionValues();
			SelectionValue optional = new SelectionValue(AssessmentObligation.optional.name(), translate("config.obligation.optional"));
			SelectionValue mandatory = new SelectionValue(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory"));
			obligationModes.add(optional, mandatory);
			
			for (OverviewRow row : context.getCourseNodes()) {
				if (row.getLearningPathConfigs() != null) {
					if (row.getLearningPathConfigs().getObligation() != null) {
						// Obligation chooser
						if (row.getObligationChooser() == null) {
							SingleSelection obligationChooser = uifactory.addDropdownSingleselect("obligation_" + row.getCourseNode().getIdent(), tableItems, obligationModes.keys(), obligationModes.values());
							obligationChooser.setUserObject(row);
							obligationChooser.addActionListener(FormEvent.ONCHANGE);
							obligationChooser.select(row.getLearningPathConfigs().getObligation().name(), true);
							row.setObligationChooser(obligationChooser);
						}
						
						// Start date chooser
						if (row.getStartChooser() == null) {
							Date startDate = calculateDate(row.getStart(), context.getDateDifference());
							DateChooser startDateChooser = uifactory.addDateChooser("start_" + row.getCourseNode().getIdent(), startDate, tableItems);
							startDateChooser.setUserObject(row);
							startDateChooser.addActionListener(FormEvent.ONCHANGE);
							startDateChooser.setInitialDate(startDate);
							row.setStartChooser(startDateChooser);
						}
						
						// End date chooser
						if (row.getEndChooser() == null) {
							Date endDate = calculateDate(row.getEnd(), context.getDateDifference());
							DateChooser endDateChooser = uifactory.addDateChooser("end_" + row.getCourseNode().getIdent(), endDate, tableItems);
							endDateChooser.setUserObject(row);
							endDateChooser.addActionListener(FormEvent.ONCHANGE);
							endDateChooser.setInitialDate(endDate);
							endDateChooser.setVisible(row.getObligationChooser().getSelectedKey().equals(AssessmentObligation.mandatory.name()));
							row.setEndChooser(endDateChooser);
						}
					}
				}
			}
			
			dataModel.setObjects(context.getCourseNodes());
			tableEl.reset();
		}
		
		private Date calculateDate(Date initialDate, long timeDifference) {
			if (initialDate == null) {
				return null;
			} 
			
			Date calculatedDate = new Date(initialDate.getTime() + timeDifference);
			
			return calculatedDate;
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source instanceof SingleSelection) {
				SingleSelection sourceSelection = (SingleSelection) source;
				
				if (sourceSelection.getName().startsWith("obligation")) {
					OverviewRow row = (OverviewRow) sourceSelection.getUserObject();
					updateVisibility(row);
				}
			} else if (source instanceof DateChooser) {
				if (source.getName().startsWith("start_") || source.getName().startsWith("end_")) {
					DateChooser sourceDateChooser = (DateChooser) source;
					boolean hasInitialDate = sourceDateChooser.getInitialDate() != null;
					
					if (hasInitialDate) {
						if (askForDateMove) {
							doAskForDateMove(ureq, sourceDateChooser);
						} else if (moveDates) {
							moveAllDates(sourceDateChooser, dataModel);
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
						moveAllDates(moveDatesEvent.getDateChooser(), dataModel);					
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
		
		private void cleanUp() {
			removeAsListenerAndDispose(moveDateConfirmController);
			removeAsListenerAndDispose(cmc);
			
			moveDateConfirmController = null;
			cmc = null;
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
		
		private void moveAllDates(DateChooser dateChooser, OverviewDataModel model) {	
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			long difference = dateChooser.getDate().getTime() - dateChooser.getInitialDate().getTime();
			
			for (OverviewRow row : model.getObjects()) {
				DateChooser start = row.getStartChooser();
				DateChooser end = row.getEndChooser();
				
				if (start != null && !start.equals(dateChooser) && start.getDate() != null) {
					Date startDate = start.getInitialDate();
					startDate.setTime(startDate.getTime() + difference);
					start.setDate(startDate);
					start.setInitialDate(startDate);
				}
				
				if (end != null && !end.equals(dateChooser) && end.getDate() != null) {
					Date endDate = end.getInitialDate();
					endDate.setTime(endDate.getTime() + difference);
					end.setDate(endDate);
					end.setInitialDate(endDate);
				}
			}
			
			dateChooser.setInitialDate(dateChooser.getDate());
		}
		
		private void updateVisibility(OverviewRow row) {
			boolean endChooserVisible = row.getObligationChooser().getSelectedKey().equals(AssessmentObligation.mandatory.name());
			row.getEndChooser().setVisible(endChooserVisible);
		}		
	}

}
