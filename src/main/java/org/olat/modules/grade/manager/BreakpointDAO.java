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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScaleRef;
import org.olat.modules.grade.model.BreakpointImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BreakpointDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Breakpoint create(GradeScale gradeScale) {
		BreakpointImpl breakpoint = new BreakpointImpl();
		breakpoint.setCreationDate(new Date());
		breakpoint.setLastModified(breakpoint.getCreationDate());
		breakpoint.setGradeScale(gradeScale);
		dbInstance.getCurrentEntityManager().persist(breakpoint);
		return breakpoint;
	}
	
	public Breakpoint save(Breakpoint breakpoint) {
		if (breakpoint instanceof BreakpointImpl) {
			BreakpointImpl impl = (BreakpointImpl)breakpoint;
			impl.setLastModified(new Date());
			breakpoint = dbInstance.getCurrentEntityManager().merge(breakpoint);
		}
		return breakpoint;
	}

	public List<Breakpoint> load(GradeScaleRef gradeScale) {
		if (gradeScale == null) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select bp");
		sb.append("  from gradebreakpoint bp");
		sb.and().append("bp.gradeScale.key = :gradeScaleKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Breakpoint.class)
				.setParameter("gradeScaleKey", gradeScale.getKey())
				.getResultList();
	}

	public void delete(Collection<Long> keys) {
		if (keys == null || keys.isEmpty()) return;
		
		String query = "delete from gradebreakpoint bp where bp.key in :keys";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("keys", keys)
				.executeUpdate();
		}
	
	public void delete(GradeScaleRef gradeScale) {
		String query = "delete from gradebreakpoint bp where bp.gradeScale.key = :gradeScaleKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("gradeScaleKey", gradeScale.getKey())
				.executeUpdate();
	}

	public void delete(RepositoryEntryRef repositoryEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from gradebreakpoint bp");
		sb.append(" where bp.gradeScale.key in (");
		sb.append("select gs.key");
		sb.append("  from gradescale gs");
		sb.append(" where gs.repositoryEntry.key = :repoKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and gs.subIdent = :subIdent");
		}
		sb.append(")");
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repoKey", repositoryEntry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		query.executeUpdate();
	}

}
