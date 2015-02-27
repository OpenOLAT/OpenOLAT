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
package org.olat.modules.iq;

import static org.olat.modules.iq.IQTestHelper.createRepository;
import static org.olat.modules.iq.IQTestHelper.createSet;
import static org.olat.modules.iq.IQTestHelper.modDate;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIResultSet;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private IQManager iqManager;
	
	@Test
	public void testLastResultSet() {
		RepositoryEntry re = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("iq-mgr-1");
		dbInstance.commit();
		
		long assessmentId = 836l;
		String resSubPath = "2687";
		
		//3 try for id1
		QTIResultSet set1_1 = createSet(2.0f, assessmentId, id, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		QTIResultSet set1_3 = createSet(6.0f, assessmentId, id, re, resSubPath, modDate(3, 14, 8), modDate(3, 14, 32));
		QTIResultSet set1_2 = createSet(4.0f, assessmentId, id, re, resSubPath, modDate(3, 10, 35), modDate(3, 10, 55));
		dbInstance.commit();
		
		QTIResultSet lastSet = iqManager.getLastResultSet(id, re.getOlatResource().getResourceableId(), resSubPath);
		Assert.assertNotNull(lastSet);
		Assert.assertEquals(set1_3, lastSet);
		Assert.assertFalse(set1_1.equals(lastSet));
		Assert.assertFalse(set1_2.equals(lastSet));
	}
}
