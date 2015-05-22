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
import java.util.Date;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.model.CandidateEvent;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.ui.components.AssessmentItemFormItem;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiPlus;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.ItemProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
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
	private final AssessmentItemRef itemRef;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private CandidateEvent lastEvent;
	private Date currentRequestTimestamp;
	private UserTestSession candidateSession;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private JqtiExtensionManager jqtiExtensionManager;
	
	public AssessmentItemDisplayController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, File fUnzippedDirRoot) {
		super(ureq, wControl);
		
		this.itemRef = itemRef;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		currentRequestTimestamp = ureq.getRequestTimestamp();
		mapperUri = registerCacheableMapper(null, UUID.randomUUID().toString(), new ResourcesMapper());
		
		itemSessionController = enterSession(ureq);
		
		if (itemSessionController.getItemSessionState().isEnded()) {
			mainVC = createVelocityContainer("end");
		} else {
			mainVC = createVelocityContainer("run");
        	initQtiWorks(ureq);
		}
		putInitialPanel(mainVC);
	}
	
	private void initQtiWorks(UserRequest ureq) {
		String filename = itemRef.getHref().toString();
		qtiWorksCtrl = new QtiWorksController(ureq, getWindowControl(), filename);
    	listenTo(qtiWorksCtrl);
    	mainVC.put("qtirun", qtiWorksCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public UserTestSession getCandidateSession() {
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
		currentRequestTimestamp = ureq.getRequestTimestamp();
		//
	}
	
	private void processQTIEvent(UserRequest ureq, QTIWorksEvent qe) {
		currentRequestTimestamp = ureq.getRequestTimestamp();
		
	}
	
	private ItemSessionController enterSession(UserRequest ureq /*, final UserTestSession candidateSession */) {
        /* Set up listener to record any notifications */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);

        /* Create fresh JQTI+ state Object and try to create controller */
        final ItemSessionController itemSessionController = createNewItemSessionStateAndController(notificationRecorder);
        if (itemSessionController==null) {
        	logError("", null);
            return null;//handleExplosion(null, candidateSession);
        }

        /* Try to Initialise JQTI+ state */
        final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
        try {
            final Date timestamp = ureq.getRequestTimestamp();
            itemSessionController.initialize(timestamp);
            itemSessionController.performTemplateProcessing(timestamp);
            itemSessionController.enterItem(timestamp);
        }
        catch (final RuntimeException e) {
        	logError("", e);
            return null;//handleExplosion(null, candidateSession);
        }

        /* Record and log entry event */
        final CandidateEvent candidateEvent = qtiService.recordCandidateItemEvent(candidateSession, CandidateItemEventType.ENTER, itemSessionState, notificationRecorder);
        //candidateAuditLogger.logCandidateEvent(candidateEvent);
        lastEvent = candidateEvent;

        /* Record current result state */
        //final AssessmentResult assessmentResult = computeAndRecordItemAssessmentResult(candidateSession, itemSessionController);

        /* Handle immediate end of session */
        if (itemSessionState.isEnded()) {
            //candidateSessionFinisher.finishCandidateSession(candidateSession, assessmentResult);
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
        final ItemSessionController result = new ItemSessionController(jqtiExtensionManager,
                itemSessionControllerSettings, itemProcessingMap, itemSessionState);
        if (notificationRecorder != null) {
            result.addNotificationListener(notificationRecorder);
        }
        return result;
    }
    
    public ItemProcessingMap getItemProcessingMap() {
        ItemProcessingMap result = new ItemProcessingInitializer(resolvedAssessmentItem, true).initialize();
        return result;
    }
    
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
	private class QtiWorksController extends FormBasicController {
		
		private AssessmentItemFormItem qtiEl;
		private String filename;
		
		public QtiWorksController(UserRequest ureq, WindowControl wControl, String filename) {
			super(ureq, wControl, LAYOUT_BAREBONE);
			this.filename = filename;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			mainForm.setStandaloneRendering(true);
			mainForm.setMultipartEnabled(true, Integer.MAX_VALUE);
			
			qtiEl = new AssessmentItemFormItem("qtirun");
			formLayout.add("qtirun", qtiEl);

			ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
			final ResourceLocator inputResourceLocator = 
	        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
			qtiEl.setResourceLocator(inputResourceLocator);
			qtiEl.setItemSessionController(itemSessionController);
			
			
			File manifestPath = new File(fUnzippedDirRoot, filename);
			
			qtiEl.setAssessmentObjectUri(manifestPath.toURI());
			qtiEl.setCandidateSessionContext(AssessmentItemDisplayController.this);
			qtiEl.setMapperUri(mapperUri);
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
				if(event instanceof QTIWorksEvent) {
					QTIWorksEvent qe = (QTIWorksEvent)event;
					processQTIEvent(ureq, qe);
				}
			}
			super.formInnerEvent(ureq, source, event);
		}
	}
}
