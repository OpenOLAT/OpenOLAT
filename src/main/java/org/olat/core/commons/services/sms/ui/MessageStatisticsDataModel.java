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
package org.olat.core.commons.services.sms.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.sms.model.MessageStatistics;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessageStatisticsDataModel extends DefaultFlexiTableDataModel<MessageStatistics>
	implements SortableFlexiTableDataModel<MessageStatistics> {
	
	private final Locale locale;
		
	public MessageStatisticsDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<MessageStatistics> sorter = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			List<MessageStatistics> stats = sorter.sort();
			super.setObjects(stats);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		MessageStatistics infos = getObject(row);
		return getValueAt(infos, col);
	}

	@Override
	public Object getValueAt(MessageStatistics row, int col) {
		switch(MLogStatsCols.values()[col]) {
			case year:
			case month: return row.getDate();
			case numOfMessages: return row.getNumOfMessages();
			default: return null;
		}
	}
	
	public enum MLogStatsCols implements FlexiSortableColumnDef {
		year("table.header.year"),
		month("table.header.month"),
		numOfMessages("table.header.numOfMessages");
		
		private final String i18nKey;
		
		private MLogStatsCols(String i18nKey) {
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