/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.BusinessInformationsImpl;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-unit-test", "Org-app-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}

	@Test
	public void testManagers() {
		assertNotNull(applicationDao);
	}
	
	@Test
	public void saveApplication() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		
		Person person = app.getPerson();
		person.setFirstName("Rei");
		person.setLastName("Ayanami");
		person.setNationality("JP");
		person.setMail("rei@nerv.co.jp");
		person.setPhone("9435892");
		person.setBirthday(new Date());
		
		Address add = app.getAddress();
		add.setAddressLine1("Private add 1");
		add.setAddressLine2("Private add 2");
		add.setZipCode("4050");
		add.setCity("Tokyo");
		add.setCountry("Japan");
		
		BusinessInformations bi = app.getBusinessInformations();
		((BusinessInformationsImpl)bi).setAffiliation("NERV");
		bi.setOrganization("Neon genesis evangelion");
		bi.setUnit("Research and development");
		bi.setCurrentPosition("Pilot");

		AcademicalBackground bg = app.getAcademicalBackground();
		bg.setHighestDegreeType(HighestDegreeType.master.name());
		bg.setHighestDegreeDate(new Date());
		bg.setHighestDegreeInstitution("NERV Institut");


		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		//check if saved
		assertNotNull(app.getKey());
		Application retrievedApp = applicationDao.loadApplicationByKey(app.getKey());
		assertNotNull(retrievedApp.getPerson());
		assertNotNull(retrievedApp.getAddress());
		assertNotNull(retrievedApp.getBusinessInformations());
		assertNotNull(retrievedApp.getAcademicalBackground());

		//person
		assertEquals("Rei", retrievedApp.getPerson().getFirstName());
		assertEquals("Ayanami", retrievedApp.getPerson().getLastName());
		assertEquals("JP", retrievedApp.getPerson().getNationality());
		assertEquals("rei@nerv.co.jp", retrievedApp.getPerson().getMail());
		assertEquals("9435892", retrievedApp.getPerson().getPhone());
		assertNotNull(retrievedApp.getPerson().getBirthday());
		
		//address
		assertEquals("Private add 1", retrievedApp.getAddress().getAddressLine1());
		assertEquals("Private add 2", retrievedApp.getAddress().getAddressLine2());
		assertEquals("4050", retrievedApp.getAddress().getZipCode());
		assertEquals("Tokyo", retrievedApp.getAddress().getCity());
		assertEquals("Japan", retrievedApp.getAddress().getCountry());
		
		//business informations
		assertEquals("NERV", retrievedApp.getBusinessInformations().getAffiliation());
		assertEquals("Research and development", retrievedApp.getBusinessInformations().getUnit());
		assertEquals("Pilot", retrievedApp.getBusinessInformations().getCurrentPosition());
		
		//highest degree
		assertEquals("master", retrievedApp.getAcademicalBackground().getHighestDegreeType());
		assertNotNull(retrievedApp.getAcademicalBackground().getHighestDegreeDate());
		assertEquals("NERV Institut", retrievedApp.getAcademicalBackground().getHighestDegreeInstitution());
	}
	
	
	@Test
	public void testSaveApplicationWithCustomAttributes() {
		Position position = createRandomPosition(PositionStatus.published);
		
		PositionAttributeDefinition attrDef = positionDao.createAttributeDefinitionAndPersist(position,
				PositionApplicationAttributeTabEnum.academicalBackground, PositionAttributeDefinitionTypeEnum.question,
				"Custom", null, null, true, null, null);
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();
		definitions.add(attrDef);
		
		PositionAttributeDefinition attrDef2 = positionDao.createAttributeDefinitionAndPersist(position,
				PositionApplicationAttributeTabEnum.academicalBackground, PositionAttributeDefinitionTypeEnum.question,
				"Custom 2", null, null, false, null, null);
		definitions.add(attrDef2);
		position = positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		dbInstance.commit();
		
		ApplicationAttribute attr1 = applicationDao.createApplicationAttribute(null, app, attrDef, "Val");
		app.getAttributes().add(attr1);
		ApplicationAttribute attr2 = applicationDao.createApplicationAttribute(null, app, attrDef2, "ue");
		app.getAttributes().add(attr2);
		applicationDao.saveApplication(app);
		dbInstance.commit();
		
		Application reloadedApp = applicationDao.loadApplicationByKey(app.getKey());
		Set<ApplicationAttribute> reloadedAttributes = reloadedApp.getAttributes();
		Assert.assertNotNull(reloadedAttributes);
		Assert.assertEquals(2, reloadedAttributes.size());
		for(ApplicationAttribute reloadedAttribute:reloadedAttributes) {
			String value = reloadedAttribute.getValue();
			Assert.assertTrue("Val".equals(value) || "ue".equals(value));
		}
	}
	
	@Test
	public void deleteSimpleApplication() {
		Position pos = createRandomPosition(PositionStatus.closed);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("John");
		person.setLastName("Deleted");
		applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		//delete it
		applicationDao.deleteApplication(app);
		dbInstance.commitAndCloseSession();
		
		//try to retrieve it
		Application retrievedApp = applicationDao.loadApplicationByKey(app.getKey());
		assertNull(retrievedApp);
		
		//add other methods to get applications
	}
	
	@Test
	public void testFindApplication() {
		//create an app.
		Position pos = createRandomPosition(PositionStatus.closedAndNoRating);
		Application validApp = applicationDao.createApplication(pos);
		Person person = validApp.getPerson();
		person.setFirstName("John");
		person.setLastName("Find valid");
		validApp = applicationDao.saveTempApplication(validApp, true);
		dbInstance.commitAndCloseSession();
		
		Application tempApp = applicationDao.createApplication(pos);
		tempApp.getPerson().setFirstName("John");
		tempApp.getPerson().setLastName("Find temporary");
		tempApp = applicationDao.saveApplication(tempApp);
		dbInstance.commitAndCloseSession();
		
		//load valid
		List<Application> apps = applicationDao.findApplications(null, true);
		Assert.assertNotNull(apps);
		Assert.assertTrue(1 <= apps.size());
		Assert.assertTrue(apps.contains(validApp));
		Assert.assertFalse(apps.contains(tempApp));
		
		//load temporary
		List<Application> tempApps = applicationDao.findApplications(null, false);
		Assert.assertNotNull(tempApps);
		Assert.assertTrue(1 <= tempApps.size());
		Assert.assertFalse(tempApps.contains(validApp));
		Assert.assertTrue(tempApps.contains(tempApp));
	}
	
	@Test
	public void testFindApplication_withPosition() {
		//create an app.
		Position pos = createRandomPosition(PositionStatus.closedAndNoRating);
		Application validApp = applicationDao.createApplication(pos);
		Person person = validApp.getPerson();
		person.setFirstName("John");
		person.setLastName("Find");
		applicationDao.saveTempApplication(validApp, true);
		dbInstance.commitAndCloseSession();
		
		Application tempApp = applicationDao.createApplication(pos);
		tempApp.getPerson().setFirstName("John");
		tempApp.getPerson().setLastName("Find temporary");
		tempApp = applicationDao.saveApplication(tempApp);
		dbInstance.commitAndCloseSession();
		
		//load only valid
		List<Application> apps = applicationDao.findApplications(pos, true);
		Assert.assertNotNull(apps);
		Assert.assertEquals(1, apps.size());
		Assert.assertTrue(apps.contains(validApp));
		Assert.assertFalse(apps.contains(tempApp));
		
		//load only temporary
		List<Application> tempApps = applicationDao.findApplications(pos, false);
		Assert.assertNotNull(tempApps);
		Assert.assertTrue(1 <= tempApps.size());
		Assert.assertFalse(tempApps.contains(validApp));
		Assert.assertTrue(tempApps.contains(tempApp));
	}
	
	@Test
	public void checkUniqueApplication() {
		// create app with the reference email
		Position pos = createRandomPosition(PositionStatus.closedAndNoRating);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("John");
		person.setLastName("Find");
		person.setMail("john.smith@frentix.com");
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		
		// transient
		Application secondTryApp = applicationDao.createApplication(pos);
		secondTryApp.getPerson().setFirstName("John");
		secondTryApp.getPerson().setLastName("Find");
		secondTryApp.getPerson().setMail("john.smith@frentix.com");
		boolean isUnique = applicationDao.checkUniqueApplicationByEmail(secondTryApp);
		Assert.assertFalse(isUnique);
		
		Application uniqueTryApp = applicationDao.createApplication(pos);
		uniqueTryApp.getPerson().setFirstName("John");
		uniqueTryApp.getPerson().setLastName("Find");
		uniqueTryApp.getPerson().setMail("john.smith.zac.6432@frentix.com");
		boolean isReallyUnique = applicationDao.checkUniqueApplicationByEmail(uniqueTryApp);
		Assert.assertTrue(isReallyUnique);
	}
	
	@Test
	public void checkUniqueApplicationWithFirstnameLastname() {
		// create app with the reference email
		Position pos = createRandomPosition(PositionStatus.closedAndNoRating);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("John");
		person.setLastName("Find");
		person.setMail("john.smith@frentix.com");
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		// transient
		Application secondTryApp = applicationDao.createApplication(pos);
		secondTryApp.getPerson().setFirstName("John");
		secondTryApp.getPerson().setLastName("Find");
		secondTryApp.getPerson().setMail("john.smith@frentix.com");
		boolean isUnique = applicationDao.checkUniqueApplicationByEmailFistnameLastname(secondTryApp);
		Assert.assertFalse(isUnique);
		
		Application uniqueTryApp = applicationDao.createApplication(pos);
		uniqueTryApp.getPerson().setFirstName("John");
		uniqueTryApp.getPerson().setLastName("Find");
		uniqueTryApp.getPerson().setMail("john.smith.zac.6432@frentix.com");
		boolean isReallyUnique = applicationDao.checkUniqueApplicationByEmailFistnameLastname(uniqueTryApp);
		Assert.assertTrue(isReallyUnique);
		
		Application childApp= applicationDao.createApplication(pos);
		childApp.getPerson().setFirstName("Gabriella");
		childApp.getPerson().setLastName("Find");
		childApp.getPerson().setMail("john.smith@frentix.com");
		boolean childIsUnique = applicationDao.checkUniqueApplicationByEmailFistnameLastname(childApp);
		Assert.assertTrue(childIsUnique);
	}
	
	@Test
	public void getNextAppId() {
		// create a first app as reference for the id
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application referenceApp = applicationDao.createApplication(pos);
		Person person = referenceApp.getPerson();
		person.setFirstName("John");
		person.setLastName("Reference id");
		person.setMail("john.smith.ref@frentix.com");
		referenceApp = applicationDao.saveTempApplication(referenceApp, true);
		dbInstance.commitAndCloseSession();
		
		Application nextApp = applicationDao.createApplication(pos);
		nextApp.getPerson().setFirstName("John");
		nextApp.getPerson().setLastName("Next id");
		nextApp.getPerson().setMail("john.smith.next@frentix.com");
		nextApp = applicationDao.saveTempApplication(nextApp, true);
		dbInstance.commitAndCloseSession();
		
		//check the application id
		Integer referenceId = referenceApp.getId();
		Integer nextId = nextApp.getId();
		Assert.assertNotNull(referenceId);
		Assert.assertNotNull(nextId);
		Assert.assertEquals(referenceId.intValue() + 1, nextId.intValue());
	}
	
	@Test
	public void loadApplicationByKey() {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("John");
		person.setLastName("Load app by Key");
		person.setMail("john.loadappbykey@frentix.com");
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		Application reloadedApp = applicationDao.loadApplicationByKey(app.getKey());
		Assert.assertNotNull(reloadedApp);
		Assert.assertEquals(app, reloadedApp);
	}
	
	@Test
	public void loadApplicationsByKeyForRelations() {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Manfred");
		person.setLastName("Load apps by Keys");
		person.setMail("john.loadappsbykeys@frentix.com");
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		List<Long> keys = Collections.singletonList(app.getKey());
		List<Application> apps = applicationDao.loadApplicationsByKeyForRelations(keys);
		Assert.assertNotNull(apps);
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}
	
	@Test
	public void loadValidApplicationByKey() {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("John");
		person.setLastName("Load valid app by Key");
		person.setMail("john.loadvalidappbykey@frentix.com");
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		//check it doesn't load non-valid application
		Application notValidApp = applicationDao.loadValidApplicationByKey(app.getKey());
		Assert.assertNull(notValidApp);
		
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		Application validApp = applicationDao.loadValidApplicationByKey(app.getKey());
		Assert.assertNotNull(validApp);
		Assert.assertEquals(app, validApp);
	}
	
	@Test
	public void loadApplicationByApplicantKey() {
		// Create an application
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("John");
		person.setLastName("Load valid app by Key");
		person.setMail("john.loadvalidappbyapplicanturl@frentix.com");
		app = applicationDao.saveApplication(app);
		dbInstance.commit();
		
		// Add an applicant identity
		Identity applicant = JunitTestHelper.createAndPersistIdentityAsRndUser("applicant-1");
		app.setIdentity(applicant);
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		// Load with the key used in URL
		Application loadedApp = applicationDao.loadApplicationByApplicantKey(app.getApplicantUrl());
		Assert.assertEquals(app, loadedApp);
	}
	
	@Test
	public void hasApplicationByIdentity() {
		// Create an application
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Jane");
		person.setLastName("Has app");
		person.setMail("jane.hasapp@frentix.com");
		app = applicationDao.saveApplication(app);
		dbInstance.commit();
		
		// Add an applicant identity
		Identity applicant = JunitTestHelper.createAndPersistIdentityAsRndUser("applicant-2");
		Identity notAnApplicant = JunitTestHelper.createAndPersistIdentityAsRndUser("not-applicant-3");
		app.setIdentity(applicant);
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		// Check if the users have applications or not
		boolean hasApp = applicationDao.hasApplicationByIdentity(applicant);
		Assert.assertTrue(hasApp);
		boolean hasNotApp = applicationDao.hasApplicationByIdentity(notAnApplicant);
		Assert.assertFalse(hasNotApp);
	}
	
	@Test
	public void loadApplicationsByApplicant() {
		// Create an application
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Henry");
		person.setLastName("Apps");
		person.setMail("henry.apps@frentix.com");
		app = applicationDao.saveApplication(app);
		dbInstance.commit();
		
		// Enable referee dashboard
		pos.setRefereeRecommandationDeadline(DateUtils.addDays(pos.getCreationDate(), 12));
		pos.setRefereeRecommendationEnabled(true);
		pos.setApplicantRefereeManagementDeadline(DateUtils.addDays(pos.getCreationDate(), 12));
		pos.setApplicantRefereeManagementEnabled(true);
		pos = positionDao.savePosition(pos);
		
		// Add an applicant identity
		Identity applicant = JunitTestHelper.createAndPersistIdentityAsRndUser("applicant-4");
		app.setIdentity(applicant);
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();
		
		// Load all applications
		boolean hasApp = applicationDao.hasApplicationsByApplicant(applicant);
		Assert.assertTrue(hasApp);
		
		// Load current applications
		List<Application> currentApps = applicationDao.loadCurrentApplicationsByApplicant(applicant);
		Assert.assertEquals(1, currentApps.size());
		Assert.assertTrue(currentApps.contains(app));
	}
	
	@Test
	public void findApplicationsLightWithoutDecision() {
		//create 2 applications, one with a decision, the other without
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application withDecisionApp = applicationDao.createApplication(pos);
		withDecisionApp.getPerson().setFirstName("John");
		withDecisionApp.getPerson().setLastName("Decision B");
		withDecisionApp = applicationDao.saveTempApplication(withDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withDecisionApp, 2);
		
		Application withoutDecisionApp = applicationDao.createApplication(pos);
		withoutDecisionApp.getPerson().setFirstName("John");
		withoutDecisionApp.getPerson().setLastName("No decision");
		withoutDecisionApp = applicationDao.saveTempApplication(withoutDecisionApp, true);
		dbInstance.commitAndCloseSession();
		
		//load applications without decision
		List<ApplicationLight> apps = applicationDao.findApplicationsLightWithoutDecision(pos);
		Assert.assertNotNull(apps);
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(withoutDecisionApp.getKey(), apps.get(0).getKey());
	}
	
	@Test
	public void findApplicationsLight_withCDecision() {
		//create 3 applications, one with a B decision, one with a C decision and the last without
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application withDecisionApp = applicationDao.createApplication(pos);
		withDecisionApp.getPerson().setFirstName("John");
		withDecisionApp.getPerson().setLastName("Decision B");
		withDecisionApp = applicationDao.saveTempApplication(withDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withDecisionApp, 2);
		
		Application withCDecisionApp = applicationDao.createApplication(pos);
		withCDecisionApp.getPerson().setFirstName("John");
		withCDecisionApp.getPerson().setLastName("Decision C");
		withCDecisionApp = applicationDao.saveTempApplication(withCDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withCDecisionApp, 1);
		
		Application withoutDecisionApp = applicationDao.createApplication(pos);
		withoutDecisionApp.getPerson().setFirstName("John");
		withoutDecisionApp.getPerson().setLastName("No decision");
		withoutDecisionApp = applicationDao.saveTempApplication(withoutDecisionApp, true);
		dbInstance.commitAndCloseSession();
		
		//load applications without decision
		List<Integer> cDecisions = new ArrayList<>();
		cDecisions.add(1);
		List<ApplicationLight> apps = applicationDao.findApplicationsLightWithDecisions(pos, cDecisions, null, false, null, Collections.emptyList(), false);
		Assert.assertNotNull(apps);
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(withCDecisionApp.getKey(), apps.get(0).getKey());
	}
	
	@Test
	public void findApplicationsLight_withNoDecision() {
		//create 3 applications, one with a B decision, one with a C decision and the last without
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application withDecisionApp = applicationDao.createApplication(pos);
		withDecisionApp.getPerson().setFirstName("John");
		withDecisionApp.getPerson().setLastName("Decision B");
		withDecisionApp = applicationDao.saveTempApplication(withDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withDecisionApp, 2);
		
		Application withCDecisionApp = applicationDao.createApplication(pos);
		withCDecisionApp.getPerson().setFirstName("John");
		withCDecisionApp.getPerson().setLastName("Decision C");
		withCDecisionApp = applicationDao.saveTempApplication(withCDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withCDecisionApp, 1);
		
		Application withoutDecisionApp = applicationDao.createApplication(pos);
		withoutDecisionApp.getPerson().setFirstName("John");
		withoutDecisionApp.getPerson().setLastName("No decision");
		withoutDecisionApp = applicationDao.saveTempApplication(withoutDecisionApp, true);
		dbInstance.commitAndCloseSession();
		
		//load applications without decision
		List<ApplicationLight> apps = applicationDao.findApplicationsLightWithDecisions(pos,
				Collections.emptyList(), null, true, null, Collections.emptyList(), false);
		Assert.assertNotNull(apps);
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(withoutDecisionApp.getKey(), apps.get(0).getKey());
	}
	
	@Test
	public void findApplicationsLight_mixDecision() {
		//create 3 applications, one with a B decision, one with a C decision and the last without
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application withDecisionApp = applicationDao.createApplication(pos);
		withDecisionApp.getPerson().setFirstName("John");
		withDecisionApp.getPerson().setLastName("Decision B");
		withDecisionApp = applicationDao.saveTempApplication(withDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withDecisionApp, 2);
		
		Application withCDecisionApp = applicationDao.createApplication(pos);
		withCDecisionApp.getPerson().setFirstName("John");
		withCDecisionApp.getPerson().setLastName("Decision C");
		withCDecisionApp = applicationDao.saveTempApplication(withCDecisionApp, true);
		dbInstance.commitAndCloseSession();
		applicationDao.setDecision(withCDecisionApp, 1);
		
		Application withoutDecisionApp = applicationDao.createApplication(pos);
		withoutDecisionApp.getPerson().setFirstName("John");
		withoutDecisionApp.getPerson().setLastName("No decision");
		withoutDecisionApp = applicationDao.saveTempApplication(withoutDecisionApp, true);
		dbInstance.commitAndCloseSession();
		
		//load applications without decision
		List<ApplicationLight> apps = applicationDao.findApplicationsLightWithDecisions(pos,
				Collections.singletonList(1), Collections.emptyList(), true, Collections.emptyList(), Collections.emptyList(), false);
		assertThat(apps)
			.isNotNull()
			.hasSize(2)
			.map(ApplicationLight::getKey)
			.contains(withoutDecisionApp.getKey(), withCDecisionApp.getKey());
	}
	
	@Test
	public void setAttachmentDatas() throws IOException {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		app.getPerson().setFirstName("John");
		app.getPerson().setLastName("Uploader");
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		//upload covering letter
		Attachment attachment = DocumentEnum.coveringLetter.path(app);
		URL url = ApplicationDAOTest.class.getResource("Covering_letter_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		Attachment savedAttachment = applicationDao.setAttachmentDatas(attachment, "Covering letter.pdf", "pdf", bytes);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(savedAttachment);
		Assert.assertNotNull(savedAttachment.getSize());
		Assert.assertEquals("Covering letter.pdf", savedAttachment.getName());
		Assert.assertEquals(bytes.length, savedAttachment.getSize().intValue());
	}
	
	@Test
	public void getAttachmentDatas_byAttachment() throws IOException {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		app.getPerson().setFirstName("John");
		app.getPerson().setLastName("Bytes downloader");
		
		//upload covering letter
		Attachment attachment = DocumentEnum.coveringLetter.path(app);
		URL url = ApplicationDAOTest.class.getResource("Covering_letter_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		Attachment savedAttachment = applicationDao.setAttachmentDatas(attachment, "Bytes covering stuff.pdf", "pdf", bytes);
		DocumentEnum.coveringLetter.setPath(app, savedAttachment);
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		//reload by saved attachment
		byte[] datas = applicationDao.getAttachmentDatas(savedAttachment);
		Assert.assertArrayEquals(bytes, datas);
		
		//reload application -> than attachment
		Application reloadedApp = applicationDao.loadApplicationByKey(app.getKey());
		Attachment reloadedAttachment = DocumentEnum.coveringLetter.path(reloadedApp);
		byte[] reloadDatas = applicationDao.getAttachmentDatas(reloadedAttachment);
		Assert.assertArrayEquals(bytes, reloadDatas);
	}
	
	@Test
	public void getAttachmentDatas_byAttachmentKey() throws IOException {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		app.getPerson().setFirstName("John");
		app.getPerson().setLastName("Bytes downloader");
		
		//upload covering letter
		Attachment attachment = DocumentEnum.curriculumVitae.path(app);
		URL url = ApplicationDAOTest.class.getResource("Curriculum_vitae_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		Attachment savedAttachment = applicationDao.setAttachmentDatas(attachment, "Bytes CV.pdf", "pdf", bytes);
		DocumentEnum.curriculumVitae.setPath(app, savedAttachment);
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		//reload attachment by key
		byte[] datas = applicationDao.getAttachmentDatas(savedAttachment.getKey());
		Assert.assertArrayEquals(bytes, datas);
	}
	
	@Test
	public void setAndGetAttachmentDatas_allTypeOfDocuments() throws IOException {
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application app = applicationDao.createApplication(pos);
		app.getPerson().setFirstName("John");
		app.getPerson().setLastName("Uploader");
		app = applicationDao.saveTempApplication(app, true);
		
		URL url = ApplicationDAOTest.class.getResource("Covering_letter_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		
		//upload
		for(DocumentEnum doc:DocumentEnum.values()) {
			Attachment attachment = doc.path(app);
			Attachment savedAttachment = applicationDao.setAttachmentDatas(attachment, doc.name() + ".pdf", "pdf", bytes);
			doc.setPath(app, savedAttachment);
			Assert.assertNotNull(savedAttachment);
		}

		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		//download
		Application reloadedApp = applicationDao.loadApplicationByKey(app.getKey());
		for(DocumentEnum doc:DocumentEnum.values()) {
			Attachment savedAttachment = doc.path(reloadedApp);
			Assert.assertNotNull("Check attachment of doc:" + doc.name(), savedAttachment);
			byte[] datas = applicationDao.getAttachmentDatas(savedAttachment.getKey());
			Assert.assertNotNull(savedAttachment.getSize());
			Assert.assertArrayEquals(bytes, datas);
			Assert.assertEquals(doc.name() + ".pdf", savedAttachment.getName());
			Assert.assertEquals(bytes.length, savedAttachment.getSize().intValue());
		}
	}
	
	@Test
	public void deleteApplication() {
		Position pos = createRandomPosition(PositionStatus.closed);
		Application toBeDeleteApp = applicationDao.createApplication(pos);
		toBeDeleteApp.getPerson().setFirstName("John");
		toBeDeleteApp.getPerson().setLastName("To be delete");
		toBeDeleteApp = applicationDao.saveTempApplication(toBeDeleteApp, true);
		
		Application referenceApp = applicationDao.createApplication(pos);
		referenceApp.getPerson().setFirstName("Jane");
		referenceApp.getPerson().setLastName("To be delete");
		referenceApp = applicationDao.saveTempApplication(referenceApp, true);
		dbInstance.commitAndCloseSession();
		
		//delete the app
		applicationDao.deleteApplication(toBeDeleteApp);
		dbInstance.commit();
		
		//by key
		Application deletedApp = applicationDao.loadApplicationByKey(toBeDeleteApp.getKey());
		Assert.assertNull(deletedApp);
		//not deleted by key
		Application referenceReloadedApp = applicationDao.loadApplicationByKey(referenceApp.getKey());
		Assert.assertNotNull(referenceReloadedApp);
		
		//search by position
		List<Application> currentApps = applicationDao.findApplications(pos, true);
		Assert.assertFalse(currentApps.contains(toBeDeleteApp));
		Assert.assertTrue(currentApps.contains(referenceReloadedApp));
		//check there isn't any invalid app
		List<Application> notValidApps = applicationDao.findApplications(pos, false);
		Assert.assertTrue(notValidApps.isEmpty());
		
		//be paranoid
		List<Application> allValidApps = applicationDao.findApplications(null, true);
		Assert.assertFalse(allValidApps.contains(toBeDeleteApp));
		Assert.assertTrue(allValidApps.contains(referenceReloadedApp));
		//check there isn't any invalid app
		List<Application> allTempApps = applicationDao.findApplications(null, false);
		Assert.assertFalse(allTempApps.contains(toBeDeleteApp));	
	}
	
	@Test
	public void deleteApplication_withDocuments() throws IOException {
		Position pos = createRandomPosition(PositionStatus.closed);
		Application toBeDeleteApp = applicationDao.createApplication(pos);
		toBeDeleteApp.getPerson().setFirstName("John");
		toBeDeleteApp.getPerson().setLastName("To be delete");
		
		//upload
		URL url = ApplicationDAOTest.class.getResource("Covering_letter_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		for(DocumentEnum doc:DocumentEnum.values()) {
			Attachment attachment = doc.path(toBeDeleteApp);
			Attachment savedAttachment = applicationDao.setAttachmentDatas(attachment, doc.name() + ".pdf", "pdf", bytes);
			doc.setPath(toBeDeleteApp, savedAttachment);
			Assert.assertNotNull(savedAttachment);
		}
		toBeDeleteApp = applicationDao.saveTempApplication(toBeDeleteApp, true);
		dbInstance.commitAndCloseSession();
		
		//check the documents
		List<Attachment> attachments = new ArrayList<>();
		Application reloadedApp = applicationDao.loadApplicationByKey(toBeDeleteApp.getKey());
		for(DocumentEnum doc:DocumentEnum.values()) {
			Attachment savedAttachment = doc.path(reloadedApp);
			Assert.assertNotNull("Check attachment of doc:" + doc.name(), savedAttachment);
			byte[] datas = applicationDao.getAttachmentDatas(savedAttachment.getKey());
			Assert.assertNotNull(savedAttachment.getSize());
			Assert.assertArrayEquals(bytes, datas);
			Assert.assertEquals(doc.name() + ".pdf", savedAttachment.getName());
			Assert.assertEquals(bytes.length, savedAttachment.getSize().intValue());
			attachments.add(savedAttachment);
		}
		
		//delete the app
		applicationDao.deleteApplication(toBeDeleteApp);
		dbInstance.commit();
		
		//check that the application is deleted
		Application deletedApp = applicationDao.loadApplicationByKey(toBeDeleteApp.getKey());
		Assert.assertNull(deletedApp);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select attachment from ").append(AttachmentImpl.class.getName()).append(" attachment where attachment.key=:attachmentKey");
		
		//check that the documents are deleted
		for(Attachment deletedAttachment:attachments) {
			List<AttachmentImpl> impls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AttachmentImpl.class)
				.setParameter("attachmentKey", deletedAttachment.getKey())
				.getResultList();
			Assert.assertTrue(impls.isEmpty());
		}
		Assert.assertEquals(DocumentEnum.values().length, attachments.size());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-234");
		position.setPositionTitle("Technician in robotic");
		position.setShortTitle("Pilot of robot");
		position.setDepartment("NERV");
		position.setHomepage("http://www.nerv.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young pilot for our semi-living robot.");
		return positionDao.savePosition(position);
	}
}
