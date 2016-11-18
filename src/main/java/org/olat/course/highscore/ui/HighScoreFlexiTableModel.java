/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.highscore.ui;
/**
 * Initial Date:  5.08.2016 <br>
 * @author fkiefer
 */
import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.Identity;

public class HighScoreFlexiTableModel implements TableDataModel<HighScoreTableEntry> {
	
	private final int COLUMN_COUNT = 3;
	private final int ROW_COUNT;
	private final String placeholder;
	private final List<HighScoreTableEntry> entries;
	private final boolean anonymous;
	private Identity ownId;

	public HighScoreFlexiTableModel(List<HighScoreTableEntry> entries, boolean anonymous, String placeholder,
			Identity ownId) {
		this.ROW_COUNT = entries.size();
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
		return ROW_COUNT;
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
	public void setObjects(List objects) {
		
	}

	@Override
	public Object createCopyWithEmptyList() {
		return null;
	}
	

}
