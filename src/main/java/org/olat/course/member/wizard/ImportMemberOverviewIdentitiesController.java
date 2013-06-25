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
import java.util.List;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberOverviewIdentitiesController extends StepFormBasicController {
	
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private List<Identity> oks;
	private List<String> notfounds;
	private boolean isAdministrativeUser;
	
	private final UserManager userManager;
	private final BaseSecurity securityManager;
	private final BaseSecurityModule securityModule;

	public ImportMemberOverviewIdentitiesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		userManager = UserManager.getInstance();
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);

		oks = null;
		if(containsRunContextKey("logins")) {
			String logins = (String)runContext.get("logins");
			loadModel(logins);
		} else if(containsRunContextKey("keys")) {
			@SuppressWarnings("unchecked")
			List<String> keys = (List<String>)runContext.get("keys");
			loadModel(keys);
		}

		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());

		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(notfounds != null && !notfounds.isEmpty()) {
			String page = velocity_root + "/warn_notfound.html";
			FormLayoutContainer warnLayout = FormLayoutContainer.createCustomFormLayout("warnNotFounds", getTranslator(), page);
			warnLayout.setRootForm(mainForm);
			formLayout.add(warnLayout);
			
			StringBuffer sb = new StringBuffer();
			for(String notfound:notfounds) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(notfound);
			}
			String msg = translate("user.notfound", new String[]{sb.toString()});
			addToRunContext("notFounds", sb.toString());
			warnLayout.contextPut("notFounds", msg);
		}
		
		//add the table
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.login"));
		}
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<UserPropertyHandler>();
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			if(visible) {
				resultingPropertyHandlers.add(userPropertyHandler);
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey()));
			}
		}
		
		Translator myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
		ImportMemberOverviewDataModel userTableModel = new ImportMemberOverviewDataModel(oks, resultingPropertyHandlers,
				isAdministrativeUser, getLocale(), tableColumnModel);
		uifactory.addTableElement("users", userTableModel, myTrans, formLayout);
	}
	
	private void loadModel(List<String> keys) {
		oks = new ArrayList<Identity>();
		List<String> isanonymous = new ArrayList<String>();
		notfounds = new ArrayList<String>();
		
		SecurityGroup anonymousSecGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
		for (String identityKey : keys) {
			Identity ident = securityManager.loadIdentityByKey(Long.parseLong(identityKey));
			if (ident == null) { // not found, add to not-found-list
				notfounds.add(identityKey);
			} else if (securityManager.isIdentityInSecurityGroup(ident, anonymousSecGroup)) {
				isanonymous.add(identityKey);
			} else if (!PersistenceHelper.containsPersistable(oks, ident)) {
				oks.add(ident);
			}
		}
	}
	
	private void loadModel(String inp) {
		oks = new ArrayList<Identity>();
		List<String> isanonymous = new ArrayList<String>();
		notfounds = new ArrayList<String>();

		SecurityGroup anonymousSecGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);

		String[] lines = inp.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if (!username.equals("")) { // skip empty lines
				Identity ident = securityManager.findIdentityByName(username);
				if (ident == null) { // not found, add to not-found-list
					notfounds.add(username);
				} else if (securityManager.isIdentityInSecurityGroup(ident, anonymousSecGroup)) {
					isanonymous.add(username);
				} else if (!PersistenceHelper.containsPersistable(oks, ident)) {
					oks.add(ident);
				}
			}
		}
	}

	public boolean validate() {
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		addToRunContext("members", oks);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}
}