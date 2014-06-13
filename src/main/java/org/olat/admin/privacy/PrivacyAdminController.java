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

import java.util.Collection;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PrivacyAdminController extends FormBasicController {
	
	private MultipleSelectionElement adminPropsEl;
	private MultipleSelectionElement lastloginEl;
	private MultipleSelectionElement tunnelEl;

	private final BaseSecurityModule module;
	
	private String[] adminPropKeys = new String[]{
			"users","authors", "usermanagers", "groupmanagers", "administrators"
	};
	private String[] onKeys = new String[]{ "on" };
	
	public PrivacyAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		module = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FormLayoutContainer propsCont = FormLayoutContainer.createDefaultFormLayout("props", getTranslator());
		formLayout.add(propsCont);
		propsCont.setFormTitle(translate("admin.menu.title"));
		propsCont.setFormDescription(translate("admin.props.desc"));
		
		String[] adminPropValues = new String[]{
				translate("admin.props.users"),
				translate("admin.props.authors"),
				translate("admin.props.usermanagers"),
				translate("admin.props.groupmanagers"),
				translate("admin.props.administrators")
		};
		adminPropsEl = uifactory.addCheckboxesVertical("admin.props", propsCont, adminPropKeys, adminPropValues, 1);
		adminPropsEl.select("users", "enabled".equals(module.getUserSearchAdminPropsForUsers()));
		adminPropsEl.select("authors", "enabled".equals(module.getUserSearchAdminPropsForAuthors()));
		adminPropsEl.select("usermanagers", "enabled".equals(module.getUserSearchAdminPropsForUsermanagers()));
		adminPropsEl.select("groupmanagers", "enabled".equals(module.getUserSearchAdminPropsForGroupmanagers()));
		adminPropsEl.select("administrators", "enabled".equals(module.getUserSearchAdminPropsForAdministrators()));
		adminPropsEl.addActionListener(FormEvent.ONCHANGE);
		
		uifactory.addSpacerElement("admin.space.1", propsCont, true);

		lastloginEl = uifactory.addCheckboxesVertical("last.login", propsCont, adminPropKeys, adminPropValues, 1);
		lastloginEl.select("users", "enabled".equals(module.getUserLastLoginVisibleForUsers()));
		lastloginEl.select("authors", "enabled".equals(module.getUserLastLoginVisibleForAuthors()));
		lastloginEl.select("usermanagers", "enabled".equals(module.getUserLastLoginVisibleForUsermanagers()));
		lastloginEl.select("groupmanagers", "enabled".equals(module.getUserLastLoginVisibleForGroupmanagers()));
		lastloginEl.select("administrators", "enabled".equals(module.getUserLastLoginVisibleForAdministrators()));
		lastloginEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer tuCont = FormLayoutContainer.createDefaultFormLayout("tu", getTranslator());
		formLayout.add(tuCont);
		tuCont.setFormTitle(translate("tunnel.title"));
		tuCont.setFormDescription(translate("tunnel.desc"));
		
		tunnelEl = uifactory.addCheckboxesHorizontal("tunnel.cbb", tuCont, onKeys, new String[]{""});
		tunnelEl.select("on", "enabled".equals(module.getUserInfosTunnelCourseBuildingBlock()));
		tunnelEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == adminPropsEl) {
			Collection<String> selectedKeys = adminPropsEl.getSelectedKeys();
			module.setUserSearchAdminPropsForUsers(selectedKeys.contains("users") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForAuthors(selectedKeys.contains("authors") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForUsermanagers(selectedKeys.contains("usermanagers") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForGroupmanagers(selectedKeys.contains("groupmanagers") ? "enabled" : "disabled");
			module.setUserSearchAdminPropsForAdministrators(selectedKeys.contains("administrators") ? "enabled" : "disabled");
		} else if(source == lastloginEl) {
			Collection<String> selectedKeys = lastloginEl.getSelectedKeys();
			module.setUserLastLoginVisibleForUsers(selectedKeys.contains("users") ? "enabled" : "disabled");
			module.setUserLastLoginVisibleForAuthors(selectedKeys.contains("authors") ? "enabled" : "disabled");
			module.setUserLastLoginVisibleForUsermanagers(selectedKeys.contains("usermanagers") ? "enabled" : "disabled");
			module.setUserLastLoginVisibleForGroupmanagers(selectedKeys.contains("groupmanagers") ? "enabled" : "disabled");
			module.setUserLastLoginVisibleForAdministrators(selectedKeys.contains("administrators") ? "enabled" : "disabled");
		} else if (source == tunnelEl) {
			Collection<String> selectedKeys = tunnelEl.getSelectedKeys();
			module.setUserInfosTunnelCourseBuildingBlock(selectedKeys.contains("on") ? "enabled" : "disabled");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}