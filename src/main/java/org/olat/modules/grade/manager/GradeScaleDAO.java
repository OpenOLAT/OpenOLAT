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
package org.olat.modules.grade.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScaleSearchParams;
import org.olat.modules.grade.GradeScaleStats;
import org.olat.modules.grade.GradeSystemRef;
import org.olat.modules.grade.model.GradeScaleImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradeScaleDAO {
	
	@Autowired
	private DB dbInstance;
	
	public GradeScale create(RepositoryEntry repositoryEntry, String subIdent) {
		GradeScaleImpl gradeScale = new GradeScaleImpl();
		gradeScale.setCreationDate(new Date());
		gradeScale.setLastModified(gradeScale.getCreationDate());
		gradeScale.setRepositoryEntry(repositoryEntry);
		gradeScale.setSubIdent(subIdent);
		dbInstance.getCurrentEntityManager().persist(gradeScale);
		return gradeScale;
	}

	public GradeScale save(GradeScale gradeScale) {
		if (gradeScale instanceof GradeScaleImpl) {
			GradeScaleImpl impl = (GradeScaleImpl)gradeScale;
			impl.setLastModified(new Date());
			gradeScale = dbInstance.getCurrentEntityManager().merge(gradeScale);
		}
		return gradeScale;
	}

	public List<GradeScale> load(GradeScaleSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select gs");
		sb.append("  from gradescale gs");
		sb.append("       join fetch gs.gradeSystem gsys");
		if (searchParams.getRepositoryEntry() != null) {
			sb.and().append("gs.repositoryEntry.key = :repoKey");
		}
		if (searchParams.getSubIdents() != null && !searchParams.getSubIdents().isEmpty()) {
			sb.and().append("gs.subIdent in :subIdent");
		}
		
		TypedQuery<GradeScale> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradeScale.class);
		if (searchParams.getRepositoryEntry() != null) {
			query.setParameter("repoKey", searchParams.getRepositoryEntry().getKey());
		}
		if (searchParams.getSubIdents() != null && !searchParams.getSubIdents().isEmpty()) {
			query.setParameter("subIdent", searchParams.getSubIdents());
		}
		
		return query.getResultList();
	}

	public void delete(RepositoryEntryRef repositoryEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from gradescale gs");
		sb.and().append("gs.repositoryEntry.key = :repoKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("gs.subIdent = :subIdent");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repoKey", repositoryEntry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		query.executeUpdate();
	}
	
	public List<GradeScaleStats> loadStats(GradeSystemRef gradeSystem) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.grade.model.GradeScaleStatsImpl(");
		sb.append("       gs.gradeSystem.key");
		sb.append("     , count(*)");
		sb.append(")");
		sb.append("  from gradescale gs");
		if (gradeSystem != null) {
			sb.and().append("gs.gradeSystem.key = :gradeSystemKey");
		}
		sb.append(" group by gs.gradeSystem.key");
		
		TypedQuery<GradeScaleStats> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradeScaleStats.class);
		if (gradeSystem != null) {
			query.setParameter("gradeSystemKey", gradeSystem.getKey());
		}
		
		return query.getResultList();
	}

	public boolean hasPassed(RepositoryEntryRef courseEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select gsys.hasPassed");
		sb.append("  from gradescale gs");
		sb.append("       join gs.gradeSystem gsys");
		sb.and().append("gs.repositoryEntry.key = :repoKey");
		sb.and().append("gs.subIdent = :subIdent");
		
		List<Boolean> resultList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Boolean.class)
				.setParameter("repoKey", courseEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
		
		return resultList != null && !resultList.isEmpty()? resultList.get(0).booleanValue(): false;
	}

}
