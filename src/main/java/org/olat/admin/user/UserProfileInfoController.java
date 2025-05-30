/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.admin.user;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.util.StringHelper;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoController;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mar 27, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class UserProfileInfoController extends UserInfoController {

	private static final List<OrganisationRoles> DISPLAY_ROLE_ORDER = List.of(
			OrganisationRoles.invitee,
			OrganisationRoles.user,
			OrganisationRoles.author,
			OrganisationRoles.usermanager,
			OrganisationRoles.rolesmanager,
			OrganisationRoles.groupmanager,
			OrganisationRoles.poolmanager,
			OrganisationRoles.curriculummanager,
			OrganisationRoles.lecturemanager,
			OrganisationRoles.projectmanager,
			OrganisationRoles.qualitymanager,
			OrganisationRoles.linemanager,
			OrganisationRoles.learnresourcemanager,
			OrganisationRoles.educationmanager,
			OrganisationRoles.principal,
			OrganisationRoles.administrator,
			OrganisationRoles.sysadmin
	);


	private final Identity identity;
	private final Roles editedRoles;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BaseSecurity securityManager;

	public UserProfileInfoController(UserRequest ureq, WindowControl wControl,
									 UserInfoProfileConfig profileConfig, PortraitUser portraitUser,
									 Identity identity, Roles editedRoles) {
		super(ureq, wControl, profileConfig, portraitUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.identity = identity;
		this.editedRoles = editedRoles;
		initForm(ureq);
	}

	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		super.initFormItems(itemsCont, listener, ureq);

		// 1. User ID
		addUserId(itemsCont);

		// 2. Organisations (with user role only)
		addUserOrganisations(itemsCont);

		// 3. Roles (excluding 'user')
		addRoles(itemsCont, editedRoles);

		// 4. User mail
		addUserMail(itemsCont);

		// 5. Account type (invitee, guest, user)
		addAccountType(itemsCont, editedRoles);

		// 6. Username
		addUsername(itemsCont);
	}

	private void addUserId(FormLayoutContainer itemsCont) {
		uifactory.addStaticTextElement("userid", "user.identity", String.valueOf(identity.getKey()), itemsCont);
	}

	private void addUserOrganisations(FormLayoutContainer itemsCont) {
		List<String> userOrgNames = organisationService.getUsersOrganisationsNames(List.of(identity)).getOrDefault(identity.getKey(), null);
		if (userOrgNames.isEmpty()) {
			uifactory.addStaticTextElement("organisations", "user.organisations.label", translate("user.no.data"), itemsCont);
			return;
		}
		
		String orgName = userOrgNames.stream()
				.map(StringHelper::escapeHtml)
				.collect(Collectors.joining("; "));
		uifactory.addFormLink("organisations", "org-click", orgName, "user.organisations.label", itemsCont, Link.LINK + Link.NONTRANSLATED);
	}

	private void addRoles(FormLayoutContainer itemsCont, Roles fullRoles) {
		Set<String> addedRoles = new LinkedHashSet<>();

		for (OrganisationRef orgRef : fullRoles.getOrganisations()) {
			RolesByOrganisation rolesByOrg = fullRoles.getRoles(orgRef);
			if (rolesByOrg == null) continue;

			for (OrganisationRoles role : OrganisationRoles.values()) {
				if (!OrganisationRoles.user.equals(role) && rolesByOrg.hasRole(role)) {
					addedRoles.add(role.name());
				}
			}
		}

		if (addedRoles.isEmpty()) {
			return;
		}
		
		String roles = "";
		for (OrganisationRoles role : DISPLAY_ROLE_ORDER) {
			if (!addedRoles.contains(role.name())) {
				continue;
			}

			String label = translate("role." + role.name());
			boolean multiOrg = fullRoles.getOrganisationsWithRole(role).size() > 1;
			if (multiOrg) {
				label += " (" + fullRoles.getOrganisationsWithRole(role).size() + ")";
			}
			
			roles += "<span class=\"o_labeled_light\">" + StringHelper.escapeHtml(label) + "</span> ";
		}
		
		 uifactory.addFormLink("roles", "role-click", roles, "user.roles.label", itemsCont, Link.LINK + Link.NONTRANSLATED);
	}

	private void addUserMail(FormLayoutContainer itemsCont) {
		String email = identity.getUser().getEmail();
		if (StringHelper.containsNonWhitespace(email)) {
			email = StringHelper.escapeHtml(email);
			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"mailto:")
			  .append(email)
			  .append("\"><i class='o_icon o_icon_mail'> </i> ")
			  .append(email)
			  .append("</a>");
			email = StringHelper.xssScan(sb.toString());
		} else {
			email = translate("user.no.data");
		}
		uifactory.addStaticTextElement("usermail", "user.mail", email, itemsCont);
	}

	private void addAccountType(FormLayoutContainer itemsCont, Roles fullRoles) {
		String accountType;

		if (fullRoles.isInvitee()) {
			accountType = translate("user.type.invitee");
		} else {
			if (fullRoles.isGuestOnly())
				accountType = translate("user.type.guest");
			else
				accountType = translate("user.type.user");
		}
		
		uifactory.addStaticTextElement("ac", "user.account.type.label", accountType, itemsCont);
	}

	private void addUsername(FormLayoutContainer itemsCont) {
		String name = securityManager.findAuthenticationName(identity);
		uifactory.addStaticTextElement("username", "user.username", name, itemsCont);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("org-click".equals(cmd) || "role-click".equals(cmd)) {
				fireEvent(ureq, new Event(String.valueOf(cmd)));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}

