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
package org.olat.repository.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.model.RepositoryEntryEducationalTypeImpl;
import org.olat.repository.model.RepositoryEntryEducationalTypeStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryEducationalTypeDAO {

	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryEducationalType create(String identifier) {
		return create(identifier, false, null);
	}
	
	public RepositoryEntryEducationalType createPredefined(String identifier, String cssClass) {
		return create(identifier, true, cssClass);
	}

	public RepositoryEntryEducationalType save(RepositoryEntryEducationalType educationalType) {
		if (educationalType instanceof RepositoryEntryEducationalTypeImpl) {
			RepositoryEntryEducationalTypeImpl impl = (RepositoryEntryEducationalTypeImpl)educationalType;
			impl.setLastModified(new Date());
			educationalType = dbInstance.getCurrentEntityManager().merge(educationalType);
		}
		return educationalType;
	}

	private RepositoryEntryEducationalType create(String identifier, boolean predefined, String cssClass) {
		RepositoryEntryEducationalTypeImpl educationalType = new RepositoryEntryEducationalTypeImpl();
		educationalType.setCreationDate(new Date());
		educationalType.setLastModified(educationalType.getCreationDate());
		educationalType.setIdentifier(identifier);
		educationalType.setPredefined(predefined);
		educationalType.setCssClass(cssClass);
		dbInstance.getCurrentEntityManager().persist(educationalType);
		return educationalType;
	}

	public RepositoryEntryEducationalType loadByKey(Long key) {
		if (key == null) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select educationalType");
		sb.append("  from repositoryentryeducationaltype educationalType");
		sb.and().append("educationalType.key = :key");

		List<RepositoryEntryEducationalType> educationalTypes = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryEducationalType.class)
				.setParameter("key", key)
				.getResultList();
		return educationalTypes.isEmpty()? null: educationalTypes.get(0);
	}
	
	public RepositoryEntryEducationalType loadByIdentifier(String identifier) {
		if (!StringHelper.containsNonWhitespace(identifier)) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select educationalType");
		sb.append("  from repositoryentryeducationaltype educationalType");
		sb.and().append("educationalType.identifier = :identifier");

		List<RepositoryEntryEducationalType> educationalTypes = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryEducationalType.class)
				.setParameter("identifier", identifier)
				.getResultList();
		return educationalTypes.isEmpty()? null: educationalTypes.get(0);
	}

	public List<RepositoryEntryEducationalType> loadAll() {
		String query = "select educationalType from repositoryentryeducationaltype educationalType";
		
		return  dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntryEducationalType.class)
				.getResultList();
	}
	

	public void delete(RepositoryEntryEducationalType educationalType) {
		String query = "delete from repositoryentryeducationaltype educationalType where educationalType.key = :key";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("key", educationalType.getKey())
				.executeUpdate();
	}

	public List<RepositoryEntryEducationalTypeStat> loadStats() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.repository.model.RepositoryEntryEducationalTypeStat(");
		sb.append("       re.educationalType.key as key");
		sb.append("     , count(*) as countRe");
		sb.append("       )");
		sb.append("  from repositoryentry as re");
		sb.append(" where re.educationalType.key is not null");
		sb.append(" group by re.educationalType.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryEducationalTypeStat.class)
				.getResultList();
	}

}
