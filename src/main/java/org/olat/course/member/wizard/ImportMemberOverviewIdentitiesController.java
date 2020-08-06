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
package org.olat.course.member.wizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
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
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberOverviewIdentitiesController extends StepFormBasicController {
	
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private List<Identity> oks;
	private List<String> notFounds;
	private boolean isAdministrativeUser;
	private final List<Identity> anonymousUsers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;

	public ImportMemberOverviewIdentitiesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		anonymousUsers = organisationService.getIdentitiesWithRole(OrganisationRoles.guest);
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		
		oks = null;
		notFounds = getListFromRunContext("notFounds", String.class);
		if(notFounds == null) {
			notFounds = new ArrayList<>();
		}
		List<Identity> keys = getListFromRunContext("keyIdentities", Identity.class);
		loadModelByIdentities(keys);

		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_user_import_overview");
		if(notFounds != null && !notFounds.isEmpty()) {
			String page = velocity_root + "/warn_notfound.html";
			FormLayoutContainer warnLayout = FormLayoutContainer.createCustomFormLayout("warnNotFounds", getTranslator(), page);
			warnLayout.setRootForm(mainForm);
			formLayout.add(warnLayout);
			
			StringBuilder sb = new StringBuilder();
			for(String notfound:notFounds) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(notfound);
			}
			String msg = translate("user.notfound", new String[]{sb.toString()});
			addToRunContext("notFounds", sb.toString());
			warnLayout.contextPut("notFounds", msg);
		}
		
		//add the table
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colIndex = 0;
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<>();
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			if(visible) {
				resultingPropertyHandlers.add(userPropertyHandler);
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++));
			}
		}
		
		ImportMemberOverviewDataModel userTableModel = new ImportMemberOverviewDataModel(oks, resultingPropertyHandlers, getLocale(), tableColumnModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "users", userTableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadModelByIdentities(List<Identity> identities) {
		Set<Identity> okSet = new HashSet<>();
		for (Identity ident : identities) {
			if (!validIdentity(ident)) {
				String fullname = userManager.getUserDisplayName(ident);
				if(fullname != null) {
					notFounds.add(fullname);
				}
			} else if (!okSet.contains(ident)) {
				okSet.add(ident);
			}
		}
		oks = new ArrayList<>(okSet);
	}
	
	private boolean validIdentity(Identity ident) {
		return ident != null
				&& ident.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) < 0
				&& !anonymousUsers.contains(ident);
	}

	public boolean validate() {
		return true;
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		if(notFounds == null || notFounds.isEmpty()) {
			removeFromRunContext("notFounds");
		} else {
			addToRunContext("notFounds", notFounds);
		}
		addToRunContext("members", oks);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}