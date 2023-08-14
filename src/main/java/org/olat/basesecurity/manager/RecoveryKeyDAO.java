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

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.RecoveryKey;
import org.olat.basesecurity.model.RecoveryKeyImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RecoveryKeyDAO {
	
	private final SecureRandom keyRandom = new SecureRandom();
	
	@Autowired
	private DB dbInstance;
	
	public String generateRecoveryKey() {
		byte[] key = new byte[16];
		keyRandom.nextBytes(key);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
	}
	
	public RecoveryKey createRecoveryKey(String key, Encoder.Algorithm algorithm, Identity identity) {
		RecoveryKeyImpl recoveryKey = new RecoveryKeyImpl();
		recoveryKey.setCreationDate(new Date());
		String salt = algorithm.isSalted() ? Encoder.getSalt() : null;
		String hash = Encoder.encrypt(key, salt, algorithm);
		recoveryKey.setRecoveryKeyHash(hash);
		recoveryKey.setRecoverySalt(salt);
		recoveryKey.setRecoveryAlgorithm(algorithm.name());
		recoveryKey.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(recoveryKey);
		return recoveryKey;
	}
	
	public List<RecoveryKey> loadAvailableRecoveryKeys(IdentityRef identity) {
		String query = """
				select rkey from recoverykey as rkey
				where rkey.identity.key=:identityKey and rkey.useDate is null
				order by rkey.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RecoveryKey.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public int deleteRecoveryKeys(IdentityRef identity) {
		String query = """
				delete from recoverykey as rkey
				where rkey.identity.key=:identityKey""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public RecoveryKey updateRecoveryKey(RecoveryKey recoveryKey) {
		return dbInstance.getCurrentEntityManager().merge(recoveryKey);
	}

}
