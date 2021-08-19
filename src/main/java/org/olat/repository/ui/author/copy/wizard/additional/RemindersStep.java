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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
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
import org.olat.course.reminder.model.ReminderRow;
import org.olat.course.reminder.ui.CourseReminderListController;
import org.olat.course.reminder.ui.CourseReminderTableModel;
import org.olat.course.reminder.ui.CourseReminderTableModel.ReminderCols;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.rule.BeforeDateRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;
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
public class RemindersStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditReminders()) {
			return new RemindersStep(ureq, steps, stepCollection);
		} else {
			return AssessmentModesStep.create(ureq, stepCollection, steps);
		}
	}
	
	public RemindersStep(UserRequest ureq, CopyCourseSteps steps, BasicStepCollection stepCollection) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("reminders", null);
	
		// Stepcollection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "additional.settings");
		}
		
		setStepCollection(stepCollection);
		
		setNextStep(AssessmentModesStep.create(ureq, stepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new RemindersStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class RemindersStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private FlexiTableElement tableEl;
		private CourseReminderTableModel tableModel;
		
		private CloseableModalController cmc;
		private MoveDateConfirmController moveDateConfirmController;
		private boolean askForDateMove = true;
		private boolean moveDates = false;
		
		@Autowired
		private ReminderService reminderManager;
		@Autowired
		private ReminderModule reminderModule;
		
		public RemindersStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(CourseReminderListController.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose here			
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.id.i18nHeaderKey(), ReminderCols.id.ordinal(),
					true, ReminderCols.id.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.description.i18nHeaderKey(), ReminderCols.description.ordinal(),
					true, ReminderCols.description.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.afterDate.i18nHeaderKey(), ReminderCols.afterDate.ordinal(),
					true, ReminderCols.afterDate.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.untilDate.i18nHeaderKey(), ReminderCols.untilDate.ordinal(),
					true, ReminderCols.untilDate.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.creator.i18nHeaderKey(), ReminderCols.creator.ordinal(),
					true, ReminderCols.creator.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.creationDate.i18nHeaderKey(), ReminderCols.creationDate.ordinal(),
					true, ReminderCols.creationDate.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.lastModified.i18nHeaderKey(), ReminderCols.lastModified.ordinal(),
					true, ReminderCols.lastModified.name()));
			
			tableModel = new CourseReminderTableModel(columnsModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
			
			loadModel(formLayout);
		}
		
		private void loadModel(FormItemContainer formLayout) {
			if (context.getReminderRows() == null) {
				// Add items to formLayout -> catch events in formInnerEvent
				// Set empty page as template -> items are only shown in table
				FormItemContainer tableItems = FormLayoutContainer.createCustomFormLayout("formItemsTableLayout", getTranslator(), velocity_root + "/table_formitems.html");
				tableItems.setRootForm(mainForm);
				formLayout.add(tableItems);
				
				List<Reminder> reminders = reminderManager.getReminders(context.getSourceRepositoryEntry());
				List<ReminderInfos> reminderInfos = reminderManager.getReminderInfos(context.getSourceRepositoryEntry());
				
				List<ReminderRow> rows = new ArrayList<>(reminders.size());
				
				int counter = 0;
				
				for(Reminder reminder : reminders) {
					String configuration = reminder.getConfiguration();
					ReminderRules rules = reminderManager.toRules(configuration);
					
					List<ReminderRule> beforeDateRules = new ArrayList<>();
					List<ReminderRule> afterDateRules = new ArrayList<>();
					
					BeforeDateRuleSPI beforeDateRuleSPI = null;
					DateRuleSPI afterDateRuleSPI = null;
					
					for (ReminderRule rule : rules.getRules()) {
						RuleSPI ruleSPI = reminderModule.getRuleSPIByType(rule.getType());
						
						if (!ruleSPI.isDateDependant()) {
							continue;
						}
						
						if (ruleSPI instanceof BeforeDateRuleSPI) {
							beforeDateRules.add(rule);
							
							if (beforeDateRuleSPI == null) {
								beforeDateRuleSPI = (BeforeDateRuleSPI) ruleSPI;
							}
						} else if (ruleSPI instanceof DateRuleSPI) {
							afterDateRules.add(rule);
							
							if (afterDateRuleSPI == null) {
								afterDateRuleSPI = (DateRuleSPI) ruleSPI;
							}
						}
					}
					
					if (beforeDateRules.isEmpty() && afterDateRules.isEmpty()) {
						continue;
					}
									
					ReminderInfos info = reminderInfos.stream().filter(reminderInfo -> reminderInfo.getKey().equals(reminder.getKey())).findFirst().orElse(null);
					ReminderRow row = new ReminderRow(info);
					
					List<Date> beforeDates = new ArrayList<>();
					List<Date> afterDates = new ArrayList<>();
					
					
					for (ReminderRule beforeDateRule : beforeDateRules) {
						if (beforeDateRuleSPI.getDate(beforeDateRule) != null) {
							beforeDates.add(beforeDateRuleSPI.getDate(beforeDateRule));
						}
					}
					
					for (ReminderRule afterDateRule : afterDateRules) {
						if (afterDateRuleSPI.getDate(afterDateRule) != null) {
							afterDates.add(afterDateRuleSPI.getDate(afterDateRule));
						}
					}
					
					Date beforeDate = null;
					Date afterDate = null;
					DateChooser beforeDateChooser = null;
					DateChooser afterDateChooser = null;
					
					if (!beforeDates.isEmpty()) {
						beforeDate = Collections.min(beforeDates);
						row.setInitialBeforeDate(beforeDate);
					}
					
					if (!afterDates.isEmpty()) {
						afterDate = Collections.min(afterDates);
						row.setInitialAfterDate(afterDate);
					}
					
					if (beforeDate != null) {
						beforeDate.setTime(beforeDate.getTime() + context.getDateDifference());
						
						beforeDateChooser = uifactory.addDateChooser("before_date_" + counter, beforeDate, tableItems);
						beforeDateChooser.addActionListener(FormEvent.ONCHANGE);
						beforeDateChooser.setInitialDate(beforeDate);
						
					}
					
					if (afterDate != null) {
						afterDate.setTime(afterDate.getTime() + context.getDateDifference());
						
						afterDateChooser = uifactory.addDateChooser("after_date_" + counter++, afterDate, tableItems);
						afterDateChooser.addActionListener(FormEvent.ONCHANGE);
						afterDateChooser.setInitialDate(afterDate);
					}
					
					row.setAfterDateChooser(afterDateChooser);
					row.setBeforeDateChooser(beforeDateChooser);
					
					rows.add(row);
				}
				
				context.setReminderRows(rows);
				
			}
			
			tableModel.setObjects(context.getReminderRows());
			tableEl.reset();
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source instanceof DateChooser) {
				if (source.getName().startsWith("before_date_") || source.getName().startsWith("after_date_")) {
					DateChooser sourceDateChooser = (DateChooser) source;
					boolean hasInitialDate = sourceDateChooser.getInitialDate() != null;
					
					if (hasInitialDate) {
						if (askForDateMove) {
							doAskForDateMove(ureq, sourceDateChooser);
						} else if (moveDates) {
							moveAllDates(sourceDateChooser, tableModel);
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
						moveAllDates(moveDatesEvent.getDateChooser(), tableModel);					
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
		
		private void moveAllDates(DateChooser dateChooser, CourseReminderTableModel model) {	
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			long difference = dateChooser.getDate().getTime() - dateChooser.getInitialDate().getTime();
			
			for (ReminderRow row : model.getObjects()) {
				DateChooser afterDateChooser = row.getAfterDateChooser();
				DateChooser beforeDateChooser = row.getBeforeDateChooser();
				
				if (afterDateChooser != null && !afterDateChooser.equals(dateChooser) && afterDateChooser.getDate() != null) {
					Date startDate = afterDateChooser.getInitialDate();
					startDate.setTime(startDate.getTime() + difference);
					afterDateChooser.setDate(startDate);
					afterDateChooser.setInitialDate(startDate);
				}
				
				if (beforeDateChooser != null && !beforeDateChooser.equals(dateChooser) && beforeDateChooser.getDate() != null) {
					Date endDate = beforeDateChooser.getInitialDate();
					endDate.setTime(endDate.getTime() + difference);
					beforeDateChooser.setDate(endDate);
					beforeDateChooser.setInitialDate(endDate);
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
