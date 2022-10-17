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
package org.olat.modules.docpool.manager;

import java.util.List;

import jakarta.persistence.FlushModeType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.modules.docpool.DocumentPoolManager;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DocumentPoolManagerImpl implements DocumentPoolManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private DocumentPoolModule documentPoolModule;

	@Override
	public boolean hasValidCompetence(IdentityRef identity) {
		String taxonomyKey = documentPoolModule.getTaxonomyTreeKey();
		if(!StringHelper.isLong(taxonomyKey)) {
			return false;
		}

		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence.key from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join competence.taxonomyLevel level")
		  .append(" inner join level.type type")
		  .append(" where level.taxonomy.key=:taxonomyKey and ident.key=:identityKey")
		  .append(" and (")
		  .append("  (competence.type='").append(TaxonomyCompetenceTypes.manage.name()).append("' and type.documentsLibraryManagerCompetenceEnabled=true)")
		  .append("  or")
		  .append("  (competence.type='").append(TaxonomyCompetenceTypes.teach.name()).append("' and (type.documentsLibraryTeachCompetenceReadEnabled=true or type.documentsLibraryTeachCompetenceWriteEnabled=true))")
		  .append("  or")
		  .append("  (competence.type='").append(TaxonomyCompetenceTypes.have.name()).append("' and type.documentsLibraryHaveCompetenceReadEnabled=true)")
		  .append("  or")
		  .append("  (competence.type='").append(TaxonomyCompetenceTypes.target.name()).append("' and type.documentsLibraryTargetCompetenceReadEnabled=true)")
		  .append(" ) and not(level.identifier in (:lostFoundIds))")
		  .append(" and not exists (select parent.key from ctaxonomylevel parent")
		  .append("   where locate(parent.materializedPathKeys, level.materializedPathKeys) = 1")
		  .append("   and parent.identifier in (:lostFoundIds)")
		  .append(" )");
		
		List<Long> competenceKeys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setFlushMode(FlushModeType.COMMIT)//don't flush for this query
			.setParameter("taxonomyKey", new Long(taxonomyKey))
			.setParameter("identityKey", identity.getKey())
			.setParameter("lostFoundIds", taxonomyModule.getLostAndFoundsIdentifiers())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return competenceKeys != null && competenceKeys.size() > 0
				&& competenceKeys.get(0) != null && competenceKeys.get(0).longValue() > 0;
	}
}
