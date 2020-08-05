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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

public class DENListTableDataModel extends DefaultTableDataModel<KalendarEvent> {
	
	public static final String CHANGE_ACTION = "denPartsChange";
	public static final String MAIL_ACTION = "denSendMail";
	public static final String DELETE_ACTION = "denDeleteParticipants";
	
	// subject, date, location, comment, participants, usernames, action 
	private static final int COLUMN_COUNT = 7;
	
	private final Translator translator;
	private final DENManager denManager;
	private final UserManager userManager;
	
	private final boolean readOnly;

	public DENListTableDataModel(List<KalendarEvent> objects, Translator translator, boolean readOnly) {
		super(objects);
		denManager = DENManager.getInstance();
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		this.translator = translator;
		this.readOnly = readOnly;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		KalendarEvent event = getObject(row);
		switch (col) {
			case 0: return event.getSubject();
			case 1: return event.getBegin();
			case 2://duration
				Date begin = event.getBegin();
				Date end = event.getEnd();
				long milliSeconds = denManager.getDuration(begin, end);
				return denManager.formatDuration(milliSeconds, translator);
			case 3: return event.getLocation();
			case 4: return event.getComment();
			case 5:
				//participants
				StringBuilder names = new StringBuilder();
				if(event.getParticipants() != null && event.getParticipants().length > 0) {
					for( String participant : event.getParticipants() ) {
						if(names.length() > 0) names.append("<br/>");
						String fullName = userManager.getUserDisplayName(participant);
						if(StringHelper.containsNonWhitespace(fullName)) {
							names.append(fullName);
						}
					}
				}
				return names.toString();
			case 7: return Boolean.TRUE;
			default:	return "error";
		}
	}
}