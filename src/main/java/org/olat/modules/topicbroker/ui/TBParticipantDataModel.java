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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 7 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantDataModel extends DefaultFlexiTableDataModel<TBParticipantRow>
	implements SortableFlexiTableDataModel<TBParticipantRow> {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = TBParticipantDataModel.class.getCanonicalName();
	private static final TBParticipantCols[] COLS = TBParticipantCols.values();
	
	private final Locale locale;
	private final int colFirstname;
	private final int colLastname;
	
	public TBParticipantDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		this(columnModel, locale, -1, -1);
	}
	
	public TBParticipantDataModel(FlexiTableColumnModel columnModel, Locale locale, int colFirstname, int colLastname) {
		super(columnModel);
		this.locale = locale;
		this.colFirstname = colFirstname;
		this.colLastname = colLastname;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<TBParticipantRow> views = new TBParticipantRowSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TBParticipantRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(TBParticipantRow row, int col) {
		if(col >= 0 && col < TBParticipantCols.values().length) {
			switch(COLS[col]) {
				case boost: return row.getBoost();
				case prioritySortOrder: return row.getPrioritySortOrder();
				case enrolled: return row.getEnrolledString();
				case waitingList: return row.getMaxSelections() > row.getWaitingList() ? row.getWaitingList() : row.getMaxSelections();
				case selected: return row.getMaxSelections() > row.getNumSelections() ? row.getNumSelections() : row.getMaxSelections();
				case priority: return row;
				case enroll: return Boolean.valueOf(row.isAnonym());
				case withdraw: return Boolean.valueOf(row.isAnonym());
				case tools: return row.getToolsLink();
				default: return "ERROR";
			}
		}
		
		if (col == colFirstname || col == colLastname) {
			return row;
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum TBParticipantCols implements FlexiSortableColumnDef {
		boost("participant.boost"),
		prioritySortOrder("selection.priority"),
		enrolled("selection.status.enrolled"),
		waitingList("selection.status.waiting.list"),
		selected("selection.status.selected"),
		priority("selection.priority"),
		enroll("enroll"),
		withdraw("withdraw"),
		tools("action.more");
		
		private final String i18nKey;

		private TBParticipantCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return this != priority;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}