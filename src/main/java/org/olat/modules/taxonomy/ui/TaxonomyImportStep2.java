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
package org.olat.modules.taxonomy.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableModel.TaxonomyLevelCols;

/**
 * Initial date: Jan 12, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class TaxonomyImportStep2 extends BasicStep {

	public TaxonomyImportStep2(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("import.taxonomy.step.2.title", null);
		setNextStep(new TaxonomyImportStep3(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new TaxonomyImportStep2Controller(ureq, windowControl, form, stepsRunContext);
	}
	
	private class TaxonomyImportStep2Controller extends StepFormBasicController {

		private TaxonomyImportContext context;
		
		private FlexiTableElement tableElement;
		private TaxonomyTreeTableModel reviewTableModel;
		
		public TaxonomyImportStep2Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			this.context = (TaxonomyImportContext) runContext.get(TaxonomyImportContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			// Fire event to get to the next step
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			// Define the column model
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.key));
			DefaultFlexiColumnModel updateWarningCol = new DefaultFlexiColumnModel(TaxonomyLevelCols.updateWarning);
			updateWarningCol.setIconHeader(TaxonomyLevelCols.updateWarning.iconHeader());
			updateWarningCol.setCellRenderer(new TaxonomyImportWarningRenderer());
			columnsModel.addFlexiColumnModel(updateWarningCol);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.path));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.displayName));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.typeIdentifier));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.numOfChildren));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.order));
			DefaultFlexiColumnModel descriptionCol = new DefaultFlexiColumnModel(TaxonomyLevelCols.description);
			descriptionCol.setDefaultVisible(false);
			columnsModel.addFlexiColumnModel(descriptionCol);

			reviewTableModel = new TaxonomyTreeTableModel(columnsModel);
			reviewTableModel.setObjects(context.getReviewList());
			tableElement = uifactory.addTableElement(getWindowControl(), "table", reviewTableModel, 20, false, getTranslator(), formLayout);
			tableElement.setSearchEnabled(true);
			tableElement.setCustomizeColumns(true);
			tableElement.setEmtpyTableMessageKey("table.taxonomy.level.empty");
			tableElement.setNumOfRowsEnabled(true);
			tableElement.setExportEnabled(false);
			tableElement.setPageSize(20);
			
			// Legend to table
	        uifactory.addStaticTextElement("import.taxonomy.review.legend.label", translate("import.taxonomy.review.legend"), formLayout);
		}		
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == tableElement) {
				if (event instanceof FlexiTableSearchEvent) {
		            String searchString = ((FlexiTableSearchEvent) event).getSearch();
		            reviewTableModel.filter(searchString, null);
		            tableElement.reset();
		        }
			}
		}
	}
}
