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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupLifecycleManagerImpl;
import org.olat.group.ui.lifecycle.BusinessGroupLifecycleTypeEnum;
import org.olat.ims.lti13.LTI13Service;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 9 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessGroupLifecycleManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManagerImpl lifecycleManager;
	
	@Test
	public void getInactivationDate() {
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Group cycle 40.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		Date inactivationDate = lifecycleManager.getInactivationDate(businessGroup);
		Assert.assertEquals(720, DateUtils.countDays(new Date(), inactivationDate));
		
		// change last usage
		businessGroup.setLastUsage(DateUtils.addDays(new Date(), -100));
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		inactivationDate = lifecycleManager.getInactivationDate(businessGroup);
		Assert.assertEquals(620, DateUtils.countDays(new Date(), inactivationDate));
		
		// reactivation
		((BusinessGroupImpl)businessGroup).setReactivationDate(DateUtils.addDays(new Date(), -10));
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		inactivationDate = lifecycleManager.getInactivationDate(businessGroup);
		Assert.assertEquals(20, DateUtils.countDays(new Date(), inactivationDate));	
	}
	
	@Test
	public void getInactivationResponseDelay() {
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Group cycle 41.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		long days = lifecycleManager.getInactivationResponseDelayUsed(businessGroup);
		Assert.assertEquals(-1l, days);
		
		// mail send 21 days ago
		((BusinessGroupImpl)businessGroup).setInactivationEmailDate(DateUtils.addDays(new Date(), -21));
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		days = lifecycleManager.getInactivationResponseDelayUsed(businessGroup);
		Assert.assertEquals(21l, days);
		
		// mail send 40 days ago (it can theoretically be bigger)
		((BusinessGroupImpl)businessGroup).setInactivationEmailDate(DateUtils.addDays(new Date(), -40));
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		days = lifecycleManager.getInactivationResponseDelayUsed(businessGroup);
		Assert.assertEquals(40l, days);
	}
	
	@Test
	public void getSoftDeleteDate() {
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(120);
		businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(30);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Group cycle 42.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		Date deleteDate = lifecycleManager.getSoftDeleteDate(businessGroup);
		Assert.assertNull(deleteDate);
		
		// inactivate
		((BusinessGroupImpl)businessGroup).setInactivationDate(DateUtils.addDays(new Date(), -40));
		((BusinessGroupImpl)businessGroup).setGroupStatus(BusinessGroupStatusEnum.inactive);
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		deleteDate = lifecycleManager.getSoftDeleteDate(businessGroup);
		Assert.assertEquals(80l, DateUtils.countDays(new Date(), deleteDate));
		
		// deleted
		((BusinessGroupImpl)businessGroup).setGroupStatus(BusinessGroupStatusEnum.trash);
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		deleteDate = lifecycleManager.getSoftDeleteDate(businessGroup);
		Assert.assertNull(deleteDate);
	}
	
	@Test
	public void getDefinitiveDeleteDate() {
		businessGroupModule.setNumberOfSoftDeleteDayBeforeDefinitivelyDelete(80);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Group cycle 42.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		Date deleteDate = lifecycleManager.getDefinitiveDeleteDate(businessGroup);
		Assert.assertNull(deleteDate);
		
		// inactivate
		((BusinessGroupImpl)businessGroup).setInactivationDate(DateUtils.addDays(new Date(), -900));
		((BusinessGroupImpl)businessGroup).setGroupStatus(BusinessGroupStatusEnum.inactive);
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		deleteDate = lifecycleManager.getDefinitiveDeleteDate(businessGroup);
		Assert.assertNull(deleteDate);
		
		// soft delete
		((BusinessGroupImpl)businessGroup).setGroupStatus(BusinessGroupStatusEnum.trash);
		((BusinessGroupImpl)businessGroup).setSoftDeleteDate(DateUtils.addDays(new Date(), 60));
		businessGroup = businessGroupDao.merge(businessGroup);
		dbInstance.commitAndCloseSession();
		
		deleteDate = lifecycleManager.getDefinitiveDeleteDate(businessGroup);
		Assert.assertEquals(20l, DateUtils.countDays(new Date(), deleteDate));
	}
	
	@Test
	public void getReadyToInactivateBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(id, "Group cycle 1.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group1, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));

		BusinessGroup group2 = businessGroupService.createBusinessGroup(id, "Group cycle 1.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group2, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -710));
		dbInstance.commitAndCloseSession();
		
		Date beforeDate = DateUtils.addDays(new Date(), -900);
		Date reactivationDateLimite = DateUtils.addDays(new Date(), -30);
		List<BusinessGroup> businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		Assert.assertNotNull(businessGroupsToInactivate);
		Assert.assertTrue(businessGroupsToInactivate.contains(group1));
		Assert.assertFalse(businessGroupsToInactivate.contains(group2));
		for(BusinessGroup businessGroupToInactivate:businessGroupsToInactivate) {
			Date lastUsageDate = businessGroupToInactivate.getLastUsage();
			if(lastUsageDate == null) {
				lastUsageDate = businessGroupToInactivate.getCreationDate();
			}
			
			Assert.assertTrue(lastUsageDate.before(beforeDate));
			assertThat(businessGroupToInactivate.getGroupStatus())
				.isIn(BusinessGroupStatusEnum.active);
		}
	}
	
	@Test
	public void getReadyToInactivateBusinessGroupsByTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-1");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(id, "Group cycle 51.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		businessGroup = setLastUsage(businessGroup, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));

		BusinessGroup ltiGroup = businessGroupService.createBusinessGroup(id, "Group cycle 51.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		ltiGroup = setLastUsage(ltiGroup, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));
		
		BusinessGroup managedGroup = businessGroupService.createBusinessGroup(id, "Group cycle 51.3", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		managedGroup = setLastUsage(managedGroup, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));
		managedGroup.setManagedFlagsString("all");
		managedGroup = businessGroupDao.merge(managedGroup);
		
		BusinessGroup notManagedGroup = businessGroupService.createBusinessGroup(id, "Group cycle 51.4", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		notManagedGroup = setLastUsage(notManagedGroup, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));
		managedGroup.setManagedFlagsString("");
		notManagedGroup = businessGroupDao.merge(notManagedGroup);
		
		RepositoryEntry pseudoCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup courseGroup =  businessGroupService.createBusinessGroup(id, "Group cycle 51.5", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, pseudoCourse);
		courseGroup = setLastUsage(courseGroup, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));
		dbInstance.commitAndCloseSession();

		Date beforeDate = DateUtils.addDays(new Date(), -900);
		Date reactivationDateLimite = DateUtils.addDays(new Date(), -30);
		
		// all enabled
		businessGroupModule.setGroupLifecycleTypes(List.of(BusinessGroupLifecycleTypeEnum.business.name(),
				BusinessGroupLifecycleTypeEnum.lti.name(), BusinessGroupLifecycleTypeEnum.managed.name(),
				BusinessGroupLifecycleTypeEnum.course.name()));
		List<BusinessGroup> businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		
		Assert.assertTrue(businessGroupsToInactivate.contains(businessGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(ltiGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(managedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(notManagedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(courseGroup));
		
		// business, lti, managed
		businessGroupModule.setGroupLifecycleTypes(List.of(BusinessGroupLifecycleTypeEnum.business.name(),
				BusinessGroupLifecycleTypeEnum.lti.name(), BusinessGroupLifecycleTypeEnum.managed.name()));
		businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		
		Assert.assertTrue(businessGroupsToInactivate.contains(businessGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(ltiGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(managedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(notManagedGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(courseGroup));
		
		// business, course
		businessGroupModule.setGroupLifecycleTypes(List.of(BusinessGroupLifecycleTypeEnum.business.name(),
				BusinessGroupLifecycleTypeEnum.course.name()));
		businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		
		Assert.assertTrue(businessGroupsToInactivate.contains(businessGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(ltiGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(managedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(notManagedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(courseGroup));
		
		// managed, course
		businessGroupModule.setGroupLifecycleTypes(List.of(BusinessGroupLifecycleTypeEnum.managed.name(),
				BusinessGroupLifecycleTypeEnum.course.name()));
		businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		
		Assert.assertFalse(businessGroupsToInactivate.contains(businessGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(ltiGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(managedGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(notManagedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(courseGroup));
		
		// course
		businessGroupModule.setGroupLifecycleTypes(List.of(BusinessGroupLifecycleTypeEnum.course.name()));
		businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		
		Assert.assertFalse(businessGroupsToInactivate.contains(businessGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(ltiGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(managedGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(notManagedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(courseGroup));

		// business (must be last)
		businessGroupModule.setGroupLifecycleTypes(List.of(BusinessGroupLifecycleTypeEnum.business.name()));
		businessGroupsToInactivate = lifecycleManager.getReadyToInactivateBusinessGroups(beforeDate, reactivationDateLimite);
		
		Assert.assertTrue(businessGroupsToInactivate.contains(businessGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(ltiGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(managedGroup));
		Assert.assertTrue(businessGroupsToInactivate.contains(notManagedGroup));
		Assert.assertFalse(businessGroupsToInactivate.contains(courseGroup));

	}
	

	
	@Test
	public void getBusinessGroupsToInactivate() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-2");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(id, "Group cycle 2.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group1, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -905));
		
		BusinessGroup group2 = businessGroupService.createBusinessGroup(id, "Group cycle 2.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group2,  BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -908));
		dbInstance.commitAndCloseSession();
		
		Date beforeDate = DateUtils.addDays(new Date(), -900);
		Date reactivationDatebefore = DateUtils.addDays(new Date(), -30);
		List<BusinessGroup> businessGroupsToInactivate = lifecycleManager.getBusinessGroupsToInactivate(beforeDate, null, reactivationDatebefore);
		Assert.assertNotNull(businessGroupsToInactivate);
		Assert.assertTrue(businessGroupsToInactivate.contains(group1));
		Assert.assertFalse(businessGroupsToInactivate.contains(group2));
		
		for(BusinessGroup businessGroupToInactivate:businessGroupsToInactivate) {
			Date lastLoginDate = businessGroupToInactivate.getLastUsage();
			if(lastLoginDate == null) {
				lastLoginDate = businessGroupToInactivate.getCreationDate();
			}
			
			Assert.assertTrue(lastLoginDate.before(beforeDate));
			assertThat(businessGroupToInactivate.getGroupStatus())
				.isIn(BusinessGroupStatusEnum.active);
		}
	}
	
	@Test
	public void inactivateASingleInformedBusinessGroup() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("enabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		businessGroupModule.setMailCopyAfterDeactivation(null);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-3");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 3.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setLastUsage(group1,  BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -1205));

		((BusinessGroupImpl)group1).setInactivationEmailDate(DateUtils.addDays(new Date(), -31));
		group1 = businessGroupDao.merge(group1);
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// check mails sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach.getUser().getEmail(), inactivedMessages));
		Assert.assertFalse(hasTo("copy@openolat.org", inactivedMessages));
		getSmtpServer().reset();
	}
	
	@Test
	public void inactivateASingleInformedBusinessGroupWithCopyEmail() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("enabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		businessGroupModule.setMailCopyAfterDeactivation("copy@openolat.org");
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-4");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 4.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setLastUsage(group1,  BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -1205));

		// Artificially mail in past
		((BusinessGroupImpl)group1).setInactivationEmailDate(DateUtils.addDays(new Date(), -31));
		group1 = businessGroupDao.merge(group1);
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// check mails sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach.getUser().getEmail(), inactivedMessages));
		Assert.assertTrue(hasTo("copy@openolat.org", inactivedMessages));
		getSmtpServer().reset();
	}
	
	@Test
	public void reactivateBusinessGroup() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("enabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-5");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 5.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setLastUsage(group1,  BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -910));

		group1 = lifecycleManager.changeBusinessGroupStatus(group1, BusinessGroupStatusEnum.inactive, coach);
		group1 = lifecycleManager.changeBusinessGroupStatus(group1, BusinessGroupStatusEnum.active, coach);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertNotNull(((BusinessGroupImpl)reloadedGroup1).getReactivationDate());
		Assert.assertNull(((BusinessGroupImpl)reloadedGroup1).getInactivationDate());
		Assert.assertNull(((BusinessGroupImpl)reloadedGroup1).getInactivatedBy());

		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		reloadedGroup1 = businessGroupService.loadBusinessGroup(reloadedGroup1);
		Assert.assertEquals(BusinessGroupStatusEnum.active, reloadedGroup1.getGroupStatus());
	}
	
	@Test
	public void inactivateBBusinessGroups() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("enabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-5.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach1, "Group cycle 5.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group1, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -1205));

		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecylce-5.2");
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach2, "Group cycle 5.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group2, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -708));

		Identity coach3 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecylce-5.3");
		BusinessGroup group3 = businessGroupService.createBusinessGroup(coach3, "Group cycle 5.3", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group3, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -1708));

		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// vetoed for the rest of the process
		Assert.assertTrue(vetoed.contains(group1));
		Assert.assertTrue(vetoed.contains(group2));
		
		// email is enabled
		BusinessGroup informedBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.active, informedBg1.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)informedBg1).getInactivationDate());
		Assert.assertNotNull(((BusinessGroupImpl)informedBg1).getInactivationEmailDate());
		
		BusinessGroup informedBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertNull(((BusinessGroupImpl)informedBg2).getInactivationDate());
		Assert.assertEquals(BusinessGroupStatusEnum.active, informedBg2.getGroupStatus());
		Assert.assertNotNull(((BusinessGroupImpl)informedBg2).getInactivationEmailDate());
		
		BusinessGroup permanentBg3 = businessGroupService.loadBusinessGroup(group3);
		Assert.assertNull(((BusinessGroupImpl)permanentBg3).getInactivationDate());
		Assert.assertEquals(BusinessGroupStatusEnum.active, permanentBg3.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)permanentBg3).getInactivationEmailDate());
		
		// check mails sent
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach1.getUser().getEmail(), messages));
		Assert.assertTrue(hasTo(coach2.getUser().getEmail(), messages));
		Assert.assertFalse(hasTo(coach3.getUser().getEmail(), messages));
		getSmtpServer().reset();
		
		// Artificially mail in past
		((BusinessGroupImpl)informedBg1).setInactivationEmailDate(DateUtils.addDays(new Date(), -31));
		informedBg1 = businessGroupDao.merge(informedBg1);
		((BusinessGroupImpl)informedBg2).setInactivationEmailDate(DateUtils.addDays(new Date(), -21));
		informedBg2 = businessGroupDao.merge(informedBg2);
		dbInstance.commitAndCloseSession();

		Set<BusinessGroup> vetoed2 = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed2);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup inactiveBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, inactiveBg1.getGroupStatus());
		Assert.assertNotNull(((BusinessGroupImpl)inactiveBg1).getInactivationDate());
		Assert.assertNotNull(((BusinessGroupImpl)inactiveBg1).getInactivationEmailDate());
		
		BusinessGroup inactiveBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertEquals(BusinessGroupStatusEnum.active, inactiveBg2.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg2).getInactivationDate());
		Assert.assertNotNull(((BusinessGroupImpl)inactiveBg2).getInactivationEmailDate());
		
		BusinessGroup ltiBg3 = businessGroupService.loadBusinessGroup(group3);
		Assert.assertEquals(BusinessGroupStatusEnum.active, ltiBg3.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)ltiBg3).getInactivationDate());
		Assert.assertNull(((BusinessGroupImpl)ltiBg3).getInactivationEmailDate());
		
		// check mails sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach1.getUser().getEmail(), inactivedMessages));
		Assert.assertFalse(hasTo(coach2.getUser().getEmail(), inactivedMessages));
		getSmtpServer().reset();
	}
	
	@Test
	public void inactivateBusinessGroupNegativeTest() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("enabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecylce-6.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach1, "Group cycle 6.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group1, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -600));
		
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecylce-6.2");
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach2, "Group cycle 6.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group2, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -600));
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// check no status changed for standard group and LTI groups
		BusinessGroup activeBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.active, activeBg1.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)activeBg1).getInactivationDate());
		Assert.assertNull(((BusinessGroupImpl)activeBg1).getInactivationEmailDate());
		
		BusinessGroup activeBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertEquals(BusinessGroupStatusEnum.active, activeBg2.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)activeBg2).getInactivationDate());
		Assert.assertNull(((BusinessGroupImpl)activeBg2).getInactivationEmailDate());
		
		// check no mails were sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertFalse(hasTo(coach1.getUser().getEmail(), inactivedMessages));
		Assert.assertFalse(hasTo(coach2.getUser().getEmail(), inactivedMessages));
		getSmtpServer().reset();	
	}
	
	/**
	 * Sending the E-mail is manual but inactivation is automatic.
	 * 
	 */
	@Test
	public void inactivateManuallyAutomaticallyAfterResponseTime() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("disabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecylce-37.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 37.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setLastUsage(group1, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -900));
		dbInstance.commitAndCloseSession();
		
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Group cycle 37.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group2 = setLastUsage(group2, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -900));
		((BusinessGroupImpl)group2).setInactivationEmailDate(DateUtils.addDays(new Date(), -40));
		group2 = businessGroupDao.merge(group2);
		dbInstance.commitAndCloseSession();
	
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateBusinessGroupsAfterResponseTime(vetoed);
		dbInstance.commitAndCloseSession();

		// the email date has a higher priority, the business group will not be inactivated
		BusinessGroup reloadedGroup1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.active, reloadedGroup1.getGroupStatus());
		Assert.assertNotNull(reloadedGroup1.getLastUsage());
		Assert.assertNull(((BusinessGroupImpl)reloadedGroup1).getInactivationDate());	
		Assert.assertNull(((BusinessGroupImpl)reloadedGroup1).getInactivationEmailDate());
		
		BusinessGroup reloadedGroup2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, reloadedGroup2.getGroupStatus());
		Assert.assertNotNull(reloadedGroup2.getLastUsage());
		Assert.assertNotNull(((BusinessGroupImpl)reloadedGroup2).getInactivationDate());	
		Assert.assertNotNull(((BusinessGroupImpl)reloadedGroup2).getInactivationEmailDate());	
	}
	
	/**
	 * Test the case of an administrator which play activate/inactivate. The
	 * usage date has a higher priority and win against the reactivation grace
	 * period of 30 days.
	 */
	@Test
	public void manuallyReactivatedBusinessGroup() {
		businessGroupModule.setAutomaticGroupInactivationEnabled("enabled");
		businessGroupModule.setMailBeforeDeactivation(true);
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(720);
		businessGroupModule.setNumberOfDayBeforeDeactivationMail(30);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecylce-7.1");
		BusinessGroup group = businessGroupService.createBusinessGroup(coach, "Group cycle 7.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setLastUsage(group, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -180));
		dbInstance.commitAndCloseSession();
		
		group = lifecycleManager.changeBusinessGroupStatus(group, BusinessGroupStatusEnum.inactive, coach);
		group = lifecycleManager.changeBusinessGroupStatus(group, BusinessGroupStatusEnum.active, coach);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(group.getLastUsage());
		Assert.assertNotNull(((BusinessGroupImpl)group).getReactivationDate());
		// set reactivation date before the limit of 30 days
		((BusinessGroupImpl)group).setReactivationDate(DateUtils.addDays(new Date(), -40));
		businessGroupDao.merge(group);
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();

		// the last usage date has a higher priority, the business group will not be inactivated again
		BusinessGroup reloadedBg = businessGroupService.loadBusinessGroup(group);
		Assert.assertEquals(BusinessGroupStatusEnum.active, reloadedBg.getGroupStatus());
		Assert.assertNotNull(reloadedBg.getLastUsage());
		Assert.assertNotNull(((BusinessGroupImpl)reloadedBg).getReactivationDate());	
	}
	
	@Test
	public void getReadyToSofDeleteBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-10");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(id, "Group cycle 10.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));

		BusinessGroup group2 = businessGroupService.createBusinessGroup(id, "Group cycle 10.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group2, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -100));
		dbInstance.commitAndCloseSession();
		
		Date beforeDate = DateUtils.addDays(new Date(), -120);
		List<BusinessGroup> businessGroupsToSoftDelete = lifecycleManager.getReadyToSoftDeleteBusinessGroups(beforeDate);
		Assert.assertNotNull(businessGroupsToSoftDelete);
		Assert.assertTrue(businessGroupsToSoftDelete.contains(group1));
		Assert.assertFalse(businessGroupsToSoftDelete.contains(group2));
		for(BusinessGroup businessGroupToSoftDelete:businessGroupsToSoftDelete) {
			Date inactivationDate = ((BusinessGroupImpl)businessGroupToSoftDelete).getInactivationDate();
			Assert.assertTrue(inactivationDate.before(beforeDate));
			assertThat(businessGroupToSoftDelete.getGroupStatus())
				.isIn(BusinessGroupStatusEnum.inactive);
		}
	}
	
	@Test
	public void getBusinessGroupsToSoftDelete() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-11");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 11.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));

		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Group cycle 11.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		group2 = setInactivationDate(group2,  BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		
		BusinessGroup group3 = businessGroupService.createBusinessGroup(coach, "Group cycle 11.3", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group3 = setInactivationDate(group3,  BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -100));
		
		BusinessGroup group4 = businessGroupService.createBusinessGroup(coach, "Group cycle 11.4", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group4 = setInactivationDate(group4, BusinessGroupStatusEnum.active, DateUtils.addDays(new Date(), -210));
		dbInstance.commitAndCloseSession();
		
		Date beforeDate = DateUtils.addDays(new Date(), -120);
		List<BusinessGroup> businessGroupsToSoftDelete = lifecycleManager.getBusinessGroupsToSoftDelete(beforeDate, null);
		Assert.assertNotNull(businessGroupsToSoftDelete);
		Assert.assertTrue(businessGroupsToSoftDelete.contains(group1));
		Assert.assertFalse(businessGroupsToSoftDelete.contains(group2));
		Assert.assertFalse(businessGroupsToSoftDelete.contains(group3));
		Assert.assertFalse(businessGroupsToSoftDelete.contains(group4));
		
		for(BusinessGroup businessGroupToInactivate:businessGroupsToSoftDelete) {
			Date inactivationDate = ((BusinessGroupImpl)businessGroupToInactivate).getInactivationDate();
			
			Assert.assertTrue(inactivationDate.before(beforeDate));
			assertThat(businessGroupToInactivate.getGroupStatus())
				.isIn(BusinessGroupStatusEnum.inactive);
		}
	}
	
	@Test
	public void getBusinessGroupsToSoftDeleteWithEmail() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-12");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 12.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		((BusinessGroupImpl)group1).setSoftDeleteEmailDate(DateUtils.addDays(new Date(), -60));
		group1 = businessGroupDao.merge(group1);
		dbInstance.commitAndCloseSession();

		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Group cycle 12.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		group2 = setInactivationDate(group2, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		dbInstance.commitAndCloseSession();
		
		Date beforeDate = DateUtils.addDays(new Date(), -120);
		Date emailDate = DateUtils.addDays(new Date(), -30);
		List<BusinessGroup> businessGroupsToSoftDelete = lifecycleManager.getBusinessGroupsToSoftDelete(beforeDate, emailDate);
		Assert.assertNotNull(businessGroupsToSoftDelete);
		Assert.assertTrue(businessGroupsToSoftDelete.contains(group1));
		Assert.assertFalse(businessGroupsToSoftDelete.contains(group2));
		
		for(BusinessGroup businessGroupToInactivate:businessGroupsToSoftDelete) {
			Date inactivationDate = ((BusinessGroupImpl)businessGroupToInactivate).getInactivationDate();
			
			Assert.assertTrue(inactivationDate.before(beforeDate));
			assertThat(businessGroupToInactivate.getGroupStatus())
				.isIn(BusinessGroupStatusEnum.inactive);
		}
	}
	
	@Test
	public void softDeleteASingleInformedBusinessGroupWithCopyEmail() {
		businessGroupModule.setAutomaticGroupSoftDeleteEnabled("enabled");
		businessGroupModule.setMailBeforeSoftDelete(true);
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(120);
		businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(30);
		businessGroupModule.setMailCopyAfterSoftDelete("copy@openolat.org");
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-14");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 14.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -205));

		// Artificially mail in past
		((BusinessGroupImpl)group1).setSoftDeleteEmailDate(DateUtils.addDays(new Date(), -32));
		group1 = businessGroupDao.merge(group1);
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.softDeleteAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// check mails sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach.getUser().getEmail(), inactivedMessages));
		Assert.assertTrue(hasTo("copy@openolat.org", inactivedMessages));
		getSmtpServer().reset();
	}

	@Test
	public void softDeleteBBusinessGroups() {
		businessGroupModule.setAutomaticGroupSoftDeleteEnabled("enabled");
		businessGroupModule.setMailBeforeSoftDelete(true);
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(120);
		businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(30);
		
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecycle-15.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach1, "Group cycle 15.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -205));

		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecylce-15.2");
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach2, "Group cycle 15.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group2, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -80));

		Identity coach3 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecylce-15.3");
		BusinessGroup group3 = businessGroupService.createBusinessGroup(coach3, "Group cycle 15.3", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group3, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -1708));

		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.softDeleteAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// vetoed for the rest of the process
		Assert.assertTrue(vetoed.contains(group1));
		Assert.assertFalse(vetoed.contains(group2));
		Assert.assertFalse(vetoed.contains(group3));
		
		// email is enabled
		BusinessGroup informedBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, informedBg1.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)informedBg1).getSoftDeleteDate());
		Assert.assertNotNull(((BusinessGroupImpl)informedBg1).getSoftDeleteEmailDate());
		
		BusinessGroup youngBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertNull(((BusinessGroupImpl)youngBg2).getSoftDeleteDate());
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, youngBg2.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)youngBg2).getSoftDeleteEmailDate());
		
		BusinessGroup permanentBg3 = businessGroupService.loadBusinessGroup(group3);
		Assert.assertNull(((BusinessGroupImpl)permanentBg3).getSoftDeleteDate());
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, permanentBg3.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)permanentBg3).getSoftDeleteEmailDate());
		
		// check mails sent
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach1.getUser().getEmail(), messages));
		Assert.assertFalse(hasTo(coach2.getUser().getEmail(), messages));
		Assert.assertFalse(hasTo(coach3.getUser().getEmail(), messages));
		getSmtpServer().reset();
		
		// Artificially mail in past
		((BusinessGroupImpl)informedBg1).setSoftDeleteEmailDate(DateUtils.addDays(new Date(), -31));
		informedBg1 = businessGroupDao.merge(informedBg1);
		dbInstance.commitAndCloseSession();

		Set<BusinessGroup> vetoed2 = new HashSet<>();
		lifecycleManager.softDeleteAutomaticallyBusinessGroups(vetoed2);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup inactiveBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.trash, inactiveBg1.getGroupStatus());
		Assert.assertNotNull(((BusinessGroupImpl)inactiveBg1).getSoftDeleteDate());
		Assert.assertNotNull(((BusinessGroupImpl)inactiveBg1).getSoftDeleteEmailDate());
		
		youngBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, youngBg2.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)youngBg2).getSoftDeleteDate());
		Assert.assertNull(((BusinessGroupImpl)youngBg2).getSoftDeleteEmailDate());
		
		BusinessGroup ltiBg3 = businessGroupService.loadBusinessGroup(group3);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, ltiBg3.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)ltiBg3).getSoftDeleteDate());
		Assert.assertNull(((BusinessGroupImpl)ltiBg3).getSoftDeleteEmailDate());
		
		// check mails sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertTrue(hasTo(coach1.getUser().getEmail(), inactivedMessages));
		Assert.assertFalse(hasTo(coach2.getUser().getEmail(), inactivedMessages));
		Assert.assertFalse(hasTo(coach3.getUser().getEmail(), inactivedMessages));
		getSmtpServer().reset();
	}
	
	@Test
	public void softDeleteBusinessGroupNegativeTest() {
		businessGroupModule.setAutomaticGroupSoftDeleteEnabled("enabled");
		businessGroupModule.setMailBeforeSoftDelete(true);
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(120);
		businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(30);
		
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecylce-16.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach1, "Group cycle 16.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -60));
		
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecylce-16.2");
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach2, "Group cycle 16.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group2, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -90));
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.softDeleteAutomaticallyBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// check no status changed for standard group and LTI groups
		BusinessGroup inactiveBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, inactiveBg1.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg1).getSoftDeleteDate());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg1).getSoftDeleteEmailDate());
		
		BusinessGroup inactiveBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, inactiveBg2.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg2).getSoftDeleteDate());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg2).getSoftDeleteEmailDate());
		
		// check no mails were sent
		List<SmtpMessage> inactivedMessages = getSmtpServer().getReceivedEmails();
		Assert.assertFalse(hasTo(coach1.getUser().getEmail(), inactivedMessages));
		Assert.assertFalse(hasTo(coach2.getUser().getEmail(), inactivedMessages));
		getSmtpServer().reset();	
	}
	
	/**
	 * Sending the E-mail is manual, soft deletion is automatic.
	 */
	@Test
	public void softDeleteManuallyAutomaticallyBusinessGroup() {
		businessGroupModule.setAutomaticGroupSoftDeleteEnabled("disabled");
		businessGroupModule.setMailBeforeSoftDelete(true);
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(120);
		businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(30);
		
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecylce-46.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach1, "Group cycle 46.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -160));
		
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecylce-46.2");
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach2, "Group cycle 46.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setInactivationDate(group2, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -160));
		((BusinessGroupImpl)group2).setSoftDeleteEmailDate(DateUtils.addDays(new Date(), -40));
		group2 = businessGroupDao.merge(group2);
		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.softDeleteBusinessGroupsAfterResponseTime(vetoed);
		dbInstance.commitAndCloseSession();
		
		// check no status changed for standard group and LTI groups
		BusinessGroup inactiveBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive, inactiveBg1.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg1).getSoftDeleteDate());
		Assert.assertNull(((BusinessGroupImpl)inactiveBg1).getSoftDeleteEmailDate());
		
		BusinessGroup deletedBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertEquals(BusinessGroupStatusEnum.trash, deletedBg2.getGroupStatus());
		Assert.assertNotNull(((BusinessGroupImpl)deletedBg2).getSoftDeleteDate());
		Assert.assertNotNull(((BusinessGroupImpl)deletedBg2).getSoftDeleteEmailDate());
	}
	
	@Test
	public void getBusinessGroupsToDefinitivelyDelete() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("group-lifecycle-20");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 20.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		group1 = setInactivationDate(group1, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		group1 = setSoftDeleteDate(group1, BusinessGroupStatusEnum.trash, DateUtils.addDays(new Date(), -160));
		dbInstance.commitAndCloseSession();

		// LTI group -> permanent
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Group cycle 20.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		group2 = setInactivationDate(group2, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		group2 = setSoftDeleteDate(group2, BusinessGroupStatusEnum.trash, DateUtils.addDays(new Date(), -140));
		dbInstance.commitAndCloseSession();
		
		// deleted but only 40 days
		BusinessGroup group3 = businessGroupService.createBusinessGroup(coach, "Group cycle 20.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		group3 = setInactivationDate(group3, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		group3 = setSoftDeleteDate(group3, BusinessGroupStatusEnum.trash, DateUtils.addDays(new Date(), -40));
		dbInstance.commitAndCloseSession();
		
		// only inactivated
		BusinessGroup group4 = businessGroupService.createBusinessGroup(coach, "Group cycle 20.2", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		group4 = setInactivationDate(group4, BusinessGroupStatusEnum.inactive, DateUtils.addDays(new Date(), -210));
		dbInstance.commitAndCloseSession();
		
		Date beforeDate = DateUtils.addDays(new Date(), -80);
		List<BusinessGroup> businessGroupsToDelete = lifecycleManager.getBusinessGroupsToDefinitivelyDelete(beforeDate);
		Assert.assertNotNull(businessGroupsToDelete);
		Assert.assertTrue(businessGroupsToDelete.contains(group1));
		Assert.assertFalse(businessGroupsToDelete.contains(group2));
		Assert.assertFalse(businessGroupsToDelete.contains(group3));
		Assert.assertFalse(businessGroupsToDelete.contains(group4));
		
		for(BusinessGroup businessGroupToDelete:businessGroupsToDelete) {
			Date softDeleteDate = ((BusinessGroupImpl)businessGroupToDelete).getSoftDeleteDate();
			
			Assert.assertTrue(softDeleteDate.before(beforeDate));
			assertThat(businessGroupToDelete.getGroupStatus())
				.isIn(BusinessGroupStatusEnum.trash);
		}
	}

	@Test
	public void definitivelyDeleteBusinessGroups() {
		businessGroupModule.setAutomaticGroupDefinitivelyDeleteEnabled("true");
		businessGroupModule.setNumberOfSoftDeleteDayBeforeDefinitivelyDelete(80);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gp-lifecycle-21.1");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Group cycle 21.1", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setSoftDeleteDate(group1, BusinessGroupStatusEnum.trash, DateUtils.addDays(new Date(), -205));

		// not ready to be deleted
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Group cycle 21.2", "", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		setSoftDeleteDate(group2, BusinessGroupStatusEnum.trash, DateUtils.addDays(new Date(), -60));

		// LTI group is permanent
		BusinessGroup group3 = businessGroupService.createBusinessGroup(coach, "Group cycle 21.3", "", LTI13Service.LTI_GROUP_TYPE,
				-1, -1, false, false, null);
		setSoftDeleteDate(group3, BusinessGroupStatusEnum.trash, DateUtils.addDays(new Date(), -1708));

		dbInstance.commitAndCloseSession();
		
		Set<BusinessGroup> vetoed = new HashSet<>();
		lifecycleManager.definitivelyDeleteBusinessGroups(vetoed);
		dbInstance.commitAndCloseSession();
		
		// vetoed for the rest of the process
		Assert.assertFalse(vetoed.contains(group1));
		Assert.assertFalse(vetoed.contains(group2));
		Assert.assertFalse(vetoed.contains(group3));
		
		// 1 is deleted
		BusinessGroup informedBg1 = businessGroupService.loadBusinessGroup(group1);
		Assert.assertNull(informedBg1);
		
		BusinessGroup youngBg2 = businessGroupService.loadBusinessGroup(group2);
		Assert.assertNotNull(((BusinessGroupImpl)youngBg2).getSoftDeleteDate());
		Assert.assertEquals(BusinessGroupStatusEnum.trash, youngBg2.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)youngBg2).getSoftDeleteEmailDate());
		
		BusinessGroup permanentBg3 = businessGroupService.loadBusinessGroup(group3);
		Assert.assertNotNull(((BusinessGroupImpl)permanentBg3).getSoftDeleteDate());
		Assert.assertEquals(BusinessGroupStatusEnum.trash, permanentBg3.getGroupStatus());
		Assert.assertNull(((BusinessGroupImpl)permanentBg3).getSoftDeleteEmailDate());
	}
	
	
	private BusinessGroup setLastUsage(BusinessGroup group, BusinessGroupStatusEnum status, Date date) {
		group.setLastUsage(date);
		group.setGroupStatus(status);
		group = businessGroupDao.merge(group);
		dbInstance.commitAndCloseSession();
		return group;
	}
	
	private BusinessGroup setInactivationDate(BusinessGroup group, BusinessGroupStatusEnum status, Date date) {
		((BusinessGroupImpl)group).setInactivationDate(date);
		group.setGroupStatus(status);
		group = businessGroupDao.merge(group);
		dbInstance.commitAndCloseSession();
		return group;
	}
	
	private BusinessGroup setSoftDeleteDate(BusinessGroup group, BusinessGroupStatusEnum status, Date date) {
		((BusinessGroupImpl)group).setSoftDeleteDate(date);
		((BusinessGroupImpl)group).setGroupStatus(status);
		group = businessGroupDao.merge(group);
		dbInstance.commitAndCloseSession();
		return group;
	}
	
	private boolean hasTo(String to, List<SmtpMessage> messages) {
		for(SmtpMessage message:messages) {
			if(message.getHeaderValue("To").contains(to)) {
				return true;
			}
		}
		return false;
	}

}
