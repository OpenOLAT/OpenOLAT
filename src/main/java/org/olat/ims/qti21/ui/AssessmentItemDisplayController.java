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
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.FileUtils;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.audit.AssessmentResponseData;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateExceptionReason;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.ui.ResponseInput.Base64Input;
import org.olat.ims.qti21.ui.ResponseInput.FileInput;
import org.olat.ims.qti21.ui.ResponseInput.StringInput;
import org.olat.ims.qti21.ui.components.AssessmentItemFormItem;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.exception.QtiCandidateStateException;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.ItemProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.types.FileResponseData;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemDisplayController extends BasicController implements CandidateSessionContext {
	
	private final VelocityContainer mainVC;
	private QtiWorksController qtiWorksCtrl;
	
	private ItemSessionController itemSessionController;
	
	private final String mapperUri;
	private final File fUnzippedDirRoot;
	private final File itemFileRef;
	private final QTI21DeliveryOptions deliveryOptions;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	/* This directory will be deleted at the disposal of the controller */
	private File submissionDirToDispose;
	
	private CandidateEvent lastEvent;
	private Date currentRequestTimestamp;
	private RepositoryEntry entry;
	private AssessmentTestSession candidateSession;
	
	private final AssessmentSessionAuditLogger candidateAuditLogger;

	@Autowired
	private QTI21Service qtiService;
	
	/**
	 * OPen in memory session
	 * @param ureq
	 * @param wControl
	 * @param authorMode
	 * @param resolvedAssessmentItem
	 * @param fUnzippedDirRoot
	 * @param itemFileRef
	 */
	public AssessmentItemDisplayController(UserRequest ureq, WindowControl wControl, ResolvedAssessmentItem resolvedAssessmentItem,
			File fUnzippedDirRoot, File itemFileRef, AssessmentSessionAuditLogger candidateAuditLogger) {
		super(ureq, wControl);
		
		this.itemFileRef = itemFileRef;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.candidateAuditLogger = candidateAuditLogger;
		deliveryOptions = QTI21DeliveryOptions.defaultSettings();
		currentRequestTimestamp = ureq.getRequestTimestamp();
		candidateSession = qtiService.createInMemoryAssessmentTestSession(getIdentity());
		submissionDirToDispose = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(), new ResourcesMapper(itemFileRef.toURI(), submissionDirToDispose));
		
		itemSessionController = enterSession(ureq);
		
		if(itemSessionController == null) {
			mainVC = createVelocityContainer("error");
		} else if (itemSessionController.getItemSessionState().isEnded()) {
			mainVC = createVelocityContainer("end");
		} else {
			mainVC = createVelocityContainer("run");
			initQtiWorks(ureq);
		}
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemDisplayController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, File fUnzippedDirRoot,
			AssessmentSessionAuditLogger candidateAuditLogger) {
		super(ureq, wControl);
		
		this.itemFileRef = new File(fUnzippedDirRoot, itemRef.getHref().toString());
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.candidateAuditLogger = candidateAuditLogger;
		deliveryOptions = QTI21DeliveryOptions.defaultSettings();
		currentRequestTimestamp = ureq.getRequestTimestamp();
		candidateSession = qtiService.createInMemoryAssessmentTestSession(getIdentity());
		submissionDirToDispose = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(), new ResourcesMapper(itemFileRef.toURI(), submissionDirToDispose));
		
		itemSessionController = enterSession(ureq);
		
		if(itemSessionController == null) {
			mainVC = createVelocityContainer("error");
		} else if (itemSessionController.getItemSessionState().isEnded()) {
			mainVC = createVelocityContainer("end");
		} else {
			mainVC = createVelocityContainer("run");
			initQtiWorks(ureq);
		}
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemDisplayController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, AssessmentEntry assessmentEntry, boolean authorMode,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef,
			File fUnzippedDirRoot, File itemFile,
			AssessmentSessionAuditLogger candidateAuditLogger) {
		super(ureq, wControl);
		
		this.itemFileRef = itemFile;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.candidateAuditLogger = candidateAuditLogger;
		deliveryOptions = QTI21DeliveryOptions.defaultSettings();
		currentRequestTimestamp = ureq.getRequestTimestamp();
		candidateSession = qtiService.createAssessmentTestSession(getIdentity(), null, assessmentEntry, testEntry, itemRef.getIdentifier().toString(), testEntry, authorMode);
		File submissionDir = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(), new ResourcesMapper(itemFileRef.toURI(), submissionDir));
		
		itemSessionController = enterSession(ureq);
		
		if(itemSessionController == null) {
			mainVC = createVelocityContainer("error");
		} else if (itemSessionController.getItemSessionState().isEnded()) {
			mainVC = createVelocityContainer("end");
		} else {
			mainVC = createVelocityContainer("run");
			initQtiWorks(ureq);
		}
		putInitialPanel(mainVC);
	}
	
	private void initQtiWorks(UserRequest ureq) {
		String filename = itemFileRef.getName();
		qtiWorksCtrl = new QtiWorksController(ureq, getWindowControl(), filename);
		listenTo(qtiWorksCtrl);
		mainVC.put("qtirun", qtiWorksCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		if(submissionDirToDispose != null) {
			FileUtils.deleteDirsAndFiles(submissionDirToDispose, true, true);
		}
	}
	
	public boolean isExploded() {
		return itemSessionController == null;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public AssessmentTestSession getCandidateSession() {
		return candidateSession;
	}

	@Override
	public CandidateEvent getLastEvent() {
		return lastEvent;
	}

	@Override
	public Date getCurrentRequestTimestamp() {
		return currentRequestTimestamp;
	}

	@Override
	public boolean isMarked(String itemKey) {
		return false;
	}

	@Override
	public boolean isRubricHidden(Identifier sectionKey) {
		return false;
	}

	@Override
	public int getNumber(TestPlanNode node) {
		return 1;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		currentRequestTimestamp = ureq.getRequestTimestamp();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(qtiWorksCtrl == source) {
			if(event instanceof QTIWorksAssessmentItemEvent) {
				processQTIEvent(ureq, (QTIWorksAssessmentItemEvent)event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void processQTIEvent(UserRequest ureq, QTIWorksAssessmentItemEvent qe) {
		currentRequestTimestamp = ureq.getRequestTimestamp();
		
		switch(qe.getEvent()) {
			case solution:
				requestSolution(ureq);
				break;
			case response:
				handleResponses(ureq, qe.getStringResponseMap(), qe.getFileResponseMap(), qe.getComment());
				break;
			case tmpResponse:
				handleTemporaryResponses(ureq, qe.getStringResponseMap());
				break;
			case close:
				endSession(ureq);
				break;
			case exit:
				exitSession(ureq);
				break;
			case resetsoft:
				break;
			case resethard:
				break;
			case source:
				logError("QtiWorks event source not implemented", null);
				break;
			case state:
				logError("QtiWorks event state not implemented", null);
				break;
			case validation:
				logError("QtiWorks event validation not implemented", null);
				break;
			case authorview:
				logError("QtiWorks event authorview not implemented", null);
				break;
			case result:
				logError("QtiWorks event result not implemented", null);
				break;
		}
	}
	
	private ItemSessionController enterSession(UserRequest ureq /*, final UserTestSession candidateSession */) {
        /* Set up listener to record any notifications */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);

        /* Create fresh JQTI+ state Object and try to create controller */
        itemSessionController = createNewItemSessionStateAndController(notificationRecorder);
        if (itemSessionController == null) {
        		logError("Cannot create item session controller for:" + itemFileRef, null);
            return null;
        }

        /* Try to Initialise JQTI+ state */
        final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
        try {
            final Date timestamp = ureq.getRequestTimestamp();
            itemSessionController.initialize(timestamp);
            itemSessionController.performTemplateProcessing(timestamp);
            itemSessionController.enterItem(timestamp);
        }  catch (final RuntimeException e) {
        		logError("", e);
            return null;
        }

        /* Record and log entry event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, null, entry,
        		CandidateItemEventType.ENTER, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;

        /* Record current result state */
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);

        /* Handle immediate end of session */
        if (itemSessionState.isEnded()) {
            qtiService.finishItemSession(candidateSession, assessmentResult, ureq.getRequestTimestamp());
        }

        return itemSessionController;
    }
	
    public ItemSessionController createNewItemSessionStateAndController(NotificationRecorder notificationRecorder) {
        /* Resolve the underlying JQTI+ object */
        final ItemProcessingMap itemProcessingMap = getItemProcessingMap();
        if (itemProcessingMap == null) {
            return null;
        }

        /* Create fresh state for session */
        final ItemSessionState itemSessionState = new ItemSessionState();

        /* Create config for ItemSessionController */
        final ItemSessionControllerSettings itemSessionControllerSettings = new ItemSessionControllerSettings();
        itemSessionControllerSettings.setTemplateProcessingLimit(computeTemplateProcessingLimit());
        itemSessionControllerSettings.setMaxAttempts(10 /*itemDeliverySettings.getMaxAttempts() */);

        /* Create controller and wire up notification recorder */
        final ItemSessionController result = new ItemSessionController(qtiService.jqtiExtensionManager(),
                itemSessionControllerSettings, itemProcessingMap, itemSessionState);
        if (notificationRecorder != null) {
            result.addNotificationListener(notificationRecorder);
        }
        return result;
    }
    
    public ItemProcessingMap getItemProcessingMap() {
        return new ItemProcessingInitializer(resolvedAssessmentItem, true).initialize();

    }
    
	public int computeTemplateProcessingLimit() {
		final Integer requestedLimit = deliveryOptions.getTemplateProcessingLimit();
		if (requestedLimit == null) {
			/* Not specified, so use default */
			return JqtiPlus.DEFAULT_TEMPLATE_PROCESSING_LIMIT;
		}
		final int requestedLimitIntValue = requestedLimit.intValue();
		return requestedLimitIntValue > 0 ? requestedLimitIntValue : JqtiPlus.DEFAULT_TEMPLATE_PROCESSING_LIMIT;
	}
	
	public void handleTemporaryResponses(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap) {

		/* Retrieve current JQTI state and set up JQTI controller */
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

		/* Make sure an attempt is allowed */
		if (itemSessionState.isEnded()) {
			candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.RESPONSES_NOT_EXPECTED, null);
			logError("RESPONSES_NOT_EXPECTED", null);
			return;
		}

		/* Build response map in required format for JQTI+.
		 * NB: The following doesn't test for duplicate keys in the two maps. I'm not sure
		 * it's worth the effort.
		 */
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<>();
		final Map<Identifier, AssessmentResponse> assessmentResponseDataMap = new HashMap<>();

		if (stringResponseMap!=null) {
			for (final Entry<Identifier, ResponseInput> stringResponseEntry : stringResponseMap.entrySet()) {
				Identifier identifier = stringResponseEntry.getKey();
				ResponseInput responseData = stringResponseEntry.getValue();
				if(responseData instanceof StringInput) {
					responseDataMap.put(identifier, new StringResponseData(((StringInput)responseData).getResponseData()));
				}
			}
		}
 
		final Date timestamp = ureq.getRequestTimestamp();


		/* Attempt to bind responses */
		boolean allResponsesValid = false;
		boolean allResponsesBound = false;
		try {
			itemSessionController.bindResponses(timestamp, responseDataMap);
		} catch (final QtiCandidateStateException e) {
	        candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.RESPONSES_NOT_EXPECTED, null);
			logError("RESPONSES_NOT_EXPECTED", e);
			return;
		} catch (final RuntimeException e) {
			logError("", e);
			return;
		}

		/* Record resulting attempt and event */
		final CandidateItemEventType eventType = allResponsesBound ?
				(allResponsesValid ? CandidateItemEventType.ATTEMPT_VALID : CandidateItemEventType.RESPONSE_INVALID)
				: CandidateItemEventType.RESPONSE_BAD;
		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, null, entry,
	                eventType, itemSessionState, notificationRecorder);
		candidateAuditLogger.logCandidateEvent(candidateEvent, assessmentResponseDataMap);
		lastEvent = candidateEvent;
	}
	
	public void handleResponses(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap,
			Map<Identifier,ResponseInput> fileResponseMap, String candidateComment) {

		/* Retrieve current JQTI state and set up JQTI controller */
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

		/* Make sure an attempt is allowed */
		if (itemSessionState.isEnded()) {
			candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.RESPONSES_NOT_EXPECTED, null);
			logError("RESPONSES_NOT_EXPECTED", null);
			return;
		}

		/* Build response map in required format for JQTI+.
		 * NB: The following doesn't test for duplicate keys in the two maps. I'm not sure
		 * it's worth the effort.
		 */
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<>();
		//final Map<Identifier, CandidateFileSubmission> fileSubmissionMap = new HashMap<>();
		final Map<Identifier, AssessmentResponse> assessmentResponseDataMap = new HashMap<>();

		if (stringResponseMap!=null) {
			for (final Entry<Identifier, ResponseInput> stringResponseEntry : stringResponseMap.entrySet()) {
				Identifier identifier = stringResponseEntry.getKey();
				ResponseInput responseData = stringResponseEntry.getValue();
				if(responseData instanceof StringInput) {
					responseDataMap.put(identifier, new StringResponseData(((StringInput)responseData).getResponseData()));
				} else if(responseData instanceof Base64Input) {
					//TODO
				} else if(responseData instanceof FileInput) {
					
				}
			}
		}
 
        if (fileResponseMap!=null) {
            for (final Entry<Identifier, ResponseInput> fileResponseEntry : fileResponseMap.entrySet()) {
                final Identifier identifier = fileResponseEntry.getKey();
                final FileInput multipartFile = (FileInput)fileResponseEntry.getValue();
                if (!multipartFile.isEmpty()) {
                    //final CandidateFileSubmission fileSubmission = candidateUploadService.importFileSubmission(candidateSession, multipartFile);
                	File storedFile = qtiService.importFileSubmission(candidateSession, multipartFile.getMultipartFileInfos());
                	final FileResponseData fileResponseData = new FileResponseData(storedFile, multipartFile.getContentType(), multipartFile.getFileName());
                    responseDataMap.put(identifier, fileResponseData);
    				assessmentResponseDataMap.put(identifier, new AssessmentResponseData(identifier, fileResponseData));
                    //fileSubmissionMap.put(identifier, fileSubmission);
                }
            }
        }

		/* Submit comment (if provided)
		 * NB: Do this first in case next actions end the item session.
		 */
		final Date timestamp = ureq.getRequestTimestamp();
		if (candidateComment != null) {
			try {
				itemSessionController.setCandidateComment(timestamp, candidateComment);
			} catch (final QtiCandidateStateException e) {
				candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANDIDATE_COMMENT_FORBIDDEN, e);
				logError("CANDIDATE_COMMENT_FORBIDDEN", null);
				return;
			} catch (final RuntimeException e) {
				logError("", e);
				return; //handleExplosion(e, candidateSession);
			}
		}

		/* Attempt to bind responses */
		boolean allResponsesValid = false, allResponsesBound = false;
		try {
			itemSessionController.bindResponses(timestamp, responseDataMap);

			/* Note any responses that failed to bind */
			final Set<Identifier> badResponseIdentifiers = itemSessionState.getUnboundResponseIdentifiers();
			allResponsesBound = badResponseIdentifiers.isEmpty();

			/* Now validate the responses according to any constraints specified by the interactions */
			if (allResponsesBound) {
				final Set<Identifier> invalidResponseIdentifiers = itemSessionState.getInvalidResponseIdentifiers();
				allResponsesValid = invalidResponseIdentifiers.isEmpty();
			}

			/* (We commit responses immediately here) */
			itemSessionController.commitResponses(timestamp);

			/* Invoke response processing (only if responses are valid) */
			if (allResponsesValid) {
				itemSessionController.performResponseProcessing(timestamp);
			}
		} catch (final QtiCandidateStateException e) {
	        candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.RESPONSES_NOT_EXPECTED, null);
			logError("RESPONSES_NOT_EXPECTED", e);
			return;
		} catch (final RuntimeException e) {
			logError("", e);
			return;// handleExplosion(e, candidateSession);
		}

		/* Record resulting attempt and event */
		final CandidateItemEventType eventType = allResponsesBound ?
				(allResponsesValid ? CandidateItemEventType.ATTEMPT_VALID : CandidateItemEventType.RESPONSE_INVALID)
				: CandidateItemEventType.RESPONSE_BAD;
		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, null, entry,
	                eventType, itemSessionState, notificationRecorder);
		candidateAuditLogger.logCandidateEvent(candidateEvent, assessmentResponseDataMap);
		lastEvent = candidateEvent;

		/* Record current result state, or finish session */
		updateSessionFinishedStatus(ureq);
	}
	
    private AssessmentTestSession updateSessionFinishedStatus(UserRequest ureq) {
        /* Record current result state and maybe close session */
        final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);
        if (itemSessionState.isEnded()) {
            qtiService.finishItemSession(candidateSession, assessmentResult, null);
        }
        else {
            if (candidateSession != null && candidateSession.getFinishTime() != null) {
                /* (Session is being reopened) */
                candidateSession.setFinishTime(null);
                candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
            }
        }
        return candidateSession;
    }
    
    public AssessmentResult computeAndRecordItemAssessmentResult(UserRequest ureq) {
        final AssessmentResult assessmentResult = computeItemAssessmentResult(ureq);
        qtiService.recordItemAssessmentResult(candidateSession, assessmentResult, candidateAuditLogger);
        return assessmentResult;
    }
    
    public AssessmentResult computeItemAssessmentResult(UserRequest ureq) {
    	String baseUrl = "http://localhost:8080/olat";
        final URI sessionIdentifierSourceId = URI.create(baseUrl);
        final String sessionIdentifier = "itemsession/" + (candidateSession == null ? "sdfj" : candidateSession.getKey());
        return itemSessionController.computeAssessmentResult(ureq.getRequestTimestamp(), sessionIdentifier, sessionIdentifierSourceId);
    }
    
    public void requestSolution(UserRequest ureq) {
        ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

        /* Make sure caller may do this */
        boolean allowSolutionWhenOpen = true;//itemDeliverySettings.isAllowSolutionWhenOpen()

        if (!itemSessionState.isEnded()  && !allowSolutionWhenOpen) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.SOLUTION_WHEN_INTERACTING_FORBIDDEN, null);
        	logError("SOLUTION_WHEN_INTERACTING_FORBIDDEN", null);
            return;
        } else if (itemSessionState.isEnded() /* && !itemDeliverySettings.isAllowSoftResetWhenEnded() */) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.SOLUTION_WHEN_ENDED_FORBIDDEN, null);
        	logError("SOLUTION_WHEN_ENDED_FORBIDDEN", null);
            return;
        }

        /* End session if still open */
        final Date timestamp = ureq.getRequestTimestamp();
        boolean isClosingSession = false;
        if (!itemSessionState.isEnded()) {
            isClosingSession = true;
            try {
                itemSessionController.endItem(timestamp);
            } catch (final QtiCandidateStateException e) {
                candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.SOLUTION_WHEN_ENDED_FORBIDDEN, null);
            	logError("SOLUTION_WHEN_ENDED_FORBIDDEN", e);
                return;
            } catch (final RuntimeException e) {
            	logError("", e);
                return;// handleExplosion(e, candidateSession);
            }
        }

        /* Record current result state, and maybe close session */
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);
        if (isClosingSession) {
            qtiService.finishItemSession(candidateSession, assessmentResult, timestamp);
        }

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, null, entry,
        		CandidateItemEventType.SOLUTION, itemSessionState);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;
    }
    
	public void endSession(UserRequest ureq) {
        NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        //final ItemSessionController itemSessionController = candidateDataService.createItemSessionController(mostRecentEvent, notificationRecorder);
        ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

        /* Check this is allowed in current state */
        
        if (itemSessionState.isEnded()) {
        	logError("END_SESSION_WHEN_ALREADY_ENDED", null);
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.END_SESSION_WHEN_ALREADY_ENDED, null);
            return;
        } /* else if (!itemDeliverySettings.isAllowEnd()) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.END_SESSION_WHEN_INTERACTING_FORBIDDEN);
            return null;
        }*/

        /* Update state */
        final Date timestamp = ureq.getRequestTimestamp();
        try {
            itemSessionController.endItem(timestamp);
        } catch (QtiCandidateStateException e) {
        	String msg = itemSessionState.isEnded() ? "END_SESSION_WHEN_ALREADY_ENDED" : "END_SESSION_WHEN_INTERACTING_FORBIDDEN";
        	logError(msg, e);
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, itemSessionState.isEnded() ? CandidateExceptionReason.END_SESSION_WHEN_ALREADY_ENDED : CandidateExceptionReason.END_SESSION_WHEN_INTERACTING_FORBIDDEN, null);
            return;
        }
        catch (final RuntimeException e) {
        	logError("", e);
            return; //handleExplosion(e, candidateSession);
        }

        /* Record current result state */
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, null, entry,
                CandidateItemEventType.END, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;

        /* Close session */
        qtiService.finishItemSession(candidateSession, assessmentResult, timestamp);
    }
	
	public void exitSession(UserRequest ureq) {
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

		/* Are we terminating a session that hasn't already been ended? If so end the session and record final result. */
		final Date currentTimestamp = ureq.getRequestTimestamp();
		if (!itemSessionState.isEnded()) {
		    try {
		        itemSessionController.endItem(currentTimestamp);
		    } catch (final RuntimeException e) {
		    	logError("", e);
		        return;// handleExplosion(e, candidateSession);
		    }
		    final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);
		    qtiService.finishItemSession(candidateSession, assessmentResult, currentTimestamp);
		}
		
		/* Update session entity */
		candidateSession.setTerminationTime(currentTimestamp);
		candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
		
		/* Record and log event */
		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, null, entry,
					CandidateItemEventType.EXIT, itemSessionState);
		lastEvent = candidateEvent;
		candidateAuditLogger.logCandidateEvent(candidateEvent);
	}
	
	/**
	 * QtiWorks manage the form tag itself.
	 * 
	 * Initial date: 20.05.2015<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private class QtiWorksController extends AbstractQtiWorksController {
		
		private AssessmentItemFormItem qtiEl;
		private final String filename;
		
		public QtiWorksController(UserRequest ureq, WindowControl wControl, String filename) {
			super(ureq, wControl, "ff_run");
			this.filename = filename;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			mainForm.setMultipartEnabled(true);

			FormSubmit submit = uifactory.addFormSubmitButton("submit", formLayout);
			qtiEl = new AssessmentItemFormItem("qtirun", submit);
			formLayout.add("qtirun", qtiEl);

			ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
			final ResourceLocator inputResourceLocator = 
	        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
			qtiEl.setResourceLocator(inputResourceLocator);
			qtiEl.setItemSessionController(itemSessionController);
			qtiEl.setResolvedAssessmentItem(resolvedAssessmentItem);

			File manifestPath = new File(fUnzippedDirRoot, filename);
			qtiEl.setAssessmentObjectUri(manifestPath.toURI());
			qtiEl.setCandidateSessionContext(AssessmentItemDisplayController.this);
			qtiEl.setMapperUri(mapperUri);
		}
		
		@Override
		protected Identifier getResponseIdentifierFromUniqueId(String uniqueId) {
			return qtiEl.getInteractionOfResponseUniqueIdentifier(uniqueId).getResponseIdentifier();
		}

		@Override
		protected void formOK(UserRequest ureq) {
			processResponse(ureq, qtiEl.getSubmitButton());
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(source == qtiEl) {
				if(event instanceof QTIWorksAssessmentItemEvent) {
					QTIWorksAssessmentItemEvent qwaie = (QTIWorksAssessmentItemEvent)event;
					if(qwaie.getEvent() == QTIWorksAssessmentItemEvent.Event.tmpResponse) {
						processTemporaryResponse(ureq);
					} else {
						fireEvent(ureq, event);
					}
				}
			} else if(source instanceof FormLink) {
				FormLink formLink = (FormLink)source;
				processResponse(ureq, formLink);	
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void fireTemporaryResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap) {
			fireEvent(ureq, new QTIWorksAssessmentItemEvent(QTIWorksAssessmentItemEvent.Event.tmpResponse, stringResponseMap, null, null, null));
		}

		@Override
		protected void fireResponse(UserRequest ureq, FormItem source,
				Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier, ResponseInput> fileResponseMap,
				String comment) {
			fireEvent(ureq, new QTIWorksAssessmentItemEvent(QTIWorksAssessmentItemEvent.Event.response, stringResponseMap, fileResponseMap, comment, source));
		}
	}
}
