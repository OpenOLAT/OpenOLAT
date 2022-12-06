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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.ldap.LDAPConstants;
import org.olat.ldap.LDAPSyncConfiguration;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * third step: present users which will be deleted out of OLAT
 * 
 * <P>
 * Initial Date: 30.07.2008 <br>
 * 
 * @author mrohrer
 */
public class RemovalStep01 extends BasicStep {
	
	private final boolean delete;

	public RemovalStep01(UserRequest ureq, boolean delete) {
		super(ureq);
		this.delete = delete;
		setI18nTitleAndDescr("delete.step1.description", null);
		setNextStep(Step.NOSTEP);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_FINISH;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new StepForm01(ureq, windowControl, form, stepsRunContext);
	}

	private final class StepForm01 extends StepFormBasicController {
		
		@Autowired
		private LDAPSyncConfiguration syncConfiguration;

		public StepForm01(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_CUSTOM, "step");
			UserManager um = UserManager.getInstance();
			setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			String i18nDesc = delete ? "delete.step1.content" : "inactivate.step1.content";
			setFormDescription(i18nDesc);

			Map<String, String> reqProbertyMap = new HashMap<>(syncConfiguration.getUserAttributeMap());
			Collection<String> reqProberty = reqProbertyMap.values();
			reqProberty.remove(LDAPConstants.LDAP_USER_IDENTIFYER);

			@SuppressWarnings("unchecked")
			List<Identity> identitiesToDelete = (List<Identity>) getFromRunContext("identitiesToDelete");
			for (Identity identityToDelete : identitiesToDelete) {
				List<String> rowData = new ArrayList<>();
				rowData.add(identityToDelete.getName());
				for (String property : reqProberty) {
					rowData.add(identityToDelete.getUser().getProperty(property, null));
				}
			}

			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colPos = 0;
			List<UserPropertyHandler> handlers = new ArrayList<>();
			for (String property : reqProberty) {
				List<UserPropertyHandler> properHandlerList = UserManager.getInstance().getAllUserPropertyHandlers();
				for (UserPropertyHandler userProperty : properHandlerList) {
					if (userProperty.getName().equals(property)) {
						tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userProperty.i18nColumnDescriptorLabelKey(), colPos++));
						handlers.add(userProperty);
					}
				}
			}

			FlexiTableDataModel<Identity> tableDataModel = new IdentityFlexiTableModel(identitiesToDelete, tableColumnModel, handlers, getLocale());
			uifactory.addTableElement(getWindowControl(), "table", tableDataModel, getTranslator(), formLayout);
		}
	}
}