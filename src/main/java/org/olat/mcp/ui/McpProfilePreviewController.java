/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The read-only configuration of one profile for the dialog "Preview" of the
 * user administration: the general settings and the tools as the tree of
 * {@link McpToolTreeController}.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class McpProfilePreviewController extends FormBasicController {

	private FormLink closeButton;

	private final McpProfile profile;
	private final String organisations;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;

	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param profile The profile, freshly loaded
	 * @param organisations The display names of the organisations of the profile
	 */
	public McpProfilePreviewController(UserRequest ureq, WindowControl wControl, McpProfile profile, String organisations) {
		super(ureq, wControl, "profile_preview", Util.createPackageTranslator(UserSearchFlexiController.class, ureq.getLocale()));
		this.profile = profile;
		this.organisations = organisations;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean system = profile.getType() == McpProfileType.system;
		flc.contextPut("name", profile.getName());
		flc.contextPut("identifier", profile.getIdentifier());
		flc.contextPut("type", translate(system ? "profile.type.system" : "profile.type.user"));
		flc.contextPut("description", profile.getDescription());
		flc.contextPut("organisations", organisations);
		flc.contextPut("enabled", translate(profile.isEnabled() ? "yes" : "no"));
		flc.contextPut("system", Boolean.valueOf(system));
		if(system) {
			List<String> roles = new ArrayList<>();
			for(OrganisationRoles role:profile.getOrganisationRoles()) {
				roles.add(translate("role." + role.name()));
			}
			flc.contextPut("roles", String.join(", ", roles));
		} else {
			flc.contextPut("members", Integer.toString(mcpService.countGrantedIdentities(profile)));
		}
		Integer rateLimit = profile.getRateLimitRequestsPerMinute();
		flc.contextPut("rateLimit", rateLimit == null
				? translate("access.check.rpm.default", Integer.toString(mcpModule.getRateLimitRequestsPerMinute()))
				: rateLimit.toString());

		McpToolTreeController toolsCtrl = new McpToolTreeController(ureq, getWindowControl(), mainForm, profile.getTools());
		listenTo(toolsCtrl);
		formLayout.add("tools", toolsCtrl.getInitialFormItem());

		closeButton = uifactory.addFormLink("close", formLayout, Link.BUTTON);
		closeButton.setElementCssClass("o_sel_mcp_profile_preview_close");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == closeButton) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
