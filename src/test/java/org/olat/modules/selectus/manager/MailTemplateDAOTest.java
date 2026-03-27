/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.mail.PositionMailTemplateImpl;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailTemplateDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private MailTemplateDAO mailTemplateDao;
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
	public void createTemplate() {
		Position position = createRandomPosition(PositionStatus.published);
		PositionMailTemplate template = mailTemplateDao.createTemplate(position, "template-id", "Template Name");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(template);
		Assert.assertNotNull(template.getKey());
		Assert.assertNotNull(template.getCreationDate());
		Assert.assertNotNull(template.getLastModified());
		Assert.assertEquals(position, ((PositionMailTemplateImpl)template).getPosition());
	}
	
	@Test
	public void getTemplates() {
		Position position = createRandomPosition(PositionStatus.published);
		PositionMailTemplate template = mailTemplateDao.createTemplate(position, null, "Custom template");
		dbInstance.commitAndCloseSession();
		
		List<PositionMailTemplate> templates = mailTemplateDao.getTemplates(position);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(templates);
		Assert.assertEquals(1, templates.size());
		Assert.assertTrue(templates.contains(template));
	}
	
	@Test
	public void getTemplate() {
		Position position = createRandomPosition(PositionStatus.published);
		PositionMailTemplate template = mailTemplateDao.createTemplate(position, null, "Custom template 2");
		dbInstance.commitAndCloseSession();
		
		PositionMailTemplate reloadedTemplate = mailTemplateDao.getTemplate(template);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reloadedTemplate);
		Assert.assertEquals(template, reloadedTemplate);
		Assert.assertEquals(position, ((PositionMailTemplateImpl)reloadedTemplate).getPosition());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-234");
		position.setPositionTitle("Automated mail service");
		position.setShortTitle("Postman");
		position.setDepartment("POST");
		position.setHomepage("https://www.post.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young postman");
		return positionDao.savePosition(position);
	}

}
