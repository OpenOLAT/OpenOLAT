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
package org.olat.basesecurity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.basesecurity.model.IdentityPropertiesRow;
import org.olat.core.id.Identity;
import org.olat.modules.coach.ui.UserListController;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityPowerSearchQueriesTest extends OlatTestCase {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;
	
	@Test
	public void searchWithInheritanceButNoOrganisation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("power-1");
		
		OrganisationRoles[] roles = { OrganisationRoles.user };
		GroupMembershipInheritance[] inheritence = { GroupMembershipInheritance.root, GroupMembershipInheritance.none };
		SearchIdentityParams params = SearchIdentityParams.roles(roles, inheritence, Identity.STATUS_VISIBLE_LIMIT);
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		List<IdentityPropertiesRow> rows = identityPowerSearchQueries.getIdentitiesByPowerSearch(params, userPropertyHandlers, Locale.ENGLISH, null, 0, -1);
		assertThat(rows)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(row -> row.getIdentityKey())
			.contains(id.getKey());
	}
	
	@Test
	public void searchWithInheritanceAndOrganisation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("power-2");
		
		OrganisationRoles[] roles = { OrganisationRoles.user };
		GroupMembershipInheritance[] inheritence = { GroupMembershipInheritance.root, GroupMembershipInheritance.none };
		SearchIdentityParams params = SearchIdentityParams.roles(roles, inheritence, Identity.STATUS_VISIBLE_LIMIT);
		params.setOrganisations(List.of(organisationService.getDefaultOrganisation()));
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		List<IdentityPropertiesRow> rows = identityPowerSearchQueries.getIdentitiesByPowerSearch(params, userPropertyHandlers, Locale.ENGLISH, null, 0, -1);
		assertThat(rows)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(row -> row.getIdentityKey())
			.contains(id.getKey());
	}

}
