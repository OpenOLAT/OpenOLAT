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
package org.olat.modules.gotomeeting;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface GoToOrganizer extends CreateInfo {
	
	public Long getKey();
	
	public Date getLastModified();
	
	public String getName();
	
	public String getUsername();
	
	public String getFirstName();
	
	public String getLastName();
	
	public String getEmail();
	
	public String getAccessToken();
	
	public String getOrganizerKey();
	
	public Date getRenewDate();
	
	/**
	 * Return the owner of this organizer configuration, or null for 
	 * system wide organizer.
	 * @return
	 */
	public Identity getOwner();

}
