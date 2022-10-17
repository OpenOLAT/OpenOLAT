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

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemRef;
import org.olat.modules.grade.GradeSystemSearchParams;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.model.GradeSystemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradeSystemDAO {
	
	@Autowired
	private DB dbInstance;

	public GradeSystem create(String identifier, GradeSystemType type) {
		return create(identifier, type, false);
	}
	
	GradeSystem create(String identifier, GradeSystemType type, boolean predefined) {
		GradeSystemImpl gradeSystem = new GradeSystemImpl();
		gradeSystem.setCreationDate(new Date());
		gradeSystem.setLastModified(gradeSystem.getCreationDate());
		gradeSystem.setIdentifier(identifier);
		gradeSystem.setPredefined(predefined);
		gradeSystem.setEnabled(true);
		gradeSystem.setPassed(false);
		gradeSystem.setType(type);
		dbInstance.getCurrentEntityManager().persist(gradeSystem);
		return gradeSystem;
	}
	
	public GradeSystem save(GradeSystem gradeSystem) {
		if (gradeSystem instanceof GradeSystemImpl) {
			GradeSystemImpl impl = (GradeSystemImpl)gradeSystem;
			impl.setLastModified(new Date());
			gradeSystem = dbInstance.getCurrentEntityManager().merge(gradeSystem);
		}
		return gradeSystem;
	}

	public List<GradeSystem> load(GradeSystemSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select gs");
		sb.append("  from gradesystem gs");
		if (searchParams.getKeys() != null && !searchParams.getKeys().isEmpty()) {
			sb.and().append("gs.key in :keys");
		}
		if (StringHelper.containsNonWhitespace(searchParams.getIdentifier())) {
			sb.and().append("gs.identifier = :identifier");
		}
		if (searchParams.isEnabledOnly()) {
			sb.and().append("gs.enabled is true");
		}
		
		TypedQuery<GradeSystem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradeSystem.class);
		if (searchParams.getKeys() != null && !searchParams.getKeys().isEmpty()) {
			query.setParameter("keys", searchParams.getKeys());
		}
		if (StringHelper.containsNonWhitespace(searchParams.getIdentifier())) {
			query.setParameter("identifier", searchParams.getIdentifier());
		}
		
		return query.getResultList();
	}

	public void delete(GradeSystemRef gradeSystem) {
		String query = "delete from gradesystem gs where gs.key = :key";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("key", gradeSystem.getKey())
				.executeUpdate();
	}

}
