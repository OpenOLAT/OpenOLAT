/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.assessment.Role;

/**
 * 
 * Initial date: 20 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentInspection extends ModifiedInfo, CreateInfo {
	
	Long getKey();
	
	String getSubIdent();
	
	Date getFromDate();

	void setFromDate(Date date);

	Date getToDate();

	void setToDate(Date date);
	
	Integer getExtraTime();

	void setExtraTime(Integer extraTime);

	String getAccessCode();

	void setAccessCode(String accessCode);
	
	/**
	 * Set every time the inspection is started
	 * 
	 * @return The last start date of the inspection
	 */
	Date getStartTime();
	
	/**
	 * The end time is calculated based on the start time and the 
	 * time already used for inspection.
	 * 
	 * @return The planned date of the end of the inspection
	 */
	Date getEndTime();
	
	Role getEndBy();

	AssessmentInspectionStatusEnum getInspectionStatus();
	
	void setInspectionStatus(AssessmentInspectionStatusEnum inspectionStatus);
	
	Long getEffectiveDuration();

	void setEffectiveDuration(Long effectiveDuration);

	String getComment();

	void setComment(String comment);
	
	Identity getIdentity();

	AssessmentInspectionConfiguration getConfiguration();

}
