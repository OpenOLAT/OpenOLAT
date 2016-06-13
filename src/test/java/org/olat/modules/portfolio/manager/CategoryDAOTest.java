package org.olat.modules.portfolio.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Category;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CategoryDAO categoryDao;
	
	@Test
	public void createCategory() {
		Category category = categoryDao.createAndPersistCategory("Swag");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(category);
		Assert.assertNotNull(category.getKey());
		Assert.assertNotNull(category.getCreationDate());
		Assert.assertEquals("Swag", category.getName());
	}
	
	@Test
	public void createRelationToCategory() {
		Category category = categoryDao.createAndPersistCategory("Cool");
		dbInstance.commitAndCloseSession();
		
		// create relation
		String rndType = UUID.randomUUID().toString();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(rndType, 234l);
		categoryDao.appendRelation(ores, category);
		dbInstance.commitAndCloseSession();
		
		//load relation
		List<Category> categories = categoryDao.getCategories(ores);
		Assert.assertNotNull(categories);
		Assert.assertEquals(1, categories.size());
		Assert.assertEquals(category, categories.get(0));
	}

}
