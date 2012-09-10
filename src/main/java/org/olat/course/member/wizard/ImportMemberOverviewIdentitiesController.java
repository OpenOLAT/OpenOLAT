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
import java.util.Collections;
import java.util.List;

import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SyncHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberOverviewIdentitiesController extends StepFormBasicController {
	private TableController identityTableCtrl;
	private List<Identity> oks;
	private final BaseSecurity securityManager;

	public ImportMemberOverviewIdentitiesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "confirm_identities");
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);

		oks = null;
		if(containsRunContextKey("logins")) {
			String logins = (String)runContext.get("logins");
			oks = loadModel(logins);
		} else if(containsRunContextKey("keys")) {
			@SuppressWarnings("unchecked")
			List<String> keys = (List<String>)runContext.get("keys");
			oks = loadModel(keys);
		}
		
		
		identityTableCtrl = UserControllerFactory.createTableControllerFor(null, oks, ureq, getWindowControl(), null);
		listenTo(identityTableCtrl);

		initForm (ureq);
	}
	
	private List<Identity> loadModel(List<String> keys) {
		List<Identity> existIdents = Collections.emptyList();//securityManager.getIdentitiesOfSecurityGroup(securityGroup);

		List<Identity> oks = new ArrayList<Identity>();
		List<String> isanonymous = new ArrayList<String>();
		List<String> notfounds = new ArrayList<String>();
		List<String> alreadyin = new ArrayList<String>();

		SecurityGroup anonymousSecGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
		for (String identityKey : keys) {
			Identity ident = securityManager.loadIdentityByKey(Long.parseLong(identityKey));
			if (ident == null) { // not found, add to not-found-list
				notfounds.add(identityKey);
			} else if (securityManager.isIdentityInSecurityGroup(ident, anonymousSecGroup)) {
				isanonymous.add(identityKey);
			} else {
				// check if already in group
				boolean inGroup = SyncHelper.containsPersistable(existIdents, ident);
				if (inGroup) {
					// added to warning: already in group
					alreadyin.add(ident.getName());
				} else {
					// ok to add -> preview (but filter duplicate entries)
					if (!SyncHelper.containsPersistable(oks, ident)) {
						oks.add(ident);
					}
				}
			}
		}
		
		return oks;
	}
	
	private List<Identity> loadModel(String inp) {
		List<Identity> existIdents = Collections.emptyList();//securityManager.getIdentitiesOfSecurityGroup(securityGroup);

		List<Identity> oks = new ArrayList<Identity>();
		List<String> isanonymous = new ArrayList<String>();
		List<String> notfounds = new ArrayList<String>();
		List<String> alreadyin = new ArrayList<String>();

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
				} else {
					// check if already in group
					boolean inGroup = SyncHelper.containsPersistable(existIdents, ident);
					if (inGroup) {
						// added to warning: already in group
						alreadyin.add(ident.getName());
					} else {
						// ok to add -> preview (but filter duplicate entries)
						if (!SyncHelper.containsPersistable(oks, ident)) {
							oks.add(ident);
						}
					}
				}
			}
		}
		
		return oks;
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
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.put("identityList", identityTableCtrl.getInitialComponent());	
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}