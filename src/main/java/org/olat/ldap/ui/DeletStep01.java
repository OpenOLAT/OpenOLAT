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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelImpl;
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
import org.olat.ldap.LDAPLoginModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * third step: present users which will be deleted out of OLAT
 * 
 * <P>
 * Initial Date: 30.07.2008 <br>
 * 
 * @author mrohrer
 */
public class DeletStep01 extends BasicStep {

	public DeletStep01(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("delete.step1.description", null);
		setNextStep(Step.NOSTEP);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getInitialPrevNextFinishConfig()
	 */
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_FINISH;
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.generic.wizard.StepsRunContext,
	 *      org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new DeletStepForm01(ureq, windowControl, form, stepsRunContext);
		return stepI;
	}

	private final class DeletStepForm01 extends StepFormBasicController {
		private FormLayoutContainer textContainer;
		boolean hasIdentitesToDelete;
		private FlexiTableDataModel<List<String>> tableDataModel;
		private List<Identity> identitiesToDelete;

		public DeletStepForm01(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			UserManager um = UserManager.getInstance();
			setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
		// TODO Auto-generated method stub

		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			hasIdentitesToDelete = (Boolean) getFromRunContext("hasIdentitiesToDelete");
			textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/delet_step01.html");
			formLayout.add(textContainer);
			textContainer.contextPut("hasIdentitesToDelete", hasIdentitesToDelete);
			if (!hasIdentitesToDelete) {
				setNextStep(Step.NOSTEP);
				return;
			}

			Map<String, String> reqProbertyMap = new HashMap<String,String>(LDAPLoginModule.getUserAttributeMapper());
			Collection<String> reqProberty = reqProbertyMap.values();
			reqProberty.remove(LDAPConstants.LDAP_USER_IDENTIFYER);
			List<List<String>> mergedDataChanges = new ArrayList<List<String>>();

			identitiesToDelete = (List<Identity>) getFromRunContext("identitiesToDelete");
			for (Identity identityToDelete : identitiesToDelete) {
				List<String> rowData = new ArrayList<>();
				rowData.add(identityToDelete.getName());
				for (String property : reqProberty) {
					rowData.add(identityToDelete.getUser().getProperty(property, null));
				}
				mergedDataChanges.add(rowData);
			}

			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colPos = 0;
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("username", colPos++));
			for (String property : reqProberty) {
				List<UserPropertyHandler> properHandlerList = UserManager.getInstance().getAllUserPropertyHandlers();
				for (UserPropertyHandler userProperty : properHandlerList) {
					if (userProperty.getName().equals(property)) {
						tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userProperty.i18nColumnDescriptorLabelKey(), colPos++));
					}
				}
			}

			tableDataModel = new FlexiTableDataModelImpl<List<String>>(new IdentityFlexiTableModel(mergedDataChanges, colPos + 1), tableColumnModel);
			uifactory.addTableElement(getWindowControl(), "newUsers", tableDataModel, formLayout);
		}

	}

}
