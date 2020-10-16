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

import java.net.URI;
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

		KeyValues layoutKeyValues = new KeyValues();
		layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
		if(isWebcamLayoutAvailable()) {
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
		updateTemplateInformations();
		
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
		
		if(withSaveButtons) {
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add("buttons", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
			if(editable) {
				uifactory.addFormSubmitButton("save", buttonLayout);
			}
		}
	}
	
	private boolean isWebcamLayoutAvailable() {
		if(!templateEl.isOneSelected()) {
			return true;
		}
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
		return template != null && (template.getWebcamsOnlyForModerator() == null || !template.getWebcamsOnlyForModerator().booleanValue());
	}
	
	private void updateTemplateInformations() {
		templateEl.setExampleKey(null, null);
		if(templateEl.isOneSelected()) {
			BigBlueButtonMeetingTemplate template = getSelectedTemplate();
			if(template != null && template.getMaxParticipants() != null) {
				Integer maxConcurrentInt = template.getMaxConcurrentMeetings();
				String maxConcurrent = (maxConcurrentInt == null ? " ∞" : maxConcurrentInt.toString());
				String[] args = new String[] { template.getMaxParticipants().toString(), maxConcurrent};				
				if(template.getWebcamsOnlyForModerator() != null && template.getWebcamsOnlyForModerator().booleanValue()) {
					templateEl.setExampleKey("template.explain.max.participants.with.webcams.mod", args);
				} else {
					templateEl.setExampleKey("template.explain.max.participants", args);
				}
			}
			boolean visible = template != null && template.isExternalUsersAllowed();
			externalLinkEl.setVisible(visible);
			if(visible && !StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
				externalLinkEl.setValue(Long.toString(CodeHelper.getForeverUniqueID()));
			}
		} else {
			externalLinkEl.setVisible(false);
		}
	}
	
	private void updateLayoutSelection() {
		boolean webcamAvailable = isWebcamLayoutAvailable();
		if(webcamAvailable && layoutEl.getKeys().length == 1) {
			KeyValues layoutKeyValues = new KeyValues();
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translate("layout.webcam")));
			layoutEl.setKeysAndValues(layoutKeyValues.keys(), layoutKeyValues.values(), null);
		} else if(!webcamAvailable && layoutEl.getKeys().length > 1) {
			layoutEl.select(BigBlueButtonMeetingLayoutEnum.standard.name(), true);
			
			KeyValues layoutKeyValues = new KeyValues();
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
			layoutEl.setKeysAndValues(layoutKeyValues.keys(), layoutKeyValues.values(), null);
		}
		
		layoutEl.setVisible(layoutEl.getKeys().length > 1);
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
		
		allOk &= validateReadableIdentifier();

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
			
			allOk &= validateTime(leadTimeEl, 15l);
			allOk &= validateTime(followupTimeEl, 15l);
		}
		
		templateEl.clearError();
		if(!templateEl.isOneSelected()) {
			endDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		// dates ok
		if(allOk) {
			if(mode == Mode.permanent) {
				allOk &= validatePermanentSlot();
			} else if(mode == Mode.dates) {
				allOk &= validateDuration();
				allOk &= validateSlot();
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
	
	private boolean validateReadableIdentifier() {
		boolean allOk = true;
		
		externalLinkEl.clearError();
		if(externalLinkEl.isVisible()) {
			String identifier = externalLinkEl.getValue();
			if (StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
				if(identifier.length() > 64) {
					externalLinkEl.setErrorKey("form.error.toolong", new String[] { "64" });
					allOk &= false;
				} else if(bigBlueButtonManager.isIdentifierInUse(identifier, meeting)) {
					externalLinkEl.setErrorKey("error.identifier.in.use", null);
					allOk &= false;
				} else {
					try {
						URI uri = new URI(BigBlueButtonDispatcher.getMeetingUrl(identifier));
						uri.normalize();
					} catch(Exception e) {
						externalLinkEl.setErrorKey("error.identifier.url.not.valid", new String[] { e.getMessage() });
						allOk &= false;
					}
				}
				externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(identifier)});			
			} else {
				externalLinkEl.setExampleKey(null, null);			
			}
		}

		return allOk;
	}
	
	private boolean validateTime(TextElement el, long maxValue) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			if(!StringHelper.isLong(el.getValue())) {
				el.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			} else if(Long.parseLong(el.getValue()) > maxValue) {
				el.setErrorKey("error.too.long.time", new String[] { Long.toString(maxValue) });
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateDuration() {
		boolean allOk = true;
		
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
		Date start = startDateEl.getDate();
		Date end = endDateEl.getDate();
		if(template != null && template.getMaxDuration() != null && start != null && end != null) {
			// all calculation in milli-seconds
			long realStart = start.getTime() - (60 * 1000 * getLeadTime());
			long realEnd = end.getTime() + (60 * 1000 * getFollowupTime());
			long duration = realEnd - realStart;
			long maxDuration  = (60 * 1000 * template.getMaxDuration());
			if(duration > maxDuration) {
				endDateEl.setErrorKey("error.duration", new String[] { template.getMaxDuration().toString() });
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateSlot() {
		boolean allOk = true;
		
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
		boolean slotFree = bigBlueButtonManager.isSlotAvailable(meeting, template,
				startDateEl.getDate(), getLeadTime(), endDateEl.getDate(), getFollowupTime());
		if(!slotFree) {
			startDateEl.setErrorKey("server.overloaded", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validatePermanentSlot() {
		boolean allOk = true;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 12);
		Date endDate = cal.getTime();
		
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
		boolean slotFree = bigBlueButtonManager.isSlotAvailable(meeting, template,
				new Date(), 0, endDate, 0);
		if(!slotFree) {
			templateEl.setErrorKey("server.overloaded", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private BigBlueButtonMeetingTemplate getSelectedTemplate() {
		String selectedTemplateId = templateEl.getSelectedKey();
		return templates.stream()
					.filter(tpl -> selectedTemplateId.equals(tpl.getKey().toString()))
					.findFirst()
					.orElse(null);
	}
	
	public long getLeadTime() {
		long leadTime = 0;
		if(leadTimeEl.isVisible() && StringHelper.isLong(leadTimeEl.getValue())) {
			leadTime = Long.valueOf(leadTimeEl.getValue());
		}
		return leadTime;
	}
	
	private long getFollowupTime() {
		long followupTime = 0;
		if(followupTimeEl.isVisible() && StringHelper.isLong(followupTimeEl.getValue())) {
			followupTime = Long.valueOf(followupTimeEl.getValue());
		}
		return followupTime;
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
			updateTemplateInformations();
			updateLayoutSelection();
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if (externalLinkEl == source) {
			validateReadableIdentifier();
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
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
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
			long leadTime = getLeadTime();
			meeting.setLeadTime(leadTime);
			long followupTime = getFollowupTime();
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
