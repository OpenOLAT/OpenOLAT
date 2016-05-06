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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.resource.OresHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestMarks;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesListener;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.ResponseFormater;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.jpa.CandidateEvent;
import org.olat.ims.qti21.ui.components.AssessmentTestFormItem;
import org.olat.ims.qti21.ui.components.AssessmentTreeFormItem;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.exception.QtiCandidateStateException;
import uk.ac.ed.ph.jqtiplus.node.result.AbstractResult;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingContext;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestPlanVisitor;
import uk.ac.ed.ph.jqtiplus.running.TestPlanner;
import uk.ac.ed.ph.jqtiplus.running.TestProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPartSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
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
 * Initial date: 08.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestDisplayController extends BasicController implements CandidateSessionContext {
	
	private final File fUnzippedDirRoot;
	private final String mapperUri;
	private final QTI21DeliveryOptions deliveryOptions;
	
	private VelocityContainer mainVC;
	private final StackedPanel mainPanel;
	private QtiWorksController qtiWorksCtrl;
	private AssessmentResultController resultCtrl;
	private TestSessionController testSessionController;

	private DialogBoxController advanceTestPartDialog;
	private DialogBoxController confirmCancelDialog;
	private DialogBoxController confirmSuspendDialog;
	
	private CandidateEvent lastEvent;
	private Date currentRequestTimestamp;
	private AssessmentTestSession candidateSession;
	private AssessmentEntry assessmentEntry;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private AssessmentTestMarks marks;
	
	private RepositoryEntry testEntry;
	private RepositoryEntry entry;
	private String subIdent;
	
	private OutcomesListener outcomesListener;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param listener
	 * @param testEntry
	 * @param entry
	 * @param subIdent
	 * @param deliveryOptions
	 * @param authorMode if true, the database objects are not counted and can be deleted without warning
	 */
	public AssessmentTestDisplayController(UserRequest ureq, WindowControl wControl, OutcomesListener listener,
			RepositoryEntry testEntry, RepositoryEntry entry, String subIdent, QTI21DeliveryOptions deliveryOptions,
			boolean authorMode) {
		super(ureq, wControl);

		this.entry = entry;
		this.subIdent = subIdent;
		this.testEntry = testEntry;
		this.outcomesListener = listener;
		this.deliveryOptions = deliveryOptions;
		
		FileResourceManager frm = FileResourceManager.getInstance();
		fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false);
		
		URI assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		mapperUri = registerCacheableMapper(null, "QTI21Resources::" + testEntry.getKey(), new ResourcesMapper(assessmentObjectUri));
		
		currentRequestTimestamp = ureq.getRequestTimestamp();
		
		assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), entry, subIdent, testEntry);
		marks = qtiService.getMarks(getIdentity(), entry, subIdent, testEntry);

		AssessmentTestSession lastSession = null;
		if(deliveryOptions.isEnableSuspend()) {
			lastSession = qtiService.getResumableAssessmentTestSession(getIdentity(), entry, subIdent, testEntry);
		}
		if(lastSession == null) {
			candidateSession = qtiService.createAssessmentTestSession(getIdentity(), assessmentEntry, entry, subIdent, testEntry, authorMode);
			testSessionController = enterSession(ureq);
		} else {
			candidateSession = lastSession;
			lastEvent = new CandidateEvent();
			lastEvent.setCandidateSession(candidateSession);
			lastEvent.setTestEventType(CandidateTestEventType.ITEM_EVENT);
			
			testSessionController = resumeSession();
		}

		/* Handle immediate end of test session */
        if (testSessionController.getTestSessionState() != null && testSessionController.getTestSessionState().isEnded()) {
        	AssessmentResult assessmentResult = null;
            qtiService.finishTestSession(candidateSession, testSessionController.getTestSessionState(), assessmentResult, ureq.getRequestTimestamp());
        	mainVC = createVelocityContainer("end");
        } else {
        	mainVC = createVelocityContainer("run");
        	initQtiWorks(ureq);
        }
        
        mainPanel = putInitialPanel(mainVC);
	}
	
	private void initQtiWorks(UserRequest ureq) {
		qtiWorksCtrl = new QtiWorksController(ureq, getWindowControl());
    	listenTo(qtiWorksCtrl);
    	mainVC.put("qtirun", qtiWorksCtrl.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		suspendAssessmentTest();
	}

	@Override
	public boolean isTerminated() {
		return candidateSession.getTerminationTime() != null;
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
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(advanceTestPartDialog == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				processAdvanceTestPart(ureq);
			}
			mainVC.setDirty(true);
		} else if(confirmCancelDialog == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doCancel(ureq);
			}
		}  else if(confirmSuspendDialog == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doSuspend(ureq);
			}
		} else if(qtiWorksCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doConfirmCancel(ureq);
			} else if("suspend".equals(event.getCommand())) {
				doConfirmSuspend(ureq);
			} else if(event instanceof QTIWorksAssessmentTestEvent) {
				processQTIEvent(ureq, (QTIWorksAssessmentTestEvent)event);
			}
		}
		super.event(ureq, source, event);
	}

	private void doExitTest(UserRequest ureq) {
		fireEvent(ureq, new QTI21Event(QTI21Event.EXIT));
	}
	
	private void doConfirmSuspend(UserRequest ureq) {
		String title = translate("suspend.test");
		String text = translate("confirm.suspend.test");
		confirmSuspendDialog = activateOkCancelDialog(ureq, title, text, confirmSuspendDialog);
	}
	
	private void doSuspend(UserRequest ureq) {
		VelocityContainer suspendedVC = createVelocityContainer("suspended");
		mainPanel.setContent(suspendedVC);
		suspendAssessmentTest();
		fireEvent(ureq, new Event("suspend"));
	}

	/**
	 * It suspend the current item
	 * @return
	 */
	private boolean suspendAssessmentTest() {
		if(!deliveryOptions.isEnableSuspend() || testSessionController == null
				|| testSessionController.getTestSessionState() == null
				|| testSessionController.getTestSessionState().isEnded()
				|| testSessionController.getTestSessionState().isExited()) {
			return false;
		}
		
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		if(currentItemKey == null) {
			return false;
		}
		TestPlanNode currentItemNode = testSessionState.getTestPlan().getNode(currentItemKey);

		ItemProcessingContext itemProcessingContext = testSessionController.getItemProcessingContext(currentItemNode);
		ItemSessionState itemSessionState = itemProcessingContext.getItemSessionState();
		if(itemProcessingContext instanceof ItemSessionController
				&& !itemSessionState.isEnded()
				&& !itemSessionState.isExited()
				&& itemSessionState.isOpen()
				&& !itemSessionState.isSuspended()) {
			ItemSessionController itemSessionController = (ItemSessionController)itemProcessingContext;
			itemSessionController.suspendItemSession(new Date());
			
			NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
			final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession,
	                CandidateTestEventType.SUSPEND, null, testSessionState, notificationRecorder);
	        //candidateAuditLogger.logCandidateEvent(candidateEvent);
	        this.lastEvent = candidateEvent;
			return true;
		}
		return false;
	}
	
	private void doConfirmCancel(UserRequest ureq) {
		String title = translate("cancel.test");
		String text = translate("confirm.cancel.test");
		confirmCancelDialog = activateOkCancelDialog(ureq, title, text, confirmCancelDialog);
	}
	
	private void doCancel(UserRequest ureq) {
		VelocityContainer cancelledVC = createVelocityContainer("cancelled");
		mainPanel.setContent(cancelledVC);
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		qtiService.cancelTestSession(candidateSession, testSessionState);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void processQTIEvent(UserRequest ureq, QTIWorksAssessmentTestEvent qe) {
		currentRequestTimestamp = ureq.getRequestTimestamp();
		
		switch(qe.getEvent()) {
			case selectItem:
				processSelectItem(ureq, qe.getSubCommand());
				break;
			case nextItem:
				processNextItem(ureq);
				break;
			case finishItem:
				processFinishLinearItem(ureq);
				break;
			case reviewItem:
				processReviewItem(ureq, qe.getSubCommand());
				break;
			case itemSolution:
				processItemSolution(ureq, qe.getSubCommand());
				break;
			case testPartNavigation:
				processTestPartNavigation(ureq);
				break;
			case response:
				handleResponse(ureq, qe.getStringResponseMap(), qe.getFileResponseMap(), qe.getComment());
				break;
			case endTestPart:
				processEndTestPart(ureq);
				break;
			case advanceTestPart:
				confirmAdvanceTestPart(ureq);
				break;
			case reviewTestPart:
				processReviewTestPart();
				break;
			case exitTest:
				processExitTest(ureq);
				break;
			case source:
				logError("QtiWorks event source not implemented", null);
				break;
			case state:
				logError("QtiWorks event state not implemented", null);
				break;
			case result:
				logError("QtiWorks event result not implemented", null);
				break;
			case validation:
				logError("QtiWorks event validation not implemented", null);
				break;
			case authorview:
				logError("QtiWorks event authorview not implemented", null);
				break;
			case mark:
				toogleMark(qe.getSubCommand());
				break;
		}
	}
	
	private void toogleMark(String itemRef) {
		if(marks == null) {
			marks = qtiService.createMarks(getIdentity(), entry, subIdent, testEntry, itemRef);
		} else {
			String currentMarks = marks.getMarks();
			if(currentMarks == null) {
				marks.setMarks(itemRef);
			} else if(currentMarks.indexOf(itemRef) >= 0) {
				marks.setMarks(currentMarks.replace(itemRef, ""));
			} else {
				marks.setMarks(currentMarks + " " + itemRef);
			}
			marks = qtiService.updateMarks(marks);
		}
	}
	
	@Override
	public boolean isMarked(String itemKey) {
		if(marks == null || marks.getMarks() == null) return false;
		return marks.getMarks().indexOf(itemKey) >= 0;
	}

	private void processSelectItem(UserRequest ureq, String key) {
		TestPlanNodeKey nodeKey = TestPlanNodeKey.fromString(key);
		Date requestTimestamp = ureq.getRequestTimestamp();
        testSessionController.selectItemNonlinear(requestTimestamp, nodeKey);
	}
	
	private void processNextItem(UserRequest ureq) {
		Date requestTimestamp = ureq.getRequestTimestamp();
        if(testSessionController.hasFollowingNonLinearItem()) {
        	testSessionController.selectFollowingItemNonLinear(requestTimestamp);
        }
	}
	
	private void processReviewItem(UserRequest ureq, String key) {
		TestPlanNodeKey itemKey = TestPlanNodeKey.fromString(key);
        //Assert.notNull(itemKey, "itemKey");

		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		TestSessionState testSessionState = testSessionController.getTestSessionState();

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
        computeAndRecordTestAssessmentResult(ureq, testSessionState, false);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.REVIEW_ITEM, null, itemKey, testSessionState, notificationRecorder);
        this.lastEvent = candidateTestEvent;
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
	}

	private void processItemSolution(UserRequest ureq, String key) {
		TestPlanNodeKey itemKey = TestPlanNodeKey.fromString(key);

        NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
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
        computeAndRecordTestAssessmentResult(ureq, testSessionState, false);

        /* Record and log event */
        CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.SOLUTION_ITEM, null, itemKey, testSessionState, notificationRecorder);
        this.lastEvent = candidateTestEvent;
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent); 
	}
	
	//public CandidateSession finishLinearItem(final CandidateSessionContext candidateSessionContext)
    // throws CandidateException {
	private void processFinishLinearItem(UserRequest ureq) {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();
		
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
		final Date requestTimestamp = ureq.getRequestTimestamp();
	    final TestPlanNode nextItemNode = testSessionController.advanceItemLinear(requestTimestamp);
	    
	    //boolean terminated = nextItemNode == null && testSessionController.findNextEnterableTestPart() == null; 

	    // Record current result state
	    final AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(ureq, testSessionState, false);

	    /* If we ended the testPart and there are now no more available testParts, then finish the session now */
	    if (nextItemNode==null && testSessionController.findNextEnterableTestPart()==null) {
	    	candidateSession = qtiService.finishTestSession(candidateSession, testSessionState, assessmentResult, requestTimestamp);
	    }


	    // Record and log event 
	    final CandidateTestEventType eventType = nextItemNode!=null ? CandidateTestEventType.FINISH_ITEM : CandidateTestEventType.FINISH_FINAL_ITEM;
	   	final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession,
	                eventType, null, testSessionState, notificationRecorder);
	   	this.lastEvent = candidateTestEvent;
	}
	
	private void processTestPartNavigation(UserRequest ureq) {
		final Date requestTimestamp = ureq.getRequestTimestamp();
        TestPlanNode nextNode = testSessionController.selectItemNonlinear(requestTimestamp, null);
        if(nextNode == null) {
        	System.out.println();
        }
	}
	
	//public CandidateSession handleResponses(final CandidateSessionContext candidateSessionContext,
    //        final Map<Identifier, StringResponseData> stringResponseMap,
    //        final Map<Identifier, MultipartFile> fileResponseMap,
    //        final String candidateComment)       
	private void handleResponse(UserRequest ureq, Map<Identifier, StringResponseData> stringResponseMap,
			Map<Identifier,MultipartFileInfos> fileResponseMap, String candidateComment) {

		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<Identifier, ResponseData>();
		if (stringResponseMap != null) {
			for (final Entry<Identifier, StringResponseData> stringResponseEntry : stringResponseMap.entrySet()) {
				final Identifier identifier = stringResponseEntry.getKey();
				final StringResponseData stringResponseData = stringResponseEntry.getValue();
				responseDataMap.put(identifier, stringResponseData);
            }
		}
        
		TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		String assessmentItemIdentifier = currentItemKey.getIdentifier().toString();
		AssessmentItemSession itemSession = qtiService.getOrCreateAssessmentItemSession(candidateSession, assessmentItemIdentifier);
		Map<Identifier,File> fileSubmissionMap = new HashMap<>();
        if (fileResponseMap!=null) {
            for (Entry<Identifier, MultipartFileInfos> fileResponseEntry : fileResponseMap.entrySet()) {
                Identifier identifier = fileResponseEntry.getKey();
                MultipartFileInfos multipartFile = fileResponseEntry.getValue();
                if (!multipartFile.isEmpty()) {
                	File storedFile = qtiService.importFileSubmission(candidateSession, multipartFile);
                    responseDataMap.put(identifier, new FileResponseData(storedFile, multipartFile.getContentType(), multipartFile.getFileName()));
                    fileSubmissionMap.put(identifier, storedFile);
                }
            }
        }
        
        Map<Identifier, AssessmentResponse> candidateResponseMap = qtiService.getAssessmentResponses(itemSession);
        for (Entry<Identifier, ResponseData> responseEntry : responseDataMap.entrySet()) {
            Identifier responseIdentifier = responseEntry.getKey();
            ResponseData responseData = responseEntry.getValue();
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
        
        boolean allResponsesValid = true;
        boolean allResponsesBound = true;
		final Date timestamp = ureq.getRequestTimestamp();
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
        final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.ITEM_EVENT, candidateItemEventType, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateEvent);
        this.lastEvent = candidateEvent;

        /* Record current result state */
        AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(ureq, testSessionState, false);
        
        ItemSessionState itemSessionState = testSessionState.getCurrentItemSessionState();
		long itemDuration = itemSessionState.getDurationAccumulated();
		itemSession.setDuration(itemDuration);
		ItemResult itemResult = assessmentResult.getItemResult(assessmentItemIdentifier);
		processOutcomeVariables_bricolage(itemResult, itemSession);
        /* Persist CandidateResponse entities */
        qtiService.recordTestAssessmentResponses(itemSession, candidateResponseMap.values());

        /* Save any change to session state */
        candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
	}
	
	private void processOutcomeVariables_bricolage(ItemResult resultNode, AssessmentItemSession itemSession) {
		BigDecimal score = null;
		Boolean pass = null;
		
        for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
            if (itemVariable instanceof OutcomeVariable) {
            	OutcomeVariable outcomeVariable = (OutcomeVariable)itemVariable;
            	Identifier identifier = outcomeVariable.getIdentifier();
            	if(QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
            		Value value = itemVariable.getComputedValue();
            		if(value instanceof FloatValue) {
            			score = new BigDecimal(((FloatValue)value).doubleValue());
            		} else if(value instanceof IntegerValue) {
            			score = new BigDecimal(((IntegerValue)value).intValue());
            		}
            	} else if(QTI21Constants.PASS_IDENTIFIER.equals(identifier)) {
            		Value value = itemVariable.getComputedValue();
            		if(value instanceof BooleanValue) {
            			pass = ((BooleanValue)value).booleanValue();
            		}
            	}
            }
        }
        
        if(score != null) {
        	itemSession.setScore(score);
        }
        if(pass != null) {
        	itemSession.setPassed(pass);
        }
    }

	//public CandidateSession endCurrentTestPart(final CandidateSessionContext candidateSessionContext)
	private void processEndTestPart(UserRequest ureq) {
		 /* Update state */
        final Date requestTimestamp = ureq.getRequestTimestamp();
        testSessionController.endCurrentTestPart(requestTimestamp);
	}
	
	private void confirmAdvanceTestPart(UserRequest ureq) {
		String title = translate("confirm.advance.testpart.title");
		String text = translate("confirm.advance.testpart.text");
		advanceTestPartDialog = activateOkCancelDialog(ureq, title, text, advanceTestPartDialog);
	}
	
	private void processAdvanceTestPart(UserRequest ureq) {
        /* Get current JQTI state and create JQTI controller */
        NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Perform action */
        final TestPlanNode nextTestPart;
        final Date currentTimestamp = ureq.getRequestTimestamp();
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
                candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
            }
            else {
                eventType = CandidateTestEventType.ADVANCE_TEST_PART;
            }
        }
        
        boolean terminated = isTerminated();

        /* Record current result state */
        computeAndRecordTestAssessmentResult(ureq, testSessionState, terminated);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession,
               eventType, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;

        if (terminated) {
        	qtiWorksCtrl.updateGUI(ureq);
        	doExitTest(ureq);
        }
	}
	
	private void processReviewTestPart() {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Make sure caller may do this */
        //assertSessionNotTerminated(candidateSession);
        if (testSessionState.getCurrentTestPartKey()==null || !testSessionState.getCurrentTestPartSessionState().isEnded()) {
            // candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_PART);
            logError("CANNOT_REVIEW_TEST_PART", null);
        	return;
        }

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.REVIEW_TEST_PART, null, null, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;
	}
	
	/**
	 * Exit multi-part tests
	 */
	private void processExitTest(UserRequest ureq) {
        NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Perform action */
        final Date currentTimestamp = ureq.getRequestTimestamp();
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
        candidateSession = qtiService.updateAssessmentTestSession(candidateSession);

        /* Record current result state (final) */
        computeAndRecordTestAssessmentResult(ureq, testSessionState, true);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.EXIT_TEST, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;
        
        doExitTest(ureq);
	}
	
	//private CandidateSession enterCandidateSession(final CandidateSession candidateSession)
	private TestSessionController enterSession(UserRequest ureq) {
		/* Set up listener to record any notifications */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);

        /* Create fresh JQTI+ state & controller for it */
        testSessionController = createNewTestSessionStateAndController(notificationRecorder);
        if (testSessionController == null) {
            return null;
        }
        
        /* Initialise test state and enter test */
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        final Date timestamp = ureq.getRequestTimestamp();
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
        final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession,
                CandidateTestEventType.ENTER_TEST, testSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateEvent);
        this.lastEvent = candidateEvent;
        
        boolean ended = testSessionState.isEnded();

        /* Record current result state */
        final AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(ureq, testSessionState, ended);

        /* Handle immediate end of test session */
        if (ended) {
            qtiService.finishTestSession(candidateSession, testSessionState, assessmentResult, timestamp);
        } else {
        	TestPart currentTestPart = testSessionController.getCurrentTestPart();
        	if(currentTestPart.getNavigationMode() == NavigationMode.NONLINEAR) {
        		//go to the first assessment item
        		if(testSessionController.hasFollowingNonLinearItem()) {
        			testSessionController.selectFollowingItemNonLinear(currentRequestTimestamp);
        			//
        		}
        	}
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
        
        testProcessingMap.reduceItemProcessingMapMap(testPlan.getTestPlanNodeList());

        /* Create controller and wire up notification recorder */
        final TestSessionController result = new TestSessionController(qtiService.jqtiExtensionManager(),
                testSessionControllerSettings, testProcessingMap, testSessionState);
        if (notificationRecorder!=null) {
            result.addNotificationListener(notificationRecorder);
        }
		return result;
	}
	
	private TestSessionController resumeSession() {
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionController controller =  createTestSessionController(notificationRecorder);
       
        TestSessionState testSessionState = controller.getTestSessionState();
		TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		if(currentItemKey != null) {
			TestPlanNode currentItemNode = testSessionState.getTestPlan().getNode(currentItemKey);
			ItemProcessingContext itemProcessingContext = controller.getItemProcessingContext(currentItemNode);
			ItemSessionState itemSessionState = itemProcessingContext.getItemSessionState();
			if(itemProcessingContext instanceof ItemSessionController
					&& itemSessionState.isSuspended()) {
				ItemSessionController itemSessionController = (ItemSessionController)itemProcessingContext;
				itemSessionController.unsuspendItemSession(new Date());
			}
		}
		
        return controller;
	}
	
	private TestSessionController createTestSessionController(NotificationRecorder notificationRecorder) {
        final TestSessionState testSessionState = qtiService.loadTestSessionState(candidateSession);
        return createTestSessionController(testSessionState, notificationRecorder);
    }
	
    public TestSessionController createTestSessionController(TestSessionState testSessionState,  NotificationRecorder notificationRecorder) {
        /* Try to resolve the underlying JQTI+ object */
        final TestProcessingMap testProcessingMap = getTestProcessingMap();
        if (testProcessingMap == null) {
            return null;
        }

        /* Create config for TestSessionController */
        final TestSessionControllerSettings testSessionControllerSettings = new TestSessionControllerSettings();
        testSessionControllerSettings.setTemplateProcessingLimit(computeTemplateProcessingLimit());

        /* Create controller and wire up notification recorder (if passed) */
        final TestSessionController result = new TestSessionController(qtiService.jqtiExtensionManager(),
                testSessionControllerSettings, testProcessingMap, testSessionState);
        if (notificationRecorder!=null) {
            result.addNotificationListener(notificationRecorder);
        }

        return result;
    }
	
	private AssessmentResult computeAndRecordTestAssessmentResult(UserRequest ureq, TestSessionState testSessionState, boolean submit) {
		AssessmentResult assessmentResult = computeTestAssessmentResult(ureq, candidateSession);
		qtiService.recordTestAssessmentResult(candidateSession, testSessionState, assessmentResult);
		processOutcomeVariables(assessmentResult.getTestResult(), submit);
		return assessmentResult;
	}
	
	private void processOutcomeVariables(AbstractResult resultNode, boolean submit) {
		Float score = null;
		Boolean pass = null;
		
        for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
            if (itemVariable instanceof OutcomeVariable) {
            	OutcomeVariable outcomeVariable = (OutcomeVariable)itemVariable;
            	Identifier identifier = outcomeVariable.getIdentifier();
            	if(QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
            		Value value = itemVariable.getComputedValue();
            		if(value instanceof NumberValue) {
            			score = (float) ((NumberValue)value).doubleValue();
            		}
            	} else if(QTI21Constants.PASS_IDENTIFIER.equals(identifier)) {
            		Value value = itemVariable.getComputedValue();
            		if(value instanceof BooleanValue) {
            			pass = ((BooleanValue)value).booleanValue();
            		}
            	}
            }
        }
        
        if(score != null || pass != null) {
        	if(submit) {
        		outcomesListener.submit(score, pass, candidateSession.getKey());
        	} else {
        		outcomesListener.updateOutcomes(score, pass);
        	}
        }
    }
	
    private AssessmentResult computeTestAssessmentResult(UserRequest ureq, final AssessmentTestSession testSession) {
    	List<ContextEntry> entries = getWindowControl().getBusinessControl().getEntries();
    	OLATResourceable testSessionOres = OresHelper.createOLATResourceableInstance("TestSession", testSession.getKey());
    	entries.add(BusinessControlFactory.getInstance().createContextEntry(testSessionOres));
    	String url = BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
        final URI sessionIdentifierSourceId = URI.create(url);
        final String sessionIdentifier = "testsession/" + testSession.getKey();
        return testSessionController
        		.computeAssessmentResult(ureq.getRequestTimestamp(), sessionIdentifier, sessionIdentifierSourceId);
    }
	
	private TestProcessingMap getTestProcessingMap() {
		boolean assessmentPackageIsValid = true;

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
	
	/**
	 * QtiWorks manage the form tag itself.
	 * 
	 * Initial date: 20.05.2015<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private class QtiWorksController extends AbstractQtiWorksController {

		private AssessmentTestFormItem qtiEl;
		private AssessmentTreeFormItem qtiTreeEl;
		private ProgressBarItem scoreProgress, questionProgress;
		private FormLink endTestPartButton, closeTestButton, cancelTestButton, suspendTestButton;
		
		private final QtiWorksStatus qtiWorksStatus = new QtiWorksStatus();
		
		public QtiWorksController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "at_run");
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			mainForm.setMultipartEnabled(true);
			mainForm.setOnSubmitCallback("QtiWorksRendering.maySubmit();");

			FormSubmit submit = uifactory.addFormSubmitButton("submit", formLayout);
			qtiEl = new AssessmentTestFormItem("qtirun", submit);
			qtiEl.setResolvedAssessmentTest(resolvedAssessmentTest);
			formLayout.add("qtirun", qtiEl);
			
			qtiTreeEl = new AssessmentTreeFormItem("qtitree", qtiEl.getComponent(), submit);
			qtiTreeEl.setResolvedAssessmentTest(resolvedAssessmentTest);
			formLayout.add("qtitree", qtiTreeEl);
			
			String endName = qtiEl.getComponent().hasMultipleTestParts()
					? "assessment.test.end.testPart" : "assessment.test.end.test";
			endTestPartButton = uifactory.addFormLink("endTest", endName, null, formLayout, Link.BUTTON);
			closeTestButton = uifactory.addFormLink("closeTest", "assessment.test.close.test", null, formLayout, Link.BUTTON);
			if(deliveryOptions.isEnableCancel()) {
				cancelTestButton = uifactory.addFormLink("cancelTest", "cancel.test", null, formLayout, Link.BUTTON);
			}
			if(deliveryOptions.isEnableSuspend()) {
				suspendTestButton = uifactory.addFormLink("suspendTest", "suspend.test", null, formLayout, Link.BUTTON);
			}

			ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
			final ResourceLocator inputResourceLocator = 
	        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
			qtiEl.setResourceLocator(inputResourceLocator);
			qtiEl.setTestSessionController(testSessionController);
			qtiEl.setAssessmentObjectUri(qtiService.createAssessmentObjectUri(fUnzippedDirRoot));
			qtiEl.setCandidateSessionContext(AssessmentTestDisplayController.this);
			qtiEl.setMapperUri(mapperUri);
			qtiEl.setRenderNavigation(false);
			qtiEl.setPersonalNotes(deliveryOptions.isPersonalNotes());
			qtiEl.setShowTitles(deliveryOptions.isShowTitles());
			
			qtiTreeEl.setResourceLocator(inputResourceLocator);
			qtiTreeEl.setTestSessionController(testSessionController);
			qtiTreeEl.setAssessmentObjectUri(qtiService.createAssessmentObjectUri(fUnzippedDirRoot));
			qtiTreeEl.setCandidateSessionContext(AssessmentTestDisplayController.this);
			qtiTreeEl.setMapperUri(mapperUri);
			
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractAssumingSuccessful();
				layoutCont.contextPut("title", assessmentTest.getTitle());
				layoutCont.contextPut("qtiWorksStatus", qtiWorksStatus);
				
				JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/jquery/ui/jquery-ui-1.11.4.custom.resize.min.js" }, null);
				layoutCont.put("js", js);
				
				layoutCont.contextPut("displayScoreProgress", deliveryOptions.isDisplayScoreProgress());
				layoutCont.contextPut("displayQuestionProgress", deliveryOptions.isDisplayQuestionProgress());
				
				if(deliveryOptions.isDisplayScoreProgress()) {
					scoreProgress = uifactory.addProgressBar("scoreProgress", null, 250, 0, 0, "", formLayout);
					formLayout.add("", scoreProgress);
				}
				
				if(deliveryOptions.isDisplayQuestionProgress()) {
					questionProgress = uifactory.addProgressBar("questionProgress", null, 250, 0, 0, "", formLayout);
					formLayout.add("questionProgress", questionProgress);
				}
			}

			updateGUI(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			processResponse(ureq, qtiEl.getSubmitButton());
			updateGUI(ureq);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(endTestPartButton == source) {
				doEndTestPart(ureq);
			} else if(closeTestButton == source) {
				doCloseTest(ureq);
			} else if(cancelTestButton == source) {
				doCancelTest(ureq);
			} else if(suspendTestButton == source) {
				doSuspendTest(ureq);
			} else if(source == qtiEl || source == qtiTreeEl) {
				if(event instanceof QTIWorksAssessmentTestEvent) {
					fireEvent(ureq, event);
				}
			} else if(source instanceof FormLink) {
				FormLink formLink = (FormLink)source;
				processResponse(ureq, formLink);
			}
			super.formInnerEvent(ureq, source, event);
			updateGUI(ureq);
		}
		
		@Override
		protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
			if(!"mark".equals(fe.getCommand())) {
				super.propagateDirtinessToContainer(fiSrc, fe);
			}
		}

		@Override
		protected void fireResponse(UserRequest ureq, FormItem source,
				Map<Identifier, StringResponseData> stringResponseMap, Map<Identifier, MultipartFileInfos> fileResponseMap,
				String comment) {
			fireEvent(ureq, new QTIWorksAssessmentTestEvent(QTIWorksAssessmentTestEvent.Event.response, stringResponseMap, fileResponseMap, comment, source));
		}
		
		/**
		 * Make sure that the end test part is not clicked 2x (which
		 * the qtiworks runtime don't like).
		 * 
		 * @param ureq
		 */
		private void doEndTestPart(UserRequest ureq) {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			if(!candidateSessionContext.isTerminated() && !testSessionState.isExited()
					&& testSessionController.mayEndCurrentTestPart()) {
				final TestPlanNodeKey currentTestPartKey = testSessionState.getCurrentTestPartKey();
				final TestPartSessionState currentTestPartSessionState = testSessionState.getTestPartSessionStates().get(currentTestPartKey);
				if(!currentTestPartSessionState.isEnded()) {
					fireEvent(ureq, new QTIWorksAssessmentTestEvent(QTIWorksAssessmentTestEvent.Event.endTestPart, endTestPartButton));
					qtiEl.getComponent().setDirty(true);
					qtiTreeEl.getComponent().setDirty(true);
				}
			}
		}
		
		/**
		 * Make sure that the close test happens only once.
		 * 
		 * @param ureq
		 */
		private void doCloseTest(UserRequest ureq) {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			if(!candidateSessionContext.isTerminated() && !testSessionState.isExited()
					&& testSessionController.mayEndCurrentTestPart()) {
				final TestPlanNodeKey currentTestPartKey = testSessionState.getCurrentTestPartKey();
				final TestPartSessionState currentTestPartSessionState = testSessionState.getTestPartSessionStates().get(currentTestPartKey);
				if(currentTestPartSessionState.isEnded()) {
					fireEvent(ureq, new QTIWorksAssessmentTestEvent(QTIWorksAssessmentTestEvent.Event.advanceTestPart, closeTestButton));
				}
			}
		}
		
		private void doCancelTest(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
		
		private void doSuspendTest(UserRequest ureq) {
			fireEvent(ureq, new Event("suspend"));
		}
		
		private void updateGUI(UserRequest ureq) {
			//updateButtons();
			if(testSessionController.getTestSessionState().isEnded()
					&& deliveryOptions.getShowResultsOnFinish() != null
					&& !ShowResultsOnFinish.none.equals(deliveryOptions.getShowResultsOnFinish())) {
				removeAsListenerAndDispose(resultCtrl);
				resultCtrl = new AssessmentResultController(ureq, getWindowControl(), getIdentity(),
						AssessmentTestDisplayController.this.getCandidateSession(),
						deliveryOptions.getShowResultsOnFinish(), fUnzippedDirRoot, mapperUri);
				listenTo(resultCtrl);
				flc.add("qtiResults", resultCtrl.getInitialFormItem());
			}
			updateQtiWorksStatus();
		}
		
		/*private void updateButtons() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			boolean enabled = !candidateSessionContext.isTerminated() && !testSessionState.isExited()
					&& testSessionController.mayEndCurrentTestPart();
			
			//endTestPartButton.setEnabled(enabled);
			
			//closeTestButton.setEnabled(enabled);
		}*/
		
		private void updateQtiWorksStatus() {
			if(deliveryOptions.isDisplayQuestionProgress() || deliveryOptions.isDisplayScoreProgress()) {

				TestPlanInfos testPlanInfos = new TestPlanInfos();
				testSessionController.visitTestPlan(testPlanInfos);
				
				if(deliveryOptions.isDisplayQuestionProgress()) {
					questionProgress.setMax(testPlanInfos.getNumOfItems());
					questionProgress.setActual(testPlanInfos.getNumOfAnsweredItems());
					qtiWorksStatus.setNumOfItems(testPlanInfos.getNumOfItems());
					qtiWorksStatus.setNumOfAnsweredItems(testPlanInfos.getNumOfAnsweredItems());
				}
				
				if(deliveryOptions.isDisplayScoreProgress()) {
					double score = testPlanInfos.getScore();
					double maxScore = testPlanInfos.getMaxScore();
					
					// real assessment test score overwrite
					Value assessmentTestScoreValue = testSessionController.getTestSessionState()
						.getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
					if(assessmentTestScoreValue instanceof FloatValue) {
						score = ((FloatValue)assessmentTestScoreValue).doubleValue();
					}
					Value assessmentTestMaxScoreValue = testSessionController.getTestSessionState()
							.getOutcomeValue(QTI21Constants.MAXSCORE_IDENTIFIER);
					if(assessmentTestMaxScoreValue instanceof FloatValue) {
						maxScore = ((FloatValue)assessmentTestMaxScoreValue).doubleValue();
					}
					
					qtiWorksStatus.setScore(score);
					qtiWorksStatus.setMaxScore(maxScore);
					scoreProgress.setActual((float)score);
					scoreProgress.setMax((float)maxScore);
				}
			}
		}
	}
	
	public class TestPlanInfos implements TestPlanVisitor {
		
		private int numOfItems = 0;
		private int numOfAnsweredItems = 0;
		
		private double score = 0.0d;
		private double maxScore = 0.0d;
		
		@Override
		public void visit(TestPlanNode testPlanNode) {
			final TestNodeType type = testPlanNode.getTestNodeType();
			if(type == TestNodeType.ASSESSMENT_ITEM_REF) {
				numOfItems++;
				
				ItemSessionState state = testSessionController.getTestSessionState()
						.getItemSessionStates().get(testPlanNode.getKey());
				if(state != null && state.isResponded()) {
					numOfAnsweredItems++;
				}
				
				Value maxScoreValue = state.getOutcomeValue(QTI21Constants.MAXSCORE_IDENTIFIER);
				if(maxScoreValue instanceof FloatValue) {
					maxScore += ((FloatValue)maxScoreValue).doubleValue();
				}
				Value scoreValue = state.getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
				if(scoreValue instanceof FloatValue) {
					score += ((FloatValue)scoreValue).doubleValue();
				}
			}
		}
		
		public int getNumOfItems() {
			return numOfItems;
		}
		
		public int getNumOfAnsweredItems() {
			return numOfAnsweredItems;
		}
		
		public double getMaxScore() {
			return maxScore;
		}
		
		public double getScore() {
			return score;
		}
	}
	
	public class QtiWorksStatus {

		private final DecimalFormat scoreFormat = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.ENGLISH));
		
		private int numOfItems;
		private int numOfAnsweredItems;
		
		private double score;
		private double maxScore;


		public boolean isSurvey() {
			return false;
		}
		
		public String getScore() {
			return scoreFormat.format(score);
		}
		
		public void setScore(double score) {
			this.score = score;
		}
		
		public boolean hasMaxScore() {
			return true;
		}
		
		public String getMaxScore() {
			return scoreFormat.format(maxScore);
		}
		
		public void setMaxScore(double maxScore) {
			this.maxScore = maxScore;
		}
		
		public String getNumberOfAnsweredQuestions() {
			return numOfAnsweredItems <= 0 ? "0" : Integer.toString(numOfAnsweredItems);
		}
		
		public void setNumOfItems(int numOfItems) {
			this.numOfItems = numOfItems;
		}
		
		public String getNumberOfQuestions() {
			return numOfItems <= 0 ? "0" : Integer.toString(numOfItems);
		}
		
		public void setNumOfAnsweredItems(int numOfAnsweredItems) {
			this.numOfAnsweredItems = numOfAnsweredItems;
		}
		
		public boolean maySuspendTest() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			return deliveryOptions.isEnableSuspend() && !candidateSessionContext.isTerminated()
					&& !testSessionState.isExited() && !testSessionState.isEnded();
		}
		
		public boolean mayCancelTest() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			return deliveryOptions.isEnableCancel() &&  !candidateSessionContext.isTerminated()
					&& !testSessionState.isExited() && !testSessionState.isEnded();
		}
		
		public boolean mayEndCurrentTestPart() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			return !candidateSessionContext.isTerminated() && !testSessionState.isExited()
					&& testSessionController.mayEndCurrentTestPart();
		}
		
		public boolean mayCloseTest() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			if(!candidateSessionContext.isTerminated() && !testSessionState.isExited()
					&& testSessionController.mayEndCurrentTestPart()) {
				final TestPlanNodeKey currentTestPartKey = testSessionState.getCurrentTestPartKey();
				final TestPartSessionState currentTestPartSessionState = testSessionState.getTestPartSessionStates().get(currentTestPartKey);
				if(currentTestPartSessionState.isEnded()) {
					return true;
				}
			}
			return false;
		}
	}
}