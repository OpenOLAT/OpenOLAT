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
package org.olat.modules.grading;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntryGradingConfiguration extends CreateInfo, ModifiedInfo {
	

	public RepositoryEntry getEntry();
	
	public boolean isGradingEnabled();
	
	public void setGradingEnabled(boolean enabled);
	
	public GradingAssessedIdentityVisibility getIdentityVisibilityEnum();

	public void setIdentityVisibilityEnum(GradingAssessedIdentityVisibility identityVisibility);

	public Integer getGradingPeriod();

	public void setGradingPeriod(Integer days);
	
	public GradingNotificationType getNotificationTypeEnum();

	public void setNotificationTypeEnum(GradingNotificationType type);
	
	public String getNotificationSubject();

	public void setNotificationSubject(String subject);

	public String getNotificationBody();

	public void setNotificationBody(String body);
	
	public Integer getFirstReminder();

	public void setFirstReminder(Integer days);

	public String getFirstReminderSubject();

	public void setFirstReminderSubject(String subject);

	public String getFirstReminderBody();

	public void setFirstReminderBody(String body);
	
	public Integer getSecondReminder();

	public void setSecondReminder(Integer days);

	public String getSecondReminderSubject();

	public void setSecondReminderSubject(String subject);

	public String getSecondReminderBody();

	public void setSecondReminderBody(String body);

}
