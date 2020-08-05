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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.FindNamedIdentity;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberOverviewIdentitiesController extends StepFormBasicController {
	
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private ImportMemberOverviewDataModel userTableModel;
	
	private List<Identity> oks;
	private List<String> notfounds;
	private boolean isAdministrativeUser;
	private final List<Identity> anonymousUsers;
	private Map<String,Set<Identity>> duplicates = new HashMap<>();
	
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;

	public ImportMemberOverviewIdentitiesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		
		anonymousUsers = organisationService.getIdentitiesWithRole(OrganisationRoles.guest);
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		
		oks = null;
		if(containsRunContextKey("logins")) {
			String logins = (String)runContext.get("logins");
			loadModel(logins);
		} else if(containsRunContextKey("keys")) {
			@SuppressWarnings("unchecked")
			List<String> keys = (List<String>)runContext.get("keys");
			loadModel(keys);
		} else if(containsRunContextKey("keyIdentities")) {
			@SuppressWarnings("unchecked")
			List<Identity> keys = (List<Identity>)runContext.get("keyIdentities");
			loadModelByIdentities(keys);
		}

		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_user_import_overview");
		if(notfounds != null && !notfounds.isEmpty()) {
			String page = velocity_root + "/warn_notfound.html";
			FormLayoutContainer warnLayout = FormLayoutContainer.createCustomFormLayout("warnNotFounds", getTranslator(), page);
			warnLayout.setRootForm(mainForm);
			formLayout.add(warnLayout);
			
			StringBuilder sb = new StringBuilder();
			for(String notfound:notfounds) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(notfound);
			}
			String msg = translate("user.notfound", new String[]{sb.toString()});
			addToRunContext("notFounds", sb.toString());
			warnLayout.contextPut("notFounds", msg);
		}
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String,Set<Identity>> entry:duplicates.entrySet()) {
			if(entry.getValue().size() > 1) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(entry.getKey());
			}	
		}
		
		
		if(sb.length() > 0) {
			String page = velocity_root + "/warn_duplicates.html";
			FormLayoutContainer warnLayout = FormLayoutContainer.createCustomFormLayout("warnNotFounds", getTranslator(), page);
			warnLayout.setRootForm(mainForm);
			formLayout.add(warnLayout);
			warnLayout.contextPut("duplicatesMsg", translate("warn.duplicates.names", sb.toString()));
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
		
		Translator myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
		userTableModel = new ImportMemberOverviewDataModel(oks, resultingPropertyHandlers, getLocale(), tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "users", userTableModel, myTrans, formLayout);
		tableEl.setCustomizeColumns(false);
		//TODO OO-4545
		/*
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.selectAll();
		*/
	}
	
	private void loadModel(List<String> keys) {
		notfounds = new ArrayList<>();
		
		Set<Identity> okSet = new HashSet<>();
		for (String identityKey : keys) {
			Identity ident = securityManager.loadIdentityByKey(Long.parseLong(identityKey));
			if (!validIdentity(ident)) { // not found, add to not-found-list
				notfounds.add(identityKey);
			} else if (!okSet.contains(ident)) {
				okSet.add(ident);
			}
		}
		oks = new ArrayList<>(okSet);
	}
	
	private void loadModelByIdentities(List<Identity> keys) {
		notfounds = new ArrayList<>();
		
		Set<Identity> okSet = new HashSet<>();
		for (Identity ident : keys) {
			if (!validIdentity(ident)) {
				String fullname = userManager.getUserDisplayName(ident);
				if(fullname != null) {
					notfounds.add(fullname);
				}
			} else if (!okSet.contains(ident)) {
				okSet.add(ident);
			}
		}
		oks = new ArrayList<>(okSet);
	}
	
	private void loadModel(String inp) {
		oks = new ArrayList<>();
		notfounds = new ArrayList<>();
		duplicates.clear();
		
		Set<Identity> okSet = new HashSet<>();
		List<String> identList = new ArrayList<>();
		String[] lines = inp.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if(username.length() > 0) {
				identList.add(username);
			}
		}
		
		// make a lower case copy of identList for processing username and email
		Collection<String> identListLowercase = new HashSet<>(identList.size());
		for (String ident:identList) {
			identListLowercase.add(ident.toLowerCase());
		}
		
		// search by names, institutional identifier, first + last names, authentication user names
		List<FindNamedIdentity> identities = securityManager.findIdentitiesBy(identList);
		for(FindNamedIdentity identity:identities) {
			identListLowercase.removeAll(identity.getNamesLowerCase());
			if(!validIdentity(identity.getIdentity())) {
				notfounds.add(identity.getFirstFoundName());
			} else if (!okSet.contains(identity.getIdentity())) {
				okSet.add(identity.getIdentity());
			}
			
			for(String name:identity.getNamesLowerCase()) {
				Set<Identity> ids = duplicates.computeIfAbsent(name, n -> new HashSet<>());
				ids.add(identity.getIdentity());
			}
		}
		
		//search by email, case insensitive
		List<String> emailListLowercase = new ArrayList<>(identList.size());
		for (String ident:identList) {
			if(ident.indexOf('@') > 0) {
				emailListLowercase.add(ident.toLowerCase());
			}
		}
		
		List<Identity> mailIdentities = userManager.findIdentitiesByEmail(emailListLowercase);
		for(Identity identity:mailIdentities) {
			String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
			if(email != null && identListLowercase.remove(email.toLowerCase())) {
				duplicates.computeIfAbsent(email.toLowerCase(), n -> new HashSet<>()).add(identity);
			}
			String institutEmail = identity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
			if(institutEmail != null && identListLowercase.remove(institutEmail.toLowerCase())) {
				duplicates.computeIfAbsent(institutEmail.toLowerCase(), n -> new HashSet<>()).add(identity);
			}
			if(!validIdentity(identity)) {
				if(email != null) {
					notfounds.remove(email);
				} else if(institutEmail != null) {
					notfounds.remove(institutEmail);
				}
			} else if (!okSet.contains(identity)) {
				okSet.add(identity);
			}
		}
		
		notfounds.addAll(identListLowercase);
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
		//TODO OO-4545
		/*
		List<Identity> selectedOks = new ArrayList<>(oks.size());
		for(Integer index:tableEl.getMultiSelectedIndex()) {
			Identity selected = userTableModel.getObject(index.intValue());
			if(selected != null) {
				selectedOks.add(selected);
			}
		}
		addToRunContext("members", selectedOks);
		*/
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