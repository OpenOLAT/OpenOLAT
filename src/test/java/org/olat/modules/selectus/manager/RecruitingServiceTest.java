/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceType;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	
	@Test
	public void testManagers() {
		assertNotNull(dbInstance);
		assertNotNull(recruitingFrontendManager);
	}
	
	@Test
	public void addComparativeReference() {
		Position position = recruitingFrontendManager.createPosition();
		position.setPlaningsNumber("AC-256");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Some research");
		position.setDepartment("Unkown departement");
		position.setHomepage("https://www.404.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department of ...");
		position = recruitingFrontendManager.savePosition(position);
		dbInstance.commit();
		
		Application app1 = recruitingFrontendManager.createTempApplication(position, false);
		app1 = recruitingFrontendManager.saveApplication(app1);
		
		Application app2 = recruitingFrontendManager.createTempApplication(position, false);
		app2 = recruitingFrontendManager.saveApplication(app2);
		dbInstance.commit();
		
		List<Application> apps = new ArrayList<>();
		apps.add(app1);
		apps.add(app2);
		Reference reference = recruitingFrontendManager.addReference("Dr.", "Frank", "Loyd", null, "loyd@frentix.com", null,
				ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.notAnswered, null, null, apps);
		dbInstance.commit();
		
		Assert.assertNotNull(reference);
		
		
	}
	
	@Test
	public void deletePosition() throws IOException {
		Position position = recruitingFrontendManager.createPosition();
		position.setPlaningsNumber("TR-808");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Short title");
		position.setDepartment("Gotham institute for psychology");
		position.setHomepage("http://www.asylum.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department of psychology ...");
		position = recruitingFrontendManager.savePosition(position);
		dbInstance.commit();
		
		Application app = recruitingFrontendManager.createTempApplication(position, false);
		app = recruitingFrontendManager.saveApplication(app);
		dbInstance.commit();
		
		//upload covering letter
		Attachment attachment = DocumentEnum.curriculumVitae.path(app);
		URL url = ApplicationDAOTest.class.getResource("Curriculum_vitae_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		Attachment savedAttachment = recruitingFrontendManager.setAttachmentDatas(app, attachment, bytes, "Bytes CV.pdf", DocumentType.pdf);
		DocumentEnum.curriculumVitae.setPath(app, savedAttachment);
		app = recruitingFrontendManager.saveApplication(app);
		dbInstance.commitAndCloseSession();
	
		Reference ref = recruitingFrontendManager.addReference("", "Otto", "Stern", "ETH", "stern@frentix.com", null,
				ReferenceType.expert, ReferenceRequestStatus.notAnswered, null, app, null);
		dbInstance.commit();
		Assert.assertNotNull(ref);
		
		//upload letter
		URL letterUrl = ApplicationDAOTest.class.getResource("Letter_of_recommendation.pdf");
		byte[] letterBytes = IOUtils.toByteArray(letterUrl);
		Attachment savedLetterAttachment = recruitingFrontendManager.setAttachmentDatas(position, ref, attachment, "Letter", DocumentType.pdf, letterBytes);
		ref.setLetter(savedLetterAttachment);
		app = recruitingFrontendManager.saveApplication(app);
		dbInstance.commitAndCloseSession();

		recruitingFrontendManager.deletePosition(position);
		dbInstance.commit();
	}
	
	/**
	 * Organisation unit author / staff can see only their organisation units
	 */
	@Test
	public void deleteOrganisationUnit() {
		OrganisationUnit unit = recruitingFrontendManager.createOrganisationUnit();
		unit.setName("Unit permission");
		unit = recruitingFrontendManager.saveOrganisationUnit(unit);
		
		Position position = recruitingFrontendManager.createPosition();
		position.setPlaningsNumber("TR-809");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Short title");
		position.setOrganisationUnit(unit);
		position = recruitingFrontendManager.savePosition(position);
		dbInstance.commitAndCloseSession();

		//delete the unit
		recruitingFrontendManager.deleteOrganisationUnit(unit);
		dbInstance.commit();
		
		Position reloadedPosition = recruitingFrontendManager.getPosition(position.getKey());
		Assert.assertNotNull(reloadedPosition);
		Assert.assertNull(reloadedPosition.getOrganisationUnit());
		
		OrganisationUnit deletedUnit = recruitingFrontendManager.getOrganisationUnit(unit.getKey());
		Assert.assertNull(deletedUnit);
	}
	
	/**
	 * Organisation unit author / staff can see only their organisation units
	 */
	@Test
	public void deleteOrganisationUnit_paranoid() {
		// make a first position, staff and unit
		Identity staffOrg1 = JunitTestHelper.createAndPersistIdentityAsRndUser("staff-org-1");
		OrganisationUnit unit1 = recruitingFrontendManager.createOrganisationUnit();
		unit1.setName("Unit permission");
		unit1 = recruitingFrontendManager.saveOrganisationUnit(unit1);
		recruitingFrontendManager.addOrganisationUnitMembership(staffOrg1, unit1);
		
		Position position1 = recruitingFrontendManager.createPosition();
		position1.setPlaningsNumber("TR-810");
		position1.setPositionTitle("Prof.");
		position1.setShortTitle("Short title");
		position1.setOrganisationUnit(unit1);
		position1 = recruitingFrontendManager.savePosition(position1);
		dbInstance.commitAndCloseSession();
		
		// make a second position, staff and unit
		Identity staffOrg2 = JunitTestHelper.createAndPersistIdentityAsRndUser("staff-org-2");
		OrganisationUnit unit2 = recruitingFrontendManager.createOrganisationUnit();
		unit2.setName("Unit permission");
		unit2 = recruitingFrontendManager.saveOrganisationUnit(unit2);
		recruitingFrontendManager.addOrganisationUnitMembership(staffOrg2, unit2);
		
		Position position2 = recruitingFrontendManager.createPosition();
		position2.setPlaningsNumber("TR-811");
		position2.setPositionTitle("Prof.");
		position2.setShortTitle("Short title");
		position2.setOrganisationUnit(unit2);
		position2 = recruitingFrontendManager.savePosition(position2);
		dbInstance.commitAndCloseSession();
				
		//delete the unit one
		recruitingFrontendManager.deleteOrganisationUnit(unit1);
		dbInstance.commit();
		
		//check unit 1
		Position reloadedPosition1 = recruitingFrontendManager.getPosition(position1.getKey());
		Assert.assertNotNull(reloadedPosition1);
		Assert.assertNull(reloadedPosition1.getOrganisationUnit());

		boolean member_1_1 = recruitingFrontendManager.isMemberOfOrganisationUnit(staffOrg1, unit1);
		Assert.assertFalse(member_1_1);
		boolean member_2_1 = recruitingFrontendManager.isMemberOfOrganisationUnit(staffOrg2, unit1);
		Assert.assertFalse(member_2_1);
		
		OrganisationUnit deletedUnit1 = recruitingFrontendManager.getOrganisationUnit(unit1.getKey());
		Assert.assertNull(deletedUnit1);
		
		// check unit 2
		Position reloadedPosition2 = recruitingFrontendManager.getPosition(position2.getKey());
		Assert.assertNotNull(reloadedPosition2);
		Assert.assertEquals(unit2, reloadedPosition2.getOrganisationUnit());
		
		boolean member_1_2 = recruitingFrontendManager.isMemberOfOrganisationUnit(staffOrg1, unit2);
		Assert.assertFalse(member_1_2);
		boolean member_2_2 = recruitingFrontendManager.isMemberOfOrganisationUnit(staffOrg1, unit2);
		Assert.assertFalse(member_2_2);
		
		OrganisationUnit reloadedUnit2 = recruitingFrontendManager.getOrganisationUnit(unit2.getKey());
		Assert.assertNotNull(reloadedUnit2);
		Assert.assertEquals(unit2,  reloadedUnit2);
	}
}
