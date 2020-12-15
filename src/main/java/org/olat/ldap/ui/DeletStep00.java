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
package org.olat.ldap.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * second step: show (TreeModel) all useres deleted out of LDAP but still existing in OLAT
 * 
 * <P>
 * Initial Date: 30.07.2008 <br>
 * 
 * @author mrohrer
 */
public class DeletStep00 extends BasicStep{

	private List<Identity> identitiesToDelete;
	private boolean hasIdentitesToDelete;
	
	public DeletStep00(UserRequest ureq, boolean hasIDToDelete, List<Identity> iDToDelete){
		super(ureq);
		setI18nTitleAndDescr("delete.step0.description", null);
		setNextStep(new DeletStep01(ureq));
		identitiesToDelete = iDToDelete;
		hasIdentitesToDelete = hasIDToDelete;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new DeletStepForm00(ureq, windowControl, form, stepsRunContext);
		return stepI;
	}
	
	private final class DeletStepForm00 extends StepFormBasicController{
		private FlexiTableElement tableEl;
		private IdentityFlexiTableModel tableModel;

		public DeletStepForm00(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			UserManager um = UserManager.getInstance();
			setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
			initForm(ureq);
			addToRunContext("hasIdentitiesToDelete", hasIdentitesToDelete);
			addToRunContext("identitiesToDelete", identitiesToDelete);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formOK(UserRequest ureq) {
			Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
			List<Identity> identities = new ArrayList<>(selectedIndexes.size());
			for(Integer index:selectedIndexes) {
				identities.add(tableModel.getObject(index.intValue()));
			}
			addToRunContext("hasIdentitiesToDelete", Boolean.valueOf(!identities.isEmpty()));
			addToRunContext("identitiesToDelete", identities);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), velocity_root + "/delet_step00.html");
			formLayout.add(textContainer);
			// Create selection tree and model
			// Note: since the flexi table is not finished, we have to use the tree here as alternative
			//identitiesToDelete = (List<Identity>) getFromRunContext("identitiesToDelete");
			// use the user short description and not an own identifyer
			String usageIdentifyer = UserShortDescription.class.getCanonicalName();
			List<UserPropertyHandler> handlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
			
			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colPos = 0;
			for (UserPropertyHandler userProperty : handlers) {
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userProperty.i18nColumnDescriptorLabelKey(), colPos++));
			}

			tableModel = new IdentityFlexiTableModel(identitiesToDelete, tableColumnModel, handlers, getLocale());
			tableEl = uifactory.addTableElement(getWindowControl(), "newUsers", tableModel, getTranslator(), formLayout);
			tableEl.setMultiSelect(true);
			tableEl.setPageSize(10000);
			tableEl.setSelectAllEnable(true);
		}
	}
}