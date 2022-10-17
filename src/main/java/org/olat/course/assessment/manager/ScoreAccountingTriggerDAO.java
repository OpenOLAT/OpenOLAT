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

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.ScoreAccountingTrigger;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.assessment.ScoreAccountingTriggerSearchParams;
import org.olat.course.assessment.model.ScoreAccountingTriggerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ScoreAccountingTriggerDAO {
	
	@Autowired
	private DB dbInstance;

	public ScoreAccountingTrigger create(RepositoryEntry entry, String subIdent, ScoreAccountingTriggerData data) {
		ScoreAccountingTriggerImpl trigger = new ScoreAccountingTriggerImpl();
		trigger.setCreationDate(new Date());
		trigger.setRepositoryEntry(entry);
		trigger.setSubIdent(subIdent);
		trigger.setIdentifier(data.getIdentifier());
		trigger.setBusinessGroupRef(data.getBusinessGroupRef());
		trigger.setOrganisationRef(data.getOrganisationRef());
		trigger.setCurriculumElementRef(data.getCurriculumElementRef());
		trigger.setUserPropertyName(data.getUserPropertyName());
		trigger.setUserPropertyValue(data.getUserPropertyValue());
		dbInstance.getCurrentEntityManager().persist(trigger);
		return trigger;
	}
	
	public List<ScoreAccountingTrigger> load(RepositoryEntryRef entryRef) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select trigger");
		sb.append("  from scoreaccountingtrigger trigger");
		sb.and().append("trigger.repositoryEntry.key = :entryKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ScoreAccountingTrigger.class)
				.setParameter("entryKey", entryRef.getKey())
				.getResultList();
	}

	public List<RepositoryEntry> load(ScoreAccountingTriggerSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select trigger.repositoryEntry");
		sb.append("  from scoreaccountingtrigger trigger");
		sb.and().append("trigger.repositoryEntry.olatResource.resName = 'CourseModule'");
		if (searchParams.getBusinessGroupRef() != null) {
			sb.and().append("trigger.businessGroupKey = :businessGroupKey");
		}
		if (searchParams.getOrganisationRef() != null) {
			sb.and().append("trigger.organisationKey = :organisationKey");
		}
		if (searchParams.getCurriculumElementRef() != null) {
			sb.and().append("trigger.curriculumElementKey = :curriculumElementKey");
		}
		if (StringHelper.containsNonWhitespace(searchParams.getUserPropertyName())) {
			sb.and().append("trigger.userPropertyName = :userPropertyName");
		}
		if (StringHelper.containsNonWhitespace(searchParams.getUserPropertyValue())) {
			sb.and().append("trigger.userPropertyValue = :userPropertyValue");
		}

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class);
		if (searchParams.getBusinessGroupRef() != null) {
			query.setParameter("businessGroupKey", searchParams.getBusinessGroupRef().getKey());
		}
		if (searchParams.getOrganisationRef() != null) {
			query.setParameter("organisationKey", searchParams.getOrganisationRef().getKey());
		}
		if (searchParams.getCurriculumElementRef() != null) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElementRef().getKey());
		}
		if (StringHelper.containsNonWhitespace(searchParams.getUserPropertyName())) {
			query.setParameter("userPropertyName", searchParams.getUserPropertyName());
		}
		if (StringHelper.containsNonWhitespace(searchParams.getUserPropertyValue())) {
			query.setParameter("userPropertyValue", searchParams.getUserPropertyValue().toLowerCase());
		}
		
		return query.getResultList();
	}

	public void delete(List<ScoreAccountingTrigger> scoreAccountingTrigger) {
		String query = "delete from scoreaccountingtrigger trigger where trigger.key in (:keys)";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("keys", PersistenceHelper.toKeys(scoreAccountingTrigger))
				.executeUpdate();
	}

	public void delete(RepositoryEntryRef entryRef) {
		String query = "delete from scoreaccountingtrigger trigger where trigger.repositoryEntry.key = :entryKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("entryKey", entryRef.getKey())
				.executeUpdate();
	}

}
