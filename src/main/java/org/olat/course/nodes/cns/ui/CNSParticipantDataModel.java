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
package org.olat.course.nodes.cns.ui;

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
 * Initial date: 22 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSParticipantDataModel extends DefaultFlexiTableDataModel<CNSParticipantRow>
	implements SortableFlexiTableDataModel<CNSParticipantRow> {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = CNSParticipantDataModel.class.getCanonicalName();
	static final CNSParticipantCols[] COLS = CNSParticipantCols.values();
	
	private final Locale locale;
	private final int colFirstname;
	private final int colLastname;
	
	public CNSParticipantDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		this(columnModel, locale, -1, -1);
	}
	
	public CNSParticipantDataModel(FlexiTableColumnModel columnModel, Locale locale, int colFirstname, int colLastname) {
		super(columnModel);
		this.locale = locale;
		this.colFirstname = colFirstname;
		this.colLastname = colLastname;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CNSParticipantRow> views = new CNSParticipantRowSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CNSParticipantRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(CNSParticipantRow row, int col) {
		if(col >= 0 && col < CNSParticipantCols.values().length) {
			switch(COLS[col]) {
				case selected: return row.getSelected();
				default: return "ERROR";
			}
		}
		
		if (col == colFirstname || col == colLastname) {
			return row;
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum CNSParticipantCols implements FlexiSortableColumnDef {
		selected("participant.selected");
		
		private final String i18nKey;

		private CNSParticipantCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}