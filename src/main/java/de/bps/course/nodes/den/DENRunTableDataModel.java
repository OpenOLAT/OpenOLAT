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
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModelWithMarkableRows;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;

import de.bps.course.nodes.DENCourseNode;

/**
 * TableDataModel for run view of date enrollment
 * @author skoeber
 */
public class DENRunTableDataModel extends DefaultTableDataModel<KalendarEvent> implements TableDataModelWithMarkableRows<KalendarEvent> {
	
	public static final String CMD_ENROLL_IN_DATE = "cmd.enroll.in.date";
	public static final String CMD_ENROLLED_CANCEL = "cmd.enrolled.cancel";
	
	// subject, begin, end, location, comment, enrolled/max participants, status, enrollBtn, cancelBtn
	private static final int COLUMN_COUNT = 7;
	
	private DENManager denManager;
	private Identity identity;
	private DENCourseNode courseNode;
	private Translator translator;
	private final boolean cancelEnrollEnabled;
	private final boolean enrollmentEnabled;

	/**
	 * Standard constructor of the table data model for run view
	 * @param List of KalendarEvent objects
	 * @param ureq
	 * @param DENCourseNode courseNode
	 */
	public DENRunTableDataModel(List<KalendarEvent> objects, Identity identity,
			DENCourseNode courseNode, boolean cancelEnrollEnabled, boolean enrollmentEnabled, Translator translator) {
		super(objects);
		denManager = DENManager.getInstance();
		this.identity = identity;
		this.courseNode = courseNode;
		this.translator = translator;
		this.cancelEnrollEnabled = cancelEnrollEnabled;
		this.enrollmentEnabled = enrollmentEnabled;
	}
	
	public boolean isEnrollmentEnabled() {
		return enrollmentEnabled;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		KalendarEvent event = getObject(row);
		
		switch (col) {
		case 0:
			//subject
			return event.getSubject();
		case 1:
			return event.getBegin();
		case 2:
			Date begin = event.getBegin();
			Date end = event.getEnd();
			long milliSeconds = denManager.getDuration(begin, end);
			
			return denManager.formatDuration(milliSeconds, translator);
		case 3:
			//location
			return event.getLocation();
		case 4:
			//comment
			return event.getComment();
		case 5:
			//enrolled & total
			StringBuilder numStrBuf = new StringBuilder();
			String[] participants = event.getParticipants(); 
			numStrBuf.append(participants == null ? "0" : participants.length)
			  .append("/").append(event.getNumParticipants());
			return numStrBuf.toString();
		case 6:
			//status
			boolean isEnrolled = denManager.isEnrolledInDate(identity, event);
			if(isEnrolled)
				return translator.translate("dates.table.run.enrolled");
			else if(denManager.isDateFull(event))
				return translator.translate("dates.table.run.full");
			else
				return translator.translate("dates.table.run.notenrolled");
		case 7:
			if(!enrollmentEnabled) return Boolean.FALSE;
			//enroll
			if(denManager.isAlreadyEnrolled(identity, objects, courseNode)) {
				return Boolean.FALSE;
			}
			if( (event.getParticipants() != null) && (event.getParticipants().length >= event.getNumParticipants()) ) {
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		case 8:
			if(!enrollmentEnabled) {
				return denManager.isEnrolledInDate(identity, event);
			}
			//cancel enrollment
			if(cancelEnrollEnabled)
				return denManager.isEnrolledInDate(identity, event);
			return Boolean.FALSE;

		default: return "error";
		}
	}

	@Override
	public String getRowCssClass(int rowId) {
		KalendarEvent event = getObject(rowId);
		boolean isEnrolled = denManager.isEnrolledInDate(identity, event);
		return (isEnrolled ? "o_row_selected" : "");
	}
}
