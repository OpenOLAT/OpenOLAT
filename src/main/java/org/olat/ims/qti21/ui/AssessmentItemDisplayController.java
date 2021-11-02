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
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesAssessmentItemListener;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.ResponseFormater;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateExceptionReason;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.ui.ResponseInput.Base64Input;
import org.olat.ims.qti21.ui.ResponseInput.FileInput;
import org.olat.ims.qti21.ui.ResponseInput.StringInput;
import org.olat.ims.qti21.ui.components.AssessmentCountDownFormItem;
import org.olat.ims.qti21.ui.components.AssessmentItemFormItem;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.exception.QtiCandidateStateException;
import uk.ac.ed.ph.jqtiplus.exception.ResponseBindingException;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
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
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemDisplayController extends BasicController implements CandidateSessionContext {
	
	protected final VelocityContainer mainVC;
	protected QtiWorksController qtiWorksCtrl;
	
	protected ItemSessionController itemSessionController;
	
	private final String mapperUri;
	private final File fUnzippedDirRoot;
	private final File itemFileRef;
	private final QTI21DeliveryOptions deliveryOptions;
	protected final ResolvedAssessmentItem resolvedAssessmentItem;
	
	/* This directory will be deleted at the disposal of the controller */
	private File submissionDirToDispose;
	
	private CandidateEvent lastEvent;
	private Date currentRequestTimestamp;
	private RepositoryEntry entry;
	private RepositoryEntry testEntry;
	protected AssessmentItemSession itemSession;
	protected AssessmentTestSession candidateSession;
	
	protected final OutcomesAssessmentItemListener outcomesListener;
	private final AssessmentSessionAuditLogger candidateAuditLogger;

	@Autowired
	protected QTI21Service qtiService;
	
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
			File fUnzippedDirRoot, File itemFileRef, QTI21DeliveryOptions deliveryOptions,
			AssessmentSessionAuditLogger candidateAuditLogger) {
		super(ureq, wControl);
		
		this.itemFileRef = itemFileRef;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.candidateAuditLogger = candidateAuditLogger;
		this.deliveryOptions = deliveryOptions;
		outcomesListener = new InMemoryOutcomeListener();
		currentRequestTimestamp = ureq.getRequestTimestamp();
		candidateSession = qtiService.createInMemoryAssessmentTestSession(getIdentity());
		submissionDirToDispose = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(),
				new ResourcesMapper(itemFileRef.toURI(), fUnzippedDirRoot, submissionDirToDispose));
		
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
			QTI21DeliveryOptions deliveryOptions, AssessmentSessionAuditLogger candidateAuditLogger) {
		super(ureq, wControl);
		
		this.itemFileRef = new File(fUnzippedDirRoot, itemRef.getHref().toString());
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.candidateAuditLogger = candidateAuditLogger;
		this.deliveryOptions = deliveryOptions;
		outcomesListener = new InMemoryOutcomeListener();
		currentRequestTimestamp = ureq.getRequestTimestamp();
		candidateSession = qtiService.createInMemoryAssessmentTestSession(getIdentity());
		submissionDirToDispose = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(), new ResourcesMapper(itemFileRef.toURI(), fUnzippedDirRoot, submissionDirToDispose));
		
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
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, AssessmentEntry assessmentEntry, boolean authorMode,
			ResolvedAssessmentItem resolvedAssessmentItem,  File fUnzippedDirRoot, File itemFile,
			QTI21DeliveryOptions deliveryOptions, OutcomesAssessmentItemListener outcomesListener, AssessmentSessionAuditLogger candidateAuditLogger) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentItemDisplayController.class, ureq.getLocale()));
		
		this.entry = entry;
		this.testEntry = testEntry;
		this.itemFileRef = itemFile;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.candidateAuditLogger = candidateAuditLogger;
		this.deliveryOptions = deliveryOptions;
		this.outcomesListener = outcomesListener;
		currentRequestTimestamp = ureq.getRequestTimestamp();
		candidateSession = initOrResumeAssessmentTestSession(entry, subIdent, testEntry, assessmentEntry, authorMode);
		String assessmentItemIdentifier = resolvedAssessmentItem.getRootNodeLookup()
        		.extractIfSuccessful().getIdentifier();
		itemSession = qtiService.getOrCreateAssessmentItemSession(candidateSession, null, assessmentItemIdentifier);
		
		File submissionDir = qtiService.getSubmissionDirectory(candidateSession);
		mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(), new ResourcesMapper(itemFileRef.toURI(), fUnzippedDirRoot, submissionDir));
		
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
	
	protected AssessmentTestSession initOrResumeAssessmentTestSession(RepositoryEntry courseEntry, String subIdent, RepositoryEntry referenceEntry,
			AssessmentEntry assessmentEntry, boolean authorMode) {
		return qtiService.createAssessmentTestSession(getIdentity(), null, assessmentEntry, courseEntry, subIdent, referenceEntry, null, authorMode);
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
				handleTemporaryResponses(ureq, qe.getStringResponseMap(), qe.getFileResponseMap(), qe.getComment());
				break;
			case fullTmpResponse:
				handleTemporaryResponses(ureq, qe.getStringResponseMap(), qe.getFileResponseMap(), qe.getComment());
				break;
			case deleteResponse:
				deleteResponse(qe.getSubCommand());
				break;
			case close:
				endSession(ureq);
				break;
			case exit:
				exitSession(ureq);
				break;
			case resetsoft:
				handleResetSoft(ureq);
				break;
			case resethard:
				handleResetHard(ureq);
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
			case back:
				next(ureq, qe);
				break;
			case skip:
				next(ureq, qe);
				break;
			case next:
				next(ureq, qe);
				break;
			case timesUp:
				next(ureq, qe);
				break;
				
		}
	}
	
	protected ItemSessionController enterSession(UserRequest ureq) {
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
            if(!itemSessionState.isEntered()) {
            	itemSessionController.initialize(timestamp);
            	itemSessionController.performTemplateProcessing(timestamp);
            	itemSessionController.enterItem(timestamp);
            } else if (!itemSessionState.isEnded() && itemSessionState.isSuspended()) {
            	itemSessionController.unsuspendItemSession(timestamp);
            } else if(itemSessionState.isEnded()) {
            	itemSessionState.setEndTime(null);
            	itemSessionState.setExitTime(null);
            }
        }  catch (final RuntimeException e) {
        	logError("", e);
            return null;
        }

        /* Record and log entry event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
        		CandidateItemEventType.ENTER, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;

        /* Record current result state */
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);

        /* Handle immediate end of session */
        if (itemSessionState.isEnded()) {
            finishCandidateSession(assessmentResult, ureq.getRequestTimestamp());
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
        ItemSessionState itemSessionState = loadItemSessionState();

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
    
    protected ItemSessionState loadItemSessionState() {
    	return new ItemSessionState();
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
	
	public void handleResetSoft(UserRequest ureq) {
        /* Retrieve current JQTI state and set up JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

        /* Make sure caller may reset the session */
        if (!itemSessionState.isEnded() && !deliveryOptions.isEnableAssessmentItemResetSoft()) {
            return;
        } else if (itemSessionState.isEnded() && !deliveryOptions.isEnableAssessmentItemResetSoft()) {
            return;
        }

        /* Update state */
        final Date timestamp = ureq.getRequestTimestamp();
        try {
            itemSessionController.resetItemSessionSoft(timestamp, true);
        }  catch (final QtiCandidateStateException e) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, itemSessionState.isEnded() ? CandidateExceptionReason.SOFT_RESET_SESSION_WHEN_ENDED_FORBIDDEN : CandidateExceptionReason.SOFT_RESET_SESSION_WHEN_INTERACTING_FORBIDDEN, e);
            return;
        } catch (final RuntimeException e) {
			logError("", e);
            return;
        }

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession,
        		testEntry, entry, CandidateItemEventType.RESET, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);

        /* Record current result state, or close session */
        updateSessionFinishedStatus(ureq);
	}
	
	public void handleResetHard(UserRequest ureq) {
        /* Retrieve current JQTI state and set up JQTI controller */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

        if (!itemSessionState.isEnded() && !deliveryOptions.isEnableAssessmentItemResetHard()) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.HARD_RESET_SESSION_WHEN_INTERACTING_FORBIDDEN, null);
            return;
        } else if (itemSessionState.isEnded() && !deliveryOptions.isEnableAssessmentItemResetHard()) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.HARD_RESET_SESSION_WHEN_ENDED_FORBIDDEN, null);
            return;
        }

        /* Update state */
        final Date timestamp = ureq.getRequestTimestamp();
        try {
            itemSessionController.resetItemSessionHard(timestamp, true);
        }  catch (final QtiCandidateStateException e) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, itemSessionState.isEnded() ? CandidateExceptionReason.HARD_RESET_SESSION_WHEN_ENDED_FORBIDDEN : CandidateExceptionReason.HARD_RESET_SESSION_WHEN_INTERACTING_FORBIDDEN, e);
            return;
        }  catch (final RuntimeException e) {
        	logError("", e);
            return;
        }

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
                CandidateItemEventType.REINIT, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);

        /* Record current result state, or close session */
        updateSessionFinishedStatus(ureq);	
	}
	

	private void deleteResponse(String responseIdentifier) {
		/* Retrieve current JQTI state and set up JQTI controller */
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

		/* Make sure an attempt is allowed */
		if (itemSessionState.isEnded()) {
			candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.RESPONSES_NOT_EXPECTED, null);
			logError("RESPONSES_NOT_EXPECTED", null);
			return;
		}
		
		Identifier rIdentifier = qtiWorksCtrl.getResponseIdentifierFromUniqueId(responseIdentifier);
		itemSessionController.unbindResponse(rIdentifier);
		
        String assessmentItemIdentifier = resolvedAssessmentItem.getRootNodeLookup()
        		.extractIfSuccessful().getIdentifier();
		itemSession = qtiService
				.getOrCreateAssessmentItemSession(candidateSession, null, assessmentItemIdentifier);
        
        Map<Identifier, AssessmentResponse> candidateResponseMap = qtiService.getAssessmentResponses(itemSession);
        Map<Identifier, AssessmentResponse> removedCandidateResponseMap = new HashMap<>();
        AssessmentResponse assessmentResponse = candidateResponseMap.get(rIdentifier);
		if(assessmentResponse != null) {
			qtiService.deleteAssessmentResponse(assessmentResponse);
			removedCandidateResponseMap.put(rIdentifier, assessmentResponse);
		}
		
		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
				CandidateItemEventType.RESPONSE_REMOVED, itemSessionState, notificationRecorder);
		candidateAuditLogger.logCandidateEvent(candidateEvent, removedCandidateResponseMap);
		lastEvent = candidateEvent;
	}
	
	public void handleTemporaryResponses(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap,
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
		final Map<Identifier, File> fileSubmissionMap = new HashMap<>();
		final Map<Identifier, AssessmentResponse> assessmentResponseDataMap = new HashMap<>();
		mapStringResponseData(stringResponseMap, responseDataMap, fileSubmissionMap);
		mapFileResponseDate(fileResponseMap, responseDataMap, fileSubmissionMap);

		final Date timestamp = ureq.getRequestTimestamp();

        String assessmentItemIdentifier = resolvedAssessmentItem.getRootNodeLookup()
        		.extractIfSuccessful().getIdentifier();
		itemSession = qtiService
				.getOrCreateAssessmentItemSession(candidateSession, null, assessmentItemIdentifier);
        
		List<Interaction> interactions = itemSessionController.getInteractions();
		Map<Identifier,Interaction> interactionMap = new HashMap<>();
		for(Interaction interaction:interactions) {
			interactionMap.put(interaction.getResponseIdentifier(), interaction);
		}
		
        Map<Identifier, AssessmentResponse> candidateResponseMap = qtiService.getAssessmentResponses(itemSession);
        for (Entry<Identifier, ResponseData> responseEntry : responseDataMap.entrySet()) {
            Identifier responseIdentifier = responseEntry.getKey();
            ResponseData responseData = responseEntry.getValue();
            mapCandidateResponse(responseIdentifier, responseData, itemSession, candidateResponseMap, fileSubmissionMap);
            
            itemSessionState.setRawResponseData(responseIdentifier, responseData);
            
            try {
                Interaction interaction = interactionMap.get(responseIdentifier);
                interaction.bindResponse(itemSessionController, responseData);
            } catch (final ResponseBindingException e) {
               //
            }
        }
        
        /* Copy uncommitted responses over */
        for (final Entry<Identifier, Value> uncommittedResponseEntry : itemSessionState.getUncommittedResponseValues().entrySet()) {
            final Identifier identifier = uncommittedResponseEntry.getKey();
            final Value value = uncommittedResponseEntry.getValue();
            itemSessionState.setResponseValue(identifier, value);
        }

		/* Attempt to bind responses */
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
		
		if (candidateComment != null) {
			try {
				itemSessionController.setCandidateComment(timestamp, candidateComment);
			} catch (final QtiCandidateStateException e) {
				candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANDIDATE_COMMENT_FORBIDDEN, e);
				logError("CANDIDATE_COMMENT_FORBIDDEN", null);
				return;
			} catch (final RuntimeException e) {
				logError("", e);
				return;
			}
		}

		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
				CandidateItemEventType.RESPONSE_TEMPORARY, itemSessionState, notificationRecorder);
		candidateAuditLogger.logCandidateEvent(candidateEvent, assessmentResponseDataMap);
		lastEvent = candidateEvent;
		
		AssessmentResult assessmentResult = computeItemAssessmentResult(ureq);
		synchronized(this) {
			qtiService.recordItemAssessmentResult(candidateSession, assessmentResult, candidateAuditLogger);
		}
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
		final Map<Identifier,File> fileSubmissionMap = new HashMap<>();
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<>();
		mapStringResponseData(stringResponseMap, responseDataMap, fileSubmissionMap);
		mapFileResponseDate(fileResponseMap, responseDataMap, fileSubmissionMap);

        String assessmentItemIdentifier = resolvedAssessmentItem.getRootNodeLookup()
        		.extractIfSuccessful().getIdentifier();
		itemSession = qtiService
				.getOrCreateAssessmentItemSession(candidateSession, null, assessmentItemIdentifier);
        
        Map<Identifier, AssessmentResponse> candidateResponseMap = qtiService.getAssessmentResponses(itemSession);
        for (Entry<Identifier, ResponseData> responseEntry : responseDataMap.entrySet()) {
            Identifier responseIdentifier = responseEntry.getKey();
            ResponseData responseData = responseEntry.getValue();
            mapCandidateResponse(responseIdentifier, responseData, itemSession, candidateResponseMap, fileSubmissionMap);
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
				return;
			}
		}

		/* Attempt to bind responses */
		boolean allResponsesValid = false;
		boolean allResponsesBound = false;
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
			return;
		}

		/* Record resulting attempt and event */
		final CandidateItemEventType eventType = allResponsesBound ?
				(allResponsesValid ? CandidateItemEventType.ATTEMPT_VALID : CandidateItemEventType.RESPONSE_INVALID)
				: CandidateItemEventType.RESPONSE_BAD;
		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
	                eventType, itemSessionState, notificationRecorder);
		candidateAuditLogger.logCandidateEvent(candidateEvent, candidateResponseMap);
		lastEvent = candidateEvent;
		
		/* Record current result state, or finish session */
		AssessmentResult assessmentResult = updateSessionFinishedStatus(ureq);
		ItemResult itemResult = assessmentResult.getItemResult(assessmentItemIdentifier);
		collectOutcomeVariablesForItemSession(itemResult);
        /* Persist CandidateResponse entities */
        qtiService.recordTestAssessmentResponses(itemSession, candidateResponseMap.values());
	}
	
	private void mapCandidateResponse(Identifier responseIdentifier, ResponseData responseData, AssessmentItemSession itemSession,
			Map<Identifier, AssessmentResponse> candidateResponseMap, Map<Identifier,File> fileSubmissionMap) {
		
		AssessmentResponse candidateItemResponse;
        if(candidateResponseMap.containsKey(responseIdentifier)) {
        	candidateItemResponse = candidateResponseMap.get(responseIdentifier);
        } else {
        	candidateItemResponse = qtiService
        		.createAssessmentResponse(candidateSession, itemSession, responseIdentifier.toString(), ResponseLegality.VALID, responseData.getType());
        }
	
        switch (responseData.getType()) {
            case STRING: {
            	List<String> data = ((StringResponseData) responseData).getResponseData();
            	String stringuifiedResponse = ResponseFormater.format(data);
                candidateItemResponse.setStringuifiedResponse(stringuifiedResponse);
                break;
            }
            case FILE: {
            	if(fileSubmissionMap.get(responseIdentifier) != null) {
            		File storedFile = fileSubmissionMap.get(responseIdentifier);
            		candidateItemResponse.setStringuifiedResponse(storedFile.getName());
            	}
                break;
            }
            default:
                throw new OLATRuntimeException("Unexpected switch case: " + responseData.getType());
        }
        candidateResponseMap.put(responseIdentifier, candidateItemResponse);
	}
	
	private void mapStringResponseData(Map<Identifier, ResponseInput> stringResponseMap,
		Map<Identifier, ResponseData> responseDataMap, Map<Identifier,File> fileSubmissionMap) {
		if (stringResponseMap != null) {
			for (final Entry<Identifier, ResponseInput> stringResponseEntry : stringResponseMap.entrySet()) {
				Identifier identifier = stringResponseEntry.getKey();
				ResponseInput responseData = stringResponseEntry.getValue();
				if(responseData instanceof StringInput) {
					responseDataMap.put(identifier, new StringResponseData(((StringInput)responseData).getResponseData()));
				} else if(responseData instanceof Base64Input) {
					//only used from drawing interaction
					Base64Input fileInput = (Base64Input)responseData;
					String filename = "submitted_image.png";
					File storedFile = qtiService.importFileSubmission(candidateSession, filename, fileInput.getResponseData(), "json", fileInput.getResponseCompanionData());
                    responseDataMap.put(identifier, new FileResponseData(storedFile, fileInput.getContentType(), storedFile.getName()));
                    fileSubmissionMap.put(identifier, storedFile);
				} else if(responseData instanceof FileInput) {
					FileInput fileInput = (FileInput)responseData;
					File storedFile = qtiService.importFileSubmission(candidateSession, fileInput.getMultipartFileInfos());
                    responseDataMap.put(identifier, new FileResponseData(storedFile, fileInput.getContentType(), storedFile.getName()));
                    fileSubmissionMap.put(identifier, storedFile);
				}
			}
		}
	}
	
	private void mapFileResponseDate(Map<Identifier, ResponseInput> fileResponseMap,
			Map<Identifier, ResponseData> responseDataMap, Map<Identifier,File> fileSubmissionMap) {
		if (fileResponseMap!=null) {
			for (final Entry<Identifier, ResponseInput> fileResponseEntry : fileResponseMap.entrySet()) {
				final Identifier identifier = fileResponseEntry.getKey();
				final FileInput multipartFile = (FileInput)fileResponseEntry.getValue();
				if (!multipartFile.isEmpty()) {
					File storedFile = qtiService.importFileSubmission(candidateSession, multipartFile.getMultipartFileInfos());
					final FileResponseData fileResponseData = new FileResponseData(storedFile, multipartFile.getContentType(), storedFile.getName());
					responseDataMap.put(identifier, fileResponseData);
					fileSubmissionMap.put(identifier, storedFile);
				}
			}
		}
	}
	
	private void collectOutcomeVariablesForItemSession(ItemResult resultNode) {
		BigDecimal score = null;
		Boolean pass = null;

		for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
			if (itemVariable instanceof OutcomeVariable) {
				OutcomeVariable outcomeVariable = (OutcomeVariable) itemVariable;
				Identifier identifier = outcomeVariable.getIdentifier();
				if (QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
					Value value = itemVariable.getComputedValue();
					if (value instanceof FloatValue) {
						score = new BigDecimal(
								((FloatValue) value).doubleValue());
					} else if (value instanceof IntegerValue) {
						score = new BigDecimal(
								((IntegerValue) value).intValue());
					}
				} else if (QTI21Constants.PASS_IDENTIFIER.equals(identifier)) {
					Value value = itemVariable.getComputedValue();
					if (value instanceof BooleanValue) {
						pass = ((BooleanValue) value).booleanValue();
					}
				}
			}
		}

		if (score != null) {
			itemSession.setScore(score);
		}
		if (pass != null) {
			itemSession.setPassed(pass);
		}
	}
	
    protected AssessmentResult updateSessionFinishedStatus(UserRequest ureq) {
        /* Record current result state and maybe close session */
        final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);
        if (itemSessionState.isEnded()) {
            finishCandidateSession(assessmentResult, ureq.getRequestTimestamp());
        }
        else {
            if (candidateSession != null && candidateSession.getFinishTime() != null) {
                /* (Session is being reopened) */
                candidateSession.setFinishTime(null);
                candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
            }
        }
        return assessmentResult;
    }
    
    public AssessmentResult computeAndRecordItemAssessmentResult(UserRequest ureq) {
        final AssessmentResult assessmentResult = computeItemAssessmentResult(ureq);
        qtiService.recordItemAssessmentResult(candidateSession, assessmentResult, candidateAuditLogger);
        
        ItemResult itemResult = assessmentResult.getItemResult(resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful().getIdentifier());
		processOutcomeVariables(itemResult);
        
        return assessmentResult;
    }
    
	private void processOutcomeVariables(ItemResult itemResult) {
		Float score = null;
		Boolean pass = null;

		for (final ItemVariable itemVariable : itemResult.getItemVariables()) {
			if (itemVariable instanceof OutcomeVariable) {
				OutcomeVariable outcomeVariable = (OutcomeVariable) itemVariable;
				Identifier identifier = outcomeVariable.getIdentifier();
				if (QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
					Value value = itemVariable.getComputedValue();
					if (value instanceof NumberValue) {
						score = (float) ((NumberValue) value).doubleValue();
					}
				} else if (QTI21Constants.PASS_IDENTIFIER.equals(identifier)) {
					Value value = itemVariable.getComputedValue();
					if (value instanceof BooleanValue) {
						pass = ((BooleanValue) value).booleanValue();
					}
				}
			}
		}

		outcomesListener.outcomes(candidateSession, score, pass);
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
                return;
            }
        }

        /* Record current result state, and maybe close session */
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);
        if (isClosingSession) {
            finishCandidateSession(assessmentResult, timestamp);
        }

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
        		CandidateItemEventType.SOLUTION, itemSessionState);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;
    }
    
	private void next(UserRequest ureq, QTIWorksAssessmentItemEvent event) {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
		
		/* Check this is allowed in current state */
        if (itemSessionState.isEnded()) {
    		fireEvent(ureq, event);
        	return;
        }
        
        /* Update state */
        final Date timestamp = ureq.getRequestTimestamp();
        try {
            itemSessionController.endItem(timestamp);
        } catch (QtiCandidateStateException e) {
        	String msg = itemSessionState.isEnded() ? "END_SESSION_WHEN_ALREADY_ENDED" : "END_SESSION_WHEN_INTERACTING_FORBIDDEN";
        	logError(msg, e);
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, itemSessionState.isEnded() ? CandidateExceptionReason.END_SESSION_WHEN_ALREADY_ENDED : CandidateExceptionReason.END_SESSION_WHEN_INTERACTING_FORBIDDEN, null);
            return;
        } catch (final RuntimeException e) {
        	logError("", e);
            return;
        }
        
        /* Record current result state */
        computeAndRecordItemAssessmentResult(ureq);
        
        CandidateItemEventType eventType = CandidateItemEventType.NEXT;
        if(event.getEvent() == QTIWorksAssessmentItemEvent.Event.timesUp) {
        	eventType = CandidateItemEventType.EXIT_DUE_TIME_LIMIT;
        }

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
        		eventType, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;

		fireEvent(ureq, event);
	}
    
	public void endSession(UserRequest ureq) {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();

        /* Check this is allowed in current state */
        if (itemSessionState.isEnded()) {
        	logError("END_SESSION_WHEN_ALREADY_ENDED", null);
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.END_SESSION_WHEN_ALREADY_ENDED, null);
            return;
        }

        /* Update state */
        final Date timestamp = ureq.getRequestTimestamp();
        try {
            itemSessionController.endItem(timestamp);
        } catch (QtiCandidateStateException e) {
        	String msg = itemSessionState.isEnded() ? "END_SESSION_WHEN_ALREADY_ENDED" : "END_SESSION_WHEN_INTERACTING_FORBIDDEN";
        	logError(msg, e);
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, itemSessionState.isEnded() ? CandidateExceptionReason.END_SESSION_WHEN_ALREADY_ENDED : CandidateExceptionReason.END_SESSION_WHEN_INTERACTING_FORBIDDEN, null);
            return;
        } catch (final RuntimeException e) {
        	logError("", e);
            return;
        }

        /* Record current result state */
        final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);

        /* Record and log event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
                CandidateItemEventType.END, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;

        /* Close session */
        finishCandidateSession(assessmentResult, timestamp);
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
		        return;
		    }
		    final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(ureq);
		    finishCandidateSession(assessmentResult, currentTimestamp);
		}
		
		/* Update session entity */
		candidateSession.setTerminationTime(currentTimestamp);
		candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
		
		/* Record and log event */
		final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, itemSession, testEntry, entry,
					CandidateItemEventType.EXIT, itemSessionState);
		lastEvent = candidateEvent;
		candidateAuditLogger.logCandidateEvent(candidateEvent);
	}
	
	protected void finishCandidateSession(AssessmentResult assessmentResult, final Date currentTimestamp) {
		candidateSession = qtiService.finishItemSession(candidateSession, assessmentResult, currentTimestamp);
	}
	
	/**
	 * QtiWorks manage the form tag itself.
	 * 
	 * Initial date: 20.05.2015<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	protected class QtiWorksController extends AbstractQtiWorksController {
		
		private AssessmentItemFormItem qtiEl;
		private AssessmentCountDownFormItem timerEl;
		private final String filename;
		
		public QtiWorksController(UserRequest ureq, WindowControl wControl, String filename) {
			super(ureq, wControl, "ff_run");
			this.filename = filename;
			initForm(ureq);
		}
		
		public void setTimeLimit(long seconds) {
			if(seconds > 0) {
				timerEl.setTimerInSeconds(seconds);
				timerEl.setEnabled(true);
			} else {
				timerEl.setEnabled(false);
			}
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			mainForm.setMultipartEnabled(true);

			FormSubmit submit = uifactory.addFormSubmitButton("submit", formLayout);
			qtiEl = new AssessmentItemFormItem("qtirun", submit);
			qtiEl.setEnableBack(deliveryOptions.isEnableAssessmentItemBack());
			qtiEl.setEnableResetHard(deliveryOptions.isEnableAssessmentItemResetHard());
			qtiEl.setEnableResetSoft(deliveryOptions.isEnableAssessmentItemResetSoft());
			qtiEl.setEnableSkip(deliveryOptions.isEnableAssessmentItemSkip());
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
			
			timerEl = new AssessmentCountDownFormItem("timer", qtiEl);
			timerEl.setEnabled(false);
			formLayout.add("timer", timerEl);
			
			String[] jss = new String[] {
					"js/jquery/qti/jquery.qtiCountDown.js"
			};
			JSAndCSSFormItem js = new JSAndCSSFormItem("js", jss);
			formLayout.add("js", js);
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
						processPartialTemporaryResponse(ureq);
					} else if(qwaie.getEvent() == QTIWorksAssessmentItemEvent.Event.fullTmpResponse) {
						processFullTemporaryResponse(ureq);
					} else if(qwaie.getEvent() == QTIWorksAssessmentItemEvent.Event.timesUp) {
						timerEl.setAlreadyEnded(true);
						fireEvent(ureq, event);
					} else if(qwaie.getEvent() == QTIWorksAssessmentItemEvent.Event.resethard
							|| qwaie.getEvent() == QTIWorksAssessmentItemEvent.Event.resetsoft) {
						timerEl.setAlreadyEnded(false);
						fireEvent(ureq, event);
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
		protected void firePartialTemporaryResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap) {
			fireEvent(ureq, new QTIWorksAssessmentItemEvent(QTIWorksAssessmentItemEvent.Event.tmpResponse,
					stringResponseMap, null, null, null));
		}

		@Override
		protected void fireFullTemporaryResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap,
				Map<Identifier, ResponseInput> fileResponseMap, String comment) {
			fireEvent(ureq, new QTIWorksAssessmentItemEvent(QTIWorksAssessmentItemEvent.Event.fullTmpResponse,
					stringResponseMap, fileResponseMap, comment, null));
		}

		@Override
		protected void fireResponse(UserRequest ureq, FormItem source,
				Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier, ResponseInput> fileResponseMap,
				String comment) {
			fireEvent(ureq, new QTIWorksAssessmentItemEvent(QTIWorksAssessmentItemEvent.Event.response, stringResponseMap, fileResponseMap, comment, source));
		}
	}
}
