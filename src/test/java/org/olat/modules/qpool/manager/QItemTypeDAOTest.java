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
package org.olat.modules.qpool.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.model.QItemType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QItemTypeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QItemTypeDAO qpoolItemTypeDao;
	
	@Test
	public void testCreate() {
		String typeStr = "interessantType-" + UUID.randomUUID().toString();
		QItemType type = qpoolItemTypeDao.create(typeStr, true);
		dbInstance.commit();
		//check
		Assert.assertNotNull(type);
		Assert.assertNotNull(type.getKey());
		Assert.assertNotNull(type.getCreationDate());
		Assert.assertNotNull(type.getType());
		//lower case always
		Assert.assertEquals(typeStr.toLowerCase(), type.getType());
		Assert.assertTrue(type.isDeletable());
	}
	
	@Test
	public void testCreateAndGet() {
		String typeStr = "veryInteressantType-" + UUID.randomUUID().toString();
		QItemType type = qpoolItemTypeDao.create(typeStr, true);
		dbInstance.commit();
		//load it
		QItemType reloadedType = qpoolItemTypeDao.loadById(type.getKey());
		//check the values
		Assert.assertNotNull(reloadedType);
		Assert.assertEquals(type.getKey(), reloadedType.getKey());
		Assert.assertNotNull(reloadedType.getCreationDate());
		Assert.assertEquals(typeStr.toLowerCase(), reloadedType.getType());
		Assert.assertTrue(reloadedType.isDeletable());
	}
	
	@Test
	public void testGetItemTypes() {
		String typeStr = "cute-" + UUID.randomUUID().toString();
		QItemType type = qpoolItemTypeDao.create(typeStr, true);
		dbInstance.commit();
		//load it
		List<QItemType> allTypes = qpoolItemTypeDao.getItemTypes();
		//check the values
		Assert.assertNotNull(allTypes);
		Assert.assertTrue(allTypes.contains(type));
	}
	
	@Test
	public void testDelete_deletable() {
		String typeStr = "cute-" + UUID.randomUUID().toString();
		QItemType type = qpoolItemTypeDao.create(typeStr, true);
		dbInstance.commitAndCloseSession();
		
		//delete it
		boolean deleted = qpoolItemTypeDao.delete(type);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deleted);

		//check that the type is really, really deleted
		QItemType reloadedType = qpoolItemTypeDao.loadById(type.getKey());
		Assert.assertNull(reloadedType);
		List<QItemType> allTypes = qpoolItemTypeDao.getItemTypes();
		Assert.assertFalse(allTypes.contains(type));
	}
	
	@Test
	public void testDelete_notDeletable() {
		String typeStr = "titanium-" + UUID.randomUUID().toString();
		QItemType type = qpoolItemTypeDao.create(typeStr, false);
		dbInstance.commitAndCloseSession();
		
		//delete it
		boolean deleted = qpoolItemTypeDao.delete(type);
		dbInstance.commitAndCloseSession();
		Assert.assertFalse(deleted);

		//check that the type is really, really deleted
		QItemType reloadedType = qpoolItemTypeDao.loadById(type.getKey());
		Assert.assertNotNull(reloadedType);
		List<QItemType> allTypes = qpoolItemTypeDao.getItemTypes();
		Assert.assertTrue(allTypes.contains(type));
	}
}