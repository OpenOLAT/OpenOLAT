/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.certificationprogram.CertificationProgramToCurriculumElement;
import org.olat.modules.certificationprogram.model.CertificationCurriculumElementWithInfos;
import org.olat.modules.certificationprogram.model.CertificationProgramCandidate;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.OrderBy;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.Type;
import org.olat.modules.certificationprogram.model.CertificationProgramToCurriculumElementImpl;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramToCurriculumElementDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CertificationProgramToCurriculumElement createRelation(CertificationProgram program, CurriculumElement curriculumElement) {
		CertificationProgramToCurriculumElementImpl rel = new CertificationProgramToCurriculumElementImpl();
		rel.setCreationDate(new Date());
		rel.setCertificationProgram(program);
		rel.setCurriculumElement(curriculumElement);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public CertificationProgram getCertificationProgram(CurriculumElementRef curriculumElement) {
		String query = """
				select program from certificationprogramtoelement as rel
				inner join rel.certificationProgram as program
				left join fetch program.creditPointSystem as system
				where rel.curriculumElement.key=:elementKey
				order by rel.key desc""";
		
		List<CertificationProgram> programs = dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgram.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.getResultList();
		return programs != null && !programs.isEmpty() ? programs.get(0) : null; 
	}
	
	/**
	 * 
	 * @param curriculumElement The curriculum element
	 * @return A list of relations certification program to element without any fetching
	 */
	List<CertificationProgramToCurriculumElement> getRelations(CurriculumElementRef curriculumElement) {
		String query = """
				select rel from certificationprogramtoelement as rel
				where rel.curriculumElement.key=:elementKey
				order by rel.key desc""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgramToCurriculumElement.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.getResultList();
	}
	
	public void deleteRelation(CurriculumElement element) {
		String query = "delete from certificationprogramtoelement as rel where rel.curriculumElement.key=:elementKey";
		dbInstance.getCurrentEntityManager().createQuery(query)
			.setParameter("elementKey", element.getKey())
			.executeUpdate();
	}
	
	public void deleteRelation(CertificationProgramToCurriculumElement relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}
	
	public boolean hasCurriculumElement(CertificationProgramRef program, CurriculumElementRef curriculumElement) {
		String query = """
				select rel.key from certificationprogramtoelement as rel
				where rel.curriculumElement.key=:elementKey and rel.certificationProgram.key=:programKey""";
		
		List<Long> relationsKeys = dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.setParameter("programKey", program.getKey())
				.setParameter("elementKey", curriculumElement.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return relationsKeys != null && !relationsKeys.isEmpty()
				&& relationsKeys.get(0) != null && relationsKeys.get(0).longValue() > 0;
	}
	
	public List<CertificationCurriculumElementWithInfos> getCurriculumElementsFor(CertificationProgramRef program, Date referenceDate) {
		String query = """
				select new CertificationCurriculumElementWithInfos(curEl, cur,
				 (select count(distinct participants.identity.key) from bgroupmember as participants
				   where participants.group.key=curEl.group.key and participants.role='participant'
				 ) as numOfParticipants,
				 (select count(distinct validCertificate.key) from certificate as validCertificate
				  inner join validCertificate.certificationProgram as certificationProgram
				  inner join certificationprogramtoelement as rel2prog on (certificationProgram.key=rel2prog.certificationProgram.key)
				  where rel2prog.curriculumElement.key=curEl.key
				  and validCertificate.last=true and validCertificate.recertificationPaused=false
				  and (validCertificate.nextRecertificationDate is null or validCertificate.nextRecertificationDate>=:referenceDate)
				 ) as validCertificates,
				 (select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup
				  where reToGroup.group.key=curEl.group.key
				 ) as numOfElements
				) from curriculumelement as curEl
				inner join curEl.curriculum as cur
				left join fetch curEl.type as type
				where exists (select rel.key from certificationprogramtoelement as rel
				 where rel.curriculumElement.key=curEl.key and rel.certificationProgram.key=:programKey
				)""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationCurriculumElementWithInfos.class)
				.setParameter("programKey", program.getKey())
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Identity> getMembers(CertificationProgramMemberSearchParameters searchParams) {
		String query = """
				select ident from curriculumelement el
				inner join el.group baseGroup
				inner join baseGroup.members membership
				inner join membership.identity ident
				inner join fetch ident.user identUser
				where exists (select rel.key from certificationprogramtoelement as rel
				 where rel.curriculumElement.key=el.key and rel.certificationProgram.key=:programKey
				) and membership.role=:role""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Identity.class)
				.setParameter("programKey", searchParams.getCertificationProgram().getKey())
				.setParameter("role", GroupRoles.participant.name())
				.getResultList();
	}
	
	public long countCandidate(CertificationProgramMemberSearchParameters searchParams) {
		QueryBuilder query = new QueryBuilder();
		query.append("select count(distinct participant.identity.key) from curriculumelement as el")
			 .append(" inner join certificationprogramtoelement as rel on (el.key=rel.curriculumElement.key)")
		     .append(" inner join el.group as bGroup")
		     .append(" inner join bGroup.members as participant");
		appendQueryCandidates(query);
		List<Long> count = dbInstance.getCurrentEntityManager().createQuery(query.toString(), Long.class)
				.setParameter("programKey", searchParams.getCertificationProgram().getKey())
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null
				? 0l
				: count.get(0).longValue();
	}
	
	public List<CertificationProgramCandidate> getCandidates(CertificationProgramMemberSearchParameters searchParams) {
		QueryBuilder query = new QueryBuilder();
		query.append("select new CertificationProgramCandidate(ident, el) from curriculumelement as el")
		     .append(" inner join certificationprogramtoelement as rel on (el.key=rel.curriculumElement.key)")
	         .append(" inner join el.group as bGroup")
	         .append(" inner join bGroup.members as participant")
	         .append(" inner join participant.identity as ident")
	         .append(" inner join fetch ident.user as identUser");
		appendQueryCandidates(query);
		return dbInstance.getCurrentEntityManager().createQuery(query.toString(), CertificationProgramCandidate.class)
				.setParameter("programKey", searchParams.getCertificationProgram().getKey())
				.getResultList();
	}
	
	private void appendQueryCandidates(QueryBuilder query) {
		query.where()
			.append(" rel.certificationProgram.key=:programKey and participant.role").in(GroupRoles.participant)
			.append(" and not exists (select cer.key from certificate as cer")
			.append("  where cer.certificationProgram.key=:programKey and cer.identity.key=participant.identity.key")
			.append(" )");
	}
	
	public long countCertificates(CertificationProgramMemberSearchParameters searchParams, Date referenceDate) {
		QueryBuilder query = new QueryBuilder();
		query.append("select count(distinct ident.key) from certificate as cert")
		     .append(" inner join cert.identity as ident");
		
		appendQueryCertificates(query, searchParams);
		List<Long> count = dbInstance.getCurrentEntityManager().createQuery(query.toString(), Long.class)
				.setParameter("programKey", searchParams.getCertificationProgram().getKey())
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null
				? 0l
				: count.get(0).longValue();
	}
	
	public List<Certificate> getCertificates(CertificationProgramMemberSearchParameters searchParams, Date referenceDate, int maxResults) {
		QueryBuilder query = new QueryBuilder();
		query.append("select cert from certificate as cert")
		     .append(" inner join fetch cert.identity as ident")
		     .append(" inner join fetch ident.user as identUser");
		appendQueryCertificates(query, searchParams);
		
		if(searchParams.getOrderBy() != null) {
			if(searchParams.getOrderBy() == OrderBy.NEXTRECERTIFICATIONDATE) {
				query.append(" order by cert.nextRecertificationDate").append(" asc", " desc", searchParams.isOrderAsc());
			} else if(searchParams.getOrderBy() == OrderBy.CREATIONDATE) {
				query.append(" order by cert.creationDate").append(" asc", " desc", searchParams.isOrderAsc());
			}
		}
		
		TypedQuery<Certificate> certificatesQuery = dbInstance.getCurrentEntityManager().createQuery(query.toString(), Certificate.class)
				.setParameter("programKey", searchParams.getCertificationProgram().getKey())
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP);
		if(maxResults > 0) {
			certificatesQuery
				.setFirstResult(0)
				.setMaxResults(maxResults);
		}
		return certificatesQuery.getResultList();
	}
	
	private void appendQueryCertificates(QueryBuilder query, CertificationProgramMemberSearchParameters searchParams) {
		query.where().append(" cert.certificationProgram.key=:programKey");
		
		if(searchParams.getType() == Type.CERTIFIED) {
			// Paused, or valid (before next recertification), expired (after next recertification but in window)
			query.append(" ").append("""
					and cert.last=true
					and (
					  (cert.nextRecertificationDate is null or cert.nextRecertificationDate>=:referenceDate)
					  or
					  (cert.nextRecertificationDate<:referenceDate and cert.recertificationWindowDate>=:referenceDate)
					)""");
		} else if(searchParams.getType() == Type.CERTIFYING) {
			// Paused, or valid (before next recertification), expired (after next recertification but in window)
			query.append(" ").append("""
					and cert.last=true
					and cert.nextRecertificationDate<:referenceDate and cert.recertificationWindowDate>=:referenceDate
					""");
		} else if(searchParams.getType() == Type.REMOVED) {
			// After recertification window or without a last certificate (all revoked)
			query.append(" ").append("""
					and (
					 (cert.last=true and cert.recertificationWindowDate<:referenceDate)
					 or
					 (cert.last=true and cert.nextRecertificationDate<:referenceDate and cert.recertificationWindowDate is null)
					 or
					 not exists (select lastOne from certificate as lastOne
					 	where lastOne.last=true and lastOne.identity.key=ident.key and lastOne.certificationProgram.key=:programKey
					 )
					)""");
		}
	}

}
