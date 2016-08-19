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
package org.olat.modules.assessment.manager;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 20.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO courseNodeAssessmentDao;
	
	@Test
	public void createCourseNodeAssessment() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = "39485349759";

		AssessmentEntry nodeAssessment = courseNodeAssessmentDao
				.createCourseNodeAssessment(assessedIdentity, null, entry, subIdent, entry);
		Assert.assertNotNull(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		//check values
		Assert.assertNotNull(nodeAssessment.getKey());
		Assert.assertNotNull(nodeAssessment.getCreationDate());
		Assert.assertNotNull(nodeAssessment.getLastModified());
		Assert.assertEquals(assessedIdentity, nodeAssessment.getIdentity());
		Assert.assertEquals(entry, nodeAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, nodeAssessment.getSubIdent());
	}
	
	@Test
	public void loadCourseNodeAssessmentById() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = courseNodeAssessmentDao
				.createCourseNodeAssessment(assessedIdentity, null, entry, subIdent, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = courseNodeAssessmentDao.loadAssessmentEntryById(nodeAssessment.getKey());
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(assessedIdentity, reloadedAssessment.getIdentity());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
	}
	
	@Test
	public void loadCourseNodeAssessment() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = courseNodeAssessmentDao
				.createCourseNodeAssessment(assessedIdentity, null, entry, subIdent, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = courseNodeAssessmentDao
				.loadAssessmentEntry(assessedIdentity, null, entry, subIdent);
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(assessedIdentity, reloadedAssessment.getIdentity());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
	}

}
