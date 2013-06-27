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
package org.olat.admin.privacy;

import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PrivacyAdminController extends FormBasicController {
	
	private MultipleSelectionElement adminPropsEl;

	private final BaseSecurityModule module;
	
	private String[] adminPropKeys = new String[]{
			"users","authors", "usermanagers", "groupmanagers", "administrators"
	};
	
	public PrivacyAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		module = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.menu.title");
		setFormDescription("admin.props.desc");

		String[] adminPropValues = new String[]{
				translate("admin.props.users"),
				translate("admin.props.authors"),
				translate("admin.props.usermanagers"),
				translate("admin.props.groupmanagers"),
				translate("admin.props.administrators")
		};
		adminPropsEl = uifactory.addCheckboxesVertical("admin.props", formLayout, adminPropKeys, adminPropValues, null, 1);
		adminPropsEl.select("users", "enabled".equals(module.getUserSearchAdminPropsForUsers()));
		adminPropsEl.select("authors", "enabled".equals(module.getUserSearchAdminPropsForAuthors()));
		adminPropsEl.select("usermanagers", "enabled".equals(module.getUserSearchAdminPropsForUsermanagers()));
		adminPropsEl.select("groupmanagers", "enabled".equals(module.getUserSearchAdminPropsForGroupmanagers()));
		adminPropsEl.select("administrators", "enabled".equals(module.getUserSearchAdminPropsForAdministrators()));
		adminPropsEl.addActionListener(this, FormEvent.ONCHANGE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == adminPropsEl) {
			Set<String> enrolmentSelectedKeys = adminPropsEl.getSelectedKeys();
			module.setUserSearchAdminPropsForUsers(enrolmentSelectedKeys.contains("users") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForAuthors(enrolmentSelectedKeys.contains("authors") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForUsermanagers(enrolmentSelectedKeys.contains("usermanagers") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForGroupmanagers(enrolmentSelectedKeys.contains("groupmanagers") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForAdministrators(enrolmentSelectedKeys.contains("administrators") ? "enabled" : "disabled");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}