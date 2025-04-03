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
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.util.StringHelper;
import org.olat.user.DisplayPortraitController;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoController;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mar 27, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class UserProfileInfoController extends UserInfoController {

	private final Identity identity;
	private final Boolean showTitle;
	private final Roles editedRoles;
	private DisplayPortraitController portraitCtr;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;

	public UserProfileInfoController(UserRequest ureq, WindowControl wControl,
									 UserInfoProfileConfig profileConfig, PortraitUser portraitUser,
									 Identity identity, Roles editedRoles, Boolean showTitle) {
		super(ureq, wControl, profileConfig, portraitUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.identity = identity;
		this.editedRoles = editedRoles;
		this.showTitle = showTitle;
		initForm(ureq);
	}

	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		super.initFormItems(itemsCont, listener, ureq);

		// 1. Portrait
		addPortrait(itemsCont, ureq);

		// 2. User ID
		addUserId(itemsCont);

		// 3. Organisations (with user role only)
		addUserOrganisations(itemsCont, editedRoles);

		// 4. Status (with badge)
		addStatus(itemsCont);

		// 5. Roles (excluding 'user')
		addRoles(itemsCont, editedRoles);

		// 6. User mail
		addUserMail(itemsCont);

		// 7. Account type (invitee, guest, user)
		addAccountType(itemsCont, editedRoles);

		// 8. Username
		addUsername(itemsCont);
	}

	private void addPortrait(FormLayoutContainer itemsCont, UserRequest ureq) {
		removeAsListenerAndDispose(portraitCtr);
		portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), identity, UserPortraitComponent.PortraitSize.large, false);
		listenTo(portraitCtr);
		itemsCont.put("portrait", portraitCtr.getInitialComponent());
	}

	private void addUserId(FormLayoutContainer itemsCont) {
		uifactory.addStaticTextElement("userid", "user.identity", String.valueOf(identity.getKey()), itemsCont);
	}

	private void addUserOrganisations(FormLayoutContainer itemsCont, Roles fullRoles) {
		List<OrganisationRef> userOrgs = fullRoles.getOrganisationsWithRole(OrganisationRoles.user);
		if (userOrgs.isEmpty()) {
			uifactory.addStaticTextElement("organisations", "user.organisations.label", translate("user.no.data"), itemsCont);
			return;
		}

		// Create a horizontal container to hold the links and separators
		FormLayoutContainer orgLinkCont = FormLayoutContainer.createHorizontalFormLayout("organisations.links", getTranslator());
		orgLinkCont.setLabel("user.organisations.label", null);
		itemsCont.add(orgLinkCont);

		for (int i = 0; i < userOrgs.size(); i++) {
			Organisation org = organisationService.getOrganisation(userOrgs.get(i));
			if (org == null) continue;

			FormLink link = uifactory.addFormLink("org_link_" + org.getKey(), "org_roles", org.getDisplayName(), null, orgLinkCont, Link.NONTRANSLATED | Link.NONTRANSLATED);
			link.setUserObject("org-click");

			// Add separator
			if (i < userOrgs.size() - 1) {
				uifactory.addStaticTextElement("org_sep_" + i, null, ";", orgLinkCont);
			}
		}
	}


	private void addStatus(FormLayoutContainer itemsCont) {
		if (Boolean.TRUE.equals(showTitle)) {
			int status = identity.getStatus().intValue();
			String statusKey = "";
			String cssClass = "";

			if (status <= Identity.STATUS_ACTIV) {
				statusKey = "rightsForm.status.activ";
				cssClass = "o_user_status_active";
			} else if (status == Identity.STATUS_INACTIVE) {
				statusKey = "rightsForm.status.inactive";
				cssClass = "o_user_status_inactive";
			} else if (status == Identity.STATUS_LOGIN_DENIED) {
				statusKey = "rightsForm.status.login_denied";
				cssClass = "o_user_status_login_denied";
			} else if (status == Identity.STATUS_PENDING) {
				statusKey = "rightsForm.status.pending";
				cssClass = "o_user_status_pending";
			} else if (status == Identity.STATUS_DELETED) {
				statusKey = "rightsForm.status.deleted";
				cssClass = "o_user_status_deleted";
			}

			String statusValue = "<span class=\"o_user_status_badge " + cssClass + "\">" + translate(statusKey) + "</span>";
			uifactory.addStaticTextElement("status", "user.status", statusValue, itemsCont);
		}
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
			uifactory.addStaticTextElement("roles", "user.roles.label", translate("user.no.data"), itemsCont);
			return;
		}

		FormLayoutContainer roleLinkCont = FormLayoutContainer.createHorizontalFormLayout("roles.links", getTranslator());
		roleLinkCont.setLabel("user.roles.label", null);
		itemsCont.add(roleLinkCont);

		int i = 0;
		for (String roleName : addedRoles) {
			OrganisationRoles roleEnum = OrganisationRoles.valueOf(roleName);
			String label = translate("role." + roleName);
			boolean multiOrg = fullRoles.getOrganisationsWithRole(roleEnum).size() > 1;
			if (multiOrg) {
				label += " (" + translate("roles.multi.org") + ")";
			}

			String badge = "<span class=\"o_labeled_light\">" + StringHelper.escapeHtml(label) + "</span>";

			FormLink link = uifactory.addFormLink("role_link_" + i++, "role_click", badge, null, roleLinkCont, Link.NONTRANSLATED);
			link.setUserObject("role-click");

			// Add separator
			if (i < addedRoles.size()) {
				uifactory.addStaticTextElement("role_sep_" + i, null, "", roleLinkCont);
			}
		}
	}

	private void addUserMail(FormLayoutContainer itemsCont) {
		String email = identity.getUser().getEmail();
		String userMail = StringHelper.containsNonWhitespace(email) ? email : translate("user.no.data");
		uifactory.addStaticTextElement("usermail", "user.mail", userMail, itemsCont);
	}

	private void addAccountType(FormLayoutContainer itemsCont, Roles fullRoles) {
		String accountType = fullRoles.isInvitee()
				? translate("user.type.invitee")
				: fullRoles.isGuestOnly()
				? translate("user.type.guest")
				: translate("user.type.user");

		uifactory.addStaticTextElement("ac", "user.account.type.label", accountType, itemsCont);
	}

	private void addUsername(FormLayoutContainer itemsCont) {
		uifactory.addStaticTextElement("username", "user.username", identity.getName(), itemsCont);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			Object tag = link.getUserObject();
			if ("org-click".equals(tag) || "role-click".equals(tag)) {
				fireEvent(ureq, new Event(String.valueOf(tag)));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}

