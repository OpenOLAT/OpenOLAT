package org.olat.modules.curriculum.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 9 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementLectureBlocksTableModel extends DefaultFlexiTableDataModel<CurriculumElementLectureBlockRow>
implements SortableFlexiTableDataModel<CurriculumElementLectureBlockRow> {

	private static final BlockCols[] COLS = BlockCols.values();
	
	private final Locale locale;
	
	public CurriculumElementLectureBlocksTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumElementLectureBlockRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementLectureBlockRow blockRow = getObject(row);
		return getValueAt(blockRow, col);
	}

	@Override
	public Object getValueAt(CurriculumElementLectureBlockRow row, int col) {
		final LectureBlock lectureBlock = row.getLectureBlock();
		return switch(COLS[col]) {
			case key -> lectureBlock.getKey();
			case externalId -> lectureBlock.getExternalId();
			case title -> lectureBlock.getTitle();
			case location -> lectureBlock.getLocation();
			case date -> lectureBlock.getStartDate();
			case startTime -> lectureBlock.getStartDate();
			case endTime -> lectureBlock.getEndDate();
			case status -> lectureBlock;
			case tools -> row.getToolsLink();
			default -> "ERROR";
		};
	}
	
	public enum BlockCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		externalId("table.header.external.id"),
		title("table.header.title"),
		location("table.header.location"),
		date("table.header.date"),
		startTime("table.header.start.time"),
		endTime("table.header.end.time"),
		status("table.header.status"),
		tools("action.more")
		;
		
		private final String i18nHeaderKey;
		
		private BlockCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
