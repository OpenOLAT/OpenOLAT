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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsRecurringMeetingsContext {
	
	private String name;
	private String description;
	private String mainPresenter;
	private Date startTime;
	private Date endTime;
	private long leadTime;
	private long followupTime;
	private Date startRecurringDate;
	private Date endRecurringDate;
	
	private String accessLevel;
	private String allowedPresenters;
	private boolean entryExitAnnouncement;
	private String lobbyBypassScope;
	
	private List<TeamsRecurringMeeting> meetings = new ArrayList<>();
	
	private RecurringMode recurringMode;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	
	public TeamsRecurringMeetingsContext(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			RecurringMode recurringMode) {
		this.entry = entry;
		this.subIdent = subIdent;
		this.recurringMode = recurringMode;
		this.businessGroup = businessGroup;
	}
	
	public RecurringMode getRecurringMode() {
		return recurringMode;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMainPresenter() {
		return mainPresenter;
	}

	public void setMainPresenter(String mainPresenter) {
		this.mainPresenter = mainPresenter;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date time) {
		this.startTime = time;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date time) {
		this.endTime = time;
	}

	public long getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(long leadTime) {
		this.leadTime = leadTime;
	}

	public long getFollowupTime() {
		return followupTime;
	}

	public void setFollowupTime(long followupTime) {
		this.followupTime = followupTime;
	}

	public Date getStartRecurringDate() {
		return startRecurringDate;
	}

	public void setStartRecurringDate(Date startRecurringDate) {
		this.startRecurringDate = startRecurringDate;
	}

	public Date getEndRecurringDate() {
		return endRecurringDate;
	}

	public void setEndRecurringDate(Date endRecurringDate) {
		this.endRecurringDate = endRecurringDate;
	}
	
	public String getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}

	public String getAllowedPresenters() {
		return allowedPresenters;
	}

	public void setAllowedPresenters(String allowedPresenters) {
		this.allowedPresenters = allowedPresenters;
	}

	public boolean isEntryExitAnnouncement() {
		return entryExitAnnouncement;
	}

	public void setEntryExitAnnouncement(boolean entryExitAnnouncement) {
		this.entryExitAnnouncement = entryExitAnnouncement;
	}

	public String getLobbyBypassScope() {
		return lobbyBypassScope;
	}

	public void setLobbyBypassScope(String lobbyBypassScope) {
		this.lobbyBypassScope = lobbyBypassScope;
	}

	public List<TeamsRecurringMeeting> getMeetings() {
		return meetings;
	}
	
	public void addMeetingAt(Date date) {
		meetings.add(generateMeeting(date));
	}
	
	public void generateMeetings() {
		Date start = CalendarUtils.startOfDay(startRecurringDate);
		Date end = CalendarUtils.endOfDay(endRecurringDate);
		
		meetings.clear();
		for(Date current=start; current.before(end); current = nextDay(current)) {
			meetings.add(generateMeeting(current));
		}
	}
	
	private Date nextDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(recurringMode == RecurringMode.daily) {
			cal.add(Calendar.DATE, 1);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			// only working day
			if(dayOfWeek == Calendar.SATURDAY) {
				cal.add(Calendar.DATE, 2);
			} else if(dayOfWeek == Calendar.SUNDAY) {
				cal.add(Calendar.DATE, 1);
			}
		} else if(recurringMode == RecurringMode.weekly) {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
		}
		return cal.getTime();
	}
	
	private TeamsRecurringMeeting generateMeeting(Date date) {
		Date start = transferTime(date, startTime);
		Date end = transferTime(date, endTime);
		return new TeamsRecurringMeeting(start, end);
	}
	
	public static final Date transferTime(Date date, Date time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public enum RecurringMode {
		
		daily,
		weekly
	}
}
