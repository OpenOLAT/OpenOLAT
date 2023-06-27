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
package org.olat.modules.ceditor.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageReference;
import org.olat.modules.ceditor.model.jpa.PageReferenceImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PageReferenceDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PageReference createReference(Page page, RepositoryEntry repositoryEntry, 
			String subIdent) {
		PageReferenceImpl reference = new PageReferenceImpl();
		reference.setCreationDate(new Date());
		reference.setPage(page);
		reference.setRepositoryEntry(repositoryEntry);
		reference.setSubIdent(subIdent);
		dbInstance.getCurrentEntityManager().persist(reference);
		return reference;
	}
	
	public boolean hasReference(Page page, RepositoryEntry repositoryEntry, 
			String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ref.key from cepagereference ref")
		  .append(" where ref.page.key=:pageKey and ref.repositoryEntry.key=:entryKey and ref.subIdent=:subIdent");

		List<Long> refs = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
			.setParameter("pageKey", page.getKey())
			.setParameter("entryKey", repositoryEntry.getKey())
			.setParameter("subIdent", subIdent)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return refs != null && !refs.isEmpty() && refs.get(0) != null && refs.get(0).longValue() > 0l;
	}

}
