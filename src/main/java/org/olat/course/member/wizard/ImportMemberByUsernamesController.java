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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
public class ImportMemberByUsernamesController extends StepFormBasicController {
	
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();

	private TextElement idata;
	private FlexiTableElement tableEl;
	private ImportMemberOverviewDataModel userTableModel;
	
	private FormLink backLink;
	private FormLayoutContainer inputContainer;
	private FormLayoutContainer tableContainer;

	private boolean isAdministrativeUser;
	private final List<Identity> anonymousUsers;

	private List<String> notFoundNames;
	private List<Identity> identitiesList;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;

	public ImportMemberByUsernamesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		anonymousUsers = organisationService.getIdentitiesWithRole(OrganisationRoles.guest);
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		initForm (ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// input field
		inputContainer = FormLayoutContainer.createDefaultFormLayout("input", getTranslator());
		formLayout.add(inputContainer);
		
		idata = uifactory.addTextAreaElement("addusers", "form.addusers", -1, 15, 40, true, false, " ", inputContainer);
		idata.setElementCssClass("o_sel_user_import");
		idata.setExampleKey ("form.names.example", null);
		
		// table for duplicates
		String page = velocity_root + "/warn_duplicates.html";
		tableContainer = FormLayoutContainer.createCustomFormLayout("table", getTranslator(), page);
		formLayout.add(tableContainer);
		tableContainer.setVisible(false);
		
		// user search form
		backLink = uifactory.addFormLink("back", tableContainer);
		backLink.setIconLeftCSS("o_icon o_icon_back");
		
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
		
		userTableModel = new ImportMemberOverviewDataModel(resultingPropertyHandlers, getLocale(), tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "users", userTableModel, 100, false, getTranslator(), tableContainer);
		tableEl.setCustomizeColumns(false);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			doBackToInput();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		String logins = idata.getValue();
		if(tableContainer.isVisible()) {
			List<Identity> all = new ArrayList<>(identitiesList);
			all.addAll(selectDuplicates());
			addToRunContext("keyIdentities", all);
			addToRunContext("notFounds", notFoundNames);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if(processInput(logins)) {
			tableContainer.setVisible(true);
			inputContainer.setVisible(false);
		} else {
			addToRunContext("keyIdentities", new ArrayList<>(identitiesList));
			addToRunContext("notFounds", notFoundNames);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	private void doBackToInput() {
		tableContainer.setVisible(false);
		inputContainer.setVisible(true);
		identitiesList = null;
	}
	
	private List<Identity> selectDuplicates() {
		List<Identity> selectedIdentities = new ArrayList<>();
		for(Integer index:tableEl.getMultiSelectedIndex()) {
			Identity identity = userTableModel.getObject(index.intValue());
			if(identity != null) {
				selectedIdentities.add(identity);
			}
		}
		return selectedIdentities;
	}

	/**
	 * 
	 * @param inp The text input
	 * @return true if duplicates found
	 */
	private boolean processInput(String inp) {
		List<String> identList = getLines(inp);
		Set<String> identListLowercase = identList.stream()
				.map(String::toLowerCase)
				.collect(Collectors.toSet());

		notFoundNames = new ArrayList<>();
		Map<String, Set<Identity>> duplicates = new HashMap<>();
		Set<Identity> okSet = new HashSet<>();

		// search by names, institutional identifier, first + last names, authentication user names
		List<FindNamedIdentity> identities = securityManager.findIdentitiesBy(identList);
		for(FindNamedIdentity identity:identities) {
			identListLowercase.removeAll(identity.getNamesLowerCase());
			if(!validIdentity(identity.getIdentity())) {
				notFoundNames.add(identity.getFirstFoundName());
			} else if (!okSet.contains(identity.getIdentity())) {
				okSet.add(identity.getIdentity());
			}
			
			for(String name:identity.getNamesLowerCase()) {
				Set<Identity> ids = duplicates.computeIfAbsent(name, n -> new HashSet<>());
				ids.add(identity.getIdentity());
			}
		}

		notFoundNames.addAll(identListLowercase);
		return processDuplicates(okSet, duplicates);
	}
	
	private boolean processDuplicates(Set<Identity> okSet, Map<String, Set<Identity>> duplicates) {
		Set<Identity> duplicatesSet = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String,Set<Identity>> entry:duplicates.entrySet()) {
			if(entry.getValue().size() > 1) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(entry.getKey());
				duplicatesSet.addAll(entry.getValue());
			}	
		}
		
		okSet.removeAll(duplicatesSet);
		
		identitiesList = new ArrayList<>(okSet);

		if(sb.length() > 0) {
			tableContainer.contextPut("duplicatesMsg", translate("warn.duplicates.names", sb.toString()));
			tableContainer.setVisible(true);
			inputContainer.setVisible(false);
			
			userTableModel.setObjects(new ArrayList<>(duplicatesSet));
			tableEl.reset(true, true, true);
			return true;
		}
		return false;
	}
	
	private List<String> getLines(String inp) {
		List<String> identList = new ArrayList<>();
		String[] lines = inp.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if(username.length() > 0) {
				identList.add(username);
			}
		}
		return identList;
	}
	
	private boolean validIdentity(Identity ident) {
		return ident != null
				&& ident.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) < 0
				&& !anonymousUsers.contains(ident);
	}
}