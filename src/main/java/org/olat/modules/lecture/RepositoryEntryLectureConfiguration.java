package org.olat.modules.lecture;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntryLectureConfiguration extends CreateInfo, ModifiedInfo {

	public Long getKey();
	
	public RepositoryEntry getEntry();
	
	public boolean isOverrideModuleDefault();

	public void setOverrideModuleDefault(boolean overrideModuleDefault);
	
	public boolean isLectureEnabled();

	public void setLectureEnabled(boolean lectureEnabled);

	public Boolean getRollCallEnabled();

	public void setRollCallEnabled(Boolean rollCallEnabled);

	public Boolean getCalculateAttendanceRate();

	public void setCalculateAttendanceRate(Boolean calculateAttendanceRate);
	
	public Double getRequiredAttendanceRate();

	public void setRequiredAttendanceRate(Double requiredAttendanceRate);
	
	public Boolean getTeacherCalendarSyncEnabled();

	public void setTeacherCalendarSyncEnabled(Boolean teacherCalendarSyncEnabled);
	
	public Boolean getParticipantCalendarSyncEnabled();

	public void setParticipantCalendarSyncEnabled(Boolean participantCalendarSyncEnabled);
	
}
