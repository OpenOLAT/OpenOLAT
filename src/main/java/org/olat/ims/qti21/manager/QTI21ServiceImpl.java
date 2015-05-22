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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21ContentPackage;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.model.CandidateEvent;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionPackage;
import uk.ac.ed.ph.jqtiplus.QtiConstants;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObjectType;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.result.AbstractResult;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentObject;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.value.RecordValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;
import uk.ac.ed.ph.qtiworks.mathassess.GlueValueBinder;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessConstants;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessExtensionPackage;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21ServiceImpl implements QTI21Service {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21ServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EventDAO eventDao;
	@Autowired
	private SessionDAO testSessionDao;
	@Autowired
	private QTI21Storage storage;
	@Autowired
	private QTI21Module qtiModule;
	
	private InfinispanXsltStylesheetCache xsltStylesheetCache;
	
	@Autowired
	public QTI21ServiceImpl(InfinispanXsltStylesheetCache xsltStylesheetCache) {
		this.xsltStylesheetCache = xsltStylesheetCache;
	}
	
    @Bean(initMethod="init", destroyMethod="destroy")
    public JqtiExtensionManager jqtiExtensionManager() {
        final List<JqtiExtensionPackage<?>> extensionPackages = new ArrayList<JqtiExtensionPackage<?>>();

        /* Enable MathAssess extensions if requested */
        if (qtiModule.isMathAssessExtensionEnabled()) {
            log.info("Enabling the MathAssess extensions");
            extensionPackages.add(new MathAssessExtensionPackage(xsltStylesheetCache));
        }

        return new JqtiExtensionManager(extensionPackages);
    }
    
    @Bean
    public QtiSerializer qtiSerializer() {
        return new QtiSerializer(jqtiExtensionManager());
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends ResolvedAssessmentObject<?>> E loadAndResolveAssessmentObject(File resourceDirectory) {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager());
        
		ResourceLocator fileResourceLocator = new PathResourceLocator(resourceDirectory.toPath());
		final ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
        final URI assessmentObjectSystemId = createAssessmentObjectUri(resourceDirectory);
        final AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, inputResourceLocator);
        final AssessmentObjectType assessmentObjectType = AssessmentObjectType.ASSESSMENT_TEST;
        E result;
        if (assessmentObjectType==AssessmentObjectType.ASSESSMENT_ITEM) {
            result = (E) assessmentObjectXmlLoader.loadAndResolveAssessmentItem(assessmentObjectSystemId);
        }
        else if (assessmentObjectType==AssessmentObjectType.ASSESSMENT_TEST) {
            result = (E) assessmentObjectXmlLoader.loadAndResolveAssessmentTest(assessmentObjectSystemId);
        }
        else {
            throw new OLATRuntimeException("Unexpected branch " + assessmentObjectType, null);
        }
        return result;
	}
	
	public URI createAssessmentObjectUri(File resourceDirectory) {
		File manifestPath = new File(resourceDirectory, "imsmanifest.xml");
		QTI21ContentPackage	cp = new QTI21ContentPackage(manifestPath.toPath());
		try {
			Path testPath = cp.getTest();
			return testPath.toUri();
		} catch (IOException e) {
			log.error("", e);
		}
		return null;
	}

	@Override
	public UserTestSession createTestSession(RepositoryEntry testEntry, RepositoryEntry courseEntry, String courseSubIdent, Identity identity) {
		return testSessionDao.createTestSession(testEntry, courseEntry, courseSubIdent, identity);
	}

	@Override
	public UserTestSession updateTestSession(UserTestSession session) {
		return testSessionDao.update(session);
	}

	@Override
	public List<UserTestSession> getUserTestSessions(RepositoryEntryRef courseEntry, String courseSubIdent, IdentityRef identity) {
		return testSessionDao.getUserTestSessions(courseEntry, courseSubIdent, identity);
	}

	@Override
	public void recordTestAssessmentResult(UserTestSession candidateSession, AssessmentResult assessmentResult) {
		// First record full result XML to filesystem
        storeAssessmentResultFile(candidateSession, assessmentResult);

        // Then record test outcome variables to DB
        recordOutcomeVariables(candidateSession, assessmentResult.getTestResult());
	}

	@Override
	public UserTestSession finishTestSession(UserTestSession candidateSession, AssessmentResult assessmentResul, Date timestamp) {
		/* Mark session as finished */
        candidateSession.setFinishTime(timestamp);

        /* Also nullify LIS result info for session. These will be updated later, if pre-conditions match for sending the result back */
        //candidateSession.setLisOutcomeReportingStatus(null);
        //candidateSession.setLisScore(null);
        candidateSession = testSessionDao.update(candidateSession);

        /* Finally schedule LTI result return (if appropriate and sane) */
        //maybeScheduleLtiOutcomes(candidateSession, assessmentResult);
        return candidateSession;
	}
	
    private void recordOutcomeVariables(UserTestSession candidateSession, AbstractResult resultNode) {
        for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
            if (itemVariable instanceof OutcomeVariable
                    || QtiConstants.VARIABLE_DURATION_IDENTIFIER.equals(itemVariable.getIdentifier())) {
                log.audit(candidateSession.getKey() + " :: " + itemVariable.getIdentifier() + " - " + stringifyQtiValue(itemVariable.getComputedValue()));
            }
        }
    }
    
    private String stringifyQtiValue(final Value value) {
        if (qtiModule.isMathAssessExtensionEnabled() && GlueValueBinder.isMathsContentRecord(value)) {
            /* This is a special MathAssess "Maths Content" variable. In this case, we'll record
             * just the ASCIIMath input form or the Maxima form, if either are available.
             */
            final RecordValue mathsValue = (RecordValue) value;
            final SingleValue asciiMathInput = mathsValue.get(MathAssessConstants.FIELD_CANDIDATE_INPUT_IDENTIFIER);
            if (asciiMathInput!=null) {
                return "ASCIIMath[" + asciiMathInput.toQtiString() + "]";
            }
            final SingleValue maximaForm = mathsValue.get(MathAssessConstants.FIELD_MAXIMA_IDENTIFIER);
            if (maximaForm!=null) {
                return "Maxima[" + maximaForm.toQtiString() + "]";
            }
        }
        /* Just convert to QTI string in the usual way */
        return value.toQtiString();
    }
    
    private void storeAssessmentResultFile(final UserTestSession candidateSession, final QtiNode resultNode) {
        final File resultFile = getAssessmentResultFile(candidateSession);
        try(OutputStream resultStream = FileUtils.getBos(resultFile);) {
            qtiSerializer().serializeJqtiObject(resultNode, resultStream);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Unexpected", e);
        }
    }
    
    private File getAssessmentResultFile(final UserTestSession candidateSession) {
    	File myStore = storage.getDirectory(candidateSession.getStorage());
        if(!myStore.exists()) {
        	myStore.mkdirs();
        }
        return new File(myStore, "assessmentResult.xml");
    }

	@Override
	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		return recordCandidateTestEvent(candidateSession, textEventType, null, null, testSessionState, notificationRecorder);
	}

	@Override
	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		
		CandidateEvent event = new CandidateEvent();
		event.setTestEventType(textEventType);
		return recordCandidateTestEvent(candidateSession, textEventType, itemEventType, null, testSessionState, notificationRecorder);
	}

	@Override
	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		return eventDao.create(textEventType, itemEventType, itemKey);
	}
	
	@Override
    public CandidateEvent recordCandidateItemEvent(UserTestSession candidateSession, CandidateItemEventType itemEventType,
    		ItemSessionState itemSessionState, NotificationRecorder notificationRecorder) {
    	return eventDao.create(itemEventType, itemSessionState);
    }
}
