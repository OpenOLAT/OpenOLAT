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

import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.testFeedbackVisible;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.resource.WindowedResourceableList;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestMarks;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesListener;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.ResponseFormater;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.InMemoryAssessmentTestMarks;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateExceptionReason;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;
import org.olat.ims.qti21.ui.ResponseInput.Base64Input;
import org.olat.ims.qti21.ui.ResponseInput.FileInput;
import org.olat.ims.qti21.ui.ResponseInput.StringInput;
import org.olat.ims.qti21.ui.components.AssessmentTestFormItem;
import org.olat.ims.qti21.ui.components.AssessmentTestTimerFormItem;
import org.olat.ims.qti21.ui.components.AssessmentTreeFormItem;
import org.olat.ims.qti21.ui.event.DeleteAssessmentTestSessionEvent;
import org.olat.ims.qti21.ui.event.RestartEvent;
import org.olat.ims.qti21.ui.event.RetrieveAssessmentTestSessionEvent;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.exception.QtiCandidateStateException;
import uk.ac.ed.ph.jqtiplus.exception.ResponseBindingException;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.result.TestResult;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedbackAccess;
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
public class AssessmentTestDisplayController extends BasicController implements CandidateSessionContext, GenericEventListener {
	
	private final File fUnzippedDirRoot;
	private String mapperUri;
	private final QTI21DeliveryOptions deliveryOptions;
	private final QTI21OverrideOptions overrideOptions;
	
	private VelocityContainer mainVC;
	private final StackedPanel mainPanel;
	private QtiWorksController qtiWorksCtrl;
	private AssessmentResultController resultCtrl;
	private TestSessionController testSessionController;

	private DialogBoxController endTestPartDialog;
	private DialogBoxController advanceTestPartDialog;
	private DialogBoxController confirmCancelDialog;
	private DialogBoxController confirmSuspendDialog;
	
	private CandidateEvent lastEvent;
	private Date touchCacheTimestamp;
	private AssessmentTestSession candidateSession;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private AssessmentTestMarks marks;
	private final Map<TestPlanNode, Integer> numbering = new HashMap<>();
	
	private RepositoryEntry testEntry;
	private RepositoryEntry entry;
	private String subIdent;
	private final boolean authorMode;
	private final Date controllerCreationDate = new Date();
	
	private final boolean anonym;
	private final Identity assessedIdentity;
	private final String anonymousIdentifier;
	
	/**
	 * Additional time in seconds
	 */
	private Integer extraTime;
	private Integer compensationExtraTime;
	private boolean sessionDeleted = false;
	private final boolean showCloseResults;
	private OutcomesListener outcomesListener;
	private AssessmentSessionAuditLogger candidateAuditLogger;
	private final WindowedResourceableList resourcesList;

	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param listener If the listener is null, the controller will use the default listener which save the score and pass in assessment entry
	 * @param testEntry
	 * @param entry
	 * @param subIdent
	 * @param deliveryOptions
	 * @param showCloseResults set to false prevent the close results button to appears (this boolean
	 * 		don't change the settings to show or not the results at the end of the test)
	 * @param authorMode if true, the database objects are not counted and can be deleted without warning
	 */
	public AssessmentTestDisplayController(UserRequest ureq, WindowControl wControl, OutcomesListener listener,
			RepositoryEntry testEntry, RepositoryEntry entry, String subIdent,
			QTI21DeliveryOptions deliveryOptions, QTI21OverrideOptions overrideOptions,
			boolean showCloseResults, boolean authorMode, boolean anonym) {
		super(ureq, wControl);

		this.entry = entry;
		this.subIdent = subIdent;
		this.testEntry = testEntry;
		this.outcomesListener = listener;
		this.deliveryOptions = deliveryOptions;
		this.overrideOptions = overrideOptions;
		this.showCloseResults = showCloseResults;
		this.authorMode = authorMode;
		
		UserSession usess = ureq.getUserSession();
		if(usess.getRoles().isGuestOnly() || anonym) {
			this.anonym = true;
			assessedIdentity = null;
			anonymousIdentifier = getAnonymousIdentifier(usess);
		} else {
			this.anonym = false;
			assessedIdentity = getIdentity();
			anonymousIdentifier = null;
		}
		
		if(testEntry == entry) {
			// Limit to the case where the test is launched as resource,
			// within course is this task delegated to the QTI21AssessmentRunController
			addLoggingResourceable(LoggingResourceable.wrapTest(entry));
		}

		resourcesList = usess.getResourceList();
		if(subIdent == null && !authorMode && !resourcesList.registerResourceable(entry, subIdent, getWindow())) {
			showWarning("warning.multi.window");
		}
		
		FileResourceManager frm = FileResourceManager.getInstance();
		fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		touchCacheTimestamp = ureq.getRequestTimestamp();
		if(resolvedAssessmentTest == null || resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful() == null) {
			mainVC = createVelocityContainer("error");
		} else {
			initMarks();
			initOrResumeAssessmentTestSession(ureq, authorMode);
			
			URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
			File submissionDir = qtiService.getSubmissionDirectory(candidateSession);
			mapperUri = registerCacheableMapper(ureq, "QTI21Resources::" + testEntry.getKey(),
					new ResourcesMapper(assessmentObjectUri, submissionDir));
	
			/* Handle immediate end of test session */
			testSessionController.setCurrentRequestTimestamp(ureq.getRequestTimestamp());
	        if (testSessionController.getTestSessionState() != null && testSessionController.getTestSessionState().isEnded()) {
	        		immediateEndTestSession(ureq);
	        		mainVC = createVelocityContainer("end");
	        } else {
	        		mainVC = createVelocityContainer("run");
	        		initQtiWorks(ureq);
	        		if(!deliveryOptions.isShowTitles()) {
	        			initNumbering();
	        		}
	        }
	        
	        OLATResourceable sessionOres = OresHelper
	        		.createOLATResourceableInstance(AssessmentTestSession.class, candidateSession.getKey());
	        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), sessionOres);
		}
        mainPanel = putInitialPanel(mainVC);
	}
	
	private void initNumbering() {
		AtomicInteger number = new AtomicInteger(0);
		List<TestPlanNode> nodes = testSessionController.getTestSessionState().getTestPlan().getTestPlanNodeList();
		for(TestPlanNode node:nodes) {
			if(node.getTestNodeType() == TestNodeType.ASSESSMENT_ITEM_REF) {
				numbering.put(node, number.incrementAndGet());
			} else if(node.getTestNodeType() == TestNodeType.TEST_PART) {
				number.set(0);
			}
		}
	}
	
	private void immediateEndTestSession(UserRequest ureq) {
		AssessmentResult assessmentResult = qtiService.getAssessmentResult(candidateSession);
		if(assessmentResult == null) {
            candidateSession.setTerminationTime(ureq.getRequestTimestamp());
            candidateSession.setExploded(true);
            candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
		} else {
			candidateSession = qtiService.finishTestSession(candidateSession, testSessionController.getTestSessionState(), assessmentResult,
					ureq.getRequestTimestamp(), getDigitalSignatureOptions(), getIdentity());
		}
	}
	
	private String getAnonymousIdentifier(UserSession usess) {
		String sessionId = usess.getSessionInfo().getSession().getId();
		String testKey = (entry == null ? testEntry.getKey() : entry.getKey()) + "-" + subIdent +"-" + testEntry.getKey() + "-" + sessionId;
		Object id = usess.getEntry(testKey);
		if(id instanceof String) {
			return (String)id;
		}

		String newId = UUID.randomUUID().toString();
		usess.putEntryInNonClearedStore(testKey, newId);
		return newId;
	}
	
	private void initMarks() {
		if(anonym) {
			marks = new InMemoryAssessmentTestMarks();
		} else {
			marks = qtiService.getMarks(getIdentity(), entry, subIdent, testEntry);
		}
	}
	
	private void initOrResumeAssessmentTestSession(UserRequest ureq, boolean authorMode) {
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent, Boolean.FALSE, testEntry);
		if(outcomesListener == null) {
			boolean manualCorrections = AssessmentTestHelper.needManualCorrection(resolvedAssessmentTest);
			outcomesListener = new AssessmentEntryOutcomesListener(entry, testEntry, assessmentEntry, manualCorrections, assessmentService, authorMode);
		}
		
		if(StringHelper.containsNonWhitespace(subIdent)) {
			DisadvantageCompensation compensation = disadvantageCompensationService.getActiveDisadvantageCompensation(getIdentity(), entry, subIdent);
			if(compensation != null) {
				compensationExtraTime = compensation.getExtraTime();
			} else {
				compensationExtraTime = null;
			}
		}

		AssessmentTestSession lastSession = qtiService.getResumableAssessmentTestSession(assessedIdentity, anonymousIdentifier, entry, subIdent, testEntry, authorMode);
		if(lastSession == null) {
			initNewAssessmentTestSession(ureq, assessmentEntry, compensationExtraTime, authorMode);
		} else {
			candidateSession = lastSession;
			extraTime = lastSession.getExtraTime();
			candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(candidateSession, authorMode);
			
			lastEvent = new CandidateEvent(candidateSession, testEntry, entry);
			lastEvent.setTestEventType(CandidateTestEventType.ITEM_EVENT);
			
			if(authorMode) {
				//check that the resumed session match the current test
				try {
					testSessionController = resumeSession(ureq);
					if(!checkAuthorSession()) {
						initNewAssessmentTestSession(ureq, assessmentEntry, compensationExtraTime, authorMode);
					}
				} catch(Exception e) {
					logWarn("Cannot resume session as author", e);
					initNewAssessmentTestSession(ureq, assessmentEntry, compensationExtraTime, authorMode);
				}
			} else {
				testSessionController = resumeSession(ureq);
			}
		}
	}
	
	private void initNewAssessmentTestSession(UserRequest ureq, AssessmentEntry assessmentEntry, Integer compensationTime, boolean authorMode) {
		candidateSession = qtiService.createAssessmentTestSession(assessedIdentity, anonymousIdentifier, assessmentEntry,
				entry, subIdent, testEntry, compensationTime, authorMode);
		candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(candidateSession, authorMode);
		testSessionController = enterSession(ureq);
	}
	
	/**
	 * If the session data doesn't match the current assessmentTest and assessmentItems, it will
	 * return false.
	 * @return
	 */
	private boolean checkAuthorSession() {
		try {
			//
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			if(!isTerminated() && !testSessionState.isExited() && testSessionState.getCurrentTestPartKey() != null) {
				testSessionController.mayEndCurrentTestPart();
			}
			return true;
		} catch(Exception e) {
			logError("Cannot resume session as author", e);
			return false;
		}
	}
	
	private void initQtiWorks(UserRequest ureq) {
		qtiWorksCtrl = new QtiWorksController(ureq, getWindowControl());
		listenTo(qtiWorksCtrl);
		mainVC.put("qtirun", qtiWorksCtrl.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		if(subIdent == null && !authorMode) {
			resourcesList.deregisterResourceable(entry, subIdent, getWindow());
		}
		try {
			candidateSession = qtiService.reloadAssessmentTestSession(candidateSession);
			if(candidateSession != null) {
				testSessionController = qtiService.getCachedTestSessionController(candidateSession, testSessionController);
				suspendAssessmentTest(new Date());
				
				OLATResourceable sessionOres = OresHelper
						.createOLATResourceableInstance(AssessmentTestSession.class, candidateSession.getKey());
				CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, sessionOres);
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	/**
	 * @return The assessment test if the test cab be read or null.
	 */
	public AssessmentTest getAssessmentTest() {
		return resolvedAssessmentTest == null ? null : resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
	}
	
	/**
	 * @return true if the termination time is set.
	 */
	@Override
	public boolean isTerminated() {
		return candidateSession.getTerminationTime() != null;
	}
	
	/**
	 * @return true if the termination time or the finished time is set.
	 */
	public boolean isEnded() {
		return candidateSession.getTerminationTime() != null || candidateSession.getFinishTime() != null;
	}

	@Override
	public AssessmentTestSession getCandidateSession() {
		return candidateSession;
	}
	
	protected AssessmentTestSession getCandidateSession(boolean reload) {
		if(reload) {
			candidateSession = qtiService.reloadAssessmentTestSession(candidateSession);
		}
		return getCandidateSession();
	}
	
	@Override
	public CandidateEvent getLastEvent() {
		return lastEvent;
	}

	@Override
	public Date getCurrentRequestTimestamp() {
		return testSessionController.getCurrentRequestTimestamp();
	}
	
	public boolean isResultsVisible() {
		return qtiWorksCtrl != null && qtiWorksCtrl.isResultsVisible();
	}

	@Override
	public void event(Event event) {
		if(event instanceof RetrieveAssessmentTestSessionEvent) {
			RetrieveAssessmentTestSessionEvent rats = (RetrieveAssessmentTestSessionEvent)event;
			processRetrieveAssessmentTestSessionEvent(rats);
		} else if(event instanceof DeleteAssessmentTestSessionEvent) {
			DeleteAssessmentTestSessionEvent dats = (DeleteAssessmentTestSessionEvent)event;
			processDeleteAssessmentTestSessionEvent(dats);
		}
	}
	
	private void processRetrieveAssessmentTestSessionEvent(RetrieveAssessmentTestSessionEvent rats) {
		if(candidateSession != null && candidateSession.getKey().equals(rats.getAssessmentTestSessionKey())) {
			candidateSession = qtiService.reloadAssessmentTestSession(candidateSession);
			extraTime = candidateSession.getExtraTime();
			compensationExtraTime = candidateSession.getCompensationExtraTime();
			if(extraTime != null || compensationExtraTime != null) {
				qtiWorksCtrl.extraTime();
			}
		}
	}
	
	private void processDeleteAssessmentTestSessionEvent(DeleteAssessmentTestSessionEvent dats) {
		if(candidateSession != null && candidateSession.getKey().equals(dats.getAssessmentTestSessionKey())) {
			candidateSession = qtiService.reloadAssessmentTestSession(candidateSession);
			sessionDeleted = true;
		}
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
		} else if(endTestPartDialog == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				processEndTestPart(ureq);
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
			} else if(QTI21Event.CLOSE_RESULTS.equals(event.getCommand())) {
				fireEvent(ureq, event);
			} else if(event instanceof QTIWorksAssessmentTestEvent) {
				processQTIEvent(ureq, (QTIWorksAssessmentTestEvent)event);
			}
		}
		super.event(ureq, source, event);
	}
	
	/**
	 * Check if the session has the finished date set by someone else.
	 * 
	 * @param ureq The user request
	 * @return true if the test exited, false if not
	 */
	private boolean checkConcurrentExit(UserRequest ureq) {
		if(candidateSession != null && candidateSession.getFinishTime() != null) {
			doExitTest(ureq);
			return true;
		}
		return false;
	}

	private void doExitTest(UserRequest ureq) {
		resourcesList.deregisterResourceable(entry, subIdent, getWindow());
		fireEvent(ureq, new QTI21Event(QTI21Event.EXIT));
	}
	
	private void doCloseResults(UserRequest ureq) {
		resourcesList.deregisterResourceable(entry, subIdent, getWindow());
		fireEvent(ureq, new QTI21Event(QTI21Event.CLOSE_RESULTS));
	}
	
	private void doConfirmSuspend(UserRequest ureq) {
		String title = translate("suspend.test");
		String text = translate("confirm.suspend.test");
		confirmSuspendDialog = activateOkCancelDialog(ureq, title, text, confirmSuspendDialog);
	}
	
	private void doSuspend(UserRequest ureq) {
		testSessionController = qtiService.getCachedTestSessionController(candidateSession, testSessionController);
		
		VelocityContainer suspendedVC = createVelocityContainer("suspended");
		mainPanel.setContent(suspendedVC);
		suspendAssessmentTest(ureq.getRequestTimestamp());
		resourcesList.deregisterResourceable(entry, subIdent, getWindow());
		fireEvent(ureq, new Event("suspend"));
	}

	/**
	 * It suspend the current item
	 * @return
	 */
	private boolean suspendAssessmentTest(Date requestTimestamp) {
		if(!deliveryOptions.isEnableSuspend() || testSessionController == null
				|| testSessionController.getTestSessionState() == null
				|| testSessionController.getTestSessionState().isEnded()
				|| testSessionController.getTestSessionState().isExited()
				|| testSessionController.getTestSessionState().isSuspended()) {
			return false;
		}
		
		testSessionController.touchDurations(testSessionController.getCurrentRequestTimestamp());
		testSessionController.suspendTestSession(requestTimestamp);
		
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
			itemSessionController.suspendItemSession(requestTimestamp);
			computeAndRecordTestAssessmentResult(requestTimestamp, testSessionState, false);
			
			NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
			final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
	                CandidateTestEventType.SUSPEND, null, null, testSessionState, notificationRecorder);
	        candidateAuditLogger.logCandidateEvent(candidateEvent);
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
		//only cancel unfinished test (prevent concurrent pull session / cancel to delete the data)
		if(candidateSession.getFinishTime() == null) {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			qtiService.deleteTestSession(candidateSession, testSessionState);
		}
		fireEvent(ureq, Event.CANCELLED_EVENT);
		candidateSession = null;
	}
	
	private boolean sessionReseted(UserRequest ureq) {
		if(sessionDeleted) {
			qtiWorksCtrl.markSessionAsDeleted();
			doExitTest(ureq);
			showWarning("warning.reset.assessmenttest.data");
		}
		return sessionDeleted;
	}
	
	private boolean sessionEndedOrSuspended() {
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		if(testSessionState.isEnded() || testSessionState.isSuspended()) {
			candidateSession = qtiService.reloadAssessmentTestSession(candidateSession);
			showWarning("warning.suspended.ended.assessmenttest");
			logAudit("Try to work on an ended/suspended test");
			return true;
		}
		return false;
	}
	
	/**
	 * This method maintains the assessment test in cache during
	 * a test session. This controller doesn't need the cache, the
	 * test is strong referenced but at the end of the test, score
	 * evaluation of the course, rendering of the assessment result
	 * can need the cache again. For very large tests, it can be a
	 * performance issue.
	 * 
	 * @param ureq The user request
	 */
	private void touchResolvedAssessmentTest(UserRequest ureq) {
		try {
			Date timestamp = ureq.getRequestTimestamp();
			if(touchCacheTimestamp == null || (timestamp.getTime() > touchCacheTimestamp.getTime() + 300000l)) {
				touchCacheTimestamp = timestamp;
				qtiService.touchCachedResolveAssessmentTest(fUnzippedDirRoot);
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private boolean timeLimitBarrier(UserRequest ureq) {
		Long assessmentTestMaxTimeLimits = getAssessmentTestMaxTimeLimit();
		if(assessmentTestMaxTimeLimits != null) {
			long maximumAssessmentTestDuration = assessmentTestMaxTimeLimits.longValue() * 1000;//convert in milliseconds
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			if(!testSessionState.isEnded() && !testSessionState.isExited()) {
				long durationMillis = testSessionState.getDurationAccumulated();
				durationMillis += getRequestTimeStampDifferenceToNow();
				if(durationMillis > maximumAssessmentTestDuration) {
					testSessionController.setCurrentRequestTimestamp(ureq.getRequestTimestamp());
					processExitTestAfterTimeLimit(ureq);
					return true;
				}
			}
			
			/*
			ItemProcessingContext ctx = testSessionController.getItemProcessingContext(null);
			ItemSessionState itemSessionState = ctx.getItemSessionState();
			itemSessionState.getDurationAccumulated();
			double itemDuration = ctx.getItemSessionState().computeDuration();
			*/
			
		}
		return false;
	}
	
	/**
	 * 
	 * @return The maximum time limit in seconds.
	 */
	private Long getAssessmentTestMaxTimeLimit() {
		int extra = extraTime == null ? 0 : extraTime.intValue();
		int extraCompensation = compensationExtraTime == null ? 0 : compensationExtraTime.intValue();
		int totalExtra = extra + extraCompensation;
		
		Long leadingTimeInMilliSeconds = getLeadingTimeEndTestOption();
		long leadingDuration = Long.MAX_VALUE;
		if(leadingTimeInMilliSeconds != null) {
			double leadingDurationInSeconds = (leadingTimeInMilliSeconds + getAssessmentTestDuration()) / 1000.0d;
			leadingDuration = Math.round(leadingDurationInSeconds);
		}
		if(overrideOptions != null && overrideOptions.getAssessmentTestMaxTimeLimit() != null) {
			long timeLimits = overrideOptions.getAssessmentTestMaxTimeLimit().longValue();
			return timeLimits > 0 ? Math.min(leadingDuration, timeLimits) + totalExtra : null;
		}
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		if(assessmentTest.getTimeLimits() != null && assessmentTest.getTimeLimits().getMaximum() != null) {
			long timeLimits = assessmentTest.getTimeLimits().getMaximum().longValue();
			return Math.min(leadingDuration, timeLimits) + totalExtra;
		}
		if(leadingTimeInMilliSeconds != null) {
			return leadingDuration + totalExtra;
		}
		return null;
	}

	
	/**
	 * @return The number of milliseconds up to the defined end of the test (or a very long time if nothing is configured)
	 */
	private Long getLeadingTimeEndTestOption() {
		if(overrideOptions != null && overrideOptions.getEndTestDate() != null) {
			Date endTestDate = overrideOptions.getEndTestDate();
			long diff = endTestDate.getTime() - testSessionController.getCurrentRequestTimestamp().getTime();
			if(diff < 0l) {
				diff = 0l;
			}
			return diff;
		}
		return null;// default is a year
	}
	
	/**
	 * @return the difference in milliseconds
	 */
	private long getRequestTimeStampDifferenceToNow() {
		long diff = 0l;
		if(testSessionController.getCurrentRequestTimestamp() != null) {
			//take time between 2 reloads if the user reload the page
			diff = (new Date().getTime() - testSessionController.getCurrentRequestTimestamp().getTime());
			if(diff < 0) {
				diff = 0;
			}
		}
		return diff;
	}
	
	/**
	 * @return The test duration in milliseconds
	 */
	private long getAssessmentTestDuration() {
		if(testSessionController == null || candidateSession == null
				|| testSessionController.getTestSessionState() == null) {
			return -1;
		}
		
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		Date startTime = testSessionState.getDurationIntervalStartTime();
		if(startTime == null) {
			startTime = testSessionState.getEntryTime();
		}
		if(startTime == null) {
			startTime = candidateSession.getCreationDate();
		}

		Date timestamp = new Date();
		startTime = startTime == null ? controllerCreationDate : startTime;
        final long durationDelta = timestamp.getTime() - startTime.getTime();
        return testSessionState.getDurationAccumulated() + durationDelta;
	}

	private void processQTIEvent(UserRequest ureq, QTIWorksAssessmentTestEvent qe) {
		testSessionController = qtiService.getCachedTestSessionController(candidateSession, testSessionController);
		testSessionController.setCurrentRequestTimestamp(ureq.getRequestTimestamp());
		
		if(authorMode && qe.getEvent() == QTIWorksAssessmentTestEvent.Event.restart) {
			restartTest(ureq);
		} else if(timeLimitBarrier(ureq) || sessionReseted(ureq) || sessionEndedOrSuspended()) {
			return;
		}
		
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
				nextItemIfAllowed(ureq);
				break;
			case endTestPart:
				confirmEndTestPart(ureq);
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
			case timesUp:
				processExitTestAfterTimeLimit(ureq);
				break;
			case tmpResponse:
				handleTemporaryResponse(ureq, qe.getStringResponseMap());
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
			case rubric:
				toogleRubric(qe.getSubCommand());
				break;
			case restart:
				restartTest(ureq);
				break;
		}
		
		touchResolvedAssessmentTest(ureq);
	}
	
	private void restartTest(UserRequest ureq) {
		resourcesList.deregisterResourceable(entry, subIdent, getWindow());
		if(!candidateSession.isAuthorMode()) return;
		fireEvent(ureq, new RestartEvent());
	}
	
	private void toogleMark(String itemRef) {
		marks = getMarkerObject();
		
		String currentMarks = marks.getMarks();
		if(currentMarks == null) {
			marks.setMarks(itemRef);
		} else if(currentMarks.indexOf(itemRef) >= 0) {
			marks.setMarks(currentMarks.replace(itemRef, ""));
		} else {
			marks.setMarks(currentMarks + " " + itemRef);
		}
		if(marks instanceof Persistable) {
			marks = qtiService.updateMarks(marks);
		}
	}
	
	private void toogleRubric(String sectionRef) {
		marks = getMarkerObject();
		
		String hiddenRubrics = marks.getHiddenRubrics();
		if(hiddenRubrics == null) {
			marks.setHiddenRubrics(sectionRef);
		} else if(hiddenRubrics.indexOf(sectionRef) >= 0) {
			marks.setHiddenRubrics(hiddenRubrics.replace(sectionRef, ""));
		} else {
			marks.setHiddenRubrics(hiddenRubrics + " " + sectionRef);
		}
		if(marks instanceof Persistable) {
			marks = qtiService.updateMarks(marks);
		}
	}
	
	private final AssessmentTestMarks getMarkerObject() {
		if(marks == null) {
			if(anonym) {
				marks = new InMemoryAssessmentTestMarks();
			} else {
				marks = qtiService.createMarks(assessedIdentity, entry, subIdent, testEntry, "");
			}
		}
		return marks;
	}
	
	@Override
	public boolean isMarked(String itemKey) {
		if(marks == null || marks.getMarks() == null) return false;
		return marks.getMarks().indexOf(itemKey) >= 0;
	}
	
	@Override
	public boolean isRubricHidden(Identifier sectionKey) {
		if(marks == null || marks.getHiddenRubrics() == null || sectionKey == null) return false;
		return marks.getHiddenRubrics().indexOf(sectionKey.toString()) >= 0;
	}

	@Override
	public int getNumber(TestPlanNode node) {
		Integer number =  numbering.get(node);
		return number == null ? -1 : number.intValue(); 
	}

	private void processSelectItem(UserRequest ureq, String key) {
		if (checkConcurrentExit(ureq)) {
			return;
		}

		try {
			TestPlanNodeKey nodeKey = TestPlanNodeKey.fromString(key);
			Date requestTimestamp = ureq.getRequestTimestamp();
			TestPlanNode selectedNode = testSessionController.selectItemNonlinear(requestTimestamp, nodeKey);

			/* Record and log event */
			TestPlanNodeKey selectedNodeKey = (selectedNode == null ? null : selectedNode.getKey());
			NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
					CandidateTestEventType.SELECT_MENU, null, selectedNodeKey,testSessionState, notificationRecorder);
			candidateAuditLogger.logCandidateEvent(candidateEvent);
		} catch (final QtiCandidateStateException e) {
			logError("CANNOT_SELECT_MENU", e);//log informations
			ServletUtil.printOutRequestParameters(ureq.getHttpReq());
			mainVC.setDirty(true);// redraw the full panel -> browser view must be same as server states
		}
	}
	
	/**
	 * Try to go to the next item. It will check fi the current
	 * item want to show some feedback (modal or element), has some
	 * bad or invalid responses, state of the test session... or if
	 * the item is an adaptive one.
	 * 
	 * @param ureq
	 */
	private void nextItemIfAllowed(UserRequest ureq) {
		if (testSessionController.hasFollowingNonLinearItem()
				&& testSessionController.getTestSessionState() != null
				&& !testSessionController.getTestSessionState().isEnded()
				&& !testSessionController.getTestSessionState().isExited()) {

			try {
				TestSessionState testSessionState = testSessionController.getTestSessionState();
				TestPlanNodeKey itemNodeKey = testSessionState.getCurrentItemKey();
				if (itemNodeKey != null) {
					TestPlanNode currentItemNode = testSessionState.getTestPlan().getNode(itemNodeKey);
					boolean hasFeedbacks = qtiWorksCtrl.willShowSomeAssessmentItemFeedbacks(currentItemNode);
					// allow skipping
					if (!hasFeedbacks) {
						processNextItem(ureq);
					}
				}
			} catch (QtiCandidateStateException e) {
				logError("", e);//log informations
				ServletUtil.printOutRequestParameters(ureq.getHttpReq());
			}
		}
	}
	
	private void processNextItem(UserRequest ureq) {
		if (checkConcurrentExit(ureq)) {
			return;
		}

		Date requestTimestamp = ureq.getRequestTimestamp();
		if (testSessionController.hasFollowingNonLinearItem()) {
			TestPlanNode selectedNode = testSessionController.selectFollowingItemNonLinear(requestTimestamp);

			TestPlanNodeKey selectedNodeKey = (selectedNode == null ? null : selectedNode.getKey());
			NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
					CandidateTestEventType.NEXT_ITEM, null, selectedNodeKey, testSessionState, notificationRecorder);
			candidateAuditLogger.logCandidateEvent(candidateEvent);
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
            	candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_ITEM, null);
                return;
            }
        } catch (final QtiCandidateStateException e) {
        	logError("CANNOT_REVIEW_TEST_ITEM", e);
        	candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_ITEM, e);
            return;
        }  catch (final RuntimeException e) {
        	candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_ITEM, e);
        	logError("CANNOT_REVIEW_TEST_ITEM", e);
            return;// handleExplosion(e, candidateSession);
        }

        /* Record current result state */
        computeAndRecordTestAssessmentResult(ureq.getRequestTimestamp(), testSessionState, false);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.REVIEW_ITEM, null, itemKey, testSessionState, notificationRecorder);
        this.lastEvent = candidateTestEvent;
        candidateAuditLogger.logCandidateEvent(candidateTestEvent);
	}

	private void processItemSolution(UserRequest ureq, String key) {
		TestPlanNodeKey itemKey = TestPlanNodeKey.fromString(key);

        NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Make sure caller may do this */
        //assertSessionNotTerminated(candidateSession);
        try {
            if (!testSessionController.mayAccessItemSolution(itemKey)) {
                candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_SOLUTION_TEST_ITEM, null);
            	logError("CANNOT_SOLUTION_TEST_ITEM", null);
                return;
            }
        } catch (final QtiCandidateStateException e) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_SOLUTION_TEST_ITEM, e);
            logError("CANNOT_SOLUTION_TEST_ITEM", e);
        	return;
        } catch (final RuntimeException e) {
        	candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_SOLUTION_TEST_ITEM, e);
            logError("Exploded", e);
            return;// handleExplosion(e, candidateSession);
        }

        /* Record current result state */
        computeAndRecordTestAssessmentResult(ureq.getRequestTimestamp(), testSessionState, false);

        /* Record and log event */
        CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.SOLUTION_ITEM, null, itemKey, testSessionState, notificationRecorder);
        this.lastEvent = candidateTestEvent;
        candidateAuditLogger.logCandidateEvent(candidateTestEvent); 
	}
	
	//public CandidateSession finishLinearItem(final CandidateSessionContext candidateSessionContext)
    // throws CandidateException {
	private void processFinishLinearItem(UserRequest ureq) {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();
		
		try {
			if (!testSessionController.mayAdvanceItemLinear()) {
	            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_FINISH_LINEAR_TEST_ITEM, null);
				logError("CANNOT_FINISH_LINEAR_TEST_ITEM", null);
                return;
            }
		} catch (RuntimeException e) {
			candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_FINISH_LINEAR_TEST_ITEM, e);
         	logError("CANNOT_FINISH_LINEAR_TEST_ITEM", e);
			return;
		}
		 
		// Update state
		final Date requestTimestamp = ureq.getRequestTimestamp();
	    final TestPlanNode nextItemNode = testSessionController.advanceItemLinear(requestTimestamp);
	    
	    boolean terminated = nextItemNode == null && testSessionController.findNextEnterableTestPart() == null;
	    // Record current result state
	    final AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(requestTimestamp, testSessionState, terminated);
	    /* If we ended the testPart and there are now no more available testParts, then finish the session now */
	    if (terminated) {
	    		candidateSession = qtiService.finishTestSession(candidateSession, testSessionState, assessmentResult,
	    			requestTimestamp, getDigitalSignatureOptions(), getIdentity());
	    }

	    // Record and log event 
	    final CandidateTestEventType eventType = nextItemNode!=null ? CandidateTestEventType.FINISH_ITEM : CandidateTestEventType.FINISH_FINAL_ITEM;
	   	final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
	                eventType, null, null, testSessionState, notificationRecorder);
	   	this.lastEvent = candidateTestEvent;
	   	candidateAuditLogger.logCandidateEvent(candidateTestEvent);
	}
	
	private void processTestPartNavigation(UserRequest ureq) {
		final Date requestTimestamp = ureq.getRequestTimestamp();
        testSessionController.selectItemNonlinear(requestTimestamp, null);
	}
	
	private  ParentPartItemRefs getParentSection(TestPlanNodeKey itemKey) {
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		return AssessmentTestHelper.getParentSection(itemKey, testSessionState, resolvedAssessmentTest);
	}
	
	private void handleTemporaryResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap) {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		if(currentItemKey == null || sessionDeleted) {
			return;//
		}
		
		String cmd = ureq.getParameter("tmpResponse");
		if(!qtiWorksCtrl.validateResponseIdentifierCommand(cmd, currentItemKey)) {
			return;//this is not the right node in the plan
		}
		
		final Date timestamp = ureq.getRequestTimestamp();
		
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<>();
		if (stringResponseMap != null) {
			for (final Entry<Identifier, ResponseInput> responseEntry : stringResponseMap.entrySet()) {
				final Identifier identifier = responseEntry.getKey();
				final ResponseInput responseData = responseEntry.getValue();
				if(responseData instanceof StringInput) {
					responseDataMap.put(identifier, new StringResponseData(((StringInput)responseData).getResponseData()));
				}
            }
		}
		
		ParentPartItemRefs parentParts = getParentSection(currentItemKey);
		String assessmentItemIdentifier = currentItemKey.getIdentifier().toString();
		AssessmentItemSession itemSession = qtiService
				.getOrCreateAssessmentItemSession(candidateSession, parentParts, assessmentItemIdentifier);

        TestPlanNode currentItemRefNode = testSessionState.getTestPlan().getNode(currentItemKey);
        ItemSessionController itemSessionController = (ItemSessionController)testSessionController
        		.getItemProcessingContext(currentItemRefNode);
		ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
		
		List<Interaction> interactions = itemSessionController.getInteractions();
		Map<Identifier,Interaction> interactionMap = new HashMap<>();
		for(Interaction interaction:interactions) {
			interactionMap.put(interaction.getResponseIdentifier(), interaction);
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
                default:
                    throw new OLATRuntimeException("Unexpected switch case: " + responseData.getType());
            }
            candidateResponseMap.put(responseIdentifier, candidateItemResponse);
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
        
        /* Persist CandidateResponse entities */
        qtiService.recordTestAssessmentResponses(itemSession, candidateResponseMap.values());

        /* Record resulting event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.ITEM_EVENT, null, currentItemKey, testSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent, candidateResponseMap);
        
        /* Record current result state */
		AssessmentResult assessmentResult = computeTestAssessmentResult(timestamp, candidateSession);
		synchronized(this) {
			qtiService.recordTestAssessmentResult(candidateSession, testSessionState, assessmentResult, candidateAuditLogger);
		}
	}
	
	//public CandidateSession handleResponses(final CandidateSessionContext candidateSessionContext,
    //        final Map<Identifier, StringResponseData> stringResponseMap,
    //        final Map<Identifier, MultipartFile> fileResponseMap,
    //        final String candidateComment)       
	private void handleResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap,
			Map<Identifier, ResponseInput> fileResponseMap, String candidateComment) {
		if(checkConcurrentExit(ureq)) {
			return;
		}

		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
		TestSessionState testSessionState = testSessionController.getTestSessionState();

		TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		if(currentItemKey == null && getLastEvent() != null && getLastEvent().getTestEventType() == CandidateTestEventType.REVIEW_ITEM) {
			//someone try to send the form in review with tab / return
			return;
		}
		
		ItemSessionState currentItemSession = testSessionState.getCurrentItemSessionState();
		if(currentItemSession != null && currentItemSession.isEnded()) {
			showWarning("error.item.ended");
			return;
		}
		
		if(!qtiWorksCtrl.validatePresentedItem(currentItemKey)) {
			logError("Response send by browser doesn't match current item key", null);
			ServletUtil.printOutRequestParameters(ureq.getHttpReq());
			if(candidateSession != null && candidateSession.getFinishTime() != null) {
				showWarning("error.test.closed");
			} else {
				showWarning("error.reload.question");
			}
			return;//this is not the right node in the plan
		}
		
		if(currentItemSession != null && currentItemSession.isSuspended()) {
			TestPlanNode currentItemNode = testSessionState.getTestPlan().getNode(currentItemKey);
			ItemSessionController itemSessionController = (ItemSessionController)testSessionController
	        		.getItemProcessingContext(currentItemNode);
			itemSessionController.unsuspendItemSession(ureq.getRequestTimestamp());
		}
		
		final Map<Identifier,File> fileSubmissionMap = new HashMap<>();
		final Map<Identifier, ResponseData> responseDataMap = new HashMap<>();
		
		if (stringResponseMap != null) {
			for (final Entry<Identifier, ResponseInput> responseEntry : stringResponseMap.entrySet()) {
				final Identifier identifier = responseEntry.getKey();
				final ResponseInput responseData = responseEntry.getValue();
				if(responseData instanceof StringInput) {
					responseDataMap.put(identifier, new StringResponseData(((StringInput)responseData).getResponseData()));
				} else if(responseData instanceof Base64Input) {
					//only used from drawing interaction
					Base64Input fileInput = (Base64Input)responseData;
					String filename = "submitted_image.png";
					File storedFile = qtiService.importFileSubmission(candidateSession, filename, fileInput.getResponseData());
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
		
		ParentPartItemRefs parentParts = getParentSection(currentItemKey);

		String assessmentItemIdentifier = currentItemKey.getIdentifier().toString();
		AssessmentItemSession itemSession = qtiService
				.getOrCreateAssessmentItemSession(candidateSession, parentParts, assessmentItemIdentifier);
		
        if (fileResponseMap!=null) {
            for (Entry<Identifier, ResponseInput> fileResponseEntry : fileResponseMap.entrySet()) {
                Identifier identifier = fileResponseEntry.getKey();
                FileInput multipartFile = (FileInput)fileResponseEntry.getValue();
                if (!multipartFile.isEmpty()) {
                	File storedFile = qtiService.importFileSubmission(candidateSession, multipartFile.getMultipartFileInfos());
                    responseDataMap.put(identifier, new FileResponseData(storedFile, multipartFile.getContentType(), storedFile.getName()));
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
        final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.ITEM_EVENT, candidateItemEventType, currentItemKey, testSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent, candidateResponseMap);
        this.lastEvent = candidateEvent;

        /* Record current result state */
        AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(timestamp, testSessionState, false);
        
        ItemSessionState itemSessionState = testSessionState.getCurrentItemSessionState();
		long itemDuration = itemSessionState.getDurationAccumulated();
		itemSession.setDuration(itemDuration);
		ItemResult itemResult = assessmentResult.getItemResult(assessmentItemIdentifier);
		collectOutcomeVariablesForItemSession(itemResult, itemSession);
        /* Persist CandidateResponse entities */
        qtiService.recordTestAssessmentResponses(itemSession, candidateResponseMap.values());

        /* Save any change to session state */
        candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
        
        addToHistory(ureq, this);
	}
	
	private void collectOutcomeVariablesForItemSession(ItemResult resultNode, AssessmentItemSession itemSession) {
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
	
	private void confirmEndTestPart(UserRequest ureq) {
		TestPlanNode nextTestPart = testSessionController.findNextEnterableTestPart();

		if(nextTestPart == null) {
			String title = translate("confirm.finish.test.title");
			String text = translate("confirm.finish.test.text");
			endTestPartDialog = activateOkCancelDialog(ureq, title, text, endTestPartDialog);
		} else {
			TestPart currentTestPart = testSessionController.getCurrentTestPart();
			if(currentTestPart == null) {
				processEndTestPart(ureq);
			} else {
				String title = translate("confirm.finish.testpart.title");
				String text = translate("confirm.finish.testpart.text");
				endTestPartDialog = activateOkCancelDialog(ureq, title, text, endTestPartDialog);
			}
		}
	}

	//public CandidateSession endCurrentTestPart(final CandidateSessionContext candidateSessionContext)
	private void processEndTestPart(UserRequest ureq) {
		if(testSessionController.getCurrentTestPart() == null && testSessionController.getTestSessionState().isEnded()) {
        	// try to end an already ended test
			return;
        }
		
		/* Update state */
        final Date requestTimestamp = ureq.getRequestTimestamp();
        testSessionController.endCurrentTestPart(requestTimestamp);
        
        TestSessionState testSessionState = testSessionController.getTestSessionState();
        TestPlanNode nextTestPart = testSessionController.findNextEnterableTestPart();
        
        // Record current result state
	    final AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(requestTimestamp, testSessionState, nextTestPart == null);
        if(nextTestPart == null) {
        	candidateSession = qtiService.finishTestSession(candidateSession, testSessionState, assessmentResult,
        			requestTimestamp, getDigitalSignatureOptions(), getIdentity());
        	if(!qtiWorksCtrl.willShowSomeAssessmentTestFeedbacks()) {
        		//need feedback, no more parts, quickly exit
        		try {
        			//end current test part
                    testSessionController.enterNextAvailableTestPart(requestTimestamp);
                } catch (final QtiCandidateStateException e) {
                    candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_ADVANCE_TEST_PART, e);
                    logError("CANNOT_ADVANCE_TEST_PART", e);
                    return;
                } catch (final RuntimeException e) {
                    candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_ADVANCE_TEST_PART, e);
                    logError("RuntimeException", e);
                    return;// handleExplosion(e, candidateSession);
                }

        		//exit the test
                NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
                CandidateTestEventType eventType = CandidateTestEventType.EXIT_TEST;
                testSessionController.exitTest(requestTimestamp);
                candidateSession.setTerminationTime(requestTimestamp);
                candidateSession = qtiService.updateAssessmentTestSession(candidateSession);
        		
        		/* Record and log event */
                final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                       eventType, testSessionState, notificationRecorder);
                candidateAuditLogger.logCandidateEvent(candidateTestEvent);
                this.lastEvent = candidateTestEvent;

                qtiWorksCtrl.updateStatusAndResults(ureq);
        		doExitTest(ureq);
        	}
        } else if(!qtiWorksCtrl.willShowSomeTestPartFeedbacks()) {
        	//no feedback, go to the next part
        	processAdvanceTestPart(ureq);
        }
	}
	
	/**
	 * In the case of a multi-part test, the entry to the first part
	 * must not be confirmed.
	 * @param ureq
	 */
	private void confirmAdvanceTestPart(UserRequest ureq) {
		TestPlanNode nextTestPart = testSessionController.findNextEnterableTestPart();

		if(nextTestPart == null) {
			String title = translate("confirm.close.test.title");
			String text = translate("confirm.close.test.text");
			advanceTestPartDialog = activateOkCancelDialog(ureq, title, text, advanceTestPartDialog);
		} else {
			TestPart currentTestPart = testSessionController.getCurrentTestPart();
			if(currentTestPart == null) {
				processAdvanceTestPart(ureq);
			} else {
				String title = translate("confirm.advance.testpart.title");
				String text = translate("confirm.advance.testpart.text");
				advanceTestPartDialog = activateOkCancelDialog(ureq, title, text, advanceTestPartDialog);
			}
		}
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
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_ADVANCE_TEST_PART, e);
            logError("CANNOT_ADVANCE_TEST_PART", e);
            return;
        } catch (final RuntimeException e) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_ADVANCE_TEST_PART, e);
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
            } else {
                eventType = CandidateTestEventType.ADVANCE_TEST_PART;
            }
        }
        
        boolean terminated = isTerminated();
        
        /* Enter first assessment item if possible */
        enterFirstAssessmentItemOfAdvancedTestPart(nextTestPart, ureq);

        /* Record current result state */
        computeAndRecordTestAssessmentResult(currentTimestamp, testSessionState, terminated);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
               eventType, testSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;

        if (terminated) {
        	qtiWorksCtrl.updateStatusAndResults(ureq);
        	doExitTest(ureq);
        }
	}
	
	private void enterFirstAssessmentItemOfAdvancedTestPart(TestPlanNode nextTestPart, UserRequest ureq) {
		try {
			if(!isTerminated() && nextTestPart != null) {
			    TestPart currentTestPart = testSessionController.getCurrentTestPart();
				if(currentTestPart != null && currentTestPart.getNavigationMode() == NavigationMode.NONLINEAR) {
			    	//go to the first assessment item
			    	if(testSessionController.hasFollowingNonLinearItem()) {
			    		testSessionController.selectFollowingItemNonLinear(ureq.getRequestTimestamp());
			    	}
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void processReviewTestPart() {
		NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionState testSessionState = testSessionController.getTestSessionState();

        /* Make sure caller may do this */
        //assertSessionNotTerminated(candidateSession);
        if (testSessionState.getCurrentTestPartKey()==null || !testSessionState.getCurrentTestPartSessionState().isEnded()) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_REVIEW_TEST_PART, null);
            logError("CANNOT_REVIEW_TEST_PART", null);
        	return;
        }

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.REVIEW_TEST_PART, null, null, testSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateTestEvent);
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
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_EXIT_TEST, e);
        	logError("CANNOT_EXIT_TEST", null);
            return;
        } catch (final RuntimeException e) {
            candidateAuditLogger.logAndThrowCandidateException(candidateSession, CandidateExceptionReason.CANNOT_EXIT_TEST, e);
        	logError("Exploded", null);
            return;// handleExplosion(e, candidateSession);
        }

        /* Update CandidateSession as appropriate */
        candidateSession.setTerminationTime(currentTimestamp);
        candidateSession = qtiService.updateAssessmentTestSession(candidateSession);

        /* Record current result state (final) */
        computeAndRecordTestAssessmentResult(currentTimestamp, testSessionState, true);

        /* Record and log event */
        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.EXIT_TEST, testSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateTestEvent);
        this.lastEvent = candidateTestEvent;
        
        doExitTest(ureq);
	}
	
	private void processExitTestAfterTimeLimit(UserRequest ureq) {
        synchronized(testSessionController) {
        	// make sure the ajax call and a user click don't close both the session
            NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			if(testSessionState.isEnded() || testSessionState.isExited()) return;
			
			//close duration
			testSessionController.touchDurations(testSessionController.getCurrentRequestTimestamp());
			
	        final Date requestTimestamp = ureq.getRequestTimestamp();
			testSessionController.exitTestIncomplete(requestTimestamp);
			
	        candidateSession.setTerminationTime(requestTimestamp);
	
			 // Record current result state
		    final AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(requestTimestamp, testSessionState, true);
	        candidateSession = qtiService.finishTestSession(candidateSession, testSessionState, assessmentResult,
	        			requestTimestamp, getDigitalSignatureOptions(), getIdentity());
	        
	        qtiWorksCtrl.updateStatusAndResults(ureq);
	        
	        /* Record and log event */
	        final CandidateEvent candidateTestEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
	                CandidateTestEventType.EXIT_DUE_TIME_LIMIT, testSessionState, notificationRecorder);
	        candidateAuditLogger.logCandidateEvent(candidateTestEvent);
	        this.lastEvent = candidateTestEvent;
        }
        
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
        final CandidateEvent candidateEvent = qtiService.recordCandidateTestEvent(candidateSession, testEntry, entry,
                CandidateTestEventType.ENTER_TEST, testSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateEvent(candidateEvent);
        this.lastEvent = candidateEvent;
        
        boolean ended = testSessionState.isEnded();

        /* Record current result state */
        final AssessmentResult assessmentResult = computeAndRecordTestAssessmentResult(timestamp, testSessionState, ended);

        /* Handle immediate end of test session */
        if (ended) {
        		candidateSession = qtiService.finishTestSession(candidateSession, testSessionState, assessmentResult,
            		timestamp, getDigitalSignatureOptions(), getIdentity());
        } else {
        	TestPart currentTestPart = testSessionController.getCurrentTestPart();
        	if(currentTestPart != null && currentTestPart.getNavigationMode() == NavigationMode.NONLINEAR) {
        		//go to the first assessment item
        		if(testSessionController.hasFollowingNonLinearItem()) {
        			testSessionController.selectFollowingItemNonLinear(ureq.getRequestTimestamp());
        		}
        	}
        }
        
        return testSessionController;
	}
	
	private DigitalSignatureOptions getDigitalSignatureOptions() {
		boolean sendMail = deliveryOptions.isDigitalSignatureMail();
		boolean digitalSignature = deliveryOptions.isDigitalSignature() && qtiModule.isDigitalSignatureEnabled();
		DigitalSignatureOptions options = new DigitalSignatureOptions(digitalSignature, sendMail, entry, testEntry);
		if(digitalSignature) {
			outcomesListener.decorateConfirmation(candidateSession, options, getCurrentRequestTimestamp(), getLocale());
		}
		return options;
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
        qtiService.putCachedTestSessionController(candidateSession, result);
		return result;
	}
	
	private TestSessionController resumeSession(UserRequest ureq) {
		Date requestTimestamp = ureq.getRequestTimestamp();
		
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);
        TestSessionController controller = createTestSessionController(notificationRecorder);
        if(!controller.getTestSessionState().isEnded() && !controller.getTestSessionState().isExited()
        		&& (controller.getTestSessionState().isSuspended() || controller.getCurrentRequestTimestamp() == null)) {
        	controller.setCurrentRequestTimestamp(ureq.getRequestTimestamp());
        	controller.unsuspendTestSession(requestTimestamp);
            
            TestSessionState testSessionState = controller.getTestSessionState();
	    		TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
	    		if(currentItemKey != null) {
	    			TestPlanNode currentItemNode = testSessionState.getTestPlan().getNode(currentItemKey);
	    			ItemProcessingContext itemProcessingContext = controller.getItemProcessingContext(currentItemNode);
	    			ItemSessionState itemSessionState = itemProcessingContext.getItemSessionState();
	    			if(itemProcessingContext instanceof ItemSessionController
	    					&& itemSessionState.isSuspended()) {
	    				ItemSessionController itemSessionController = (ItemSessionController)itemProcessingContext;
	    				itemSessionController.unsuspendItemSession(requestTimestamp);
	    			}
	    		}
        }
        
        return controller;
	}
	
	private TestSessionController createTestSessionController(NotificationRecorder notificationRecorder) {
		TestSessionController result = qtiService.getCachedTestSessionController(candidateSession, null);
		if(result == null) {
			final TestSessionState testSessionState = qtiService.loadTestSessionState(candidateSession);
			result = createTestSessionController(testSessionState, notificationRecorder);
			qtiService.putCachedTestSessionController(candidateSession, result);
		}
        return result;
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
	
	private AssessmentResult computeAndRecordTestAssessmentResult(Date requestTimestamp, TestSessionState testSessionState, boolean submit) {
		AssessmentResult assessmentResult = computeTestAssessmentResult(requestTimestamp, candidateSession);

		TestPlanInfos testPlanInfos = new TestPlanInfos();
		testSessionController.visitTestPlan(testPlanInfos);
		candidateSession.setNumOfQuestions(testPlanInfos.getNumOfItems());
		candidateSession.setNumOfAnsweredQuestions(testPlanInfos.getNumOfAnsweredItems());
		candidateSession.setMaxScore(BigDecimal.valueOf(testPlanInfos.getMaxScore()));
		
		synchronized(this) {
			candidateSession = qtiService.recordTestAssessmentResult(candidateSession, testSessionState, assessmentResult, candidateAuditLogger);
		}
		processOutcomeVariables(assessmentResult.getTestResult(), testPlanInfos, submit);
		return assessmentResult;
	}
	
	private void processOutcomeVariables(TestResult resultNode, TestPlanInfos testPlanInfos, boolean submit) {
		Float score = null;
		Boolean pass = null;
		double completion = testPlanInfos.getCompletion();

		for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
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

		if (submit) {
			outcomesListener.submit(score, pass, candidateSession.getCreationDate(), completion, candidateSession.getKey());
		} else if(candidateSession != null && candidateSession.getFinishTime() == null) {
			//don't change the outcome if the test is finished
			outcomesListener.updateOutcomes(score, pass, candidateSession.getCreationDate(), completion);
		}
	}
	
	private AssessmentResult computeTestAssessmentResult(Date requestTimestamp, AssessmentTestSession testSession) {
		List<ContextEntry> entries = getWindowControl().getBusinessControl().getEntries();
		OLATResourceable testSessionOres = OresHelper.createOLATResourceableInstance("TestSession", testSession.getKey());
		entries.add(BusinessControlFactory.getInstance().createContextEntry(testSessionOres));
		String url = BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
		URI sessionIdentifierSourceId = URI.create(url);
		String sessionIdentifier = "testsession/" + testSession.getKey();
		return testSessionController.computeAssessmentResult(requestTimestamp, sessionIdentifier,
				sessionIdentifierSourceId);
	}
	
	private TestProcessingMap getTestProcessingMap() {
		boolean assessmentPackageIsValid = true;

		BadResourceException ex = resolvedAssessmentTest.getTestLookup().getBadResourceException();
		if(ex instanceof QtiXmlInterpretationException) {
			try {//try to log some informations
				QtiXmlInterpretationException exml = (QtiXmlInterpretationException)ex;
				logError(exml.getInterpretationFailureReason().toString(), null);
				for(QtiModelBuildingError err :exml.getQtiModelBuildingErrors()) {
					logError(err.toString(), null);
				}
			} catch (Exception e) {
				logError("", e);
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
		final Integer requestedLimit = deliveryOptions.getTemplateProcessingLimit();
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
		private AssessmentTestTimerFormItem timerEl;
		private ProgressBarItem scoreProgress, questionProgress;
		private FormLink endTestPartButton, closeTestButton, cancelTestButton, suspendTestButton, closeResultsButton;
		private FormLink restartTest;
		
		private String menuWidth;
		private boolean resultsVisible = false;
		private final QtiWorksStatus qtiWorksStatus = new QtiWorksStatus();
		
		public QtiWorksController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "at_run");
			initPreferences(ureq);
			initForm(ureq);
		}
		
		private void initPreferences(UserRequest ureq) {
			try {
				menuWidth = (String)ureq.getUserSession().getGuiPreferences()
						.get(this.getClass(), getMenuPrefsKey());
			} catch (Exception e) {
				logError("", e);
			}
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			mainForm.setMultipartEnabled(true);

			FormSubmit submit = uifactory.addFormSubmitButton("submit", formLayout);
			submit.setElementCssClass("o_sel_assessment_item_submit");
			qtiEl = new AssessmentTestFormItem("qtirun", submit);
			qtiEl.setResolvedAssessmentTest(resolvedAssessmentTest);
			formLayout.add("qtirun", qtiEl);
			
			timerEl = new AssessmentTestTimerFormItem("timer", qtiWorksStatus, qtiEl);
			formLayout.add("timer", timerEl);
			
			boolean showMenuTree = deliveryOptions.isShowMenu();
			
			qtiTreeEl = new AssessmentTreeFormItem("qtitree", qtiEl.getComponent(), submit);
			qtiTreeEl.setResolvedAssessmentTest(resolvedAssessmentTest);
			qtiTreeEl.setVisible(showMenuTree);
			formLayout.add("qtitree", qtiTreeEl);
			
			String endName = qtiEl.getComponent().hasMultipleTestParts()
					? "assessment.test.end.testPart" : "assessment.test.end.test";
			endTestPartButton = uifactory.addFormLink("endTest", endName, null, formLayout, Link.BUTTON);
			endTestPartButton.setForceOwnDirtyFormWarning(true);
			endTestPartButton.setElementCssClass("o_sel_end_testpart");
			endTestPartButton.setPrimary(true);
			endTestPartButton.setIconLeftCSS("o_icon o_icon-fw o_icon_qti_end_testpart");
			
			closeTestButton = uifactory.addFormLink("closeTest", "assessment.test.close.test", null, formLayout, Link.BUTTON);
			closeTestButton.setElementCssClass("o_sel_close_test");
			closeTestButton.setPrimary(true);
			closeTestButton.setIconLeftCSS("o_icon o_icon-fw o_icon_qti_close_test");
			
			if(deliveryOptions.isEnableCancel()) {
				cancelTestButton = uifactory.addFormLink("cancelTest", "cancel.test", null, formLayout, Link.BUTTON);
				cancelTestButton.setElementCssClass("o_sel_cancel_test");
				cancelTestButton.setIconLeftCSS("o_icon o_icon-fw o_icon_qti_cancel");
			}
			if(deliveryOptions.isEnableSuspend()) {
				suspendTestButton = uifactory.addFormLink("suspendTest", "suspend.test", null, formLayout, Link.BUTTON);
				suspendTestButton.setElementCssClass("o_sel_suspend_test");
				suspendTestButton.setIconLeftCSS("o_icon o_icon-fw o_icon_qti_suspend");
				suspendTestButton.setForceOwnDirtyFormWarning(true);
			}
			
			closeResultsButton = uifactory.addFormLink("closeResults", "assessment.test.close.results", null, formLayout, Link.BUTTON);
			closeResultsButton.setElementCssClass("o_sel_close_results");
			closeResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_qti_close_results");
			closeResultsButton.setPrimary(true);
			closeResultsButton.setVisible(false);
			
			restartTest = uifactory.addFormLink("restartTest", "assessment.test.restart.test", null, formLayout, Link.BUTTON);
			restartTest.setTitle("assessment.test.restart.test.explanation");
			restartTest.setElementCssClass("o_sel_restart_test");
			restartTest.setVisible(false);

			ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
			final ResourceLocator inputResourceLocator = 
	        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
			qtiEl.setResourceLocator(inputResourceLocator);
			qtiEl.setTestSessionController(testSessionController);
			qtiEl.setAssessmentObjectUri(qtiService.createAssessmentTestUri(fUnzippedDirRoot));
			qtiEl.setCandidateSessionContext(AssessmentTestDisplayController.this);
			qtiEl.setMapperUri(mapperUri);
			qtiEl.setRenderNavigation(!showMenuTree);
			qtiEl.setPersonalNotes(deliveryOptions.isPersonalNotes());
			qtiEl.setShowTitles(deliveryOptions.isShowTitles());
			qtiEl.setHideFeedbacks(deliveryOptions.isHideFeedbacks());
			qtiEl.setMaxScoreAssessmentItem(deliveryOptions.isDisplayMaxScoreItem());
			
			qtiTreeEl.setResourceLocator(inputResourceLocator);
			qtiTreeEl.setTestSessionController(testSessionController);
			qtiTreeEl.setAssessmentObjectUri(qtiService.createAssessmentTestUri(fUnzippedDirRoot));
			qtiTreeEl.setCandidateSessionContext(AssessmentTestDisplayController.this);
			qtiTreeEl.setMapperUri(mapperUri);
			qtiTreeEl.setShowTitles(deliveryOptions.isShowTitles());
			
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractAssumingSuccessful();
				layoutCont.contextPut("title", assessmentTest.getTitle());
				layoutCont.contextPut("qtiWorksStatus", qtiWorksStatus);
				
				String[] jss = new String[] {
						"js/jquery/ui/jquery-ui-1.11.4.custom.resize.min.js",
						"js/jquery/qti/jquery.qtiTimer.js",
						"js/jquery/qti/jquery.qtiAutosave.js"
				};
				JSAndCSSComponent js = new JSAndCSSComponent("js", jss, null);
				layoutCont.put("js", js);
				
				layoutCont.contextPut("displayScoreProgress", deliveryOptions.isDisplayScoreProgress());
				layoutCont.contextPut("displayQuestionProgress", deliveryOptions.isDisplayQuestionProgress());
				
				if(deliveryOptions.isDisplayScoreProgress()) {
					scoreProgress = uifactory.addProgressBar("scoreProgress", null, 100, 0, 0, "", formLayout);
					scoreProgress.setWidthInPercent(true);
					formLayout.add("", scoreProgress);
				}
				
				if(deliveryOptions.isDisplayQuestionProgress()) {
					questionProgress = uifactory.addProgressBar("questionProgress", null, 100, 0, 0, "", formLayout);
					questionProgress.setWidthInPercent(true);
					formLayout.add("questionProgress", questionProgress);
				}
			}
			
			flc.getFormItemComponent().addListener(this);
			if(StringHelper.containsNonWhitespace(menuWidth)) {
				flc.contextPut("menuWidth", menuWidth);
			}
			updateStatusAndResults(ureq);
		}
		
		public boolean willShowSomeAssessmentItemFeedbacks(TestPlanNode itemNode) {
			if(itemNode == null || testSessionController == null
					|| testSessionController.getTestSessionState().isExited()
					|| testSessionController.getTestSessionState().isEnded()) {
				return true;
			}
			
			return qtiEl.getComponent().willShowFeedbacks(itemNode);
		}
		
		/**
		 * 
		 * @return
		 */
		public boolean willShowSomeAssessmentTestFeedbacks() {
			if(testSessionController == null
					|| testSessionController.getTestSessionState().isExited()
					|| testSessionController.getTestSessionState().isEnded()) {
				return true;
			}
			
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			TestPlanNodeKey currentTestPartNodeKey = testSessionState.getCurrentTestPartKey();
			TestPlanNode currentTestPlanNode = testSessionState.getTestPlan().getNode(currentTestPartNodeKey);
			boolean hasReviewableItems = currentTestPlanNode.searchDescendants(TestNodeType.ASSESSMENT_ITEM_REF)
					.stream().anyMatch(itemNode
							-> itemNode.getEffectiveItemSessionControl().isAllowReview()
							|| itemNode.getEffectiveItemSessionControl().isShowFeedback());
			if(hasReviewableItems) {
				return true;
			}
			
			//Show 'atEnd' test feedback f there's only 1 testPart
			List<TestFeedback> testFeedbacks = qtiEl.getComponent().getAssessmentTest().getTestFeedbacks();
			for(TestFeedback testFeedback:testFeedbacks) {
				if(testFeedback.getTestFeedbackAccess() == TestFeedbackAccess.AT_END
						&& testFeedbackVisible(testFeedback, testSessionController.getTestSessionState())) {
					return true;
				}
			}
			
			return false;
		}
		
		/**
		 * 
		 * Check if the current test part will show some test part feedback,
		 * item feedback or item reviews.
		 * 
		 * @return
		 */
		public boolean willShowSomeTestPartFeedbacks() {
			if(testSessionController == null
					|| testSessionController.getTestSessionState().isExited()
					|| testSessionController.getTestSessionState().isEnded()) {
				return true;
			}
			
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			TestPlanNodeKey currentTestPartNodeKey = testSessionState.getCurrentTestPartKey();
			TestPlanNode currentTestPlanNode = testSessionState.getTestPlan().getNode(currentTestPartNodeKey);
			boolean hasReviewableItems = currentTestPlanNode.searchDescendants(TestNodeType.ASSESSMENT_ITEM_REF)
					.stream().anyMatch(itemNode
							-> itemNode.getEffectiveItemSessionControl().isAllowReview()
							|| itemNode.getEffectiveItemSessionControl().isShowFeedback());
			if(hasReviewableItems) {
				return true;
			}

			TestPart currentTestPart = testSessionController.getCurrentTestPart();
			List<TestFeedback> testFeedbacks = currentTestPart.getTestFeedbacks();
			for(TestFeedback testFeedback:testFeedbacks) {
				if(testFeedback.getTestFeedbackAccess() == TestFeedbackAccess.AT_END
						&& testFeedbackVisible(testFeedback, testSessionController.getTestSessionState())) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		protected Identifier getResponseIdentifierFromUniqueId(String uniqueId) {
			Interaction interaction = qtiEl.getInteractionOfResponseUniqueIdentifier(uniqueId);
			return interaction == null ? null : interaction.getResponseIdentifier();
		}
		
		protected boolean validateResponseIdentifierCommand(String cmd, TestPlanNodeKey nodeKey) {
			return qtiEl.validateCommand(cmd, nodeKey);
		}
		
		protected boolean validatePresentedItem(TestPlanNodeKey nodeKey) {
			return qtiEl.validateRequest(nodeKey);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			processResponse(ureq, qtiEl.getSubmitButton());
			updateStatusAndResults(ureq);
		}
		
		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if(source == flc.getFormItemComponent()) {
				if("saveLeftColWidth".equals(event.getCommand())) {
					String width = ureq.getParameter("newEmWidth");
					doSaveMenuWidth(ureq, width);
				}
			}

			super.event(ureq, source, event);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(source == resultCtrl) {
				fireEvent(ureq, event);
			}
			super.event(ureq, source, event);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(closeResultsButton == source) {
				doCloseResults(ureq);
			} else if(restartTest == source) {
				restartTest(ureq);
				return;// test will be completely reloaded
			} else if(!timeLimitBarrier(ureq) && !sessionReseted(ureq)) {
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
						QTIWorksAssessmentTestEvent qwate = (QTIWorksAssessmentTestEvent)event;
						if(qwate.getEvent() == QTIWorksAssessmentTestEvent.Event.tmpResponse) {
							processTemporaryResponse(ureq);
							return; // only save the response
						} else if(qwate.getEvent() == QTIWorksAssessmentTestEvent.Event.mark) {
							fireEvent(ureq, event);
							return; // only toggle, don't update
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
			
			updateStatusAndResults(ureq);
			mainForm.setDirtyMarking(false);
			mainForm.forceSubmittedAndValid();
		}
		
		@Override
		protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
			if(!"mark".equals(fe.getCommand()) && !"rubric".equals(fe.getCommand())
					&& !"tmpResponse".equals(fe.getCommand())) {
				super.propagateDirtinessToContainer(fiSrc, fe);
			}
		}

		@Override
		protected void fireResponse(UserRequest ureq, FormItem source,
				Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier, ResponseInput> fileResponseMap,
				String comment) {
			fireEvent(ureq, new QTIWorksAssessmentTestEvent(QTIWorksAssessmentTestEvent.Event.response, stringResponseMap, fileResponseMap, comment, source));
		}

		@Override
		protected void fireTemporaryResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap) {
			fireEvent(ureq, new QTIWorksAssessmentTestEvent(QTIWorksAssessmentTestEvent.Event.tmpResponse, stringResponseMap, null, null, null));
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
					&& testSessionState.getCurrentTestPartKey() != null
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
					&& testSessionState.getCurrentTestPartKey() != null
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
		
		private void doSaveMenuWidth(UserRequest ureq, String newMenuWidth) {
			this.menuWidth = newMenuWidth;
			if(StringHelper.containsNonWhitespace(newMenuWidth)) {
				flc.getFormItemComponent().getContext().put("menuWidth", newMenuWidth);
				if(testEntry != null) {
					UserSession usess = ureq.getUserSession();
					if (usess.isAuthenticated() && !usess.getRoles().isGuestOnly()) {
						usess.getGuiPreferences().commit(this.getClass(), getMenuPrefsKey(), newMenuWidth);
					}
				}
			}
		}
		
		private String getMenuPrefsKey() {
			return "menuWidth_" + testEntry.getKey();
		}
		
		public boolean isResultsVisible() {
			return resultsVisible;
		}
		
		public void extraTime() {
			timerEl.getComponent().setDirty(true);
		}
		
		/**
		 * Update the status and show the test results the test is at the end
		 * and the configuration allow it.
		 * 
		 * @param ureq
		 * @return true if the results are visible
		 */
		private boolean updateStatusAndResults(UserRequest ureq) {
			resultsVisible = false;
			if(testSessionController.getTestSessionState().isEnded()
					&& deliveryOptions.isShowAssessmentResultsOnFinish()
					&& deliveryOptions.getAssessmentResultsOptions() != null
					&& !deliveryOptions.getAssessmentResultsOptions().none()) {
				removeAsListenerAndDispose(resultCtrl);
				// show results in anonymous mode to hide the user info table - user knows who he is (same as on test start page)
				AssessmentTestSession cSession = AssessmentTestDisplayController.this.getCandidateSession(true);
				resultCtrl = new AssessmentResultController(ureq, getWindowControl(), assessedIdentity, true,
						cSession, fUnzippedDirRoot, mapperUri, null,
						deliveryOptions.getAssessmentResultsOptions(), false, true, true);
				listenTo(resultCtrl);
				flc.add("qtiResults", resultCtrl.getInitialFormItem());
				if(cSession.isAuthorMode() && cSession.getTestEntry().equals(cSession.getRepositoryEntry())) {
					restartTest.setVisible(true);
				}
				resultsVisible = true;
			}
			
			if(testSessionController.getTestSessionState().isEnded() || testSessionController.findNextEnterableTestPart() == null) {
				closeTestButton.setI18nKey("assessment.test.close.test");
			} else {
				closeTestButton.setI18nKey("assessment.test.close.testpart");
			}
			
			closeResultsButton.setVisible(resultsVisible && showCloseResults);
			updateQtiWorksStatus();
			return resultsVisible;
		}
		
		private void markSessionAsDeleted() {
			flc.contextPut("testSessionDeleted", Boolean.TRUE);
		}
		
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
		
		public double getCompletion() {
			if(numOfItems == 0) {
				return 0.0d;
			}
			return (double)numOfAnsweredItems / numOfItems;
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
		
		public boolean isAssessmentTestTimeLimit() {
			Long timeLimits = getAssessmentTestMaxTimeLimit();
			return timeLimits != null;
		}
		
		public String getAssessmentTestEndTime() {
			Long timeLimits = getAssessmentTestMaxTimeLimit();
			if(timeLimits != null) {
				long testDuration = getAssessmentTestDuration();
				if(testDuration < 0l) {
					testDuration = 0l;
				}
				
				Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
				calendar.add(Calendar.MILLISECOND, -(int)testDuration);
				calendar.add(Calendar.SECOND, timeLimits.intValue());
				return Formatter.getInstance(getLocale()).formatTimeShort(calendar.getTime());
			}
			return "";
		}
		
		/**
		 * @return The test duration in milliseconds
		 */
		public long getAssessmentTestDuration() {
			return AssessmentTestDisplayController.this.getAssessmentTestDuration();
		}
		
		/**
		 * 
		 * @return A duration in milliseconds
		 */
		public long getAssessmentTestMaximumTimeLimits() {
			long maxDuration = -1l;
			Long timeLimits = getAssessmentTestMaxTimeLimit();
			if(timeLimits != null) {
				maxDuration = timeLimits.longValue() * 1000;
			}
			return maxDuration;
		}
		
		public boolean isEnded() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			return candidateSessionContext.isTerminated() || testSessionState.isExited() || testSessionState.isEnded();
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
					&& testSessionState.getCurrentTestPartKey() != null
					&& testSessionController.mayEndCurrentTestPart();
		}
		
		public boolean mayCloseTest() {
			TestSessionState testSessionState = testSessionController.getTestSessionState();
			CandidateSessionContext candidateSessionContext = AssessmentTestDisplayController.this;
			if(!candidateSessionContext.isTerminated() && !testSessionState.isExited()
					&& testSessionState.getCurrentTestPartKey() != null
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