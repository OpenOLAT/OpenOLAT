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
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PrivacyAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	
	private MultipleSelectionElement adminPropsEl;
	private MultipleSelectionElement lastloginEl;
	private MultipleSelectionElement tunnelEl;

	@Autowired
	private BaseSecurityModule module;
	
	
	public PrivacyAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer propsCont = FormLayoutContainer.createDefaultFormLayout("props", getTranslator());
		formLayout.add(propsCont);
		propsCont.setFormTitle(translate("admin.menu.title"));
		propsCont.setFormDescription(translate("admin.props.desc"));

		OrganisationRoles[] roles = BaseSecurityModule.getUserAllowedRoles();
		String[] adminPropKeys = new String[roles.length];
		String[] adminPropValues = new String[roles.length];
		for(int i=roles.length; i-->0; ) {
			adminPropKeys[i] = roles[i].name();
			adminPropValues[i] = translate("admin.props." + roles[i].name() + "s");
		}

		adminPropsEl = uifactory.addCheckboxesVertical("admin.props", propsCont, adminPropKeys, adminPropValues, 1);
		for(OrganisationRoles adminProp:roles) {
			if("enabled".equals(module.getUserSearchAdminPropsFor(adminProp))) {
				adminPropsEl.select(adminProp.name(), true);
			}
		}
		adminPropsEl.addActionListener(FormEvent.ONCHANGE);
		
		uifactory.addSpacerElement("admin.space.1", propsCont, true);

		lastloginEl = uifactory.addCheckboxesVertical("last.login", propsCont, adminPropKeys, adminPropValues, 1);
		for(OrganisationRoles role:roles) {
			if("enabled".equals(module.getUserLastLoginVisibleFor(role))) {
				lastloginEl.select(role.name(), true);
			}
		}
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == adminPropsEl) {
			Collection<String> selectedKeys = adminPropsEl.getSelectedKeys();
			OrganisationRoles[] roleArray = BaseSecurityModule.getUserAllowedRoles();
			for(OrganisationRoles adminProp:roleArray) {
				module.setUserSearchAdminPropsFor(adminProp, getEnable(selectedKeys.contains(adminProp.name())));
			}
		} else if(source == lastloginEl) {
			Collection<String> selectedKeys = lastloginEl.getSelectedKeys();
			OrganisationRoles[] roleArray = BaseSecurityModule.getUserAllowedRoles();
			for(OrganisationRoles adminProp:roleArray) {
				module.setUserLastLoginVisibleFor(adminProp, getEnable(selectedKeys.contains(adminProp.name())));
			}
		} else if (source == tunnelEl) {
			Collection<String> selectedKeys = tunnelEl.getSelectedKeys();
			module.setUserInfosTunnelCourseBuildingBlock(selectedKeys.contains("on") ? "enabled" : "disabled");
		}
	}
	
	private String getEnable(boolean enabled) {
		return enabled ?  "enabled" : "disabled";
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}