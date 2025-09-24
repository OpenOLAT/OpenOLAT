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
package org.olat.course.certificate.manager;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificatesDAO {
	
	@Autowired
	private DB dbInstance;
	
	public long certificationCount(IdentityRef identity, CertificationProgram certificationProgram) {
		String query = """
				select max(cer.recertificationCount) from certificate cer
				where cer.identity.key=:identityKey and cer.certificationProgram.key=:certificationProgramKey
				group by cer.identity.key, cer.certificationProgram.key""";
		List<Long> counter = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("certificationProgramKey", certificationProgram.getKey())
				.getResultList();
		return counter == null || counter.isEmpty() || counter.get(0) == null ? 0l : counter.get(0).longValue();
	}
	
	public List<Certificate> getCertificates(IdentityRef identity, CertificationProgram certificationProgram) {
		String query = """
				select cer from certificate cer
				inner join fetch cer.identity ident
				inner join fetch ident.user identUser
				where ident.key=:identityKey and cer.certificationProgram.key=:certificationProgramKey
				order by cer.last, cer.creationDate desc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Certificate.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("certificationProgramKey", certificationProgram.getKey())
				.getResultList();
	}
	
	public Certificate getLastCertificate(IdentityRef identity, CertificationProgram certificationProgram) {
		String query = """
				select cer from certificate cer
				inner join fetch cer.identity ident
				inner join fetch ident.user identUser
				where cer.identity.key=:identityKey and cer.certificationProgram.key=:certificationProgramKey
				and cer.last=true
				order by cer.key desc""";
		List<Certificate> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(query, Certificate.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("certificationProgramKey", certificationProgram.getKey())
				.getResultList();
		return certificates != null && !certificates.isEmpty() ? certificates.get(0) : null;
	}
	
	public Certificate pauseCertificate(Certificate certificate) {
		if(certificate instanceof CertificateImpl cert) {
			cert.setRecertificationPaused(true);
			certificate = updateCertificate(cert);
		}
		return certificate;
	}
	
	public Certificate continueCertificate(Certificate certificate) {
		if(certificate instanceof CertificateImpl cert) {
			cert.setRecertificationPaused(false);
			certificate = updateCertificate(cert);
		}
		return certificate;
	}
	
	public void removeLastFlag(IdentityRef identity, CertificationProgramRef program) {
		String query = """
				update certificate cer set cer.last=false
				where cer.certificationProgram.key=:programKey and cer.identity.key=:identityKey""";
		
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("programKey", program.getKey())
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public void removeLastFlag(IdentityRef identity, Long resourceKey) {
		String query = """
				update certificate cer set cer.last=false
				where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey""";
		
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public Certificate updateCertificate(Certificate certificate) {
		return dbInstance.getCurrentEntityManager().merge(certificate);
	}

}
