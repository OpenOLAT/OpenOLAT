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
package org.olat.user;

import java.util.List;
import java.util.Locale;

import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;

/**
 * Initial date: Jan 13, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChangeOrgRoleRenderer {

	public String renderRolesAsHtml(Roles roles, BaseSecurity securityManager,
									OrganisationService organisationService, Identity identityToModify,
									Locale locale) {
		StringBuilder html = new StringBuilder();

		html.append("<ul>"); // Outer list for organisations
		List<OrganisationRef> currentOrgs = roles.getOrganisations();

		for (OrganisationRef orgRef : currentOrgs) {
			// Get org name
			String organisationName = organisationService.getOrganisation(orgRef).getDisplayName();

			// Get roles as strings for this org
			List<String> rolesAsString = securityManager.getRolesAsString(identityToModify, orgRef);

			// Add org name as a list item
			html.append("<li><strong>").append(organisationName).append("</strong>");

			// Add nested roles list
			if (!rolesAsString.isEmpty()) {
				Translator translator = Util.createPackageTranslator(UserAdminController.class, locale);
				html.append("<ul>"); // inner list for roles
				for (String role : rolesAsString) {
					String translatedRole = translator.translate("role." + role);
					html.append("<li>").append(translatedRole).append("</li>");
				}
				html.append("</ul>");
			}

			html.append("</li>");
		}

		html.append("</ul>");

		return html.toString();
	}
}
