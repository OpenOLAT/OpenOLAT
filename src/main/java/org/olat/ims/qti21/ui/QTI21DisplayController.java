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
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObjectType;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
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
import uk.ac.ed.ph.jqtiplus.state.TestProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 08.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21DisplayController extends FormBasicController {
	
	private File fUnzippedDirRoot;
	
	private QTI21FormItem qtiEl;
	private TestSessionController testSessionController;
	
	private RequestTimestampContext requestTimestampContext = new RequestTimestampContext();
    private JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
	
	public QTI21DisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "run");
		
		FileResourceManager frm = FileResourceManager.getInstance();
		fUnzippedDirRoot = frm.unzipFileResource(entry.getOlatResource());
		
		testSessionController = enterSession();
		
		/* Handle immediate end of test session */
        if (testSessionController.getTestSessionState().isEnded()) {
            //candidateSessionFinisher.finishCandidateSession(candidateSession, assessmentResult);
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
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == qtiEl) {
			if(event instanceof QTI21FormEvent) {
				QTI21FormEvent qe = (QTI21FormEvent)event;
				switch(qe.getCommand()) {
					case "select-item": doSelectItem(qe.getSubCommand()); break;
					default: {}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectItem(String subCommand) {
		System.out.println(subCommand);
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
        
        return testSessionController;
	}
	
	
	public AssessmentResult computeTestAssessmentResult(final String candidateSessionId, final TestSessionController testSessionController) {
        String qtiWorksBaseUrl = null;//TODO
		final URI sessionIdentifierSourceId = URI.create(qtiWorksBaseUrl);
        final String sessionIdentifier = "testsession/" + candidateSessionId;
        return testSessionController.computeAssessmentResult(requestTimestampContext.getCurrentRequestTimestamp(),
        		sessionIdentifier, sessionIdentifierSourceId);
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
	        if (requestedLimit==null) {
	            /* Not specified, so use default */
	            return JqtiPlus.DEFAULT_TEMPLATE_PROCESSING_LIMIT;
	        }
	        final int requestedLimitIntValue = requestedLimit.intValue();
	        return requestedLimitIntValue > 0 ? requestedLimitIntValue : JqtiPlus.DEFAULT_TEMPLATE_PROCESSING_LIMIT;
	  }
}
