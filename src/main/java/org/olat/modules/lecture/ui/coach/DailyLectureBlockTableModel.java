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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyLectureBlockTableModel extends DefaultFlexiTreeTableDataModel<DailyLectureBlockRow>
implements SortableFlexiTableDataModel<DailyLectureBlockRow> {
	
	private final boolean dailyRecordingEnabled;

	public DailyLectureBlockTableModel(FlexiTableColumnModel columnModel, boolean dailyRecordingEnabled) {
		super(columnModel);
		this.dailyRecordingEnabled = dailyRecordingEnabled;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		//
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		DailyLectureBlockRow block = getObject(row);
		return block.isSeparator();
	}

	public List<LectureBlock> getLectureBlocks() {
		List<DailyLectureBlockRow> rows = getObjects();
		return rows.stream()
			.filter(row -> row.getLectureBlockBlockStatistics() != null)
			.map(DailyLectureBlockRow::getLectureBlockBlockStatistics)
			.map(LectureBlockBlockStatistics::getLectureBlock)
			.collect(Collectors.toList());
	}
	
	public List<LectureBlock> getLectureBlocks(int row) {
		List<LectureBlock> blocks = new ArrayList<>();
		DailyLectureBlockRow blockRow = getObject(row);
		if(blockRow.isSeparator()) {
			List<DailyLectureBlockRow> allObjects = getObjects();
			for(DailyLectureBlockRow object:allObjects) {
				if(!object.isSeparator() && isAncestor(blockRow, object)) {
					blocks.add(object.getLectureBlock());
				}
			}
		} else if(blockRow.getLectureBlock() != null) {
			blocks.add(blockRow.getLectureBlock());
		}
		return blocks;
	}
	
	private final boolean isAncestor(FlexiTreeTableNode ancestor, FlexiTreeTableNode node) {
		for(FlexiTreeTableNode parent=node.getParent(); parent != null; parent=parent.getParent()) {
			if(ancestor.equals(parent)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		DailyLectureBlockRow block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(DailyLectureBlockRow row, int col) {
		if(row.isSeparator()) {
			switch(BlockCols.values()[col]) {
				case times: return row.getLectureBlock();
				case tools: return row.getTools();
				case details: return Boolean.valueOf(dailyRecordingEnabled);
				default: return null;
			}
		}
		
		switch(BlockCols.values()[col]) {
			case times: return row.getLectureBlock();
			case externalRef: return row.getEntryExternalRef();
			case entry: return row.getEntryDisplayname();
			case lectureBlock: return row.getLectureBlock().getTitle();
			case location: return row.getLectureBlock().getLocation();
			case numOfParticipants: return row.getNumOfParticipants();
			case numOfPresences: return row.getNumOfPresences();
			case numOfAbsences: return row.getNumOfAbsences();
			case warnings:
			case alerts: return row.getLectureBlockBlockStatistics();
			case details: return allowDetails(row);
			case tools: return row.getTools();
			default: return "ERROR";
		}
	}
	
	private boolean allowDetails(DailyLectureBlockRow row) {
		Date end = row.getLectureBlock().getEndDate();
		Date start = row.getLectureBlock().getStartDate();
		Date now = new Date();
		return end.before(new Date()) || (row.isIamTeacher() && start.compareTo(now) <= 0);
	}
	
	public enum BlockCols implements FlexiSortableColumnDef {
		externalRef("table.header.external.ref"),
		times("table.header.times"),
		entry("table.header.entry"),
		lectureBlock("table.header.lecture.block"),
		location("table.header.location"),
		numOfParticipants("table.header.num.participants"),
		numOfPresences("table.header.num.presences"),
		numOfAbsences("table.header.num.absences"),
		warnings("table.header.absences.warning"),
		alerts("table.header.absences.alert"),
		details("table.header.details"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private BlockCols(String i18nKey) {
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
