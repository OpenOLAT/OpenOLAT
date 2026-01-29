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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateWithInfos;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.resource.OLATResource;
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
	
	public CertificateImpl getCertificateById(Long key) {
		String query = """
				select cer from certificate cer
				inner join fetch cer.identity ident
				inner join fetch ident.user identUser
				left join fetch cer.certificationProgram program
				left join fetch cer.olatResource resource
				where cer.key=:certificateKey""";
		List<CertificateImpl> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(query, CertificateImpl.class)
				.setParameter("certificateKey", key)
				.getResultList();
		return certificates.isEmpty() ? null : certificates.get(0);
	}
	
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
				left join fetch cer.certificationProgram as program
				where ident.key=:identityKey and program.key=:certificationProgramKey
				order by cer.last, cer.key desc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Certificate.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("certificationProgramKey", certificationProgram.getKey())
				.getResultList();
	}
	
	public Certificate getLastCertificate(IdentityRef identity, Long resourceKey) {
		String query = """
				select cer from certificate cer
				inner join fetch cer.identity ident
				inner join fetch ident.user identUser
				where (cer.olatResource.key=:resourceKey or cer.archivedResourceKey=:resourceKey or cer.key=:resourceKey)
				and cer.identity.key=:identityKey and cer.last=true order by cer.creationDate""";
		List<Certificate> certififcates = dbInstance.getCurrentEntityManager()
				.createQuery(query, Certificate.class)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.setMaxResults(1)
				.getResultList();
		return certififcates.isEmpty() ? null : certififcates.get(0);
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
	
	public List<CertificateLight> getLastCertificates(IdentityRef identity) {
		String query = """
				select cer from certificatelight cer
				where cer.identityKey=:identityKey and cer.last=true""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CertificateLight.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Certificate> getCertificates(IdentityRef identity, OLATResource resource,
			String externalId, Boolean managedOnly, Boolean lastOnly) {
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select cer from certificate cer")
		  .append(" inner join fetch cer.identity ident")
		  .append(" inner join fetch ident.user identUser");
		if(identity != null) {
			sb.and().append("ident.key=:identityKey");
		}
		if(resource != null) {
			sb.and().append("cer.olatResource.key=:resourceKey");
		}
		if(lastOnly != null && lastOnly.booleanValue()) {
			sb.and().append("cer.last=true");
		}
		
		if(StringHelper.containsNonWhitespace(externalId)) {
			sb.and().append("cer.externalId=:externalId");
		} else if(managedOnly != null) {
			if(managedOnly.booleanValue()) {
				sb.and().append("cer.externalId is not null");
			} else {
				sb.and().append("cer.externalId is null");
			}
		}
		
		sb.append(" order by cer.creationDate desc");
		TypedQuery<Certificate> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class);
		if(identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		if(resource != null) {
			query.setParameter("resourceKey", resource.getKey());
		}
		if(StringHelper.containsNonWhitespace(externalId)) {
			query.setParameter("externalId", externalId);
		}
		return query.getResultList();
	}
	
	/**
	 * 
	 * @param identity The identity
	 * @return
	 */
	public List<CertificateWithInfos> getCertificatesWithInfos(IdentityRef identity) {
		String query = """
				select new CertificateWithInfos(cer, course, certificateConfig,
				 (select count(issuedCertificate.key) from certificate as issuedCertificate
				  where issuedCertificate.olatResource.key=cer.olatResource.key and issuedCertificate.identity.key=cer.identity.key
				 ) as issuedCertificates
				) from certificate as cer
				left join fetch cer.certificationProgram as program
				left join fetch cer.olatResource as ores
				left join fetch repositoryentry as course on (course.olatResource.key=ores.key)
				left join fetch certificateentryconfig as certificateConfig on (certificateConfig.entry.key=course.key)
				left join fetch cer.uploadedBy as uploadedByIdent
				left join fetch uploadedByIdent.user as uploadedByUser
				where cer.identity.key=:identityKey and (cer.last=true or cer.statusString=:revokedStatus)
				order by cer.key desc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CertificateWithInfos.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("revokedStatus", CertificateStatus.revoked.name())
				.getResultList();
	}
	
	public int removeLastFlag(IdentityRef identity, CertificationProgramRef program) {
		String query = """
				update certificate cer set cer.last=false,cer.removalDate=:removalDate
				where cer.last=true and cer.certificationProgram.key=:programKey and cer.identity.key=:identityKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("programKey", program.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("removalDate", new Date(), TemporalType.TIMESTAMP)
				.executeUpdate();
	}
	
	public void removeLastFlag(IdentityRef identity, Long resourceKey) {
		String query = """
				update certificate cer set cer.last=false
				where cer.identity.key=:identityKey and cer.olatResource.key=:resourceKey and cer.last=true""";
		
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public Certificate updateCertificate(Certificate certificate) {
		return dbInstance.getCurrentEntityManager().merge(certificate);
	}
}