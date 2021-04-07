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
package org.olat.ims.lti13.manager;

import java.security.KeyPair;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.model.LTI13KeyImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * 
 * Initial date: 9 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13KeyDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13KeyDAO lti13KeyDao;
	
	@Test
	public void generateKey() {
		String issuer = "https://a.frentix.com";
		LTI13Key key = lti13KeyDao.generateKey(issuer);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(key);
		Assert.assertNotNull(key.getKey());
		Assert.assertNotNull(key.getCreationDate());
		Assert.assertNotNull(key.getLastModified());
		Assert.assertEquals(issuer, key.getIssuer());
		Assert.assertEquals("RS256", key.getAlgorithm());
		Assert.assertNotNull(key.getKeyId());
		Assert.assertNotNull(key.getPublicKey());
		Assert.assertNotNull(key.getPrivateKey());
		Assert.assertNotEquals(((LTI13KeyImpl)key).getPublicKeyText(), ((LTI13KeyImpl)key).getPrivateKeyText());
	}
	
	/**
	 * Make sur two generate keys are not the same.
	 */
	@Test
	public void generateKeys() {
		String issuer = "https://b.frentix.com";
		LTI13Key key1 = lti13KeyDao.generateKey(issuer);
		LTI13Key key2 = lti13KeyDao.generateKey(issuer);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(key1);
		Assert.assertNotNull(key2);
		Assert.assertNotEquals(key1, key2);
		Assert.assertNotEquals(key1.getPublicKey(), key2.getPublicKey());
		Assert.assertNotEquals(key1.getPrivateKey(), key2.getPrivateKey());
	}
	
	@Test
	public void createKey() {
		String issuer = "https://c.frentix.com";
		String kid = UUID.randomUUID().toString();

		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
		LTI13Key key = lti13KeyDao.createKey(issuer, kid, SignatureAlgorithm.RS256.name(), keyPair.getPublic());
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(key);
		Assert.assertNotNull(key.getKey());
		Assert.assertNotNull(key.getCreationDate());
		Assert.assertNotNull(key.getLastModified());
		Assert.assertEquals(issuer, key.getIssuer());
		Assert.assertEquals("RS256", key.getAlgorithm());
		Assert.assertNotNull(key.getKeyId());
		Assert.assertNotNull(key.getPublicKey());
		Assert.assertEquals(keyPair.getPublic(), key.getPublicKey());
		Assert.assertNull(key.getPrivateKey());
		Assert.assertNull(((LTI13KeyImpl)key).getPrivateKeyText());
	}
	
	@Test
	public void getKeysByIssuer() {
		String issuer = "https://" + UUID.randomUUID() + ".frentix.com";
		LTI13Key key = lti13KeyDao.generateKey(issuer);
		dbInstance.commitAndCloseSession();
		
		List<LTI13Key> keys = lti13KeyDao.getKeys(issuer);
		Assert.assertNotNull(keys);
		Assert.assertEquals(1, keys.size());
		Assert.assertEquals(key, keys.get(0));
	}
	
	@Test
	public void getKeysByKidIssuer() {
		String issuer = "https://" + UUID.randomUUID() + ".frentix.com";
		LTI13Key key = lti13KeyDao.generateKey(issuer);
		dbInstance.commitAndCloseSession();
		
		List<LTI13Key> keys = lti13KeyDao.getKey(key.getKeyId(), issuer);
		Assert.assertNotNull(keys);
		Assert.assertEquals(1, keys.size());
		Assert.assertEquals(key, keys.get(0));
	}

}
