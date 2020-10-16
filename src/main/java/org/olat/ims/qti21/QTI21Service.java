/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.DigitalSignatureValidation;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObject;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentObject;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetManager;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QTI21Service {
	
	public static final String PACKAGE_CONFIG_FILE_NAME = "QTI21PackageConfig.xml";
	
	/**
	 * New QTI serializer
	 * @return
	 */
	public QtiSerializer qtiSerializer();
	
	public QtiXmlReader qtiXmlReader();
	
	/**
	 * The manager for custom extensions to QTI (MathExtensio )
	 * @return
	 */
	public JqtiExtensionManager jqtiExtensionManager();
	
	/**
	 * @return The cache for stylesheets used by MathML transformation
	 */
	public XsltStylesheetCache getXsltStylesheetCache();
	
	/**
	 * @return The stylesheets manager used by MathML transformation
	 */
	public XsltStylesheetManager getXsltStylesheetManager();
	
	
	public URI createAssessmentTestUri(File resourceDirectory);
	
	/**
	 * Ensure the assessment test is cached and not expired by
	 * the max. idle configuration. The goal is to maintain the
	 * object in cache despite it to be strong reference by the
	 * run controller.
	 * 
	 * @param resourceDirectory The directory where is the package
	 */
	public void touchCachedResolveAssessmentTest(File resourceDirectory);
	
	
	/**
	 * Load the assessmentTest based on the imsmanifest.xml found in the resource
	 * directory. Return null if the imsmanifest.xml is not found. The assessmentTest
	 * is cached.
	 * 
	 * @param resourceDirectory The directory where is the package
	 * @param replace If true updates the cache
	 * @param debugInfo If true writes more infos 
	 * @return The resolved assessment test or null if the imsmanifest.xml was not found.
	 */
	public ResolvedAssessmentTest loadAndResolveAssessmentTest(File resourceDirectory, boolean replace, boolean debugInfo);
	
	/**
	 * The assessment item is load and cached.
	 * 
	 * @param assessmentObjectSystemId
	 * @param resourceDirectory
	 * @return
	 */
	public ResolvedAssessmentItem loadAndResolveAssessmentItem(URI assessmentObjectSystemId, File resourceDirectory);
	
	/**
	 * This method load a fresh instance from the disk and don't cache it. The instance can be changed and saved
	 * safely.
	 * 
	 * @param assessmentObjectSystemId
	 * @param resourceDirectory
	 * @return
	 */
	public ResolvedAssessmentItem loadAndResolveAssessmentItemForCopy(URI assessmentObjectSystemId, File resourceDirectory);
	
	public boolean updateAssesmentObject(File resourceFile, ResolvedAssessmentObject<?> resolvedAssessmentObject);
	
	public boolean persistAssessmentObject(File resourceFile, AssessmentObject assessmentObject);
	
	/**
	 * 
	 * @param The test resource
	 * @return
	 */
	public boolean needManualCorrection(RepositoryEntry testEntry);
	
	/**
	 * 
	 * @param identities
	 * @param testEntry
	 * @param entry
	 * @param subIdent
	 * @return
	 */
	public boolean deleteAssessmentTestSession(List<Identity> identities, RepositoryEntryRef testEntry, RepositoryEntryRef entry, String subIdent);
	
	/**
	 * Remove all test sessions in author mode, e.g. after an assessment test
	 * was changed.
	 * 
	 * @param testEntry The test repository entry
	 * @return
	 */
	public boolean deleteAuthorsAssessmentTestSession(RepositoryEntryRef testEntry);
	
	/**
	 * Remove a test sessions in author mode, e.g. after an assessment test
	 * was changed.
	 * 
	 * @param testEntry The test repository entry
	 * @param testSession The session of the author
	 * @return
	 */
	public boolean deleteAuthorAssessmentTestSession(RepositoryEntryRef testEntry, AssessmentTestSession testSession);
	
	/**
	 * Delete a specific preview test session.
	 * 
	 * @param testSession
	 * @return
	 */
	public boolean deleteAssessmentTestSession(AssessmentTestSession testSession);
	
	/**
	 * Set some extra options for the QTI package.
	 * 
	 * @param testEntry
	 * @return
	 */
	public QTI21DeliveryOptions getDeliveryOptions(RepositoryEntry testEntry);
	
	/**
	 * Set some extra options for the QTI 2.1 which are not part
	 * of the standard fomr IMS.
	 * 
	 * @param testEntry
	 * @param options
	 */
	public void setDeliveryOptions(RepositoryEntry testEntry, QTI21DeliveryOptions options);
	
	/**
	 * Check if some user made assessment with this test.
	 * 
	 * @param testEntry
	 * @return
	 */
	public boolean isAssessmentTestActivelyUsed(RepositoryEntry testEntry);
	
	
	
	public AssessmentTestSession createAssessmentTestSession(Identity identity, String anonymousIdentifier,
			AssessmentEntry assessmentEntry, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry,
			Integer compensationExtraTime, boolean authorMode);
	
	
	/**
	 * This create an transient session which are not saved on the database. But
	 * please, at the end of the session, delete the storage.
	 * 
	 * @param identity
	 * @param anonymousIdentifier
	 * @param assessmentEntry
	 * @param entry
	 * @param subIdent
	 * @param testEntry
	 * @param authorMode
	 * @return
	 */
	public AssessmentTestSession createInMemoryAssessmentTestSession(Identity identity);
	
	/**
	 * Return the implementation of the log audit.
	 * 
	 * @param session
	 * @return
	 */
	public AssessmentSessionAuditLogger getAssessmentSessionAuditLogger(AssessmentTestSession session, boolean authorMode);
	
	/**
	 * 
	 * @param session The test session
	 * @return The file or null if it doesn't exists
	 */
	public File getAssessmentSessionAuditLogFile(AssessmentTestSession session);
	
	/**
	 * This will return the last session if it's not finished, terminated or exploded.
	 * 
	 * @param identity The identity which play the session
	 * @param anonymousIdentifier The anonymous identifier which play the session
	 * @param entry The repository entry (course or test)
	 * @param subIdent The sub identifier (typically course element ident)
	 * @param testEntry The repository entry of the test
	 * @param authorMode If the sesssion is played as an author
	 * @return A test session
	 */
	public AssessmentTestSession getResumableAssessmentTestSession(Identity identity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, boolean authorMode);

	public AssessmentTestSession reloadAssessmentTestSession(AssessmentTestSession session);
	
	public AssessmentTestSession getResumableAssessmentItemsSession(Identity identity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, boolean authorMode);
	
	/**
	 * 
	 * @param session The assessment test session to update
	 * @return The merged assessment test session
	 */
	public AssessmentTestSession updateAssessmentTestSession(AssessmentTestSession session);
	
	/**
	 * Recalculate the score and manual score of an assessment test session. As a security
	 * will the method do a commit first to ensure that all item session are saved on the
	 * database.
	 * 
	 * @param session The assessment test session primary key
	 * @return The merged assessment test session
	 */
	public AssessmentTestSession recalculateAssessmentTestSessionScores(Long sessionKey);

	public boolean isRunningAssessmentTestSession(RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, List<? extends IdentityRef> identities);
	
	public boolean isRunningAssessmentTestSession(RepositoryEntry entry, List<String> subIdents, List<? extends IdentityRef> identities);
	
	/**
	 * Add some extra time to an assessment test session.
	 * 
	 * @param session The session to extend
	 * @param extraTime The extra time in seconds
	 * @param actor The user which do the change
	 */
	public void extraTimeAssessmentTestSession(AssessmentTestSession session, int extraTime, Identity actor);
	
	/**
	 * Add some extra time due to compensation for disadvantages to a test session.
	 * 
	 * @param session The session to extend
	 * @param extraTime The extra time in seconds
	 * @param actor The user which do the change
	 */
	public void compensationExtraTimeAssessmentTestSession(AssessmentTestSession session, int extraTime, Identity actor);
	
	/**
	 * Reopen a closed test. The method remove end and exit date, set a current
	 * question... to make the test playable again.
	 * 
	 * @param session The session to reopen
	 */
	public AssessmentTestSession reopenAssessmentTestSession(AssessmentTestSession session, Identity actor);
	
	public List<AssessmentTestSession> getRunningAssessmentTestSession(RepositoryEntry entry, String subIdent, RepositoryEntry testEntry);
	
	public TestSessionState loadTestSessionState(AssessmentTestSession session);
	
	public ItemSessionState loadItemSessionState(AssessmentTestSession session, AssessmentItemSession itemSession);

	public AssessmentTestMarks createMarks(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, String marks);
	
	public AssessmentTestMarks getMarks(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry);
	
	public AssessmentTestMarks updateMarks(AssessmentTestMarks marks);
	
	public File getAssessmentResultFile(final AssessmentTestSession candidateSession);
	
	/**
	 * Reload the test session by its key and fetch identity, user...
	 * 
	 * @param assessmentTestSessionKey
	 * @return The assessment test session or null if not found.
	 */
	public AssessmentTestSession getAssessmentTestSession(Long assessmentTestSessionKey);
	
	/**
	 * Retrieve the sessions of a user.
	 * 
	 * @param courseEntry
	 * @param subIdent
	 * @param identity
	 * @return
	 */
	public List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent,
			IdentityRef identity, boolean onlyValid);
	
	/**
	 * Retrieve the sessions of a user with the number of corrected assessment items (only the test and its resource are fetched).
	 * 
	 * @param courseEntry The course
	 * @param subIdent The course node identifier
	 * @param identity The user to assess
	 * @param onlyValid true to excluded exploded or cancelled sessions
	 * @return A list of assessment test sessions wrapped with number of corrected items
	 */
	public List<AssessmentTestSessionStatistics> getAssessmentTestSessionsStatistics(RepositoryEntryRef courseEntry, String subIdent,
			IdentityRef identity, boolean onlyValid);
	
	/**
	 * Retrieve the last finished test session.
	 * 
	 * @param courseEntry
	 * @param subIdent
	 * @param testEntry
	 * @param identity
	 * @return
	 */
	public AssessmentTestSession getLastAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntry testEntry, IdentityRef identity);
	
	/**
	 * Retrieve the sessions for a test. It returns only the sessions of authenticated users (fetched).
	 * The anonymous ones are not included as exploded and cancelled.
	 * 
	 * @param courseEntry The repository entry
	 * @param subIdent Typically the course element identifier (optional)
	 * @param testEntry The test entry
	 * @return A list of valid test sessions
	 */
	public List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntry testEntry);
	
	/**
	 * 
	 * @param candidateSession The candidate session
	 * @param parentParts The parent sections (optional)
	 * @param assessmentItemIdentifier The assessment item identifier 
	 * @return The item session (persistent if the candidate session is persitable, in memory if not)
	 */
	public AssessmentItemSession getOrCreateAssessmentItemSession(AssessmentTestSession candidateSession, ParentPartItemRefs parentParts, String assessmentItemIdentifier);

	public AssessmentItemSession getAssessmentItemSession(AssessmentItemSessionRef candidateSession);
	
	/**
	 * Update the review flag on a batch of assessment item sessions. The method will do a commit.
	 * 
	 * @param courseEntry The course
	 * @param subIdent Typically the course element identifier
	 * @param testEntry The test (mandatory)
	 * @param itemRef The item reference (mandatory)
	 * @param toReview The flag (true/false)
	 */
	public int setAssessmentItemSessionReviewFlag(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntry testEntry, String itemRef, boolean toReview);
	
	public List<AssessmentItemSession> getAssessmentItemSessions(AssessmentTestSession candidateSession);
	
	public List<AssessmentItemSession> getAssessmentItemSessions(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntry testEntry, String itemRef);
	
	public AssessmentItemSession updateAssessmentItemSession(AssessmentItemSession itemSession);
	
	public AssessmentResponse createAssessmentResponse(AssessmentTestSession candidateSession, AssessmentItemSession assessmentItemSession,
			String responseIdentifier, ResponseLegality legality, ResponseDataType type);
	
	public Map<Identifier, AssessmentResponse> getAssessmentResponses(AssessmentItemSession assessmentItemSession);
	
	public void recordTestAssessmentResponses(AssessmentItemSession assessmentItemSession, Collection<AssessmentResponse> responses);
	

	public AssessmentTestSession recordTestAssessmentResult(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResult,
			AssessmentSessionAuditLogger auditLogger);
	
	/**
	 * Finish the test session. The assessment result is for the last time and would not updated anymore.
	 * 
	 * @param candidateSession
	 * @param testSessionState
	 * @param assessmentResul
	 * @param timestamp
	 * @param digitalSignature
	 * @param bundle
	 * @return
	 */
	public AssessmentTestSession finishTestSession(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResul,
			Date timestamp, DigitalSignatureOptions signatureOptions, Identity assessedIdentity);
	
	/**
	 * The test session and all files will be deleted.
	 * 
	 * @param candidateSession The test session
	 * @param testSessionState The test session state
	 */
	public void deleteTestSession(AssessmentTestSession candidateSession, TestSessionState testSessionState);
	
	/**
	 * Pull a running test
	 * 
	 * @param candidateSession The test session to pull
	 * @param actor The user which pull the test session
	 * @return The updated test session
	 */
	public AssessmentTestSession pullSession(AssessmentTestSession candidateSession, DigitalSignatureOptions signatureOptions, Identity actor);
	
	/**
	 * Update the assessment entry if the test is done within a test repository entry.
	 * 
	 * @param candidateSession The assessment test tession.
	 * @return The updated assessment entry
	 */
	public AssessmentEntry updateAssessmentEntry(AssessmentTestSession candidateSession, boolean updateScoring);
	
	/**
	 * Sign the assessment result. Be careful, the file must not be changed
	 * after that!
	 * 
	 * @param candidateSession
	 * @param sendMail
	 * @param mail
	 */
	public void signAssessmentResult(AssessmentTestSession candidateSession, DigitalSignatureOptions signatureOptions, Identity assessedIdentity);
	
	public DigitalSignatureValidation validateAssessmentResult(File xmlSignature);
	
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, RepositoryEntryRef testEntry, RepositoryEntryRef entry,
			CandidateTestEventType textEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder);

	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, RepositoryEntryRef testEntry, RepositoryEntryRef entry,
			CandidateTestEventType textEventType, CandidateItemEventType itemEventType,
			TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder);
	
	/**
	 * Return the assessment result for the specified test session.
	 * 
	 * @param candidateSession
	 * @return The assessment result
	 */
	public AssessmentResult getAssessmentResult(AssessmentTestSession candidateSession);
	
	/**
	 * Return the file where the XML Digital Signature of the assessment result
	 * is saved or null if it not exists.
	 * 
	 * @return The file
	 */
	public File getAssessmentResultSignature(AssessmentTestSession candidateSession);
	
	/**
	 * Return the issue date saved in the XML Digital Signature
	 * 
	 * @param candidateSession
	 * @return
	 */
	public Date getAssessmentResultSignatureIssueDate(AssessmentTestSession candidateSession);
	

	public AssessmentTestSession finishItemSession(AssessmentTestSession candidateSession, AssessmentResult assessmentResul, Date timestamp);
	

	public void recordItemAssessmentResult(AssessmentTestSession candidateSession, AssessmentResult assessmentResult, AssessmentSessionAuditLogger candidateAuditLogger);
	
	public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, AssessmentItemSession itemSession,
			RepositoryEntryRef testEntry, RepositoryEntryRef entry, CandidateItemEventType itemEventType,
			ItemSessionState itemSessionState, NotificationRecorder notificationRecorder);
	
	public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, AssessmentItemSession itemSession,
			RepositoryEntryRef testEntry, RepositoryEntryRef entry, CandidateItemEventType itemEventType,
			ItemSessionState itemSessionState);
	
	/**
	 * 
	 */
	public File getSubmissionDirectory(AssessmentTestSession candidateSession);
	
	/**
	 * Import submitted file by an assessed identity in its session storage.
	 * 
	 * @param candidateSession
	 * @param multipartFile
	 * @return
	 */
	public File importFileSubmission(AssessmentTestSession candidateSession, MultipartFileInfos multipartFile);
	
	public File importFileSubmission(AssessmentTestSession candidateSession, String filename, byte[] data);
	
	
	public File getAssessmentDocumentsDirectory(AssessmentTestSession candidateSession);
	
	/**
	 * The method allow to prevent creation of a lot of empty directories.
	 * 
	 * @param candidateSession The assessment test session
	 * @param itemSession The assessment item session
	 * @param createDirectory Create a directory, if false and the directory doesn't exist, return null
	 * @return The directory or null if @createDirectory is false and the directory doesn't exist
	 */
	public File getAssessmentDocumentsDirectory(AssessmentTestSession candidateSession, AssessmentItemSession itemSession);
	
	/**
	 * Returns the sum of the correction time set in metadata of the test. Only
	 * the items proposed to the assessed user are counted (with or without response
	 * of him).
	 * 
	 * @param testEntry The reference / test entry
	 * @param candidateSession The test session
	 * @return A number of seconds, 0 if nothing found
	 */
	public Long getMetadataCorrectionTimeInSeconds(RepositoryEntry testEntry, AssessmentTestSession candidateSession);
	

	public void putCachedTestSessionController(AssessmentTestSession testSession, TestSessionController testSessionController);
	
	public TestSessionController getCachedTestSessionController(AssessmentTestSession testSession, TestSessionController testSessionController);

}
