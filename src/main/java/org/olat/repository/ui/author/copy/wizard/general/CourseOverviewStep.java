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
package org.olat.repository.ui.author.copy.wizard.general;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.editor.EditorMainController;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeDatesListController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewDataModel;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewDataModel.CopyCourseOverviewCols;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.author.copy.wizard.dates.MoveDateConfirmController;
import org.olat.repository.ui.author.copy.wizard.dates.MoveDatesEvent;

/**
 * Initial date: 10.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CourseOverviewStep extends BasicStep {

	public static Step create(UserRequest ureq, CopyCourseSteps steps) {
		if (steps.isAdvancedMode() && steps.showNodesOverview()) {
			return new CourseOverviewStep(ureq, steps);
		} else {
			return CatalogStep.create(ureq, null, steps);
		}
	}
	
	public CourseOverviewStep(UserRequest ureq, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.move.dates.title", null);
		
		// Next step
		setNextStep(CatalogStep.create(ureq, null, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new CourseOverviewStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class CourseOverviewStepController extends StepFormBasicController implements FlexiTableComponentDelegate {

		private CopyCourseContext context;
		
		private FlexiTableElement tableEl;
		private CopyCourseOverviewDataModel dataModel;
		
		private CloseableModalController cmc;
		private MoveDateConfirmController moveDateConfirmController;
		private boolean askForDateMove = true;
		
		private CourseNodeDatesListController courseNodeDatesListController;
		
		public CourseOverviewStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			courseNodeDatesListController = new CourseNodeDatesListController(ureq, wControl, context);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}

		@Override
		protected void formOK(UserRequest ureq) {			
			saveDatesToContext(context, dataModel.getObjects());
			removeFormElementsFromContext(context);
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);			
		}
		
		private void saveDatesToContext(CopyCourseContext context, List<CopyCourseOverviewRow> rows) {
			for (CopyCourseOverviewRow row : rows) {
				if (row.getObligationChooser() != null) {
					row.setAssesssmentObligation(AssessmentObligation.valueOf(row.getObligationChooser().getSelectedKey()));
				}
				
				if (row.getResourceCopyType() != null) {
					row.setResourceCopyType(CopyType.valueOf(row.getResourceChooser().getSelectedKey()));
				}
				
				if (row.getNewStartDateChooser() != null) {
					row.setNewStartDate(row.getNewStartDateChooser().getDate());
				}
				
				if (row.getNewEndDateChooser() != null ) {
					row.setNewEndDate(row.getNewEndDateChooser().getDate());
				}
			}
			
			context.setCourseNodes(rows);
		}
		
		private void removeFormElementsFromContext(CopyCourseContext context) {
			List<CopyCourseOverviewRow> rows = context.getCourseNodes();
			
			if (rows == null || rows.isEmpty()) {
				return;
			}
			
			for (CopyCourseOverviewRow row : rows) {
				row.setObligationChooser(null);
				row.setResourceChooser(null);
				row.setNewEndDateChooser(null);
				row.setNewStartDateChooser(null);
			}
			
			context.setCourseNodes(rows);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			
			IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
			intendedNodeRenderer.setIndentationEnabled(false);
			FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer);
			
			DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(CopyCourseOverviewCols.node, nodeRenderer);
			nodeModel.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(nodeModel);
						
			if (context.isLearningPath()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyCourseOverviewCols.obligationChooser));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyCourseOverviewCols.startChooser));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyCourseOverviewCols.endChooser));
			}
			
			if (context.hasNodeSpecificSettings()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyCourseOverviewCols.resourceChooser));
			}
									
			dataModel = new CopyCourseOverviewDataModel(columnsModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
			tableEl.setEmptyTableMessageKey("table.empty");
			tableEl.setExportEnabled(false);
			tableEl.setMultiSelect(false);
			tableEl.setBordered(false);
			tableEl.setCustomizeColumns(false);
			
			VelocityContainer detailsVC = createVelocityContainer("node_dates_details");
			detailsVC.put("node.dates.details.controller", courseNodeDatesListController.getInitialComponent());
			tableEl.setDetailsRenderer(detailsVC, this);
			
			forgeRows(formLayout);
			context.setCustomConfigsLoaded(true);
		}
		
		@Override
		public Iterable<Component> getComponents(int row, Object rowObject) {
			if (courseNodeDatesListController == null) {
				return null;
			}
			
			List<Component> components = new ArrayList<>(1);
			components.add(courseNodeDatesListController.getInitialComponent());
			
			return components;
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
			
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("options.empty.resource"));
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("options.reference"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.configure.later"));
			SelectionValue copyContent = new SelectionValue(CopyType.copy.name(), translate("options.copy.content"));
			SelectionValue ignoreContent = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.content"));
			SelectionValue copyAssignmentAndSolution = new SelectionValue(CopyType.copy.name(), translate("options.copy.assignment.solution"));
			SelectionValue ignoreAssignmentAndSolution = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.assignment.solution"));
			
			SelectionValues copyModes = null;
			
			List<Integer> dateDependantNodesRows = new ArrayList<>();
			int rowCount = 0;
			
			for (CopyCourseOverviewRow row : context.getCourseNodes()) {
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
						Date startDate = calculateDate(row.getStart(), context.getDateDifference());
						DateChooser startDateChooser = uifactory.addDateChooser("start_" + row.getCourseNode().getIdent(), startDate, tableItems);
						startDateChooser.setUserObject(row);
						startDateChooser.addActionListener(FormEvent.ONCHANGE);
						startDateChooser.setInitialDate(startDate);
						startDateChooser.setDateChooserTimeEnabled(true);
						row.setNewStartDateChooser(startDateChooser);
						if (row.getNewStartDate() != null) {
							startDateChooser.setDate(row.getNewStartDate());
						}
						
						// End date chooser
						Date endDate = calculateDate(row.getEnd(), context.getDateDifference());
						DateChooser endDateChooser = uifactory.addDateChooser("end_" + row.getCourseNode().getIdent(), endDate, tableItems);
						endDateChooser.setUserObject(row);
						endDateChooser.addActionListener(FormEvent.ONCHANGE);
						endDateChooser.setInitialDate(endDate);
						endDateChooser.setDateChooserTimeEnabled(true);
						endDateChooser.setVisible(row.getObligationChooser().getSelectedKey().equals(AssessmentObligation.mandatory.name()));
						row.setNewEndDateChooser(endDateChooser);
						if (row.getNewEndDate() != null) {
							endDateChooser.setDate(row.getNewEndDate());
						}
					}
				}
				
				if (isConfigurable(row.getCourseNode())) {
					if (row.getResourceChooser() == null) {
						if (row.getCourseNode() instanceof BCCourseNode) {
							copyModes = new SelectionValues(copyContent, ignoreContent);
						} else if (row.getCourseNode() instanceof GTACourseNode) {
							copyModes = new SelectionValues(copyAssignmentAndSolution, ignoreAssignmentAndSolution);
						} else {
							copyModes = new SelectionValues(reference, createNew, ignore);
						}
						
						SingleSelection resourceChooser = uifactory.addDropdownSingleselect("resource_" + row.getCourseNode().getIdent(), tableItems, copyModes.keys(), copyModes.values());
						selectCopyMode(context, row.getCourseNode(), resourceChooser);
						row.setResourceChooser(resourceChooser);
					}
				}
				
				if (row.getCourseNode().hasDates()) {
					dateDependantNodesRows.add(rowCount);
				}
				
				rowCount++;
			}
			
			dataModel.setObjects(context.getCourseNodes());
			tableEl.setDetailsRows(dateDependantNodesRows);
			tableEl.reset();
		}
		
		private boolean isConfigurable(CourseNode courseNode) {
			if (courseNode instanceof WikiCourseNode ||
					courseNode instanceof BlogCourseNode ||
					courseNode instanceof BCCourseNode ||
					courseNode instanceof GTACourseNode) {
				return true;
			}
			
			return false;
		}
		
		private void selectCopyMode(CopyCourseContext context, CourseNode courseNode, SingleSelection chooser) {
			String selectKey = "";
			
			if (courseNode instanceof WikiCourseNode) {
				selectKey = getCopyType(context.getWikiCopyType());
			} else if (courseNode instanceof BlogCourseNode) {
				selectKey = getCopyType(context.getBlogCopyType());
			} else if (courseNode instanceof BCCourseNode) {
				selectKey = getCopyType(context.getFolderCopyType());
			} else if (courseNode instanceof GTACourseNode) {
				selectKey = getCopyType(context.getTaskCopyType());
			}
			
			if (StringHelper.containsNonWhitespace(selectKey)) {
				chooser.select(selectKey, true);
			}
		}
		
		private String getCopyType(CopyType copyType) {
			if (copyType != null) {
				return copyType.name();
			}
			
			return null;
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
			if (source == tableEl) {
				if (event instanceof DetailsToggleEvent) {
					DetailsToggleEvent dte = (DetailsToggleEvent)event;
					if (dte.isVisible()) {
						CopyCourseOverviewRow row = dataModel.getObject(dte.getRowIndex());
						courseNodeDatesListController.updateCourseNode(row.getCourseNode(), ureq);
					}
				}
			}
			
			if (source instanceof SingleSelection) {
				SingleSelection sourceSelection = (SingleSelection) source;
				
				if (sourceSelection.getName().startsWith("obligation")) {
					CopyCourseOverviewRow row = (CopyCourseOverviewRow) sourceSelection.getUserObject();
					updateVisibility(row);
				}
			} else if (source instanceof DateChooser) {
				if (source.getName().startsWith("start_") || source.getName().startsWith("end_")) {
					DateChooser sourceDateChooser = (DateChooser) source;
					boolean hasInitialDate = sourceDateChooser.getInitialDate() != null;
					
					if (hasInitialDate && askForDateMove) {
						doAskForDateMove(ureq, sourceDateChooser);
					} else {
						sourceDateChooser.setInitialDate(sourceDateChooser.getDate());
					}
					
					courseNodeDatesListController.updateDates(ureq);
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
						moveAllDates(moveDatesEvent, dataModel);					
					} 
					
					askForDateMove = !moveDatesEvent.isRememberChoice();
				}
				
				courseNodeDatesListController.updateDates(ureq);
				
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
			
			if (dateChooser.getInitialDate().equals(dateChooser.getDate())) {
				return;
			}
			
			moveDateConfirmController = new MoveDateConfirmController(ureq, getWindowControl(), dateChooser);
			listenTo(moveDateConfirmController);
			
			cmc = new CloseableModalController(getWindowControl(), "close", moveDateConfirmController.getInitialComponent(), true, translate("dates.update.others"));
			listenTo(cmc);
			cmc.activate();
		}
		
		private void moveAllDates(MoveDatesEvent moveDatesEvent, CopyCourseOverviewDataModel model) {
			DateChooser dateChooser = moveDatesEvent.getDateChooser();
			
			if (dateChooser == null || dateChooser.getInitialDate() == null || dateChooser.getDate() == null) {
				return;
			}
			
			long difference = dateChooser.getDate().getTime() - dateChooser.getInitialDate().getTime();
			
			for (CopyCourseOverviewRow row : model.getObjects()) {
				DateChooser start = row.getNewStartDateChooser();
				DateChooser end = row.getNewEndDateChooser();
				
				if (start != null && !start.equals(dateChooser) && start.getDate() != null) {
					if ((moveDatesEvent.isMoveAllAfterCurrentDate() && !start.getInitialDate().before(dateChooser.getInitialDate())) || !moveDatesEvent.isMoveAllAfterCurrentDate()) {
						Date startDate = start.getInitialDate();
						startDate.setTime(startDate.getTime() + difference);
						start.setDate(startDate);
						start.setInitialDate(startDate);
					}
				}
				
				if (end != null && !end.equals(dateChooser) && end.getDate() != null) {
					if ((moveDatesEvent.isMoveAllAfterCurrentDate() && !end.getInitialDate().before(dateChooser.getInitialDate())) || !moveDatesEvent.isMoveAllAfterCurrentDate()) {
						Date endDate = end.getInitialDate();
						endDate.setTime(endDate.getTime() + difference);
						end.setDate(endDate);
						end.setInitialDate(endDate);
					}
				}
			}
			
			dateChooser.setInitialDate(dateChooser.getDate());
		}
		
		private void updateVisibility(CopyCourseOverviewRow row) {
			boolean endChooserVisible = row.getObligationChooser().getSelectedKey().equals(AssessmentObligation.mandatory.name());
			row.getNewEndDateChooser().setVisible(endChooserVisible);
		}
		
	}

}
