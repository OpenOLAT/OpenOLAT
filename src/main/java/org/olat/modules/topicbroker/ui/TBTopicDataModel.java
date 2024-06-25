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
package org.olat.modules.topicbroker.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicDataModel extends DefaultFlexiTableDataModel<TBTopicRow> {
	
	private static final TopicCols[] COLS = TopicCols.values();

	public TBTopicDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	public TBTopicRow getObjectByKey(Long key) {
		List<TBTopicRow> rows = getObjects();
		for (TBTopicRow row: rows) {
			if (row != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		TBTopicRow project = getObject(row);
		return getValueAt(project, col);
	}

	public Object getValueAt(TBTopicRow row, int col) {
		switch(COLS[col]) {
		case identifier: return row.getIdentifier();
		case title: return row.getTitle();
		case minParticipants: return row.getMinParticipants();
		case maxParticipants: return row.getMaxParticipants();
		case enrolled: return row.getEnrolledString();
		case waitingList: return row.getWaitingListString();
		case groupRestrictions: return row.getGroupRestrictions();
		case createdBy: return row.getCreatedByDisplayname();
		case upDown: return row.getUpDown();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}
	
	public enum TopicCols implements FlexiColumnDef {
		identifier("topic.identifier"),
		title("topic.title"),
		minParticipants("topic.participants.min"),
		maxParticipants("topic.participants.max"),
		enrolled("selection.status.enrolled"),
		waitingList("selection.status.waiting.list"),
		groupRestrictions("topic.group.restrictions"),
		createdBy("created.by"),
		upDown("updown"),
		tools("tools");
		
		private final String i18nKey;
		
		private TopicCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
