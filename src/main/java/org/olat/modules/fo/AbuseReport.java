/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.fo;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * Interface for abuse reports on forum messages.
 * Allows users to report inappropriate content to moderators.
 * 
 * Initial date: January 2026
 * @author OpenOLAT community
 */
public interface AbuseReport extends CreateInfo, Persistable {
	
	/**
	 * @return the reported message
	 */
	public Message getMessage();
	
	/**
	 * @param message the message being reported
	 */
	public void setMessage(Message message);
	
	/**
	 * @return the user who reported the abuse
	 */
	public Identity getReporter();
	
	/**
	 * @param reporter the user reporting the abuse
	 */
	public void setReporter(Identity reporter);
	
	/**
	 * @return the reason for reporting
	 */
	public String getReason();
	
	/**
	 * @param reason the reason for reporting
	 */
	public void setReason(String reason);
	
	/**
	 * @return the current status of the report
	 */
	public AbuseReportStatus getStatus();
	
	/**
	 * @param status the status of the report
	 */
	public void setStatus(AbuseReportStatus status);
	
	/**
	 * @return the date when the report was resolved
	 */
	public Date getResolutionDate();
	
	/**
	 * @param resolutionDate the date when resolved
	 */
	public void setResolutionDate(Date resolutionDate);
	
	/**
	 * @return the moderator who handled the report
	 */
	public Identity getResolvedBy();
	
	/**
	 * @param resolvedBy the moderator who handled it
	 */
	public void setResolvedBy(Identity resolvedBy);
	
	/**
	 * Status enum for abuse reports
	 */
	public enum AbuseReportStatus {
		PENDING,
		REVIEWED,
		DISMISSED,
		ACTION_TAKEN
	}
}
