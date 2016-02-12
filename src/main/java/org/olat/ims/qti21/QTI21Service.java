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
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.jpa.CandidateEvent;
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
	
	
	public URI createAssessmentObjectUri(File resourceDirectory);
	
	/**
	 * Load the assessmentTest based on the imsmanifest.xml found in the resource
	 * directory.
	 * 
	 * @param resourceDirectory
	 * @return
	 */
	public ResolvedAssessmentTest loadAndResolveAssessmentTest(File resourceDirectory);
	
	public ResolvedAssessmentItem loadAndResolveAssessmentItem(URI assessmentObjectSystemId, File resourceDirectory);
	
	public boolean updateAssesmentObject(File resourceFile, ResolvedAssessmentObject<?> resolvedAssessmentObject);
	
	public boolean persistAssessmentObject(File resourceFile, AssessmentObject assessmentObject);
	
	/**
	 * Set some extra options for the QTI package.
	 * 
	 * @param testEntry
	 * @return
	 */
	public QTI21DeliveryOptions getDeliveryOptions(RepositoryEntry testEntry);
	
	public void setDeliveryOptions(RepositoryEntry testEntry, QTI21DeliveryOptions options);
	
	
	public AssessmentTestSession createAssessmentTestSession(Identity identity, AssessmentEntry assessmentEntry,
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry,
			boolean authorMode);
	
	public AssessmentTestSession getResumableAssessmentTestSession(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry);
	
	public AssessmentTestSession updateAssessmentTestSession(AssessmentTestSession session);
	
	public TestSessionState loadTestSessionState(AssessmentTestSession session);
	
	/**
	 * Retrieve the sessions of a user.
	 * 
	 * @param courseEntry
	 * @param subIdent
	 * @param identity
	 * @return
	 */
	public List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef courseEntry, String subIdent, IdentityRef identity);
	
	public AssessmentItemSession getOrCreateAssessmentItemSession(AssessmentTestSession candidateSession, String assessmentItemIdentifier);
	
	public AssessmentResponse createAssessmentResponse(AssessmentTestSession candidateSession, AssessmentItemSession assessmentItemSession,
			String responseIdentifier, ResponseLegality legality, ResponseDataType type);
	
	public Map<Identifier, AssessmentResponse> getAssessmentResponses(AssessmentItemSession assessmentItemSession);
	
	public void recordTestAssessmentResponses(AssessmentItemSession assessmentItemSession, Collection<AssessmentResponse> responses);
	
	public AssessmentTestSession recordTestAssessmentResult(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResult);
	
	public AssessmentTestSession finishTestSession(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResul, Date timestamp);
	
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, CandidateTestEventType textEventType,
			TestSessionState testSessionState, NotificationRecorder notificationRecorder);

	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder);

	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder);
	
	
	

	public AssessmentTestSession finishItemSession(AssessmentTestSession candidateSession, AssessmentResult assessmentResul, Date timestamp);
	

	public void recordItemAssessmentResult(AssessmentTestSession candidateSession, AssessmentResult assessmentResult);
	
	public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, CandidateItemEventType itemEventType,
			ItemSessionState itemSessionState, NotificationRecorder notificationRecorder);
	
	public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession,
            CandidateItemEventType itemEventType, ItemSessionState itemSessionState);
	
	public String importFileSubmission(AssessmentTestSession candidateSession, MultipartFileInfos multipartFile);

}
