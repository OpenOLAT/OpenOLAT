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
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
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
public class LectureBlocksStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditLectureBlocks()) {
			return new LectureBlocksStep(ureq, stepCollection, steps);
		} else {
			return RemindersStep.create(ureq, stepCollection, steps);
		}
	}
	
	public LectureBlocksStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("lecture.blocks", null);
	
		// Stepcollection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "additional.settings");
		}
		setStepCollection(stepCollection);
		
		setNextStep(RemindersStep.create(ureq, stepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new LectureBlocksStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private static class LectureBlocksStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private FlexiTableElement tableEl;
		private LectureListRepositoryDataModel tableModel;
		
		private CloseableModalController cmc;
		private CloseableCalloutWindowController calloutCtrl;
		private LectureBlockTeacherController teacherCtrl;
		private MoveDateConfirmController moveDateConfirmController;
		private boolean askForDateMove = true;
		private boolean moveDates = false;
		
		@Autowired
		private LectureService lectureService;
		
		public LectureBlocksStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.id));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.assessmentMode,
					new BooleanCellRenderer(new CSSIconFlexiCellRenderer("o_icon_assessment_mode"), null)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.compulsory, new YesNoCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.locationElement));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.dateChooser));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.chosenTeachers));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.teacherChooser));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.status, new LectureBlockStatusCellRenderer(getTranslator())));

			tableModel = new LectureListRepositoryDataModel(columnsModel, getLocale()); 
			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
			tableEl.setExportEnabled(false);
			tableEl.setMultiSelect(false);
			tableEl.setEmptyTableMessageKey("empty.table.lectures.blocks.admin");
			
			FlexiTableSortOptions options = new FlexiTableSortOptions();
			options.setDefaultOrderBy(new SortKey(BlockCols.dateChooser.name(), false));
			tableEl.setSortSettings(options);
			
			loadModel(context, formLayout);
		}
		
		private void loadModel(CopyCourseContext context, FormItemContainer formLayout) {
			// Add items to formLayout -> catch events in formInnerEvent
			// Set empty page as template -> items are only shown in table
			FormItemContainer tableItems = FormLayoutContainer.createCustomFormLayout("formItemsTableLayout", getTranslator(), velocity_root + "/table_formitems.html");
			tableItems.setRootForm(mainForm);
			formLayout.add(tableItems);
						
			int counter = 0;
			
			if (context.getLectureBlockRows() == null) {
				List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithTeachers(context.getSourceRepositoryEntry());
				List<LectureBlockRow> rows = new ArrayList<>(blocks.size());
							
				for(LectureBlockWithTeachers block:blocks) {
					LectureBlockRow row = new LectureBlockRow(block.getLectureBlock(), context.getSourceRepositoryEntry().getDisplayname(), context.getSourceRepositoryEntry().getExternalRef(),
							null, false, block.isAssessmentMode());
					
					Date startDate = null;
					Date endDate = null;
					
					if (block.getLectureBlock().getStartDate() != null) {
						startDate = new Date(block.getLectureBlock().getStartDate().getTime() + context.getDateDifference());
					}
					
					if (block.getLectureBlock().getEndDate() != null) {
						endDate = new Date(block.getLectureBlock().getEndDate().getTime() + context.getDateDifference());
					}
					
					DateChooser dateChooser = uifactory.addDateChooser("lecture_block_date_" + counter, startDate, tableItems);
					dateChooser.setInitialDate(startDate);
					dateChooser.setDateChooserTimeEnabled(true);
					dateChooser.setSecondDate(true);
					dateChooser.setSecondDate(endDate);
					dateChooser.setSameDay(true);
					dateChooser.addActionListener(FormEvent.ONCHANGE);
					row.setDateChooser(dateChooser);
					
					TextElement locationElement = uifactory.addTextElement("lecture_block_location_" + counter, -1, block.getLectureBlock().getLocation(), tableItems);
					row.setLocationElement(locationElement);
					
					FormLink teachersLink = uifactory.addFormLink("lecture_block_teachers_" + counter++, tableItems);
					teachersLink.setI18nKey("edit.teachers");
					teachersLink.setUserObject(row);
					
					row.setTeacherChooserLink(teachersLink);
					row.setTeachersList(filterTeachers(block.getTeachers(), context.getNewCoaches()));
					
					rows.add(row);
				}
				
				context.setLectureBlockRows(rows);
			}
			
			tableModel.setObjects(context.getLectureBlockRows());
			tableEl.reset(true, true, true);
		}
		
		private List<Identity> filterTeachers(List<Identity> teachers, List<Identity> newCoaches) {
			List<Identity> filteredTeachers = new ArrayList<>();
			
			if (teachers == null || teachers.isEmpty() || newCoaches == null || newCoaches.isEmpty()) {
				return filteredTeachers;
			}
			
			for (Identity teacher : teachers) {
				if (newCoaches.contains(teacher)) {
					filteredTeachers.add(teacher);
				}
			}
			
			
			return filteredTeachers;
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source instanceof DateChooser) {
				if (source.getName().startsWith("lecture_block_date_")) {	
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
			} else if (source instanceof FormLink) {
				if (source.getName().startsWith("lecture_block_teachers_")) {
					FormLink sourceLink = (FormLink) source;
					LectureBlockRow row = (LectureBlockRow) source.getUserObject();
					
					teacherCtrl = new LectureBlockTeacherController(ureq, getWindowControl(), context, row);
					calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), teacherCtrl.getInitialComponent(), sourceLink, null, true, null);
					calloutCtrl.activate();
					calloutCtrl.addControllerListener(this);
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
			} else if (source == calloutCtrl) {
				teacherCtrl.saveToContext();
				tableEl.reloadData();
				cleanUp();
			}
		}
		
		private void cleanUp() {
			removeAsListenerAndDispose(moveDateConfirmController);
			removeAsListenerAndDispose(teacherCtrl);
			removeAsListenerAndDispose(cmc);
			
			moveDateConfirmController = null;
			teacherCtrl = null;
			cmc = null;
		}
		
		private void doAskForDateMove(UserRequest ureq, DateChooser dateChooser) {
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			if (dateChooser.getInitialDate().equals(dateChooser.getDate())) {
				return;
			}
			
			// Milliseconds to days => 1000*60*60*24 = 86400000
			if (Math.abs(dateChooser.getInitialDate().getTime() - dateChooser.getDate().getTime()) < 86400000) {
				return;
			}
			
			moveDateConfirmController = new MoveDateConfirmController(ureq, getWindowControl(), dateChooser);
			listenTo(moveDateConfirmController);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), moveDateConfirmController.getInitialComponent(), true, translate("dates.update.others"));
			listenTo(cmc);
			cmc.activate();
		}
		
		private void moveAllDates(DateChooser dateChooser, LectureListRepositoryDataModel model) {	
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			long difference = dateChooser.getDate().getTime() - dateChooser.getInitialDate().getTime();
			
			for (LectureBlockRow row : model.getObjects()) {
				DateChooser lectureDateChooser = row.getDateChooser();
				
				if (lectureDateChooser != null && !lectureDateChooser.equals(dateChooser) && lectureDateChooser.getDate() != null) {
					Date startDate = lectureDateChooser.getInitialDate();
					startDate.setTime(startDate.getTime() + difference);
					lectureDateChooser.setDate(startDate);
					lectureDateChooser.setInitialDate(startDate);
				}
			}
			
			dateChooser.setInitialDate(dateChooser.getDate());
		}
		
		
	}

}
