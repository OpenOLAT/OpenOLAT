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
package org.olat.modules.teams;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.model.TeamsMeetingsSearchParameters;
import org.olat.repository.RepositoryEntry;

import com.microsoft.graph.models.User;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TeamsService {
	
	public TeamsMeeting createMeeting(String subject, Date startDate, Date endDate, RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup, Identity creator);
	
	/**
	 * Reload a meeting
	 * @param meeting The meeting to reload
	 * @return A meeting or null if not found / deleted
	 */
	public TeamsMeeting getMeeting(TeamsMeeting meeting);
	
	public TeamsMeeting updateMeeting(TeamsMeeting meeting);
	
	public void deleteMeeting(TeamsMeeting meeting);
	
	public List<TeamsMeeting> getMeetings(RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup);
	
	public List<TeamsMeeting> getAllMeetings();
	
	public List<TeamsMeeting> getUpcomingsMeetings(RepositoryEntry entry, String subIdent, int maxResults);
	
	public boolean isIdentifierInUse(String identifier, TeamsMeeting meeting);
	
	public boolean isMeetingRunning(TeamsMeeting meeting);
	
	public TeamsMeeting getMeeting(String identifier);
	
	public TeamsMeeting joinMeeting(TeamsMeeting meeting, Identity identity, boolean presenter, boolean guest,
			OAuth2Tokens oauth2Tokens, TeamsErrors errors);

	public int countMeetings(TeamsMeetingsSearchParameters searchParams);
	
	public List<TeamsMeeting> searchMeetings(TeamsMeetingsSearchParameters searchParams, int firstResult, int maxResults);
	
	/**
	 * 
	 * @param identity The identity to lookup
	 * @param oauth2Tokens  The access token for the above identity
	 * @param errors Wrapper to collect errors
	 * @return The user or null if not found
	 */
	public User lookupMe(Identity identity, OAuth2Tokens oauth2Tokens, TeamsErrors errors);

}
