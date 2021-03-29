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
package org.olat.modules.bigbluebutton.ui.recurring;

import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.isWebcamLayoutAvailable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlugButtonMeetingConfigurationController extends StepFormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] yesNoKeys = new String[] { "yes", "no" };
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private TextElement welcomeEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private MultipleSelectionElement externalLinkEl;
	private MultipleSelectionElement passwordEnableEl;
	private TextElement passwordEl;
	private DateChooser startTimeEl;
	private DateChooser endTimeEl;
	private DateChooser endRecurringDateEl;
	private DateChooser startRecurringDateEl;
	private SingleSelection templateEl;
	private SingleSelection layoutEl;
	private SingleSelection recordEl;
	private SingleSelection publishingEl;

	private RecurringMeetingsContext meetingsContext;
	private List<BigBlueButtonMeetingTemplate> templates;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlugButtonMeetingConfigurationController(UserRequest ureq, WindowControl wControl,
			RecurringMeetingsContext meetingsContext, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale()));

		this.meetingsContext = meetingsContext;
		templates = bigBlueButtonManager.getTemplates();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = meetingsContext.getName();
		nameEl = uifactory.addTextElement("meeting.name", "meeting.name", 128, name, formLayout);
		nameEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		String description = meetingsContext.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);

		String welcome = meetingsContext.getWelcome();
		welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", welcome, 8, 60, formLayout, getWindowControl());
		
		String mainPresenter = meetingsContext.getMainPresenter();
		mainPresenterEl = uifactory.addTextElement("meeting.main.presenter", "meeting.main.presenter", 128, mainPresenter, formLayout);
		
		Long selectedTemplateKey = meetingsContext.getTemplate() == null ? null : meetingsContext.getTemplate().getKey();
		KeyValues templatesKeyValues = new KeyValues();
		for(BigBlueButtonMeetingTemplate template:templates) {
			if((template.isEnabled() && template.availableTo(meetingsContext.getPermissions()))
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
		
		String[] yesNoValues = new String[] { translate("yes"), translate("no")  };
		recordEl = uifactory.addRadiosVertical("meeting.record", formLayout, yesNoKeys, yesNoValues);
		recordEl.addActionListener(FormEvent.ONCHANGE);
		if(BigBlueButtonUIHelper.isRecord(meetingsContext.getTemplate())) {
			recordEl.select(yesNoKeys[0], true);
		} else {
			recordEl.select(yesNoKeys[1], true);
		}
		
		recordEl.select(yesNoKeys[0], true);
		
		KeyValues publishKeyValues = new KeyValues();
		publishKeyValues.add(KeyValues.entry(BigBlueButtonRecordingsPublishingEnum.auto.name(), translate("meeting.publishing.auto")));
		publishKeyValues.add(KeyValues.entry(BigBlueButtonRecordingsPublishingEnum.manual.name(), translate("meeting.publishing.manual")));
		publishingEl = uifactory.addRadiosVertical("meeting.publishing", formLayout, publishKeyValues.keys(), publishKeyValues.values());
		BigBlueButtonRecordingsPublishingEnum publish = meetingsContext.getRecordingsPublishing() == null
				? BigBlueButtonRecordingsPublishingEnum.auto :  meetingsContext.getRecordingsPublishing();
		publishingEl.select(publish.name(), true);

		KeyValues layoutKeyValues = new KeyValues();
		layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
		if(isWebcamLayoutAvailable(BigBlueButtonUIHelper.getSelectedTemplate(templateEl, templates))) {
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translate("layout.webcam")));
		}
		layoutEl = uifactory.addDropdownSingleselect("meeting.layout", "meeting.layout", formLayout,
				layoutKeyValues.keys(), layoutKeyValues.values());
		boolean layoutSelected = false;
		String selectedLayout = meetingsContext.getMeetingLayout() == null
				? BigBlueButtonMeetingLayoutEnum.standard.name() : meetingsContext.getMeetingLayout().name();
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
		
		String[] externalLinkValues = new String[] { translate("enable.generate.url") };
		externalLinkEl = uifactory.addCheckboxesHorizontal("meeting.external.users", formLayout, onKeys, externalLinkValues);
		externalLinkEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] enableValues = new String[] { translate("meeting.password.enable.on") };
		passwordEnableEl = uifactory.addCheckboxesHorizontal("meeting.password.enable", "meeting.password.enable", formLayout, onKeys, enableValues);
		passwordEnableEl.addActionListener(FormEvent.ONCHANGE);
		
		passwordEl = uifactory.addTextElement("meeting.password", 64, "", formLayout);
		
		BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, passwordEnableEl, passwordEl, publishingEl, recordEl,
				templates, false);
		
		startRecurringDateEl = uifactory.addDateChooser("meeting.recurring.start", "meeting.recurring.start", null, formLayout);
		startRecurringDateEl.setMandatory(true);
		
		endRecurringDateEl = uifactory.addDateChooser("meeting.recurring.end", "meeting.recurring.end", null, formLayout);
		endRecurringDateEl.setMandatory(true);
		
		Date startDate = new Date();
		startTimeEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
		startTimeEl.setMandatory(true);
		startTimeEl.setTimeOnly(true);
		
		String leadtime = Long.toString(meetingsContext.getLeadTime());
		leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
		
		Date endDate = null;
		if (endDate == null && startDate != null) {
			// set meeting time default to 1 hour
			Calendar calendar = Calendar.getInstance();
		    calendar.setTime(startDate);
		    calendar.add(Calendar.HOUR_OF_DAY, 1);
		    endDate = calendar.getTime();
		}
		endTimeEl = uifactory.addDateChooser("meeting.end", "meeting.end", endDate, formLayout);
		endTimeEl.setMandatory(true);
		endTimeEl.setDefaultValue(startTimeEl);
		endTimeEl.setTimeOnly(true);
		
		String followup = Long.toString(meetingsContext.getFollowupTime());
		followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
	}
	
	private void updatePasswordElement() {
		boolean externalInkenabled = externalLinkEl.isVisible() && externalLinkEl.isAtLeastSelected(1);
		passwordEnableEl.setVisible(externalInkenabled);
		passwordEl.setVisible(externalInkenabled && passwordEnableEl.isAtLeastSelected(1));
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startRecurringDateEl.clearError();
		if(startRecurringDateEl.getDate() == null) {
			startRecurringDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		endRecurringDateEl.clearError();
		if(endRecurringDateEl.getDate() == null) {
			endRecurringDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(startRecurringDateEl.getDate() != null && endRecurringDateEl.getDate() != null
				&& endRecurringDateEl.getDate().before(startRecurringDateEl.getDate())) {
			endRecurringDateEl.setErrorKey("error.start.after.end", null);
			allOk &= false;
		}

		startTimeEl.clearError();
		if(startTimeEl.getDate() == null) {
			startTimeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		endTimeEl.clearError();
		if(endTimeEl.getDate() == null) {
			endTimeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(startTimeEl.getDate() != null && endTimeEl.getDate() != null) {
			long start = startTimeEl.getDate().getTime();
			long end = endTimeEl.getDate().getTime();
			if(start > end) {
				endTimeEl.setErrorKey("error.start.after.end", null);
				allOk &= false;
			}
		}
		
		if(allOk) {
			Date firstDate = getFirstDateTime();
			if(firstDate != null && firstDate.before(new Date())) {
				startRecurringDateEl.setErrorKey("error.first.date.in.past", null);
				allOk &= false;
			}
		}
		
		allOk &= validateTime(leadTimeEl, 15l);
		allOk &= validateTime(followupTimeEl, 15l);
		
		templateEl.clearError();
		if(!templateEl.isOneSelected()) {
			endTimeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		// dates ok
		if(allOk) {
			allOk &= validateDuration();
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
	
	private Date getFirstDateTime() {
		if(startRecurringDateEl.getDate() != null && startTimeEl.getDate() != null) {
			return RecurringMeetingsContext
					.transferTime(startRecurringDateEl.getDate(), startTimeEl.getDate());
		}
		return null;
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
		Date start = startTimeEl.getDate();
		Date end = endTimeEl.getDate();
		if(template != null && template.getMaxDuration() != null && start != null && end != null) {
			// all calculation in milli-seconds
			long realStart = start.getTime() - (60 * 1000 * getLeadTime());
			long realEnd = end.getTime() + (60 * 1000 * getFollowupTime());
			long duration = realEnd - realStart;
			long maxDuration  = (60 * 1000 * template.getMaxDuration());
			if(duration > maxDuration) {
				endTimeEl.setErrorKey("error.duration", new String[] { template.getMaxDuration().toString() });
				allOk &= false;
			}
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
		if(templateEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, passwordEnableEl, passwordEl, publishingEl, recordEl,
					templates, false);
			boolean webcamAvailable = isWebcamLayoutAvailable(BigBlueButtonUIHelper.getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
		} else if(recordEl == source || passwordEnableEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, passwordEnableEl, passwordEl, publishingEl, recordEl,
					templates, false);
		} else if(externalLinkEl == source || passwordEnableEl == source) {
			updatePasswordElement();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		meetingsContext.setName(nameEl.getValue());
		meetingsContext.setDescription(descriptionEl.getValue());
		meetingsContext.setWelcome(welcomeEl.getValue());
		meetingsContext.setMainPresenter(mainPresenterEl.getValue());
		BigBlueButtonMeetingTemplate template = getSelectedTemplate();
		meetingsContext.setTemplate(template);
		
		if(layoutEl.isVisible() && layoutEl.isOneSelected()) {
			BigBlueButtonMeetingLayoutEnum layout = BigBlueButtonMeetingLayoutEnum.secureValueOf(layoutEl.getSelectedKey());
			meetingsContext.setMeetingLayout(layout);
		} else {
			meetingsContext.setMeetingLayout(BigBlueButtonMeetingLayoutEnum.standard);
		}
		
		if(publishingEl.isVisible() && publishingEl.isOneSelected()) {
			meetingsContext.setRecordingsPublishing(BigBlueButtonRecordingsPublishingEnum.valueOf(publishingEl.getSelectedKey()));
		} else {
			meetingsContext.setRecordingsPublishing(BigBlueButtonRecordingsPublishingEnum.manual);
		}
		if(recordEl.isVisible() && recordEl.isOneSelected()) {
			meetingsContext.setRecord(Boolean.valueOf(yesNoKeys[0].equals(recordEl.getSelectedKey())));
		} else {
			meetingsContext.setRecord(null);
		}

		Date startDate = startTimeEl.getDate();
		meetingsContext.setStartTime(startDate);
		Date endDate = endTimeEl.getDate();
		meetingsContext.setEndTime(endDate);
		long leadTime = getLeadTime();
		meetingsContext.setLeadTime(leadTime);
		long followupTime = getFollowupTime();
		meetingsContext.setFollowupTime(followupTime);
		
		if(externalLinkEl.isVisible() && externalLinkEl.isAtLeastSelected(1)) {
			meetingsContext.setGenerateUrl(true);
			if(passwordEnableEl.isAtLeastSelected(1)) {
				meetingsContext.setPassword(passwordEl.getValue());
			}
		} else {
			meetingsContext.setGenerateUrl(false);
			meetingsContext.setPassword(null);
		}
		
		meetingsContext.setStartRecurringDate(startRecurringDateEl.getDate());
		meetingsContext.setEndRecurringDate(endRecurringDateEl.getDate());
		
		meetingsContext.generateMeetings();

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
