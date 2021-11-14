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
package org.olat.modules.qpool.ui.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
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
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportAuthorOverviewIdentitiesController extends StepFormBasicController {
	
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private List<Identity> oks;
	private boolean isAdministrativeUser;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;

	public ImportAuthorOverviewIdentitiesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		oks = null;
		if(containsRunContextKey("logins")) {
			String logins = (String)runContext.get("logins");
			oks = loadModel(logins);
		} else if(containsRunContextKey("keys")) {
			@SuppressWarnings("unchecked")
			List<String> keys = (List<String>)runContext.get("keys");
			oks = loadModel(keys);
		}

		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());

		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
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
		ImportAuthorOverviewDataModel userTableModel = new ImportAuthorOverviewDataModel(oks, resultingPropertyHandlers,
				isAdministrativeUser, getLocale(), tableColumnModel);
		uifactory.addTableElement(getWindowControl(), "users", userTableModel, myTrans, formLayout);
	}
	
	private List<Identity> loadModel(List<String> keys) {
		List<Identity> existIdents = Collections.emptyList();

		List<Identity> okIdentities = new ArrayList<>();
		List<String> isanonymous = new ArrayList<>();
		List<String> notfounds = new ArrayList<>();
		List<String> alreadyin = new ArrayList<>();

		for (String identityKey : keys) {
			Identity ident = securityManager.loadIdentityByKey(Long.parseLong(identityKey));
			if (ident == null) { // not found, add to not-found-list
				notfounds.add(identityKey);
			} else if (organisationService.hasRole(ident, OrganisationRoles.guest)) {
				isanonymous.add(identityKey);
			} else {
				// check if already in group
				boolean inGroup = PersistenceHelper.containsPersistable(existIdents, ident);
				if (inGroup) {
					// added to warning: already in group
					alreadyin.add(ident.getName());
				} else {
					// ok to add -> preview (but filter duplicate entries)
					if (!PersistenceHelper.containsPersistable(okIdentities, ident)) {
						okIdentities.add(ident);
					}
				}
			}
		}
		
		return okIdentities;
	}
	
	private List<Identity> loadModel(String inp) {
		List<Identity> existIdents = Collections.emptyList();

		List<Identity> okIdentities = new ArrayList<>();
		List<String> isanonymous = new ArrayList<>();
		List<String> notfounds = new ArrayList<>();
		List<String> alreadyin = new ArrayList<>();

		String[] lines = inp.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if (!username.equals("")) { // skip empty lines
				List<FindNamedIdentity> namedIdentities = securityManager
						.findIdentitiesBy(Collections.singletonList(username));
				
				if (namedIdentities.isEmpty()) { // not found, add to not-found-list
					notfounds.add(username);
				} else {
					for(FindNamedIdentity namedIdentity:namedIdentities) {
						Identity ident = namedIdentity.getIdentity();
						if (organisationService.hasRole(ident, OrganisationRoles.guest)) {
							isanonymous.add(username);
						} else {
							// check if already in group
							boolean inGroup = PersistenceHelper.containsPersistable(existIdents, ident);
							if (inGroup) {
								// added to warning: already in group
								alreadyin.add(ident.getName());
							} else if (!PersistenceHelper.containsPersistable(okIdentities, ident)) {
								// ok to add -> preview (but filter duplicate entries)
								okIdentities.add(ident);
							}
						}
					}
				}
			}
		}
		
		return okIdentities;
	}
	

	public boolean validate() {
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		addToRunContext("members", oks);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}