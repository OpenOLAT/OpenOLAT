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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.repository.ui.author.ModifyOwnersReviewTableModel.ModifyOwnersReviewCols;
import org.olat.repository.ui.author.ModifyOwnersReviewTableRow.ModifyOwnersReviewState;

/**
 * Initial date: Dec 21, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersStep3 extends BasicStep {

	public ModifyOwnersStep3(UserRequest ureq) {
		super(ureq);
		
		setI18nTitleAndDescr("modify.owners.review", null);
		setNextStep(new ModifyOwnersStep4(ureq));
		
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new AuthorListEditOwnersStep3Controller(ureq, windowControl, form, stepsRunContext);
	}
	
	
	
	private static class AuthorListEditOwnersStep3Controller extends StepFormBasicController {

		private ModifyOwnersContext context;
		
		private FlexiTableElement tableEl;
		private ModifyOwnersReviewTableModel tableModel;
		
		public AuthorListEditOwnersStep3Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			this.context = (ModifyOwnersContext) runContext.get(ModifyOwnersContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			
			DefaultFlexiColumnModel resourceColumn = new DefaultFlexiColumnModel(ModifyOwnersReviewCols.resourceOrIdentity);
			DefaultFlexiColumnModel stateColumn = new DefaultFlexiColumnModel(ModifyOwnersReviewCols.state);
			resourceColumn.setCellRenderer(new TreeNodeFlexiCellRenderer(new ModifyOwnersReviewResourceRenderer()));
			stateColumn.setCellRenderer(new ModifyOwnersReviewStateRenderer());
			
			columnModel.addFlexiColumnModel(resourceColumn);
			columnModel.addFlexiColumnModel(stateColumn);
			
			List<ModifyOwnersReviewTableRow> tableRows = new ArrayList<>();
			
			for (AuthoringEntryRow resource : context.getAuthoringEntryRows()) {
				ModifyOwnersReviewTableRow parent = new ModifyOwnersReviewTableRow();
				parent.setResource(resource);
				parent.setHasChildren(true);
				parent.setState(ModifyOwnersReviewState.resource);
				tableRows.add(parent);
				
				for (Identity identity : context.getAllIdentities(getLocale())) {
					ModifyOwnersReviewTableRow child = new ModifyOwnersReviewTableRow();
					child.setIdentity(identity);
					child.setParent(parent);
					child.setState(getState(identity, parent));
					child.setHasChildren(false);
					tableRows.add(child);
				}
			}
			
			tableModel = new ModifyOwnersReviewTableModel(columnModel, tableRows);
			if (tableRows.size() > 20) {
				tableModel.closeAll();
			}
			
			tableEl = uifactory.addTableElement(getWindowControl(), "reviewTable", tableModel, getTranslator(), formLayout);
			tableEl.setElementCssClass("o_sel_modify_owners_review");
			tableEl.setCustomizeColumns(false);
			tableEl.setNumOfRowsEnabled(false);
		}
		
		private ModifyOwnersReviewState getState(Identity identity, ModifyOwnersReviewTableRow parent) {
			boolean isAlreadyOwner = false;
			
			if (context.getOwnersResourcesMap().get(identity) != null) {
				isAlreadyOwner = context.getOwnersResourcesMap().get(identity).contains(parent.getResource());
			}
			
			boolean isRemoved = context.getOwnersToRemove().contains(identity);
			boolean isAdded = false;
			if (context.getOwnersToAdd() != null) {
				isAdded = context.getOwnersToAdd().contains(identity);
			}
			
			if (isAlreadyOwner) {
				if (isRemoved && isAdded) {
					return ModifyOwnersReviewState.granted;
				} else if (isRemoved) {
					parent.increaseRemovedOwners();
					return ModifyOwnersReviewState.removed;
				} else {
					return ModifyOwnersReviewState.granted;
				}
			} else {
				if (isRemoved && isAdded) {
					return ModifyOwnersReviewState.denied;
				} else if (isAdded) {
					parent.increaseAddedOwners();
					return ModifyOwnersReviewState.added;
				} else {
					return ModifyOwnersReviewState.denied;
				}
			}
		}
		
	}

}
