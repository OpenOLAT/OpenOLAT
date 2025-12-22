/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.ui.wizard.UsersOverviewTableModel.UserOverviewCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AddMembersOverviewController extends StepFormBasicController {
	
	public static final int USER_PROPS_OFFSET = UsersOverviewController.USER_PROPS_OFFSET;
	public static final String usageIdentifyer = UsersOverviewController.usageIdentifyer;

	private FlexiTableElement tableEl;
	private UsersOverviewTableModel tableModel;
	
	private final AddProgramMembersContext membersContext;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public AddMembersOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, AddProgramMembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_overview");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.membersContext = membersContext;

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initMessage();
		initTableForm(formLayout);
	}
	
	private void initMessage() {
		List<UserToCertify> selectedIdentities = membersContext.getSelectedIdentities();	
		long numToCertify = selectedIdentities == null ? 0 : selectedIdentities.size();
		long numFutureCertified = numToCertify;
		if(selectedIdentities != null && numToCertify > 0) {
			numFutureCertified = selectedIdentities.stream()
					.filter(t -> t.currentStatus() == null || t.currentStatus() == UserMembershipStatus.CANDIDATE || t.currentStatus() == UserMembershipStatus.ALUMNI)
					.count();
		}
		
		String i18nKey = numFutureCertified <= 1 ? "user.to.certify.descr.singular" : "user.to.certify.descr.plural";
		setFormDescription(i18nKey, new String[] { Long.toString(numToCertify), Long.toString(numFutureCertified) });
	}

	private void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.modifications,
				new ModificationCellRenderer(getTranslator())));
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.currentMembership,
				new UserMembershipStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.newMembership,
				new UserMembershipStatusCellRenderer(getTranslator())));
		
		tableModel = new UsersOverviewTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(false);
		tableEl.setSelectAllEnable(false);
	}
	
	private void loadModel() {
		List<UserToCertify> selectedIdentities = membersContext.getSelectedIdentities();
		List<UserRow> rows = selectedIdentities == null ? List.of() : selectedIdentities.stream()
				.map(id -> new UserRow(id.identity(), id.currentStatus(), userPropertyHandlers, getLocale()))
				.toList();
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		List<UserToCertify> selectedIdentities = membersContext.getSelectedIdentities();
		List<Identity> identitiesToCertify = new ArrayList<>(selectedIdentities.size());
		for(UserToCertify selectedIdentity:selectedIdentities) {
			if(selectedIdentity.currentStatus() != UserMembershipStatus.ACTIVE) {
				identitiesToCertify.add(selectedIdentity.identity());
			}
		}
		membersContext.setIdentitiesToCertify(identitiesToCertify);
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
