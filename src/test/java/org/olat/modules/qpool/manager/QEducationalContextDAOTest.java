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
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QEducationalContextDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QEducationalContextDAO qEduContextDao;
	
	@Test
	public void testCreate() {
		String levelStr = "lowLevel-" + UUID.randomUUID().toString();
		QEducationalContext level = qEduContextDao.create(levelStr, true);
		dbInstance.commit();
		//check
		Assert.assertNotNull(level);
		Assert.assertNotNull(level.getKey());
		Assert.assertNotNull(level.getCreationDate());
		Assert.assertEquals(levelStr, level.getLevel());
		Assert.assertTrue(level.isDeletable());
	}
	
	@Test
	public void testCreateAndGet() {
		String levelStr = "highLevel-" + UUID.randomUUID().toString();
		QEducationalContext level = qEduContextDao.create(levelStr, true);
		dbInstance.commit();
		//load it
		QEducationalContext reloadedLevel = qEduContextDao.loadById(level.getKey());
		//check the values
		Assert.assertNotNull(reloadedLevel);
		Assert.assertEquals(level.getKey(), reloadedLevel.getKey());
		Assert.assertNotNull(reloadedLevel.getCreationDate());
		Assert.assertEquals(levelStr, reloadedLevel.getLevel());
		Assert.assertTrue(reloadedLevel.isDeletable());
	}
	
	@Test
	public void testCreateAndGet_byLevel() {
		String levelStr = "onlyLevel-" + UUID.randomUUID().toString();
		QEducationalContext level = qEduContextDao.create(levelStr, true);
		dbInstance.commit();
		//load it
		QEducationalContext reloadedLevel = qEduContextDao.loadByLevel(levelStr);
		//check the values
		Assert.assertNotNull(reloadedLevel);
		Assert.assertEquals(level.getKey(), reloadedLevel.getKey());
		Assert.assertNotNull(reloadedLevel.getCreationDate());
		Assert.assertEquals(levelStr, reloadedLevel.getLevel());
		Assert.assertTrue(reloadedLevel.isDeletable());
	}
	
	@Test
	public void testGetItemLevels() {
		String levelStr = "primary-school-" + UUID.randomUUID().toString();
		QEducationalContext level = qEduContextDao.create(levelStr, true);
		dbInstance.commit();
		//load it
		List<QEducationalContext> allLevels = qEduContextDao.getEducationalContexts();
		//check the values
		Assert.assertNotNull(allLevels);
		Assert.assertTrue(allLevels.contains(level));
	}
	
	@Test
	public void testDelete_deletable() {
		String levelStr = "secondary-school-" + UUID.randomUUID().toString();
		QEducationalContext level = qEduContextDao.create(levelStr, true);
		dbInstance.commitAndCloseSession();
		
		//delete it
		boolean deleted = qEduContextDao.delete(level);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deleted);

		//check that the type is really, really deleted
		QEducationalContext reloadedLevel = qEduContextDao.loadById(level.getKey());
		Assert.assertNull(reloadedLevel);
		List<QEducationalContext> allLevels = qEduContextDao.getEducationalContexts();
		Assert.assertFalse(allLevels.contains(level));
	}
	
	@Test
	public void testDelete_notDeletable() {
		String levelStr = "uni-" + UUID.randomUUID().toString();
		QEducationalContext level = qEduContextDao.create(levelStr, false);
		dbInstance.commitAndCloseSession();
		
		//delete it
		boolean deleted = qEduContextDao.delete(level);
		dbInstance.commitAndCloseSession();
		Assert.assertFalse(deleted);

		//check that the type is really, really deleted
		QEducationalContext reloadedLevel = qEduContextDao.loadById(level.getKey());
		Assert.assertNotNull(reloadedLevel);
		List<QEducationalContext> allLevels = qEduContextDao.getEducationalContexts();
		Assert.assertTrue(allLevels.contains(level));
	}
}