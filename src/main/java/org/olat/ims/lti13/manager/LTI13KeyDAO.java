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
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.model.LTI13KeyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * 
 * Initial date: 9 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13KeyDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LTI13Key generateKey(String issuer) {
		LTI13KeyImpl key = new LTI13KeyImpl();
		key.setCreationDate(new Date());
		key.setLastModified(key.getCreationDate());
		key.setIssuer(issuer);
		key.setKeyId(UUID.randomUUID().toString());
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
		key.setKeyPair(SignatureAlgorithm.RS256.name(), keyPair);
		dbInstance.getCurrentEntityManager().persist(key);
		return key;
	}
	
	public LTI13Key createKey(String issuer, String kid, String algorithm, PublicKey publicKey) {
		LTI13KeyImpl key = new LTI13KeyImpl();
		key.setCreationDate(new Date());
		key.setLastModified(key.getCreationDate());
		key.setIssuer(issuer);
		key.setKeyId(kid);
		key.setAlgorithm(algorithm);
		key.setPublicKey(publicKey);
		dbInstance.getCurrentEntityManager().persist(key);
		return key;
	}
	
	public List<LTI13Key> getKeys(String issuer) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select pkey from ltikey as pkey where pkey.issuer=:issuer")
		  .append(" order by pkey.creationDate desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTI13Key.class)
				.setParameter("issuer", issuer)
				.getResultList();
	}
}
