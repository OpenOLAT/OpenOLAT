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
package org.olat.ims.qti21.ui.assessment;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityRow;
import org.olat.modules.lecture.ui.ParticipantListRepositoryController;

/**
 * 
 * Initial date: 28 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityTableModel extends DefaultFlexiTableDataModel<CorrectionIdentityRow>
implements SortableFlexiTableDataModel<CorrectionIdentityRow> {
	
	private final Locale locale;
	
	public CorrectionIdentityTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CorrectionIdentityRow> rows = new CorrectionIdentityTableSort(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CorrectionIdentityRow identRow = getObject(row);
		return getValueAt(identRow, col);
	}

	@Override
	public Object getValueAt(CorrectionIdentityRow row, int col) {
		if(col < CorrectionIdentityListController.USER_PROPS_OFFSET) {
			switch(IdentityCols.values()[col]) {
				case user: return row.getUser();
				case score: return row.getCandidateSession().getFinalScore();
				case answered: return row.getNumAnswered();
				case notAnswered: return row.getNumNotAnswered();
				case autoCorrected:
				case corrected:
				case notCorrected: return row;
				case toReview: return row.getNumToReview();
				default: return "ERROR";
			}
		}
		
		int propPos = col - ParticipantListRepositoryController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum IdentityCols implements FlexiSortableColumnDef {
		user("table.header.user"),
		score("table.header.score"),
		answered("table.header.answered"),
		notAnswered("table.header.notAnswered"),
		autoCorrected("table.header.autoCorrected"),
		corrected("table.header.corrected"),
		toReview("table.header.to.review"),
		notCorrected("table.header.not.corrected");
		
		
		private final String i18n;
		
		private IdentityCols(String i18n) {
			this.i18n = i18n;
		}

		@Override
		public String i18nHeaderKey() {
			return i18n;
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
