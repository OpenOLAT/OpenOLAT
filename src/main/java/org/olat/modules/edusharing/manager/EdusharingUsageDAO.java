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
package org.olat.modules.edusharing.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.EdusharingHtmlElement;
import org.olat.modules.edusharing.EdusharingUsage;
import org.olat.modules.edusharing.model.EdusharingUsageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class EdusharingUsageDAO {
	
	@Autowired
	private DB dbInstance;
	
	EdusharingUsage create(Identity identity, EdusharingHtmlElement element, OLATResourceable ores) {
		return create(identity, element, ores, null);
	}

	EdusharingUsage create(Identity identity, EdusharingHtmlElement element, OLATResourceable ores, String subPath) {
		EdusharingUsageImpl usage = new EdusharingUsageImpl();
		usage.setCreationDate(new Date());
		usage.setLastModified(usage.getCreationDate());
		usage.setIdentifier(element.getIdentifier());
		usage.setResName(ores.getResourceableTypeName());
		usage.setResId(ores.getResourceableId());
		usage.setSubPath(subPath);
		usage.setObjectUrl(element.getObjectUrl());
		usage.setVersion(element.getVersion());
		usage.setMimeType(element.getMimeType());
		usage.setMediaType(element.getMediaType());
		usage.setWidth(element.getWidth());
		usage.setHeight(element.getHight());
		usage.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(usage);
		return usage;
	}
	
	EdusharingUsage loadByIdentifier(String identifier) {
		if (!StringHelper.containsNonWhitespace(identifier)) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select usage");
		sb.append("  from edusharingusage as usage");
		sb.and().append("usage.identifier = :identifier");
		
		List<EdusharingUsage> usages = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EdusharingUsage.class)
				.setParameter("identifier", identifier)
				.getResultList();
		return !usages.isEmpty()? usages.get(0): null;
	}

	public List<EdusharingUsage> loadByResoureable(OLATResourceable ores, String subPath) {
		if (ores == null) return new ArrayList<>(0);
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select usage");
		sb.append("  from edusharingusage as usage");
		sb.and().append("usage.resName = :resName");
		sb.and().append("usage.resId = :resId");
		if (StringHelper.containsNonWhitespace(subPath)) {
			sb.and().append("usage.subPath = :subPath");
		}
		
		TypedQuery<EdusharingUsage> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EdusharingUsage.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId());
		if (StringHelper.containsNonWhitespace(subPath)) {
			query.setParameter("subPath", subPath);
		}
		return query.getResultList();
	}

	void delete(EdusharingUsage usage) {
		if (usage == null) return;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from edusharingusage as usage");
		sb.append(" where usage.key = :usageKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("usageKey", usage.getKey())
				.executeUpdate();
	}

}
