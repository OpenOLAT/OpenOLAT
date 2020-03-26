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
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRoles;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditBigBlueButtonMeetingController extends FormBasicController {
	
	private static final String[] permKeys = new String[] { "on" };
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement welcomeEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private SingleSelection templateEl;
	private MultipleSelectionElement permanentEl;
	
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private BigBlueButtonMeeting meeting;
	private final List<BigBlueButtonRoles> editionRoles;
	private List<BigBlueButtonMeetingTemplate> templates;
	
	private FormLink openCalLink;
	private BigBlueButtonMeetingsCalendarController calCtr;
	private CloseableModalController cmc;
	
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, List<BigBlueButtonRoles> editionRoles) {
		super(ureq, wControl);
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		this.editionRoles = editionRoles;
		templates = bigBlueButtonManager.getTemplates();
		
		initForm(ureq);
		updateUI();
	}
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			BigBlueButtonMeeting meeting, List<BigBlueButtonRoles> editionRoles) {
		super(ureq, wControl);
		entry = meeting.getEntry();
		subIdent = meeting.getSubIdent();
		businessGroup = meeting.getBusinessGroup();
		this.meeting = meeting;
		this.editionRoles = editionRoles;
		templates = bigBlueButtonManager.getTemplates();
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = meeting == null ? "" : meeting.getName();
		nameEl = uifactory.addTextElement("meeting.name", "meeting.name", 128, name, formLayout);
		nameEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		String description = meeting == null ? "" : meeting.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);

		String welcome = meeting == null ? "" : meeting.getWelcome();
		welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", welcome, 8, 60, formLayout, getWindowControl());
		
		Long selectedTemplateKey = meeting == null || meeting.getTemplate() == null
				? null : meeting.getTemplate().getKey();
		KeyValues templatesKeyValues = new KeyValues();
		for(BigBlueButtonMeetingTemplate template:templates) {
			if((template.isEnabled() && template.availableTo(editionRoles))
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
		openCalLink = uifactory.addFormLink("calendar.open", formLayout);
		openCalLink.addActionListener(FormEvent.ONCLICK);
		openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
		updateTemplateInformations();
		
		String[] permValues = new String[] { translate("meeting.permanent.on") };
		permanentEl = uifactory.addCheckboxesHorizontal("meeting.permanent", formLayout, permKeys, permValues);
		permanentEl.addActionListener(FormEvent.ONCHANGE);
		boolean permanent = meeting != null && bigBlueButtonModule.isPermanentMeetingEnabled() && meeting.isPermanent();
		permanentEl.select(permKeys[0], permanent);
		permanentEl.setVisible(bigBlueButtonModule.isPermanentMeetingEnabled());

		Date startDate = meeting == null ? new Date() : meeting.getStartDate();
		startDateEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
		startDateEl.setMandatory(!permanent);
		startDateEl.setDateChooserTimeEnabled(true);
		
		String leadtime = meeting == null ? null : Long.toString(meeting.getLeadTime());
		leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
		
		Date endDate = meeting == null ? null : meeting.getEndDate();
		if (endDate == null && startDate != null) {
			// set meeting time default to 1 hour
			Calendar calendar = Calendar.getInstance();
		    calendar.setTime(startDate);
		    calendar.add(Calendar.HOUR_OF_DAY, 1);
		    endDate = calendar.getTime();
		}
		endDateEl = uifactory.addDateChooser("meeting.end", "meeting.end", endDate, formLayout);
		endDateEl.setMandatory(!permanent);
		endDateEl.setDefaultValue(startDateEl);
		endDateEl.setDateChooserTimeEnabled(true);
		
		String followup = meeting == null ? null : Long.toString(meeting.getFollowupTime());
		followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void updateUI() {
		boolean permanent = permanentEl.isAtLeastSelected(1);
		startDateEl.setVisible(!permanent);
		leadTimeEl.setVisible(!permanent);
		endDateEl.setVisible(!permanent);
		followupTimeEl.setVisible(!permanent);
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
		}
	}
	
	
	private void doOpenCalendar(UserRequest ureq) {
		// cleanup first
		if (calCtr != null) {
			removeAsListenerAndDispose(calCtr);
		}
		if (cmc != null) {
			removeAsListenerAndDispose(cmc);
		}
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		startDateEl.clearError();
		endDateEl.clearError();
		if(!permanentEl.isVisible() || !permanentEl.isAtLeastSelected(1)) {
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
		}
		
		allOk &= validateTime(leadTimeEl, 15l);
		allOk &= validateTime(followupTimeEl, 15l);
		
		templateEl.clearError();
		if(!templateEl.isOneSelected()) {
			endDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		// dates ok
		if(allOk && (!permanentEl.isVisible() || !permanentEl.isAtLeastSelected(1))) {
			allOk &= validateDuration();
			allOk &= validateSlot();
		}
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (nameEl.getValue().contains("&")) {
			nameEl.setErrorKey("form.invalidchar.noamp", null);
			allOk &= false;
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(permanentEl == source) {
			updateUI();
		} else if(templateEl == source) {
			updateTemplateInformations();
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(meeting == null) {
			meeting = bigBlueButtonManager.createAndPersistMeeting(nameEl.getValue(), entry, subIdent, businessGroup);
		} else {
			meeting = bigBlueButtonManager.getMeeting(meeting);
			meeting.setName(nameEl.getValue());
		}
		
		meeting.setDescription(descriptionEl.getValue());
		meeting.setWelcome(welcomeEl.getValue());
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
		meeting.setTemplate(template);
		
		boolean permanent = permanentEl.isVisible() && permanentEl.isAtLeastSelected(1);
		meeting.setPermanent(permanent);
		if(permanent) {
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
		
		bigBlueButtonManager.updateMeeting(meeting);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			removeAsListenerAndDispose(calCtr);
			calCtr = null;
			removeAsListenerAndDispose(cmc);
			cmc = null;
		}
		super.event(ureq, source, event);
	}

}
