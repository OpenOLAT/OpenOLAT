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
package org.olat.core.commons.services.doceditor.wopi.manager;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.model.AccessImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class AccessDAO {
	
	@Autowired
	private DB dbInstance;
	
	@PostConstruct
	void init() {
		deleteAll();
		dbInstance.commitAndCloseSession();
	}

	Access createAccess(VFSMetadata metadata, Identity identity, String token, boolean canEdit,
			boolean canClose, boolean versionControlled, Date expiresAt) {
		AccessImpl access = new AccessImpl();
		access.setCreationDate(new Date());
		access.setLastModified(access.getCreationDate());
		access.setToken(token);
		access.setCanEdit(canEdit);
		access.setCanClose(canClose);
		access.setVersionControlled(versionControlled);
		access.setExpiresAt(expiresAt);
		access.setMetadata(metadata);
		access.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(access);
		return access;
	}

	Access updateExpiresAt(Access access, Date expiresAt) {
		if (access instanceof AccessImpl) {
			AccessImpl accessImpl = (AccessImpl) access;
			accessImpl.setLastModified(new Date());
			accessImpl.setExpiresAt(expiresAt);
			return dbInstance.getCurrentEntityManager().merge(accessImpl);
		}
		return access;
	}

	Access loadAccess(String token) {
		if (!StringHelper.containsNonWhitespace(token)) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select access");
		sb.append("  from wopiaccess access");
		sb.append("       join fetch access.metadata metadata");
		sb.append("       join fetch access.identity identity");
		sb.and().append("access.token = :token");

		List<Access> accesses = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Access.class)
				.setParameter("token", token)
				.getResultList();
		return accesses.isEmpty() ? null : accesses.get(0);
	}

	Access loadAccess(VFSMetadata metadata, Identity identity) {
		if (metadata == null || identity == null) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select access");
		sb.append("  from wopiaccess access");
		sb.append("       join fetch access.metadata metadata");
		sb.append("       join fetch access.identity identity");
		sb.and().append("access.metadata.key = :metadataKey");
		sb.and().append("access.identity.key = :identityKey");

		List<Access> accesses = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Access.class)
				.setParameter("metadataKey", metadata.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return accesses.isEmpty() ? null : accesses.get(0);
	}

	void deleteAccess(String token) {
		if (!StringHelper.containsNonWhitespace(token)) return;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from wopiaccess access");
		sb.and().append("access.token = :token");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("token", token)
				.executeUpdate();
	}

	void deleteAll() {
		dbInstance.getCurrentEntityManager()
		.createQuery("delete from wopiaccess")
		.executeUpdate();
	}
	
}
