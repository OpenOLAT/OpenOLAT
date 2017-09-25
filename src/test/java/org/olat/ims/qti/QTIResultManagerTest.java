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
package org.olat.ims.qti;


import static org.olat.modules.iq.IQTestHelper.createRepository;
import static org.olat.modules.iq.IQTestHelper.createResult;
import static org.olat.modules.iq.IQTestHelper.createSet;
import static org.olat.modules.iq.IQTestHelper.modDate;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIResultManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTIResultManager qtiResultManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	
	@Test
	public void hasResultSets() {
		RepositoryEntry re = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-2");
		dbInstance.commit();
		
		long assessmentId = 838l;
		String resSubPath = "qtiResult34";
		
		//3 try for id1 and id2
		QTIResultSet set1_1 = createSet(1.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResultSet set1_2 = createSet(3.0f, assessmentId, id1, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResultSet set2_1 = createSet(5.0f, assessmentId, id2, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		dbInstance.commit();
		Assert.assertNotNull(set1_1);
		Assert.assertNotNull(set1_2);
		Assert.assertNotNull(set2_1);
		
		boolean hasSet = qtiResultManager.hasResultSets(re.getOlatResource().getResourceableId(), resSubPath, re.getKey());
		Assert.assertTrue(hasSet);
	}
	
	@Test
	public void hasResultSets_negativeTest() {
		RepositoryEntry re = createRepository();
		String resSubPath = "qtiResult35";
		
		boolean hasSet = qtiResultManager.hasResultSets(re.getOlatResource().getResourceableId(), resSubPath, re.getKey());
		Assert.assertFalse(hasSet);
	}
	
	@Test
	public void getResultSets_withIdentity() {
		RepositoryEntry re = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-3");
		dbInstance.commit();
		
		long assessmentId = 839l;
		String resSubPath = "qtiResult36";
		
		//3 try for id1
		QTIResultSet set1_1 = createSet(1.0f, assessmentId, id, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResultSet set1_3 = createSet(3.0f, assessmentId, id, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResultSet set1_2 = createSet(5.0f, assessmentId, id, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		dbInstance.commit();
		
		List<QTIResultSet> set = qtiResultManager.getResultSets(re.getOlatResource().getResourceableId(), resSubPath, re.getKey(), id);
		Assert.assertNotNull(set);
		Assert.assertEquals(3, set.size());
		Assert.assertTrue(set.contains(set1_1));
		Assert.assertTrue(set.contains(set1_2));
		Assert.assertTrue(set.contains(set1_3));
	}
	
	@Test
	public void getResultSets_withoutIdentity() {
		RepositoryEntry re = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-4");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-5");
		dbInstance.commit();
		
		long assessmentId = 840l;
		String resSubPath = "qtiResult36";
		
		//3 try for id1
		QTIResultSet set1_1 = createSet(1.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResultSet set1_2 = createSet(3.0f, assessmentId, id1, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResultSet set2_1 = createSet(5.0f, assessmentId, id2, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		dbInstance.commit();
		
		List<QTIResultSet> set = qtiResultManager.getResultSets(re.getOlatResource().getResourceableId(), resSubPath, re.getKey(), null);
		Assert.assertNotNull(set);
		Assert.assertEquals(3, set.size());
		Assert.assertTrue(set.contains(set1_1));
		Assert.assertTrue(set.contains(set1_2));
		Assert.assertTrue(set.contains(set2_1));
	}
	
	@Test
	public void selectResults() {
		RepositoryEntry re = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-6");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-7");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-8");
		dbInstance.commit();

		long assessmentId = 841l;
		String resSubPath = "qtiResult37";
		String itemIdent = "NES:PS4:849235789";
		
		QTIResultSet set1_1 = createSet(1.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResult result1_1 = createResult(itemIdent, "Hello world", set1_1);
		QTIResultSet set2_1 = createSet(3.0f, assessmentId, id2, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResult result2_1 = createResult(itemIdent, "Bonjour madame", set2_1);
		QTIResultSet set3_1 = createSet(5.0f, assessmentId, id3, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		QTIResult result3_1 = createResult(itemIdent, "Tschuss", set3_1);
		dbInstance.commit();
		
		//order by last name
		List<QTIResult> resultsType1 =  qtiResultManager.selectResults(re.getOlatResource().getResourceableId(), resSubPath, re.getKey(), null, 1);
		Assert.assertNotNull(resultsType1);
		Assert.assertEquals(3, resultsType1.size());
		Assert.assertTrue(resultsType1.contains(result1_1));
		Assert.assertTrue(resultsType1.contains(result2_1));
		Assert.assertTrue(resultsType1.contains(result3_1));

		//order by last name
		List<QTIResult> resultsType2 =  qtiResultManager.selectResults(re.getOlatResource().getResourceableId(), resSubPath, re.getKey(), null,  2);
		Assert.assertNotNull(resultsType2);
		Assert.assertEquals(3, resultsType2.size());
		Assert.assertTrue(resultsType2.contains(result1_1));
		Assert.assertTrue(resultsType2.contains(result2_1));
		Assert.assertTrue(resultsType2.contains(result3_1));

		//order by creation date
		List<QTIResult> resultsType3 =  qtiResultManager.selectResults(re.getOlatResource().getResourceableId(), resSubPath, re.getKey(), null,  3);
		Assert.assertNotNull(resultsType3);
		Assert.assertEquals(3, resultsType3.size());
		Assert.assertTrue(resultsType3.contains(result1_1));
		Assert.assertTrue(resultsType3.contains(result2_1));
		Assert.assertTrue(resultsType3.contains(result3_1));	
	}
	
	@Test
	public void selectResults_limitToSecurityGroup() {
		RepositoryEntry re = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-16");
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.participant.name());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-17");
		dbInstance.commit();

		long assessmentId = 841l;
		String resSubPath = "qtiResult37";
		String itemIdent1 = "NES:PS4:849235797";
		String itemIdent2 = "NES:PS4:849235798";
		
		QTIResultSet set1_1 = createSet(1.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResult result1_1 = createResult(itemIdent1, "Hello world", set1_1);
		QTIResultSet set2_1 = createSet(3.0f, assessmentId, id2, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResult result2_1 = createResult(itemIdent1, "Bonjour madame", set2_1);
		QTIResultSet set1_1b = createSet(5.0f, assessmentId, id1, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		QTIResult result1_1b = createResult(itemIdent1, "Tschuss", set1_1b);
		QTIResult result1_1c = createResult(itemIdent2, "Tschuss", set1_1b);
		dbInstance.commitAndCloseSession();
		
		List<Group> secGroups = Collections.singletonList(repositoryEntryRelationDao.getDefaultGroup(re));
		List<QTIResult> resultsType1 =  qtiResultManager.selectResults(re.getOlatResource().getResourceableId(), resSubPath, re.getKey(), secGroups, 1);
		Assert.assertNotNull(resultsType1);
		Assert.assertEquals(3, resultsType1.size());
		Assert.assertTrue(resultsType1.contains(result1_1));
		Assert.assertTrue(resultsType1.contains(result1_1b));
		Assert.assertTrue(resultsType1.contains(result1_1c));
		//not a participant in the security group
		Assert.assertFalse(resultsType1.contains(result2_1));
	}
	
	@Test
	public void hasResults() {
		RepositoryEntry re = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-9");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-10");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-11");
		dbInstance.commit();

		long assessmentId = 842l;
		String resSubPath = "qtiResult38";
		String itemIdent = "NES:PS4:849235790";
		
		QTIResultSet set1_1 = createSet(1.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResult result1_1 = createResult(itemIdent, "Hello world", set1_1);
		QTIResultSet set2_1 = createSet(3.0f, assessmentId, id2, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResult result2_1 = createResult(itemIdent, "Bonjour madame", set2_1);
		QTIResultSet set3_1 = createSet(5.0f, assessmentId, id3, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		QTIResult result3_1 = createResult(itemIdent, "Tschuss", set3_1);
		dbInstance.commit();
		Assert.assertNotNull(result1_1);
		Assert.assertNotNull(result2_1);
		Assert.assertNotNull(result3_1);
		
		int numOfResults =  qtiResultManager.countResults(re.getOlatResource().getResourceableId(), resSubPath, re.getKey());
		Assert.assertEquals(3, numOfResults);
	}
	
	@Test
	public void hasResults_negativeTest() {
		RepositoryEntry re = createRepository();
		dbInstance.commit();
		
		String resSubPath = "qtiResult39";

		int numOfResults =  qtiResultManager.countResults(re.getOlatResource().getResourceableId(), resSubPath, re.getKey());
		Assert.assertEquals(0, numOfResults);
	}
	
	@Test
	public void findQtiResultSets() {
		RepositoryEntry re1 = createRepository();
		RepositoryEntry re2 = createRepository();
		RepositoryEntry re3 = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-12");
		dbInstance.commit();

		QTIResultSet set1_1 = createSet(1.0f, 842l, id, re1, "qtiResult38", modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResultSet set1_2 = createSet(3.0f, 843l, id, re2, "qtiResult39", modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResultSet set1_3 = createSet(5.0f, 844l, id, re3, "qtiResult40", modDate(3, 10, 35), modDate(3, 10, 55));
		dbInstance.commit();
		
		List<QTIResultSet> sets = qtiResultManager.findQtiResultSets(id);
		Assert.assertNotNull(sets);
		Assert.assertEquals(3, sets.size());
		Assert.assertTrue(sets.contains(set1_1));
		Assert.assertTrue(sets.contains(set1_2));
		Assert.assertTrue(sets.contains(set1_3));
	}
	
	@Test
	public void deleteResultSet() {
		RepositoryEntry re1 = createRepository();
		RepositoryEntry re2 = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-17");
		
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-18");
		dbInstance.commit();
		
		String itemIdent1 = "NES:PS4:849235795";
		String itemIdent2 = "NES:PS4:849235796";
		String resSubPath = "qtiResult43";

		//the set to delete
		QTIResultSet set1_1 = createSet(1.0f, 842l, id1, re1, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResult result1_1a = createResult(itemIdent1, "Hello world", set1_1);
		QTIResult result1_1b = createResult(itemIdent2, "Hello world", set1_1);
		//two sets which stay on the database and use to check that the queries didn't delete too much rows
		QTIResultSet set1_2 = createSet(3.0f, 843l, id1, re2, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResult result1_2 = createResult(itemIdent1, "Hello world", set1_2);
		QTIResultSet set2_1 = createSet(5.0f, 844l, id2, re1, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		QTIResult result2_1 = createResult(itemIdent1, "Hello world", set2_1);
		dbInstance.commitAndCloseSession();
		
		qtiResultManager.deleteResultSet(set1_1);
		dbInstance.commit();
		
		//check sets on database
		List<QTIResultSet> sets = qtiResultManager.getResultSets(re1.getOlatResource().getResourceableId(), resSubPath, re1.getKey(), null);
		Assert.assertNotNull(sets);
		Assert.assertEquals(1, sets.size());
		Assert.assertTrue(sets.contains(set2_1));
		
		List<QTIResultSet> setRe2s = qtiResultManager.getResultSets(re2.getOlatResource().getResourceableId(), resSubPath, re2.getKey(), null);
		Assert.assertNotNull(setRe2s);
		Assert.assertEquals(1, setRe2s.size());
		Assert.assertTrue(setRe2s.contains(set1_2));
		
		//check results
		List<QTIResult> results =  qtiResultManager.selectResults(re1.getOlatResource().getResourceableId(), resSubPath, re1.getKey(), null,  3);
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains(result2_1));
		Assert.assertFalse(results.contains(result1_1a));
		Assert.assertFalse(results.contains(result1_1b));
		
		List<QTIResult> resultsRe2 =  qtiResultManager.selectResults(re2.getOlatResource().getResourceableId(), resSubPath, re2.getKey(), null,  3);
		Assert.assertNotNull(resultsRe2);
		Assert.assertEquals(1, resultsRe2.size());
		Assert.assertTrue(resultsRe2.contains(result1_2));
	}
	
	@Test
	public void deleteAllResults() {
		RepositoryEntry re1 = createRepository();
		RepositoryEntry re2 = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-15");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-16");
		dbInstance.commit();
		
		String itemIdent1 = "NES:PS4:849235793";
		String itemIdent2 = "NES:PS4:849235794";
		String resSubPath = "qtiResult42";

		//the set to delete
		QTIResultSet set1_1 = createSet(1.0f, 842l, id1, re1, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResult result1_1a = createResult(itemIdent1, "Hello world", set1_1);
		QTIResult result1_1b = createResult(itemIdent2, "Hello world", set1_1);
		//two sets which stay on the database and use to check that the queries didn't delete too much rows
		QTIResultSet set1_2 = createSet(3.0f, 843l, id1, re2, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResult result1_2 = createResult(itemIdent1, "Hello world", set1_2);
		QTIResultSet set2_1 = createSet(5.0f, 844l, id2, re1, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		QTIResult result2_1 = createResult(itemIdent1, "Hello world", set2_1);
		dbInstance.commitAndCloseSession();
		
		//delete all results of re1
		qtiResultManager.deleteAllResults(re1.getOlatResource().getResourceableId(), resSubPath, re1.getKey());
		dbInstance.commit();
		
		//check sets on database
		List<QTIResultSet> sets = qtiResultManager.getResultSets(re1.getOlatResource().getResourceableId(), resSubPath, re1.getKey(), null);
		Assert.assertNotNull(sets);
		Assert.assertEquals(0, sets.size());
		
		List<QTIResultSet> setRe2s = qtiResultManager.getResultSets(re2.getOlatResource().getResourceableId(), resSubPath, re2.getKey(), null);
		Assert.assertNotNull(setRe2s);
		Assert.assertEquals(1, setRe2s.size());
		Assert.assertTrue(setRe2s.contains(set1_2));
		
		//check results
		List<QTIResult> results =  qtiResultManager.selectResults(re1.getOlatResource().getResourceableId(), resSubPath, re1.getKey(), null, 3);
		Assert.assertNotNull(results);
		Assert.assertEquals(0, results.size());
		Assert.assertFalse(results.contains(result1_1a));
		Assert.assertFalse(results.contains(result1_1b));
		Assert.assertFalse(results.contains(result2_1));
		
		List<QTIResult> resultsRe2 =  qtiResultManager.selectResults(re2.getOlatResource().getResourceableId(), resSubPath, re2.getKey(), null, 3);
		Assert.assertNotNull(resultsRe2);
		Assert.assertEquals(1, resultsRe2.size());
		Assert.assertTrue(resultsRe2.contains(result1_2));
	}
}
