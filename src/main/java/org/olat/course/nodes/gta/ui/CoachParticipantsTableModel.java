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
package org.olat.course.nodes.gta.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.nodes.gta.ui.component.SubmissionDateCellRenderer;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachParticipantsTableModel extends DefaultFlexiTableDataModel<CoachedIdentityRow> implements SortableFlexiTableDataModel<CoachedIdentityRow> {
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public CoachParticipantsTableModel(List<UserPropertyHandler> userPropertyHandlers, Locale locale,
			FlexiTableColumnModel columnModel) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public DefaultFlexiTableDataModel<CoachedIdentityRow> createCopyWithEmptyList() {
		return new CoachParticipantsTableModel(userPropertyHandlers, locale, getTableColumnModel());
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CoachedIdentityRow> views = new CoachParticipantsModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CoachedIdentityRow participant = getObject(row);
		return getValueAt(participant, col);
	}
	
	@Override
	public Object getValueAt(CoachedIdentityRow row, int col) {
		if(col == CGCols.mark.ordinal()) {
			return row.getMarkLink();
		} else if(col == CGCols.username.ordinal()) {
			return row.getIdentity().getIdentityName();
		} else if(col == CGCols.taskStatus.ordinal()) {
			return row.getTaskStatus();
		} else if(col == CGCols.taskName.ordinal()) {
			return row.getTaskName();
		} else if(col == CGCols.submissionDate.ordinal()) {
			return SubmissionDateCellRenderer.cascading(row);
		} else if(col == CGCols.userVisibility.ordinal()) {
			return row.getUserVisibility();
		} else if(col == CGCols.score.ordinal()) {
			return row.getScore();
		} else if(col == CGCols.passed.ordinal()) {
			return row.getPassed();
		} else if(col == CGCols.numOfSubmissionDocs.ordinal()) {
			if(row.getCollectionDate() != null) {
				return row.getNumOfCollectedDocs();
			}
			return row.getNumOfSubmissionDocs();
		} else if(col >= GTACoachedGroupGradingController.USER_PROPS_OFFSET) {
			int propIndex = col - GTACoachedGroupGradingController.USER_PROPS_OFFSET;
			return row.getIdentity().getIdentityProp(propIndex);
		}
		return "ERROR";
	}
	
	public enum CGCols implements FlexiSortableColumnDef {
		mark("table.header.mark"),
		username("username"),
		taskName("table.header.group.taskName"),
		taskStatus("table.header.group.step"),
		submissionDate("table.header.submissionDate"),
		userVisibility("table.header.userVisibility"),
		score("table.header.score"),
		passed("table.header.passed"),
		numOfSubmissionDocs("table.header.num.submissionDocs");
		
		private final String i18nKey;
		
		private CGCols(String i18nKey) {
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
