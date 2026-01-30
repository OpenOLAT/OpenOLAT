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

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramLog;
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.model.CertificationProgramLogImpl;
import org.olat.modules.certificationprogram.model.CertificationProgramLogSearchParameters;
import org.olat.modules.curriculum.CurriculumElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CertificationProgramLog createMailLog(Certificate certificate, CertificationProgram program,
			CertificationProgramMailConfiguration mailConfiguration, Identity doer) {
		CertificationProgramLogImpl mailLog = new CertificationProgramLogImpl();
		mailLog.setCreationDate(LocalDateTime.now());
		CertificationProgramLogAction action = actionFrom(mailConfiguration);
		mailLog.setAction(action);
		mailLog.setCertificate(certificate);
		mailLog.setMailConfiguration(mailConfiguration);
		mailLog.setCertificationProgram(program);
		mailLog.setDoer(doer);
		dbInstance.getCurrentEntityManager().persist(mailLog);
		return mailLog;
	}
	
	public CertificationProgramLog createLog(Certificate certificate, CertificationProgram program, CertificationProgramLogAction action,
			String beforeStatus, String beforeValue, String afterStatus, String afterValue,
			CertificationProgramMailConfiguration mailConfiguration, CurriculumElement curriculumElement, Identity doer) {
		return createLog(LocalDateTime.now(), certificate, program, action, beforeStatus, beforeValue, afterStatus, afterValue, mailConfiguration, curriculumElement, doer);
	}
		
	protected CertificationProgramLog createLog(LocalDateTime creationDate, Certificate certificate, CertificationProgram program, CertificationProgramLogAction action,
				String beforeStatus, String beforeValue, String afterStatus, String afterValue,
				CertificationProgramMailConfiguration mailConfiguration, CurriculumElement curriculumElement, Identity doer) {
		CertificationProgramLogImpl programLog = new CertificationProgramLogImpl();
		programLog.setCreationDate(creationDate);
		programLog.setAction(action);
		if(certificate != null) {
			programLog.setIdentity(certificate.getIdentity());
			programLog.setCertificate(certificate);
		}
		programLog.setCertificationProgram(program);
		programLog.setMailConfiguration(mailConfiguration);
		programLog.setCurriculumElement(curriculumElement);
		programLog.setBeforeStatus(beforeStatus);
		programLog.setBefore(beforeValue);
		programLog.setAfterStatus(afterStatus);
		programLog.setAfter(afterValue);
		programLog.setDoer(doer);
		dbInstance.getCurrentEntityManager().persist(programLog);
		return programLog;
	}
	
	public CertificationProgramLog createOwnerLog(Identity owner, CertificationProgram program, CertificationProgramLogAction action, Identity doer) {
		CertificationProgramLogImpl programLog = new CertificationProgramLogImpl();
		programLog.setCreationDate(LocalDateTime.now());
		programLog.setAction(action);
		programLog.setIdentity(owner);
		programLog.setCertificationProgram(program);
		programLog.setDoer(doer);
		dbInstance.getCurrentEntityManager().persist(programLog);
		return programLog;
	}
	
	public static CertificationProgramLogAction actionFrom(CertificationProgramMailConfiguration mailConfiguration) {
		return mailConfiguration.getType().logAction();
	}
	
	public List<Identity> loadLogsIdentities(CertificationProgram program) {
		String query = """
				select distinct ident from certificationprogramlog as log
				inner join log.identity as ident
				inner join fetch ident.user as identUser
				where log.certificationProgram.key=:programKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, Identity.class)
				.setParameter("programKey", program.getKey())
				.getResultList();
	}
	
	public List<Identity> loadLogsDoers(CertificationProgram program) {
		String query = """
				select distinct doerIdent from certificationprogramlog as log
				inner join log.doer as doerIdent
				inner join fetch doerIdent.user as doerUser
				where log.certificationProgram.key=:programKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, Identity.class)
				.setParameter("programKey", program.getKey())
				.getResultList();
	}
	
	public List<CertificationProgramLog> loadLogs(CertificationProgramLogSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select log from certificationprogramlog as log")
		  .append(" left join fetch log.certificate as cert")
		  .append(" left join fetch log.doer as doerIdent")
		  .append(" left join fetch doerIdent.user as doerUser")
		  .append(" left join fetch log.identity as ident")
		  .append(" left join fetch ident.user as identUser")
		  .append(" left join fetch log.curriculumElement as element")
		  .append(" inner join fetch log.certificationProgram as certprogram");
		
		if(searchParams.getCertificationProgram() != null) {
			sb.and().append("log.certificationProgram.key=:programKey");
		}
		if(searchParams.getDateRange() != null) {
			sb.and().append("log.creationDate>=:startDate and log.creationDate<=:endDate");
		}
		
		sb.append(" order by log.creationDate desc");
	
		TypedQuery<CertificationProgramLog> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), CertificationProgramLog.class);
		if(searchParams.getCertificationProgram() != null) {
			query.setParameter("programKey", searchParams.getCertificationProgram().getKey());
		}
		if(searchParams.getDateRange() != null) {
			query.setParameter("startDate", searchParams.getDateRange().from());
			query.setParameter("endDate", searchParams.getDateRange().to());
		}
		return query.getResultList();
	}
}
