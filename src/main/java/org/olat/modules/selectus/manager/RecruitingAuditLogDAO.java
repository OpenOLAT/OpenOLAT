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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignment;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationAttributeImpl;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionImpl;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.assignment.ApplicationAssignmentImpl;
import org.olat.modules.selectus.model.feedback.ApplicationFeedbackImpl;
import org.olat.modules.selectus.model.feedback.ApplicationsFeedbackConfigurationImpl;
import org.olat.modules.selectus.model.feedback.PublicFeedbackImpl;
import org.olat.modules.selectus.model.log.NotificationPermission;
import org.olat.modules.selectus.model.log.PositionNotificationsPermissions;
import org.olat.modules.selectus.model.log.RecruitingAuditLogImpl;
import org.olat.modules.selectus.model.log.RecruitingAuditLogSearchParameters;
import org.olat.modules.selectus.model.references.ReferenceImpl;
import org.olat.modules.selectus.model.review.ApplicationCommentImpl;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionReviewDefinitionImpl;
import org.olat.modules.selectus.model.review.ReviewElementDefinitionImpl;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.model.review.ReviewResponseImpl;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

/**
 * 
 * Initial date: 20 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RecruitingAuditLogDAO {
	
	private static final Logger log = Tracing.createLoggerFor(RecruitingAuditLogDAO.class);
	
	private static final XStream positionXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(positionXStream);
		positionXStream.ignoreUnknownElements();
		
		positionXStream.alias("position", PositionImpl.class);
		positionXStream.omitField(PositionImpl.class, "committeeGroup");
		positionXStream.omitField(PositionImpl.class, "committeeHeadGroup");
		positionXStream.omitField(PositionImpl.class, "secretaryGroup");
		positionXStream.omitField(PositionImpl.class, "exOfficioGroup");
		positionXStream.omitField(PositionImpl.class, "reviewDefinition");
		
		positionXStream.omitField(OrganisationImpl.class, "creationDate");
		positionXStream.omitField(OrganisationImpl.class, "lastModified");
		positionXStream.omitField(OrganisationImpl.class, "group");
		positionXStream.omitField(OrganisationImpl.class, "type");
		positionXStream.omitField(OrganisationImpl.class, "root");
		positionXStream.omitField(OrganisationImpl.class, "parent");
		positionXStream.omitField(OrganisationImpl.class, "children");
		
		positionXStream.omitField(ApplicationAttributeImpl.class, "position");
		positionXStream.omitField(ApplicationAttributeImpl.class, "application");
		positionXStream.omitField(ApplicationAttributeImpl.class, "definition");
		
		positionXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}

	private static final XStream reviewDefinitionsXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(reviewDefinitionsXStream);
		reviewDefinitionsXStream.ignoreUnknownElements();
		reviewDefinitionsXStream.omitField(PositionReviewDefinitionImpl.class, "lastModified");
		
		reviewDefinitionsXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream applicationXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(applicationXStream);
		applicationXStream.ignoreUnknownElements();
		applicationXStream.omitField(ApplicationImpl.class, "lastModified");
		applicationXStream.omitField(ApplicationImpl.class, "position");
		applicationXStream.omitField(ApplicationAttributeImpl.class, "definition");
		applicationXStream.omitField(ApplicationAttributeImpl.class, "application");
		applicationXStream.omitField(ApplicationAttributeImpl.class, "position");
		
		applicationXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream identityXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(identityXStream);
		identityXStream.ignoreUnknownElements();
		identityXStream.omitField(IdentityImpl.class, "lastModified");
		identityXStream.omitField(IdentityImpl.class, "version");
		identityXStream.omitField(UserImpl.class, "version");
		
		identityXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream referenceXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(referenceXStream);
		referenceXStream.ignoreUnknownElements();
		referenceXStream.omitField(ReferenceImpl.class, "lastModified");
		referenceXStream.omitField(ReferenceImpl.class, "application");
		referenceXStream.omitField(ReferenceImpl.class, "attachment");
		referenceXStream.omitField(ReferenceImpl.class, "version");
		referenceXStream.omitField(AttachmentImpl.class, "version");
		
		referenceXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream reviewXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(reviewXStream);
		reviewXStream.ignoreUnknownElements();
		reviewXStream.omitField(ReviewResponseImpl.class, "lastModified");
		reviewXStream.omitField(ReviewResponseImpl.class, "application");
		reviewXStream.omitField(ReviewResponseImpl.class, "attachment");
		reviewXStream.omitField(ReviewResponseImpl.class, "reviewer");
		reviewXStream.omitField(ReviewElementDefinitionImpl.class, "creationDate");
		reviewXStream.omitField(ReviewElementDefinitionImpl.class, "lastModified");
		reviewXStream.omitField(ReviewElementDefinitionImpl.class, "positionReviewDefinition");
		
		reviewXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream commentXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(commentXStream);
		commentXStream.ignoreUnknownElements();
		commentXStream.omitField(ApplicationCommentImpl.class, "lastModified");
		commentXStream.omitField(ApplicationCommentImpl.class, "author");
		commentXStream.omitField(ApplicationCommentImpl.class, "reviewer");
		commentXStream.omitField(ApplicationCommentImpl.class, "application");
		commentXStream.omitField(ApplicationCommentImpl.class, "parentComment");
		
		commentXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream assignmentXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(assignmentXStream);
		assignmentXStream.ignoreUnknownElements();
		assignmentXStream.omitField(ApplicationAssignmentImpl.class, "assignee");
		assignmentXStream.omitField(ApplicationAssignmentImpl.class, "application");
		
		assignmentXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream publicFeedbackXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(publicFeedbackXStream);
		publicFeedbackXStream.ignoreUnknownElements();
		publicFeedbackXStream.omitField(PublicFeedbackImpl.class, "application");
		
		publicFeedbackXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private static final XStream applicationFeedbackXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {

		XStreamHelper.allowDefaultPackage(applicationFeedbackXStream);
		applicationFeedbackXStream.ignoreUnknownElements();
		applicationFeedbackXStream.omitField(ApplicationFeedbackImpl.class, "application");
		applicationFeedbackXStream.omitField(ApplicationsFeedbackConfigurationImpl.class, "position");

		applicationFeedbackXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	@Autowired
	private DB dbInstance;
	
	public String toXml(Position position) {
		if(position == null) return null;
		return positionXStream.toXML(position);
	}
	
	public String toXml(PositionReviewDefinition reviewDefinition) {
		if(reviewDefinition == null) return null;
		return reviewDefinitionsXStream.toXML(reviewDefinition);
	}
	
	public String toXml(Application application) {
		if(application == null) return null;
		return applicationXStream.toXML(application);
	}
	
	public String toXml(Identity identity) {
		if(identity == null) return null;
		return identityXStream.toXML(identity);
	}
	
	public String toXml(Reference reference) {
		if(reference == null) return null;
		return referenceXStream.toXML(reference);
	}
	
	public String toXml(List<ReviewResponse> responses) {
		if(responses == null) return null;
		return reviewXStream.toXML(responses);
	}
	
	public String toXml(ApplicationComment comment) {
		if(comment == null) return null;
		return commentXStream.toXML(comment);
	}
	
	public String toXml(ApplicationAssignment assignment) {
		if(assignment == null) return null;
		return assignmentXStream.toXML(assignment);
	}
	
	public String toXml(ApplicationAssignmentLight assignment) {
		if(assignment == null) return null;
		return assignmentXStream.toXML(assignment);
	}
	
	public String toXml(PublicFeedback feedback) {
		if(feedback == null) return null;
		return publicFeedbackXStream.toXML(feedback);
	}
	
	public String toXml(ApplicationFeedback feedback) {
		if(feedback == null) return null;
		return applicationFeedbackXStream.toXML(feedback);
	}
	
	public RecruitingAuditLog auditLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			String before, String after, String message, String messageI18n, String[] args,
			PositionRef position, ApplicationRef application, Identity committeeMember,
			UserRating userRating, Reference reference, ApplicationFeedback feedback, ApplicationComment comment,
			Identity doer) {
		RecruitingAuditLogImpl auditLog = new RecruitingAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action.name());
		auditLog.setActionTarget(target.name());
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		if(message.length() >= 2000) {
			message = message.substring(0, 1990);
			log.error("Audit message too long: {}", message);
		}
		auditLog.setMessage(message);
		auditLog.setMessageI18n(messageI18n);
		if(args != null) {
			if(args.length > 0) {
				auditLog.setMessageValue1(args[0]);
			}
			if(args.length > 1) {
				auditLog.setMessageValue2(args[1]);
			}
			if(args.length > 2) {
				auditLog.setMessageValue3(args[2]);
			}
			if(args.length > 3) {
				auditLog.setMessageValue4(args[3]);
			}
			if(args.length > 4) {
				auditLog.setMessageValue5(args[4]);
			}
		}
		if(position != null) {
			auditLog.setPositionKey(position.getKey());
		}
		if(application != null) {
			auditLog.setApplicationKey(application.getKey());
		}
		if(committeeMember != null) {
			auditLog.setCommitteeIdentityKey(committeeMember.getKey());
		}
		if(userRating != null) {
			auditLog.setRatingKey(userRating.getKey());
		}
		if(reference != null) {
			auditLog.setReferenceKey(reference.getKey());
		}
		if(feedback != null) {
			auditLog.setFeedbackKey(feedback.getKey());
		}
		if(comment != null) {
			auditLog.setCommentKey(comment.getKey());
		}

		if(doer != null) {
			auditLog.setIdentity(doer);
		}
		dbInstance.getCurrentEntityManager().persist(auditLog);
		return auditLog;
	}
	
	public RecruitingAuditLog getReference(Long key) {
		return dbInstance.getCurrentEntityManager()
				.getReference(RecruitingAuditLogImpl.class, key);
	}
	
	public int countPositionLogs(IdentityRef identity, RecruitingAuditLogSearchParameters params) {
		TypedQuery<Number> query = getPositionLogs(identity, params, Number.class);
		List<Number> counts = query.getResultList();
		return counts == null || counts.isEmpty() || counts.get(0) == null ? 0 : counts.get(0).intValue();
	}
		
	public List<RecruitingAuditLog> getPositionLogs(IdentityRef identity, RecruitingAuditLogSearchParameters params) {
		TypedQuery<RecruitingAuditLog> query = getPositionLogs(identity, params, RecruitingAuditLog.class);
		return query.getResultList();
	}
	
	public List<RecruitingAuditLogLight> getPositionLightLogs(IdentityRef identity, RecruitingAuditLogSearchParameters params) {
		TypedQuery<RecruitingAuditLogLight> query = getPositionLogs(identity, params, RecruitingAuditLogLight.class);
		return query.getResultList();
	}
	
	public <U> TypedQuery<U> getPositionLogs(IdentityRef identity, RecruitingAuditLogSearchParameters params, Class<U> type) {
		boolean count = Number.class.equals(type);
		QueryBuilder sb = new QueryBuilder(10000);
		if(count) {
			sb.append("select count(distinct log.key) from recruitingauditlog as log");
		} else if(RecruitingAuditLogLight.class.equals(type)) {
			sb.append("select log from recruitingauditloglight as log");
		} else {
			sb.append("select log from recruitingauditlog as log");
		}
		if(params.getPosition() != null) {
			sb.and().append(" log.positionKey=:positionKey");
		}

		if(params.getPermittedPositions() != null && !params.getPermittedPositions().isEmpty()) {
			sb.and().append(" (");

			List<PositionNotificationsPermissions> permissions = params.getPermittedPositions();
			for(int i=0; i<permissions.size(); i++) {
				if(i != 0) {
					sb.append(" or ");
				}
				sb.append(" (log.positionKey in (:positionKeys_").append(i).append(") and (");
				
				NotificationPermission[] not = permissions.get(i).getPermissions();
				for(int j=0; j<not.length; j++) {
					if(j != 0) {
						sb.append(" or ");
					}
					
					NotificationPermission notificationPermission = not[j];
					sb.append("(log.actionTarget='").append(notificationPermission.getTarget().name())
					  .append("' and log.action='").append(notificationPermission.getAction().name()).append("')");
				}
				sb.append("))");
			}
			sb.append(")");	
		}
		
		if(params.isOrganisation()) {
			// Limit to the organisation of the position
			sb.and()
			  .append(" exists (select rMembership.key from rposition as pos")
			  .append("  inner join pos.organisation as rorg")
			  .append("  inner join rorg.group rGroup")
			  .append("  inner join rGroup.members rMembership")
			  .append("  where pos.key=log.positionKey and rMembership.identity.key=:readerKey")
			  .append(")");
		}
		
		if(params.getPermittedTargets() != null && params.getPermittedTargets().length > 0) {
			sb.and().append(" log.actionTarget ").in(params.getPermittedTargets());
		}
		
		if(params.getApplication() != null) {
			sb.and().append(" log.applicationKey=:applicationKey");
		}
		if(params.getFrom() != null) {
			sb.and().append(" log.creationDate>=:from");
		}
		if(params.getUntil() != null) {
			sb.and().append(" log.creationDate<=:until");
		}
		if(params.getTarget() != null) {
			sb.and().append(" log.actionTarget=:target");
		}
		if(params.isUnreadOnly()) {
			sb.and()
			  .append(" not exists (select read from recruitingauditlogread as read")
			  .append("  where log.key=read.auditLog.key and read.identity.key=:readerKey)");
		}

		TypedQuery<U> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if(params.getPosition() != null) {
			query.setParameter("positionKey", params.getPosition().getKey());
		}
		if(params.getPermittedPositions() != null && !params.getPermittedPositions().isEmpty()) {
			List<PositionNotificationsPermissions> permissions = params.getPermittedPositions();
			for(int i=0; i<permissions.size(); i++) {
				query.setParameter("positionKeys_" + i, permissions.get(i).getPositionKeys());	
			}
		}
		if(params.getApplication() != null) {
			query.setParameter("applicationKey", params.getApplication().getKey());
		}
		if(params.getFrom() != null) {
			query.setParameter("from", params.getFrom(), TemporalType.TIMESTAMP);
		}
		if(params.getUntil() != null) {
			query.setParameter("until", params.getUntil(), TemporalType.TIMESTAMP);
		}
		if(params.getTarget() != null) {
			query.setParameter("target", params.getTarget().name());
		}
		if(params.isUnreadOnly() || params.isOrganisation()) {
			query.setParameter("readerKey", identity.getKey());
		}
		return query;
	}
	
	public int delete(PositionRef position) {
		String del = "delete recruitingauditlog as log where log.positionKey=:positionKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(del)
				.setParameter("positionKey", position.getKey())
				.executeUpdate();
	}
}
