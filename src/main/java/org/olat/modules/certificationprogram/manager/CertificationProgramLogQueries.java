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

import org.olat.core.commons.persistence.DB;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Calculate the certificates which expired without log entry...
 * 
 * 
 * Initial date: 18 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramLogQueries {
	
	@Autowired
	private DB dbInstance;
	
	public List<Certificate> getRemovedCertificates(CertificationProgram program, Date referenceDate) {
		String query = """
				select cer from certificate as cer
				inner join fetch cer.certificationProgram program
				where cer.last=true and program.key=:programKey
				and cer.nextRecertificationDate<:from and (cer.recertificationWindowDate is null or cer.recertificationWindowDate<:from)
				and not exists (select log.key from certificationprogramlog as log
				  where log.certificate.key=cer.key and log.action=:action
				)
				""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Certificate.class)
				.setParameter("action", CertificationProgramLogAction.remove_membership)
				.setParameter("programKey", program.getKey())
				.setParameter("from", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Certificate> getExpiredCertificates(CertificationProgram program, Date referenceDate) {
		String query = """
				select cer from certificate as cer
				inner join fetch cer.certificationProgram program
				where cer.last=true and program.key=:programKey
				and cer.nextRecertificationDate<:from
				and not exists (select log.key from certificationprogramlog as log
				  where log.certificate.key=cer.key and log.action=:action
				)
				""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Certificate.class)
				.setParameter("action", CertificationProgramLogAction.expire_certificate)
				.setParameter("programKey", program.getKey())
				.setParameter("from", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
}
