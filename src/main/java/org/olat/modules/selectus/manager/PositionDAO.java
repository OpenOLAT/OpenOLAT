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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingDuplicateApplicationAlgorithm;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.AcceptPolicyImpl;
import org.olat.modules.selectus.model.ApplicationAttributeLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.CommitteeMembershipSummary;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionImpl;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionImpl;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.model.attributes.AttributeConfiguration;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.position.PositionLightWithMembership;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("positionDAO")
public class PositionDAO {
	
	private static final Logger log = Tracing.createLoggerFor(PositionDAO.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private RecruitingModule recruitingModule;

	public Position createPosition(String refereeDocs, String expertDocs, Organisation organisation) {
		PositionImpl position = new PositionImpl();
		position.setCreationDate(new Date());
		position.setStatus(PositionStatus.preparation.name());
		position.setCommitteeGroup(securityGroupDao.createAndPersistSecurityGroup());
		position.setCommitteeHeadGroup(securityGroupDao.createAndPersistSecurityGroup());
		position.setSecretaryGroup(securityGroupDao.createAndPersistSecurityGroup());
		position.setExOfficioGroup(securityGroupDao.createAndPersistSecurityGroup());
		position.setRefereeRecommendationDocs(refereeDocs);
		position.setExpertRecommendationDocs(expertDocs);
		position.setExcludedAttributesList(recruitingModule.getNewPositionExcludedAttributesList());
		position.setAdvertised(recruitingModule.isAdvertisementDefaultEnabled());
		
		boolean project = recruitingModule.isApplicationProjectEnabled() && recruitingModule.isApplicationProjectEnabledDefault();
		position.setApplicationProject(project);
		position.setApplicationAcademicalBackground(true);
		
		position.setOrganisation(organisation);

		Position lastCreatedPosition = findLastPosition();
		if(lastCreatedPosition != null) {
			if(lastCreatedPosition.getPolicyLink1() != null) {
				position.setPolicyLink1(lastCreatedPosition.getPolicyLink1().clone());
			}
			if(lastCreatedPosition.getPolicyLink2() != null) {
				position.setPolicyLink1(lastCreatedPosition.getPolicyLink2().clone());
			}
			if(lastCreatedPosition.getPolicyLink3() != null) {
				position.setPolicyLink1(lastCreatedPosition.getPolicyLink3().clone());
			}
			if(lastCreatedPosition.getPolicyLink4() != null) {
				position.setPolicyLink1(lastCreatedPosition.getPolicyLink4().clone());
			}
		}
		return position;
	}
	
	public PositionAttributeDefinition createAttributeDefinitionAndPersist(Position position, PositionApplicationAttributeTabEnum tab,
			PositionAttributeDefinitionTypeEnum attributeType, String label, String labelDe, String labelFr, boolean mandatory,
			String placeholder, String placeholderDe) {
		PositionAttributeDefinition definition = createAttributeDefinition(position, tab, attributeType, label, labelDe, labelFr, mandatory, placeholder, placeholderDe);
		dbInstance.getCurrentEntityManager().persist(definition);
		return definition;
	}
	
	public PositionAttributeDefinition createAttributeDefinition(Position position, PositionApplicationAttributeTabEnum tab,
			PositionAttributeDefinitionTypeEnum attributeType, String label, String labelDe, String labelFr, boolean mandatory,
			String placeholder, String placeholderDe) {
		PositionAttributeDefinitionImpl attr = new PositionAttributeDefinitionImpl();
		attr.setCreationDate(new Date());
		attr.setLastModified(attr.getCreationDate());
		attr.setOrderPosition(Integer.valueOf(0));
		attr.setLabel(label);
		attr.setLabelDe(labelDe);
		attr.setLabelFr(labelFr);
		attr.setMandatory(mandatory);
		attr.setPlaceholder(placeholder);
		attr.setPlaceholderDe(placeholderDe);
		attr.setTab(tab.name());
		attr.setPosition(position);
		attr.setTypeEnum(attributeType);
		return attr;
	}
	
	protected Position findLastPosition() {
		List<Position> positions = dbInstance.getCurrentEntityManager()
				.createNamedQuery("positionOrderedByCreationDate", Position.class)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return positions.isEmpty() ? null : positions.get(0);
	}

	public Position savePosition(Position position) {
		if(position.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(position);
		} else {
			position = dbInstance.getCurrentEntityManager().merge(position);
		}
		//reload unit
		if(position.getOrganisation() != null) {
			position.getOrganisation().getDisplayName();
		}
		position.getAttributesDefinitions().size();
		return position;
	}
	
	public List<PositionAttributeDefinition>  getGlobalAttributeDefinition() {
		StringBuilder sb = new StringBuilder();
		sb.append("select def from posattributedefinition as def")
		  .append(" where def.position.key is null and def.tab=:tab");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PositionAttributeDefinition.class)
				.setParameter("tab", PositionApplicationAttributeTabEnum.global.name())
				.getResultList();
	}
	
	public List<ApplicationAttributeLight> getGlobalAttributes() {
		StringBuilder sb = new StringBuilder();
		sb.append("select attr from appattributelight as attr")
		  .append(" where attr.positionKey is not null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationAttributeLight.class)
				.getResultList();
	}
	
	public PositionAttributeDefinition updateAttributeDefinition(PositionAttributeDefinition attributeDefinition) {
		((PositionAttributeDefinitionImpl)attributeDefinition).setLastModified(new Date());
		if(attributeDefinition.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(attributeDefinition);
		} else {
			attributeDefinition = dbInstance.getCurrentEntityManager().merge(attributeDefinition);
		}
		return attributeDefinition;
	}
	
	public void persistAttributeDefinition(PositionAttributeDefinition attributeDefinition) {
		dbInstance.getCurrentEntityManager().persist(attributeDefinition);
	}
	
	public void deletePositionAttributeDefinition(PositionAttributeDefinition attributeDefinition) {
		PositionAttributeDefinition reloadedDefinition = dbInstance.getCurrentEntityManager()
			.getReference(PositionAttributeDefinitionImpl.class, attributeDefinition.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedDefinition);
	}
	
	public int removeOrganisationUnit(OrganisationUnit unitToRemove) {
		if(unitToRemove == null) return 0;//nothing to do

		String q = "update rposition position set position.organisation.key=null where position.organisation.key=:unitKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("unitKey", unitToRemove.getKey())
			.executeUpdate();
	}
	
	public Position loadPositionByKey(Long key) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select position from rposition position ")
		  .append(" inner join fetch position.committeeGroup committee")
		  .append(" left join fetch position.organisation org")
		  .append(" left join fetch position.reviewDefinition reviewDef")
		  .append(" left join fetch position.committeeHeadGroup committeeHead")
		  .append(" left join fetch position.secretaryGroup committeeSecretary")
		  .append(" left join fetch position.exOfficioGroup exOfficioGroup")
		  .append(" left join fetch position.attributesDefinitions appAttributesDefs")
		  .append(" left join fetch position.attributes positionGlobalAttributes")
		  .append(" left join fetch positionGlobalAttributes.definition positionGlobalAttributesDefs")
		  .append(" where position.key=:key and position.valid=true");
		
		List<Position> positions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Position.class)
				.setParameter("key", key)
				.getResultList();
		return positions.isEmpty() ? null : positions.get(0);
	}
	
	public List<Position> loadPositionByOrganisationUnit(OrganisationUnit unit) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select position from rposition position ")
		  .append(" inner join fetch position.committeeGroup committee")
		  .append(" inner join fetch position.organisation org")
		  .append(" left join fetch position.reviewDefinition reviewDef")
		  .append(" left join fetch position.committeeHeadGroup committeeHead")
		  .append(" left join fetch position.secretaryGroup committeeSecretary")
		  .append(" left join fetch position.exOfficioGroup exOfficioGroup")
		  .append(" where position.valid=true and orgUnit.key=:unitKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Position.class)
				.setParameter("unitKey", unit.getKey())
				.getResultList();
	}
	
	public List<Position> loadPositionsToRemind() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select position from rposition position ")
		  .append(" inner join fetch position.committeeGroup committee")
		  .append(" left join fetch position.organisation org")
		  .append(" left join fetch position.reviewDefinition reviewDef")
		  .append(" left join fetch position.committeeHeadGroup committeeHead")
		  .append(" left join fetch position.secretaryGroup committeeSecretary")
		  .append(" left join fetch position.exOfficioGroup exOfficioGroup")
		  .append(" where position.committeeReminderDate is not null and position.committeeReminderSentDate is null")
		  .append(" and position.committeeReminderDate<:now and position.status in (:status)");
		
		List<String> status = new ArrayList<>();
		status.add(PositionStatus.publishedAndInScreening.name());
		status.add(PositionStatus.closedAndInScreening.name());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Position.class)
				.setParameter("status", status)
				.setParameter("now", new Date())
				.getResultList();
	}
	
	public Date getLastApplicationModification(Position position) {
		if(position.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select max(app.lastModified) from rapplication app where app.position.key=:positionKey and app.valid=true");
		List<Date> dates = dbInstance.getCurrentEntityManager()
				.createNamedQuery("lastApplicationByModificationAndPosition", Date.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
		try {
			return dates.isEmpty() || dates.get(0) == null ? null : dates.get(0);
		} catch (Exception e) {
			log.error("Cannot find last modification date of applications for: " + position, e);
			return null;
		}
	}
	
	public Long getEstimatedSizeOfAttachment(Position position, List<DocumentOption> docOptions) {
		if(position.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for(int i=0; i<docOptions.size(); i++) {
			if(!DocumentEnum.combined.equals(docOptions.get(i).getDoc())) {
				if(i != 0) {
					sb.append(", ");
				}
				sb.append(" sum(").append(docOptions.get(i).getDoc().field()).append(".size)");
			}
		}
		sb.append(" from rapplication app ");
		for(int i=0; i<docOptions.size(); i++) {
			if(!DocumentEnum.combined.equals(docOptions.get(i).getDoc())) {
				String field = docOptions.get(i).getDoc().field();
				sb.append(" left join app.attachments.").append(field).append(" ").append(field);
			}
		}
		sb.append(" where app.position.key=:positionKey and app.valid=true");

		try {
			List<Object[]> sizes = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
					.setParameter("positionKey", position.getKey())
					.getResultList();
			if(sizes == null || sizes.isEmpty()) {
				return null;
			}
			
			long size = 0l;
			for(Object intermadiateSize:sizes.get(0)) {
				if(intermadiateSize != null) {
					size += ((Number)intermadiateSize).intValue();
				}
			}
			return Long.valueOf(size);
		} catch (Exception e) {
			log.error("Cannot find size of attachments for: " + position, e);
			return null;
		}
	}
	
	public long countPositions(List<PositionStatus> status, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(position.key) from rposition position")
		  .append(" inner join position.committeeGroup committee")
		  .append(" where position.valid=:posValid");
		if(status != null && !status.isEmpty()) {
			sb.append(" and position.status in (:status)");
		}
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("posValid", valid);
		if(status != null && !status.isEmpty()) {
			List<String> statusStrings = new ArrayList<>();
			for(PositionStatus state:status) {
				statusStrings.add(state.name());
			}
			query.setParameter("status", statusStrings);
		}
		List<Number> count = query.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0l : count.get(0).longValue();
	}

	public List<Position> findPositions(List<PositionStatus> status, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select position from rposition position")
		  .append(" inner join fetch position.committeeGroup committee")
		  .append(" left join fetch position.committeeHeadGroup committeeHead")
		  .append(" left join fetch position.secretaryGroup committeeSecretary")
		  .append(" left join fetch position.exOfficioGroup exOfficioGroup")
		  .append(" where position.valid=:posValid");
		if(status != null && !status.isEmpty()) {
			sb.append(" and position.status in (:status)");
		}
		TypedQuery<Position> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Position.class)
				.setParameter("posValid", valid);
		if(status != null && !status.isEmpty()) {
			List<String> statusStrings = new ArrayList<>();
			for(PositionStatus state:status) {
				statusStrings.add(state.name());
			}
			query.setParameter("status", statusStrings);
		}
		return query.getResultList();
	}
	
	public CommitteeMembershipsStats getCommitteeMembershipsStats(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select identity.key");
		sb.append(", (select count(secretary.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" secretary, rposition position ")
		  .append("    where secretary.identity.key=:identityKey and secretary.securityGroup.key=position.secretaryGroup.key and position.valid=:posValid) as numOfSecretaries");
		sb.append(", (select count(head.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" head, rposition headPosition ")
		  .append("    where head.identity.key=:identityKey and head.securityGroup.key=headPosition.committeeHeadGroup.key and headPosition.valid=:posValid) as numOfHeads");
		if(recruitingModule.isRoleExOfficioEnabled()) {
			sb.append(", (select count(exofficio.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" exofficio, rposition exofficioPosition ")
			  .append("    where exofficio.identity.key=:identityKey and exofficio.securityGroup.key=exofficioPosition.exOfficioGroup.key and exofficioPosition.valid=:posValid) as numOfExOfficio");
		}
		sb.append(" from ").append(IdentityImpl.class.getName()).append(" identity ")
		  .append(" where identity.key=:identityKey");

		List<Object[]> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("posValid", Boolean.TRUE)
				.getResultList();
		
		if(counts != null && counts.size() == 1) {
			Object[] count = counts.get(0);
			int numAsSecretaries = count[1] == null ? 0 : ((Number)count[1]).intValue();
			int numAsHeads = count[2] == null ? 0 : ((Number)count[2]).intValue();
			int numAsExOfficios = 0;
			if(count.length > 3) {
				numAsExOfficios = count[3] == null ? 0 : ((Number)count[3]).intValue();
			}
			return new CommitteeMembershipsStats(numAsSecretaries, numAsHeads, numAsExOfficios);
		}
		return new CommitteeMembershipsStats(0, 0, 0);
	}
	
	public CommitteeMembershipSummary getCommitteeMembershipsStats(Identity identity, Position position) {
		StringBuilder sb = new StringBuilder();
		sb.append("select identity.key");
		sb.append(", (select count(members.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" members, rposition position ")
		  .append("    where members.identity.key=:identityKey and position.key=:positionKey and members.securityGroup.key=position.committeeGroup.key and position.valid=:posValid) as numOfMembers");
		sb.append(", (select count(secretary.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" secretary, rposition secPosition ")
		  .append("    where secretary.identity.key=:identityKey and secPosition.key=:positionKey and secretary.securityGroup.key=secPosition.secretaryGroup.key and secPosition.valid=:posValid) as numOfSecretaries");
		sb.append(", (select count(head.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" head, rposition headPosition ")
		  .append("    where head.identity.key=:identityKey and headPosition.key=:positionKey and head.securityGroup.key=headPosition.committeeHeadGroup.key and headPosition.valid=:posValid) as numOfHeads");
		if(recruitingModule.isRoleExOfficioEnabled()) {
			sb.append(", (select count(exofficio.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" exofficio, rposition exofficioPosition ")
			  .append("    where exofficio.identity.key=:identityKey and exofficioPosition.key=:positionKey and exofficio.securityGroup.key=exofficioPosition.exOfficioGroup.key and exofficioPosition.valid=:posValid) as numOfExOfficio");
		}
		sb.append(" from ").append(IdentityImpl.class.getName()).append(" identity ")
		  .append(" where identity.key=:identityKey");

		List<Object[]> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("positionKey", position.getKey())
				.setParameter("posValid", Boolean.TRUE)
				.getResultList();
		
		if(counts != null && counts.size() == 1) {
			Object[] count = counts.get(0);
			boolean member = toBoolean(count, 1);
			boolean secretary = toBoolean(count, 2);
			boolean head = toBoolean(count, 3);
			boolean exofficio = toBoolean(count, 4);
			return new CommitteeMembershipSummary(member, secretary, head, exofficio);
		}
		return new CommitteeMembershipSummary(false, false, false, false);
	}
	
	private boolean toBoolean(Object[] count, int index) {
		if(count != null && count.length > index) {
			return count[index] == null ? false : ((Number)count[index]).intValue() > 0;
		}
		return false;
	}
	
	public List<PositionLight> findParallelApplicationsLight(String email, String firstName, String lastName,
			Long referencePositionKey, Long organisationKey, RecruitingDuplicateApplicationAlgorithm algorithm) {
		boolean withNames = algorithm == RecruitingDuplicateApplicationAlgorithm.EMAIL_FIRST_LAST_NAME
				&& StringHelper.containsNonWhitespace(firstName)
				&& StringHelper.containsNonWhitespace(lastName);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select position from rpositionlight position")
		  .append(" where not(position.key=:refPosKey) and exists (select app.key from rapplication as app")
		  .append("  where app.position.key=position.key and app.valid=:valid and lower(app.person.email)=:mail");
		if(withNames) {
			sb.append(" and lower(app.person.firstName)=:firstName and lower(app.person.lastName)=:lastName");
		}
		sb.append(" )");
		if(organisationKey != null) {
			sb.append(" and position.organisation.key=:organisationKey");
		}
		
		TypedQuery<PositionLight> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PositionLight.class)
				.setParameter("mail", email.toLowerCase())
				.setParameter("valid", Boolean.TRUE)
				.setParameter("refPosKey", referencePositionKey);
		if(withNames) {
			query.setParameter("firstName", firstName.toLowerCase());
			query.setParameter("lastName", lastName.toLowerCase());
		}
		if(organisationKey != null) {
			query.setParameter("organisationKey", organisationKey);
		}
		return query.getResultList();
	}
	
	public List<ParallelApplication> findParallelApplications(Long referencePositionKey, Long organisationKey,
			RecruitingDuplicateApplicationAlgorithm algorithm) {
		StringBuilder sb = new StringBuilder();
		sb.append("select app.key, app.person.email, app.person.firstName, app.person.lastName, position from rapplication as app")
		  .append(" inner join rpositionlight position on (app.position.key=position.key)")
		  .append(" where not(position.key=:refPosKey) and app.valid=:valid and exists (select refApp.key from rapplication as refApp")
		  .append("  where refApp.position.key=:refPosKey and refApp.valid=:valid and lower(app.person.email)=lower(refApp.person.email)");
		if(algorithm == RecruitingDuplicateApplicationAlgorithm.EMAIL_FIRST_LAST_NAME) {
			sb.append(" and lower(app.person.firstName)=lower(refApp.person.firstName) and lower(app.person.lastName)=lower(refApp.person.lastName)");
		}
		sb.append(" )");
		if(organisationKey != null) {
			sb.append(" and position.organisation.key=:organisationKey");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("valid", Boolean.TRUE)
				.setParameter("refPosKey", referencePositionKey);
		if(organisationKey != null) {
			query.setParameter("organisationKey", organisationKey);
		}
		
		List<Object[]> rawObjects = query.getResultList();
		List<ParallelApplication> apps = new ArrayList<>(rawObjects.size());
		for(Object[] objects:rawObjects) {
			Long applicationKey = (Long)objects[0];
			String applicationEmail = (String)objects[1];
			String applicationFirstName = (String)objects[2];
			String applicationLastName = (String)objects[3];
			PositionLight position = (PositionLight)objects[4];
			apps.add(new ParallelApplication(applicationKey, applicationEmail, applicationFirstName, applicationLastName, position));
		}
		return apps;
	}

	public List<Position> findPublishedPositions() {
		StringBuilder sb = new StringBuilder();
		sb.append("select position from rposition position")
		  .append(" inner join fetch position.committeeGroup committee")
		  .append(" left join fetch position.organisation org")
		  .append(" left join fetch position.reviewDefinition reviewDef")
		  .append(" left join fetch position.committeeHeadGroup committeeHead")
		  .append(" left join fetch position.secretaryGroup committeeSecretary")
		  .append(" left join fetch position.exOfficioGroup exOfficioGroup")
		  .append(" where position.advertised=true and position.valid=:posValid and position.status in (:status)");
		
		List<String> publishedStatus = new ArrayList<>(2);
		publishedStatus.add(PositionStatus.published.name());
		publishedStatus.add(PositionStatus.publishedAndInScreening.name());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Position.class)
				.setParameter("posValid", Boolean.TRUE)
				.setParameter("status", publishedStatus)
				.getResultList();
	}
	
	public List<PositionLightWithMembership> findPositionsLight(Identity identity, PositionStatusFilters filters,
			boolean valid) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("select position,")
		  .append(" (select count(head.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" head")
		  .append("    where head.securityGroup.key=position.committeeHeadGroup.key and head.identity.key=:identityKey) as numOfHeads,")
		  .append(" (select count(secretary.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" secretary")
		  .append("    where secretary.securityGroup.key=position.secretaryGroup.key and secretary.identity.key=:identityKey) as numOfSecretaries,")
		  .append(" (select count(committee.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" committee")
		  .append("    where committee.securityGroup.key=position.committeeGroup.key and committee.identity.key=:identityKey) as numOfCommittees,")
		  .append(" (select count(exOfficio.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" exOfficio")
		  .append("    where exOfficio.securityGroup.key=position.exOfficioGroup.key and exOfficio.identity.key=:identityKey) as numOfExOfficios")
		  .append(" from rpositionlight position")
		  .append(" left join fetch position.organisation orga")
		  .append(" where position.valid=:posValid");
		if(!appendPositionPermission(sb, filters)) {
			return Collections.emptyList();
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("posValid", Boolean.valueOf(valid));
		appendPositionPermission(query, identity, filters);
		
		List<Object[]> rawObjects = query.getResultList();
		List<PositionLightWithMembership> memberships = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			PositionLight position = (PositionLight)rawObject[0];
			boolean head = isMember(rawObject, 1);
			boolean secretary = isMember(rawObject, 2);
			boolean committee = isMember(rawObject, 3);
			boolean exOfficio = isMember(rawObject, 4);
			memberships.add(new PositionLightWithMembership(position, head, secretary, committee, exOfficio));
		}
		return memberships;
	}
	
	private boolean isMember(Object[] rawObject, int pos) {
		if(rawObject != null && rawObject.length > pos) {
			Long numOf = (Long)rawObject[pos];
			return numOf != null && numOf.longValue() > 0;
		}
		return false;
	}
	
	public boolean hasPositions(Identity identity, PositionStatusFilters filters,
			boolean valid) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("select position.key")
		  .append(" from rpositionlight position")
		  .append(" left join position.organisation orga")
		  .append(" where position.valid=:posValid");
		if(!appendPositionPermission(sb, filters)) {
			return false;
		}

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("posValid", Boolean.valueOf(valid));
		appendPositionPermission(query, identity, filters);
		List<Long> first = query
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return first != null && !first.isEmpty() && first.get(0) != null && first.get(0).longValue() > 0;
	}

	public List<PositionLightWithStatistics> findPositionsLightWithStatistics(Identity identity, PositionStatusFilters filters,
			List<PositionAttributeDefinition> globalDefinitions, boolean valid, Locale locale) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("select position,")
		  .append(" (select count(app1.key) from rapplication app1 where app1.valid=true and app1.position.key=position.key) as numOfApps,")
		  .append(" (select count(app2.key) from rapplication app2 where app2.valid=true and app2.position.key=position.key and app2.person.gender = 'm') as numOfMaleApps,")
		  .append(" (select count(app3.key) from rapplication app3 where app3.valid=true and app3.position.key=position.key and app3.person.gender = 'f') as numOfFemaleApps")
		  .append(" from rpositionlight position")
		  .append(" left join fetch position.organisation orga")
		  .append(" where position.valid=:posValid");
		if(!appendPositionPermission(sb, filters)) {
			return Collections.emptyList();
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("posValid", Boolean.valueOf(valid));
		appendPositionPermission(query, identity, filters);
		
		List<Object[]> withStatistics  = query.getResultList();
		List<ApplicationAttributeLight> customAttributes = getGlobalAttributes();
		
		int numOfAttributes = globalDefinitions.size();
		PositionAttributeDefinitionConfiguration[] types = new PositionAttributeDefinitionConfiguration[numOfAttributes];
		for(int i=0; i<globalDefinitions.size(); i++) {
			AttributeConfiguration config = globalDefinitions.get(i).getConfiguration(AttributeConfiguration.class);
			types[i] = new PositionAttributeDefinitionConfiguration(globalDefinitions.get(i), globalDefinitions.get(i).getTypeEnum(), config);
		}
		
		String pathPrefix = "[Positions:0][Position:";
		List<PositionLightWithStatistics> positionWithStatistics = new ArrayList<>(withStatistics.size());
		for(Object[] withStat:withStatistics) {
			PositionLight position = (PositionLight)withStat[0];
			Number numOfAppsObj = (Number)withStat[1];
			Number numOfMaleAppsObj = (Number)withStat[2];
			Number numOfFemaleAppsObj = (Number)withStat[3];
			
			int numOfApps = numOfAppsObj == null ? 0 : numOfAppsObj.intValue();
			int numOfMaleApps = numOfMaleAppsObj == null ? 0 : numOfMaleAppsObj.intValue();
			int numOfFemaleApps = numOfFemaleAppsObj == null ? 0 : numOfFemaleAppsObj.intValue();
			
			String path = pathPrefix + position.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			
			String[] rawValues = attributesToArray(position, globalDefinitions, customAttributes);
			Object[] additionalValues = localizedAdditionalValues(rawValues, types, locale);
			PositionLightWithStatistics posWithStats = new PositionLightWithStatistics(position,
					numOfApps, numOfMaleApps, numOfFemaleApps, additionalValues, url);
			positionWithStatistics.add(posWithStats);
		}
		return positionWithStatistics;
	}
	
	private Object[] localizedAdditionalValues(String[] rawValues, PositionAttributeDefinitionConfiguration[] selectConfigurations, Locale locale) {
		if(rawValues == null) return null;
		
		Object[] values = new Object[rawValues.length];
		for(int i=0; i<rawValues.length; i++) {
			if(i < selectConfigurations.length && selectConfigurations[i] != null) {	
				values[i] = ApplicationAttributesDelegate.getLocalizedValuesWithOthers(selectConfigurations[i], rawValues[i], locale);
			} else {
				values[i] = rawValues[i];
			}
		}
		return values;
	}
	
	private String[] attributesToArray(PositionLight position, List<PositionAttributeDefinition> definitions, List<ApplicationAttributeLight> customAttributes) {
		if(customAttributes == null || customAttributes.isEmpty()) {
			return new String[0];
		}
		
		int numOfAttributes = definitions.size();
		String[] attrs = new String[numOfAttributes];
		
		for(int i=0; i<definitions.size(); i++) {
			PositionAttributeDefinition definition = definitions.get(i);
			for(ApplicationAttributeLight customAttribute:customAttributes) {
				if(customAttribute.getDefinitionKey().equals(definition.getKey())
						&& customAttribute.getPositionKey().equals(position.getKey())) {
					attrs[i] = customAttribute.getValue();
				}
			}
		}
		return attrs;
	}
	
	public boolean appendPositionPermission(StringBuilder sb, PositionStatusFilters filters) {
		if(filters.isCommittee()) {
			StringBuilder roleCondition = new StringBuilder(512);

			boolean or = false;
			// committee
			if(filters.hasFilterPerRole(PositionRole.member)) {
				or = appendOr(roleCondition, or);
				roleCondition.append("exists (select membership.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" membership")
				  .append("  where membership.identity.key=:identityKey and membership.securityGroup.key=position.committeeGroup.key")
				  .append("  and position.status in (:statusmember)) ");
			}
			// secretary
			if(filters.hasFilterPerRole(PositionRole.secretary)) {
				or = appendOr(roleCondition, or);
				roleCondition.append("exists (select secretaryship.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" secretaryship")
				  .append("  where secretaryship.identity.key=:identityKey and secretaryship.securityGroup.key=position.secretaryGroup.key")
				  .append("  and position.status in (:statussecretary)) ");
			}
			// head
			if(filters.hasFilterPerRole(PositionRole.head)) {
				or = appendOr(roleCondition, or);
				roleCondition.append("exists (select headship.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" headship")
				  .append("  where headship.identity.key=:identityKey and position.committeeHeadGroup.key=headship.securityGroup.key")
				  .append("  and position.status in (:statushead)) ");
			}
			// ex-officio (optional)
			if(recruitingModule.isRoleExOfficioEnabled() && filters.hasFilterPerRole(PositionRole.exofficio)) {
				or = appendOr(roleCondition, or);
				roleCondition.append("exists (select exofficio.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" exofficio")
				  .append(" where exofficio.identity.key=:identityKey and position.exOfficioGroup.key=exofficio.securityGroup.key")
				  .append(" and position.status in (:statusexofficio)) ");
			}
			
			if(roleCondition.length() > 0) {
				sb.append(" and (")
				  .append(roleCondition);
				// organisation staff
				if(filters.isOrganisation()) {
					sb.append(" or ");
					appendPositionPermissionOrganisation(sb, filters);
				}
				sb.append(")");
			} else if(filters.isOrganisation()) {
				sb.append(" and ");
				appendPositionPermissionOrganisation(sb, filters);
			} else {
				return false;
			}
		} else if(filters.isOrganisation()) {
			sb.append(" and ");
			appendPositionPermissionOrganisation(sb, filters);
		} else if(filters.getFiltered().size() > 0) {
			sb.append(" and position.status in (:status)");
		}
		return true;
	}
	
	private boolean appendOr(StringBuilder sb, boolean started) {
		if(started) {
			sb.append(" or ");
		}
		return true;
	}
	
	private void appendPositionPermissionOrganisation(StringBuilder sb, PositionStatusFilters filters) {
		if(filters.isOrganisation()) {
			sb.append(" exists (select orgmember.key from bgroupmember as orgmember")
			  .append("  where orgmember.identity.key=:identityKey and orga.group.key=orgmember.group.key");
			if(filters.getFiltered().size() > 0) {
				sb.append(" and position.status in (:status)");
			}
			sb.append(")");
		}
	}
	
	public void appendPositionPermission(TypedQuery<?> query, Identity identity, PositionStatusFilters filters) {
		if(filters.isCommittee() || filters.isOrganisation()) {
			query.setParameter("identityKey", identity.getKey());
		}

		if(!filters.getFiltered().isEmpty()) {
			List<String> statusStrings = new ArrayList<>();
			for(PositionStatus state:filters.getFiltered()) {
				statusStrings.add(state.name());
			}
			query.setParameter("status", statusStrings);
		}
		
		for(PositionRole role:PositionRole.values()) {
			if(filters.hasFilterPerRole(role)) {
				List<PositionStatus> filterRole = filters.getFilterPerRole(role);
				List<String> statusStrings = new ArrayList<>(filterRole.size());
				for(PositionStatus state:filterRole) {
					statusStrings.add(state.name());
				}
				query.setParameter("status" + role.name(), statusStrings);
			}
		}
	}
	
	public PositionStatusFilters getPositionStatusFilters(Identity identity, Roles roles, List<PositionStatus> wishedStatusFilters) {
		boolean committee = false;
		boolean organisation = false;
		List<PositionStatus> filtered = new ArrayList<>();
		List<PositionStatus> committeeFiltered = new ArrayList<>();
		Map<PositionRole, List<PositionStatus>> filterPerRole = new EnumMap<>(PositionRole.class);
		if(identity == null || roles == null || roles.isGuestOnly()) {
			//not logged in -> limited to published and published in screening
			if(wishedStatusFilters == null || wishedStatusFilters.isEmpty()) {
				filtered.add(PositionStatus.published);
				filtered.add(PositionStatus.publishedAndInScreening);
			} else {
				for(PositionStatus state:wishedStatusFilters) {
					if(state.equals(PositionStatus.published) || state.equals(PositionStatus.publishedAndInScreening)) {
						filtered.add(state);
					}
				}
			}
		} else if(roles.isAdministrator() || roles.isSelectusManager()) {
			//administrator -> they can see want they want
			if(wishedStatusFilters != null) {
				filtered.addAll(wishedStatusFilters);
			}
			organisation = true;
		} else {
			//committee
			committee = true;
			//limited to published and in screening, closed and in screening
			
			PositionRole[] extendedRoles = recruitingModule.getRolesAllowedToSeePublishedPositions();
			List<PositionRole> enabledRoles = recruitingModule.getPositionRolesEnabled();
			for(PositionRole role:enabledRoles) {
				if(role == PositionRole.exofficio && !recruitingModule.isRoleExOfficioEnabled()) {
					continue;
				}
				
				boolean extended = false;
				for(PositionRole extendedRole:extendedRoles) {
					if(role == extendedRole) {
						extended = true;
					}
				}

				List<PositionStatus> filterRole = new ArrayList<>(4);
				if(extended) {
					extendedCommitteeFilters(wishedStatusFilters, filterRole);
				} else {
					standardCommitteeFilters(wishedStatusFilters, filterRole);
				}
				filterPerRole.put(role, filterRole);
			}
		}
		return new PositionStatusFilters(committee, filtered, committeeFiltered, filterPerRole, organisation);
	}
	
	/**
	 * Allowed are: published and in screening, closed and in screening, closed and no rating
	 * 
	 * @param status
	 * @param filtered
	 */
	private void standardCommitteeFilters(List<PositionStatus> status, List<PositionStatus> filtered) {
		if(status == null || status.isEmpty()) {
			filtered.add(PositionStatus.publishedAndInScreening);
			filtered.add(PositionStatus.closedAndInScreening);
			filtered.add(PositionStatus.closedAndNoRating);
		} else {
			for(PositionStatus state:status) {
				if(state.equals(PositionStatus.publishedAndInScreening) || state.equals(PositionStatus.closedAndInScreening)
						|| state.equals(PositionStatus.closedAndNoRating)) {
					filtered.add(state);
				}
			}
		}
	}
	
	/**
	 * Allowed are: published, published and in screening, closed and in screening, closed and no rating
	 * 
	 * @param status
	 * @param filtered
	 */
	private void extendedCommitteeFilters(List<PositionStatus> status, List<PositionStatus> filtered) {
		if(status == null || status.isEmpty()) {
			filtered.add(PositionStatus.published);
			filtered.add(PositionStatus.publishedAndInScreening);
			filtered.add(PositionStatus.closedAndInScreening);
			filtered.add(PositionStatus.closedAndNoRating);
		} else {
			for(PositionStatus state:status) {
				if(state.equals(PositionStatus.published) || state.equals(PositionStatus.publishedAndInScreening)
						|| state.equals(PositionStatus.closedAndInScreening) || state.equals(PositionStatus.closedAndNoRating)) {
					filtered.add(state);
				}
			}
		}
	}
	
	public boolean isInCommittee(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select position.key")
		  .append(" from rpositionlight position")
		  .append(" where exists (select membership.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" membership")
		  .append(" where membership.identity.key=:identityKey and membership.securityGroup.key=position.committeeGroup.key")
		  .append(")")
		  .append(" or exists (select secretaryship.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" secretaryship")
		  .append(" where secretaryship.identity.key=:identityKey and secretaryship.securityGroup.key=position.secretaryGroup.key")
		  .append(")")
		  .append(" or exists (select headship.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" headship")
		  .append("  where headship.identity.key=:identityKey and position.committeeHeadGroup.key=headship.securityGroup.key")
		  .append(")")
		  .append(" or exists (select exofficio.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" exofficio")
		  .append(" where exofficio.identity.key=:identityKey and position.exOfficioGroup.key=exofficio.securityGroup.key")
		  .append(")");
		
		List<Long> keys = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}

	public void deletePosition(Position position) {
		//delete accept policy
		deleteAcceptPolicies(position);
		//delete ratings
		deleteRatings(position);
		//position -> cascade delete docs
		dbInstance.deleteObject(position);
		
		try {
			File tmpDir = new File(WebappHelper.getUserDataRoot(), "tmp");
			if(tmpDir.exists()) {
				File cachedZip = new File(tmpDir, position.getKey() + ".zip");
				FileUtils.deleteFile(cachedZip);
			}
		} catch (Exception e) {
			log.error("Cannot delete cached zip file from position: {}", position, e);
		}
	}
	
	private void deleteRatings(Position position) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from userrating rating where")
			.append(" rating.resId=:resId and rating.resName=:resName");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("resName", position.getResourceableTypeName())
				.setParameter("resId", position.getResourceableId())
				.executeUpdate();
	}
	
	private void deleteAcceptPolicies(Position position) {
		String sb = "delete from racceptpolicy policy where policy.position.key=:positionKey";
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("positionKey", position.getKey())
				.executeUpdate();
	}

	public boolean acceptPositionPolicy(Position position, Identity identity, AcceptPolicyEnum policyType, Boolean dontShowNextTime) {
		AcceptPolicyImpl policy = loadAcceptPolicy(position, policyType, identity);
		if(policy == null && dontShowNextTime == null) {
			return false;
		}
		
		if(policy == null) {
			policy = new AcceptPolicyImpl();
			policy.setIdentity(identity);
			policy.setPosition(position);
			policy.setName(policyType.name());
			policy.setDontShowNextTime(dontShowNextTime == null ? false : dontShowNextTime.booleanValue());
			dbInstance.getCurrentEntityManager().persist(policy);
			return true;
		} else {
			if(dontShowNextTime != null) {
				policy.setDontShowNextTime(dontShowNextTime.booleanValue());
				dbInstance.getCurrentEntityManager().merge(policy);
			}
			return policy.isDontShowNextTime();
		}
	}
	
	public AcceptPolicyImpl loadAcceptPolicy(Position position, AcceptPolicyEnum policy, Identity identity) {
		if(policy == AcceptPolicyEnum.ratingPolicy) {
			String sb = "select policy from racceptpolicy policy where (policy.name is null or policy.name=:policyType) and policy.identity.key=:identityKey and policy.position.key=:positionKey";
			List<AcceptPolicyImpl> acceptList = dbInstance.getCurrentEntityManager()
					.createQuery(sb, AcceptPolicyImpl.class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("positionKey", position.getKey())
					.setParameter("policyType", AcceptPolicyEnum.ratingPolicy.name())
					.getResultList();
			return acceptList == null || acceptList.isEmpty() ? null : acceptList.get(0);
		}
		
		String sb = "select policy from racceptpolicy policy where policy.name=:policyType and policy.identity.key=:identityKey and policy.position.key=:positionKey";
		List<AcceptPolicyImpl> acceptList = dbInstance.getCurrentEntityManager()
				.createQuery(sb, AcceptPolicyImpl.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("positionKey", position.getKey())
				.setParameter("policyType", policy.name())
				.getResultList();
		return acceptList == null || acceptList.isEmpty() ? null : acceptList.get(0);
		
	}
	
	public List<AcceptPolicyImpl> loadAcceptPolicy(Position position, Identity identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("policyByIdentityAndPosition", AcceptPolicyImpl.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}

	public void addMemberToCommittee(Position position, Identity member) {
		SecurityGroup committee = position.getCommitteeGroup();
		if(!securityGroupDao.isIdentityInSecurityGroup(member, committee)) {
			securityGroupDao.addIdentityToSecurityGroup(member, committee);
		}
	}

	public void removeMemberFromCommittee(Position position, Identity member) {
		SecurityGroup committee = position.getCommitteeGroup();
		if(securityGroupDao.isIdentityInSecurityGroup(member, committee)) {
			securityGroupDao.removeIdentityFromSecurityGroup(member, committee);
		}	
	}
	
	public void addHeadToCommittee(Position position, Identity member) {
		SecurityGroup heads = position.getCommitteeHeadGroup();
		
		if(heads == null) {
			heads = securityGroupDao.createAndPersistSecurityGroup();
			position.setCommitteeHeadGroup(heads);
			savePosition(position);
		}
		
		if(!securityGroupDao.isIdentityInSecurityGroup(member, heads)) {
			securityGroupDao.addIdentityToSecurityGroup(member, heads);
		}
	}

	public void removeHeadFromCommittee(Position position, Identity member) {
		SecurityGroup heads = position.getCommitteeHeadGroup();
		if(heads != null && securityGroupDao.isIdentityInSecurityGroup(member, heads)) {
			securityGroupDao.removeIdentityFromSecurityGroup(member, heads);
		}
	}
	
	public void addSecretaryToCommittee(Position position, Identity secretary) {
		SecurityGroup secretaries = position.getSecretaryGroup();
		if(secretaries == null) {
			secretaries = securityGroupDao.createAndPersistSecurityGroup();
			position.setSecretaryGroup(secretaries);
			savePosition(position);
		}
		
		if(!securityGroupDao.isIdentityInSecurityGroup(secretary, secretaries)) {
			securityGroupDao.addIdentityToSecurityGroup(secretary, secretaries);
		}
	}

	public void removeSecretaryFromCommittee(Position position, Identity secretary) {
		SecurityGroup secretaries = position.getSecretaryGroup();
		if(secretaries != null && securityGroupDao.isIdentityInSecurityGroup(secretary, secretaries)) {
			securityGroupDao.removeIdentityFromSecurityGroup(secretary, secretaries);
		}
	}
	
	public void addExOfficioToCommittee(Position position, Identity exOfficio) {
		SecurityGroup secretaries = position.getExOfficioGroup();
		if(secretaries == null) {
			secretaries = securityGroupDao.createAndPersistSecurityGroup();
			position.setExOfficioGroup(secretaries);
			savePosition(position);
		}
		
		if(!securityGroupDao.isIdentityInSecurityGroup(exOfficio, secretaries)) {
			securityGroupDao.addIdentityToSecurityGroup(exOfficio, secretaries);
		}
	}
	
	public void removeExOfficioFromCommittee(Position position, Identity exOfficio) {
		SecurityGroup exOfficios = position.getExOfficioGroup();
		if(exOfficios != null && securityGroupDao.isIdentityInSecurityGroup(exOfficio, exOfficios)) {
			securityGroupDao.removeIdentityFromSecurityGroup(exOfficio, exOfficios);
		}
	}

	public List<UserRating> getRatings(Position position, List<? extends IdentityRef> committee) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rating from userratingshort rating")
		  .append(" where rating.resName=:resname and rating.resId=:resId");

		List<UserRating> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserRating.class)
				.setParameter("resname", position.getResourceableTypeName())
				.setParameter("resId", position.getResourceableId())
				.getResultList();
		//make sure that ratings are from committee
		Set<Long> committeeSet = new HashSet<>();
		for(IdentityRef member:committee) {
			committeeSet.add(member.getKey());
		}
		for(Iterator<UserRating> resultIt=results.iterator(); resultIt.hasNext(); ) {
			UserRating rating = resultIt.next();
			if(!committeeSet.contains(rating.getCreator().getKey())) {
				resultIt.remove();
			}	
		}
		
		return results;
	}
	
	public List<UserRating> getRatings(Position position, IdentityRef committee) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rating from userratingshort rating where")
		  .append(" rating.resName=:resname and rating.resId=:resId and rating.creatorKey=:creatorId");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserRating.class)
				.setParameter("resname", position.getResourceableTypeName())
				.setParameter("resId", position.getResourceableId())
				.setParameter("creatorId", committee.getKey())
				.getResultList();
	}

	public Position deleteAttachment(Position position, Attachment attachment) {
		if(attachment == null || attachment.getKey() == null) return position;
		
		Position reloadedPosition = dbInstance.getCurrentEntityManager().find(PositionImpl.class, position.getKey());
		Attachment reloadedAttachment = dbInstance.getCurrentEntityManager().find(AttachmentImpl.class, attachment.getKey());
		
		if(reloadedAttachment.equals(reloadedPosition.getDocument1())) {
			reloadedPosition.setDocument1(null);
		} else if(reloadedAttachment.equals(reloadedPosition.getDocument2())) {
			reloadedPosition.setDocument2(null);
		} else if(reloadedAttachment.equals(reloadedPosition.getDocument3())) {
			reloadedPosition.setDocument3(null);
		}
		dbInstance.updateObject(reloadedPosition);
		dbInstance.deleteObject(reloadedAttachment);
		return reloadedPosition;
	}
	
	public SecurityGroup getMemberGroup(PositionRef position, PositionRole role) {
		StringBuilder sb = new StringBuilder();
		sb.append("select secGroup from rposition pos");
		switch(role) {
			case member: sb.append(" inner join pos.committeeGroup secGroup"); break;
			case head: sb.append(" inner join pos.committeeHeadGroup secGroup"); break;
			case secretary: sb.append(" inner join pos.secretaryGroup secGroup"); break;
			case exofficio: sb.append(" inner join pos.exOfficioGroup secGroup"); break;	
		}
		sb.append(" where pos.key=:positionKey");
		
		List<SecurityGroup> secGroups = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), SecurityGroup.class)
			.setParameter("positionKey", position.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return secGroups == null || secGroups.isEmpty() ? null : secGroups.get(0);
	}
	
	public long countMembers(Position position, PositionRole role) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(memberships) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" memberships")
		  .append(" where memberships.securityGroup.key in(select pos.");
		switch(role) {
			case member: sb.append("committeeGroup"); break;
			case head: sb.append("committeeHeadGroup"); break;
			case secretary: sb.append("secretaryGroup"); break;
			case exofficio: sb.append("exOfficioGroup"); break;	
		}
		sb.append(".key from rposition pos where pos.key=:positionKey)");
		
		List<Number> count = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
			.setParameter("positionKey", position.getKey())
			.getResultList();
		return count == null || count.isEmpty() ? 0 : count.get(0).longValue();
	}
}