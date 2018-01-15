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
package de.bps.onyx.plugin;

import static org.olat.modules.iq.IQTestHelper.createRepository;
import static org.olat.modules.iq.IQTestHelper.createSet;
import static org.olat.modules.iq.IQTestHelper.modDate;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.ims.qti.QTIResultSet;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OnyxResultManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Test
	public void findResultSets() {
		List<QTIResultSet> resultSets = OnyxResultManager.findResultSets();
		Assert.assertNotNull(resultSets);
	}
	
	@Test
	public void isLastTestTry() {
		RepositoryEntry re = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-1");
		long assessmentId = CodeHelper.getForeverUniqueID();
		String resSubPath = "qtiResult35";
		QTIResultSet set = createSet(1.0f, assessmentId, id, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		dbInstance.commit();
		Assert.assertNotNull(set);
		
		//check
		Boolean last = OnyxResultManager.isLastTestTry(set);
		Assert.assertTrue(last.booleanValue());
	}
	
	@Test
	public void getSuspendedQTIResultSet() {
		RepositoryEntry re = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-2");
		long assessmentId = CodeHelper.getForeverUniqueID();
		String resSubPath = "qtiResult36";
		QTIResultSet set = createSet(1.0f, assessmentId, id, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		dbInstance.commit();
		Assert.assertNotNull(set);
		
		// fake the course node
		QTICourseNode courseNode = new IQTESTCourseNode();
		courseNode.setIdent(resSubPath);
		//check
		List<Long> suspendedResults = OnyxResultManager.getSuspendedQTIResultSet(id, courseNode);
		Assert.assertNotNull(suspendedResults);
	}
	
	@Test
	public void getResultSetByAssessmentId() {
		RepositoryEntry re = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-3");
		long assessmentId = CodeHelper.getForeverUniqueID();
		String resSubPath = "qtiResult37";
		QTIResultSet set = createSet(1.0f, assessmentId, id, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		dbInstance.commit();
		Assert.assertNotNull(set);

		//check
		List<Long> results = OnyxResultManager.getResultSetByAssessmentId(assessmentId);
		Assert.assertNotNull(results);
		Assert.assertTrue(results.contains(set.getKey()));
	}
}
