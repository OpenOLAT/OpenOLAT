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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemRef;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.model.PerformanceClassImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PerformanceClassDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PerformanceClass create(GradeSystem gradeSystem, String identifier) {
		PerformanceClassImpl performanceClass = new PerformanceClassImpl();
		performanceClass.setCreationDate(new Date());
		performanceClass.setLastModified(performanceClass.getCreationDate());
		performanceClass.setGradeSystem(gradeSystem);
		performanceClass.setIdentifier(identifier);
		performanceClass.setBestToLowest(1000);
		performanceClass.setPassed(false);
		dbInstance.getCurrentEntityManager().persist(performanceClass);
		return performanceClass;
	}
	
	public PerformanceClass save(PerformanceClass performanceClass) {
		if (performanceClass instanceof PerformanceClassImpl) {
			PerformanceClassImpl impl = (PerformanceClassImpl)performanceClass;
			impl.setLastModified(new Date());
			performanceClass = dbInstance.getCurrentEntityManager().merge(performanceClass);
		}
		return performanceClass;
	}

	public List<PerformanceClass> load(GradeSystemRef gradeSystem) {
		if (gradeSystem == null) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select pc");
		sb.append("  from gradeperformanceclass pc");
		sb.and().append("pc.gradeSystem.key = :gradeSystemKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PerformanceClass.class)
				.setParameter("gradeSystemKey", gradeSystem.getKey())
				.getResultList();
	}

	public void delete(PerformanceClass performanceClass) {
		String query = "delete from gradeperformanceclass pc where pc.key = :key";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("key", performanceClass.getKey())
				.executeUpdate();
		}
	
	public void delete(GradeSystemRef gradeSystem) {
		String query = "delete from gradeperformanceclass pc where pc.gradeSystem.key = :gradeSystemKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("gradeSystemKey", gradeSystem.getKey())
				.executeUpdate();
	}

}
