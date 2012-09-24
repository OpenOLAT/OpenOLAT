/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.user.imp;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElment;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

class ImportStep01 extends BasicStep {

	boolean canCreateOLATPassword;
	boolean newUsers;
	static final String usageIdentifyer = UserImportController.class.getCanonicalName();

	public ImportStep01(UserRequest ureq, boolean canCreateOLATPassword, boolean newUsers) {
		super(ureq);
		this.canCreateOLATPassword = canCreateOLATPassword;
		this.newUsers = newUsers;
		setI18nTitleAndDescr("step1.description", "step1.short.description");
		setNextStep(new ImportStep02(ureq)); //fxdiff: 101 have another step for group addition
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		if (newUsers) {
			return new PrevNextFinishConfig(true, true, true);
		} else {
			return new PrevNextFinishConfig(true, false, false);
		}
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new ImportStepForm01(ureq, windowControl, form, stepsRunContext);
		return stepI;
	}

	private final class ImportStepForm01 extends StepFormBasicController {
		private ArrayList<List<String>> newIdents;
		private List<Object> idents;
		private FormLayoutContainer textContainer;
		private List<UserPropertyHandler> userPropertyHandlers;

		public ImportStepForm01(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			// use custom translator with fallback to user properties translator
			UserManager um = UserManager.getInstance();
			setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
			flc.setTranslator(getTranslator());
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
		// TODO Auto-generated method stub
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@SuppressWarnings({ "unused", "unchecked"})
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FormLayoutContainer formLayoutVertical = FormLayoutContainer.createVerticalFormLayout("vertical", getTranslator());
			formLayout.add(formLayoutVertical);

			idents = (List<Object>) getFromRunContext("idents");
			newIdents = (ArrayList<List<String>>) getFromRunContext("newIdents");
			textContainer = FormLayoutContainer.createCustomFormLayout("step1", getTranslator(), this.velocity_root + "/step1.html");
			formLayoutVertical.add(textContainer);

			int cntall = idents.size();
			int cntNew = newIdents.size();
			int cntOld = cntall - cntNew;
			textContainer.contextPut("newusers", newUsers);
			String overview = getTranslator().translate("import.confirm", new String[] { "" + cntall, "" + cntNew, "" + cntOld });
			textContainer.contextPut("overview", overview);

			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colPos = 0;
			// add special column with information about whether this user
			// exists already or not
			FlexiColumnModel newUserCustomColumnModel = new DefaultFlexiColumnModel("table.user.existing");
			newUserCustomColumnModel.setCellRenderer(new UserNewOldCustomFlexiCellRenderer());
			newUserCustomColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
			tableColumnModel.addFlexiColumnModel(newUserCustomColumnModel);
			colPos++;
			
			// fixed fields:
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.login"));
			colPos++;
			if (canCreateOLATPassword) {
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.pwd"));
			}
			colPos++;
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.lang"));
			colPos++;
			UserManager um = UserManager.getInstance();
			// followed by all properties configured
			// if only mandatory required: check for um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
			userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
					tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey()));
					colPos++;
			}


			FlexiTableDataModel tableDataModel = FlexiTableDataModelFactory.createFlexiTableDataModel(new Model(idents, colPos),
					tableColumnModel);
			FlexiTableElment fte = uifactory.addTableElement("newUsers", tableDataModel, formLayoutVertical);

		}

	}
}

/**
 * 
 * Description:<br>
 * Special cell renderer that uses a css class icon to display the new user type
 * 
 * <P>
 * Initial Date:  21.03.2008 <br>
 * @author gnaegi
 */
class UserNewOldCustomFlexiCellRenderer extends CSSIconFlexiCellRenderer {

	@Override
	protected String getCellValue(Object cellValue) {
		return "";
	}

	@Override
	protected String getCssClass(Object cellValue) {
		if (cellValue instanceof Boolean) {
			if (((Boolean) cellValue).booleanValue()) {
				return "b_new_icon";
			} else {
				return "b_warn_icon";
			}
		}
		return "b_error_icon";
	}

	@Override
	protected String getHoverText(Object cellValue, Translator translator) {
		if (cellValue instanceof Boolean) {
			if (((Boolean) cellValue).booleanValue()) {
				return translator.translate("import.user.new.alt");
			} else {
				return translator.translate("import.user.existing.alt");
			}
		}
		return translator.translate("error");
	}

}