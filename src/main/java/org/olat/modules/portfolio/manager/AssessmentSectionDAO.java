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
package org.olat.modules.portfolio.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AssessmentSectionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentSectionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentSection createAssessmentSection(BigDecimal score, Boolean passed, Section section, Identity assessedIdentity) {
		AssessmentSectionImpl assessmentSection = new AssessmentSectionImpl();
		assessmentSection.setCreationDate(new Date());
		assessmentSection.setLastModified(assessmentSection.getCreationDate());
		assessmentSection.setPassed(passed);
		assessmentSection.setScore(score);
		assessmentSection.setSection(section);
		assessmentSection.setIdentity(assessedIdentity);
		dbInstance.getCurrentEntityManager().persist(assessmentSection);
		return assessmentSection;
	}
	
	public AssessmentSection loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select asection from pfassessmentsection asection ")
		  .append(" inner join fetch asection.section as section")
		  .append(" where asection.key=:aSectionKey");
		
		List<AssessmentSection> aSections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentSection.class)
				.setParameter("aSectionKey", key)
				.getResultList();
		return aSections == null || aSections.size() < 1 ? null : aSections.get(0);
	}
	
	public List<AssessmentSection> loadAssessmentSections(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select asection from pfassessmentsection asection ")
		  .append(" inner join fetch asection.section as section")
		  .append(" where section.binder.key=:binderKey");
		
		List<AssessmentSection> aSections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentSection.class)
				.setParameter("binderKey", binder.getKey())
				.getResultList();
		return aSections;
	}
	
	public List<AssessmentSection> loadAssessmentSections(BinderRef binder, IdentityRef assessedIdentity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select asection from pfassessmentsection asection ")
		  .append(" inner join fetch asection.section as section")
		  .append(" where section.binder.key=:binderKey and asection.identity.key=:identityKey");
		
		List<AssessmentSection> aSections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentSection.class)
				.setParameter("binderKey", binder.getKey())
				.setParameter("identityKey", assessedIdentity.getKey())
				.getResultList();
		return aSections;
	}
	
	public AssessmentSection update(AssessmentSection assessmentSection) {
		return dbInstance.getCurrentEntityManager().merge(assessmentSection);
	}
	
	public int deleteAssessmentSections(SectionRef section) {
		String partQ = "delete from pfassessmentsection asection where asection.section.key=:sectionKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(partQ)
				.setParameter("sectionKey", section.getKey())
				.executeUpdate();
	}

}
