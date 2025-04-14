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
package org.olat.ims.qti21.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.ui.QTI21AssessmentDetailsController.AssessmentTestSessionDetailsComparator;

/**
 * 
 * Initial date: 21.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentTestSessionTableModel extends DefaultFlexiTableDataModel<QTI21AssessmentTestSessionDetails> {
	
	private static final TSCols[] COLS = TSCols.values();
	
	private AssessmentTestSession lastSession;
	
	public QTI21AssessmentTestSessionTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	public AssessmentTestSession getLastTestSession() {
		return lastSession;
	}

	@Override
	public Object getValueAt(int row, int col) {
		QTI21AssessmentTestSessionDetails session = getObject(row);

		return switch(COLS[col]) {
			case run -> session.getRun();
			case id -> session.getTestSession().getKey();
			case terminationTime -> getTerminationTime(session);
			case lastModified -> session.getTestSession().getLastModified();
			case duration -> getDuration(session);
			case status -> session.getSessionStatus();
			case test -> session.getTestSession().getTestEntry().getDisplayname();
			case testEntry -> session.getTestSession().getTestEntry();
			case numOfItemSessions -> session.getNumOfItems();
			case responded -> session.getNumOfItemsResponded();
			case corrected -> session.getNumOfItemsCorrected();
			case score -> getScore(session);
			case manualScore -> session.getTestSession().getFinishTime() != null
					? session.getTestSession().getManualScore() : null;
			case finalScore -> session.getTestSession().getFinishTime() != null
					?  session.getTestSession().getFinalScore() : null;
			case results -> Boolean.valueOf(!isTestSessionOpen(session));
			case correct -> isCorrectionAllowed(session);
			case invalidate -> !isTestSessionOpen(session) && !session.getTestSession().isCancelled() && !session.getTestSession().isExploded();
			case tools -> session.getToolsLink();
			default -> "ERROR";
		};
	}
	
	private String getDuration(QTI21AssessmentTestSessionDetails session) {
		if(session.getTestSession().getFinishTime() != null) {
			return Formatter.formatDuration(session.getTestSession().getDuration().longValue());
		}
		return null;
	}
	
	private String getScore(QTI21AssessmentTestSessionDetails session) {
		StringBuilder sb = new StringBuilder(32);
		if(session.getTestSession().getFinishTime() != null) {
			if(session.hasManualScore()) {
				sb.append("<span class='o_deleted'>")
				  .append(AssessmentHelper.getRoundedScore(session.getAutomaticScore()))
				  .append("</span> ");	
			}
			sb.append(AssessmentHelper.getRoundedScore(session.getScore()));
		}
		return sb.toString();
	}
	
	protected Boolean isCorrectionAllowed(QTI21AssessmentTestSessionDetails session) {
		AssessmentTestSession testSession = session.getTestSession();
		if(lastSession != null && lastSession.equals(testSession)) {
			return Boolean.valueOf(testSession.getFinishTime() != null || testSession.getTerminationTime() != null);
		}
		return null;
	}
	
	private boolean isTestSessionOpen(QTI21AssessmentTestSessionDetails session) {
		Date finished = session.getTestSession().getFinishTime();
		return finished == null;
	}
	
	private Date getTerminationTime(QTI21AssessmentTestSessionDetails session) {
		Date endTime = session.getTestSession().getTerminationTime();
		if(endTime == null) {
			endTime = session.getTestSession().getFinishTime();
		}
		return endTime;
	}

	@Override
	public void setObjects(List<QTI21AssessmentTestSessionDetails> objects) {
		super.setObjects(objects);
		lastSession = null;
		
		List<QTI21AssessmentTestSessionDetails> sessions = new ArrayList<>(objects);
		Collections.sort(sessions, new AssessmentTestSessionDetailsComparator());
		for(QTI21AssessmentTestSessionDetails session:sessions) {
			AssessmentTestSession testSession = session.getTestSession();
			if(testSession != null && !testSession.isCancelled() && !testSession.isExploded()) {
				lastSession = session.getTestSession();
				break;
			}
		}
	}

	public enum TSCols implements FlexiSortableColumnDef {
		terminationTime("table.header.terminationTime"),
		lastModified("table.header.lastModified"),
		duration("table.header.duration"),
		test("table.header.test"),
		numOfItemSessions("table.header.itemSessions"),
		responded("table.header.responded"),
		corrected("table.header.corrected"),
		score("table.header.score"),
		manualScore("table.header.manualScore"),
		finalScore("table.header.finalScore"),
		results("table.header.results.report"),
		correct("table.header.correct"),
		tools("action.more"),
		invalidate("table.header.invalidate"),
		id("table.header.id"),
		run("table.header.run"),
		status("table.header.status"),
		testEntry("table.header.test.entry"),
		;
		
		private final String i18nKey;
		
		private TSCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
