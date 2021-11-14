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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditAdobeConnectMeetingController extends FormBasicController {
	
	private static final String[] permKeys = new String[] { "on" };
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private MultipleSelectionElement permanentEl;
	private SingleSelection templateEl;
	
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final AdobeConnectMeetingDefaultConfiguration configuration;
	private AdobeConnectMeeting meeting;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public EditAdobeConnectMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			AdobeConnectMeetingDefaultConfiguration configuration) {
		super(ureq, wControl);
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		this.configuration = configuration;
		
		initForm(ureq);
	}
	
	public EditAdobeConnectMeetingController(UserRequest ureq, WindowControl wControl,
			AdobeConnectMeeting meeting, AdobeConnectMeetingDefaultConfiguration configuration) {
		super(ureq, wControl);
		entry = meeting.getEntry();
		subIdent = meeting.getSubIdent();
		businessGroup = meeting.getBusinessGroup();
		this.meeting = meeting;
		this.configuration = configuration;
		
		initForm(ureq);
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
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 8, 72, false, false, description, formLayout);
		
		List<AdobeConnectSco> templates = adobeConnectManager.getTemplates();
		String[] theKeys = new String[templates.size() + 1];
		String[] theValues = new String[templates.size() + 1];
		theKeys[0] = "";
		theValues[0] = translate("no.template");
		for(int i=0; i<templates.size(); i++) {
			AdobeConnectSco template = templates.get(i);
			theKeys[i + 1] = template.getScoId();
			theValues[i + 1] = template.getName();
		}
		templateEl = uifactory.addDropdownSingleselect("meeting.templates", formLayout, theKeys, theValues);
		templateEl.setVisible(!templates.isEmpty());
		
		String[] permValues = new String[] { translate("meeting.permanent.on") };
		permanentEl = uifactory.addCheckboxesHorizontal("meeting.permanent", formLayout, permKeys, permValues);
		permanentEl.addActionListener(FormEvent.ONCHANGE);
		boolean permanent = meeting == null ? adobeConnectModule.isSingleMeetingMode() : meeting.isPermanent();
		permanentEl.select(permKeys[0], permanent);
		boolean permanentDisabled = adobeConnectModule.isSingleMeetingMode()
				|| (meeting != null && StringHelper.containsNonWhitespace(meeting.getScoId()));
		// if single mode -> always permanent
		// if sco linked -> cannot changed it
		permanentEl.setEnabled(!permanentDisabled);
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
		uifactory.addFormSubmitButton("ok", buttonLayout);
	}
	
	private void updateUI() {
		boolean permanent = permanentEl.isAtLeastSelected(1);
		startDateEl.setMandatory(!permanent);
		endDateEl.setMandatory(!permanent);
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
		AdobeConnectErrors errors = new AdobeConnectErrors();

		Date startDate = startDateEl.getDate();
		Date endDate = endDateEl.getDate();
		boolean permanent;	
		if(permanentEl.isVisible()) {
			permanent = permanentEl.isAtLeastSelected(1);
		} else {
			permanent = true;
		}
		
		long leadTime = 0;
		if(leadTimeEl.isVisible() && StringHelper.isLong(leadTimeEl.getValue())) {
			leadTime = Long.valueOf(leadTimeEl.getValue());
		}
		long followupTime = 0;
		if(followupTimeEl.isVisible() && StringHelper.isLong(followupTimeEl.getValue())) {
			followupTime = Long.valueOf(followupTimeEl.getValue());
		}
		
		String templateId = null;
		if(templateEl.isVisible() && StringHelper.containsNonWhitespace(templateEl.getSelectedKey())) {
			templateId = templateEl.getSelectedKey();
		}

		if(meeting == null) {
			adobeConnectManager.createMeeting(nameEl.getValue(), descriptionEl.getValue(), templateId, permanent,
					startDate, leadTime, endDate, followupTime, getLocale(), configuration.isAllowGuestAccess(),
					entry, subIdent, businessGroup, getIdentity(), errors);
		} else {
			adobeConnectManager.updateMeeting(meeting, nameEl.getValue(), descriptionEl.getValue(),
					templateId, permanent, startDate, leadTime, endDate, followupTime, errors);
		}
		
		if(errors.hasErrors()) {
			fireEvent(ureq, new AdobeConnectErrorEvent(errors));
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	

}
