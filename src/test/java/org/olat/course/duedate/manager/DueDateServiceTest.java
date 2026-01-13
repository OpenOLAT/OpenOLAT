/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.duedate.manager;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DueDateServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private DueDateServiceImpl dueDateService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private RepositoryEntryLifecycleDAO reLifeCycleDao;

	/**
	 * @see https://track.frentix.com/issue/OO-9160
	 */
	@Test
	public void getRepositoryEntryLifecycle() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		RepositoryEntryLifecycle cycle = reLifeCycleDao.create("Due date", "DD-100", true, new Date(), new Date());
		re = repositoryManager.setDescriptionAndName(re, null, null, null, null, null, null, null, null, cycle);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reference = repositoryEntryDao.loadReferenceByKey(re.getKey());
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryLifecycle reloadedCycle = dueDateService.getRepositoryEntryLifecycle(reference);
		Assert.assertNotNull(reloadedCycle);
		Assert.assertEquals(cycle, reloadedCycle);
	}
	
}
