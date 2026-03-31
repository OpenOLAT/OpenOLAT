/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.AcceptPolicyImpl;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void testSavePosition() {
		//create a position and save it
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("TR-808");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Short title");
		position.setDepartment("Gotham institute for psychology");
		position.setHomepage("http://www.asylum.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department of psychology ...");
		
		positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		//check the saved position
		Position retrievedPos = positionDao.loadPositionByKey(position.getKey());
		assertNotNull(retrievedPos);
		assertNotNull(retrievedPos.getCommitteeGroup());
		
		assertEquals("TR-808", retrievedPos.getPlaningsNumber());
		assertEquals("Prof.", retrievedPos.getPositionTitle());
		assertEquals("Short title", retrievedPos.getShortTitle());
		assertEquals("Gotham institute for psychology", retrievedPos.getDepartment());
		assertEquals("http://www.asylum.com", retrievedPos.getHomepage());
		assertNotNull(retrievedPos.getApplicationDeadline());
		assertEquals("in preparation", retrievedPos.getStatus());
		assertEquals("The department of psychology ...", retrievedPos.getDescription());
	}
	
	@Test
	public void testSavePositionWithCustomAttributes() {
		//create a position and save it
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("TOA-1200");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Short title");
		position.setDepartment("Artificial human department");
		position.setHomepage("https://www.toa.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department ...");
		
		positionDao.savePosition(position);
		dbInstance.commit();
		
		PositionAttributeDefinition attrDef = positionDao.createAttributeDefinition(position,
				PositionApplicationAttributeTabEnum.academicalBackground, PositionAttributeDefinitionTypeEnum.question,
				"Custom", null, true, null, null);
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();
		definitions.add(attrDef);
		
		PositionAttributeDefinition attrDef2 = positionDao.createAttributeDefinition(position,
				PositionApplicationAttributeTabEnum.academicalBackground, PositionAttributeDefinitionTypeEnum.question,
				"Custom 2", null, false, null, null);
		definitions.add(attrDef2);
		positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		Position reloadedPosition = positionDao.loadPositionByKey(position.getKey());
		List<PositionAttributeDefinition> reloadedDefinitions = reloadedPosition.getAttributesDefinitions();
		Assertions.assertThat(reloadedDefinitions)
			.hasSize(2);
	}
	
	@Test
	public void testDeletePosition() {
		//create position and save it
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("TR-809");
		position.setStatus("in preparation");
		
		positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		//check if it is really saved
		Position retrievedPos = positionDao.loadPositionByKey(position.getKey());
		assertNotNull(retrievedPos);
		assertEquals("TR-809", retrievedPos.getPlaningsNumber());
		
		//delete it
		positionDao.deletePosition(retrievedPos);
		dbInstance.commitAndCloseSession();
		
		//try to reload it
		Position deletedPos = positionDao.loadPositionByKey(position.getKey());
		assertNull(deletedPos);
	}
	
	@Test
	public void testFindPosition() {
		//create position and save it
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("TR-809");
		position.setStatus(PositionStatus.preparation.name());
		positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		//all
		List<Position> positions = positionDao.findPositions(null, true);
		Assert.assertNotNull(positions);
		Assert.assertTrue(positions.contains(position));
		
		//in preparation
		List<Position> positionsInPreparation = positionDao.findPositions(Collections.singletonList(PositionStatus.preparation), true);
		Assert.assertNotNull(positionsInPreparation);
		Assert.assertTrue(positions.contains(position));
		
		//preparation and published
		List<PositionStatus> statusList = new ArrayList<>();
		statusList.add(PositionStatus.preparation);
		statusList.add(PositionStatus.published);
		List<Position> multiStatusPositions = positionDao.findPositions(statusList, true);
		Assert.assertNotNull(multiStatusPositions);
		Assert.assertTrue(multiStatusPositions.contains(position));
		
		//closed
		List<Position> closedPositions = positionDao.findPositions(Collections.singletonList(PositionStatus.closed), true);
		Assert.assertNotNull(closedPositions);
		Assert.assertFalse(closedPositions.contains(position));
	}
	
	@Test
	public void findLastPosition() {
		Position pos1 = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos1.setPlaningsNumber("MX-809");
		pos1.setStatus(PositionStatus.preparation.name());
		pos1 = positionDao.savePosition(pos1);
		dbInstance.commitAndCloseSession();
		
		sleep(1200);
		Position pos2 = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos2.setPlaningsNumber("MX-810");
		pos2.setStatus(PositionStatus.preparation.name());
		pos2 = positionDao.savePosition(pos2);
		dbInstance.commitAndCloseSession();
		
		Position lastPos = positionDao.findLastPosition();
		Assert.assertNotNull(lastPos);
		Assert.assertEquals(pos2, lastPos);	
	}
	
	@Test
	public void loadPositionByKey() {
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos.setPlaningsNumber("MX-811");
		pos.setStatus(PositionStatus.preparation.name());
		pos = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();
		
		Position loadedPos = positionDao.loadPositionByKey(pos.getKey());
		Assert.assertNotNull(loadedPos);
		Assert.assertEquals(pos, loadedPos);
	}
	
	@Test
	public void getLastApplicationModification() {
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos.setPlaningsNumber("MX-812");
		pos.setStatus(PositionStatus.published.name());
		pos = positionDao.savePosition(pos);
		
		Application app1 = createRandomApplication(pos);
		Application app2 = createRandomApplication(pos);
		Application app3 = createRandomApplication(pos);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(app1);
		Assert.assertNotNull(app2);
		Assert.assertNotNull(app3);
		
		Date lastModified = positionDao.getLastApplicationModification(pos);
		Assert.assertNotNull(lastModified);
	}
	
	@Test
	public void loadPositionsToRemind() {
		Position posToRemind = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		posToRemind.setPlaningsNumber("MZ-812");
		posToRemind.setStatus(PositionStatus.publishedAndInScreening.name());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		posToRemind.setCommitteeReminderDate(cal.getTime());
		posToRemind = positionDao.savePosition(posToRemind);
		
		Position closedPos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		closedPos.setPlaningsNumber("MZ-813");
		closedPos.setStatus(PositionStatus.closedAndNoRating.name());
		closedPos.setCommitteeReminderDate(cal.getTime());
		closedPos = positionDao.savePosition(closedPos);
		
		Position sentPos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		sentPos.setPlaningsNumber("MZ-813");
		sentPos.setStatus(PositionStatus.closedAndNoRating.name());
		sentPos.setCommitteeReminderDate(cal.getTime());
		sentPos.setCommitteeReminderSentDate(cal.getTime());
		sentPos = positionDao.savePosition(sentPos);
		
		dbInstance.commitAndCloseSession();

		List<Position> positionsToRemind = positionDao.loadPositionsToRemind();
		Assert.assertNotNull(positionsToRemind);
		Assert.assertFalse(positionsToRemind.isEmpty());
		Assert.assertTrue(positionsToRemind.contains(posToRemind));
		Assert.assertFalse(positionsToRemind.contains(sentPos));
		Assert.assertFalse(positionsToRemind.contains(closedPos));
	}
	
	@Test
	public void loadPositionsToRemind_notNow() {
		Position posNotToRemind = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		posNotToRemind.setPlaningsNumber("MZ-812");
		posNotToRemind.setStatus(PositionStatus.publishedAndInScreening.name());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, +1);
		posNotToRemind.setCommitteeReminderDate(cal.getTime());
		posNotToRemind = positionDao.savePosition(posNotToRemind);

		dbInstance.commitAndCloseSession();

		List<Position> positionsToRemind = positionDao.loadPositionsToRemind();
		Assert.assertNotNull(positionsToRemind);
		Assert.assertFalse(positionsToRemind.contains(posNotToRemind));
	}
	
	@Test
	public void findPositions() {
		Position publishedPos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		publishedPos.setPlaningsNumber("MX-813");
		publishedPos.setStatus(PositionStatus.published.name());
		publishedPos = positionDao.savePosition(publishedPos);
		
		Position closedPos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		closedPos.setPlaningsNumber("MX-813");
		closedPos.setStatus(PositionStatus.closed.name());
		closedPos = positionDao.savePosition(closedPos);
		dbInstance.commitAndCloseSession();
		
		//find published positions
		List<PositionStatus> published = new ArrayList<>();
		published.add(PositionStatus.published);
		List<Position> publishedPositions = positionDao.findPositions(published, true);
		Assert.assertNotNull(publishedPositions);
		Assert.assertFalse(publishedPositions.isEmpty());
		Assert.assertTrue(publishedPositions.contains(publishedPos));
		Assert.assertFalse(publishedPositions.contains(closedPos));
		
		//find published and closed positions
		List<PositionStatus> publishedAndClosed = new ArrayList<>();
		publishedAndClosed.add(PositionStatus.published);
		publishedAndClosed.add(PositionStatus.closed);
		List<Position> publishedAndClosedPositions = positionDao.findPositions(publishedAndClosed, true);
		Assert.assertNotNull(publishedAndClosedPositions);
		Assert.assertFalse(publishedAndClosedPositions.isEmpty());
		Assert.assertTrue(publishedAndClosedPositions.contains(publishedPos));
		Assert.assertTrue(publishedAndClosedPositions.contains(closedPos));
		
		//find in preparation
		List<PositionStatus> inPreparation = new ArrayList<>();
		inPreparation.add(PositionStatus.preparation);
		List<Position> inPreparationPositions = positionDao.findPositions(inPreparation, true);
		Assert.assertNotNull(inPreparationPositions);
		Assert.assertFalse(inPreparationPositions.contains(publishedPos));
		Assert.assertFalse(inPreparationPositions.contains(closedPos));
	}
	
	@Test
	public void findParallelApplicationsLight() {
		//position 1
		Position pos1 = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos1.setPlaningsNumber("MX-814");
		pos1.setStatus(PositionStatus.published.name());
		pos1 = positionDao.savePosition(pos1);
		//position 2
		Position pos2 = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos2.setPlaningsNumber("MX-815");
		pos2.setStatus(PositionStatus.published.name());
		pos2 = positionDao.savePosition(pos2);
		
		String email = UUID.randomUUID().toString().replace("-", "") + "@frentix.com";
		Application app1 = createRandomApplication(pos1, email);
		Application app2 = createRandomApplication(pos2, email);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(app1);
		Assert.assertNotNull(app2);
		
		//find parallel applications
		List<PositionLight> parallelPositions = positionDao.findParallelApplicationsLight(email, pos1.getKey(), null);
		Assert.assertNotNull(parallelPositions);
		Assert.assertEquals(1, parallelPositions.size());
		Assert.assertEquals(pos2.getKey(), parallelPositions.get(0).getKey());
		
		//double check
		List<PositionLight> parallelPositionsInverted = positionDao.findParallelApplicationsLight(email, pos2.getKey(), null);
		Assert.assertNotNull(parallelPositionsInverted);
		Assert.assertEquals(1, parallelPositionsInverted.size());
		Assert.assertEquals(pos1.getKey(), parallelPositionsInverted.get(0).getKey());
	}
	
	@Test
	public void findPositions_applicant() {
		//position in preparation
		for(PositionStatus status:PositionStatus.values()) {
			Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
			pos.setPlaningsNumber("MX-816-" + status);
			pos.setStatus(status.name());
			pos = positionDao.savePosition(pos);
		}
		dbInstance.commitAndCloseSession();
		
		//published position
		List<Position> openPositions = positionDao.findPublishedPositions();
		Assert.assertNotNull(openPositions);
		Assert.assertTrue(openPositions.size() >= 2);
		
		for(Position position:openPositions) {
			String status = position.getStatus();
			boolean open = PositionStatus.published.name().equals(status)
					|| PositionStatus.publishedAndInScreening.name().equals(status);
			Assert.assertTrue(open);
		}
		
		//paranoia mode
		List<Position> hackedOpenPositions = positionDao.findPublishedPositions();
		Assert.assertNotNull(hackedOpenPositions);
		Assert.assertTrue(hackedOpenPositions.size() >= 2);
		
		for(Position position:hackedOpenPositions) {
			String status = position.getStatus();
			boolean open = PositionStatus.published.name().equals(status)
					|| PositionStatus.publishedAndInScreening.name().equals(status);
			Assert.assertTrue(open);
		}
	}
	
	@Test
	public void findPositions_committee() {
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Committee-pos-1-");
		dbInstance.commitAndCloseSession();
		
		//position in preparation
		for(PositionStatus status:PositionStatus.values()) {
			Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
			pos.setPlaningsNumber("MX-817-" + status);
			pos.setStatus(status.name());
			pos = positionDao.savePosition(pos);
			positionDao.addMemberToCommittee(pos, committeeMember);
		}
		dbInstance.commitAndCloseSession();
		
		//visible position
		Roles roles = securityManager.getRoles(committeeMember);
		PositionStatusFilters cmFilters = positionDao.getPositionStatusFilters(committeeMember, roles, null);
		List<PositionLightWithStatistics> openPositions = positionDao.findPositionsLightWithStatistics(committeeMember, cmFilters,
				Collections.emptyList(), true, Locale.ENGLISH);
		Assert.assertNotNull(openPositions);
		Assert.assertTrue(openPositions.size() >= 2);
		
		for(PositionLightWithStatistics position:openPositions) {
			String status = position.getStatus();
			boolean open = PositionStatus.publishedAndInScreening.name().equals(status)
					|| PositionStatus.closedAndInScreening.name().equals(status)
					|| PositionStatus.closedAndNoRating.name().equals(status);
			Assert.assertTrue(open);
		}
		
		//screening
		List<PositionStatus> screeningStatus = new ArrayList<>();
		screeningStatus.add(PositionStatus.publishedAndInScreening);
		screeningStatus.add(PositionStatus.closedAndInScreening);
		PositionStatusFilters cmScreeningFilters = positionDao.getPositionStatusFilters(committeeMember, roles, screeningStatus);
		List<PositionLightWithStatistics> screenedPositions = positionDao.findPositionsLightWithStatistics(committeeMember, cmScreeningFilters,
				Collections.emptyList(), true, Locale.ENGLISH);
		Assert.assertNotNull(screenedPositions);
		Assert.assertTrue(screenedPositions.size() >= 2);
		
		for(PositionLightWithStatistics position:screenedPositions) {
			String status = position.getStatus();
			boolean open = PositionStatus.publishedAndInScreening.name().equals(status)
					|| PositionStatus.closedAndInScreening.name().equals(status);
			Assert.assertTrue(open);
		}
		
		//paranoia mode
		List<PositionStatus> allStatus = new ArrayList<>();
		for(PositionStatus status:PositionStatus.values()) {
			allStatus.add(status);
		}
		PositionStatusFilters cmAllFilters = positionDao.getPositionStatusFilters(committeeMember, roles, allStatus);
		List<PositionLightWithStatistics> hackedOpenPositions = positionDao.findPositionsLightWithStatistics(committeeMember, cmAllFilters,
				Collections.emptyList(), true, Locale.ENGLISH);
		Assert.assertNotNull(hackedOpenPositions);
		Assert.assertTrue(hackedOpenPositions.size() >= 2);
		
		for(PositionLightWithStatistics position:hackedOpenPositions) {
			String status = position.getStatus();
			boolean open = PositionStatus.publishedAndInScreening.name().equals(status)
					|| PositionStatus.closedAndInScreening.name().equals(status)
					|| PositionStatus.closedAndNoRating.name().equals(status);
			Assert.assertTrue(open);
		}
	}
	

	@Test
	public void findPositionsSelectusManager() {
		Identity staffMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Staff-pos-1-", defaultUnitTestOrganisation, "test21");
		organisationService.addMember(organisationService.getDefaultOrganisation(), staffMember, OrganisationRoles.selectusmanager, null);
		dbInstance.commitAndCloseSession();
		
		//position in preparation
		List<Position> positions = new ArrayList<>();
		for(PositionStatus status:PositionStatus.values()) {
			Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
			pos.setPlaningsNumber("MX-818-" + status);
			pos.setStatus(status.name());
			pos = positionDao.savePosition(pos);
			positions.add(pos);
		}
		dbInstance.commitAndCloseSession();
		
		//published position
		Roles roles = securityManager.getRoles(staffMember);
		PositionStatusFilters staffFilters = positionDao.getPositionStatusFilters(staffMember, roles, null);
		List<PositionLightWithStatistics> openPositions = positionDao.findPositionsLightWithStatistics(staffMember, staffFilters,
				List.of(), true, Locale.ENGLISH);
		Assert.assertNotNull(openPositions);
		Assert.assertTrue(openPositions.size() >= PositionStatus.values().length);
		
		Set<Long> openPositionKeySet = new HashSet<>();
		for(PositionLightWithStatistics openPosition:openPositions) {
			openPositionKeySet.add(openPosition.getKey());
		}
		
		int found = 0;
		for(Position pos:positions) {
			if(openPositionKeySet.contains(pos.getKey())) {
				found++;
			}
		}
		Assert.assertEquals(positions.size(), found);
	}
	
	@Test
	public void isInCommittee() {
		Position publishedPos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		publishedPos.setPlaningsNumber("MX-830");
		publishedPos.setStatus(PositionStatus.published.name());
		publishedPos = positionDao.savePosition(publishedPos);
		
		Identity headOfCommittee = JunitTestHelper.createAndPersistIdentityAsRndUser("MX-830-head");
		positionDao.addMemberToCommittee(publishedPos, headOfCommittee);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("MX-830-nobody");
		
		boolean inCommittee = positionDao.isInCommittee(headOfCommittee);
		Assert.assertTrue(inCommittee);
		boolean notInCommittee = positionDao.isInCommittee(identity);
		Assert.assertFalse(notInCommittee);
	}
	
	@Test
	public void acceptPositionPolicy() {
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsAuthor("Committee-pos-2-");
		
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos.setPlaningsNumber("MX-819");
		pos.setStatus(PositionStatus.publishedAndInScreening.name());
		pos = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();
		
		//accept
		boolean allOk = positionDao.acceptPositionPolicy(pos, committeeMember, AcceptPolicyEnum.ratingPolicy, Boolean.TRUE);
		Assert.assertTrue(allOk);
		dbInstance.commit();
		
		//reload
		List<AcceptPolicyImpl> policies = positionDao.loadAcceptPolicy(pos, committeeMember);
		Assert.assertNotNull(policies);
		Assert.assertEquals(1, policies.size());
		
		AcceptPolicyImpl policy = policies.get(0);
		Assert.assertNotNull(policy);
		Assert.assertNotNull(policy.getKey());
		Assert.assertEquals(pos, policy.getPosition());
		Assert.assertEquals(committeeMember, policy.getIdentity());
		Assert.assertEquals(AcceptPolicyEnum.ratingPolicy.name(), policy.getName());
		Assert.assertTrue(policy.isDontShowNextTime());
	}
	
	@Test
	public void acceptPositionPolicy_messageToCommittee() {
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsAuthor("Committee-pos-2-");
		
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos.setPlaningsNumber("MX-819");
		pos.setStatus(PositionStatus.publishedAndInScreening.name());
		pos = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();
		
		//accept
		boolean allOk = positionDao.acceptPositionPolicy(pos, committeeMember, AcceptPolicyEnum.messageToCommittee, Boolean.TRUE);
		Assert.assertTrue(allOk);
		dbInstance.commit();
		
		//reload
		List<AcceptPolicyImpl> policies = positionDao.loadAcceptPolicy(pos, committeeMember);
		Assert.assertNotNull(policies);
		Assert.assertEquals(1, policies.size());
		
		AcceptPolicyImpl policy = policies.get(0);
		Assert.assertNotNull(policy);
		Assert.assertNotNull(policy.getKey());
		Assert.assertEquals(pos, policy.getPosition());
		Assert.assertEquals(committeeMember, policy.getIdentity());
		Assert.assertEquals(AcceptPolicyEnum.messageToCommittee.name(), policy.getName());
		Assert.assertTrue(policy.isDontShowNextTime());
	}
	
	@Test
	public void getMemberGroup() {
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		SecurityGroup committeeGroup = pos.getCommitteeGroup();
		SecurityGroup headGroup = securityGroupDao.createAndPersistSecurityGroup();
		pos.setCommitteeHeadGroup(headGroup);
		SecurityGroup secretaryGroup = securityGroupDao.createAndPersistSecurityGroup();
		pos.setSecretaryGroup(secretaryGroup);
		SecurityGroup exOfficioGroup = securityGroupDao.createAndPersistSecurityGroup();
		pos.setExOfficioGroup(exOfficioGroup);
		
		Position savedPosition = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();
		
		// check the method
		SecurityGroup loadedCommitteeGroup = positionDao.getMemberGroup(savedPosition, PositionRole.member);
		Assert.assertEquals(committeeGroup, loadedCommitteeGroup);
		SecurityGroup loadedHeadGroup = positionDao.getMemberGroup(savedPosition, PositionRole.head);
		Assert.assertEquals(headGroup, loadedHeadGroup);
		SecurityGroup loadedSecretaryGroup = positionDao.getMemberGroup(savedPosition, PositionRole.secretary);
		Assert.assertEquals(secretaryGroup, loadedSecretaryGroup);
		SecurityGroup loadedExOfficioGroup = positionDao.getMemberGroup(savedPosition, PositionRole.exofficio);
		Assert.assertEquals(exOfficioGroup, loadedExOfficioGroup);
	}
	
	@Test
	public void countMembers() {
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		Position savedPosition = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();
		
		for(int i=0; i<3; i++) {
			Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Count-com-" + i);
			positionDao.addMemberToCommittee(savedPosition, committeeMember);
		}
		
		// check the method
		long count = positionDao.countMembers(savedPosition, PositionRole.member);
		Assert.assertEquals(3, count);
	}
	

	private Application createRandomApplication(Position pos) {
		return createRandomApplication(pos, null);
	}

	private Application createRandomApplication(Position pos, String email) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Albert " + UUID.randomUUID());
		person.setLastName("Le Vert");
		person.setNationality("CH");
		if(email == null) {
			person.setMail( person.getFirstName().toLowerCase() + "@frentix.com");
		} else {
			person.setMail(email);
		}
		person.setBirthday(new Date());
		return applicationDao.saveTempApplication(app, true);
	}
}
