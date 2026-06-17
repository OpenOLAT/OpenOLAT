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
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectusCategoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SelectusCategoryDAO categoryDao;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private RecruitingService recruitingService;
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
	public void createCategory() {
		String categoryName = UUID.randomUUID().toString();
		Category category = categoryDao.createCategory(categoryName, null, null);
		dbInstance.commit();
		
		Assert.assertNotNull(category);
		Assert.assertNotNull(category.getKey());
		Assert.assertNotNull(category.getCreationDate());
		Assert.assertEquals(categoryName, category.getName());
	}
	
	@Test
	public void createCategoryWithPosition() {
		Position position = createRandomPosition(PositionStatus.preparation);
		
		String categoryName = UUID.randomUUID().toString();
		Category category = categoryDao.createCategory(categoryName, "#0f0f0f", position);
		dbInstance.commit();
		
		Assert.assertNotNull(category);
		Assert.assertNotNull(category.getKey());
		Assert.assertNotNull(category.getCreationDate());
		Assert.assertEquals(categoryName, category.getName());
		Assert.assertEquals("#0f0f0f", category.getColor());
		Assert.assertEquals(position, category.getPosition());
	}
	
	@Test
	public void updateCategory() {
		String categoryName = UUID.randomUUID().toString();
		Category category = categoryDao.createCategory(categoryName, "violet", null);
		dbInstance.commit();

		String newName = UUID.randomUUID().toString();
		category.setName(newName);
		category.setColor("#000000");
		category = categoryDao.updateCategory(category);
		dbInstance.commitAndCloseSession();
		
		Category reloadCategory = categoryDao.loadCategory(category.getKey());
		Assert.assertNotNull(reloadCategory);
		Assert.assertEquals(category, reloadCategory);
		Assert.assertEquals(newName, category.getName());
		Assert.assertEquals("#000000", category.getColor());
	}
	
	@Test
	public void getSystemCategories() {
		String categoryName = UUID.randomUUID().toString();
		Category category = categoryDao.createCategory(categoryName, null, null);
		dbInstance.commit();
		
		List<Category> allCategories = categoryDao.getSystemCategories();
		Assert.assertNotNull(allCategories);
		Assert.assertTrue(allCategories.contains(category));
	}
	
	@Test
	public void getPositionCategories() {
		Position position = createRandomPosition(PositionStatus.preparation);
		
		String categoryName1 = UUID.randomUUID().toString();
		Category category1 = categoryDao.createCategory(categoryName1, null, position);
		String categoryName2 = UUID.randomUUID().toString();
		Category category2 = categoryDao.createCategory(categoryName2, null, position);
		dbInstance.commit();
		
		List<Category> categories = categoryDao.getPositionCategories(position);
		Assert.assertNotNull(categories);
		Assert.assertEquals(2, categories.size());
		Assert.assertTrue(categories.contains(category1));
		Assert.assertTrue(categories.contains(category2));
	}
	
	@Test
	public void getCategoriesByName() {
		String categoryName = UUID.randomUUID().toString();
		Category category = categoryDao.createCategory(categoryName, null, null);
		dbInstance.commit();
		
		List<Category> loadedCategories = categoryDao.getCategoriesByName(categoryName, null, true, true);
		Assert.assertNotNull(loadedCategories);
		Assert.assertEquals(1, loadedCategories.size());
		Assert.assertTrue(loadedCategories.contains(category));
		// 
		List<Category> loadedFalseCategory = categoryDao.getCategoriesByName("Groou", null, true, true);
		Assert.assertNotNull(loadedFalseCategory);
		Assert.assertTrue(loadedFalseCategory.isEmpty());
	}
	
	@Test
	public void getCategoriesByNameWithPosition() {
		Position position = createRandomPosition(PositionStatus.closed);
		String categoryName = UUID.randomUUID().toString();
		Category systemCategory = categoryDao.createCategory(categoryName, null, null);
		Category positionCategory = categoryDao.createCategory(categoryName, null, position);
		dbInstance.commit();
		
		List<Category> loadedCategories = categoryDao.getCategoriesByName(categoryName, position, true, true);
		Assert.assertNotNull(loadedCategories);
		Assert.assertEquals(2, loadedCategories.size());
		Assert.assertTrue(loadedCategories.contains(systemCategory));
		Assert.assertTrue(loadedCategories.contains(positionCategory));
		// 
		List<Category> loadedFalseCategory = categoryDao.getCategoriesByName("Groou", position, true, true);
		Assert.assertNotNull(loadedFalseCategory);
		Assert.assertTrue(loadedFalseCategory.isEmpty());
	}
	
	@Test
	public void deletePositionWithCategory() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Position positionToDelete = createRandomPosition(PositionStatus.closed);
		String categoryName = UUID.randomUUID().toString();
		Category systemCategory = categoryDao.createCategory(categoryName, null, null);
		Category positionCategory = categoryDao.createCategory(categoryName, null, positionToDelete);
		dbInstance.commit();
		
		// check
		List<Category> loadedCategories = categoryDao.getCategoriesByName(categoryName, positionToDelete, true, true);
		Assert.assertNotNull(loadedCategories);
		Assert.assertEquals(2, loadedCategories.size());
		Assert.assertTrue(loadedCategories.contains(systemCategory));
		Assert.assertTrue(loadedCategories.contains(positionCategory));
		dbInstance.commitAndCloseSession();
		
		positionToDelete = recruitingService.getPosition(positionToDelete.getKey());
		recruitingService.deletePosition(positionToDelete, actor);
		dbInstance.commitAndCloseSession();
		
		Category reloadedSystemCategory = categoryDao.loadCategory(systemCategory.getKey());
		Assert.assertNotNull(reloadedSystemCategory);
		Assert.assertEquals(systemCategory, reloadedSystemCategory);
		
		Category reloadedPositionCategory = categoryDao.loadCategory(positionCategory.getKey());
		Assert.assertNull(reloadedPositionCategory);
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Cat. comment");
		position.setPositionTitle("Comments");
		position.setShortTitle("Pilot of comments");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a expert in categorisation of rare events.");
		return positionDao.savePosition(position);
	}
}
