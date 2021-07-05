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
package org.olat.repository.ui.author.copy.wizard.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
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
import org.olat.course.editor.overview.OverviewDataModel;
import org.olat.course.editor.overview.OverviewDataModel.OverviewCols;
import org.olat.course.editor.overview.OverviewRow;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.author.copy.wizard.additional.RemindersStep;

/**
 * Initial date: 14.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class NodesOverviewStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.showNodesOverview()) {
			return new NodesOverviewStep(ureq, stepCollection, steps);
		} else {
			return RemindersStep.create(ureq, steps);
		}
	}
	
	public NodesOverviewStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("nodes.overview", null);
		
		// Check or create step collection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "steps.course.nodes.title");
		}
		setStepCollection(stepCollection);
		
		// Next step
		setNextStep(RemindersStep.create(ureq, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new NodesOverviewStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class NodesOverviewStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private FlexiTableElement tableEl;
		private OverviewDataModel dataModel;

		public NodesOverviewStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
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
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.resourceChooser));
			}
									
			dataModel = new OverviewDataModel(columnsModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 15, false, getTranslator(), formLayout);
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
			FormItemContainer tableItems = FormLayoutContainer.createCustomFormLayout("nodeOverviewTableLayout", getTranslator(), velocity_root + "/table_formitems.html");
			tableItems.setRootForm(mainForm);
			formLayout.add(tableItems);
			
			
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("options.empty.resource"));
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("options.reference"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.configure.later"));
			
			SelectionValues copyModes = new SelectionValues(reference, createNew, ignore);
			
			for (OverviewRow row : context.getCourseNodes()) {
				if (isConfigurable(row.getCourseNode())) {
					if (row.getResourceChooser() == null) {
						SingleSelection resourceChooser = uifactory.addDropdownSingleselect("resource_" + row.getCourseNode().getIdent(), tableItems, copyModes.keys(), copyModes.values());
						selectCopyMode(context, row.getCourseNode(), resourceChooser);
						row.setResourceChooser(resourceChooser);
					}
				}
			}
			
			dataModel.setObjects(context.getCourseNodes());
			tableEl.reset();
		}
		
		private boolean isConfigurable(CourseNode courseNode) {
			if (courseNode instanceof WikiCourseNode ||
					courseNode instanceof BlogCourseNode ||
					courseNode instanceof BCCourseNode) {
				return true;
			}
			
			return false;
		}
		
		private void selectCopyMode(CopyCourseContext context, CourseNode courseNode, SingleSelection chooser) {
			String selectKey = "";
			
			if (courseNode instanceof WikiCourseNode) {
				selectKey = getCopyType(context.getCustomWikiCopyType(), context.getWikiCopyType());
			} else if (courseNode instanceof BlogCourseNode) {
				selectKey = getCopyType(context.getCustomBlogCopyType(), context.getBlogCopyType());
			} else if (courseNode instanceof BCCourseNode) {
				selectKey = getCopyType(context.getCustomFolderCopyType(), context.getFolderCopyType());
			}
			
			if (StringHelper.containsNonWhitespace(selectKey)) {
				chooser.select(selectKey, true);
			}
		}
		
		private String getCopyType(CopyType customCopyType, CopyType copyType) {
			if (customCopyType != null) {
				return customCopyType.name();
			}
			
			if (copyType != null) {
				return copyType.name();
			}
			
			return null;
		}
				
	}

}