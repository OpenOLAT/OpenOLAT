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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.core.gui.components.link.Link;
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
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.editor.EditorMainController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.manager.NodeRightServiceImpl;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeDatesListController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.ExecutionType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewDataModel;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewDataModel.CopyCourseOverviewCols;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.author.copy.wizard.DateWithLabel;
import org.olat.repository.ui.author.copy.wizard.DateWithLabelRenderer;
import org.olat.repository.ui.author.copy.wizard.dates.MoveAllDatesController;

/**
 * Initial date: 10.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CourseOverviewStep extends BasicStep {

	public static Step create(UserRequest ureq, CopyCourseSteps steps) {
		if (steps.isAdvancedMode() && steps.showNodesOverview()) {
			return new CourseOverviewStep(ureq, steps);
		}
		return CatalogStep.create(ureq, null, steps);
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
	
	private static class CourseOverviewStepController extends StepFormBasicController implements FlexiTableComponentDelegate {

		private CopyCourseContext context;
		
		private FormLayoutContainer dateWarning;
		private FormLink shiftAllDates;
		private FlexiTableElement tableEl;
		private CopyCourseOverviewDataModel dataModel;
		
		private CloseableModalController cmc;
		private MoveAllDatesController moveAllDatesController;
		private CourseNodeDatesListController courseNodeDatesListController;
		
		private final DueDateConfigFormatter dueDateConfigFormatter;
		
		public CourseOverviewStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(NodeRightsController.class, getLocale(), getTranslator()));
			
			this.dueDateConfigFormatter = DueDateConfigFormatter.create(getLocale());
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			courseNodeDatesListController = new CourseNodeDatesListController(ureq, wControl, context);
			
			initForm(ureq);
			updateDateWarningUI();
		}

		@Override
		protected void formOK(UserRequest ureq) {			
			saveDatesToContext(context);
			removeFormElementsFromContext(context);
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);			
		}
		
		private void saveDatesToContext(CopyCourseContext context) {
			for (CopyCourseOverviewRow row : context.getCourseNodes()) {
				if (row.getObligationChooser() != null) {
					row.setAssesssmentObligation(AssessmentObligation.valueOf(row.getObligationChooser().getSelectedKey()));
				}
				
				if (row.getResourceChooser() != null) {
					row.setResourceCopyType(CopyType.valueOf(row.getResourceChooser().getSelectedKey()));
				}
				
				FormItem newStartDateChooser = row.getNewStartDateChooser();
				if (newStartDateChooser instanceof DateChooser) {
					row.setNewStartDate(((DateChooser)newStartDateChooser).getDate());
				}
				
				FormItem newEndDateChooser = row.getNewEndDateChooser();
				if (newEndDateChooser instanceof DateChooser) {
					row.setNewEndDate(((DateChooser)newEndDateChooser).getDate());
				}
			}
			
			context.setCustomConfigsLoaded(true);
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
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			boolean canSwitchAllDates = ExecutionType.beginAndEnd != context.getExecutionType()
					|| context.getSourceRepositoryEntry().getLifecycle() == null 
					|| !context.getSourceRepositoryEntry().getLifecycle().isPrivateCycle();
			String dateWarningText = canSwitchAllDates
					? translate("date.early.warning")
					: translate("date.early.warning.period");
			dateWarning = FormLayoutContainer.createCustomFormLayout("date_warning", getTranslator(), velocity_root + "/date_warning.html");
			dateWarning.contextPut("warning", dateWarningText);
			dateWarning.setVisible(false);
			formLayout.add(dateWarning);
			
			shiftAllDates = uifactory.addFormLink("shift.all.dates", formLayout, Link.BUTTON);
			shiftAllDates.setElementCssClass("pull-right");
			shiftAllDates.setVisible(canSwitchAllDates);
			
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
			
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyCourseOverviewCols.earliestDate, new DateWithLabelRenderer(getLocale())));
			
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
		}

		@Override
		public boolean isDetailsRow(int row, Object rowObject) {
			if(rowObject instanceof CopyCourseOverviewRow) {
				CopyCourseOverviewRow ccor = (CopyCourseOverviewRow)rowObject;
				return ccor.getCourseNode() != null && ccor.getCourseNode().hasDates();
			}
			return false;
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
			
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("options.empty.resource"));
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("options.reference"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.configure.later"));
			SelectionValue copy = new SelectionValue(CopyType.copy.name(), translate("options.copy"));
			SelectionValue copyContent = new SelectionValue(CopyType.copy.name(), translate("options.copy.content"));
			SelectionValue ignoreContent = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.content"));
			SelectionValue copyAssignmentAndSolution = new SelectionValue(CopyType.copy.name(), translate("options.copy.assignment.solution"));
			SelectionValue ignoreAssignmentAndSolution = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.assignment.solution"));
			
			SelectionValues copyModes = null;
			
			for (CopyCourseOverviewRow row : context.getCourseNodes()) {
				if (row.getLearningPathConfigs() != null) {
					if (row.getObligationChooser() == null && row.getLearningPathConfigs().getObligation() != null && !row.getLearningPathConfigs().getAvailableObligations().isEmpty()) {
						Set<AssessmentObligation> availableObligations = row.getLearningPathConfigs().getAvailableObligations();
						SelectionValues obligationModes = new SelectionValues();
						if (availableObligations.contains(AssessmentObligation.mandatory)) {
							obligationModes.add(new SelectionValue(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
						}
						if (availableObligations.contains(AssessmentObligation.optional)) {
							obligationModes.add(new SelectionValue(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
						}
						if (availableObligations.contains(AssessmentObligation.excluded)) {
							obligationModes.add(new SelectionValue(AssessmentObligation.excluded.name(), translate("config.obligation.excluded")));
						}
						if (availableObligations.contains(AssessmentObligation.evaluated)) {
							obligationModes.add(new SelectionValue(AssessmentObligation.evaluated.name(), translate("config.obligation.evaluated")));
						}
						
						SingleSelection obligationChooser = uifactory.addDropdownSingleselect("obligation_" + row.getCourseNode().getIdent(), tableItems, obligationModes.keys(), obligationModes.values());
						obligationChooser.setUserObject(row);
						obligationChooser.addActionListener(FormEvent.ONCHANGE);
						if (obligationChooser.containsKey(row.getLearningPathConfigs().getObligation().name())) {
							obligationChooser.select(row.getLearningPathConfigs().getObligation().name(), true);
						} else {
							obligationChooser.select(obligationChooser.getKey(0), true);
						}
						row.setObligationChooser(obligationChooser);
					}
						
					// Start date
					DueDateConfig startDateConfig = row.getStart();
					if (startDateConfig != null) {
						if (DueDateConfig.isRelative(startDateConfig)) {
							StaticTextElement staticElement = uifactory.addStaticTextElement("start_" + row.getCourseNode().getIdent(), null, dueDateConfigFormatter.formatRelativDateConfig(startDateConfig), tableItems);
							row.setNewStartDateChooser(staticElement);
						} else if (row.getLearningPathConfigs().isRelativeDates()) {
							// not possible to change
						} else {
							Date startDate = calculateDate(startDateConfig.getAbsoluteDate(), context.getDateDifference());
							DateChooser startDateChooser = uifactory.addDateChooser("start_" + row.getCourseNode().getIdent(), startDate, tableItems);
							startDateChooser.setUserObject(row);
							startDateChooser.addActionListener(FormEvent.ONCHANGE);
							startDateChooser.setInitialDate(startDate);
							startDateChooser.setKeepTime(true);
							row.setNewStartDateChooser(startDateChooser);
							if (row.getNewStartDate() != null) {
								startDateChooser.setDate(new Date(row.getNewStartDate().getTime()));
							}
						}
					}
					
					// End date
					DueDateConfig endDateConfig = row.getEnd();
					if (endDateConfig != null) {
						if (DueDateConfig.isRelative(endDateConfig)) {
							StaticTextElement staticElement = uifactory.addStaticTextElement("end_" + row.getCourseNode().getIdent(), null, dueDateConfigFormatter.formatRelativDateConfig(endDateConfig), tableItems);
							row.setNewStartDateChooser(staticElement);
						} else if (row.getLearningPathConfigs().isRelativeDates()) {
							// not possible to change
						} else {
							Date endDate = calculateDate(row.getEnd().getAbsoluteDate(), context.getDateDifference());
							DateChooser endDateChooser = uifactory.addDateChooser("end_" + row.getCourseNode().getIdent(), endDate, tableItems);
							endDateChooser.setUserObject(row);
							endDateChooser.addActionListener(FormEvent.ONCHANGE);
							endDateChooser.setInitialDate(endDate);
							endDateChooser.setKeepTime(true);
							endDateChooser.setVisible(row.getObligationChooser() != null && row.getObligationChooser().getSelectedKey().equals(AssessmentObligation.mandatory.name()));
							row.setNewEndDateChooser(endDateChooser);
							if (row.getNewEndDate() != null) {
								endDateChooser.setDate(new Date(row.getNewEndDate().getTime()));
							}
						}
					}
				}
				
				if (isConfigurable(row.getCourseNode())) {
					if (row.getResourceChooser() == null) {
						if (row.getCourseNode() instanceof BCCourseNode) {
							copyModes = new SelectionValues(copyContent, ignoreContent);
						} else if (row.getCourseNode() instanceof GTACourseNode) {
							copyModes = new SelectionValues(copyAssignmentAndSolution, ignoreAssignmentAndSolution);
						} else if (row.getCourseNode() instanceof IQTESTCourseNode) {
							copyModes = new SelectionValues(reference, copy, ignore);
						} else {
							copyModes = new SelectionValues(reference, createNew, ignore);
						}
						
						SingleSelection resourceChooser = uifactory.addDropdownSingleselect("resource_" + row.getCourseNode().getIdent(), tableItems, copyModes.keys(), copyModes.values());
						selectCopyMode(context, row.getCourseNode(), resourceChooser);
						row.setResourceChooser(resourceChooser);
					}
				}
				
				DateWithLabel earliestCourseNodeDate = getEarliestDateWithLabel(row, true);
				setRowEarliestDate(row, earliestCourseNodeDate);
			}
			
			dataModel.setObjects(context.getCourseNodes());
			tableEl.reset();
		}
		
		private boolean isConfigurable(CourseNode courseNode) {
			if (courseNode instanceof WikiCourseNode ||
					courseNode instanceof BlogCourseNode ||
					courseNode instanceof BCCourseNode ||
					courseNode instanceof IQTESTCourseNode ||	
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
			} else if (courseNode instanceof IQTESTCourseNode) {
				selectKey = getCopyType(context.getTestCopyType());
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
			} else if (source == shiftAllDates) {
				doOpenShiftAllDates(ureq);
			} else if (source instanceof SingleSelection) {
				SingleSelection sourceSelection = (SingleSelection) source;
				
				if (sourceSelection.getName().startsWith("obligation")) {
					CopyCourseOverviewRow row = (CopyCourseOverviewRow) sourceSelection.getUserObject();
					updateVisibility(row);
					
					saveDatesToContext(context);
				}
			} else if (source instanceof DateChooser) {
				if (source.getName().startsWith("start_") || source.getName().startsWith("end_")) {
					DateChooser sourceDateChooser = (DateChooser) source;
					if (sourceDateChooser.getUserObject() instanceof CopyCourseOverviewRow) {
						CopyCourseOverviewRow row = (CopyCourseOverviewRow) sourceDateChooser.getUserObject();
						shiftDate(ureq, row, sourceDateChooser);
					}
					saveDatesToContext(context);
					courseNodeDatesListController.updateDates(ureq);
				}
			}
			
			updateDateWarningUI();
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if (source == moveAllDatesController) {
				if (event.equals(Event.DONE_EVENT)) {
					shiftAllDates(ureq, moveAllDatesController.getCurrentDateDifference(), null);
				}
				
				saveDatesToContext(context);
				courseNodeDatesListController.updateDates(ureq);
				
				cmc.deactivate();
				cleanUp();
			} else if (source == cmc) {
				cmc.deactivate();
				cleanUp();
			}
			
			updateDateWarningUI();
		}
		
		private void cleanUp() {	
			removeAsListenerAndDispose(moveAllDatesController);
			removeAsListenerAndDispose(cmc);
			moveAllDatesController = null;
			cmc = null;
		}
		
		private void doOpenShiftAllDates(UserRequest ureq) {
			moveAllDatesController = new MoveAllDatesController(ureq, getWindowControl(), context, getEarliestDate());
			listenTo(moveAllDatesController);
			
			cmc = new CloseableModalController(getWindowControl(), "close", moveAllDatesController.getInitialComponent(), true, translate("shift.all.dates"));
			listenTo(cmc);
			cmc.activate();
		}
		
		private DateWithLabel getEarliestDate() {
			DateWithLabel earliestDate = null;
			for (CopyCourseOverviewRow row : context.getCourseNodes()) {
				DateWithLabel rowEarliestDate = row.getEarliestDateWithLabel();
				if (earliestDate == null || (rowEarliestDate != null && rowEarliestDate.getDate().before(earliestDate.getDate()))) {
					earliestDate = rowEarliestDate;
				}
			}
			return earliestDate;
		}
		
		private void shiftDate(UserRequest ureq, CopyCourseOverviewRow row, DateChooser dateChooser) {
			dateChooser.setInitialDate(dateChooser.getDate());
			saveDatesToContext(context);
			courseNodeDatesListController.updateDates(ureq);
			
			DateWithLabel earliestDateWithLabel = getEarliestDateWithLabel(row, false);
			DateChooser startEl = row.getNewStartDateChooser() instanceof DateChooser? (DateChooser)row.getNewStartDateChooser(): null;
			if (startEl != null && startEl.getDate() != null) {
				if (earliestDateWithLabel == null || earliestDateWithLabel.getDate()  == null || earliestDateWithLabel.getDate().after(startEl.getDate())) {
					earliestDateWithLabel = new DateWithLabel(new Date(startEl.getDate().getTime()), "table.header.start", row.getCourseNode().getShortName());
				}
			}
			DateChooser endEl = row.getNewEndDateChooser() instanceof DateChooser? (DateChooser)row.getNewEndDateChooser(): null;
			if (endEl != null && endEl.getDate() != null) {
				if (earliestDateWithLabel == null || earliestDateWithLabel.getDate()  == null || earliestDateWithLabel.getDate().after(endEl.getDate())) {
					earliestDateWithLabel = new DateWithLabel(new Date(endEl.getDate().getTime()), "table.header.end", row.getCourseNode().getShortName());
				}
			}
			
			setRowEarliestDate(row, earliestDateWithLabel);
		}

		private void setRowEarliestDate(CopyCourseOverviewRow row, DateWithLabel earliestDateWithLabel) {
			row.setEarliestDate(earliestDateWithLabel);
			if (earliestDateWithLabel != null) {
				if (earliestDateWithLabel.needsTranslation() &&  StringHelper.containsNonWhitespace(earliestDateWithLabel.getLabel())) {
					earliestDateWithLabel.setLabel(translate(earliestDateWithLabel.getLabel()));
				}
			}
		}
		
		private void shiftAllDates(UserRequest ureq, long difference, DateChooser ignore) {
			for (CopyCourseOverviewRow row : dataModel.getObjects()) {
				DateChooser start = row.getNewStartDateChooser() instanceof DateChooser? (DateChooser)row.getNewStartDateChooser(): null;
				DateChooser end = row.getNewEndDateChooser() instanceof DateChooser? (DateChooser)row.getNewEndDateChooser(): null;
				
				if (start != null && start.getDate() != null && (ignore == null || ignore != start)) {
					Date startDate = new Date(start.getDate().getTime() + difference);
					start.setDate(startDate);
					start.setInitialDate(startDate);
				}
				
				if (end != null && end.getDate() != null && (ignore == null || ignore != end)) {
					Date endDate =  new Date(end.getDate().getTime() + difference);
					end.setDate(endDate);
					end.setInitialDate(endDate);
				}
				
				DateWithLabel earliestDate = row.getEarliestDateWithLabel();
				if (earliestDate != null) {
					earliestDate.setDate(new Date(earliestDate.getDate().getTime() + difference));
				}
			}	
			
			saveDatesToContext(context);
			courseNodeDatesListController.updateDates(ureq);
		}
		
		private void updateVisibility(CopyCourseOverviewRow row) {
			boolean endChooserVisible = row.getObligationChooser().getSelectedKey().equals(AssessmentObligation.mandatory.name());
			if(row.getNewEndDateChooser() != null) {
				row.getNewEndDateChooser().setVisible(endChooserVisible);
			}
		}
		
		private DateWithLabel getEarliestDateWithLabel(CopyCourseOverviewRow row, boolean withStartEnd) {
			CourseNode courseNode = row.getCourseNode();
			
			// If there are no dates, stop here
			if (courseNode == null) {
				return null;
			}
			
			List<DateWithLabel> dates = new ArrayList<>();
			
			// Load course node dependant dates
			if (courseNode.getNodeSpecificDatesWithLabel().stream().map(Entry::getValue).anyMatch(DueDateConfig::isDueDate)) {
				
				for (Entry<String, DueDateConfig> innerDate : courseNode.getNodeSpecificDatesWithLabel()) {
					DueDateConfig dueDateConfig = innerDate.getValue();
					
					if (DueDateConfig.isRelative(dueDateConfig)) {
						// Don't do anything in this case yet
					} else if(DueDateConfig.isAbsolute(dueDateConfig)) {
						dates.add(new DateWithLabel(new Date(dueDateConfig.getAbsoluteDate().getTime()), innerDate.getKey(), courseNode.getShortName()));
					}
				}
			}
			
			// Load course node config
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			
			// Load potential highscore data
			DueDateConfig startDateConfig = courseNode.getDueDateConfig(HighScoreEditController.CONFIG_KEY_DATESTART);
			if (DueDateConfig.isDueDate(startDateConfig)) {
				if (DueDateConfig.isRelative(startDateConfig)) {
					// Do not do anything in this case yet
				} else if (DueDateConfig.isAbsolute(startDateConfig)) {
					dates.add(new DateWithLabel(startDateConfig.getAbsoluteDate(), "highscore.date.start", courseNode.getShortName()));
				}
			}
			
			// User rights
			List<NodeRightType> nodeRightTypes = courseNode.getNodeRightTypes();
			Map<String, Object> potentialNodeRights = config.getConfigEntries(NodeRightServiceImpl.KEY_PREFIX);
			
			// Create map for easier handling
			Map<String, NodeRightType> nodeRightTypesMap = nodeRightTypes.stream().collect(Collectors.toMap(NodeRightType::getIdentifier, Function.identity()));
			Map<NodeRightType, List<NodeRightGrant>> nodeRightGrants = new HashMap<>();
			
			for (Map.Entry<String, Object> entry : potentialNodeRights.entrySet()) {
				if (!(entry.getValue() instanceof NodeRight)) {
					continue;
				}
				
				NodeRight nodeRight = (NodeRight) entry.getValue();
				
				
				if (nodeRight.getGrants() != null) {
					for (NodeRightGrant grant : nodeRight.getGrants()) {
						// Remove any rights associated with an identity or group, they won't be copied
						if (grant.getBusinessGroupRef() != null || grant.getIdentityRef() != null) {
							continue;
						}
						
						// If the right does not include any date, don't list it
						boolean hasDate = grant.getStart() != null || grant.getEnd() != null;
						if (!hasDate) {
							continue;
						}
						
						// Put the grant into the map
						NodeRightType type = nodeRightTypesMap.get(nodeRight.getTypeIdentifier());
						
						if (type != null) {
							List<NodeRightGrant> grants = nodeRightGrants.get(type);
							
							if (grants == null) {
								grants = new ArrayList<>();
							}
							
							grants.add(grant);
							
	 						nodeRightGrants.put(type, grants);
						}
					}
				}
			}
			
			for (NodeRightType type : nodeRightGrants.keySet()) {
				for (NodeRightGrant grant : nodeRightGrants.get(type)) {
					String rightRole = grant.getRole() != null ? translate("role." + grant.getRole().name().toLowerCase()) : "";
					String rightTitle = type.getTranslatorBaseClass() != null ? Util.createPackageTranslator(type.getTranslatorBaseClass(), getLocale()).translate(type.getI18nKey()) : type.getIdentifier();
					
					String label = rightTitle + (StringHelper.containsNonWhitespace(rightRole) ? " - " + rightRole : "");
					
					Date start = grant.getStart();
					if (start != null) {
						start = new Date(start.getTime());
					}
					
					DateWithLabel userRightDate = new DateWithLabel(start, label, courseNode.getShortTitle());
					userRightDate.setNeedsTranslation(false);
					dates.add(userRightDate);
				}
			}
			
			
			if (withStartEnd) {
				// Start date
				DueDateConfig startConfig = row.getStart();
				if (startConfig != null) {
					if (DueDateConfig.isRelative(startConfig)) {
						// Nothing to do yet
					} else if (row.getLearningPathConfigs().isRelativeDates()) {
						// Nothing to do yet
					} else {
						// Date startDate = calculateDate(startConfig.getAbsoluteDate(), context.getDateDifference());
						Date startDate = startConfig.getAbsoluteDate();
						
						if (startDate != null) {
							dates.add(new DateWithLabel(startDate, "table.header.start", courseNode.getShortName()));
						}
					}
				}
				
				// End date
				DueDateConfig endDateConfig = row.getEnd();
				if (endDateConfig != null) {
					if (DueDateConfig.isRelative(endDateConfig)) {
						// Nothing to do yet
					} else if (row.getLearningPathConfigs().isRelativeDates()) {
						// Nothing to do yet
					} else {
						// Date endDate = calculateDate(row.getEnd().getAbsoluteDate(), context.getDateDifference());
						Date endDate = row.getEnd().getAbsoluteDate();
						
						if (endDate != null) {
							dates.add(new DateWithLabel(endDate, "table.header.end", courseNode.getShortName()));
						}
					}
				}
			}
			
			DateWithLabel earliestDate = dates.stream()
					.min(Comparator.comparing(DateWithLabel::getDate))
					.orElse(null);
			
			if (earliestDate != null) {
				long dateDifference = context.getDateDifference(courseNode.getIdent());
				if (dateDifference == 0l) {
					dateDifference = context.getDateDifference();
				}
				earliestDate.setDate(new Date(earliestDate.getDate().getTime() + dateDifference));
			}
			
			return earliestDate;
		}
		
		private void updateDateWarningUI() {
			LocalDate today = LocalDate.now();
			boolean datesInPast = dataModel.getObjects().stream()
					.map(CopyCourseOverviewRow::getEarliestDateWithLabel)
					.filter(Objects::nonNull)
					.map(DateWithLabel::getDate)
					.filter(Objects::nonNull)
					.anyMatch(date -> today.isAfter(DateUtils.toLocalDate(date)));
			dateWarning.setVisible(datesInPast);
		}
		
	}

}
