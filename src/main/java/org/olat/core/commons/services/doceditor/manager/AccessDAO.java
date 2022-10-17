/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by editorTypelicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.doceditor.manager;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.AccessRef;
import org.olat.core.commons.services.doceditor.AccessSearchParams;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.model.AccessImpl;
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
public class AccessDAO {
	
	@Autowired
	private DB dbInstance;
	
	@PostConstruct
	void init() {
		deleteAll();
		dbInstance.commitAndCloseSession();
	}

	public Access createAccess(VFSMetadata metadata, Identity identity, String editorType, Mode mode, boolean versionControlled,
			boolean download, Date expiresAt) {
		AccessImpl access = new AccessImpl();
		access.setCreationDate(new Date());
		access.setLastModified(access.getCreationDate());
		access.setEditorType(editorType);
		access.setMode(mode);
		access.setVersionControlled(versionControlled);
		access.setDownload(download);
		access.setExpiresAt(expiresAt);
		access.setMetadata(metadata);
		access.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(access);
		return access;
	}

	public Access updateExpiresAt(Access access, Date expiresAt) {
		if (access instanceof AccessImpl) {
			AccessImpl accessImpl = (AccessImpl) access;
			accessImpl.setLastModified(new Date());
			accessImpl.setExpiresAt(expiresAt);
			return dbInstance.getCurrentEntityManager().merge(accessImpl);
		}
		return access;
	}
	
	public Access updateEditStartDate(Access access, Date editStartDate) {
		if (access instanceof AccessImpl) {
			AccessImpl accessImpl = (AccessImpl) access;
			accessImpl.setLastModified(new Date());
			accessImpl.setEditStartDate(editStartDate);
			return dbInstance.getCurrentEntityManager().merge(accessImpl);
		}
		return access;
	}
	
	public Access updateMode(Access access, Mode mode) {
		if (access instanceof AccessImpl) {
			AccessImpl accessImpl = (AccessImpl) access;
			accessImpl.setLastModified(new Date());
			accessImpl.setMode(mode);
			return dbInstance.getCurrentEntityManager().merge(accessImpl);
		}
		return access;
	}

	public Access loadAccess(AccessRef accessRef) {
		if (accessRef == null) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select access");
		sb.append("  from doceditoraccess access");
		sb.append("       join fetch access.metadata metadata");
		sb.append("       join fetch access.identity identity");
		sb.and().append("access.key = :accessKey");

		List<Access> accesses = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Access.class)
				.setParameter("accessKey", accessRef.getKey())
				.getResultList();
		return accesses.isEmpty() ? null : accesses.get(0);
	}

	public List<Access> getAccesses(AccessSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select access");
		sb.append("  from doceditoraccess access");
		sb.append("       join").append(" fetch", params.isFetch()).append(" access.metadata metadata");
		sb.append("       join").append(" fetch", params.isFetch()).append(" access.identity identity");
		sb.append("       join").append(" fetch", params.isFetch()).append(" identity.user user");
		if (params.getIdentityKey() != null) {
			sb.and().append("access.identity.key = :identityKey");
		}
		if (params.getMetadataKeys() != null) {
			sb.and().append("access.metadata.key in (:metadataKeys)");
		}
		if (StringHelper.containsNonWhitespace(params.getEditorType())) {
			sb.and().append("access.editorType = :editorType");
		}
		if (params.getMode() != null) {
			sb.and().append("access.mode = :mode");
		}
		
		TypedQuery<Access> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Access.class);
		if (params.getIdentityKey() != null) {
			query.setParameter("identityKey", params.getIdentityKey());
		}
		if (params.getMetadataKeys() != null) {
			query.setParameter("metadataKeys", params.getMetadataKeys());
		}
		if (StringHelper.containsNonWhitespace(params.getEditorType())) {
			query.setParameter("editorType", params.getEditorType());
		}
		if (params.getMode() != null) {
			query.setParameter("mode", params.getMode());
		}
		return query.getResultList();
	}

	public Long getAccessCount(String editorType, Mode mode) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from doceditoraccess access");
		sb.and().append("access.mode = :mode");
		if (StringHelper.containsNonWhitespace(editorType)) {
			sb.and().append("access.editorType = :editorType");
		}

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("mode", mode);
		if (StringHelper.containsNonWhitespace(editorType)) {
			query.setParameter("editorType", editorType);
		}
		
		return query.getResultList().get(0);
	}

	public Long getAccessCount(String editorType, VFSMetadata metadata, Identity identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(access.id)");
		sb.append("  from doceditoraccess access");
		sb.and().append("access.editorType = :editorType");
		sb.and().append("access.metadata.key = :metadataKey");
		sb.and().append("access.identity.key = :identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("metadataKey", metadata.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("editorType", editorType)
				.getSingleResult();
	}
	
	public void delete(AccessRef accessRef) {
		if (accessRef == null) return;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from doceditoraccess access");
		sb.and().append("access.key = :accessKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("accessKey", accessRef.getKey())
				.executeUpdate();
	}

	public void deleteByIdentity(Identity identity) {
		String query = "delete from doceditoraccess access where access.identity.key = :identityKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public void deleteExpired(Date before) {
		if (before == null) return;

		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from doceditoraccess access");
		sb.and().append("access.expiresAt < :before");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("before", before)
				.executeUpdate();
	}

	public void deleteAll() {
		dbInstance.getCurrentEntityManager()
		.createQuery("delete from doceditoraccess")
		.executeUpdate();
	}

}
