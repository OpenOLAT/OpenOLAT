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
package org.olat.modules.teams.ui;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTeamsMeetingController extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement subjectEl;
	private TextElement descriptionEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private TextElement joinInformationEl;
	private SingleSelection accessLevelEl;
	private SingleSelection presentersEl;
	private MultipleSelectionElement annoncementEl;
	
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	
	private boolean editable = true;
	private TeamsMeeting meeting;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamsService teamsService;
	
	public EditTeamsMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdentifier, BusinessGroup group) {
		super(ureq, wControl);
		this.entry = entry;
		this.group = group;
		this.subIdent = subIdentifier;
		
		initForm(ureq);
	}
	
	public EditTeamsMeetingController(UserRequest ureq, WindowControl wControl,
			TeamsMeeting meeting) {
		super(ureq, wControl);
		this.entry = meeting.getEntry();
		this.subIdent = meeting.getSubIdent();
		this.group = meeting.getBusinessGroup();
		this.meeting = meeting;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String subject = meeting == null ? "" : meeting.getSubject();
		subjectEl = uifactory.addTextElement("meeting.subject", "meeting.subject", 128, subject, formLayout);
		subjectEl.setElementCssClass("o_sel_teams_edit_meeting_subject");
		subjectEl.setMandatory(true);
		subjectEl.setEnabled(editable);
		if(editable && !StringHelper.containsNonWhitespace(subject)) {
			subjectEl.setFocus(true);
		}
		
		Identity creator = meeting == null ? getIdentity() : meeting.getCreator();
		if(creator != null) {
			String creatorFullName = userManager.getUserDisplayName(creator);
			uifactory.addStaticTextElement("meeting.creator", creatorFullName, formLayout);
		}
		
		String description = meeting == null ? "" : meeting.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);
		descriptionEl.setEnabled(editable);
		
		Date startDate = meeting == null ? new Date() : meeting.getStartDate();
		startDateEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
		startDateEl.setMandatory(true);
		startDateEl.setDateChooserTimeEnabled(true);
		startDateEl.setEnabled(editable);
		
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
		
		String joinInfos = meeting == null ? "" : meeting.getJoinInformation();
		joinInformationEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.join.information", "meeting.join.information", joinInfos, 8, 60, formLayout, getWindowControl());
		joinInformationEl.setEnabled(editable);
		
		KeyValues accessKeyValues = new KeyValues();
		accessKeyValues.add(KeyValues.entry(AccessLevel.EVERYONE.name(), translate("meeting.accesslevel.everyone")));
		accessKeyValues.add(KeyValues.entry(AccessLevel.SAME_ENTERPRISE.name(), translate("meeting.accesslevel.same.enterprise")));
		accessKeyValues.add(KeyValues.entry(AccessLevel.SAME_ENTERPRISE_AND_FEDERATED.name(), translate("meeting.accesslevel.same.enterprise.federated")));
		accessLevelEl = uifactory.addDropdownSingleselect("meeting.accesslevel", formLayout, accessKeyValues.keys(), accessKeyValues.values());
		accessLevelEl.setMandatory(true);
		accessLevelEl.setEnabled(editable);
		if(meeting != null && meeting.getAccessLevel() != null && accessKeyValues.containsKey(meeting.getAccessLevel())) {
			accessLevelEl.select(meeting.getAccessLevel(), true);
		} else {
			accessLevelEl.select(AccessLevel.EVERYONE.name(), true);
		}
		
		KeyValues presentersKeyValues = new KeyValues();
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.EVERYONE.name(), translate("meeting.presenters.everyone")));
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ORGANIZATION.name(), translate("meeting.presenters.organization")));
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), translate("meeting.presenters.role")));
		presentersEl = uifactory.addDropdownSingleselect("meeting.presenters", formLayout, presentersKeyValues.keys(), presentersKeyValues.values());
		presentersEl.setMandatory(true);
		presentersEl.setEnabled(editable);
		if(meeting != null && meeting.getAllowedPresenters() != null && presentersKeyValues.containsKey(meeting.getAllowedPresenters())) {
			presentersEl.select(meeting.getAllowedPresenters(), true);
		} else {
			presentersEl.select(OnlineMeetingPresenters.EVERYONE.name(), true);
		}
		
		String[] onValues = new String[] { translate("meeting.annoncement.on") };
		annoncementEl = uifactory.addCheckboxesHorizontal("meeting.annoncement", formLayout, onKeys, onValues);
		if(meeting != null && meeting.isEntryExitAnnouncement()) {
			annoncementEl.select(onKeys[0], true);
		}
		
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		if(editable) {
			uifactory.addFormSubmitButton("save", buttonLayout);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextElement(subjectEl, 255, true);
		allOk &= validateTextElement(descriptionEl, 4000, false);
		allOk &= validateTextElement(joinInformationEl, 4000, false);
		
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
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;

		el.clearError();
		String val = el.getValue();
		if(!StringHelper.containsNonWhitespace(val) && mandatory) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(meeting == null) {
			meeting = teamsService.createMeeting(subjectEl.getValue(),
					startDateEl.getDate(), endDateEl.getDate(),
					entry, subIdent, group, getIdentity());
		} else {
			meeting.setSubject(subjectEl.getValue());
			meeting.setStartDate(startDateEl.getDate());
			meeting.setEndDate(endDateEl.getDate());
		}
		
		meeting.setDescription(descriptionEl.getValue());
		meeting.setJoinInformation(joinInformationEl.getValue());
		meeting.setAccessLevel(accessLevelEl.getSelectedKey());
		meeting.setAllowedPresenters(presentersEl.getSelectedKey());
		meeting.setEntryExitAnnouncement(annoncementEl.isAtLeastSelected(1));
		
		meeting = teamsService.updateMeeting(meeting);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
