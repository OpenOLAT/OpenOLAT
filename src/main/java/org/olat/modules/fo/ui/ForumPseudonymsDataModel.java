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
package org.olat.modules.fo.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.fo.model.PseudonymStatistics;

/**
 * 
 * Initial date: 19 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumPseudonymsDataModel extends DefaultFlexiTableDataModel<PseudonymStatistics>
	implements SortableFlexiTableDataModel<PseudonymStatistics> {
	
	public ForumPseudonymsDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<PseudonymStatistics> sorter = new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<PseudonymStatistics> views = sorter.sort();
		setObjects(views);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		PseudonymStatistics stats = getObject(row);
		return getValueAt(stats, col);
	}

	@Override
	public Object getValueAt(PseudonymStatistics row, int col) {
		switch(PseudoCols.values()[col]) {
			case pseudonym: return row.getPseudonym();
			case creationDate: return row.getCreationDate();
			case numOfMessages: return row.getNumOfMessages();
			default: return "ERROR";
		}
	}
	
	public enum PseudoCols implements FlexiSortableColumnDef {
		pseudonym("table.header.typeimg"),
		creationDate("table.thread"),
		numOfMessages("table.userfriendlyname");
		
		private final String i18nKey;
	
		private PseudoCols(String i18nKey) {
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
