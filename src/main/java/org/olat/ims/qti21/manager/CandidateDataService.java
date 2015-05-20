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
package org.olat.ims.qti21.manager;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;

import javax.annotation.Resource;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.model.CandidateEvent;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

@Service
public class CandidateDataService {
	
	@Resource
	private QtiSerializer qtiSerializer;

	public AssessmentResult computeAndRecordTestAssessmentResult(UserTestSession candidateSession, TestSessionController testSessionController) {
		AssessmentResult assessmentResult = computeTestAssessmentResult(candidateSession, testSessionController);
		recordTestAssessmentResult(candidateSession, assessmentResult);
		return assessmentResult;
	}
	
    public AssessmentResult computeTestAssessmentResult(final UserTestSession candidateSession, final TestSessionController testSessionController) {
    	String baseUrl = "http://localhost:8080/olat";
        final URI sessionIdentifierSourceId = URI.create(baseUrl);
        final String sessionIdentifier = "testsession/" + candidateSession.getKey();
        
        Date timestamp = new Date();//requestTimestampContext.getCurrentRequestTimestamp();
        return testSessionController.computeAssessmentResult(timestamp, sessionIdentifier, sessionIdentifierSourceId);
    }
    
    public void recordTestAssessmentResult(final UserTestSession candidateSession, final AssessmentResult assessmentResult) {
        /* First record full result XML to filesystem */
        storeAssessmentResultFile(candidateSession, assessmentResult);

        /* Then record test outcome variables to DB */
        //recordOutcomeVariables(candidateSession, assessmentResult.getTestResult());
    }
    
    private void storeAssessmentResultFile(final UserTestSession candidateSession, final QtiNode resultNode) {
        final File resultFile = getAssessmentResultFile(candidateSession);
        try(OutputStream resultStream = FileUtils.getBos(resultFile);) {
            qtiSerializer.serializeJqtiObject(resultNode, resultStream);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Unexpected", e);
        }
    }
    
    private File getAssessmentResultFile(final UserTestSession candidateSession) {
        File sessionFolder = new File(getFullQtiPath(), candidateSession.getKey().toString());
        if(!sessionFolder.exists()) {
        	sessionFolder.mkdirs();
        }
        return new File(sessionFolder, "assessmentResult.xml");
    }
    
    private File getFullQtiPath() {
    	return new File(WebappHelper.getUserDataRoot(), "qti21");
	}
    
	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		return recordCandidateTestEvent(candidateSession, textEventType, null, null, testSessionState, notificationRecorder);
	}
	
	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		
		CandidateEvent event = new CandidateEvent();
		event.setTestEventType(textEventType);
		return recordCandidateTestEvent(candidateSession, textEventType, itemEventType, null, testSessionState, notificationRecorder);
	}

	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		
		CandidateEvent event = new CandidateEvent();
		event.setTestEventType(textEventType);
		event.setItemEventType(itemEventType);
		if (itemKey!=null) {
            event.setTestItemKey(itemKey.toString());
        }
		return event;
	}
	






}
