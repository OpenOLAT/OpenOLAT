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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
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
import org.olat.core.util.Util;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.settings.CatalogListModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 21.04.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CatalogStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditCatalog()) {
			return new CatalogStep(ureq, stepCollection, steps);
		} else {
			return DisclaimerStep.create(ureq, stepCollection, steps);
		}
	}
	
	public CatalogStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.catalog.title", null);

		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "steps.general.title");
		}
		setStepCollection(stepCollection);
		
		setNextStep(DisclaimerStep.create(ureq, stepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new CatalogStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class CatalogStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private SingleSelection catalogCopyModeEl;
		
		private FlexiTableElement tableEl;
		private CatalogListModel model;
		
		@Autowired
		private CatalogManager catalogManager;
		

		public CatalogStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
			loadData();
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			catalogCopyModeEl.clearError();
			
			if (!catalogCopyModeEl.isOneSelected()) {
				allOk &= false;
				catalogCopyModeEl.setErrorKey("error.select", null);
			}
			
			return allOk;
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			context.setCustomCatalogCopyType(CopyType.valueOf(catalogCopyModeEl.getSelectedKey()));
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			SelectionValues copyModes = new SelectionValues();
			SelectionValue copy = new SelectionValue(CopyType.copy.name(), translate("options.copy"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.ignore"));
			
			copyModes.add(copy, ignore);
			
			catalogCopyModeEl = uifactory.addRadiosHorizontal("catalog", formLayout, copyModes.keys(), copyModes.values());
			catalogCopyModeEl.setAllowNoSelection(false);
			
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("catalog.path", 0));
			
			model = new CatalogListModel(new ArrayList<>(), columnsModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 200, false, getTranslator(), formLayout);
			tableEl.setCustomizeColumns(false);
			tableEl.setEmptyTableSettings("no.catalog.entries" ,"no.catalog.entries.hint", "o_icon_catalog");
		}

		private void loadData() {
			if (context.getCustomCatalogCopyType() != null) {
				catalogCopyModeEl.select(context.getCustomCatalogCopyType().name(), true);
			} else {
				catalogCopyModeEl.select(CopyType.copy.name(), true);
			}
			
			List<CatalogEntry> catalogEntries = catalogManager.getCatalogCategoriesFor(context.getSourceRepositoryEntry());
			model.setObjects(catalogEntries);
			flc.contextPut("hasContent", Boolean.valueOf(!catalogEntries.isEmpty()));
			tableEl.reset();
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}
		
	}

}
