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
package org.olat.course.highscore.ui;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.Identity;
/**
 * Initial Date:  5.08.2016 <br>
 * @author fkiefer
 */
public class HighScoreFlexiTableModel implements TableDataModel<HighScoreTableEntry> {
	
	private final int COLUMN_COUNT = 3;
	private final String placeholder;
	private List<HighScoreTableEntry> entries;
	private final boolean anonymous;
	private Identity ownId;

	public HighScoreFlexiTableModel(List<HighScoreTableEntry> entries, boolean anonymous, String placeholder,
			Identity ownId) {
		this.entries = entries;
		this.anonymous = anonymous;
		this.placeholder = placeholder;
		this.ownId = ownId;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return entries == null ? 0 : entries.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		HighScoreTableEntry entry = entries.get(row);
		switch(col) {
			case 0: return entry.getRank();
			case 1: return entry.getScore();
			case 2: return anonymous && !entry.getIdentity().equals(ownId) ? placeholder : entry.getName();
			default: return entry;
		}
	}

	@Override
	public HighScoreTableEntry getObject(int row) {
		return null;
	}

	@Override
	public void setObjects(List<HighScoreTableEntry> entries) {
		this.entries = entries;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new HighScoreFlexiTableModel(new ArrayList<>(), anonymous,placeholder,ownId);
	}
	

}
