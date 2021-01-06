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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.repository.ui.author.ModifyOwnersRemoveTableModel.ModifyOwnersStep1Cols;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: Dec 21, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersStep1 extends BasicStep {
	
	private ModifyOwnersContext context;
	
	public ModifyOwnersStep1(UserRequest ureq, ModifyOwnersContext context) {
		super(ureq);
	
		this.context = context;
		
		setI18nTitleAndDescr("modify.owners.remove", "modify.owners.remove.desc");
		setNextStep(new ModifyOwnersStep2(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(ModifyOwnersContext.CONTEXT_KEY, context);
		return new ModifyOwnerRemoveController(ureq, windowControl, form, stepsRunContext);
	}
	
	
	private class ModifyOwnerRemoveController extends StepFormBasicController {

		private ModifyOwnersContext context;
		
		private ModifyOwnersRemoveTableModel tableModel;
		private FlexiTableElement tableElement; 
		
		private CloseableCalloutWindowController detailsCallOutController;
		
		public ModifyOwnerRemoveController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			this.context = (ModifyOwnersContext) runContext.get(ModifyOwnersContext.CONTEXT_KEY);
			
			setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			
		}

		@Override
		protected void formOK(UserRequest ureq) {
			List<Identity> ownersToRemove = new ArrayList<>();
			tableElement.getMultiSelectedIndex().forEach(index -> ownersToRemove.add(tableModel.getObject(index).getIdentity()));
			
			context.setOwnersToRemove(ownersToRemove);
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			List<ModifyOwnersRemoveTableRow> tableRows = new ArrayList<>();
			
			for (Identity owner : context.getOwners()) {
				ModifyOwnersRemoveTableRow row = new ModifyOwnersRemoveTableRow(owner, context.getOwnersResourcesMap().get(owner));
				FormLink detailsLink = uifactory.addFormLink("details_" + owner.getKey().toString(), "modify.owners.remove.resource.details", null, (FormItemContainer)null, Link.LINK);
				detailsLink.setUserObject(row);
				row.setDetailsLink(detailsLink);
				tableRows.add(row);
			}
			
			FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModifyOwnersStep1Cols.firstName));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModifyOwnersStep1Cols.lastName));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModifyOwnersStep1Cols.nickName));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModifyOwnersStep1Cols.resourcesCount));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModifyOwnersStep1Cols.resourcesDetails));
			
			tableModel = new ModifyOwnersRemoveTableModel(columnModel, tableRows);
			tableElement = uifactory.addTableElement(getWindowControl(), "remove_owners_table", tableModel, getTranslator(), formLayout);
			tableElement.setCustomizeColumns(false);
			tableElement.setMultiSelect(true);
			tableElement.setSelectAllEnable(true);
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source instanceof FormLink && source.getName().contains("details")) {
				removeAsListenerAndDispose(detailsCallOutController);
				
				ModifyOwnersRemoveTableRow row = (ModifyOwnersRemoveTableRow) source.getUserObject();
				CalloutSettings settings = new CalloutSettings(true);
				VelocityContainer detailsContainer = createVelocityContainer("modify_owners_remove_resources_details");
				detailsContainer.contextPut("resources", context.getOwnersResourcesMap().get(row.getIdentity()));

				detailsCallOutController = new CloseableCalloutWindowController(ureq, getWindowControl(), detailsContainer, source.getFormDispatchId(), "", true, "", settings);
				listenTo(detailsCallOutController);
				detailsCallOutController.activate();
			}
		}
	}
}
