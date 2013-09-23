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
package org.olat.user;

import java.util.List;

import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;


/**
 * Small controller which shows the roles of the current identity
 * 
 * Initial date: 23.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRolesController extends FormBasicController {
	
	private static final String[] roleKeys = new String[] {
		Constants.GROUP_USERMANAGERS, Constants.GROUP_GROUPMANAGERS, Constants.GROUP_POOL_MANAGER,
		Constants.GROUP_AUTHORS, Constants.GROUP_INST_ORES_MANAGER, Constants.GROUP_ADMIN
	};
	
	private String[] roleValues;
	
	private final BaseSecurity securityManager;
	
	public UserRolesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(SystemRolesAndRightsController.class, ureq.getLocale()));
		
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		
		String iname = getIdentity().getUser().getProperty("institutionalName", null);
		String ilabel = iname != null
				? translate("rightsForm.isInstitutionalResourceManager.institution",iname)
				: translate("rightsForm.isInstitutionalResourceManager");
		
		roleValues = new String[]{
				translate("rightsForm.isUsermanager"), translate("rightsForm.isGroupmanager"), translate("rightsForm.isPoolmanager"),
				translate("rightsForm.isAuthor"), ilabel, translate("rightsForm.isAdmin")
		};

		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionElement rolesEl = uifactory.addCheckboxesVertical("roles", "rightsForm.roles", formLayout, roleKeys, roleValues,null, 1);
		rolesEl.setEnabled(false);
		List<String> roles = securityManager.getRolesAsString(getIdentity());
		for(String role:roles) {
			for(String roleKey:roleKeys) {
				if(roleKey.equals(role)) {
					rolesEl.select(roleKey, true);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}