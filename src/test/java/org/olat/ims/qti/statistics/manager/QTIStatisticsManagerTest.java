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
package org.olat.ims.qti.statistics.manager;

import static org.olat.modules.iq.IQTestHelper.createRepository;
import static org.olat.modules.iq.IQTestHelper.createResult;
import static org.olat.modules.iq.IQTestHelper.createSet;
import static org.olat.modules.iq.IQTestHelper.modDate;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.model.QTIStatisticResult;
import org.olat.ims.qti.statistics.model.QTIStatisticResultSet;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIStatisticsManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTIStatisticsManager qtiStatisticsManager;
	
	/**
	 * Test retrieve the last modified result set for every assessed users
	 */
	@Test
	public void testLastQTIResultsSetQuery() {
		RepositoryEntry re = createRepository();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-stats-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-stats-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-stats-3");
		dbInstance.commit();
		
		long assessmentId = 835l;
		String resSubPath = "1234";
		
		//3 try for id1
		QTIResultSet set1_1 = createSet(2.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 8), modDate(3, 8, 12));
		QTIResultSet set1_3 = createSet(6.0f, assessmentId, id1, re, resSubPath, modDate(3, 14, 7), modDate(3, 14, 38));
		QTIResultSet set1_2 = createSet(4.0f, assessmentId, id1, re, resSubPath, modDate(3, 10, 34), modDate(3, 10, 45));

		//2 try for id2
		QTIResultSet set2_1 = createSet(6.0f, assessmentId, id2, re, resSubPath, modDate(3, 9, 21), modDate(3, 9, 45));
		QTIResultSet set2_2 = createSet(5.0f, assessmentId, id2, re, resSubPath, modDate(3, 12, 45), modDate(3, 12, 55));

		//1 try for id1
		QTIResultSet set3_1 = createSet(1.0f, assessmentId, id3, re, resSubPath, modDate(3, 12, 1), modDate(3, 12, 12));
		dbInstance.commit();

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(re.getOlatResource().getResourceableId(), resSubPath);
		List<QTIStatisticResultSet> sets = qtiStatisticsManager.getAllResultSets(searchParams);

		Assert.assertNotNull(sets);
		Assert.assertEquals(3, sets.size());
		
		List<Long> setKeys = PersistenceHelper.toKeys(sets);
		Assert.assertTrue(setKeys.contains(set1_3.getKey()));
		Assert.assertTrue(setKeys.contains(set2_2.getKey()));
		Assert.assertTrue(setKeys.contains(set3_1.getKey()));
		Assert.assertFalse(setKeys.contains(set1_1.getKey()));
		Assert.assertFalse(setKeys.contains(set1_2.getKey()));
		Assert.assertFalse(setKeys.contains(set2_1.getKey()));	
	}
	
	@Test
	public void testResultSetStatistics() {
		RepositoryEntry re = createRepository();
		long assessmentId = 837l;
		String resSubPath = "1237";

		for(int i=0; i<10; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-stats-1" + i);
			float score = Math.round((Math.random() * 10) + 1l);
			createSet(score, assessmentId, id, re, resSubPath, modDate(3, 8, 8), modDate(3, 8, 12));
		}
		dbInstance.commit();
		
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(re.getOlatResource().getResourceableId(), resSubPath);
		StatisticAssessment stats = qtiStatisticsManager.getAssessmentStatistics(searchParams);
		Assert.assertNotNull(stats);
	}
	
	/**
	 * retrieve the results of the last modified result set
	 */
	@Test
	public void testResultStatistics() {
		RepositoryEntry re = createRepository();
		long assessmentId = 838l;
		String resSubPath = "1238";
		String firstQuestion = "id:123";
		String secondQuestion = "id:124";
		String thirdQuestion = "id:125";

		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-stats-20");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-stats-21");
		
		//3 try for id1
		QTIResultSet set1_1 = createSet(2.0f, assessmentId, id1, re, resSubPath, modDate(3, 8, 8), modDate(3, 8, 12));
		QTIResult result1_1_1 = createResult(firstQuestion, "test 1", set1_1);
		QTIResult result1_1_2 = createResult(secondQuestion, "test 2", set1_1);
		QTIResult result1_1_3 = createResult(thirdQuestion, "test 3", set1_1);

		QTIResultSet set1_3 = createSet(6.0f, assessmentId, id1, re, resSubPath, modDate(3, 14, 7), modDate(3, 14, 38));
		QTIResult result1_3_1 = createResult(firstQuestion, "test 1", set1_3);
		QTIResult result1_3_2 = createResult(secondQuestion, "test 2", set1_3);
		QTIResult result1_3_3 = createResult(thirdQuestion, "test 3", set1_3);
		
		QTIResultSet set1_2 = createSet(4.0f, assessmentId, id1, re, resSubPath, modDate(3, 10, 34), modDate(3, 10, 45));
		QTIResult result1_2_1 = createResult(firstQuestion, "test 1", set1_2);
		QTIResult result1_2_2 = createResult(secondQuestion, "test 2", set1_2);
		QTIResult result1_2_3 = createResult(thirdQuestion, "test 3", set1_2);

		//1 try for id2
		QTIResultSet set2_1 = createSet(6.0f, assessmentId, id2, re, resSubPath, modDate(3, 9, 21), modDate(3, 9, 45));
		QTIResult result2_1_1 = createResult(firstQuestion, "test 1", set2_1);
		QTIResult result2_1_2 = createResult(secondQuestion, "test 2", set2_1);
		QTIResult result2_1_3 = createResult(thirdQuestion, "test 3", set2_1);
		dbInstance.commit();
		
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(re.getOlatResource().getResourceableId(), resSubPath);
		List<QTIStatisticResult> results = qtiStatisticsManager.getResults(searchParams);
		
		Assert.assertNotNull(results);
		Assert.assertEquals(6, results.size());

		List<Long> setKeys = PersistenceHelper.toKeys(results);
		Assert.assertTrue(setKeys.contains(result1_3_1.getKey()));
		Assert.assertTrue(setKeys.contains(result1_3_2.getKey()));
		Assert.assertTrue(setKeys.contains(result1_3_3.getKey()));
		Assert.assertTrue(setKeys.contains(result2_1_1.getKey()));
		Assert.assertTrue(setKeys.contains(result2_1_2.getKey()));
		Assert.assertTrue(setKeys.contains(result2_1_3.getKey()));
		
		Assert.assertFalse(setKeys.contains(result1_1_1.getKey()));
		Assert.assertFalse(setKeys.contains(result1_1_2.getKey()));
		Assert.assertFalse(setKeys.contains(result1_1_3.getKey()));
		Assert.assertFalse(setKeys.contains(result1_2_1.getKey()));
		Assert.assertFalse(setKeys.contains(result1_2_2.getKey()));
		Assert.assertFalse(setKeys.contains(result1_2_3.getKey()));
	}
}