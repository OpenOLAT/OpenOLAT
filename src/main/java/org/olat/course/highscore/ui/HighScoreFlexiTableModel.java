package org.olat.course.highscore.ui;

import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;

public class HighScoreFlexiTableModel implements TableDataModel<HighScoreTableEntry> {
	
	private final int COLUMN_COUNT = 3;
	private final int ROW_COUNT;
	private final List<HighScoreTableEntry> entries;

	public HighScoreFlexiTableModel(List<HighScoreTableEntry> entries) {
		this.ROW_COUNT = entries.size();
		this.entries = entries;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return ROW_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		HighScoreTableEntry entry = entries.get(row);
		switch(col) {
			case 0: return entry.getRank();
			case 1: return entry.getScore();
			case 2: return entry.getName();
			default: return entry;
		}
	}

	@Override
	public HighScoreTableEntry getObject(int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObjects(List objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object createCopyWithEmptyList() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
