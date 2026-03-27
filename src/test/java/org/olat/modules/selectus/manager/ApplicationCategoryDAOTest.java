/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationCategory;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCategoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SelectusCategoryDAO categoryDao;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ApplicationCategoryDAO applicationCategoryDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createApplicationCategory() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		Category category = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();
		
		// make the test
		ApplicationCategory appCat = applicationCategoryDao.createApplicationCategory(app, category, false);
		dbInstance.commit();
		Assert.assertNotNull(appCat);
		Assert.assertNotNull(appCat.getKey());
		Assert.assertNotNull(appCat.getCreationDate());
		Assert.assertEquals(app, appCat.getApplication());
		Assert.assertEquals(category, appCat.getCategory());
	}
	
	@Test
	public void getCategories_application() {
		// create an application with some tags
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		Category category1 = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		Category category2 = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();
		// link the tags
		applicationCategoryDao.createApplicationCategory(app, category1, false);
		applicationCategoryDao.createApplicationCategory(app, category2, true);
		dbInstance.commit();
		
		// test the method
		List<Category> categories = applicationCategoryDao.getCategories(app, true, true);
		Assert.assertNotNull(categories);
		Assert.assertEquals(2, categories.size());
		Assert.assertTrue(categories.contains(category1));
		Assert.assertTrue(categories.contains(category2));
	
		List<Category> systemCategories = applicationCategoryDao.getCategories(app, true, false);
		Assert.assertNotNull(systemCategories);
		List<Category> positionCategories = applicationCategoryDao.getCategories(app, false, true);
		Assert.assertNotNull(positionCategories);
	}
	
	@Test
	public void countApplications() {
		// create an application with some tags
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = createRandomApplication(position);
		Application app2 = createRandomApplication(position);
		
		Category category = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		Category categoryAlt = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();
		// link the tags
		applicationCategoryDao.createApplicationCategory(app1, category, false);
		applicationCategoryDao.createApplicationCategory(app2, category, false);
		dbInstance.commit();
		
		// test the method
		int usage = applicationCategoryDao.countApplications(category);
		Assert.assertEquals(2, usage);
		int usageAlt = applicationCategoryDao.countApplications(categoryAlt);
		Assert.assertEquals(0, usageAlt);
	}
	
	@Test
	public void getCategories_position() {
		// create an application with some tags
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = createRandomApplication(position);
		Application app2 = createRandomApplication(position);
		Category category1 = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		Category category2 = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();
		// link the tags
		applicationCategoryDao.createApplicationCategory(app1, category1, false);
		applicationCategoryDao.createApplicationCategory(app1, category2, false);
		applicationCategoryDao.createApplicationCategory(app2, category2, false);
		dbInstance.commit();
		
		// test the method
		List<Category> categories = applicationCategoryDao.getCategories(position);
		Assert.assertNotNull(categories);
		Assert.assertEquals(2, categories.size());
		Assert.assertTrue(categories.contains(category1));
		Assert.assertTrue(categories.contains(category2));
	}
	
	@Test
	public void getApplicationCategories_application() {
		// create an application with some tags
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		Category category = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();
		// link the tags
		ApplicationCategory appCat = applicationCategoryDao.createApplicationCategory(app, category, true);
		dbInstance.commit();
		
		// test the method
		List<ApplicationCategory> appCategories = applicationCategoryDao.getApplicationCategories(app, true, true);
		Assert.assertNotNull(appCategories);
		Assert.assertEquals(1, appCategories.size());
		ApplicationCategory loadedAppCat = appCategories.get(0);
		Assert.assertEquals(appCat, loadedAppCat);
		Assert.assertEquals(category, loadedAppCat.getCategory());
	}
	
	@Test
	public void getApplicationCategories_position() {
		// create an application with some tags
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		Category category = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();
		// link the tags
		ApplicationCategory appCat = applicationCategoryDao.createApplicationCategory(app, category, false);
		dbInstance.commit();
		
		// test the method
		List<ApplicationCategoryInfos> appCategories = applicationCategoryDao.getApplicationCategoriesInfos(position, null, true, true, true);
		Assert.assertNotNull(appCategories);
		Assert.assertEquals(1, appCategories.size());
		ApplicationCategoryInfos loadedAppCat = appCategories.get(0);
		Assert.assertEquals(category, loadedAppCat.getCategory());
		Assert.assertEquals(app.getKey(), loadedAppCat.getApplicationKey());
		Assert.assertEquals(appCat.getApplication().getKey(), loadedAppCat.getApplicationKey());
		
		List<ApplicationCategoryInfos> positionAppCategories = applicationCategoryDao.getApplicationCategoriesInfos(position, null, false, true, false);
		Assert.assertNotNull(positionAppCategories);
		
		List<ApplicationCategoryInfos> systemAppCategories = applicationCategoryDao.getApplicationCategoriesInfos(position, null, true, false, true);
		Assert.assertNotNull(systemAppCategories);
	}

	@Test
	public void replaceCategory() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = createRandomApplication(position);
		Application app2 = createRandomApplication(position);
		
		Category originalCategory = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		Category replacementCategory = categoryDao.createCategory(UUID.randomUUID().toString(), null, null);
		dbInstance.commit();
		// link the tags
		ApplicationCategory appCat1 = applicationCategoryDao.createApplicationCategory(app1, originalCategory, false);
		ApplicationCategory appCat2 = applicationCategoryDao.createApplicationCategory(app2, originalCategory, false);
		dbInstance.commitAndCloseSession();

		applicationCategoryDao.replaceCategory(originalCategory, replacementCategory);
		dbInstance.commitAndCloseSession();
		
		// app 1
		List<ApplicationCategory> app1Categories = applicationCategoryDao.getApplicationCategories(app1, true, true);
		Assert.assertNotNull(app1Categories);
		Assert.assertEquals(1, app1Categories.size());
		Assert.assertEquals(appCat1, app1Categories.get(0));
		Assert.assertEquals(replacementCategory, app1Categories.get(0).getCategory());
		// app 2
		List<ApplicationCategory> app2Categories = applicationCategoryDao.getApplicationCategories(app2, true, true);
		Assert.assertNotNull(app2Categories);
		Assert.assertEquals(1, app2Categories.size());
		Assert.assertEquals(appCat2, app2Categories.get(0));
		Assert.assertEquals(replacementCategory, app2Categories.get(0).getCategory());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Lonely comment");
		position.setPositionTitle("Comments");
		position.setShortTitle("Pilot of comments");
		position.setDepartment("COM");
		position.setHomepage("http://www.comments.ch");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a expert in making of comments.");
		return positionDao.savePosition(position);
	}
	
	private Application createRandomApplication(Position pos) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Ryomou " + UUID.randomUUID());
		person.setLastName("Shimei");
		person.setNationality("JP");
		person.setMail("kanu@ikki.co.jp");
		person.setPhone("9435898");
		person.setBirthday(new Date());
		return applicationDao.saveTempApplication(app, true);
	}

}
