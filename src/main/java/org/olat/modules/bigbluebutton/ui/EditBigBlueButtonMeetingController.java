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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
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
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
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
	private List<BigBlueButtonMeetingTemplate> templates;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		super(ureq, wControl);
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		templates = bigBlueButtonManager.getTemplates();
		
		initForm(ureq);
		updateUI();
	}
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl, BigBlueButtonMeeting meeting) {
		super(ureq, wControl);
		entry = meeting.getEntry();
		subIdent = meeting.getSubIdent();
		businessGroup = meeting.getBusinessGroup();
		this.meeting = meeting;
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
		
		KeyValues templatesKeyValues = new KeyValues();
		templatesKeyValues.add(KeyValues.entry("-", translate("meeting.no.template")));
		for(BigBlueButtonMeetingTemplate template:templates) {
			templatesKeyValues.add(KeyValues.entry(template.getKey().toString(), template.getName()));
		}
		String[] templatesKeys = templatesKeyValues.keys();
		templateEl = uifactory.addDropdownSingleselect("meeting.template", "meeting.template", formLayout,
				templatesKeys, templatesKeyValues.values());
		templateEl.setMandatory(true);
		boolean templateSelected = false;
		if(meeting != null && meeting.getTemplate() != null) {
			String currentTemplateId = meeting.getTemplate().getKey().toString();
			for(String key:templatesKeys) {
				if(currentTemplateId.equals(key)) {
					templateEl.select(currentTemplateId, true);
					templateSelected = true;
				}
			}
		}
		if(!templateSelected) {
			templateEl.select(templatesKeys[0], true);
		}
		
		String[] permValues = new String[] { translate("meeting.permanent.on") };
		permanentEl = uifactory.addCheckboxesHorizontal("meeting.permanent", formLayout, permKeys, permValues);
		permanentEl.addActionListener(FormEvent.ONCHANGE);
		boolean permanent = meeting == null ? false : meeting.isPermanent();
		permanentEl.select(permKeys[0], permanent);
		permanentEl.setHelpTextKey("meeting.permanent.explain", null);

		Date startDate = meeting == null ? null : meeting.getStartDate();
		startDateEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
		startDateEl.setMandatory(!permanent);
		startDateEl.setDateChooserTimeEnabled(true);
		
		String leadtime = meeting == null ? null : Long.toString(meeting.getLeadTime());
		leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
		
		Date endDate = meeting == null ? null : meeting.getEndDate();
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
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (nameEl.getValue().contains("&")) {
			nameEl.setErrorKey("form.invalidchar.noamp", null);
			allOk &= false;
		}
		
		startDateEl.clearError();
		endDateEl.clearError();
		if(!permanentEl.isAtLeastSelected(1)) {
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
			}
		}
		
		allOk &= validateTime(leadTimeEl);
		allOk &= validateTime(followupTimeEl);
		return allOk;
	}
	
	private boolean validateTime(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue()) && !StringHelper.isLong(el.getValue())) {
			el.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(permanentEl == source) {
			updateUI();
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
		BigBlueButtonMeetingTemplate template = null;
		if(templateEl.isOneSelected() && !"-".equals(templateEl.getSelectedKey())) {
			String selectedTemplateId = templateEl.getSelectedKey();
			template = templates.stream()
					.filter(tpl -> selectedTemplateId.equals(tpl.getKey().toString()))
					.findFirst()
					.orElse(null);
		}
		meeting.setTemplate(template);
		
		boolean permanent;	
		if(permanentEl.isVisible()) {
			permanent = permanentEl.isAtLeastSelected(1);
		} else {
			permanent = false;
		}
		meeting.setPermanent(permanent);
		if(permanent) {
			meeting.setStartDate(null);
			meeting.setEndDate(null);
			meeting.setLeadTime(0l);
			meeting.setFollowupTime(0l);
		} else {
			Date startDate = startDateEl.getDate();
			Date endDate = endDateEl.getDate();
			meeting.setStartDate(startDate);
			meeting.setEndDate(endDate);
			
			long leadTime = 0;
			if(leadTimeEl.isVisible() && StringHelper.isLong(leadTimeEl.getValue())) {
				leadTime = Long.valueOf(leadTimeEl.getValue());
			}
			meeting.setLeadTime(leadTime);
			
			long followupTime = 0;
			if(followupTimeEl.isVisible() && StringHelper.isLong(followupTimeEl.getValue())) {
				followupTime = Long.valueOf(followupTimeEl.getValue());
			}
			meeting.setFollowupTime(followupTime);
		}
		
		bigBlueButtonManager.updateMeeting(meeting);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
