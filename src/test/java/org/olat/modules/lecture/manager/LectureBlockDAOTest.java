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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(lectureBlock);
		Assert.assertNotNull(lectureBlock.getKey());
		Assert.assertNotNull(lectureBlock.getCreationDate());
		Assert.assertNotNull(lectureBlock.getLastModified());
		Assert.assertNotNull(lectureBlock.getStartDate());
		Assert.assertNotNull(lectureBlock.getEndDate());
		Assert.assertEquals("Hello lecturers", lectureBlock.getTitle());
		Assert.assertEquals(LectureBlockStatus.active, lectureBlock.getStatus());
		Assert.assertEquals(LectureRollCallStatus.open, lectureBlock.getRollCallStatus());
	}
	
	@Test
	public void createAndLoadLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Bienvenue");
		lectureBlock.setExternalId("XYZ-8976");
		lectureBlock.setLocation("Basel");
		lectureBlock.setDescription("Welcome");
		lectureBlock.setPreparation("Prepare you");
		lectureBlock.setEffectiveEndDate(new Date());
		lectureBlock.setComment("A little comment");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		LectureBlock reloadedBlock = lectureBlockDao.loadByKey(lectureBlock.getKey());
		Assert.assertNotNull(reloadedBlock);
		Assert.assertNotNull(reloadedBlock.getKey());
		Assert.assertNotNull(reloadedBlock.getCreationDate());
		Assert.assertNotNull(reloadedBlock.getLastModified());
		Assert.assertNotNull(reloadedBlock.getStartDate());
		Assert.assertNotNull(reloadedBlock.getEndDate());
		Assert.assertNotNull(reloadedBlock.getEffectiveEndDate());
		
		Assert.assertEquals("Bienvenue", reloadedBlock.getTitle());
		Assert.assertEquals("XYZ-8976", reloadedBlock.getExternalId());
		Assert.assertEquals("Basel", reloadedBlock.getLocation());
		Assert.assertEquals("Welcome", reloadedBlock.getDescription());
		Assert.assertEquals("Prepare you", reloadedBlock.getPreparation());
		Assert.assertEquals("A little comment", reloadedBlock.getComment());
		
		Assert.assertEquals(LectureBlockStatus.active, reloadedBlock.getStatus());
		Assert.assertEquals(LectureRollCallStatus.open, reloadedBlock.getRollCallStatus());
	}
	
	@Test
	public void loadByEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlock> blocks = lectureBlockDao.loadByEntry(entry);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		LectureBlock loadedBlock = blocks.get(0);
		Assert.assertEquals(lectureBlock, loadedBlock);
	}
	
	@Test
	public void addGroup() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		dbInstance.commitAndCloseSession();

		LectureBlockImpl reloadedLectureBlock = (LectureBlockImpl)lectureBlockDao.loadByKey(lectureBlock.getKey());
		Set<LectureBlockToGroup> blockToGroupSet = reloadedLectureBlock.getGroups();
		Assert.assertNotNull(blockToGroupSet);
		Assert.assertEquals(1,  blockToGroupSet.size());
		LectureBlockToGroupImpl defBlockToGroup = (LectureBlockToGroupImpl)blockToGroupSet.iterator().next();
		Assert.assertEquals(defGroup, defBlockToGroup.getGroup());
		Assert.assertEquals(lectureBlock, defBlockToGroup.getLectureBlock());
	}

}
