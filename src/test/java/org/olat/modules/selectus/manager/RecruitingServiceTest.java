/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


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
	private RecruitingService selectusService;
	@Autowired
	private OrganisationService organisationService;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-selectus-service-unit-test", "Org-selectus-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void addComparativeReference() {
		Position position = selectusService.createPosition(defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-256");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Some research");
		position.setDepartment("Unkown departement");
		position.setHomepage("https://www.404.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department of ...");
		position = selectusService.savePosition(position);
		dbInstance.commit();
		
		Application app1 = selectusService.createTempApplication(position, false);
		app1 = selectusService.saveApplication(app1);
		
		Application app2 = selectusService.createTempApplication(position, false);
		app2 = selectusService.saveApplication(app2);
		dbInstance.commit();
		
		List<Application> apps = new ArrayList<>();
		apps.add(app1);
		apps.add(app2);
		Reference reference = selectusService.addReference("Dr.", "Frank", "Loyd", null, "loyd@frentix.com", null,
				ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.notAnswered, null, null, apps);
		dbInstance.commit();
		
		Assert.assertNotNull(reference);
	}
	
	@Test
	public void deletePosition() throws IOException {
		Identity actor = JunitTestHelper.getDefaultActor();
		Position position = selectusService.createPosition(defaultUnitTestOrganisation);
		position.setPlaningsNumber("TR-808");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Short title");
		position.setDepartment("Gotham institute for psychology");
		position.setHomepage("http://www.asylum.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department of psychology ...");
		position = selectusService.savePosition(position);
		dbInstance.commit();
		
		Application app = selectusService.createTempApplication(position, false);
		app = selectusService.saveApplication(app);
		dbInstance.commit();
		
		//upload covering letter
		Attachment attachment = DocumentEnum.curriculumVitae.path(app);
		URL url = ApplicationDAOTest.class.getResource("Curriculum_vitae_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		Attachment savedAttachment = selectusService.setAttachmentDatas(app, attachment, bytes, "Bytes CV.pdf", DocumentType.pdf);
		DocumentEnum.curriculumVitae.setPath(app, savedAttachment);
		app = selectusService.saveApplication(app);
		dbInstance.commitAndCloseSession();
	
		Reference ref = selectusService.addReference("", "Otto", "Stern", "ETH", "stern@frentix.com", null,
				ReferenceType.expert, ReferenceRequestStatus.notAnswered, null, app, null);
		dbInstance.commit();
		Assert.assertNotNull(ref);
		
		//upload letter
		URL letterUrl = ApplicationDAOTest.class.getResource("Letter_of_recommendation.pdf");
		byte[] letterBytes = IOUtils.toByteArray(letterUrl);
		Attachment savedLetterAttachment = selectusService.setAttachmentDatas(position, ref, attachment, "Letter", DocumentType.pdf, letterBytes);
		ref.setLetter(savedLetterAttachment);
		app = selectusService.saveApplication(app);
		dbInstance.commitAndCloseSession();

		selectusService.deletePosition(position, actor);
		dbInstance.commit();
	}
}
