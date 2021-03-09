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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.OpenBusinessGroupRow;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupDAOTest extends OlatTestCase {

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
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupDao);
	}

	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		dbInstance.commit();

		Assert.assertNotNull(group);
		Assert.assertNull(group.getMinParticipants());
		Assert.assertNull(group.getMaxParticipants());
		Assert.assertNotNull(group.getLastUsage());
		Assert.assertNotNull(group.getCreationDate());
		Assert.assertNotNull(group.getLastModified());
		Assert.assertNotNull(group.getResource());
		Assert.assertEquals("gdao", group.getName());
		Assert.assertEquals("gdao-desc", group.getDescription());
		Assert.assertFalse(group.getWaitingListEnabled());
		Assert.assertFalse(group.getAutoCloseRanksEnabled());
	}

	@Test
	public void loadBusinessGroupStandard() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdbo", "gdbo-desc", -1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());

		Assert.assertNotNull(reloadedGroup);
		Assert.assertNull(reloadedGroup.getMinParticipants());
		Assert.assertNull(reloadedGroup.getMaxParticipants());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertNotNull(group.getResource());
		Assert.assertEquals("gdbo", reloadedGroup.getName());
		Assert.assertEquals("gdbo-desc", reloadedGroup.getDescription());
		Assert.assertFalse(reloadedGroup.getWaitingListEnabled());
		Assert.assertFalse(reloadedGroup.getAutoCloseRanksEnabled());
	}

	@Test
	public void loadBusinessGroup() {
		//create business group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdco", "gdco-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check the saved values
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNotNull(reloadedGroup.getMinParticipants());
		Assert.assertNotNull(reloadedGroup.getMaxParticipants());
		Assert.assertEquals(0, reloadedGroup.getMinParticipants().intValue());
		Assert.assertEquals(10, reloadedGroup.getMaxParticipants().intValue());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertEquals("gdco", reloadedGroup.getName());
		Assert.assertEquals("gdco-desc", reloadedGroup.getDescription());
		Assert.assertTrue(reloadedGroup.getWaitingListEnabled());
		Assert.assertTrue(reloadedGroup.getAutoCloseRanksEnabled());
	}

	@Test
	public void loadBusinessGroup_fetch() {
		//create business group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gd-fetch", "gd-fetch-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		dbInstance.commitAndCloseSession();

		//check lazy
		Group baseGroup = reloadedGroup.getBaseGroup();
		Assert.assertNotNull(baseGroup);
		Assert.assertNotNull(baseGroup.getKey());
		OLATResource resource = reloadedGroup.getResource();
		Assert.assertNotNull(resource);
		Assert.assertNotNull(resource.getKey());
	}

	@Test
	public void loadBusinessGroup_forUpdate() {
		//create a group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdco", "gdco-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//load an lock
		BusinessGroup groupForUpdate = businessGroupDao.loadForUpdate(group.getKey());
		Assert.assertNotNull(groupForUpdate);
		Assert.assertEquals(group, groupForUpdate);
		dbInstance.commit();//release lock
	}

	@Test
	public void loadBusinessGroup_forUpdate_notFound() {
		//load and lock an inexistent group
		BusinessGroup groupForUpdate = businessGroupDao.loadForUpdate(Long.valueOf(0l));
		Assert.assertNull(groupForUpdate);
		dbInstance.commit();//release lock
	}

	@Test
	public void loadBusinessGroupWithOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gddo", "gddo-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check if the owner is in the owner security group
		Assert.assertNotNull(reloadedGroup);
		boolean isOwner = businessGroupRelationDao.hasRole(owner, reloadedGroup, GroupRoles.coach.name());
		Assert.assertTrue(isOwner);
	}

	@Test
	public void loadBusinessGroupsByIds() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-2-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdeo", "gdeo-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdfo", "gdfo-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//check if the method is robust against empty list fo keys
		List<BusinessGroup> groups1 = businessGroupDao.load(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());

		//check load 1 group
		List<BusinessGroup> groups2 = businessGroupDao.load(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1, groups2.get(0));

		//check load 2 groups
		List<Long> groupKeys = new ArrayList<>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroup> groups3 = businessGroupDao.load(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		Assert.assertTrue(groups3.contains(group1));
		Assert.assertTrue(groups3.contains(group2));
	}

	@Test
	public void loadShortBusinessGroupsByKeys() {
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "shorty-1", "shorty-1-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "shorty-2", "shorty-2-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//check if the method is robust against empty list fo keys
		List<BusinessGroupShort> groups1 = businessGroupDao.loadShort(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());

		//check load 1 group
		List<BusinessGroupShort> groups2 = businessGroupDao.loadShort(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1.getKey(), groups2.get(0).getKey());
		Assert.assertEquals(group1.getName(), groups2.get(0).getName());

		//check load 2 groups
		List<Long> groupKeys = new ArrayList<>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroupShort> groups3 = businessGroupDao.loadShort(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		List<Long> groupShortKeys3 = new ArrayList<>(3);
		for(BusinessGroupShort group:groups3) {
			groupShortKeys3.add(group.getKey());
		}
		Assert.assertTrue(groupShortKeys3.contains(group1.getKey()));
		Assert.assertTrue(groupShortKeys3.contains(group2.getKey()));
	}

	@Test
	public void loadDescription() {
		String description = "My desc " + UUID.randomUUID();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "load descr", description, 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		String loadDescription = businessGroupDao.loadDescription(group.getKey());
		Assert.assertNotNull(loadDescription);
		Assert.assertEquals(description, loadDescription);
	}

	@Test
	public void loadAllBusinessGroups() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdgo", "gdgo-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//load all business groups
		List<BusinessGroup> allGroups = businessGroupDao.loadAll();
		Assert.assertNotNull(allGroups);
		Assert.assertTrue(allGroups.size() >= 2);
		Assert.assertTrue(allGroups.contains(group1));
		Assert.assertTrue(allGroups.contains(group2));
	}

	@Test
	public void mergeBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//delete a business group
		group.setAutoCloseRanksEnabled(false);
		group.setName("gdho-2");

		//merge business group
		BusinessGroup mergedGroup = businessGroupDao.merge(group);
		Assert.assertNotNull(mergedGroup);
		Assert.assertEquals(group, mergedGroup);
		Assert.assertEquals("gdho-2", mergedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, mergedGroup.getAutoCloseRanksEnabled());

		dbInstance.commitAndCloseSession();

		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdho-2", reloadedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getAutoCloseRanksEnabled());
	}

	@Test
	public void updateBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-4-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdio", "gdio-desc", 1, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//delete a business group
		group.setWaitingListEnabled(false);
		group.setDescription("gdio-2-desc");

		//update business group (semantic of Hibernate before JPA)
		BusinessGroup updatedGroup = businessGroupDao.merge(group);
		Assert.assertNotNull(updatedGroup);
		Assert.assertEquals(group, updatedGroup);
		Assert.assertEquals("gdio-2-desc", updatedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, updatedGroup.getWaitingListEnabled());
		Assert.assertEquals(group, updatedGroup);

		dbInstance.commitAndCloseSession();

		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdio-2-desc", reloadedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getWaitingListEnabled());
	}

	@Test
	public void findBusinessGroupsWithWaitingListAttendedBy() {
		//3 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-5-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-6-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-7-" + UUID.randomUUID().toString());

		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();

		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.waiting.name());
		//id2 -> group 1 and 3
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.waiting.name());

		//check:
		//id1: group 1 and 2
		List<BusinessGroup> groupOfId1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id1,  null);
		Assert.assertNotNull(groupOfId1);
		Assert.assertTrue(groupOfId1.contains(group1));
		Assert.assertTrue(groupOfId1.contains(group2));
		//id2 -> group 1 and 3
		List<BusinessGroup> groupOfId2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id2,  null);
		Assert.assertNotNull(groupOfId2);
		Assert.assertTrue(groupOfId2.contains(group1));
		Assert.assertTrue(groupOfId2.contains(group3));

		List<BusinessGroup> groupOfId3 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id3,  null);
		Assert.assertNotNull(groupOfId3);
		Assert.assertTrue(groupOfId3.isEmpty());
	}

	@Test
	public void testVisibilityOfSecurityGroups() {
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdro", "gdro-desc", 0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdso", "gdso-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdto", "gdto-desc", 0, 5, true, false, false, false, true);
		dbInstance.commitAndCloseSession();

		//check the value
		Assert.assertTrue(group1.isOwnersVisibleIntern());
		Assert.assertTrue(group1.isParticipantsVisibleIntern());
		Assert.assertFalse(group1.isWaitingListVisibleIntern());

		Assert.assertFalse(group2.isOwnersVisibleIntern());
		Assert.assertTrue(group2.isParticipantsVisibleIntern());
		Assert.assertFalse(group2.isWaitingListVisibleIntern());

		Assert.assertFalse(group3.isOwnersVisibleIntern());
		Assert.assertFalse(group3.isParticipantsVisibleIntern());
		Assert.assertTrue(group3.isWaitingListVisibleIntern());
	}

	@Test
	public void findBusinessGroups() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gduo", "gduo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdvo", "gdvo-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertTrue(groups.size() >= 2);
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));

		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
		Assert.assertNotNull(groupViews);
		Assert.assertTrue(groupViews.size() >= 2);
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));

		List<StatisticsBusinessGroupRow> groupToSelect = businessGroupDao.searchBusinessGroupsForSelection(searchParams, identity);
		Assert.assertNotNull(groupToSelect);
		Assert.assertTrue(groupToSelect.size() >= 2);

		List<OpenBusinessGroupRow> openGroups = businessGroupDao.searchPublishedBusinessGroups(searchParams, identity);
		Assert.assertNotNull(openGroups);
	}

	@Test
	public void findBusinessGroupsByExactName() {
		String exactName = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, exactName, "gdwo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, exactName + "x", "gdxo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "y" +exactName, "gdyo-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName(exactName);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
	}

	@Test
	public void findBusinessGroupsByName() {
		String marker = UUID.randomUUID().toString();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-2");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, marker.toUpperCase(), "fingbg-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, marker + "xxx", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + marker.toUpperCase(), "fingbg-3-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group4 = businessGroupDao.createAndPersist(null, "yyyyZZZxxx", "fingbg-3-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, marker.toUpperCase(), "fingbg-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, marker + "xxx", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + marker.toUpperCase(), "fingbg-3-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker.toUpperCase() + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + marker, 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-other-one", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + marker.toUpperCase(), 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + marker + "-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker.toUpperCase() + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, marker.toUpperCase() + "-xxx", "desc-fingb-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", marker + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + marker.toUpperCase(), 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + marker + "-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, identity);
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

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbgown-1", "fingbgown-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbgown-2", "fingbgown-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbgown-3", "fingbgown-3-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group4 = businessGroupDao.createAndPersist(id4, "fingbgown-4", "fingbgown-4-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check the same with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setOwnerName(marker);
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, id1);
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

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbg-own-1-1", "fingbg-own-1-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbg-own-1-2", "fingbg-own-1-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbg-own-1-3", "fingbg-own-1-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check the same with the views
		BusinessGroupQueryParams searchParams = new BusinessGroupQueryParams();
		searchParams.setOwnerName("*" + marker + "*");
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(searchParams, id1);
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

		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(author, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(author, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, re);
		businessGroupRelationDao.addRelationToResource(group3, re);
		dbInstance.commitAndCloseSession();

		//check
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setAuthorConnection(true);
		List<StatisticsBusinessGroupRow> groups = businessGroupDao.searchBusinessGroupsForSelection(params, author);
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
		BusinessGroup groupManaged = businessGroupDao.createAndPersist(null, random(), random(), random(),
				managedFlags, 0, 5, true, false, true, false, false, null);
		// Groups with external ID should be treated as managed even if they have no managed flag.
		BusinessGroup groupExternalId = businessGroupDao.createAndPersist(null, random(), random(), random(),
				null, 0, 5, true, false, true, false, false, null);
		BusinessGroup groupUnmanaged = businessGroupDao.createAndPersist(null, random(), random(), null,
				null, 0, 5, true, false, true, false, false, null);
		dbInstance.commitAndCloseSession();

		// Check managed
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setManaged(Boolean.TRUE);
		List<StatisticsBusinessGroupRow> groups = businessGroupDao.searchBusinessGroupsForSelection(params, identity);
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
		groups = businessGroupDao.searchBusinessGroupsForSelection(params, identity);
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
		BusinessGroup before = businessGroupDao.createAndPersist(null, random(), random(), random(),
				null, 0, 5, true, false, true, false, false, null);
		before.setLastUsage(DateUtils.addDays(lastUsageBefore, -2));
		businessGroupDao.merge(before);
		BusinessGroup after = businessGroupDao.createAndPersist(null, random(), random(), null,
				null, 0, 5, true, false, true, false, false, null);
		after.setLastUsage(DateUtils.addDays(lastUsageBefore, 3));
		businessGroupDao.merge(after);
		dbInstance.commitAndCloseSession();

		// Check managed
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setLastUsageBefore(lastUsageBefore);
		List<StatisticsBusinessGroupRow> groups = businessGroupDao.searchBusinessGroupsForSelection(params, identity);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> ownedGroupViews = businessGroupDao.searchBusinessGroupsWithMemberships(queryParamsOwner, id);
		Assert.assertNotNull(ownedGroupViews);
		Assert.assertEquals(1, ownedGroupViews.size());
		Assert.assertTrue(contains(ownedGroupViews, group1));

		//check attendee on views
		BusinessGroupQueryParams queryParamsAttendee = new BusinessGroupQueryParams();
		queryParamsAttendee.setAttendee(true);
		List<StatisticsBusinessGroupRow> attendeeGroupViews = businessGroupDao.searchBusinessGroupsWithMemberships(queryParamsAttendee, id);
		Assert.assertNotNull(attendeeGroupViews);
		Assert.assertEquals(1, attendeeGroupViews.size());
		Assert.assertTrue(contains(attendeeGroupViews, group2));

		//check waiting on views
		BusinessGroupQueryParams queryParamsWaiting = new BusinessGroupQueryParams();
		queryParamsWaiting.setWaiting(true);
		List<StatisticsBusinessGroupRow> waitingGroupViews = businessGroupDao.searchBusinessGroupsWithMemberships(queryParamsWaiting, id);
		Assert.assertNotNull(waitingGroupViews);
		Assert.assertEquals(1, waitingGroupViews.size());
		Assert.assertTrue(contains(waitingGroupViews, group3));

		//check all on views
		BusinessGroupQueryParams queryParamsAll = new BusinessGroupQueryParams();
		queryParamsAll.setOwner(true);
		queryParamsAll.setAttendee(true);
		queryParamsAll.setWaiting(true);
		List<StatisticsBusinessGroupRow> allGroupViews = businessGroupDao.searchBusinessGroupsWithMemberships(queryParamsAll, id);
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
		BusinessGroup group = businessGroupDao.createAndPersist(id, "grp-course-1", "grp-course-1-desc", 0, 5, true, false, true, false, false);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRelationToResource(group, re);
		dbInstance.commitAndCloseSession();

		//retrieve the group through its relation
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		params.setRepositoryEntry(re);
		List<StatisticsBusinessGroupRow> groupViews =  businessGroupDao.searchBusinessGroupsWithMemberships(params, id);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(1, groupViews.size());
		Assert.assertEquals(group.getKey(), groupViews.get(0).getKey());
	}


	@Test
	public void findBusinessGroupsByCourseTitle() {
		//create a repository entry with a relation to a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-grp-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(id, "grp-course-1", "grp-course-1-desc", 0, 5, true, false, true, false, false);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRelationToResource(group, re);
		dbInstance.commitAndCloseSession();

		//retrieve the group through its relation
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		params.setCourseTitle(re.getDisplayname());
		List<StatisticsBusinessGroupRow> groupViews = businessGroupDao.searchBusinessGroupsWithMemberships(params, id);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(1, groupViews.size());
		Assert.assertEquals(group.getKey(), groupViews.get(0).getKey());
	}

	@Test
	public void findManagedGroups() {
		//create a managed group with an external ID
		String externalId = UUID.randomUUID().toString();
		String managedFlags = "title,description";
		BusinessGroup managedGroup = businessGroupDao.createAndPersist(null, "managed-grp-1", "managed-grp-1-desc",
				externalId, managedFlags, 0, 5, true, false, true, false, false, null);
		BusinessGroup freeGroup = businessGroupDao.createAndPersist(null, "free-grp-1", "free-grp-1-desc",
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//search managed group
		SearchBusinessGroupParams paramsManaged = new SearchBusinessGroupParams();
		paramsManaged.setManaged(Boolean.TRUE);
		List<BusinessGroup> managedGroups = businessGroupDao.findBusinessGroups(paramsManaged, null, 0, 0);
		Assert.assertNotNull(managedGroups);
		Assert.assertTrue(managedGroups.size() >= 1);
		Assert.assertTrue(managedGroups.contains(managedGroup));
		Assert.assertFalse(managedGroups.contains(freeGroup));

		//search free group
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setManaged(Boolean.FALSE);
		List<BusinessGroup> freeGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(freeGroups);
		Assert.assertTrue(freeGroups.size() >= 1);
		Assert.assertTrue(freeGroups.contains(freeGroup));
		Assert.assertFalse(freeGroups.contains(managedGroup));
	}


	@Test
	public void findGroupByExternalId() {
		//create a managed group with an external ID
		String externalId = UUID.randomUUID().toString();
		String managedFlags = "all";
		BusinessGroup group = businessGroupDao.createAndPersist(null, "managed-grp-2", "managed-grp-2-desc",
				externalId, managedFlags, 0, 5, true, false, true, false, false, null);
		dbInstance.commitAndCloseSession();

		//search
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setExternalId(externalId);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(group));
	}

	@Test
	public void findPublicGroups() {
		//create a group with an access control
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bg-search-11");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "access-grp-1", "access-grp-1-desc", 0, 5, true, false, true, false, false);
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
		List<OpenBusinessGroupRow> accessGroupViews = businessGroupDao.searchPublishedBusinessGroups(queryAllParams, identity);
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
		BusinessGroup groupVisible = businessGroupDao.createAndPersist(null, "access-grp-2", "access-grp-2-desc", 0, 5, true, false, true, false, false);
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
		BusinessGroup oldGroup = businessGroupDao.createAndPersist(null, "access-grp-3", "access-grp-3-desc", 0, 5, true, false, true, false, false);
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
		List<StatisticsBusinessGroupRow> accessGroups = businessGroupDao.searchBusinessGroupsWithMemberships(paramsAll, id);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "rsrc-grp-1", "rsrc-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "rsrc-grp-2", "rsrc-grp-2-desc", 0, 5, true, false, true, false, false);
		businessGroupRelationDao.addRelationToResource(group1, re);
		dbInstance.commitAndCloseSession();

		//check the same with the views
		//check the search function with resources
		BusinessGroupQueryParams queryWithParams = new BusinessGroupQueryParams();
		queryWithParams.setResources(Boolean.TRUE);
		List<StatisticsBusinessGroupRow> groupViewWith = businessGroupDao.searchBusinessGroupsWithMemberships(queryWithParams, id);
		Assert.assertNotNull(groupViewWith);
		Assert.assertFalse(groupViewWith.isEmpty());
		Assert.assertTrue(contains(groupViewWith, group1));

		//check the search function without resources
		BusinessGroupQueryParams queryWithoutParams = new BusinessGroupQueryParams();
		queryWithoutParams.setResources(Boolean.FALSE);
		List<StatisticsBusinessGroupRow> groupViewWithout = businessGroupDao.searchBusinessGroupsWithMemberships(queryWithoutParams, id);
		Assert.assertNotNull(groupViewWithout);
		Assert.assertFalse(groupViewWithout.isEmpty());
		Assert.assertTrue(contains(groupViewWithout, group2));
	}

	@Test
	public void findMarkedBusinessGroup() {
		Identity marker = JunitTestHelper.createAndPersistIdentityAsUser("marker-" + UUID.randomUUID().toString());
		//create a group with a mark and an other without as control
		BusinessGroup group1 = businessGroupDao.createAndPersist(marker, "marked-grp-1", "marked-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(marker, "marked-grp-2", "marked-grp-2-desc", 0, 5, true, false, true, false, false);
		markManager.setMark(group1.getResource(), marker, null, "[BusinessGroup:" + group1.getKey() + "]");
		dbInstance.commitAndCloseSession();

		//check the search with the views
		//check marked
		BusinessGroupQueryParams queryMarkedParams = new BusinessGroupQueryParams();
		queryMarkedParams.setOwner(true);
		queryMarkedParams.setMarked(true);
		List<StatisticsBusinessGroupRow> markedGroupViews = businessGroupDao.searchBusinessGroupsWithMemberships(queryMarkedParams, marker);
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
		BusinessGroup group1 = businessGroupDao.createAndPersist(marker1, "marked-grp-3", "marked-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(marker1, "marked-grp-4", "marked-grp-2-desc", 0, 5, true, false, true, false, false);
		markManager.setMark(group1.getResource(), marker1, null, "[BusinessGroup:" + group1.getKey() + "]");
		markManager.setMark(group2.getResource(), marker2, null, "[BusinessGroup:" + group2.getKey() + "]");
		dbInstance.commitAndCloseSession();

		//check the search with views
		//check marked
		BusinessGroupQueryParams queryParamsMarker1 = new BusinessGroupQueryParams();
		queryParamsMarker1.setMarked(true);
		List<StatisticsBusinessGroupRow> markedGroupViews = businessGroupDao.searchBusinessGroupsWithMemberships(queryParamsMarker1, marker1);
		Assert.assertNotNull(markedGroupViews);
		Assert.assertEquals(1, markedGroupViews.size());
		Assert.assertTrue(contains(markedGroupViews, group1));
	}

	@Test
	public void findBusinessGroupsHeadless() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("head-1-" + UUID.randomUUID().toString());
		BusinessGroup headlessGroup = businessGroupDao.createAndPersist(null, "headless-grp", "headless-grp-desc", 0, 5, true, false, true, false, false);
		BusinessGroup headedGroup = businessGroupDao.createAndPersist(owner, "headed-grp", "headed-grp-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check marked
		BusinessGroupQueryParams headlessParams = new BusinessGroupQueryParams();
		headlessParams.setHeadless(true);
		List<StatisticsBusinessGroupRow> groups = businessGroupDao.searchBusinessGroupsWithMemberships(headlessParams, owner);
		Assert.assertNotNull(groups);
		Assert.assertFalse(groups.isEmpty());
		Assert.assertTrue(contains(groups, headlessGroup));
		Assert.assertFalse(contains(groups, headedGroup));
	}

	@Test
	public void findBusinessGroups_my() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//check
		List<BusinessGroup> myLazyGroups = businessGroupDao.findBusinessGroup(id, 0);
		Assert.assertNotNull(myLazyGroups);
		Assert.assertEquals(2, myLazyGroups.size());
		List<Long> originalKeys = PersistenceHelper.toKeys(myLazyGroups);
		Assert.assertTrue(originalKeys.contains(group1.getKey()));
		Assert.assertTrue(originalKeys.contains(group2.getKey()));
		Assert.assertFalse(originalKeys.contains(group3.getKey()));
	}

	@Test
	public void isIdentityInBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id, group3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		List<BusinessGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);

		//check owner + attendee
		List<Long> groupKeysA = businessGroupDao.isIdentityInBusinessGroups(id, true, true, false, groups);
		Assert.assertNotNull(groupKeysA);
		Assert.assertEquals(2, groupKeysA.size());
		Assert.assertTrue(groupKeysA.contains(group1.getKey()));
		Assert.assertTrue(groupKeysA.contains(group2.getKey()));

		//check owner
		List<Long> groupKeysB = businessGroupDao.isIdentityInBusinessGroups(id, true, false, false, groups);
		Assert.assertNotNull(groupKeysB);
		Assert.assertEquals(1, groupKeysB.size());
		Assert.assertTrue(groupKeysB.contains(group1.getKey()));

		//check attendee
		List<Long> groupKeysC = businessGroupDao.isIdentityInBusinessGroups(id, false, true, false, groups);
		Assert.assertNotNull(groupKeysC);
		Assert.assertEquals(1, groupKeysC.size());
		Assert.assertTrue(groupKeysC.contains(group2.getKey()));
	}

	@Test
	public void getMembershipInfoInBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id, group3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee
		int countMembershipA = businessGroupDao.countMembershipInfoInBusinessGroups(id, groupKeys);
		Assert.assertEquals(3, countMembershipA);
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDao.getMembershipInfoInBusinessGroups(groupKeys, id);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(3, memberships.size());

		int found = 0;
		for(BusinessGroupMembershipViewImpl membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
			if(membership.getGroupKey() != null && group1.getKey().equals(membership.getGroupKey())) {
				found++;
			}
			if(membership.getGroupKey() != null && group2.getKey().equals(membership.getGroupKey())) {
				found++;
			}
			if(membership.getGroupKey() != null && group3.getKey().equals(membership.getGroupKey())) {
				found++;
			}
		}
		Assert.assertEquals(3, found);
	}

	@Test
	public void getBusinessGroupsMembership() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-rev-1" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-rev-2" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "is-in-grp-rev-1", "is-in-grp-rev-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "is-in-grp-rev-2", "is-in-grp-rev-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-rev-3", "is-in-grp-rev-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, group3, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<BusinessGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee
		int countMembership1 = businessGroupDao.countMembershipInfoInBusinessGroups(id1, groupKeys);
		int countMembership2 = businessGroupDao.countMembershipInfoInBusinessGroups(id2, groupKeys);
		Assert.assertEquals(3, countMembership1);
		Assert.assertEquals(3, countMembership2);
		List<BusinessGroupMembership> memberships = businessGroupDao.getBusinessGroupsMembership(groups);
		Assert.assertNotNull(memberships);
		//5: id1 3 membership in group1, group2, group3 and id2 2 memberships in group2 and group3
		Assert.assertEquals(5, memberships.size());

		int foundOwn = 0;
		int foundPart = 0;
		int foundWait = 0;
		for(BusinessGroupMembership membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
			Assert.assertNotNull(membership.getGroupKey());
			Assert.assertTrue(groupKeys.contains(membership.getGroupKey()));
			if(membership.isOwner()) {
				foundOwn++;
			}
			if(membership.isParticipant()) {
				foundPart++;
			}
			if(membership.isWaiting()) {
				foundWait++;
			}
		}
		Assert.assertEquals("Owners", 2, foundOwn);
		Assert.assertEquals("Participants", 3, foundPart);
		Assert.assertEquals("Waiting", 1, foundWait);
	}


	@Test
	public void getMembershipInfoInBusinessGroupsWithoutIdentityParam() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, group3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id3, group2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee + waiting
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDao.getMembershipInfoInBusinessGroups(groupKeys);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(7, memberships.size());
		for(BusinessGroupMembershipViewImpl membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
		}
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
