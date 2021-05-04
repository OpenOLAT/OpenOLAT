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
package org.olat.modules.adobeconnect.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectMeetingPermission;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.ui.AdobeConnectContentTableModel.ACContentsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectMeetingController extends FormBasicController implements GenericEventListener {
	
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	private AdobeConnectMeeting meeting;
	
	private boolean registered;
	private boolean meetingCreated;
	private boolean validMeeting;
	private final boolean guestsAccess;
	private final boolean userMeetingsDates;
	private final boolean moderatorStartMeeting;
	private final OLATResourceable meetingOres;

	private int counter;
	private Link joinButton;
	private Link configureMeetingButton;
	
	private FormLink registerButton;
	private FormLink createMeetingButton;
	private FormLink sharedDocumentButton;
	private FlexiTableElement contentTableEl;
	private AdobeConnectContentTableModel contentModel;
	
	private CloseableModalController cmc;
	private AdobeConnectShareDocumentsController shareDocumentsCtrl;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public AdobeConnectMeetingController(UserRequest ureq, WindowControl wControl,
			AdobeConnectMeeting meeting, AdobeConnectMeetingDefaultConfiguration configuration,
			boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl, "meeting");
		this.meeting = meeting;
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.administrator = administrator;
		meetingOres = OresHelper.createOLATResourceableInstance(AdobeConnectMeeting.class.getSimpleName(), meeting.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		
		meetingCreated = StringHelper.containsNonWhitespace(meeting.getScoId());
		validMeeting = adobeConnectModule.getBaseUrl().equals(meeting.getEnvName());
		userMeetingsDates = configuration.isUseMeetingDates();
		moderatorStartMeeting = configuration.isModeratorStartMeeting();
		guestsAccess = configuration.isAllowGuestAccess();
		
		initForm(ureq);
		
		if(validMeeting && meetingCreated) {
			AdobeConnectErrors errors = new AdobeConnectErrors();
			registered = adobeConnectManager.isRegistered(meeting, getIdentity(), getPermission(), errors);
			loadModel();
			if(errors.hasErrors()) {
				getWindowControl().setWarning(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
			}
		}
		updateButtonsAndStatus();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean ended = isEnded();
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", meeting.getName());
			if(StringHelper.containsNonWhitespace(meeting.getDescription())) {
				layoutCont.contextPut("description", meeting.getDescription());
			}
			if(meeting.getStartDate() != null) {
				String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
				layoutCont.contextPut("start", start);
			}
			if(meeting.getEndDate() != null) {
				String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
				layoutCont.contextPut("end", end);
			}
		}

		registerButton = uifactory.addFormLink("meeting.register.button", flc, Link.BUTTON);
		registerButton.setVisible(!ended);
		
		sharedDocumentButton = uifactory.addFormLink("meeting.share.documents", flc, Link.BUTTON);
		sharedDocumentButton.setVisible(administrator || moderator);
		
		createMeetingButton = uifactory.addFormLink("meeting.create.button", flc, Link.BUTTON);
		createMeetingButton.setVisible(!StringHelper.containsNonWhitespace(meeting.getScoId()));
		
		configureMeetingButton = LinkFactory.createButton("meeting.configure.button", flc.getFormItemComponent(), this);
		configureMeetingButton.setVisible(moderator || administrator);
		configureMeetingButton.setTarget("_blank");
		
		joinButton = LinkFactory.createButtonLarge("meeting.join.button", flc.getFormItemComponent(), this);
		joinButton.setTarget("_blank");
		joinButton.setVisible(!ended || moderator || administrator);

		initContent(formLayout);
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isValidDates() {
		if(!userMeetingsDates) {
			return true;
		}
		Date now = new Date();
		Date start = meeting.getStartWithLeadTime();
		Date end = meeting.getEndWithFollowupTime();
		
		if(start != null && start.compareTo(now) >= 0) {
			return false;
		}
		if(end != null && end.compareTo(now) <= 0) {
			return false;
		}
		return true;
	}
	
	private void reloadButtonsAndStatus() {
		meeting = adobeConnectManager.getMeeting(meeting);
		updateButtonsAndStatus();
	}
	
	private void updateButtonsAndStatus() {
		boolean invalidProvider = meeting.getEnvName() != null && !meeting.getEnvName().equals(adobeConnectModule.getBaseUrl());
		boolean meetingsExists = StringHelper.containsNonWhitespace(meeting.getScoId());
		boolean isEnded = isEnded();

		meetingCreated = StringHelper.containsNonWhitespace(meeting.getScoId());
		validMeeting = adobeConnectModule.getBaseUrl().equals(meeting.getEnvName());
		
		flc.contextPut("invalidProvider", Boolean.valueOf(invalidProvider));
		flc.contextPut("meetingsExists", Boolean.valueOf(meetingsExists));
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		
		boolean accessible = !isEnded() || administrator || moderator;
		boolean canCreate = !StringHelper.containsNonWhitespace(meeting.getScoId())
				&& (administrator || moderator);
		createMeetingButton.setVisible(canCreate);
		
		if(canCreate) {
			registerButton.setVisible(false);
			joinButton.setVisible(false);
		} else if(moderator || administrator) {
			registerButton.setVisible(false);
			
			joinButton.setVisible(accessible);
			joinButton.setEnabled(!readOnly && validMeeting);
			
			if(!meeting.isOpened() && moderatorStartMeeting) {
				joinButton.setCustomDisplayText(translate("meeting.start.button"));
			} else if(isValidDates()) {
				joinButton.setCustomDisplayText(translate("meeting.join.button"));
			} else {
				joinButton.setCustomDisplayText(translate("meeting.go.button"));
			}
		} else {
			registerButton.setVisible(accessible && !registered && !readOnly && validMeeting);
			boolean validDates = isValidDates();

			joinButton.setVisible(accessible && registered);
			if(!meeting.isOpened() && moderatorStartMeeting) {
				joinButton.setEnabled(false);
			} else {
				joinButton.setEnabled(!readOnly && validMeeting && validDates);
			}

			if(validDates && !meeting.isOpened() && moderatorStartMeeting) {
				flc.contextPut("notStarted", Boolean.TRUE);	
			} else if(validDates || isEnded) {
				flc.contextPut("notStarted", Boolean.FALSE);
			} else {
				flc.contextPut("notStarted", Boolean.TRUE);
			}
		}
	}
	
	protected void initContent(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.icon, new AdobeConnectIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.dateBegin, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.resource));

		contentModel = new AdobeConnectContentTableModel(columnsModel, getLocale());
		contentTableEl = uifactory.addTableElement(getWindowControl(), "meetingContents", contentModel, 24, false, getTranslator(), formLayout);
		contentTableEl.setCustomizeColumns(false);
		contentTableEl.setNumOfRowsEnabled(false);
		contentTableEl.setEmptyTableMessageKey("no.shared.contents");
	}
	
	private void loadModel() {
		List<String> sharedDocumentIds = meeting.getSharedDocumentIds();
		
		if(!sharedDocumentIds.isEmpty() || administrator || moderator) {
			AdobeConnectErrors error = new AdobeConnectErrors();
			List<AdobeConnectSco> scos = adobeConnectManager.getRecordings(meeting, error);
			List<AdobeConnectContentRow> rows = new ArrayList<>(scos.size());
			for(AdobeConnectSco sco:scos) {
				if(sharedDocumentIds.contains(sco.getScoId())) {
					AdobeConnectContentRow row = new AdobeConnectContentRow(sco);
					if(registered) {
						MediaResource resource = new AdobeConnectContentRedirectResource(getIdentity(), sco);
						DownloadLink openLink = uifactory.addDownloadLink("open-" + (++counter), translate("content.open"), null, resource, contentTableEl);
						row.setOpenLink(openLink);
					}
					rows.add(row);
				}
			}
			contentModel.setObjects(rows);
			contentTableEl.reset(true, true, true);
			contentTableEl.setVisible(!rows.isEmpty() || administrator || moderator);
			sharedDocumentButton.setVisible(!scos.isEmpty() && (administrator || moderator));
			flc.contextPut("notRegistered", Boolean.valueOf(!registered));
		} else {
			contentTableEl.setVisible(false);
			sharedDocumentButton.setVisible(false);
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, meetingOres);
	}

	@Override
	public void event(Event event) {
		if(event instanceof AdobeConnectEvent) {
			AdobeConnectEvent ace = (AdobeConnectEvent)event;
			if(ace.getMeetingKey() != null && ace.getMeetingKey().equals(meeting.getKey())) {
				reloadButtonsAndStatus();
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(joinButton == source) {
			doJoin(ureq);
		} else if(configureMeetingButton == source) {
			doConfigureMeeting(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(shareDocumentsCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				meeting = shareDocumentsCtrl.getMeeting();
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeControllerListener(shareDocumentsCtrl);
		removeControllerListener(cmc);
		shareDocumentsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(registerButton == source) {
			doRegister();
		} else if(sharedDocumentButton == source) {
			doShareDocuments(ureq);
		} else if(createMeetingButton == source) {
			doCreateMeeting();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doCreateMeeting() {
		meeting = adobeConnectManager.getMeeting(meeting);
		AdobeConnectErrors errors = new AdobeConnectErrors();
		meeting = adobeConnectManager.createAdobeMeeting(meeting, getLocale(), guestsAccess, errors);
		updateButtonsAndStatus();
		
		if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			AdobeConnectEvent createEvent = new AdobeConnectEvent(AdobeConnectEvent.CREATE_MEETING, meeting.getKey(), getIdentity().getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(createEvent, meetingOres);
		}
	}
	
	private void doConfigureMeeting(UserRequest ureq) {
		meeting = adobeConnectManager.getMeeting(meeting);
		AdobeConnectErrors errors = new AdobeConnectErrors();
		if(!StringHelper.containsNonWhitespace(meeting.getScoId())) {
			meeting = adobeConnectManager.createAdobeMeeting(meeting, getLocale(), guestsAccess, errors);
		}
		updateButtonsAndStatus();
		if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else if(administrator || moderator) {
			String meetingUrl = adobeConnectManager.join(meeting, getIdentity(), (administrator || moderator), errors);
			redirectToAdobeConnect(ureq, meetingUrl, errors);
		} 
	}
	
	private void doJoin(UserRequest ureq) {
		meeting = adobeConnectManager.getMeeting(meeting);
		if(meeting == null) {
			showWarning("warning.no.meeting");
			fireEvent(ureq, Event.BACK_EVENT);
			return;
		}
		
		String meetingUrl = null;
		AdobeConnectErrors errors = new AdobeConnectErrors();
		if(moderator || administrator) {
			meetingUrl = adobeConnectManager.open(meeting, getIdentity(), errors);
			AdobeConnectEvent openEvent = new AdobeConnectEvent(AdobeConnectEvent.OPEN_MEETING, meeting.getKey(), getIdentity().getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(openEvent, meetingOres);
		} else if(meeting.isOpened() || !moderatorStartMeeting) {
			meetingUrl = adobeConnectManager.join(meeting, getIdentity(), (administrator || moderator), errors);
		}
		redirectToAdobeConnect(ureq, meetingUrl, errors);
	}
	
	private void redirectToAdobeConnect(UserRequest ureq, String meetingUrl, AdobeConnectErrors errors) {
		if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else if(StringHelper.containsNonWhitespace(meetingUrl)) {
			MediaResource redirect = new RedirectMediaResource(meetingUrl);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		} else {
			showWarning("warning.no.access");
		}
	}
	
	private void doRegister() {
		AdobeConnectErrors errors = new AdobeConnectErrors();
		registered = adobeConnectManager.registerFor(meeting, getIdentity(), getPermission(), errors);
		if(registered) {
			showInfo("meeting.successfully.registered");
			reloadButtonsAndStatus();
		} else if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			showInfo("meeting.registration.failed");
		}
	}
	
	private void doShareDocuments(UserRequest ureq) {
		shareDocumentsCtrl = new AdobeConnectShareDocumentsController(ureq, getWindowControl(), meeting);
		listenTo(shareDocumentsCtrl);
		
		String title = translate("meeting.share.documents.of", new String[] { StringHelper.escapeHtml(meeting.getName() )});
		cmc = new CloseableModalController(getWindowControl(), "close", shareDocumentsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private AdobeConnectMeetingPermission getPermission() {
		AdobeConnectMeetingPermission permission;
		if(administrator || moderator) {
			permission = AdobeConnectMeetingPermission.host;
		} else {
			permission = AdobeConnectMeetingPermission.view;
		}
		return permission;
	}
}
