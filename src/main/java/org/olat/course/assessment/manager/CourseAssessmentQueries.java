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
package org.olat.course.assessment.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 17 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class CourseAssessmentQueries {
	
	@Autowired
	private DB dbInstance;

	public List<RepositoryEntry> loadCoursesLifecycle(Date validToBefore) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re");
		sb.append("  from ").append(RepositoryEntry.class.getName()).append(" as re ");
		sb.append("       inner join fetch re.olatResource as ores");
		sb.append("       inner join fetch re.lifecycle as lifecycle");
		sb.and().append("ores.resName ='CourseModule'");
		sb.and().append("re.status").in(RepositoryEntryStatusEnum.preparationToClosed());
		sb.and().append("lifecycle.validTo < :validToBefore");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("validToBefore", validToBefore)
				.getResultList();
		
	}

}
