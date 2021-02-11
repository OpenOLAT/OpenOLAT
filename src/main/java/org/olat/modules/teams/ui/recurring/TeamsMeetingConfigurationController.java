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
package org.olat.modules.teams.ui.recurring;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.ui.EditTeamsMeetingController;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 11 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingConfigurationController extends StepFormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private DateChooser startTimeEl;
	private DateChooser endTimeEl;
	private DateChooser endRecurringDateEl;
	private DateChooser startRecurringDateEl;
	private SingleSelection presentersEl;
	private MultipleSelectionElement participantsOpenEl;
	
	private TeamsRecurringMeetingsContext meetingsContext;
	
	@Autowired
	private TeamsModule teamsModule;
	
	public TeamsMeetingConfigurationController(UserRequest ureq, WindowControl wControl,
			TeamsRecurringMeetingsContext meetingsContext, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(EditTeamsMeetingController.class, getLocale()));

		this.meetingsContext = meetingsContext;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = meetingsContext.getName();
		nameEl = uifactory.addTextElement("meeting.subject", "meeting.subject", 128, name, formLayout);
		nameEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		String description = meetingsContext.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);

		String mainPresenter = meetingsContext.getMainPresenter();
		mainPresenterEl = uifactory.addTextElement("meeting.main.presenter", "meeting.main.presenter", 128, mainPresenter, formLayout);
		
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
		
		String[] onOpenValues = new String[] { translate("meeting.participants.open.on",
				new String[] {teamsModule.getTenantOrganisation() }) };
		participantsOpenEl = uifactory.addCheckboxesHorizontal("meeting.participants.open", formLayout, onKeys, onOpenValues);
		participantsOpenEl.setHelpTextKey("meeting.participants.open.hint", null);
		participantsOpenEl.setVisible(StringHelper.containsNonWhitespace(teamsModule.getProducerId()));
		
		KeyValues presentersKeyValues = new KeyValues();
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), translate("meeting.presenters.role")));
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ORGANIZATION.name(), translate("meeting.presenters.organization")));
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.EVERYONE.name(), translate("meeting.presenters.everyone")));
		presentersEl = uifactory.addDropdownSingleselect("meeting.presenters", formLayout, presentersKeyValues.keys(), presentersKeyValues.values());
		presentersEl.setMandatory(true);
		if(meetingsContext.getAllowedPresenters() != null && presentersKeyValues.containsKey(meetingsContext.getAllowedPresenters())) {
			presentersEl.select(meetingsContext.getAllowedPresenters(), true);
		} else {
			presentersEl.select(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), true);
		}
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
			return TeamsRecurringMeetingsContext
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
	protected void formNext(UserRequest ureq) {
		meetingsContext.setName(nameEl.getValue());
		meetingsContext.setDescription(descriptionEl.getValue());
		meetingsContext.setMainPresenter(mainPresenterEl.getValue());

		Date startDate = startTimeEl.getDate();
		meetingsContext.setStartTime(startDate);
		Date endDate = endTimeEl.getDate();
		meetingsContext.setEndTime(endDate);
		long leadTime = getLeadTime();
		meetingsContext.setLeadTime(leadTime);
		long followupTime = getFollowupTime();
		meetingsContext.setFollowupTime(followupTime);
		
		meetingsContext.setStartRecurringDate(startRecurringDateEl.getDate());
		meetingsContext.setEndRecurringDate(endRecurringDateEl.getDate());
		
		meetingsContext.setAllowedPresenters(presentersEl.getSelectedKey());
		meetingsContext.setParticipantsCanOpen(participantsOpenEl.isAtLeastSelected(1));
		
		meetingsContext.generateMeetings();

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
