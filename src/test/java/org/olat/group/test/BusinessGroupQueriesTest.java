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
package org.olat.group.test;

import static org.junit.Assert.assertNotNull;
import static org.olat.test.JunitTestHelper.random;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupQueries;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;
import org.olat.group.model.OpenBusinessGroupRow;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.ims.lti13.LTI13Service;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupQueriesTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupQueries businessGroupToSearchQueries;


	@Test
	public void findBusinessGroups() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gduo", "gduo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdvo", "gdvo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertTrue(groups.size() >= 2);
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));

		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertTrue(groupViews.size() >= 2);
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));

		List<StatisticsBusinessGroupRow> groupToSelect = businessGroupToSearchQueries.searchBusinessGroupsForSelection(searchParams, identity);
		Assert.assertNotNull(groupToSelect);
		Assert.assertTrue(groupToSelect.size() >= 2);

		List<OpenBusinessGroupRow> openGroups = businessGroupToSearchQueries.searchPublishedBusinessGroups(searchParams, identity);
		Assert.assertNotNull(openGroups);
	}

	@Test
	public void findBusinessGroupsByName() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-2");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, marker.toUpperCase(), "fingbg-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, marker + "xxx", "fingbg-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + marker.toUpperCase(), "fingbg-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group4 = businessGroupDao.createAndPersist(null, "yyyyZZZxxx", "fingbg-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName(marker);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		Assert.assertFalse(groups.contains(group4));

		//check the same with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setName(marker);
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
		Assert.assertFalse(contains(groupViews, group4));
	}

	@Test
	public void findBusinessGroupsByNameFuzzy() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-3");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, marker.toUpperCase(), "fingbg-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, marker + "xxx", "fingbg-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + marker.toUpperCase(), "fingbg-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName("*" + marker + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));

		//check the same with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setName("*" + marker + "*");
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}

	@Test
	public void findBusinessGroupsByDescription() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-4");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker.toUpperCase() + "-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + marker, BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-other-one", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check find business group
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setDescription(marker);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));

		//check find business group
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setDescription(marker);
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(2, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertFalse(contains(groupViews, group3));
	}

	@Test
	public void findBusinessGroupsByDescriptionFuzzy() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-5");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker + "-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + marker.toUpperCase(), BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + marker + "-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setDescription("*" + marker + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));

		//check same search with the views

		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setDescription("*" + marker + "*");
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}

	@Test
	public void findBusinessGroupsByNameOrDesc() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-6");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker.toUpperCase() + "-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "fingbg-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, marker.toUpperCase() + "-xxx", "desc-fingb-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setNameOrDesc(marker);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));

		//check the same search with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setNameOrDesc(marker);
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(2, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertFalse(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}

	@Test
	public void findBusinessGroupsByNameOrDescFuzzy() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-7");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker + "-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + marker.toUpperCase(), BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + marker + "-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setNameOrDesc("*" + marker + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));

		//check the same search with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setNameOrDesc("*" + marker + "*");
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}

	@Test
	public void findBusinessGroupsByOwner() {
		//5 identities
		String marker = UUID.randomUUID().toString();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser(marker);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + marker);
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser(marker + "-ddao-3");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("something-else-ddao-4");

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbgown-1", "fingbgown-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbgown-2", "fingbgown-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbgown-3", "fingbgown-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group4 = businessGroupDao.createAndPersist(id4, "fingbgown-4", "fingbgown-4-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check the same with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setOwnerName(marker);
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, id1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
		Assert.assertFalse(contains(groupViews, group4));
	}

	@Test
	public void findBusinessGroupsByOwnerFuzzy() {
		String marker = UUID.randomUUID().toString();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser(marker);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + marker.toUpperCase());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser(marker + "-ddao-3-");

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbg-own-1-1", "fingbg-own-1-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbg-own-1-2", "fingbg-own-1-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbg-own-1-3", "fingbg-own-1-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check the same with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setOwnerName("*" + marker + "*");
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(searchParams, id1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}

	@Test
	public void findBusinessGroupWithAuthorConnection() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsUser("bdao-5-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(author, re, GroupRoles.owner.name());

		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(author, "gdmo", "gdmo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(author, "gdmo", "gdmo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, re);
		businessGroupRelationDao.addRelationToResource(group3, re);
		dbInstance.commitAndCloseSession();

		//check
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setAuthorConnection(true);
		List<StatisticsBusinessGroupRow> groups = businessGroupToSearchQueries.searchBusinessGroupsForSelection(params, author);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size());

		Set<Long> retrievedGroupkey = new HashSet<>();
		for(StatisticsBusinessGroupRow group:groups) {
			retrievedGroupkey.add(group.getKey());
		}
		Assert.assertTrue(retrievedGroupkey.contains(group1.getKey()));
		Assert.assertTrue(retrievedGroupkey.contains(group3.getKey()));
		Assert.assertFalse(retrievedGroupkey.contains(group2.getKey()));
	}
	
	@Test
	public void findBusinessGroupsManaged() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-7");
		String managedFlags = "all";
		BusinessGroup groupManaged = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				random(), managedFlags, 0, 5, true, false, true, false, false, null);
		// Groups with external ID should be treated as managed even if they have no managed flag.
		BusinessGroup groupExternalId = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				random(), null, 0, 5, true, false, true, false, false, null);
		BusinessGroup groupUnmanaged = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				null, null, 0, 5, true, false, true, false, false, null);
		dbInstance.commitAndCloseSession();

		// Check managed
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setManaged(Boolean.TRUE);
		List<StatisticsBusinessGroupRow> groups = businessGroupToSearchQueries.searchBusinessGroupsForSelection(params, identity);
		Assert.assertNotNull(groups);

		Set<Long> retrievedGroupkey = new HashSet<>();
		for(StatisticsBusinessGroupRow group:groups) {
			retrievedGroupkey.add(group.getKey());
		}
		Assert.assertTrue(retrievedGroupkey.contains(groupManaged.getKey()));
		Assert.assertTrue(retrievedGroupkey.contains(groupExternalId.getKey()));
		Assert.assertFalse(retrievedGroupkey.contains(groupUnmanaged.getKey()));
		
		// Check managed
		params.setManaged(Boolean.FALSE);
		groups = businessGroupToSearchQueries.searchBusinessGroupsForSelection(params, identity);
		Assert.assertNotNull(groups);

		retrievedGroupkey = new HashSet<>();
		for(StatisticsBusinessGroupRow group:groups) {
			retrievedGroupkey.add(group.getKey());
		}
		Assert.assertFalse(retrievedGroupkey.contains(groupManaged.getKey()));
		Assert.assertFalse(retrievedGroupkey.contains(groupExternalId.getKey()));
		Assert.assertTrue(retrievedGroupkey.contains(groupUnmanaged.getKey()));
	}
	
	@Test
	public void findBusinessLastUsageBefore() {
		Date lastUsageBefore = new GregorianCalendar(2020, 8, 9).getTime();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		BusinessGroup before = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				random(), null, 0, 5, true, false, true, false, false, null);
		before.setLastUsage(DateUtils.addDays(lastUsageBefore, -2));
		businessGroupDao.merge(before);
		BusinessGroup after = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				null, null, 0, 5, true, false, true, false, false, null);
		after.setLastUsage(DateUtils.addDays(lastUsageBefore, 3));
		businessGroupDao.merge(after);
		dbInstance.commitAndCloseSession();

		// Check managed
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setLastUsageBefore(lastUsageBefore);
		List<StatisticsBusinessGroupRow> groups = businessGroupToSearchQueries.searchBusinessGroupsForSelection(params, identity);
		Assert.assertNotNull(groups);

		Set<Long> retrievedGroupkey = new HashSet<>();
		for(StatisticsBusinessGroupRow group:groups) {
			retrievedGroupkey.add(group.getKey());
		}
		Assert.assertTrue(retrievedGroupkey.contains(before.getKey()));
		Assert.assertFalse(retrievedGroupkey.contains(after.getKey()));
	}

	@Test
	public void findBusinessGroupsByIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id, group3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		//check owner
		SearchBusinessGroupParams paramsOwner = new SearchBusinessGroupParams();
		paramsOwner.setIdentity(id);
		paramsOwner.setOwner(true);
		List<BusinessGroup> ownedGroups = businessGroupDao.findBusinessGroups(paramsOwner, null, 0, 0);
		Assert.assertNotNull(ownedGroups);
		Assert.assertEquals(1, ownedGroups.size());
		Assert.assertTrue(ownedGroups.contains(group1));

		//check attendee
		SearchBusinessGroupParams paramsAttendee = new SearchBusinessGroupParams();
		paramsAttendee.setIdentity(id);
		paramsAttendee.setAttendee(true);
		List<BusinessGroup> attendeeGroups = businessGroupDao.findBusinessGroups(paramsAttendee, null, 0, 0);
		Assert.assertNotNull(attendeeGroups);
		Assert.assertEquals(1, attendeeGroups.size());
		Assert.assertTrue(attendeeGroups.contains(group2));

		//check waiting
		SearchBusinessGroupParams paramsWaiting = new SearchBusinessGroupParams();
		paramsWaiting.setIdentity(id);
		paramsWaiting.setWaiting(true);
		List<BusinessGroup> waitingGroups = businessGroupDao.findBusinessGroups(paramsWaiting, null, 0, 0);
		Assert.assertNotNull(waitingGroups);
		Assert.assertEquals(1, waitingGroups.size());
		Assert.assertTrue(waitingGroups.contains(group3));

		//check all
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setIdentity(id);
		paramsAll.setOwner(true);
		paramsAll.setAttendee(true);
		paramsAll.setWaiting(true);
		List<BusinessGroup> allGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(allGroups);
		Assert.assertEquals(3, allGroups.size());
		Assert.assertTrue(allGroups.contains(group1));
		Assert.assertTrue(allGroups.contains(group2));
		Assert.assertTrue(allGroups.contains(group3));

		//The same tests with the views
		//check owner on views
		BusinessGroupQueryParams queryParamsOwner = new BusinessGroupQueryParams();
		queryParamsOwner.setOwner(true);
		List<StatisticsBusinessGroupRow> ownedGroupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryParamsOwner, id);
		Assert.assertNotNull(ownedGroupViews);
		Assert.assertEquals(1, ownedGroupViews.size());
		Assert.assertTrue(contains(ownedGroupViews, group1));

		//check attendee on views
		BusinessGroupQueryParams queryParamsAttendee = new BusinessGroupQueryParams();
		queryParamsAttendee.setAttendee(true);
		List<StatisticsBusinessGroupRow> attendeeGroupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryParamsAttendee, id);
		Assert.assertNotNull(attendeeGroupViews);
		Assert.assertEquals(1, attendeeGroupViews.size());
		Assert.assertTrue(contains(attendeeGroupViews, group2));

		//check waiting on views
		BusinessGroupQueryParams queryParamsWaiting = new BusinessGroupQueryParams();
		queryParamsWaiting.setWaiting(true);
		List<StatisticsBusinessGroupRow> waitingGroupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryParamsWaiting, id);
		Assert.assertNotNull(waitingGroupViews);
		Assert.assertEquals(1, waitingGroupViews.size());
		Assert.assertTrue(contains(waitingGroupViews, group3));

		//check all on views
		BusinessGroupQueryParams queryParamsAll = new BusinessGroupQueryParams();
		queryParamsAll.setOwner(true);
		queryParamsAll.setAttendee(true);
		queryParamsAll.setWaiting(true);
		List<StatisticsBusinessGroupRow> allGroupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryParamsAll, id);
		Assert.assertNotNull(allGroupViews);
		Assert.assertEquals(3, allGroupViews.size());
		Assert.assertTrue(contains(allGroupViews, group1));
		Assert.assertTrue(contains(allGroupViews, group2));
		Assert.assertTrue(contains(allGroupViews, group3));
	}

	@Test
	public void findBusinessGroupsByRepositoryEntry() {
		//create a repository entry with a relation to a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-grp-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(id, "grp-course-1", "grp-course-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRelationToResource(group, re);
		dbInstance.commitAndCloseSession();

		//retrieve the group through its relation
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		params.setRepositoryEntry(re);
		List<StatisticsBusinessGroupRow> groupViews =  businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(1, groupViews.size());
		Assert.assertEquals(group.getKey(), groupViews.get(0).getKey());
	}


	@Test
	public void findBusinessGroupsByCourseTitle() {
		//create a repository entry with a relation to a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-grp-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(id, "grp-course-1", "grp-course-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRelationToResource(group, re);
		dbInstance.commitAndCloseSession();

		//retrieve the group through its relation
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		params.setCourseTitle(re.getDisplayname());
		List<StatisticsBusinessGroupRow> groupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(1, groupViews.size());
		Assert.assertEquals(group.getKey(), groupViews.get(0).getKey());
	}

	@Test
	public void findPublicGroups() {
		//create a group with an access control
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-11");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "access-grp-1", "access-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		//create and save an offer
		Offer offer = acService.createOffer(group.getResource(), "TestBGWorkflow");
		assertNotNull(offer);
		offer = acService.save(offer);

		acMethodManager.enableMethod(TokenAccessMethod.class, true);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//retrieve the offer
		//check the search with the views
		BusinessGroupQueryParams queryAllParams = new BusinessGroupQueryParams();
		queryAllParams.setPublicGroups(Boolean.TRUE);
		List<OpenBusinessGroupRow> accessGroupViews = businessGroupToSearchQueries.searchPublishedBusinessGroups(queryAllParams, identity);
		Assert.assertNotNull(accessGroupViews);
		Assert.assertTrue(accessGroupViews.size() >= 1);
		Assert.assertTrue(contains(accessGroupViews, group));

		for(OpenBusinessGroupRow accessGroup:accessGroupViews) {
			OLATResource resource = resourceManager.findResourceById(accessGroup.getResourceKey());
			List<Offer> offers = acService.findOfferByResource(resource, true, new Date());
			Assert.assertNotNull(offers);
			Assert.assertFalse(offers.isEmpty());
		}
	}

	@Test
	public void findPublicGroupsLimitedDate() {
		//create a group with an access control limited by a valid date
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-11");
		BusinessGroup groupVisible = businessGroupDao.createAndPersist(null, "access-grp-2", "access-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		//create and save an offer
		Offer offer = acService.createOffer(groupVisible.getResource(), "TestBGWorkflow");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		offer.setValidFrom(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		offer.setValidTo(cal.getTime());
		assertNotNull(offer);
		offer = acService.save(offer);

		acMethodManager.enableMethod(TokenAccessMethod.class, true);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		//create a group with an access control limited by dates in the past
		BusinessGroup oldGroup = businessGroupDao.createAndPersist(null, "access-grp-3", "access-grp-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		//create and save an offer
		Offer oldOffer = acService.createOffer(oldGroup.getResource(), "TestBGWorkflow");
		cal.add(Calendar.HOUR_OF_DAY, -5);
		oldOffer.setValidFrom(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, -5);
		oldOffer.setValidTo(cal.getTime());
		assertNotNull(oldOffer);
		oldOffer = acService.save(oldOffer);

		OfferAccess oldAccess = acMethodManager.createOfferAccess(oldOffer, method);
		acMethodManager.save(oldAccess);

		dbInstance.commitAndCloseSession();

		//retrieve the offer
		BusinessGroupQueryParams paramsAll = new BusinessGroupQueryParams();
		paramsAll.setPublicGroups(Boolean.TRUE);
		List<StatisticsBusinessGroupRow> accessGroups = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(paramsAll, id);
		Assert.assertNotNull(accessGroups);
		Assert.assertTrue(accessGroups.size() >= 1);
		Assert.assertTrue(contains(accessGroups, groupVisible));
		Assert.assertFalse(contains(accessGroups, oldGroup));
	}

	@Test
	public void findBusinessGroupsWithResources() {
		//create a group attach to a resource
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-10");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("marker-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "rsrc-grp-1", "rsrc-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "rsrc-grp-2", "rsrc-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		businessGroupRelationDao.addRelationToResource(group1, re);
		dbInstance.commitAndCloseSession();

		//check the same with the views
		//check the search function with resources
		BusinessGroupQueryParams queryWithParams = new BusinessGroupQueryParams();
		queryWithParams.setResources(Boolean.TRUE);
		List<StatisticsBusinessGroupRow> groupViewWith = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryWithParams, id);
		Assert.assertNotNull(groupViewWith);
		Assert.assertFalse(groupViewWith.isEmpty());
		Assert.assertTrue(contains(groupViewWith, group1));

		//check the search function without resources
		BusinessGroupQueryParams queryWithoutParams = new BusinessGroupQueryParams();
		queryWithoutParams.setResources(Boolean.FALSE);
		List<StatisticsBusinessGroupRow> groupViewWithout = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryWithoutParams, id);
		Assert.assertNotNull(groupViewWithout);
		Assert.assertFalse(groupViewWithout.isEmpty());
		Assert.assertTrue(contains(groupViewWithout, group2));
	}

	@Test
	public void findMarkedBusinessGroup() {
		Identity marker = JunitTestHelper.createAndPersistIdentityAsUser("marker-" + UUID.randomUUID().toString());
		//create a group with a mark and an other without as control
		BusinessGroup group1 = businessGroupDao.createAndPersist(marker, "marked-grp-1", "marked-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(marker, "marked-grp-2", "marked-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		markManager.setMark(group1.getResource(), marker, null, "[BusinessGroup:" + group1.getKey() + "]");
		dbInstance.commitAndCloseSession();

		//check the search with the views
		//check marked
		BusinessGroupQueryParams queryMarkedParams = new BusinessGroupQueryParams();
		queryMarkedParams.setOwner(true);
		queryMarkedParams.setMarked(true);
		List<StatisticsBusinessGroupRow> markedGroupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryMarkedParams, marker);
		Assert.assertNotNull(markedGroupViews);
		Assert.assertEquals(1, markedGroupViews.size());
		Assert.assertTrue(contains(markedGroupViews, group1));
		Assert.assertFalse(contains(markedGroupViews, group2));
	}

	@Test
	public void findMarkedBusinessGroupCrossContamination() {
		Identity marker1 = JunitTestHelper.createAndPersistIdentityAsUser("marker-1-" + UUID.randomUUID().toString());
		Identity marker2 = JunitTestHelper.createAndPersistIdentityAsUser("marker-2-" + UUID.randomUUID().toString());
		//create a group with a mark and an other without as control
		BusinessGroup group1 = businessGroupDao.createAndPersist(marker1, "marked-grp-3", "marked-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(marker1, "marked-grp-4", "marked-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		markManager.setMark(group1.getResource(), marker1, null, "[BusinessGroup:" + group1.getKey() + "]");
		markManager.setMark(group2.getResource(), marker2, null, "[BusinessGroup:" + group2.getKey() + "]");
		dbInstance.commitAndCloseSession();

		//check the search with views
		//check marked
		BusinessGroupQueryParams queryParamsMarker1 = new BusinessGroupQueryParams();
		queryParamsMarker1.setMarked(true);
		List<StatisticsBusinessGroupRow> markedGroupViews = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(queryParamsMarker1, marker1);
		Assert.assertNotNull(markedGroupViews);
		Assert.assertEquals(1, markedGroupViews.size());
		Assert.assertTrue(contains(markedGroupViews, group1));
	}

	@Test
	public void findBusinessGroupsHeadless() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("head-1-" + UUID.randomUUID().toString());
		BusinessGroup headlessGroup = businessGroupDao.createAndPersist(null, "headless-grp", "headless-grp-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup headedGroup = businessGroupDao.createAndPersist(owner, "headed-grp", "headed-grp-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check marked
		BusinessGroupQueryParams headlessParams = new BusinessGroupQueryParams();
		headlessParams.setHeadless(true);
		List<StatisticsBusinessGroupRow> groups = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(headlessParams, owner);
		Assert.assertNotNull(groups);
		Assert.assertFalse(groups.isEmpty());
		Assert.assertTrue(contains(groups, headlessGroup));
		Assert.assertFalse(contains(groups, headedGroup));
	}
	
	@Test
	public void searchBusinessGroupsForRepositoryEntry() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("stats-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("stats-2");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gstat", "gstat-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group, re);
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));

		List<StatisticsBusinessGroupRow> stats = businessGroupToSearchQueries.searchBusinessGroupsForRepositoryEntry(params, owner, re);
		Assert.assertNotNull(stats);
		Assert.assertEquals(1, stats.size());
	}
	
	/**
	 * The test is only there to check that all queries syntax is correct.
	 */
	@Test
	public void searchBusinessGroupsWithMembershipsAllLifecycle() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("stats-1");
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setLifecycleStatusReference(new Date());
		
		for(LifecycleSyntheticStatus status:LifecycleSyntheticStatus.values()) {
			params.setLifecycleStatus(status);
			List<StatisticsBusinessGroupRow> rows = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
			Assert.assertNotNull(rows);
		}	
	}
	
	@Test
	public void searchBusinessGroupsWithMembershipsActive() {
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("search-lifecycle-1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(id, "Search cycle 1.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1.setGroupStatus(BusinessGroupStatusEnum.active);
		group1.setLastUsage(DateUtils.addDays(new Date(), -500));
		group1 = businessGroupDao.merge(group1);
		
		// not active but long ago
		BusinessGroup group2 = businessGroupService.createBusinessGroup(id, "Search cycle 1.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group2.setGroupStatus(BusinessGroupStatusEnum.active);
		group2.setLastUsage(DateUtils.addDays(new Date(), -900));
		group2 = businessGroupDao.merge(group2);
		
		// LTI is not part of the deal
		BusinessGroup ltiGroup = businessGroupService.createBusinessGroup(id, "Search cycle 1.3", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		ltiGroup.setGroupStatus(BusinessGroupStatusEnum.active);
		ltiGroup.setLastUsage(DateUtils.addDays(new Date(), -500));
		ltiGroup = businessGroupDao.merge(ltiGroup);
		dbInstance.commitAndCloseSession();

		// Active active
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setLifecycleStatusReference(new Date());
		params.setLifecycleStatus(LifecycleSyntheticStatus.ACTIVE);
		params.setTechnicalTypes(businessGroupModule.getGroupLifecycleTypesList());
		List<StatisticsBusinessGroupRow> activeRows = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
		
		Assert.assertTrue(contains(activeRows, group1));// active
		Assert.assertTrue(contains(activeRows, group2));// active
		Assert.assertFalse(contains(activeRows, ltiGroup));// out of scope
		
		// Not active 
		params.setLifecycleStatus(LifecycleSyntheticStatus.TO_INACTIVATE);
		List<StatisticsBusinessGroupRow> notActiveRows = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
		Assert.assertFalse(contains(notActiveRows, group1));
		Assert.assertTrue(contains(notActiveRows, group2));
		Assert.assertFalse(contains(notActiveRows, ltiGroup));
	}
	
	@Test
	public void searchBusinessGroupsWithMembershipsInactive() {
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(120);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("search-lifecycle-1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(id, "Search cycle 1.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1.setGroupStatus(BusinessGroupStatusEnum.inactive);
		((BusinessGroupImpl)group1).setInactivationDate(new Date());
		group1 = businessGroupDao.merge(group1);
		
		// not active but long ago
		BusinessGroup group2 = businessGroupService.createBusinessGroup(id, "Search cycle 1.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group2.setGroupStatus(BusinessGroupStatusEnum.inactive);
		((BusinessGroupImpl)group2).setInactivationDate(DateUtils.addDays(new Date(), -900));
		group2 = businessGroupDao.merge(group2);
		
		// LTI is not part of the deal
		BusinessGroup ltiGroup = businessGroupService.createBusinessGroup(id, "Search cycle 1.3", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		ltiGroup.setGroupStatus(BusinessGroupStatusEnum.inactive);
		((BusinessGroupImpl)ltiGroup).setInactivationDate(DateUtils.addDays(new Date(), -150));
		ltiGroup = businessGroupDao.merge(ltiGroup);
		dbInstance.commitAndCloseSession();

		// Active active
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setLifecycleStatusReference(new Date());
		params.setLifecycleStatus(LifecycleSyntheticStatus.INACTIVE);
		params.setTechnicalTypes(businessGroupModule.getGroupLifecycleTypesList());
		List<StatisticsBusinessGroupRow> inactiveRows = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
		
		Assert.assertTrue(contains(inactiveRows, group1));
		Assert.assertTrue(contains(inactiveRows, group2));
		Assert.assertFalse(contains(inactiveRows, ltiGroup));
		
		// Not active 
		params.setLifecycleStatus(LifecycleSyntheticStatus.TO_SOFT_DELETE);
		List<StatisticsBusinessGroupRow> toDeleteRows = businessGroupToSearchQueries.searchBusinessGroupsWithMemberships(params, id);
		Assert.assertFalse(contains(toDeleteRows, group1));
		Assert.assertFalse(contains(toDeleteRows, group2));
		Assert.assertFalse(contains(toDeleteRows, ltiGroup));
	}
	
	

	private boolean contains(List<? extends BusinessGroupRef> rows, BusinessGroup group) {
		if(rows != null && !rows.isEmpty()) {
			for(BusinessGroupRef row:rows) {
				if(row.getKey().equals(group.getKey())) {
					return true;
				}
			}
		}
		return false;
	}
}
