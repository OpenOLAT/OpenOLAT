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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.Reason;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReasonDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ReasonDAO reasonDao;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	
	@Test
	public void createReason() {
		String title = "1. reason";
		String description = "Because";
		Reason reason = reasonDao.createReason(title, description, true);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reason);
		Assert.assertNotNull(reason.getKey());
		Assert.assertNotNull(reason.getCreationDate());
		Assert.assertNotNull(reason.getLastModified());
	}
	
	@Test
	public void getReasons() {
		String title = "2. reason";
		String description = "Find a list";
		Reason reason = reasonDao.createReason(title, description, true);
		dbInstance.commitAndCloseSession();
		
		List<Reason> reasons = reasonDao.getReasons();
		Assert.assertNotNull(reasons);
		Assert.assertFalse(reasons.isEmpty());
		Assert.assertTrue(reasons.contains(reason));
	}
	
	@Test
	public void getDisabledReasons() {
		Reason enabledReason = reasonDao.createReason("Enabled reason 1.", "Very good reason", true);
		Reason disabledReason = reasonDao.createReason("Disabled reason 2.0", "To remove", false);
		dbInstance.commitAndCloseSession();
		
		List<Reason> reasons = reasonDao.getReasons();
		assertThat(reasons)
			.isNotNull()
			.contains(enabledReason, disabledReason);
	}
	
	@Test
	public void isReasonInUse() {
		// create a reason
		String title = "3. reason";
		String description = "Use it";
		Reason reason = reasonDao.createReason(title, description, true);
		dbInstance.commitAndCloseSession();
		
		// add to a lecture block
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setReasonEffectiveEnd(reason);
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		// check
		boolean inUse = reasonDao.isReasonInUse(reason);
		Assert.assertTrue(inUse);
	}
	
	@Test
	public void isReasonInUse_notInUse() {
		// create a reason
		String title = "4. reason";
		String description = "Nobody use me";
		Reason reason = reasonDao.createReason(title, description, true);
		dbInstance.commitAndCloseSession();

		// check
		boolean inUse = reasonDao.isReasonInUse(reason);
		Assert.assertFalse(inUse);
	}
	
	@Test
	public void deleteReasons() {
		//create a reason
		String title = "Forgotten reason";
		String description = "Find a list";
		Reason reason = reasonDao.createReason(title, description, true);
		dbInstance.commitAndCloseSession();
		//check it exists
		List<Reason> reasons = reasonDao.getReasons();
		Assert.assertTrue(reasons.contains(reason));
		
		//delete the reason
		boolean deleted = reasonDao.delete(reason);
		Assert.assertTrue(deleted);
		
		//check it exists
		List<Reason> reloadedDasons = reasonDao.getReasons();
		Assert.assertFalse(reloadedDasons.contains(reason));
	}
}
