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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionRowComparator;
import org.olat.modules.video.VideoTaskSession;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentDetailsTableModel extends DefaultFlexiTableDataModel<VideoTaskSessionRow>
implements FlexiTableCssDelegate, FilterableFlexiTableModel {
	
	private static final DetailsCols[] COLS = DetailsCols.values();
	
	public static final String FILTER_PERFORMANCE = "performance";
	public static final String FILTER_PERFORMANCE_HIGH = "high";
	public static final String FILTER_PERFORMANCE_MEDIUM = "medium";
	public static final String FILTER_PERFORMANCE_LOW = "low";
	
	public static final String FILTER_ATTEMPTS = "attempts";
	public static final String FILTER_IDENTITY = "identity";
	
	private static final BigDecimal HIGH = BigDecimal.valueOf(67);
	private static final BigDecimal MEDIUM = BigDecimal.valueOf(34);
	
	private VideoTaskSession lastSession;

	private List<VideoTaskSessionRow> backupRows;
	
	public VideoTaskAssessmentDetailsTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	public VideoTaskSession getLastSession() {
		return lastSession;
	}
	
	public int getMaxAttempts() {
		int maxAttempts = 0;
		for(int i=this.getRowCount(); i-->0; ) {
			VideoTaskSessionRow row = this.getObject(i);
			if(row != null && row.getAttempt() > maxAttempts) {
				maxAttempts = (int)row.getAttempt();
			}
		}
		return maxAttempts;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		setObjects(backupRows);
		
		if (filters != null && !filters.isEmpty()) {
			List<String> filterValues = null;
			Set<Long> attempts = null;
			String search = null;
			
			FlexiTableFilter performanceFilter = FlexiTableFilter.getFilter(filters, FILTER_PERFORMANCE);
			if (performanceFilter instanceof FlexiTableExtendedFilter extFilter) {
				filterValues = extFilter.getValues();
			}
			
			FlexiTableFilter attemptsFilter = FlexiTableFilter.getFilter(filters, FILTER_ATTEMPTS);
			if (attemptsFilter instanceof FlexiTableExtendedFilter extFilter) {
				List<String> attemptsValues = extFilter.getValues();
				if(attemptsValues != null) {
					attempts = attemptsValues.stream()
							.map(Long::parseLong)
							.collect(Collectors.toSet());
				}
			}
			
			FlexiTableFilter identityFilter = FlexiTableFilter.getFilter(filters, FILTER_IDENTITY);
			if (identityFilter != null) {
				String searchVal = identityFilter.getValue();
				if(searchVal != null) {
					search = searchVal.toLowerCase();
				}
			}
			
			if((filterValues != null && !filterValues.isEmpty())
					|| (attempts != null && !attempts.isEmpty())
					|| StringHelper.containsNonWhitespace(search)) {
				List<VideoTaskSessionRow> filteredRows = new ArrayList<>();
				int numOfRows = getRowCount();
				for(int i=0; i<numOfRows; i++) {
					VideoTaskSessionRow row = getObject(i);
					if((filterValues == null || acceptPerformance(row, filterValues))
							&& (attempts == null || acceptAttempts(row, attempts))
							&& (!StringHelper.containsNonWhitespace(search) || acceptSearch(row, search))) {
						filteredRows.add(row);
					}
				}
				super.setObjects(filteredRows);
			}
		}
	}
	
	private boolean acceptSearch(VideoTaskSessionRow row, String search) {
		Identity identity = row.getAssessedIdentity();
		if(identity != null && identity.getUser() != null) {
			User user = identity.getUser();
			return acceptSearch(user.getFirstName(), search)
					|| acceptSearch(user.getLastName(), search)
					|| acceptSearch(user.getNickName(), search)
					|| acceptSearch(user.getEmail(), search);
		}
		return false;
	}
	
	private boolean acceptSearch(String val, String search) {
		if(StringHelper.containsNonWhitespace(val)) {
			return val.toLowerCase().contains(search);
		}
		return false;
	}
	
	private boolean acceptAttempts(VideoTaskSessionRow row, Set<Long> attempts) {
		return attempts.contains(Long.valueOf(row.getAttempt()));
	}
	
	private boolean acceptPerformance(VideoTaskSessionRow row, List<String> filterValues) {
		for(String filterValue:filterValues) {
			if((FILTER_PERFORMANCE_HIGH.equals(filterValue)
					&& row.getResultInPercent().compareTo(HIGH) >= 0)) {
				return true;
			}
			if(FILTER_PERFORMANCE_MEDIUM.equals(filterValue)
					&& row.getResultInPercent().compareTo(MEDIUM) >= 0
					&& row.getResultInPercent().compareTo(HIGH) < 0) {
				return true;
			}
			if(FILTER_PERFORMANCE_LOW.equals(filterValue)
					&& row.getResultInPercent().compareTo(MEDIUM) < 0) {
				return true;
			}
		}
		return false;
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
				case assessedIdentity: return session.getIdentityFullName();
				case attempt: return Long.valueOf(session.getAttempt());
				case duration: return session.isFinished() ? Long.valueOf(session.getDuration()) : null;
				case scorePercent: return session.isFinished() ? session.getResultInPercent() : null;
				case scorePoints: return session.isFinished() ? session.getScore() : null;
				case play: return Boolean.valueOf(session.isFinished());
				case tools: return session.getToolsButton();
				default: return "ERROR";
			}
		} else if(col >= AbstractVideoTaskSessionListController.CATEGORY_PROPS_OFFSET) {
			int colIndex = col - AbstractVideoTaskSessionListController.CATEGORY_PROPS_OFFSET;
			return session.getCategoryScoring(colIndex);
		}
		return null;
	}
	
	@Override
	public void setObjects(List<VideoTaskSessionRow> objects) {
		backupRows = new ArrayList<>(objects);
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
		assessedIdentity("table.header.identity"),
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
