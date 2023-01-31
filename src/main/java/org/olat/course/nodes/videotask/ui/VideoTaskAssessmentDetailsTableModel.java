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
package org.olat.course.nodes.videotask.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.course.nodes.videotask.ui.VideoTaskAssessmentDetailsController.VideoTaskSessionRowComparator;
import org.olat.modules.video.VideoTaskSession;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentDetailsTableModel extends DefaultFlexiTableDataModel<VideoTaskSessionRow>
implements FlexiTableCssDelegate  {
	
	private static final DetailsCols[] COLS = DetailsCols.values();
	
	private VideoTaskSession lastSession;
	
	public VideoTaskAssessmentDetailsTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	public VideoTaskSession getLastSession() {
		return lastSession;
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		VideoTaskSessionRow session = getObject(pos);
		if(session.getTaskSession().isCancelled()) {
			return "o_test_session_cancelled";	
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		VideoTaskSessionRow session = getObject(row);
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case id: return session.getTaskSessionKey();
				case attempt: return Long.valueOf(session.getAttempt());
				case duration: return session.getDuration();
				case scorePercent: return session.getScoreInPercent();
				case scorePoints: return session.getPoints();
				case play: return Boolean.valueOf(session.getTaskSession().getFinishTime() != null);
				case tools: return session.getToolsButton();
				default: return "ERROR";
			}
		} else if(col >= VideoTaskAssessmentDetailsController.CATEGORY_PROPS_OFFSET) {
			int colIndex = col - VideoTaskAssessmentDetailsController.CATEGORY_PROPS_OFFSET;
			return session.getCategoryScoring(colIndex);
		}
		return null;
	}
	
	@Override
	public void setObjects(List<VideoTaskSessionRow> objects) {
		super.setObjects(objects);
		
		List<VideoTaskSessionRow> sessions = new ArrayList<>(objects);
		Collections.sort(sessions, new VideoTaskSessionRowComparator());
		for(VideoTaskSessionRow session:sessions) {
			VideoTaskSession taskSession = session.getTaskSession();
			if(taskSession != null && !taskSession.isCancelled()) {
				lastSession = taskSession;
				break;
			}
		}
	}

	public enum DetailsCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		attempt("table.header.attempt"),
		duration("table.header.duration"),
		scorePercent("table.header.score.percent"),
		scorePoints("table.header.score.points"),
		play("table.header.play"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private DetailsCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools && this != play;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
