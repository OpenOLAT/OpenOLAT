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

import java.util.List;

import org.olat.admin.user.imp.Model.ModelCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractCSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

class ImportStep01 extends BasicStep {
	private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
	private static final String[] theKeys = new String[]{ "", "update", "ignore" };

	private boolean newUsers;
	private final UserImportContext userImportContext;

	public ImportStep01(UserRequest ureq, UserImportContext userImportContext, boolean newUsers) {
		super(ureq);
		this.newUsers = newUsers;
		this.userImportContext = userImportContext;
		setI18nTitleAndDescr("step1.description", "step1.short.description");
		setNextStep(new ImportStep02(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new ImportStepForm01(ureq, windowControl, form, stepsRunContext);
	}

	protected final class ImportStepForm01 extends StepFormBasicController {
		
		public static final int USER_PROPS_OFFSET = 500;
		
		private SingleSelection updateEl;
		private SingleSelection updatePasswordEl;

		@Autowired
		private UserManager userManager;
		
		public ImportStepForm01(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			// use custom translator with fallback to user properties translator
			setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
			flc.setTranslator(getTranslator());
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			Boolean updateUsers = Boolean.FALSE;
			if(updateEl != null && updateEl.isOneSelected() && updateEl.isSelected(1)) {
				updateUsers = Boolean.TRUE; 
			}
			addToRunContext("updateUsers", updateUsers);
			
			Boolean updatePasswords = Boolean.FALSE;
			if(updatePasswordEl != null && updatePasswordEl.isOneSelected() && updatePasswordEl.isSelected(1)) {
				updatePasswords = Boolean.TRUE; 
			}
			addToRunContext("updatePasswords", updatePasswords);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			if(updateEl != null && (!updateEl.isOneSelected() || updateEl.isSelected(0))) {
				updateEl.setErrorKey("form.mandatory.hover", null);
				allOk &= false;
			}
			
			if(updatePasswordEl != null && (!updatePasswordEl.isOneSelected() || updatePasswordEl.isSelected(0))) {
				updatePasswordEl.setErrorKey("form.mandatory.hover", null);
				allOk &= false;
			}
			return allOk;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.setElementCssClass("o_sel_import_users_overview");
			
			FormLayoutContainer formLayoutVertical = FormLayoutContainer.createVerticalFormLayout("vertical", getTranslator());
			formLayout.add(formLayoutVertical);

			@SuppressWarnings("unchecked")
			List<Identity> idents = (List<Identity>) getFromRunContext("idents");
			@SuppressWarnings("unchecked")
			List<UpdateIdentity> updateIdents = (List<UpdateIdentity>) getFromRunContext("updateIdents");
			@SuppressWarnings("unchecked")
			List<TransientIdentity> newIdents = (List<TransientIdentity>) getFromRunContext("newIdents");
			FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("step1", getTranslator(), velocity_root + "/step1.html");
			formLayoutVertical.add(textContainer);

			int cntall = idents.size();
			int cntNew = newIdents.size();
			int cntOld = cntall - cntNew;
			textContainer.contextPut("newusers", newUsers);
			String overview = getTranslator().translate("import.confirm", "" + cntall, "" + cntNew, "" + cntOld);
			textContainer.contextPut("overview", overview);
			textContainer.contextPut("updateusers", updateIdents.isEmpty());
			
			if(userImportContext.getPreselectedOrganisation() != null) {
				String org = userImportContext.getPreselectedOrganisation().getDisplayName();
				uifactory.addStaticTextElement("organisation", org, textContainer);
			}

			if(!updateIdents.isEmpty()) {
				String[] theValues = new String[]{ translate("update.select"), translate("update.yes"), translate("update.no") };
				updateEl = uifactory
						.addDropdownSingleselect("update.user", textContainer, theKeys, theValues, null);
				
				if(userImportContext.canCreateOLATPassword()) {
					updatePasswordEl = uifactory
						.addDropdownSingleselect("update.password", textContainer, theKeys, theValues, null);
				}
			}

			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

			// add special column with information about whether this user
			// exists already or not
			FlexiColumnModel newUserCustomColumnModel = new DefaultFlexiColumnModel(ModelCols.newUser);
			newUserCustomColumnModel.setCellRenderer(new UserNewOldCustomFlexiCellRenderer());
			newUserCustomColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
			tableColumnModel.addFlexiColumnModel(newUserCustomColumnModel);
			
			// fixed fields:
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModelCols.login));
			if (userImportContext.canCreateOLATPassword()) {
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModelCols.cred));
			}
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModelCols.lang));

			// followed by all properties configured
			int colPos = USER_PROPS_OFFSET;
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, true);
			for (UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos++));
			}
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModelCols.expiration, new DateFlexiCellRenderer(getLocale())));

			Model tableDataModel = new Model(idents, tableColumnModel, userPropertyHandlers, getLocale());
			uifactory.addTableElement(getWindowControl(), "newUsers", tableDataModel, getTranslator(), formLayoutVertical);
		}
	}
	
	private static class UserNewOldCustomFlexiCellRenderer extends AbstractCSSIconFlexiCellRenderer {

		@Override
		protected String getCellValue(Object cellValue) {
			return "";
		}

		@Override
		protected String getCssClass(Object cellValue) {
			if (cellValue instanceof Boolean) {
				if (((Boolean) cellValue).booleanValue()) {
					return "o_icon_new";
				} else {
					return "o_icon_warn";
				}
			}
			return "o_icon_error";
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
}