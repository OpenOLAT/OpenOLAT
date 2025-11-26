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
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.CertificationRoles;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.model.CertificationProgramActiveMemberStatistics;
import org.olat.modules.certificationprogram.model.CertificationProgramImpl;
import org.olat.modules.certificationprogram.model.CertificationProgramWithStatistics;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private OLATResourceManager olatResourceManager;
	
	public CertificationProgram createCertificationProgram(String identifier, String displayName) {
		CertificationProgramImpl program = new CertificationProgramImpl();
		program.setCreationDate(new Date());
		program.setLastModified(program.getCreationDate());
		program.setIdentifier(identifier);
		program.setDisplayName(displayName);
		program.setStatus(CertificationProgramStatusEnum.active);
		program.setValidityEnabled(false);
		program.setRecertificationEnabled(false);
		program.setOrganisations(new HashSet<>());
		program.setGroup(groupDao.createGroup());
		dbInstance.getCurrentEntityManager().persist(program);
		
		createResource(program);
		program = dbInstance.getCurrentEntityManager().merge(program);
		return program;
	}
	
	public OLATResource createResource(CertificationProgram element) {
		OLATResource resource = olatResourceManager.createOLATResourceInstance(element);
		olatResourceManager.saveOLATResource(resource);
		((CertificationProgramImpl)element).setResource(resource);
		return resource;
	}
	
	public CertificationProgram loadCertificationProgram(Long key) {
		String query = """
				select program from certificationprogram as program
				inner join fetch program.group as bGroup
				inner join fetch program.resource as rsrc
				left join fetch program.creditPointSystem as system
				left join fetch program.template as template
				where program.key=:programKey""";
		
		List<CertificationProgram> programs = dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgram.class)
			.setParameter("programKey", key)
			.getResultList();
		return programs == null || programs.isEmpty() ? null : programs.get(0);
	}
	
	public CertificationProgram updateCertificationProgram(CertificationProgram certificationProgram) {
		certificationProgram.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(certificationProgram);
	}
	
	public List<CertificationProgram> loadCertificationPrograms() {
		String query = """
				select program from certificationprogram as program
				inner join fetch program.group as bGroup
				inner join fetch program.resource as rsrc
				left join fetch program.creditPointSystem as system
				left join fetch program.template as template""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgram.class)
			.getResultList();
	}
	
	public List<CertificationProgram> loadCertificationPrograms(List<Organisation> organisations) {
		String query = """
				select program from certificationprogram as program
				inner join fetch program.group as bGroup
				inner join fetch program.resource as rsrc
				left join fetch program.creditPointSystem as system
				left join fetch program.template as template
				where exists (select rel.key from certificationprogramtoorganisation as rel
					where rel.certificationProgram.key=program.key and rel.organisation.key in (:organisationsKeys)
				)""";
		
		List<Long> organisationsKeys = organisations.stream()
				.map(Organisation::getKey)
				.toList();
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgram.class)
				.setParameter("organisationsKeys", organisationsKeys)
				.getResultList();
	}
	
	public List<CertificationProgramActiveMemberStatistics> loadCertificationProgramsActiveMembersStatistics(CertificationProgramRef program, Date referenceDate) {
		String query = """
				select new CertificationProgramActiveMemberStatistics(
				 (select count(certifiedCertificate.key) from certificate as certifiedCertificate
				  where certifiedCertificate.certificationProgram.key=program.key
				  and certifiedCertificate.last=true
				  and (certifiedCertificate.nextRecertificationDate>=:referenceDate or certifiedCertificate.nextRecertificationDate is null)
				 ) as certifiedCertificates,
				 (select count(expiringSoonCertificate.key) from certificate as expiringSoonCertificate
				  where expiringSoonCertificate.certificationProgram.key=program.key
				  and expiringSoonCertificate.last=true
				  and expiringSoonCertificate.nextRecertificationDate>=:referenceDate and expiringSoonCertificate.nextRecertificationDate<=:soonDate
				 ) as expiringSoonCertificates,
				 (select count(inRecertificationCertificate.key) from certificate as inRecertificationCertificate
				  where inRecertificationCertificate.certificationProgram.key=program.key
				  and inRecertificationCertificate.last=true
				  and inRecertificationCertificate.nextRecertificationDate<:referenceDate and inRecertificationCertificate.recertificationWindowDate>=:referenceDate
				 ) as inRecertificationCertificates
				) from certificationprogram as program
				where program.key = :programKey""";
		
		Date soon = DateUtils.getEndOfDay(DateUtils.addDays(referenceDate, 7));
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgramActiveMemberStatistics.class)
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP)
				.setParameter("soonDate", soon, TemporalType.TIMESTAMP)
				.setParameter("programKey", program.getKey())
				.getResultList();
	}
	
	public List<CertificationProgramWithStatistics> loadCertificationProgramsWithStatistics(IdentityRef identity, Date referenceDate) {
		String query = """
				select new CertificationProgramWithStatistics(program,
				 (select count(validCertificate.key) from certificate as validCertificate
				  where validCertificate.certificationProgram.key=program.key
				  and validCertificate.last=true and validCertificate.recertificationPaused=false
				  and (validCertificate.nextRecertificationDate is null or validCertificate.nextRecertificationDate>=:referenceDate)
				 ) as validCertificates,
				 (select count(expiredCertificate.key) from certificate as expiredCertificate
				  where expiredCertificate.certificationProgram.key=program.key
				  and expiredCertificate.last=true and expiredCertificate.recertificationPaused=false
				  and expiredCertificate.nextRecertificationDate<:referenceDate
				  and (expiredCertificate.recertificationWindowDate is null or expiredCertificate.recertificationWindowDate>=:referenceDate)
				 ) as expiredCertificates,
				 (select count(notRenewableCertificate.key) from certificate as notRenewableCertificate
				  where notRenewableCertificate.certificationProgram.key=program.key
				  and notRenewableCertificate.last=true and notRenewableCertificate.recertificationPaused=false
				  and notRenewableCertificate.nextRecertificationDate<:referenceDate and notRenewableCertificate.recertificationWindowDate<:referenceDate
				 ) as notRenewableCertificates,
				 (select count(distinct userWithCertificate.identity.key) from certificate as userWithCertificate
				  where userWithCertificate.certificationProgram.key=program.key
				 ) as userWithCertificates,
				 (select count(distinct userWithLastCertificate.identity.key) from certificate as userWithLastCertificate
				  where userWithLastCertificate.certificationProgram.key=program.key and userWithLastCertificate.last=true
				 ) as userWithLastCertificate,
				 (select count(distinct participant.identity.key) from certificationprogramtoelement as rel2prog
				 	 inner join rel2prog.curriculumElement as progCurEl
				 	 inner join progCurEl.group as rpGroup
				 	 inner join rpGroup.members as participant on (participant.role='participant')
				   where rel2prog.certificationProgram.key=program.key and not exists (select candidateCertificate from certificate as candidateCertificate
				   	 where candidateCertificate.certificationProgram.key=program.key and candidateCertificate.identity.key=participant.identity.key
				   ) 
				 ) as programParticipants
				) from certificationprogram as program
				inner join fetch program.group as bGroup
				left join fetch program.creditPointSystem as system
				where exists (select ownerMember.key from bgroupmember as ownerMember
				  where ownerMember.identity.key=:identityKey and ownerMember.group.key=bGroup.key and ownerMember.role=:ownerRole
				) or exists (select rel.key from certificationprogramtoorganisation as rel
				  inner join rel.organisation as org
				  inner join org.group as oGroup
				  inner join oGroup.members as manager
				  where manager.identity.key=:identityKey and rel.certificationProgram.key=program.key and manager.role in (:managerRoles)
				)
				""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgramWithStatistics.class)
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP)
				.setParameter("identityKey", identity.getKey())
				.setParameter("ownerRole", CertificationRoles.programowner.name())
				.setParameter("managerRoles", List.of(OrganisationRoles.administrator.name(), OrganisationRoles.curriculummanager.name()))
				.getResultList();
	}
	
	public boolean isCertificationProgram(RepositoryEntryRef entry) {
		String query = """
				select program.key from repositoryentry as v
				inner join v.groups as rel
				inner join curriculumelement as curEl on (curEl.group.key=rel.group.key)
				inner join certificationprogramtoelement as cpRel on (cpRel.curriculumElement.key=curEl.key)
				inner join certificationprogram as program on (cpRel.certificationProgram.key=program.key)
				where v.key=:entryKey""";
		
		List<Long> keys = dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public List<CertificationProgram> getCertificationPrograms(RepositoryEntryRef entry) {
		String query = """
				select program from repositoryentry as v
				inner join v.groups as rel
				inner join curriculumelement as curEl on (curEl.group.key=rel.group.key)
				inner join certificationprogramtoelement as cpRel on (cpRel.curriculumElement.key=curEl.key)
				inner join certificationprogram as program on (cpRel.certificationProgram.key=program.key)
				inner join fetch program.resource as rsrc
				where v.key=:entryKey
				order by program.key asc""";
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgram.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public List<AssessmentEntry> getAssessmentEntries(CertificationProgram program, IdentityRef identity, List<RepositoryEntryStatusEnum> status) {
		String query = """
				select ae from assessmententry as ae
				inner join fetch ae.repositoryEntry as re
				inner join courseelement rootElement on (rootElement.repositoryEntry.key=re.key and rootElement.subIdent=ae.subIdent)
				where ae.entryRoot=true and rootElement.passedMode<>'none' and re.status in :status
				and ae.identity.key=:identityKey
				and re.key in (select distinct rel.entry.key from repoentrytogroup as rel
				 inner join curriculumelement as el on (el.group.key=rel.group.key)
				 inner join certificationprogramtoelement as programrel on (programrel.curriculumElement.key=el.key)
				 where programrel.certificationProgram.key=:programKey
				)""";
		
		List<String> statusList = status.stream()
				.map(RepositoryEntryStatusEnum::name)
				.toList();
		
		return dbInstance.getCurrentEntityManager().createQuery(query, AssessmentEntry.class)
				.setParameter("programKey", program.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("status", statusList)
				.getResultList();
	}
	
	/**
	 * This method doesn't check wallet balance.
	 * 
	 * @param program The certification program
	 * @param recertificationOnCreationDate 
	 * @param windowOnCreationDate
	 * @param referenceDate Now typically
	 * @return A list of identities
	 */
	public List<Identity> getEligibleForRecertifications(CertificationProgram program, Date referenceDate) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select certIdent from certificate as cert")
		  .append(" inner join cert.certificationProgram as program")
		  .append(" inner join cert.identity as certIdent")
		  .append(" inner join fetch certIdent.user as certUser");
		whereClauseEligibleForRecertifications(sb, program)
		  .append(" order by cert.identity.key");
		
		TypedQuery<Identity> query = queryEligibleForRecertifications(sb, program, referenceDate);
		return query.getResultList();
	}
	
	public List<Identity> getEligibleForRecertificationsWithCreditPoints(CertificationProgram program, Date referenceDate) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select certIdent from certificate as cert")
		  .append(" inner join cert.certificationProgram as program")
		  .append(" inner join cert.identity as certIdent")
		  .append(" inner join fetch certIdent.user as certUser")
		  .append(" inner join creditpointwallet as wallet on (cert.identity.key = wallet.identity.key and wallet.creditPointSystem.key = program.creditPointSystem.key)");
		whereClauseEligibleForRecertifications(sb, program)
		  .and().append(" wallet.balance>=program.creditPoints")
		  .append(" order by cert.identity.key");
		
		TypedQuery<Identity> query = queryEligibleForRecertifications(sb, program, referenceDate);
		return query.getResultList();
	}
	
	/**
	 * Checks the dates and the membership to program.
	 * 
	 * @param sb The query
	 * @param program The certification program
	 * @return The query
	 */
	private QueryBuilder whereClauseEligibleForRecertifications(QueryBuilder sb, CertificationProgram program) {
		sb.where().append(" program.recertificationMode=:recertificationMode and program.status=:programStatus and program.key=:programKey")
		  .and().append("cert.last=true and cert.recertificationPaused=false");
		
		sb.and().append(" cert.nextRecertificationDate<=:referenceDate");

		if(program.isRecertificationWindowEnabled()) {
			sb.and().append(" cert.recertificationWindowDate>=:referenceDate");
		}
		
		sb.and().append(" exists (select programRel.key from certificationprogramtoelement as programRel")
		  .append(" inner join curriculumelement as curEl on (programRel.curriculumElement.key=curEl.key)")
		  .append(" inner join curEl.group as cGroup")
		  .append(" inner join cGroup.members as cMember")
		  .append(" where programRel.certificationProgram.key=program.key and cMember.identity.key=certIdent.key")
		  .append(" and cMember.role ").in(GroupRoles.participant)
		  .append(")");

		return sb;
	}
	
	private TypedQuery<Identity> queryEligibleForRecertifications(QueryBuilder sb, CertificationProgram program, Date referenceDate) {
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Identity.class)
				.setParameter("recertificationMode", RecertificationMode.automatic)
				.setParameter("programStatus", CertificationProgramStatusEnum.active)
				.setParameter("programKey", program.getKey())
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP);
	}
}
