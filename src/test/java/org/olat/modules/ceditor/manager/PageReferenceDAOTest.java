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
package org.olat.modules.ceditor.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageReference;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageReferenceDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private PageReferenceDAO pageReferenceDao;
	
	@Test
	public void createReference() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Page page = pageDao.createAndPersist("New referenced page", "A brand new page but with a ref.", null, null, true, null, null);
		dbInstance.commitAndCloseSession();
		
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-234");
		dbInstance.commit();
		
		Assert.assertNotNull(reference);
		Assert.assertNotNull(reference.getKey());
		Assert.assertNotNull(reference.getCreationDate());
		Assert.assertEquals(page, reference.getPage());
		Assert.assertEquals(re, reference.getRepositoryEntry());
		Assert.assertEquals("AC-234", reference.getSubIdent());
	}
	
	@Test
	public void hasReference() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Page page = pageDao.createAndPersist("New referenced page", "A brand new page but with a ref.", null, null, true, null, null);
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-235");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);
		
		boolean hasReference = pageReferenceDao.hasReference(page, re, "AC-235");
		Assert.assertTrue(hasReference);
		boolean hasNotReference = pageReferenceDao.hasReference(page, re, "AC-234");
		Assert.assertFalse(hasNotReference);
	}
	
	@Test
	public void deleteReference() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Page page = pageDao.createAndPersist("Referenced page to delete", "A brand new page but with a ref.", null, null, true, null, null);
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-236");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);
		
		int deletedReference = pageReferenceDao.deleteReference(re, "AC-236");
		Assert.assertEquals(1, deletedReference);
	}
}
