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
package org.olat.modules.teams.ui;

import java.net.URI;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsDispatcher;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.TeamsError;
import org.olat.modules.teams.model.TeamsErrors;

import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.LobbyBypassScope;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 11 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsUIHelper {
	
	public static long getLongOrZero(TextElement textElement) {
		long followupTime = 0l;
		if(textElement.isVisible() && StringHelper.isLong(textElement.getValue())) {
			followupTime = Long.valueOf(textElement.getValue());
		}
		return followupTime;
	}
	
	public static void setDefaults(SingleSelection accessLevelEl, SingleSelection presentersEl, SingleSelection lobbyEl,
			TeamsMeeting meeting, boolean meetingExtendedOptionsEnabled) {
		
		accessLevelEl.setVisible(meetingExtendedOptionsEnabled);
		if(meeting != null && meeting.getAccessLevel() != null && accessLevelEl.containsKey(meeting.getAccessLevel())) {
			accessLevelEl.select(meeting.getAccessLevel(), true);
		} else if(meetingExtendedOptionsEnabled) {
			accessLevelEl.select(AccessLevel.SAME_ENTERPRISE_AND_FEDERATED.name(), true);
		} else {
			accessLevelEl.select(AccessLevel.EVERYONE.name(), true);
		}
		
		presentersEl.setVisible(meetingExtendedOptionsEnabled);
		if(meeting != null && meeting.getAllowedPresenters() != null && presentersEl.containsKey(meeting.getAllowedPresenters())) {
			presentersEl.select(meeting.getAllowedPresenters(), true);
		} else if(meetingExtendedOptionsEnabled) {
			presentersEl.select(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), true);
		} else {
			presentersEl.select(OnlineMeetingPresenters.EVERYONE.name(), true);
		}
		
		lobbyEl.setVisible(meetingExtendedOptionsEnabled);
		if(meeting != null && meeting.getLobbyBypassScope() != null && lobbyEl.containsKey(meeting.getLobbyBypassScope())) {
			lobbyEl.select(meeting.getLobbyBypassScope(), true);
		} else if(meetingExtendedOptionsEnabled) {
			lobbyEl.select(LobbyBypassScope.ORGANIZATION_AND_FEDERATED.name(), true);
		} else {
			lobbyEl.select(LobbyBypassScope.ORGANIZATION.name(), true);
		}
	}
	
	public static boolean validateReadableIdentifier(TextElement externalLinkEl, TeamsMeeting meeting) {
		boolean allOk = true;
		
		externalLinkEl.clearError();
		if(externalLinkEl.isVisible()) {
			String identifier = externalLinkEl.getValue();
			if (StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
				if(identifier.length() > 64) {
					externalLinkEl.setErrorKey("form.error.toolong", new String[] { "64" });
					allOk &= false;
				} else if(getTeamsService().isIdentifierInUse(identifier, meeting)) {
					externalLinkEl.setErrorKey("error.identifier.in.use", null);
					allOk &= false;
				} else {
					try {
						URI uri = new URI(TeamsDispatcher.getMeetingUrl(identifier));
						uri.normalize();
					} catch(Exception e) {
						externalLinkEl.setErrorKey("error.identifier.url.not.valid", new String[] { e.getMessage() });
						allOk &= false;
					}
				}
				externalLinkEl.setExampleKey("noTransOnlyParam", new String[] { TeamsDispatcher.getMeetingUrl(identifier)});			
			} else {
				externalLinkEl.setExampleKey(null, null);
			}
		}

		return allOk;
	}
	
	public static boolean validateDates(DateChooser startDateEl, DateChooser endDateEl) {
		boolean allOk = true;
		
		if(startDateEl.getDate() == null) {
			startDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		if(endDateEl.getDate() == null) {
			endDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(startDateEl.getDate() != null && endDateEl.getDate() != null) {
			Date start = startDateEl.getDate();
			Date end = endDateEl.getDate();
			if(end.before(start)) {
				endDateEl.setErrorKey("error.start.after.end", null);
				allOk &= false;
			}
			
			Date now = new Date();
			if(end.before(now)) {
				endDateEl.setErrorKey("error.end.past", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	public static final String formatErrors(Translator translator, TeamsErrors errors) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(translator.translate("error.prefix"))
		  .append("<ul>");
		for(TeamsError error:errors.getErrors()) {
			sb.append("<li>");
			if(StringHelper.containsNonWhitespace(error.getMessageKey())) {
				sb.append(translator.translate("error.server.raw", new String[]{ error.getMessageKey(), error.getMessage() } ));
			} else {
				sb.append(translator.translate("error." + error.getCode().name(), error.getArguments()));
			}
			sb.append("</li>");
		}
		return sb.append("</ul>").toString();
	}
	
	public static boolean isEditable(TeamsMeeting m, UserRequest ureq) {
		Date now = ureq.getRequestTimestamp();
		return m == null || m.isPermanent()
				|| (m.getEndWithFollowupTime() != null && m.getEndWithFollowupTime().compareTo(now) > 0);
	}
	
	public static  boolean isEditableGraph(TeamsMeeting m) {
		return m == null || !StringHelper.containsNonWhitespace(m.getOnlineMeetingId());
	}
	
	private static TeamsService getTeamsService() {
		return CoreSpringFactory.getImpl(TeamsService.class);
	}

}
