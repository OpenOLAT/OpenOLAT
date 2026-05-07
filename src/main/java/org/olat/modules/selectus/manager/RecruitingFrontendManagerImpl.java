/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.keystore.pkcs12.PKCS12KeyStoreSpi;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;
import org.olat.modules.selectus.AnonymiseService;
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.ParallelApplicationScope;
import org.olat.modules.selectus.RecruitingDuplicateApplicationAlgorithm;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.comparator.ApplicationCommentCreationDateComparator;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.AcceptPolicyImpl;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationAttributeLight;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationLightImpl;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.CommitteeMembershipSummary;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAndAttributeDefinition;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceComment;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.RejectionEmailLog;
import org.olat.modules.selectus.model.RejectionEmailLogFull;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.model.mail.SentEmailTemplates;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.pdf.PDFDataCache;
import org.olat.modules.selectus.pdf.PDFDataProvider;
import org.olat.modules.teams.manager.MicrosoftGraphDAO;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserLifecycleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class RecruitingFrontendManagerImpl implements RecruitingService, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(RecruitingFrontendManagerImpl.class);
	private static final String CHARSET = "23456789abcdefghjkmnpqrstuvwxyz23456789ABCDEFGHJKMNPQRSTUVWXYZ";
	private static final String EXPERT_OPINIONS_PREFIX = "EO-";

	@Autowired
	private DB dbInstance;
	@Autowired
	private NotesDAO notesDao;
	@Autowired
	private SelectusCategoryDAO categoryDao;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private RejectionDAO rejectionDao;
	@Autowired
	private ReferenceDAO referenceDao;
	@Autowired
	private SelectusAssignmentDAO assignmentDao;
	@Autowired
	private SecurityGroupDAO secGroupDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private MailTemplateDAO mailTemplateDao;
	@Autowired
	private AnonymiseService anonymiseService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private PublicFeedbackDAO publicFeedbackDao;
	@Autowired
	private DecisionRubricDAO decisionRubricDao;
	@Autowired
	private ReferenceCommentDAO referenceCommentDao;
	@Autowired
	private OrganisationUnitDAO organisationUnitDao;
	@Autowired
	private ApplicationCommentDAO applicationCommentDao;
	@Autowired
	private RecruitingAuditLogDAO recruitingAuditLogDao;
	@Autowired
	private ReportingCommitteeDAO reportingCommitteeDao;
	@Autowired
	private ApplicationFeedbackDAO applicationFeedbackDao;
	@Autowired
	private ApplicationCategoryDAO applicationCategoryDao;
	@Autowired
	private RecruitingAuditLogReadDAO recruitingAuditLogReadDao;
	@Autowired
	private ApplicationCommentVoteDAO applicationCommentVoteDao;
	@Autowired
	private ReferenceToApplicationDAO referenceToApplicationDao;
	@Autowired
	private ApplicationsFeedbackConfigurationDAO applicationsFeedbackConfigurationDao;
	
	@Autowired
	private ReviewResponseDAO reviewResponseDao;
	@Autowired
	private ReviewElementDefinitionDAO reviewElementDefinitionDao;
	@Autowired
	private PositionReviewDefinitionDAO positionReviewDefinitionDao;

	@Autowired
	private BaseSecurity baseSecurity;
	@Autowired
	private LDAPLoginModule ldapModule;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private MicrosoftGraphDAO microsoftGraphDao;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	
	private final PDFDataCache pdfCache = new PDFDataCache();
	private final BouncyCastleProvider bcProvider = new BouncyCastleProvider();
	private final PKCS12KeyStoreSpi.BCPKCS12KeyStore keyStore = new PKCS12KeyStoreSpi.BCPKCS12KeyStore();
	
	@Autowired
	public RecruitingFrontendManagerImpl(FolderModule folderModule) {
		log.debug("FolderModule loaded {}", folderModule);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		File rootCache = new File(new File(FolderConfig.getCanonicalTmpDir(), "selectus"), "combinedapps");
		pdfCache.setRootCache(rootCache);
		pdfCache.setUseCache(true);
		pdfCache.init();
	}

	@Override
	public Position createPosition(Organisation organisation) {
		String refereeDocs = recruitingModule.getDefaultPositionRefereeRecommendationDocs();
		String expertDocs = recruitingModule.getDefaultPositionExpertRecommendationDocs();
		return positionDao.createPosition(refereeDocs, expertDocs, organisation);
	}

	@Override
	public Position savePosition(Position position) {
		return positionDao.savePosition(position);
	}
	
	@Override
	public void deletePosition(Position position, Identity doer) {
		log.info("Delete position {}", position);
		deletePosition(position, false, doer);
	}
	
	@Override
	public void toReportPositionOnly(Position position, Identity doer) {
		log.info("Anonymize position {}", position);
		position = anonymiseService.anonymise(position);
		deletePosition(position, true, doer);
	}
	
	private void deletePosition(Position position, boolean reportOnly, Identity doer) {
		position = positionDao.loadPositionByKey(position.getKey());
		
		if(recruitingModule.isAttachmenOnFileSystem()) {
			if(position.getDocument1() != null) {
				applicationDao.removeAttachmentDatas(position.getDocument1());
			}
			if(position.getDocument2() != null) {
				applicationDao.removeAttachmentDatas(position.getDocument2());
			}
			if(position.getDocument3() != null) {
				applicationDao.removeAttachmentDatas(position.getDocument3());
			}
		}
		
		List<DecisionRubricDefinition> rubricDefs = decisionRubricDao.getDecisionRubricDefinition(position);
		for(DecisionRubricDefinition rubricDef:rubricDefs) {
			decisionRubricDao.deleteDefinition(rubricDef);
		}
		
		if(!reportOnly) {
			List<PositionAttributeDefinition> attributesDefs = position.getAttributesDefinitions();
			if(attributesDefs != null && !attributesDefs.isEmpty()) {
				for(PositionAttributeDefinition attributeDef:attributesDefs) {
					applicationDao.deleteAttributes(attributeDef);
				}
			}
		}

		List<Application> applications = applicationDao.findApplications(position, true);
		applications.addAll(applicationDao.findApplications(position, false));
		List<Identity> applicants = new ArrayList<>();
		
		// First remove comparative assessment
		for(Application application:applications) {
			referenceToApplicationDao.deleteReferencesToApplication(application);
		}
		
		for(Application application:applications) {
			if(application.getIdentity() != null) {
				applicants.add(application.getIdentity());
			}
			
			notesDao.deleteNotes(application);
			rejectionDao.deleteApplication(application);
			referenceDao.deleteReferences(application);
			reviewResponseDao.delete(application);
			decisionRubricDao.deleteRubrics(application);
			applicationCategoryDao.delete(application);
			
			List<ApplicationComment> comments = applicationCommentDao.getApplicationComments(application);
			if(!comments.isEmpty()) {
				Collections.sort(comments, new ApplicationCommentCreationDateComparator());
				for(ApplicationComment comment:comments) {
					applicationCommentVoteDao.deleteVotes(comment);
					applicationCommentDao.delete(comment);
				}
			}
			assignmentDao.deleteApplication(application);
			publicFeedbackDao.deleteApplication(application);
			applicationFeedbackDao.deleteApplication(application);
			
			if(!reportOnly || !application.isValid()) {
				applicationDao.deleteApplication(application);
			}
		}
		
		List<PositionMailTemplate> templates = mailTemplateDao.getTemplates(position);
		for(PositionMailTemplate template:templates) {
			mailTemplateDao.deleteTemplate(template);
		}
		
		PositionReviewDefinition reviewDefinition = null;
		if(position.getReviewDefinition() != null) {
			reviewDefinition = positionReviewDefinitionDao.loadByKey(position.getReviewDefinition().getKey());
		}
		recruitingAuditLogReadDao.delete(position);
		recruitingAuditLogDao.delete(position);
		
		mailTemplateDao.deleteTemplates(position);
		applicationsFeedbackConfigurationDao.deleteFeedbackConfigurations(position);

		if(reportOnly) {
			position.setStatus(PositionStatus.reporting.name());
			position.setReviewDefinition(null);
			position = positionDao.savePosition(position);
		} else {
			// Delete tags and reports
			categoryDao.delete(position);
			reportingCommitteeDao.deleteReportsCommittee(position);
			// Delete the position
			positionDao.deletePosition(position);
		}
		dbInstance.commit();
		
		pdfCache.forceDelete(position.getKey().toString());
		pdfCache.forceDelete(EXPERT_OPINIONS_PREFIX + position.getKey().toString());
		
		//delete reviews
		if(reviewDefinition != null) {
			List<ReviewElementDefinition> elements = reviewDefinition.getElements();
			for(ReviewElementDefinition element:elements) {
				if(element == null) continue;
				
				reviewResponseDao.delete(element);
				reviewElementDefinitionDao.delete(element);
			}
			positionReviewDefinitionDao.delete(reviewDefinition);
		}
		
		for(Identity applicant:applicants) {
			if(applicant != null && !isIdentityInUse(applicant)) {
				CoreSpringFactory.getImpl(UserLifecycleManager.class).deleteIdentity(applicant, doer);
			}
		}
	}

	@Override
	public Date getLastApplicationModification(Position position) {
		return positionDao.getLastApplicationModification(position);
	}

	@Override
	public Long getEstimatedSizeOfAttachment(Position position) {
		return positionDao.getEstimatedSizeOfAttachment(position, recruitingModule.getDocumentOptions(position));
	}

	@Override
	public boolean acceptPositionPolicy(Position position, Identity identity, AcceptPolicyEnum policyType, Boolean dontShowNextTime) {
		return positionDao.acceptPositionPolicy(position, identity, policyType, dontShowNextTime);
	}

	@Override
	public List<AcceptPolicyEnum> needToAcceptPositionPolicies(Position position, Identity member) {
		boolean message = StringHelper.containsNonWhitespace(position.getMessageToCommitte());
		List<AcceptPolicyEnum> toAccept = new ArrayList<>();
		if(message) {
			CommitteeMembershipSummary stats = positionDao.getCommitteeMembershipsStats(member, position);
			if(stats.isHead() || stats.isMember()) {
				toAccept.add(AcceptPolicyEnum.messageToCommittee);
			}
		}
		toAccept.add(AcceptPolicyEnum.ratingPolicy);
		
		List<AcceptPolicyImpl> policies = positionDao.loadAcceptPolicy(position, member);
		for(AcceptPolicyImpl policy:policies) {
			if(AcceptPolicyEnum.messageToCommittee.name().equals(policy.getName())) {
				// message to committee
				if(policy.isDontShowNextTime()) {
					toAccept.remove(AcceptPolicyEnum.messageToCommittee);
				}
			} else {
				// standard rating policy
				if(policy.isDontShowNextTime()) {
					toAccept.remove(AcceptPolicyEnum.ratingPolicy);
				}
			}	
		}
		return toAccept;
	}
	
	@Override
	public CommitteeMembershipsStats getCommitteeMembershipsStats(Identity identity) {
		return positionDao.getCommitteeMembershipsStats(identity);
	}

	@Override
	public Position getPosition(Long positionKey) {
		return positionDao.loadPositionByKey(positionKey);
	}

	@Override
	public List<Position> getPositions(PositionStatus... status) {
		List<PositionStatus> statusList = new ArrayList<>();
		for(PositionStatus state:status) {
			statusList.add(state);
		}
		return positionDao.findPositions(statusList, true);
	}

	@Override
	public List<Position> getPositionsToRemind() {
		return positionDao.loadPositionsToRemind();
	}

	@Override
	public long countPositions(PositionStatus... status) {
		List<PositionStatus> statusList = new ArrayList<>();
		for(PositionStatus state:status) {
			statusList.add(state);
		}
		return positionDao.countPositions(statusList, true);
	}

	@Override
	public List<Position> getPublishedPositions() {
		return positionDao.findPublishedPositions();
	}
	
	@Override
	public boolean hasPositions(Identity identity, Roles roles, PositionStatus... status) {
		List<PositionStatus> statusList = PositionStatus.toList(status);
		PositionStatusFilters filters = positionDao.getPositionStatusFilters(identity, roles, statusList);
		return positionDao.hasPositions(identity, filters, true);
	}
	
	@Override
	public List<PositionLightWithStatistics> getPositionsLightWithStatistics(Identity identity, Roles roles,
			List<PositionAttributeDefinition> globalAttributes, Locale locale, PositionStatus... status) {
		List<PositionStatus> statusList = PositionStatus.toList(status);
		PositionStatusFilters filters = positionDao.getPositionStatusFilters(identity, roles, statusList);
		return positionDao.findPositionsLightWithStatistics(identity, filters, globalAttributes, true, locale);
	}
	
	@Override
	public List<Reference> getPositionReferences(Position position, ReferenceType type, boolean onlyWithLetter) {
		return referenceDao.getReferences(position, type, onlyWithLetter);
	}

	@Override
	public PositionAttributeDefinition createAttributeDefinition(Position position, PositionApplicationAttributeTabEnum tab,
			PositionAttributeDefinitionTypeEnum attributeType, String label, String labelDe, boolean mandatory,
			String placeholder, String placeholderDe) {
		return positionDao.createAttributeDefinition(position, tab, attributeType, label, labelDe, mandatory, placeholder, placeholderDe);
	}

	@Override
	public PositionAndAttributeDefinition createAttributeDefinitionAndPersist(Position position, PositionApplicationAttributeTabEnum tab,
			PositionAttributeDefinitionTypeEnum attributeType, String label, String labelDe, boolean mandatory,
			String placeholder, String placeholderDe) {
		Position reloadedPosition = positionDao.loadPositionByKey(position.getKey());
		
		PositionAttributeDefinition newDefinition = positionDao.createAttributeDefinitionAndPersist(position, tab, attributeType,
				label, labelDe, mandatory, placeholder, placeholderDe);

		boolean added = false;
		List<PositionAttributeDefinition> definitions = reloadedPosition.getAttributesDefinitions();
		for(int i=0; i<definitions.size(); i++) {
			PositionAttributeDefinition definition = definitions.get(i);
			PositionApplicationAttributeTabEnum defTab = definition.getTabEnum();
			if(defTab.ordinal() > tab.ordinal()) {
				definitions.add(newDefinition);
				added = true;
				break;
			}
		}
		if(!added) {
			definitions.add(newDefinition);
		}
		
		reloadedPosition = positionDao.savePosition(reloadedPosition);
		return new PositionAndAttributeDefinition(reloadedPosition, newDefinition);
	}
	
	@Override
	public List<PositionAttributeDefinition> getGlobalAttributeDefinition() {
		return positionDao.getGlobalAttributeDefinition();
	}

	@Override
	public PositionAttributeDefinition updateAttributeDefinition(PositionAttributeDefinition attributeDefinition) {
		return positionDao.updateAttributeDefinition(attributeDefinition);
	}
	
	@Override
	public void persistAttributeDefinition(PositionAttributeDefinition attributeDefinition) {
		positionDao.persistAttributeDefinition(attributeDefinition);
	}

	@Override
	public Position deleteAttributeDefinition(Position position, PositionAttributeDefinition attributeDefinition) {
		applicationDao.deleteAttributes(attributeDefinition);
		if(position == null) {
			positionDao.deletePositionAttributeDefinition(attributeDefinition);
		} else if(position.getAttributesDefinitions().contains(attributeDefinition)) {
			position = positionDao.loadPositionByKey(position.getKey());
			position.getAttributesDefinitions().remove(attributeDefinition);
			position = positionDao.savePosition(position);
		}
		dbInstance.commit();
		return position;
	}

	@Override
	public ApplicationAttribute createAttribute(Position position, Application application, PositionAttributeDefinition attributeDefinition, String value) {
		return applicationDao.createApplicationAttribute(position, application, attributeDefinition, value);
	}	

	@Override
	public long getAttributeUsage(Position position, PositionAttributeDefinition attributeDefinition) {
		if(position == null) {
			return applicationDao.getGlobalAttributeUsage(attributeDefinition);
		}
		return applicationDao.getAttributeUsage(position, attributeDefinition);
	}

	@Override
	public boolean hasApplicationReferencePDFs(Position position, ReferenceType type) {
		return referenceDao.hasReferences(position, type);
	}

	@Override
	public boolean createOLATResource(Position position) {
		OLATResource ores = resourceManager.findOrPersistResourceable(position);
		return ores != null;
	}

	@Override
	public Application createTempApplication(Position position, boolean submittedByStaff) {
		return applicationDao.createTempApplication(position, submittedByStaff);
	}
	
	@Override
	public Application saveApplication(Application app) {
		if(app.getPosition().getKey() != null) {
			pdfCache.invalidate(app.getPosition().getKey().toString());
		}
		app = applicationDao.saveApplication(app);
		app.getPosition().getKey();
		return app;
	}

	@Override
	public void setDecision(Application app, int decision) {
		applicationDao.setDecision(app, decision);
		log.info(Tracing.M_AUDIT,"Set decision " + decision + " for application: " + app.toString());
	}

	@Override
	public Application saveTempApplication(Application app, boolean removeTempFlag) {
		if(app.getPosition().getKey() != null) {
			pdfCache.invalidate(app.getPosition().getKey().toString());
		}
		Application mergedApp = applicationDao.saveTempApplication(app, removeTempFlag);
		mergedApp.getPosition().getKey();
		return mergedApp;
	}

	@Override
	public boolean checkUniqueApplication(Application app) {
		RecruitingDuplicateApplicationAlgorithm duplicateAlgorithm = recruitingModule.getApplicationDuplicateAlgorithm();
		if(duplicateAlgorithm == RecruitingDuplicateApplicationAlgorithm.EMAIL_FIRST_LAST_NAME) {
			return applicationDao.checkUniqueApplicationByEmailFistnameLastname(app);
		}
		return applicationDao.checkUniqueApplicationByEmail(app);
	}

	@Override
	public void sendToApplicant(Application app, Position position, ApplicationMailTemplate template, boolean withBcc) {
		MailerResult mailerResult = new MailerResult();
		try {
			String to = app.getPerson().getMail();
			String bcc = null;
			if(withBcc && recruitingModule.isSendBccForConfirmation()) {
				OrganisationUnit organisationSettings = organisationUnitDao.loadOrganisationUnitByPosition(position);
				bcc = recruitingModule.getBccStaffMail(position, organisationSettings);
			}
			new MailerSender(bcProvider, keyStore).send(to, bcc, app, null, position, null, null, null, null, template, mailerResult);
			if(mailerResult.getReturnCode() != MailerResult.OK) {
				log.error("Cannot send confirmation email");
			}
		} catch (Exception e) {
			log.error("Cannot send confirmation email", e);
		}
	}

	@Override
	public void sendReminder(Position position, Identity member, ApplicationMailTemplate template) {
		MailerResult mailerResult = new MailerResult();
		try {
			String to = member.getUser().getProperty(UserConstants.EMAIL, null);
			new MailerSender(bcProvider, keyStore).send(to, null, null, null, position, null, member, null, null, template, mailerResult);
			if(mailerResult.getReturnCode() != MailerResult.OK) {
				log.error("Cannot send reminder to committee member: {}", member);
			}
		} catch (Exception e) {
			log.error("Cannot send reminder to committee member: {}", member, e);
		}
	}

	@Override
	public void sendMail(String to, String subject, String body) {
		MailerResult mailerResult = new MailerResult();
		try {
			new MailerSender(bcProvider, keyStore).send(null, to, null, subject, body, null, mailerResult);
			if(mailerResult.getReturnCode() != MailerResult.OK) {
				log.error("Cannot send email to staff");
			}
		} catch (Exception e) {
			log.error("Cannot send email to staff", e);
		}
	}

	@Override
	public void deleteApplication(Application app, Identity doer) {
		if(app.getPosition().getKey() != null) {
			String cacheKey = app.getPosition().getKey().toString();
			pdfCache.invalidate(cacheKey);
			pdfCache.invalidate(EXPERT_OPINIONS_PREFIX + cacheKey);
		}
		notesDao.deleteNotes(app);
		rejectionDao.deleteApplication(app);
		
		referenceToApplicationDao.deleteReferencesToApplication(app);
		// Load all references
		List<Reference> references = referenceDao.getReferences(app, null);
		for(Reference reference:references) {
			referenceToApplicationDao.deleteReferenceToApplications(reference);
		}
		referenceDao.deleteReferences(app);
		decisionRubricDao.deleteRubrics(app);
		reviewResponseDao.delete(app);
		applicationCategoryDao.delete(app);
		assignmentDao.deleteApplication(app);
		publicFeedbackDao.deleteApplication(app);
		applicationFeedbackDao.deleteApplication(app);
		
		List<ApplicationComment> comments = applicationCommentDao.getApplicationComments(app);
		if(!comments.isEmpty()) {
			Collections.sort(comments, new ApplicationCommentCreationDateComparator());
			for(ApplicationComment comment:comments) {
				applicationCommentVoteDao.deleteVotes(comment);
				applicationCommentDao.delete(comment);
			}
		}
		
		Identity applicant = app.getIdentity();
		applicationDao.deleteApplication(app);
		dbInstance.commit();
		
		if(applicant != null && !isIdentityInUse(applicant)) {
			CoreSpringFactory.getImpl(UserLifecycleManager.class).deleteIdentity(applicant, doer);
		}
	}
	
	private boolean isIdentityInUse(Identity identity) {
		Roles roles = baseSecurity.getRoles(identity);
		if(roles.isManager()) {
			return true;
		}
		if(positionDao.isInCommittee(identity)) {
			return true;
		}
		
		return applicationDao.hasApplicationsByApplicant(identity);
	}

	@Override
	public Application getApplication(ApplicationShort app) {
		if(app == null || app.getKey() == null) return null;
		return getApplicationByKey(app.getKey());
	}

	@Override
	public Application getApplicationByKey(Long appKey) {
		return applicationDao.loadApplicationByKey(appKey);
	}

	@Override
	public Application getApplicationByPublicFeedbackKey(String key) {
		return applicationDao.loadApplicationByPublicFeedbackKey(key);
	}
	
	@Override
	public Application getApplicationByApplicantKey(String key) {
		return applicationDao.loadApplicationByApplicantKey(key);
	}

	@Override
	public boolean hasApplicationByIdentity(IdentityRef identity) {
		return applicationDao.hasApplicationByIdentity(identity);
	}

	@Override
	public List<Application> getCurrentApplicationsByApplicant(IdentityRef identity) {
		return applicationDao.loadCurrentApplicationsByApplicant(identity);
	}

	@Override
	public Application getApplicationToEdit(ApplicationRef app) {
		Application application = getApplicationByKey(app.getKey());
		if(!StringHelper.containsNonWhitespace(application.getPublicFeedbackKey())
				&& recruitingModule.isPublicFeedbackEnabled()) {
			application.setPublicFeedbackKey(UUID.randomUUID().toString().replace("-", ""));
			applicationDao.saveApplication(application);
			dbInstance.commitAndCloseSession();
			application = getApplicationByKey(app.getKey());
		}	
		return application;
	}

	@Override
	public Application getApplicationWithAttributes(ApplicationRef app) {
		if(app == null || app.getKey() == null) return null;
		Application application = applicationDao.loadApplicationByKey(app.getKey());
		if(application != null) {
			application.getAttributes().size();// load the attributes
		}
		return application;
	}

	@Override
	public ApplicationLight getApplicationLight(Position position, ApplicationRef app) {
		return applicationDao.loadApplicationLightByKeyWithAttributes(position, app.getKey());
	}

	@Override
	public List<ApplicationLight> getApplications(Position position) {
		return applicationDao.findApplicationsLight(position, true);
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

	@Override
	public List<ApplicationLight> getApplications(Position position, List<Integer> limitDecisions) {
		return applicationDao.findApplicationsLight(position, limitDecisions);
	}

	@Override
	public List<ApplicationRefereeStats> getApplicationReviewerStats(Position position) {
		return applicationDao.findApplicationReviewerStats(position, true);
	}

	@Override
	public List<Application> searchApplications(String searchText, Identity identity, Roles roles, List<PositionStatus> status) {
		if(status == null) {
			status = new ArrayList<>();
		}
		PositionStatusFilters filters = positionDao.getPositionStatusFilters(identity, roles, status);
		return applicationDao.searchApplications(searchText, identity, filters);
	}

	@Override
	public List<ApplicationLight> getApplicationsWithoutDecision(Position position) {
		return applicationDao.findApplicationsLightWithoutDecision(position);
	}

	@Override
	public List<ApplicationLight> getApplicationsWithCDecision(Position position) {
		return applicationDao.findApplicationsLightWithDecisions(position, Collections.singletonList(1),
				null, false, Collections.emptyList(), Collections.emptyList(), false);
	}
	
	@Override
	public List<ApplicationLight> getApplicationsWithDecisions(Position position, List<Integer> decisions,
			List<ApplicationStatus> status, boolean noDecision, List<String> excludeTemplateNames, boolean excludeSendEmails) {
		List<String> currentTemplatesName = new ArrayList<>();
		if(excludeTemplateNames != null && !excludeTemplateNames.isEmpty()) {
			for(String mailTemplate:recruitingModule.getMailTemplateTitles()) {
				currentTemplatesName.add(mailTemplate);
			}
			List<PositionMailTemplate> currentPositionTemplates = mailTemplateDao.getTemplates(position);
			for(PositionMailTemplate currentPositionTemplate:currentPositionTemplates) {
				currentTemplatesName.add(currentPositionTemplate.getKey().toString());
				currentTemplatesName.add(currentPositionTemplate.getName());
			}
		}
		return applicationDao.findApplicationsLightWithDecisions(position, decisions, status, noDecision,
				excludeTemplateNames, currentTemplatesName, excludeSendEmails);
	}

	@Override
	public List<PositionLight> getParallelApplications(Application application, Position position, ParallelApplicationScope scope) {
		List<PositionLight> apps;
		if(application != null && application.getPerson() != null && application.getPerson().getMail() != null) {
			String email = application.getPerson().getMail();
			Long positionKey = position.getKey();
			Long organisationKey = null;
			if(scope == ParallelApplicationScope.organisation && position.getOrganisation() != null) {
				organisationKey = position.getOrganisation().getKey();
			}
			apps = positionDao.findParallelApplicationsLight(email, positionKey, organisationKey);
		} else {
			apps = Collections.emptyList();
		}
		return apps;
	}
	
	@Override
	public List<ParallelApplication> getParallelApplications(Position position, ParallelApplicationScope scope) {
		Long organisationKey = null;
		if(scope == ParallelApplicationScope.organisation && position.getOrganisation() != null) {
			organisationKey = position.getOrganisation().getKey();
		}
		return positionDao.findParallelApplications(position.getKey(), organisationKey);
	}

	@Override
	public List<Application> getApplicationsWithAttachment(Position position) {
		return applicationDao.findApplications(position, true);
	}

	@Override
	public Position deleteAttachment(Position position, Attachment attachment) {
		if(attachment != null && recruitingModule.isAttachmenOnFileSystem()) {
			applicationDao.removeAttachmentDatas(attachment.getKey());
		}
		return positionDao.deleteAttachment(position, attachment);
	}

	@Override
	public Attachment setAttachmentDatas(Attachment attachment, String filename, DocumentType fileType, byte[] bytes) {
		String type = fileType == null ? DocumentType.pdf.name() : fileType.name();
		return applicationDao.setAttachmentDatas(attachment, filename, type, bytes);
	}
	
	@Override
	public Attachment setAttachmentDatas(Application app, Attachment attachment, byte[] bytes, String filename, DocumentType fileType) {
		if(app.getPosition().getKey() != null) {
			pdfCache.invalidate(app.getPosition().getKey().toString());
		}
		String type = fileType == null ? DocumentType.pdf.name() : fileType.name();
		return applicationDao.setAttachmentDatas(attachment, filename, type, bytes);
	}

	@Override
	public void removeAttachmentDatas(Application application, Attachment attachment) {
		applicationDao.removeAttachmentDatas(attachment);
	}

	@Override
	public byte[] getAttachmentDatas(Attachment attachment) {
		return applicationDao.getAttachmentDatas(attachment);
	}
	
	@Override
	public byte[] getAttachmentDatas(Long attachmentKey) {
		return applicationDao.getAttachmentDatas(attachmentKey);
	}

	@Override
	public Reference addReference(String title, String firstName, String lastName, String institution, String email, Date submissionDeadline,
			ReferenceType type, ReferenceRequestStatus requestStatus, String adminNote,
			Application application, List<Application> applicationsToCompare) {
		Reference reference = referenceDao.createReference(title, firstName, lastName, institution, email, submissionDeadline,
				type, requestStatus, adminNote, application);
		if(applicationsToCompare != null) {
			for(Application app:applicationsToCompare) {
				referenceToApplicationDao.createRelation(reference, app);
			}
		}
		return reference;
	}
	
	@Override
	public List<ReferenceToApplication> getReferenceToApplications(Reference reference) {
		return referenceToApplicationDao.getReferenceToApplications(reference);
	}
	
	@Override
	public List<ReferenceToApplication> getReferenceToApplications(List<Reference> references) {
		return referenceToApplicationDao.getReferenceToApplications(references);
	}
	
	@Override
	public List<Application> getReferenceToApplicationsList(Reference reference) {
		return referenceToApplicationDao.getReferenceToApplicationsList(reference);
	}
	
	@Override
	public List<ReferenceToApplication> getReferenceToApplications(PositionRef position) {
		return referenceToApplicationDao.getReferenceToApplications(position);
	}

	@Override
	public Reference updateReference(Reference reference) {
		return referenceDao.updateReference(reference);
	}

	@Override
	public void addReferenceToApplication(Reference reference, Application application) {
		List<ReferenceToApplication> refToApps = referenceToApplicationDao.getReferenceToApplications(reference, application);
		if(refToApps.isEmpty()) {
			referenceToApplicationDao.createRelation(reference, application);
		}
	}

	@Override
	public void deleteReferenceToApplications(Reference reference, ApplicationShort application) {
		List<ReferenceToApplication> refToApps = referenceToApplicationDao.getReferenceToApplications(reference);
		for(ReferenceToApplication refToApp:refToApps) {
			if(reference.equals(refToApp.getReference()) && application.getKey().equals(refToApp.getApplication().getKey())) {
				referenceToApplicationDao.deleteReferenceToApplication(refToApp);
			}
		}
		dbInstance.commit();
	}

	@Override
	public Attachment setAttachmentDatas(Position position, Reference reference, Attachment attachment, String filename, DocumentType fileType, byte[] bytes) {
		if(reference.getReferenceType() == ReferenceType.expert) {
			if(position.getKey() != null) {
				pdfCache.invalidate(EXPERT_OPINIONS_PREFIX + position.getKey().toString());
			}
		}
		String type = fileType == null ? DocumentType.pdf.name() : fileType.name();
		return applicationDao.setAttachmentDatas(attachment, filename, type, bytes);
	}

	@Override
	public Reference deleteAttachment(Reference reference, Attachment attachment) {
		if(attachment != null && recruitingModule.isAttachmenOnFileSystem()) {
			Long attachmentKey = attachment.getKey();
			applicationDao.removeAttachmentDatas(attachmentKey);
		}
		return referenceDao.deleteAttachment(reference, attachment);
	}

	@Override
	public List<Reference> getApplicationReferences(Application application, ReferenceType type) {
		return referenceDao.getReferences(application, type);
	}
	
	@Override
	public Reference getReferenceById(Long key) {
		return referenceDao.loadByKey(key);
	}

	@Override
	public Reference getReferenceBySubmissionUrl(String url) {
		return referenceDao.loadBySubmissionUrl(url);
	}

	@Override
	public List<Reference> getReferences(ReferenceSearchParameters params) {
		return referenceDao.getReferences(params);
	}
	
	@Override
	public boolean hasReferenceWithEmail(Application app, Reference reference, String email) {
		return referenceDao.hasReferenceWithEmail(app, reference, email);
	}

	@Override
	public void deleteReference(Reference reference) {
		referenceToApplicationDao.deleteReferenceToApplications(reference);
		referenceCommentDao.deleteComments(reference);
		referenceDao.deleteReference(reference);
	}

	@Override
	public ReferenceComment addReferenceComment(Reference reference, String comment) {
		return referenceCommentDao.createComment(reference, comment);
	}

	@Override
	public List<ReferenceComment> getComments(Reference reference) {
		return referenceCommentDao.getComments(reference);
	}

	@Override
	public List<Long> getReferencesWithComments(Position position) {
		return referenceCommentDao.getReferencesWithComments(position);
	}

	@Override
	public Long streamSize(Position position) {
		if(position.getKey() == null) return 0l;
		String cacheKey = position.getKey().toString();
		return pdfCache.getSize(cacheKey);
	}

	@Override
	public Long streamExpertOpinionsSize(Position position) {
		if(position.getKey() == null) return 0l;
		String cacheKey = position.getKey().toString();
		return pdfCache.getSize(EXPERT_OPINIONS_PREFIX + cacheKey);
	}

	@Override
	public void deleteCachedStream(Position position) {
		if(position.getKey() == null) return;
		String cacheKey = position.getKey().toString();
		pdfCache.forceDelete(cacheKey);
		pdfCache.forceDelete(EXPERT_OPINIONS_PREFIX + cacheKey);
	}

	@Override
	public Long stream(Position position, PDFDataProvider provider, OutputStream out) {
		if(position.getKey() == null) return 0l;
		String cacheKey = position.getKey().toString();
		return pdfCache.stream(cacheKey, provider, out);
	}
	
	@Override
	public Long streamExpertOpinions(Position position, PDFDataProvider provider, OutputStream out) {
		if(position.getKey() == null) return 0l;
		String cacheKey = EXPERT_OPINIONS_PREFIX + position.getKey().toString();
		return pdfCache.stream(cacheKey, provider, out);
	}

	@Override
	public Identity getCommitteeMember(String email) {
		Map<String,String> props = new HashMap<>();
		props.put(UserConstants.EMAIL, email);
		List<Identity> ids = baseSecurity.getIdentitiesByPowerSearch(null, props, false, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		if(ids.size() == 1) {
			return ids.get(0);
		}
		//not found as OLAT user. Try LDAP
		if(ldapModule.isLDAPEnabled() && ldapModule.isLdapLookupEnabled()) {
			LdapContext ldapContext = ldapManager.bindSystem();
			try {
				Attributes userAttrs = ldapManager.findByEmail(email, ldapContext);
				if (userAttrs == null) {
					return null;
				} else {
					ldapManager.createAndPersistUser(userAttrs);
					ids = baseSecurity.getIdentitiesByPowerSearch(null, props, false, null, null, null, null, null, null, Identity.STATUS_ACTIV);
					if(ids.size() == 1) {
						return ids.get(0);
					}
				}
			} catch(Exception e) {
				log.error("Error synching a new identity", e);
			} finally {
				try {
					ldapContext.close();
				} catch (Exception e) {
					//fail silently
				}
			}
		}
		
		return null;
	}

	@Override
	public Identity findCommitteeMember(String email, OAuth2Tokens oauthTokens) {
		Map<String,String> props = new HashMap<>();
		props.put(UserConstants.EMAIL, email);
		List<Identity> ids = baseSecurity.getIdentitiesByPowerSearch(null, props, false, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		if(ids.size() == 1) {
			return ids.get(0);
		}
		
		if(oauthLoginModule.isAzureAdfsEnabled() && oauthLoginModule.isAzureLookupEnabled() && oauthTokens != null) {
			TeamsErrors errors = new TeamsErrors();
			List<TransientIdentity> azureIdentities = microsoftGraphDao.toIdentity(microsoftGraphDao.searchUsersByMail(email, email, oauthTokens, errors));
			if(!azureIdentities.isEmpty()) {
				return azureIdentities.get(0);
			}
		}
		
		//not found as OLAT user. Try LDAP
		if(ldapModule.isLDAPEnabled() && ldapModule.isLdapLookupEnabled()) {
			LdapContext ldapContext = ldapManager.bindSystem();
			try {
				Attributes userAttrs = ldapManager.findByEmail(email, ldapContext);
				if (userAttrs == null) {
					return null;
				} else {
					TransientIdentity id = ldapManager.createTransientIdentity(userAttrs);
					if(id != null) {
						return id;
					}
				}
			} catch(Exception e) {
				log.error("Error synching a new identity", e);
			} finally {
				try {
					ldapContext.close();
				} catch (Exception e) {
					//fail silently
				}
			}
		}
		
		return null;
	}

	@Override
	public List<Identity> getCommitteeMembers(PositionRef position) {
		SecurityGroup committeeGroup = positionDao.getMemberGroup(position, PositionRole.member);
		return secGroupDao.getIdentitiesOfSecurityGroup(committeeGroup);
	}
	
	@Override
	public int countCommittee(Position position, PositionRole... roles) {
		List<SecurityGroup> secGroups = new ArrayList<>(4);
		if(roles != null && roles.length > 0 && roles[0] != null) {
			for(PositionRole role:roles) {
				SecurityGroup group = null;
				switch(role) {
					case member: group = position.getCommitteeGroup(); break;
					case head: group = position.getCommitteeHeadGroup(); break;
					case secretary: group = position.getSecretaryGroup(); break;
					case exofficio: group = position.getExOfficioGroup(); break;	
				}
				
				if(group != null) {
					secGroups.add(group);
				}
			}
		}
		return secGroupDao.countIdentitiesOfSecurityGroups(secGroups);
	}

	@Override
	public List<Identity> getCommittee(Position position, PositionRole... roles) {
		List<SecurityGroup> secGroups = new ArrayList<>(4);
		if(roles != null && roles.length > 0 && roles[0] != null) {
			for(PositionRole role:roles) {
				SecurityGroup group = null;
				switch(role) {
					case member: group = position.getCommitteeGroup(); break;
					case head: group = position.getCommitteeHeadGroup(); break;
					case secretary: group = position.getSecretaryGroup(); break;
					case exofficio: group = position.getExOfficioGroup(); break;	
				}
				
				if(group != null) {
					secGroups.add(group);
				}
			}
		}
		return secGroupDao.getIdentitiesOfSecurityGroups(secGroups);
	}

	@Override
	public List<IdentityRef> getCommitteeRefs(Position position, PositionRole... roles) {
		List<SecurityGroup> secGroups = new ArrayList<>(4);
		if(roles != null && roles.length > 0 && roles[0] != null) {
			for(PositionRole role:roles) {
				SecurityGroup group = null;
				switch(role) {
					case member: group = position.getCommitteeGroup(); break;
					case head: group = position.getCommitteeHeadGroup(); break;
					case secretary: group = position.getSecretaryGroup(); break;
					case exofficio: group = position.getExOfficioGroup(); break;	
				}
				
				if(group != null) {
					secGroups.add(group);
				}
			}
		}
		return secGroupDao.getIdentityRefsOfSecurityGroups(secGroups);
	}

	@Override
	public Identity createCommitteeIdentity(String username, User newUser, boolean ldap, boolean azure,
			Position position, Organisation organisation, Identity doer) {
		// Init preferences
		newUser.getPreferences().setLanguage("en");
		newUser.getPreferences().setInformSessionTimeout(true);
		// Save everything in database
		Identity ident;
		if(ldap) {
			ident = baseSecurity.createAndPersistIdentityAndUserWithOrganisation(null, username, null, newUser,
					LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER, null, username, null, organisation, null, doer);
			log.info("Created committee LDAP user username::{}", username);
		} else if(azure) {
			ident = baseSecurity.createAndPersistIdentityAndUserWithOrganisation(null, username, null, newUser,
					MicrosoftAzureADFSProvider.PROVIDER, BaseSecurity.DEFAULT_ISSUER, null, username, null, organisation, null, doer);
			log.info("Created committee Azure user username::{}", username);
		} else {
			ident = baseSecurity.createAndPersistIdentityAndUserWithOrganisation(null, username, null, newUser,
					null, null, null, username, null, organisation, null, doer);
			log.info("Created committee user username::{}", username);
		}

		return ident;
	}

	@Override
	public void addToCommittee(Position position, Identity member) {
		positionDao.removeSecretaryFromCommittee(position, member);
		positionDao.removeHeadFromCommittee(position, member);
		positionDao.removeExOfficioFromCommittee(position, member);
		positionDao.addMemberToCommittee(position, member);
		log.info(Tracing.M_AUDIT,member.getName() + "(" + member.getKey() + ")"  + " added to committee: " + position.toString());
	}

	@Override
	public void removeFromCommitte(Position position, Identity member) {
		positionDao.removeMemberFromCommittee(position, member);
		positionDao.removeSecretaryFromCommittee(position, member);
		positionDao.removeExOfficioFromCommittee(position, member);
		positionDao.removeHeadFromCommittee(position, member);
		assignmentDao.removeAssignee(position, member);
		log.info(Tracing.M_AUDIT,member.getName() + "(" + member.getKey() + ")"  + " removed from committee: " + position.toString());
	}

	@Override
	public void addToCommitteeAsHead(Position position, Identity head) {
		position = positionDao.loadPositionByKey(position.getKey());
		positionDao.removeMemberFromCommittee(position, head);
		positionDao.removeSecretaryFromCommittee(position, head);
		positionDao.removeExOfficioFromCommittee(position, head);
		positionDao.addHeadToCommittee(position, head);
		log.info(Tracing.M_AUDIT, "{} ({}) added as head in committee: {}", head.getName(), head.getKey(), position);
	}
	
	@Override
	public void addToCommitteeAsSecretary(Position position, Identity secretary) {
		position = positionDao.loadPositionByKey(position.getKey());
		positionDao.removeMemberFromCommittee(position, secretary);
		positionDao.removeHeadFromCommittee(position, secretary);
		positionDao.removeExOfficioFromCommittee(position, secretary);
		positionDao.addSecretaryToCommittee(position, secretary);
		log.info(Tracing.M_AUDIT, "{} ({}) added as secretary in committee: {}", secretary.getName(), secretary.getKey(), position);
	}
	
	@Override
	public void addToCommitteeAsExOfficio(Position position, Identity exOfficio) {
		position = positionDao.loadPositionByKey(position.getKey());
		positionDao.removeMemberFromCommittee(position, exOfficio);
		positionDao.removeHeadFromCommittee(position, exOfficio);
		positionDao.removeSecretaryFromCommittee(position, exOfficio);
		positionDao.addExOfficioToCommittee(position, exOfficio);
		log.info(Tracing.M_AUDIT, "{} ({}) added as ex-officio in committee: {}", exOfficio.getName(), exOfficio.getKey(), position);
	}

	@Override
	public List<Identity> getHeads(Position position) {
		return secGroupDao.getIdentitiesOfSecurityGroup(position.getCommitteeHeadGroup());
	}
	
	@Override
	public Identity getHeadOfCommittee(Position position) {
		Identity headOfCommittee = null;
		List<Identity> headsOfCommittee = getHeads(position);
		if(headsOfCommittee != null && !headsOfCommittee.isEmpty()) {
			headOfCommittee = headsOfCommittee.get(0);
		}
		return headOfCommittee;
	}

	@Override
	public Identity getSecretary(Position position) {
		List<Identity> secretaries = getSecretaries(position);
		if(secretaries != null && !secretaries.isEmpty()) {
			return secretaries.get(0);
		}
		return null;
	}

	@Override
	public List<Identity> getSecretaries(Position position) {
		return secGroupDao.getIdentitiesOfSecurityGroup(position.getSecretaryGroup());
	}

	@Override
	public List<Identity> getExOfficios(Position position) {
		return secGroupDao.getIdentitiesOfSecurityGroup(position.getExOfficioGroup());
	}

	@Override
	public PositionRole getRole(Position position, IdentityRef identity) {
		PositionRole role = null;
		
		List<SecurityGroup> secGroups = secGroupDao.getSecurityGroupsForIdentity(identity);
		for(SecurityGroup group:secGroups) {
			if(group.equals(position.getCommitteeGroup())) {
				role = PositionRole.member;
			} else if(group.equals(position.getCommitteeHeadGroup())) {
				role = PositionRole.head;
			} else if(group.equals(position.getSecretaryGroup())) {
				role = PositionRole.secretary;
			} else if(group.equals(position.getExOfficioGroup())) {
				role = PositionRole.exofficio;
			}
		}
		
		return role;
	}

	@Override
	public Notes createNotes(Long application, Identity identity, String content) {
		return notesDao.createNotes(application, identity, content);
	}

	@Override
	public Notes updateNotes(Long application, Identity identity, String content) {
		return notesDao.updateNotes(application, identity, content);
	}

	@Override
	public Notes getNotes(Long application, Identity identity) {
		return notesDao.getNotes(application, identity);
	}

	@Override
	public List<Notes> getNotes(Position position, Identity identity) {
		return notesDao.getNotes(position, identity);
	}

	@Override
	public List<UserRating> getRatings(Position position, List<? extends IdentityRef> committee) {
		if(committee.size() == 1) {
			return positionDao.getRatings(position, committee.get(0));
		}
		return positionDao.getRatings(position, committee);
	}

	@Override
	public List<UserRating> getRatings(Application application, List<? extends IdentityRef> committee) {
		return applicationDao.getRatings(application, committee);
	}
	
	@Override
	public List<UserRating> getRatings(Position position, ApplicationRef application, List<? extends IdentityRef> committee) {
		return applicationDao.getRatings(position, application, committee);
	}
	
	@Override
	public UserRating getRating(Application application, Identity committeeMember) {
		String resSubPath = application.getKey().toString();
		Position position = application.getPosition();
		return userRatingsDao.getRating(committeeMember, position, resSubPath);
	}
	
	@Override
	public UserRating getRating(Position position, ApplicationLight application, Identity committeeMember) {
		String resSubPath = application.getKey().toString();
		return userRatingsDao.getRating(committeeMember, position, resSubPath);
	}
	
	@Override
	public UserRating setRating(Application application, Identity committeeMember, int rating)
	throws RatingClosedException {
		Integer decision = applicationDao.getApplicationDecision(application.getKey());
		if(decision != null && decision.intValue() > 0) {
			UserRating lastRating = getRating(application, committeeMember);
			throw new RatingClosedException(lastRating);
		}
		
		try {
			String resSubPath = application.getKey().toString();
			Position position = application.getPosition();
			UserRating userRating = userRatingsDao.getRating(committeeMember, position, resSubPath);
			if(userRating == null) {
				if(rating > -1 || rating == ABSTENTION) {
					logRating("Set", application, committeeMember, rating);
					return userRatingsDao.createRating(committeeMember, position, resSubPath, rating);
				}
				return null;
			} else {
				if(rating > -1 || rating == ABSTENTION) {
					logRating("Update", application, committeeMember, rating);
					return userRatingsDao.updateRating(userRating, rating);
				}
				logRating("Delete", application, committeeMember, rating);
				userRatingsDao.deleteRating(userRating);
				return null;
			}
		} catch (Exception e) {
			log.error("Unexpected error while rating:", e);
			return null;
		}
	}

	@Override
	public void removeRating(Application application, Identity committeeMember) throws RatingClosedException {
		Integer decision = applicationDao.getApplicationDecision(application.getKey());
		if(decision != null && decision.intValue() > 0) {
			UserRating lastRating = getRating(application, committeeMember);
			throw new RatingClosedException(lastRating);
		}
		
		try {
			String resSubPath = application.getKey().toString();
			Position position = application.getPosition();
			UserRating userRating = userRatingsDao.getRating(committeeMember, position, resSubPath);
			if(userRating != null) {
				logRating("Delete", application, committeeMember, 0);
				userRatingsDao.deleteRating(userRating);
			}
		} catch (Exception e) {
			log.error("Unexpected error while rating:", e);
		}
	}

	@Override
	public UserRating setRating(Position position, ApplicationLight application, Identity committeeMember, int rating)
	throws RatingClosedException {
		Integer decision = applicationDao.getApplicationDecision(application.getKey());
		if(decision != null && decision.intValue() > 0) {
			UserRating lastRating = getRating(position, application, committeeMember);
			throw new RatingClosedException(lastRating);
		}
		
		try {
			String resSubPath = application.getKey().toString();
			UserRating userRating = userRatingsDao.getRating(committeeMember, position, resSubPath);
			if(userRating == null) {
				if(rating > -1 || rating == ABSTENTION) {
					logRating("Set", position, application, committeeMember, rating);
					return userRatingsDao.createRating(committeeMember, position, resSubPath, rating);
				}
				return null;
			} else {
				if(rating > -1 || rating == ABSTENTION) {
					logRating("Update", position, application, committeeMember, rating);
					return userRatingsDao.updateRating(userRating, rating);
				}
				logRating("Delete", position, application, committeeMember, rating);
				userRatingsDao.deleteRating(userRating);
				return null;
			}
		} catch (Exception e) {
			log.error("Unexpected error while rating:", e);
			return null;
		}
	}
	
	@Override
	public void removeRating(Position position, ApplicationLight application, Identity committeeMember)
	throws RatingClosedException {
		Integer decision = applicationDao.getApplicationDecision(application.getKey());
		if(decision != null && decision.intValue() > 0) {
			UserRating lastRating = getRating(position, application, committeeMember);
			throw new RatingClosedException(lastRating);
		}
		
		try {
			String resSubPath = application.getKey().toString();
			UserRating userRating = userRatingsDao.getRating(committeeMember, position, resSubPath);
			if(userRating != null) {
				logRating("Delete", position, application, committeeMember, 0);
				userRatingsDao.deleteRating(userRating);
			}
		} catch (Exception e) {
			log.error("Unexpected error while rating:", e);
		}
	}

	private void logRating(String msg, Position position, ApplicationLight application, Identity committeeMember, int rating) {
		if(application == null) {
			log.info(Tracing.M_AUDIT,"Set rating: application is null");
		} else if (committeeMember == null) {
			log.info(Tracing.M_AUDIT,"Set rating: committee member is null");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(msg).append(" rating (position=").append(position.toString())
				.append(") ").append(rating).append(" by committee member: ")
				.append(committeeMember.getName()).append(" for application:")
				.append(application.getKey()).append(" ").append(application.getPerson());
		}
	}
	
	private void logRating(String msg, Application application, Identity committeeMember, int rating) {
		if(application == null) {
			log.info(Tracing.M_AUDIT,"Set rating: application is null");
		} else if (committeeMember == null) {
			log.info(Tracing.M_AUDIT,"Set rating: committee member is null");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(msg).append(" rating (position=").append(application.getPosition().toString())
				.append(") ").append(rating).append(" by committee member: ")
				.append(committeeMember.getName()).append(" for application:")
				.append(application.getKey()).append(" ").append(application.getPerson());
		}
	}
	
	@Override
	public String generatePassword() {
		StringBuilder sb = new StringBuilder();
		int countNum = 0;
		for (int i = 0; i < 8; i++) {
			double dPos = Math.random() * CHARSET.length();
			long pos = Math.round(dPos);
			if(pos >= CHARSET.length()) {
				pos = CHARSET.length() - 1l;
			}
			char ch = CHARSET.charAt((int)pos);
			if(Character.isDigit(ch)) {
				countNum++;
			}
			sb.append(ch);
		}
		
		//make a OLAT-compatible password
		for (int i = countNum; i < 2; i++) {
			double dPos = Math.random() * 10;
			long pos = Math.round(dPos);
			if(pos >= CHARSET.length()) {
				pos = CHARSET.length() - 1l;
			}
			char ch = CHARSET.charAt((int)pos);
			sb.append(ch);
		}
		return sb.toString();
	}
	
	@Override
	public MailerSender createMailSender() {
		return new MailerSender(bcProvider, keyStore);
	}
	
	@Override
	public RejectionEmailLogFull getFullLog(RejectionEmailLog mailLog) {
		return rejectionDao.getFullLog(mailLog);
	}

	@Override
	public List<MailLogInfos> getMailLog(Position position) {
		List<RejectionEmailLog> mailLogs = rejectionDao.getLog(position);
		List<MailLogInfos> infos = new ArrayList<>();
		
		Map<Long,List<MailLogInfos>> applicationKeyMap = new HashMap<>();
		for(RejectionEmailLog mailLog:mailLogs) {
			MailLogInfos mailLogInfos = new MailLogInfos(mailLog);
			infos.add(mailLogInfos);
			applicationKeyMap
				.computeIfAbsent(mailLog.getApplication().getKey(), key -> new ArrayList<>())
				.add(mailLogInfos);
		}

		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();

		Long currentApplicationKey = null;
		List<ApplicationAttributeLight> applicationAttributes = new ArrayList<>();
		List<ApplicationAttributeLight> attributes = applicationDao.loadApplicationAttributes(position, true);
		for(ApplicationAttributeLight attribute:attributes) {
			Long attrAppKey = attribute.getApplicationKey();
			if(currentApplicationKey != null && !currentApplicationKey.equals(attrAppKey)) {
				List<MailLogInfos> apps = applicationKeyMap.get(currentApplicationKey);
				if(apps != null) {
					String[] array = attributesToArray(definitions, applicationAttributes);
					for(MailLogInfos app:apps) {
						((ApplicationLightImpl)app.getApplication()).setAdditionalValues(array);
					}
				}
				applicationAttributes.clear();
			}
			applicationAttributes.add(attribute);
			currentApplicationKey = attrAppKey;
		}
		
		if(currentApplicationKey != null) {
			List<MailLogInfos> apps = applicationKeyMap.get(currentApplicationKey);
			if(apps != null) {
				String[] array = attributesToArray(definitions, applicationAttributes);
				for(MailLogInfos app:apps) {
					((ApplicationLightImpl)app.getApplication()).setAdditionalValues(array);
				}
			}
		}

		return infos;
	}

	@Override
	public Set<Long> getApplicationKeySentEmails(PositionRef position) {
		List<Long> keys = rejectionDao.getRejectedApplicationKeys(position);
		return new HashSet<>(keys);
	}

	@Override
	public List<SentEmailTemplates> getApplicationSentEmails(PositionRef position) {
		return rejectionDao.getApplicationSentEmails(position);
	}

	@Override
	public void sendRejectionMail(Position position, ApplicationLight application, ApplicationMailTemplate template, MailerResult result) {
		OrganisationUnit organisationSettings = organisationUnitDao.loadOrganisationUnitByPosition(position);
		//send mail
		String to = application.getPerson().getMail();
		String bcc = recruitingModule.getBccStaffMail(position, organisationSettings);
		MailerSender mailSender = new MailerSender(bcProvider, keyStore);
		SubjectAndBody content = mailSender.send(to, bcc, application, null, position, null, null, null, null, template, result);
		
		boolean markAsRejected = true;
		if(template.getName() != null && recruitingModule.getMailTemplateRejectionTitle() != null) {
			markAsRejected = template.getName().equalsIgnoreCase(recruitingModule.getMailTemplateRejectionTitle());
		}
		
		String templateName = template.getName();
		if(!recruitingModule.isMailTemplateTitle(templateName)) {
			if(template.getKey() != null) {
				templateName = template.getKey().toString();
			} else {
				templateName = template.getName();
			}
		}
		rejectionDao.addLog(templateName, content.getSubject(), content.getBody(), content.getLetter(), markAsRejected, application, result);
		log.info(Tracing.M_AUDIT, "Mail send to application(template={}, rejected={}): {}", template.getName(), markAsRejected, application);
	}
	
	@Override
	public Reference sendRefereeMail(Reference reference, ApplicationShort application, List<? extends ApplicationShort> applicationsList,
			Position position, ApplicationMailTemplate template, ReferenceStatus nextStatus, boolean reminderByApplicant, MailerResult result) {
		OrganisationUnit organisationSettings = organisationUnitDao.loadOrganisationUnitByPosition(position);
		//send mail
		String to = reference.getEmail();
		String bcc = recruitingModule.getBccStaffMail(position, organisationSettings);
		new MailerSender(bcProvider, keyStore).send(to, bcc, application, applicationsList, position, reference, null, null, null, template, result);
		if(nextStatus != null) {
			if(nextStatus == ReferenceStatus.sentAwaiting) {
				if(result.getReturnCode() != MailerResult.OK) {
					nextStatus = ReferenceStatus.notSent;
					reference.setDateInvitation(null);
				} else if(reference.getReferenceStatus() == ReferenceStatus.notSent && reference.getDateInvitation() == null) {
					reference.setDateInvitation(new Date());
				} else {
					reference.setDateLastReminder(new Date());
				}
			}
			if(reminderByApplicant) {
				reference.setRemindersByApplicant(reference.getRemindersByApplicant() + 1);
			}
			
			reference.setReferenceStatus(nextStatus);
			reference = referenceDao.updateReference(reference);
		}
		log.info(Tracing.M_AUDIT,"Invitation send to {} for application {}", reference, application);
		return reference;
	}
	
	@Override
	public void sendToReference(Reference reference, ApplicationShort application, List<? extends ApplicationShort> applicationList,
			Position position, ApplicationMailTemplate template) {
		OrganisationUnit organisationSettings = organisationUnitDao.loadOrganisationUnitByPosition(position);
		String to = reference.getEmail();
		String bcc = recruitingModule.getBccStaffMail(position, organisationSettings);
		MailerResult result = new MailerResult();
		new MailerSender(bcProvider, keyStore).send(to, bcc, application, applicationList, position, reference, null, null, null, template, result);
		if(result.isSuccessful()) {
			log.info(Tracing.M_AUDIT,"Confirmation reference send to {} for application {}", reference, application);
		} else {
			log.warn(Tracing.M_AUDIT,"Cannot send confirmation reference to {} for application {}", reference, application);
		}
	}

	@Override
	public List<ApplicationFeedback> sendFeedbackContactMail(Identity member, List<ApplicationFeedback> feedbacks, Date deadline,
			ApplicationsFeedbackConfiguration feedbackConfig, List<? extends ApplicationShort> applications, Position position,
			ApplicationMailTemplate template, MailerResult result) {

		//send mail
		String to = member.getUser().getProperty(UserConstants.EMAIL, template.getLocale());
		if(!StringHelper.containsNonWhitespace(to)) {
			to = member.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, template.getLocale());
		}

		OrganisationUnit organisationSettings = organisationUnitDao.loadOrganisationUnitByPosition(position);
		String bcc = recruitingModule.getBccStaffMail(position, organisationSettings);

		new MailerSender(bcProvider, keyStore).send(to, bcc, null, applications, position, null, member, feedbacks, feedbackConfig, template, result);
		log.info(Tracing.M_AUDIT, "Contact send to faculty member {} for position {}", member, position);
		
		List<ApplicationFeedback> updatedFeedbacks = new ArrayList<>();
		if(result.getReturnCode() == MailerResult.OK) {
			for(ApplicationFeedback feedback:feedbacks) {
				if(feedback.getReferenceStatus() == ReferenceStatus.notSent && feedback.getRequest() == null) {
					feedback.setRequest(new Date());
				} else {
					feedback.setLastReminder(new Date());
				}
				if(deadline != null) {
					feedback.setDeadline(deadline);
				}
				
				feedback.setReferenceStatus(ReferenceStatus.sentAwaiting);
				feedback = applicationFeedbackDao.updateFeedback(feedback);
				log.info(Tracing.M_AUDIT, "Invitation send to " + feedback.getIdentity() +  " for application " + feedback.getApplication());
				updatedFeedbacks.add(feedback);
			}
		} else if(deadline != null) {
			for(ApplicationFeedback feedback:feedbacks) {
				feedback.setDeadline(deadline);
				feedback = applicationFeedbackDao.updateFeedback(feedback);
			}
		}
		return updatedFeedbacks;
	}

	@Override
	public void sendAssignmentNotificationMail(Position position, List<Identity> assignees, ApplicationMailTemplate template, MailerResult result) {
		OrganisationUnit organisationSettings = organisationUnitDao.loadOrganisationUnitByPosition(position);
		//send mail
		String bcc = recruitingModule.getBccStaffMail(position, organisationSettings);
		for(Identity assignee:assignees) {
			String to = assignee.getUser().getProperty(UserConstants.EMAIL, Locale.ENGLISH);
			if(StringHelper.containsNonWhitespace(to)) {
				new MailerSender(bcProvider, keyStore).send(to, bcc, null, null, position, null, null, null, null, template, result);
				log.info(Tracing.M_AUDIT, "Notification send for assignment to : {}", assignee);
			}
		}
	}

	@Override
	public DecisionRubricDefinition createDecisionRubricDefinition() {
		return decisionRubricDao.createDecisionRubricDefinition();
	}

	@Override
	public List<DecisionRubricDefinition> getDecisionRubricDefinition(Position position) {
		return decisionRubricDao.getDecisionRubricDefinition(position);
	}

	@Override
	public DecisionRubricDefinition saveDecisionRubricDefinition(DecisionRubricDefinition definition, Position position) {
		return decisionRubricDao.saveDefinition(definition, position);
	}

	@Override
	public void deleteDecisionRubricDefinition(DecisionRubricDefinition definition) {
		decisionRubricDao.deleteDefinition(definition);
	}

	@Override
	public DecisionRubric createDecisionRubric(DecisionRubricDefinition definition, ApplicationLight app) {
		return decisionRubricDao.createDecisionRubric(definition, app);
	}
	
	@Override
	public DecisionRubric saveDecisionRubric(DecisionRubric decision) {
		if(decision == null) return null;
		DecisionRubric savedDecision = decisionRubricDao.saveDecisionRubric(decision);
		dbInstance.commit();
		return decisionRubricDao.loadDecisionRubric(savedDecision.getKey());
	}

	@Override
	public List<DecisionRubric> getDecisionRubric(Position position) {
		return decisionRubricDao.getDecisionRubric(position);
	}

	@Override
	public OrganisationUnit createOrganisationUnit(Organisation organisation) {
		OrganisationUnit unit = organisationUnitDao.loadOrganisationUnitByOrganisation(organisation);
		if(unit == null) {
			unit = organisationUnitDao.createOrganisationUnit(organisation);
			dbInstance.commit();
		}
		return unit;
	}

	@Override
	public OrganisationUnit getOrganisationUnit(PositionRef position) {
		if(position == null || position.getKey() == null) return null;
		return organisationUnitDao.loadOrganisationUnitByPosition(position);
	}

	@Override
	public OrganisationUnit getOrganisationUnit(Organisation organisation) {
		return organisationUnitDao.loadOrganisationUnitByOrganisation(organisation);
	}

	@Override
	public OrganisationUnit updateOrganisationUnit(OrganisationUnit settings) {
		return organisationUnitDao.save(settings);
	}
	
	@Override
	public List<OrganisationUnit> getOrganisationUnits() {
		return organisationUnitDao.findAllOrganisationUnits();
	}

	@Override
	public String getPrivacyDisclaimerEmail(Identity identity) {
		String email = null;
		
		Roles roles = baseSecurity.getRoles(identity);
		if(roles.isAdministrator() || roles.isSelectusManager()) {//TODO selectus
			email = recruitingModule.getStaffMail();
		} else if(recruitingModule.isMailProPositionEnabled()) {
			List<PositionAttributeDefinition> globalAttributes = positionDao.getGlobalAttributeDefinition();
			List<PositionLightWithStatistics> positions = getPositionsLightWithStatistics(identity, roles, globalAttributes, Locale.ENGLISH, PositionStatus.values());
			if(positions.size() == 1) {
				PositionLightWithStatistics positionStats = positions.get(0);
				Position position = positionDao.loadPositionByKey(positionStats.getKey());
				OrganisationUnit organisationSettings = getOrganisationUnit(position);
				email = recruitingModule.getStaffMail(position, organisationSettings);
			}
		}
		
		if(!StringHelper.containsNonWhitespace(email)) {
			email = recruitingModule.getStaffMail();
		}
		return email;
	}
}
