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
package org.olat.modules.bigbluebutton.ui;

import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.getSelectedTemplate;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.isWebcamLayoutAvailable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditBigBlueButtonMeetingController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] yesNoKeys = new String[] { "yes", "no" };

	private FormLink openCalLink;
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private TextElement welcomeEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private SingleSelection templateEl;
	private SingleSelection layoutEl;
	private SingleSelection serverEl;
	private SingleSelection recordEl;
	private SingleSelection publishingEl;
	private MultipleSelectionElement guestEl;
	private TextElement externalLinkEl;

	private final Mode mode;
	private final String subIdent;
	private final boolean editable;
	private final RepositoryEntry entry;
	private final boolean withSaveButtons;
	private final BusinessGroup businessGroup;
	private BigBlueButtonMeeting meeting;
	private final List<BigBlueButtonTemplatePermissions> permissions;
	private List<BigBlueButtonMeetingTemplate> templates;
	
	private BigBlueButtonMeetingsCalendarController calCtr;
	private CloseableModalController cmc;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			List<BigBlueButtonTemplatePermissions> permissions, Mode mode) {
		super(ureq, wControl);
		withSaveButtons = true;
		editable = true;
		
		this.mode = mode;
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		this.permissions = permissions;
		templates = bigBlueButtonManager.getTemplates();
		
		initForm(ureq);
	}
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			BigBlueButtonMeeting meeting, List<BigBlueButtonTemplatePermissions> permissions) {
		super(ureq, wControl);
		withSaveButtons = true;
		mode = (meeting.isPermanent() && bigBlueButtonModule.isPermanentMeetingEnabled()) ? Mode.permanent : Mode.dates;
		entry = meeting.getEntry();
		subIdent = meeting.getSubIdent();
		businessGroup = meeting.getBusinessGroup();
		this.meeting = meeting;
		this.permissions = permissions;
		editable = meeting.getServer() == null || meeting.isPermanent();
		templates = bigBlueButtonManager.getTemplates();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_bbb_edit_meeting");
		
		if(!editable) {
			setFormWarning("warning.meeting.started");
		}
		
		String name = meeting == null ? "" : meeting.getName();
		nameEl = uifactory.addTextElement("meeting.name", "meeting.name", 128, name, formLayout);
		nameEl.setElementCssClass("o_sel_bbb_edit_meeting_name");
		nameEl.setMandatory(true);
		nameEl.setEnabled(editable);
		if(editable && !StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		Identity creator = meeting == null ? getIdentity() : meeting.getCreator();
		if(creator != null) {
			String creatorFullName = userManager.getUserDisplayName(creator);
			uifactory.addStaticTextElement("meeting.creator", creatorFullName, formLayout);
		}
		
		String description = meeting == null ? "" : meeting.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);
		descriptionEl.setEnabled(editable);
		
		String welcome = meeting == null ? "" : meeting.getWelcome();
		welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", welcome, 8, 60, formLayout, getWindowControl());
		welcomeEl.setEnabled(editable);
		
		String presenter = meeting == null ? userManager.getUserDisplayName(getIdentity()) : meeting.getMainPresenter();
		mainPresenterEl = uifactory.addTextElement("meeting.main.presenter", "meeting.main.presenter", 128, presenter, formLayout);
		mainPresenterEl.setElementCssClass("o_sel_bbb_edit_meeting_presenter");
		mainPresenterEl.setEnabled(editable);
		
		Long selectedTemplateKey = meeting == null || meeting.getTemplate() == null
				? null : meeting.getTemplate().getKey();
		KeyValues templatesKeyValues = new KeyValues();
		for(BigBlueButtonMeetingTemplate template:templates) {
			if((template.isEnabled() && template.availableTo(permissions))
					|| template.getKey().equals(selectedTemplateKey)) {
				templatesKeyValues.add(KeyValues.entry(template.getKey().toString(), template.getName()));
			}
		}
		String[] templatesKeys = templatesKeyValues.keys();
		templateEl = uifactory.addDropdownSingleselect("meeting.template", "meeting.template", formLayout,
				templatesKeys, templatesKeyValues.values());
		templateEl.addActionListener(FormEvent.ONCHANGE);
		templateEl.setMandatory(true);
		templateEl.setElementCssClass("o_omit_margin");
		templateEl.setEnabled(editable);
		boolean templateSelected = false;
		if(selectedTemplateKey != null) {
			String currentTemplateId = selectedTemplateKey.toString();
			for(String key:templatesKeys) {
				if(currentTemplateId.equals(key)) {
					templateEl.select(currentTemplateId, true);
					templateSelected = true;
				}
			}
		}
		if(!templateSelected && templatesKeys.length > 0) {
			templateEl.select(templatesKeys[0], true);
		}
		
		KeyValues publishKeyValues = new KeyValues();
		publishKeyValues.add(KeyValues.entry(BigBlueButtonRecordingsPublishingEnum.auto.name(), translate("meeting.publishing.auto")));
		publishKeyValues.add(KeyValues.entry(BigBlueButtonRecordingsPublishingEnum.manual.name(), translate("meeting.publishing.manual")));
		publishingEl = uifactory.addRadiosVertical("meeting.publishing", formLayout, publishKeyValues.keys(), publishKeyValues.values());
		BigBlueButtonRecordingsPublishingEnum publish = meeting == null ? BigBlueButtonRecordingsPublishingEnum.auto :  meeting.getRecordingsPublishingEnum();
		publishingEl.select(publish.name(), true);
		publishingEl.setEnabled(editable);
		
		String[] yesNoValues = new String[] { translate("yes"), translate("no")  };
		recordEl = uifactory.addRadiosVertical("meeting.record", formLayout, yesNoKeys, yesNoValues);
		if(meeting == null || BigBlueButtonUIHelper.isRecord(meeting)) {
			recordEl.select(yesNoKeys[0], true);
		} else {
			recordEl.select(yesNoKeys[1], true);
		}

		KeyValues layoutKeyValues = new KeyValues();
		layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
		if(isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates))) {
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translate("layout.webcam")));
		}
		layoutEl = uifactory.addDropdownSingleselect("meeting.layout", "meeting.layout", formLayout,
				layoutKeyValues.keys(), layoutKeyValues.values());
		boolean layoutSelected = false;
		String selectedLayout = meeting == null ? BigBlueButtonMeetingLayoutEnum.standard.name() : meeting.getMeetingLayout().name();
		for(String layoutKey:layoutKeyValues.keys()) {
			if(layoutKey.equals(selectedLayout)) {
				layoutEl.select(layoutKey, true);
				layoutSelected = true;
			}
		}
		if(!layoutSelected) {
			layoutEl.select(BigBlueButtonMeetingLayoutEnum.standard.name(), true);
		}
		layoutEl.setVisible(layoutEl.getKeys().length > 1);
		
		String[] guestValues = new String[] { translate("meeting.guest.on") };
		guestEl = uifactory.addCheckboxesHorizontal("meeting.guest", formLayout, onKeys, guestValues);
		guestEl.setVisible(entry != null && entry.isGuests());
		guestEl.select(onKeys[0], meeting != null && meeting.isGuest());
		guestEl.setEnabled(editable);
		
		String externalLink = meeting == null ? CodeHelper.getForeverUniqueID() + "" : meeting.getReadableIdentifier();
		externalLinkEl = uifactory.addTextElement("meeting.external.users", 64, externalLink, formLayout);
		externalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
		externalLinkEl.setHelpTextKey("meeting.external.users.help", null);
		externalLinkEl.addActionListener(FormEvent.ONCHANGE);
		if (externalLink != null) {
			externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(externalLink)});			
		}
		
		openCalLink = uifactory.addFormLink("calendar.open", formLayout);
		openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
		BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, recordEl, templates);
		
		if(mode == Mode.dates) {
			Date startDate = meeting == null ? new Date() : meeting.getStartDate();
			startDateEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
			startDateEl.setMandatory(true);
			startDateEl.setDateChooserTimeEnabled(true);
			startDateEl.setEnabled(editable);
			
			String leadtime = meeting == null ? null : Long.toString(meeting.getLeadTime());
			leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
			leadTimeEl.setEnabled(editable);
			leadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			Date endDate = meeting == null ? null : meeting.getEndDate();
			if (endDate == null && startDate != null) {
				// set meeting time default to 1 hour
				Calendar calendar = Calendar.getInstance();
			    calendar.setTime(startDate);
			    calendar.add(Calendar.HOUR_OF_DAY, 1);
			    endDate = calendar.getTime();
			}
			endDateEl = uifactory.addDateChooser("meeting.end", "meeting.end", endDate, formLayout);
			endDateEl.setMandatory(true);
			endDateEl.setDefaultValue(startDateEl);
			endDateEl.setDateChooserTimeEnabled(true);
			endDateEl.setEnabled(editable);
			
			String followup = meeting == null ? null : Long.toString(meeting.getFollowupTime());
			followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
			followupTimeEl.setEnabled(editable);
		}
		
		KeyValues serverKeyValues = new KeyValues();
		serverKeyValues.add(KeyValues.entry("auto", translate("meeting.server.auto")));
		appendServerList(serverKeyValues);
		serverEl = uifactory.addDropdownSingleselect("meeting.server", formLayout, serverKeyValues.keys(), serverKeyValues.values());
		serverEl.setEnabled(editable);
		if(meeting != null && meeting.getServer() != null && serverKeyValues.containsKey(meeting.getServer().getKey().toString())) {
			serverEl.select(meeting.getServer().getKey().toString(), true);
		} else {
			serverEl.select(serverKeyValues.keys()[0], true);
		}
		
		if(withSaveButtons) {
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add("buttons", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
			if(editable) {
				uifactory.addFormSubmitButton("save", buttonLayout);
			}
		}
	}
	
	private void appendServerList(KeyValues serverKeyValues) {
		List<BigBlueButtonServer> servers = bigBlueButtonManager.getServers();
		for(BigBlueButtonServer server:servers) {
			if(!server.isEnabled()) continue;
			
			String name = server.getName();
			if(!StringHelper.containsNonWhitespace(name)) {
				name = BigBlueButtonUIHelper.getServerNameFromUrl(server.getUrl());
			}
			serverKeyValues.add(KeyValues.entry(server.getKey().toString(), name));
		}
	}
	
	private void doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calCtr);
		removeAsListenerAndDispose(cmc);

		// open calendar controller in modal. Not very nice to have stacked modal, but
		// still better than having no overview at all
		calCtr = new BigBlueButtonMeetingsCalendarController(ureq, getWindowControl());
		listenTo(calCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", calCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, meeting);

		if(mode == Mode.dates) {
			startDateEl.clearError();
			endDateEl.clearError();
			if(startDateEl.getDate() == null) {
				startDateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			if(endDateEl.getDate() == null) {
				endDateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(startDateEl.getDate() != null && endDateEl.getDate() != null) {
				Date start = startDateEl.getDate();
				Date end = endDateEl.getDate();
				if(end.before(start)) {
					endDateEl.setErrorKey("error.start.after.end", null);
					allOk &= false;
				}
				
				Date now = new Date();
				if(end.before(now)) {
					endDateEl.setErrorKey("error.end.past", null);
					allOk &= false;
				}
			}
			
			allOk &= BigBlueButtonUIHelper.validateTime(leadTimeEl, 15l);
			allOk &= BigBlueButtonUIHelper.validateTime(followupTimeEl, 15l);
		}
		
		templateEl.clearError();
		if(!templateEl.isOneSelected()) {
			templateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		serverEl.clearError();
		if(!serverEl.isOneSelected()) {
			serverEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		// dates ok
		if(allOk) {
			BigBlueButtonMeetingTemplate template = BigBlueButtonUIHelper.getSelectedTemplate(templateEl, templates);
			if(mode == Mode.permanent) {
				allOk &= BigBlueButtonUIHelper.validatePermanentSlot(templateEl, meeting, template);
			} else if(mode == Mode.dates) {
				allOk &= BigBlueButtonUIHelper.validateDuration(startDateEl, leadTimeEl, endDateEl, followupTimeEl, template);
				allOk &= BigBlueButtonUIHelper.validateSlot(startDateEl, leadTimeEl, endDateEl, followupTimeEl, meeting, template);
			}
		}
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (nameEl.getValue().contains("&")) {
			nameEl.setErrorKey("form.invalidchar.noamp", null);
			allOk &= false;
		}
		
		publishingEl.clearError();
		if(!publishingEl.isOneSelected()) {
			publishingEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calCtr);
		removeAsListenerAndDispose(cmc);
		calCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(templateEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, recordEl, templates);
			boolean webcamAvailable = isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if (externalLinkEl == source) {
			BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, meeting);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(meeting == null) {
			meeting = bigBlueButtonManager
					.createAndPersistMeeting(nameEl.getValue(), entry, subIdent, businessGroup, getIdentity());
		} else {
			meeting = bigBlueButtonManager.getMeeting(meeting);
			meeting.setName(nameEl.getValue());
		}
		
		meeting.setDescription(descriptionEl.getValue());
		meeting.setWelcome(welcomeEl.getValue());
		meeting.setMainPresenter(mainPresenterEl.getValue());
		BigBlueButtonMeetingTemplate template = getSelectedTemplate(templateEl, templates);
		meeting.setTemplate(template);
		
		if(template != null && template.isExternalUsersAllowed()
				&& externalLinkEl.isVisible() && StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
			meeting.setReadableIdentifier(externalLinkEl.getValue());
		} else {
			meeting.setReadableIdentifier(null);
		}
		
		meeting.setPermanent(mode == Mode.permanent);
		if(mode == Mode.permanent) {
			meeting.setStartDate(null);
			meeting.setEndDate(null);
			meeting.setLeadTime(0l);
			meeting.setFollowupTime(0l);
		} else {
			Date startDate = startDateEl.getDate();
			meeting.setStartDate(startDate);
			Date endDate = endDateEl.getDate();
			meeting.setEndDate(endDate);
			long leadTime = BigBlueButtonUIHelper.getLongOrZero(leadTimeEl);
			meeting.setLeadTime(leadTime);
			long followupTime = BigBlueButtonUIHelper.getLongOrZero(followupTimeEl);
			meeting.setFollowupTime(followupTime);
		}
		
		boolean guests = guestEl.isVisible() && guestEl.isAtLeastSelected(1);
		meeting.setGuest(guests);

		if(layoutEl.isVisible() && layoutEl.isOneSelected()) {
			BigBlueButtonMeetingLayoutEnum layout = BigBlueButtonMeetingLayoutEnum.secureValueOf(layoutEl.getSelectedKey());
			meeting.setMeetingLayout(layout);
		} else {
			meeting.setMeetingLayout(BigBlueButtonMeetingLayoutEnum.standard);
		}
		
		meeting.setRecordingsPublishingEnum(BigBlueButtonRecordingsPublishingEnum.valueOf(publishingEl.getSelectedKey()));
		if(recordEl.isVisible() && recordEl.isOneSelected()) {
			meeting.setRecord(Boolean.valueOf(yesNoKeys[0].equals(recordEl.getSelectedKey())));
		} else {
			meeting.setRecord(null);
		}
		
		String selectedServerKey = serverEl.getSelectedKey();
		if("auto".equals(selectedServerKey)) {
			meeting.setServer(null);
		} else if(StringHelper.isLong(selectedServerKey)) {
			BigBlueButtonServer server = bigBlueButtonManager.getServer(Long.valueOf(selectedServerKey));
			meeting.setServer(server);
		}

		meeting = bigBlueButtonManager.updateMeeting(meeting);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public enum Mode {
		permanent,
		dates
	}
}
