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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OpenMeetingsManager {
	
	
	public String getURL(Identity identity, long roomId, String securedHash, Locale language);
	
	public String setUser(Identity identity, long roomId);
	
	public Long getRoomId(BusinessGroup group, OLATResourceable ores, String subIdentifier);

	public OpenMeetingsRoom getRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier);
	
	public OpenMeetingsRoom addRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier, OpenMeetingsRoom room);
	
	public OpenMeetingsRoom updateRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier, OpenMeetingsRoom room);
	
	
	public boolean checkConnection(String url, String login, String password) throws OpenMeetingsNotAvailableException;
	
	public void deleteAll(BusinessGroup group, OLATResourceable ores, String subIdentifier);

}
