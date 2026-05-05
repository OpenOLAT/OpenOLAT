/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.AcademicalBackgroundImpl;
import org.olat.modules.selectus.model.AddressImpl;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationAttributeImpl;
import org.olat.modules.selectus.model.ApplicationAttributeLight;
import org.olat.modules.selectus.model.ApplicationDecisionImpl;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationLightImpl;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.AttachmentDataImpl;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.AttachmentsImpl;
import org.olat.modules.selectus.model.BusinessAddressImpl;
import org.olat.modules.selectus.model.BusinessInformationsImpl;
import org.olat.modules.selectus.model.PersonImpl;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.attributes.AttributeConfiguration;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 juil. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("applicationDao")
public class ApplicationDAO {
	
	private static final Logger log = Tracing.createLoggerFor(ApplicationDAO.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private RecruitingModule recruitingModule;

	public Application createApplication(Position position) {
		return create(position); 
	}
	
	public Application createTempApplication(Position position, boolean submittedByStaff) {
		ApplicationImpl app = create(position); 
		if(position != null) {
			app.setSubmittedByStaff(submittedByStaff);
		}
		return app;
	}
	
	private ApplicationImpl create(Position position) {
		ApplicationImpl app = new ApplicationImpl();
		app.setCreationDate(new Date());
		app.setLastModified(app.getCreationDate());
		app.setPosition(position);
		app.setPerson(new PersonImpl());
		app.setAddress(new AddressImpl());
		app.setBusinessAddress(new BusinessAddressImpl());
		app.setBusinessInformations(new BusinessInformationsImpl());
		app.setApplicationStatus(ApplicationStatus.active);
		app.setPublicFeedbackEnabled(false);
		app.setPublicFeedbackKey(UUID.randomUUID().toString().replace("-", ""));
		app.setApplicantUrl(UUID.randomUUID().toString().replace("-", ""));
		app.setAttributes(new HashSet<>());
		
		AcademicalBackgroundImpl academicalBackground = new AcademicalBackgroundImpl();
		academicalBackground.setNumberOfFirstAuthorships(null);
		academicalBackground.setNumberOfLastAuthorships(null);
		academicalBackground.setNumberOfOriginalPublications(null);
		academicalBackground.setCitations(null);
		academicalBackground.setImpactFactor(null);
		academicalBackground.setHFactor(null);
		
		app.setAcademicalBackground(academicalBackground);
		app.setAttachments(new AttachmentsImpl());
		app.setValid(false);
		return app;
	}
	
	public ApplicationAttribute createApplicationAttribute(Position position, Application application, PositionAttributeDefinition definition, String value) {
		ApplicationAttributeImpl attr = new ApplicationAttributeImpl();
		attr.setCreationDate(new Date());
		attr.setLastModified(attr.getCreationDate());
		// Application or position but never both!
		if(application != null) {
			attr.setApplication(application);
		} else if(position != null) {
			attr.setPosition(position);
		}
		attr.setDefinition(definition);
		attr.setValue(value);
		dbInstance.getCurrentEntityManager().persist(attr);
		return attr;
	}

	public synchronized Application saveApplication(Application application) {
		if(application instanceof ApplicationImpl) {
			if(application.isValid() && (application.getId() == null || application.getId().intValue() <= 0)) {
				int nextAppId = getNextAppId(application);
				((ApplicationImpl)application).setId(nextAppId);
			}
		}
		
		application.setLastModified(new Date());
		
		if(application.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(application);
		} else {
			application = dbInstance.getCurrentEntityManager().merge(application);
		}
		dbInstance.commit();
		return application;
	}

	public Application saveTempApplication(Application application, boolean removeTemp) {
		if(!application.isValid() && application instanceof ApplicationImpl) {
			((ApplicationImpl)application).setValid(removeTemp);
		}
		return saveApplication(application);
	}

	public boolean checkUniqueApplicationByEmail(Application application) {
		Position position = application.getPosition();
		if(position == null || application.getPerson() == null) return false;
		
		Number countMail = dbInstance.getCurrentEntityManager()
				.createNamedQuery("numOfApplicationsByPositionAndMail", Number.class)
				.setParameter("positionKey", position.getKey())
				.setParameter("mail", application.getPerson().getMail().toLowerCase())
				.getSingleResult();
		return (countMail != null && countMail.intValue() == 0);
	}
	
	public boolean checkUniqueApplicationByEmailFistnameLastname(Application application) {
		Position position = application.getPosition();
		if(position == null || application.getPerson() == null) return false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(app.id) from rapplication app")
		  .append(" where app.position.key=:positionKey and lower(app.person.email)=:mail")
		  .append(" and lower(app.person.firstName)=:firstName and lower(app.person.lastName)=:lastName");
		
		Number countMail = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("positionKey", position.getKey())
				.setParameter("mail", application.getPerson().getMail().toLowerCase())
				.setParameter("firstName", application.getPerson().getFirstName().toLowerCase())
				.setParameter("lastName", application.getPerson().getLastName().toLowerCase())
				.getSingleResult();
		return (countMail != null && countMail.intValue() == 0);
	}

	private int getNextAppId(Application application) {
		Position position = application.getPosition();
		if(position == null) return 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select max(app.id) from rapplication app where app.position=:position");
		List<Number> maxAppIds = dbInstance.getCurrentEntityManager()
				.createNamedQuery("nextApplicationId", Number.class)
				.setParameter("position", position)
				.getResultList();
		return (maxAppIds == null || maxAppIds.isEmpty() || maxAppIds.get(0) == null) ? 1 : maxAppIds.get(0).intValue() + 1;
	}
	
	public Application loadApplicationByKey(Long key) {
		List<Application> apps = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadApplicationByKey", Application.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}
	
	/**
	 * @param keys The primary keys of applications to load
	 * @return A list of applications with nothing fetched
	 */
	public List<Application> loadApplicationsByKeyForRelations(List<Long> keys) {
		if(keys == null || keys.isEmpty()) {
			return new ArrayList<>();
		}

		String query = "select app from rapplication app where app.key in (:applicationKeys)";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Application.class)
				.setParameter("applicationKeys", keys)
				.getResultList();
	}
	
	public Application loadApplicationByPublicFeedbackKey(String key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select app from rapplication app")
		  .append(" inner join fetch app.position as pos")
		  .append(" where app.publicFeedbackKey=:key");
		
		List<Application> apps = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Application.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}
	
	public Application loadApplicationByApplicantKey(String key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select app from rapplication app")
		  .append(" inner join fetch app.position as pos")
		  .append(" where app.applicantUrl=:key");
		
		List<Application> apps = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Application.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}
	
	public boolean hasApplicationByIdentity(IdentityRef identity) {
		String query = "select app.key from rapplication app where app.identity.key=:key";
		
		List<Long> apps = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("key", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return apps != null && !apps.isEmpty() && apps.get(0).longValue() > 0;
	}
	
	public List<Application> loadCurrentApplicationsByApplicant(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select app from rapplication app")
		  .append(" inner join fetch app.position as pos")
		  .append(" where app.identity.key=:key")
		  .append(" and pos.applicantRefereeManagementEnabled=true")
		  .append(" and pos.applicantRefereeManagementDeadline>=:date");
		
		Date date = RecruitingHelper.startOfDay(new Date());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Application.class)
				.setParameter("key", identity.getKey())
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public boolean hasApplicationsByApplicant(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select app.key from rapplication app")
		  .append(" where app.identity.key=:key");
		List<Long> first = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("key", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return first != null && !first.isEmpty() && first.get(0) != null && first.get(0).longValue() > 0;
	}
	
	public ApplicationLight loadApplicationLightByKeyWithAttributes(Position position, Long key) {
		List<ApplicationLight> apps = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadApplicationLightByKey", ApplicationLight.class)
				.setParameter("key", key)
				.getResultList();
		enrichApplicationLight(position, apps);
		return apps.isEmpty() ? null : apps.get(0);
	}
	
	public ApplicationLight loadApplicationLightForReference(Long key) {
		List<ApplicationLight> apps = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadApplicationLightByKey", ApplicationLight.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}

	public Application loadValidApplicationByKey(Long key) {
		List<Application> apps = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadValidApplicationByKey", Application.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}
	
	public Integer getApplicationDecision(Long key) {
		List<Integer> apps  = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadApplicationDecisionByKey", Integer.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}
	
	public void setDecision(Application app, int decision) {
		ApplicationDecisionImpl appDecision = getApplicationDecisionImpl(app.getKey());
		if(decision > 0 && decision <= 3) {
			appDecision.setDecision(Integer.valueOf(decision));
		} else {
			appDecision.setDecision(null);
		}
		dbInstance.getCurrentEntityManager().merge(appDecision);
	}
	
	public ApplicationDecisionImpl getApplicationDecisionImpl(Long key) {
		String q = "select appdec from rapplicationdecision appdec where appdec.key=:key";
		List<ApplicationDecisionImpl> apps  = dbInstance.getCurrentEntityManager()
				.createQuery(q, ApplicationDecisionImpl.class)
				.setParameter("key", key)
				.getResultList();
		return apps.isEmpty() ? null : apps.get(0);
	}

	public List<Application> findApplications(Position position, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select app from rapplication app where app.valid=:valid");
		if(position != null) {
			sb.append(" and app.position=:position");
		}
		sb.append(" order by app.id asc");
		TypedQuery<Application> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Application.class)
				.setParameter("valid", valid);
		if(position != null) {
			query.setParameter("position", position);
		}
		return query.getResultList();
	}
	
	public List<ApplicationLight> findApplicationsLight(Position position, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select app from rapplicationlight app ")
		  .append(" where app.valid=:valid and app.positionKey=:position");
		sb.append(" order by app.id asc");
		List<ApplicationLight> apps = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationLight.class)
				.setParameter("valid", valid)
				.setParameter("position", position.getKey())
				.getResultList();
		enrichApplicationLight(position, apps);
		return apps;
	}
	
	public List<ApplicationLight> findApplicationsLight(Position position, List<Integer> limitDecisions) {
		StringBuilder sb = new StringBuilder();
		sb.append("select app from rapplicationlight app where app.valid=:valid")
		  .append(" and app.positionKey=:position");
		if(limitDecisions != null && !limitDecisions.isEmpty()) {
			sb.append(" and (app.decision is null or app.decision not in (:limitDecisions))");
		}
		sb.append(" order by app.id asc");
		TypedQuery<ApplicationLight> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationLight.class)
				.setParameter("valid", Boolean.TRUE)
				.setParameter("position", position.getKey());
		if(limitDecisions != null && !limitDecisions.isEmpty()) {
			query.setParameter("limitDecisions", limitDecisions);
		}
		List<ApplicationLight> apps = query.getResultList();
		enrichApplicationLight(position, apps);
		return apps;
	}
	
	private void enrichApplicationLight(Position position, List<ApplicationLight> applications) {
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();

		Map<Long,ApplicationLight> applicationKeyMap = applications.stream()
				.collect(Collectors.toMap(ApplicationLight::getKey, app -> app));
		
		int numOfAttributes = definitions.size();
		PositionAttributeDefinitionConfiguration[] types = new PositionAttributeDefinitionConfiguration[numOfAttributes];
		for(int i=0; i<definitions.size(); i++) {
			AttributeConfiguration config = definitions.get(i).getConfiguration(AttributeConfiguration.class);
			types[i] = new PositionAttributeDefinitionConfiguration(definitions.get(i), definitions.get(i).getTypeEnum(), config);
		}

		Long currentApplicationKey = null;
		List<ApplicationAttributeLight> applicationAttributes = new ArrayList<>();
		List<ApplicationAttributeLight> attributes = loadApplicationAttributes(position, true);
		for(ApplicationAttributeLight attribute:attributes) {
			Long attrAppKey = attribute.getApplicationKey();
			if(currentApplicationKey != null && !currentApplicationKey.equals(attrAppKey)) {
				ApplicationLightImpl app = (ApplicationLightImpl)applicationKeyMap.get(currentApplicationKey);
				if(app != null) {
					app.setAdditionalValues(attributesToArray(definitions, applicationAttributes));
					app.setAdditionalValuesTypes(types);
				}
				applicationAttributes.clear();
			}
			applicationAttributes.add(attribute);
			currentApplicationKey = attrAppKey;
		}
		
		if(currentApplicationKey != null) {
			ApplicationLightImpl app = (ApplicationLightImpl)applicationKeyMap.get(currentApplicationKey);
			if(app != null) {
				app.setAdditionalValues(attributesToArray(definitions, applicationAttributes));
				app.setAdditionalValuesTypes(types);
			}
		}
	}
	
	private String[] attributesToArray(List<PositionAttributeDefinition> definitions, List<ApplicationAttributeLight> applicationAttributes) {
		if(applicationAttributes == null || applicationAttributes.isEmpty()) {
			return new String[0];
		}
		
		int numOfAttributes = definitions.size();
		String[] attrs = new String[numOfAttributes];
		
		for(int i=0; i<definitions.size(); i++) {
			PositionAttributeDefinition definition = definitions.get(i);
			for(ApplicationAttributeLight applicationAttribute:applicationAttributes) {
				if(applicationAttribute.getDefinitionKey().equals(definition.getKey())) {
					attrs[i] = applicationAttribute.getValue();
				}
			}
		}
		return attrs;
	}
	
	public List<ApplicationAttributeLight> loadApplicationAttributes(Position position, boolean valid) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select attr from appattributelight attr")
		  .append(" inner join rapplication as app on (app.key = attr.applicationKey)")
		  .append(" where app.position.key=:positionKey and app.valid=:valid")
		  .append(" order by app.id asc, attr.id asc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationAttributeLight.class)
				.setParameter("valid", Boolean.valueOf(valid))
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public void deleteAttributes(PositionAttributeDefinition definition) {
		String query = "delete appattribute attr where attr.definition.key=:definitionKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("definitionKey", definition.getKey())
			.executeUpdate();
	}
	
	public long getAttributeUsage(PositionRef position, PositionAttributeDefinition definition) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(attr.key) from appattribute attr")
		  .append(" inner join attr.application as app")
		  .append(" where app.position.key=:positionKey and app.valid=:valid")
		  .append(" and attr.definition.key=:definitionKey");
		
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("valid", Boolean.TRUE)
				.setParameter("positionKey", position.getKey())
				.setParameter("definitionKey", definition.getKey())
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).longValue();
	}
	
	public long getGlobalAttributeUsage(PositionAttributeDefinition definition) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(attr.key) from appattribute attr")
		  .append(" where attr.position.key is null and attr.definition.key=:definitionKey");
		
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("definitionKey", definition.getKey())
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).longValue();
	}
	
	public List<ApplicationRefereeStats> findApplicationReviewerStats(Position position, boolean valid) {
		boolean comparativeAssessmentExpertEnabled = position.isComparativeAssessmentExpertEnabled() && recruitingModule.isComparativeAssessmentExpertsEnabled();
		boolean expertsEnabled = position.isExpertRecommendationEnabled() && recruitingModule.isReferenceEnabled();
		boolean refereesEnabled = position.isRefereeRecommendationEnabled() && recruitingModule.isReferenceEnabled();
		
		List<ReferenceType> types = new ArrayList<>();
		if(expertsEnabled) {
			types.add(ReferenceType.expert);
		}
		if(refereesEnabled) {
			types.add(ReferenceType.recommendation);
		}
		
		QueryBuilder sb = new QueryBuilder(1600);
		sb.append("select app.key,");

		if(expertsEnabled) {
			sb.append(" (select count(rrefexpert.key) from rreference as rrefexpert ")
			  .append("   where rrefexpert.application.key=app.key and rrefexpert.status in ('").append(ReferenceStatus.sentAwaiting.name()).append("','").append(ReferenceStatus.late.name()).append("','").append(ReferenceStatus.submitted.name()).append("') and rrefexpert.type='").append(ReferenceType.expert).append("'")
			  .append(" ) as numOfExperts,")
			  .append(" (select count(rrefsubmittedexpert.key) from rreference as rrefsubmittedexpert ")
			  .append("   where rrefsubmittedexpert.application.key=app.key and rrefsubmittedexpert.status='").append(ReferenceStatus.submitted.name()).append("' and rrefsubmittedexpert.type='").append(ReferenceType.expert).append("'")
			  .append(" ) as numOfSubmittedExperts,");
		} else {
			sb.append(" 0 as numOfExperts,")
			  .append(" 0 as numOfSubmittedExperts,");
		}
		  
		if(refereesEnabled) {
			sb.append(" (select count(rrefrecommendation.key) from rreference as rrefrecommendation ")
			  .append("   where rrefrecommendation.application.key=app.key and rrefrecommendation.status in ('").append(ReferenceStatus.sentAwaiting.name()).append("','").append(ReferenceStatus.late.name()).append("','").append(ReferenceStatus.submitted.name()).append("') and rrefrecommendation.type='").append(ReferenceType.recommendation).append("'")
			  .append(" ) as numOfRecommendations,")
			  .append(" (select count(rrefsubmittedrecommendation.key) from rreference as rrefsubmittedrecommendation ")
			  .append("   where rrefsubmittedrecommendation.application.key=app.key and rrefsubmittedrecommendation.status='").append(ReferenceStatus.submitted.name()).append("' and rrefsubmittedrecommendation.type='").append(ReferenceType.recommendation).append("'")
			  .append(" ) as numOfSubmittedRecommendations,");
		} else {
			sb.append(" 0 as numOfRecommendations,")
			  .append(" 0 as numOfSubmittedRecommendations,");
		}
		
		if(comparativeAssessmentExpertEnabled) {
			sb.append(" (select count(rrefcompexpert.key) from rreference as rrefcompexpert ")
			  .append("   inner join rreferencetoapp as compreftoapp on (rrefcompexpert.key=compreftoapp.reference.key)")
			  .append("   where compreftoapp.application.key=app.key and rrefcompexpert.status in ('").append(ReferenceStatus.sentAwaiting.name()).append("','").append(ReferenceStatus.late.name()).append("','").append(ReferenceStatus.submitted.name()).append("') and rrefcompexpert.type='").append(ReferenceType.comparativeAssessmentExpert).append("'")
			  .append(" ) as numOfComparativeExperts,")
			  .append(" (select count(rrefsubmittedcompexpert.key) from rreference as rrefsubmittedcompexpert")
			  .append("   inner join rreferencetoapp as compsubmittedreftoapp on (rrefsubmittedcompexpert.key=compsubmittedreftoapp.reference.key)")
			  .append("   where compsubmittedreftoapp.application.key=app.key and rrefsubmittedcompexpert.status='").append(ReferenceStatus.submitted.name()).append("' and rrefsubmittedcompexpert.type='").append(ReferenceType.comparativeAssessmentExpert).append("'")
			  .append(" ) as numOfSubmittedComparativeExperts,");
		} else {
			sb.append(" 0 as numOfComparativeExperts,")
			  .append(" 0 as numOfSubmittedComparativeExperts,");
		}
		
		if(types.isEmpty()) {
			sb.append(" 0 as numOfTotalSubmitted,");
		} else {
			sb.append(" (select count(rtotalsubmitted.key) from rreference as rtotalsubmitted ")
			  .append("   where rtotalsubmitted.application.key=app.key and rtotalsubmitted.type").in(types.toArray())
			  .append(" ) as numOfTotalSubmitted,");
		}
		
		if(comparativeAssessmentExpertEnabled) {
			sb.append(" (select count(rrefcompexpert.key) from rreference as rrefcompexpert ")
			  .append("   inner join rreferencetoapp as compreftoapp on (rrefcompexpert.key=compreftoapp.reference.key)")
			  .append("   where compreftoapp.application.key=app.key and rrefcompexpert.type='").append(ReferenceType.comparativeAssessmentExpert).append("'")
			  .append(" ) as numOfTotalComparativeExpertsSubmitted");
		} else {
			sb.append(" 0 as numOfTotalComparativeExpertsSubmitted");
		}
		
		sb.append(" from rapplication app")
		  .append(" where app.position.key=:positionKey and app.valid=:valid");
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("positionKey", position.getKey())
				.setParameter("valid", valid)
				.getResultList();
		
		List<ApplicationRefereeStats> stats = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long key = (Long)object[0];
			int experts = extractInt(object, 1);
			int submittedExperts = extractInt(object, 2);
			int recommendations = extractInt(object, 3);
			int submittedRecommendations = extractInt(object, 4);
			int comparativeExperts = extractInt(object, 5);
			int submittedComparativeExperts = extractInt(object, 6);
			int totalSubmitted = extractInt(object, 7) + extractInt(object, 8);
			stats.add(new ApplicationRefereeStats(key, experts, submittedExperts,
					recommendations, submittedRecommendations, comparativeExperts, submittedComparativeExperts,
					totalSubmitted));
		}
		return stats;
	}
	
	private int extractInt(Object[] objects, int pos) {
		int val = 0;
		if(objects.length > pos) {
			Number number = (Number)objects[pos];
			val = number == null ? 0 : number.intValue();
		}
		return val;
	}
	
	public List<Application> searchApplications(String searchText, Identity identity, PositionStatusFilters filters) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("select app from rapplication app ")
		  .append(" inner join fetch app.position position")
		  .append(" left join position.organisation orga")
		  .append(" where app.valid=:valid ")
		  .append(" and (lower(app.person.firstName) like :searchText or lower(app.person.lastName) like :searchText or lower(app.person.email) like :searchText)");
		if(!positionDao.appendPositionPermission(sb, filters)) {
			return Collections.emptyList();
		}
		sb.append(" order by app.id asc");
		
		TypedQuery<Application> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Application.class)
				.setParameter("valid", Boolean.TRUE);
		positionDao.appendPositionPermission(query, identity, filters);
		
		String fuzzyText = PersistenceHelper.makeFuzzyQueryString(searchText.toLowerCase());
		query.setParameter("searchText", fuzzyText);

		return query.getResultList();
	}
	
	public List<ApplicationLight> findApplicationsLightWithoutDecision(Position position) {
		StringBuilder sb = new StringBuilder();
		sb.append("select app from rapplicationlight app")
		  .append(" where app.positionKey=:positionKey and (app.decision is null or app.decision<=0)")
		  .append(" and app.valid=true")
		  .append(" and app.withdrawn=false")
		  .append(" order by app.id asc");
		List<ApplicationLight> apps = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationLight.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
		enrichApplicationLight(position, apps);
		return apps;
	}
	
	public List<ApplicationLight> findApplicationsLightWithDecisions(Position position,
			List<Integer> decisions, List<ApplicationStatus> status, boolean noDecision,
			List<String> excludeTemplateNames, List<String> currentTemplateNames, boolean excludeSendEmails) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select app from rapplicationlight app")
		  .append(" where app.positionKey=:positionKey and app.valid=true");
		if(decisions.isEmpty() && noDecision) {
			sb.append(" and app.decision is null");
		} else if(!decisions.isEmpty()) {
			sb.append(" and (app.decision in (:decisions)");
			if(noDecision) {
				sb.append(" or app.decision is null");
			}
			sb.append(")");
		}
		if(status != null && !status.isEmpty()) {
			sb.append(" and app.status in (:status)");
		}
		
		if(excludeTemplateNames != null && !excludeTemplateNames.isEmpty()) {
			sb.append(" and not exists (select log.key from rrejectionlog log")
			  .append("  where log.application.key=app.key and (log.mailTemplate in (:templateNames)");
			if(excludeTemplateNames.contains("-") || excludeTemplateNames.contains("")) {
				sb.append(" or log.mailTemplate = '-' or log.mailTemplate = '' or log.mailTemplate is null");
				currentTemplateNames.removeAll(excludeTemplateNames);
				if(!currentTemplateNames.isEmpty()) {
					sb.append(" or log.mailTemplate in (:currentTemplateNames)");
				}
			}
			sb.append(" ))");
		} else if(excludeSendEmails) {
			sb.append(" and not exists (select log.application.key from rrejectionlog log")
			  .append("  where log.application.key=app.key")
			  .append(" )");
		}

		sb.append(" order by app.id asc");
		TypedQuery<ApplicationLight> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationLight.class)
				.setParameter("positionKey", position.getKey());
		if(!decisions.isEmpty()) {
			query.setParameter("decisions", decisions);
		}
		if(status != null && !status.isEmpty()) {
			List<String> statusList = status.stream()
					.map(ApplicationStatus::name)
					.collect(Collectors.toList());
			query.setParameter("status", statusList);
		}
		if(excludeTemplateNames != null && !excludeTemplateNames.isEmpty()) {
			query.setParameter("templateNames", excludeTemplateNames);
		}
		
		if(excludeTemplateNames != null && !excludeTemplateNames.isEmpty()
				&& (excludeTemplateNames.contains("-") || excludeTemplateNames.contains(""))
				&& !currentTemplateNames.isEmpty()) {
			query.setParameter("currentTemplateNames", currentTemplateNames);
		}
		
		List<ApplicationLight> apps = query.getResultList();
		enrichApplicationLight(position, apps);
		return apps;
	}

	public void deleteApplication(Application app) {
		if(recruitingModule.isAttachmenOnFileSystem()) {
			for(DocumentEnum doc:DocumentEnum.values()) {
				Attachment attachment = doc.path(app);
				if(attachment != null) {
					removeAttachmentDatas(attachment);
				}
			}
		}
		dbInstance.deleteObject(app);
	}

	public List<UserRating> getRatings(Application application, List<? extends IdentityRef> committee) {
		Position position = application.getPosition();
		return getRatings(position, application, committee);
	}

	public List<UserRating> getRatings(Position position, ApplicationRef application, List<? extends IdentityRef> committee) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rating from userrating rating where")
		  .append(" rating.resName=:resname and rating.resId=:resId and rating.resSubPath=:resSubPath");

		List<UserRating> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserRating.class)
				.setParameter("resname", position.getResourceableTypeName())
				.setParameter("resId", position.getResourceableId())
				.setParameter("resSubPath", application.getKey().toString())
				.getResultList();
		
		//make sure that ratings are from committee
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
	
	/**
	 * @param attachment
	 * @param filename
	 * @param bytes
	 */
	public Attachment setAttachmentDatas(Attachment attachment, String filename, String type, byte[] bytes) {
		if(attachment == null) {
			attachment = new AttachmentImpl();
			((AttachmentImpl)attachment).setSize(bytes.length);
			((AttachmentImpl)attachment).setName(filename);
			((AttachmentImpl)attachment).setType(type);
			dbInstance.getCurrentEntityManager().persist(attachment);
			dbInstance.commit();
		}
		
		Long key = attachment.getKey();
		AttachmentDataImpl data = dbInstance.getCurrentEntityManager().find(AttachmentDataImpl.class, key);
		if(recruitingModule.isAttachmenOnFileSystem()) {
			File file =  getStorage(data.getKey(), true);
			try(OutputStream out = new FileOutputStream(file)) {
				out.write(bytes);
			} catch(Exception ex) {
				log.error("", ex);
			}
			data.setDatas(new byte[0]);
		} else {
			data.setDatas(bytes);
		}
		data.setSize(bytes.length);
		data.setName(filename);
		data.setType(type);
		dbInstance.updateObject(data);
		return attachment;
	}
	
	public void removeAttachmentDatas(Attachment attachment) {
		if(attachment == null) return;
		
		if(recruitingModule.isAttachmenOnFileSystem()) {
			File file =  getStorage(attachment.getKey(), false);
			if(file.exists()) {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					log.error("Cannot delete: {}", file, e);
				}
			}
		}
	}
	
	public void removeAttachmentDatas(Long attachmentKey) {
		File file =  getStorage(attachmentKey, false);
		if(file.exists() && !file.delete()) {
			log.error("Cannot delete file: {}", file);
		}
	}
	
	private File getStorage(Long attachmentKey, boolean mkdir) {
		File bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		File attachments = new File(bcrootDirectory, "attachments");

		String value = attachmentKey.toString();
		String directory;
		switch(value.length()) {
			case 0: directory = "000"; break;
			case 1: directory = "00" + value; break;
			case 2: directory = "0" + value; break;
			case 3: directory = value; break;
			default: directory = value.substring(value.length() - 3, value.length()); break;
		}
		
		File subAttachments = new File(attachments, directory);
		if(mkdir && !subAttachments.exists()) {
			subAttachments.mkdirs();
		}
		
		String filename = attachmentKey + ".pdf";
		return new File(subAttachments, filename);
	}

	public byte[] getAttachmentDatas(Attachment attachment) {
		Long key = attachment.getKey();
		return getAttachmentDatas(key);
	}
	
	public byte[] getAttachmentDatas(Long attachmentKey) {
		byte[] content = null;
		if(recruitingModule.isAttachmenOnFileSystem()) {
			content = getAttachmentDatasFromFile(attachmentKey);
			if(content == null) {
				AttachmentDataImpl data = dbInstance
						.getCurrentEntityManager()
						.find(AttachmentDataImpl.class, attachmentKey);
				content = data.getDatas();
			}
		} else {
			AttachmentDataImpl data = dbInstance
					.getCurrentEntityManager()
					.find(AttachmentDataImpl.class, attachmentKey);
			content = data.getDatas();
			if(content == null || content.length == 0) {
				content = getAttachmentDatasFromFile(attachmentKey);
			}
		}
		return content;
	}
	
	private byte[] getAttachmentDatasFromFile(Long attachmentDataKey) {
		File file = getStorage(attachmentDataKey, false);
		if(file.exists()) {
			try(InputStream in = new FileInputStream(file)) {
				return IOUtils.toByteArray(in);
			} catch(Exception ex) {
				log.error("", ex);
			}
		}
		return null;
	}
}
