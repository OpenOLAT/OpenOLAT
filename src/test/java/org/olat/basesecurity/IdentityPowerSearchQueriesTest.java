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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.basesecurity.model.IdentityPropertiesRow;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DateUtils;
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
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
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
		Assertions.assertThat(rows)
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
		Assertions.assertThat(rows)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(row -> row.getIdentityKey())
			.contains(id.getKey());
	}

	@Test
	public void searchOrderedByDaysToExpiry() {
		// OO-8382: sorting the "days to expiry" column crashed with a Hibernate syntax exception
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("power-3");

		OrganisationRoles[] roles = { OrganisationRoles.user };
		GroupMembershipInheritance[] inheritence = { GroupMembershipInheritance.root, GroupMembershipInheritance.none };
		SearchIdentityParams params = SearchIdentityParams.roles(roles, inheritence, Identity.STATUS_VISIBLE_LIMIT);

		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		SortKey sortKey = new SortKey("daysToExpiry", true);
		List<IdentityPropertiesRow> rows = identityPowerSearchQueries.getIdentitiesByPowerSearch(params, userPropertyHandlers, Locale.ENGLISH, sortKey, 0, -1);
		Assertions.assertThat(rows)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(row -> row.getIdentityKey())
			.contains(id.getKey());
	}

	@Test
	public void searchOrderedByIllegalKeyFallsBackGracefully() {
		// OO-8382: an unmapped sort key must fall back to a valid order-by, not a malformed one
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("power-4");

		OrganisationRoles[] roles = { OrganisationRoles.user };
		GroupMembershipInheritance[] inheritence = { GroupMembershipInheritance.root, GroupMembershipInheritance.none };
		SearchIdentityParams params = SearchIdentityParams.roles(roles, inheritence, Identity.STATUS_VISIBLE_LIMIT);

		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		SortKey sortKey = new SortKey("notAValidSortKey", true);
		List<IdentityPropertiesRow> rows = identityPowerSearchQueries.getIdentitiesByPowerSearch(params, userPropertyHandlers, Locale.ENGLISH, sortKey, 0, -1);
		Assertions.assertThat(rows)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(row -> row.getIdentityKey())
			.contains(id.getKey());
	}

	@Test
	public void searchModifiedSince() {
		Identity changedId = JunitTestHelper.createAndPersistIdentityAsRndUser("power-mod-1");
		Identity statusId = JunitTestHelper.createAndPersistIdentityAsRndUser("power-mod-2");
		Identity boundaryId = JunitTestHelper.createAndPersistIdentityAsRndUser("power-mod-3");
		Identity unchangedId = JunitTestHelper.createAndPersistIdentityAsRndUser("power-mod-4");
		dbInstance.commitAndCloseSession();
		
		// Full seconds for databases without milliseconds
		Date modifiedSince = new Date((DateUtils.addDays(new Date(), -1).getTime() / 1000l) * 1000l);
		Date before = DateUtils.addDays(new Date(), -2);
		JunitTestHelper.setUserLastModified(changedId, before);
		JunitTestHelper.setUserLastModified(statusId, before);
		JunitTestHelper.setUserLastModified(boundaryId, modifiedSince);
		JunitTestHelper.setUserLastModified(unchangedId, before);
		
		Identity reloadedChangedId = securityManager.loadIdentityByKey(changedId.getKey());
		reloadedChangedId.getUser().setProperty(UserConstants.FIRSTNAME, "Modified");
		userManager.updateUserFromIdentity(reloadedChangedId);
		securityManager.saveIdentityStatus(statusId, Identity.STATUS_LOGIN_DENIED, statusId);
		dbInstance.commitAndCloseSession();
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setModifiedSince(modifiedSince);
		List<Identity> identities = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assertions.assertThat(identities)
			.map(Identity::getKey)
			.contains(changedId.getKey(), statusId.getKey(), boundaryId.getKey())
			.doesNotContain(unchangedId.getKey())
			.isSortedAccordingTo(Comparator.naturalOrder());
		
		int count = securityManager.countIdentitiesByPowerSearch(params);
		Assertions.assertThat(count).isGreaterThanOrEqualTo(3);
	}
}
