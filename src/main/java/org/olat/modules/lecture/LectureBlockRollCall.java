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
package org.olat.modules.lecture;

import java.util.Date;
import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LectureBlockRollCall extends LectureBlockRollCallRef, ModifiedInfo, CreateInfo {
	
	public Identity getIdentity();
	
	public LectureBlock getLectureBlock();
	
	public int getLecturesAbsentNumber();
	
	public int getLecturesAttendedNumber();
	
	public List<Integer> getLecturesAttendedList();
	
	public List<Integer> getLecturesAbsentList();
	
	public Boolean getAbsenceAuthorized();
	
	public void setAbsenceAuthorized(Boolean absenceAuthorized);
	
	public String getAbsenceReason();

	public void setAbsenceReason(String absenceReason);
	
	public String getComment();
	
	public void setComment(String comment);
	
	public Date getAppealDate();
	
	public void setAppealDate(Date date);
	
	public LectureBlockAppealStatus getAppealStatus();
	
	public void setAppealStatus(LectureBlockAppealStatus status);
	
	public String getAppealStatusReason();

	public void setAppealStatusReason(String statusReason);
	
	public String getAppealReason();
	
	public void setAppealReason(String reason);

	public Date getAbsenceSupervisorNotificationDate();

	public void setAbsenceSupervisorNotificationDate(Date absenceSupervisorNotificationDate);
	
	public AbsenceCategory getAbsenceCategory();
	
	public void setAbsenceCategory(AbsenceCategory category);
	
	public AbsenceNotice getAbsenceNotice();

}
