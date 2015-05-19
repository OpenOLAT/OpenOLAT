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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21ContentPackage;
import org.olat.ims.qti21.RequestTimestampContext;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.manager.CandidateDataService;
import org.olat.ims.qti21.manager.TestSessionDAO;
import org.olat.ims.qti21.model.CandidateEvent;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.exception.QtiCandidateStateException;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObjectType;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentObject;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestPlanner;
import uk.ac.ed.ph.jqtiplus.running.TestProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 08.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21DisplayController extends FormBasicController implements CandidateSessionContext {
	
	private final File fUnzippedDirRoot;
	private final String mapperUri;
	
	private QTI21FormItem qtiEl;
	private TestSessionController testSessionController;

    private JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
	private RequestTimestampContext requestTimestampContext = new RequestTimestampContext();
	private CandidateSessionFinisher candidateSessionFinisher = new CandidateSessionFinisher();
	
	private UserTestSession candidateSession;
	private CandidateEvent lastEvent;

	@Autowired
	private TestSessionDAO testSessionDao;
	@Autowired
	private CandidateDataService candidateDataService;
	
	public QTI21DisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "run");
		
		FileResourceManager frm = FileResourceManager.getInstance();
		fUnzippedDirRoot = frm.unzipFileResource(entry.getOlatResource());
		
		candidateSession = testSessionDao.createTestSession(entry, null, getIdentity());
		mapperUri = registerCacheableMapper(ureq, "QTI21Resources::" + entry.getKey(), new ResourcesMapper());
		
		testSessionController = enterSession();

		/* Handle immediate end of test session */
        if (testSessionController.getTestSessionState().isEnded()) {
        	AssessmentResult assessmentResult = null;
            candidateSessionFinisher.finishCandidateSession(candidateSession, assessmentResult);
        }
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		qtiEl = new QTI21FormItem("qtirun");
		formLayout.add("qtirun", qtiEl);
		
		mainForm.setStandaloneRendering(true);
		
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		final ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		qtiEl.setResourceLocator(inputResourceLocator);
		qtiEl.setRequestTimestampContext(requestTimestampContext);
		qtiEl.setTestSessionController(testSessionController);
		qtiEl.setAssessmentObjectUri(createAssessmentObjectUri());
		qtiEl.setCandidateSessionContext(this);
		qtiEl.setMapperUri(mapperUri);
		
		mainForm.setMultipartEnabled(true, Integer.MAX_VALUE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean isTerminated() {
		return candidateSession.getTerminationTime() != null;
	}

	@Override
	public UserTestSession getCandidateSession() {
		return candidateSession;
	}
	
	@Override
	public CandidateEvent getLastEvent() {
		return lastEvent;
	}

	protected CandidateEvent assertSessionEntered(UserTestSession candidateSession) {
		return lastEvent;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == qtiEl) {
			if(event instanceof QTIWorksEvent) {
				QTIWorksEvent qe = (QTIWorksEvent)event;
				processQTIEvent(qe);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void processQTIEvent(QTIWorksEvent qe) {
		switch(qe.getEvent()) {
			case selectItem:
				doSelectItem(qe.getSubCommand());
				break;
			case finishItem:
				doFinish();
				break;
			case reviewItem:
				doReviewItem(qe.getSubCommand());
				break;
			case itemSolution:
				doItemSolution(qe.getSubCommand());
				break;
			case testPartNavigation:
				doTestPartNavigation();
				break;
			case response:
				doResponse(qe.getStringResponseMap());
				break;
			case endTestPart:
				doEndTestPart();
				break;
			case advanceTestPart:
				doAdvanceTestPart();
				break;
			case reviewTestPart:
				doReviewTestPart();
				break;
			case exitTest:
				doExitTest();
				break;
		}
	}
	
	private void doSelectItem(String key) {
		TestPlanNodeKey nodeKey = TestPlanNodeKey.fromString(key);
		Date requestTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
        testSessionController.selectItemNonlinear(requestTimestamp, nodeKey);
	}
	
	private void doReviewItem(String key) {
		TestPlanNodeKey itemKey = TestPlanNodeKey.fromString(key);
		Date requestTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
		
        //Assert.notNull(itemKey, "itemKey");

        /* Get current JQTI state and create JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        //final TestSessionController testSessionController = candidateDataService.createTestSessionController(mostRecentEvent, notificationRecorder);
        final TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Make sure caller may do this */
        //assertSessionNotTerminated(candidateSession);
        try {
            if (!testSessionController.mayReviewItem(itemKey)) {
            	logError("CANNOT_REVIEW_TEST_ITEM", null);
               //candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_ITEM);
                return;
            }
        } catch (final QtiCandidateStateException e) {
        	logError("CANNOT_REVIEW_TEST_ITEM", e);
           // candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_ITEM);
            return;
        }  catch (final RuntimeException e) {
        	logError("CANNOT_REVIEW_TEST_ITEM", e);
            return;// handleExplosion(e, candidateSession);
        }

        /* Record current result state */
        candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.REVIEW_ITEM, null, itemKey, testSessionState, notificationRecorder);
        this.lastEvent = candidateTestEvent;
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
	}

	private void doItemSolution(String key) {
		TestPlanNodeKey itemKey = TestPlanNodeKey.fromString(key);

        /* Get current JQTI state and create JQTI controller */
        NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        //final TestSessionController testSessionController = candidateDataService.createTestSessionController(mostRecentEvent, notificationRecorder);
        TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Make sure caller may do this */
        //assertSessionNotTerminated(candidateSession);
        try {
            if (!testSessionController.mayAccessItemSolution(itemKey)) {
                //candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_SOLUTION_TEST_ITEM);
            	logError("CANNOT_SOLUTION_TEST_ITEM", null);
                return;
            }
        }
        catch (final QtiCandidateStateException e) {
            //candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_SOLUTION_TEST_ITEM);
            logError("CANNOT_SOLUTION_TEST_ITEM", e);
        	return;
        } catch (final RuntimeException e) {
        	logError("Exploded", e);
            return;// handleExplosion(e, candidateSession);
        }

        /* Record current result state */
        candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

        /* Record and log event */
        CandidateEvent candidateTestEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.SOLUTION_ITEM, null, itemKey, testSessionState, notificationRecorder);
        this.lastEvent = candidateTestEvent;
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent); 
	}
	
	//public CandidateSession finishLinearItem(final CandidateSessionContext candidateSessionContext)
    // throws CandidateException {
	private void doFinish() {
		
        /* Get current JQTI state and create JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        //final TestSessionController testSessionController = candidateDataService.createTestSessionController(mostRecentEvent, notificationRecorder);
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
		
		try {
			if (!testSessionController.mayAdvanceItemLinear()) {
				logError("CANNOT_FINISH_LINEAR_TEST_ITEM", null);
                return;
            }
		} catch (QtiCandidateStateException e) {
         	logError("CANNOT_FINISH_LINEAR_TEST_ITEM", e);
         	return;
		} catch (RuntimeException e) {
         	logError("CANNOT_FINISH_LINEAR_TEST_ITEM", e);
			 //return handleExplosion(e, candidateSession);
		}
		 
		// Update state
		final Date requestTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
	    final TestPlanNode nextItemNode = testSessionController.advanceItemLinear(requestTimestamp);

	    // Record current result state
	    final AssessmentResult assessmentResult = candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

	    /* If we ended the testPart and there are now no more available testParts, then finish the session now */
	    if (nextItemNode==null && testSessionController.findNextEnterableTestPart()==null) {
	    	candidateSession = candidateSessionFinisher.finishCandidateSession(candidateSession, assessmentResult);
	    }

	    // Record and log event 
	    final CandidateTestEventType eventType = nextItemNode!=null ? CandidateTestEventType.FINISH_ITEM : CandidateTestEventType.FINISH_FINAL_ITEM;
	   	final CandidateEvent candidateTestEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
	                eventType, null, testSessionState, notificationRecorder);
	   	this.lastEvent = candidateTestEvent;
	}
	
	private void doTestPartNavigation() {
		final Date requestTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
        testSessionController.selectItemNonlinear(requestTimestamp, null);
	}
	
	//public CandidateSession handleResponses(final CandidateSessionContext candidateSessionContext,
    //        final Map<Identifier, StringResponseData> stringResponseMap,
    //        final Map<Identifier, MultipartFile> fileResponseMap,
    //        final String candidateComment)
            
	private void doResponse(Map<Identifier, StringResponseData> stringResponseMap) {
		String candidateComment = null;
		
		//Assert.notNull(candidateSessionContext, "candidateSessionContext");
        //assertSessionType(candidateSessionContext, AssessmentObjectType.ASSESSMENT_TEST);
        //final CandidateSession candidateSession = candidateSessionContext.getCandidateSession();
        //assertSessionNotTerminated(candidateSession);

        /* Get current JQTI state and create JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        //final TestSessionController testSessionController = candidateDataService.createTestSessionController(mostRecentEvent, notificationRecorder);
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
		
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<Identifier, ResponseData>();
        if (stringResponseMap != null) {
            for (final Entry<Identifier, StringResponseData> stringResponseEntry : stringResponseMap.entrySet()) {
                final Identifier identifier = stringResponseEntry.getKey();
                final StringResponseData stringResponseData = stringResponseEntry.getValue();
                responseDataMap.put(identifier, stringResponseData);
            }
        }
        
        boolean allResponsesValid = true;
        boolean allResponsesBound = true;
		
		//TODO files upload
		final Date timestamp = requestTimestampContext.getCurrentRequestTimestamp();
        if (candidateComment != null) {
            testSessionController.setCandidateCommentForCurrentItem(timestamp, candidateComment);
        }

        /* Attempt to bind responses (and maybe perform RP & OP) */
        testSessionController.handleResponsesToCurrentItem(timestamp, responseDataMap);
        
        /* Classify this event */
        final SubmissionMode submissionMode = testSessionController.getCurrentTestPart().getSubmissionMode();
        final CandidateItemEventType candidateItemEventType;
        if (allResponsesValid) {
            candidateItemEventType = submissionMode == SubmissionMode.INDIVIDUAL
            		? CandidateItemEventType.ATTEMPT_VALID : CandidateItemEventType.RESPONSE_VALID;
        }  else {
            candidateItemEventType = allResponsesBound
            		? CandidateItemEventType.RESPONSE_INVALID : CandidateItemEventType.RESPONSE_BAD;
        }

        /* Record resulting event */
        final CandidateEvent candidateEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.ITEM_EVENT, candidateItemEventType, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateEvent);
        this.lastEvent = candidateEvent;

        /* Persist CandidateResponse entities */
        /*for (final CandidateResponse candidateResponse : candidateResponseMap.values()) {
            candidateResponse.setCandidateEvent(candidateEvent);
            candidateResponseDao.persist(candidateResponse);
        }*/
        
        
        /* Record current result state */
        candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

        /* Save any change to session state */
        candidateSession = testSessionDao.update(candidateSession);
	}

	//public CandidateSession endCurrentTestPart(final CandidateSessionContext candidateSessionContext)
	private void doEndTestPart() {
		 /* Update state */
        final Date requestTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
        testSessionController.endCurrentTestPart(requestTimestamp);
	}
	
	private void doAdvanceTestPart() {
		
		//final CandidateSessionContext candidateSessionContext = getCandidateSessionContext();
		
        /* Get current JQTI state and create JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        final TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Perform action */
        final TestPlanNode nextTestPart;
        final Date currentTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
        try {
            nextTestPart = testSessionController.enterNextAvailableTestPart(currentTimestamp);
        } catch (final QtiCandidateStateException e) {
            logError("CANNOT_ADVANCE_TEST_PART", e);
            return;
        } catch (final RuntimeException e) {
            logError("RuntimeException", e);
            return;// handleExplosion(e, candidateSession);
        }

        CandidateTestEventType eventType;
        if (nextTestPart!=null) {
            /* Moved into next test part */
            eventType = CandidateTestEventType.ADVANCE_TEST_PART;
        }
        else {
            /* No more test parts.
             *
             * For single part tests, we terminate the test completely now as the test feedback was shown with the testPart feedback.
             * For multi-part tests, we shall keep the test open so that the test feedback can be viewed.
             */
            if (testSessionState.getTestPlan().getTestPartNodes().size()==1) {
                eventType = CandidateTestEventType.EXIT_TEST;
                testSessionController.exitTest(currentTimestamp);
                candidateSession.setTerminationTime(currentTimestamp);
                candidateSession = testSessionDao.update(candidateSession);
            }
            else {
                eventType = CandidateTestEventType.ADVANCE_TEST_PART;
            }
        }

        /* Record current result state */
        candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
               eventType, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;

		
		/*
        String redirect;
        if (candidateSession.isTerminated()) {
            // We exited the test
            //TODO fire event eXIT
            redirect = redirectToExitUrl(candidateSessionContext, xsrfToken);
        }
        else {
            // Moved onto next part
            redirect = redirectToRenderSession(xid, xsrfToken);
        }
        */
		
	}
	
	private void doReviewTestPart() {
		
        /* Get current JQTI state and create JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        //final TestSessionController testSessionController = candidateDataService.createTestSessionController(mostRecentEvent, notificationRecorder);
        final TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Make sure caller may do this */
        //assertSessionNotTerminated(candidateSession);
        if (testSessionState.getCurrentTestPartKey()==null || !testSessionState.getCurrentTestPartSessionState().isEnded()) {
        	
            // candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_PART);
            logError("CANNOT_REVIEW_TEST_PART", null);
        	return;
        }

        /* Record and log event */
        final CandidateEvent candidateTestEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.REVIEW_TEST_PART, null, null, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;
	}
	
	/**
	 * Exit multi-part tests
	 */
	private void doExitTest() {

        /* Get current JQTI state and create JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final CandidateEvent mostRecentEvent = assertSessionEntered(candidateSession);
        //final TestSessionController testSessionController = candidateDataService.createTestSessionController(mostRecentEvent, notificationRecorder);
        final TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Perform action */
        final Date currentTimestamp = requestTimestampContext.getCurrentRequestTimestamp();
        try {
            testSessionController.exitTest(currentTimestamp);
        } catch (final QtiCandidateStateException e) {
            //candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_EXIT_TEST);
        	logError("CANNOT_EXIT_TEST", null);
            return;
        } catch (final RuntimeException e) {
        	logError("Exploded", null);
            return;// handleExplosion(e, candidateSession);
        }

        /* Update CandidateSession as appropriate */
        candidateSession.setTerminationTime(currentTimestamp);
        candidateSession = testSessionDao.update(candidateSession);

        /* Record current result state (final) */
        candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.EXIT_TEST, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;
		
	}
	
	//private CandidateSession enterCandidateSession(final CandidateSession candidateSession)
	private TestSessionController enterSession() {
		/* Set up listener to record any notifications */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);

        /* Create fresh JQTI+ state & controller for it */
        TestSessionController testSessionController = createNewTestSessionStateAndController(notificationRecorder);
        if (testSessionController==null) {
            return null;
        }
        
        /* Initialise test state and enter test */
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        final Date timestamp = requestTimestampContext.getCurrentRequestTimestamp();
        try {
            testSessionController.initialize(timestamp);
            final int testPartCount = testSessionController.enterTest(timestamp);
            if (testPartCount==1) {
                /* If there is only testPart, then enter this (if possible).
                 * (Note that this may cause the test to exit immediately if there is a failed
                 * preCondition on this part.)
                 */
                testSessionController.enterNextAvailableTestPart(timestamp);
            }
            else {
                /* Don't enter first testPart yet - we shall tell candidate that
                 * there are multiple parts and let them enter manually.
                 */
            }
        }
        catch (final RuntimeException e) {
        	logError("", e);
            return null;
        }
        
        /* Record and log event */
        final CandidateEvent candidateEvent = candidateDataService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.ENTER_TEST, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateEvent);
        this.lastEvent = candidateEvent;

        /* Record current result state */
        final AssessmentResult assessmentResult = candidateDataService.computeAndRecordTestAssessmentResult(candidateSession, testSessionController);

        /* Handle immediate end of test session */
        if (testSessionState.isEnded()) {
            candidateSessionFinisher.finishCandidateSession(candidateSession, assessmentResult);
        }
        
        return testSessionController;
	}
	
	private TestSessionController createNewTestSessionStateAndController(NotificationRecorder notificationRecorder) {
		TestProcessingMap testProcessingMap = getTestProcessingMap();
		/* Generate a test plan for this session */
        final TestPlanner testPlanner = new TestPlanner(testProcessingMap);
        if (notificationRecorder!=null) {
            testPlanner.addNotificationListener(notificationRecorder);
        }
        final TestPlan testPlan = testPlanner.generateTestPlan();

        final TestSessionState testSessionState = new TestSessionState(testPlan);
        
        final TestSessionControllerSettings testSessionControllerSettings = new TestSessionControllerSettings();
        testSessionControllerSettings.setTemplateProcessingLimit(computeTemplateProcessingLimit());

        /* Create controller and wire up notification recorder */
        final TestSessionController result = new TestSessionController(jqtiExtensionManager,
                testSessionControllerSettings, testProcessingMap, testSessionState);
        if (notificationRecorder!=null) {
            result.addNotificationListener(notificationRecorder);
        }
		return result;
	}
	
	private TestProcessingMap getTestProcessingMap() {
		boolean assessmentPackageIsValid = true;

		final ResolvedAssessmentTest resolvedAssessmentTest = loadAndResolveAssessmentObject();
		BadResourceException ex = resolvedAssessmentTest.getTestLookup().getBadResourceException();
		if(ex instanceof QtiXmlInterpretationException) {
			QtiXmlInterpretationException exml = (QtiXmlInterpretationException)ex;
			System.out.println(exml.getInterpretationFailureReason());
			for(QtiModelBuildingError err :exml.getQtiModelBuildingErrors()) {
				System.out.println(err);
			}
		}
		
		TestProcessingInitializer initializer = new TestProcessingInitializer(resolvedAssessmentTest, assessmentPackageIsValid);
		TestProcessingMap result = initializer.initialize();
		return result;
	}
	
	public <E extends ResolvedAssessmentObject<?>> E loadAndResolveAssessmentObject() {
		
		QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager);
        
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		final ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
        final URI assessmentObjectSystemId = createAssessmentObjectUri();
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
	
	public URI createAssessmentObjectUri() {
		File manifestPath = new File(fUnzippedDirRoot, "imsmanifest.xml");
		QTI21ContentPackage	cp = new QTI21ContentPackage(manifestPath.toPath());
		try {
			Path testPath = cp.getTest();
			return testPath.toUri();
		} catch (IOException e) {
			logError("", e);
		}
		return null;
	}
	
	/**
	 * Request limit configured outer of the QTI 2.1 file.
	 * @return
	 */
	public int computeTemplateProcessingLimit() {
		final Integer requestedLimit = null;// deliverySettings.getTemplateProcessingLimit();
		if (requestedLimit == null) {
			/* Not specified, so use default */
			return JqtiPlus.DEFAULT_TEMPLATE_PROCESSING_LIMIT;
		}
		final int requestedLimitIntValue = requestedLimit.intValue();
		return requestedLimitIntValue > 0 ? requestedLimitIntValue : JqtiPlus.DEFAULT_TEMPLATE_PROCESSING_LIMIT;
	}
	
	private class CandidateSessionFinisher {
		

	    public UserTestSession finishCandidateSession(UserTestSession candidateSession, AssessmentResult assessmentResult) {
	        /* Mark session as finished */
	        candidateSession.setFinishTime(requestTimestampContext.getCurrentRequestTimestamp());

	        /* Also nullify LIS result info for session. These will be updated later, if pre-conditions match for sending the result back */
	        //candidateSession.setLisOutcomeReportingStatus(null);
	        //candidateSession.setLisScore(null);
	        candidateSession = testSessionDao.update(candidateSession);

	        /* Finally schedule LTI result return (if appropriate and sane) */
	        //maybeScheduleLtiOutcomes(candidateSession, assessmentResult);
	        return candidateSession;
	    }
	}
}