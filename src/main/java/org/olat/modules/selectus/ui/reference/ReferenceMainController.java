/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.PrivacyDisclaimerController;
import org.olat.modules.selectus.ui.RecruitingPositionSecurityCallbackForReviewer;
import org.olat.registration.DisclaimerFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceMainController extends MainLayoutBasicController implements Activateable2 {

	private static final String SR_ERROR_DISCLAIMER_CHECKBOX = "sr.error.disclaimer.checkbox";

	private final VelocityContainer layoutMainVC;
	
	private CloseableModalController cmc;
	private RefereeDisclaimerController disclaimerCtr;
	private PrivacyDisclaimerController privacyDisclaimerCtr;
	private ReferenceFinishController finishCtrl;
	private ReferenceTabbedController referenceCtrl;
	private ReferenceWarningController alreadySubmittedCtrl;
	private ConfirmOneTimeCodeController oneTimeCodeCtrl;
	
	private Position position;
	private Reference reference;
	private boolean passOtp = false;
	private RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public ReferenceMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		secCallback = RecruitingPositionSecurityCallbackForReviewer.withoutDocuments();
		
		String page = velocity_root + "/reference_reviewer_main.html";
		layoutMainVC = new VelocityContainer("vc_main", page, getTranslator(), this);
		layoutMainVC.put("content", new Panel("empty"));
		putInitialPanel(layoutMainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		String requestUri = ureq.getHttpReq().getRequestURI();
		String uriPrefix = ureq.getUriPrefix();
		if(uriPrefix.length() < requestUri.length()) {
			requestUri = requestUri.substring(uriPrefix.length());
		}
		
		if(requestUri.startsWith("/")) {
			requestUri = requestUri.substring(1, requestUri.length());
		}
		
		int slashFromRestUrlIndex = requestUri.indexOf('/');
		if(slashFromRestUrlIndex > 0 && slashFromRestUrlIndex + 1 < requestUri.length()) {
			requestUri = requestUri.substring(slashFromRestUrlIndex + 1, requestUri.length());
		}
		
		if(StringHelper.containsNonWhitespace(requestUri)) {
			reference = recruitingService.getReferenceBySubmissionUrl(requestUri);
			init(ureq);
			if(reference != null) {
				Position pos = loadPosition();
				if(pos == null) {
					doErrorMsg(ureq);
				} else {
					position = recruitingService.getPosition(pos.getKey());
				}
			} else {
				logError("Cannot found reference with URI: " + requestUri, null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == disclaimerCtr) {
			if(event == Event.DONE_EVENT) {
				if(disclaimerCtr.isAccepted()) {
					cmc.deactivate();
					doAcceptDisclaimer(ureq);
				} else {
					disclaimerError();
				}
			}
		} else if(source == privacyDisclaimerCtr) {
			if(event == Event.DONE_EVENT) {
				if(privacyDisclaimerCtr.isAccepted()) {
					cmc.deactivate();
					doAcceptPrivacyDisclaimer(ureq);
				} else {
					disclaimerError();
				}
			}
		} else if(source == referenceCtrl) {
			if(event == Event.DONE_EVENT) {
				doFinished(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				reference = recruitingService.getReferenceById(reference.getKey());
				doErrorMsg(ureq);
			}
		} else if(source == oneTimeCodeCtrl) {
			if(event == Event.DONE_EVENT) {
				acceptOneTimeCode();
				init(ureq);
			} else if(event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
				// Do nothing
			}
		}
		super.event(ureq, source, event);
	}
	
	private void init(UserRequest ureq) {
		if(reference == null) {
			doErrorMsg(ureq);
		} else if(needOtpConfirmation()) {
			doConfirmOneTimeCode(ureq);
		} else  {
			ReferenceStatus status = reference.getReferenceStatus();
			ReferenceRequestStatus requestStatus = reference.getRequestStatus();
			loadPosition();
			if(status == ReferenceStatus.submitted || status == ReferenceStatus.late || status == ReferenceStatus.deactivated
					|| requestStatus == ReferenceRequestStatus.declined
					|| position == null || position.getStatus() == null ||  PositionStatus.valueOf(position.getStatus()) == PositionStatus.closed) {
				doErrorMsg(ureq);
			} else {
				doReview(ureq);
			}
		}
	}
	
	private boolean needOtpConfirmation() {
		if(passOtp) return false;
		return recruitingModule.isReferenceOneTimeCodeEnabled();
	}
	
	private Position loadPosition() {
		if(reference == null) return null;
		if(reference.getApplication() == null) {
			List<Application> applications = recruitingService.getReferenceToApplicationsList(reference);
			if(!applications.isEmpty()) {
				position = applications.get(0).getPosition();
			}
		} else {
			position = reference.getApplication().getPosition();
		}
		return position;
	}
	
	private void disclaimerError() {
		String error = Util.createPackageTranslator(DisclaimerFormController.class, getLocale())
				.translate(SR_ERROR_DISCLAIMER_CHECKBOX);
		getWindowControl().setError(error);
	}
	
	private void doConfirmOneTimeCode(UserRequest ureq) {
		oneTimeCodeCtrl = new ConfirmOneTimeCodeController(ureq, getWindowControl(), reference);
		listenTo(oneTimeCodeCtrl);
		
		String title = translate("reference.otp.validation");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), oneTimeCodeCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void acceptOneTimeCode() {
		this.passOtp = true;
		cmc.deactivate();
		
		removeControllerListener(oneTimeCodeCtrl);
		removeControllerListener(cmc);
		oneTimeCodeCtrl = null;
		cmc = null;
	}
	
	private void doAcceptDisclaimer(UserRequest ureq) {
		reference = recruitingService.getReferenceById(reference.getKey());
		reference.setDisclaimer(true);
		reference = recruitingService.updateReference(reference);
		dbInstance.commit();

		doReview(ureq);
	}
	
	private void doAcceptPrivacyDisclaimer(UserRequest ureq) {
		reference = recruitingService.getReferenceById(reference.getKey());
		reference.setPrivacyDisclaimer(true);
		reference = recruitingService.updateReference(reference);
		dbInstance.commit();

		doReview(ureq);
	}
	
	private void doDisclaimer(UserRequest ureq) {
		removeAsListenerAndDispose(disclaimerCtr);
		removeAsListenerAndDispose(cmc);
		
		disclaimerCtr = new RefereeDisclaimerController(ureq, getWindowControl());
		listenTo(disclaimerCtr);
		
		String title = disclaimerCtr.disableLegend();
		title = FilterFactory.getHtmlTagAndDescapingFilter().filter(title);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerCtr.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doPrivacyDisclaimer(UserRequest ureq) {
		removeAsListenerAndDispose(privacyDisclaimerCtr);
		removeAsListenerAndDispose(cmc);

		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		String positionStaffMail = recruitingModule.getStaffMail(position, organisationSettings);
		privacyDisclaimerCtr = new PrivacyDisclaimerController(ureq, getWindowControl(), positionStaffMail);
		listenTo(privacyDisclaimerCtr);
		String title = privacyDisclaimerCtr.disableLegend();
		title = FilterFactory.getHtmlTagAndDescapingFilter().filter(title);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), privacyDisclaimerCtr.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doReview(UserRequest ureq) {
		Position pos = loadPosition();
		if(pos == null) return;
		
		position = recruitingService.getPosition(pos.getKey());
	
		Set<String> visibleDocs;
		Set<String> visibleFields;
		if(reference.getReferenceType() == ReferenceType.expert) {
			visibleDocs = position.getExpertRecommendationDocuments();
			visibleFields = position.getExpertRecommendationFields();
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			visibleDocs = position.getComparativeAssessmentExpertDocuments();
			visibleFields = position.getComparativeAssessmentExpertFields();
		} else {
			visibleDocs = position.getRefereeRecommendationDocuments();
			visibleFields = position.getRefereeRecommendationFields();
		}
		secCallback = RecruitingPositionSecurityCallbackForReviewer.valueOf(visibleFields, visibleDocs);
		
		if(!reference.isDisclaimer()) {
			doDisclaimer(ureq);
		}  else if(!reference.isPrivacyDisclaimer() && recruitingModule.isReferencePrivacyDisclaimer()) {
			doPrivacyDisclaimer(ureq);
		} else {
			referenceCtrl = new ReferenceTabbedController(ureq, getWindowControl(), position, reference, secCallback);
			listenTo(referenceCtrl);
			layoutMainVC.put("content", referenceCtrl.getInitialComponent());
		}
	}
	
	private void doFinished(UserRequest ureq) {
		finishCtrl = new ReferenceFinishController(ureq, getWindowControl(), reference);
		listenTo(finishCtrl);
		layoutMainVC.put("content", finishCtrl.getInitialComponent());
	}
	
	private void doErrorMsg(UserRequest ureq) {
		alreadySubmittedCtrl = new ReferenceWarningController(ureq, getWindowControl(), position, reference);
		listenTo(alreadySubmittedCtrl);
		layoutMainVC.put("content", alreadySubmittedCtrl.getInitialComponent());
	}
}
