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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 6 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecurringMeetingsContext {
	
	private String name;
	private String description;
	private String welcome;
	private String mainPresenter;
	private Date startTime;
	private Date endTime;
	private long leadTime;
	private long followupTime;
	private Date startRecurringDate;
	private Date endRecurringDate;
	private String password;
	private boolean generateUrl;
	private Boolean record;
	private BigBlueButtonMeetingLayoutEnum layout;
	private BigBlueButtonRecordingsPublishingEnum recordingsPublishing;
	
	private BigBlueButtonMeetingTemplate template;
	
	private List<RecurringMeeting> meetings = new ArrayList<>();
	
	private RecurringMode recurringMode;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final List<BigBlueButtonTemplatePermissions> permissions;
	
	public RecurringMeetingsContext(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			List<BigBlueButtonTemplatePermissions> permissions, RecurringMode recurringMode) {
		this.entry = entry;
		this.subIdent = subIdent;
		this.recurringMode = recurringMode;
		this.businessGroup = businessGroup;
		this.permissions = permissions;
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
	
	public List<BigBlueButtonTemplatePermissions> getPermissions() {
		return permissions;
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

	public String getWelcome() {
		return welcome;
	}

	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}

	public String getMainPresenter() {
		return mainPresenter;
	}

	public void setMainPresenter(String mainPresenter) {
		this.mainPresenter = mainPresenter;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isGenerateUrl() {
		return generateUrl;
	}

	public void setGenerateUrl(boolean generateUrl) {
		this.generateUrl = generateUrl;
	}

	public Boolean getRecord() {
		return record;
	}

	public void setRecord(Boolean record) {
		this.record = record;
	}

	public BigBlueButtonRecordingsPublishingEnum getRecordingsPublishing() {
		return recordingsPublishing;
	}

	public void setRecordingsPublishing(BigBlueButtonRecordingsPublishingEnum recordingsPublishing) {
		this.recordingsPublishing = recordingsPublishing;
	}

	public BigBlueButtonMeetingLayoutEnum getMeetingLayout() {
		return layout;
	}

	public void setMeetingLayout(BigBlueButtonMeetingLayoutEnum layout) {
		this.layout = layout;
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

	public BigBlueButtonMeetingTemplate getTemplate() {
		return template;
	}

	public void setTemplate(BigBlueButtonMeetingTemplate template) {
		this.template = template;
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
	
	public List<RecurringMeeting> getMeetings() {
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
	
	private RecurringMeeting generateMeeting(Date date) {
		Date start = transferTime(date, startTime);
		Date end = transferTime(date, endTime);
		return new RecurringMeeting(start, end);
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
