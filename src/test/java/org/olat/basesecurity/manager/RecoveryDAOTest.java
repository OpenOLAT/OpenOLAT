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
package org.olat.basesecurity.manager;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.RecoveryKey;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.Encoder;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecoveryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecoveryKeyDAO recoveryDao;
	
	@Test
	public void generateRecoveryKey() {
		String key = recoveryDao.generateRecoveryKey();
		Assert.assertNotNull(key);
	}
	
	@Test
	public void createRecoveryKey() {
		String key = "text-key";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rec-1");
		RecoveryKey recoveryKey = recoveryDao.createRecoveryKey(key, Encoder.Algorithm.sha512, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(recoveryKey);
		Assert.assertNotNull(recoveryKey.getCreationDate());
		Assert.assertNotNull(recoveryKey.getRecoveryKeyHash());
		Assert.assertEquals(id, recoveryKey.getIdentity());
		Assert.assertNull(recoveryKey.getUseDate());
		Assert.assertTrue(recoveryKey.isSame(key));
	}
	
	@Test
	public void loadAvailableRecoveryKeys() {
		String key = "text-key";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rec-2");
		RecoveryKey recoveryKey = recoveryDao.createRecoveryKey(key, Encoder.Algorithm.sha512, id);
		dbInstance.commitAndCloseSession();
		
		List<RecoveryKey> recoveryKeys = recoveryDao.loadAvailableRecoveryKeys(id);
		assertThat(recoveryKeys)
			.hasSize(1)
			.containsExactly(recoveryKey);
	}
	
	@Test
	public void deleteRecoveryKeys() {
		String key = "text-key-3";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rec-3");
		RecoveryKey recoveryKey = recoveryDao.createRecoveryKey(key, Encoder.Algorithm.sha512, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(recoveryKey);
		
		int numOfDeletedKeys = recoveryDao.deleteRecoveryKeys(id);
		Assert.assertEquals(1, numOfDeletedKeys);
	}

}
