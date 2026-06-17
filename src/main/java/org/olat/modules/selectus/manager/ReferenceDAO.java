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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.references.ReferenceImpl;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("referenceDao")
public class ReferenceDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private ReferenceCommentDAO commentDao;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public Reference createReference(String title, String firstName, String lastName, String institution, String email, Date deadline,
			ReferenceType type, ReferenceRequestStatus requestStatus, String adminNote, Application application) {
		ReferenceImpl reference = new ReferenceImpl();
		reference.setCreationDate(new Date());
		reference.setLastModified(reference.getCreationDate());
		reference.setTitle(title);
		reference.setFirstName(firstName);
		reference.setLastName(lastName);
		reference.setInstitution(institution);
		reference.setEmail(email);
		reference.setSubmissionUrl(UUID.randomUUID().toString().toLowerCase());
		reference.setSubmissionDeadline(deadline);
		reference.setApplication(application);
		reference.setReferenceType(type);
		reference.setRequestStatus(requestStatus);
		reference.setAdminNote(adminNote);
		if(requestStatus == ReferenceRequestStatus.accepted || requestStatus == ReferenceRequestStatus.declined) {
			reference.setDateConsent(new Date());
		}
		reference.setReferenceStatus(ReferenceStatus.notSent);
		dbInstance.getCurrentEntityManager().persist(reference);
		return reference;
	}
	
	public Reference updateReference(Reference reference) {
		((ReferenceImpl)reference).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(reference);
	}
	
	public Reference deleteAttachment(Reference reference, Attachment attachment) {
		if(reference.getKey() == null || attachment == null) return reference;
		
		Reference reloadedReference = dbInstance.getCurrentEntityManager().find(ReferenceImpl.class, reference.getKey());
		Attachment reloadedAttachment = dbInstance.getCurrentEntityManager().find(AttachmentImpl.class, attachment.getKey());
		reloadedReference.setLetter(null);
		reloadedReference = dbInstance.getCurrentEntityManager().merge(reloadedReference);
		dbInstance.getCurrentEntityManager().remove(reloadedAttachment);
		return reloadedReference;
	}
	
	public Reference loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref from rreference ref where ref.key=:referenceKey");
		
		List<Reference> references = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Reference.class)
				.setParameter("referenceKey", key)
				.getResultList();
		return references.isEmpty() ? null : references.get(0);
	}
	
	public Reference loadBySubmissionUrl(String url) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref from rreference ref ")
		  .append(" left join fetch ref.application app")
		  .append(" left join fetch app.position position")
		  .append(" where ref.submissionUrl=:url");
		
		List<Reference> references = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Reference.class)
				.setParameter("url", url)
				.getResultList();
		return references.isEmpty() ? null : references.get(0);
	}
	
	public List<Reference> getReferences(Application application, ReferenceType type) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ref from rreference ref")
		  .append(" left join rreferencetoapp as reftoapp on (ref.key=reftoapp.reference.key)")
		  .and().append(" (ref.application.key=:applicationKey");
		if(type == null || type == ReferenceType.comparativeAssessmentExpert) {
			sb.append(" or reftoapp.application.key=:applicationKey");
		}
		sb.append(")");
		if(type != null) {
			sb.and().append("ref.type=:referenceType");
		}
		
		TypedQuery<Reference> references = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Reference.class)
				.setParameter("applicationKey", application.getKey());
		if(type != null) {
			references.setParameter("referenceType", type.name());
		}
		List<Reference> list = references.getResultList();
		return new ArrayList<>(new HashSet<>(list));
	}
	
	/**
	 * The list of references of the specified position. The references returned
	 * has a valid application.
	 * 
	 * @param position The position
	 * @param type The type (null means all types)
	 * @param onlyWithLetter true restricts to reference with a letter saved
	 * @return
	 */
	public List<Reference> getReferences(Position position, ReferenceType type, boolean onlyWithLetter) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref from rreference ref");
		if(type == ReferenceType.expert || type == ReferenceType.recommendation) {
			sb.append(" inner join fetch ref.application app");
		} else {
			sb.append(" left join fetch ref.application app")
			  .append(" left join rreferencetoapp as reftoapp on (ref.key=reftoapp.reference.key)")
			  .append(" left join reftoapp.application appRef");
		}
		if(onlyWithLetter) {
			sb.append(" inner join fetch ref.letter letter");
		}
		sb.append(" where ((app.valid=true and app.position.key=:positionKey)");
		if(type != ReferenceType.expert && type != ReferenceType.recommendation) {
			sb.append(" or (appRef.valid=true and appRef.position.key=:positionKey)");
		}
		sb.append(")");
		
		if(type != null) {
			sb.append(" and ref.type=:referenceType");
		}
		
		TypedQuery<Reference> references = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Reference.class)
				.setParameter("positionKey", position.getKey());
		if(type != null) {
			references.setParameter("referenceType", type.name());
		}
		List<Reference> list = references.getResultList();
		return new ArrayList<>(new HashSet<>(list));
	}
	
	public boolean hasReferenceWithEmail(Application app, Reference reference, String email) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref.key from rreference ref")
		  .append(" where ref.application.key=:applicationKey")
		  .append(" and lower(ref.email)=:email");
		if(reference != null) {
			sb.append(" and ref.key<>:referenceKey");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("applicationKey", app.getKey())
				.setParameter("email", email.toLowerCase());
		if(reference != null) {
			query.setParameter("referenceKey", reference.getKey());
		}
		
		List<Long> emails = query.getResultList();
		return emails != null && !emails.isEmpty() && emails.get(0) != null && emails.get(0).longValue() > 0;
	}
	
	public List<Reference> getReferences(ReferenceSearchParameters params) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select ref from rreference ref")
		  .append(" left join fetch ref.application app")
		  .append(" left join fetch app.position pos")
		  .append(" left join rreferencetoapp as reftoapp on (ref.key=reftoapp.reference.key)")
		  .append(" left join reftoapp.application appRef")
		  .append(" where ((app.key is not null and app.valid=true");
		
		if(params.getApplications() != null && !params.getApplications().isEmpty()) {
			sb.append(" and app.key in :applicationKeys");
		}
		if(params.getPosition() != null) {
			sb.append(" and pos.key=:positionKey");
		}
		sb.append(") or (appRef.key is not null and appRef.valid=true");
		
		if(params.getApplications() != null && !params.getApplications().isEmpty()) {
			sb.append(" and appRef.key in :applicationKeys");
		}
		if(params.getPosition() != null) {
			sb.append(" and appRef.position.key=:positionKey");
		}
		sb.append("))");
		
		if(params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.append(" and ref.status in (:referenceStatus)");
		}
		if(params.getTypes() != null && !params.getTypes().isEmpty()) {
			sb.append(" and ref.type in (:referenceTypes)");
		}

		TypedQuery<Reference> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Reference.class);
		
		if(params.getStatus() != null && !params.getStatus().isEmpty()) {
			List<String> status = params.getStatus().stream()
					.map(ReferenceStatus::name).collect(Collectors.toList());
			query.setParameter("referenceStatus", status);
		}
		if(params.getTypes() != null && !params.getTypes().isEmpty()) {
			List<String> types = params.getTypes().stream()
					.map(ReferenceType::name).collect(Collectors.toList());
			query.setParameter("referenceTypes", types);
		}
		if(params.getApplications() != null && !params.getApplications().isEmpty()) {
			List<Long> applicationKeys = params.getApplications().stream()
					.map(ApplicationRef::getKey).collect(Collectors.toList());
			query.setParameter("applicationKeys", applicationKeys);
		}
		if(params.getPosition() != null) {
			query.setParameter("positionKey", params.getPosition().getKey());
		}
		
		List<Reference> refs = query.getResultList();
		return new ArrayList<>(new HashSet<>(refs));
	}
	
	public boolean hasReferences(Position position, ReferenceType type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref.key from rreference ref")
		  .append(" inner join ref.application app")
		  .append(" inner join ref.letter letter")
		  .append(" where app.valid=true and app.position.key=:positionKey");
		if(type != null) {
			sb.append(" and ref.type=:referenceType");
		}
		
		TypedQuery<Long> references = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("positionKey", position.getKey());
		if(type != null) {
			references.setParameter("referenceType", type.name());
		}
		List<Long> referenceKeys = references
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return referenceKeys != null && !referenceKeys.isEmpty() && referenceKeys.get(0) >= 0;
	}
	
	public void deleteReference(Reference reference) {
		Reference reloadReference = dbInstance.getCurrentEntityManager().getReference(ReferenceImpl.class, reference.getKey());
		if(recruitingModule.isAttachmenOnFileSystem()) {
			applicationDao.removeAttachmentDatas(reloadReference.getLetter());
		}
		dbInstance.getCurrentEntityManager().remove(reloadReference);
	}
	
	public void deleteReferences(Application application) {
		List<Reference> referencesToDelete = getReferences(application, null);
		for(Reference reference:referencesToDelete) {
			if(recruitingModule.isAttachmenOnFileSystem()) {
				applicationDao.removeAttachmentDatas(reference.getLetter());
			}
			commentDao.deleteComments(reference);
			dbInstance.getCurrentEntityManager().remove(reference);
		}
	}
}
