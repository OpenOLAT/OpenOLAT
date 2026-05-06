/**


 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.manager.RatingClosedException;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.ExternalUserResults;
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
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.model.mail.SentEmailTemplates;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;
import org.olat.modules.selectus.pdf.PDFDataProvider;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface RecruitingService {
	
	public static final OLATResourceable ORESOURCE_COMMITTEE = OresHelper.lookupType(BaseSecurityModule.class, "RCommittee");
	public static final OLATResourceable ORESOURCE_COMMITTEE_HEAD = OresHelper.lookupType(BaseSecurityModule.class, "RCommitteeHead");
	public static final OLATResourceable ORESOURCE_SECRETARY = OresHelper.lookupType(BaseSecurityModule.class, "RSecretary");

	public static final int ABSTENTION = -32;
	
	public Position createPosition(Organisation organisation);
	
	public Position savePosition(Position position);
	
	public void deletePosition(Position position);
	
	/**
	 * Anonymise and transform the position for reporting only purposer
	 * @param position
	 */
	public void toReportPositionOnly(Position position);

	public Position getPosition(Long positionKey);
	
	/**
	 * Return the last change in these position's applications
	 * @param position
	 * @return The date of the last modified application
	 */
	public Date getLastApplicationModification(Position position);
	
	/**
	 * Return the size of all attachments in the position.
	 * @param position
	 * @return
	 */
	public Long getEstimatedSizeOfAttachment(Position position);
	
	/**
	 * Accept the position
	 * @param position
	 * @param member
	 * @return True if don't want to show the policy
	 */
	public boolean acceptPositionPolicy(Position position, Identity member, AcceptPolicyEnum policy, Boolean dontShow);
	
	/**
	 * Return the list of policies to accept.
	 * 
	 * @param position
	 * @param member
	 * @return
	 */
	public List<AcceptPolicyEnum> needToAcceptPositionPolicies(Position position, Identity member);
	
	/**
	 * Count the memberships with the administrative roles.
	 * 
	 * @param identity
	 * @param roles
	 * @param status
	 * @return
	 */
	public CommitteeMembershipsStats getCommitteeMembershipsStats(Identity identity);
	
	/**
	 * Retrieve the positions by status, the method only fetch the security groups
	 * for committee, head... but none of the other objects.
	 * 
	 * @param status The status
	 * @return A list of positions
	 */
	public List<Position> getPositions(PositionStatus... status);
	
	public List<Position> getPositionsToRemind();
	
	/**
	 * Return the references saved for a specific position. The application
	 * of every reference is fetched. Return only the references of valid
	 * application.
	 * 
	 * @param position
	 * @param type The type of reference (optional, it can be null)
	 * @return A list of references
	 */
	public List<Reference> getPositionReferences(Position position, ReferenceType type, boolean onlyWithLetter);
	
	/**
	 * The number of positions in the specified states.
	 * @param status
	 * @return The number of positions
	 */
	public long countPositions(PositionStatus... status);
	
	/**
	 * Retrieve the published and advertised positions and check that the identity can see them.
	 * 
	 * @return A list of published and advertised positions
	 */
	public List<Position> getPublishedPositions();
	
	public boolean hasPositions(Identity identity, Roles roles, PositionStatus... status);

	/**
	 * Retrieve the positions and check that the identity can see them
	 * 
	 * @param identity
	 * @param roles
	 * @param locale
	 * @param status
	 * @return
	 */
	public List<PositionLightWithStatistics> getPositionsLightWithStatistics(Identity identity, Roles roles,
			List<PositionAttributeDefinition> globalAttributes, Locale locale, PositionStatus... status);

	public PositionAttributeDefinition createAttributeDefinition(Position position, PositionApplicationAttributeTabEnum tab,
			PositionAttributeDefinitionTypeEnum attributeType, String label, String labelDe, boolean mandatory,
			String placeholder, String placeholderDe);
	
	public PositionAndAttributeDefinition createAttributeDefinitionAndPersist(Position position, PositionApplicationAttributeTabEnum tab,
			PositionAttributeDefinitionTypeEnum attributeType, String label, String labelDe, boolean mandatory,
			String placeholder, String placeholderDe);
	
	public PositionAttributeDefinition updateAttributeDefinition(PositionAttributeDefinition attributeDefinition);
	
	public void persistAttributeDefinition(PositionAttributeDefinition attributeDefinition);
	
	public List<PositionAttributeDefinition> getGlobalAttributeDefinition();
	
	public Position deleteAttributeDefinition(Position position, PositionAttributeDefinition attributeDefinition);
	
	/**
	 * Create a new attribute with a position or an application (not both!)
	 * 
	 * @param position The attribute is hold by a position 
	 * @param application The attribute is hold by a n application 
	 * @param attributeDefinition
	 * @param value
	 * @return
	 */
	public ApplicationAttribute createAttribute(Position position, Application application, PositionAttributeDefinition attributeDefinition, String value);
	
	public long getAttributeUsage(Position position, PositionAttributeDefinition attributeDefinition);

	public Application createTempApplication(Position position, boolean submittedByStaff);
	
	public Application saveApplication(Application app);
	
	public void setDecision(Application app, int decision);
	
	public Application saveTempApplication(Application app, boolean removeTempFlag);
	
	public boolean checkUniqueApplication(Application app);
	
	/**
	 * Send the confirmation mail to the applicant. If configured will a
	 * BCC sended too.
	 * 
	 * @param app The application
	 * @param position The position
	 * @param mailToApplicant The mail's variables
	 */
	public void sendToApplicant(Application app, Position position, ApplicationMailTemplate template, boolean bcc);
	
	public void deleteApplication(Application app);
	

	public Application getApplication(ApplicationShort app);

	/**
	 * Load the application by its primary key.
	 * @param appKey
	 * @return The application or null
	 */
	public Application getApplicationByKey(Long appKey);
	
	public Application getApplicationByPublicFeedbackKey(String key);
	
	public Application getApplicationByApplicantKey(String key);
	
	public boolean hasApplicationByIdentity(IdentityRef identitys);
	
	/**
	 * @param identity The applicant
	 * @return A list of applications that the applicant is allowed to see now.
	 */
	public List<Application> getCurrentApplicationsByApplicant(IdentityRef identity);
	
	public Application getApplicationToEdit(ApplicationRef app);
	
	public Application getApplicationWithAttributes(ApplicationRef app);
	
	public ApplicationLight getApplicationLight(Position position, ApplicationRef app);
	
	
	public List<Application> getApplicationsWithAttachment(Position position);
	
	public List<ApplicationLight> getApplications(Position position);

	public List<ApplicationLight> getApplications(Position position, List<Integer> limitDecisions);
	
	public List<ApplicationRefereeStats> getApplicationReviewerStats(Position position);
	
	public List<Application> searchApplications(String searchText, Identity identity, Roles roles, List<PositionStatus> status);
	
	
	
	/**
	 * Return applications without decision and not withdrawn
	 * @param position
	 * @return
	 */
	public List<ApplicationLight> getApplicationsWithoutDecision(Position position);
	
	/**
	 * Return C-decisions applications but not withdrawn
	 * 
	 * @param position
	 * @return
	 */
	public List<ApplicationLight> getApplicationsWithCDecision(Position position);

	/**
	 * Return the applications with the specified decisions
	 * 
	 * @param position The position
	 * @param decisions The list of decisions to search for
	 * @param status The list of application status to search for
	 * @param noDecision Application without a decision set
	 * @param excludeTemplateNames Exclude applications which receive an email with the specified template
	 * @param excludeSendEmails Exclude the applicants which already received an e-mail
	 * @return
	 */
	public List<ApplicationLight> getApplicationsWithDecisions(Position position, List<Integer> decisions, List<ApplicationStatus> status,
			boolean noDecision, List<String> excludeTemplateNames, boolean excludeSendEmails);
	
	/**
	 * Return other parallel applications from the same applicant
	 * @param application
	 * @return
	 */
	public List<PositionLight> getParallelApplications(Application application, Position position, ParallelApplicationScope scope);
	
	public List<ParallelApplication> getParallelApplications(Position position, ParallelApplicationScope scope);
	
	public Position deleteAttachment(Position position, Attachment attachment);
	
	public Attachment setAttachmentDatas(Attachment attachment, String filename, DocumentType fileType, byte[] bytes);
	
	/**
	 * Add the file to the specified reference.
	 * 
	 * @param position The position of the application's reference
	 * @param reference The reference itself
	 * @param attachment The attachment
	 * @param filename The name of the file
	 * @param fileType The type of the file
	 * @param bytes The content of the file
	 * @return The merged / persisted attachment
	 */
	public Attachment setAttachmentDatas(Position position, Reference reference, Attachment attachment, String filename, DocumentType fileType, byte[] bytes);
	
	/**
	 * 
	 * 
	 * 
	 * @param attachment The first time is the attachment null
	 * @param bytes
	 */
	public Attachment setAttachmentDatas(Application application, Attachment attachment, byte[] bytes, String filename, DocumentType fileType);
	
	/**
	 * The method is used to remove file from the file system
	 * in first place. It won't change the attachment itself because
	 * it holds to the application and the application need to discard it.
	 * 
	 * @param application
	 * @param attachment
	 */
	public void removeAttachmentDatas(Application application, Attachment attachment);
	
	public byte[] getAttachmentDatas(Attachment attachment);
	
	public byte[] getAttachmentDatas(Long attachmentKey);
	
	/**
	 * Return the size of the cached stream. It can be null if the stream is not cached
	 * or not streaming one time.
	 * @param position
	 * @return
	 */
	public Long streamSize(Position position);
	
	/**
	 * Return the size of the cached stream. It can be null if the stream is not cached
	 * or not streaming one time.
	 * @param position
	 * @return
	 */
	public Long streamExpertOpinionsSize(Position position);
	
	/**
	 * Force delete the cached stream.
	 * @param position
	 */
	public void deleteCachedStream(Position position);
	
	/**
	 * Stream and cached the stream on the disk using a cache.
	 * 
	 * @param position The position
	 * @param provider The provider
	 * @param out The output stream
	 * @return The size of the archive
	 */
	public Long stream(Position position, PDFDataProvider provider, OutputStream out);
	
	/**
	 * Stream and cached the stream on the disk using a cache.
	 * 
	 * @param position The position
	 * @param provider The data provider
	 * @param out The output stream
	 * @return The size of the archive
	 */
	public Long streamExpertOpinions(Position position, PDFDataProvider provider, OutputStream out);
	

	/**
	 * Find an identity in selectus database or search on LDAP id enabled and import
	 * it if only one user is found in LDAP.
	 * 
	 * @param email
	 * @return
	 */
	public Identity getCommitteeMember(String email);
	
	/**
	 * 
	 * @param searchString
	 * @return
	 */
	public ExternalUserResults searchUsers(String searchString, OAuth2Tokens oauth2Tokens, Locale locale);

	/**
	 * Find identity in selectus database or on LDAP (ldap user are not automatically imported)
	 * 
	 * @param email
	 * @return
	 */
	public Identity findCommitteeMember(String email, OAuth2Tokens oauth2Tokens);
	
	public List<Identity> getCommitteeMembers(PositionRef position);
	

	public int countCommittee(Position position, PositionRole... roles);
	
	public List<Identity> getCommittee(Position position, PositionRole... roles);
	
	public List<IdentityRef> getCommitteeRefs(Position position, PositionRole... roles);
	
	/**
	 * Create and persist a new identity with the default properties.
	 * And send the generated password to the staff.
	 * @param username
	 * @param newUser
	 * @return
	 */
	public Identity createCommitteeIdentity(String username, User newUser, boolean ldap, boolean azure,
			Position position, Organisation organisation, Identity doer);
	
	/**
	 * Check if the OLAT Resource exists to prevent a nested doInSync
	 * @param position
	 * @return
	 */
	public boolean createOLATResource(Position position);
	
	/**
	 * Add member to the committee
	 * @param position
	 * @param member
	 */
	public void addToCommittee(Position position, Identity member);
	
	public void removeFromCommitte(Position position, Identity member);
	
	
	public List<Identity> getHeads(Position position);
	
	public Identity getHeadOfCommittee(Position position);
	
	public void addToCommitteeAsHead(Position position, Identity head);

	/**
	 * Return the first found for this position
	 * @param position
	 * @return The first secretary or null
	 */
	public Identity getSecretary(Position position);
	
	public List<Identity> getSecretaries(Position position);
	
	public PositionRole getRole(Position position, IdentityRef identity);
	
	public void addToCommitteeAsSecretary(Position position, Identity secretary);
	

	public List<Identity> getExOfficios(Position position);
	
	public void addToCommitteeAsExOfficio(Position position, Identity exOfficio);

	
	public String generatePassword();
	
	public Reference addReference(String title, String firstName, String lastName, String institution, String email, Date submissionDeadline,
			ReferenceType type, ReferenceRequestStatus requestStatus, String adminNote,
			Application application, List<Application> applicationsToCompare);
	
	public List<ReferenceToApplication> getReferenceToApplications(Reference reference);
	
	public List<Application> getReferenceToApplicationsList(Reference reference);
	
	public List<ReferenceToApplication> getReferenceToApplications(List<Reference> references);
	
	public List<ReferenceToApplication> getReferenceToApplications(PositionRef position);
	
	public void deleteReferenceToApplications(Reference reference, ApplicationShort application);
	
	public void addReferenceToApplication(Reference reference, Application application);

	public Reference updateReference(Reference reference);
	
	public Reference deleteAttachment(Reference reference, Attachment attachment);
	
	/**
	 * The references (experts and recommendations) of the specified application.
	 * The method doesn't fetch the application and position.
	 * 
	 * @param application
	 * @param type To filter one type of references (optional, can be null)
	 * @return
	 */
	public List<Reference> getApplicationReferences(Application application, ReferenceType type);
	
	
	public boolean hasApplicationReferencePDFs(Position position, ReferenceType type);
	

	public Reference getReferenceById(Long key);
	
	public Reference sendRefereeMail(Reference reference, ApplicationShort application, List<? extends ApplicationShort> applicationsList,
			Position position, ApplicationMailTemplate template, ReferenceStatus nextStatus, boolean reminderByApplicant, MailerResult result);
	
	public void sendToReference(Reference reference, ApplicationShort application, List<? extends ApplicationShort> applicationsList,
			Position position, ApplicationMailTemplate template);
	
	public List<ApplicationFeedback> sendFeedbackContactMail(Identity member, List<ApplicationFeedback> feedbacks, Date deadline,
			ApplicationsFeedbackConfiguration feedbackConfig, List<? extends ApplicationShort> applications, Position position,
			ApplicationMailTemplate template, MailerResult result);
	
	public void sendAssignmentNotificationMail(Position position, List<Identity> assignees, ApplicationMailTemplate template, MailerResult result);
	
	/**
	 * Retrieve the reference with the unique identifier
	 * 
	 * @param url
	 * @return
	 */
	public Reference getReferenceBySubmissionUrl(String url);
	
	public List<Reference> getReferences(ReferenceSearchParameters params);
	
	/**
	 * 
	 * @param app The application
	 * @param reference A reference to exclude from the search
	 * @param email The E-mail to lookup
	 * @return true if an other reference use already the email adress
	 */
	public boolean hasReferenceWithEmail(Application app, Reference reference, String email);
	
	public void deleteReference(Reference reference);
	
	public ReferenceComment addReferenceComment(Reference reference, String comment);
	
	public List<ReferenceComment> getComments(Reference reference);
	
	public List<Long> getReferencesWithComments(Position position);
	

	public Notes createNotes(Long application, Identity identity, String content);
	
	public Notes updateNotes(Long application, Identity identity, String content);
	
	public Notes getNotes(Long application, Identity identity);
	
	public List<Notes> getNotes(Position position, Identity identity);
	
	
	public List<UserRating> getRatings(Position position, List<? extends IdentityRef> committee);
	
	public List<UserRating> getRatings(Application application, List<? extends IdentityRef> committee);
	
	public List<UserRating> getRatings(Position position, ApplicationRef application, List<? extends IdentityRef> committee);
	
	public UserRating getRating(Application application, Identity committeeMember);
	
	public UserRating getRating(Position position, ApplicationLight application, Identity committeeMember);

	public UserRating setRating(Application application, Identity committeeMember, int rating) throws RatingClosedException;
	
	public void removeRating(Application application, Identity committeeMember) throws RatingClosedException;
	
	public UserRating setRating(Position position, ApplicationLight application, Identity committeeMember, int rating) throws RatingClosedException;
	
	public void removeRating(Position position, ApplicationLight application, Identity committeeMember) throws RatingClosedException;
	
	public void sendReminder(Position position, Identity member, ApplicationMailTemplate template);
	
	
	/**
	 * @return The mail sender with its encryption provider loaded
	 */
	public MailerSender createMailSender();
	
	public RejectionEmailLogFull getFullLog(RejectionEmailLog log);

	public List<MailLogInfos> getMailLog(Position position);
	

	public Set<Long> getApplicationKeySentEmails(PositionRef position);
	
	public List<SentEmailTemplates> getApplicationSentEmails(PositionRef position);
	
	public void sendRejectionMail(Position position, ApplicationLight application, ApplicationMailTemplate template, MailerResult result);
	
	/**
	 * Create a transient rubric definition. The definition is not persisted on
	 * the database.
	 * 
	 * @return
	 */
	public DecisionRubricDefinition createDecisionRubricDefinition();

	public List<DecisionRubricDefinition> getDecisionRubricDefinition(Position position);
	
	public DecisionRubricDefinition saveDecisionRubricDefinition(DecisionRubricDefinition definition, Position position);
	
	public void deleteDecisionRubricDefinition(DecisionRubricDefinition definition);
	
	
	public DecisionRubric createDecisionRubric(DecisionRubricDefinition definition, ApplicationLight app);

	public DecisionRubric saveDecisionRubric(DecisionRubric decision);

	public List<DecisionRubric> getDecisionRubric(Position position);

	
	public void sendMail(String to, String subject, String body);
	
	public OrganisationUnit createOrganisationUnit(Organisation organisation);
	
	public OrganisationUnit getOrganisationUnit(PositionRef position);
	
	public OrganisationUnit getOrganisationUnit(Organisation organisation);
	
	public OrganisationUnit updateOrganisationUnit(OrganisationUnit settings);
	
	public List<OrganisationUnit> getOrganisationUnits();

	
	public String getPrivacyDisclaimerEmail(Identity identity);

}
