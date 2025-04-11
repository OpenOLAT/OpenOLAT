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
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.user.PortraitSize;
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

	private static final String USER_PROFILE_ELEMENTS_CSS = "o_user_card_elements";
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
		portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), identity, PortraitSize.large, false);
		listenTo(portraitCtr);
		itemsCont.put("portrait", portraitCtr.getInitialComponent());
	}

	private void addUserId(FormLayoutContainer itemsCont) {
		StaticTextElement userIdEl = uifactory.addStaticTextElement("userid", "user.identity", String.valueOf(identity.getKey()), itemsCont);
		userIdEl.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
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

			String orgName = StringHelper.escapeHtml(org.getDisplayName());
			if (i < userOrgs.size() - 1) {
				orgName += ";";
			}

			FormLink link = uifactory.addFormLink("org_link_" + org.getKey(), "org_roles",
					orgName, null, orgLinkCont, Link.NONTRANSLATED);
			link.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
			link.setUserObject("org-click");
		}
	}


	private void addStatus(FormLayoutContainer itemsCont) {
		if (Boolean.TRUE.equals(showTitle)) {
			int status = identity.getStatus();
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
			StaticTextElement statusEl = uifactory.addStaticTextElement("status", "user.status", statusValue, itemsCont);
			statusEl.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
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
			return;
		}

		FormLayoutContainer roleLinkCont = FormLayoutContainer.createHorizontalFormLayout("roles.links", getTranslator());
		roleLinkCont.setLabel("user.roles.label", null);
		itemsCont.add(roleLinkCont);

		int i = 0;
		for (OrganisationRoles role : DISPLAY_ROLE_ORDER) {
			if (!addedRoles.contains(role.name())) {
				continue;
			}

			String label = translate("role." + role.name());
			boolean multiOrg = fullRoles.getOrganisationsWithRole(role).size() > 1;
			if (multiOrg) {
				label += " (" + fullRoles.getOrganisationsWithRole(role).size() + ")";
			}

			String badge = "<span class=\"o_labeled_light\">" + StringHelper.escapeHtml(label) + "</span>";
			FormLink link = uifactory.addFormLink("role_link_" + i++, "role_click", badge, null, roleLinkCont, Link.NONTRANSLATED);
			link.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
			link.setUserObject("role-click");
		}
	}

	private void addUserMail(FormLayoutContainer itemsCont) {
		String email = identity.getUser().getEmail();
		String userMail = StringHelper.containsNonWhitespace(email) ? email : translate("user.no.data");
		StaticTextElement userMailEl = uifactory.addStaticTextElement("usermail", "user.mail", userMail, itemsCont);
		userMailEl.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
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

		StaticTextElement accountTypeEl = uifactory.addStaticTextElement("ac", "user.account.type.label", accountType, itemsCont);
		accountTypeEl.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
	}

	private void addUsername(FormLayoutContainer itemsCont) {
		StaticTextElement usernameEl = uifactory.addStaticTextElement("username", "user.username", identity.getName(), itemsCont);
		usernameEl.setElementCssClass(USER_PROFILE_ELEMENTS_CSS);
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

